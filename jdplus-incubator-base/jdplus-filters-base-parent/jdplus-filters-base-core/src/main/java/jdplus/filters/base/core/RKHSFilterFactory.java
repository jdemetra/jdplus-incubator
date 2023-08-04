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

import jdplus.filters.base.api.RKHSFilterSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.toolkit.base.core.math.linearfilters.FilterUtility;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.ISymmetricFiltering;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.stats.Kernels;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class RKHSFilterFactory {

    private static final Map<RKHSFilterSpec, ISymmetricFiltering> dictionary = new HashMap<>();

    public static ISymmetricFiltering of(RKHSFilterSpec spec) {
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

        private final SymmetricFilter symmetricFilter;
        private final FiniteFilter[] asymmetricFilters;

        private Filter(RKHSFilterSpec spec) {
            int len = spec.getFilterLength();
            DoubleUnaryOperator kernel = kernel(spec);
            symmetricFilter = KernelsUtility.symmetricFilter(kernel, len + 1, len);
            asymmetricFilters = new FiniteFilter[len];
            double passBand = spec.getPassBand();
            DoubleUnaryOperator density = spec.getDensity().asFunction();
            for (int i = 0, j=len-1; i < len; ++i, --j) {
            	double bandWidth;
                if (spec.isOptimalBandWidth()) {
                	switch (spec.getAsymmetricBandWith()) {
	                    case FrequencyResponse:
	                        bandWidth = CutAndNormalizeFilters.optimalBandWidth(len, j,
	                        		AsymmetricFiltersFactory.frequencyResponseDistance(density),
	                                kernel, spec.getMinBandWidth(), spec.getMaxBandWidth());
	                        break;
	                    case Accuracy:
	                        bandWidth = CutAndNormalizeFilters.optimalBandWidth(len, j,
	                        		AsymmetricFiltersFactory.accuracyDistance(density, passBand),
	                                kernel, spec.getMinBandWidth(), spec.getMaxBandWidth());
	                        break;
	                    case Smoothness:
	                        bandWidth = CutAndNormalizeFilters.optimalBandWidth(len, j,
	                        		AsymmetricFiltersFactory.smoothnessDistance(density, passBand),
	                                kernel, spec.getMinBandWidth(), spec.getMaxBandWidth());
	                        break;
	                    case Timeliness:
	                        bandWidth = CutAndNormalizeFilters.optimalBandWidth(len, j,
	                        		AsymmetricFiltersFactory.timelinessDistance(density, passBand),
	                                kernel, spec.getMinBandWidth(), spec.getMaxBandWidth());
	                        break;
	                    default:
	                        bandWidth = len + 1;
                	}
                }else {
                	bandWidth=spec.getBandWidth();
                }
                asymmetricFilters[i] = CutAndNormalizeFilters.of(kernel, bandWidth, len, j);
            }
        }

        private static DoubleUnaryOperator kernel(RKHSFilterSpec spec) {
            int deg = spec.getPolynomialDegree();
            switch (spec.getKernel()) {
                case BiWeight:
                    return HighOrderKernels.kernel(Kernels.BIWEIGHT, deg);
                case TriWeight:
                    return HighOrderKernels.kernel(Kernels.TRIWEIGHT, deg);
                case Uniform:
                    return HighOrderKernels.kernel(Kernels.UNIFORM, deg);
                case Triangular:
                    return HighOrderKernels.kernel(Kernels.TRIANGULAR, deg);
                case Epanechnikov:
                    return HighOrderKernels.kernel(Kernels.EPANECHNIKOV, deg);
                case Henderson:
                    return HighOrderKernels.kernel(Kernels.henderson(spec.getFilterLength()), deg);
                default:
                    return null;
            }
        }

        @Override
        public DoubleSeq process(DoubleSeq in) {
            return FilterUtility.filter(in, symmetricFilter, asymmetricFilters);
        }
        
        @Override
        public SymmetricFilter symmetricFilter(){
            return symmetricFilter;
        }
        
        @Override
        public IFiniteFilter[] endPointsFilters(){
            return asymmetricFilters;
        }
        
    }

}
