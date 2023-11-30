/*
 * Copyright 2016 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stl.base.core;

import jdplus.stl.base.api.StlSpec;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.stl.base.api.LoessSpec;
import jdplus.stl.base.api.SeasonalSpec;
import jdplus.toolkit.base.core.data.DataBlock;
import java.util.Random;
import jdplus.toolkit.base.api.data.DoubleSeq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 *
 * @author Jean Palate
 */
public class StlKernelTest {

    public StlKernelTest() {
    }

    @Test
//    @Ignore
    public void testDefault() {
        StlSpec spec = StlSpec.builder()
                .multiplicative(false)
                .seasonalSpec(
                        SeasonalSpec.builder()
                                .period(12)
                                .seasonalSpec(
                                        LoessSpec.builder()
                                                .window(13)
                                                .degree(0)
                                                .jump(0)
                                                .build())
                                .lowPassSpec(
                                        LoessSpec.builder()
                                                .window(13)
                                                .degree(1)
                                                .jump(0)
                                                .build())
                                .build())
                .trendSpec(LoessSpec.builder()
                        .window(23)
                        .degree(1)
                        .jump(0)
                        .build())
                .innerLoopsCount(2)
                .outerLoopsCount(1)
                .build();
        RawStlKernel stl = new RawStlKernel(spec);
        double[] data = Data.ABS_RETAIL;
        
        RawStlResults rslt = stl.process(DoubleSeq.of(data, data.length-120, 120));
        System.out.println(rslt.getTrend());
        System.out.println(rslt.getSeasonal());
        System.out.println(rslt.getFit());
    }

    @Test
//    @Ignore
    public void testLargeFilter() {
        StlSpec spec = StlSpec.builder()
                .seasonalSpec(new SeasonalSpec(12, 7, false))
                .trendSpec(LoessSpec.of(21, 1, false))
                .build();
        RawStlKernel stl = new RawStlKernel(spec);
        RawStlResults rslt = stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(rslt.getTrend());
//        System.out.println(rslt.getSeasonal());
//        System.out.println(rslt.getIrregular());
    }

    @Test
    //@Ignore
    public void testSpec() {

        StlSpec spec = StlSpec
                .createDefault(12, false, false);
        RawStlKernel stl = new RawStlKernel(spec);
        RawStlResults rslt = stl.process(Doubles.of(Data.EXPORTS));
//        System.out.println(rslt.getSeries());
//        System.out.println(rslt.getTrend());
//        System.out.println(rslt.getSeasonal());
//        System.out.println(rslt.getIrregular());
    }

    @Test
//    @Ignore
    public void testMul() {

        StlSpec spec = StlSpec.createDefault(12, true, true);
//        spec.setNumberOfOuterIterations(5);
        RawStlKernel stl = new RawStlKernel(spec);
        DoubleSeq s=DoubleSeq.of(Data.EXPORTS);
        RawStlResults rslt = stl.process(s);
//        System.out.println(rslt.getTrend());
//        System.out.println(rslt.getSeasonal());
//        System.out.println(rslt.getIrregular());
    }

    @Test
//    @Ignore
    public void testMissing() {

        StlSpec spec = StlSpec.createDefault(12, false, false);
//        spec.setMultiplicative(true);
//        spec.setNumberOfOuterIterations(5);
        RawStlKernel stl = new RawStlKernel(spec);
        DataBlock s = DataBlock.copyOf(Data.EXPORTS);
        Random rnd = new Random();
        for (int i = 0; i < 30; ++i) {
            s.set(rnd.nextInt(s.length()), Double.NaN);
        }
        RawStlResults rslt = stl.process(s);
//        System.out.println(rslt.getTrend());
//        System.out.println(rslt.getSeasonal());
//        System.out.println(rslt.getIrregular());
      }

    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
//            StlPlusKernel stl = new StlPlusKernel(12, 7);
            StlSpec spec = StlSpec.createDefault(12, 7, false, false);
//            spec.setNumberOfOuterIterations(5);
            RawStlKernel stl = new RawStlKernel(spec);
            stl.process(Doubles.of(Data.EXPORTS));
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
