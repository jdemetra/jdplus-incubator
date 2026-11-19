/*
 * Copyright 2025 JDemetra+.
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
package jdplus.advancedsa.base.api.tdarima;

import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;

/**
 *
 * @author Jean Palate
 */
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
@lombok.Value
public class TdModelSpec implements SaSpecification {

    public static final String METHOD = "tdarima";
    public static final String VERSION_V3 = "1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    @lombok.EqualsAndHashCode.Exclude
    private int frequency;

    @lombok.NonNull private ModellingSpec preprocessing;

    @lombok.With
    private boolean usingPreprocessing;

    @lombok.NonNull private TdEstimationSpec tdarima;

    @lombok.NonNull SaBenchmarkingSpec benchmarking;

    public static Builder builder() {
        return new Builder()
                .preprocessing(ModellingSpec.DEFAULT)
                .usingPreprocessing(true)
                .tdarima(TdEstimationSpec.DEFAULT)
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED);
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static final TdModelSpec FULL = new Builder()
            .preprocessing(ModellingSpec.FULL)
            .usingPreprocessing(true)
            .tdarima(TdEstimationSpec.FULL)
            .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
            .build();

    public static final TdModelSpec DEFAULT = builder().build();

    @Override
    public int getFrequency() {
        return frequency;
    }

    @Override
    public TdModelSpec setFrequency(int freq) {
        if (freq == frequency) return this;
        ModellingSpec npreprocessing = preprocessing.setFrequency(freq);
        return toBuilder().frequency(freq).preprocessing(npreprocessing).build();
    }

    @Override
    public String display() {
        return "td-arima";
    }

    @Override
    public String longDisplay() {
        return "td-arima";
    }
}
