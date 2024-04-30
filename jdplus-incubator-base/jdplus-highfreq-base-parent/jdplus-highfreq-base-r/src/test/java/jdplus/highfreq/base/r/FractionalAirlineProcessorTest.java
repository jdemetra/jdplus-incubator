/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.base.r;

import java.util.Arrays;
import jdplus.toolkit.base.api.data.DoubleSeq;
import tck.demetra.data.WeeklyData;
import jdplus.highfreq.base.core.extendedairline.decomposition.LightExtendedAirlineDecomposition;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineEstimation;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.highfreq.base.core.ssf.extractors.SsfUcarimaEstimation;
import org.junit.Assert;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FractionalAirlineProcessorTest {

    public FractionalAirlineProcessorTest() {
    }

    private final double[] OBS = {2.52, 1.43, 0.77, 3.19, 1.6, 0.35, 0.95, 1.69, 1.91, 1.46, 1.21, 2.48, 1.35, 0.6, 1.09, 1.73, 0.58, 2.56, 1.48, 0.36, 0.12, 1.6, 0.87, 1.31, 2.19, 1.46, 0.45, 2.43, 2.98, 11.93, 0.08, 2.42, 2.99, 0.44, 0.36, 3.83, 0.44, 1.19, 3.25, 2.65, 2.86, 1.18, 0.92, 2.06, 1.28, 2.6, 1.82, 0.53, 1.2, 0.76};
    private final double[] OBS_minus1 = {2.52, 1.43, 0.77, 3.19, 1.6, 0.35, 0.95, 1.69, 1.91, 1.46, 1.21, 2.48, 1.35, 0.6, 1.09, 1.73, 0.58, 2.56, 1.48, 0.36, 0.12, 1.6, 0.87, 1.31, 2.19, 1.46, 0.45, 2.43, 2.98, 11.93, 0.08, 2.42, 2.99, 0.44, 0.36, 3.83, 0.44, 1.19, 3.25, 2.65, 2.86, 1.18, 0.92, 2.06, 1.28, 2.6, 1.82, 0.53, 1.2};
    private final double[] logOBS = DoubleSeq.of(OBS).log().toArray();
    private final double[] logOBS_minus1 = DoubleSeq.of(OBS_minus1).log().toArray();

    private final double[] Reg = {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0};
    private final Matrix Matrix_Reg = Matrix.of(Reg, Reg.length, 1);

    @Test
    public void EstimationLogAO() {

        double[] OBS_A = {2.52, 1.43, 0.77, 3.19, 1.6, 1000000, 0.95, 1.69, 1.91, 1.46, 1.21, 2.48, 1.35, 0.6, 1.09, 1.73, 0.58, 2.56, 1.48, 0.36, 0.12, 1.6, 0.87, 1.31, 2.19, 1.46, 0.45, 2.43, 2.98, 11.93, 0.08, 2.42, 2.99, 0.44, 0.36, 3.83, 0.44, 1.19, 3.25, 2.65, 2.86, 1.18, 0.92, 2.06, 1.28, 2.6, 1.82, 0.53, 1.2, 0.76};
        double[] logOBS_A = DoubleSeq.of(OBS_A).log().toArray();
        ExtendedAirlineEstimation rslt_level_logOBS = FractionalAirlineProcessor.estimate(logOBS_A, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 3, 1e-12, false, 0, false);
        ExtendedAirlineEstimation rslt_log_OBS = FractionalAirlineProcessor.estimate(OBS_A, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 6, 1e-12, false, 0, true);
        assertEquals(rslt_level_logOBS.getOutliers().length, rslt_log_OBS.getOutliers().length, "Differnce in Number of outliers");
        assertEquals(Arrays.toString(rslt_level_logOBS.getOutliers()), Arrays.toString(rslt_log_OBS.getOutliers()), "Difference in Outlier");
    }

    @Test
    public void EstimationLogMissing() {

        double[] OBS_Na = {2.52, 1.43, 0.77, 3.19, 1.6, Double.NaN, 0.95, 1.69, 1.91, 1.46, 1.21, 2.48, 1.35, 0.6, 1.09, 1.73, 0.58, 2.56, 1.48, 0.36, 0.12, 1.6, 0.87, 1.31, 2.19, 1.46, 0.45, 2.43, 2.98, 11.93, 0.08, 2.42, 2.99, 0.44, 0.36, 3.83, 0.44, 1.19, 3.25, 2.65, 2.86, 1.18, 0.92, 2.06, 1.28, 2.6, 1.82, 0.53, 1.2, 0.76};
        double[] logOBS_NA = DoubleSeq.of(OBS_Na).log().toArray();
        ExtendedAirlineEstimation rslt_level_logOBS = FractionalAirlineProcessor.estimate(logOBS_NA, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 3, 1e-12, false, 0, false);
        ExtendedAirlineEstimation rslt_log_OBS = FractionalAirlineProcessor.estimate(OBS_Na, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 6, 1e-12, false, 0, true);
        assertEquals(rslt_level_logOBS.getMissing().length, rslt_log_OBS.getMissing().length, "Differnce in Number of Missing");
        Assert.assertArrayEquals("Difference in Missing", rslt_level_logOBS.getMissing(), rslt_log_OBS.getMissing());
        int[] arr = {5};
        Assert.assertArrayEquals("Difference in Missing", rslt_level_logOBS.getMissing(), arr);
    }

    @Test
    public void EstimationLog() {

        ExtendedAirlineEstimation rslt_level_logOBS = FractionalAirlineProcessor.estimate(logOBS, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 6, 1e-12, false, 0, false);
        ExtendedAirlineEstimation rslt_log_OBS = FractionalAirlineProcessor.estimate(OBS, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 6, 1e-12, false, 0, true);

        assertFalse(rslt_level_logOBS.isLog(), "Logs are taken");
        assertTrue(rslt_log_OBS.isLog(), "Logs are not taken");
        assertEquals(rslt_level_logOBS.getMissing().length, 0, "Wrong number of missing");
        assertEquals(rslt_log_OBS.getMissing().length, 0, "Wrong number of missing");

        assertArrayEquals(rslt_level_logOBS.getMissing(), rslt_log_OBS.getMissing(), "Different Missing values");
        assertEquals(rslt_level_logOBS.getOutliers().length, rslt_log_OBS.getOutliers().length, "Number of outliers");

        //the original Seris
        assertArrayEquals(rslt_level_logOBS.getY(), logOBS, "Difference in Original Series");
        assertArrayEquals(rslt_log_OBS.getY(), OBS, 0.0000000001, "Difference in Original Series");
        assertArrayEquals(rslt_level_logOBS.linearized(), rslt_log_OBS.linearized(), "Difference in linarized Series"); //Not back Transformed

        assertArrayEquals(rslt_level_logOBS.component_userdef_reg_variables(), DoubleSeq.of(rslt_log_OBS.component_userdef_reg_variables()).log().toArray(), 0.000000001, "Difference in User-Defined Reg Variable");

        assertArrayEquals(rslt_level_logOBS.component_ao(), DoubleSeq.of(rslt_log_OBS.component_ao()).log().toArray(), 0.000000001, "Difference in Compnent AO");
        assertArrayEquals(rslt_level_logOBS.component_ls(), DoubleSeq.of(rslt_log_OBS.component_ls()).log().toArray(), 0.000000001, "Difference in Compnent LS");
        assertArrayEquals(rslt_level_logOBS.component_outliers(), DoubleSeq.of(rslt_log_OBS.component_outliers()).log().toArray(), 0.000000001, "Difference in Compnent LS");
        assertArrayEquals(rslt_level_logOBS.component_wo(), DoubleSeq.of(rslt_log_OBS.component_wo()).log().toArray(), 0.000000001, "Difference in Compnent WO");

        assertArrayEquals(rslt_level_logOBS.getCoefficients().toArray(), rslt_log_OBS.getCoefficients().toArray(), "Difference in Coefficents");
        assertArrayEquals(rslt_level_logOBS.getCoefficientsCovariance().toArray(), rslt_log_OBS.getCoefficientsCovariance().toArray(), "Difference in CoefficentsCovariance");
        assertArrayEquals(rslt_level_logOBS.tstats(), rslt_log_OBS.tstats(), "Difference in TSTat");
        assertEquals(rslt_level_logOBS.getLikelihood().getAIC(), rslt_log_OBS.getLikelihood().getAIC(), "Difference in AIC");
        assertEquals(rslt_level_logOBS.getLikelihood().getAICC(), rslt_log_OBS.getLikelihood().getAICC(), "Difference in AICC");
        assertEquals(rslt_level_logOBS.getLikelihood().getAdjustedLogLikelihood(), rslt_log_OBS.getLikelihood().getAdjustedLogLikelihood(), "Difference in Adjusted Likelihood");
        assertEquals(rslt_level_logOBS.getLikelihood().getBIC(), rslt_log_OBS.getLikelihood().getBIC(), "Difference in BIC");
        assertEquals(rslt_level_logOBS.getLikelihood().getBIC2(), rslt_log_OBS.getLikelihood().getBIC2(), "Difference in BIC2");
        assertEquals(rslt_level_logOBS.getLikelihood().getBICC(), rslt_log_OBS.getLikelihood().getBICC(), "Difference in BICC");
        assertEquals(rslt_level_logOBS.getLikelihood().getEffectiveObservationsCount(), rslt_log_OBS.getLikelihood().getEffectiveObservationsCount(), "Difference active Obersvations Count");
        assertEquals(rslt_level_logOBS.getLikelihood().getEstimatedParametersCount(), rslt_log_OBS.getLikelihood().getEstimatedParametersCount(), "Difference in Estimated Parameters Count");
        assertEquals(rslt_level_logOBS.getLikelihood().getHannanQuinn(), rslt_log_OBS.getLikelihood().getHannanQuinn(), "Difference in HannanQuinn");
        assertEquals(rslt_level_logOBS.getLikelihood().getObservationsCount(), rslt_log_OBS.getLikelihood().getObservationsCount(), "Difference in Observations Count");
        assertEquals(rslt_level_logOBS.getLikelihood().getSsqErr(), rslt_log_OBS.getLikelihood().getSsqErr(), "Difference in SsqErr");
        assertEquals(rslt_level_logOBS.getLikelihood().getTransformationAdjustment(), rslt_log_OBS.getLikelihood().getTransformationAdjustment(), "Difference in Transformation Adjustment");
    }

    @Test
    public void EstimationLogFcast() {

        ExtendedAirlineEstimation rslt_level_logOBS = FractionalAirlineProcessor.estimate(logOBS_minus1, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 6, 1e-12, false, 1, false);
        ExtendedAirlineEstimation rslt_log_OBS = FractionalAirlineProcessor.estimate(OBS_minus1, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 6, 1e-12, false, 1, true);
        assertEquals(rslt_level_logOBS.getOutliers().length, rslt_log_OBS.getOutliers().length, "Number of outliers");

        assertArrayEquals(rslt_level_logOBS.getY(), DoubleSeq.of(rslt_log_OBS.getY()).log().toArray(),0.00000001, "Difference in Original Series inkl. one fcast");
        assertArrayEquals(rslt_level_logOBS.linearized(), rslt_log_OBS.linearized(),0.00000001, "Difference in Original Series inkl. one fcast");

    }

    @Test
    public void EstimationInDetail() {
        double[] OBS = {2.52, -1.43, -0.77, -3.19, -1.6, 0.35, 0.95, 1.69, 1.91, 1.46, -1.21, -2.48, 1.35, 0.6, 1.09, 1.73, -0.58, -2.56, -1.48, 0.36, 0.12, 1.6, 0.87, -1.31, -2.19, -1.46, -0.45, 2.43, 2.98, 11.93, 0.08, -2.42, -2.99, -0.44, 0.36, 3.83, -0.44, -1.19, -3.25, -2.65, -2.86, -1.18, 0.92, -2.06, -1.28, -2.6, -1.82, 0.53, -1.2, -0.76};

        double[] Reg = {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0};

        Matrix Matrix_Reg = Matrix.of(Reg, Reg.length, 1);
        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(OBS, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 6, 1e-12, false, 0);
        //  System.out.println(rslt.getLikelihood());

    }

    @Test
    public void EstimationInDetail_fcast() {
        double[] OBS = {2.52, -1.43, -0.77, -3.19, -1.6, 0.35, 0.95, 1.69, 1.91, 1.46, -1.21, -2.48, 1.35, 0.6, 1.09, 1.73, -0.58, -2.56, -1.48, 0.36, 0.12, 1.6, 0.87, -1.31, -2.19, -1.46, -0.45, 2.43, 2.98, 11.93, 0.08, -2.42, -2.99, -0.44, 0.36, 3.83, -0.44, -1.19, -3.25, -2.65, -2.86, -1.18, 0.92, -2.06, -1.28, -2.6, -1.82, 0.53, -1.2, -0.76};

        double[] Reg = {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0};

        Matrix Matrix_Reg = Matrix.of(Reg, Reg.length, 1);
        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(OBS, Matrix_Reg, false, new double[]{7}, 1, false, new String[]{"ao"}, 6, 1e-12, false, 1);
        assertEquals(OBS.length + 1, rslt.getY().length, "Forcast has the wrong length");

    }

    @Test
    public void testWeeklyDecomp() {
        DoubleSeq y = DoubleSeq.of(WeeklyData.US_CLAIMS2).log();
        LightExtendedAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(y.toArray(), 365.25 / 7, false, true, 0, 0);
//        System.out.println(rslt.component("t").getData());
//        System.out.println(rslt.component("s").getData());
//        System.out.println(rslt.component("i").getData());
//        System.out.println(rslt.component("t").getStde());
//        System.out.println(rslt.component("s").getStde());
//        System.out.println(rslt.component("i").getStde());
        assertTrue(null != rslt.getData("sa", double[].class
        ));
    }

    @Test
    public void testWeeklyEstimation_mini() {
        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, null, false, new double[]{52},
                2, false, null, 6, 1e-12, false, 0);

//        System.out.println();
    }

    @Test
    public void testWeeklyEstimation() {
        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, null, false, new double[]{52},
                -1, false, new String[]{"ao", "ls", "wo"}, 5, 1e-12, true, 0);
//        System.out.println(rslt.getLikelihood());
//        System.out.println();
    }

    private static final double[] REGRES = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, -0.875, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, -0.875, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, -0.875, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 1.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, -0.875, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0.125, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Test
    public void testComponentEstimation_with_fcast_withReg_comparisonR() {
        double[] rdata = new double[]{150.4821, 167.4522, 185.8829, 141.7225, 139.2304, 155.1260, 148.2967, 153.3347, 158.0910, 162.9566, 161.1472, 158.4820};
        double[] data = new double[]{33.2, 31.6, 39.7, 34.3, 36.5, 37, 37.8, 36.9, 35.8, 35.7, 40.5, 58.3, 36.5, 34, 42, 40, 40.2, 39.6, 40.5, 41, 39.2, 39.5, 42, 60.3, 39.4, 39.4, 42.5, 42, 42.7, 40.5, 43.4, 41.6, 38.8, 42.8, 44.5, 61.4, 41.4, 39.3, 43.6, 44.2, 44, 43.2, 46.5, 44.4, 44.4, 46, 45.9, 67.8, 43.5, 40.6, 45.5, 46.8, 47.7, 46.2, 50.9, 48.6, 48, 49.6, 54.9, 77.2, 51.3, 47.9, 56.8, 51.6, 53.6, 55.7, 58.2, 54.6, 55.2, 55.4, 61, 83.8, 54.8, 54.3, 59.2, 58.1, 67.9, 59.8, 60.7, 62.6, 59.9, 60.8, 67.5, 88.1, 63.6, 57.5, 65.6, 64, 70.3, 66.5, 68.8, 70, 63.3, 68.7, 73.8, 93.2, 64.9, 61, 70.1, 69.5, 71.6, 68.3, 74.3, 72, 68.4, 69.6, 73.7, 94.8, 68.6, 65, 69.1, 73.1, 74.1, 69.7, 76.4, 70.2, 72.1, 73.1, 72.4, 95.6, 69.1, 65.4, 75.3, 75.6, 71.6, 75.5, 77.7, 73, 72.6, 74.6, 79.1, 103.4, 69.2, 66.7, 76.1, 74.2, 75.8, 75.9, 77.1, 77.1, 74.6, 73.3, 80, 105.5, 73.2, 69.2, 81.1, 77.9, 81.1, 79.3, 83.3, 84.2, 80.5, 80.4, 87.8, 110.8, 78.3, 74, 85.6, 81.4, 85.6, 85.2, 85.3, 86.6, 80.4, 85.8, 90.8, 110.9, 81.9, 80.6, 85.3, 86.5, 86.8, 84.2, 89.3, 86, 84.6, 87, 93.6, 118.5, 82.7, 76.8, 84.7, 87.3, 87.2, 85.5, 92.4, 88.1, 85.4, 90.7, 99.2, 127.1, 84.7, 85.8, 98.6, 92.4, 92.4, 93.3, 97.8, 93.2, 89.4, 92.4, 102.1, 129.8, 88.1, 83.9, 95.5, 95.2, 97.6, 92.7, 96, 97.8, 94.7, 91.8, 100.7, 130.8, 87.9, 82.6, 98.8, 86.3, 90.6, 93.5, 81.9, 84.1, 75, 79.8, 86.3, 111.5, 76.7, 73.2, 87.3, 80, 82.5, 79.9, 88.5, 86.6, 78.2, 85.5, 93.2, 119.5, 84.5, 81.2, 88.2, 90.4, 92.7, 86.8, 95, 91.3, 90.4, 95.4, 100.3, 130.9, 93.7, 86, 98.1, 95.3, 95, 97.7, 101.8, 96.2, 95.6, 100.5, 104.3, 135.8, 92.7, 93.5, 103.7, 100.9, 100.6, 101.7, 104.3, 104.6, 102.9, 104.1, 111.9, 141.5, 96.2, 93.5, 106.1, 106.3, 108, 104.5, 107.7, 107.6, 104.8, 103, 118.3, 144.8, 101.4, 96.4, 114.9, 105.1, 105, 110.1, 110, 111.7, 103.4, 111.2, 121.5, 146.8, 105.2, 101.1, 109.4, 113.5, 112.3, 109.5, 118.6, 116.5, 107.4, 115.6, 129.8, 150.2, 111.1, 112.1, 115.5, 92.4, 102.4, 107.3, 116.8, 113.6, 111.1, 122.4, 125.6, 147.2, 101.5, 100.2, 118.3, 106.6, 112.6, 118.1, 125.3, 119.6, 115.5, 122.6, 139.3, 158.1, 112.3, 109.2, 127.9, 125.7, 126.6, 137.4, 145, 145.1, 140.9, 135.3, 161.8, 173.9, 129, 126.1, 144.5, 138.5, 146.9, 149.9, 150.9, 149.4, 150};
        int nforecast = 12;
        Matrix x = Matrix.of(REGRES, data.length + nforecast, 1);

        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(data, x, false, new double[]{12},
                -1, false, null, 6, 1e-12, false, nforecast);
             
        for (int i = 0; i < (rslt.getY().length - data.length); i++) {
            boolean ident = Math.abs(Math.round(10000*rslt.getY()[i + data.length])/10000.0 - rdata[i]) > 0.00000001;
            assertFalse(ident, "The forecasts do not match, for " + i);
        }

        assertTrue(true);
    }

    @Test
    public void testComponentEstimation_with_fcast_withReg() {
        int n = WeeklyData.US_CLAIMS2.length;
        double[] data = new double[2 * (WeeklyData.US_CLAIMS2.length + 7)];
        for (int i = 0; i < WeeklyData.US_CLAIMS2.length; i++) {
            data[i] = 0; //1
            data[i + n + 7] = 0;//3;
        }

        data[1] = 1;
        data[WeeklyData.US_CLAIMS2.length + 7] = 1;
        for (int i = WeeklyData.US_CLAIMS2.length; i < WeeklyData.US_CLAIMS2.length + 7; i++) {
            data[i] = 2;
            data[i + n + 7] = 4;
        }

        Matrix x = Matrix.of(data, WeeklyData.US_CLAIMS2.length + 7, 2);

        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, x, false, new double[]{365.25 / 7},
                -1, false, new String[]{"ao", "wo", "ls"}, 5, 1e-12, true, 7);

        for (int i = 0; i < rslt.component_ls().length; i++) {
            boolean comp_out = Math.abs(rslt.component_ls()[i] + rslt.component_ao()[i] + rslt.component_wo()[i] - rslt.component_outliers()[i]) > 0.00000001;
            assertFalse(comp_out, "The outlier components don't sum up to the outliers, for " + i);
        }

        for (int i = 0; i < WeeklyData.US_CLAIMS2.length; i++) {
            boolean comp = Math.abs(WeeklyData.US_CLAIMS2[i] - rslt.component_outliers()[i] - rslt.component_userdef_reg_variables()[i] - rslt.linearized()[i]) > 0.00000001;
            assertFalse(comp, "The componets don't sum up to the lin " + i);
        }

        //   System.out.println("LL: " + rslt.getLikelihood());
    }

    @Test
    public void testComponentEstimation_with_fcast_withoutReg() {

        ExtendedAirlineEstimation rslt = FractionalAirlineProcessor.estimate(WeeklyData.US_CLAIMS2, null, false, new double[]{365.25 / 7},
                -1, false, new String[]{"ao", "wo", "ls"}, 5, 1e-12, true, 7);

        for (int i = 0; i < rslt.component_ls().length; i++) {
            boolean comp_out = Math.abs(rslt.component_ls()[i] + rslt.component_ao()[i] + rslt.component_wo()[i] - rslt.component_outliers()[i]) > 0.00000001;
            assertFalse(comp_out, "The outlier components don't sum up to the outliers, for " + i);
        }

        for (int i = 0; i < WeeklyData.US_CLAIMS2.length; i++) {
            boolean comp = Math.abs(WeeklyData.US_CLAIMS2[i] - rslt.component_outliers()[i] - rslt.component_userdef_reg_variables()[i] - rslt.linearized()[i]) > 0.00000001;
            assertFalse(comp, "The components don't sum up to the lin " + i);
        }

        //    System.out.println("LL: " + rslt.getLikelihood());
    }

    @Test
    public void testWeeklySsf() {
        LightExtendedAirlineDecomposition rslt = FractionalAirlineProcessor.decompose(WeeklyData.US_CLAIMS2, new double[]{365.25 / 7}, -1, false, true, 7, 7);
        SsfUcarimaEstimation details = FractionalAirlineProcessor.ssfDetails(rslt);
        assertTrue(null != details.getData("smoothing.states", Matrix.class
        ));
    }

//    final static DoubleSeq EDF;
//
//    static {
//        DoubleSeq y;
//        try {
//            InputStream stream = ExtendedAirlineMapping.class.getResourceAsStream("/edf.txt");
//            Matrix edf = MatrixSerializer.read(stream);
//            y = edf.column(0);
//        } catch (IOException ex) {
//            y = null;
//        }
//        EDF = y;
//    }
//
//    @Test
//    public void testRandom() {
//        DoubleSeq y = EDF;
//        double[] rnd = FractionalAirlineProcessor.random(new double[]{7, 365.25}, .1, new double[]{.7, .85}, false, 2000, y.range(0,374).log().toArray(), .01, 0);
//        System.out.println(DoubleSeq.of(rnd));
//    }
//
}
