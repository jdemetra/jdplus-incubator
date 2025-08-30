/*
 * Copyright 2025 JDemetra+.
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
package jdplus.advancedsa.base.core.tarima;

import java.util.function.IntFunction;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;

/**
 *
 * @author Jean Palate
 */
public class TdLoading implements ISsfLoading {

    private final ISsfLoading[] loadings;

    public TdLoading(int n, IntFunction<ISsfLoading> fn) {
        loadings = new ISsfLoading[n];
        for (int i = 0; i < n; ++i) {
            loadings[i] = fn.apply(i);
        }
    }

    @Override
    public void Z(int i, DataBlock db) {
        loadings[i].Z(i, db);
    }

    @Override
    public double ZX(int i, DataBlock db) {
        return loadings[i].ZX(i, db);
    }

    @Override
    public double ZVZ(int i, FastMatrix fm) {
        return loadings[i].ZVZ(i, fm);
    }

    @Override
    public void VpZdZ(int i, FastMatrix fm, double d) {
        loadings[i].VpZdZ(i, fm, d);
    }

    @Override
    public void XpZd(int i, DataBlock db, double d) {
        loadings[i].XpZd(i, db, d);
    }

    @Override
    public boolean isTimeInvariant() {
        return false;
    }

}
