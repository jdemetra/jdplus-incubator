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
package jdplus.sts.base.core;

import jdplus.sa.base.core.CholetteProcessor;
import jdplus.sa.base.core.PreliminaryChecks;
import jdplus.advancedsa.base.core.regarima.FastKernel;
import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.api.StsSpec;
import jdplus.toolkit.base.api.modelling.regular.SeriesSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 *
 * @author palatej
 */
public class StsKernel {

    private static PreliminaryChecks.Tool of(StsSpec spec) {

        SeriesSpec series = spec.getPreprocessing().getSeries();
        return (s, logs) -> {
            TsData sc = s.select(series.getSpan());
            if (series.isPreliminaryCheck()) {
                PreliminaryChecks.testSeries(sc);
            }
            if (!spec.getPreprocessing().isEnabled()) {
                return s.select(series.getSpan());
            } else {
                return s;
            }
        };
    }

    private PreliminaryChecks.Tool preliminary;
    private FastKernel preprocessor;
    private BsmSpec spec;
    private boolean preprop;
    private CholetteProcessor cholette;

    public static StsKernel of(StsSpec spec, ModellingContext context) {
        PreliminaryChecks.Tool check = of(spec);
        boolean blPreprop = spec.getPreprocessing().isEnabled();
        FastKernel preprocessor = FastKernel.of(spec.getPreprocessing(), context);
//        return new StsKernel(check, preprocessor, spec.getX11(), blPreprop, CholetteProcessor.of(spec.getBenchmarking()));
        return null;
    }

    public StsResults process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        try {
            // Step 0. Preliminary checks
            TsData sc = preliminary.check(s, log);
            // Step 1. Preprocessing
            RegSarimaModel preprocessing;
//            X13plusPreadjustment preadjustment;
//            TsData alin;
//            if (preprocessor != null) {
//                preprocessing = preprocessor.process(sc, log);
//                // Step 2. Link between regarima and x11
//                int nb = spec == null ? 0 : spec.getBackcastHorizon();
//                if (nb < 0) {
//                    nb = -nb * s.getAnnualFrequency();
//                }
//                int nf = spec == null ? 0 : spec.getForecastHorizon();
//                if (nf < 0) {
//                    nf = -nf * s.getAnnualFrequency();
//                }
//                X13plusPreadjustment.Builder builder = X13plusPreadjustment.builder();
//                alin = initialStep(preprocessing, nb, nf, builder);
//                preadjustment = builder.build();
//            } else {
//                preprocessing = null;
//                preadjustment = X13plusPreadjustment.builder().a1(sc).build();
//                alin = sc;
//            }
//            // Step 3. X11
//            X11plusSpec nspec = updateSpec(spec, preprocessing);
//            X11plusKernel x11 = X11plusKernel.of(nspec);
//            X11plusResults xr = x11.process(alin);
//            X13plusFinals finals = finals(nspec.getMode(), preadjustment, xr);
//            SaBenchmarkingResults bench = null;
//            if (cholette != null) {
//                bench = cholette.process(s, TsData.concatenate(finals.getD11final(), finals.getD11a()), preprocessing);
//            }
            return StsResults.builder()
                    //                    .preprocessing(preprocessing)
                    //                    .preadjustment(preadjustment)
                    //                    .decomposition(xr)
                    //                    .finals(finals)
                    //                    .benchmarking(bench)
                    //                    .diagnostics(X13plusDiagnostics.of(preprocessing, preadjustment, xr, finals))
                    .log(log)
                    .build();
        } catch (Exception err) {
            log.error(err);
            return null;
        }
    }
}
