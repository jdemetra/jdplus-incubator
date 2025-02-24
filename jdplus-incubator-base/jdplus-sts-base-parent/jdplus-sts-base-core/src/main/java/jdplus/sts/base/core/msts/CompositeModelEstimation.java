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
package jdplus.sts.base.core.msts;

import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.toolkit.base.core.ssf.composite.MultivariateCompositeSsf;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.StateFilteringResults;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.stats.likelihood.Likelihood;
import jdplus.toolkit.base.api.math.functions.Optimizer;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import java.util.Arrays;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.ssf.ISsfLoading;

/**
 *
 * @author palatej
 */
public class CompositeModelEstimation {

    public static CompositeModelEstimation estimationOf(CompositeModel model, FastMatrix data,
            boolean marginal, boolean concentrated, SsfInitialization initialization, Optimizer optimizer, double eps, double[] parameters) {
        CompositeModelEstimation rslt = new CompositeModelEstimation();
        rslt.data = data;
        MstsMonitor monitor = MstsMonitor.builder()
                .marginal(marginal)
                .concentratedLikelihood(concentrated)
                .initialization(initialization)
                .optimizer(optimizer)
                .precision(eps)
                .build();
        MstsMapping mapping = model.mapping();
        monitor.process(data, mapping, parameters == null ? null : DoubleSeq.of(parameters));
        rslt.model = model;
        rslt.likelihood = monitor.getLikelihood();
        rslt.ssf = monitor.getSsf();
        rslt.cmpPos = rslt.getSsf().componentsPosition();
        rslt.parameters = monitor.getParameters().toArray();
        rslt.fullParameters = monitor.fullParameters().toArray();
        rslt.parametersName = mapping.parametersName();
        rslt.cmpName = model.getCmpsName();
        return rslt;
    }

    public static CompositeModelEstimation computationOf(CompositeModel model, FastMatrix data, DoubleSeq fullParameters, boolean marginal, boolean concentrated) {
        CompositeModelEstimation rslt = new CompositeModelEstimation();
        rslt.model = model;
        rslt.data = data;
        rslt.fullParameters = fullParameters.toArray();
        MstsMapping mapping = model.mapping();
        mapping.fixModelParameters(p -> true, fullParameters);
        rslt.parameters = DoubleSeq.EMPTYARRAY;
        rslt.ssf = mapping.map(Doubles.EMPTY);
        rslt.cmpPos = rslt.getSsf().componentsPosition();
        rslt.cmpName = mapping.parametersName();
        rslt.parametersName = mapping.parametersName();
        if (marginal) {
            rslt.likelihood = AkfToolkit.marginalLikelihoodComputer(concentrated, true).
                    compute(M2uAdapter.of(rslt.getSsf()), M2uAdapter.of(new SsfMatrix(data)));
        } else {
            rslt.likelihood = DkToolkit.likelihood(rslt.getSsf(), new SsfMatrix(data), true, false);
        }
        return rslt;
    }

    private Likelihood likelihood;
    private MultivariateCompositeSsf ssf;
    private int[] cmpPos;
    private FastMatrix data;
    private double[] fullParameters, parameters;
    private String[] parametersName, cmpName;
    private StateStorage smoothedStates, filteredStates, filteringStates;
    private CompositeModel model;

    public StateStorage getSmoothedStates() {
        if (smoothedStates == null) {
            try {
                StateStorage ss = AkfToolkit.robustSmooth(getSsf(), new SsfMatrix(getData()), true, false);
                if (likelihood.isScalingFactor()) {
                    ss.rescaleVariances(likelihood.sigma2());
                }
                smoothedStates = ss;
            } catch (OutOfMemoryError err) {
                ISsf ussf = M2uAdapter.of(ssf);
                ISsfData udata = M2uAdapter.of(new SsfMatrix(data));
                DataBlockStorage ds = DkToolkit.fastSmooth(ussf, udata);
                StateStorage ss = StateStorage.light(StateInfo.Smoothed);
                int m = data.getColumnsCount(), n = data.getRowsCount();
                ss.prepare(ussf.getStateDim(), 0, n);
                for (int i = 0; i < n; ++i) {
                    ss.save(i, ds.block(i * m), null);
                }
                if (likelihood.isScalingFactor()) {
                    ss.rescaleVariances(likelihood.sigma2());
                }
                smoothedStates = ss;

            } catch (Exception err) {
//                StateStorage ss = AkfToolkit.smooth(getSsf(), new SsfMatrix(getData()), false, false, false);
                StateStorage ss = DkToolkit.smooth(getSsf(), new SsfMatrix(getData()), true, false);
                if (likelihood.isScalingFactor()) {
                    ss.rescaleVariances(likelihood.sigma2());
                }
                smoothedStates = ss;
            }
        }
        return smoothedStates;
    }

    public StateStorage getFastSmoothedStates() {
        return AkfToolkit.robustSmooth(getSsf(), new SsfMatrix(getData()), false, false);
    }

    public StateStorage getFilteredStates() {
        if (filteredStates == null) {
            try {
                ISsf ussf = M2uAdapter.of(ssf);
                ISsfData udata = M2uAdapter.of(new SsfMatrix(data));
                StateFilteringResults fr = new StateFilteringResults(StateInfo.Concurrent, true);
                int m = data.getColumnsCount(), n = data.getRowsCount();
                fr.prepare(ussf.getStateDim(), 0, udata.length());
                DkToolkit.sqrtFilter(ussf, udata, fr, true);
                StateStorage ss = StateStorage.full(StateInfo.Concurrent);
                ss.prepare(ussf.getStateDim(), 0, n);
                for (int i = 1; i <= n; ++i) {
                    ss.save(i - 1, fr.a(i * m - 1), fr.P(i * m - 1));
                }
                if (likelihood.isScalingFactor()) {
                    ss.rescaleVariances(likelihood.sigma2());
                }
                filteredStates = ss;
            } catch (java.lang.OutOfMemoryError err) {
                ISsf ussf = M2uAdapter.of(ssf);
                ISsfData udata = M2uAdapter.of(new SsfMatrix(data));
                StateFilteringResults fr = new StateFilteringResults(StateInfo.Concurrent, false);
                int m = data.getColumnsCount(), n = data.getRowsCount();
                fr.prepare(ussf.getStateDim(), 0, udata.length());
                DkToolkit.sqrtFilter(ussf, udata, fr, false);
                StateStorage ss = StateStorage.light(StateInfo.Forecast);
                ss.prepare(ussf.getStateDim(), 0, n);
                int nd = fr.getEndDiffusePosition() / m;
                if (fr.getEndDiffusePosition() % m != 0) {
                    ++nd;
                }
                for (int i = 0; i < n; ++i) {
                    ss.save(i, fr.a(i * m), null);
                }
                for (int i = 0; i < nd; ++i) {
                    ss.a(i).set(Double.NaN);
                }
                if (likelihood.isScalingFactor()) {
                    ss.rescaleVariances(likelihood.sigma2());
                }
                filteredStates = ss;
            }
        }
        return filteredStates;
    }

    public StateStorage getFilteringStates() {
        if (filteringStates == null) {
            try {
                ISsf ussf = M2uAdapter.of(ssf);
                ISsfData udata = M2uAdapter.of(new SsfMatrix(data));
                DefaultDiffuseSquareRootFilteringResults fr = DkToolkit.sqrtFilter(ussf, udata, true);
                StateStorage ss = StateStorage.full(StateInfo.Forecast);
                int m = data.getColumnsCount(), n = data.getRowsCount();
                ss.prepare(ussf.getStateDim(), 0, n);
                int nd = fr.getEndDiffusePosition() / m;
                if (fr.getEndDiffusePosition() % m != 0) {
                    ++nd;
                }
                for (int i = 0; i < n; ++i) {
                    ss.save(i, fr.a(i * m), fr.P(i * m));
                }
                for (int i = 0; i < nd; ++i) {
                    ss.a(i).set(Double.NaN);
                    ss.P(i).set(Double.NaN);
                }
                if (likelihood.isScalingFactor()) {
                    ss.rescaleVariances(likelihood.sigma2());
                }
                filteringStates = ss;
            } catch (java.lang.OutOfMemoryError err) {
                // Just computes the states
                ISsf ussf = M2uAdapter.of(ssf);
                ISsfData udata = M2uAdapter.of(new SsfMatrix(data));
                DefaultDiffuseSquareRootFilteringResults fr = DkToolkit.sqrtFilter(ussf, udata, false);
                StateStorage ss = StateStorage.light(StateInfo.Forecast);
                int m = data.getColumnsCount(), n = data.getRowsCount();
                ss.prepare(ussf.getStateDim(), 0, n);
                int nd = fr.getEndDiffusePosition() / m;
                if (fr.getEndDiffusePosition() % m != 0) {
                    ++nd;
                }
                for (int i = 0; i < n; ++i) {
                    ss.save(i, fr.a(i * m), null);
                }
                for (int i = 0; i < nd; ++i) {
                    ss.a(i).set(Double.NaN);
                }
                if (likelihood.isScalingFactor()) {
                    ss.rescaleVariances(likelihood.sigma2());
                }
                filteringStates = ss;
            }
        }
        return filteringStates;
    }

    private static int find(String[] ss, String s) {
        for (int i = 0; i < ss.length; ++i) {
            if (ss[i].equalsIgnoreCase(s)) {
                return i;
            }
        }
        return -1;
    }

    public FastMatrix getSmoothedComponents(int p) {
        ModelEquation equation = model.getEquation(p);
        StateStorage ss = getSmoothedStates();
        int nr = ss.size();
        int nc = equation.getItemsCount();
        FastMatrix C = FastMatrix.make(nr, nc);
        int[] cmpDim = ssf.componentsDimension();

        for (int i = 0; i < nc; ++i) {
            ModelEquation.Item item = equation.getItem(i);
            String cmp = item.getCmp();
            int pos = find(cmpName, cmp);
            if (pos >= 0) {
                ISsfLoading loading = item.getLoading();
                if (loading == null){
                    loading=model.getItem(pos).defaultLoading(p);
                }
                int start = cmpPos[pos], end = start + cmpDim[pos];
                DataBlock col = C.column(i);
                for (int j = 0; j < nr; ++j) {
                    col.set(j, loading.ZX(j, ss.a(j).range(start, end)));
                }
                LoadingInterpreter li = item.getC();
                if (li != null) {
                    col.mul(li.value());
                }
            }
        }
        return C;
    }

    public FastMatrix getFastSmoothedComponents(int p) {
        ModelEquation equation = model.getEquation(p);
        StateStorage ss = getFastSmoothedStates();
        int nr = ss.size();
        int nc = equation.getItemsCount();
        FastMatrix C = FastMatrix.make(nr, nc);
        int[] cmpDim = ssf.componentsDimension();

        for (int i = 0; i < nc; ++i) {
            ModelEquation.Item item = equation.getItem(i);
            String cmp = item.getCmp();
            int pos = find(cmpName, cmp);
            if (pos >= 0) {
                ISsfLoading loading = item.getLoading();
                if (loading == null){
                    loading=model.getItem(pos).defaultLoading(p);
                }
                int start = cmpPos[pos], end = start + cmpDim[pos];
                DataBlock col = C.column(i);
                for (int j = 0; j < nr; ++j) {
                    col.set(j, loading.ZX(j, ss.a(j).range(start, end)));
                }
                LoadingInterpreter li = item.getC();
                if (li != null) {
                    col.mul(li.value());
                }
            }
        }
        return C;
    }

    public FastMatrix getSmoothedComponentsVariance(int eq) {
        ModelEquation equation = model.getEquation(eq);
        StateStorage ss = getSmoothedStates();
        int nr = ss.size();
        int nc = equation.getItemsCount();
        FastMatrix C = FastMatrix.make(nr, nc);
        int[] cmpDim = ssf.componentsDimension();

        for (int i = 0; i < nc; ++i) {
            ModelEquation.Item item = equation.getItem(i);
            String cmp = item.getCmp();
            int pos = find(cmpName, cmp);
            if (pos >= 0) {
                ISsfLoading loading = item.getLoading();
                if (loading == null){
                    loading=model.getItem(pos).defaultLoading(eq);
                }
                int start = cmpPos[pos];
                DataBlock col = C.column(i);
                for (int j = 0; j < nr; ++j) {
                    double v = loading.ZVZ(j, ss.P(j).extract(start, cmpDim[pos], start, cmpDim[pos]));
                    col.set(j, v < 0 ? 0 : v);
                }
                LoadingInterpreter li = item.getC();
                if (li != null) {
                    col.mul(li.value() * li.value());
                }
            }
        }
        return C;
    }

    public CompositeModel getModel() {
        return model;
    }

    public DoubleSeq signal(int obs, int[] cmps) {
        if (obs >= data.getColumnsCount()) {
            return null;
        }
        FastMatrix L = loading(obs, cmps);
        return signal(L);
    }

    public DoubleSeq signal(FastMatrix L) {
        double[] x = new double[data.getRowsCount()];
        DataBlockIterator rows = L.rowsIterator();
        StateStorage ss = getSmoothedStates();
        int pos = 0;
        while (rows.hasNext()) {
            x[pos] = ss.a(pos).dot(rows.next());
            ++pos;
        }
        return DoubleSeq.of(x);
    }

    public DoubleSeq signal(FastMatrix L, int[] cols) {
        DataBlock x = DataBlock.make(data.getRowsCount());
        DataBlockIterator columns = L.columnsIterator();
        StateStorage ss = getSmoothedStates();
        x.set(columns.next(), ss.getComponent(cols[0]), (a, b) -> a * b);
        for (int i = 1; i < cols.length; ++i) {
            x.add(columns.next(), ss.getComponent(cols[i]), (a, b) -> a * b);
        }
        return x.unmodifiable();
    }

    public FastMatrix loading(int obs, int[] cmps) {
        FastMatrix L = FastMatrix.make(data.getRowsCount(), ssf.getStateDim());
        ISsfLoading l = ssf.loading(obs);
        DataBlockIterator rows = L.rowsIterator();
        int pos = 0;
        while (rows.hasNext()) {
            l.Z(pos++, rows.next());
        }
        // suppress unwanted columns
        if (cmps != null) {
            for (int j = 0; j < cmpPos.length; ++j) {
                if (Arrays.binarySearch(cmps, j) < 0) {
                    int start = cmpPos[j], end = j < cmpPos.length - 1 ? cmpPos[j + 1] : ssf.getStateDim();
                    for (int k = start; k < end; ++k) {
                        L.column(k).set(0);
                    }
                }
            }
        }
        return L;
    }

    public DoubleSeq stdevSignal(int obs, int[] cmps) {
        if (obs >= data.getColumnsCount()) {
            return null;
        }
        FastMatrix L = loading(obs, cmps);
        return stdevSignal(L);
    }

    public DoubleSeq stdevSignal(FastMatrix L) {
        double[] x = new double[data.getRowsCount()];
        L.rowsIterator();
        DataBlockIterator rows = L.rowsIterator();
        StateStorage ss = getSmoothedStates();
        int pos = 0;
        while (rows.hasNext()) {
            double v = QuadraticForm.apply(ss.P(pos), rows.next());
            x[pos++] = v <= 0 ? 0 : Math.sqrt(v);
        }
        return DoubleSeq.of(x);
    }

    public DoubleSeq stdevSignal(FastMatrix L, int[] pos) {
        if (pos.length == 1) {
            StateStorage ss = getSmoothedStates();
            double[] v = ss.getComponentVariance(pos[0]).toArray();
            DoubleSeqCursor cursor = L.column(0).cursor();
            for (int i = 0; i < v.length; ++i) {
                v[i] = Math.sqrt(v[i]) * Math.abs(cursor.getAndNext());
            }
            return DoubleSeq.of(v);
        } else {
            FastMatrix M = FastMatrix.make(data.getRowsCount(), ssf.getStateDim());
            for (int i = 0; i < pos.length; ++i) {
                M.column(pos[i]).copy(L.column(i));
            }
            return stdevSignal(M);
        }
    }

    /**
     * @return the likelihood
     */
    public Likelihood getLikelihood() {
        return likelihood;
    }

    /**
     * @return the ssf
     */
    public MultivariateCompositeSsf getSsf() {
        return ssf;
    }

    /**
     * @return the cmpPos
     */
    public int[] getCmpPos() {
        return cmpPos;
    }

    /**
     * @return the cmpPos
     */
    public String[] getCmpName() {
        return cmpName;
    }

    /**
     * @return the data
     */
    public FastMatrix getData() {
        return data;
    }

    /**
     * @return the fullParameters
     */
    public double[] getFullParameters() {
        return fullParameters;
    }

    /**
     * @return the parameters
     */
    public double[] getParameters() {
        return parameters;
    }

    public String[] getParametersName() {
        return parametersName;
    }
}
