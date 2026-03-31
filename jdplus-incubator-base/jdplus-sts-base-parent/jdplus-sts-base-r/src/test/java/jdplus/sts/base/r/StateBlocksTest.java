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

import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.ckms.CkmsFilter;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.PredictionErrorDecomposition;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class StateBlocksTest {

    public StateBlocksTest() {
    }

    @Test
    public void testArma() {
        StateComponent sarma1 = StateBlocks.sarma(4, new double[]{.2, -.3, .1}, new double[]{-.5}, null, new double[]{-.6});
        assertTrue(sarma1 != null);
        StateComponent sarma2 = StateBlocks.sarma2(4, new double[]{.2, -.3, .1}, new double[]{-.5}, null, new double[]{-.6});
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
    public void testArima() {
        StateComponent arima1 = StateBlocks.arima(new double[]{1, -.6}, new double[]{1, -2, 1}, new double[]{1, .2, -.3, .1}, 1);
        assertTrue(arima1 != null);
        StateComponent arima2 = StateBlocks.arima2(new double[]{1, -.6}, new double[]{1, -2, 1}, new double[]{1, .2, -.3, .1}, 1);
        assertTrue(arima2 != null);
//        System.out.println(StateBlocks.T(arima1, 0));
//        System.out.println();
//        System.out.println(StateBlocks.S(arima1, 0));
//        System.out.println();
//        System.out.println(StateBlocks.T(arima2, 0));
//        System.out.println();
//        System.out.println(StateBlocks.S(arima2, 0));
    }

    @Test
    public void testLL() {
        SarimaModel arima = SarimaModel.builder(4)
                .phi(new double[]{.2, -.3, .1})
                .theta(new double[]{-.5})
                .btheta(new double[]{-.6})
                .build();
        StateComponent sarma1 = StateBlocks.sarma(4, new double[]{.2, -.3, .1}, new double[]{-.5}, null, new double[]{-.6});
        ISsf ssf = StateSpaceModels.ssf(sarma1, Loading.fromPosition(0), 0);
        CkmsFilter filter = new CkmsFilter(SsfArima.fastInitializer(arima));
        SsfData data = new SsfData(Data.ABS_RETAIL);
        PredictionErrorDecomposition decomp = new PredictionErrorDecomposition(true);
        decomp.prepare(ssf, data.length());
        filter.process(ssf, data, decomp);
        System.out.println(decomp.likelihood(true));
    }

    @Test
    public void testLL2() {
        
        StateComponent ll = StateBlocks.localLinearTrend(0.1, 0);
        StateComponent seasonal = StateBlocks.seasonal("Trigonometric", 12, 0.1);
        StateComponent n = StateBlocks.noise(1);
        StateComponent composite = StateBlocks.composite(new StateComponent[]{ll, seasonal});
        ISsf ssf=StateSpaceModels.ssf(composite, Loading.fromPositions(new int[]{0,2}), 1);
        DiffuseLikelihood l = Algorithms.akfLikelihood(ssf, Data.ABS_RETAIL, "NORMAL", true, true);
        System.out.println(l);
    }
}
