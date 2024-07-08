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
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.SarimaInterpreter;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.ssf.StateComponent;
import java.util.Arrays;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;

/**
 *
 * @author palatej
 */
public class SarimaItem extends StateItem {

    private final VarianceInterpreter v;
    private final SarimaInterpreter p;

    public SarimaItem(final String name, int period, int[] orders, int[] seasonal, double[] parameters, boolean fixed, double var, boolean fixedvar) {
        super(name);
        SarimaOrders spec = new SarimaOrders(period);
        spec.setP(orders[0]);
        spec.setD(orders[1]);
        spec.setQ(orders[2]);
        if (seasonal != null) {
            spec.setBp(seasonal[0]);
            spec.setBd(seasonal[1]);
            spec.setBq(seasonal[2]);
        }
        v = new VarianceInterpreter(name + ".var", var, fixedvar, true);
        p = new SarimaInterpreter(name, spec, parameters, fixed);
    }

    private SarimaItem(SarimaItem item) {
        super(item.name);
        this.v = item.v.duplicate();
        this.p = item.p.duplicate();
    }

    @Override
    public SarimaItem duplicate() {
        return new SarimaItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add(p);
        SarimaOrders spec = p.getDomain().getSpec();
        mapping.add((p, builder) -> {
            double var = p.get(0);
            int np = spec.getParametersCount();
            StateComponent cmp;
            SarimaModel sarima = SarimaModel.builder(spec)
                    .parameters(p.extract(1, np))
                    .build();
            if (var == 1) {
                cmp = SsfArima.stateComponent(sarima);
            } else {
                ArimaModel arima = new ArimaModel(sarima.getStationaryAr(), sarima.getNonStationaryAr(), sarima.getMa(), var);
                cmp = SsfArima.stateComponent(arima);
            }
            builder.add(name, cmp, SsfArima.defaultLoading());
            return np + 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(v, p);
    }

    @Override
    public StateComponent build(DoubleSeq x) {
        SarimaOrders spec = p.getDomain().getSpec();
        double var = x == null ? v.variance() : x.get(0);
        int np = spec.getParametersCount();
        DoubleSeq z = x == null ? p.values() : x.extract(1, np);
        SarimaModel sarima = SarimaModel.builder(spec)
                .parameters(z)
                .build();
        StateComponent cmp;
        if (var == 1) {
            cmp = SsfArima.stateComponent(sarima);
        } else {
            ArimaModel arima = new ArimaModel(sarima.getStationaryAr(), sarima.getNonStationaryAr(), sarima.getMa(), var);
            cmp = SsfArima.stateComponent(arima);
        }
        return cmp;
    }

    @Override
    public int parametersCount() {
        SarimaOrders spec = p.getDomain().getSpec();
        return spec.getParametersCount() + 1;
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
        SarimaOrders spec = p.getDomain().getSpec();
        int p = spec.getP() + spec.getD();
        int q = spec.getQ();
        int s = spec.getPeriod();
        if (s > 0) {
            p += s * (spec.getBp() + spec.getBd());
            q += s * spec.getBq();
        }
        return Math.max(p, q + 1);
    }
}
