/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.api;

import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.Variable;

/**
 *
 * @author PALATEJ
 */
public interface BsmDescription {
       /**
         * Original series
         *
         * @return
         */
        TsData getSeries();

        /**
         * Log transformation
         *
         * @return
         */
        boolean isLogTransformation();

        /**
         * Transformation for leap year or length of period
         *
         * @return
         */
        LengthOfPeriodType getLengthOfPeriodTransformation();

        /**
         * Regression variables (including mean correction)
         *
         * @return
         */
        Variable[] getVariables();

        /**
         * For instance SarimaSpec
         *
         * @return
         */
        BsmSpec getSpecification();
        
    
}
