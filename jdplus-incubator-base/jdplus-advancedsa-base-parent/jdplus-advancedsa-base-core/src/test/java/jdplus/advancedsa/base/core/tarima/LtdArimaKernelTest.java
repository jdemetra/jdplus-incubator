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
package jdplus.advancedsa.base.core.tarima;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.timeseries.TsData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class LtdArimaKernelTest {

    public LtdArimaKernelTest() {
    }

    @Test
    public void testAirline() {
        TsData[] s = Data.retail_us();

        LtdArimaSpec spec = LtdArimaSpec.builder()
                .sarimaSpec(SarimaSpec.airline())
                .vTheta(true)
                .vBtheta(true)
                //                .vVar(true)
                .build();

        LtdArimaKernel kernel = LtdArimaKernel.of(spec);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < s.length; ++i) {
            LtdArimaResults result = kernel.process(s[i].log());
//            System.out.print(result.getLl0().logLikelihood());
//            System.out.print('\t');
//            System.out.println(result.getLl1().logLikelihood());
//            System.out.println(result.getStart().parameters());
//            System.out.println(result.getModel().getP0());
//            System.out.println(result.getModel().getP1());
//
//            System.out.println(result.getMax().getParameters());
//            System.out.println(result.getMax().getScore());
//
            DoubleSeq t = DoublesMath.divide(result.getMax().getParameters(), result.getMax().asymptoticCovariance().diagonal().sqrt());
//            System.out.println();
//            System.out.println(t);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

   @Test
    public void test311011() {
        TsData[] s = Data.retail_us();
        SarimaSpec airline = SarimaSpec.airline();
        SarimaSpec aspec = airline.toBuilder().p(3).build();
        
        LtdArimaSpec spec = LtdArimaSpec.builder()
                .sarimaSpec(aspec)
                .vTheta(true)
                .vBtheta(true)
                //                .vVar(true)
                .build();

        LtdArimaKernel kernel = LtdArimaKernel.of(spec);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < s.length; ++i) {
            LtdArimaResults result = kernel.process(s[i].log());
//            System.out.print(result.getLl0().logLikelihood());
//            System.out.print('\t');
//            System.out.println(result.getLl1().logLikelihood());
//            System.out.println(result.getStart().parameters());
//            System.out.println(result.getModel().getP0());
//            System.out.println(result.getModel().getP1());
//
//            System.out.println(result.getMax().getParameters());
//            System.out.println(result.getMax().getScore());
//
//            DoubleSeq t = DoublesMath.divide(result.getMax().getParameters(), result.getMax().asymptoticCovariance().diagonal().sqrt());
//            System.out.println();
//            System.out.println(t);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
