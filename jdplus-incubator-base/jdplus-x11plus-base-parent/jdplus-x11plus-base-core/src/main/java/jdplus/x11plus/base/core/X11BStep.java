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

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearfilters.IFiltering;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11BStep {

    /**
     * @return the b4a
     */
    public DoubleSeq getB4a() {
        return b4a;
    }

    /**
     * @return the b4anorm
     */
    public DoubleSeq getB4anorm() {
        return b4anorm;
    }

    /**
     * @return the b4d
     */
    public DoubleSeq getB4d() {
        return b4d;
    }

    /**
     * @return the b4g
     */
    public DoubleSeq getB4g() {
        return b4g;
    }

    /**
     * @return the b9g
     */
    public DoubleSeq getB9g() {
        return b9g;
    }

    private DoubleSeq b1, b2, b3, b4, b4a, b4anorm, b4d, b4g, b5, b6,
            b7, b8, b9, b9g, b10, b11, b13, b17, b20;
    private int b2drop;

    public X11BStep() {
    }

    public void process(DoubleSeq input, X11Context context) {
        b1 = input;
        b2(context);
        b3(context);
        b4(context);
        b5(context);
        b6(context);
        b7(context);
        b8(context);
        b9(context);
        bfinal(context);
    }

    private void b2(X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        b2drop = filter.length() / 2;

        double[] x = RawX11Kernel.table(b1.length() - 2 * b2drop, Double.NaN);
        DataBlock out = DataBlock.of(x, 0, x.length);
        filter.apply(b1, out);
        b2 = DoubleSeq.of(x);
    }

    private void b3(X11Context context) {
        b3 = context.remove(b1.drop(b2drop, b2drop), b2);
    }

    private void b4(X11Context context) {
        IFiltering filter = context.getInitialSeasonalFiltering();
        b4a = filter.process(b3);
        b4anorm = DefaultSeasonalNormalizer.normalize(b4a, 0, context);
        b4d = context.remove(b3, b4anorm);

        DefaultExtremeValuesCorrector ecorr = new DefaultExtremeValuesCorrector();
        ecorr.setStart(b2drop);
        ecorr.analyse(b4d, context);

        b4 = ecorr.computeCorrections(b3);
        b4g = ecorr.applyCorrections(b3, b4);
    }

    private void b5(X11Context context) {
        IFiltering filter = context.getInitialSeasonalFiltering();
        DoubleSeq b5a = filter.process(b4g);
        b5 = DefaultSeasonalNormalizer.normalize(b5a, b2drop, context);
    }

    private void b6(X11Context context) {
        b6 = context.remove(b1, b5);
    }

    private void b7(X11Context context) {
        b7 = context.getTrendFiltering().process(b6);
        if (context.getMode().isMultiplicative() && b7.anyMatch(z -> z <= 0)) {
            throw new X11Exception(X11Exception.ERR_NEG);
        }
    }

    private void b8(X11Context context) {
        b8 = context.remove(b1, b7);
    }

    private void b9(X11Context context) {
        IFiltering filter = context.getFinalSeasonalFiltering();
        DoubleSeq b9a = filter.process(b8);
        DoubleSeq b9c = DefaultSeasonalNormalizer.normalize(b9a, 0, context);
        DoubleSeq b9d = context.remove(b8, b9c);
        DefaultExtremeValuesCorrector ecorr = new DefaultExtremeValuesCorrector();
        ecorr.setStart(0);
        ecorr.analyse(b9d, context);

        b9 = ecorr.computeCorrections(b8);
        b9g = ecorr.applyCorrections(b8, b9);
    }

    private void bfinal(X11Context context) {
        IFiltering filter = context.getFinalSeasonalFiltering();
        DoubleSeq b10a = filter.process(b9g);
        b10 = DefaultSeasonalNormalizer.normalize(b10a, 0, context);
        b11 = context.remove(b1, b10);
        b13 = context.remove(b11, b7);

        DefaultExtremeValuesCorrector ecorr = new DefaultExtremeValuesCorrector();
        ecorr.setStart(0);
        ecorr.analyse(b13, context);
        b17 = ecorr.getObservationWeights();
        b20 = ecorr.getCorrectionFactors();
    }

    /**
     * @return the b1
     */
    public DoubleSeq getB1() {
        return b1;
    }

    /**
     * @return the b2
     */
    public DoubleSeq getB2() {
        return b2;
    }

    /**
     * @return the b3
     */
    public DoubleSeq getB3() {
        return b3;
    }

    public DoubleSeq getB4() {
        return b4;
    }

    /**
     * @return the b5
     */
    public DoubleSeq getB5() {
        return b5;
    }

    /**
     * @return the b6
     */
    public DoubleSeq getB6() {
        return b6;
    }

    /**
     * @return the b7
     */
    public DoubleSeq getB7() {
        return b7;
    }

    /**
     * @return the b8
     */
    public DoubleSeq getB8() {
        return b8;
    }

    /**
     * @return the b9
     */
    public DoubleSeq getB9() {
        return b9;
    }

    /**
     * @return the b10
     */
    public DoubleSeq getB10() {
        return b10;
    }

    /**
     * @return the b11
     */
    public DoubleSeq getB11() {
        return b11;
    }

    /**
     * @return the b13
     */
    public DoubleSeq getB13() {
        return b13;
    }

    /**
     * @return the b19
     */
    public DoubleSeq getB17() {
        return b17;
    }

    /**
     * @return the b20
     */
    public DoubleSeq getB20() {
        return b20;
    }

    /**
     * @param b20 the b20 to set
     */
    public void setB20(DoubleSeq b20) {
        this.b20 = b20;
    }
}
