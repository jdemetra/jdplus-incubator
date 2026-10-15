/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.advancedsa.base.api.tdarima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarmaOrders;

/**
 *
 * @author Jean Palate
 */
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
@lombok.Value
public class LtdSarimaSpec {

    @lombok.Value
    @lombok.Builder(toBuilder = true, builderClassName = "Builder")
    public static class Orders {

        @lombok.With
        int period;
        int p, d, q, bp, bd, bq;

        public static Orders of(SarmaOrders spec) {
            return builder()
                    .period(spec.getPeriod())
                    .p(spec.getP())
                    .q(spec.getQ())
                    .bp(spec.getBp())
                    .bq(spec.getBq())
                    .build();
        }

        public static Orders of(SarimaOrders spec) {
            return builder()
                    .period(spec.getPeriod())
                    .p(spec.getP())
                    .d(spec.getD())
                    .q(spec.getQ())
                    .bp(spec.getBp())
                    .bd(spec.getBd())
                    .bq(spec.getBq())
                    .build();
        }

        public static Orders airline(int period) {
            return builder()
                    .period(period)
                    .d(1)
                    .q(1)
                    .bd(1)
                    .bq(1)
                    .build();
        }

        public SarimaOrders asSarimaOrders() {
            SarimaOrders orders = new SarimaOrders(period);
            orders.setRegular(p, d, q);
            orders.setSeasonal(bp, bd, bq);
            return orders;
        }

        public int getParametersCount() {
            return p + q + bp + bq;
        }

        public Orders doStationary() {
            return toBuilder().d(0).bd(0).build();
        }
    }

    public boolean isTimeVarying() {
        return (orders.getP()> 0 && vPhi) || (orders.getBp()>0 && vBphi) || (orders.getQ()>0 && vTheta) || (orders.getBq()>0 && vBtheta) || vVar;
    }

    @lombok.With
    private final Orders orders;
    private final boolean vPhi, vBphi, vTheta, vBtheta, vVar;

    public LtdSarimaSpec withPeriod(int p) {
        return withOrders(orders.withPeriod(p));
    }

    public int parametersCount() {
        int n = 0;
        int o = orders.getP();
        if (o > 0) {
            n += vPhi ? 2 * o : o;
        }
        o = orders.getBp();
        if (o > 0) {
            n += vBphi ? 2 * o : o;
        }
        o = orders.getQ();
        if (o > 0) {
            n += vTheta ? 2 * o : o;
        }
        o = orders.getBq();
        if (o > 0) {
            n += vBtheta ? 2 * o : o;
        }
        if (vVar) {
            ++n;
        }
        return n;
    }
}
