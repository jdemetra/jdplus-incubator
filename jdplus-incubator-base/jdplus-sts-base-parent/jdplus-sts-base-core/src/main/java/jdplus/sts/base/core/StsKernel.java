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
package jdplus.sts.base.core;

import java.util.ArrayList;
import java.util.List;
import jdplus.sa.base.core.CholetteProcessor;
import jdplus.sa.base.core.PreliminaryChecks;
import jdplus.advancedsa.base.core.regarima.FastKernel;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.sa.base.core.modelling.TwoStepsDecomposition;
import jdplus.sts.base.api.BsmDecomposition;
import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.api.Component;
import jdplus.sts.base.api.RawBsmDecomposition;
import jdplus.sts.base.api.StsSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import jdplus.toolkit.base.api.modelling.regular.SeriesSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.stats.ProbabilityType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.ResidualsType;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.dstats.T;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.Residuals;
import jdplus.toolkit.base.core.modelling.regression.RegressionDesc;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseLikelihoodStatistics;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.core.stats.tests.NiidTests;

/**
 *
 * @author palatej
 */
@lombok.AllArgsConstructor
public class StsKernel {

    private static PreliminaryChecks.Tool of(StsSpec spec) {

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

    private final BsmSpec spec;
    private final PreliminaryChecks.Tool preliminary;
    private final FastKernel preprocessor;
    private final BsmKernel kernel;
    private final CholetteProcessor cholette;

    public static StsKernel of(StsSpec spec, ModellingContext context) {
        PreliminaryChecks.Tool check = of(spec);
        FastKernel preprocessor = FastKernel.of(spec.getPreprocessing(), context);
        BsmKernel bsm = new BsmKernel(spec.getEstimation());
        return new StsKernel(spec.getBsm(), check, preprocessor, bsm, CholetteProcessor.of(spec.getBenchmarking()));
    }

    public StsResults process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        try {
            // Step 0. Preliminary checks
            TsData sc = preliminary.check(s, log);
            // Step 1. Preprocessing
            RegSarimaModel preprocessing = null;
            FastMatrix X = null;
            DoubleSeq y = s.getValues();
            int period = s.getAnnualFrequency();
//            X12plusPreadjustment preadjustment;
            // TODO : backcasts/forecasts
            if (preprocessor != null) {
                // we have to remove fixed effects of the transformed series
                preprocessing = preprocessor.process(sc, log);
                sc = preprocessing.transformedSeries();
                TsDomain domain = s.getDomain();
                TsData preadj = preprocessing.preadjustmentEffect(domain, v -> true);
                sc = TsData.subtract(sc, preadj);
                X = preprocessing.regressionMatrix(domain);
                y = sc.getValues();
            }
            boolean ok = kernel.process(y, X, period, spec);

            Variable[] vars = preprocessing == null ? new Variable[0] : preprocessing.getDescription().getVariables();
            Variable[] variables = new Variable[vars.length];
            // update the coefficients of the variables
            BsmSpec fspec = kernel.finalSpecification(true);
            int nhp = fspec.getFreeParametersCount();
            DiffuseConcentratedLikelihood ll = kernel.getLikelihood();
            DoubleSeqCursor cursor = ll.coefficients().cursor();
            DoubleSeqCursor.OnMutable diag = ll.unscaledCovariance().diagonal().cursor();
            int df = ll.degreesOfFreedom() - nhp;
            double vscale = ll.ssq() / df;
            T tstat = new T(df);

            int k = 0, pos = 0;

            List<RegressionDesc> regressionDesc = new ArrayList<>();
            // fill the free coefficients
            for (Variable var : vars) {
                int nfree = var.freeCoefficientsCount();
                if (nfree == var.dim()) {
                    Parameter[] p = new Parameter[nfree];
                    for (int j = 0; j < nfree; ++j) {
                        double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
                        if (e == 0) {
                            p[j] = Parameter.zero();
                            regressionDesc.add(new RegressionDesc(var.getName(), var.getCore(), j, pos++, 0, 0, 0));
                        } else {
                            p[j] = Parameter.estimated(c);
                            regressionDesc.add(new RegressionDesc(var.getName(), var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
                        }
                    }
                    variables[k++] = var.withCoefficients(p);
                } else if (nfree > 0) {
                    Parameter[] p = var.getCoefficients();
                    for (int j = 0; j < p.length; ++j) {
                        if (p[j].isFree()) {
                            double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
                            p[j] = Parameter.estimated(c);
                            regressionDesc.add(new RegressionDesc(var.getName(), var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
                        }
                    }
                    variables[k++] = var.withCoefficients(p);
                } else {
                    variables[k++] = var;
                }
            }

            BsmMapping mapping = new BsmMapping(fspec, s.getAnnualFrequency(), null);
            DoubleSeq params = mapping.map(kernel.result(true));
            ParametersEstimation parameters = new ParametersEstimation(params, "bsm");

            DoubleSeq coef = kernel.getLikelihood().coefficients();
            LightBasicStructuralModel.Estimation estimation = LightBasicStructuralModel.Estimation.builder()
                    .y(s.getValues())
                    .X(X)
                    .domain(s.getDomain())
                    .missing(new MissingValueEstimation[0])
                    .coefficients(coef)
                    .coefficientsCovariance(kernel.getLikelihood().covariance(nhp, true))
                    .parameters(parameters)
                    .residuals(kernel.getLikelihood().e())
                    .statistics(lstats(kernel.getLikelihood().stats(0, nhp)))
                    .build();
            LightBasicStructuralModel.Description description = LightBasicStructuralModel.Description.builder()
                    .series(s)
                    .logTransformation(preprocessing == null ? false : preprocessing.getDescription().isLogTransformation())
                    .lengthOfPeriodTransformation(preprocessing == null ? LengthOfPeriodType.None : preprocessing.getDescription().getLengthOfPeriodTransformation())
                    .specification(kernel.finalSpecification(false))
                    .variables(variables)
                    .build();
            DoubleSeq e = kernel.getLikelihood().e();
            RawBsmDecomposition rdecomp = kernel.decompose();
            NiidTests niid = NiidTests.builder()
                    .data(e)
                    .period(period)
                    .hyperParametersCount(params.length())
                    .build();
            Residuals residuals = Residuals.builder()
                    .type(ResidualsType.FullResiduals)
                    .res(e)
                    .start(description.getSeries().getStart())
                    .test(ResidualsDictionaries.MEAN, niid.meanTest())
                    .test(ResidualsDictionaries.SKEW, niid.skewness())
                    .test(ResidualsDictionaries.KURT, niid.kurtosis())
                    .test(ResidualsDictionaries.DH, niid.normalityTest())
                    .test(ResidualsDictionaries.LB, niid.ljungBox())
                    .test(ResidualsDictionaries.BP, niid.boxPierce())
                    .test(ResidualsDictionaries.SEASLB, niid.seasonalLjungBox())
                    .test(ResidualsDictionaries.SEASBP, niid.seasonalBoxPierce())
                    .test(ResidualsDictionaries.LB2, niid.ljungBoxOnSquare())
                    .test(ResidualsDictionaries.BP2, niid.boxPierceOnSquare())
                    .test(ResidualsDictionaries.NRUNS, niid.runsNumber())
                    .test(ResidualsDictionaries.LRUNS, niid.runsLength())
                    .test(ResidualsDictionaries.NUDRUNS, niid.upAndDownRunsNumbber())
                    .test(ResidualsDictionaries.LUDRUNS, niid.upAndDownRunsLength())
                    .build();
            LightBasicStructuralModel bsm = LightBasicStructuralModel.builder()
                    .description(description)
                    .estimation(estimation)
                    .bsmDecomposition(rdecomp)
                    .regressionItems(regressionDesc)
                    .residuals(residuals)
                    .build();

            BsmResults results = BsmResults.builder()
                    .bsm(kernel.result(true))
                    .decomposition(BsmDecomposition.of(rdecomp, s.getStart()))
                    .build();
            boolean mul = bsm.getDescription().isLogTransformation();
            SeriesDecomposition components = components(mul, results);
            SeriesDecomposition finals = TwoStepsDecomposition.merge(bsm, components);

            return StsResults.builder()
                    .preprocessing(preprocessing)
                    .bsm(bsm)
                    .sts(results)
                    .components(components)
                    .finals(finals)
                    //                    .benchmarking(bench)
                    //                    .diagnostics(X13plusDiagnostics.of(preprocessing, preadjustment, xr, finals))
                    .log(log)
                    .build();

        } catch (Exception err) {
            log.error(err);
            return null;
        }
    }

    private SeriesDecomposition components(boolean mul, BsmResults rslts) {
        BsmDecomposition decomp = rslts.getDecomposition();
        TsData trend = decomp.getSeries(Component.Level, false);
        TsData cycle = decomp.getSeries(Component.Cycle, false);
        TsData tc = TsData.add(trend, cycle);
        TsData seas = decomp.getSeries(Component.Seasonal, false);
        TsData series = decomp.getSeries(Component.Series, false);
        TsData sa = TsData.subtract(series, seas);
        TsData irr = TsData.subtract(sa, tc);

        // bias correction
        if (mul) {
            series = series.exp();
            tc = tc.exp();
            seas = seas.exp();
            irr = irr.exp();
            double ci = irr.getValues().average();
            double si = fullYears(seas).getValues().average();
            seas = seas.divide(si);
            tc.multiply(ci * si);
            sa = TsData.divide(series, seas);
            irr = TsData.divide(sa, tc);
        }

        return SeriesDecomposition.builder(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .add(series, ComponentType.Series)
                .add(tc, ComponentType.Trend)
                .add(sa, ComponentType.SeasonallyAdjusted)
                .add(seas, ComponentType.Seasonal)
                .add(irr, ComponentType.Irregular)
                .build();
    }

    public static TsData fullYears(TsData s) {
        int p0 = s.getStart().annualPosition();
        int p1 = s.getEnd().annualPosition();
        return s.drop(p0, p1);
    }

    public static LikelihoodStatistics lstats(DiffuseLikelihoodStatistics dll) {
        return LikelihoodStatistics.statistics(dll.getLogLikelihood(), dll.getEffectiveObservationsCount())
                .build();
    }
}
