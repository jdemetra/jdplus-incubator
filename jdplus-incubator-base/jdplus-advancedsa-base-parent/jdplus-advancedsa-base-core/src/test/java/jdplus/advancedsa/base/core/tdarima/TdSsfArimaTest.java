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
package jdplus.advancedsa.base.core.tdarima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class TdSsfArimaTest {

    public TdSsfArimaTest() {
    }

    @Test
    public void testLinearAirline() {
        double[] s = Data.PROD;
        int N = s.length;
        IArimaModel[] models = new IArimaModel[N];
        IArimaModel[] stmodels = new IArimaModel[N-13];
        SarimaOrders spec = SarimaOrders.airline(12);

 //       double phi0 = -.1, phi1 = -.2;
        double th0 = -.2, th1 = -.9;
        double bth0 = -.9, bth1 = -.1;
//        double dphi = (phi1 - phi0) / (N - 14);
        double dth = (th1 - th0) / (N - 14);
        double dbth = (bth1 - bth0) / (N - 14);
        for (int i = 0; i < 13; ++i) {
            SarimaModel arima = SarimaModel.builder(spec)
                    .theta(th0)
//                    .phi(phi0)
                    .btheta(bth0)
                    .build();
            models[i] = ArimaModel.of(arima).scaleVariance(.25);;
         }
        for (int i = 13, j=0; i < N; ++i, ++j) {
            SarimaModel arima = SarimaModel.builder(spec)
//                    .phi(phi0 + j * dphi)
                    .theta(th0 + j * dth)
                    .btheta(bth0 + j * dbth)
                    .build();
            models[i] = ArimaModel.of(arima).scaleVariance(.25);
            stmodels[j] = arima.stationaryTransformation().getStationaryModel();
        }

        Ssf ssf = TdSsfArima.ssf(N, i -> models[i]);
        Ssf stssf = TdSsfArma.ssf(N-13, i -> stmodels[i]);
        
        TsData ts=Data.TS_PROD;
        TsData stts = ts.delta(1).delta(12);
        
        DiffuseLikelihood stll1 = DkToolkit.likelihoodComputer(true, true, true).compute(stssf, new SsfData(stts.getValues()));
        System.out.println(stll1);

        DiffuseLikelihood ll1 = DkToolkit.likelihoodComputer(false,true, true).compute(ssf, new SsfData(s));
        System.out.println(ll1);

        Ssf ssf2 = SsfArima.ssf(models[0]);
        DiffuseLikelihood ll2 = DkToolkit.likelihood(ssf2, new SsfData(s), true, true);
        System.out.println(ll2);
    }

    public static void main(String[] arg) {
        double[] x = Data.PROD;
        int N = x.length;
        int K = 1000;
        IArimaModel[] models = new IArimaModel[N];
        SarimaOrders spec = SarimaOrders.airline(12);

        double th0 = .2, th1 = -.9;
        double bth0 = -.2, bth1 = -.9;
        double dth = (th1 - th0) / (N - 1);
        double dbth = (bth1 - bth0) / (N - 1);
        for (int i = 0; i < N; ++i) {
            SarimaModel arma = SarimaModel.builder(spec)
                    .theta(th0 + i * dth)
                    .btheta(bth0 + dbth * i)
                    .build();
            models[i] = arma;
        }

        Ssf ssf1 = SsfArima.ssf(models[0]);
        Ssf ssf2 = TdSsfArima.ssf(N, i -> models[i]);
        Ssf ssf3 = TdSsfArima2.ssf(N, i -> models[i]);
        DiffuseLikelihood ll1 = null, ll2 = null, ll3 = null;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            ll1 = DkToolkit.likelihood(ssf1, new SsfData(x), true, true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            ll2 = DkToolkit.likelihood(ssf2, new SsfData(x), true, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            ll3 = DkToolkit.likelihood(ssf3, new SsfData(x), true, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(ll1.logLikelihood());
        System.out.println(ll2.logLikelihood());
        System.out.println(ll3.logLikelihood());
    }

}
