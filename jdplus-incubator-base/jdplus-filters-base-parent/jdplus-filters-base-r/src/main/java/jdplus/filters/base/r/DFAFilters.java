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
package jdplus.filters.base.r;

import jdplus.filters.base.core.FiltersToolkit;
import jdplus.filters.base.api.SpectralDensity;
import jdplus.filters.base.core.DFAFilterFactory;
import jdplus.filters.base.api.DFAFilterSpec;
import jdplus.toolkit.base.core.math.linearfilters.ISymmetricFiltering;


@lombok.experimental.UtilityClass
public class DFAFilters {
	public FiltersToolkit.FiniteFilters filterProperties(double[] target,
			int nlags, int pdegree, boolean rwdensity, double passband, 
			double waccuracy, double wsmoothness, double wtimeliness) {
        // Creates the filters
    	DFAFilterSpec tspec= DFAFilterSpec.builder()
                .w0(0)
                .w1(passband)
                .polynomialPreservationDegree(pdegree)
                .lags(nlags)
                .target(target)
                .accuracyWeight(waccuracy)
                .smoothnessWeight(wsmoothness)
                .timelinessWeight(wtimeliness)
                .density(rwdensity ? SpectralDensity.RandomWalk : SpectralDensity.Undefined)
                .build();

        ISymmetricFiltering dfafilter= DFAFilterFactory.of(tspec);
       
        return new FiltersToolkit.FiniteFilters(dfafilter.centralFilter(),
        		dfafilter.endPointsFilters());
    }
}
