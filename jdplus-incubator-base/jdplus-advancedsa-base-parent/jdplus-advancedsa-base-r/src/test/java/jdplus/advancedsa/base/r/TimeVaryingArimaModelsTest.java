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
package jdplus.advancedsa.base.r;

import jdplus.advancedsa.base.core.tdarima.LtdArimaResults;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingArimaModelsTest {

    public TimeVaryingArimaModelsTest() {
    }

    @Test
    public void testAirline() {
        TsData[] s = Data.retail_us();
        int[] regular = new int[]{0, 1, 1};
        int[] seasonal = new int[]{0, 1, 1};
        for (int i = 0; i < s.length; ++i) {

            LtdArimaResults result = TimeVaryingArimaModels.estimate(s[i].log().getValues().toArray(), s[i].getAnnualFrequency(), true, null, regular, seasonal, true, true, false, false, false, 1e-7, "mean_delta");
            assertTrue(result != null);
            System.out.print(result.getData("ll0.ll", Double.class));
            System.out.print('\t');
            System.out.print(result.getData("ll1.ll", Double.class));
            System.out.print('\t');
            System.out.println(result.getLtd().getModel().getVar1());
        }
    }

    @Test
    public void testDecomposition() {
        TsData s = Data.TS_ABS_RETAIL;
        int n = s.length();
        int[] regular = new int[]{0, 1, 1};
        int[] seasonal = new int[]{0, 1, 1};
        boolean vvar = true;
        LtdArimaResults result = TimeVaryingArimaModels.estimate(s.log().getValues().toArray(), s.getAnnualFrequency(), true, null, regular, seasonal, true, true, false, false, !vvar, 1e-7, "mean_delta");
        assertTrue(result != null);
        FastMatrix parameters = parameters(result.getLtd().getModel().getP0(), result.getLtd().getModel().getP1(), n);
        if (vvar) {
            FastMatrix V = FastMatrix.make(1, n);
            double var1 = result.getLtd().getModel().getVar1();
            double e1 = Math.sqrt(var1);
            V.row(0).set(j -> {
                double et = 1 + j * (e1 - 1) / (n - 1);
                return et * et;
            });
            parameters = MatrixFactory.rowBind(parameters, V);
        }

        Matrix sa = TimeVaryingArimaModels.arimaDecomposition(s.log().getValues().toArray(), s.getAnnualFrequency(), regular, seasonal, vvar, parameters, true);
        System.out.println(sa);
    }

    private FastMatrix parameters(DoubleSeq p0, DoubleSeq p1, int n) {
        DoubleSeq delta = DoublesMath.subtract(p1, p0);
        double d = n - 1;

        FastMatrix P = FastMatrix.make(p0.length(), n);
        for (int i = 0; i < n; ++i) {
            int cur = i;
            P.column(i).set(j -> p0.get(j) + delta.get(j) * cur / d);
        }
        return P;
    }
}
