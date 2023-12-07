/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sts.base.core;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.sts.base.api.RawBsmDecomposition;
import jdplus.sts.base.api.BsmSpec;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.toolkit.base.core.modelling.Residuals;
import jdplus.toolkit.base.core.modelling.regression.RegressionDesc;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class LightBasicStructuralModel implements GenericExplorable, BasicStructuralModel {

    Description description;
    Estimation estimation;
    RawBsmDecomposition bsmDecomposition;
    Residuals residuals;

    @lombok.Singular
    List<RegressionDesc> regressionItems;
    
    @lombok.Singular
    private Map<String, StatisticalTest> diagnostics;

    @lombok.Singular
    private Map<String, Object> additionalResults;

    @lombok.Value
    @lombok.Builder
    public static class Description implements GeneralLinearModel.Description<BsmSpec> {

        /**
         * Original series
         */
        TsData series;
        /**
         * Log transformation
         */
        boolean logTransformation;

        /**
         * Transformation for leap year or length of period
         */
        LengthOfPeriodType lengthOfPeriodTransformation;

        /**
         * Regression variables (including mean correction)
         */
        Variable[] variables;

        /**
         * For instance SarimaSpec
         */
        BsmSpec specification;

        @Override
        public BsmSpec getStochasticComponent() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }

    @lombok.Value
    @lombok.Builder
    public static class Estimation implements GeneralLinearModel.Estimation {

//        @lombok.NonNull
        private DoubleSeq y;
//        @lombok.NonNull
        private Matrix X;
        
        private TsDomain domain;

        /**
         * Regression estimation. The order correspond to the order of the
         * variables
         * Fixed coefficients are not included
         */
//        @lombok.NonNull
        private DoubleSeq coefficients;
//        @lombok.NonNull
        private Matrix coefficientsCovariance;

//        @lombok.NonNull
        private MissingValueEstimation[] missing;
        /**
         * Parameters of the stochastic component. Fixed parameters are not
         * included
         */
//        @lombok.NonNull
        private ParametersEstimation parameters;

//        @lombok.NonNull
        private LikelihoodStatistics statistics;

//        @lombok.NonNull
        private DoubleSeq residuals;

        @lombok.Singular
        private List<ProcessingLog.Information> logs;

    }

}
