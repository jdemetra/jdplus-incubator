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

import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.api.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
@lombok.AllArgsConstructor
@lombok.Value
public class LtdArimaResults implements GenericExplorable {

    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    public static class SarimaResults {

        private SarimaModel model;
        private LikelihoodStatistics ll;
        private DoubleSeq coefficients;
        private Matrix covariance;
        private TsResiduals residuals;
        private DoubleSeq parameters;
        private Matrix parametersCovariance;
        private DoubleSeq linearizedSeries, regsEffect;

    }

    @lombok.Builder(builderClassName = "Builder")
    @lombok.Value
    public static class LtdResults {

        private LtdArimaModel model;
        private LikelihoodStatistics ll;
        private DoubleSeq coefficients;
        private Matrix covariance;
        private TsResiduals residuals;
        private String[] parametersNames;
        private DoubleSeq parameters;
        private Matrix parametersCovariance;
        private String[] derivedParametersNames;
        private DoubleSeq derivedParameters;
        private DoubleSeq derivedParametersStderr;
        private DoubleSeq linearizedSeries, regsEffect;
        private StatisticalTest stationaryTest;
        private StatisticalTest likelihoodRatioTest;

        public double var0() {
            return ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount());
        }

        public double var1() {
            return model.getVar1() * ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount());
        }
    }

    SarimaResults start;
    LtdResults ltd;
}
