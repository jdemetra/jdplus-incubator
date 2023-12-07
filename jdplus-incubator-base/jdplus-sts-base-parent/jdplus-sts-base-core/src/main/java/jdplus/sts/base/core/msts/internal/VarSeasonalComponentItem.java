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
package jdplus.sts.base.core.msts.internal;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sts.base.core.msts.StateItem;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.sts.base.core.msts.ScaleInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.sts.VarSeasonalComponent;

/**
 *
 * @author palatej
 */
public class VarSeasonalComponentItem extends StateItem {

    private final SeasonalModel model;
    private final int period;
    private final double[] std;
    private final ScaleInterpreter scale;

    public VarSeasonalComponentItem(String name, String smodel, int period, double[] std, double scale, boolean fixed) {
        super(name);
        this.model = SeasonalModel.valueOf(smodel);
        this.period = period;
        this.scale = new ScaleInterpreter(name + ".scale", scale, fixed, true);
        this.std=std;
    }

    private VarSeasonalComponentItem(VarSeasonalComponentItem item) {
        super(item.name);
        this.scale = item.scale.duplicate();
        this.model=item.model;
        this.period=item.period;
        this.std=item.std;
    }

    @Override
    public VarSeasonalComponentItem duplicate() {
        return new VarSeasonalComponentItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(scale);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            StateComponent cmp = VarSeasonalComponent.of(model, period, std, e);
            builder.add(name, cmp, VarSeasonalComponent.defaultLoading());
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(scale);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double e = p.get(0);
        return VarSeasonalComponent.of(model, period, std, e);
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return VarSeasonalComponent.defaultLoading();
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim(){
        return period-1;
    }
    
    @Override
    public boolean isScalable() {
        return !scale.isFixed();
    }
}
