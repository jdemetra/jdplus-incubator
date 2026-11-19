/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.advancedsa.base.api.modelling;

import jdplus.toolkit.base.api.DemetraException;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.modelling.regular.CalendarSpec;
import jdplus.toolkit.base.api.modelling.regular.EasterSpec;
import jdplus.toolkit.base.api.modelling.regular.EstimateSpec;
import jdplus.toolkit.base.api.modelling.regular.MeanSpec;
import jdplus.toolkit.base.api.modelling.regular.OutlierSpec;
import jdplus.toolkit.base.api.modelling.regular.RegressionSpec;
import jdplus.toolkit.base.api.modelling.regular.TradingDaysSpec;
import jdplus.toolkit.base.api.modelling.regular.TransformSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.api.util.Validatable;
import lombok.NonNull;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;

/**
 * Defines the specifications for the Tramo pre-processing, which is
 * an(automatic)regarima modelling
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class RegArimaModellingSpec implements Validatable<RegArimaModellingSpec>, ProcSpecification {

    public static final String METHOD = "tramo";
    public static final String FAMILY = "Modelling";
    public static final String VERSION_LEGACY = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_LEGACY = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_LEGACY);

    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_V3 = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    public static final RegArimaModellingSpec DEFAULT =
            RegArimaModellingSpec.builder().build();

    /**
     * Gets the predefined Arima specification. The AutoModel and the Arima
     * properties are mutually exclusive specifications: Related TRAMO options:
     * P, D, Q, BP, BD, BQ, IMEAN, JPR, JPS, JQR, JQS, INIT.
     *
     * @return the Arima specifications
     */
    @lombok.NonNull private SarimaSpec arima;

    /**
     * Gets the specifications related to the transformation of the original
     * series. Related TRAMO options: LAM, FCT, UNITS. -- SETTER -- Sets the
     * specifications related to the transformation of the original series.
     *
     * @return The transform specifications
     * @param transform The new transform specifications. Should not be null
     */
    @lombok.NonNull private TransformSpec transform;

    /**
     * Gets the specifications related to the automatic ARIMA modelling (AMI).
     * The AutoModelSpec and the ArimaSpec are mutually exclusive
     * specifications: Related TRAMO options: IDIF, INIC, UB1, UB2, CANCEL,
     * TSIG, PR, PCR.
     *
     * @return The AMI specifications
     */
    @lombok.NonNull private AutoModelSpec autoModel;

    /**
     * Gets options related to the estimation routine Related TRAMO options:
     * TOL, TYPE. -- SETTER -- Sets new options for the estimation routine.
     *
     * @param estimate The new options
     * @return
     */
    @lombok.NonNull private EstimateSpec estimate;

    /**
     * Gets the options for the automatic outliers detection Related TRAMO
     * options: IATIP, AIO, TC, VA -- SETTER -- Sets the options for the
     * automatic outliers detection.
     *
     * @return the options for automatic outliers detection.
     * @param outliers The new specifications.
     */
    @lombok.NonNull private OutlierSpec outliers;

    /**
     * Gets the specifications for the regression model (including calendar
     * effects). -- SETTER -- Sets the specifications for the regression model.
     *
     * @param regression The new regression specifications.
     * @return The specifications for the regression model.
     */
    @lombok.NonNull private RegressionSpec regression;

    /**
     * Creates a new default specification builder. No transformation, no
     * regression variables, no outliers detection, default airline model
     * (without mean).
     *
     * @return the builder initialized with default parameters
     */
    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .transform(TransformSpec.DEFAULT)
                .estimate(EstimateSpec.DEFAULT)
                .autoModel(AutoModelSpec.DEFAULT_DISABLED)
                .outliers(OutlierSpec.DEFAULT_DISABLED)
                .arima(SarimaSpec.airline())
                .regression(RegressionSpec.DEFAULT);
    }

    @Override
    public RegArimaModellingSpec validate() throws IllegalArgumentException {
        autoModel.validate();
        outliers.validate();
        regression.validate();
        return this;
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR_V3;
    }

    /**
     * Checks that the AMI is enabled
     *
     * @return true if the AMI spec is enabled, false otherwise.
     */
    public boolean isUsingAutoModel() {
        return autoModel.isEnabled();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<RegArimaModellingSpec> {

        /**
         * Enables/disables the AMI.
         *
         * @param enableAutoModel
         * @return
         */
        public Builder usingAutoModel(boolean enableAutoModel) {
            if (autoModel.isEnabled() != enableAutoModel) {
                if (enableAutoModel) {
                    autoModel = AutoModelSpec.DEFAULT_ENABLED;
                } else {
                    autoModel = AutoModelSpec.DEFAULT_DISABLED;
                }
            }
            return this;
        }

        /**
         * Sets a predefined Sarima specification. The AMI is disabled.
         *
         * @param sarima new Sarima spec
         * @return
         */
        public Builder arima(@NonNull SarimaSpec sarima) {
            this.arima = sarima;
            this.autoModel = AutoModelSpec.DEFAULT_DISABLED;

            return this;
        }
    }

    public boolean checkFrequency(int freq) {
        return regression.getInterventionVariables().isEmpty()
                && regression.getOutliers().isEmpty()
                && regression.getRamps().isEmpty()
                && regression.getRamps().isEmpty();
    }

    public RegArimaModellingSpec setFrequency(int freq) {
        if (freq <= 0) {
            // Nothing to do
            return this;
        }
        Builder builder = toBuilder();
        // Remove pre-specified variables (keeping them would mean a lot of checks/conversions/hypotheses...)
        // We should perhaps change the calendar effects
        builder.regression(regression.toBuilder()
                .clearOutliers()
                .clearInterventionVariables()
                .clearRamps()
                .clearUserDefinedVariables()
                .build());
        return builder.buildWithoutValidation();
    }

    // <editor-fold defaultstate="collapsed" desc="Default Specifications">
    public static final RegArimaModellingSpec M0, M1, M2, M3, M4, M5, Mfull;

    public static final RegArimaModellingSpec[] allSpecifications() {
        return new RegArimaModellingSpec[] {M0, M1, M2, M3, M4, M5, Mfull};
    }

    static {
        M0 = RegArimaModellingSpec.DEFAULT;

        EasterSpec e =
                EasterSpec.builder().type(EasterSpec.Type.EASTER).test(true).build();

        TradingDaysSpec wd = TradingDaysSpec.td(TradingDaysType.TD2, LengthOfPeriodType.LeapYear, true, false);

        TradingDaysSpec td = TradingDaysSpec.td(TradingDaysType.TD7, LengthOfPeriodType.LeapYear, true, false);

        TradingDaysSpec dc = TradingDaysSpec.automatic(
                LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.BIC, TradingDaysSpec.DEF_PTD, false);

        EasterSpec ec = e.toBuilder().type(EasterSpec.Type.EASTER).test(true).build();

        CalendarSpec cwd = CalendarSpec.builder().easter(e).tradingDays(wd).build();
        CalendarSpec ctd = CalendarSpec.builder().easter(e).tradingDays(td).build();

        OutlierSpec o = OutlierSpec.builder().ao(true).tc(true).ls(true).build();

        M1 = RegArimaModellingSpec.builder()
                .transform(TransformSpec.DEF_AUTO)
                .outliers(o)
                .build();

        M2 = RegArimaModellingSpec.builder()
                .transform(TransformSpec.DEF_AUTO)
                .outliers(o)
                .regression(RegressionSpec.builder()
                        .mean(MeanSpec.DEFAULT_USED)
                        .calendar(cwd)
                        .build())
                .build();

        M3 = RegArimaModellingSpec.builder()
                .transform(TransformSpec.DEF_AUTO)
                .outliers(o)
                .usingAutoModel(true)
                .build();

        M4 = RegArimaModellingSpec.builder()
                .transform(TransformSpec.DEF_AUTO)
                .outliers(o)
                .regression(RegressionSpec.builder().calendar(cwd).build())
                .usingAutoModel(true)
                .build();

        M5 = RegArimaModellingSpec.builder()
                .transform(TransformSpec.DEF_AUTO)
                .outliers(o)
                .regression(RegressionSpec.builder().calendar(ctd).build())
                .usingAutoModel(true)
                .build();

        CalendarSpec cc = CalendarSpec.builder().easter(ec).tradingDays(dc).build();
        RegressionSpec rc = RegressionSpec.builder().calendar(cc).build();
        Mfull = RegArimaModellingSpec.builder()
                .transform(TransformSpec.DEF_AUTO)
                .outliers(o)
                .regression(rc)
                .usingAutoModel(true)
                .build();
    }

    public static RegArimaModellingSpec fromString(String name) {
        switch (name) {
            case "TR0", "tr0" -> {
                return M0;
            }
            case "TR1", "tr1" -> {
                return M1;
            }
            case "TR2", "tr2" -> {
                return M2;
            }
            case "TR3", "tr3" -> {
                return M3;
            }
            case "TR4", "tr4" -> {
                return M4;
            }
            case "TR5", "tr5" -> {
                return M5;
            }
            case "TRfull", "trfull" -> {
                return Mfull;
            }
            default -> throw new DemetraException();
        }
    }
    // </editor-fold>

    @Override
    public String display() {
        if (this == M0) {
            return "M0";
        }
        if (this == M1) {
            return "M1";
        }
        if (this == M2) {
            return "M2";
        }
        if (this == M3) {
            return "M3";
        }
        if (this == M4) {
            return "M4";
        }
        if (this == M5) {
            return "M5";
        }
        if (this == Mfull) {
            return "Mfull";
        }
        if (equals(M0)) {
            return "TR0";
        }
        if (equals(M1)) {
            return "TR1";
        }
        if (equals(M2)) {
            return "TR2";
        }
        if (equals(M3)) {
            return "TR3";
        }
        if (equals(M4)) {
            return "TR4";
        }
        if (equals(M5)) {
            return "TR5";
        }
        if (equals(Mfull)) {
            return "TRfull";
        }
        return SMETHOD;
    }

    private static final String SMETHOD = "TR";
}
