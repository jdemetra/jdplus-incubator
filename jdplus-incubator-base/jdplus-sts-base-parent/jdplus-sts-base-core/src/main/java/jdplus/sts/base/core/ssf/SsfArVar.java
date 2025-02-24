/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.ssf;

import jdplus.toolkit.base.core.arima.AutoCovarianceFunction;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import lombok.NonNull;
import jdplus.toolkit.base.core.ssf.ISsfLoading;

/**
 * AR model with time-varying variances Dynamics of the state array for y(t) =
 * ar(0) y(t-1)+ ... + ar(p)y(t-p-1) The state array block contains y(t),
 * y(t-1)...y(t-p)
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SsfArVar {

    public StateComponent of(@NonNull double[] ar, double[] stde, double scale) {
        return of(ar, stde, scale, false);
    }

    /**
     * @param ar Auto-regressive parameters
     * @param stde Unscaled stde of the innovations
     * @param scale Scale of the stde
     * @param zeroinit Zero initialization. Should be false by default
     * @return
     */
    public StateComponent of(@NonNull double[] ar, double[] stde, double scale, boolean zeroinit) {
        if (ar.length == 0) {
            throw new IllegalArgumentException();
        }
        Data data = new Data(ar, stde, scale);
        return new StateComponent(new Initialization(data, zeroinit), new Dynamics(data));
    }

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    @lombok.Value
    private static class Data {
        
        Data(double[] phi, double[] std, double scale){
            this.phi=phi;
            this.std=std;
            this.scale=scale;
            this.scale2=scale*scale;
        }

        double[] phi, std;
        double scale, scale2;

        Polynomial ar() {
            double[] c = new double[1 + phi.length];
            c[0] = 1;
            for (int i = 0; i < phi.length; ++i) {
                c[i + 1] = -phi[i];
            }
            return Polynomial.ofInternal(c);
        }

        int dim() {
            return phi.length;
        }

        double stderr(int pos) {
            return pos >= std.length ? scale : scale * std[pos];
        }

        double var(int pos) {
            return pos >= std.length ? scale2 : scale2 * std[pos] * std[pos];
        }

    }

    private static class Initialization implements ISsfInitialization {

        private final Data info;
        private final boolean zeroinit;

        Initialization(final Data info, final boolean zeroinit) {
            this.info = info;
            this.zeroinit = zeroinit;
        }

        @Override
        public int getStateDim() {
            return info.dim();
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
        public void diffuseConstraints(FastMatrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pi0(FastMatrix pf0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            if (!zeroinit) {
                AutoCovarianceFunction acf = new AutoCovarianceFunction(Polynomial.ONE, info.ar(), info.getScale2());
                acf.prepare(pf0.getColumnsCount());
                pf0.diagonal().set(acf.get(0));
                for (int i = 1; i < pf0.getColumnsCount(); ++i) {
                    pf0.subDiagonal(i).set(acf.get(i));
                    pf0.subDiagonal(-i).set(acf.get(i));
                }
            } else {
                pf0.set(0, 0, info.getScale2());
            }
        }

    }

    private static class Dynamics implements ISsfDynamics {

        private final Data info;
        private final DataBlock z;

        Dynamics(final Data info) {
            this.info = info;
            z = DataBlock.make(info.dim());
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.set(0, 0, info.var(pos));
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            cm.set(0, 0, Math.sqrt(info.var(pos)));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.subDiagonal(-1).set(1);
            tr.row(0).extract(0, info.dim()).copyFrom(info.getPhi(), 0);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            double y = 0;
            DoubleSeqCursor reader = x.cursor();
            for (int i = 0; i < info.phi.length; ++i) {
                y += info.phi[i] * reader.getAndNext();
            }
            x.fshift(1);
            x.set(0, y);
        }

        @Override
        public void TVT(final int pos, final FastMatrix vm) {
            z.set(0);
            DataBlockIterator cols = vm.columnsIterator();
            for (int i = 0; i < info.phi.length; ++i) {
                z.addAY(info.phi[i], cols.next());
            }
            TX(pos, z);
            vm.downRightShift(1);
            vm.column(0).copy(z);
            vm.row(0).copy(z);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(0, 0, info.var(pos));
        }

        @Override
        public void XT(int pos, DataBlock x) {
            double first = x.get(0);
            x.bshift(1);
            x.setLast(0);
            if (first != 0) {
                for (int i = 0; i < info.phi.length; ++i) {
                    x.add(i, first * info.phi[i]);
                }
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

}
