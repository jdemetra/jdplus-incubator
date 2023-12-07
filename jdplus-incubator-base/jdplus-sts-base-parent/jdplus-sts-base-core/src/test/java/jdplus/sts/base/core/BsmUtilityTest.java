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

import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class BsmUtilityTest {
    
    public BsmUtilityTest() {
    }
    
      @Test
    public void testUcm() {
        BsmData bsm=BsmData.builder(12)
                .levelVar(.9)
                .slopeVar(.5)
                .cycleVar(2)
                .seasonalModel(SeasonalModel.HarrisonStevens)
                .seasonalVar(0.1)
                .noiseVar(1)
                .build();
        UcarimaModel ucm=BsmUtility.ucm(bsm, true);
        assertTrue(ucm != null);
    }
  
}
