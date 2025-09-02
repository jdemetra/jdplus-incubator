/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.advancedsa.base.r;


import jdplus.advancedsa.base.core.tdarima.LtdArimaResults;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.advancedsa.base.core.tdarima.TdAirlineDecomposer;
import jdplus.advancedsa.base.core.tdarima.TdSsfUcarima;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TimeVaryingArimaModels {

    public Matrix airlineDecomposition(double[] data, int period, double[] th, double[] bth, boolean se) {
        TdAirlineDecomposer decomposer = new TdAirlineDecomposer(period, th, bth);
        UcarimaModel[] ucarimaModels = decomposer.ucarimaModels();
        CompositeSsf ssf = TdSsfUcarima.of(data.length, i -> ucarimaModels[i]);
        int[] pos = ssf.componentsPosition();
        DefaultSmoothingResults sf = DkToolkit.smooth(ssf, new SsfData(data), se, true);
        FastMatrix rslt = FastMatrix.make(data.length, se ? 6 : 3);
        for (int i = 0; i < 3; ++i) {
            rslt.column(i).copy(sf.getComponent(pos[i]));
            if (se) {
                rslt.column(i + 3).copy(sf.getComponentVariance(pos[i]).sqrt());
            }
        }
        return rslt;
    }
    
    public LtdArimaResults estimate(double[] data, int period, boolean mean, Matrix X, int[] regular, int[] seasonal, 
            double[] phi, double[] bphi, double[] theta, double[] btheta, 
            double[] dphi, double[] dbphi, double[] dtheta, double[] dbtheta, 
            double dvar, double eps){
        return null;
    }
}
