/*
 * Copyright 2026 JDemetra+.
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
package jdplus.advancedsa.base.core.tdarima;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import jdplus.advancedsa.base.core.ssf.Utility;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaVariable;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.akf.QAugmentation;
import jdplus.toolkit.base.core.ssf.arima.SsfUcarima;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
public class TdDecompositionKernel {

    public TdDecompositionResults process(RegSarimaModel regarima, TdSarimaResults rslts, ProcessingLog log) {
        TdDecompositionResults.Builder dbuilder = TdDecompositionResults.builder();
        processInitial(regarima, dbuilder, log);
        if (rslts != null) {
            processFinal(regarima, rslts, dbuilder, log);
        }
        return dbuilder.build();
    }

    private void processInitial(RegSarimaModel model, TdDecompositionResults.Builder dbuilder, ProcessingLog log) {
        boolean mul = model.getDescription().isLogTransformation();
        TsData series = model.interpolatedSeries(false);
        TsData det = model.deterministicEffect(null, v -> !SaVariable.isRegressionEffect(v, ComponentType.Undefined));
        det = model.backTransform(det, true);
        // we remove all the regression effects except the undefined ones (which will be included in the different
        // components)
        TsData ls;
        if (mul) {
            series = TsData.divide(series, det);
            ls = series.log();
        } else {
            series = TsData.subtract(series, det);
            ls = series;
        }

        UcarimaModel ucm = TdArimaDecomposer.ucm(model.arima(), series.getAnnualFrequency());
        CompositeSsf ssf = SsfUcarima.of(ucm);

        DefaultSmoothingResults srslts =
                AkfToolkit.smooth(ssf, new SsfData(ls.getValues()), true, true, true, QAugmentation.QType.NORMAL);
        int[] cmps = ssf.componentsPosition();
        TsPeriod start = ls.getStart();
        TsData trend = TsData.of(start, srslts.getComponent(cmps[0]));
        TsData seas = TsData.of(start, srslts.getComponent(cmps[1]));
        TsData irr = TsData.of(start, srslts.getComponent(cmps[2]));
        TsData etrend = TsData.of(start, srslts.getComponentVariance(cmps[0])).fn(x -> x < 0 ? 0 : Math.sqrt(x));
        TsData eseas = TsData.of(start, srslts.getComponentVariance(cmps[1])).fn(x -> x < 0 ? 0 : Math.sqrt(x));
        TsData eirr = TsData.of(start, srslts.getComponentVariance(cmps[2])).fn(x -> x < 0 ? 0 : Math.sqrt(x));
        TsData sa = TsData.subtract(ls, seas);
        SeriesDecomposition decomp = SeriesDecomposition.builder(DecompositionMode.Additive)
                .add(ls, ComponentType.Series)
                .add(trend, ComponentType.Trend)
                .add(seas, ComponentType.Seasonal)
                .add(irr, ComponentType.Irregular)
                .add(sa, ComponentType.SeasonallyAdjusted)
                .add(etrend, ComponentType.Trend, ComponentInformation.Stdev)
                .add(eseas, ComponentType.Seasonal, ComponentInformation.Stdev)
                .add(eirr, ComponentType.Irregular, ComponentInformation.Stdev)
                .add(eseas, ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev)
                .build();
        dbuilder.stochasticSeries(decomp);
        if (mul) {
            decomp = SeriesDecomposition.builder(DecompositionMode.Multiplicative)
                    .add(series, ComponentType.Series)
                    .add(trend.exp(), ComponentType.Trend)
                    .add(seas.exp(), ComponentType.Seasonal)
                    .add(irr.exp(), ComponentType.Irregular)
                    .add(sa.exp(), ComponentType.SeasonallyAdjusted)
                    .build();
        }
        dbuilder.components(decomp);
    }

    private void processFinal(
            RegSarimaModel model, TdSarimaResults rslts, TdDecompositionResults.Builder dbuilder, ProcessingLog log) {
        try {
            boolean mul = model.getDescription().isLogTransformation();
            TsData series = model.interpolatedSeries(false);
            TsData det = deterministicEffect(
                    model,
                    rslts.getCoefficients(),
                    null,
                    v -> !SaVariable.isRegressionEffect(v, ComponentType.Undefined));
            det = model.backTransform(det, true);
            // we remove all the regression effects except the undefined ones (which will be included in the different
            // components)
            TsData ls;
            if (mul) {
                series = TsData.divide(series, det);
                ls = series.log();
            } else {
                series = TsData.subtract(series, det);
                ls = series;
            }
            int n = series.length();
            IntFunction<IArimaModel> fmodels = rslts.getModel().models();
            TdArimaDecomposer ucmKernel = new TdArimaDecomposer(series.getAnnualFrequency(), n, fmodels, log);
            UcarimaModel[] ucarimaModels = ucmKernel.ucarimaModels();
            dbuilder.models(Arrays.asList(ucarimaModels));
            CompositeSsf ssf = TdSsfUcarima.of(n, i -> ucarimaModels[i]);
            int[] dim = ssf.componentsDimension();
            FastMatrix cmps = FastMatrix.make(n, dim.length);
            FastMatrix ecmps = FastMatrix.make(n, dim.length);
            Utility.smoothedComponents(ssf, ls.getValues(), cmps, ecmps);
            ecmps.apply(x -> x > 0 ? Math.sqrt(x) : 0);
            TsPeriod start = ls.getStart();
            TsData trend = TsData.of(start, cmps.column(0));
            TsData seas = TsData.of(start, cmps.column(1));
            TsData irr = TsData.of(start, cmps.column(2));
            TsData etrend = TsData.of(start, ecmps.column(0));
            TsData eseas = TsData.of(start, ecmps.column(1));
            TsData eirr = TsData.of(start, ecmps.column(2));
            TsData sa = TsData.subtract(ls, seas);
            SeriesDecomposition decomp = SeriesDecomposition.builder(DecompositionMode.Additive)
                    .add(ls, ComponentType.Series)
                    .add(trend, ComponentType.Trend)
                    .add(seas, ComponentType.Seasonal)
                    .add(irr, ComponentType.Irregular)
                    .add(sa, ComponentType.SeasonallyAdjusted)
                    .add(etrend, ComponentType.Trend, ComponentInformation.Stdev)
                    .add(eseas, ComponentType.Seasonal, ComponentInformation.Stdev)
                    .add(eirr, ComponentType.Irregular, ComponentInformation.Stdev)
                    .add(eseas, ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev)
                    .build();
            dbuilder.tdStochasticSeries(decomp);
            if (mul) {
                decomp = SeriesDecomposition.builder(DecompositionMode.Multiplicative)
                        .add(series, ComponentType.Series)
                        .add(trend.exp(), ComponentType.Trend)
                        .add(seas.exp(), ComponentType.Seasonal)
                        .add(irr.exp(), ComponentType.Irregular)
                        .add(sa.exp(), ComponentType.SeasonallyAdjusted)
                        .build();
            }
            dbuilder.tdComponents(decomp);
        } catch (Exception err) {

        }
    }

    public static TsData deterministicEffect(
            GeneralLinearModel model, DoubleSeq coeff, TsDomain domain, Predicate<Variable> test) {
        GeneralLinearModel.Description description = model.getDescription();
        if (domain == null) {
            domain = description.getSeries().getDomain();
        }
        DataBlock all = DataBlock.make(domain.getLength());
        Variable[] variables = description.getVariables();
        if (variables.length > 0) {
            DoubleSeqCursor cursor = coeff.cursor();
            for (int i = 0; i < variables.length; ++i) {
                Variable cur = variables[i];
                if (test.test(cur)) {
                    FastMatrix m = Regression.matrix(domain, cur.getCore());
                    int ic = 0;
                    DataBlockIterator cols = m.columnsIterator();
                    while (cols.hasNext()) {
                        DataBlock col = cols.next();
                        Parameter c = cur.getCoefficient(ic++);
                        if (c.isFree()) {
                            all.addAY(cursor.getAndNext(), col);
                        } else {
                            all.addAY(c.getValue(), col);
                        }
                    }
                } else {
                    cursor.skip(cur.freeCoefficientsCount());
                }
            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), all.getStorage());
    }
}
