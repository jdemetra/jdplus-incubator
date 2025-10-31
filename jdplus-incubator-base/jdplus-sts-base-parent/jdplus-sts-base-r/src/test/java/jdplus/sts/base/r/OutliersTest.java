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
package jdplus.sts.base.r;

import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.core.BsmData;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class OutliersTest {

    public OutliersTest() {
    }

    @Test
    public void testRegArima() {
        double[] y = Data.ABS_RETAIL;
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(-.6)
                .btheta(-.6)
                .build();
        Matrix all = Outliers.regarimaOutlier(y, sarima, false, null, new String[]{"ao", "ls"}, "fast", true);
        System.out.println(all);
    }

    @Test
    public void testTramo() {
        double[] y = Data.RETAIL_BOOKSTORES;
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(-.6)
                .btheta(-.6)
                .build();
        Matrix all = Outliers.tramoOutliers(y, sarima, false, null, 3, new String[]{"ao", "ls"}, false, true, false);
        System.out.println(all);
    }

    @Test
    public void testX12() {
        double[] y = Data.RETAIL_BOOKSTORES;
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(-.6)
                .btheta(-.6)
                .build();
        Matrix all = Outliers.x12Outliers(y, sarima, false, null, 0, new String[]{"ao", "ls"}, true);
        System.out.println(all);
    }

    @Test
    public void testBsm() {
        double[] y = Data.RETAIL_BOOKSTORES;
        BsmSpec spec = BsmSpec.builder()
                .noise(true)
                .level(true, true)
                .seasonal(SeasonalModel.HarrisonStevens)
                .cycle(false)
                .build();
        Matrix all = Outliers.bsmOutliers(y, 12, spec, null, 2.7*2.7, true, true, false, true, "Full", "Point");
        System.out.println(all);
    }

    @Test
    public void testBsm2() {
        double[] y = Data.RETAIL_BOOKSTORES;
        BsmData model = BsmData.builder(12)
                .noiseVar(1)
                .levelVar(1)
                .slopeVar(1)
                .seasonalVar(1)
                .seasonalModel(SeasonalModel.HarrisonStevens)
                .build();
        BsmSpec spec = Bsm.specOf(model, false, true);
        Matrix all = Outliers.bsmOutliers(y, 12, spec, null, 2.5*2.5, true, true, false, true, "Full", "Point");
        System.out.println(all);
    }

    public static void main(String[] args) {
        double[] y = Data.ABS_RETAIL;
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(-.6)
                .btheta(-.6)
                .build();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            Matrix all = Outliers.regarimaOutlier(y, sarima, false, null, new String[]{"ao", "ls"}, "ansley", true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            Matrix all = Outliers.regarimaOutlier(y, sarima, false, null, new String[]{"ao", "ls"}, "fast", true);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            Matrix all = Outliers.regarimaOutlier(y, sarima, false, null, new String[]{"ao", "ls", "tc", "tc:0.9"}, "fast", false);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            Matrix all = Outliers.regarimaOutlier(y, sarima, false, null, new String[]{"ao", "ls", "tc", "tc:0.9"}, "ansley", false);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            Matrix all = Outliers.regarimaOutlier(y, sarima, false, null, new String[]{"ao", "ls", "tc", "tc:0.9"}, "kalman", false);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            Matrix all = Outliers.regarimaOutlier(y, sarima, false, null, new String[]{"ao", "ls", "tc", "tc:0.9"}, "ljungbox", false);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i) {
            Matrix all = Outliers.regarimaOutlier(y, sarima, false, null, new String[]{"ao", "ls", "tc", "tc:0.9"}, "x12", false);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
