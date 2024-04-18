/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl.base.r;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.WeightFunction;

import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StlDecompositionTest {

    public StlDecompositionTest() {
    }

    @Test
    public void testFilter() {
        assertTrue(null != StlDecomposition.loess(Data.ABS_RETAIL, 13, 1, 1));
    }

    @Test
    public void testSTL20() {
        Matrix decomp = StlDecomposition.stl(Data.ABS_RETAIL, 12, false, 0, 0, 0,
                1, 0, 1, 0, 0, 0, 2, 0, 0.1, WeightFunction.TRICUBE.name(), false);
        System.out.println(decomp);
        assertTrue(null != decomp);
    }

    @Test
    public void testStl() {
        Matrix decomp = StlDecomposition.stl(Data.ABS_RETAIL, 12, false, 0, 0, 0, 1, 0, 1, 0, 0, 0, 2, 0, 0.1, WeightFunction.TRICUBE.name(), false);
//        System.out.println(decomp);
        assertTrue(null != decomp);
    }

    @Test
    public void testStl_legacy() {
        Matrix decomp = StlDecomposition.stl(Data.ABS_RETAIL, 12, false, 0, 0, 0, 1, 0, 1, 0, 0, 0, 2, 0, 0.1, WeightFunction.TRICUBE.name(), true);
//        System.out.println(decomp);
        assertTrue(null != decomp);
    }

    @Test
    public void testMStl() {
        Matrix decomp = StlDecomposition.mstl(Data.ABS_RETAIL, new int[]{12}, false, null, 0, 2, 5, true, 0.1, WeightFunction.EPANECHNIKOV.name());
        //       System.out.println(decomp);
        assertTrue(null != decomp);
    }

    @Test
    public void testIStl() {
        Matrix decomp = StlDecomposition.istl(Data.ABS_RETAIL, new int[]{12}, false, null, null, 2, 5, true, 0.1, WeightFunction.TRIANGULAR.name());
        //       System.out.println(decomp);
        assertTrue(null != decomp);
    }
}
