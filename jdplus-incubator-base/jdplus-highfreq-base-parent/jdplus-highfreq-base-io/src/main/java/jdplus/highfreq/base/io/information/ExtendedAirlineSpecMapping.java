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

import jdplus.highfreq.base.api.ExtendedAirlineSpec;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.information.InformationSet;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
class ExtendedAirlineSpecMapping {

    final String MEAN = "mean", PERIODS = "periods", DIFF = "differencing",
            PHI = "phi", THETA = "theta", STHETA = "stheta", TOINT = "toint";

    public InformationSet write(ExtendedAirlineSpec spec, boolean verbose) {
        InformationSet info = new InformationSet();
        if (verbose || spec.isMean()) {
            info.add(MEAN, spec.isMean());
        }
        info.add(PERIODS, spec.getPeriodicities());
        if (verbose || spec.getDifferencingOrder() != -1) {
            info.add(DIFF, spec.getDifferencingOrder());
        }
        if (spec.getPhi() != null) {
            info.add(PHI, spec.getPhi());
        }
        if (spec.getTheta() != null) {
            info.add(THETA, spec.getTheta());
        }
        info.add(STHETA, spec.getStheta());
        if (verbose || spec.isAdjustToInt()) {
            info.add(TOINT, spec.isAdjustToInt());
        }
        return info;
    }

    public ExtendedAirlineSpec read(InformationSet info) {

        ExtendedAirlineSpec.Builder builder = ExtendedAirlineSpec.builder();
        Boolean mean = info.get(MEAN, Boolean.class);
        if (mean != null) {
            builder.mean(mean);
        }
        double[] p = info.get(PERIODS, double[].class);
        if (p != null) {
            builder.periodicities(p);
        }
        Integer d = info.get(DIFF, Integer.class);
        if (d != null) {
            builder.differencingOrder(d);
        }
        Parameter phi = info.get(PHI, Parameter.class);
        if (phi != null) {
            builder.phi(phi);
        }
        Parameter theta = info.get(THETA, Parameter.class);
        if (theta != null) {
            builder.theta(theta);
        }
        Parameter[] stheta = info.get(STHETA, Parameter[].class);
        if (stheta != null) {
            builder.stheta(stheta);
        }
        Boolean toint = info.get(TOINT, Boolean.class);
        if (toint != null) {
            builder.adjustToInt(toint);
        }

        return builder.build();

    }

}
