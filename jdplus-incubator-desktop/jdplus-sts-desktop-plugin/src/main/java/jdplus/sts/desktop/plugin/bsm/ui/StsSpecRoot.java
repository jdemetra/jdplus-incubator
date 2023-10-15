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
package jdplus.sts.desktop.plugin.bsm.ui;

import jdplus.sa.desktop.plugin.descriptors.regular.RegularSpecUI;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.api.StsSpec;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
public class StsSpecRoot implements RegularSpecUI {

    @NonNull
    ModellingSpec preprocessing;
    BsmSpec bsm;
    @NonNull
    SaBenchmarkingSpec benchmarking;
    boolean ro;

    public StsSpecRoot(StsSpec spec, boolean ro) {
        this.preprocessing = spec.getPreprocessing();
        this.bsm = spec.getBsm();
        this.benchmarking = spec.getBenchmarking();
        this.ro = ro;
    }

    public StsSpec getCore() {
        return StsSpec.builder()
                .preprocessing(preprocessing)
                .bsm(bsm)
                .benchmarking(benchmarking)
                .build();
    }

    @Override
    public void update(ModellingSpec spec) {
        preprocessing=spec;
    }

    public void update(BsmSpec spec) {
        bsm = spec;
    }

    @Override
    public ModellingSpec preprocessing() {
        return preprocessing;
    }

}
