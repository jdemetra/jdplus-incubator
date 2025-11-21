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

import jdplus.sts.base.api.Component;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.api.util.TableOfBoolean;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixFactory;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.akf.SmoothationsComputer;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.stats.RobustStandardDeviationComputer;

/**
 *
 * @author Jean Palate
 */
public class SsfOutlierDetector {

    private final RobustStandardDeviationComputer computer;
    private FastMatrix coef, tau;
    private TableOfBoolean allowedTable;
    private int lbound;
    private int ubound;

    private int posMax = -1, oMax = -1;

    public SsfOutlierDetector(RobustStandardDeviationComputer computer) {
        this.computer = computer;
    }

    public boolean process(DoubleSeq y, BsmData bsm, FastMatrix X, double sig2) {

        SsfBsm ssf = SsfBsm.of(bsm);
        Ssf wssf = X == null ? ssf : RegSsf.ssf(ssf, X);
        SsfData data = new SsfData(y);
        int n = y.length();
        tau = FastMatrix.make(n, 4); // noise, level, seasonal, all
        SmoothationsComputer scomputer = new SmoothationsComputer();
        scomputer.process(wssf, data);
        if (sig2 <= 0) {
            if (computer == null) {
                DiffuseLikelihood ll = scomputer.getFilteringResults().getAugmentation().likelihood(true);
                sig2 = ll.sigma2();
            } else {
                DiffuseLikelihood dll = DkToolkit.likelihoodComputer(true, true, true).compute(ssf, data);
                double sig = computer.compute(dll.e().drop(dll.getD(), 0));
                sig2 = sig * sig;
            }
        }
        int ncmp = SsfBsm.searchPosition(bsm, Component.Noise);
        int lcmp = SsfBsm.searchPosition(bsm, Component.Level);
        int scmp = SsfBsm.searchPosition(bsm, Component.Seasonal);
        IntList sel = new IntList(3);

        for (int i = lbound; i < ubound; ++i) {
            try {
                sel.clear();
                DataBlock R = scomputer.R(i);
                FastMatrix Rvar = scomputer.Rvar(i);
                if (ncmp >= 0 && allowedTable.get(i, 0)) {
                    double r = R.get(ncmp), v = Rvar.get(ncmp, ncmp);
                    if (v > 0) {
                        setCoefficient(i, 0, r / v);
                        setTau(i, 0, (r * r) / (v * sig2));
                    }
                    sel.add(ncmp);
                } else {
                    setCoefficient(i, 0, Double.NaN);
                    setTau(i, 0, Double.NaN);
                }
                if (lcmp >= 0 && allowedTable.get(i, 1)) {
                    double r = R.get(lcmp), v = Rvar.get(lcmp, lcmp);
                    if (v > 0) {
                        setCoefficient(i, 1, r / v);
                        setTau(i, 1, (r * r) / (v * sig2));
                    }
                    sel.add(lcmp);
                } else {
                    setCoefficient(i, 1, Double.NaN);
                    setTau(i, 1, Double.NaN);
                }
                if (scmp >= 0 && allowedTable.get(i, 2)) {
                    double r = R.get(scmp), v = Rvar.get(scmp, scmp);
                    if (v > 0) {
                        setCoefficient(i, 2, r / v);
                        setTau(i, 2, (r * r) / (v * sig2));
                    }
                    sel.add(scmp);
                } else {
                    setCoefficient(i, 2, Double.NaN);
                    setTau(i, 2, Double.NaN);
                }

                if (! sel.isEmpty()) {

                    FastMatrix S = MatrixFactory.select(Rvar, sel, sel);
                    DataBlock ur = DataBlock.of(R.select(sel));
                    SymmetricMatrix.lcholesky(S, 1e-9);
                    LowerTriangularMatrix.solveLx(S, ur, 1e-9);
                    setTau(i, 3, ur.ssq() / sig2);
                }
            } catch (Exception err) {
            }
        }

        searchMax();
        return posMax >= 0;
    }

    private void clear(boolean all) {
        oMax = -1;
        posMax = -1;
        tau = null;
        if (all) {
            coef = null;
            allowedTable = null;
        } else {
            coef.set(0);
        }
    }

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    public double coefficient(int pos, int outlier) {
        return coef.get(pos, outlier);
    }

    /**
     *
     * @param pos
     * @param ioutlier
     */
    public void exclude(int pos, int ioutlier) {
        // avoid outliers outside the current range
        if (pos >= 0 && pos < allowedTable.getRowsCount() && ioutlier >= 0) {
            allowedTable.set(pos, ioutlier, false);
        }
    }

    /**
     *
     * @param pos
     * @param ioutlier
     */
    public void allow(int pos, int ioutlier) {
        // avoid outliers outside the current range
        if (pos >= 0 && pos < allowedTable.getRowsCount() && ioutlier >= 0) {
            allowedTable.set(pos, ioutlier, true);
        }
    }

    /**
     *
     * @param pos
     */
    public void exclude(int[] pos) {
        if (pos == null) {
            return;
        }
        for (int i = 0; i < pos.length; ++i) {
            for (int j = 0; j < 3; ++j) {
                exclude(pos[i], j);
            }
        }
    }

    public void excludeType(int type) {
        allowedTable.column(type).set(false);
    }

    public void allowType(int type) {
        allowedTable.column(type).set(true);
        coef.column(type).set(0);
    }

    /**
     *
     * @param pos
     */
    public void allow(int[] pos) {
        if (pos == null) {
            return;
        }
        for (int i = 0; i < pos.length; ++i) {
            for (int j = 0; j < 3; ++j) {
                allow(pos[i], j);
            }
        }
    }

    /**
     *
     * @param pos
     */
    public void exclude(int pos) {
        for (int j = 0; j < 3; ++j) {
            exclude(pos, j);
        }
    }

    /**
     *
     * @return
     */
    public int getLBound() {
        return lbound;
    }

    /**
     *
     * @return
     */
    public int getMaxOutlierType() {
        if (posMax == -1) {
            searchMax();
        }
        return oMax;
    }

    /**
     *
     * @return
     */
    public int getMaxOutlierPosition() {
        if (posMax == -1) {
            searchMax();
        }
        return posMax;
    }

    /**
     *
     * @return
     */
    public double getMaxGlobalTau() {
        if (posMax == -1) {
            searchMax();
        }
        return tau(posMax);
    }

    public double getMaxTau() {
        if (posMax == -1) {
            searchMax();
        }
        double tmax = tau(posMax, oMax);
        return tmax;
    }

    public void setBounds(int lbound, int ubound) {
        this.lbound = lbound;
        this.ubound = ubound;
    }

    /**
     *
     * @param n
     */
    public void prepare(int n) {
        lbound = 0;
        ubound = n;
        coef = FastMatrix.make(n, 3);
        allowedTable = new TableOfBoolean(n, 3);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < n; ++j) {
                allowedTable.set(j, i, true);
            }
        }
    }

    private void searchMax() {
        if (tau == null) {
            return;
        }
        double max = 0;
        int rmax = -1;
        for (int r = 0; r < tau.getRowsCount(); ++r) {
            double cur = tau(r);
            if (cur > max) {
                rmax = r;
                max = cur;
            }
        }
        max = 0;
        int cmax = -1;
        for (int c = 0; c < 3; ++c) {
            double cur = tau.get(rmax, c);
            if (cur > max) {
                cmax = c;
                max = cur;
            }
        }

        posMax = rmax;
        oMax = cmax;
    }

    /**
     *
     * @param pos
     * @param col
     * @param val
     */
    protected void setTau(int pos, int col, double val) {
        tau.set(pos, col, val);
    }

    /**
     *
     * @param pos
     * @param col
     * @param val
     */
    protected void setCoefficient(int pos, int col, double val) {
        coef.set(pos, col, val);
    }

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    public double tau(int pos, int outlier) {
        return tau.get(pos, outlier);
    }

    public double tau(int pos) {
        return tau.get(pos, tau.getColumnsCount() - 1);
    }

    public FastMatrix getTau() {
        return tau;
    }

    public FastMatrix getCoefficients() {
        return coef;
    }

}
