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

import jdplus.advancedsa.base.api.tdarima.Orders;
import jdplus.advancedsa.base.api.tdarima.TdEstimationSpec;
import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.advancedsa.base.api.tdarima.TdSarimaSpec;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 * @author Jean Palate
 */
public class TdModellingKernelTest {

    public TdModellingKernelTest() {}

    @Test
    public void testAirline() {
        TsData[] s = Data.retail_us();

        TdSarimaSpec tspec = TdSarimaSpec.builder()
                .vTheta(true)
                .vBtheta(true)
                //                .vVar(true)
                .orders(Orders.airline())
                .build();

        TdEstimationSpec espec = TdEstimationSpec.builder()
                .parametrization(TdEstimationSpec.Parametrization.MEAN_DELTA)
                .simplificationThreshold(0)
                .tdspec(tspec)
                .build();

        TdModelSpec spec = TdModelSpec.FULL.toBuilder().tdarima(espec).build();

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < s.length; ++i) {
            TdModellingKernel kernel = new TdModellingKernel();
            TdModellingResults result =
                    kernel.process(s[i], spec, ModellingContext.getActiveContext(), ProcessingLog.dummy());
            TdResults tdarima = result.getTdarima();
            if (tdarima.isTimeVarying()) {
                SeriesDecomposition decomp = result.getDecomposition().getTdStochasticSeries();
                TsData esa = result.getData("decomposition.tdsarima.sastochastics.sa_e", TsData.class);
                TsData et = result.getData("decomposition.tdsarima.sastochastics.t_e", TsData.class);
                TsData ei = result.getData("decomposition.tdsarima.sastochastics.i_e", TsData.class);
                //                System.out.println(decomp.getSeries(ComponentType.Series, ComponentInformation.Value)
                //                        .getValues());
                //                System.out.println(decomp.getSeries(ComponentType.Trend, ComponentInformation.Value)
                //                        .getValues());
                //                System.out.println(decomp.getSeries(ComponentType.Seasonal,
                // ComponentInformation.Value)
                //                        .getValues());
                //                System.out.println(decomp.getSeries(ComponentType.Irregular,
                // ComponentInformation.Value)
                //                        .getValues());
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
}
