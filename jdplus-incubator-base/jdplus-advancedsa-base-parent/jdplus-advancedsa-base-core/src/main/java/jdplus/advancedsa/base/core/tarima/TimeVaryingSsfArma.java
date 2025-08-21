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
package jdplus.advancedsa.base.core.tarima;

import java.util.function.IntFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.DataPointer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.matrices.lapack.SYRK;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
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
public class TimeVaryingSsfArma {

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    public StateComponent stateComponent(int n, IntFunction<IArimaModel> fn) {
        Data data = new Data(n, fn);
        SsfArma2.Initialization initialization = new SsfArma2.Initialization(data.arma[0], data.dim);
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

            IArimaModel arima = fn.apply(0);
            if (!arima.isStationary()) {
                throw new IllegalArgumentException();
            }
            int p = arima.getStationaryArOrder(), q = arima.getMaOrder();
            dim = Math.max(p, q + 1);
            // to simplify the computations...
            phi = FastMatrix.make(p, n + dim - 1);
            theta = FastMatrix.make(q + 1, n + dim - 1);
            for (int i = 0; i < n; ++i) {
                arima = fn.apply(i);
                int np = arima.getStationaryArOrder(), nq = arima.getMaOrder();
                if (p != np || q != nq) {
                    throw new IllegalArgumentException();
                }
                arma[i] = arima;
                var[i] = arima.getInnovationVariance();
                se[i] = Math.sqrt(var[i]);
                Polynomial ar = arima.getStationaryAr().asPolynomial();
                Polynomial ma = arima.getMa().asPolynomial();
                DoubleSeq c = ar.coefficients().drop(1, 0);
                phi.column(0).range(0, c.length()).copy(c);
                c=ma.coefficients();
                theta.column(i).range(0, c.length()).copy(c);
            }
            DataBlock pn = phi.column(n - 1), qn = theta.column(n - 1);
            for (int i = n; i < phi.getColumnsCount(); ++i) {
                phi.column(i).copy(pn);
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
            DataBlock th = data.theta.subDiagonal(i);
            SYRK.laddaXXt(data.var[i], DataPointer.of(th.getStorage(), th.getStartPosition(), th.getIncrement()), Vc);
            SymmetricMatrix.fromLower(Vc);
        }

        @Override
        public void S(int i, FastMatrix sm) {
            sm.column(0).setAY(data.se[i], data.theta.subDiagonal(i));
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
                DataBlock phi = data.phi.subDiagonal(i);
                t0.setAY(-1, phi);
            }
        }

        @Override
        public void TX(int i, DataBlock x) {
            int p = data.p();
            double x0 = x.get(0);
            x.bshiftAndZero();
            if (p > 0 && x0 != 0) {
                DataBlock phi = data.phi.subDiagonal(i);
                x.range(0, p).addAY(-x0, phi);
            }
        }

        @Override
        public void addSU(int i, DataBlock x, DataBlock u) {
            double a = u.get(0) * data.se[i];
            int q = data.q();
            x.range(0, q + 1).addAY(a, data.theta.subDiagonal(i));
        }

        @Override
        public void addV(int i, FastMatrix fm) {
            // we suppose that fm is symmetric !
            int q = data.q();
            FastMatrix Vc = fm.extract(0, q + 1, 0, q + 1);
            DataBlock th = data.theta.subDiagonal(i);
            SYRK.laddaXXt(data.var[i], DataPointer.of(th.getStorage(), th.getStartPosition(), th.getIncrement()), Vc);
            SymmetricMatrix.fromLower(Vc);
        }

        @Override
        public void XT(int i, DataBlock x) {
            double[] px = x.getStorage();
            double x0 = 0;
            int p = data.p();
            if (p > 0) {
                x0 = -x.range(0, p).dot(data.phi.subDiagonal(i));
            }
            x.fshift(1);
            x.set(0, x0);
        }

        @Override
        public void XS(int i, DataBlock x, DataBlock sx) {
            int q = data.q();
            double a = x.range(0, q).dot(data.theta.subDiagonal(i));
            sx.set(0, a);
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

    }

}
