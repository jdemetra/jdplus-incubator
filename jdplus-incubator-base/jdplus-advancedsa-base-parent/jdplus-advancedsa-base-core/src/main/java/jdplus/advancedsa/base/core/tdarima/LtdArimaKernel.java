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
import jdplus.toolkit.base.api.timeseries.TsData;
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

/**
 *
 * @author Jean Palate
 */
public class LtdArimaKernel {

    private final LtdArimaSpec spec;

    public static LtdArimaKernel of(LtdArimaSpec spec) {
        return new LtdArimaKernel(spec);
    }

    public LtdArimaResults process(TsData s) {

        LtdArimaResults.Builder builder = LtdArimaResults.builder();

        SarimaOrders orders = spec.getSarimaSpec()
                .withPeriod(s.getAnnualFrequency())
                .orders();

        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder()
                .y(s.getValues())
                .arima(SarimaModel.builder(orders).build())
                .build();

        SarimaMapping mapping = SarimaMapping.of(orders);
        RegArimaEstimation<SarimaModel> initial = RegSarimaComputer.PROCESSOR.process(regarima, mapping);

        LikelihoodStatistics ll0 = initial.statistics();
        builder.start(initial.getModel().arima())
                .ll0(ll0);

        RegArimaModel<SarimaModel> model = initial.getModel();
        RegArmaModel<SarimaModel> dmodel = model.differencedModel();
        SarmaOrders storders = orders.doStationary();

        // estimate the stationary model
        LtdArimaMapping ltdmapping = LtdArimaMapping.builder(SarimaOrders.of(storders, 0, 0))
                .n(s.length())
                .vPhi(spec.isVBphi())
                .vBphi(spec.isVBphi())
                .vTheta(spec.isVTheta())
                .vBtheta(spec.isVBtheta())
                .vVar(spec.isVVar())
                .build();
        SsfFunction fn = SsfFunction.builder(new SsfData(dmodel.getY()), ltdmapping, (LtdArimaModel lmodel) -> lmodel.ssf())
                .useScalingFactor(true)
                .useLog(false)
                .useParallelProcessing(true)
                .build();
        int dim = ltdmapping.getDim();
        DataBlock p = DataBlock.make(dim);
        p.range(0, storders.getParametersCount()).copy(dmodel.getArma().parameters());
        LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer.builder()
                .functionPrecision(1e-9)
                .build();
        SsfFunctionPoint pstart = fn.evaluate(p);
        min.minimize(pstart);
        SsfFunctionPoint pt = (SsfFunctionPoint) min.getResult();
        DiffuseConcentratedLikelihood likelihood = pt.getLikelihood();

        LikelihoodStatistics ll1 = LikelihoodStatistics.statistics(likelihood.logLikelihood(), model.getObservationsCount() - model.getMissingValuesCount())
                .llAdjustment(0)
                .differencingOrder(model.arima().getNonStationaryArOrder())
                .parametersCount(dim + model.getVariablesCount() + 1)
                .ssq(likelihood.ssq())
                .build();

        builder.model((LtdArimaModel) pt.getCore())
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
