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
package jdplus.advancedsa.base.core.tdarima;

import jdplus.advancedsa.base.api.tdarima.LtdSarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;

/**
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class LtdSarimaModel {

    private final LtdSarimaSpec.Orders spec;
    private final DoubleSeq p0, p1;
    private final double var0, var1;
    private final int n;

    public static Builder builder() {
        return new Builder().var0(1).var1(1);
    }

    public static LtdSarimaModel of(LtdSarimaSpec.Orders spec, DoubleSeq p, int n) {
        return new LtdSarimaModel(spec, p, p, 1, 1, n);
    }

    public Ssf ssf() {

        DoubleSeq delta = DoublesMath.subtract(p1, p0);
        double r = 1.0 / (n - 1);

        return TdSsfArima.ssf(
                n,
                i -> {
                    DataBlock q = DataBlock.of(p0);
                    q.addAY(i * r, delta);
                    SarimaModel sarima =
                            SarimaModel.builder(spec.asSarimaOrders()).parameters(q).build();
                    double e0 = var0 == 1 ? 1 : Math.sqrt(var0),
                            e1 = var1 == 1 ? 1 : Math.sqrt(var1);
                    double et = e0 + (i * r) * (e1 - e0);
                    return new ArimaModel(
                            sarima.getStationaryAr(),
                            sarima.getNonStationaryAr(),
                            sarima.getMa(),
                            et * et);
                });
    }

    public boolean isTimeVarying() {
        if (var1 != var0) {
            return true;
        }
        if (p0.equals(p1)) {
            return false;
        }
        return !p0.hasSameContentAs(p1);
    }

    public LtdSarimaSpec.Orders getStationaryOrders() {
        return spec.doStationary();
    }

    public LtdSarimaSpec spec() {

        LtdSarimaSpec.Builder builder = LtdSarimaSpec.builder();
        builder.orders(spec);
        int j = 0, p = spec.getP();
        if (p > 0) {
            builder.vPhi(!p0.range(j, j + p).hasSameContentAs(p1.range(j, j + p)));
            j += p;
        }
        p = spec.getBp();
        if (p > 0) {
            builder.vBphi(!p0.range(j, j + p).hasSameContentAs(p1.range(j, j + p)));
            j += p;
        }
        p = spec.getQ();
        if (p > 0) {
            builder.vTheta(!p0.range(j, j + p).hasSameContentAs(p1.range(j, j + p)));
            j += p;
        }
        p = spec.getBq();
        if (p > 0) {
            builder.vBtheta(!p0.range(j, j + p).hasSameContentAs(p1.range(j, j + p)));
        }
        builder.vVar(var1 != var0);
        return builder.build();
    }
}
