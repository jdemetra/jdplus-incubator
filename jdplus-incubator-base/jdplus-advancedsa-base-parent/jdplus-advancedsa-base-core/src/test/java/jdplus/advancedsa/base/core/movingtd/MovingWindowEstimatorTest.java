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
package jdplus.advancedsa.base.core.movingtd;

import jdplus.advancedsa.base.api.movingtd.MovingWindowSpec;
import jdplus.advancedsa.base.core.movingtd.MovingWindowEstimator;
import jdplus.advancedsa.base.core.regarima.FastKernel;
import jdplus.toolkit.base.api.modelling.regular.CalendarSpec;
import jdplus.toolkit.base.api.modelling.regular.EasterSpec;
import jdplus.toolkit.base.api.modelling.regular.MeanSpec;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.modelling.regular.RegressionSpec;
import jdplus.toolkit.base.api.modelling.regular.TradingDaysSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author palatej
 */
public class MovingWindowEstimatorTest {
    
    public MovingWindowEstimatorTest() {
    }

    @Test
    public void testProd() {
        TsData s = Data.TS_ABS_RETAIL;
        ModellingSpec spec = ModellingSpec.FULL;
        TradingDaysSpec tradingDays = TradingDaysSpec
                .td(TradingDaysType.TD7, LengthOfPeriodType.LeapYear, true, true);
        //.automatic(LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.BIC, 0.01, true);

        CalendarSpec cspec = CalendarSpec.builder()
                .easter(EasterSpec.DEFAULT_USED)
                .tradingDays(tradingDays)
                .build();
        RegressionSpec rspec = RegressionSpec.builder()
                .mean(MeanSpec.DEFAULT_USED)
                .calendar(cspec)
                .build();

        spec = spec.toBuilder().regression(rspec)
                .build();
        FastKernel kernel = FastKernel.of(spec, null);
        RegSarimaModel rslt = kernel.process(s, null);
        MovingWindowSpec mwSpec = MovingWindowSpec.DEF_SPEC;
        MovingWindowEstimator mwe = new MovingWindowEstimator(mwSpec);
        MovingWindowCorrection q = mwe.process(rslt, 0, 0);
//        System.out.println(q.getTdCoefficients());
        
//        System.out.println(q.getTdEffect().getValues());
    }
}
