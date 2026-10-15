/*
 * Copyright 2026 JDemetra+.
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
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SarimaResults implements GenericExplorable{

    private RegArimaModel<SarimaModel> model;
    private LikelihoodStatistics likelihood;
    private DoubleSeq coefficients;
    private Matrix covariance;
    private TsResiduals residuals;
    private DoubleSeq parameters;
    private Matrix parametersCovariance;
    private DoubleSeq linearizedSeries, regsEffect;

}
