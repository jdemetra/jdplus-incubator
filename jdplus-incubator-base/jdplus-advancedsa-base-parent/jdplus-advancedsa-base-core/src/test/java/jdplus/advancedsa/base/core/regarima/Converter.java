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
package jdplus.advancedsa.base.core.regarima;

import jdplus.toolkit.base.api.timeseries.TsData;

/**
 *
 * @author palatej
 */
class Converter {
    static ec.tstoolkit.timeseries.simplets.TsData convert(TsData s) {
        int period = s.getAnnualFrequency();
        int year = s.getStart().year(), pos = s.getStart().annualPosition();
        return new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(period),
                year, pos, s.getValues().toArray(), false);
    }
    
}
