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

/**
 *
 * @author Jean Palate
 */
@lombok.Builder(builderClassName = "Builder")
@lombok.Value
public class LtdArimaResults implements GenericExplorable {
    
    private DoubleSeq linearizedSeries0, regsEffect0;
    private DoubleSeq linearizedSeries1, regsEffect1;

    private SarimaModel start;
    private LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> startMax;
    private LikelihoodStatistics ll0;
    private DoubleSeq coefficients0;
    private Matrix covariance0;

    private LtdArimaModel model;
    private LogLikelihoodFunction.Point<LtdArimaModel, DiffuseConcentratedLikelihood> max;
    private LikelihoodStatistics ll1;
    private DoubleSeq coefficients1;
    private Matrix covariance1;

    public double v0() {
        return ll1.getSsqErr() / (ll1.getEffectiveObservationsCount() - ll1.getEstimatedParametersCount());
    }

    public double v1() {
        return model.getVar1() * ll1.getSsqErr() / (ll1.getEffectiveObservationsCount() - ll1.getEstimatedParametersCount());
    }
    
}
