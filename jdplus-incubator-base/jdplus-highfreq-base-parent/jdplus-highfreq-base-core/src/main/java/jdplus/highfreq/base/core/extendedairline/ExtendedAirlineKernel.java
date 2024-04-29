/*
 * Copyright 2023 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.highfreq.base.core.extendedairline;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.modelling.highfreq.EasterSpec;
import jdplus.highfreq.base.api.ExtendedAirline;
import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import jdplus.highfreq.base.api.ExtendedAirlineSpec;
import jdplus.toolkit.base.api.modelling.highfreq.HolidaysSpec;
import jdplus.toolkit.base.api.modelling.highfreq.OutlierSpec;
import jdplus.toolkit.base.api.modelling.highfreq.RegressionSpec;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.modelling.OutlierDescriptor;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.EasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.JulianEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.api.timeseries.regression.SwitchOutlier;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.highfreq.base.core.regarima.HighFreqRegArimaModel;
import jdplus.highfreq.base.core.regarima.ModelDescription;
import jdplus.sa.base.api.SaException;
import static jdplus.sa.base.core.PreliminaryChecks.MAX_MISSING_COUNT;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.core.data.interpolation.AverageInterpolator;
import jdplus.toolkit.base.core.data.interpolation.DataInterpolator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.AdditiveOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.IOutlierFactory;
import jdplus.toolkit.base.core.modelling.regression.LevelShiftFactory;
import jdplus.toolkit.base.core.modelling.regression.SwitchOutlierFactory;
import jdplus.toolkit.base.core.regarima.GlsArimaProcessor;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.ami.GenericOutliersDetection;
import jdplus.toolkit.base.core.regarima.ami.OutliersDetectionModule;
import jdplus.toolkit.base.core.ssf.arima.FastArimaForecasts;
import jdplus.toolkit.base.core.ssf.arima.ExactArimaForecasts;
import jdplus.toolkit.base.core.ssf.arima.SsfUcarima;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
import jdplus.toolkit.base.core.timeseries.simplets.Transformations;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ExtendedAirlineKernel {

    public static final String EA = "extended airline";

    private final ExtendedAirlineModellingSpec spec;
    private final ModellingContext modellingContext;

    private ExtendedAirlineKernel(ExtendedAirlineModellingSpec spec, ModellingContext context) {
        this.spec = spec;
        this.modellingContext = context;
    }

    public static ExtendedAirlineKernel of(ExtendedAirlineModellingSpec spec, ModellingContext context) {
        if (spec.isEnabled()) {
            return new ExtendedAirlineKernel(spec, context);
        } else {
            return null;
        }
    }

    public HighFreqRegArimaModel process(TsData y, ProcessingLog log) {

        if (log == null) {
            log = ProcessingLog.dummy();
        }
        log.push(EA);
        ModelDescription desc = build(y, log);
        if (desc == null) {
            throw new ExtendedAirlineException("Initialization failed");
        }
        ExtendedRegAirlineModelling modelling = ExtendedRegAirlineModelling.of(desc, log);
        HighFreqRegArimaModel rslt = exec(modelling, log);
        log.pop();
        // step 1. Build the model

        return rslt;
    }

    private ModelDescription<ArimaModel, ExtendedAirlineDescription> build(TsData originalTs, ProcessingLog log) {
        TsData y = originalTs.select(spec.getSeries().getSpan());
        ModelDescription<ArimaModel, ExtendedAirlineDescription> desc = new ModelDescription(y, y.getDomain().select(spec.getEstimate().getSpan()));
        // regression variables
        desc.setMean(spec.getStochastic().isMean());
        // calendar
        buildCalendar(desc);
        buildOutliers(desc);
        buildInterventionVariables(desc);
        buildUsers(desc);

        desc.setStochasticSpec(new ExtendedAirlineDescription(spec.getStochastic()));
        return desc;
    }

    private void buildCalendar(ModelDescription desc) {
        RegressionSpec regression = spec.getRegression();
        HolidaysSpec calendar = regression.getHolidays();
        if (calendar.isUsed()) {
            HolidaysVariable hvar = HolidaysVariable.of(calendar.getHolidays(),
                    calendar.getHolidaysOption(), calendar.getNonWorkingDays(), calendar.isSingle(), modellingContext);
            add(desc, hvar, "holidays", ComponentType.CalendarEffect, calendar.getCoefficients());
        }
        EasterSpec easter = regression.getEaster();
        if (easter.isUsed()) {
            IEasterVariable ev;
            if (easter.isJulian()) {
                ev = new JulianEasterVariable(easter.getDuration(), true);
            } else {
                ev = EasterVariable.builder()
                        .duration(easter.getDuration())
                        .meanCorrection(EasterVariable.Correction.Simple)
                        .endPosition(-1)
                        .build();
            }
            Parameter ec = easter.getCoefficient();
            add(desc, ev, "easter", ComponentType.CalendarEffect, ec == null ? null : new Parameter[]{ec});
        }
    }

    private void buildOutliers(ModelDescription desc) {
        RegressionSpec regression = spec.getRegression();
        List<Variable<IOutlier>> outliers = regression.getOutliers();
        for (Variable<IOutlier> outlier : outliers) {
            IOutlier cur = outlier.getCore();
            String code = cur.getCode();
            LocalDateTime pos = cur.getPosition();
            IOutlier v;
            ComponentType cmp = ComponentType.Undefined;
            switch (code) {
                case AdditiveOutlier.CODE:
                    v = AdditiveOutlierFactory.FACTORY.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                case LevelShift.CODE:
                    v = LevelShiftFactory.FACTORY_ZEROSTARTED.make(pos);
                    cmp = ComponentType.Trend;
                    break;
                case SwitchOutlier.CODE:
                    v = SwitchOutlierFactory.FACTORY.make(pos);
                    cmp = ComponentType.Irregular;
                    break;
                default:
                    v = null;
            }
            if (v != null) {
                Variable nvar = outlier.withCore(v);
                if (!nvar.hasAttribute(SaVariable.REGEFFECT)) {
                    nvar = nvar.setAttribute(SaVariable.REGEFFECT, cmp.name());
                }
                desc.addVariable(nvar);
            }
        }
    }

    private void addOutliers(ModelDescription desc, int[][] io) {
        OutlierSpec ospec = spec.getOutlier();
        String[] outliers = ospec.allOutliers();
        TsDomain edom = desc.getEstimationDomain();
        for (int i = 0; i < io.length; ++i) {
            int[] cur = io[i];
            TsPeriod pos = edom.get(cur[0]);
            IOutlier o = outlier(outliers[cur[1]], pos);
            desc.addVariable(Variable.variable(IOutlier.defaultName(o.getCode(), pos), o, attributes(o)));
        }
    }

    private void buildInterventionVariables(ModelDescription desc) {
        for (Variable<InterventionVariable> iv : spec.getRegression().getInterventionVariables()) {
            desc.addVariable(iv);
        }
    }

    private void buildUsers(ModelDescription desc) {
        for (Variable<TsContextVariable> user : spec.getRegression().getUserDefinedVariables()) {
            String name = user.getName();
            ITsVariable var = user.getCore().instantiateFrom(modellingContext, name);
            desc.addVariable(user.withCore(var));
        }
    }

    private HighFreqRegArimaModel exec(ExtendedRegAirlineModelling modelling, ProcessingLog log) {
        // step 1: log/level
        execTransform(modelling, log);
        // step 2: outliers
        if (spec.getOutlier().isUsed()) {
            if (modelling.needEstimation()) {
                modelling.estimate(1e-5);
            }
            execOutliers(modelling, log);

        }
        // step 3: final estimation
        modelling.estimate(spec.getEstimate().getPrecision());

        return HighFreqRegArimaModel.of(modelling.getDescription(), modelling.getEstimation(), log);
    }

    private void add(@NonNull ModelDescription model, ITsVariable v, @NonNull String name, @NonNull ComponentType cmp, Parameter[] c) {
        if (v == null) {
            return;
        }
        Variable var = Variable.builder()
                .name(name)
                .core(v)
                .coefficients(c)
                .attribute(SaVariable.REGEFFECT, cmp.name())
                .build();
        model.addVariable(var);
    }

    public static ExtendedAirlineEstimation fastProcess(DoubleSeq y, Matrix X, boolean mean, String[] outliers, double cv, ExtendedAirlineSpec spec, double eps, boolean log) {
        return fastProcess(y, X, mean, outliers, cv, spec, eps, 0, log);
    }

    public static ExtendedAirlineEstimation fastProcess(DoubleSeq y, Matrix X, boolean mean, String[] outliers, double cv, ExtendedAirlineSpec spec, double eps, int nfcasts, boolean log) {

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

        if (log) {
            if (y.anyMatch(x -> x < 0.0000001)) {
                log = false;
            } else {
                y = y.log();
            }
        }

        Matrix X_withoutFcast;
        if (nfcasts > 0 && X
                != null) {
            X_withoutFcast = X.extract(0, X.getRowsCount() - nfcasts, 0, X.getColumnsCount());
        } else {
            X_withoutFcast = X;
        }

        final ExtendedAirlineMapping mapping = ExtendedAirlineMapping.of(spec);
        //
        RegArimaModel.Builder builder = RegArimaModel.<ArimaModel>builder()
                .y(y)
                .addX(FastMatrix.of(X_withoutFcast))
                .arima(mapping.getDefault())
                .meanCorrection(mean);
        OutlierDescriptor[] o;
        if (outliers != null && outliers.length > 0) {
            GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                    .precision(1e-5)
                    .build();
            IOutlierFactory[] factories = factories(outliers);
            OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                    .maxOutliers(100)
                    .addFactories(factories)
                    .processor(processor)
                    .build();

            cv = Math.max(cv, GenericOutliersDetection.criticalValue(y.length(), 0.01));
            od.setCriticalValue(cv);

            RegArimaModel regarima = builder.build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima, mapping);
            int[][] io = od.getOutliers();
            o = new OutlierDescriptor[io.length];
            for (int i = 0; i < io.length; ++i) {
                int[] cur = io[i];
                DataBlock xcur = DataBlock.make(y.length());
                factories[cur[1]].fill(cur[0], xcur);
                o[i] = new OutlierDescriptor(factories[cur[1]].getCode(), cur[0]);
                builder.addX(xcur);
            }
        } else {
            o = new OutlierDescriptor[0];
        }
        RegArimaModel regarima = builder.build();
        GlsArimaProcessor<ArimaModel> finalProcessor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(eps)
                .computeExactFinalDerivatives(true)
                .build();
        RegArimaEstimation rslt = finalProcessor.process(regarima, mapping);
        LogLikelihoodFunction.Point max = rslt.getMax();
        DoubleSeq parameters = max.getParameters();

        ExtendedAirline ea = ExtendedAirline.of(spec)
                .toBuilder()
                .p(parameters)
                .build();

        //Ausgabe anpassen
        RegArimaModel model = rslt.getModel();

        DoubleSeq y_fcasts;

        if (nfcasts > 0) {
            ExactArimaForecasts fcasts = new ExactArimaForecasts();
            fcasts.prepare(model.arima(), false); //Jean said mean should not be used
            double[] detAll = new double[y.length()];
            DoubleSeqCursor coeff = rslt.getConcentratedLikelihood().coefficients().cursor();
            FastMatrix variables = regarima.variables();
            for (int j = 0; j < variables.getColumnsCount(); ++j) {
                double c = coeff.getAndNext();
                if (c != 0) {
                    DoubleSeqCursor cursor = variables.column(j).cursor();
                    for (int k = 0; k < y.length(); ++k) {
                        detAll[k] += c * cursor.getAndNext();
                    }
                }
            }

            //lin series is the original series y minus coeff*variables
            double[] y_lin_a = new double[y.length()];
            for (int i = 0; i < y.length(); i++) {
                y_lin_a[i] = y.get(i) - detAll[i];
            }
            DoubleSeq y_lin = DoubleSeq.of(y_lin_a);
            // y plus  \beta X to use as fcast
            DoubleSeq y_fcasts_lin = fcasts.forecasts(y_lin, nfcasts); // we should use the lin series for the fcasts
            double[] y_fcast_a;
            coeff = rslt.getConcentratedLikelihood().coefficients().cursor();
            y_fcast_a = y_fcasts_lin.toArray().clone();
            if (X != null && X.getColumnsCount() != 0) {
                for (int j = 0; j < X.getColumnsCount(); ++j) {
                    double c = coeff.getAndNext();
                    if (c != 0) {
                        for (int k = y.length(); k < y.length() + nfcasts; ++k) {
                            y_fcast_a[k - y.length()] += c * X.column(j).get(k);
                        }
                    }
                }
            }
            y_fcasts = DoubleSeq.of(y_fcast_a);
        } else {
            y_fcasts = DoubleSeq.empty();
        }
        //
        int xNumberRows = 0;
        int xNumberColumns = 0;
        if (X
                != null) {
            xNumberColumns = X.getColumnsCount();
            xNumberRows = X.getRowsCount();
        }

        Matrix regVariables;
        if (nfcasts
                > 0) {
            double[] data = new double[(regarima.variables().getRowsCount() + nfcasts) * regarima.variables().getColumnsCount()];
            for (int i = 0; i < regarima.variables().getRowsCount(); i++) {
                for (int j = 0; j < regarima.variables().getColumnsCount(); j++) {
                    data[i + j * (regarima.variables().getRowsCount() + nfcasts)] = regarima.variables().get(i, j);
                }
            }

            // Regression Variable in the future
            for (int i = regarima.variables().getRowsCount(); i < regarima.variables().getRowsCount() + nfcasts; i++) {
                for (int j = 0; j < xNumberColumns; j++) {
                    data[(i + j * (regarima.variables().getRowsCount() + nfcasts))] = X.get(i, j);
                }
            }

            //zeros for the other variables
            for (int i = regarima.variables().getRowsCount(); i < regarima.variables().getRowsCount() + nfcasts; i++) {
                for (int j = xNumberColumns; j < regarima.variables().getColumnsCount(); j++) {
                    data[(i + j * (regarima.variables().getRowsCount() + nfcasts))] = 0;
                }
            }

            regVariables = Matrix.of(data, xNumberRows, regarima.variables().getColumnsCount());

        } else {
            regVariables = regarima.variables();
        }

        DoubleSeq y_f = regarima.getY().extend(0, nfcasts);
        double[] y_inclFcasts = y_f.toArray();
        for (int i = 0;
                i < nfcasts;
                i++) {
            y_inclFcasts[regarima.getY().length() + i] = y_fcasts.get(i);
        }

        //Missing values are still replaced
        return ExtendedAirlineEstimation.builder()
                .y(y_inclFcasts)
                .x(regVariables)
                .model(ea)
                .coefficients(rslt.getConcentratedLikelihood().coefficients())
                .coefficientsCovariance(rslt.getConcentratedLikelihood().covariance(mapping.getDim(), true))
                .likelihood(rslt.statistics())
                .residuals(rslt.getConcentratedLikelihood().e())
                .outliers(o)
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .log(log)
                .missing(missing)
                .build();
    }

    private void execTransform(ExtendedRegAirlineModelling modelling, ProcessingLog log) {
        log.push("log/level");
        switch (spec.getTransform().getFunction()) {
            case Auto:
                LogLevelModule ll = LogLevelModule.builder()
                        .aiccLogCorrection(spec.getTransform().getAicDiff())
                        .estimationPrecision(1e-5)
                        .build();

                ll.process(modelling);
            //    log.warning("not implemented yet. log used");
break;
            case Log:
                if (modelling.getDescription().getSeries().getValues().allMatch(x -> x > 0)) {
                    modelling.getDescription().setLogTransformation(true);
                } else {
                    log.warning("non positive values; log disabled");
                }
                break;

        }
        log.pop();
    }

    private void execOutliers(ExtendedRegAirlineModelling modelling, ProcessingLog log) {
        log.push("outliers");
        OutlierSpec ospec = spec.getOutlier();
        String[] outliers = ospec.allOutliers();
        LevenbergMarquardtMinimizer.LmBuilder min = LevenbergMarquardtMinimizer.builder().maxIter(5);
        GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class
        )
                .minimizer(min)
                .precision(1e-5)
                .build();
        IOutlierFactory[] factories = factories(outliers);
        OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class
        )
                .maxOutliers(spec.getOutlier().getMaxOutliers())
                .maxRound(spec.getOutlier().getMaxRound())
                .addFactories(factories)
                .processor(processor)
                .build();
        double cv = ospec.getCriticalValue();

        RegArimaModel<ArimaModel> regarima = modelling.getDescription().regarima();
        TsDomain edom = modelling.getDescription().getEstimationDomain();
        cv = Math.max(cv, GenericOutliersDetection.criticalValue(edom.getLength(), 0.01));
        od.setCriticalValue(cv);
        od.prepare(edom.getLength());
        TsDomain odom = edom.select(ospec.getSpan());
        int nb = edom.getStartPeriod().until(odom.getStartPeriod());
        od.setBounds(nb, nb + odom.getLength());
        // remove missing values
        int[] missing = modelling.getDescription().getMissingInEstimationDomain();
        if (missing != null) {
            for (int i = 0; i < missing.length; ++i) {
                for (int j = 0; j < outliers.length; ++j) {
                    od.exclude(missing[i], j);
                }
            }
        }
        // current outliers ([fixed], pre-specified, identified)
        modelling.getDescription().variables()
                .filter(var -> var.getCore() instanceof IOutlier)
                .map(var -> (IOutlier) var.getCore()).forEach(
                o -> od.exclude(edom.indexOf(o.getPosition()), outlierType(outliers, o.getCode())));

        ExtendedAirlineMapping mapping = (ExtendedAirlineMapping) modelling.getDescription().mapping();

        od.process(regarima, mapping);
        int[][] io = od.getOutliers();
        if (io.length > 0) {
            addOutliers(modelling.getDescription(), io);
            modelling.clearEstimation();
        }
        log.pop();
    }

    private static int outlierType(String[] all, String cur) {
        for (int i = 0; i < all.length; ++i) {
            if (cur.equals(all[i])) {
                return i;
            }
        }
        return -1;
    }

    private static IOutlierFactory[] factories(String[] code) {
        List<IOutlierFactory> fac = new ArrayList<>();
        for (int i = 0; i < code.length; ++i) {
            switch (code[i]) {
                case "ao", "AO" ->
                    fac.add(AdditiveOutlierFactory.FACTORY);
                case "wo", "WO" ->
                    fac.add(SwitchOutlierFactory.FACTORY);
                case "ls", "LS" ->
                    fac.add(LevelShiftFactory.FACTORY_ZEROENDED);
            }
        }

        return fac.toArray(IOutlierFactory[]::new);
    }

    private static IOutlier outlier(String code, TsPeriod p) {
        LocalDateTime pos = p.start();
        return switch (code) {
            case "ao", "AO" ->
                AdditiveOutlierFactory.FACTORY.make(pos);
            case "wo", "WO" ->
                SwitchOutlierFactory.FACTORY.make(pos);
            case "ls", "LS" ->
                LevelShiftFactory.FACTORY_ZEROENDED.make(pos);
            default ->
                null;
        };
    }

    private Map<String, String> attributes(IOutlier o) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(ModellingUtility.AMI, "tramo");
        attributes.put(SaVariable.REGEFFECT, SaVariable.defaultComponentTypeOf(o).name());
        return attributes;
    }

    public static ArimaModel estimate(DoubleSeq s, double period) {
        ExtendedAirlineMapping mapping = new ExtendedAirlineMapping(new double[]{period});

        GlsArimaProcessor.Builder<ArimaModel> builder = GlsArimaProcessor.builder(ArimaModel.class
        );
        builder.minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-12)
                .useMaximumLikelihood(true)
                .useParallelProcessing(true)
                .build();
        ArimaModel arima = mapping.getDefault();
        RegArimaModel<ArimaModel> regarima
                = RegArimaModel.<ArimaModel>builder()
                        .y(s)
                        .arima(arima)
                        .build();
        GlsArimaProcessor<ArimaModel> monitor = builder.build();
        RegArimaEstimation<ArimaModel> rslt = monitor.process(regarima, mapping);
        return rslt.getModel().arima();
    }

}
