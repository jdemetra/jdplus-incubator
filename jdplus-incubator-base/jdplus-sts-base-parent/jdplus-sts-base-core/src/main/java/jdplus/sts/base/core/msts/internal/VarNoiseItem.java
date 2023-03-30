/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts.internal;

import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sts.base.core.msts.MstsMapping;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.sts.base.core.msts.ScaleInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.VarNoise;

/**
 *
 * @author palatej
 */
public class VarNoiseItem extends StateItem {

    private final ScaleInterpreter scale;
    private final double[] std;

    public VarNoiseItem(String name, double[] std, double scale, boolean fixed) {
        super(name);
        this.scale = new ScaleInterpreter(name + ".scale", scale, fixed, true);
        this.std=std;
    }
    
    private VarNoiseItem(VarNoiseItem item){
        super(item.name);
        scale=item.scale.duplicate();
        this.std=item.std;
    }
    
    @Override
    public VarNoiseItem duplicate(){
        return new VarNoiseItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(scale);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            StateComponent cmp = VarNoise.of(std, e);
            builder.add(name, cmp, VarNoise.defaultLoading());
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
            return VarNoise.of(std, e);
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
            return VarNoise.defaultLoading();
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
    public boolean isScalable(){
        return ! scale.isFixed();
    }

}
