/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.r;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.basic.TimeInvariantDynamics;
import jdplus.toolkit.base.core.ssf.basic.TimeInvariantDynamics.Innovations;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Dynamics {
    public ISsfDynamics of(Matrix T, Matrix V, Matrix S){
        return new TimeInvariantDynamics(FastMatrix.of(T), new Innovations(FastMatrix.of(V), FastMatrix.of(S)));
    }
}
