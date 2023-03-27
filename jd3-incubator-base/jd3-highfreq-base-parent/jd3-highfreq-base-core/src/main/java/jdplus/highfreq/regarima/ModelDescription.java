/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.highfreq.regarima;

import demetra.data.DoubleSeq;
import demetra.data.Parameter;
import demetra.modelling.highfreq.CleanedData;
import demetra.modelling.highfreq.DataCleaning;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.Variable;
import demetra.util.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.interpolation.DataInterpolator;
import jdplus.data.transformation.LogJacobian;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.Regression;
import jdplus.stats.likelihood.LogLikelihoodFunction;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import demetra.timeseries.regression.ModellingUtility;
import java.util.Arrays;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.timeseries.simplets.Transformations;
import nbbrd.design.Development;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 * @param <S>
 * @param <M>
 */
@Development(status = Development.Status.Preliminary)
public final class ModelDescription<S extends IArimaModel, M extends ArimaDescription<S>> {

    /**
     * Original series
     */
    private final TsData series;

    private final TsDomain estimationDomain;

    private final CleanedData cleanedData;

    /**
     * Interpolated data (before transformation) and transformed data. They
     * correspond to the cleaned data. So, to retrieve a time series
     * corresponding to the original series, you should use
     */
    private double[] interpolatedData, transformedData;
    private double llCorrection;
    /**
     * Positions of the missing values (if interpolated) in the cleaned data
     */
    private int[] missing = IntList.EMPTY;

    private boolean logTransformation;
    /**
     * Regression variables
     */
    private boolean mean;
    private final List<Variable> variables = new ArrayList<>();

    /**
     * Arima component
     */
    private M model;

    private boolean sortedVariables; //optimization

    public static ModelDescription dummyModel() {
        return new ModelDescription();
    }

    public static ModelDescription copyOf(@NonNull ModelDescription model) {
        return copyOf(model, null);
    }

    public static ModelDescription copyOf(@NonNull ModelDescription model, TsDomain estimationDomain) {
        ModelDescription nmodel = new ModelDescription(model.series, estimationDomain, model.cleanedData.getCleaning());
        nmodel.model = model.model;
        nmodel.logTransformation = model.logTransformation;
        nmodel.interpolatedData = model.interpolatedData;
        nmodel.transformedData = model.transformedData;
        nmodel.missing = model.missing;
        nmodel.llCorrection = model.llCorrection;
        nmodel.mean = model.mean;
        model.variables.forEach(nmodel.variables::add);
        return nmodel;
    }

    private ModelDescription() {
        this.series = null;
        this.estimationDomain = null;
        this.cleanedData = null;
    }

    public ModelDescription(@NonNull TsData series, TsDomain estimationDomain) {
        this(series, estimationDomain, DataCleaning.of(series));
    }

    /**
     * Creates a new Model Description The series should not contain missing
     * values outside the estimation domain when it is specified
     *
     * @param series The given series
     * @param estimationDomain Estimation domain. Can be null. In that case, the
     * estimation will be performed on the whole series
     * @param cleaning
     */
    public ModelDescription(@NonNull TsData series, TsDomain estimationDomain, DataCleaning cleaning) {
        this.series = series;
        TsData s;
        if (estimationDomain != null) {
            estimationDomain = estimationDomain.intersection(series.getDomain());
            // check possible missing values
            int beg = series.getStart().until(estimationDomain.getStartPeriod());
            if (series.getValues().range(0, beg).anyMatch(z -> !Double.isFinite(z))) {
                throw new TsException("Missing values outside the estimation domain");
            }
            int end = series.getStart().until(estimationDomain.getEndPeriod());
            if (series.getValues().range(end, series.length()).anyMatch(z -> !Double.isFinite(z))) {
                throw new TsException("Missing values outside the estimation domain");
            }
            this.estimationDomain = estimationDomain;
            s = TsData.fitToDomain(series, estimationDomain);
        } else {
            this.estimationDomain = null;
            s = series;
        }
        cleanedData = CleanedData.of(s, cleaning);

    }

    // the regression variables are organized as follows:
    // [0. additive outliers_ for missing values]
    // [1. Mean correction]
    // 2 users
    // 3 calendars
    // 4 moving holidays
    // 5 outliers, 5.1 pre-specified, 5.2 detected 
    private void sortVariables() {
        if (sortedVariables) {
            return;
        }
        List<Variable> vars = new ArrayList<>();
        variables.stream()
                .filter(v -> ModellingUtility.isUser(v))
                .forEachOrdered(v -> vars.add(v));
        variables.stream()
                .filter(v -> ModellingUtility.isDaysRelated(v))
                .forEachOrdered(v -> vars.add(v));
        variables.stream()
                .filter(v -> ModellingUtility.isMovingHoliday(v))
                .forEachOrdered(v -> vars.add(v));
        variables.stream()
                .filter(v -> ModellingUtility.isOutlier(v))
                .filter(v -> !ModellingUtility.isAutomaticallyIdentified(v))
                .forEachOrdered(v -> vars.add(v));
        variables.stream()
                .filter(v -> ModellingUtility.isOutlier(v))
                .filter(v -> ModellingUtility.isAutomaticallyIdentified(v))
                .forEachOrdered(v -> vars.add(v));
        variables.clear();
        variables.addAll(vars);
        sortedVariables = true;
    }

    private void buildTransformation() {
        if (transformedData == null) {
            int diff = model.differencingOrders();
            LogJacobian lj;
            TsData tmp;
            lj = new LogJacobian(diff, series.length(), missing);
            tmp = interpolatedData == null ? series : TsData.ofInternal(series.getStart(), interpolatedData);
            if (logTransformation) {
                tmp = Transformations.log().transform(tmp, lj);
            }
            llCorrection = lj.value;

            // remove preadjustment
            if (hasFixedEffects()) {
                final DataBlock ndata = DataBlock.of(tmp.getValues());
                final TsDomain domain = tmp.getDomain();
                variables.forEach(v -> {
                    if (!v.isFree()) {
                        FastMatrix m = Regression.matrix(domain, v.getCore());
                        DataBlockIterator columns = m.columnsIterator();
                        int cur = 0;
                        while (columns.hasNext()) {
                            DataBlock col = columns.next();
                            Parameter c = v.getCoefficient(cur++);
                            if (c.isFixed()) {
                                ndata.addAY(-c.getValue(), col);
                            }
                        }
                    }
                });
                tmp = TsData.ofInternal(domain.getStartPeriod(), ndata.getStorage());
            }
            transformedData = tmp.getValues().toArray();
        }
    }

    /**
     * Gets the estimation domain of the model
     *
     * @return
     */
    public TsDomain getEstimationDomain() {
        return estimationDomain == null ? series.getDomain() : estimationDomain;
    }

    public int[] getMissingInEstimationDomain() {
        if (estimationDomain == null || missing.length == 0) {
            return missing;
        }
        int start = series.getStart().until(estimationDomain.getStartPeriod());
        if (start == 0) {
            return missing;
        }
        int[] nmissing = missing.clone();
        for (int i = 0; i < nmissing.length; ++i) {
            nmissing[i] -= start;
        }
        return nmissing;
    }

    /**
     * Gets the regarima model corresponding to the estimation domain
     *
     * @return
     */
    public RegArimaModel<S> regarima() {
        buildTransformation();
        sortVariables();
        TsDomain domain = getEstimationDomain();
        double[] y = transformedData;
        DoubleSeq yc;
        int n = domain.getLength();
        int[] missingc = missing;
        if (y.length > n) {
            int pos = series.getStart().until(domain.getStartPeriod());
            yc = DoubleSeq.of(y, pos, n);
            if (missing.length > 0) {
                missingc = missing.clone();
                for (int i = 0; i < missingc.length; ++i) {
                    missingc[i] -= pos;
                }
            }
        } else {
            yc = DoubleSeq.of(y);
        }
        RegArimaModel.Builder builder = RegArimaModel.<ArimaModel>builder()
                .y(yc)
                .missing(missingc)
                .meanCorrection(mean)
                .arima(model.arima());
        List<Variable> excluded = new ArrayList<>();
        for (Variable v : variables) {
            if (!v.isPreadjustment()) {
                FastMatrix x = Regression.matrix(domain, v.getCore());
                if (x == null) {
                    excluded.add(v);
                } else {
                    DataBlockIterator columns = x.columnsIterator();
                    int ic = 0;
                    while (columns.hasNext()) {
                        DataBlock col = columns.next();
                        if (v.getCoefficient(ic++).isFree()) {
                            builder.addX(col.unmodifiable());
                        }
                    }
                }
            }
        }
        if (!excluded.isEmpty()) {
            variables.replaceAll(v -> v.exclude(true));
        }
        return builder.build();
    }

    private void invalidateTransformation() {
        this.transformedData = null;
        this.llCorrection = 0;
    }

    public Variable variable(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getName().equals(name))
                .findFirst();
        return search.orElse(null);
    }

    public Variable variable(ITsVariable v) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getCore() == v)
                .findFirst();
        return search.orElse(null);
    }

    public boolean remove(String name) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getName().equals(name))
                .findFirst();
        if (search.isPresent()) {
            variables.remove(search.orElseThrow());
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(ITsVariable v) {
        Optional<Variable> search = variables.stream()
                .filter(var -> var.getCore() == v)
                .findFirst();
        if (search.isPresent()) {
            variables.remove(search.orElseThrow());
            return true;
        } else {
            return false;
        }
    }

    public Variable addVariable(Variable var) {
        String name = var.getName();
        while (contains(name)) {
            name = ITsVariable.nextName(name);
        }
        Variable nvar = var.rename(name);
        variables.add(nvar);
        sortedVariables = false;
        return nvar;
    }

    public boolean contains(String name) {
        return variables.stream()
                .anyMatch(var -> var.getName().equals(name));
    }

    public void setLogTransformation(boolean log) {
        if (this.logTransformation == log) {
            return;
        }
        this.logTransformation = log;
        invalidateTransformation();
    }

    public boolean isLogTransformation() {
        return this.logTransformation;
    }

    /**
     * @return the original_
     */
    public TsData getSeries() {
        return series;
    }

    /**
     * @return the estimationDomain_
     */
    public TsDomain getDomain() {
        return series.getDomain();
    }

    public TsData getTransformedSeries(boolean correctedForMissings) {
        buildTransformation();
        if (correctedForMissings || missing.length == 0) {
            return TsData.ofInternal(series.getStart(), transformedData);
        } else {
            double[] data = transformedData.clone();
            for (int i = 0; i < missing.length; ++i) {
                data[missing[i]] = Double.NaN;
            }
            return TsData.ofInternal(series.getStart(), data);
        }
    }

    /**
     * Gets the transformed original series. The original may be transformed for
     * leap year correction or log-transformation and for fixed effects. The
     * fixed effects are always applied additively after the log-transformation.
     * The transformed original may also be corrected for missing values when an
     * interpolator is provided
     *
     * @return
     */
    public TsData getTransformedSeries() {
        buildTransformation();
        return TsData.ofInternal(series.getStart(), transformedData);
    }

    public TsData getInterpolatedSeries() {
        if (interpolatedData == null) {
            return series;
        } else {
            return TsData.ofInternal(series.getStart(), interpolatedData);
        }
    }

    /**
     * @return the arima
     */
    public M getStochasticSpec() {
        return model;
    }

    public void setStochasticSpec(M spec) {
        model = spec;
        if (transformedData != null) {
            transformedData = null;
            buildTransformation();
        }
    }

    public IArimaModel arima() {
        return model.arima();
    }

    /**
     * @return the mean_
     */
    public boolean isMean() {
        return mean;
    }

    public boolean hasFixedEffects() {
        return variables.stream().anyMatch(v -> !v.isFree());
    }

    /**
     * @return the variables
     */
    public Stream<Variable> variables() {
        return variables.stream();
    }

    /**
     * Counts all the (non fixed) regression variables, which statisfy a given
     * condition
     *
     * @param pred The condition
     * @return The number of regressors (>= # variables)
     */
    public int countRegressors(Predicate<Variable> pred) {
        return variables()
                .filter(pred)
                .mapToInt(var -> var.getCore().dim()).sum();
    }

    /**
     * Interpolates missing values. The interpolation is processed on the
     * cleaned series, which depends on the estimation period and on the
     * cleaning option (sundays, weekends...) The position of the missing are
     * relative to the cleaned series
     *
     * @param interpolator
     */
    public void interpolate(@NonNull DataInterpolator interpolator) {
        DoubleSeq cdata = cleanedData.getData();
        if (cdata.anyMatch(z -> Double.isNaN(z))) {
            IntList lmissing = new IntList();
            interpolatedData = interpolator.interpolate(cdata, lmissing);
            if (lmissing.isEmpty()) {
                missing = IntList.EMPTY;
            } else {
                missing = lmissing.toArray();
                Arrays.sort(missing);
            }
            invalidateTransformation();
        } else {
            interpolatedData = null;
            missing = IntList.EMPTY;
        }
    }

    public void setMean(boolean mean) {
        this.mean = mean;
    }

    public boolean removeVariable(Predicate<Variable> pred) {
        if (variables.removeIf(pred.and(var -> ModellingUtility.isAutomaticallyIdentified(var)))) {
            return true;
        } else {
            return false;
        }
    }

    public int getAnnualFrequency() {
        return series.getAnnualFrequency();
    }

    /**
     * Position of the variable in the generated regarima model, The returned
     * position take into account an eventual mean correction.
     *
     * @param variable
     * @return -1 if not found, otherwise the position of the first free
     * coefficient of the considered variable, corrected for the presence of a
     * mean correction (which is always the first one).
     */
    public int findPosition(ITsVariable variable) {
        sortVariables();
        int pos = 0;
        boolean found = false;
        for (Variable var : variables) {
            if (!var.isPreadjustment()) {
                if (var.getCore() == variable) {
                    found = true;
                    break;
                } else {
                    pos += var.freeCoefficientsCount();
                }
            }
        }
        if (!found) {
            return -1;
        }
        return mean ? pos + 1 : pos;
    }

    public IArimaMapping<S> mapping() {
        return model.mapping();
    }

    public RegArimaEstimation<S> estimate(ArimaComputer processor) {

        RegArimaModel<S> regarima = regarima();
        RegArimaEstimation<S> rslt = processor.process(regarima, mapping());
        // update current description
        LogLikelihoodFunction.Point<RegArimaModel<S>, ConcentratedLikelihoodWithMissing> max = rslt.getMax();
        if (max != null) {
            setFreeParameters(max.getParameters());
        }
        return RegArimaEstimation.<S>builder()
                .model(rslt.getModel())
                .concentratedLikelihood(rslt.getConcentratedLikelihood())
                .max(max)
                .llAdjustment(llCorrection)
                .build();
    }

    public void setFreeParameters(DoubleSeq p) {
        
        model=(M) model.withParameters(p);
    }

}
