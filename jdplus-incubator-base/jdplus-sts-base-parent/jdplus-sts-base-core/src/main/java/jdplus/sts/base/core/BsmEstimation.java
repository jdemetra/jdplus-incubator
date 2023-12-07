/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core;

import jdplus.toolkit.base.core.stats.likelihood.DiffuseLikelihoodStatistics;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsDomain;

/**
 *
 * @author PALATEJ
 */
public interface BsmEstimation {

    TsDomain getDomain();

    DoubleSeq getY();

    Matrix getX();

    DoubleSeq getCoefficients();

    Matrix getCoefficientsCovariance();

    MissingValueEstimation[] getMissing();

    /**
     * Parameters of the stochastic component.Fixed parameters are not included
     *
     * @return
     */
    ParametersEstimation getParameters();

    /**
     *
     * @return
     */
    DiffuseLikelihoodStatistics getStatistics();

    DoubleSeq getResiduals();

}
