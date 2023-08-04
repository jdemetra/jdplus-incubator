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

import jdplus.filters.base.api.FSTFilterSpec;
import jdplus.toolkit.base.core.math.linearfilters.IFiltering;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearfilters.FilterUtility;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;

/**
 *
 * @author Jean Palate
 */
public class FSTFilterFactory {

    public static IFiltering of(FSTFilterSpec spec) {
        return new Filter(spec);
    }

    private static class Filter implements IFiltering {

        private final IFiniteFilter cf;
        private final IFiniteFilter[] lf, rf;

        private Filter(FSTFilterSpec spec) {
            // central filter
            FSTFilter.Builder builder = FSTFilter.builder()
                    .degreeOfSmoothness(spec.getSmoothnessDegree())
                    .polynomialPreservation(spec.getPolynomialPreservationDegree())
                    .nlags(spec.getLags())
                    .nleads(spec.getLeads())
                    .timelinessAntiphaseCriterion(spec.isAntiphase())
                    .timelinessLimits(spec.getW0(), spec.getW1());
                    
            FSTFilter.Results rslt = builder.build().make(spec.getSmoothnessWeight(), spec.getTimelinessWeight(), true);
            cf=rslt.getFilter();
            lf=new IFiniteFilter[spec.getLags()];
            rf=new IFiniteFilter[spec.getLeads()];
            int del=spec.getLags()+spec.getLeads();
            for (int i=0; i<lf.length; ++i){
                rslt = builder.nlags(i).nleads(del-i).build().make(spec.getSmoothnessWeight(), spec.getTimelinessWeight(), false);
                lf[lf.length-i-1]=rslt.getFilter();
            }
            builder.nlags(spec.getLags());
            builder.nleads(spec.getLeads());
            for (int i=0; i<rf.length; ++i){
                rslt = builder.nleads(i).nlags(del-i).build().make(spec.getSmoothnessWeight(), spec.getTimelinessWeight(), false);
               rf[rf.length-i-1]=rslt.getFilter();
            }
        }

        @Override
        public DoubleSeq process(DoubleSeq in) {
            return FilterUtility.filter(in, cf, lf, rf);
        }

        @Override
        public IFiniteFilter centralFilter() {
            return cf;
        }

        @Override
        public IFiniteFilter[] leftEndPointsFilters() {
            return lf;
        }

        @Override
        public IFiniteFilter[] rightEndPointsFilters() {
            return rf;
        }

    }

}
