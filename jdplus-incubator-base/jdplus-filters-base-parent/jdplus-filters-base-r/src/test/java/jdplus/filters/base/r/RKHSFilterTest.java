package jdplus.filters.base.r;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;

/**
*
* @author Alain QLT
*/

public class RKHSFilterTest {
	public RKHSFilterTest() {
    }

    @Test
    public void testOptimalCriteria() {
    	DoubleUnaryOperator ocsym = RKHSFilters.optimalCriteria(6, 6, 2, "BiWeight", "Timeliness", true, Math.PI/8);


        assertEquals(0, ocsym.applyAsDouble(6), 1e-6);
        assertEquals(0, ocsym.applyAsDouble(7), 1e-6);
        assertEquals(0, ocsym.applyAsDouble(6 * 3), 1e-6);
    }
}
