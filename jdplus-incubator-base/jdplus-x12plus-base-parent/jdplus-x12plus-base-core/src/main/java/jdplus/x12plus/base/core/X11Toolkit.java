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
package jdplus.x12plus.base.core;

import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.x12plus.base.api.MX11plusSpec;
import jdplus.x12plus.base.api.PeriodSpec;
import jdplus.x12plus.base.api.X11plusSpec;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class X11Toolkit {

    public X11plusResults process(TsData data, X11plusSpec spec) {
        RawX11Kernel x11 = new RawX11Kernel(spec);
        RawX11Results decomp = x11.process(data.getValues());

        TsPeriod start = data.getStart();

        return X11plusResults.builder()
                .mode(decomp.getMode())
                .b1(TsData.of(start, decomp.getB1()))
                .b2(TsData.of(start, decomp.getB2()))
                .b3(TsData.of(start, decomp.getB3()))
                .b4(TsData.of(start, decomp.getB4()))
                .b5(TsData.of(start, decomp.getB5()))
                .b6(TsData.of(start, decomp.getB6()))
                .b7(TsData.of(start, decomp.getB7()))
                .b8(TsData.of(start, decomp.getB8()))
                .b9(TsData.of(start, decomp.getB9()))
                .b10(TsData.of(start, decomp.getB10()))
                .b11(TsData.of(start, decomp.getB11()))
                .b13(TsData.of(start, decomp.getB13()))
                .b17(TsData.of(start, decomp.getB17()))
                .b20(TsData.of(start, decomp.getB20()))
                .c1(TsData.of(start, decomp.getC1()))
                .c2(TsData.of(start, decomp.getC2()))
                .c4(TsData.of(start, decomp.getC4()))
                .c5(TsData.of(start, decomp.getC5()))
                .c6(TsData.of(start, decomp.getC6()))
                .c7(TsData.of(start, decomp.getC7()))
                .c9(TsData.of(start, decomp.getC9()))
                .c10(TsData.of(start, decomp.getC10()))
                .c11(TsData.of(start, decomp.getC11()))
                .c13(TsData.of(start, decomp.getC13()))
                .c17(TsData.of(start, decomp.getC17()))
                .c20(TsData.of(start, decomp.getC20()))
                .d1(TsData.of(start, decomp.getD1()))
                .d2(TsData.of(start, decomp.getD2()))
                .d4(TsData.of(start, decomp.getD4()))
                .d5(TsData.of(start, decomp.getD5()))
                .d6(TsData.of(start, decomp.getD6()))
                .d7(TsData.of(start, decomp.getD7()))
                .d8(TsData.of(start, decomp.getD8()))
                .d10(TsData.of(start, decomp.getD10()))
                .d11(TsData.of(start, decomp.getD11()))
                .d12(TsData.of(start, decomp.getD12()))
                .d13(TsData.of(start, decomp.getD13()))
                .build();
    }

    public MX11Results process(TsData data, MX11plusSpec spec) {
        // We should add pre-processing
        MX11Results.Builder builder = MX11Results.builder()
                .mode(spec.getMode());
        TsData b1 = data;
        for (PeriodSpec pspec : spec.getPeriodSpecs()) {
            RawX11Kernel x11 = new RawX11Kernel(spec.step(pspec.getPeriod()));
            RawX11Results decomp = x11.process(b1.getValues());

            TsPeriod start = b1.getStart();

            MX11Results.Step step = MX11Results.Step.builder()
                    .b1(b1)
                    .d10(TsData.of(start, decomp.getD10()))
                    .d11(TsData.of(start, decomp.getD11()))
                    .d12(TsData.of(start, decomp.getD12()))
                    .d13(TsData.of(start, decomp.getD13()))
                    .build();
            builder.step(step);
            b1=step.getD11();
        }

        return builder.build();
    }

}
