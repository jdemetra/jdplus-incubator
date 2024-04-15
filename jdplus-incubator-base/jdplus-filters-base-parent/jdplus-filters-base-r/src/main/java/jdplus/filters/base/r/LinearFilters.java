/*
 * Copyright 2022 National Bank of Belgium
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

import java.util.function.DoubleFunction;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class LinearFilters {
    
    /**
     * 
     * @param weights Weights of the filter, from its lower bound to the upper bound
     * @param lbound Lower bound
     * @param start First frequency 
     * @param step Step between two successive frequencies
     * @param n Number of steps
     * @return A matrix with the used frequencies in the first column, the gain
     * of the filter in the second column and the phase of the filter in the
     * third column
     * 
     */
    public Matrix frequencyResponse(double[] weights, int lbound, double start, double step, int n){
        FiniteFilter ff=FiniteFilter.of(weights, lbound);
        DoubleFunction<Complex> fn = ff.frequencyResponseFunction();
        FastMatrix fr=FastMatrix.make(n, 3);
        double f=start;
        for (int i=0; i<n; ++i){
            Complex cur=fn.apply(f);
            DataBlock row = fr.row(i);
            row.set(0, f);
            row.set(1, cur.abs());
            row.set(2, cur.arg());
            f+=step;
        }
        return fr;
    }
}
