/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.core.msts.internal;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.StateComponent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class SarimaItemTest {
    
    public SarimaItemTest() {
    }

    @Test
    public void testDim() {
        SarimaItem item = new SarimaItem("", 6, new int[]{2,1,2}, new int[]{0,1,1}, null, false, 1, true);
        int np = item.parametersCount();
        DataBlock p = DataBlock.make(np);
        p.set(i->i == 0 ? 1 : .011*(i-1));

        StateComponent s = item.build(p);
        int dim = s.dim();
        assertTrue(dim == item.stateDim());
    }

}
