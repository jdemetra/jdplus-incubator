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

import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.sts.RegularSplineComponent;

/**
 *
 * @author palatej
 */
public class RegularSplineItem extends StateItem {

    private final VarianceInterpreter v;
    private final int startpos;
    private final RegularSplineComponent.Data data;

    public RegularSplineItem(String name, int[] pos, int startpos, double cvar, boolean fixedvar) {
        super(name);
        v = new VarianceInterpreter(name + ".var", cvar, fixedvar, true);
        this.data = RegularSplineComponent.Data.of(pos);
        this.startpos = startpos;
    }

    private RegularSplineItem(RegularSplineItem item) {
        super(item.name);
        this.data = item.data;
        this.startpos = item.startpos;
        v = item.v.duplicate();
    }

    @Override
    public RegularSplineItem duplicate() {
        return new RegularSplineItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double var = p.get(0);
            builder.add(name, RegularSplineComponent.stateComponent(data, var), RegularSplineComponent.loading(data, startpos));
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(v);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double var = p.get(0);
        return RegularSplineComponent.stateComponent(data, var);
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        }
        return RegularSplineComponent.loading(data, startpos);
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return data.getDim();
    }

    @Override
    public boolean isScalable() {
        return !v.isFixed();
    }

}
