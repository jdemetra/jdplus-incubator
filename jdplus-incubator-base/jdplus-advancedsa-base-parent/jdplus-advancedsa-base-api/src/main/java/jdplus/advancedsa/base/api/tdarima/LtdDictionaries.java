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

import jdplus.toolkit.base.api.dictionaries.AtomicDictionary;
import jdplus.toolkit.base.api.dictionaries.ComplexDictionary;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.PrefixedDictionary;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
public class LtdDictionaries {
    
    public static final String PARAMETERS_FIXED="pfixed", PARAMETERS_ALL = "pall", PARAMETERS_MEAN="pmean", PARAMETERS_DELTA="pdelta", 
            PARAMETERS_P0="p0", PARAMETERS_P1="p1", 
            PARAMETERS_V0="v0", PARAMETERS_V1="v1",
            PARAMETERS_FIXED_COV="pfixed_cov", PARAMETERS_COV="pall_cov", PARAMETERS_NAMES="pnames",
            PARAMETERS_DERIVED="pderived", PARAMETERS_DERIVED_COV="pderived_cov", PARAMETERS_DERIVED_NAMES="pderived_names",
            TEST_STATIONARITY="test_stationarity";
    
    public static final String REGS_COV0="cov0", REGS_COV1="cov1", REGS_C0="c0", REGS_C1="c1", 
            REGS_EFFECT0="effect0", Y_LIN0="y_lin0",
            REGS_EFFECT1="effect1", Y_LIN1="y_lin1";
    
    public static final String MODEL="model", RESIDUALS="residuals", REGRESSION="regression";
    
    public final Dictionary LTDARIMA_MODEL = AtomicDictionary.builder()
            .name("ltdarima")
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_FIXED).description("arima parameters of the fixed model").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_ALL).description("parameters of the time-dependent model").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_NAMES).description("names of the parameters of the time-dependent model").outputClass(String.class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_COV).description("covariance of the parameters of the time-dependent model").outputClass(Matrix.class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_DERIVED).description("derived parameters of the time-dependent model").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_DERIVED_NAMES).description("names of the derived parameters of the time-dependent model").outputClass(String.class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_DERIVED_COV).description("covariance of the derived parameters of the time-dependent model").outputClass(Matrix.class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_P0).description("arima parameters at the beginning").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_P1).description("arima parameters at the end").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_MEAN).description("mean of the arima parameters").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_DELTA).description("delta of the arima parameters").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_V0).description("initial variance of the innovation").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(PARAMETERS_V1).description("final variance of the innovation").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(TEST_STATIONARITY).description("test of the stationarity of the time-dependent parameters").outputClass(StatisticalTest.class).build())
           .build();
    
    public final Dictionary LTDARIMA_REG = AtomicDictionary.builder()
            .name("ltdarima_regresion")
            .item(AtomicDictionary.Item.builder().name(REGS_C0).description("coefficients of the fixed model").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(REGS_C1).description("coefficients of the time dependent model").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(REGS_COV0).description("covariance of the coefficients of the fixed model").outputClass(Matrix.class).build())
            .item(AtomicDictionary.Item.builder().name(REGS_COV1).description("covariance of the coefficients of the time dependent model").outputClass(Matrix.class).build())
            .item(AtomicDictionary.Item.builder().name(REGS_EFFECT0).description("regression effect in the fixed model").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(REGS_EFFECT1).description("regression effect in the tme dependent model").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(Y_LIN0).description("linearized series of the fixed model").outputClass(double[].class).build())
            .item(AtomicDictionary.Item.builder().name(Y_LIN1).description("linearized series of the tme dependent model").outputClass(double[].class).build())
            .build();
    
       public final Dictionary LTDARIMA = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(MODEL, LTDARIMA_MODEL))
            .dictionary(new PrefixedDictionary(REGRESSION, LTDARIMA_REG))
            .dictionary(new PrefixedDictionary(RESIDUALS, ResidualsDictionaries.RESIDUALS_DEFAULT))
            .build();

    
}
