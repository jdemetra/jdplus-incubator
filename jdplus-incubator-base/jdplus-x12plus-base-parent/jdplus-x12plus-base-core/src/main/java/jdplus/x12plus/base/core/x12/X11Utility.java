/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x12plus.base.core.x12;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X11Utility {

    /**
     * Corrects a series, using weights attached to the observations and an
     * alternative series
     *
     * @param sorig The series being corrected
     * @param sweights The weights of the different data of the series
     * @param salternative The series containing the alternative values
     * @return The corrected series
     */
    public DoubleSeq correctSeries(DoubleSeq sorig, DoubleSeq sweights,
            DoubleSeq salternative) {
        double[] ns=sorig.toArray();
        for (int i = 0; i < ns.length; ++i) {
            double x = sweights.get(i);
            if (x == 0) {
                ns[i]=salternative.get(i);
            }
        }
        return DoubleSeq.of(ns);
    }

    /**
     * Corrects a series, using weights attached to the observations and an
     * alternative series
     *
     * @param sorig The series being corrected
     * @param sweights The weights of the different data of the series
     * @param alternative The series containing the alternative values
     * @return The corrected series
     */
    public DoubleSeq correctSeries(DoubleSeq sorig, DoubleSeq sweights,
            double alternative) {
        double[] ns=sorig.toArray();
        for (int i = 0; i < ns.length; ++i) {
            double x = sweights.get(i);
            if (x == 0) {
                ns[i]=alternative;
            }
        }
        return DoubleSeq.of(ns);
    }

    public double[] calcAbsMeanVariations(DoubleSeq x, int nlags, boolean mul) {
        double[] mean = new double[nlags];

        int n = x.length();
        for (int lag = 1; lag <= nlags; ++lag) {
            double sum = 0;
            for (int i = lag; i < n; ++i) {
                double x1 = x.get(i), x0 = x.get(i - lag);
                double d = x1 - x0;
                if (mul) {
                    d /= x0;
                }
                sum += Math.abs(d);
            }
            mean[lag - 1] = sum / (n - lag);
        }
        return mean;
    }

    public static double[] calcAbsMeanVariations(DoubleSeq x, int nlags, boolean mul, boolean[] valid) {
        if (valid == null) {
            return calcAbsMeanVariations(x, nlags, mul);
        }
        double[] mean = new double[nlags];

        for (int lag = 1; lag <= nlags; ++lag) {
            double sum = 0;
            int n = 0;
            for (int i = lag; i < n; ++i) {
                if (valid[i - lag]) {
                    ++n;
                    double x1 = x.get(i), x0 = x.get(i - lag);
                    double d = x1 - x0;
                    if (mul) {
                        d /= x0;
                    }
                    sum += Math.abs(d);
                }
            }
            mean[lag - 1] = sum / n;
        }
        return mean;
    }

    public double calcAbsMeanVariation(DoubleSeq x, int lag, boolean mul) {
        double sum = 0;
        int n = x.length();
        for (int i = lag; i < n; ++i) {
            double x1 = x.get(i), x0 = x.get(i - lag);
            double d = Math.abs(x1 - x0);
            if (mul) {
                d /= x0;
            }
            sum += d;
        }
        return sum / (n - lag);
    }

    public double calcAbsMeanVariation(DoubleSeq x, int lag, boolean mul, boolean[] valid) {
        if (valid == null) {
            return calcAbsMeanVariation(x, lag, mul);
        }
        double sum = 0;
        int n = 0;
        for (int i = lag; i < n; ++i) {
            if (valid[i - lag]) {
                double x1 = x.get(i), x0 = x.get(i - lag);
                double d = Math.abs(x1 - x0);
                if (mul) {
                    d /= x0;
                }
                sum += d;
                ++n;
            }
        }
        return sum / n;
    }

    public double[] calcMeanVariations(DoubleSeq x, int nlags, boolean mul) {
        double[] mean = new double[nlags];
        int n = x.length();

        for (int lag = 1; lag <= nlags; ++lag) {
            double sum = 0;
            for (int i = lag; i < n; ++i) {
                double x1 = x.get(i), x0 = x.get(i - lag);
                double d = x1 - x0;
                if (mul) {
                    d /= x0;
                }
                sum += d;
            }
            mean[lag - 1] = sum / (n - lag);
        }
        return mean;
    }

    public double calcMeanVariation(DoubleSeq x, int lag, boolean mul) {
        int n = x.length();

        double sum = 0;
        for (int i = lag; i < n; ++i) {
            double x1 = x.get(i), x0 = x.get(i - lag);
            double d = x1 - x0;
            if (mul) {
                d /= x0;
            }
            sum += d;
        }
        return sum / (n - lag);
    }

    public static double[][] calcVariations(DoubleSeq s, int nlags, boolean mul, boolean[] valid) {
        double[] mean = new double[nlags];
        double[] std = new double[nlags];
        int iend=s.length();
         for (int l = 1; l <= nlags; ++l) {
            double sum = 0, sum2 = 0;
            for (int i = l; i < iend; ++i) {
                if (valid == null || valid[i - l]) {
                    double x1 = s.get(i), x0 = s.get(i - l);
                    double d = x1 - x0;
                    if (mul) {
                        d *= 100 / x0;
                    }
                    sum += d;
                    sum2 += d * d;
                }
            }
            int n = (iend - l);
            mean[l - 1] = sum / n;
            std[l - 1] = Math.sqrt((sum2 - sum * sum / n) / n);
        }
        return new double[][]{mean, std};
    }
    /**
     * average duration of run for MStatistics
     *
     * @param x
     * @param mul
     *
     * @return
     */
    public double adr(DoubleSeq x, boolean mul) {
        if (x.length() < 2) {
            return 0;
        }

        int n = x.length() - 1;
        double[] d = new double[n];
        DoubleSeqCursor reader = x.cursor();
        double s0 = reader.getAndNext();
        if (mul) {
            for (int i = 0; i < n; ++i) {
                double s1 = reader.getAndNext();
                d[i] = (s1 - s0) / s0;
                s0 = s1;
            }
        } else {
            for (int i = 0; i < n; ++i) {
                double s1 = reader.getAndNext();
                d[i] = s1 - s0;
                s0 = s1;
            }
        }
        int c = 0;
        int s = 0;
        for (int i = 0; i < n; ++i) {
            int cur = sign(d[i]);
            if (s != cur && cur != 0) {
                ++c;
                s = cur;
            }
        }
        double N = n;
        return N / c;
    }

    private int sign(double val) {
        if (val < 0) {
            return -1;
        } else if (val > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
