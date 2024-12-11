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

import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;

/**
 *
 * @author palatej
 */
public class SplineData {

    @lombok.Getter
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class CycleInformation {

        /**
         * Q = variance of the innovations S = Cholesky factor of Q. Q, S have
         * same size, corresponding to the number of nodes (-1) (constant
         * between years) Z = loadings The number of rows in Z corresponds to
         * the number of obs. Could vary between cycles
         */
        FastMatrix Q, S, Z;
        IntSeq observations;

        public int firstObservation() {
            return observations.pos(0);
        }
    }

    public SplineData(SplineDefinition definition) {
        this.definition = definition;
        splines = definition.splines();
        dim = splines.length - 1;
    }

    public CycleInformation informationForCycle(int cycle) {
        return information(cycle);
    }

    public CycleInformation informationForObservation(int pos) {
        return information(definition.cycleFor(pos));
    }

    private CycleInformation information(int cycle) {
        CycleInformation info = infos.get(cycle);
        if (info != null) {
            return info;
        }
        int cdim = splines.length;
        double[] wstar = new double[cdim];
        IntSeq obs = definition.observations(cycle);
        int m = obs.length();
        FastMatrix Z = FastMatrix.make(m, cdim);
        for (int i = 0; i < cdim; ++i) {
            DoubleSeqCursor.OnMutable cursor = Z.column(i).cursor();
            double s = 0;
            for (int j = 0; j < m; ++j) {
                double w = splines[i].applyAsDouble(obs.pos(j));
                cursor.setAndNext(w);
                s += w;
            }
            wstar[i] = s;
        }
        DataBlock zh = Z.column(cdim - 1);
        double wh = wstar[cdim - 1];
        for (int i = 0; i < cdim - 1; ++i) {
            Z.column(i).addAY(-wstar[i] / wh, zh);
        }

        DataBlock W = DataBlock.of(wstar, 0, cdim);
        FastMatrix Q = FastMatrix.identity(cdim - 1);
        Q.addXaXt(-1 / W.ssq(), W.drop(0, 1));
        FastMatrix S = Q.deepClone();
        SymmetricMatrix.lcholesky(S, 1e-9);
        LowerTriangularMatrix.toLower(S);
        info = new CycleInformation(Q, S, Z.dropBottomRight(0, 1), obs);
        infos.put(cycle, info);
        return info;
    }

    public int getDim() {
        return dim;
    }

    private final SplineDefinition definition;
    private final ConcurrentMap<Integer, CycleInformation> infos = new ConcurrentHashMap<>();
    private final DoubleUnaryOperator[] splines;
    private final int dim;
}
