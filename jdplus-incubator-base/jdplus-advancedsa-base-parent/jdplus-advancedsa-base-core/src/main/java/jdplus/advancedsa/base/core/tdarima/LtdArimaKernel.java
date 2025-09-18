/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.advancedsa.base.core.tdarima;

import jdplus.advancedsa.base.api.tdarima.LtdArimaSpec;
import java.util.function.Function;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.api.timeseries.regression.ResidualsType;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import static jdplus.toolkit.base.core.math.matrices.SymmetricMatrix.LtL;
import static jdplus.toolkit.base.core.math.matrices.SymmetricMatrix.lcholesky;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArmaModel;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
import jdplus.toolkit.base.core.stats.tests.NiidTests;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Jean Palate
 */
public class LtdArimaKernel {

    private final LtdArimaSpec spec;

    public static LtdArimaKernel of(LtdArimaSpec spec) {
        return new LtdArimaKernel(spec);
    }

    /**
     * Raw estimation
     *
     * @param s variable
     * @param period periodicity
     * @param mean
     * @param X regression matrix. Can be null
     * @return
     */
    public LtdArimaResults process(DoubleSeq s, int period, boolean mean, @Nullable FastMatrix X) {

        SarimaOrders orders = spec.getSarimaSpec()
                .withPeriod(period)
                .orders();

        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder()
                .y(s)
                .meanCorrection(mean)
                .arima(SarimaModel.builder(orders).build())
                .addX(X)
                .build();

        SarimaMapping mapping = SarimaMapping.of(orders);
        RegArimaEstimation<SarimaModel> initial = RegSarimaComputer.PROCESSOR.process(regarima, mapping);

        ConcentratedLikelihoodWithMissing ll = initial.getConcentratedLikelihood();
        DoubleSeq coefficients0 = ll.coefficients();
        FastMatrix covariance0 = ll.covariance(mapping.getDim(), true);
        LikelihoodStatistics ll0 = initial.statistics();

        DoubleSeq e = ll.e();
        TsResiduals res0 = residuals(e, period, ll0, mapping.getDim(), ResidualsType.QR_Transformed);

        LtdArimaResults.SarimaResults.Builder builder0 = LtdArimaResults.SarimaResults.builder();
        LtdArimaResults.LtdResults.Builder builder1 = LtdArimaResults.LtdResults.builder();
        builder0.model(initial.getModel().arima())
                .max(initial.getMax())
                .ll(ll0)
                .residuals(res0)
                .coefficients(coefficients0)
                .covariance(covariance0);

        RegArimaModel<SarimaModel> model = initial.getModel();
        RegArmaModel<SarimaModel> dmodel = model.differencedModel();
        SarmaOrders storders = orders.doStationary();
        int n = dmodel.getY().length();

        LtdArimaMapping ltdmapping = stmapping(storders, n);
        SsfFunction<LtdArimaModel, Ssf> fn = SsfFunction.<LtdArimaModel, Ssf>builder(new SsfData(dmodel.getY()), ltdmapping, lmodel -> lmodel.ssf())
                .regression(dmodel.getX().isEmpty() ? null : dmodel.getX(), 0)
                .useScalingFactor(true)
                .useLog(false)
                .useParallelProcessing(true)
                .useFastAlgorithm(false)
                .build();
        int dim = ltdmapping.getDim();

        LtdArimaModel m0 = LtdArimaModel.of(SarimaOrders.of(storders, 0, 0), dmodel.getArma().parameters(), n);
        DoubleSeq p = ltdmapping.parametersOf(m0);
        SsfFunctionPoint<LtdArimaModel, Ssf> pt = fn.evaluate(p);
        LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer.builder()
                .functionPrecision(spec.getPrecision())
                .build();
        min.minimize(pt);
        pt = (SsfFunctionPoint<LtdArimaModel, Ssf>) min.getResult();

        DiffuseConcentratedLikelihood likelihood = pt.getLikelihood();

        DoubleSeq coefficients1 = likelihood.coefficients();
        FastMatrix covariance1 = likelihood.covariance(dim, true);

        if (X != null && !X.isEmpty()) {
            DataBlock regs0 = DataBlock.make(s.length()), regs1 = DataBlock.make(s.length());
            regs0.product(regarima.variables().rowsIterator(), DataBlock.of(mean ? coefficients0.drop(0, 1) : coefficients0));
            builder0.regsEffect(regs0);
            builder0.linearizedSeries(DoublesMath.subtract(s, regs0));
            regs1.product(regarima.variables().rowsIterator(), DataBlock.of(mean ? coefficients1.drop(0, 1) : coefficients1));
            builder1.regsEffect(regs1);
            builder1.linearizedSeries(DoublesMath.subtract(s, regs1));

        } else {
            builder0.regsEffect(DoubleSeq.empty());
            builder0.linearizedSeries(s);
            builder1.regsEffect(DoubleSeq.empty());
            builder1.linearizedSeries(s);
        }

        LikelihoodStatistics ll1 = LikelihoodStatistics.statistics(likelihood.logLikelihood(), model.getObservationsCount() - model.getMissingValuesCount())
                .llAdjustment(0)
                .differencingOrder(model.arima().getNonStationaryArOrder())
                //                .diffuseOrder(likelihood.ndiffuse()+likelihood.ndiffuseRegressors())
                .parametersCount(dim + model.getVariablesCount() + 1)
                .ssq(likelihood.ssq())
                .build();

        TsResiduals res1 = residuals(e, period, ll1, dim, ResidualsType.OneStepAHead);

//        double[] gradient = min.gradientAtMinimum().toArray();
//        FastMatrix hessian = min.curvatureAtMinimum();
//        double objective = pt.getSsqE();
//        int ndf = likelihood.degreesOfFreedom();
//        hessian.mul((.5 * ndf) / objective);
//        for (int i = 0; i < gradient.length; ++i) {
//            gradient[i] *= (-.5 * ndf) / objective;
//        }

        LogLikelihoodFunction<LtdArimaModel, DiffuseConcentratedLikelihood> fll = concentratedLogLikelihoodFunction(dmodel);
//        LogLikelihoodFunction.Point max = new LogLikelihoodFunction.Point(fll, pt.getParameters(), DoubleSeq.of(gradient), hessian);
        LogLikelihoodFunction.Point<LtdArimaModel, DiffuseConcentratedLikelihood> max = fll.point(pt.getParameters());
        builder1.model((LtdArimaModel) pt.getCore())
                .coefficients(coefficients1)
                .covariance(covariance1)
                .residuals(res1)
                .ll(ll1)
                .max(max);
        return new LtdArimaResults(builder0.build(), builder1.build());
    }

    private TsResiduals residuals(DoubleSeq e, int period, LikelihoodStatistics ll, int nhp, ResidualsType type) {
        NiidTests niid = NiidTests.builder()
                .data(e)
                .period(period)
                .hyperParametersCount(nhp)
                .build();

        return TsResiduals.builder()
                .type(ResidualsType.OneStepAHead)
                .res(e)
                .ssq(ll.getSsqErr())
                .n(ll.getEffectiveObservationsCount())
                .df(ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount() + nhp)
                .dfc(ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount())
                .test(ResidualsDictionaries.MEAN, niid.meanTest())
                .test(ResidualsDictionaries.SKEW, niid.skewness())
                .test(ResidualsDictionaries.KURT, niid.kurtosis())
                .test(ResidualsDictionaries.DH, niid.normalityTest())
                .test(ResidualsDictionaries.LB, niid.ljungBox())
                .test(ResidualsDictionaries.BP, niid.boxPierce())
                .test(ResidualsDictionaries.SEASLB, niid.seasonalLjungBox())
                .test(ResidualsDictionaries.SEASBP, niid.seasonalBoxPierce())
                .test(ResidualsDictionaries.LB2, niid.ljungBoxOnSquare())
                .test(ResidualsDictionaries.BP2, niid.boxPierceOnSquare())
                .test(ResidualsDictionaries.NRUNS, niid.runsNumber())
                .test(ResidualsDictionaries.LRUNS, niid.runsLength())
                .test(ResidualsDictionaries.NUDRUNS, niid.upAndDownRunsNumbber())
                .test(ResidualsDictionaries.LUDRUNS, niid.upAndDownRunsLength())
                .build();

    }

    private LtdArimaKernel(LtdArimaSpec spec) {
        this.spec = spec;
    }

    private LogLikelihoodFunction<LtdArimaModel, DiffuseConcentratedLikelihood>
            concentratedLogLikelihoodFunction(RegArmaModel<SarimaModel> dmodel) {
        Function<LtdArimaModel, ConcentratedLikelihood> lfn = m -> {

            SarmaOrders storders = dmodel.getArma().orders().doStationary();
            int n = dmodel.getY().length();

            LtdArimaMapping ltdmapping = stmapping(storders, n);
            SsfFunction<LtdArimaModel, Ssf> fn = SsfFunction.<LtdArimaModel, Ssf>builder(new SsfData(dmodel.getY()), ltdmapping, lmodel -> lmodel.ssf())
                    .regression(dmodel.getX().isEmpty() ? null : dmodel.getX(), 0)
                    .useScalingFactor(true)
                    .useLog(true)
                    .useParallelProcessing(true)
                    .useFastAlgorithm(false)
                    .useSymmetricNumericalDerivatives(true)
                    .build();
            DoubleSeq p = ltdmapping.parametersOf(m);
            SsfFunctionPoint pt = fn.evaluate(p);
            return pt.getLikelihood();
        };

        LtdArimaMapping ltdmapping = mapping(dmodel.getArma().orders(), dmodel.getY().length());

        return new LogLikelihoodFunction(ltdmapping, lfn);
    }

    private LtdArimaMapping stmapping(SarmaOrders storders, int n) {
        return mapping(SarimaOrders.of(storders, 0, 0), n, spec.getParametrization());
    }

    private LtdArimaMapping stmapping(SarmaOrders storders, int n, LtdArimaSpec.Parametrization parametrization) {
        return mapping(SarimaOrders.of(storders, 0, 0), n, parametrization);
    }

    private LtdArimaMapping mapping(SarimaOrders orders, int n) {
        return mapping(orders, n, spec.getParametrization());
    }

    private LtdArimaMapping mapping(SarimaOrders orders, int n, LtdArimaSpec.Parametrization parametrization) {
        LtdArimaMapping ltdmapping = null;

        if (parametrization == LtdArimaSpec.Parametrization.MEAN_DELTA) {
            ltdmapping = LtdArimaMapping1.builder(orders)
                    .n(n)
                    .vPhi(spec.isVPhi())
                    .vBphi(spec.isVBphi())
                    .vTheta(spec.isVTheta())
                    .vBtheta(spec.isVBtheta())
                    .vVar(spec.isVVar())
                    .build();
        } else if (parametrization == LtdArimaSpec.Parametrization.START_END) {
            ltdmapping = LtdArimaMapping2.builder(orders)
                    .n(n)
                    .vPhi(spec.isVPhi())
                    .vBphi(spec.isVBphi())
                    .vTheta(spec.isVTheta())
                    .vBtheta(spec.isVBtheta())
                    .vVar(spec.isVVar())
                    .build();
        }
        return ltdmapping;
    }

    public static FastMatrix covariance(FastMatrix H) {
        try {
            FastMatrix lower = H.deepClone();
            lcholesky(lower);
            lower = LowerTriangularMatrix.inverse(lower);
            return LtL(lower);
        } catch (MatrixException e) {
            FastMatrix I = FastMatrix.square(H.getRowsCount());
            I.set(Double.NaN);
            return I;
        }
    }

}
