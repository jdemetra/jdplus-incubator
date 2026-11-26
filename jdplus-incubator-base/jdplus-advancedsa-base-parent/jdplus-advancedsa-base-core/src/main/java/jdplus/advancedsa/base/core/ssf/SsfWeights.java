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
package jdplus.advancedsa.base.core.ssf;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixStorage;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.akf.QAugmentation;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 * 0/1 approach only for the time being
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SsfWeights {

    /**
     * Generates the smoothed states corresponding to indicator variables and
     * return them as a collection of matrices. Each matrix contains the
     * smoothed states organized in columns The current implementation could be
     * significantly improved
     *
     * @param ssf State space form
     * @param n Length of the indicators/weights
     * @return A storage containing n matrices of dim ssf.dim x n; each row of a
     * matrix gives the impact (weight) of the considered indicator on one item
     * of the smoothed states
     */
    public MatrixStorage smoothingWeights(ISsf ssf, int n) {
        MatrixStorage storage = new MatrixStorage(ssf.getStateDim(), n, n);
        storage.use(n);
        for (int i = 0; i < n; ++i) {
            double[] x = new double[n];
            x[i] = 1;
            DefaultSmoothingResults sr =
                    AkfToolkit.smooth(ssf, new SsfData(x), false, false, true, QAugmentation.QType.NORMAL);
            //            DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, new SsfData(x), false, false);
            FastMatrix M = storage.matrix(i);
            DataBlockIterator columnsIterator = M.columnsIterator();
            for (int j = 0; j < n; ++j) {
                columnsIterator.next().copy(sr.a(j));
            }
        }
        return storage;
    }

    public void smoothingWeights(ISsf ssf, int obsPos, FastMatrix M) {
        int n = M.getColumnsCount();
        double[] x = new double[n];
        x[obsPos] = 1;
        DefaultSmoothingResults sr =
                AkfToolkit.smooth(ssf, new SsfData(x), false, false, true, QAugmentation.QType.NORMAL);
        //            DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, new SsfData(x), false, false);
        DataBlockIterator columnsIterator = M.columnsIterator();
        for (int j = 0; j < n; ++j) {
            columnsIterator.next().copy(sr.a(j));
        }
    }

    public void smoothedComponentWeights(DoubleSeq Z, int zstart, FastMatrix S, DataBlock C) {
        int m = Z.length();
        int j = zstart;
        DoubleSeqCursor cursor = Z.cursor();
        C.setAY(cursor.getAndNext(), S.row(j++));
        while (j < m) {
            C.addAY(cursor.getAndNext(), S.row(j++));
        }
    }

    public void smoothedComponentsWeights(CompositeSsf ssf, FastMatrix S, FastMatrix C) {
        int[] dim = ssf.componentsDimension();
        if (ssf.measurement().isTimeInvariant()) {
            DataBlock z = DataBlock.make(ssf.getStateDim());
            ssf.loading().Z(0, z);
            int zstart = 0;
            for (int i = 0; i < dim.length; ++i) {
                smoothedComponentWeights(z.extract(zstart, dim[i]), zstart, S, C.row(i));
                zstart += dim[i];
            }
        } else {
            int n = C.getColumnsCount();
            for (int r = 0, s = 0; r < ssf.getComponentsCount(); ++r) {
                int d = dim[r];
                DataBlock row = C.row(r);
                ISsfLoading loading = ssf.loading(r);
                FastMatrix Se = S.extract(s, d, 0, n);
                DataBlockIterator cols = Se.columnsIterator();
                for (int c = 0; c < n; ++c) {
                    row.set(c, loading.ZX(c, cols.next()));
                }
                s += d;
            }
        }
    }

    /**
     * Gets the weights corresponding to the given "Z-component"
     *
     * @param storage
     * @param pos initial position of the Z selection array
     * @param Z
     * @param M n x n. On return, M contains in row-i the weights corresponding
     * to the filter for observation i
     */
    private void weights(MatrixStorage storage, int pos, DoubleSeq Z, FastMatrix M) {
        int m = Z.length();
        DataBlockIterator cols = M.columnsIterator();
        int i = 0;
        while (cols.hasNext()) {
            DataBlock col = cols.next();
            int j = pos;
            DoubleSeqCursor cursor = Z.cursor();
            col.setAY(cursor.getAndNext(), storage.item(j++, i));
            while (j < m) {
                col.addAY(cursor.getAndNext(), storage.item(j++, i));
            }
            ++i;
        }
    }

    /**
     * Gets the weights corresponding to the different components of the ssf
     *
     * @param ssf
     * @param storage Contains as many matrices as components in the ssf; each
     * matrix contains in the row i the impact of the observation i for all time
     * periods and in the column j the weights to apply to the observations to
     * get the hidden component
     *
     * @return
     */
    public MatrixStorage weights(CompositeSsf ssf, MatrixStorage storage) {
        int n = storage.getMatrixColumnsCount();
        int[] dim = ssf.componentsDimension();
        MatrixStorage rslt = new MatrixStorage(n, n, dim.length);
        rslt.use(dim.length);
        if (ssf.measurement().isTimeInvariant()) {
            DataBlock Z = DataBlock.make(ssf.getStateDim());
            ssf.loading().Z(0, Z);
            for (int i = 0, k = 0; i < dim.length; ++i) {
                FastMatrix R = rslt.matrix(i);
                DataBlock curZ = Z.extract(k, dim[i]);
                weights(storage, k, curZ, R);
                k += dim[i];
            }
        }
        return rslt;
    }

    public MatrixStorage weights2(CompositeSsf ssf, int n) {
        MatrixStorage W = smoothingWeights(ssf, n);
        return weights(ssf, W);
    }

    public MatrixStorage weights(CompositeSsf ssf, int n) {
        int[] dim = ssf.componentsDimension();
        int m = ssf.getStateDim();
        MatrixStorage rslt = new MatrixStorage(n, n, dim.length);
        rslt.use(dim.length);
        FastMatrix S = FastMatrix.make(m, n);
        FastMatrix C = FastMatrix.make(dim.length, n);
        for (int i = 0; i < n; ++i) {
            // we get the smoothed states for the obs i
            smoothingWeights(ssf, i, S);
            // convert them in smoothed components for the obs i
            smoothedComponentsWeights(ssf, S, C);
            for (int j = 0; j < dim.length; ++j) {
                rslt.matrix(j).row(i).copy(C.row(j));
            }
        }
        return rslt;
    }
}
