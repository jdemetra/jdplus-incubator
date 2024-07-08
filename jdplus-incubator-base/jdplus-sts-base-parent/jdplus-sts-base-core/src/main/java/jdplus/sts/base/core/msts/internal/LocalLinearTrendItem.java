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
import java.util.Arrays;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.sts.LocalLinearTrend;

/**
 *
 * @author palatej
 */
public class LocalLinearTrendItem extends StateItem {

    public final VarianceInterpreter lv, sv;

    public LocalLinearTrendItem(final String name, double lvar, double svar, boolean lfixed, boolean sfixed) {
        super(name);
        lv = new VarianceInterpreter(name + ".lvar", lvar, lfixed, true);
        sv = new VarianceInterpreter(name + ".svar", svar, sfixed, true);
    }

    private LocalLinearTrendItem(LocalLinearTrendItem item) {
        super(item.name);
        this.lv = item.lv.duplicate();
        this.sv = item.sv.duplicate();
    }

    @Override
    public LocalLinearTrendItem duplicate() {
        return new LocalLinearTrendItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(lv);
        mapping.add(sv);
        mapping.add((p, builder) -> {
            double e1 = p.get(0);
            double e2 = p.get(1);
            StateComponent cmp = LocalLinearTrend.stateComponent(e1, e2);
            builder.add(name, cmp, LocalLinearTrend.defaultLoading());
            return 2;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(lv, sv);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        if (p == null) {
            return LocalLinearTrend.stateComponent(lv.variance(), sv.variance());
        } else {
            double e1 = p.get(0);
            double e2 = p.get(1);
            return LocalLinearTrend.stateComponent(e1, e2);
        }
    }

    @Override
    public int parametersCount() {
        return 2;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return LocalLinearTrend.defaultLoading();
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return 2;
    }

    @Override
    public boolean isScalable() {
        return !lv.isFixed() && !sv.isFixed();
    }
}
