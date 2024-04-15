/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package jdplus.sts.base.core.splines;

import jdplus.sts.base.core.splines.SplineComponent;
import jdplus.sts.base.core.splines.RegularSpline;
import jdplus.sts.base.core.splines.SplineData;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.akf.SmoothingOutput;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.sts.LocalLinearTrend;
import jdplus.toolkit.base.core.ssf.sts.Noise;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author palat
 */
public class SplineComponentTest {
    
    public SplineComponentTest() {
    }
    
    @Test
    public void testMonthly() {
        double[] PROD = Data.PROD;
        RegularSpline rs=RegularSpline.of(12, DoubleSeq.of(0,3,6,9));
        SplineData sd=new SplineData(rs);
        CompositeSsf ssf = CompositeSsf.builder()
                .add(LocalLinearTrend.stateComponent(0.1, 0.1), LocalLinearTrend.defaultLoading())
                .add(Noise.of(1), Noise.defaultLoading())
                .add(SplineComponent.stateComponent(sd,3,0), SplineComponent.loading(sd, 0))
                .build();
        
        SmoothingOutput rslt = AkfToolkit.robustSmooth(ssf, new SsfData(PROD), true, true);
//        System.out.println(rslt.getSmoothing().getComponent(ssf.componentsPosition()[0]));
//        System.out.println(rslt.getSmoothing().getComponent(ssf.componentsPosition()[1]));
        
        ISsfLoading loading = SplineComponent.loading(sd, 0);
        for (int i=0; i<rslt.getSmoothing().size(); ++i){
            double z=loading.ZX(i, rslt.getSmoothing().a(i).extract(ssf.componentsPosition()[2], ssf.componentsDimension()[2]));
            System.out.print(z);
            System.out.print('\t');
        }
        
     }
}
