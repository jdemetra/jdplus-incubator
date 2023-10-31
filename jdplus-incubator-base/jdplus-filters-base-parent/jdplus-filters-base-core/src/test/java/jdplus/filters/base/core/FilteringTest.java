/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters.base.core;

import jdplus.toolkit.base.core.math.linearfilters.LocalPolynomialFilters;
import jdplus.toolkit.base.core.math.linearfilters.Filtering;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearfilters.IFiltering;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author PALATEJ
 */
public class FilteringTest {

    public FilteringTest() {
    }

    @Test
    public void testSymmetric() {
        IFiltering lf = LocalPolynomialFilters.of(LocalPolynomialFilterSpec.DEF_TREND_SPEC);

        DoubleSeq s = DoubleSeq.of(Data.NILE);

        DoubleSeq lout = lf.process(s);
        IFiniteFilter cf = lf.centralFilter();

        double[] cw = cf.weightsToArray();
        FastMatrix M = FastMatrix.make(cw.length - 1, cf.getUpperBound());
        IFiniteFilter[] af = lf.leftEndPointsFilters();
        for (int i = 0; i < af.length; ++i) {
            M.column(i).drop(0, i).copyFrom(af[i].weightsToArray(), 0);
        }
//        System.out.println(M);
        FastMatrix N = FastMatrix.make(cw.length - 1, cf.getUpperBound());
        IFiniteFilter[] bf = lf.rightEndPointsFilters();
        for (int i = 0; i < bf.length; ++i) {
            N.column(i).drop(i, 0).copyFrom(bf[i].weightsToArray(), 0);
        }
        Filtering F = Filtering.of(DoubleSeq.of(cw), N);
        DoubleSeq fout = F.process(s);
        assertTrue(lout.distance(fout) < 1e-9);
        Filtering G = Filtering.of(DoubleSeq.of(cw), M, N);
        DoubleSeq gout = G.process(s);
        assertTrue(lout.distance(gout) < 1e-9);
    }

}
