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
package jdplus.advancedsa.base.core.tdarima;

import java.util.function.IntFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class TdSsfArma {

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    public StateComponent stateComponent(int n, IntFunction<IArimaModel> fn) {
        Data data = new Data(n, fn);
        Initialization initialization = new Initialization(data);
        Dynamics dynamics = new Dynamics(data);
        return new StateComponent(initialization, dynamics);
    }

    public Ssf ssf(int n, IntFunction<IArimaModel> fn) {
        return Ssf.of(stateComponent(n, fn), defaultLoading());
    }

    static class Data {

        final IArimaModel[] arma;
        final double[] var, se;
        final FastMatrix phi, theta;
        final int dim;

        int q() {
            return theta.getRowsCount() - 1;
        }

        int p() {
            return phi.getRowsCount();
        }

        Data(int n, IntFunction<IArimaModel> fn) {
            var = new double[n];
            se = new double[n];
            arma = new IArimaModel[n];

            int p = 0, q = 0;
            for (int i = 0; i < n; ++i) {
                IArimaModel arima = fn.apply(i);
                if (!arima.isStationary()) {
                    throw new IllegalArgumentException();
                }
                int pc = arima.getStationaryArOrder(), qc = arima.getMaOrder();
                if (pc > p) {
                    p = pc;
                }
                if (qc > q) {
                    q = qc;
                }
            }
            dim = Math.max(p, q + 1);
            // to simplify the computations...
            phi = FastMatrix.make(p, n + dim);
            theta = FastMatrix.make(q + 1, n + dim);
            for (int i = 0; i < n; ++i) {
                IArimaModel arima = fn.apply(i);
                arma[i] = arima;
                var[i] = arima.getInnovationVariance();
                se[i] = Math.sqrt(var[i]);
                Polynomial ar = arima.getStationaryAr().asPolynomial();
                Polynomial ma = arima.getMa().asPolynomial();
                DoubleSeq c = ar.coefficients().drop(1, 0);
                if (!c.isEmpty()) {
                    phi.column(i).range(0, c.length()).copy(c);
                }
                c = ma.coefficients();
                if (!c.isEmpty()) {
                    theta.column(i).range(0, c.length()).copy(c);
                }
            }
            DataBlock pn = phi.column(n - 1), qn = theta.column(n - 1);
            for (int i = n; i < theta.getColumnsCount(); ++i) {
                if (p > 0) {
                    phi.column(i).copy(pn);
                }
                theta.column(i).copy(qn);

            }
        }
    }

    static class Dynamics implements ISsfDynamics {

        final Data data;

        Dynamics(Data data) {
            this.data = data;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int i, FastMatrix vm) {
            vm.set(0);
            int q = data.q();
            FastMatrix Vc = vm.extract(0, q + 1, 0, q + 1);
            DataBlock th = data.theta.subDiagonal(i + 1);
            Vc.addXaXt(data.var[i], th);
//            SYRK.laddaXXt(data.var[i], DataPointer.of(th.getStorage(), th.getStartPosition(), th.getIncrement()), Vc);
//            SymmetricMatrix.fromLower(Vc);
        }

        @Override
        public void S(int i, FastMatrix sm) {
            sm.column(0).setAY(data.se[i], data.theta.subDiagonal(i + 1));
        }

        @Override
        public boolean hasInnovations(int i) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return false;
        }

        @Override
        public void T(int i, FastMatrix t) {
            t.subDiagonal(1).set(1);
            int p = data.p();
            if (p > 0) {
                DataBlock t0 = t.column(0).range(0, p);
                DataBlock phi = data.phi.subDiagonal(i + 1);
                t0.setAY(-1, phi);
            }
        }

        @Override
        public void TX(int i, DataBlock x) {
            int p = data.p();
            double x0 = x.get(0);
            x.bshiftAndZero();
            if (p > 0 && x0 != 0) {
                DataBlock phi = data.phi.subDiagonal(i + 1);
                x.range(0, p).addAY(-x0, phi);
            }
        }

        @Override
        public void addSU(int i, DataBlock x, DataBlock u) {
            double a = u.get(0) * data.se[i];
            int q = data.q();
            x.range(0, q + 1).addAY(a, data.theta.subDiagonal(i + 1));
        }

        @Override
        public void addV(int i, FastMatrix fm) {
            // we suppose that fm is symmetric !
            int q = data.q();
            FastMatrix Vc = fm.extract(0, q + 1, 0, q + 1);
            DataBlock th = data.theta.subDiagonal(i + 1);
            Vc.addXaXt(data.var[i], th);
//            SYRK.laddaXXt(data.var[i], DataPointer.of(th.getStorage(), th.getStartPosition(), th.getIncrement()), Vc);
//            SymmetricMatrix.fromLower(Vc);
        }

        @Override
        public void XT(int i, DataBlock x) {
            double[] px = x.getStorage();
            double x0 = 0;
            int p = data.p();
            if (p > 0) {
                x0 = -x.range(0, p).dot(data.phi.subDiagonal(i + 1));
            }
            x.fshift(1);
            x.set(0, x0);
        }

        @Override
        public void XS(int i, DataBlock x, DataBlock sx) {
            int q = data.q();
            double a = x.range(0, q).dot(data.theta.subDiagonal(i + 1));
            sx.set(0, a);
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

    }

    static class Initialization implements ISsfInitialization {

        final Data data;

        Initialization(Data data) {
            this.data = data;
        }

        @Override
        public int getStateDim() {
            return data.dim;
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(FastMatrix fm) {
        }

        @Override
        public void a0(DataBlock db) {
            db.set(0);
        }

        @Override
        public void Pf0(FastMatrix fm) {
            unconditionalCovariance(fm);
        }

        @Override
        public void Pi0(FastMatrix pi0) {
        }

        private void unconditionalCovariance(FastMatrix V0) {
            IArimaModel arma = data.arma[0];
            // no var in psi, var in ac !
            int p = arma.getArOrder(), q = arma.getMaOrder();
            int r = data.dim;
            double[] psi = arma.getPsiWeights().getWeights(r);
            double[] ac = arma.getAutoCovarianceFunction().values(r);
            DataBlock C0 = V0.column(0);
            double v0 = data.var[0];
            C0.set(0, ac[0]);
            for (int i = 1; i < r; ++i) {
                double cov = 0;
                if (p > 0) {
                    DataBlock phi = data.phi.column(i);
                    for (int tk = 1, k = i + 1; k <= p; ++tk, ++k) {
                        cov -= phi.get(k - 1) * ac[tk];
                    }
                }
                if (q > 0) {
                    DataBlock th = data.theta.column(i);
                    for (int tl = 0, l = i; l <= q; ++tl, ++l) {
                        cov += th.get(l) * psi[tl] * v0;
                    }
                }
                C0.set(i, cov);
            }
            for (int j = 1; j < r; ++j) {
                DataBlock phi_j = data.phi.column(j);
                DataBlock th_j = data.theta.column(j);
                for (int i = j; i < r; ++i) {
                    // compute cov(a(i), a(j)), i, j>0, i>=j
                    double cov = 0;
                    DataBlock phi_i = data.phi.column(i);
                    DataBlock th_i = data.theta.column(i);
                    for (int tki = 1, ki = i + 1; ki <= p; ++tki, ++ki) {
                        for (int tkj = 1, kj = j + 1; kj <= p; ++tkj, ++kj) {
                            cov += phi_i.get(ki - 1) * phi_j.get(kj - 1) * ac[Math.abs(tki - tkj)];
                        }
                        for (int tlj = 0, lj = j; lj <= q; ++tlj, ++lj) {
                            if (tlj >= tki) {
                                cov -= phi_i.get(ki - 1) * th_j.get(lj) * psi[tlj - tki] * v0;
                            }
                        }
                    }

                    for (int tli = 0, li = i; li <= q; ++tli, ++li) {
                        for (int tkj = 1, kj = j + 1; kj <= p; ++tkj, ++kj) {
                            if (tli >= tkj) {
                                cov -= th_i.get(li) * phi_j.get(kj - 1) * psi[tli-tkj ];
                            }
                        }
                        for (int tlj = 0, lj = j; lj <= q; ++tlj, ++lj) {
                            if (tli == tlj) {
                                cov += th_i.get(li) * th_j.get(lj) * v0;
                            }
                        }
                    }

                    V0.set(i, j, cov);
                }
            }
            SymmetricMatrix.fromLower(V0);
        }

    }

}
