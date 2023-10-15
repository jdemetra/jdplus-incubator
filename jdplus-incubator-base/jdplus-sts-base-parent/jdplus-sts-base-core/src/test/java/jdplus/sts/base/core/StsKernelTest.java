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

import jdplus.sts.base.api.Component;
import jdplus.sts.base.api.StsSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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
        TsData s=Data.SP_IPI;
        StsKernel kernel=StsKernel.of(spec, null);
        StsResults rslts = kernel.process(s, null);   
//        System.out.println(rslts.getSts().getDecomposition().getSeries(Component.Series, false));
//        System.out.println(rslts.getSts().getDecomposition().getSeries(Component.Level, false));
//        System.out.println(rslts.getSts().getDecomposition().getSeries(Component.Seasonal, false));
//        System.out.println(rslts.getSts().getDecomposition().getSeries(Component.Noise, false));
//        System.out.println(rslts.getSts().getBsm());
    }

}
