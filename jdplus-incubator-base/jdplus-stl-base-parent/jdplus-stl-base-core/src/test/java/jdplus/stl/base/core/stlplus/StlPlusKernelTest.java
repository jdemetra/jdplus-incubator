/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stl.base.core.stlplus;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.stl.base.api.StlPlusSpec;
import jdplus.stl.base.api.StlSpec;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import java.util.Arrays;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class StlPlusKernelTest {

    public StlPlusKernelTest() {
    }

    @Test
    public void testPreprocessing() {
 
        StlPlusKernel kernel = StlPlusKernel.of(StlPlusSpec.FULL, null);
        StlPlusResults rslt = kernel.process(Data.TS_ABS_RETAIL, null);
        TsDataTable table = TsDataTable.of(Arrays.asList(
                rslt.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value),
                rslt.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value),
                rslt.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value),
                rslt.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value)
        ));

//        System.out.println(table);
    }

    @Test
    public void testNoPreprocessing() {
        StlPlusSpec spec = StlPlusSpec.builder()
                .preprocessing(ModellingSpec.DISABLED)
                .stl(StlSpec.createDefault(12, true, true))
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
                .build();

        StlPlusKernel kernel = StlPlusKernel.of(spec, null);
        StlPlusResults rslt = kernel.process(Data.TS_ABS_RETAIL, null);
        TsDataTable table = TsDataTable.of(Arrays.asList(
                rslt.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value),
                rslt.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value),
                rslt.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value),
                rslt.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value)
        ));

//        System.out.println(table);
    }

    public static void main(String[] args) {
        StlPlusSpec spec = StlPlusSpec.builder()
                .preprocessing(ModellingSpec.FULL)
                .stl(StlSpec.createDefault(12, true, true))
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
                .build();

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            StlPlusKernel kernel = StlPlusKernel.of(spec, null);
            StlPlusResults rslt = kernel.process(Data.TS_PROD, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
