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

import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaSpecification;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;
import jdplus.toolkit.base.api.math.linearfilters.HendersonSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class X11plusSpec implements SaSpecification {
    
    public static final String METHOD = "x11plus";
    public static final String VERSION = "1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    
    public static final double DEFAULT_LOWER_SIGMA = 1.5, DEFAULT_UPPER_SIGMA = 2.5;
    public static final int DEFAULT_FORECAST_HORIZON = 0, DEFAULT_BACKCAST_HORIZON = 0;

    /**
     * Decomposition mode of X11
     */
    private DecompositionMode mode;
    
    @lombok.With
    private Number period;
    
    private boolean seasonal;
    
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

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 cycle (defined by
     * period).
     *
     * @param forecastHorizon The forecasts horizon to set.
     */
    private int forecastHorizon;

    /**
     * Number of backcasts used in X11. By default, 0.
     *
     * @param backcastHorizon The backcasts horizon to set.
     */
    private int backcastHorizon;
    
    public static final X11plusSpec DEFAULT_UNDEFINED = X11plusSpec.builder()
            .mode(DecompositionMode.Undefined)
            .build();
    
    public static final X11plusSpec DEFAULT = X11plusSpec.builder()
            .build();
    
    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .forecastHorizon(DEFAULT_FORECAST_HORIZON)
                .backcastHorizon(DEFAULT_BACKCAST_HORIZON)
                .seasonal(true)
                .lowerSigma(DEFAULT_LOWER_SIGMA)
                .upperSigma(DEFAULT_UPPER_SIGMA)
                .mode(DecompositionMode.Multiplicative);
    }
    
    public boolean isDefault() {
        return this.equals(DEFAULT_UNDEFINED);
    }
    
    public static X11plusSpec createDefault(boolean mul, Number period, SeasonalFilterOption seas) {
        int iperiod=period.intValue()/2;
        return builder()
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .period(period)
                .initialSeasonalFilter(new X11SeasonalFilterSpec(period, seas))
                .finalSeasonalFilter(new X11SeasonalFilterSpec(period, seas))
                .trendFilter(new HendersonSpec(iperiod, 3.5))
                .forecastHorizon(-1)
                .build();
    }
    
    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }
    
    @Override
    public String display() {
        return SMETHOD;
    }
    
    private static final String SMETHOD = "X11+";
    
}
