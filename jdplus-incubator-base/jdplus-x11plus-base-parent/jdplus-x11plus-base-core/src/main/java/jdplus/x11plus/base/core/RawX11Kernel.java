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
package jdplus.x11plus.base.core;

import java.util.Arrays;
import jdplus.filters.base.core.FiltersToolkit;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.x11plus.base.api.X11SeasonalFilterSpec;
import jdplus.x11plus.base.api.X11plusSpec;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class RawX11Kernel {
    
    private final X11plusSpec spec;

//    private X11AStep astep;
    private X11BStep bstep;
    private X11CStep cstep;
    private X11DStep dstep;
    private X11EStep estep;

    static double[] table(int n, double value) {
        double[] x = new double[n];
        Arrays.fill(x, value);
        return x;
    }
    
    public RawX11Kernel(X11plusSpec spec){
        this.spec=spec;
    }

    public RawX11Results process(DoubleSeq data) {
        clear();

        // build the context from the spec
        X11Context context = X11Context.of(spec);
        return process(data, context);
    }

    public RawX11Results process(DoubleSeq data, X11Context context) {
        clear();
        bstep = new X11BStep();
        bstep.process(data, context);
        cstep = new X11CStep();
        cstep.process(data, context.remove(data, bstep.getB20()), context);
        dstep = new X11DStep();
        dstep.process(data, context.remove(data, cstep.getC20()), context);

        return RawX11Results.builder()
                .b1(bstep.getB1())
                .b2(bstep.getB2())
                .b3(bstep.getB3())
                .b4(bstep.getB4())
                .b5(bstep.getB5())
                .b6(bstep.getB6())
                .b7(bstep.getB7())
                .b8(bstep.getB8())
                .b9(bstep.getB9())
                .b10(bstep.getB10())
                .b11(bstep.getB11())
                .b13(bstep.getB13())
                .b17(bstep.getB17())
                .b20(bstep.getB20())
                .c1(cstep.getC1())
                .c2(cstep.getC2())
                .c4(cstep.getC4())
                .c5(cstep.getC5())
                .c6(cstep.getC6())
                .c7(cstep.getC7())
                .c9(cstep.getC9())
                .c10(cstep.getC10())
                .c11(cstep.getC11())
                .c13(cstep.getC13())
                .c17(cstep.getC17())
                .c20(cstep.getC20())
                .d1(dstep.getD1())
                .d2(dstep.getD2())
                .d4(dstep.getD4())
                .d5(dstep.getD5())
                .d6(dstep.getD6())
                .d7(dstep.getD7())
                .d8(dstep.getD8())
                .d9(dstep.getD9())
                .d10(dstep.getD10())
                .d11(dstep.getD11())
                .d12(dstep.getD12())
                .d13(dstep.getD13())
                .build();
    }

    /**
     * @return the bstep
     */
    public X11BStep getBstep() {
        return bstep;
    }

    /**
     * @return the cstep
     */
    public X11CStep getCstep() {
        return cstep;
    }

    /**
     * @return the dstep
     */
    public X11DStep getDstep() {
        return dstep;
    }

    /**
     * @return the estep
     */
    public X11EStep getEstep() {
        return estep;
    }

    private void clear() {
        bstep = null;
        cstep = null;
        dstep = null;
        estep = null;
    }
}
