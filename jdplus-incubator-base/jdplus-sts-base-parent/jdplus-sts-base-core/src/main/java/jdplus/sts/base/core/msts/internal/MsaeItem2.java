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
import jdplus.sts.base.core.msts.ArInterpreter;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.sts.base.core.msts.VarianceInterpreter;
import jdplus.sts.base.core.msts.survey.WaveSpecificSurveyErrors2;
import jdplus.toolkit.base.core.ssf.StateComponent;
import java.util.ArrayList;
import java.util.List;
import jdplus.sts.base.core.msts.ParameterInterpreter;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class MsaeItem2 extends StateItem {

    private final VarianceInterpreter[] v;
    private final int lag;
    private final int maxar;
    private final int[] lar;
    private final ArInterpreter[] par;

    public MsaeItem2(String name, double[] v, boolean fixedVar, Matrix ar, boolean fixedar, int lag) {
        super(name);
        int nwaves = v.length;
        this.lag = lag;
        final int nar = ar.getColumnsCount();
        maxar = ar.getRowsCount();
        lar = new int[nar];
        par = new ArInterpreter[nar];
        this.v = new VarianceInterpreter[nwaves];
        for (int i = 0; i < nwaves; ++i) {
            this.v[i] = new VarianceInterpreter(name + ".var" + (i + 1), v[i], fixedVar, true);
        }
        for (int i = 0; i < nar; ++i) {
            int j = 0;
            for (; j <= i && j < ar.getRowsCount(); ++j) {
                double c = ar.get(j, i);
                if (Double.isNaN(c)) {
                    break;
                }
            }
            lar[i] = j;
            double[] car = ar.column(i).extract(0, j).toArray();
            par[i] = new ArInterpreter(name + ".wae" + (i + 1), car, fixedar);
        }
    }

    private MsaeItem2(MsaeItem2 item) {
        super(item.name);
        this.lag = item.lag;
        this.maxar = item.maxar;
        this.lar = item.lar;
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
    public MsaeItem2 duplicate() {
        return new MsaeItem2(this);
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
            double[][] w = new double[nwaves][];
            w[0] = DoubleSeq.EMPTYARRAY;
            int nar = lar.length;
            for (int i = 0; i < nar; ++i) {
                w[i + 1] = p.extract(pos, lar[i]).toArray();
                pos += lar[i];
            }
            // same coefficients for the last waves, if any
            for (int i = nar + 1; i < nwaves; ++i) {
                w[i] = w[i - 1];
            }
            StateComponent cmp = WaveSpecificSurveyErrors2.of(var, w, lag);
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
        int pos = 0;
        for (int i = 0; i < nwaves; ++i) {
            var[i] = p.get(pos++);
        }
        double[][] w = new double[nwaves][];
        w[0] = DoubleSeq.EMPTYARRAY;
        int nar = lar.length;
        for (int i = 0; i < nar; ++i) {
            w[i + 1] = p.extract(pos, lar[i]).toArray();
            pos += lar[i];
        }
        // same coefficients for the last waves, if any
        for (int i = nar + 1; i < nwaves; ++i) {
            w[i] = w[i - 1];
        }
        return WaveSpecificSurveyErrors2.of(var, w, lag);
    }

    @Override
    public int parametersCount() {
        int n = v.length;
        int nar = lar.length;
        for (int i = 0; i < nar; ++i) {
            n += lar[i];
        }
        return n;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        int n = maxar * lag;
        return m > v.length ? null : Loading.fromPosition(m * n);
    }

    @Override
    public int defaultLoadingCount() {
        return v.length;
    }

    @Override
    public int stateDim() {
        return maxar * lag * v.length;
    }
}
