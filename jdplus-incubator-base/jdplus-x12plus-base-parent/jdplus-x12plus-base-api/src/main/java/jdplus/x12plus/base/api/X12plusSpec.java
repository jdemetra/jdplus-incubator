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
package jdplus.x12plus.base.api;

import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class X12plusSpec implements SaSpecification{
    public static final String METHOD = "x13plus";
    public static final String VERSION = "1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @lombok.NonNull
    private ModellingSpec preprocessing;
    
    private X11plusSpec x11;
    
    @lombok.NonNull
    private SaBenchmarkingSpec benchmarking;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .preprocessing(ModellingSpec.FULL)
                .x11(null)
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED);
    }


    @Override
    public String display(){
        return SMETHOD;
    }
    
    private static final String SMETHOD = "X13+";
    
    public static final X12plusSpec FULL=X12plusSpec.builder()
            .preprocessing(ModellingSpec.FULL)
            .x11(null)
            .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
            .build();
    
    public static final X12plusSpec DEFAULT=X12plusSpec.builder()
            .preprocessing(ModellingSpec.DEFAULT)
            .x11(null)
            .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
            .build();
    
}
