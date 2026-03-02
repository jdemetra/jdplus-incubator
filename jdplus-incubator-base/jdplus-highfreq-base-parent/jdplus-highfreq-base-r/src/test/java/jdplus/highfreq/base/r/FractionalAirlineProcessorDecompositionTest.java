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
package jdplus.highfreq.base.r;

import jdplus.highfreq.base.core.extendedairline.decomposition.LightExtendedAirlineDecomposition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 *
 * @author Christiane Hofer: Test for the R-Package
 */
public class FractionalAirlineProcessorDecompositionTest {

    @Test
    public void testDictionaryUcarima() {

        boolean sn = false; // sn=true decomposition into signal and noise (2 components only). The signal is the seasonally adjusted series and the noise the seasonal component.
        boolean cov = false;
        Integer fcast = 0;
        Integer bcast = 0;
        double[] OBS = {2.52, 1.43, 0.77, 3.19, 1.6, 0.35, 0.95, 1.69, 1.91, 1.46, 1.21, 2.48, 1.35, 0.6, 1.09, 1.73, 0.58, 2.56, 1.48, 0.36, 0.12, 1.6, 0.87, 1.31, 2.19, 1.46, 0.45, 2.43, 2.98, 11.93, 0.08, 2.42, 2.99, 0.44, 0.36, 3.83, 0.44, 1.19, 3.25, 2.65, 2.86, 1.18, 0.92, 2.06, 1.28, 2.6, 1.82, 0.53, 1.2, 0.76};

        LightExtendedAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(OBS, 7, sn, cov, fcast, bcast);
        // System.out.println(rslt.getDictionary());
        assertTrue(rslt.getDictionary().containsKey("ucarima.model.ar"), "Dictionary doesn't contain:ucarima.model.ar");
        assertTrue(rslt.getDictionary().containsKey("ucarima.model.ma"), "Dictionary doesn't contain:ucarima.model.ma");
        assertTrue(rslt.getDictionary().containsKey("ucarima.model.delta"), "Dictionary doesn't contain:ucarima.model.delta");
        assertTrue(rslt.getDictionary().containsKey("ucarima.model.var"), "Dictionary doesn't contain:ucarima.model.var");
        assertTrue(rslt.getDictionary().containsKey("ucarima.model.name"), "Dictionary doesn't contain:ucarima.model.name");

        double[] data_ar = rslt.getData("ucarima.model.ar", double[].class); // IARMIAModelExtractor is needed and integreatet in the package
        assertArrayEquals(rslt.getUcarima().getModel().getStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray(), rslt.getData("ucarima.model.ar", double[].class), "ucarima.model.ar");
        assertArrayEquals(rslt.getUcarima().getModel().getNonStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray(), rslt.getData("ucarima.model.delta", double[].class), "ucarima.model.delta");
        assertArrayEquals(rslt.getUcarima().getModel().getMa().asPolynomial().coefficients().drop(1, 0).toArray(), rslt.getData("ucarima.model.ma", double[].class), "ucarima.model.ma");
        assertEquals(rslt.getUcarima().getModel().getInnovationVariance(), rslt.getData("ucarima.model.var", Double.class), "ucarima.model.var");
        assertEquals(rslt.getUcarima().getModel().getClass().getName().replaceFirst(rslt.getUcarima().getModel().getClass().getPackageName() + ".", ""), rslt.getData("ucarima.model.name", String.class), "ucarima.model.name");
        rslt.getData("ucarima.model.name"); // Add in Arima Dictionary and in IARMIAModelExtractor to have the possibiilty to geht the name of the IarimaModel

//Try to get the models from all components 1 to last, the first contains the complete model
        Integer size = rslt.getData("ucarima.size", Integer.class); // Numner of Components with the first contains the aggreated common model
//jd3_fractionalairline.R iterates from 1 to ncmp=size;

        for (int i = 1; i < size + 1; i++) {
            assertEquals("ArimaModel", rslt.getData("ucarima.component(" + i + ").name", String.class), "ucarima.component(" + i + ").name");

            double[] ar_res = rslt.getData("ucarima.component(" + i + ").ar", double[].class);
            double[] ar_expected = rslt.getUcarima().getComponent(i - 1).getStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray();
            assertArrayEquals(ar_expected, ar_res, "ucarima.component(" + i + ").ar");

            double[] delta_res = rslt.getData("ucarima.component(" + i + ").delta", double[].class);
            double[] delta_expected = rslt.getUcarima().getComponent(i - 1).getNonStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray();
            assertArrayEquals(delta_expected, delta_res, "ucarima.component(" + i + ").delta");

            double[] ma_res = rslt.getData("ucarima.component(" + i + ").ma", double[].class);
            double[] ma_expected = rslt.getUcarima().getComponent(i - 1).getMa().asPolynomial().coefficients().drop(1, 0).toArray();
            assertArrayEquals(ma_expected, ma_res, "ucarima.component(" + i + ").ma");

            assertEquals(rslt.getUcarima().getComponent(i - 1).getInnovationVariance(), rslt.getData("ucarima.component(" + i + ").var", Double.class), "ucarima.component(" + i + ").var");

        }

    }

}