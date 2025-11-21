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
package jdplus.sts.base.core;

import jdplus.sts.base.api.BsmSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import jdplus.toolkit.base.core.stats.RobustStandardDeviationComputer;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class SsfOutlierDetectorTest {

    public SsfOutlierDetectorTest() {
    }

    @Test
    public void test1() {
        DoubleSeq y = DoubleSeq.of(Data.RETAIL_MOTORDEALERS).log();
        BsmSpec mspec = BsmSpec.builder()
                .noise(true)
                .level(true, true)
                .seasonal(SeasonalModel.Crude)
                .build();
        BsmKernel monitor = new BsmKernel(null);
        monitor.process(y, 12, mspec);
        BsmData result = monitor.result(true);

        SsfOutlierDetector sd = new SsfOutlierDetector(RobustStandardDeviationComputer.mad());
        sd.setBounds(0, y.length());
        sd.prepare(y.length());
        sd.process(y, result, null, 0);
//        System.out.println(sd.getTau());
    }

    public static void main(String[] args) {
        DoubleSeq y = DoubleSeq.of(Data.RETAIL_MOTORDEALERS).log();
        BsmSpec mspec = BsmSpec.builder()
                .noise(true)
                .level(true, true)
                .seasonal(SeasonalModel.Crude)
                .build();
        BsmKernel monitor = new BsmKernel(null);
        monitor.process(y, 12, mspec);
        BsmData result = monitor.result(true);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            SsfOutlierDetector sd = new SsfOutlierDetector(null);
            sd.setBounds(0, y.length());
            sd.prepare(y.length());
            sd.process(y, result, null, 0);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }

}
