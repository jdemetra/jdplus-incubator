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
import jdplus.sts.base.core.msts.survey.WaveSpecificSurveyErrors;
import jdplus.toolkit.base.core.ssf.StateComponent;
import java.util.Arrays;
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
public class MsaeItem extends StateItem {

    private final int nwaves;
    private final int lag;
    private final int[] lar;
    private final ArInterpreter[] par;

    public MsaeItem(String name, int nwaves, Matrix ar, boolean fixedar, int lag) {
        super(name);
        this.nwaves = nwaves;
        this.lag = lag;
        final int nar = ar.getColumnsCount();
        lar = new int[nar];
        par = new ArInterpreter[nar];
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
    
   private MsaeItem(MsaeItem item) {
        super(item.name);
        this.nwaves=item.nwaves;
        this.lag=item.lag;
        this.lar=item.lar;
        this.par=new ArInterpreter[item.par.length];
        for (int i=0; i<par.length; ++i)
            par[i]=item.par[i].duplicate();
     }

    @Override
    public MsaeItem duplicate() {
        return new MsaeItem(this);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        for (int i = 0; i < par.length; ++i) {
            mapping.add(par[i]);
        }
        mapping.add((p, builder) -> {
            double[][] w = new double[nwaves][];
            w[0] = DoubleSeq.EMPTYARRAY;
            int pos = 0;
            int nar = lar.length;
            for (int i = 0; i < nar; ++i) {
                w[i + 1] = p.extract(pos, lar[i]).toArray();
                pos += lar[i];
            }
            // same coefficients for the last waves, if any
            for (int i = nar + 1; i < nwaves; ++i) {
                w[i] = w[i - 1];
            }
            StateComponent cmp = WaveSpecificSurveyErrors.of(w, lag);
            builder.add(name, cmp, null);
            return pos;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(par);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double[][] w = new double[nwaves][];
        w[0] = DoubleSeq.EMPTYARRAY;
        int pos = 0;
        int nar = lar.length;
        for (int i = 0; i < nar; ++i) {
            w[i + 1] = p.extract(pos, lar[i]).toArray();
            pos += lar[i];
        }
        // same coefficients for the last waves, if any
        for (int i = nar + 1; i < nwaves; ++i) {
            w[i] = w[i - 1];
        }
        return WaveSpecificSurveyErrors.of(w, lag);
    }

    @Override
    public int parametersCount() {
        int n = 0;
        int nar = lar.length;
        for (int i = 0; i < nar; ++i) {
            n += lar[i];
        }
        return n;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > nwaves) {
            return null;
        } else {
            return Loading.fromPosition(2 * m);
        }
    }

    @Override
    public int defaultLoadingCount() {
        return nwaves;
    }

    @Override
    public int stateDim() {
        return 2*nwaves;
    }
}
