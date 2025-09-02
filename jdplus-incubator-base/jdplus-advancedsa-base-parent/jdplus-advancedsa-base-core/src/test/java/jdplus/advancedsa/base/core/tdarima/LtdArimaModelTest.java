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
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;
/**
 *
 * @author Jean Palate
 */
public class LtdArimaModelTest {
    
    public LtdArimaModelTest() {
        
    }
    
    @Test
    public void testAirline() {
        double[] s=Data.ABS_RETAIL2;
        DoubleSeq S=DataBlock.of(s);
                
        SarimaOrders orders=SarimaOrders.airline(12);
//        orders.setP(3);
        LtdArimaMapping mapping=LtdArimaMapping.builder(orders)
                .n(s.length)
//                .vPhi(true)
                .vTheta(true)
                .vBtheta(true)
//                .vVar(true)
                .build();
        SsfFunction fn = SsfFunction.builder(new SsfData(S), mapping, (LtdArimaModel model) -> model.ssf())
                .useScalingFactor(true)
                .useLog(false)
                .useParallelProcessing(true)
                .build();
        LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer.builder()
                .functionPrecision(1e-9)
                .build();
//        long t0=System.currentTimeMillis();
        min.minimize(fn.ssqEvaluate(mapping.getDefaultParameters()));
//        long t1=System.currentTimeMillis();
//        System.out.println(t1-t0);
//        System.out.println(min.getResult().getParameters());
    }
}
