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
package jdplus.sts.base.core;

import java.util.Arrays;
import jdplus.sa.base.api.ComponentType;
import jdplus.sts.base.api.StsSpec;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author palatej
 */
public class StsKernelTest {

    public StsKernelTest() {
    }

    @Test
    public void testProd() {
        StsSpec spec = StsSpec.FULL;
        TsData s=Data.TS_PROD;
        StsKernel kernel=StsKernel.of(spec, null);
        StsResults rslts = kernel.process(s, null);   
        TsDataTable table=TsDataTable.of(Arrays.asList(
                rslts.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value),
                rslts.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value),
                rslts.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value),
                rslts.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value),
                rslts.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value)
        ));
//        System.out.println(table);
//        System.out.println(rslts.getSts().getBsm());

    }

}
