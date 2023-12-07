/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
 /*
 */
package jdplus.sts.base.core;

import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.api.Component;
import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class BsmData {

    int period;
    // LLT
    double levelVar, slopeVar;
    //SEAS
    double seasonalVar;
    SeasonalModel seasonalModel;
    //NOISE
    double noiseVar;
    // CYCLE
    double cycleVar, cycleDumpingFactor, cycleLength;

    public BsmData(BsmSpec spec, int period) {
        this.period = period;
        this.levelVar = BsmSpec.valueOf(spec.getLevelVar(), BsmSpec.DEF_VAR);
        this.slopeVar = BsmSpec.valueOf(spec.getSlopeVar(), BsmSpec.DEF_VAR);
        this.seasonalVar = BsmSpec.valueOf(spec.getSeasonalVar(), BsmSpec.DEF_VAR);
        this.seasonalModel = spec.getSeasonalModel();
        this.noiseVar = BsmSpec.valueOf(spec.getNoiseVar(), BsmSpec.DEF_VAR);
        this.cycleVar = BsmSpec.valueOf(spec.getCycleVar(), BsmSpec.DEF_VAR);
        this.cycleDumpingFactor = BsmSpec.valueOf(spec.getCycleDumpingFactor(), BsmSpec.DEF_CDUMP);
        this.cycleLength = BsmSpec.valueOf(spec.getCycleLength(), BsmSpec.DEF_CLENGTH);
    }

    public static Builder builder(int period) {
        Builder builder = new Builder();
        builder.period = period;
        builder.levelVar = -1;
        builder.slopeVar = -1;
        builder.seasonalVar = -1;
        builder.seasonalModel = SeasonalModel.HarrisonStevens;
        builder.noiseVar = 0;
        builder.cycleVar = -1;
        builder.cycleDumpingFactor = BsmSpec.DEF_CDUMP;
        builder.cycleLength = BsmSpec.DEF_CLENGTH;
        return builder;
    }

    @lombok.Value
    public static class ComponentVariance {

        Component component;
        double variance;
    }

    public ComponentVariance maxVariance() {
        double max = 0;
        Component cmp = Component.Undefined;
        if (levelVar > max) {
            max = levelVar;
            cmp = Component.Level;
        }
        if (slopeVar > max) {
            max = slopeVar;
            cmp = Component.Slope;
        }
        if (seasonalVar > max) {
            max = seasonalVar;
            cmp = Component.Seasonal;
        }
        if (noiseVar > max) {
            max = noiseVar;
            cmp = Component.Noise;
        }
        if (cycleVar > max) {
            max = cycleVar;
            cmp = Component.Cycle;
        }
        return new ComponentVariance(cmp, max);
    }

    public BsmData scaleVariances(double factor) {
        return new BsmData(period,
                levelVar > 0 ? levelVar * factor : levelVar,
                slopeVar > 0 ? slopeVar * factor : slopeVar,
                seasonalVar > 0 ? seasonalVar * factor : seasonalVar, seasonalModel,
                noiseVar > 0 ? noiseVar * factor : noiseVar,
                cycleVar > 0 ? cycleVar * factor : cycleVar, cycleDumpingFactor, cycleLength);
    }

}
