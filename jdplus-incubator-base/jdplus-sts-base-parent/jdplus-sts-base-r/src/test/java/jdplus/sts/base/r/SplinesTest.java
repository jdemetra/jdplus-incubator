/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.sts.base.r;

import java.io.IOException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import tck.demetra.data.MatrixSerializer;
import jdplus.toolkit.base.api.math.functions.Optimizer;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.EasterRelatedDay;
import jdplus.toolkit.base.api.timeseries.calendars.FixedDay;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import jdplus.toolkit.base.api.timeseries.calendars.HolidaysOption;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.sts.base.core.msts.AtomicModels;
import jdplus.sts.base.core.msts.CompositeModel;
import jdplus.sts.base.core.msts.CompositeModelEstimation;
import jdplus.sts.base.core.msts.ModelEquation;
import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;
import jdplus.toolkit.base.core.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.toolkit.base.core.math.linearfilters.FilterUtility;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.LocalPolynomialFilters;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.splines.AdaptivePeriodicSpline;
import jdplus.toolkit.base.core.math.splines.AdaptivePeriodicSplines;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.timeseries.calendars.HolidaysUtility;
import tck.demetra.data.Data;

/**
 *
 * @author palatej
 */
public class SplinesTest {

    final static DoubleSeq SERIES;

    static {
        DoubleSeq y;
        try {
            InputStream stream = Data.class.getResourceAsStream("/tck/demetra/data/edf.txt");
            Matrix edf = MatrixSerializer.read(stream);
            y = edf.column(0);
        } catch (IOException ex) {
            y = null;
        }
        SERIES = y == null ? null : y.log();
    }

    private static void addDefault(List<Holiday> holidays) {
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.WHITMONDAY);
    }

    public static Holiday[] france() {
        List<Holiday> holidays = new ArrayList<>();
        addDefault(holidays);
        holidays.add(new FixedDay(5, 8));
        holidays.add(new FixedDay(7, 14));
        holidays.add(FixedDay.ARMISTICE);
        return holidays.stream().toArray(i -> new Holiday[i]);
    }

    public static void main2(String[] args) {
        DoubleSeq y = SERIES;

        TsPeriod start = TsPeriod.daily(1996, 1, 1);
        FastMatrix X = HolidaysUtility.regressionVariables(france(), TsDomain.of(start, y.length()), HolidaysOption.Skip, new int[]{6, 7}, false);

        long t0 = System.currentTimeMillis();

        int[] pos = new int[37];
        for (int i = 0; i < 33; ++i) {
            pos[i] = 10 * i;
        }
        pos[33] = 340;
        pos[34] = 350;
        pos[35] = 355;
        pos[36] = 360;

        CompositeModel model = new CompositeModel();
        //StateItem l = AtomicModels.localLevel("l", .01, false, Double.NaN);
        StateItem l = AtomicModels.localLinearTrend("l", .01, .01, false, false);
        StateItem sw = AtomicModels.seasonalComponent("sw", "HarrisonStevens", 7, .01, false);
        StateItem sy = AtomicModels.dailySplines("sy", 1996, pos, 0, .01, false);
        StateItem reg = AtomicModels.timeVaryingRegression("reg", X, 0.01, false);
        StateItem n = AtomicModels.noise("n", .01, false);
        ModelEquation eq = new ModelEquation("eq1", 0, true);
        eq.add(l);
        eq.add(sw);
        eq.add(sy);
        eq.add(n);
        eq.add(reg);
        model.add(l);
        model.add(sw);
        model.add(sy);
        model.add(n);
        model.add(reg);
        int len = y.length();
        FastMatrix M = FastMatrix.make(len, 1);
        model.add(eq);
        M.column(0).copy(y);
        CompositeModelEstimation mrslt = model.estimate(M, false, true, SsfInitialization.Augmented_Robust, Optimizer.LevenbergMarquardt, 1e-5, null);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        StateStorage smoothedStates = mrslt.getSmoothedStates();
        ISsfLoading loading = sy.defaultLoading(0);
        int[] cmpPos = mrslt.getCmpPos();
        int[] cmpDim = mrslt.getSsf().componentsDimension();
        System.out.println(y);
        System.out.println(smoothedStates.getComponent(cmpPos[0]));
        System.out.println(smoothedStates.getComponent(cmpPos[1]));
        System.out.println(smoothedStates.getComponent(cmpPos[3]));
        for (int i = 0; i < smoothedStates.size(); ++i) {
            double z = loading.ZX(i, smoothedStates.a(i).extract(cmpPos[2], cmpDim[2]));
            System.out.print(z);
            System.out.print('\t');
        }
        System.out.println();
        for (int i = 0; i < smoothedStates.size(); ++i) {
            double z = X.row(i).dot(smoothedStates.a(i).extract(cmpPos[4], cmpDim[4]));
            System.out.print(z);
            System.out.print('\t');
        }
        System.out.println("");
        System.out.println(mrslt.getLikelihood().logLikelihood());
//        System.out.println(DoubleSeq.of(mrslt.getFullParameters()));
//        Arrays.stream(mrslt.getParametersName()).forEach(s -> System.out.println(s));
        DataBlock Z = DataBlock.of(y);
        Z.sub(smoothedStates.getComponent(cmpPos[1]));
        for (int i = 0; i < smoothedStates.size(); ++i) {
            double z = X.row(i).dot(smoothedStates.a(i).extract(cmpPos[4], cmpDim[4]));
            Z.add(i, -z);
        }
        SymmetricFilter sf = LocalPolynomialFilters.of(183, 1, DiscreteKernel.uniform(183));
        IFiniteFilter[] afilters = AsymmetricFiltersFactory.mmsreFilters(sf, 0, new double[]{1}, null);
        IFiniteFilter[] lfilters = afilters.clone();
        for (int i = 0; i < lfilters.length; ++i) {
            lfilters[i] = lfilters[i].mirror();
        }
        DoubleSeq t = FilterUtility.filter(y, sf, lfilters, afilters);

        int nyears = 5;
        double P = 365.25;
        int ny = (int) (nyears * P + 1);
        DataBlock Y = DataBlock.make(ny);
        Y.set(i -> Z.get(i) - t.get(i));
        Y.normalize();
        int min = 15;
        int q = 365;
        double[] knots = new double[q];
        double c = P / q;
        for (int i = 0; i < q; ++i) {
            knots[i] = i * c;
        }

        int jump = 4;
        int nq = q / jump;

        DoubleSeq m = DoubleSeq.onMapping(ny, i -> i - P * (int) (i / P));
        AdaptivePeriodicSpline.Specification spec = AdaptivePeriodicSpline.Specification.builder()
                .x(m)
                .y(Y)
                .period(P)
                .knots(knots)
                .splineOrder(4)
                .maxIter(20)
                .build();
        AdaptivePeriodicSpline aspline = AdaptivePeriodicSpline.of(spec);

        AdaptivePeriodicSplines kernel = new AdaptivePeriodicSplines(
                AdaptivePeriodicSplines.Specification.builder()
                        .minKnots(min)
                        .criterion(AdaptivePeriodicSplines.Criterion.AIC)
                        .build());
        kernel.process(aspline);
        int cur = 0;
        for (AdaptivePeriodicSpline.Step result : kernel.allResults()) {
            System.out.print(result.getLambda());
            System.out.print('\t');
            System.out.print(result.getAic());
            System.out.print('\t');
            System.out.print(result.getBic());
            System.out.print('\t');
            System.out.print(kernel.selectedKnotsCount(cur++));
            System.out.print('\t');
            System.out.println(DoubleSeq.of(result.getZ()));
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        double[] spos = kernel.selectedKnots();
        int[] dpos = new int[spos.length];
        for (int i = 0; i < spos.length; ++i) {
            dpos[i] = (int) spos[i];
        }
        model = new CompositeModel();
        sy = AtomicModels.dailySplines("sy", 1996, dpos, 0, .01, false);
        eq = new ModelEquation("eq1", 0, true);
        eq.add(l);
        eq.add(sw);
        eq.add(sy);
        eq.add(n);
        eq.add(reg);
        model.add(l);
        model.add(sw);
        model.add(sy);
        model.add(n);
        model.add(reg);
        model.add(eq);
        M.column(0).copy(y);
        mrslt = model.estimate(M, false, true, SsfInitialization.Augmented_Robust, Optimizer.LevenbergMarquardt, 1e-5, null);
        smoothedStates = mrslt.getSmoothedStates();
        loading = sy.defaultLoading(0);
        cmpPos = mrslt.getCmpPos();
        cmpDim = mrslt.getSsf().componentsDimension();
        long t3 = System.currentTimeMillis();
        System.out.println(t3 - t2);
        System.out.println(y);
        System.out.println(smoothedStates.getComponent(cmpPos[0]));
        System.out.println(smoothedStates.getComponent(cmpPos[1]));
        System.out.println(smoothedStates.getComponent(cmpPos[3]));
        for (int i = 0; i < smoothedStates.size(); ++i) {
            double z = loading.ZX(i, smoothedStates.a(i).extract(cmpPos[2], cmpDim[2]));
            System.out.print(z);
            System.out.print('\t');
        }
        System.out.println();
        for (int i = 0; i < smoothedStates.size(); ++i) {
            double z = X.row(i).dot(smoothedStates.a(i).extract(cmpPos[4], cmpDim[4]));
            System.out.print(z);
            System.out.print('\t');
        }
        System.out.println("");
        System.out.println(mrslt.getLikelihood().logLikelihood());
    }

    public static void main(String[] args) {
//        try {
//            InputStream stream = Data.class.getResourceAsStream("/usclaims.txt");
//            Matrix us = MatrixSerializer.read(stream);
//            y = us.column(0);
//        } catch (IOException ex) {
//            y = null;
//        }

        TsPeriod start = TsPeriod.weekly(1967, 1, 2);
        DoubleSeq y = DoubleSeq.of(tck.demetra.data.WeeklyData.US_CLAIMS).log();

        int q = 35;
        double[] knots = new double[q];
        double P = 365.25 / 7;
        double c = P / q;
        for (int i = 0; i < q; ++i) {
            knots[i] = i * c;
        }

        int nyears = 5;
        int ny = (int) (nyears * P + 1);

        SymmetricFilter sf = LocalPolynomialFilters.of(26, 1, DiscreteKernel.uniform(26));
        IFiniteFilter[] afilters = AsymmetricFiltersFactory.mmsreFilters(sf, 0, new double[]{1}, null);
        IFiniteFilter[] lfilters = afilters.clone();
        for (int i = 0; i < lfilters.length; ++i) {
            lfilters[i] = lfilters[i].mirror();
        }
//        DoubleSeq t = FilterUtility.filter(DoubleSeq.of(y), sf, lfilters, afilters);
        DoubleSeq t = FilterUtility.filter(y, sf, lfilters, afilters);

        DataBlock Y = DataBlock.make(ny);
//        Y.set(i -> y[i] - t.get(i));
        Y.set(i -> y.get(i) - t.get(i));
//        Y.normalize();

        int min = 10;

        DoubleSeq m = DoubleSeq.onMapping(ny, i -> i - P * (int) (i / P));
        AdaptivePeriodicSpline.Specification spec = AdaptivePeriodicSpline.Specification.builder()
                .x(m)
                .y(Y)
                .period(P)
                .knots(knots)
                .splineOrder(4)
                .maxIter(10)
                //                    .fixedKnots(fixedKnots)
                .build();

        AdaptivePeriodicSpline aspline = AdaptivePeriodicSpline.of(spec);
        AdaptivePeriodicSplines.Specification dspec = AdaptivePeriodicSplines.Specification.builder()
                .minKnots(min)
                .lambda1(50)
                .criterion(AdaptivePeriodicSplines.Criterion.AIC)
                .build();

        AdaptivePeriodicSplines kernel = new AdaptivePeriodicSplines(dspec);
        kernel.process(aspline);
        double[] selectedKnots = kernel.selectedKnots();
        CompositeModel model = new CompositeModel();
        StateItem l = AtomicModels.localLevel("l", .01, false, Double.NaN);
//        StateItem l = AtomicModels.localLinearTrend("l", .01, .01, false, false);
//        StateItem sw = AtomicModels.seasonalComponent("sw", "HarrisonStevens", 7, .01, false);
//       StateItem sy = AtomicModels.genericSplines("sy", 365.25 / 7, selectedKnots, 2, 0, .01, false);
        StateItem sy = AtomicModels.regularSplines("sy", 365.25 / 7, selectedKnots, 0, .01, false);
//        StateItem reg=AtomicModels.timeVaryingRegression("reg", X, 0.01, false);
        StateItem n = AtomicModels.noise("n", .01, false);
        model.add(l);
        model.add(sy);
        model.add(n);
        int len = y.length();
        FastMatrix M = FastMatrix.make(len, 1);
        M.column(0).copy(y);
        CompositeModelEstimation mrslt = model.estimate(M, false, true, SsfInitialization.Augmented_Robust, Optimizer.LevenbergMarquardt, 1e-9, null);
        System.out.println(mrslt.getLikelihood().logLikelihood());
        System.out.println(DoubleSeq.of(mrslt.getFullParameters()));
        Arrays.stream(mrslt.getParametersName()).forEach(s -> System.out.println(s));
        StateStorage smoothedStates = mrslt.getSmoothedStates();
        ISsfLoading loading = sy.defaultLoading(0);
        int[] cmpPos = mrslt.getCmpPos();
        int[] cmpDim = mrslt.getSsf().componentsDimension();
        System.out.println(y);
        System.out.println(smoothedStates.getComponent(cmpPos[0]));
        for (int i = 0; i < smoothedStates.size(); ++i) {
            double z = loading.ZX(i, smoothedStates.a(i).extract(cmpPos[1], cmpDim[1]));
            System.out.print(z);
            System.out.print('\t');
        }
        System.out.println();
        System.out.println(smoothedStates.getComponent(cmpPos[2]));
        System.out.println("");
    }
}
