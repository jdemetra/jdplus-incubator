/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq.base.r;

import java.util.Arrays;
import static jdplus.sa.base.core.PreliminaryChecks.MAX_MISSING_COUNT;
import jdplus.highfreq.base.api.ExtendedAirlineSpec;
import static jdplus.highfreq.base.core.extendedairline.ExtendedAirlineKernel.factories;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineMapping;
import jdplus.highfreq.base.core.extendedairline.LightExtendedAirlineEstimation;
import jdplus.highfreq.base.core.extendedairline.LogLevelModule;
import jdplus.sa.base.api.SaException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.data.interpolation.AverageInterpolator;
import jdplus.toolkit.base.core.data.interpolation.DataInterpolator;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.IOutlierFactory;
import jdplus.toolkit.base.core.regarima.GlsArimaProcessor;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.ami.GenericOutliersDetection;
import jdplus.toolkit.base.core.regarima.ami.OutliersDetectionModule;

/**
 * Upgrade to highfreqregarima
 *
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class ExtendedAirlineProcessor {

    public ExtendedAirlineSpec spec(double[] periods, int ndiff, boolean ar, boolean toInt) {
        return ExtendedAirlineSpec.builder()
                .periodicities(periods)
                .differencingOrder(ndiff)
                .phi(ar ? Parameter.undefined() : null)
                .theta(ar ? null : Parameter.undefined())
                .adjustToInt(toInt)
                .build();
    }

    public RegArimaModel<ArimaModel> regarima(double[] Y, boolean mean, Matrix X, ExtendedAirlineSpec spec) {
        DoubleSeq y = DoubleSeq.of(Y);
        //Missing
        int nz = y.length();
        int nm = y.count(z -> !Double.isFinite(z));
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            throw new SaException("Too many missing values");
        }

        DataInterpolator interpolator = AverageInterpolator.interpolator();
        double[] interpolatedData;
        int[] missing;

        if (y.anyMatch(z -> Double.isNaN(z))) {
            IntList lmissing = new IntList();
            interpolatedData = interpolator.interpolate(y, lmissing);
            y = DoubleSeq.of(interpolatedData);
            if (lmissing.isEmpty()) {
                missing = IntList.EMPTY;
            } else {
                missing = lmissing.toArray();
                Arrays.sort(missing);
            }
        } else {
            missing = IntList.EMPTY;
        }

        final ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        //
        return RegArimaModel.<ArimaModel>builder()
                .y(y)
                .addX(FastMatrix.of(X))
                .arima(mapping.getDefault())
                .missing(missing)
                .meanCorrection(mean)
                .build();
    }

    public LightExtendedAirlineEstimation estimate(RegArimaModel<ArimaModel> regarima, ExtendedAirlineSpec spec, double eps, boolean exactderivatives) {
        final ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(eps)
                .computeExactFinalDerivatives(exactderivatives)
                .build();
        RegArimaEstimation<ArimaModel> rslt = processor.process(regarima, mapping);
        return LightExtendedAirlineEstimation.of(rslt, spec);
    }

    public double[] logLevelTest(RegArimaModel<ArimaModel> model, ExtendedAirlineSpec spec, double eps) {
        LogLevelModule ll = LogLevelModule
                .builder()
                .estimationPrecision(eps)
                .build();
        if (ll.process(model, ExtendedAirlineMapping.of(spec))) {
            return new double[]{ll.getAICcLevel(), ll.getAICcLog()};
        } else {
            return null;
        }
    }

    public Matrix outliers(RegArimaModel<ArimaModel> regarima, ExtendedAirlineSpec spec, String[] outliers, int start, int end, double cv, int maxoutliers, int maxround) {
        LevenbergMarquardtMinimizer.LmBuilder min = LevenbergMarquardtMinimizer.builder().maxIter(5);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class
        )
                .minimizer(min)
                .precision(1e-5)
                .build();
        IOutlierFactory[] factories = factories(outliers);
        OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class
        )
                .maxOutliers(maxoutliers)
                .maxRound(maxround)
                .addFactories(factories)
                .processor(processor)
                .build();

        DoubleSeq y = regarima.getY();
        cv = Math.max(cv, GenericOutliersDetection.criticalValue(y.length(), 0.01));
        od.setCriticalValue(cv);
        od.prepare(y.length());
        od.setBounds(start, end == 0 ? y.length() : end);
        // remove missing values
        int[] missing = regarima.missing();
        for (int i = 0; i < missing.length; ++i) {
            for (int j = 0; j < outliers.length; ++j) {
                od.exclude(missing[i], j);
            }
        }

        od.process(regarima, ExtendedAirlineMapping.of(spec));
        int[][] io = od.getOutliers();
        if (io.length > 0) {
            FastMatrix M = FastMatrix.make(io.length, 2);
            M.set((i, j) -> io[i][j]);
            return M;
        } else {
            return null;
        }
    }

}
