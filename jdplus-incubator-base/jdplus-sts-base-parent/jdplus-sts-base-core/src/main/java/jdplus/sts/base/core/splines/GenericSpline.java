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

import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.splines.BSplines;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class GenericSpline implements SplineDefinition {

    public static GenericSpline of(double period, int order) {
        return of(period, (int) Math.floor(period), order);
    }

    public static GenericSpline of(double period, int nnodes, int order) {
        double[] dnodes = new double[nnodes];
        double step = period / nnodes;
        double cur = step / 2;
        dnodes[0] = cur;
        int i = 1;
        while (i < nnodes) {
            cur += step;
            dnodes[i++] = cur;
        }
        return new GenericSpline(BSplines.periodic(order, dnodes, period));
    }

    /**
     *
     * @param period Period of the splines
     * @param nodes Should be ordered and in [0, period[. NOT CHECKED !
     * @param order
     * @return
     */
    public static GenericSpline of(double period, DoubleSeq nodes, int order) {
        return new GenericSpline(BSplines.periodic(order, nodes.toArray(), period));
    }

    private final BSplines.BSpline spline;

    @Override
    public double getPeriod() {
        return spline.getPeriod();
    }

    @Override
    public DoubleSeq nodes() {
        return spline.knots();
    }

    private static final double EPS = 1e-9;

    @Override
    public IntSeq observations(int cycle) {
        // we need to find the integers in [cycle*period, (cycle+1)*period[
        int i0 = (int) Math.ceil(spline.getPeriod() * cycle - EPS),
                i1 = 1 + (int) Math.floor(spline.getPeriod() * (cycle + 1) - EPS);
        return IntSeq.sequential(i0, i1);
    }

    @Override
    public int cycleFor(int obs) {
        int c = (int) Math.floor(obs / spline.getPeriod() + EPS);
//            if (obs>=(int) Math.floor(period * (c + 1) + EPS))
//                ++c;
        return c;
    }

    @Override
    public DoubleUnaryOperator[] splines() {
        DoubleSeq nodes = nodes();
        int dim = nodes.length();

        double[] dnodes = nodes().toArray();

        DoubleUnaryOperator[] splines = new DoubleUnaryOperator[dim];
        for (int i = 0; i < dim; ++i) {
            switch (spline.getOrder()) {
                case 1 -> {
                    splines[i] = of1(dnodes, i, spline.getPeriod());
                }
                case 2 -> {
                    splines[i] = of2(dnodes, i, spline.getPeriod());
                }
                default ->
                    throw new java.lang.UnsupportedOperationException("Not implemented yet");
            }
        }
        return splines;
    }

    private static DoubleUnaryOperator of1(double[] dnodes, int i, double period) {
        int last = dnodes.length - 1;
        if (i < last) {
            return x -> {
                double xc = x - period * (int) (x / period);
                return (xc >= dnodes[i] && xc < dnodes[i + 1]) ? 1 : 0;
            };
        } else {
            return x -> {
                double xc = x - period * (int) (x / period);
                return (xc < dnodes[0] || xc >= dnodes[last]) ? 1 : 0;
            };
        }
    }

    private static DoubleUnaryOperator of2(double[] dnodes, int i, double period) {
        int last = dnodes.length - 1;
        if (i != 0 && i != last) {
            return x -> {
                double xc = x - period * (int) (x / period);
                if (xc <= dnodes[i - 1] || xc >= dnodes[i + 1]) {
                    return 0;
                } else if (xc < dnodes[i]) {
                    return (xc - dnodes[i - 1]) / (dnodes[i] - dnodes[i - 1]);
                } else {
                    return (dnodes[i + 1] - xc) / (dnodes[i + 1] - dnodes[i]);
                }
            };
        } else if (i == 0) {
            return x -> {
                double xc = x - period * (int) (x / period);
                if (xc <= dnodes[last] && xc >= dnodes[1]) {
                    return 0;
                } else if (xc > dnodes[last]) {
                    return (xc - dnodes[last]) / (dnodes[0] - dnodes[last] + period);
                 } else if (xc < dnodes[0]) {
                    return (xc - dnodes[last] + period) / (dnodes[0] - dnodes[last] + period);
                } else { // xc >= dnodes[last]
                    return (dnodes[1] - xc) / (dnodes[1] - dnodes[0]);
               }
            };
        } else { // i xlast
            return x -> {
                double xc = x - period * (int) (x / period);
                if (xc <= dnodes[last-1] && xc >= dnodes[0]) {
                    return 0;
                } else if (xc < dnodes[0]) {
                    return (dnodes[0]-xc) / (dnodes[0] - dnodes[last] + period);
                } else if (xc > dnodes[last]) {
                    return (dnodes[0]+period-xc) / (dnodes[0]+period-dnodes[last]);
                } else { // xc <dnodes[0]
                    return (xc-dnodes[last-1] ) / (dnodes[last] - dnodes[last-1]);
                }
            };
        }
    }

}
