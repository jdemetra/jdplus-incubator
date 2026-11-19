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

/**
 *
 * @author Jean Palate
 */
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
@lombok.Value
public class TdSarimaSpec {

    public static final TdSarimaSpec DEFAULT = new Builder()
            .orders(Orders.airline())
            .vPhi(false)
            .vBphi(false)
            .vTheta(true)
            .vBtheta(true)
            .vVar(false)
            .build();

    public static final TdSarimaSpec FULL = new Builder()
            .orders(Orders.airline())
            .vPhi(true)
            .vBphi(true)
            .vTheta(true)
            .vBtheta(true)
            .vVar(true)
            .build();

    public boolean isTimeVarying() {
        return (orders.getP() > 0 && vPhi)
                || (orders.getBp() > 0 && vBphi)
                || (orders.getQ() > 0 && vTheta)
                || (orders.getBq() > 0 && vBtheta)
                || vVar;
    }

    @lombok.With
    private final Orders orders;

    private final boolean vPhi, vBphi, vTheta, vBtheta, vVar;

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
