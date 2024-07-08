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
package jdplus.sts.base.core.msts.internal;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.Coefficients;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class TdRegressionItem extends StateItem {

    private final FastMatrix x;
    private final FastMatrix mvar;
    private final VarianceInterpreter v;

    public TdRegressionItem(String name, TsDomain domain, int[] groups, final boolean contrast, final double var, final boolean fixed) {
        super(name);
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        this.x = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        this.v = new VarianceInterpreter(name + ".var", var, fixed, true);
        if (var == 0 && fixed) {
            this.mvar = null;
        } else {
            this.mvar = generateVar(dc, contrast);
        }
    }

    private TdRegressionItem(TdRegressionItem item) {
        super(item.name);
        this.x = item.x;
        this.mvar = item.mvar;
        this.v = item.v == null ? null : item.v.duplicate();
    }

    @Override
    public TdRegressionItem duplicate() {
        return new TdRegressionItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(v);
        mapping.add((p, builder) -> {
            double pvar = p.get(0);
            StateComponent cmp;
            if (mvar == null) {
                cmp = Coefficients.fixedCoefficients(x.getColumnsCount());
            } else {
                FastMatrix xvar = mvar.deepClone();
                xvar.mul(pvar);
                cmp = Coefficients.timeVaryingCoefficients(xvar);
            }
            builder.add(name, cmp, Loading.regression(x));
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(v);
    }

    public static Matrix tdContrasts(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        FastMatrix td = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        return td.unmodifiable();
    }

    public static Matrix rawTd(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.raw(dc);
        FastMatrix td = Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
        return td.unmodifiable();
    }

    public static FastMatrix generateVar(DayClustering dc, boolean contrasts) {
        int groupsCount = dc.getGroupsCount();
        FastMatrix full = FastMatrix.square(7);
        if (!contrasts) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        FastMatrix Q = FastMatrix.make(groupsCount - 1, 7);
        int[] gdef = dc.getGroupsDefinition();
        for (int i = 1; i < groupsCount; ++i) {
            for (int j = 0; j < 7; ++j) {
                if (gdef[j] == i) {
                    Q.set(i - 1, j, 1);
                }
            }
        }
        return SymmetricMatrix.XSXt(full, Q);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double pvar = p == null ? v.variance() : p.get(0);
       if (mvar == null) {
            return Coefficients.fixedCoefficients(x.getColumnsCount());
        } else {
            FastMatrix xvar = mvar.deepClone();
            xvar.mul(pvar);
            return Coefficients.timeVaryingCoefficients(xvar);
        }
    }

    @Override
    public int parametersCount() {
        return 1;
    }

    @Override
    public int stateDim() {
        return x.getColumnsCount();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > 0) {
            return null;
        } else {
            return Loading.regression(x);
        }
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

}
