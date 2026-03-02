/*
 * Copyright 2026 JDemetra+.
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
package jdplus.sts.base.r;


import jdplus.toolkit.base.core.ssf.StateComponent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class StateBlocksTest {
    
    public StateBlocksTest() {
    }
    
    @Test
    public void testArma(){
        StateComponent sarma1 = StateBlocks.sarma(4, new double[]{.2, -.3, .1}, null, new double[]{-.5}, new double[]{-.6});
        assertTrue(sarma1 != null);
        StateComponent sarma2 = StateBlocks.sarma2(4, new double[]{.2, -.3, .1}, null, new double[]{-.5}, new double[]{-.6});
        assertTrue(sarma2 != null);
//        System.out.println(StateBlocks.T(sarma1, 0));
//        System.out.println();
//        System.out.println(StateBlocks.S(sarma1, 0));
//        System.out.println();
//        System.out.println(StateBlocks.T(sarma2, 0));
//        System.out.println();
//        System.out.println(StateBlocks.S(sarma2, 0));
    }
    
    @Test
    public void testArima(){
        StateComponent arima1 = StateBlocks.arima(new double[]{1, -.6}, new double[]{1, -2, 1}, new double[]{1, .2, -.3, .1}, 1);
        assertTrue(arima1 != null);
        StateComponent arima2 = StateBlocks.arima2(new double[]{1,-.6}, new double[]{1, -2, 1},new double[]{1,.2, -.3, .1}, 1);
        assertTrue(arima2 != null);
        System.out.println(StateBlocks.T(arima1, 0));
        System.out.println();
        System.out.println(StateBlocks.S(arima1, 0));
        System.out.println();
        System.out.println(StateBlocks.T(arima2, 0));
        System.out.println();
        System.out.println(StateBlocks.S(arima2, 0));
    }
}
