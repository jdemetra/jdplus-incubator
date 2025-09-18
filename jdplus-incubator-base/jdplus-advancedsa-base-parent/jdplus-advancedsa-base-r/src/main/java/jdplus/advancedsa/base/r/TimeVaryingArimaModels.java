/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.advancedsa.base.r;

import java.util.Locale;
import jdplus.advancedsa.base.api.tdarima.LtdArimaSpec;
import jdplus.advancedsa.base.api.tdarima.LtdSarimaSpec;
import jdplus.advancedsa.base.core.tdarima.LtdArimaKernel;
import jdplus.advancedsa.base.core.tdarima.LtdArimaResults;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.advancedsa.base.core.tdarima.TdAirlineDecomposer;
import jdplus.advancedsa.base.core.tdarima.TdArimaDecomposer;
import jdplus.advancedsa.base.core.tdarima.TdSsfUcarima;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.SsfException;

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

    public Matrix arimaDecomposition(double[] data, int period, int[] regular, int[] seasonal, boolean tdvar, Matrix parameters, boolean se) {
        SarimaOrders orders = new SarimaOrders(period);
        orders.setP(regular[0]);
        orders.setRegular(regular[0], regular[1], regular[2]);
        if (period > 1 && seasonal != null) {
            orders.setSeasonal(seasonal[0], seasonal[1], seasonal[2]);
        }
        // we suppose that the parameters are correctly ordered (phi, bphi, th, bth)
        TdArimaDecomposer decomposer = new TdArimaDecomposer(period, data.length, i -> {
            DoubleSeq c = parameters.column(i);
            DoubleSeq p = tdvar ? c.drop(0, 1) : c;
            SarimaModel sarima = SarimaModel.builder(orders).parameters(p)
                    .build();
            if (!tdvar) {
                return sarima;
            } else {
                ArimaModel arima = ArimaModel.of(sarima);
                return arima.scaleVariance(c.get(p.length()));
            }
        });
        UcarimaModel[] ucarimaModels = decomposer.ucarimaModels();
        CompositeSsf ssf = TdSsfUcarima.of(data.length, i -> ucarimaModels[i]);
        int[] pos = ssf.componentsPosition();
        try {
            DefaultSmoothingResults sf = DkToolkit.smooth(ssf, new SsfData(data), se, true);
            FastMatrix rslt = FastMatrix.make(data.length, se ? 6 : 3);
            for (int i = 0; i < 3; ++i) {
                rslt.column(i).copy(sf.getComponent(pos[i]));
                if (se) {
                    rslt.column(i + 3).copy(sf.getComponentVariance(pos[i]).sqrt());
                }
            }
            return rslt;
        } catch (SsfException err) {
            return null;
        }
    }

    public LtdArimaResults estimate(double[] data, int period, boolean mean, Matrix X, int[] regular, int[] seasonal,
            boolean fphi, boolean fbphi, boolean ftheta, boolean fbtheta, boolean fvar,
            double eps, String param) {

        LtdArimaSpec.Parametrization pltd = LtdArimaSpec.Parametrization.valueOf(param.toUpperCase(Locale.ROOT));

        SarimaSpec sspec = SarimaSpec.builder()
                .period(period)
                .p(regular[0])
                .d(regular[1])
                .q(regular[2])
                .bp(period > 1 ? seasonal[0] : 0)
                .bd(period > 1 ? seasonal[1] : 0)
                .bq(period > 1 ? seasonal[2] : 0)
                .build();
        LtdArimaSpec spec = LtdArimaSpec.builder()
                .sarimaSpec(sspec)
                .vPhi(!fphi)
                .vBphi(!fbphi)
                .vTheta(!ftheta)
                .vBtheta(!fbtheta)
                .vVar(!fvar)
                .parametrization(pltd)
                .precision(eps)
                .build();

        LtdArimaKernel kernel = LtdArimaKernel.of(spec);
        return kernel.process(DoubleSeq.of(data), period, mean, FastMatrix.of(X));
    }

//    public LtdArimaResults estimate(double[] data, int period, Matrix X, int[] regular, int[] seasonal,
//            double[] phi, double[] bphi, double[] theta, double[] btheta,
//            double[] dphi, double[] dbphi, double[] dtheta, double[] dbtheta,
//            double dvar, 
//            boolean fphi, boolean fbphi, boolean ftheta, boolean fbtheta, boolean fvar,
//            double eps) {
//        
//        LtdSarimaSpec spec =specOf(period, regular, seasonal,
//            phi, bphi, theta, btheta,
//            dphi, dbphi, dtheta, dbtheta, dvar,
//            fphi, fbphi, ftheta, fbtheta, fvar);
//        
//        LtdArimaKernel kernel=LtdArimaKernel.of(spec);
//        return null;
//    }
    // use p and fphi or p and phi (dphi == null) or p and phi and dphi   
    public LtdSarimaSpec SarimaSpecOf(int period, int[] regular, int[] seasonal,
            double[] phi, double[] bphi, double[] theta, double[] btheta,
            double[] dphi, double[] dbphi, double[] dtheta, double[] dbtheta,
            double dvar, boolean fphi, boolean fbphi, boolean ftheta, boolean fbtheta, boolean fvar) {
        LtdSarimaSpec.Builder builder = LtdSarimaSpec.builder()
                .period(period);

        int p = regular[0];
        if (p > 0) {
            if (phi == null) {
                builder.p(p, fphi);
            } else if (dphi == null) {
                builder.phi(Parameter.of(phi, ParameterType.Initial));
            } else {
                builder.phi(Parameter.of(phi, ParameterType.Initial), Parameter.of(dphi, ParameterType.Undefined));
            }
        }

        builder.d(regular[1]);

        int q = regular[2];
        if (q > 0) {
            if (theta == null) {
                builder.q(q, ftheta);
            } else if (dtheta == null) {
                builder.theta(Parameter.of(theta, ParameterType.Initial));
            } else {
                builder.btheta(Parameter.of(theta, ParameterType.Initial), Parameter.of(dtheta, ParameterType.Undefined));
            }
        }

        if (period > 1) {
            int bp = seasonal[0];

            if (bp > 0) {
                if (bphi == null) {
                    builder.bp(bp, fbphi);
                } else if (dbphi == null) {
                    builder.bphi(Parameter.of(bphi, ParameterType.Initial));
                } else {
                    builder.bphi(Parameter.of(bphi, ParameterType.Initial), Parameter.of(dbphi, ParameterType.Undefined));
                }
            }

            builder.bd(seasonal[1]);

            int bq = seasonal[2];
            if (bq > 0) {
                if (btheta == null) {
                    builder.bq(bq, fbtheta);
                } else if (dbtheta == null) {
                    builder.btheta(Parameter.of(btheta, ParameterType.Initial));
                } else {
                    builder.btheta(Parameter.of(btheta, ParameterType.Initial), Parameter.of(dbtheta, ParameterType.Undefined));
                }
            }
        }

        if (!fvar) {
            builder.dvar(Parameter.of(dvar, ParameterType.Initial));
        }
        return builder.build();
    }

}
