/*
 * Copyright 2026 JDemetra+.
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
package jdplus.advancedsa.base.core.ssf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.DoubleFunction;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixStorage;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.akf.QAugmentation;
import jdplus.toolkit.base.core.ssf.arima.SsfUcarima;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ucarima.ModelDecomposer;
import jdplus.toolkit.base.core.ucarima.SeasonalSelector;
import jdplus.toolkit.base.core.ucarima.TrendCycleSelector;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class SsfWeightsTest {

    public SsfWeightsTest() {}

    @Test
    public void testWeights() {

        UcarimaModel ucm = ucm(12, -.4, -.8);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        int[] pos = ssf.componentsPosition();
        MatrixStorage storage = SsfWeights.smoothingWeights(ssf, 300);
        for (int i = 0; i < pos.length; ++i) {
            DataBlock item = storage.item(pos[i], 25);
            //            System.out.println(item);
        }
    }

    @Test
    public void testLinear() {

        UcarimaModel ucm = ucm(12, -.4, -.8);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        double[] a = new double[120];
        a[5] = 1;
        double[] b = new double[120];
        b[55] = 1;
        double[] c = new double[120];
        c[5] = 222;
        c[55] = 3;
        DefaultSmoothingResults sr = AkfToolkit.smooth(ssf, new SsfData(a), false, false, true, QAugmentation.QType.QR);
        DoubleSeq A = sr.getComponent(0);
        sr = AkfToolkit.smooth(ssf, new SsfData(b), false, false, false, QAugmentation.QType.QR);
        DoubleSeq B = sr.getComponent(0);
        sr = AkfToolkit.smooth(ssf, new SsfData(c), false, false, false, QAugmentation.QType.QR);
        DoubleSeq C = sr.getComponent(0);
        DataBlock D = DataBlock.of(C);
        D.addAY(-222, A);
        D.addAY(-3, B);

        assertTrue(D.normInf() < 1e-12);
    }

    @Test
    public void testSingleWeights() {

        UcarimaModel ucm = ucm(12, -.4, -.8);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        int obsPos = 35;
        FastMatrix M = FastMatrix.make(ssf.getStateDim(), 60);
        SsfWeights.smoothingWeights(ssf, obsPos, M);
        DataBlock R = M.rowSums();
        double s = ssf.measurement().loading().ZX(0, R);
        assertEquals(1, s, 0.000001);
    }

    @Test
    public void testAllWeights() {
        int N = 60;
        UcarimaModel ucm = ucm(12, -.4, -.8);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        MatrixStorage storage = SsfWeights.weights(ssf, N);
        for (int j = 0; j < N; ++j) {
            double s = 0;
            for (int i = 0; i < storage.getCurrentSize(); ++i) {
                DataBlock item = storage.matrix(i).column(j);
                s += item.sum();
            }
            assertEquals(1, s, 1e-12);
        }
        MatrixStorage storage2 = SsfWeights.weights2(ssf, N);
        for (int j = 0; j < N; ++j) {
            double s = 0;
            for (int i = 0; i < storage2.getCurrentSize(); ++i) {
                DataBlock item = storage2.matrix(i).column(j);
                s += item.sum();
            }
            assertEquals(1, s, 1e-12);
        }
    }

    private static UcarimaModel ucm(int period, double th, double bth) {
        SarimaModel airline = SarimaModel.builder(SarimaOrders.airline(period))
                .theta(th)
                .btheta(bth)
                .build();
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(period);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(airline);
        ucm = ucm.setVarianceMax(2, false);
        return ucm;
    }

    public static void main(String[] args) {
        int N = 301;
        UcarimaModel ucm = ucm(12, -.4, -.8);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        MatrixStorage weights = null;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10; ++i) {
            weights = SsfWeights.weights(ssf, N);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        int pos = N / 2;

        FiniteFilter ff0 = FiniteFilter.of(weights.matrix(0).column(pos).toArray(), -pos);
        DoubleFunction<Complex> fn0 = ff0.frequencyResponseFunction();
        FiniteFilter ff1 = FiniteFilter.of(weights.matrix(1).column(pos).toArray(), -pos);
        DoubleFunction<Complex> fn1 = ff1.frequencyResponseFunction();
        FiniteFilter ff2 = FiniteFilter.of(weights.matrix(2).column(pos).toArray(), -pos);
        DoubleFunction<Complex> fn2 = ff2.frequencyResponseFunction();

        for (int i = 0; i < N; ++i) {
            double f = Math.PI * i / (N - 1);
            Complex c = fn0.apply(f);
            System.out.print(c.absSquare());
            System.out.print('\t');
            System.out.print(c.arg());
            System.out.print('\t');
            c = fn1.apply(f);
            System.out.print(c.absSquare());
            System.out.print('\t');
            System.out.print(c.arg());
            System.out.print('\t');
            c = fn2.apply(f);
            System.out.print(c.absSquare());
            System.out.print('\t');
            System.out.println(c.arg());
        }
    }
}
