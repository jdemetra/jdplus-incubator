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
package jdplus.stl.base.core.mstlplus;

import jdplus.toolkit.base.api.modelling.highfreq.SeriesSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.stl.base.api.MStlPlusSpec;
import jdplus.stl.base.api.MStlSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineKernel;
import jdplus.highfreq.base.core.regarima.HighFreqRegArimaModel;
import jdplus.sa.base.core.PreliminaryChecks;
import jdplus.sa.base.core.modelling.RegArimaDecomposer;
import jdplus.sa.base.core.modelling.TwoStepsDecomposition;
import jdplus.stl.base.core.MStlKernel;
import jdplus.stl.base.core.MStlResults;

@lombok.Value
public class MStlPlusKernel {

    private static PreliminaryChecks.Tool of(MStlPlusSpec spec) {

        SeriesSpec series = spec.getPreprocessing().getSeries();
        return (s, logs) -> {
            if (!spec.getPreprocessing().isEnabled()) {
                return s.select(series.getSpan());
            } else {
                return s;
            }
        };
    }

    private PreliminaryChecks.Tool preliminary;
    private ExtendedAirlineKernel preprocessor;
    private MStlSpec spec;

    public static MStlPlusKernel of(MStlPlusSpec spec, ModellingContext context) {
        PreliminaryChecks.Tool check = of(spec);
        ExtendedAirlineKernel preprocessor = ExtendedAirlineKernel.of(spec.getPreprocessing(), context);
        return new MStlPlusKernel(check, preprocessor, spec.getStl());
    }

    public MStlPlusResults process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        try {
            if (preprocessor == null) {
                // Step 0. Preliminary checks
                TsData sc = preliminary.check(s, log);
                MStlKernel stl = MStlKernel.of(spec);
                MStlResults rslt = stl.process(sc.getValues());

//                // Step 6. Diagnostics
//                MStlPlusDiagnostics diagnostics = MStlPlusDiagnostics.of(null, rslt, rslt.asDecomposition());
//
                return MStlPlusResults.builder()
                        .preprocessing(null)
                        .decomposition(rslt)
                        .finals(rslt.asDecomposition(sc.getStart()))
                        .log(log)
                        .build();
            } else {
                // Step 0. Preliminary checks
                TsData sc = preliminary.check(s, log);
//                // Step 1. RegArima
                HighFreqRegArimaModel preprocessing = preprocessor.process(sc, log);
//                // Step 2. Link between regarima and stl
                MStlSpec cspec = spec;
                boolean mul = preprocessing.getDescription().isLogTransformation();
                if (cspec.isMultiplicative() != mul) {
                    cspec = spec.toBuilder().multiplicative(mul).build();
                }
                MStlKernel stl = MStlKernel.of(cspec);
                TsData det = preprocessing.deterministicEffect(s.getDomain());
                TsData user = RegArimaDecomposer.deterministicEffect(preprocessing, s.getDomain(), ComponentType.Series, true, v -> ModellingUtility.isUser(v));
                det = TsData.subtract(det, user);
                TsData cseries;
                if (mul) {
                    det = preprocessing.backTransform(det, true);
                    cseries = TsData.divide(s, det);
                } else {
                    cseries = TsData.subtract(s, det);
                }
                MStlResults rslt = stl.process(cseries.getValues());
//
//                StlResults rslt = stl.process(cseries);
//                // Step 4. Final decomposition
                SeriesDecomposition finals = TwoStepsDecomposition.merge(preprocessing, rslt.asDecomposition(sc.getStart()));
//                // Step 5. Benchmarking
//                SaBenchmarkingResults bench = null;
//                if (cholette != null) {
//                    bench = cholette.process(s, TsData.concatenate(finals.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value),
//                            finals.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast)), preprocessing);
//                }
//                // Step 6. Diagnostics
//                MStlPlusDiagnostics diagnostics = MStlPlusDiagnostics.of(preprocessing, rslt, finals);
//
                return MStlPlusResults.builder()
                        .preprocessing(preprocessing)
                        .decomposition(rslt)
                        .finals(finals)
                        .log(log)
                        .build();
            }
        } catch (Exception err) {
            log.error(err);
            return null;
        }
    }
}
