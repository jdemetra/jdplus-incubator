/*
 * Copyright 2024 JDemetra+.
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
package jdplus.sts.base.core.splines;

import jdplus.sts.base.core.splines.RegularSpline;
import jdplus.sts.base.core.splines.SplineData;
import jdplus.sts.base.core.splines.SplineData.CycleInformation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class SplineDataTest {

    public SplineDataTest() {
    }

    @Test
    public void testRegularInt() {
        RegularSpline rs = RegularSpline.of(25);
        SplineData data = new SplineData(rs);
        int n = 0;
        for (int i = 0; i < 10; ++i) {
            CycleInformation info = data.informationForCycle(i);
            n += info.Z.getRowsCount();
        }
        assertTrue(n == 250);
    }

    @Test
    public void testRegularDouble() {
        RegularSpline rs = RegularSpline.of(24.6);
        SplineData data = new SplineData(rs);
        int n = 0;
        for (int i = 0; i < 10; ++i) {
            CycleInformation info = data.informationForCycle(i);
            n += info.Z.getRowsCount();
        }
        assertTrue(n == 246);
    }

}
