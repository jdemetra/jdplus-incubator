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

import jdplus.sts.base.core.splines.DailySpline;
import jdplus.sts.base.core.splines.IntSeq;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class DailySplineTest {

    public DailySplineTest() {
    }

    @Test
    public void testPositive() {
        DailySpline rs = new DailySpline(1963, new int[]{0, 100, 200, 300});
        int n = 0;
        for (int i = 0; i < 120; ++i) {
            IntSeq observations = rs.observations(i);
            int nc = observations.length();
            for (int j = 0; j < nc; ++j) {
                assertTrue(rs.cycleFor(n+j) == i);
            }
            n += nc;
        }
        assertTrue(n == 365 * 120 + 30);
    }

}
