/*
 * Copyright 2025 JDemetra+.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.core.BsmData;
import jdplus.sts.base.core.BsmOutliersDetection;
import jdplus.sts.base.core.SsfOutlierDetector;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.modelling.regression.AdditiveOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.IOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.LevelShiftFactory;
import jdplus.toolkit.base.core.modelling.regression.PeriodicOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.SwitchOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.TransitoryChangeFactory;
import jdplus.toolkit.base.core.regarima.outlier.SingleOutlierDetector;
import jdplus.toolkit.base.core.regarima.outlier.FastOutlierDetector;
import jdplus.toolkit.base.core.regarima.outlier.ExactSingleOutlierDetector;
import jdplus.toolkit.base.core.stats.RobustStandardDeviationComputer;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.arima.estimation.ArmaFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixFactory;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regarima.estimation.ConcentratedLikelihoodComputer;
import jdplus.toolkit.base.core.regarima.outlier.CriticalValueComputer;
import jdplus.toolkit.base.core.regsarima.ami.ExactOutliersDetector;
import jdplus.toolkit.base.core.regsarima.ami.FastOutliersDetector;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Outliers {

    public Matrix regarimaOutlier(double[] y, IArimaModel arima, boolean mean, Matrix X, String[] outliers, String filter, boolean mad) {
        SingleOutlierDetector<IArimaModel> sod;
        switch (filter.toLowerCase(Locale.ROOT)) {
            case "fast" -> {
                sod = new FastOutlierDetector<>(mad ? RobustStandardDeviationComputer.mad() : null);
            }
            case "ansley" -> {
                sod = new ExactSingleOutlierDetector<>(mad ? RobustStandardDeviationComputer.mad() : null, ArmaFilter.ansley(), null);
            }
            case "kalman" -> {
                sod = new ExactSingleOutlierDetector<>(mad ? RobustStandardDeviationComputer.mad() : null, ArmaFilter.kalman(), null);
            }
            case "ljungbox" -> {
                sod = new ExactSingleOutlierDetector<>(mad ? RobustStandardDeviationComputer.mad() : null, ArmaFilter.ljungBox(false), null);
            }
            case "modifed_ljungbox", "x12" -> {
                sod = new ExactSingleOutlierDetector<>(mad ? RobustStandardDeviationComputer.mad() : null, ArmaFilter.ljungBox(true), null);
            }
            default -> {
                sod = new ExactSingleOutlierDetector<>(mad ? RobustStandardDeviationComputer.mad() : null, null, null);
            }
        }
        RegArimaModel.Builder builder = RegArimaModel.<IArimaModel>builder()
                .y(DoubleSeq.of(y))
                .meanCorrection(mean)
                .arima(arima);
        if (X != null) {
            for (int i = 0; i < X.getColumnsCount(); ++i) {
                builder.addX(X.column(i));
            }
        }
        int period = arima instanceof SarimaModel sarima ? sarima.getPeriod() : 0;
        RegArimaModel regarima = builder.build();
        sod.setOutlierFactories(Arrays.stream(outliers)
                .map(s -> outlierFactoryFor(s, period))
                .filter(o -> o != null)
                .toArray(IOutlierFactory[]::new));
        sod.prepare(y.length);
        sod.setBounds(0, y.length);
        if (!sod.process(regarima)) {
            return null;
        }
        return MatrixFactory.columnBind(sod.allCoefficients(), sod.getT());
    }

    private IOutlierFactory outlierFactoryFor(String id, int period) {
        String[] ids = id.split(":");
        switch (ids[0].toUpperCase(Locale.ROOT)) {
            case "LS" -> {
                return LevelShiftFactory.FACTORY_ZEROENDED;
            }
            case "AO" -> {
                return AdditiveOutlierFactory.FACTORY;
            }
            case "WO" -> {
                return SwitchOutlierFactory.FACTORY;
            }
            case "TC" -> {
                double r = ids.length == 1 ? 0.7 : Double.parseDouble(ids[1]);
                return new TransitoryChangeFactory(r);
            }
            case "SO", "PO" -> {
                int p = ids.length == 1 ? period : Integer.parseInt(ids[1]);
                return p <= 1 ? null : new PeriodicOutlierFactory(p, true);
            }
            default -> {
                return null;
            }
        }
    }

    public Matrix bsmOutlier(double[] y, BsmData bsm, Matrix X, boolean mad) {
        SsfOutlierDetector sod = new SsfOutlierDetector(mad ? RobustStandardDeviationComputer.mad() : null);
        sod.prepare(y.length);
        sod.setBounds(0, y.length);
        sod.process(DoubleSeq.of(y), bsm, FastMatrix.of(X), 0);
        return MatrixFactory.columnBind(sod.getCoefficients(), sod.getTau());
    }

    public Matrix tramoOutliers(double[] y, SarimaModel sarima, boolean mean, Matrix X, double cv, String[] outliers, boolean ml, boolean mad) {
        int period = sarima.getPeriod();
        if (cv == 0) {
            cv = CriticalValueComputer.simpleComputer().applyAsDouble(y.length);
        }
        SingleOutlierDetector<SarimaModel> sod = new FastOutlierDetector<>(mad ? RobustStandardDeviationComputer.mad() : null);
        sod.setOutlierFactories(factories(outliers, period));
        FastOutliersDetector od = FastOutliersDetector.builder()
                .singleOutlierDetector(sod)
                .criticalValue(cv)
                .maximumLikelihood(ml)
                .processor(RegArimaUtility.processor(true, 1e-5))
                .build();
        od.prepare(y.length);
        od.setBounds(0, y.length);

        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder()
                .y(DoubleSeq.of(y))
                .arima(sarima)
                .addX(FastMatrix.of(X))
                .meanCorrection(mean)
                .build();
        od.process(regarima, SarimaMapping.of(sarima.orders()));
        int[][] o = od.getOutliers();
        FastMatrix rslt = FastMatrix.make(o.length, 4);
        for (int i = 0; i < o.length; ++i) {
            rslt.set(i, 0, o[i][0]);
            rslt.set(i, 1, o[i][1]);
        }
        ConcentratedLikelihoodWithMissing ll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(od.getRegArima());
        int nx = X == null ? 0 : X.getColumnsCount();
        if (mean) {
            ++nx;
        }
        rslt.column(2).copy(ll.coefficients().drop(nx, 0));
        rslt.column(3).copyFrom(ll.tstats(0, true), nx);
        return rslt;
    }

    public Matrix x12Outliers(double[] y, SarimaModel sarima, boolean mean, Matrix X, double cv, String[] outliers, boolean mad) {
        int period = sarima.getPeriod();
        if (cv == 0) {
            cv = CriticalValueComputer.advancedComputer(0.5).applyAsDouble(y.length);
        }
        SingleOutlierDetector<SarimaModel> sod = new ExactSingleOutlierDetector<>(mad ? RobustStandardDeviationComputer.mad() : null, null, null);
        sod.setOutlierFactories(factories(outliers, period));
        ExactOutliersDetector od = ExactOutliersDetector.builder()
                .singleOutlierDetector(sod)
                .criticalValue(cv)
                .processor(RegArimaUtility.processor(true, 1e-5))
                .build();
        od.prepare(y.length);
        od.setBounds(0, y.length);

        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder()
                .y(DoubleSeq.of(y))
                .arima(sarima)
                .addX(FastMatrix.of(X))
                .meanCorrection(mean)
                .build();
        od.process(regarima, SarimaMapping.of(sarima.orders()));
        int[][] o = od.getOutliers();
        FastMatrix rslt = FastMatrix.make(o.length, 4);
        for (int i = 0; i < o.length; ++i) {
            rslt.set(i, 0, o[i][0]);
            rslt.set(i, 1, o[i][1]);
        }
        ConcentratedLikelihoodWithMissing ll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(od.getRegArima());
        int nx = X == null ? 0 : X.getColumnsCount();
        if (mean) {
            ++nx;
        }
        rslt.column(2).copy(ll.coefficients().drop(nx, 0));
        rslt.column(3).copyFrom(ll.tstats(0, true), nx);
        return rslt;
    }

    public Matrix bsmOutliers(double[] y, int period, BsmSpec spec, Matrix X, double cv,
            boolean ao, boolean ls, boolean so, boolean mad, String forward, String backward) {
        BsmOutliersDetection od = BsmOutliersDetection.builder()
                .bsm(spec)
                .mad(mad)
                .ao(ao)
                .ls(ls)
                .so(so)
                .criticalValue(cv)
                .forwardEstimation(BsmOutliersDetection.Estimation.valueOf(forward))
                .backardEstimation(BsmOutliersDetection.Estimation.valueOf(backward))
                .build();
        
        if (od.process(DoubleSeq.of(y), FastMatrix.of(X), period)) {
            DiffuseConcentratedLikelihood ll = od.getLikelihood();
            List<int[]> outliers = od.outliers();
            FastMatrix rslt = FastMatrix.make(outliers.size(), 4);
            int row = 0;
            for (int[] o : outliers) {
                rslt.set(row, 0, o[0]);
                rslt.set(row, 1, o[1]);
            }
            int nx = X == null ? 0 : X.getColumnsCount();
            rslt.column(2).copy(ll.coefficients().drop(nx, 0));
            rslt.column(3).copyFrom(ll.tstats(0, true), nx);
            return rslt;
        } else {
            return null;
        }
    }

    private IOutlierFactory[] factories(String[] outliers, int period) {
        return (Arrays.stream(outliers)
                .map(s -> outlierFactoryFor(s, period))
                .filter(o -> o != null)
                .toArray(IOutlierFactory[]::new));

    }

}
