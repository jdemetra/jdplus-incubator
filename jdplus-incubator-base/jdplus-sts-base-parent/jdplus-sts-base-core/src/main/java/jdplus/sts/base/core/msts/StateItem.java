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
