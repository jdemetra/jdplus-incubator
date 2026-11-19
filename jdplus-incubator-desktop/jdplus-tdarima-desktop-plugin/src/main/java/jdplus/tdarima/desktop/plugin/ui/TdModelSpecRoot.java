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
package jdplus.tdarima.desktop.plugin.ui;

import jdplus.advancedsa.base.api.tdarima.TdEstimationSpec;
import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.advancedsa.base.api.tdarima.TdSarimaSpec;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.sa.desktop.plugin.descriptors.regular.RegularSpecUI;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
public class TdModelSpecRoot implements RegularSpecUI {

    @lombok.NonNull private ModellingSpec preprocessing;

    private boolean usingPreprocessing;

    @lombok.NonNull private TdEstimationSpec tdarima;

    @lombok.NonNull SaBenchmarkingSpec benchmarking;

    boolean ro;

    public TdModelSpecRoot(TdModelSpec spec, boolean ro) {
        this.preprocessing = spec.getPreprocessing();
        this.usingPreprocessing = spec.isUsingPreprocessing();
        this.tdarima = spec.getTdarima();
        this.benchmarking = spec.getBenchmarking();
        this.ro = ro;
    }

    public TdModelSpec getCore() {
        return TdModelSpec.builder()
                .preprocessing(preprocessing)
                .usingPreprocessing(usingPreprocessing)
                .tdarima(tdarima)
                .benchmarking(benchmarking)
                .build();
    }

    @Override
    public void update(ModellingSpec spec) {
        preprocessing = spec;
    }

    public void update(TdEstimationSpec spec) {
        tdarima = spec;
    }

    public void update(TdSarimaSpec spec) {
        tdarima = tdarima.toBuilder().tdspec(spec).build();
    }

    public void setUsingPreprocessing(boolean p) {
        this.usingPreprocessing = p;
    }

    @Override
    public ModellingSpec preprocessing() {
        return preprocessing;
    }
}
