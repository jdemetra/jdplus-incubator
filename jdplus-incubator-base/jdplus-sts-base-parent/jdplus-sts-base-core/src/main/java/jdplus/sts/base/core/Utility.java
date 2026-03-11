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
package jdplus.sts.base.core;

import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.ssf.akf.AugmentedFilter;
import jdplus.toolkit.base.core.ssf.akf.AugmentedSmoother;
import jdplus.toolkit.base.core.ssf.akf.DefaultAugmentedFilteringResults;
import jdplus.toolkit.base.core.ssf.akf.QAugmentation;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsfData;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;

/**
 * Workaround to solve bugs/missing options in main 3.7.1 Should be suppressed
 * with main > 3.7.1
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Utility {

    public DefaultSmoothingResults smooth(ISsf ssf, ISsfData data, boolean all, boolean rescaleVariance, QAugmentation.QType type) {
        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(all);
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        DefaultAugmentedFilteringResults fresults = filter(ssf, data, true, type);
        if (smoother.process(ssf, data.length(), fresults, sresults)) {
            if (rescaleVariance) {
                rescaleVariances(sresults, var(data.length(), fresults));
            }
            return sresults;
        } else {
            return null;
        }
    }

    public DefaultAugmentedFilteringResults filter(ISsf ssf, ISsfData data, boolean all, QAugmentation.QType type) {
        QAugmentation Q = QAugmentation.of(type);
        DefaultAugmentedFilteringResults frslts = all
                ? DefaultAugmentedFilteringResults.full(Q) : DefaultAugmentedFilteringResults.light(Q);
        frslts.prepare(ssf, 0, data.length());
        AugmentedFilter filter = new AugmentedFilter();
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public double var(int n, DefaultAugmentedFilteringResults frslts) {
        double ssq = frslts.getAugmentation().ssq();
        int nd = frslts.getCollapsingPosition();
        int m = frslts.getAugmentation().getDegreesOfFreedom();
        for (int i = nd; i < n; ++i) {
            double e = frslts.error(i);
            if (Double.isFinite(e)) {
                ++m;
                ssq += e * e / frslts.errorVariance(i);
            }
        }
        return ssq / m;
    }

    public void rescaleVariances(DefaultSmoothingResults r, double v) {
        for (int i = 0; i < r.size(); ++i) {
            r.P(i).mul(v);
        }
    }

    public StateStorage smooth(IMultivariateSsf ssf, IMultivariateSsfData data, boolean all, boolean rescaleVariance, QAugmentation.QType type) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        StateStorage sr = smooth(ussf, udata, all, rescaleVariance, type);
        StateStorage ss = all ? StateStorage.full(StateInfo.Smoothed) : StateStorage.light(StateInfo.Smoothed);
        int m = data.getVarsCount(), n = data.getObsCount();
        ss.prepare(ussf.getStateDim(), 0, n);
        if (all) {
            for (int i = 0; i < n; ++i) {
                ss.save(i, sr.a(i * m), sr.P(i * m));
            }
        } else {
            for (int i = 0; i < n; ++i) {
                ss.save(i, sr.a(i * m), null);
            }
        }
        return ss;
    }

}
