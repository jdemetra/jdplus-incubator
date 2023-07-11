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
package jdplus.x11plus.base.core.x13;

import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.sa.base.api.StationaryVarianceDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.StationaryVarianceComputer;
import jdplus.sa.base.core.diagnostics.GenericSaTests;
import jdplus.x11plus.base.core.X11Results;

@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class X13plusDiagnostics {

    private StationaryVarianceDecomposition varianceDecomposition;
    private GenericSaTests genericDiagnostics;

//    public static X13plusDiagnostics of(RegSarimaModel preprocessing, X11Results srslts, SeriesDecomposition finals) {
//        boolean mul = finals.getMode().isMultiplicative();
//        TsData y = srslts.getB1();
//        TsData sa = srslts.getD11();
//        TsData i = srslts.getD13();
//        TsData s = srslts.getD10();
//        TsData si = srslts.getD8();
//        GenericSaTests gsadiags;
//        TsData lsa = mul ? sa.log() : sa;
//        TsData li = mul ? i.log() : i;
//        if (preprocessing != null) {
//            TsData lin = preprocessing.linearizedSeries();
//
//            gsadiags = GenericSaTests.builder()
//                    .mul(mul)
//                    .regarima(preprocessing)
//                    .lin(lin)
//                    .res(preprocessing.fullResiduals())
//                    .y(y)
//                    .sa(sa)
//                    .irr(i)
//                    .si(si)
//                    .lsa(lsa)
//                    .lirr(li)
//                    .build();
//        } else {
//            gsadiags = GenericSaTests.builder()
//                    .mul(mul)
//                    .regarima(preprocessing)
//                    .res(null)
//                    .lin(y)
//                    .y(y)
//                    .sa(sa)
//                    .irr(i)
//                    .si(si)
//                    .lsa(lsa)
//                    .lirr(li)
//                    .build();
//
//        }
//        return new X13plusDiagnostics(varDecomposition(preprocessing, srslts), gsadiags);
//    }
//
//    private static StationaryVarianceDecomposition varDecomposition(RegSarimaModel preprocessing, X11Results srslts) {
//        StationaryVarianceComputer var = new StationaryVarianceComputer(StationaryVarianceComputer.HP);
//        boolean mul = srslts.getMode().isMultiplicative();
//        if (preprocessing != null) {
//            TsData y = preprocessing.interpolatedSeries(false),
//                    t = srslts.getD12(),
//                    seas = srslts.getD10(),
//                    irr = srslts.getD13(),
//                    cal = preprocessing.getCalendarEffect(y.getDomain());
//
//            TsData others;
//            if (mul) {
//                TsData all = TsData.multiply(t, seas, irr, cal);
//                others = TsData.divide(y, all);
//            } else {
//                TsData all = TsData.add(t, seas, irr, cal);
//                others = TsData.subtract(y, all);
//            }
//            return var.build(y, t, seas, irr, cal, others, mul);
//        } else {
//            TsData y = srslts.getB1(),
//                    t = srslts.getD12(),
//                    seas = srslts.getD10(),
//                    irr = srslts.getD13(),
//                    cal = null,
//                    others = null;
//            return var.build(y, t, seas, irr, cal, others, mul);
//        }
//    }
}
