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
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.StablePolynomialInterpreter;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.toolkit.base.core.ssf.StateComponent;
import java.util.Arrays;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;

/**
 *
 * @author palatej
 */
public class ArmaItem extends StateItem {

    private final StablePolynomialInterpreter par, pma;
    private final VarianceInterpreter v;

    public ArmaItem(final String name, double[] ar, double[] ma, double var, boolean fixed) {
        super(name);
        int nar = ar == null ? 0 : ar.length, nma = ma == null ? 0 : ma.length;
        if (nar > 0) {
            par = new StablePolynomialInterpreter(name + ".ar", ar, fixed);
        } else {
            par = null;
        }
        if (nma > 0) {
            pma = new StablePolynomialInterpreter(name + ".ma", ma, fixed);
        } else {
            pma = null;
        }
        v = new VarianceInterpreter(name + ".var", var, true, true);
    }

    private ArmaItem(ArmaItem item){
        super(item.name);
        this.par=item.par == null ? null : item.par.duplicate();
        this.pma=item.pma == null ? null : item.pma.duplicate();
        this.v=item.v.duplicate();
     }
    
    @Override
    public ArmaItem duplicate(){
        return new ArmaItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        if (par != null) {
            mapping.add(par);
        }
        if (pma != null) {
            mapping.add(pma);
        }
        mapping.add(v);
        mapping.add((p, builder) -> {
            BackFilter bar = BackFilter.ONE, bma = BackFilter.ONE;
            int pos = 0;
            if (par != null) {
                int nar = par.getDomain().getDim();
                Polynomial ar = Polynomial.valueOf(1, p.extract(0, nar).toArray());
                bar = new BackFilter(ar);
                pos += nar;
            }
            if (pma != null) {
                int nma = pma.getDomain().getDim();
                Polynomial ma = Polynomial.valueOf(1, p.extract(0, nma).toArray());
                bma = new BackFilter(ma);
                pos += nma;
            }
            double n = p.get(pos++);
            ArimaModel arima = new ArimaModel(bar, BackFilter.ONE, bma, n);
            StateComponent cmp = SsfArima.stateComponent(arima);
            builder.add(name, cmp, SsfArima.defaultLoading());
            return pos;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(par, pma, v);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        BackFilter bar = BackFilter.ONE, bma = BackFilter.ONE;
        int pos = 0;
        if (par != null) {
            int nar = par.getDomain().getDim();
            Polynomial ar = Polynomial.valueOf(1, p.extract(0, nar).toArray());
            bar = new BackFilter(ar);
            pos += nar;
        }
        if (pma != null) {
            int nma = pma.getDomain().getDim();
            Polynomial ma = Polynomial.valueOf(1, p.extract(0, nma).toArray());
            bma = new BackFilter(ma);
            pos += nma;
        }
        double n = p.get(pos++);
        ArimaModel arima = new ArimaModel(bar, BackFilter.ONE, bma, n);
        return SsfArima.stateComponent(arima);
    }

    @Override
    public int parametersCount() {
        int n = 1;
        if (par != null) {
            int nar = par.getDomain().getDim();
            n += nar;
        }
        if (pma != null) {
            int nma = pma.getDomain().getDim();
            n += nma;
        }
        return n;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return SsfArima.defaultLoading();
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }
    
    @Override
    public int stateDim() {
        int p = 0;
        if (par != null) {
            p = par.getDomain().getDim();
        }
        int q = 0;
        if (pma != null) {
            q = pma.getDomain().getDim();
        }
        return Math.max(p, q + 1);
    }
}
