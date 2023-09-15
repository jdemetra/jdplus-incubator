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
package jdplus.x12plus.desktop.plugin.mx12.ui;

import jdplus.sa.desktop.plugin.descriptors.highfreq.HighFreqSpecUI;
import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import jdplus.highfreq.base.api.ExtendedAirlineSpec;
import jdplus.toolkit.base.api.modelling.highfreq.EasterSpec;
import jdplus.toolkit.base.api.modelling.highfreq.EstimateSpec;
import jdplus.toolkit.base.api.modelling.highfreq.HolidaysSpec;
import jdplus.toolkit.base.api.modelling.highfreq.OutlierSpec;
import jdplus.toolkit.base.api.modelling.highfreq.RegressionSpec;
import jdplus.toolkit.base.api.modelling.highfreq.SeriesSpec;
import jdplus.toolkit.base.api.modelling.highfreq.TransformSpec;
import jdplus.x12plus.base.api.MX11plusSpec;
import jdplus.x12plus.base.api.MX12plusSpec;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.AllArgsConstructor
public class MX12plusSpecRoot implements HighFreqSpecUI {

    MX12plusSpec core;
    boolean ro;

    public boolean isPreprocessingEnabled() {
        return core.getPreprocessing().isEnabled();
    }

    public void setPreprocessingEnabled(boolean enabled) {
        update(getPreprocessing().toBuilder().enabled(enabled).build());
    }

    public ExtendedAirlineModellingSpec getPreprocessing() {
        return core.getPreprocessing();
    }
    
    public MX11plusSpec x11(){
        return core.getX11();
    }
    
    public void update(MX11plusSpec x11){
        core=core.toBuilder().x11(x11).build();
    }

    public void update(ExtendedAirlineModellingSpec spec) {
        core = core.toBuilder().preprocessing(spec).build();
    }

    @Override
    public boolean hasFixedCoefficients() {
        return core.getPreprocessing().getRegression().hasFixedCoefficients();
    }

    public void update(ExtendedAirlineSpec spec) {
        update(getPreprocessing().toBuilder().stochastic(spec).build());
    }

    @Override
    public void update(EstimateSpec spec) {
        update(getPreprocessing().toBuilder().estimate(spec).build());
    }

    @Override
    public void update(OutlierSpec spec) {
        update(getPreprocessing().toBuilder().outlier(spec).build());
    }

    @Override
    public void update(RegressionSpec spec) {
        update(getPreprocessing().toBuilder().regression(spec).build());
    }

    @Override
    public void update(TransformSpec spec) {
        update(getPreprocessing().toBuilder().transform(spec).build());
    }

    @Override
    public void update(SeriesSpec spec) {
        update(getPreprocessing().toBuilder().series(spec).build());
    }

    @Override
    public void update(EasterSpec spec) {
        update(getPreprocessing().getRegression()
                .toBuilder()
                .easter(spec)
                .build());
    }

    @Override
    public void update(HolidaysSpec spec) {
        update(getPreprocessing().getRegression()
                .toBuilder()
                .holidays(spec)
                .build());
    }

    @Override
    public TransformSpec transform() {
        return getPreprocessing().getTransform();
    }

    @Override
    public OutlierSpec outlier() {
        return getPreprocessing().getOutlier();
    }

}
