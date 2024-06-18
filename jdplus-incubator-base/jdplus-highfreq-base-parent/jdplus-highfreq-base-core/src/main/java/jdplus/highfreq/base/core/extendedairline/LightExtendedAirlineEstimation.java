/*
 * Copyright 2024 JDemetra+.
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
package jdplus.highfreq.base.core.extendedairline;

import java.util.List;
import jdplus.highfreq.base.api.ExtendedAirline;
import jdplus.highfreq.base.api.ExtendedAirlineSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class LightExtendedAirlineEstimation implements GenericExplorable {

    @lombok.NonNull
    private DoubleSeq y;
    @lombok.NonNull
    private Matrix X;
    private boolean mean;

    @lombok.NonNull
    private ExtendedAirline model;

    /**
     * Regression estimation. The order correspond to the order of the variables
     * Fixed coefficients are not included
     */
    @lombok.NonNull
    private DoubleSeq coefficients;
    @lombok.NonNull
    private Matrix coefficientsCovariance;

    /**
     * Positions corresponding to the estimation domain
     */
    @lombok.NonNull
    private MissingValueEstimation[] missing;
    /**
     * Parameters of the stochastic component. Fixed parameters are not included
     */
    @lombok.NonNull
    private ParametersEstimation parameters;

    private LikelihoodStatistics statistics;

    private DoubleSeq fullResiduals;

    @lombok.Singular
    private List<ProcessingLog.Information> logs;

    public static Builder builder() {
        Builder builder = new Builder();
        builder.y = DoubleSeq.empty();
        builder.X = Matrix.empty();
        builder.coefficients = DoubleSeq.empty();
        builder.coefficientsCovariance = Matrix.empty();
        builder.missing = NOMISSING;
        builder.parameters = ParametersEstimation.empty();
        builder.fullResiduals = DoubleSeq.empty();
        return builder;
    }

    private static final MissingValueEstimation[] NOMISSING = new MissingValueEstimation[0];

    public static LightExtendedAirlineEstimation of(RegArimaEstimation<ArimaModel> estimation, ExtendedAirlineSpec spec) {
        Builder builder = builder();
        RegArimaModel<ArimaModel> regarima = estimation.getModel();
        ConcentratedLikelihoodWithMissing ll = estimation.getConcentratedLikelihood();
        LogLikelihoodFunction.Point<RegArimaModel<ArimaModel>, ConcentratedLikelihoodWithMissing> max = estimation.getMax();
        int free = spec.freeParametersCount();
        int df = ll.degreesOfFreedom() - free;
        double vscale = ll.ssq() / df;
        builder.y(regarima.getY())
                .X(regarima.variables())
                .mean(regarima.isMean())
                .model(ExtendedAirline.of(spec.withFreeParameters(max.getParameters())))
                .coefficients(ll.coefficients())
                .coefficientsCovariance(ll.covariance(free, true));
        ParametersEstimation pestim = new ParametersEstimation(max.getParameters(), max.asymptoticCovariance(), max.getScore(), "Extended airline");
        builder.parameters(pestim)
                .statistics(estimation.statistics());
        DoubleSeq fullRes = RegArimaUtility.fullResiduals(regarima, ll);
        builder.fullResiduals(fullRes);
        // complete for missings
        int nmissing = ll.nmissing();
        if (nmissing > 0) {
            DoubleSeq y = regarima.getY();
            MissingValueEstimation[] missing = new MissingValueEstimation[nmissing];
            DoubleSeqCursor cur = ll.missingCorrections().cursor();
            DoubleSeqCursor vcur = ll.missingUnscaledVariances().cursor();
            int[] pmissing = regarima.missing();
            for (int i = 0; i < nmissing; ++i) {
                double m = cur.getAndNext();
                double v = vcur.getAndNext();
                missing[i] = new MissingValueEstimation(pmissing[i], y.get(pmissing[i]) - m, Math.sqrt(v * vscale));
            }
            builder.missing(missing);
        }
        return builder.build();
    }

    public DoubleSeq originalY() {
        if (y.anyMatch(z -> Double.isNaN(z))) {
            // already contains the missing values
            return y;
        }
        if (missing.length == 0) {
            return y;
        }
        double[] z = y.toArray();
        for (int i = 0; i < missing.length; ++i) {
            z[missing[i].getPosition()] = Double.NaN;
        }
        return DoubleSeq.of(z);
    }

    public DoubleSeq linearizedData() {
        double[] res = y.toArray();

        // handle missing values
        if (missing.length > 0) {
            for (int i = 0; i < missing.length; ++i) {
                res[missing[i].getPosition()] = missing[i].getValue();
            }
        }
        DataBlock e = DataBlock.of(res);
        if (!X.isEmpty()) {
            DoubleSeq regs = regressionEffect();
            e.sub(regs);
        }
        return e;
    }

    public DoubleSeq regressionEffect() {

        if (coefficients.isEmpty()) {
            return DoubleSeq.empty();
        }

        DataBlock d = DataBlock.make(y.length());
        d.setAY(coefficients.get(0), X.column(0));
        for (int i = 1; i < coefficients.length(); ++i) {
            d.addAY(coefficients.get(i), X.column(i));
        }
        return d;
    }

}
