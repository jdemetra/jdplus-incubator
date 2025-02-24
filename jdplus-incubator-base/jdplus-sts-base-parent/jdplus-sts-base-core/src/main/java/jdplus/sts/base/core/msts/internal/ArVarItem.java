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
import jdplus.sts.base.core.msts.ArInterpreter;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.toolkit.base.core.ssf.arima.SsfAr;
import java.util.Arrays;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.sts.base.core.msts.ScaleInterpreter;
import jdplus.sts.base.core.ssf.SsfArVar;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public class ArVarItem extends StateItem {

    private final ArInterpreter ar;
    private final ScaleInterpreter scale;
    private final DoubleSeq std;
    private final boolean zeroinit;

    public ArVarItem(String name, double[] ar, boolean fixedar, double[] std, double scale, boolean fixed, boolean zeroinit) {
        super(name);
        this.ar = new ArInterpreter(name + ".ar", ar, fixedar);
        this.scale = new ScaleInterpreter(name + ".scale", scale, fixed, true);
        this.std = DoubleSeq.of(std);
        this.zeroinit = zeroinit;
    }

    private ArVarItem(ArVarItem item) {
        super(item.name);
        this.ar = item.ar.duplicate();
        this.scale = item.scale.duplicate();
        this.std=item.std;
        this.zeroinit = item.zeroinit;
    }

    @Override
    public ArVarItem duplicate() {
        return new ArVarItem(this);
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(ar, scale);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        if (p == null) {
            return SsfArVar.of(ar.values().toArray(), std.toArray(), scale.scale(), zeroinit);
        } else {
            int n = ar.getDomain().getDim();
            double[] par = p.extract(0, n).toArray();
            double w = p.get(n);
            return SsfArVar.of(par, std.toArray(), w, zeroinit);
        }
    }

    @Override
    public int parametersCount() {
        return 1 + ar.getDomain().getDim();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        } else {
            return SsfAr.defaultLoading();
        }
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return ar.getDomain().getDim();
    }

}
