package jdplus.sts.base.api;

import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import nbbrd.design.LombokWorkaround;

@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class StsSpec implements SaSpecification{
    public static final String METHOD = "stlplus";
    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @lombok.NonNull
    private ModellingSpec preprocessing;
    
    private BsmSpec bsm;
    
    @lombok.NonNull
    private SaBenchmarkingSpec benchmarking;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .preprocessing(ModellingSpec.FULL)
                .bsm(null)
                .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED);
    }


    @Override
    public String display(){
        return SMETHOD;
    }
    
    private static final String SMETHOD = "BSM";
    
    public static final StsSpec FULL=StsSpec.builder()
            .preprocessing(ModellingSpec.FULL)
            .bsm(BsmSpec.DEFAULT)
            .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
            .build();
    
    public static final StsSpec DEFAULT=StsSpec.builder()
            .preprocessing(ModellingSpec.DEFAULT)
            .bsm(BsmSpec.DEFAULT)
            .benchmarking(SaBenchmarkingSpec.DEFAULT_DISABLED)
            .build();
    
}
