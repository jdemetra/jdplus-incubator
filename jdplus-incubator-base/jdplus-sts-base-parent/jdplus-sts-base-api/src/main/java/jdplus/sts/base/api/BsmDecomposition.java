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
package jdplus.sts.base.api;

import java.util.Map;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class BsmDecomposition {

    @lombok.Singular
    Map<Component, TsData> cmps, ecmps;

    public static BsmDecomposition of(RawBsmDecomposition d, TsPeriod start) {
        Builder builder = new Builder();
        for (Map.Entry<Component, DoubleSeq> entry : d.getCmps().entrySet()) {
            builder.cmp(entry.getKey(), TsData.of(start, entry.getValue()));
        }
        for (Map.Entry<Component, DoubleSeq> entry : d.getEcmps().entrySet()) {
            builder.ecmp(entry.getKey(), TsData.of(start, entry.getValue()));
        }
        return builder.build();
    }

    public TsData getSeries(Component cmp, boolean stde) {
        return stde ? ecmps.get(cmp) : cmps.get(cmp);
    }
}
