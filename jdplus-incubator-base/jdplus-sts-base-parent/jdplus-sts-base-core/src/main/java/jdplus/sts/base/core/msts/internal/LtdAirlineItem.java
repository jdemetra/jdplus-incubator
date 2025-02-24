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
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.toolkit.base.core.ssf.StateComponent;
import java.util.Arrays;
import java.util.List;
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
public class LtdAirlineItem extends StateItem {

    private final int n, period;
    private final MAInterpreter pth0, pth1, pbth0, pbth1;
    private final VarianceInterpreter v;

    public LtdAirlineItem(final String name, int n, int period, double th0, double th1, double bth0, double bth1, boolean fixedth, double var, boolean fixedvar) {
        super(name);
        this.n = n;
        this.period = period;
        pth0 = new MAInterpreter("th0", th0, fixedth);
        pth1 = new MAInterpreter("th1", th1, fixedth);
        pbth0 = new MAInterpreter("bth0", bth0, fixedth);
        pbth1 = new MAInterpreter("bth1", bth1, fixedth);
        v = new VarianceInterpreter(name + ".var", var, fixedvar, true);
    }

    private LtdAirlineItem(LtdAirlineItem item) {
        super(item.name);
        this.n = item.n;
        this.period = item.period;
        this.pth0 = item.pth0.duplicate();
        this.pth1 = item.pth1.duplicate();
        this.pbth0 = item.pbth0.duplicate();
        this.pbth1 = item.pbth1.duplicate();
        this.v = item.v.duplicate();
    }

    @Override
    public LtdAirlineItem duplicate() {
        return new LtdAirlineItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(pth0);
        mapping.add(pth1);
        mapping.add(pbth0);
        mapping.add(pbth1);
        mapping.add(v);
        mapping.add((p, builder) -> {
            double th0 = p.get(0);
            double th1 = p.get(1);
            double bth0 = p.get(2);
            double bth1 = p.get(3);
            double var = p.get(4);
            double[] th = linear(th0, th1);
            double[] bth = linear(bth0, bth1);
            StateComponent sc = TimeVaryingSsfArima.of(n,
                    (int idx) -> {
                        SarimaModel airline = SarimaModel.builder(SarimaOrders.airline(period))
                                .theta(th[idx])
                                .btheta(bth[idx])
                                .build();
                        return new ArimaModel(BackFilter.ONE,
                                airline.getNonStationaryAr(),
                                airline.getMa(), var);
                    });
            builder.add(name, sc, Loading.fromPosition(0));
            return 5;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(pth0, pth1, pbth0, pbth1, v);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double th0, th1, bth0, bth1, var;
        if (p == null) {
            th0 = pth0.value();
            th1 = pth1.value();
            bth0 = pbth0.value();
            bth1 = pbth1.value();
            var = v.variance();
        } else {
            th0 = p.get(0);
            th1 = p.get(1);
            bth0 = p.get(2);
            bth1 = p.get(3);
            var = p.get(4);
        }
        double[] th = linear(th0, th1);
        double[] bth = linear(bth0, bth1);
        return TimeVaryingSsfArima.of(n,
                (int idx) -> {
                    SarimaModel airline = SarimaModel.builder(SarimaOrders.airline(period))
                            .theta(th[idx])
                            .btheta(bth[idx])
                            .build();
                    return new ArimaModel(BackFilter.ONE,
                            airline.getNonStationaryAr(),
                            airline.getMa(), var);
                }
        );
    }

    @Override
    public int parametersCount() {
        return 5;
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
        return 2 * (period + 1);
    }

    public double[] linear(double a, double b) {
        double d = (b - a) / (n - 1.0);
        double[] s = new double[n];
        s[0] = a;
        double cur = a;
        for (int i = 1; i < n; ++i) {
            cur += d;
            s[i] = cur;
        }
        return s;
    }

}
