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

import static jdplus.toolkit.base.core.math.matrices.SymmetricMatrix.LtL;
import static jdplus.toolkit.base.core.math.matrices.SymmetricMatrix.lcholesky;

import java.util.function.Function;
import jdplus.advancedsa.base.api.tdarima.LtdSarimaSpec;
import jdplus.advancedsa.base.api.tdarima.LtdSpec;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.api.timeseries.regression.ResidualsType;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.Chi2;
import jdplus.toolkit.base.core.dstats.F;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.matrices.MatrixFactory;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
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
import jdplus.toolkit.base.core.stats.tests.TestsUtility;
import org.jspecify.annotations.Nullable;

/**
 * @author Jean Palate
 */
public class LtdKernel {

    private final LtdSpec spec;

    public static LtdKernel of(LtdSpec spec) {
        return new LtdKernel(spec);
    }

    //    private DoubleSeq curP, curDelta;
    //    private FastMatrix curPCov, curDeltaCov;
    //
    /**
     * Raw estimation
     *
     * @param s variable
     * @param mean
     * @param X regression matrix. Can be null
     * @param modelSpec
     * @return
     */
    public LtdResults process(DoubleSeq s, boolean mean, @Nullable FastMatrix X, LtdSarimaSpec modelSpec) {
        //        clear();
        LtdResults.Builder builder = LtdResults.builder();

        SarimaResults initialModel = initialModel(s, mean, X, modelSpec);
        builder.start(initialModel);

        LtdSarimaSpec curSpec = modelSpec;
        do {
            LtdSarimaResults step = step(curSpec, initialModel);
            builder.ltdResult(step);
            curSpec = nextSpec(step);
        } while (curSpec != null);

        return builder.build();
    }

    //    private void clear(){
    //        curP=null;
    //        curDelta=null;
    //        curPCov=null;
    //        curDeltaCov=null;
    //    }
    //
    private SarimaResults initialModel(DoubleSeq s, boolean mean, @Nullable FastMatrix X, LtdSarimaSpec modelSpec) {
        SarimaOrders orders = modelSpec.getOrders().asSarimaOrders();

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
        TsResiduals res0 = residuals(e, orders.getPeriod(), ll0, mapping.getDim(), ResidualsType.QR_Transformed);

        SarimaResults.Builder builder = SarimaResults.builder();
        builder.model(initial.getModel())
                .parameters(initial.getMax().getParameters())
                .parametersCovariance(covariance(initial.getMax().getInformation(), false))
                .likelihood(ll0)
                .residuals(res0)
                .coefficients(coefficients0)
                .covariance(covariance0);

        if (X != null && !X.isEmpty()) {
            DataBlock regs0 = DataBlock.make(s.length());
            regs0.product(
                    regarima.variables().rowsIterator(), DataBlock.of(mean ? coefficients0.drop(0, 1) : coefficients0));
            builder.regsEffect(regs0);
            builder.linearizedSeries(DoublesMath.subtract(s, regs0));

        } else {
            builder.regsEffect(DoubleSeq.empty());
            builder.linearizedSeries(s);
        }
        return builder.build();
    }

    private LtdSarimaResults step(LtdSarimaSpec curSpec, SarimaResults initialModel) {
        if (!curSpec.isTimeVarying()) {
            return LtdSarimaResults.of(initialModel);
        }
        RegArimaModel<SarimaModel> model = initialModel.getModel();
        RegArmaModel<SarimaModel> dmodel = model.differencedModel();
        SarmaOrders storders = model.arima().orders().doStationary();
        int n = dmodel.getY().length();

        LtdSarimaResults.Builder builder = LtdSarimaResults.builder();

        LtdSarimaMapping ltdmapping = stmapping(curSpec, n);
        SsfFunction<LtdSarimaModel, Ssf> fn = SsfFunction.<LtdSarimaModel, Ssf>builder(
                        new SsfData(dmodel.getY()), ltdmapping, lmodel -> lmodel.ssf())
                .regression(dmodel.getX().isEmpty() ? null : dmodel.getX(), 0)
                .useScalingFactor(true)
                .useLog(false)
                .useParallelProcessing(true)
                .useFastAlgorithm(false)
                .build();
        int dim = ltdmapping.getDim();

        LtdSarimaModel m0 = LtdSarimaModel.of(
                LtdSarimaSpec.Orders.of(storders), dmodel.getArma().parameters(), n);
        DoubleSeq p = ltdmapping.parametersOf(m0);
        SsfFunctionPoint<LtdSarimaModel, Ssf> pt = fn.evaluate(p);
        LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer.builder()
                .functionPrecision(spec.getPrecision())
                .build();
        min.minimize(pt);
        pt = (SsfFunctionPoint<LtdSarimaModel, Ssf>) min.getResult();

        DiffuseConcentratedLikelihood likelihood = pt.getLikelihood();

        DoubleSeq coefficients1 = likelihood.coefficients();
        FastMatrix covariance1 = likelihood.covariance(dim, true);

        if (model.getXCount() > 0) {
            DataBlock regs = DataBlock.make(model.getObservationsCount());
            regs.product(
                    model.variables().rowsIterator(),
                    DataBlock.of(model.isMean() ? coefficients1.drop(0, 1) : coefficients1));
            builder.regsEffect(regs);
            builder.linearizedSeries(DoublesMath.subtract(model.getY(), regs));

        } else {
            builder.regsEffect(DoubleSeq.empty());
            builder.linearizedSeries(model.getY());
        }

        LikelihoodStatistics ll = LikelihoodStatistics.statistics(
                        likelihood.logLikelihood(), model.getObservationsCount() - model.getMissingValuesCount())
                .llAdjustment(0)
                .differencingOrder(model.arima().getNonStationaryArOrder())
                //
                // .diffuseOrder(likelihood.ndiffuse()+likelihood.ndiffuseRegressors())
                .parametersCount(dim + model.getVariablesCount() + 1)
                .ssq(likelihood.ssq())
                .build();
        DoubleSeq e = likelihood.e();
        TsResiduals res = residuals(e, storders.getPeriod(), ll, dim, ResidualsType.OneStepAHead);
        LogLikelihoodFunction<LtdSarimaModel, DiffuseConcentratedLikelihood> fll =
                concentratedLogLikelihoodFunction(dmodel, curSpec);
        //        LogLikelihoodFunction.Point max = new LogLikelihoodFunction.Point(fll,
        // pt.getParameters(), DoubleSeq.of(gradient), hessian);
        LogLikelihoodFunction.Point<LtdSarimaModel, DiffuseConcentratedLikelihood> max = fll.point(pt.getParameters());
        FastMatrix pcovariance = covariance(max.getInformation(), true);
        LtdSarimaMapping.ParametersDetails pdetails =
                new LtdSarimaMapping.ParametersDetails(curSpec, spec.getParametrization());
        LtdSarimaMapping.Parameters parameters = new LtdSarimaMapping.Parameters(
                max.getParameters(), pcovariance, n, pdetails, spec.getParametrization());
        StatisticalTest test = null;
        if (pcovariance != null) {
            if (spec.getParametrization() == LtdSpec.Parametrization.MEAN_DELTA) {
                test = stationarityTest(
                        max.getParameters().drop(pdetails.n0, 0),
                        pcovariance.extract(pdetails.n0, pdetails.n1, pdetails.n0, pdetails.n1),
                        null,
                        likelihood.degreesOfFreedom());
            } else {
                int[] sel = new int[pdetails.n1];
                for (int i = 0, j = 1; i < pdetails.nv; ++i, j += 2) {
                    sel[i] = j;
                }
                if (curSpec.isVVar()) {
                    sel[pdetails.nv] = 2 * pdetails.nv;
                }
                test = stationarityTest(
                        DoubleSeq.of(parameters.dval), parameters.dcov, sel, likelihood.degreesOfFreedom());
            }
        }
        StatisticalTest lrtest = TestsUtility.testOf(
                2 * (likelihood.logLikelihood() - initialModel.getLikelihood().getLogLikelihood()),
                new Chi2(pdetails.n1),
                TestType.Upper);
        builder.model((LtdSarimaModel) pt.getCore())
                .coefficients(coefficients1)
                .covariance(covariance1)
                .residuals(res)
                .likelihood(ll)
                .parameters(DoubleSeq.of(parameters.val))
                .parametersCovariance(parameters.cov)
                .parametersNames(pdetails.pnames)
                .derivedParameters(DoubleSeq.of(parameters.dval))
                .derivedParametersCovariance(parameters.dcov)
                .derivedParametersNames(pdetails.derivedpnames)
                .stationarityTest(test)
                .likelihoodRatioTest(lrtest);
        return builder.build();
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

    private LtdKernel(LtdSpec spec) {
        this.spec = spec;
    }

    private LogLikelihoodFunction<LtdSarimaModel, DiffuseConcentratedLikelihood> concentratedLogLikelihoodFunction(
            RegArmaModel<SarimaModel> dmodel, LtdSarimaSpec curSpec) {
        Function<LtdSarimaModel, ConcentratedLikelihood> lfn = m -> {
            int n = dmodel.getY().length();

            LtdSarimaMapping ltdmapping = stmapping(curSpec, n);
            SsfFunction<LtdSarimaModel, Ssf> fn = SsfFunction.<LtdSarimaModel, Ssf>builder(
                            new SsfData(dmodel.getY()), ltdmapping, lmodel -> lmodel.ssf())
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

        LtdSarimaMapping ltdmapping = mapping(curSpec, dmodel.getY().length());

        return new LogLikelihoodFunction(ltdmapping, lfn);
    }

    private LtdSarimaMapping stmapping(LtdSarimaSpec curspec, int n) {
        LtdSarimaSpec.Orders orders = curspec.getOrders().doStationary();
        return mapping(curspec.withOrders(orders), n, spec.getParametrization());
    }

    private LtdSarimaMapping mapping(LtdSarimaSpec curspec, int n) {
        return mapping(curspec, n, spec.getParametrization());
    }

    private LtdSarimaMapping mapping(LtdSarimaSpec curspec, int n, LtdSpec.Parametrization parametrization) {
        LtdSarimaMapping ltdmapping = null;
        LtdSarimaSpec.Orders orders = curspec.getOrders();
        if (parametrization == LtdSpec.Parametrization.MEAN_DELTA) {
            ltdmapping = LtdSarimaMapping1.builder(orders)
                    .n(n)
                    .vPhi(curspec.isVPhi())
                    .vBphi(curspec.isVBphi())
                    .vTheta(curspec.isVTheta())
                    .vBtheta(curspec.isVBtheta())
                    .vVar(curspec.isVVar())
                    .build();
        } else if (parametrization == LtdSpec.Parametrization.START_END) {
            ltdmapping = LtdSarimaMapping2.builder(orders)
                    .n(n)
                    .vPhi(curspec.isVPhi())
                    .vBphi(curspec.isVBphi())
                    .vTheta(curspec.isVTheta())
                    .vBtheta(curspec.isVBtheta())
                    .vVar(curspec.isVVar())
                    .build();
        }
        return ltdmapping;
    }

    public static FastMatrix covariance(FastMatrix H, boolean nullIfFailed) {
        try {
            FastMatrix lower = H.deepClone();
            lcholesky(lower);
            lower = LowerTriangularMatrix.inverse(lower);
            return LtL(lower);
        } catch (MatrixException e) {
            //                DoubleSeq diag = H.diagonal();
            //                return FastMatrix.diagonal(DoubleSeq.onMapping(diag.length(),
            // i->1/Math.abs(diag.get(i))));
            if (nullIfFailed) {
                return null;
            } else {
                FastMatrix I = FastMatrix.square(H.getRowsCount());
                I.set(Double.NaN);
                return I;
            }
        }
    }

    private StatisticalTest stationarityTest(DoubleSeq z, FastMatrix cov, int[] sel, int df) {
        FastMatrix V;
        DataBlock Z;
        if (sel == null) {
            V = cov.deepClone();
            Z = DataBlock.of(z);
        } else {
            V = MatrixFactory.select(cov, sel, sel);
            Z = DataBlock.of(sel.length, i -> z.get(sel[i]));
        }
        SymmetricMatrix.lcholesky(V);
        LowerTriangularMatrix.solveLx(V, Z);
        double f = (Z.ssq() / Z.length());
        F fdist = new F(Z.length(), df);
        return TestsUtility.testOf(f, fdist, TestType.Upper);
    }

    private LtdSarimaSpec nextSpec(LtdSarimaResults step) {
        LtdSarimaModel model = step.getModel();
        if (model == null || !model.isTimeVarying() || spec.getSimplificationThreshold() == 0) {
            return null;
        }
        LtdSarimaSpec curSpec = model.spec();

        int idx;
        int df = step.getLikelihood().getEffectiveObservationsCount()
                - step.getLikelihood().getEstimatedParametersCount();
        if (spec.getParametrization() == LtdSpec.Parametrization.MEAN_DELTA) {
            idx = maxPValue(curSpec, step.getParameters(), step.getParametersCovariance(), df);
        } else {
            idx = maxPValue(curSpec, step.getDerivedParameters(), step.getDerivedParametersCovariance(), df);
        }
        if (idx < 0) {
            return null;
        }
        LtdSarimaSpec.Builder builder = curSpec.toBuilder();
        switch (idx) {
            case 0 -> builder.vPhi(false);
            case 1 -> builder.vBphi(false);
            case 2 -> builder.vTheta(false);
            case 3 -> builder.vBtheta(false);
            case 4 -> builder.vVar(false);
        }
        return builder.build();
    }

    private int maxPValue(LtdSarimaSpec modelSpec, DoubleSeq parameters, Matrix parametersCovariance, int df) {
        int imax = -1;
        double pmax = 0;
        LtdSarimaSpec.Orders orders = modelSpec.getOrders();
        FastMatrix pcov = FastMatrix.of(parametersCovariance);
        int n = 0;
        int p = orders.getP();
        if (p > 0) {
            if (modelSpec.isVPhi()) {
                int[] idx = new int[p];
                for (int i = 0; i < p; ++i, n += 2) {
                    idx[i] = n + 1;
                }
                StatisticalTest test = stationarityTest(parameters, pcov, idx, df);
                if (test.isValid()) {
                    double v = test.getPvalue();
                    if (v > pmax && Double.isFinite(v)) {
                        pmax = v;
                        imax = 0;
                    }
                }
            } else {
                n += p;
            }
        }
        p = orders.getBp();
        if (p > 0) {
            if (modelSpec.isVBphi()) {
                int[] idx = new int[p];
                for (int i = 0; i < p; ++i, n += 2) {
                    idx[i] = n + 1;
                }
                StatisticalTest test = stationarityTest(parameters, pcov, idx, df);
                if (test.isValid()) {
                    double v = test.getPvalue();
                    if (v > pmax && Double.isFinite(v)) {
                        pmax = v;
                        imax = 1;
                    }
                }
            } else {
                n += p;
            }
        }
        p = orders.getQ();
        if (p > 0) {
            if (modelSpec.isVTheta()) {
                int[] idx = new int[p];
                for (int i = 0; i < p; ++i, n += 2) {
                    idx[i] = n + 1;
                }
                StatisticalTest test = stationarityTest(parameters, pcov, idx, df);
                if (test.isValid()) {
                    double v = test.getPvalue();
                    if (v > pmax && Double.isFinite(v)) {
                        pmax = v;
                        imax = 2;
                    }
                }
            } else {
                n += p;
            }
        }
        p = orders.getBq();
        if (p > 0) {
            if (modelSpec.isVBtheta()) {
                int[] idx = new int[p];
                for (int i = 0; i < p; ++i, n += 2) {
                    idx[i] = n + 1;
                }
                StatisticalTest test = stationarityTest(parameters, pcov, idx, df);
                if (test.isValid()) {
                    double v = test.getPvalue();
                    if (v > pmax && Double.isFinite(v)) {
                        pmax = v;
                        imax = 3;
                    }
                }
            } else {
                n += p;
            }
        }
        if (modelSpec.isVVar()) {
            StatisticalTest test = stationarityTest(
                    DoubleSeq.of(parameters.get(n) - 1),
                    FastMatrix.of(parametersCovariance.extract(n, 1, n, 1)),
                    null,
                    df);
            if (test.isValid()) {
                double v = test.getPvalue();
                if (v > pmax && Double.isFinite(v)) {
                    pmax = v;
                    imax = 4;
                }
            }
        }
        if (pmax > spec.getSimplificationThreshold()) {
            return imax;
        } else {
            return -1;
        }
    }
}
