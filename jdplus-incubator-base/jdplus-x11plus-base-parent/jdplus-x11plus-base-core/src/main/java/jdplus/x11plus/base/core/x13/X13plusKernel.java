/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.x11plus.base.core.x13;

import java.util.Arrays;
import java.util.Optional;
import jdplus.toolkit.base.api.modelling.regular.SeriesSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.CholetteProcessor;
import jdplus.sa.base.core.PreliminaryChecks;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.sa.base.core.modelling.RegArimaDecomposer;
import jdplus.sa.base.core.regarima.FastKernel;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.arima.FastArimaForecasts;
import jdplus.x11plus.base.api.SeasonalFilterOption;
import jdplus.x11plus.base.api.X11plusSpec;
import jdplus.x11plus.base.api.X13plusSpec;
import jdplus.x11plus.base.core.X11plusKernel;
import jdplus.x11plus.base.core.X11plusResults;

@lombok.Value
public class X13plusKernel {

    private static PreliminaryChecks.Tool of(X13plusSpec spec) {

        SeriesSpec series = spec.getPreprocessing().getSeries();
        return (s, logs) -> {
            TsData sc = s.select(series.getSpan());
            if (series.isPreliminaryCheck()) {
                PreliminaryChecks.testSeries(sc);
            }
            if (!spec.getPreprocessing().isEnabled()) {
                return s.select(series.getSpan());
            } else {
                return s;
            }
        };
    }

    private PreliminaryChecks.Tool preliminary;
    private FastKernel preprocessor;
    private X11plusSpec spec;
    private boolean preprop;
    private CholetteProcessor cholette;

    public static X13plusKernel of(X13plusSpec spec, ModellingContext context) {
        PreliminaryChecks.Tool check = of(spec);
        boolean blPreprop = spec.getPreprocessing().isEnabled();
        FastKernel preprocessor = FastKernel.of(spec.getPreprocessing(), context);
        return new X13plusKernel(check, preprocessor, spec.getX11(), blPreprop, CholetteProcessor.of(spec.getBenchmarking()));
    }

    public X13plusResults process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        try {
            // Step 0. Preliminary checks
            TsData sc = preliminary.check(s, log);
            // Step 1. Preprocessing
            RegSarimaModel preprocessing;
            X13plusPreadjustment preadjustment;
            TsData alin;
            if (preprocessor != null) {
                preprocessing = preprocessor.process(sc, log);
                // Step 2. Link between regarima and x11
                int nb = spec == null ? 0 : spec.getBackcastHorizon();
                if (nb < 0) {
                    nb = -nb * s.getAnnualFrequency();
                }
                int nf = spec == null ? 0 : spec.getForecastHorizon();
                if (nf < 0) {
                    nf = -nf * s.getAnnualFrequency();
                }
                X13plusPreadjustment.Builder builder = X13plusPreadjustment.builder();
                alin = initialStep(preprocessing, nb, nf, builder);
                preadjustment = builder.build();
            } else {
                preprocessing = null;
                preadjustment = X13plusPreadjustment.builder().a1(sc).build();
                alin = sc;
            }
            // Step 3. X11
            X11plusSpec nspec = updateSpec(spec, preprocessing);
            X11plusKernel x11 = X11plusKernel.of(nspec);
            X11plusResults xr = x11.process(alin);
            X13plusFinals finals = finals(nspec.getMode(), preadjustment, xr);
            SaBenchmarkingResults bench = null;
            if (cholette != null) {
                bench = cholette.process(s, TsData.concatenate(finals.getD11final(), finals.getD11a()), preprocessing);
            }
            return X13plusResults.builder()
                    .preprocessing(preprocessing)
                    .preadjustment(preadjustment)
                    .decomposition(xr)
                    .finals(finals)
                    .benchmarking(bench)
                    .diagnostics(X13plusDiagnostics.of(preprocessing, preadjustment, xr, finals))
                    .log(log)
                    .build();
        } catch (Exception err) {
            log.error(err);
            return null;
        }

//            if (preprocessor == null) {
//                // Step 0. Preliminary checks
//                TsData sc = preliminary.check(s, log);
//                X11Kernel x11 = X11Kernel.of(spec);
//                X11Results rslt = x11.process(sc);
//                // Step 5. Benchmarking
//                SaBenchmarkingResults bench = null;
//                // Step 6. Diagnostics
//                X13plusDiagnostics diagnostics = X13plusDiagnostics.of(null, rslt, rslt.asDecomposition());
//
//                return X13plusResults.builder()
//                        .preprocessing(null)
//                        .decomposition(rslt)
//                        .finals(rslt.asDecomposition())
//                        .benchmarking(bench)
//                        .diagnostics(diagnostics)
//                        .log(log)
//                        .build();
//
//            } else {
//                // Step 0. Preliminary checks
//                TsData sc = preliminary.check(s, log);
//                // Step 1. RegArima
//                RegSarimaModel preprocessing = preprocessor.process(sc, log);
//                // Step 2. Link between regarima and stl
//                X11plusSpec cspec = spec;
//                boolean mul = preprocessing.getDescription().isLogTransformation();
//                if (cspec == null) {
//                    cspec = X11plusSpec.createDefault(mul, s.getAnnualFrequency(), SeasonalFilterOption.S3X5);
//                } else if (cspec.getMode().isMultiplicative() != mul) {
//                    cspec = spec.toBuilder().mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive).build();
//                }
//                X11Kernel x11 = X11Kernel.of(cspec);
//
//                TsData det = preprocessing.deterministicEffect(s.getDomain(), v -> !SaVariable.isRegressionEffect(v, ComponentType.Undefined));
//                TsData user = RegArimaDecomposer.deterministicEffect(preprocessing, s.getDomain(), ComponentType.Series, true, v -> ModellingUtility.isUser(v));
//                det = TsData.subtract(det, user);
//                TsData cseries;
//                if (mul) {
//                    det = preprocessing.backTransform(det, true);
//                    cseries = TsData.divide(s, det);
//                } else {
//                    cseries = TsData.subtract(s, det);
//                }
//
//                X11Results rslt = x11.process(cseries);
//                // Step 4. Final decomposition
//                SeriesDecomposition finals = TwoStepsDecomposition.merge(preprocessing, rslt.asDecomposition());
//                // Step 5. Benchmarking
//                SaBenchmarkingResults bench = null;
//                if (cholette != null) {
//                    bench = cholette.process(s, TsData.concatenate(finals.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value),
//                            finals.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast)), preprocessing);
//                }
//                // Step 6. Diagnostics
//                X13plusDiagnostics diagnostics = X13plusDiagnostics.of(preprocessing, rslt, finals);
//
//                return X13plusResults.builder()
//                        .preprocessing(preprocessing)
//                        .decomposition(rslt)
//                        .finals(finals)
//                        .benchmarking(bench)
//                        .diagnostics(diagnostics)
//                        .log(log)
//                        .build();
//            }
//        } catch (Exception err) {
//            log.error(err);
//            return null;
//        }
    }

    private TsData initialStep(RegSarimaModel model, int nb, int nf, X13plusPreadjustment.Builder astep) {
        boolean mul = model.getDescription().isLogTransformation();
        TsData series = model.interpolatedSeries(false);
        int n = series.length();
        TsDomain sdomain = series.getDomain();
        TsDomain domain = sdomain.extend(nb, nf);
        // start of the backcasts/forecasts
        TsPeriod bstart = domain.getStartPeriod(), fstart = sdomain.getEndPeriod();

        // Gets all regression effects
        TsData mh = model.deterministicEffect(domain, v -> ModellingUtility.isMovingHoliday(v));
        TsData td = model.deterministicEffect(domain, v -> ModellingUtility.isDaysRelated(v));

        TsData pt = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Trend, true, v -> ModellingUtility.isOutlier(v));
        TsData ps = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Seasonal, true, v -> ModellingUtility.isOutlier(v));
        TsData pi = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Irregular, true, v -> ModellingUtility.isOutlier(v));
        TsData ut = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Trend, true, v -> ModellingUtility.isUser(v));
        TsData us = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Seasonal, true, v -> ModellingUtility.isUser(v));
        TsData ui = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Irregular, true, v -> ModellingUtility.isUser(v));
        TsData usa = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.SeasonallyAdjusted, true, v -> ModellingUtility.isUser(v));
        TsData user = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Series, true, v -> ModellingUtility.isUser(v));
        TsData uu = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Undefined, true, v -> ModellingUtility.isUser(v));
        pt = TsData.add(pt, ut);
        ps = TsData.add(ps, us);
        pi = TsData.add(pi, ui);
        TsData p = TsData.add(pt, ps, pi);
        TsData pall = TsData.add(pt, ps, pi);
        TsData u = TsData.add(usa, user);

        // linearized series. detlin are deterministic effects removed before the decomposition,
        // detall are all the deterministic effects
        TsData detlin = TsData.add(td, mh, p, uu), detall = TsData.add(detlin, u);
        // forecasts, backcasts
        TsData nbcasts = null, nfcasts = null;
        TsData s = model.interpolatedSeries(true);

        if (nb > 0 || nf > 0) {
            TsData lin = TsData.subtract(s, detall);
            SarimaModel arima = model.arima();
            FastArimaForecasts fcasts = new FastArimaForecasts();
            double mean = 0;
            Optional<Variable> mu = Arrays.stream(model.getDescription().getVariables()).filter(v -> v.getCore() instanceof TrendConstant).findFirst();
            if (mu.isPresent()) {
                mean = mu.orElseThrow().getCoefficient(0).getValue();
            }
            fcasts.prepare(arima, mean);

            if (nb > 0) {
                DoubleSeq tmp = fcasts.backcasts(lin.getValues(), nb);
                nbcasts = TsData.of(bstart, tmp);
                nbcasts = TsData.add(nbcasts, detall);
            }
            if (nf > 0) {
                DoubleSeq tmp = fcasts.forecasts(lin.getValues(), nf);
                nfcasts = TsData.of(fstart, tmp);
                nfcasts = TsData.add(nfcasts, detall);
            }
        }

        TsData a1a = nfcasts == null ? null : model.backTransform(nfcasts, true),
                a1b = nbcasts == null ? null : model.backTransform(nbcasts, true);

        astep.a1(series)
                .a1a(a1a)
                .a1b(a1b)
                .a6(model.backTransform(td, true))
                .a7(model.backTransform(mh, false))
                .a8(model.backTransform(pall, false))
                .a8t(model.backTransform(pt, false))
                .a8s(model.backTransform(ps, false))
                .a8i(model.backTransform(pi, false))
                .a9(model.backTransform(u, false))
                .a9sa(model.backTransform(usa, false))
                .a9ser(model.backTransform(user, false));

        series = TsData.concatenate(a1b, series, a1a);
        TsData x = model.backTransform(detlin, true);

        return (mul ? TsData.divide(series, x) : TsData.subtract(series, x));
    }

    private X11plusSpec updateSpec(X11plusSpec spec, RegSarimaModel model) {
        if (model == null) {
            return spec;
        }
        int period = model.getAnnualFrequency();
        if (spec == null) {
            spec = X11plusSpec.createDefault(model.getDescription().isLogTransformation(), period, SeasonalFilterOption.S3X5);
        }

        int nb = spec.getBackcastHorizon(), nf = spec.getForecastHorizon();
        X11plusSpec.Builder builder = spec.toBuilder()
                .backcastHorizon(nb < 0 ? -nb * period : nb)
                .forecastHorizon(nf < 0 ? -nf * period : nf);

        if (!preprop) {
            builder.mode(spec.getMode() == DecompositionMode.Undefined ? DecompositionMode.Additive : spec.getMode());
            return builder.build();
        }
        if (spec.getMode() != DecompositionMode.PseudoAdditive) {
            boolean mul = model.getDescription().isLogTransformation();
            builder.mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive);
        }
        return builder.build();
    }

    private TsData op(DecompositionMode mode, TsData l, TsData r) {
        if (mode != DecompositionMode.Multiplicative && mode != DecompositionMode.PseudoAdditive) {
            return TsData.subtract(l, r);
        } else {
            return TsData.divide(l, r);
        }
    }

    private TsData invOp(DecompositionMode mode, TsData l, TsData r) {
        if (mode != DecompositionMode.Multiplicative && mode != DecompositionMode.PseudoAdditive) {
            return TsData.add(l, r);
        } else {
            return TsData.multiply(l, r);
        }
    }

    private double mean(DecompositionMode mode) {
        if (mode != DecompositionMode.Multiplicative && mode != DecompositionMode.PseudoAdditive) {
            return 0;
        } else {
            return 1;
        }
    }

    private TsData correct(TsData s, TsData weights, TsData rs) {
        DoubleSeq sc = X11Utility.correctSeries(s.getValues(), weights.getValues(), rs.getValues());
        return TsData.of(s.getStart(), sc.commit());
    }

    private TsData correct(TsData s, TsData weights, double mean) {
        DoubleSeq sc = X11Utility.correctSeries(s.getValues(), weights.getValues(), mean);
        return TsData.of(s.getStart(), sc.commit());
    }

    private X13plusFinals finals(DecompositionMode mode, X13plusPreadjustment astep, X11plusResults x11) {
        // add preadjustment
        TsData a1 = astep.getA1();
        TsData a1a = astep.getA1a();
        TsData a1b = astep.getA1b();
        TsData a8t = astep.getA8t();
        TsData a8i = astep.getA8i();
        TsData a8s = astep.getA8s();

        TsData d10 = x11.getD10();
        TsData d11 = x11.getD11();
        TsData d12 = x11.getD12();
        TsData d13 = x11.getD13();

        X13plusFinals.Builder decomp = X13plusFinals.builder();

        TsDomain bd = a1b == null ? null : a1b.getDomain();
        TsDomain fd = a1a == null ? null : a1a.getDomain();
        TsDomain d = a1.getDomain();
        // add ps to d10
//
        TsData a6 = astep.getA6(), a7 = astep.getA7();
        TsData d18 = invOp(mode, a6, a7);
        TsData d10c = invOp(mode, d10, a8s);
        TsData d16 = invOp(mode, d10c, d18);
        // add pt, pi to d11
        TsData d11c = invOp(mode, d11, a8t);
        d11c = invOp(mode, d11c, a8i);
        //   d11c = toolkit.getContext().invOp(d11c, a8s);
        TsData a9sa = astep.getA9sa();
        d11c = invOp(mode, d11c, a9sa);
        TsData d12c = invOp(mode, d12, a8t);
        TsData d13c = invOp(mode, d13, a8i);
        if (fd != null) {
            decomp.d11a(TsData.fitToDomain(d11c, fd));
            decomp.d12a(TsData.fitToDomain(d12c, fd));
            decomp.d16a(TsData.fitToDomain(d16, fd));
            decomp.d18a(TsData.fitToDomain(d18, fd));
        }
        if (bd != null) {
            decomp.d11b(TsData.fitToDomain(d11c, bd));
            decomp.d12b(TsData.fitToDomain(d12c, bd));
            decomp.d16b(TsData.fitToDomain(d16, bd));
            decomp.d18b(TsData.fitToDomain(d18, bd));
        }
        d11c = TsData.fitToDomain(d11c, d);
        d12c = TsData.fitToDomain(d12c, d);
        d16 = TsData.fitToDomain(d16, d);
        d18 = TsData.fitToDomain(d18, d);
        d13c = TsData.fitToDomain(d13c, d);
        decomp.d11final(d11c);
        decomp.d12final(d12c);
        decomp.d13final(d13c);
        decomp.d16(d16);
        decomp.d18(d18);

        // remove pre-specified outliers
        TsData a1c = op(mode, a1, a8i);
        d11c = op(mode, d11c, a8i);

        TsData c17 = TsData.fitToDomain(x11.getC17(), d);

        TsData tmp = op(mode, a1, d13c);
        TsData e1 = correct(a1c, c17, tmp);
        TsData e2 = correct(d11c, c17, d12);
        TsData e3 = correct(TsData.fitToDomain(d13, d), c17, mean(mode));
        TsData e11 = correct(d11c, c17, invOp(mode, d12, op(mode, a1c, e1)));

        decomp.e1(e1);
        decomp.e2(e2);
        decomp.e3(e3);
        decomp.e11(e11);

        return decomp.build();

    }

}
