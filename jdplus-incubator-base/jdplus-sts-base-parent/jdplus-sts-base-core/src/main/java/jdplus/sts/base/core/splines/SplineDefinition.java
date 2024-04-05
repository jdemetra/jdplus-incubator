/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.sts.base.core.splines;

import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public interface SplineDefinition {

    /**
     * Length of a cycle
     *
     * @return
     */
    double getPeriod();

    /**
     * Nodes of the spline (same for each cycle). Nodes don't necessary
     * correspond to observations. The nodes are defined for the first cycle
     * ([0, period[
     *
     * @return
     */
    DoubleSeq nodes();

    /**
     * Positions of the observations for the given cycle
     * Must be inside [cycle*period, (cycle+1)*period[.
     *
     * @param cycle
     * @return
     */
    IntSeq observations(int cycle);
    
    /**
     * Cycle corresponding to the given observation position
     * @param obs Observation position
     * @return 
     */
    int cycleFor(int obs);

    default CubicSplines.Spline[] splines() {
        DoubleSeq nodes = nodes();
        int dim = nodes.length();

        CubicSplines.Spline[] splines = new CubicSplines.Spline[dim];
        double[] xi = new double[dim + 1];
        nodes.copyTo(xi, 0);
        double x0 = xi[0];
        xi[dim] = x0 + getPeriod();

        for (int i = 0; i < dim; ++i) {
            double[] f = new double[dim + 1];
            if (i == 0) {
                f[0] = 1;
                f[dim] = 1;
            } else {
                f[i] = 1;
            }
            splines[i] = CubicSplines.periodic(DoubleSeq.of(xi), DoubleSeq.of(f));
        }
        return splines;
    }
}
