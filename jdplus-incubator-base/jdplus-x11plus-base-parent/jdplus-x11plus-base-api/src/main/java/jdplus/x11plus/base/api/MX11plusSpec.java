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

import java.util.List;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaSpecification;
import static jdplus.sa.base.api.SaSpecification.FAMILY;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import static jdplus.x11plus.base.api.X11plusSpec.DEFAULT_BACKCAST_HORIZON;
import static jdplus.x11plus.base.api.X11plusSpec.DEFAULT_FORECAST_HORIZON;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class MX11plusSpec implements SaSpecification {

    public static final String METHOD = "MX11plus";
    public static final String VERSION = "1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    /**
     * Decomposition mode of X11
     */
    private DecompositionMode mode;

    @lombok.Singular
    private List<PeriodSpec> periodSpecs;

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
    public static X11plusSpec.Builder builder() {
        return new X11plusSpec.Builder()
                .forecastHorizon(DEFAULT_FORECAST_HORIZON)
                .backcastHorizon(DEFAULT_BACKCAST_HORIZON)
                .mode(DecompositionMode.Multiplicative);
    }

    public boolean isDefault() {
        return this.equals(DEFAULT_UNDEFINED);
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
