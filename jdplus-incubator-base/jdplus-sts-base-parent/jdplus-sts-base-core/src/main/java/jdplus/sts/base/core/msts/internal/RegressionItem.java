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

import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.Coefficients;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class RegressionItem extends StateItem {

    public final FastMatrix x;
    public final VarianceInterpreter[] v;

    public RegressionItem(String name, Matrix x, final double[] vars, final boolean fixed) {
        super(name);
        this.x = FastMatrix.of(x);
        if (vars == null) {
            v = null;
        } else {
            v = new VarianceInterpreter[vars.length];
            if (v.length == 1) {
                v[0] = new VarianceInterpreter(name + ".var", vars[0], fixed, true);
            } else {
                for (int i = 0; i < v.length; ++i) {
                    v[i] = new VarianceInterpreter(name + ".var" + (i + 1), vars[i], fixed, true);
                }
            }
        }
    }

    private RegressionItem(RegressionItem item) {
        super(item.name);
        this.x = item.x;
        if (item.v == null) {
            v = null;
        } else {
            v = new VarianceInterpreter[item.v.length];
            for (int i = 0; i < v.length; ++i) {
                v[i] = item.v[i].duplicate();
            }
        }
    }

    @Override
    public RegressionItem duplicate() {
        return new RegressionItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        if (v == null) {
            mapping.add((p, builder) -> {
                StateComponent cmp = Coefficients.fixedCoefficients(x.getColumnsCount());
                builder.add(name, cmp, Loading.regression(x));
                return 0;
            });
        } else if (v.length == 1) {
            mapping.add(v[0]);
            mapping.add((p, builder) -> {
                StateComponent cmp = Coefficients.timeVaryingCoefficients(DoubleSeq.onMapping(x.getColumnsCount(), j -> p.get(0)));
                builder.add(name, cmp, Loading.regression(x));
                return 1;
            });
        } else {
            for (int i = 0; i < v.length; ++i) {
                mapping.add(v[i]);
            }
            mapping.add((p, builder) -> {
                StateComponent cmp = Coefficients.timeVaryingCoefficients(p.extract(0, v.length));
                builder.add(name, cmp, Loading.regression(x));
                return v.length;
            });
        }
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        if (v == null) {
            return Collections.emptyList();
        } else if (v.length == 1) {
            return Collections.singletonList(v[0]);
        } else {
            return Arrays.asList(v);
        }
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        if (v == null) {
            return Coefficients.fixedCoefficients(x.getColumnsCount());
        } else if (v.length == 1) {
            if (p == null) {
                return Coefficients.timeVaryingCoefficients(DoubleSeq.onMapping(x.getColumnsCount(), j -> v[0].variance()));
            } else {
                return Coefficients.timeVaryingCoefficients(DoubleSeq.onMapping(x.getColumnsCount(), j -> p.get(0)));
            }
        } else {
            if (p == null) {
                double[] vars = new double[v.length];
                for (int i = 0; i < vars.length; ++i) {
                    vars[i] = v[i].variance();
                }
                return Coefficients.timeVaryingCoefficients(DoubleSeq.of(vars));
            } else {
                return Coefficients.timeVaryingCoefficients(p.extract(0, v.length));
            }
        }
    }

    @Override
    public int parametersCount() {
        return v == null ? 0 : v.length;
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

    @Override
    public int stateDim() {
        return x.getColumnsCount();
    }

}
