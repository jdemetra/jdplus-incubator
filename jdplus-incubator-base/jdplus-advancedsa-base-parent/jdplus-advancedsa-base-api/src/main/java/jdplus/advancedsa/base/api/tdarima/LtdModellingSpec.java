/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.advancedsa.base.api.tdarima;

import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.modelling.regular.CalendarSpec;
import jdplus.toolkit.base.api.modelling.regular.EasterSpec;
import jdplus.toolkit.base.api.modelling.regular.EstimateSpec;
import jdplus.toolkit.base.api.modelling.regular.OutlierSpec;
import jdplus.toolkit.base.api.modelling.regular.RegressionSpec;
import jdplus.toolkit.base.api.modelling.regular.SeriesSpec;
import jdplus.toolkit.base.api.modelling.regular.TradingDaysSpec;
import jdplus.toolkit.base.api.modelling.regular.TransformSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.util.Validatable;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class LtdModellingSpec implements Validatable<LtdModellingSpec>, ProcSpecification {

    public static final String METHOD = "demetra.airline";
    public static final String FAMILY = "Modelling";

    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    public static final LtdModellingSpec DEFAULT = LtdModellingSpec.builder().build(), DISABLED=builder().enabled(false).build();

    @lombok.NonNull
    private SeriesSpec series;
    
    private boolean enabled;

    @lombok.NonNull
    private TransformSpec transform;

    @lombok.NonNull
    private EstimateSpec estimate;

    @lombok.NonNull
    private OutlierSpec outliers;

    @lombok.NonNull
    private RegressionSpec regression;

    @lombok.NonNull
    private LtdSarimaSpec ltdarima;

    @LombokWorkaround
    public static Builder builder() {
        LtdSarimaSpec ltdsarima = LtdSarimaSpec.airline();
        return new Builder()
                .series(SeriesSpec.DEFAULT)
                .enabled(true)
                .transform(TransformSpec.DEFAULT)
                .estimate(EstimateSpec.DEFAULT)
                .outliers(OutlierSpec.DEFAULT_ENABLED)
                .regression(RegressionSpec.DEFAULT)
                .ltdarima(ltdsarima);
    }

    @Override
    public LtdModellingSpec validate() throws IllegalArgumentException {
        outliers.validate();
        regression.validate();
        return this;
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<LtdModellingSpec> {

    }

    //<editor-fold defaultstate="collapsed" desc="Default Specifications">
    public static final LtdModellingSpec FULL;

    static {
        
        TransformSpec tr = TransformSpec.builder()
                .function(TransformationType.Auto)
                .build();

        EasterSpec e = EasterSpec.builder()
                .type(EasterSpec.Type.EASTER)
                .test(true)
                .build();

        TradingDaysSpec td = TradingDaysSpec.automatic(LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.BIC, TradingDaysSpec.DEF_PTD, true);

        CalendarSpec cal = CalendarSpec.builder()
                .easter(e)
                .tradingDays(td)
                .build();
        RegressionSpec reg = RegressionSpec.builder()
                .calendar(cal)
                .build();

        OutlierSpec o = OutlierSpec.builder()
                .ao(true)
                .ls(true).build();

        FULL = LtdModellingSpec.builder()
                .series(SeriesSpec.DEFAULT)
                .enabled(true)
                .transform(tr)
                .estimate(EstimateSpec.DEFAULT)
                .outliers(o)
                .regression(reg)
                .ltdarima(LtdSarimaSpec.airline())
                .build();
    }

    //</editor-fold>
    @Override
    public String display() {
        return SMETHOD;
    }

    private static final String SMETHOD = "Demetra-ltdarima";

}
