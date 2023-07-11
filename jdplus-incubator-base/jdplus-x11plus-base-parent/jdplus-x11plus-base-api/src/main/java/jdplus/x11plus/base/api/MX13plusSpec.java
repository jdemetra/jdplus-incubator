package jdplus.x11plus.base.api;


import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import jdplus.highfreq.base.api.ExtendedAirlineSpec;
import jdplus.sa.base.api.SaSpecification;
import static jdplus.sa.base.api.SaSpecification.FAMILY;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import nbbrd.design.LombokWorkaround;

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

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class MX13plusSpec implements SaSpecification {

    public static final String METHOD = "mx13plus";
    public static final String VERSION = "1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @lombok.NonNull
    private ExtendedAirlineModellingSpec preprocessing;

    // We will use a default if null !
    private MX11plusSpec x11;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .preprocessing(ExtendedAirlineModellingSpec.DEFAULT_ENABLED)
                .x11(null);
    }

    @Override
    public String display() {
        return SMETHOD;
    }

    private static final String SMETHOD = "MSTL+";

    public static final MX13plusSpec DEFAULT = MX13plusSpec.builder().build();

}
 