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
package jdplus.x12plus.base.core.x12;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.sa.base.api.SaProcessor;
import jdplus.sa.base.api.SaSpecification;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.toolkit.base.core.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.toolkit.base.core.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.toolkit.base.core.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnostics;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaOutOfSampleDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaOutliersDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaResidualsDiagnosticsFactory;
import jdplus.advancedsa.base.core.regarima.FastRegArimaFactory;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.x12plus.base.api.X11plusSpec;
import jdplus.x12plus.base.api.X12plusDictionaries;
import jdplus.x12plus.base.api.X12plusSpec;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(SaProcessingFactory.class)
public class X12plusFactory implements SaProcessingFactory<X12plusSpec, X12plusResults> {

    public static X12plusFactory getInstance() {
        return (X12plusFactory) SaManager.processors().stream().filter(x -> x instanceof X12plusFactory).findAny().orElse(new X12plusFactory());
    }

    private final List<SaDiagnosticsFactory<?, X12plusResults>> diagnostics = new CopyOnWriteArrayList<>();

    public X12plusFactory() {
        diagnostics.addAll(defaultDiagnostics());
    }

    public static List<SaDiagnosticsFactory<?, X12plusResults>> defaultDiagnostics() {
        CoherenceDiagnosticsFactory<X12plusResults> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.getDefault(),
                        (X12plusResults r) -> {
                            return new CoherenceDiagnostics.Input(r.getDecomposition().getMode(), r);
                        }
                );
        SaOutOfSampleDiagnosticsFactory<X12plusResults> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.getDefault(),
                        r -> r.getDiagnostics().getGenericDiagnostics().forecastingTest());
        SaResidualsDiagnosticsFactory<X12plusResults> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        SaOutliersDiagnosticsFactory<X12plusResults> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());

        AdvancedResidualSeasonalityDiagnosticsFactory<X12plusResults> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.getDefault(),
                        (X12plusResults r) -> r.getDiagnostics().getGenericDiagnostics()
                );

        ResidualTradingDaysDiagnosticsFactory<X12plusResults> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.getDefault(),
                        (X12plusResults r) -> {
                            RegSarimaModel preprocessing = r.getPreprocessing();
                            boolean td = false;
                            if (preprocessing != null) {
                                td = Arrays.stream(preprocessing.getDescription().getVariables()).anyMatch(v -> v.getCore() instanceof ITradingDaysVariable);
                            }
                            return new ResidualTradingDaysDiagnostics.Input(r.getDiagnostics().getGenericDiagnostics().residualTradingDaysTests(), td);
                        }
                );

        List<SaDiagnosticsFactory<?, X12plusResults>> all = new ArrayList<>();

        all.add(coherence);
        all.add(residuals);
        all.add(outofsample);
        all.add(outliers);
        all.add(advancedResidualSeasonality);
        all.add(residualTradingDays);
        return all;
    }

    @Override
    public AlgorithmDescriptor descriptor() {
        return X12plusSpec.DESCRIPTOR;
    }

    @Override
    public X12plusSpec generateSpec(X12plusSpec spec, X12plusResults estimation) {
        return generateSpec(spec, estimation.getPreprocessing().getDescription());
    }

    public X12plusSpec generateSpec(X12plusSpec spec, GeneralLinearModel.Description<SarimaSpec> desc) {

        ModellingSpec ntspec = FastRegArimaFactory.getInstance().generateSpec(spec.getPreprocessing(), desc);
        X11plusSpec nsspec = update(spec.getX11());

        return spec.toBuilder()
                .preprocessing(ntspec)
                .x11(nsspec)
                .build();
    }

    @Override
    public X12plusSpec refreshSpec(X12plusSpec currentSpec, X12plusSpec domainSpec, EstimationPolicyType policy, TsDomain domain) {
        // NOT COMPLETE
        if (policy == EstimationPolicyType.None) {
            return currentSpec;
        }
        ModellingSpec ntspec = FastRegArimaFactory.getInstance().refreshSpec(currentSpec.getPreprocessing(), domainSpec.getPreprocessing(), policy, domain);
        return currentSpec.toBuilder()
                .preprocessing(ntspec)
                .build();
    }

    private X11plusSpec update(X11plusSpec stl) {
        // Nothing to do (for the time being)
        return stl;
    }

    @Override
    public SaProcessor processor(X12plusSpec spec) {
        return (s, cxt, log) -> X12plusKernel.of(spec, cxt).process(s, log);
    }

    @Override
    public X12plusSpec decode(SaSpecification spec) {
        if (spec instanceof X12plusSpec) {
            return (X12plusSpec) spec;
        } else {
            return null;
        }
    }

    @Override
    public boolean canHandle(SaSpecification spec) {
        return spec instanceof X12plusSpec;
    }

    @Override
    public List<SaDiagnosticsFactory<?, X12plusResults>> diagnosticFactories() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<?, X12plusResults> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<?, X12plusResults> olddiag, SaDiagnosticsFactory<?, X12plusResults> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

    @Override
    public void resetDiagnosticFactories(List<SaDiagnosticsFactory<?, X12plusResults>> factories) {
        diagnostics.clear();
        diagnostics.addAll(factories);
    }

    @Override
    public Dictionary outputDictionary() {
        return X12plusDictionaries.X13PLUSDICTIONARY;
    }

}
