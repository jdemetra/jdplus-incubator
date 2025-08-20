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

import java.util.ArrayList;
import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.toolkit.base.core.ssf.StateComponent;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ArInterpreter;
import jdplus.sts.base.core.msts.MAInterpreter;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.sts.base.core.ssf.TimeVaryingSsfArima;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.basic.Loading;

/**
 * Linear time varying airline model
 *
 * @author palatej
 */
public class LtdArimaItem extends StateItem {

    private final int n, period, d, bd;
    private final ArInterpreter ar;
    private final MAInterpreter pth0, pth1, pbth0, pbth1;
    private final VarianceInterpreter v;

    public LtdArimaItem(final String name, int n, int period, int d, int bd, double[] ar, boolean fixedar,
            double[] th, boolean fixedth, double[] bth, boolean fixedbth, double var, boolean fixedvar) {
        super(name);
        this.n = n;
        this.period = period;
        this.d = d;
        this.bd = bd;
        if (ar != null) {
            this.ar = new ArInterpreter(name + ".ar", ar, fixedar);
        } else {
            this.ar = null;
        }
        if (th == null) {
            pth0 = null;
            pth1 = null;
        } else if (th.length == 1) {
            pth0 = new MAInterpreter("th", th[0], fixedth);
            pth1 = null;
        } else if (th.length == 2) {
            pth0 = new MAInterpreter("th0", th[0], fixedth);
            pth1 = new MAInterpreter("th1", th[1], fixedth);
        } else {
            throw new IllegalArgumentException("th");
        }
        if (bth == null) {
            pbth0 = null;
            pbth1 = null;
        } else if (bth.length == 1) {
            pbth0 = new MAInterpreter("bth", bth[0], fixedbth);
            pbth1 = null;
        } else if (bth.length == 2) {
            pbth0 = new MAInterpreter("bth0", bth[0], fixedbth);
            pbth1 = new MAInterpreter("bth1", bth[1], fixedbth);
        } else {
            throw new IllegalArgumentException("th");
        }
        v = new VarianceInterpreter(name + ".var", var, fixedvar, true);
    }

    private LtdArimaItem(LtdArimaItem item) {
        super(item.name);
        this.n = item.n;
        this.period = item.period;
        this.d = item.d;
        this.bd = item.bd;
        this.ar = item.ar == null ? null : item.ar.duplicate();
        this.pth0 = item.pth0 == null ? null : item.pth0.duplicate();
        this.pth1 = item.pth1 == null ? null : item.pth1.duplicate();
        this.pbth0 = item.pbth0 == null ? null : item.pbth0.duplicate();
        this.pbth1 = item.pbth1 == null ? null : item.pbth1.duplicate();
        this.v = item.v.duplicate();
    }

    @Override
    public LtdArimaItem duplicate() {
        return new LtdArimaItem(this);
    }

    private int nth() {
        int m = 0;
        if (null != pth0) {
            ++m;
        }
        if (null != pth1) {
            ++m;
        }
        return m;
    }

    private int nbth() {
        int m = 0;
        if (null != pbth0) {
            ++m;
        }
        if (null != pbth1) {
            ++m;
        }
        return m;
    }

    private int nar() {
        if (ar == null) {
            return 0;
        } else {
            return ar.getDomain().getDim();
        }
    }

    @Override
    public void addTo(MstsMapping mapping) {
        if (ar != null) {
            mapping.add(ar);
        }
        if (pth0 != null) {
            mapping.add(pth0);
        }
        if (pth1 != null) {
            mapping.add(pth1);
        }
        if (pbth0 != null) {
            mapping.add(pbth0);
        }
        if (pbth1 != null) {
            mapping.add(pbth1);
        }
        mapping.add(v);
        mapping.add((p, builder) -> {
            builder.add(name, build(p), Loading.fromPosition(0));
            return parametersCount();
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        ArrayList<ParameterInterpreter> list = new ArrayList<>();
        if (ar != null) {
            list.add(ar);
        }
        if (pth0 != null) {
            list.add(pth0);
        }
        if (pth1 != null) {
            list.add(pth1);
        }
        if (pbth0 != null) {
            list.add(pbth0);
        }
        if (pbth1 != null) {
            list.add(pbth1);
        }
        list.add(v);
        return Collections.unmodifiableList(list);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double[] par;
        double th0, th1, bth0, bth1, var;
        if (p != null) {
            int m = 0;
            if (ar != null) {
                m += nar();
                par = p.extract(0, m).toArray();
            } else {
                par = null;
            }
            th0 = pth0 != null ? p.get(m++) : Double.NaN;
            th1 = pth1 != null ? p.get(m++) : Double.NaN;
            bth0 = pbth0 != null ? p.get(m++) : Double.NaN;
            bth1 = pbth1 != null ? p.get(m++) : Double.NaN;
            var = p.get(m);
        } else {
            if (ar != null) {
                par = ar.values().toArray();
            } else {
                par = null;
            }
            th0 = pth0 != null ? pth0.value() : Double.NaN;
            th1 = pth1 != null ? pth1.value() : Double.NaN;
            bth0 = pbth0 != null ? pbth0.value() : Double.NaN;
            bth1 = pbth1 != null ? pbth1.value() : Double.NaN;
            var = v.variance();
        }
        double[] th = linear(th0, th1);
        double[] bth = linear(bth0, bth1);
        return TimeVaryingSsfArima.of(n,
                (int idx) -> {
                    SarimaOrders orders = new SarimaOrders(period);
                    orders.setRegular(nar(), d, th != null ? 1 : 0);
                    orders.setSeasonal(0, bd, Double.isFinite(bth0) ? 1 : 0);
                    SarimaModel.Builder mbuilder = SarimaModel.builder(orders);
                    if (par != null) {
                        mbuilder.phi(par);
                    }
                    if (th != null) {
                        mbuilder.theta(th[idx]);
                    }
                    if (bth != null) {
                        mbuilder.btheta(bth[idx]);
                    }
                    SarimaModel sarima = mbuilder.build();
                    return new ArimaModel(sarima.getStationaryAr(),
                            sarima.getNonStationaryAr(),
                            sarima.getMa(), var);
                });
    }

    @Override
    public int parametersCount() {
        return 1 + nar() + nth() + nbth();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return Loading.fromPosition(0);
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        // TODO
        return 0;
    }

    public double[] linear(double a, double b) {
        if (!Double.isFinite(a)) {
            return null;
        }
        double[] s = new double[n];
        if (Double.isFinite(b)) {
            double d = (b - a) / (n - 1.0);
            s[0] = a;
            double cur = a;
            for (int i = 1; i < n; ++i) {
                cur += d;
                s[i] = cur;
            }
        } else {
            for (int i = 0; i < n; ++i) {
                s[i] = a;
            }
        }
        return s;
    }

}
