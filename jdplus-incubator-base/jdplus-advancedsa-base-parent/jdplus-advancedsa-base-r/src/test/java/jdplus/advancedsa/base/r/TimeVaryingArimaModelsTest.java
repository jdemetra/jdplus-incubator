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
package jdplus.advancedsa.base.r;

import jdplus.advancedsa.base.core.tdarima.LtdArimaResults;
import jdplus.toolkit.base.api.timeseries.TsData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingArimaModelsTest {

    public TimeVaryingArimaModelsTest() {
    }

    @Test
    public void testAirline() {
        TsData[] s = Data.retail_us();
        int[] regular = new int[]{0, 1, 1};
        int[] seasonal = new int[]{0, 1, 1};
        for (int i = 0; i < s.length; ++i) {
           
            LtdArimaResults result = TimeVaryingArimaModels.estimate(s[i].log().getValues().toArray(), s[i].getAnnualFrequency(), true, null, regular, seasonal, true, true, false, false, false, 1e-7, "mean_delta");
            assertTrue(result != null);
            System.out.print(result.getData("ll0.ll", Double.class));
            System.out.print('\t');
            System.out.print(result.getData("ll1.ll", Double.class));
            System.out.print('\t');
            System.out.println(result.getLtd().getModel().getVar1());
        }
    }

}
