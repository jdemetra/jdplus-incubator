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
package jdplus.advancedsa.base.core;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.x13.base.api.regarima.BasicSpec;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.x13.base.api.regarima.RegressionTestSpec;
import jdplus.advancedsa.base.api.PreprocessingSpec;
import jdplus.tramoseats.base.api.tramo.CalendarSpec;
import jdplus.tramoseats.base.api.tramo.EasterSpec;
import jdplus.tramoseats.base.api.tramo.OutlierSpec;
import jdplus.tramoseats.base.api.tramo.RegressionSpec;
import jdplus.tramoseats.base.api.tramo.RegressionTestType;
import jdplus.tramoseats.base.api.tramo.TradingDaysSpec;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.tramoseats.base.api.tramo.TransformSpec;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaProcessor;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Utility {
    public RegSarimaProcessor preprocessor(PreprocessingSpec spec){
        switch (spec.getMethod()){
            case TRAMO:
                // create the corresponding TramoSpec
                TramoSpec.builder()
                        .transform(TransformSpec
                                .builder()
                                .span(spec.getSpan())
                                .function(spec.getTransform())
                                .build())
                        .arima(SarimaSpec.airline())
                        .regression(RegressionSpec.builder()
                                .calendar(CalendarSpec.builder()
                                        .tradingDays(TradingDaysSpec
                                                .td(spec.getDtype(), spec.getLtype(), spec.pretest ? RegressionTestType.Joint_F : RegressionTestType.None, false))
                                        .easter(spec.easter ?
                                                EasterSpec.builder()
                                                        .type(EasterSpec.Type.IncludeEaster)
                                                        .test(spec.isPretest())
                                                        .build()
                                                :EasterSpec.DEFAULT_UNUSED)
                                        .build())
                                .build())
                        .outliers(OutlierSpec
                                .builder()
                                .ao(spec.isAo())
                                .ls(spec.isLs())
                                .tc(spec.isTc())
                                .so(spec.isSo())
                                .build())
                        .build();
                
            case REGARIMA:
                 RegArimaSpec.builder()
                        .basic(BasicSpec
                                .builder()
                                .span(spec.getSpan())
                                .build())
                        .transform(jdplus.x13.base.api.regarima.TransformSpec
                                .builder()
                                .function(spec.getTransform())
                                .build())
                        .arima(SarimaSpec.airline())
                        .regression(jdplus.x13.base.api.regarima.RegressionSpec.builder()
                                .tradingDays(jdplus.x13.base.api.regarima.TradingDaysSpec.
                                        td(spec.getDtype(), spec.getLtype(), spec.pretest ? RegressionTestSpec.Remove : RegressionTestSpec.None, true))
                                .easter(spec.easter ?
                                        jdplus.x13.base.api.regarima.EasterSpec.builder()
                                                .type(jdplus.x13.base.api.regarima.EasterSpec.Type.Easter)
                                                .test(spec.isPretest() ? RegressionTestSpec.Add : RegressionTestSpec.None)
                                                .build()
                                        : jdplus.x13.base.api.regarima.EasterSpec.DEFAULT_UNUSED)
                                .build())
                        .outliers(
                                jdplus.x13.base.api.regarima.OutlierSpec.of(spec.isAo(),
                                        spec.isLs(), spec.isTc(), spec.isSo())
                                .build())
                        .build();
                
                
            default:
                return null;
        }
    }
}
