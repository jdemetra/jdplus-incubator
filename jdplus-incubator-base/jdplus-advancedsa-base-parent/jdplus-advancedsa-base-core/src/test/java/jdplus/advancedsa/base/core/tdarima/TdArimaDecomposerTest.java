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
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.likelihood.MarginalLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class TdArimaDecomposerTest {

    @Test
    public void testLinear() {
        double[] s = Data.RETAIL_GASOLINE.clone();
        for (int i = 0; i < s.length; ++i) {
            s[i] = Math.log(s[i]);
        }
        double[] th = linear(s.length, 13, -0.8, -0.8);
        double[] bth = linear(s.length, 13, -.9, -.9);
        double[] var = linear(s.length, 13, 1, 6);
        SarimaOrders spec = new SarimaOrders(12);
        spec.setD(1);
        spec.setQ(1);
        spec.setBd(1);
        spec.setBq(1);
        long t0 = System.currentTimeMillis();
        TdArimaDecomposer decomposer = new TdArimaDecomposer(12, s.length,
                i -> {
                    ArimaModel arima = ArimaModel.of(SarimaModel.builder(spec)
                            .theta(th[i])
                            .btheta(bth[i])
                            .build());
//                    arima=arima.scaleVariance(var[i]);
                    return arima;
                }
        );
        SarimaModel[] models = new SarimaModel[s.length];
        for (int i = 0; i < s.length; ++i) {
            SarimaModel arima = SarimaModel.builder(spec)
                    .theta(th[i])
                    .btheta(bth[i])
                    .build();
            models[i] = arima;
        }

        Ssf ssf1 = TdSsfArima.ssf(s.length, i -> models[i]);
        MarginalLikelihood mll1 = AkfToolkit.marginalLikelihoodComputer(true, true).compute(ssf1, new SsfData(s));
        DiffuseLikelihood dll1 = DkToolkit.likelihoodComputer(true, true, true).compute(ssf1, new SsfData(s));
        System.out.println(mll1);
        System.out.println(dll1);

        UcarimaModel[] ucarimaModels = decomposer.ucarimaModels();

        CompositeSsf ssf2 = TdSsfUcarima.of(s.length, i -> ucarimaModels[i]);
        MarginalLikelihood mll2 = AkfToolkit.marginalLikelihoodComputer(true, true).compute(ssf2, new SsfData(s));
        DiffuseLikelihood dll2 = DkToolkit.likelihoodComputer(true, true, true).compute(ssf2, new SsfData(s));
        System.out.println(mll2);
        System.out.println(dll2);
        System.out.println(dll1.e());
        System.out.println(dll2.e());
        DefaultSmoothingResults sf = DkToolkit.sqrtSmooth(ssf2, new SsfData(s), false, false);

        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        int[] dim = ssf2.componentsDimension();

        DataBlock Z = DataBlock.make(ssf2.getStateDim());
        ssf2.loading().Z(0, Z);

        for (int i = 0, j = 0; i < dim.length; ++i) {
            double[] z = new double[Z.length()];
            Z.extract(j, dim[i]).copyTo(z, j);
            System.out.println(sf.zcomponent(DoubleSeq.of(z)));
            j += dim[i];
        }
        System.out.println(DoubleSeq.of(s));
        for (int i = 0; i < ucarimaModels.length; ++i) {
            UcarimaModel ucm = ucarimaModels[i];
            for (int j = 0; j < ucm.getComponentsCount(); ++j) {
                System.out.print(ucm.getComponent(j).getInnovationVariance());
                System.out.print('\t');
            }
            System.out.println();
        }
    }

    @Test
    public void testLinear2() {
        double[] s = Data.RETAIL_GASOLINE.clone();
        for (int i = 0; i < s.length; ++i) {
            s[i] = Math.log(s[i]);
        }
        double[] th1 = linear(s.length, 0, -0.8, 0.1);
        double[] th2 = linear(s.length, 0, -.1, -.75);
        SarimaOrders spec = new SarimaOrders(12);
        spec.setQ(2);
        SarimaModel[] models1 = new SarimaModel[s.length];
        for (int i = 0; i < s.length; ++i) {
            SarimaModel arima = SarimaModel.builder(spec)
                    .theta(th1[i])
                    .build();
            models1[i] = arima;
        }
        SarimaModel[] models2 = new SarimaModel[s.length];
        for (int i = 0; i < s.length; ++i) {
            SarimaModel arima = SarimaModel.builder(spec)
                    .theta(th2[i])
                    .build();
            models2[i] = arima;
        }
        System.out.println(models1[0]);
        System.out.println(models2[0]);
        System.out.println(ArimaModel.of(models1[0]).plus(ArimaModel.of(models2[0])));

        Ssf ssf = TdSsfArima.ssf(s.length, i -> ArimaModel.of(models1[i]).plus(models2[i]));
//        MarginalLikelihood mll1 = AkfToolkit.marginalLikelihoodComputer(true, true).compute(ssf, new SsfData(s));
        DiffuseLikelihood dll1 = DkToolkit.likelihoodComputer(true, true, true).compute(ssf, new SsfData(s));
//        System.out.println(mll1);
        System.out.println(dll1);

        StateComponent c1 = TdSsfArma.stateComponent(s.length, i->models1[i]);
        StateComponent c2 = TdSsfArma.stateComponent(s.length, i->models2[i]);
        
        CompositeSsf ssf2 = CompositeSsf.builder()
                .add(c1, TdSsfArma.defaultLoading())
                .add(c2, TdSsfArma.defaultLoading())
                .build();
//        MarginalLikelihood mll2 = AkfToolkit.marginalLikelihoodComputer(true, true).compute(ssf2, new SsfData(s));
        DiffuseLikelihood dll2 = DkToolkit.likelihoodComputer(true, true, true).compute(ssf2, new SsfData(s));
//        System.out.println(mll2);
        System.out.println(dll2);
        System.out.println(dll1.e());
        System.out.println(dll2.e());
    }

    public static double[] linear(int n, int nd, double a, double b) {
        double d = (b - a) / (n - nd - 1.0);
        double[] s = new double[n];
        for (int i = 0; i <= nd; ++i) {
            s[i] = a;
        }
        double cur = a;
        for (int i = nd + 1; i < n; ++i) {
            cur += d;
            s[i] = cur;
        }
        return s;
    }

}
