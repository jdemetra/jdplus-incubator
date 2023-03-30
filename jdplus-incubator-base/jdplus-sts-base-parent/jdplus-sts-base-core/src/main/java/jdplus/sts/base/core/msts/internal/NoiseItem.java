/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts.internal;

import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.toolkit.base.core.ssf.sts.Noise;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public class NoiseItem extends StateItem {

    private final VarianceInterpreter v;

    public NoiseItem(String name, double var, boolean fixed) {
        super(name);
        this.v = new VarianceInterpreter(name + ".var", var, fixed, true);
    }
    
    private NoiseItem(NoiseItem item){
        super(item.name);
        v=item.v.duplicate();
    }
    
    @Override
    public NoiseItem duplicate(){
        return new NoiseItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            StateComponent cmp = Noise.of(e);
            builder.add(name, cmp, Noise.defaultLoading());
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
            return Noise.of(e);
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        } else {
            return Noise.defaultLoading();
        }
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim(){
        return 1;
    }

    @Override
    public boolean isScalable() {
        return !v.isFixed();
    }

}
