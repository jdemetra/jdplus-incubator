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
package jdplus.sts.base.core.msts;

import jdplus.sts.base.core.msts.AtomicModels;
import jdplus.sts.base.core.msts.CompositeModel;
import jdplus.sts.base.core.msts.CompositeModelEstimation;
import jdplus.sts.base.core.msts.StateItem;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.functions.Optimizer;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingSsfArimaTest {
    
    double[] s = Data.RETAIL_ALLHOME;
    
    public TimeVaryingSsfArimaTest() {
    }

    @Test
    public void testAirline() {
        int len = s.length;
        CompositeModel model = new CompositeModel();
        StateItem l = AtomicModels.sarima("l", 12, new int[]{0, 1, 1}, new int[]{0, 1, 1}, new double[]{-.6, -.6}, false, 1, true);
        model.add(l);
        TsDomain domain = TsDomain.of(TsPeriod.monthly(1992, 1), len);
        model.add(AtomicModels.tdRegression("td", domain, new int[]{1, 2, 3, 4, 5, 6, 0}, true, 0, false));
        FastMatrix M = FastMatrix.make(len, 1);
        M.column(0).copyFrom(s, 0);
        CompositeModelEstimation rslt = model.estimate(M, false, true, SsfInitialization.SqrtDiffuse, Optimizer.LevenbergMarquardt, 1e-9, null);
        System.out.println(DoubleSeq.of(rslt.getParameters()));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }

    @Test
    public void testLtdAirline() {
        CompositeModel model = new CompositeModel();
        int len = s.length;
        StateItem l = AtomicModels.ltdAirline("l", len, 12, -.6, -.6, -.6, -.6, false, 1, true);
        model.add(l);
        TsDomain domain = TsDomain.of(TsPeriod.monthly(1992, 1), len);
        model.add(AtomicModels.tdRegression("td", domain, new int[]{1, 2, 3, 4, 5, 6, 0}, true, 0, false));
        FastMatrix M = FastMatrix.make(len, 1);
        M.column(0).copyFrom(s, 0);
        CompositeModelEstimation rslt = model.estimate(M, false, true, SsfInitialization.SqrtDiffuse, Optimizer.LevenbergMarquardt, 1e-9, null);
        System.out.println(DoubleSeq.of(rslt.getParameters()));
        System.out.println(rslt.getLikelihood().logLikelihood());
    }
    
}
