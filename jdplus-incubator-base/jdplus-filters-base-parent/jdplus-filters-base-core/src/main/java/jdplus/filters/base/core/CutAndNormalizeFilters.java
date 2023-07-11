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

import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.math.functions.GridSearch;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.math.functions.IFunctionPoint;
import jdplus.toolkit.base.core.math.functions.IParametersDomain;
import jdplus.toolkit.base.core.math.functions.ParametersRange;
import jdplus.toolkit.base.core.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.stats.Kernels;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CutAndNormalizeFilters {

    public FiniteFilter of(DoubleUnaryOperator kernel, double bandwidth, int m, int q) {
        return FiniteFilter.ofInternal(asymmetricWeights(kernel, bandwidth, m, q), -m);
    }

    private double[] asymmetricWeights(DoubleUnaryOperator kernel, double bandwidth, int m, int q) {
        double[] c = new double[m + q + 1];
        double s = 0;
        for (int i = -m; i <= q; ++i) {
            double w = kernel.applyAsDouble(i / bandwidth);
            c[m + i] = w;
            s += w;
        }
        for (int i = 0; i < c.length; ++i) {
            c[i] /= s;
        }
        return c;
    }

    static interface AsymmetricFilterProvider {

        FiniteFilter provide(double bandWitdth);
    }

    @lombok.Value
    static class D implements IFunction {

        private int m;
        private SymmetricFilter target;
        private AsymmetricFiltersFactory.Distance distance;
        private AsymmetricFilterProvider provider;
        private double lbound;
        private double ubound;

        @Override
        public IFunctionPoint evaluate(DoubleSeq parameters) {
            return new Point(parameters.get(0));
        }

        @Override
        public IParametersDomain getDomain() {
            return new ParametersRange(lbound, ubound, false);
        }

        class Point implements IFunctionPoint {

            final double bandWidth;

            Point(double bandWidth) {
                this.bandWidth = bandWidth;
            }

            @Override
            public IFunction getFunction() {
                return D.this;
            }

            @Override
            public DoubleSeq getParameters() {
                return DoubleSeq.of(bandWidth);
            }

            @Override
            public double getValue() {
                FiniteFilter f = provider.provide(bandWidth);
                return distance.compute(target, f);
            }

        }
    }

    public double optimalBandWidth(int m, int q, AsymmetricFiltersFactory.Distance distance) {
        SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
        
        D fn = new D(m, H, distance, bandWidth->of(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), bandWidth, m, q),
        		m, 3*m);
        GridSearch grid = GridSearch.builder()
                .bounds(m, 3 * m)
                .build();
        grid.minimize(fn.evaluate(DoubleSeq.of(m + 1)));
        return ((D.Point) grid.getResult()).bandWidth;
    }
    public double optimalBandWidth(int m, int q, AsymmetricFiltersFactory.Distance distance,
    		DoubleUnaryOperator kernel, double lbound, double ubound) {
        SymmetricFilter H = KernelsUtility.symmetricFilter(HighOrderKernels.kernel(Kernels.BIWEIGHT, 2), m + 1, m);
        
        D fn = new D(m, H, distance, bandWidth->of(kernel, bandWidth, m, q),
        		lbound, ubound);
        GridSearch grid = GridSearch.builder()
                .bounds(lbound, ubound)
                .build();
        grid.minimize(fn.evaluate(DoubleSeq.of(Double.max(m + 1, lbound))));
        return ((D.Point) grid.getResult()).bandWidth;
    }

}
