/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Jean Palate
 */
public class VarianceParameterTest {
    
    public VarianceParameterTest() {
    }

    @Test
    public void testStd() {
        
        VarianceInterpreter var=new VarianceInterpreter("v", 3, false, true);
        double stde = var.stde();
        var.variance(stde*stde);
        assertEquals(3, var.variance(), 1e-9);
    }
    
}
