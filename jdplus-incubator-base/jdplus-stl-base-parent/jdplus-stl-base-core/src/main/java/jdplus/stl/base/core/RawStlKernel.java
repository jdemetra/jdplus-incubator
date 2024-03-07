/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this fit except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stl.base.core;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.stl.base.api.StlSpec;

/**
 * Java implementation of the original FORTRAN routine
 *
 * Source; R.B. Cleveland, W.S.Cleveland, J.E. McRae, and I. Terpenning, STL: A
 * Seasonal-Trend Decomposition Procedure Based on Loess, Statistics Research
 * Report, AT&T Bell Laboratories.
 *
 * @author Jean Palate
 */
public class RawStlKernel {

    public RawStlKernel(StlSpec spec) {
        this.spec = spec;
    }

    private final StlSpec spec;

    private double[] y, season, trend, irr, weights, fit, si, sa;
    private int nmissing;

    private int n() {
        return y.length;
    }

    public RawStlResults process(DoubleSeq data) {
        if (!initializeProcessing(data)) {
            return null;
        }
        int istep = 0;
        do {
            innerLoop();
            if (++istep > spec.getOuterLoopsCount()) {
                return finishProcessing();
            }
            if (weights == null) {
                weights = new double[n()];
            }
            op(trend, season, fit);
            computeRobustWeights();
        } while (true);
    }

    private RawStlResults finishProcessing() {
        int n = y.length;
        if (spec.isMultiplicative()) {
            for (int i = 0; i < n; ++i) {
                fit[i] = trend[i] * season[i];
                if (Double.isFinite(y[i])) {
                    irr[i] = y[i] / fit[i];
                } else {
                    irr[i] = 1;
                }
            }
        } else {
            for (int i = 0; i < n; ++i) {
                fit[i] = trend[i] + season[i];
                if (Double.isFinite(y[i])) {
                    irr[i] = y[i] - fit[i];
                } else {
                    irr[i] = 0;
                }
            }
        }
        return RawStlResults.builder()
                .series(DoubleSeq.of(y))
                .trend(DoubleSeq.of(trend))
                .irregular(DoubleSeq.of(irr))
                .fit(DoubleSeq.of(fit))
                .seasonal(DoubleSeq.of(season))
                .weights(weights == null ? DoubleSeq.empty() : DoubleSeq.of(weights))
                .sa(DoubleSeq.of(sa))
                .build();
    }

    private boolean initializeProcessing(DoubleSeq data) {

        int n = data.length();
        nmissing = data.count(z -> !Double.isFinite(z));
        y = new double[n];
        data.copyTo(y, 0);
        fit = new double[n];
        season = new double[n];
        trend = new double[n];
        if (spec.isMultiplicative()) {
            Arrays.setAll(trend, i -> 1);
        }
        irr = new double[n];
        si = new double[n];
        sa = new double[n];
        weights = null;
        return true;
    }

    private double mad() {
        double[] sr;
        if (nmissing == 0) {
            sr = weights.clone();
        } else {
            int n = n();
            sr = new double[n - nmissing];
            for (int i = 0, j = 0; i < n; ++i) {
                if (Double.isFinite(weights[i)) {
                    sr[j++] = weights[i];
                }
            }
        }
        int n = sr.length;
        int n2 = n >> 1;
        Arrays.sort(sr);
        if (n % 2 != 0) {
            return 6 * sr[n2];
        } else {
            return 3 * (sr[n2 - 1] + sr[n2]);
        }
//        if (n % 2 != 0) {
//            int[] idx=new int[]{n2};
//            new PartialSort().psort(sr, idx);
//            return 6*sr[n2];
//        } else {
//            int[] idx=new int[]{n2, n2-1};
//            new PartialSort().psort(sr, idx);
//            return 3 * (sr[n2 - 1] + sr[n2]);
//        }
    }

    private void computeRobustWeights() {

        int n = n();
        double mu = mean();
        for (int i = 0; i < n; ++i) {
            if (Double.isFinite(y[i])) {
                weights[i] = Math.abs(invop(y[i], fit[i]) - mu);
            } else {
                weights[i] = Double.NaN;
            }
        }

        double mad = mad();
        double wthreshold = spec.getRobustWeightThreshold();
        DoubleUnaryOperator wfn = spec.getRobustWeightFunction().asFunction();
        double c1 = wthreshold * mad;
        double c9 = (1 - wthreshold) * mad;

        for (int i = 0; i < n; ++i) {
            double r = weights[i];
            if (Double.isFinite(r)) {
                if (r <= c1) {
                    weights[i] = 1;
                } else if (r <= c9) {
                    weights[i] = wfn.applyAsDouble(r / mad);
                } else {
                    weights[i] = 0;
                }
            }
        }

    }

    /**
     *
     */
    protected void innerLoop() {
        // Step 1: SI=Y-T
        for (int j = 0; j < spec.getInnerLoopsCount(); ++j) {
            // Step 1: SI=Y-T
            invop(y, trend, si);
            // Step 2: compute S
            SeasonalFilter sfilter = SeasonalFilter.of(spec.getSeasonalSpec());
            sfilter.filter(IDataGetter.of(si), weights == null ? null : k -> weights[k], spec.isMultiplicative(), IDataSelector.of(season));
            // Step 3: compute SA
            invop(y, season, sa);
            // Step 4: T=smooth(sa)
            LoessFilter tfilter = new LoessFilter(spec.getTrendSpec());
            tfilter.filter(IDataSelector.of(sa), weights == null ? null : k -> weights[k], IDataSelector.of(trend));
            if (spec.isMultiplicative() && !DoubleSeq.of(trend).allMatch(q -> q > 0)) {
                // workaround to avoid negative values in the trend
                double[] lsa = new double[sa.length];
                for (int i = 0; i < lsa.length; ++i) {
                    lsa[i] = Math.log(sa[i]);
                }
                tfilter.filter(IDataSelector.of(lsa), weights == null ? null : k -> weights[k], IDataSelector.of(trend));
                for (int i = 0; i < trend.length; ++i) {
                    trend[i] = Math.exp(trend[i]);
                }
            }
            op(trend, season, fit);
        }
    }

    private double invop(double l, double r) {
        return spec.isMultiplicative() ? l / r : l - r;
    }

    private void op(double[] l, double[] r, double[] lr) {
        if (spec.isMultiplicative()) {
            for (int i = 0; i < l.length; ++i) {
                lr[i] = l[i] * r[i];
            }
        } else {
            for (int i = 0; i < l.length; ++i) {
                lr[i] = l[i] + r[i];
            }

        }
    }

    private void invop(double[] l, double[] r, double[] lr) {
        if (spec.isMultiplicative()) {
            for (int i = 0; i < l.length; ++i) {
                lr[i] = l[i] / r[i];
            }
        } else {
            for (int i = 0; i < l.length; ++i) {
                lr[i] = l[i] - r[i];
            }

        }
    }

    private double mean() {
        return spec.isMultiplicative() ? 1 : 0;
    }
}
