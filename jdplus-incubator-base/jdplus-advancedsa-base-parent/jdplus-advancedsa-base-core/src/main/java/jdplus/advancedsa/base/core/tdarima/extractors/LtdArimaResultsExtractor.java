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
import jdplus.advancedsa.base.core.tdarima.LtdArimaResults;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseLikelihoodStatistics;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class LtdArimaResultsExtractor extends InformationMapping<LtdArimaResults> {

    @Override
    public Class<LtdArimaResults> getSourceClass() {
        return LtdArimaResults.class;
    }

    public LtdArimaResultsExtractor() {
        set(LtdDictionaries.PARAMETERS_FIRST, double[].class, s -> s.getModel().getP0().toArray());
        set(LtdDictionaries.PARAMETERS_LAST, double[].class, s -> s.getModel().getP1().toArray());
        set(LtdDictionaries.PARAMETERS_MEAN, double[].class, s -> {
            DoubleSeq p0 = s.getModel().getP0();
            DoubleSeq p1 = s.getModel().getP1();
            int n = s.getModel().getN();
            return DoubleSeq.onMapping(n, i -> (p0.get(i) + p1.get(i)) / 2).toArray();
        });
        set(LtdDictionaries.PARAMETERS_DELTA, double[].class, s -> {
            DoubleSeq p0 = s.getModel().getP0();
            DoubleSeq p1 = s.getModel().getP1();
            int n = s.getModel().getN() - 1;
            return DoubleSeq.onMapping(n, i -> (p1.get(i) - p1.get(i)) / n).toArray();
        });
        delegate("ll0", LikelihoodStatistics.class, source -> source.getLl0());
        delegate("ll1", LikelihoodStatistics.class, source -> source.getLl1());

    }
}
