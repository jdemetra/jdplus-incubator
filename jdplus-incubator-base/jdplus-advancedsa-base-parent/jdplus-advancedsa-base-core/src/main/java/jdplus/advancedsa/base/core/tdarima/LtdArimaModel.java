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

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class LtdArimaModel {

    private final SarimaOrders spec;
    private final DoubleSeq p0, p1;
    private final double var1;
    private final int n;
    
    public static LtdArimaModel of(SarimaOrders spec, DoubleSeq p, int n){
        return new LtdArimaModel(spec, p, p, 1, n);
    }

    public Ssf ssf() {

        DoubleSeq delta = DoublesMath.subtract(p1, p0);
        double r = 1.0 / (n - 1);

        return TdSsfArima.ssf(n, i -> {
            DataBlock q = DataBlock.of(p0);
            q.addAY(i * r, delta);
            SarimaModel sarima = SarimaModel.builder(spec)
                    .parameters(q)
                    .build();
            return new ArimaModel(sarima.getStationaryAr(), sarima.getNonStationaryAr(), sarima.getMa(), 1 + (i * r) * (var1 - 1));
        });
    }
    

}
