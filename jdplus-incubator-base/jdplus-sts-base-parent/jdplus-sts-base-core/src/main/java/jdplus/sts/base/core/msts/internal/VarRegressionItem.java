/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts.internal;

import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sts.base.core.msts.MstsMapping;
import java.util.Collections;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.sts.base.core.msts.ScaleInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.Coefficients;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.basic.VarNoise;

/**
 *
 * @author palatej
 */
public class VarRegressionItem extends StateItem {

    public final DoubleSeq x;
    private final ScaleInterpreter scale;
    private final DoubleSeq std;

    public VarRegressionItem(String name, double[] x, double[] std, double scale, boolean fixed) {
        super(name);
        this.x = DoubleSeq.of(x);
        this.scale = new ScaleInterpreter(name + ".scale", scale, fixed, true);
        this.std = DoubleSeq.of(std);
    }

    private VarRegressionItem(VarRegressionItem item) {
        super(item.name);
        this.x = item.x;
        this.scale = item.scale.duplicate();
        this.std = item.std;
    }

    @Override
    public VarRegressionItem duplicate() {
        return new VarRegressionItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(scale);
        mapping.add((p, builder) -> {
            double e = p.get(0);
            StateComponent cmp = Coefficients.timeVaryingCoefficient(std, e);
            builder.add(name, cmp, VarNoise.defaultLoading());
            return 1;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(scale);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double e = p.get(0);
        return Coefficients.timeVaryingCoefficient(std, e);
    }

    @Override
    public int parametersCount() {
        return 1;
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
        return 1;
    }

    @Override
    public boolean isScalable() {
        return !scale.isFixed();
    }

}
