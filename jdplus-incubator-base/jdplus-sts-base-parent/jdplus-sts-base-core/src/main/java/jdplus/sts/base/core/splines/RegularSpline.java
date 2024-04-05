/*
 * Copyright 2024 JDemetra+.
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
package jdplus.sts.base.core.splines;

import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class RegularSpline implements SplineDefinition {

    public static RegularSpline of(double period) {
        return of(period, (int) Math.floor(period));
    }

    public static RegularSpline of(double period, int nnodes) {
        double[] dnodes = new double[nnodes];
        double step = period / nnodes;
        double cur = step / 2;
        dnodes[0] = cur;
        int i = 1;
        while (i < nnodes) {
            cur += step;
            dnodes[i++] = cur;
        }
        return new RegularSpline(period, DoubleSeq.of(dnodes));
    }

    /**
     *
     * @param period Period of the splines
     * @param nodes Should be ordered and in [0, period[. NOT CHECKED !
     * @return
     */
    public static RegularSpline of(double period, DoubleSeq nodes) {
        return new RegularSpline(period, nodes);
    }

    private final double period;
    private final DoubleSeq nodes;

    @Override
    public double getPeriod() {
        return period;
    }

    @Override
    public DoubleSeq nodes() {
        return nodes;
    }

    private static final double EPS = 1e-12;

    @Override
    public IntSeq observations(int cycle) {
        // we need to find the integers in [cycle*period, (cycle+1)*period[
            int i0 =  (int) Math.floor(period * cycle + EPS),
                    i1 = (int) Math.floor(period * (cycle + 1) + EPS);
            return IntSeq.sequential(i0, i1);
    }

    @Override
    public int cycleFor(int obs) {
            int c=(int) Math.floor((obs + EPS) / period);
            if (obs>=(int) Math.floor(period * (c + 1) + EPS))
                ++c;
            return c; 
    }

}
