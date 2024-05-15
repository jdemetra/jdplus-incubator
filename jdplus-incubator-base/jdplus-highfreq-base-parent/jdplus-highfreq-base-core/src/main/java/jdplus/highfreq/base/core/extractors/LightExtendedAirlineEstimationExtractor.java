/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.base.core.extractors;

import jdplus.highfreq.base.api.ExtendedAirline;
import jdplus.highfreq.base.core.extendedairline.LightExtendedAirlineEstimation;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class LightExtendedAirlineEstimationExtractor extends InformationMapping<LightExtendedAirlineEstimation> {

    @Override
    public Class<LightExtendedAirlineEstimation> getSourceClass() {
        return LightExtendedAirlineEstimation.class;
    }

    private static final String PARAMETERS = "parameters", LL = "likelihood", MODEL = "model", RESIDUALS = "fullresiduals", PCOV = "pcov", SCORE = "score", MISSING = "missing",
            B = "b", BVAR = "bvar", X = "X", Y = "y", MEAN = "mean", MISSING_POS = "missing_pos", MISSING_VAL = "missing_val", MISSING_STDEV = "missing_stdev",
            LIN="linerized", REGEFFECT="regeffect";

    public LightExtendedAirlineEstimationExtractor() {
        set(B, double[].class, source -> source.getCoefficients().toArray());
        set(BVAR, Matrix.class, source -> source.getCoefficientsCovariance());
        set(Y, double[].class, source -> source.getY().toArray());
        set(X, double[].class, source -> source.getY().toArray());
        set(MEAN, Boolean.class, source -> source.isMean());
        delegate(MODEL, ExtendedAirline.class, r -> r.getModel());
        set(PARAMETERS, double[].class, source -> source.getParameters().getValues().toArray());
        set(SCORE, double[].class, source -> source.getParameters().getScores().toArray());
        set(PCOV, Matrix.class, source -> source.getParameters().getCovariance());
        set(RESIDUALS, double[].class, source -> source.getFullResiduals().toArray());
        set(MISSING, Matrix.class, source -> {
            MissingValueEstimation[] missing = source.getMissing();
            if (missing.length > 0) {
                FastMatrix M=FastMatrix.make(missing.length, 3);
                for (int i = 0; i < missing.length; ++i) {
                    M.set(i, 0, missing[i].getPosition());
                    M.set(i, 1, missing[i].getValue());
                    M.set(i, 2, missing[i].getStandardError());
                }
                return M;
            } else {
                return null;
            }
        });
        set(MISSING_POS, int[].class, source -> {
            MissingValueEstimation[] missing = source.getMissing();
            if (missing.length > 0) {
                int[] pos = new int[missing.length];
                for (int i = 0; i < pos.length; ++i) {
                    pos[i] = missing[i].getPosition();
                }
                return pos;
            } else {
                return null;
            }
        });
        set(MISSING_VAL, double[].class, source -> {
            MissingValueEstimation[] missing = source.getMissing();
            if (missing.length > 0) {
                double[] val = new double[missing.length];
                for (int i = 0; i < val.length; ++i) {
                    val[i] = missing[i].getValue();
                }
                return val;
            } else {
                return null;
            }
        });
        set(MISSING_STDEV, double[].class, source -> {
            MissingValueEstimation[] missing = source.getMissing();
            if (missing.length > 0) {
                double[] e = new double[missing.length];
                for (int i = 0; i < e.length; ++i) {
                    e[i] = missing[i].getStandardError();
                }
                return e;
            } else {
                return null;
            }
        });
        delegate(LL, LikelihoodStatistics.class, r -> r.getStatistics());
        set(LIN, double[].class, source -> source.linearizedData().toArray());
        set(REGEFFECT, double[].class, source -> source.regressionEffect().toArray());

    }
}
