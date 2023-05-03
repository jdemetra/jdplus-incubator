/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.r;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class RegressionModels {

    public ISsf fixed(ISsf ssf, FastMatrix x) {
        return RegSsf.ssf(ssf, x);
    }

    public ISsf timeVarying(ISsf ssf, Matrix x, Matrix v) {
        return RegSsf.timeVaryingSsf(ssf, FastMatrix.of(x), FastMatrix.of(v));
    }

    public ISsf timeVarying(ISsf ssf, FastMatrix x, double[] v) {
        FastMatrix V = FastMatrix.square(x.getColumnsCount());
        V.diagonal().copyFrom(v, 0);
        return RegSsf.timeVaryingSsf(ssf, FastMatrix.of(x), V);
    }
}
