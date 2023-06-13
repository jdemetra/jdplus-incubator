/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.highfreq.base.core.extendedairline.decomposition;

import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineKernel;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineResults;
import jdplus.highfreq.base.core.regarima.HighFreqRegArimaModel;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.highfreq.base.api.ExtendedAirlineDecompositionSpec;
import jdplus.highfreq.base.api.ExtendedAirlineDictionaries;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.highfreq.SeriesSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.sa.base.core.PreliminaryChecks;
import jdplus.sa.base.core.modelling.TwoStepsDecomposition;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDecompositionKernel {

    public static final String EA = "extended airline";

    private static PreliminaryChecks.Tool of(ExtendedAirlineDecompositionSpec spec) {

        SeriesSpec series = spec.getPreprocessing().getSeries();
        return (s, logs) -> {
            // TODO
            //TsData sc = s.select(series.getSpan());
            //
            return s;
        };
    }

    PreliminaryChecks.Tool check;
    private final ExtendedAirlineKernel preprocessor;
    private final DecompositionKernel decomposer;

    public ExtendedAirlineDecompositionKernel(ExtendedAirlineDecompositionSpec spec, ModellingContext context) {
        check = of(spec);
        this.preprocessor = ExtendedAirlineKernel.of(spec.getPreprocessing(), context);
        this.decomposer = new DecompositionKernel(spec.getDecomposition());
    }

    public static final SeriesDecomposition finalComponents(ExtendedAirlineDecomposition decomp, TsDomain edom) {

        SeriesDecomposition.Builder builder = SeriesDecomposition.builder(decomp.isMultiplicative() ? DecompositionMode.Multiplicative : DecompositionMode.Additive);
        int nb = decomp.getBackcastsCount(), nf = decomp.getForecastsCount(), n = edom.length(), ntot = n + nb + nf;

        TsPeriod start = edom.getStartPeriod(), fstart = edom.getEndPeriod(), bstart = start.plus(-nb);
        DoubleSeq z = decomp.getFinalComponent(ExtendedAirlineDictionaries.Y_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.Series, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.Series, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.Series, ComponentInformation.Forecast);
            }
        }
        z = decomp.getFinalComponent(ExtendedAirlineDictionaries.SA_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            }
        }
        z = decomp.getFinalComponent(ExtendedAirlineDictionaries.T_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.Trend, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.Trend, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.Trend, ComponentInformation.Forecast);
            }
        }
        z = decomp.getFinalComponent(ExtendedAirlineDictionaries.S_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.Seasonal, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.Seasonal, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.Seasonal, ComponentInformation.Forecast);
            }
        }
        z = decomp.getFinalComponent(ExtendedAirlineDictionaries.I_CMP);
        if (!z.isEmpty()) {
            builder.add(TsData.of(start, z.range(nb, nb + n)), ComponentType.Irregular, ComponentInformation.Value);
            if (nb > 0) {
                builder.add(TsData.of(bstart, z.range(0, nb)), ComponentType.Irregular, ComponentInformation.Backcast);
            }
            if (nf > 0) {
                builder.add(TsData.of(fstart, z.range(ntot - nf, ntot)), ComponentType.Irregular, ComponentInformation.Forecast);
            }
        }
        return builder.build();
    }

    public ExtendedAirlineResults process(TsData y, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        HighFreqRegArimaModel preprocessing = preprocessor.process(y, log);
        TsData lin = preprocessing.linearizedSeries();
        boolean mul = preprocessing.getDescription().isLogTransformation();
        ExtendedAirlineDecomposition decomp = decomposer.process(lin.getValues(), mul, log);

        // compute the final decomposition
        SeriesDecomposition components = finalComponents(decomp, lin.getDomain());
        SeriesDecomposition finals = TwoStepsDecomposition.merge(preprocessing, components);

        return ExtendedAirlineResults.builder()
                .preprocessing(preprocessing)
                .decomposition(decomp)
                .components(components)
                .finals(finals)
                .log(log)
                .build();
    }

}
