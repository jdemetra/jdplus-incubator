/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.r.rssf;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.basic.TimeInvariantLoading;
import jdplus.toolkit.base.core.ssf.basic.TimeInvariantMeasurements;
import jdplus.toolkit.base.core.ssf.multivariate.ISsfMeasurements;
import jdplus.toolkit.base.core.ssf.univariate.ISsfMeasurement;
import jdplus.toolkit.base.core.ssf.univariate.Measurement;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Measurements {

    public ISsfMeasurement of(int mpos, double var) {
        return new Measurement(Loading.fromPosition(mpos), var);
    }

    public ISsfMeasurement of(double[] Z, double var) {
        return new Measurement(new TimeInvariantLoading(DataBlock.of(Z)), var);
    }

    public ISsfMeasurements of(Matrix Z, Matrix H) {
        return new TimeInvariantMeasurements(FastMatrix.of(Z), FastMatrix.of(H), null);
    }

}
