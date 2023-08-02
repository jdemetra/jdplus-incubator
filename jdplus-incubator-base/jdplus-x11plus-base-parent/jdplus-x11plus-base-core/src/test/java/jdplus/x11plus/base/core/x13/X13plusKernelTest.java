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
package jdplus.x11plus.base.core.x13;

import java.util.Arrays;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.x11plus.base.api.SeasonalFilterOption;
import jdplus.x11plus.base.api.X11plusSpec;
import tck.demetra.data.Data;
import jdplus.x11plus.base.api.X13plusSpec;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class X13plusKernelTest {

    public X13plusKernelTest() {
    }

    @Test
    public void testPreprocessing() {

        X13plusSpec spec = X13plusSpec.builder()
                .preprocessing(ModellingSpec.FULL)
                .x11(null)
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
                .build();
        X13plusKernel kernel = X13plusKernel.of(spec, null);
        X13plusResults rslt = kernel.process(Data.TS_ABS_RETAIL, null);
    }

    @Test
    public void testNoPreprocessing() {
        X13plusSpec spec = X13plusSpec.builder()
                .preprocessing(ModellingSpec.DISABLED)
                .x11(X11plusSpec.createDefault(true, 12, SeasonalFilterOption.S3X5))
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
                .build();

        X13plusKernel kernel = X13plusKernel.of(spec, null);
        X13plusResults rslt = kernel.process(Data.TS_ABS_RETAIL, null);
     }

    public static void main(String[] args) {
        X13plusSpec spec = X13plusSpec.builder()
                .preprocessing(ModellingSpec.FULL)
                .x11(X11plusSpec.createDefault(true, 12, SeasonalFilterOption.S3X5))
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
                .build();

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            X13plusKernel kernel = X13plusKernel.of(spec, null);
            X13plusResults rslt = kernel.process(Data.TS_PROD, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
