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
import jdplus.sts.base.api.BsmDecomposition;
import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.api.StsSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.modelling.regular.SeriesSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.stats.ProbabilityType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.dstats.T;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.RegressionDesc;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;

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
                    .statistics(kernel.getLikelihood().stats(0, nhp))
                    .build();
            LightBasicStructuralModel.Description description = LightBasicStructuralModel.Description.builder()
                    .series(s)
                    .logTransformation(preprocessing == null ? false : preprocessing.getDescription().isLogTransformation())
                    .lengthOfPeriodTransformation(LengthOfPeriodType.None)
                    .specification(kernel.finalSpecification(false))
                    .variables(variables)
                    .build();

            LightBasicStructuralModel bsm = LightBasicStructuralModel.builder()
                    .description(description)
                    .estimation(estimation)
                    .bsmDecomposition(kernel.decompose())
                    .regressionItems(regressionDesc)
                    .build();

            BsmResults results = BsmResults.builder()
                    .bsm(kernel.result(true))
                    .likelihood(kernel.getLikelihood())
                    .decomposition(BsmDecomposition.of(kernel.decompose(), s.getStart()))
                    .build();
            return StsResults.builder()
                    .preprocessing(preprocessing)
                    .bsm(bsm)
                    .sts(results)
                    //                    .benchmarking(bench)
                    //                    .diagnostics(X13plusDiagnostics.of(preprocessing, preadjustment, xr, finals))
                    .log(log)
                    .build();

        } catch (Exception err) {
            log.error(err);
            return null;
        }
    }
}
