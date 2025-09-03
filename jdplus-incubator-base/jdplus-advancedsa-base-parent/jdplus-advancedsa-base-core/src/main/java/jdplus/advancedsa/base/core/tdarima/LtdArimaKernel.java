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
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArmaModel;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
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

        LtdArimaResults.Builder builder = LtdArimaResults.builder();

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
        

        DoubleSeq coefficients0 = initial.getConcentratedLikelihood().coefficients();
        FastMatrix covariance0 = initial.getConcentratedLikelihood().covariance(mapping.getDim(), true);
        LikelihoodStatistics ll0 = initial.statistics();
        builder.start(initial.getModel().arima())
                .startMax(initial.getMax())
                .ll0(ll0);

        RegArimaModel<SarimaModel> model = initial.getModel();
        RegArmaModel<SarimaModel> dmodel = model.differencedModel();
        SarmaOrders storders = orders.doStationary();
        int n=dmodel.getY().length();
       
        // estimate the stationary model
        LtdArimaMapping ltdmapping = LtdArimaMapping.builder(SarimaOrders.of(storders, 0, 0))
                .n(n)
                .vPhi(spec.isVPhi())
                .vBphi(spec.isVBphi())
                .vTheta(spec.isVTheta())
                .vBtheta(spec.isVBtheta())
                .vVar(spec.isVVar())
                .build();

        SsfFunction fn = SsfFunction.builder(new SsfData(dmodel.getY()), ltdmapping, (LtdArimaModel lmodel) -> lmodel.ssf())
                .regression(dmodel.getX().isEmpty() ? null : dmodel.getX(), 0)
                .useScalingFactor(true)
                .useLog(false)
                .useParallelProcessing(true)
                .build();
        int dim = ltdmapping.getDim();
        DataBlock p = DataBlock.make(dim);
        p.range(0, storders.getParametersCount()).copy(dmodel.getArma().parameters());
        LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer.builder()
                .functionPrecision(spec.getPrecision())
                .build();
        SsfFunctionPoint pstart = fn.evaluate(p);
        min.minimize(pstart);
        SsfFunctionPoint pt = (SsfFunctionPoint) min.getResult();
        DiffuseConcentratedLikelihood likelihood = pt.getLikelihood();

        DoubleSeq coefficients1 = likelihood.coefficients();
        FastMatrix covariance1 = likelihood.covariance(dim, true);

        if (X != null && ! X.isEmpty()){
            DataBlock regs0=DataBlock.make(s.length()), regs1=DataBlock.make(s.length());
            regs0.product(regarima.variables().rowsIterator(), DataBlock.of(mean ? coefficients0.drop(0,1) : coefficients0));
            builder.regsEffect0(regs0);
            builder.linearizedSeries0(DoublesMath.subtract(s, regs0));
            regs1.product(regarima.variables().rowsIterator(), DataBlock.of(mean ? coefficients1.drop(0,1) : coefficients1));
            builder.regsEffect1(regs1);
            builder.linearizedSeries1(DoublesMath.subtract(s, regs1));
            
        }else{
            builder.regsEffect0(DoubleSeq.empty());
            builder.linearizedSeries0(s);
            builder.regsEffect1(DoubleSeq.empty());
            builder.linearizedSeries1(s);
        }

        LikelihoodStatistics ll1 = LikelihoodStatistics.statistics(likelihood.logLikelihood(), model.getObservationsCount() - model.getMissingValuesCount())
                .llAdjustment(0)
                .differencingOrder(model.arima().getNonStationaryArOrder())
                .parametersCount(dim + model.getVariablesCount() + 1)
                .ssq(likelihood.ssq())
                .build();

        builder.model((LtdArimaModel) pt.getCore())
                .coefficients0(coefficients0)
                .covariance0(covariance0)
                .coefficients1(coefficients1)
                .covariance1(covariance1)
                .ll1(ll1);
        double[] gradient = min.gradientAtMinimum().toArray();
        FastMatrix hessian = min.curvatureAtMinimum();
        double objective = pt.getSsqE();
        int ndf = likelihood.degreesOfFreedom();
        hessian.mul((.5 * ndf) / objective);
        for (int i = 0; i < gradient.length; ++i) {
            gradient[i] *= (-.5 * ndf) / objective;
        }
        
        LogLikelihoodFunction<LtdArimaModel, DiffuseConcentratedLikelihood> ll = concentratedLogLikelihoodFunction(dmodel);
        LogLikelihoodFunction.Point max = new LogLikelihoodFunction.Point(ll, pt.getParameters(), DoubleSeq.of(gradient), hessian);
        return builder.max(max).build();
    }

    private LtdArimaKernel(LtdArimaSpec spec) {
        this.spec = spec;
    }

    private LogLikelihoodFunction<LtdArimaModel, DiffuseConcentratedLikelihood>
            concentratedLogLikelihoodFunction(RegArmaModel<SarimaModel> dmodel) {
        Function<LtdArimaModel, ConcentratedLikelihood> lfn = m -> {

            // estimate the stationary model
            Ssf ssf = m.ssf();
            return DkToolkit.concentratedLikelihoodComputer(true, false, true).compute(ssf, new SsfData(dmodel.getY()));
        };
        LtdArimaMapping ltdmapping = LtdArimaMapping.builder(dmodel.getArma().orders())
                .n(dmodel.getY().length())
                .vPhi(spec.isVBphi())
                .vBphi(spec.isVBphi())
                .vTheta(spec.isVTheta())
                .vBtheta(spec.isVBtheta())
                .vVar(spec.isVVar())
                .build();

        return new LogLikelihoodFunction(ltdmapping, lfn);
    }

}
