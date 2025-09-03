/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package jdplus.advancedsa.base.r;

import jdplus.advancedsa.base.core.tdarima.LtdArimaResults;
import jdplus.toolkit.base.api.timeseries.TsData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingArimaModelsTest {

    public TimeVaryingArimaModelsTest() {
    }

    @Test
    public void testAirline() {
        TsData[] s = Data.retail_us();
        int[] regular = new int[]{0, 1, 1};
        int[] seasonal = new int[]{0, 1, 1};
        for (int i = 0; i < s.length; ++i) {
           
            LtdArimaResults result = TimeVaryingArimaModels.estimate(s[i].log().getValues().toArray(), s[i].getAnnualFrequency(), true, null, regular, seasonal, true, true, false, false, false, 1e-7);
            assertTrue(result != null);
            System.out.print(result.getData("ll0.ll", Double.class));
            System.out.print('\t');
            System.out.print(result.getData("ll1.ll", Double.class));
            System.out.print('\t');
            System.out.println(result.getModel().getVar1());
        }
    }

}
