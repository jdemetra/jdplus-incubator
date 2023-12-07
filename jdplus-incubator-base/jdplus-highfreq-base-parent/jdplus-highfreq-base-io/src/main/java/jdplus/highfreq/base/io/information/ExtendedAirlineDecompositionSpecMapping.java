/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.highfreq.base.io.information;

import jdplus.highfreq.base.api.DecompositionSpec;
import jdplus.highfreq.base.api.ExtendedAirlineDecompositionSpec;
import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.InformationSetSerializerEx;
import jdplus.toolkit.base.api.timeseries.TsDomain;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ExtendedAirlineDecompositionSpecMapping {
    
    public final InformationSetSerializerEx<ExtendedAirlineDecompositionSpec, TsDomain> SERIALIZER 
            = new InformationSetSerializerEx<ExtendedAirlineDecompositionSpec, TsDomain>() {
        @Override
        public InformationSet write(ExtendedAirlineDecompositionSpec object, TsDomain context, boolean verbose) {
            return ExtendedAirlineDecompositionSpecMapping.write(object, context, verbose);
        }

        @Override
        public ExtendedAirlineDecompositionSpec read(InformationSet info, TsDomain context) {
            return ExtendedAirlineDecompositionSpecMapping.read(info, context);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };
    
    final String PREPROCESSING="preprocessing", DECOMPOSITION="decomposition";
    
    public InformationSet write(ExtendedAirlineDecompositionSpec spec, TsDomain domain, boolean verbose){
        InformationSet info=new InformationSet();
        info.set(PREPROCESSING, ExtendedAirlineModellingSpecMapping.write(spec.getPreprocessing(), domain, verbose));
        info.set(DECOMPOSITION, DecompositionSpecMapping.write(spec.getDecomposition(), verbose));
        return info;
    }
    
    public ExtendedAirlineDecompositionSpec read(InformationSet info, TsDomain domain){
        ExtendedAirlineModellingSpec p = ExtendedAirlineModellingSpecMapping.read(info.getSubSet(PREPROCESSING), domain);
        DecompositionSpec d = DecompositionSpecMapping.read(info.getSubSet(DECOMPOSITION));
        return ExtendedAirlineDecompositionSpec.builder()
                .preprocessing(p)
                .decomposition(d)
                .build();
    }
}
