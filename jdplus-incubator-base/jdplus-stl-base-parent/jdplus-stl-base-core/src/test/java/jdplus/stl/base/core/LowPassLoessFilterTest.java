/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
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

import jdplus.stl.base.api.LoessSpec;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.Doubles;
import org.junit.jupiter.api.Test;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class LowPassLoessFilterTest {

    public LowPassLoessFilterTest() {
    }

    @Test
    public void testSomeMethod() {
        DoubleSeq s = Doubles.of(Data.EXPORTS);
        double[] d = s.toArray();
        LoessSpec spec = LoessSpec.of(7, 0, false);
        SeasonalLoessFilter filter = new SeasonalLoessFilter(spec, 12);
        double[] sd = new double[d.length + 24];
        double[] l = new double[d.length];
        filter.filter(IDataGetter.of(d), null, IDataSelector.of(sd, -12));
        LoessSpec lspec = LoessSpec.of(13, 1, false);
        LowPassLoessFilter lfilter = new LowPassLoessFilter(lspec, 12);
        lfilter.filter(IDataGetter.of(sd), IDataSelector.of(l));
//        System.out.println(s);
//        System.out.println(DoubleSeq.of(sd));
//        System.out.println(DoubleSeq.of(l));
    }

}
