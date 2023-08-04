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

import java.time.temporal.ChronoUnit;
import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import jdplus.sa.base.information.highfreq.OutlierSpecMapping;
import jdplus.sa.base.information.highfreq.RegressionSpecMapping;
import jdplus.sa.base.information.highfreq.SeriesSpecMapping;
import jdplus.sa.base.information.highfreq.TransformSpecMapping;
import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.InformationSetSerializerEx;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsUnit;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ExtendedAirlineModellingSpecMapping {

    public final InformationSetSerializerEx<ExtendedAirlineModellingSpec, TsDomain> SERIALIZER = new InformationSetSerializerEx<ExtendedAirlineModellingSpec, TsDomain>() {
        @Override
        public InformationSet write(ExtendedAirlineModellingSpec object, TsDomain context, boolean verbose) {
            return ExtendedAirlineModellingSpecMapping.write(object, context, verbose);
        }

        @Override
        public ExtendedAirlineModellingSpec read(InformationSet info, TsDomain context) {
            return ExtendedAirlineModellingSpecMapping.read(info, context);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };
    
    
    final String UNIT = "unit", AMOUNT = "amount";

    public InformationSet writeTsUnit(TsUnit unit) {
        InformationSet info = new InformationSet();
        info.add(UNIT, unit.getChronoUnit().name());
        info.add(AMOUNT, (int) unit.getAmount());
        return info;
    }

    public TsUnit readTsUnit(InformationSet info) {
        String unit = info.get(UNIT, String.class);
        Integer amount = info.get(AMOUNT, Integer.class);
        return TsUnit.of(amount, ChronoUnit.valueOf(unit));
    }

    final String PERIOD = "period", SERIES = "series", ENABLED = "enabled",
            ESTIMATE = "estimate", TRANSFORM = "transform",
            REGRESSION = "regression", STOCHASTIC = "stochastic", OUTLIER = "outlier";

    InformationSet write(ExtendedAirlineModellingSpec spec, TsDomain domain, boolean verbose) {
        if (!spec.isEnabled()) {
            return null;
        }
        InformationSet info = new InformationSet();
        info.set(PERIOD, writeTsUnit(spec.getPeriod()));
        info.set(SERIES, SeriesSpecMapping.write(spec.getSeries(), verbose));
        info.set(TRANSFORM, TransformSpecMapping.write(spec.getTransform(), verbose));
        info.set(REGRESSION, TransformSpecMapping.write(spec.getTransform(), verbose));
        info.set(STOCHASTIC, ExtendedAirlineSpecMapping.write(spec.getStochastic(), verbose));
        info.set(OUTLIER, OutlierSpecMapping.write(spec.getOutlier(), verbose));
        return info;
    }

    ExtendedAirlineModellingSpec read(InformationSet info, TsDomain domain) {
        if (info == null) {
            return ExtendedAirlineModellingSpec.DEFAULT_DISABLED;
        }
        ExtendedAirlineModellingSpec.Builder builder = ExtendedAirlineModellingSpec
                .builder().enabled(true);
        builder.period(readTsUnit(info.getSubSet(PERIOD)))
                .series(SeriesSpecMapping.read(info.getSubSet(SERIES)))
                .transform(TransformSpecMapping.read(info.getSubSet(TRANSFORM)))
                .regression(RegressionSpecMapping.read(info.getSubSet(REGRESSION)))
                .stochastic(ExtendedAirlineSpecMapping.read(info.getSubSet(STOCHASTIC)))
                .outlier(OutlierSpecMapping.read(info.getSubSet(OUTLIER)));
        return builder.build();
    }
}
