/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.filters.base.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearfilters.ISymmetricFiltering;
import jdplus.toolkit.base.core.math.linearfilters.FilterUtility;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.filters.base.api.DFAFilterSpec;

public class DFAFilterFactory {

    private static final Map<DFAFilterSpec, ISymmetricFiltering> dictionary = new HashMap<>();

    public static ISymmetricFiltering of(DFAFilterSpec spec) {
        synchronized (dictionary) {
            ISymmetricFiltering filtering = dictionary.get(spec);
            if (filtering == null) {
                filtering = new Filter(spec);
                dictionary.put(spec, filtering);
            }
            return filtering;
        }
    }

    private static class Filter implements ISymmetricFiltering {

        private Filter(DFAFilterSpec spec) {
            int len = spec.getLags();
            symmetricFilter = SymmetricFilter.ofInternal(spec.getTarget());
            asymmetricFilters = new FiniteFilter[len];
            DoubleUnaryOperator density = spec.getDensity().asFunction();

            DFAFilter.Builder builder = DFAFilter.builder()
                    .polynomialPreservation(spec.getPolynomialPreservationDegree())
                    .nlags(spec.getLags())
                    .timelinessLimits(spec.getW0(), spec.getW1())
                    .density(density)
                    .symetricFilter(symmetricFilter);
            DFAFilter.Results rslt;
            for (int i = 0, j = len - 1; i < len; ++i, --j) {
                rslt = builder.nleads(j).build().make(spec.getAccuracyWeight(), spec.getAccuracyWeight(), spec.getTimelinessWeight());
                asymmetricFilters[i] = rslt.getFilter();
            }

        }
        private final SymmetricFilter symmetricFilter;
        private final FiniteFilter[] asymmetricFilters;

        @Override
        public DoubleSeq process(DoubleSeq in) {
            return FilterUtility.filter(in, symmetricFilter, asymmetricFilters);
        }

        @Override
        public SymmetricFilter centralFilter() {
            return symmetricFilter;
        }

        @Override
        public IFiniteFilter[] endPointsFilters() {
            return asymmetricFilters;
        }
    }
}
