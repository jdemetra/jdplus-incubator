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
import jdplus.advancedsa.base.core.tdarima.LtdResults;
import jdplus.advancedsa.base.core.tdarima.LtdSarimaResults;
import jdplus.advancedsa.base.core.tdarima.SarimaResults;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class LtdResultsExtractor extends InformationMapping<LtdResults> {

    @Override
    public Class<LtdResults> getSourceClass() {
        return LtdResults.class;
    }

    public LtdResultsExtractor() {
        delegate(LtdDictionaries.SARIMA, SarimaResults.class, source -> source.getStart());
        delegateArray(LtdDictionaries.ITER, 0, 10, LtdSarimaResults.class, (source, i) -> {
            int last = source.getLtdResults().size() - 1;
            if (i > last) {
                return null;
            }
            return source.getLtdResults().get(i);
        });
        delegate(LtdDictionaries.SARIMA, SarimaResults.class, source -> source.getStart());
        delegate(LtdDictionaries.FINAL_STEP, LtdSarimaResults.class, source -> source.finalResults());
        delegate(LtdDictionaries.INITIAL_STEP, LtdSarimaResults.class, source -> source.results(0));
        set(LtdDictionaries.NITERS, Integer.class, source -> source.getLtdResults()
                .size());
        set(LtdDictionaries.TIME_VARYING, Boolean.class, source -> source.isTimeVarying());
    }

    //    private String mlItem(String key) {
    //        return Dictionary.concatenate(RegArimaDictionaries.MAX, key);
    //    }
    //
    //    private String regItem(String key) {
    //        return Dictionary.concatenate(LtdDictionaries.REGRESSION, key);
    //    }
    //
    //    private String modelItem(String key) {
    //        return Dictionary.concatenate(LtdDictionaries.MODEL, key);
    //    }
    //
}
