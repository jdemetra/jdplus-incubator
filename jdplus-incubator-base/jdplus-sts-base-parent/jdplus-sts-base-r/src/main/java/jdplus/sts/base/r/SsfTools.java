/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.r;

import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfError;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class SsfTools {

    public Matrix transitionMatrix(ISsf ssf, int pos) {
        FastMatrix m = FastMatrix.square(ssf.getStateDim());
        ssf.dynamics().T(pos, m);
        return m;
    }

    public Matrix innovationMatrix(ISsf ssf, int pos) {
        FastMatrix m = FastMatrix.square(ssf.getStateDim());
        ssf.dynamics().V(pos, m);
        return m;
    }

    public Matrix transitionMatrix(StateComponent cmp, int pos) {
        FastMatrix m = FastMatrix.square(cmp.dim());
        cmp.dynamics().T(pos, m);
        return m;
    }

    public Matrix innovationMatrix(StateComponent cmp, int pos) {
        FastMatrix m = FastMatrix.square(cmp.dim());
        cmp.dynamics().V(pos, m);
        return m;
    }

    public double[] loading(ISsf ssf, int pos) {
        DataBlock m = DataBlock.make(ssf.getStateDim());
        ssf.loading().Z(pos, m);
        return m.getStorage();
    }

    public double measurementError(ISsf ssf, int pos) {
        ISsfError e = ssf.measurementError();
        return e == null ? 0 : e.at(pos);
    }

    public double[] loading(ISsfLoading l, int dim, int pos) {
        DataBlock m = DataBlock.make(dim);
        l.Z(pos, m);
        return m.getStorage();
    }

    public double[] initialState(ISsf ssf) {
        DataBlock m = DataBlock.make(ssf.getStateDim());
        ssf.initialization().a0(m);
        return m.getStorage();
    }

    public Matrix stationaryInitialVariance(ISsf ssf) {
        FastMatrix m = FastMatrix.square(ssf.getStateDim());
        ssf.initialization().Pf0(m);
        return m;
    }

    public Matrix diffuseInitialConstraint(ISsf ssf) {
        if (!ssf.initialization().isDiffuse()) {
            return null;
        }
        FastMatrix m = FastMatrix.make(ssf.getStateDim(), ssf.getDiffuseDim());
        ssf.initialization().diffuseConstraints(m);
        return m;
    }

    public Matrix diffuseInitialVariance(ISsf ssf) {
        if (!ssf.initialization().isDiffuse()) {
            return null;
        }
        FastMatrix m = FastMatrix.square(ssf.getStateDim());
        ssf.initialization().Pi0(m);
        return m;
    }

    public double[] initialState(StateComponent cmp) {
        DataBlock m = DataBlock.make(cmp.dim());
        cmp.initialization().a0(m);
        return m.getStorage();
    }

    public Matrix stationaryInitialVariance(StateComponent cmp) {
        FastMatrix m = FastMatrix.square(cmp.dim());
        cmp.initialization().Pf0(m);
        return m;
    }

    public Matrix diffuseInitialConstraint(StateComponent cmp) {
        if (!cmp.initialization().isDiffuse()) {
            return null;
        }
        FastMatrix m = FastMatrix.make(cmp.dim(), cmp.initialization().getDiffuseDim());
        cmp.initialization().diffuseConstraints(m);
        return m;
    }

    public Matrix diffuseInitialVariance(StateComponent cmp) {
        if (!cmp.initialization().isDiffuse()) {
            return null;
        }
        FastMatrix m = FastMatrix.square(cmp.dim());
        cmp.initialization().Pi0(m);
        return m;
    }

    public double[] loading(StateItem item, int pos) {
        DataBlock m = DataBlock.make(item.stateDim());
        item.defaultLoading(0).Z(pos, m);
        return m.getStorage();
    }

    public double[] initialState(StateItem cmp) {
        return initialState(cmp.build(null));
    }

    public Matrix stationaryInitialVariance(StateItem cmp) {
        return stationaryInitialVariance(cmp.build(null));
    }

    public Matrix diffuseInitialConstraint(StateItem cmp) {
        return diffuseInitialConstraint(cmp.build(null));
    }

    public Matrix diffuseInitialVariance(StateItem cmp) {
        return diffuseInitialVariance(cmp.build(null));
    }

    public Matrix transitionMatrix(StateItem item, int pos) {
        return transitionMatrix(item.build(null), pos);
    }

    public Matrix innovationMatrix(StateItem item, int pos) {
        return innovationMatrix(item.build(null), pos);
    }
    
    public StateComponent defaultComponent(StateItem item){
        return item.build(null);
    }

}
