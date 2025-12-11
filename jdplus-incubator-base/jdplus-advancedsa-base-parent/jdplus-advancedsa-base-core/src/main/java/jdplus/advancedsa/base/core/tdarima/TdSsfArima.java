/*
 * Copyright 2024 JDemetra+.
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
package jdplus.advancedsa.base.core.tdarima;

import java.util.function.IntFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.IntegratedDynamics;
import jdplus.toolkit.base.core.ssf.basic.IntegratedInitialization;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TdSsfArima {

    public Ssf ssf(int n, IntFunction<IArimaModel> fn) {

        return Ssf.of(stateComponent(n, fn), loading(fn));
    }

    public StateComponent stateComponent(int n, IntFunction<IArimaModel> fn) {

        IArimaModel m0 = fn.apply(0);

        if (m0.isStationary()) {
            return TdSsfArma.stateComponent(n, fn);
        } else {
            StateComponent stcmp = TdSsfArma.stateComponent(n, i -> (IArimaModel) fn.apply(i).stationaryTransformation().getStationaryModel());
            ISsfLoading loading = Loading.fromPosition(0);
            DoubleSeq d = m0.getNonStationaryAr().coefficients().drop(1, 0);
            IntegratedDynamics idyn = new IntegratedDynamics(stcmp.dynamics(), loading, d);
            IntegratedInitialization iinit = new IntegratedInitialization(stcmp.initialization(), d);
            return new StateComponent(iinit, idyn);
        }
    }

    public ISsfLoading loading(IntFunction<IArimaModel> fn) {
        IArimaModel m0 = fn.apply(0);

        if (m0.isStationary()) {
            return Loading.fromPosition(0);
        } else {
            DoubleSeq d = m0.getNonStationaryAr().coefficients().drop(1, 0);
            double[] w = new double[d.count(x -> x != 0) + 1];
            int[] pos = new int[w.length];
            DoubleSeqCursor cursor = d.cursor();
            for (int i = 0, j = 0; i < d.length(); ++i) {
                double cur = cursor.getAndNext();
                if (cur != 0) {
                    w[j] = -cur;
                    pos[j++]=i;
                }
            }
            w[w.length-1]=1;
            pos[pos.length-1]=d.length();
            return Loading.from(pos, w);
        }
    }

}
