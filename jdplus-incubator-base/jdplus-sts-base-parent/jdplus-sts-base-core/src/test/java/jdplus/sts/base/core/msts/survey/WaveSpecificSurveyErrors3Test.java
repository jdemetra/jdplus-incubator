/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts.survey;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.StateComponent;
import java.util.Random;
import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class WaveSpecificSurveyErrors3Test {
    
    public WaveSpecificSurveyErrors3Test() {
    }

    @Test
    public void testLag3() {
        Matrix M=Matrix.of(new double[]{1,1,1,1,1}, 1, 5);
        StateComponent cmp = WaveSpecificSurveyErrors3.of(new double[]{1,2,3,4,5}, new double[]{.2,.3,.4,.5}, M, 3);
        int dim=cmp.initialization().getStateDim();
        ISsfDynamics dyn = cmp.dynamics();
        FastMatrix T=FastMatrix.square(dim);
        dyn.T(0, T);
        DataBlock x = DataBlock.make(dim);
        Random rnd = new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = DataBlock.make(dim);
        y.product(T.rowsIterator(), x);
        dyn.TX(0, x);
        assertTrue(y.distance(x) < 1e-9);
        
        y.set(0);
        y.product(T.columnsIterator(), x);
        dyn.XT(0, x);
        assertTrue(y.distance(x) < 1e-9);
        
    }
    
    @Test
    public void testLag1() {
        Matrix M=Matrix.of(new double[]{1,1,1,1,1}, 1, 5);
        StateComponent cmp = WaveSpecificSurveyErrors3.of(new double[]{1,2,3,4,5}, new double[]{.2,.3,.4,.5}, M, 1);
        int dim=cmp.initialization().getStateDim();
        ISsfDynamics dyn = cmp.dynamics();
        FastMatrix T=FastMatrix.square(dim);
        dyn.T(0, T);
        DataBlock x = DataBlock.make(dim);
        Random rnd = new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = DataBlock.make(dim);
        y.product(T.rowsIterator(), x);
        dyn.TX(0, x);
        assertTrue(y.distance(x) < 1e-9);
        
        y.set(0);
        y.product(T.columnsIterator(), x);
        dyn.XT(0, x);
        assertTrue(y.distance(x) < 1e-9);
    }
}
