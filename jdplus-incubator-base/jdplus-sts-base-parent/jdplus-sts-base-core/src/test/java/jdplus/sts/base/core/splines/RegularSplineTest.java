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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class RegularSplineTest {

    public RegularSplineTest() {
    }

    @Test
    public void testInt() {
        RegularSpline rs = RegularSpline.of(25);
        int n = 0;
        for (int i = 0; i < 10000; ++i) {
            IntSeq observations = rs.observations(i);
            int nc = observations.length();
            for (int j = 0; j < nc; ++j) {
                assertTrue(rs.cycleFor(observations.pos(j)) == i);
            }
            n += nc;
        }
        assertTrue(n == 250000);
    }

    @Test
    public void testDouble() {
        RegularSpline rs = RegularSpline.of(24.6);
        int n = 0;
        for (int i = 0; i < 10000; ++i) {
            IntSeq observations = rs.observations(i);
            int nc = observations.length();
            for (int j = 0; j < nc; ++j) {
                assertTrue(rs.cycleFor(observations.pos(j)) == i);
            }
//            System.out.println(nc);
            n += nc;
        }
        assertTrue(n == 246000);
    }

    @Test
    public void testDouble2() {
        RegularSpline rs = RegularSpline.of(24.25);
        int n = 0;
        for (int i = 0; i < 12000; ++i) {
            IntSeq observations = rs.observations(i);
            int nc = observations.length();
            for (int j = 0; j < nc; ++j) {
                assertTrue(rs.cycleFor(observations.pos(j)) == i);
            }
//            System.out.println(nc);
            n += nc;
        }
        assertTrue(n == 291000);
    }
    
    @Test
    public void testNegative() {
        RegularSpline rs = RegularSpline.of(24.6);
        int n = 0;
        for (int i = 1; i < 10001; ++i) {
            IntSeq observations = rs.observations(-i);
            int nc = observations.length();
            for (int j = 0; j < nc; ++j) {
                assertTrue(rs.cycleFor(observations.pos(j)) == -i);
            }
//            System.out.println(nc);
            n += nc;
        }
        assertTrue(n == 246000);
        rs = RegularSpline.of(24);
        n = 0;
        for (int i = 1; i < 10001; ++i) {
            IntSeq observations = rs.observations(-i);
            int nc = observations.length();
            for (int j = 0; j < nc; ++j) {
                assertTrue(rs.cycleFor(observations.pos(j)) == -i);
            }
//            System.out.println(nc);
            n += nc;
        }
        assertTrue(n == 240000);
        rs = RegularSpline.of(24.25);
        n = 0;
        for (int i = 0; i < 12000; ++i) {
            IntSeq observations = rs.observations(-i);
            int nc = observations.length();
            for (int j = 0; j < nc; ++j) {
                assertTrue(rs.cycleFor(observations.pos(j)) == -i);
            }
//            System.out.println(nc);
            n += nc;
        }
        assertTrue(n == 291000);
    }
}
