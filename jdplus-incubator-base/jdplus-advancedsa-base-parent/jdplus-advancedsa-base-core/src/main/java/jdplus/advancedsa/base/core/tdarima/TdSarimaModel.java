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

import java.util.function.IntFunction;
import jdplus.advancedsa.base.api.tdarima.Orders;
import jdplus.advancedsa.base.api.tdarima.TdEstimationSpec;
import jdplus.advancedsa.base.api.tdarima.TdSarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;

/**
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class TdSarimaModel {

    private int period;
    private final Orders orders;
    private final TdEstimationSpec.Transformation fn;
    private final DoubleSeq p0, p1;
    private final double var0, var1;
    private final int startArimaRamp, endArimaRamp, startVarianceRamp, endVarianceRamp;
    private final int n;

    public static Builder builder() {
        return new Builder().var0(1).var1(1).fn(TdEstimationSpec.Transformation.LINEAR);
    }

    public static TdSarimaModel of(int period, Orders spec, DoubleSeq p, int n) {
        return new TdSarimaModel(
                period, spec, TdEstimationSpec.Transformation.LINEAR, p, p, 1, 1, n, 0, n - 1, 0, n - 1);
    }

    public static TdSarimaModel of(
            int period,
            Orders spec,
            DoubleSeq p,
            int n,
            int startArimaRamp,
            int endArimaRamp,
            int startVarianceRamp,
            int endVarianceRamp) {
        return new TdSarimaModel(
                period,
                spec,
                TdEstimationSpec.Transformation.LINEAR,
                p,
                p,
                1,
                1,
                n,
                startArimaRamp,
                endArimaRamp,
                startVarianceRamp,
                endVarianceRamp);
    }

    public IntFunction<IArimaModel> models() {
        DoubleSeq delta = DoublesMath.subtract(p1, p0);
        return i -> {
            DataBlock q;
            if (i >= endArimaRamp) {
                q = DataBlock.of(p1);
            } else {
                q = DataBlock.of(p0);
                if (i > startArimaRamp) {
                    int ic = i - startArimaRamp;
                    double rp = 1.0 / (endArimaRamp - startArimaRamp);
                    if (fn == TdEstimationSpec.Transformation.LINEAR) {
                        q.addAY(ic * rp, delta);
                    } else {
                        double x = ic * rp, x2 = x * x, x3 = x * x2;
                        q = DataBlock.of(p0);
                        q.addAY(3 * x2, delta);
                        q.addAY(-2 * x3, delta);
                    }
                }
            }
            SarimaModel sarima = SarimaModel.builder(orders.asSarimaOrders(period))
                    .parameters(q)
                    .build();
            double e = var1 == 1 ? 1 : Math.sqrt(var1);
            if (var0 != var1) {
                if (i < endVarianceRamp) {
                    double e1 = e;
                    e = var0 == 1 ? 1 : Math.sqrt(var0);
                    if (i >= startVarianceRamp) {
                        int ic = i - startVarianceRamp;
                        double rv = 1.0 / (endVarianceRamp - startVarianceRamp);
                        e += (ic * rv) * (e1 - e);
                    }
                }
            }
            return new ArimaModel(sarima.getStationaryAr(), sarima.getNonStationaryAr(), sarima.getMa(), e * e);
        };
    }

    public Ssf ssf() {
        return TdSsfArima.ssf(n, models());
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

    public Orders getStationaryOrders() {
        return orders.doStationary();
    }

    public TdSarimaSpec spec() {

        TdSarimaSpec.Builder builder = TdSarimaSpec.builder();
        builder.orders(orders);
        int j = 0, p = orders.getP();
        if (p > 0) {
            builder.vPhi(!p0.range(j, j + p).hasSameContentAs(p1.range(j, j + p)));
            j += p;
        }
        p = orders.getBp();
        if (p > 0) {
            builder.vBphi(!p0.range(j, j + p).hasSameContentAs(p1.range(j, j + p)));
            j += p;
        }
        p = orders.getQ();
        if (p > 0) {
            builder.vTheta(!p0.range(j, j + p).hasSameContentAs(p1.range(j, j + p)));
            j += p;
        }
        p = orders.getBq();
        if (p > 0) {
            builder.vBtheta(!p0.range(j, j + p).hasSameContentAs(p1.range(j, j + p)));
        }
        builder.vVar(var1 != var0);
        return builder.build();
    }
}
