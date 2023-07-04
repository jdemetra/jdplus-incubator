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
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.sts.base.core.SeasonalComponent;
import jdplus.sts.base.api.SeasonalModel;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public class SeasonalComponentItem extends StateItem {

    private final SeasonalModel model;
    private final int period;
    private final VarianceInterpreter v;

    public SeasonalComponentItem(String name, String smodel, int period, double seasvar, boolean fixed) {
        super(name);
        this.model = SeasonalModel.valueOf(smodel);
        this.period = period;
        this.v = new VarianceInterpreter(name + ".var", seasvar, fixed, true);
    }

    private SeasonalComponentItem(SeasonalComponentItem item) {
        super(item.name);
        this.v = item.v.duplicate();
        this.model=item.model;
        this.period=item.period;
    }

    @Override
    public SeasonalComponentItem duplicate() {
        return new SeasonalComponentItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            StateComponent cmp = SeasonalComponent.of(model, period, e);
            builder.add(name, cmp, SeasonalComponent.defaultLoading());
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(v);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double e = p.get(0);
        return SeasonalComponent.of(model, period, e);
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return SeasonalComponent.defaultLoading();
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
        return !v.isFixed();
    }
    
}
