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
import jdplus.sts.base.core.msts.ArInterpreter;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.sts.base.core.msts.survey.WaveSpecificSurveyErrors3;
import jdplus.toolkit.base.core.ssf.StateComponent;
import java.util.ArrayList;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class MsaeItem3 extends StateItem {
    
    private final VarianceInterpreter[] v;
    private final Matrix k;
    private final int lag;
    private final ArInterpreter[] par;
    
    public MsaeItem3(String name, double[] v, boolean fixedVar, double[] ar, boolean fixedar, Matrix k, int lag) {
        super(name);
        int nwaves = v.length;
        this.lag = lag;
        this.k = k;
        final int nar = ar.length;
        par = new ArInterpreter[nar];
        this.v = new VarianceInterpreter[nwaves];
        for (int i = 0; i < nwaves; ++i) {
            this.v[i] = new VarianceInterpreter(name + ".var" + (i + 1), v[i], fixedVar, true);
        }
        for (int i = 0; i < nar; ++i) {
            par[i] = new ArInterpreter(name + ".wae" + (i + 1), new double[]{ar[i]}, fixedar);
        }
    }
    
    private MsaeItem3(MsaeItem3 item) {
        super(item.name);
        this.lag = item.lag;
        this.k = item.k;
        this.v = new VarianceInterpreter[item.v.length];
        for (int i = 0; i < v.length; ++i) {
            v[i] = item.v[i].duplicate();
        }
        this.par = new ArInterpreter[item.par.length];
        for (int i = 0; i < par.length; ++i) {
            par[i] = item.par[i].duplicate();
        }
    }
    
    @Override
    public MsaeItem3 duplicate() {
        return new MsaeItem3(this);
    }
    
    @Override
    public void addTo(MstsMapping mapping) {
        for (int i = 0; i < v.length; ++i) {
            mapping.add(v[i]);
        }
        for (int i = 0; i < par.length; ++i) {
            mapping.add(par[i]);
        }
        mapping.add((p, builder) -> {
            int nwaves = v.length;
            double[] var = new double[nwaves];
            int pos = 0;
            for (int i = 0; i < nwaves; ++i) {
                var[i] = p.get(pos++);
            }
            double[] ar = new double[nwaves - 1];
            for (int i = 0; i < par.length; ++i) {
                ar[i] = p.get(pos++);
            }
            // same coefficients for the last waves, if any
            for (int i = par.length + 1; i < ar.length; ++i) {
                ar[i] = ar[i - 1];
            }
            StateComponent cmp = WaveSpecificSurveyErrors3.of(var, ar, k, lag);
            builder.add(name, cmp, null);
            return pos;
        });
    }
    
    @Override
    public List<ParameterInterpreter> parameters() {
        List<ParameterInterpreter> all = new ArrayList<>();
        for (int i = 0; i < v.length; ++i) {
            all.add(v[i]);
        }
        for (int i = 0; i < par.length; ++i) {
            all.add(par[i]);
        }
        return all;
    }
    
    @Override
    public StateComponent build(DoubleSeq p) {
        int nwaves = v.length;
        double[] var = new double[nwaves];
        if (p == null) {
             for (int i = 0; i < nwaves; ++i) {
                var[i] = v[i].variance();
            }
            double[] ar = new double[nwaves - 1];
            for (int i = 0; i < par.length; ++i) {
                ar[i] = par[i].values().get(0);
            }
            // same coefficients for the last waves, if any
            for (int i = par.length + 1; i < ar.length; ++i) {
                ar[i] = ar[i - 1];
            }
            return WaveSpecificSurveyErrors3.of(var, ar, k, lag);
        } else {
            int pos = 0;
            for (int i = 0; i < nwaves; ++i) {
                var[i] = p.get(pos++);
            }
            double[] ar = new double[nwaves - 1];
            for (int i = 0; i < par.length; ++i) {
                ar[i] = p.get(pos++);
            }
            // same coefficients for the last waves, if any
            for (int i = par.length + 1; i < ar.length; ++i) {
                ar[i] = ar[i - 1];
            }
            return WaveSpecificSurveyErrors3.of(var, ar, k, lag);
        }
    }
    
    @Override
    public int parametersCount() {
        return v.length + par.length;
    }
    
    @Override
    public ISsfLoading defaultLoading(int m) {
        return m > v.length ? null : Loading.fromPosition(m * lag);
    }
    
    @Override
    public int defaultLoadingCount() {
        return v.length;
    }
    
    @Override
    public int stateDim() {
        return v.length * lag;
    }
}
