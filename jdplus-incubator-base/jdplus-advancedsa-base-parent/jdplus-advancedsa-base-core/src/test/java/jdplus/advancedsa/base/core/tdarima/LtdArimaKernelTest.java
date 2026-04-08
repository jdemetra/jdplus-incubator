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

import jdplus.advancedsa.base.api.tdarima.LtdArimaSpec;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.stats.StatisticalTest;
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
    public void testDetails() {

        SarimaSpec arima = SarimaSpec.airline();
        LtdArimaSpec spec = LtdArimaSpec.builder()
                .sarimaSpec(arima)
                .parametrization(LtdArimaSpec.Parametrization.MEAN_DELTA)
                .vTheta(true)
                .vBtheta(true)
                //                .vVar(true)
                .build();

        LtdArimaKernel.ParametersDetails details = new LtdArimaKernel.ParametersDetails(spec);
//        System.out.println(details);
        spec = spec.toBuilder().vVar(true).build();
        details = new LtdArimaKernel.ParametersDetails(spec);
//        System.out.println(details);
        arima = arima.toBuilder().p(3).build();
        spec = spec.toBuilder().sarimaSpec(arima).build();
        details = new LtdArimaKernel.ParametersDetails(spec);
//        System.out.println(details);
        spec = spec.toBuilder().vVar(false).build();
        details = new LtdArimaKernel.ParametersDetails(spec);
//        System.out.println(details);

    }

    @Test
    public void testAirline() {
        TsData[] s = Data.retail_us();

        LtdArimaSpec spec = LtdArimaSpec.builder()
                .sarimaSpec(SarimaSpec.airline())
                .parametrization(LtdArimaSpec.Parametrization.MEAN_DELTA)
                .vTheta(true)
                .vBtheta(true)
                .vVar(false)
                .build();

        LtdArimaKernel kernel = LtdArimaKernel.of(spec);

        long t0 = System.currentTimeMillis();
        for (int i = 34; i < s.length; ++i) {
            LtdArimaResults result = kernel.process(s[i].getValues(), s[i].getAnnualFrequency(), false, null);
//            System.out.print(result.getStart().getLl().getLogLikelihood());
//            System.out.print('\t');
//            System.out.print(result.getLtd().getLl().getLogLikelihood());
//            System.out.print('\t');
//            System.out.print(result.getStart().parameters());
            StatisticalTest test = result.getLtd().getStationaryTest();
//            System.out.print(test == null ? Double.NaN : test.getPvalue());
//            System.out.print('\t');
//            System.out.print(result.getLtd().getLikelihoodRatioTest().getPvalue());
//            System.out.print('\t');
//            System.out.print(s[i].length());
//            System.out.print('\t');
//
//            System.out.println(result.getMax().getScore());
//
//            DoubleSeq t = DoublesMath.divide(result.getLtd().getParameters(), result.getLtd().getParametersCovariance().diagonal().sqrt());
//            System.out.println();
            DoubleSeq t = result.getLtd().getParameters();
//            System.out.println(t);
        }
        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
    }

    @Test
    public void test311011() {
        TsData[] s = Data.retail_us();
        SarimaSpec airline = SarimaSpec.airline();
        SarimaSpec aspec = airline.toBuilder().p(3).build();

        LtdArimaSpec spec = LtdArimaSpec.builder()
                .parametrization(LtdArimaSpec.Parametrization.START_END)
                .sarimaSpec(aspec)
                .vTheta(true)
                .vBtheta(true)
                .vPhi(false)
                //                .vVar(true)
                .build();

        LtdArimaKernel kernel = LtdArimaKernel.of(spec);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < s.length; ++i) {
            LtdArimaResults result = kernel.process(s[i].log().getValues(), s[i].getAnnualFrequency(), false, null);
//            System.out.print(result.getStart().getLl().getLogLikelihood());
//            System.out.print('\t');
//            System.out.print(result.getLtd().getLl().getLogLikelihood());
            StatisticalTest test = result.getLtd().getStationaryTest();
//            System.out.print('\t');
//            System.out.print(test == null ? Double.NaN : test.getPvalue());
//            System.out.print('\t');
//            System.out.println(result.getLtd().getLikelihoodRatioTest().getPvalue());
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
//        System.out.println(t1 - t0);
    }
}
