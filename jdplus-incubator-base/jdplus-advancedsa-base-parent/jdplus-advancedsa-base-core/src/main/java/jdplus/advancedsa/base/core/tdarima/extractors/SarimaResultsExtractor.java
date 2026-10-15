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
package jdplus.advancedsa.base.core.tdarima.extractors;

import jdplus.advancedsa.base.api.tdarima.LtdDictionaries;
import jdplus.advancedsa.base.core.tdarima.SarimaResults;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class SarimaResultsExtractor extends InformationMapping<SarimaResults> {

    @Override
    public Class<SarimaResults> getSourceClass() {
        return SarimaResults.class;
    }

    public SarimaResultsExtractor() {
        set(
                modelItem(LtdDictionaries.PARAMETERS),
                double[].class,
                s -> s.getParameters().toArray());
        //        set(modelItem(LtdDictionaries.PARAMETERS_NAMES), String[].class, s ->
        // s.getParametersNames());
        set(
                modelItem(LtdDictionaries.PARAMETERS_COV),
                Matrix.class,
                source -> source.getParametersCovariance());
        delegate(
                LtdDictionaries.LIKELIHOOD,
                LikelihoodStatistics.class,
                source -> source.getLikelihood());
        delegate(LtdDictionaries.RESIDUALS, TsResiduals.class, source -> source.getResiduals());

        set(
                regItem(LtdDictionaries.REGS_C),
                double[].class,
                s -> s.getCoefficients().isEmpty() ? null : s.getCoefficients().toArray());
        set(
                regItem(LtdDictionaries.REGS_COV),
                Matrix.class,
                s -> s.getCovariance().isEmpty() ? null : s.getCovariance());
        set(
                regItem(LtdDictionaries.REGS_EFFECT),
                double[].class,
                s -> s.getRegsEffect().isEmpty() ? null : s.getRegsEffect().toArray());
        set(regItem(LtdDictionaries.Y_LIN), double[].class, s -> s.getLinearizedSeries().toArray());
    }

    //    private String mlItem(String key) {
    //        return Dictionary.concatenate(RegArimaDictionaries.MAX, key);
    //    }
    //
    private String regItem(String key) {
        return Dictionary.concatenate(LtdDictionaries.REGRESSION, key);
    }

    private String modelItem(String key) {
        return Dictionary.concatenate(LtdDictionaries.MODEL, key);
    }
}
