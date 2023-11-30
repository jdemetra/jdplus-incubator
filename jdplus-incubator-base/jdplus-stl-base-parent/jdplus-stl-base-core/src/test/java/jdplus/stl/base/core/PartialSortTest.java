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
package jdplus.stl.base.core;

import java.util.Arrays;
import java.util.Random;
import jdplus.toolkit.base.api.data.DoubleSeq;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class PartialSortTest {

    public PartialSortTest() {
    }

    @Test
    public void testMedianOdd() {
        int N = 39, M = N / 2;

        double[] z = new double[N];
        Random rnd = new Random(0);
        for (int i = 0; i < z.length; ++i) {
            z[i] = rnd.nextDouble();
        }
        int[] idx = new int[]{M};
        double[] w = z.clone();
        new PartialSort().psort(z, idx);
        double m = z[M];
        Arrays.sort(w);
        assertEquals(m, w[M], 0);
    }

    @Test
    public void testMedianEven() {
        int N = 40, M2 = N / 2, M1 = M2 - 5;
        int s=0;
        for (int k = 0; k < 1; ++k) {
            double[] z = new double[N];
            Random rnd = new Random(18);
            for (int i = 0; i < z.length; ++i) {
                z[i] = rnd.nextDouble();
            }
            int[] idx = new int[]{M1, M2};
            double[] w = z.clone();
            new PartialSort().psort(z, idx);
            System.out.println(DoubleSeq.of(z));
            double m = z[M1] + z[M2];
            Arrays.sort(w);
            System.out.println(DoubleSeq.of(w));
            double m2 = w[M1] + w[M2];
            if (m == m2)
                ++s;
            //assertEquals(m, m2, 0);
        }
        System.out.println(s);
    }
}
