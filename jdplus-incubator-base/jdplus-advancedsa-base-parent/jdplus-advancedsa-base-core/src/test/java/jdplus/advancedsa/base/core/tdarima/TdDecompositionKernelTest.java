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

import java.util.Map;
import jdplus.advancedsa.base.api.tdarima.Orders;
import jdplus.advancedsa.base.api.tdarima.TdEstimationSpec;
import jdplus.advancedsa.base.api.tdarima.TdSarimaSpec;
import jdplus.toolkit.base.api.information.InformationExtractors;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 * @author Jean Palate
 */
public class TdDecompositionKernelTest {

    public TdDecompositionKernelTest() {}

    @Test
    public void testDetails() {

        Orders arima = Orders.airline();
        TdSarimaSpec modelspec = TdSarimaSpec.builder()
                .orders(arima)
                .vTheta(true)
                .vBtheta(true)
                //                .vVar(true)
                .build();
        TdEstimationSpec spec = TdEstimationSpec.builder()
                .parametrization(TdEstimationSpec.Parametrization.MEAN_DELTA)
                .build();

        TdSarimaMapping.ParametersDetails details =
                new TdSarimaMapping.ParametersDetails(modelspec, spec.getParametrization());
        System.out.println(details);
        modelspec = modelspec.toBuilder().vVar(true).build();
        details = new TdSarimaMapping.ParametersDetails(modelspec, spec.getParametrization());
        System.out.println(details);
        arima = arima.toBuilder().p(3).build();
        modelspec = modelspec.toBuilder().orders(arima).build();
        details = new TdSarimaMapping.ParametersDetails(modelspec, spec.getParametrization());
        System.out.println(details);
        modelspec = modelspec.toBuilder().vVar(false).build();
        details = new TdSarimaMapping.ParametersDetails(modelspec, spec.getParametrization());
        System.out.println(details);
    }

    @Test
    public void testAirline() {
        TsData[] s = Data.retail_us();

        TdSarimaSpec tspec = TdSarimaSpec.builder()
                .vTheta(true)
                .vBtheta(true)
                //                .vVar(true)
                .orders(Orders.airline())
                .build();

        TdEstimationSpec spec = TdEstimationSpec.builder()
                .parametrization(TdEstimationSpec.Parametrization.MEAN_DELTA)
                .simplificationThreshold(0)
                .tdspec(tspec)
                .build();

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < s.length; ++i) {
            spec = spec.withTdspec(tspec);
            TdKernel kernel = TdKernel.of(spec);
            TdResults result =
                    kernel.process(s[i].getValues(), s[i].getAnnualFrequency(), false, null, ProcessingLog.dummy());
            if (result.isTimeVarying()) {
                result.getData("final.regression.effect", double[].class);
            }
            //
            // System.out.print(result.getStart().getLikelihood().getLogLikelihood());
            // System.out.print('\t');
            //
            // System.out.println(result.finalResults().getLikelihood().getLogLikelihood());
            //            System.out.print('\t');
            //            System.out.print(result.getStart().getLikelihood().getLogLikelihood());
            //            System.out.print('\t');
            //            System.out.print(result.getLtdResults().size());
            //            System.out.print('\t');
            //            System.out.print(result.getStart().parameters());
            //            StatisticalTest test = result.finalResults(). .getStationaryTest();
            //            System.out.print(test == null ? Double.NaN : test.getPvalue());
            //            System.out.print('\t');
            //            System.out.print(result.getLtd().getLikelihoodRatioTest().getPvalue());
            //            System.out.print('\t');
            //            System.out.print(s[i].length());
            //            System.out.print('\t');
            //
            //            System.out.println(result.getMax().getScore());
            //
            //            DoubleSeq t = DoublesMath.divide(result.getLtd().getParameters(),
            // result.getLtd().getParametersCovariance().diagonal().sqrt());
            //            System.out.println();
            //            DoubleSeq t = result.finalResults().getParameters();
            //            System.out.println(t);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void test311011() {
        InformationExtractors.reloadExtractors();
        TsData[] s = Data.retail_us();
        Orders airline = Orders.airline();
        Orders aspec = airline.toBuilder().p(3).build();
        TdSarimaSpec tspec = TdSarimaSpec.builder()
                .vTheta(true)
                .vBtheta(true)
                .vPhi(true)
                .orders(aspec)
                .build();

        TdEstimationSpec spec = TdEstimationSpec.builder()
                .parametrization(TdEstimationSpec.Parametrization.MEAN_DELTA)
                .simplificationThreshold(0)
                .tdspec(tspec)
                .build();

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < s.length; ++i) {
            spec = spec.withTdspec(tspec);
            TdKernel kernel = TdKernel.of(spec);
            TdResults result =
                    kernel.process(s[i].getValues(), s[i].getAnnualFrequency(), false, null, ProcessingLog.dummy());
            //
            // System.out.print(result.initialResults().getLikelihood().getLogLikelihood());
            //            System.out.print('\t');
            //
            // System.out.print(result.finalResults().getLikelihood().getLogLikelihood());
            //            System.out.print('\t');
            //            System.out.print(result.getStart().getLikelihood().getLogLikelihood());
            //            System.out.print('\t');
            //            System.out.print(result.getLtdResults().size());
            //            StatisticalTest test = result.getLtd().getStationaryTest();
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
            //            DoubleSeq t = DoublesMath.divide(result.getMax().getParameters(),
            // result.getMax().asymptoticCovariance().diagonal().sqrt());
            //            System.out.println();
            //            //            System.out.println(t);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    public static void main(String[] args) {
        TsData s = Data.TS_PROD;
        Orders airline = Orders.airline();
        TdSarimaSpec tspec = TdSarimaSpec.builder()
                .orders(airline)
                .vTheta(true)
                .vBtheta(true)
                .build();
        TdEstimationSpec spec = TdEstimationSpec.builder()
                .parametrization(TdEstimationSpec.Parametrization.MEAN_DELTA)
                .tdspec(tspec)
                .build();

        TdKernel kernel = TdKernel.of(spec);

        TdResults result = kernel.process(s.getValues(), s.getAnnualFrequency(), false, null, ProcessingLog.dummy());
        Map<String, Class> dic = result.getDictionary();
        for (String e : dic.keySet()) {
            System.out.println(e);
        }
    }
}
