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
package jdplus.highfreq.desktop.plugin.ui;

import demetra.desktop.sa.descriptors.highfreq.HighFreqSpecUI;
import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import jdplus.highfreq.base.api.ExtendedAirlineSpec;
import jdplus.toolkit.base.api.modelling.highfreq.EasterSpec;
import jdplus.toolkit.base.api.modelling.highfreq.EstimateSpec;
import jdplus.toolkit.base.api.modelling.highfreq.HolidaysSpec;
import jdplus.toolkit.base.api.modelling.highfreq.OutlierSpec;
import jdplus.toolkit.base.api.modelling.highfreq.RegressionSpec;
import jdplus.toolkit.base.api.modelling.highfreq.SeriesSpec;
import jdplus.toolkit.base.api.modelling.highfreq.TransformSpec;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.AllArgsConstructor
public class ExtendedAirlineSpecRoot implements HighFreqSpecUI {

    ExtendedAirlineModellingSpec core;
    boolean ro;

    @Override
    public boolean hasFixedCoefficients() {
        return core.getRegression().hasFixedCoefficients();
    }

    public void update(ExtendedAirlineSpec spec) {
        core = core.toBuilder().stochastic(spec).build();
    }

    @Override
    public void update(EstimateSpec spec) {
        core = core.toBuilder().estimate(spec).build();
    }

    @Override
    public void update(OutlierSpec spec) {
        core = core.toBuilder().outlier(spec).build();
    }

    @Override
    public void update(RegressionSpec spec) {
        core = core.toBuilder().regression(spec).build();
    }

    @Override
    public void update(TransformSpec spec) {
        core = core.toBuilder().transform(spec).build();
    }

    @Override
    public void update(SeriesSpec spec) {
        core = core.toBuilder().series(spec).build();
    }

    @Override
    public void update(EasterSpec spec) {
        update(core.getRegression()
                .toBuilder()
                .easter(spec)
                .build());
    }

    @Override
    public void update(HolidaysSpec spec) {
        update(core.getRegression()
                .toBuilder()
                .holidays(spec)
                .build());
    }

    @Override
    public TransformSpec transform() {
        return core.getTransform();
    }

    @Override
    public OutlierSpec outlier() {
        return core.getOutlier();
    }

}
