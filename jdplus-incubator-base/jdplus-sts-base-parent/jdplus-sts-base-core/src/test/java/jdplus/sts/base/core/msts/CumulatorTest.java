/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.math.functions.Optimizer;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.StateStorage;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class CumulatorTest {

    public CumulatorTest() {}

    @Test
    public void testChowLin() {
        int m = Data.PCRA.length;
        int n = Data.IND_PCR.length;
        FastMatrix x = FastMatrix.make(n, 2);
        x.column(0).copyFrom(Data.IND_PCR, 0);
        // x.column(0).div(100);
        x.column(1).set(1);

        FastMatrix y = FastMatrix.make(n, 1);
        DataBlock edata = y.column(0);
        edata.set(Double.NaN);
        edata.extract(3, m, 4).copyFrom(Data.PCRA, 0);
        CompositeModel model = new CompositeModel();

        //        StateItem ar = AtomicModels.localLevel("ar", 1, true, Double.NaN);
        StateItem ar = AtomicModels.ar("ar", new double[] {.96}, false, 1, true, 0, 0);
        StateItem reg = AtomicModels.timeVaryingRegression("reg", x, 1, true);
        //        StateItem reg=AtomicModels.regression("reg", x);
        StateItem c = DerivedModels.aggregation("agg", reg, ar);
        StateItem disagg = DerivedModels.cumulator("disagg", c, 4, 0);

        model.add(disagg);
        ModelEquation eq = new ModelEquation("eq", 0, true);
        eq.add("disagg", disagg.defaultLoading(0));
        model.add(eq);

        CompositeModelEstimation rslt =
                model.estimate(y, false, true, SsfInitialization.Augmented, Optimizer.LevenbergMarquardt, 1e-15, null);
        StateStorage states = rslt.getSmoothedStates();

        DataBlock q = DataBlock.of(states.getComponent(1));
        DoubleSeq e = states.getComponent(3);
        for (int i = 0; i < q.length(); ++i) {
            q.mul(i, Data.IND_PCR[i]);
        }
        q.add(states.getComponent(2));
        q.add(e);
        //        System.out.println(q);
        //        System.out.println(states.getComponent(1));
        //        System.out.println(states.getComponent(2));
        //        System.out.println(states.getComponent(3));
        //        System.out.println(DoubleSeq.of(Data.PCRA));
        //        System.out.println(rslt.getParameters()[0]);
    }

    @Test
    public void testBug() {
        double[] y = new double[] {
            43.0, 49.6, 53.3, 60.1, 65.6, 70.9, 77.4, 83.4, 89.5, 96.5, 103.2, 113.5, 120.1, 128.0, 137.1, 144.0, 149.4
        };

        double[] x = new double[] {
            8795188, 9384774, 9283245, 10146297, 9203239, 9533075, 9340177, 11284950, 9048991, 9946120, 9543572,
            10771806, 9939084, 10191855, 9986458, 11054225, 9975887, 10419143, 10399077, 11439721, 10235215, 10848618,
            10550977, 11628756, 10470354, 11144702, 10900132, 12023012, 10847445, 11402097, 11176095, 12199783,
            10993689, 11805735, 11230590, 12360358, 11812980, 12301919, 12039686, 13435164, 11955684, 12701247,
            12393916, 13790830, 12682701, 13612501, 13247689, 14268808, 13252070, 13965811, 13121747, 14444971,
            13326156, 14216232, 13960841, 15432864, 14519439, 15337067, 14671082, 16279907, 15186929, 15873027,
            15733267, 17292163, 15524397, 16706777, 15850689, 17570354
        };

        FastMatrix yc = FastMatrix.make(4 * y.length, 1);
        yc.set(Double.NaN);
        yc.column(0).extract(3, y.length, 4).set(DoubleSeq.of(y));
        DoubleSeq X = DoubleSeq.of(x);

        StateItem vreg = AtomicModels.timeVaryingRegression(
                "x", X.toArray(), DoubleSeq.onMapping(x.length, i -> 1).toArray(), 1, false);
        StateItem cvreg = DerivedModels.cumulator("cumul", vreg, 4, 0);
        CompositeModel model = new CompositeModel();
        model.add(cvreg);
        CompositeModelEstimation rslts =
                model.estimate(yc, false, true, SsfInitialization.Augmented, Optimizer.LevenbergMarquardt, 1e-9, null);
        StateStorage ss = rslts.smoothedStates(true, null);
        DoubleSeq c = DoublesMath.multiply(ss.getComponent(1), X);
        System.out.println(c);
        ss = rslts.smoothedStates(false, null);
        c = DoublesMath.multiply(ss.getComponent(1), X);
        System.out.println(c);
        X = X.times(0.000001);
        vreg = AtomicModels.timeVaryingRegression(
                "x", X.toArray(), DoubleSeq.onMapping(x.length, i -> 1).toArray(), 1, false);
        cvreg = DerivedModels.cumulator("cumul", vreg, 4, 0);
        model = new CompositeModel();
        model.add(cvreg);
        rslts = model.estimate(yc, false, true, SsfInitialization.Augmented, Optimizer.LevenbergMarquardt, 1e-9, null);
        ss = rslts.smoothedStates(true, null);
        c = DoublesMath.multiply(ss.getComponent(1), X);
        System.out.println(c);
        ss = rslts.smoothedStates(false, null);
        c = DoublesMath.multiply(ss.getComponent(1), X);
        System.out.println(c);
    }
}
