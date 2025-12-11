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
package jdplus.advancedsa.base.r;

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class TdArimaDecompositionExtractor extends InformationMapping<TimeVaryingArimaModels.DecompositionResults> {

    @Override
    public Class<TimeVaryingArimaModels.DecompositionResults> getSourceClass() {
        return TimeVaryingArimaModels.DecompositionResults.class;
    }

    public TdArimaDecompositionExtractor() {

        delegate("direct_ll", LikelihoodStatistics.class, source -> source.getDirectLikelihood().stats(0, 0));
        delegate("decomposition_ll", LikelihoodStatistics.class, source -> source.getDecompositionLikelihood().stats(0, 0));
        set("direct_res", double[].class, s -> s.getDirectLikelihood().e().toArray());
        set("decomposition_res", double[].class, s -> s.getDecompositionLikelihood().e().toArray());

    }


}
