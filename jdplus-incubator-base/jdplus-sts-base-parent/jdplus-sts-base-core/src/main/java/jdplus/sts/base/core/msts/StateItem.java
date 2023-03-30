/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public abstract class StateItem implements ModelItem {
    
    @Override
    public abstract StateItem duplicate();

    protected final String name;

    protected StateItem(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.addAll(parameters());
        mapping.add((p, builder) -> {
            builder.add(name, build(p), defaultLoadingCount() == 1 ? defaultLoading(0) : null);
            return parametersCount();
        });
    }
    
    public abstract int stateDim();

    public abstract StateComponent build(DoubleSeq p);

    public abstract int parametersCount();

    public abstract ISsfLoading defaultLoading(int m);

    public abstract int defaultLoadingCount();
}
