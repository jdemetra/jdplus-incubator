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
package jdplus.sts.base.r;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import static jdplus.toolkit.base.core.ssf.akf.AkfToolkit.var;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.arima.SsfArma2;
import jdplus.toolkit.base.core.ssf.basic.IntegratedDynamics;
import jdplus.toolkit.base.core.ssf.basic.IntegratedInitialization;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.sts.LocalLevel;
import jdplus.toolkit.base.core.ssf.sts.LocalLinearTrend;
import jdplus.toolkit.base.core.ssf.sts.Noise;
import jdplus.toolkit.base.core.ssf.sts.SeasonalComponent;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class StateBlocks {

    public StateComponent arima(double[] ar, double[] delta, double[] ma, double var) {

        ArimaModel model = new ArimaModel(ar == null ? BackFilter.ONE : BackFilter.ofInternal(ar),
                delta == null ? BackFilter.ONE : BackFilter.ofInternal(delta),
                ma == null ? BackFilter.ONE : BackFilter.ofInternal(ma),
                var);

        return SsfArima.stateComponent(model);
    }

    public StateComponent arima2(double[] ar, double[] delta, double[] ma, double var) {
        BackFilter bar = ar == null ? BackFilter.ONE : BackFilter.ofInternal(ar);
        BackFilter bdelta = delta == null ? BackFilter.ONE : BackFilter.ofInternal(delta);
        BackFilter bma = ma == null ? BackFilter.ONE : BackFilter.ofInternal(ma);
        ArimaModel model = new ArimaModel(bar, bdelta, bma, var);
        if (delta == null || bdelta.equals(BackFilter.ONE)) {
            return SsfArma2.stateComponent(model);
        } else {
            StateComponent stcmp = SsfArma2.stateComponent(new ArimaModel(bar, BackFilter.ONE, bma, var));
            ISsfLoading loading = Loading.fromPosition(0);
            DoubleSeq d = bdelta.coefficients().drop(1, 0);
            IntegratedDynamics idyn = new IntegratedDynamics(stcmp.dynamics(), loading, d);
            IntegratedInitialization iinit = new IntegratedInitialization(stcmp.initialization(), d);
            return new StateComponent(iinit, idyn);
        }
    }

    public StateComponent sarma(int period, double[] phi, double[] bphi, double[] theta, double[] btheta) {

        SarimaModel model = SarimaModel.builder(period)
                .phi(phi)
                .bphi(bphi)
                .theta(theta)
                .btheta(btheta)
                .build();
        return SsfArima.stateComponent(model);
    }

    public StateComponent sarma2(int period, double[] phi, double[] bphi, double[] theta, double[] btheta) {

        SarimaModel model = SarimaModel.builder(period)
                .phi(phi)
                .bphi(bphi)
                .theta(theta)
                .btheta(btheta)
                .build();
        return SsfArma2.stateComponent(model);
    }
    
    public StateComponent seasonal(String type, int period, double var) {
        return SeasonalComponent.of(SeasonalModel.valueOf(type), period, var);
    }
    
    public StateComponent localLevel(double var, double initialValue) {
        return LocalLevel.stateComponent(var, initialValue);
    }
    
    public StateComponent localLinearTrend(double lvar, double svar) {
        return LocalLinearTrend.stateComponent(lvar, svar);
    }

    public StateComponent noise(double var) {
        return Noise.of(var);
    }
    
    public StateComponent composite(StateComponent[] cmps){
        return CompositeSsf.of(cmps);
    }

    public double[] Z(ISsfLoading l, int pos, int m) {
        if (l == null) {
            return null;
        }
        DataBlock z = DataBlock.make(m);
        l.Z(pos, z);
        return z.getStorage();
    }

    public Matrix T(StateComponent cmp, int pos) {
        if (cmp == null) {
            return null;
        }
        int n = cmp.dim();
        FastMatrix M = FastMatrix.square(n);
        cmp.dynamics().T(pos, M);
        return M;
    }

    public Matrix V(StateComponent cmp, int pos) {
        if (cmp == null) {
            return null;
        }
        int n = cmp.dim();
        FastMatrix M = FastMatrix.square(n);
        cmp.dynamics().V(pos, M);
        return M;
    }

    public Matrix S(StateComponent cmp, int pos) {
        if (cmp == null) {
            return null;
        }
        int n = cmp.dim();
        ISsfDynamics dynamics = cmp.dynamics();
        int m = dynamics.getInnovationsDim();
        if (m == 0) {
            return null;
        }
        FastMatrix M = FastMatrix.make(n, m);
        dynamics.S(pos, M);
        return M;
    }

    public Matrix P0(StateComponent cmp) {
        if (cmp == null) {
            return null;
        }
        int n = cmp.dim();
        FastMatrix M = FastMatrix.square(n);
        cmp.initialization().Pf0(M);
        return M;
    }

    public Matrix B(StateComponent cmp) {
        if (cmp == null) {
            return null;
        }
        ISsfInitialization initialization = cmp.initialization();
        int nd = initialization.getDiffuseDim();
        if (nd == 0) {
            return null;
        }
        int n = initialization.getStateDim();
        FastMatrix M = FastMatrix.make(n, nd);
        cmp.initialization().diffuseConstraints(M);
        return M;
    }
}
