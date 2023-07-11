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
import jdplus.toolkit.base.api.information.InformationSet;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class DecompositionSpecMapping {

    final String PERIODS = "periods", ITERATIVE = "iterative", NOISY = "noisy",
            STDEV = "stdev", NBCASTS = "nbcasts", NFCASTS = "nfcasts", BIAS = "bias", TOINT = "toint";

    InformationSet write(DecompositionSpec spec, boolean verbose) {
        InformationSet info = new InformationSet();

        info.add(PERIODS, spec.getPeriodicities());
        info.add(ITERATIVE, spec.isIterative());
        info.add(NOISY, spec.isNoisy());
        info.add(STDEV, spec.isStdev());
        info.add(NBCASTS, spec.getBackcastsCount());
        info.add(NFCASTS, spec.getForecastsCount());
        info.add(BIAS, spec.isBiasCorrection());
        info.add(TOINT, spec.isAdjustToInt());
        return info;
    }

    DecompositionSpec read(InformationSet info) {
        DecompositionSpec.Builder builder = DecompositionSpec.builder();

        double[] p = info.get(PERIODS, double[].class);
        builder.periodicities(p);
        Boolean b = info.get(ITERATIVE, Boolean.class);
        if (b != null) {
            builder.iterative(b);
        }
        b = info.get(NOISY, Boolean.class);
        if (b != null) {
            builder.noisy(b);
        }
        b = info.get(STDEV, Boolean.class);
        if (b != null) {
            builder.stdev(b);
        }
        Integer n = info.get(NBCASTS, Integer.class);
        if (n != null) {
            builder.backcastsCount(n);
        }
        n = info.get(NFCASTS, Integer.class);
        if (n != null) {
            builder.forecastsCount(n);
        }
        b = info.get(BIAS, Boolean.class);
        if (b != null) {
            builder.biasCorrection(b);
        }
        b = info.get(TOINT, Boolean.class);
        if (b != null) {
            builder.adjustToInt(b);
        }
        return builder.build();
    }
}
