/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters.base.r;

import jdplus.filters.base.core.AdvancedFiltersToolkit;
import jdplus.toolkit.base.core.math.linearfilters.FiltersToolkit;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class AdvancedFiltersToolkitTest {
    
    public AdvancedFiltersToolkitTest() {
    }

    @Test
    public void testFST() {
        AdvancedFiltersToolkit.FSTResult rslt = AdvancedFiltersToolkit.fstfilter(12, 0, 2, 0.001, 3, 0.999, Math.PI/6, true);
        FiniteFilter filter = rslt.getFilter();
        AdvancedFiltersToolkit.FSTResult fst=AdvancedFiltersToolkit.fst(filter.weightsToArray(), -12, Math.PI/6);
        
        assertEquals(fst.getCriterions()[0], rslt.getCriterions()[0], 1e-6);
        assertEquals(fst.getCriterions()[1], rslt.getCriterions()[1], 1e-6);
        assertEquals(fst.getCriterions()[2], rslt.getCriterions()[2], 1e-6);
//        System.out.println(DoubleSeq.of(rslt.getRslt().getFilter().weightsToArray()));
    }
    
}
