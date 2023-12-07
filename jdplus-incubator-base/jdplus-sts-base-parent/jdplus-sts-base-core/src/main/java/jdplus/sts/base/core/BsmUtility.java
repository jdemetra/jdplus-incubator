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
package jdplus.sts.base.core;

import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.polynomials.UnitRoots;
import jdplus.toolkit.base.core.ssf.sts.SeasonalComponent;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class BsmUtility {

    public ArimaModel cycle(BsmData bsm) {
        double cvar = bsm.getCycleVar();
        if (cvar < 0) {
            return ArimaModel.NULL;
        }
        double cperiod = bsm.getCycleLength(), cdump = bsm.getCycleDumpingFactor();
        double q = Math.PI * 2 / cperiod;
        double ccos = cdump * Math.cos(q);
        double[] ar = new double[]{1, -2 * ccos, cdump * cdump};
        if (cvar == 0) {
            return new ArimaModel(BackFilter.ofInternal(ar), BackFilter.ONE, BackFilter.ONE, 0);
        }
        double t = 1 + cdump * cdump;
        double[] ma = new double[]{t, -ccos};
        SymmetricFilter sma = SymmetricFilter.ofInternal(ma).times(cvar);
        return new ArimaModel(BackFilter.ofInternal(ar), BackFilter.ONE, sma);
    }

    public ArimaModel trend(BsmData bsm) {
        double lvar = bsm.getLevelVar(), svar = bsm.getSlopeVar();
        if (lvar < 0) // svar<0
        {
            return ArimaModel.NULL;
        }

        BackFilter D = BackFilter.D1, N = BackFilter.ONE;
        if (svar >= 0) {
            if (lvar == 0 && svar == 0) {
                return new ArimaModel(N, D.times(D), N, 0);
            } else if (lvar == 0) {
                return new ArimaModel(N, D.times(D), N, svar);
            } else if (svar == 0) {
                return new ArimaModel(N, D.times(D), D, lvar);
            } else {
                ArimaModel ml = new ArimaModel(N, N, D, lvar);
                ml = ml.plus(svar);
                return new ArimaModel(N, D.times(D), ml.symmetricMa());
            }
        } else {
            return new ArimaModel(N, D, N, lvar);
        }
    }

    public ArimaModel noise(BsmData bsm) {
        double nvar = bsm.getNoiseVar();
        if (nvar > 0) {
            return new ArimaModel(BackFilter.ONE, BackFilter.ONE, BackFilter.ONE, nvar);
        } else {
            return ArimaModel.NULL;
        }
    }

    public ArimaModel seasonal(BsmData bsm) {
        double seasvar = bsm.getSeasonalVar();
        int period = bsm.getPeriod();
        SeasonalModel seasModel = bsm.getSeasonalModel();
        if (seasvar < 0) {
            return ArimaModel.NULL;
        }
        BackFilter S = new BackFilter(UnitRoots.S(period, 1));
        if (seasvar > 0) {
            SymmetricFilter sma;
            if (seasModel != SeasonalModel.Dummy) {
                // ma is the first row of the v/c innovations
                FastMatrix O = SeasonalComponent.tsVar(seasModel, period);
                double[] w = new double[period - 1];
                for (int i = 0; i < period - 1; ++i) {
                    for (int j = i; j < period - 1; ++j) {
                        FastMatrix s = O.extract(0, 1 + j - i, 0, 1 + j);
                        w[i] += s.sum();
                    }
                }
                sma = SymmetricFilter.ofInternal(w);
            } else {
                sma = SymmetricFilter.ONE;
            }
            return new ArimaModel(BackFilter.ONE, S, sma.times(seasvar));
        } else {
            return new ArimaModel(BackFilter.ONE, S, BackFilter.ONE, 0);

        }
    }

    public UcarimaModel ucm(BsmData bsm, boolean normalized) {
        ArimaModel t = trend(bsm), c = cycle(bsm), s = seasonal(bsm), n = noise(bsm), tc = t.plus(c);
        UcarimaModel ucm = UcarimaModel.builder()
                .add(tc)
                .add(s)
                .add(n)
                .build();

        if (normalized) {
            ucm = ucm.normalize();
        }
        return ucm;
    }

}
