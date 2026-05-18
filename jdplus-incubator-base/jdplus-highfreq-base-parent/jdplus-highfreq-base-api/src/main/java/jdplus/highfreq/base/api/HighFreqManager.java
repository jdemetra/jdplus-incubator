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
package jdplus.highfreq.base.api;

import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.DefaultProcessingLog;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcQuality;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jdplus.toolkit.base.api.information.GenericExplorable;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class HighFreqManager {

    private static final AtomicReference<List<HighFreqProcessingFactory>> PROCESSORS = new AtomicReference<>(HighFreqProcessingFactoryLoader.load());
    private static final AtomicReference<List<HighFreqOutputFactory>> OUTPUT_FACTORIES = new AtomicReference<>(HighFreqOutputFactoryLoader.load());

    public synchronized List<HighFreqProcessingFactory> processors() {
        return PROCESSORS.get();
    }

    public synchronized List<HighFreqOutputFactory> outputFactories() {
        return OUTPUT_FACTORIES.get();
    }

    public synchronized void reload() {
        PROCESSORS.set(HighFreqProcessingFactoryLoader.load());
        OUTPUT_FACTORIES.set(HighFreqOutputFactoryLoader.load());
    }

    public Explorable process(TsData series, HighFreqSpec spec, ModellingContext context, ProcessingLog log) {
        List<HighFreqProcessingFactory> all = processors();
        for (HighFreqProcessingFactory fac : all) {
            HighFreqSpec dspec = fac.decode(spec);
            if (dspec != null) {
                return fac.processor(dspec).process(series, context, log);
            }
        }
        return null;
    }

    public HighFreqEstimation process(HighFreqDefinition def, ModellingContext context, boolean verbose) {
        List<HighFreqProcessingFactory> all = processors();
        HighFreqSpec spec = def.getEstimationSpec();
        for (HighFreqProcessingFactory fac : all) {
            HighFreqSpec dspec = fac.decode(spec);
            if (dspec != null) {
                ProcessingLog log = verbose ? new DefaultProcessingLog() : ProcessingLog.dummy();
                TsData data = def.getTs().getData();
                HighFreqProcessor processor = fac.processor(spec);
                GenericExplorable rslt = processor.process(data, context, log);
                if (rslt.isValid()) {
                    List<String> warnings = new ArrayList<>();
                    List<ProcDiagnostic> tests = new ArrayList<>();
                    fac.fillDiagnostics(tests, warnings, rslt);
                    HighFreqSpec pspec = fac.generateSpec(spec, rslt);
                    ProcQuality quality = ProcDiagnostic.summary(tests);
                    return HighFreqEstimation.builder()
                            .results(rslt)
                            .diagnostics(tests)
                            .quality(quality)
                            .warnings(warnings)
                            .pointSpec(pspec)
                            .build();
                } else {
                    return HighFreqEstimation.builder()
                            .results(rslt)
                            .quality(ProcQuality.Undefined)
                            .build();
                }
            }
        }
        return null;
    }

    public HighFreqEstimation resetQuality(HighFreqEstimation estimation) {
        Explorable rslt = estimation.getResults();
        if (rslt == null) {
            return estimation.withQuality(ProcQuality.Undefined);
        }
        List<HighFreqProcessingFactory> all = processors();
        HighFreqSpec spec = estimation.getPointSpec();
        for (HighFreqProcessingFactory fac : all) {
            HighFreqSpec dspec = fac.decode(spec);
            if (dspec != null) {
                List<String> warnings = new ArrayList<>();
                List<ProcDiagnostic> tests = new ArrayList<>();
                fac.fillDiagnostics(tests, warnings, rslt);
                return estimation.toBuilder()
                        .quality(ProcDiagnostic.summary(tests))
                        .warnings(warnings)
                        .build();
            }
        }
        return estimation.withQuality(ProcQuality.Undefined);
    }

    public <I extends HighFreqSpec> HighFreqProcessingFactory factoryFor(HighFreqSpec spec) {
        List<HighFreqProcessingFactory> all = processors();
        return all.stream().filter(p -> p.canHandle(spec)).findFirst().orElseThrow();
    }

    public <I extends HighFreqSpec> HighFreqProcessingFactory factoryFor(AlgorithmDescriptor desc) {
        List<HighFreqProcessingFactory> all = processors();
        return all.stream().filter(p -> p.descriptor().isCompatible(desc)).findFirst().orElseThrow();
    }
}
