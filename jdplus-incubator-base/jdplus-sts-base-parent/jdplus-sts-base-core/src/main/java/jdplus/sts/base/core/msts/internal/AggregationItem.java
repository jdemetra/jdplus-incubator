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
import java.util.ArrayList;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.core.ssf.composite.CompositeLoading;
import jdplus.toolkit.base.core.ssf.composite.CompositeState;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
public class AggregationItem extends StateItem {

    private final StateItem[] cmps;

    public AggregationItem(String name, StateItem[] cmps) {
        super(name);
        this.cmps = cmps;
    }
    
    @Override
    public AggregationItem duplicate(){
        StateItem[] citems=new StateItem[cmps.length];
        for (int i=0; i<citems.length; ++i)
            citems[i]=cmps[i].duplicate();
        return new AggregationItem(name, citems);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        CompositeState.Builder builder = CompositeState.builder();
        int pos = 0;
        for (int i = 0; i < cmps.length; ++i) {
            int n = cmps[i].parametersCount();
            builder.add(cmps[i].build(p.extract(pos, n)));
            pos += n;
        }
        return builder.build();
    }

    @Override
    public int parametersCount() {
        int n = 0;
        for (int i = 0; i < cmps.length; ++i) {
            n += cmps[i].parametersCount();
        }
        return n;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        }
        int[] dim = new int[cmps.length];
        ISsfLoading[] l = new ISsfLoading[cmps.length];

        for (int i = 0; i < cmps.length; ++i) {
            dim[i] = cmps[i].stateDim();
            l[i] = cmps[i].defaultLoading(0);
        }
        return new CompositeLoading(dim, l);
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        List<ParameterInterpreter> all = new ArrayList<>();
        for (int i = 0; i < cmps.length; ++i) {
            all.addAll(cmps[i].parameters());
        }
        return all;
    }

    @Override
    public int stateDim() {
        int dim = 0;
        for (int i = 0; i < cmps.length; ++i) {
            dim += cmps[i].stateDim();
        }
        return dim;
    }

}
