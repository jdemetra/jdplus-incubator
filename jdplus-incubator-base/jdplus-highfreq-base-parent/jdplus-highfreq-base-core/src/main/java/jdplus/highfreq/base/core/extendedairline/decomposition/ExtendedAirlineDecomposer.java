/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.highfreq.base.core.extendedairline.decomposition;

import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineMapping;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.highfreq.base.api.ExtendedAirline;
import jdplus.highfreq.base.api.SeriesComponent;
import jdplus.sa.base.api.ComponentType;
import java.util.Arrays;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.arima.Spectrum;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.regarima.GlsArimaProcessor;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ExtendedSsfData;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
import jdplus.toolkit.base.core.ucarima.AllSelector;
import jdplus.toolkit.base.core.ucarima.ModelDecomposer;
import jdplus.toolkit.base.core.ucarima.SeasonalSelector;
import jdplus.toolkit.base.core.ucarima.TrendCycleSelector;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfUcarima;
import jdplus.toolkit.base.core.ucarima.RootDecomposer;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDecomposer {

    public static LightExtendedAirlineDecomposition decompose(DoubleSeq s, double period, boolean sn, boolean cov, int nb, int nf, double eps, double deps) {
        ExtendedAirlineMapping mapping = new ExtendedAirlineMapping(new double[]{period}, deps);

        GlsArimaProcessor.Builder<ArimaModel> builder = GlsArimaProcessor.builder(ArimaModel.class);
        builder.minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(eps)
                .useMaximumLikelihood(true)
                .useParallelProcessing(true)
                .build();
        ArimaModel arima = mapping.getDefault();
        RegArimaModel<ArimaModel> regarima
                = RegArimaModel.<ArimaModel>builder()
                        .y(s)
                        .arima(arima)
                        .build();
        GlsArimaProcessor<ArimaModel> monitor = builder.build();
        RegArimaEstimation<ArimaModel> rslt = monitor.process(regarima, mapping);
        LogLikelihoodFunction.Point<RegArimaModel<ArimaModel>, ConcentratedLikelihoodWithMissing> max = rslt.getMax();
        UcarimaModel ucm = ucm(rslt.getModel().arima(), sn);

        IArimaModel sum = ucm.getModel();
        UcarimaModel ucmt;
        if (sn) {
            ArimaModel mn = ucm.getComponent(0);
            ArimaModel ms = ucm.getComponent(1);
            ucmt = UcarimaModel.builder()
                    .model(sum)
                    .add(ms, mn)
                    .build();

        } else {
            ArimaModel mt = ucm.getComponent(0);
            ArimaModel ms = ucm.getComponent(1);
            ArimaModel mi = ucm.getComponent(2);
            ucmt = UcarimaModel.builder()
                    .model(sum)
                    .add(mt, ms, mi)
                    .build();
        }
        LightExtendedAirlineDecomposition.Builder dbuilder = LightExtendedAirlineDecomposition.builder()
                .model(ExtendedAirline.builder()
                        .periodicities(new double[]{period})
                        .ndifferencing(2)
                        .ar(false)
                        .p(max.getParameters())
                        .build())
                .likelihood(rslt.statistics())
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .ucarima(ucmt);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        ISsfData data = new ExtendedSsfData(new SsfData(s), nb, nf);
        int[] pos = ssf.componentsPosition();
        DoubleSeq yc = s;
        if (cov) {
            try {
                DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, true, true);
                if (sn) {
                    DoubleSeq sc = sr.getComponent(pos[1]), nc = sr.getComponent(pos[0]);
                    if (nb > 0 || nf > 0) {
                        DataBlock q = DataBlock.of(nc);
                        q.add(sc);
                        q.drop(nb, nf).copy(s);
                        yc = q;
                    }
                    return dbuilder
                            .y(yc)
                            .component(new SeriesComponent("S", sc, sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Seasonal))
                            .component(new SeriesComponent("N", nc, sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.SeasonallyAdjusted))
                            .build();
                } else {
                    DoubleSeq sc = sr.getComponent(pos[1]), tc = sr.getComponent(pos[0]), ic = sr.getComponent(pos[2]);
                    if (nb > 0 || nf > 0) {
                        DataBlock q = DataBlock.of(tc);
                        q.add(sc);
                        q.add(ic);
                        q.drop(nb, nf).copy(s);
                        yc = q;
                    }
                    return dbuilder
                            .y(yc)
                            .component(new SeriesComponent("T", tc.commit(), sr.getComponentVariance(pos[0]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Trend))
                            .component(new SeriesComponent("S", sc.commit(), sr.getComponentVariance(pos[1]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Seasonal))
                            .component(new SeriesComponent("I", ic.commit(), sr.getComponentVariance(pos[2]).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), ComponentType.Irregular))
                            .build();
                }
            } catch (Exception err) {
            }
        }

        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        if (sn) {
            DoubleSeq sc = ds.item(pos[1]), nc = ds.item(pos[0]);
            if (nb > 0 || nf > 0) {
                DataBlock q = DataBlock.of(nc);
                q.add(sc);
                q.drop(nb, nf).copy(s);
                yc = q;
            }
            return dbuilder
                    .y(yc)
                    .component(new SeriesComponent("S", ds.item(pos[1]).commit(), DoubleSeq.empty(), ComponentType.Seasonal))
                    .component(new SeriesComponent("N", ds.item(pos[0]).commit(), DoubleSeq.empty(), ComponentType.SeasonallyAdjusted))
                    .build();
        } else {
            DoubleSeq sc = ds.item(pos[1]), tc = ds.item(pos[0]), ic = ds.item(pos[2]);
            if (nb > 0 || nf > 0) {
                DataBlock q = DataBlock.of(tc);
                q.add(sc);
                q.add(ic);
                q.drop(nb, nf).copy(s);
                yc = q;
            }
            return dbuilder
                    .y(yc)
                    .component(new SeriesComponent("S", sc.commit(), DoubleSeq.empty(), ComponentType.Seasonal))
                    .component(new SeriesComponent("T", tc.commit(), DoubleSeq.empty(), ComponentType.Trend))
                    .component(new SeriesComponent("I", ic.commit(), DoubleSeq.empty(), ComponentType.Irregular))
                    .build();
        }
    }

    public static LightExtendedAirlineDecomposition decompose(DoubleSeq s, double[] periods, int ndiff, boolean ar, boolean cov, int nb, int nf, double eps, double deps) {

        if (periods.length == 1) {
            return decompose(s, periods[0], false, cov, nb, nf, eps, deps);
        }

        double[] dp = periods.clone();
        Arrays.sort(dp);
        int[] ip = new int[dp.length - 1];
        for (int i = 0; i < ip.length; ++i) {
            int p = (int) dp[i];
            if (Math.abs(dp[i] - p) < 1e-9) {
                dp[i] = p;
                ip[i] = p;
            } else {
                throw new IllegalArgumentException("Period " + dp[i] + " should be integer");
            }
        }

        final ExtendedAirlineMapping mapping = new ExtendedAirlineMapping(dp, false, ndiff, ar, deps);

        GlsArimaProcessor.Builder<ArimaModel> builder = GlsArimaProcessor.builder(ArimaModel.class);
        builder.minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(eps)
                .useMaximumLikelihood(true)
                .useParallelProcessing(true)
                .build();
        ArimaModel arima = mapping.getDefault();
        RegArimaModel<ArimaModel> regarima
                = RegArimaModel.<ArimaModel>builder()
                        .y(s)
                        .arima(arima)
                        .build();
        GlsArimaProcessor<ArimaModel> monitor = builder.build();
        RegArimaEstimation<ArimaModel> rslt = monitor.process(regarima, mapping);
        LogLikelihoodFunction.Point<RegArimaModel<ArimaModel>, ConcentratedLikelihoodWithMissing> max = rslt.getMax();
        DoubleSeq parameters = max.getParameters();
        UcarimaModel ucm = ucm3(rslt.getModel().arima(), ip);

        LightExtendedAirlineDecomposition.Builder dbuilder = LightExtendedAirlineDecomposition.builder()
                .model(ExtendedAirline.builder()
                        .periodicities(dp)
                        .ndifferencing(ndiff)
                        .ar(ar)
                        .p(parameters)
                        .build())
                .likelihood(rslt.statistics())
                .parameters(max.getParameters())
                .parametersCovariance(max.asymptoticCovariance())
                .score(max.getScore())
                .ucarima(ucm);
        CompositeSsf ssf = SsfUcarima.of(ucm);
        ISsfData data = new ExtendedSsfData(new SsfData(s), nb, nf);
        int[] pos = ssf.componentsPosition();
        DoubleSeq sc = s;
        String[] cmpNames=cmpNames(dp, pos.length);
        ComponentType[] types=cmpTypes(pos.length, dp.length);
        if (cov) {
            try {
                DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, true, true);
                if (nb > 0 || nf > 0) {
                    DataBlock q = DataBlock.of(sr.getComponent(pos[0]));
                    for (int i = 1; i < pos.length; ++i) {
                        q.add(sr.getComponent(pos[i]));
                    }
                    sc = q;
                }
                for (int i = 0; i < pos.length; ++i) {
                    dbuilder.component(new SeriesComponent(cmpNames[i],
                            sr.getComponent(pos[i]).commit(),
                            sr.getComponentVariance(i).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), types[i]));
                }
                return dbuilder
                        .y(sc)
                        .build();
            } catch (Exception err) {
            }
        }

        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        if (nb > 0 || nf > 0) {
            sc = ds.item(pos[0]);
            for (int i = 1; i < pos.length; ++i) {
                sc = DoublesMath.add(sc, ds.item(pos[i]));
            }
        }
        
        for (int i = 0; i < pos.length; ++i) {
            dbuilder.component(new SeriesComponent(cmpNames[i],
                    ds.item(pos[i]).commit(), DoubleSeq.empty(), types[i]));
        }
        return dbuilder
                .y(sc)
                .build();
    }

    public static UcarimaModel ucm(IArimaModel arima, boolean sn) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        if (sn) {
            ucm = ucm.setVarianceMax(0, true);
        } else {
            ucm = ucm.setVarianceMax(-1, true);
        }
        return ucm.simplify();
    }

    public static UcarimaModel ucm(IArimaModel arima, int[] periods) {

        // first, we check that q <= p. 
        // Otherwise, we extract the transitory component and we decompose the remainder
        BackFilter ar = arima.getAr(), ma = arima.getMa();
        ArimaModel tr = null;
        IArimaModel arimac = arima;
        if (ma.getDegree() > ar.getDegree()) {
            RootDecomposer rdecomposer = new RootDecomposer();
            rdecomposer.setModel(ArimaModel.of(arima));
            arimac = rdecomposer.getSignal();
            tr = rdecomposer.getNoise();
        }
        TrendCycleSelector tsel = new TrendCycleSelector();
//        AllSelector ssel = new AllSelector();

        int[] np = periods.clone();
        Arrays.sort(np);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        for (int i = 0; i < np.length; ++i) {
            decomposer.add(new SeasonalSelector(np[i], 1e-6));
        }
//        decomposer.add(ssel);
        UcarimaModel ucm = decomposer.decompose(arimac);
        int nc = ucm.getComponentsCount();
        if (tr != null) {
            ArimaModel[] components = ucm.getComponents();
//        int last = components.length - 1;
//           components[last] = ArimaModel.add(tr, components[last]);
            ucm = UcarimaModel.builder()
                    .model(arima)
                    .add(components)
                    .add(tr)
                    .build();
        }
        ucm = ucm.setVarianceMax(-1, true);
        ucm = ucm.simplify();
        int n = ucm.getComponentsCount();
        if (tr != null && n > nc + 1) {
            ucm = ucm.compact(nc, 2);
        }
        return ucm;
    }

    public static UcarimaModel ucm2(IArimaModel arima, int[] periods) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        int[] np = periods.clone();
        Arrays.sort(np);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        for (int i = 0; i < np.length; ++i) {
            decomposer.add(new SeasonalSelector(np[i], 1e-6));
        }
        decomposer.add(ssel);
        UcarimaModel ucm = decomposer.decompose(arima);
        ucm = doCanonical(ucm, true);
        return ucm;
    }

    public static UcarimaModel ucm3(IArimaModel arima, int[] periods) {

        // first, we check that q <= p. 
        // Otherwise, we extract the transitory component and we decompose the remainder
        BackFilter ar = arima.getAr(), ma = arima.getMa();
        ArimaModel tr = null;
        IArimaModel arimac = arima;
        if (ma.getDegree() > ar.getDegree()) {
            RootDecomposer rdecomposer = new RootDecomposer();
            rdecomposer.setModel(ArimaModel.of(arima));
            arimac = rdecomposer.getSignal();
            tr = rdecomposer.getNoise();
        }
        TrendCycleSelector tsel = new TrendCycleSelector();
        int[] np = periods.clone();
        Arrays.sort(np);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        for (int i = 0; i < np.length; ++i) {
            decomposer.add(new SeasonalSelector(np[i], 1e-6));
        }
        UcarimaModel ucm = decomposer.decompose(arimac);
        // ucm contains the trend and the seasonals (no irregular)
        int nc = ucm.getComponentsCount();
        // we add either the transitory or an empty irregular (wn with 0 var)
        UcarimaModel.Builder builder = UcarimaModel.builder()
                .model(arima)
                .add(ucm.getComponents());
        if (tr != null) {
            builder.add(tr);
        } else {
            builder.add(ArimaModel.NULL);
        }
        ucm = doCanonical(builder.build(), true);
        return ucm;
    }

    /**
     * Put all the noise in the last component and adjust the model to a
     * decomposable one if need be.
     *
     * @param ucm
     * @param adjustModel
     * @return
     */
    public static UcarimaModel doCanonical(UcarimaModel ucm, boolean adjustModel) {
        double var = 0;
        ArimaModel[] components = ucm.getComponents().clone();
        int n = components.length - 1;
        Spectrum.Minimizer min = new Spectrum.Minimizer();
        // Do all the components canonical, except the last one
        for (int i = 0; i < n; ++i) {
            ArimaModel m = components[i];
            if (m != null) {
                min.minimize(m.getSpectrum());
                if (min.getMinimum() != 0) {
                    var += min.getMinimum();
                    components[i] = m.minus(min.getMinimum());
                }
            }
        }
        ArimaModel nmodel = components[n];
        if (var >= 0) {
            // we just need to add the noise in the last component
            components[n] = nmodel.plus(var);
        } else {
            min.minimize(nmodel.getSpectrum());
            double nmin = min.getMinimum();
            if (nmin >= -var) {
            // The last component is noisy enough to absorb some negative var
                components[n] = nmodel.plus(var);
            } else if (adjustModel) {
                // -var-nmin>
                var=-var-nmin;
                // If the last component is a white noise, we suppress it and we
                // add to the model the needed noise
                if (nmodel.isWhiteNoise()) {
                    ArimaModel[] ncomponents = Arrays.copyOf(components, n);
                    return UcarimaModel.builder()
                            .model(ArimaModel.of(ucm.getModel()).plus(var))
                            .add(ncomponents)
                            .build();
                } else {
                    // Otherwise, we must keep the last component, made canonical
                    components[n] = nmodel.minus(nmin);
                    return UcarimaModel.builder()
                            .model(ArimaModel.of(ucm.getModel()).plus(var))
                            .add(components)
                            .build();
                }
            } else {
                return null;
            }
        }
        return UcarimaModel.builder().model(ucm.getModel()).add(components).build();
    }
    
    static String[] cmpNames(double[] dperiods, int ncmps){
        String[] cmps=new String[ncmps];
        int j=0;
        cmps[j++]="T";
        for (int i=0; i<dperiods.length; ++i){
            cmps[j++]="S-"+(int)dperiods[i];
        }
        if (j<ncmps)
            cmps[j]="I";
        return cmps;
    }

    static ComponentType[] cmpTypes(int ncmps, int ns){
        ComponentType[] cmps=new ComponentType[ncmps];
        int j=0;
        cmps[j++]=ComponentType.Trend;
        for (int i=0; i<ns; ++i){
            cmps[j++]=ComponentType.Seasonal;
        }
        if (j<ncmps)
            cmps[j]=ComponentType.Irregular;
        return cmps;
    }
}
