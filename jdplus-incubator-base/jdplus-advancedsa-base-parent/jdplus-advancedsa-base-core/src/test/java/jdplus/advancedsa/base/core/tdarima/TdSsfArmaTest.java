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

import java.util.Random;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class TdSsfArmaTest {

    public TdSsfArmaTest() {
    }

    @Test
    public void testLinear() {
        int N = 120;
        IArimaModel[] models = new IArimaModel[N];
        int p = 3;
        int bp = 0;
        int q = 1;
        int bq = 1;
        SarmaOrders spec = new SarmaOrders(4);
        spec.setP(p);
        spec.setBp(bp);
        spec.setQ(q);
        spec.setBq(bq);

        double th0 = -.9, th1 = -.9;
        double bth0 = -.9, bth1 = -.9;
        double dth = (th1 - th0) / (N - 1);
        double dbth = (bth1 - bth0) / (N - 1);
        for (int i = 0; i < N; ++i) {
            SarimaModel arma = SarimaModel.builder(spec)
                    .phi(-0.5, .2, .15)
                    .theta(th0)
                    .btheta(bth0)
                    .build();
            models[i] = ArimaModel.of(arma);//.scaleVariance(0.25);
            th0 += dth;
            bth0 += dbth;
        }

        Ssf ssf = TdSsfArma.ssf(N, i -> models[i]);

        DataBlock x = DataBlock.make(N);
        Random rnd = new Random(0);
        x.set(() -> rnd.nextDouble(-1, 1));

        DiffuseLikelihood ll1 = DkToolkit.likelihood(ssf, new SsfData(x), true, false);
        System.out.println(ll1);

        Ssf ssf3 =  SsfArima.ssf(models[0]);
        DiffuseLikelihood ll3 = DkToolkit.likelihood(ssf3, new SsfData(x), true, false);
        System.out.println(ll3);
    }

    @Test
    public void testLinear2() {
        int N = 120;
        IArimaModel[] models = new IArimaModel[N];
        int p = 3;
        int bp = 0;
        int q = 1;
        int bq = 1;
        SarimaOrders spec = new SarimaOrders(4);
        spec.setP(p);
        spec.setD(1);
        spec.setBp(bp);
        spec.setQ(q);
        spec.setBq(bq);

        double th0 = -.9, th1 = -.9;
        double bth0 = -.9, bth1 = -.9;
        double dth = (th1 - th0) / (N - 1);
        double dbth = (bth1 - bth0) / (N - 1);
        for (int i = 0; i < N; ++i) {
            SarimaModel arma = SarimaModel.builder(spec)
                    .phi(-0.5,.2, .1)
                    .theta(th0)
                    .btheta(bth0)
                    .build();
            models[i] = arma;
            th0 += dth;
            bth0 += dbth;
        }

        Ssf ssf = TdSsfArima.ssf(N, i -> models[i]);

        DataBlock x = DataBlock.make(N);
        Random rnd = new Random(0);
        x.set(() -> rnd.nextDouble(-1, 1));

        DiffuseLikelihood ll1 = DkToolkit.likelihood(ssf, new SsfData(x), true, false);
        System.out.println(ll1);

//        Ssf ssf2 = TimeVaryingSsfArima2.ssf(N, i -> models[i]);
//        DiffuseLikelihood ll2 = DkToolkit.likelihood(ssf2, new SsfData(x), true, false);
//        System.out.println(ll2);
//
        Ssf ssf3 =  SsfArima.ssf(models[0]);
        DiffuseLikelihood ll3 = DkToolkit.likelihood(ssf3, new SsfData(x), true, false);
        System.out.println(ll3);
    }

    public static void main(String[] arg) {
        int N = 100, K = 100000;
        IArimaModel[] models = new IArimaModel[N];
        int p = 0;
        int bp = 0;
        int q = 1;
        int bq = 1;
        SarmaOrders spec = new SarmaOrders(12);
        spec.setP(p);
        spec.setBp(bp);
        spec.setQ(q);
        spec.setBq(bq);

        double th0 = -.19, th1 = -.9;
        double bth0 = -.19, bth1 = -.9;
        double dth = (th1 - th0) / (N - 1);
        double dbth = (bth1 - bth0) / (N - 1);
        for (int i = 0; i < N; ++i) {
            SarimaModel arma = SarimaModel.builder(spec)
                    .theta(th0 + i * dth)
                    .btheta(bth0 + dbth * i)
                    .build();
            models[i] = arma;
        }

//        Ssf ssf1 = SsfArma2.ssf(models[0]);
        Ssf ssf1 = TdSsfArma.ssf(N, i -> models[i]);
         DataBlock x = DataBlock.make(N);
        Random rnd = new Random();
        x.set(() -> rnd.nextDouble(-1, 1));
        DiffuseLikelihood ll1 = null, ll2 = null;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            ll1 = DkToolkit.likelihood(ssf1, new SsfData(x), true, true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(ll1.logLikelihood());
    }

}
