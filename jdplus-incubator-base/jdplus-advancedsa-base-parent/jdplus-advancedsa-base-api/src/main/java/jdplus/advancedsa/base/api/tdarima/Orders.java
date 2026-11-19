/*
 * Copyright 2026 JDemetra+.
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
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class Orders {

    int p, d, q, bp, bd, bq;

    public static Orders of(SarmaOrders spec) {
        return builder()
                .p(spec.getP())
                .q(spec.getQ())
                .bp(spec.getBp())
                .bq(spec.getBq())
                .build();
    }

    public static Orders of(SarimaOrders spec) {
        return builder()
                .p(spec.getP())
                .d(spec.getD())
                .q(spec.getQ())
                .bp(spec.getBp())
                .bd(spec.getBd())
                .bq(spec.getBq())
                .build();
    }

    public static Orders airline() {
        return builder().d(1).q(1).bd(1).bq(1).build();
    }

    public SarimaOrders asSarimaOrders(int period) {
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
