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

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;

/**
 *
 * @author Jean Palate
 */
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
@lombok.Value
public class LtdSarimaResults implements GenericExplorable {

    private LtdSarimaModel model;
    private LikelihoodStatistics likelihood;
    private DoubleSeq coefficients;
    private Matrix covariance;
    private TsResiduals residuals;
    private String[] parametersNames;
    private DoubleSeq parameters;
    private Matrix parametersCovariance;
    private String[] derivedParametersNames;
    private DoubleSeq derivedParameters;
    private Matrix derivedParametersCovariance;
    private DoubleSeq linearizedSeries, regsEffect;
    private StatisticalTest stationarityTest, likelihoodRatioTest;
}
