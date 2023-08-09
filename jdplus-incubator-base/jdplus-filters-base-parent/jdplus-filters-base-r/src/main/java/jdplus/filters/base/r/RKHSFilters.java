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
import jdplus.toolkit.base.core.stats.Kernels;
import jdplus.toolkit.base.core.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import java.util.Locale;
import java.util.function.DoubleUnaryOperator;
import jdplus.filters.base.api.AsymmetricCriterion;
import jdplus.filters.base.api.SpectralDensity;
import jdplus.filters.base.core.CutAndNormalizeFilters;
import jdplus.filters.base.core.HighOrderKernels;
import jdplus.filters.base.core.KernelsUtility;
import jdplus.filters.base.core.RKHSFilterFactory;
import jdplus.filters.base.api.RKHSFilterSpec;
import jdplus.toolkit.base.core.math.linearfilters.ISymmetricFiltering;
import jdplus.toolkit.base.api.math.linearfilters.KernelOption;




/**
 *
 * @author Alain QLT
 */
@lombok.experimental.UtilityClass
public class RKHSFilters {


    public FiltersToolkit.FiniteFilters filterProperties(int horizon, int degree, String kernel, 
            boolean optimalbw, String criterion, boolean rwdensity, double passband, double bandwidth) {
        // Creates the filters
    	RKHSFilterSpec tspec=RKHSFilterSpec.builder()
                .filterLength(horizon)
                .polynomialDegree(degree)
                .kernel(KernelOption.valueOf(kernel))
                .optimalBandWidth(optimalbw)
                .asymmetricBandWith(AsymmetricCriterion.valueOf(criterion))
                .density(rwdensity ? SpectralDensity.RandomWalk : SpectralDensity.Undefined)
                .passBand(passband)
                .bandWidth(bandwidth)
                .minBandWidth(horizon)
                .maxBandWidth(3*horizon)
                .build();
        ISymmetricFiltering rkhsfilter= RKHSFilterFactory.of(tspec);
       
        return new FiltersToolkit.FiniteFilters(rkhsfilter.centralFilter(),
        		rkhsfilter.endPointsFilters());
    }
    public FiltersToolkit.FiniteFilters filterProperties(int horizon, int degree, String kernel, 
            boolean optimalbw, String criterion, boolean rwdensity, double passband,
            double bandwidth, double minbandwidth, double maxbandwidth) {
        // Creates the filters
    	RKHSFilterSpec tspec=RKHSFilterSpec.builder()
                .filterLength(horizon)
                .polynomialDegree(degree)
                .kernel(KernelOption.valueOf(kernel))
                .optimalBandWidth(optimalbw)
                .asymmetricBandWith(AsymmetricCriterion.valueOf(criterion))
                .density(rwdensity ? SpectralDensity.RandomWalk : SpectralDensity.Undefined)
                .passBand(passband)
                .bandWidth(bandwidth)
                .minBandWidth(minbandwidth)
                .maxBandWidth(maxbandwidth)
                .build();
        ISymmetricFiltering rkhsfilter= RKHSFilterFactory.of(tspec);
       
        return new FiltersToolkit.FiniteFilters(rkhsfilter.centralFilter(),
        		rkhsfilter.endPointsFilters());
    }
    
    public DoubleUnaryOperator optimalCriteria(int horizon, int leads, int degree, String kernel, 
            String criterion, boolean rwdensity, double passband) {

        DoubleUnaryOperator density = rwdensity ? SpectralDensity.RandomWalk.asFunction() : SpectralDensity.Undefined.asFunction();

        DoubleUnaryOperator kernel_fun = kernel(formattingKernel(kernel), degree, horizon);
        AsymmetricFiltersFactory.Distance distance;
        distance = switch (criterion) {
            case "FrequencyResponse" -> AsymmetricFiltersFactory.frequencyResponseDistance(density);
            case "Accuracy" -> AsymmetricFiltersFactory.accuracyDistance(density, passband);
            case "Smoothness" -> AsymmetricFiltersFactory.smoothnessDistance(density, passband);
            case "Timeliness" -> AsymmetricFiltersFactory.timelinessDistance(density, passband);
            default -> null;
        };
        SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), horizon + 1, horizon);
        return (bandWidth -> distance.compute(H, CutAndNormalizeFilters.of(kernel_fun, bandWidth, horizon, leads)));
        
    }
    public double[] optimalBandwidth(int horizon, int degree, String kernelFun, 
            String criterion, boolean rwdensity, double passBand,
            double minbandwidth, double maxbandwidth) {
    	double[] optimalBw = new double[horizon];
    	
        int len = horizon;
        DoubleUnaryOperator kernel = kernel(formattingKernel(kernelFun), degree, horizon);
        DoubleUnaryOperator density = rwdensity ? SpectralDensity.RandomWalk.asFunction() : SpectralDensity.Undefined.asFunction();
       
        for (int i = 0; i < len; ++i) {
        	switch (criterion) {
                case "FrequencyResponse":
                	optimalBw[i] = CutAndNormalizeFilters.optimalBandWidth(len, i,
                			AsymmetricFiltersFactory.frequencyResponseDistance(density),
                            kernel, minbandwidth, maxbandwidth);
                    break;
                case "Accuracy":
                	optimalBw[i] = CutAndNormalizeFilters.optimalBandWidth(len, i,
                			AsymmetricFiltersFactory.accuracyDistance(density, passBand),
                            kernel, minbandwidth, maxbandwidth);
                    break;
                case "Smoothness":
                	optimalBw[i] = CutAndNormalizeFilters.optimalBandWidth(len, i,
                			AsymmetricFiltersFactory.smoothnessDistance(density, passBand),
                            kernel, minbandwidth, maxbandwidth);
                    break;
                case "Timeliness":
                	optimalBw[i] = CutAndNormalizeFilters.optimalBandWidth(len, i,
                			AsymmetricFiltersFactory.timelinessDistance(density, passBand),
                            kernel, minbandwidth, maxbandwidth);
                    break;
                default:
                	optimalBw[i] = len + 1;
        	}
        }
        return optimalBw;
        
    }
    public static DoubleUnaryOperator kernel(String kernel, int deg, int len) {
        switch (kernel) {
            case "BiWeight":
                return HighOrderKernels.kernel(Kernels.BIWEIGHT, deg);
            case "TriWeight":
                return HighOrderKernels.kernel(Kernels.TRIWEIGHT, deg);
            case "Uniform":
                return HighOrderKernels.kernel(Kernels.UNIFORM, deg);
            case "Triangular":
                return HighOrderKernels.kernel(Kernels.TRIANGULAR, deg);
            case "Epanechnikov":
                return HighOrderKernels.kernel(Kernels.EPANECHNIKOV, deg);
            case "Henderson":
                return HighOrderKernels.kernel(Kernels.henderson(len), deg);
            default:
                return null;
        }
    }
    private static String formattingKernel(String kernel) {
        switch (kernel.toLowerCase(Locale.ROOT)) {
            case "biweight":
                return "BiWeight";
            case "triweight":
                return "TriWeight";
            case "uniform":
                return "Uniform";
            case "triangular":
                return "Triangular";
            case "epanechnikov":
                return "Epanechnikov";
            case "henderson":
                return "Henderson";
            default:
                return null;
        }
    }
    
}
