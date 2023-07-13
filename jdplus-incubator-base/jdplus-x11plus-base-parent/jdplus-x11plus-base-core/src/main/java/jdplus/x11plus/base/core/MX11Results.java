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

import java.util.List;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class MX11Results {

    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    public static class Step {

        Number period;
        TsData b1, d10, d11, d12, d13;
    }

    public DecompositionMode mode;

    @lombok.Singular
    public List<Step> steps;

    public TsData seasonal() {
        if (steps.isEmpty()) {
            return null;
        }
        boolean multiplicative = mode.isMultiplicative();
        TsData all = null;
        for (Step s : steps) {
            TsData d10 = s.getD10();
            all = multiplicative ? TsData.multiply(all, d10) : TsData.add(all, d10);
        }
        return all;
    }

    public SeriesDecomposition asDecomposition(){
        if (steps.isEmpty()) {
            return null;
        }
        Step s0=steps.get(0), s1=steps.get(steps.size()-1);
        return SeriesDecomposition.builder(mode)
                .add(s0.getB1(), ComponentType.Series)
                .add(s1.getD12(), ComponentType.Trend)
                .add(s1.getD11(), ComponentType.SeasonallyAdjusted)
                .add(s1.getD13(), ComponentType.Irregular)
                .add(seasonal(), ComponentType.Seasonal)
                .build();
    }
}
