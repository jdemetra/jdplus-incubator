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
package jdplus.x11plus.base.api;

import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;
import static jdplus.x11plus.base.api.X11plusSpec.DEFAULT_LOWER_SIGMA;
import static jdplus.x11plus.base.api.X11plusSpec.DEFAULT_UPPER_SIGMA;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class PeriodSpec {
 
    private Number period;

    private FilterSpec trendFilter;
    private SeasonalFilterSpec initialSeasonalFilter, finalSeasonalFilter;


    /**
     * Lower sigma value for extreme values detection [sigmalim option in
     * X12-arima].
     *
     * @param lowerSigma Lower sigma value for extreme values detection.
     * lowerSigma should be lower than upperSigma and higher than .5.
     */
    private double lowerSigma;

    /**
     * Upper sigma value for extreme values detection [sigmalim option in
     * X12-arima].
     *
     * @param upperSigma Upper sigma value for extreme values detection
     */
    private double upperSigma;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .lowerSigma(DEFAULT_LOWER_SIGMA)
                .upperSigma(DEFAULT_UPPER_SIGMA);
   }

 }
