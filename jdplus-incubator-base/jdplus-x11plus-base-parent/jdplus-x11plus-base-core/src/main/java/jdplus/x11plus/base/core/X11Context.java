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
package jdplus.x11plus.base.core;

import jdplus.filters.base.core.FiltersToolkit;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearfilters.IFiltering;
import jdplus.toolkit.base.core.math.linearfilters.LocalPolynomialFilters;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.x11plus.base.api.X11plusSpec;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
public class X11Context {
    
    @lombok.NonNull
    DecompositionMode mode;
    @lombok.NonNull
    Number period;
    double lowerSigma, upperSigma;
    
    IFiltering trendFiltering;
    IFiltering initialSeasonalFiltering, finalSeasonalFiltering;
    
    public static X11Context of(X11plusSpec spec){
        return X11Context.builder()
                .mode(spec.getMode())
                .lowerSigma(spec.getLowerSigma())
                .upperSigma(spec.getUpperSigma())
                .trendFiltering(FiltersToolkit.of(spec.getTrendFilter()))
                .initialSeasonalFiltering(FiltersToolkit.of(spec.getInitialSeasonalFilter()))
                .finalSeasonalFiltering(FiltersToolkit.of(spec.getFinalSeasonalFilter()))
                .build();
    }

    public static Builder builder() {
        Builder builder = new Builder();
        builder.mode = DecompositionMode.Multiplicative;
        builder.trendFiltering=LocalPolynomialFilters.of(LocalPolynomialFilterSpec.DEF_TREND_SPEC);
        
        builder.lowerSigma = 1.5;
        builder.upperSigma = 2.5;
        return builder;
    }

    public DoubleSeq remove(DoubleSeq l, DoubleSeq r) {
        if (mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) / r.get(i));
        } else {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) - r.get(i));
        }
    }

    public DoubleSeq add(DoubleSeq l, DoubleSeq r) {
        if (mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) * r.get(i));
        } else {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) + r.get(i));
        }
    }

    public void remove(DoubleSeq l, DoubleSeq r, DataBlock q) {
        if (mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive) {
            q.set(l, r, (x, y) -> x / y);
        } else {
            q.set(l, r, (x, y) -> x - y);
        }
    }

    public void add(DoubleSeq l, DoubleSeq r, DataBlock q) {
        if (mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive) {
            q.set(l, r, (x, y) -> x * y);
        } else {
            q.set(l, r, (x, y) -> x + y);
        }
    }
}
