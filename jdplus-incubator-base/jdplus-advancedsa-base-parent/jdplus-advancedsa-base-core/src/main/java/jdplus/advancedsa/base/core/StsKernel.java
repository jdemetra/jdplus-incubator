/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.advancedsa.base.core;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.advancedsa.base.api.StsSpec;
import jdplus.sts.base.api.BsmSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.UserVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaProcessor;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.sts.base.core.BsmKernel;
import jdplus.sts.base.core.BsmMapping;
import jdplus.sts.base.core.LightBasicStructuralModel;

/**
 *
 * @author palatej
 */
@lombok.Value
public class StsKernel {

    private static PreliminaryChecks checks(StsSpec spec) {

        return (s, logs) -> {
            TsData sc = s.select(spec.getPreprocessing().getSpan());
            jdplus.sa.base.core.PreliminaryChecks.testSeries(sc);
            return sc;
        };
    }

    private final PreliminaryChecks preliminary;
    private final RegSarimaProcessor preprocessor;
    private final BsmKernel bsm;
    private final StsSpec spec;

    public static StsKernel of(StsSpec spec) {

        PreliminaryChecks check = checks(spec);
        RegSarimaProcessor preprocessor = Utility.preprocessor(spec.getPreprocessing());
        BsmKernel bsm = new BsmKernel(spec.getBsmEstimation());
        return new StsKernel(check, preprocessor, bsm, spec);
    }

    public StsResults process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        try {
            // Step 0. Preliminary checks
            TsData sc = preliminary.check(s, log);
            DoubleSeq y = null;
            int period = s.getAnnualFrequency();
            FastMatrix X = null;
            RegSarimaModel preprocessing = null;
            // Step 1. Preprocessing, if needed
            if (preprocessor != null) {
                // Step 2. We reuse the preprocessing to get y and X
                preprocessing = preprocessor != null ? preprocessor.process(sc, log) : null;
                RegArimaModel<SarimaModel> regarima = preprocessing.regarima();
                y = regarima.originalY();
                X = regarima.variables();
            } else {
                y = sc.select(spec.getPreprocessing().getSpan()).getValues();
            }
            // Step 3. Bsm estimation with the variables identified in the pre-processing
            boolean ok = bsm.process(y, X, period, spec.getBsm());
            if (!ok) {
                return null;
            }
            // Ste 4. Final results
            BsmSpec fspec = bsm.finalSpecification(true);
            int nhp = fspec.getFreeParametersCount();
            BsmMapping mapping = new BsmMapping(fspec, period, null);
            DoubleSeq params = mapping.map(bsm.result(true));
            ParametersEstimation parameters = new ParametersEstimation(params, "bsm");

            DoubleSeq coef = bsm.getLikelihood().coefficients();
            LightBasicStructuralModel.Estimation estimation = LightBasicStructuralModel.Estimation.builder()
                    .y(y)
                    .X(X)
                    .coefficients(coef)
                    .coefficientsCovariance(bsm.getLikelihood().covariance(nhp, true))
                    .parameters(parameters)
                    .residuals(bsm.getLikelihood().e())
                    .statistics(bsm.getLikelihood().stats(0, nhp))
                    .build();

            Variable[] vars = X == null ? new Variable[0] : new Variable[X.getColumnsCount()];
            TsPeriod start = sc.getStart();
            for (int i = 0; i < vars.length; ++i) {
                UserVariable uvar = new UserVariable("var-" + (i + 1), TsData.of(start, X.column(i)));
                vars[i] = Variable.variable("var-" + (i + 1), uvar).withCoefficient(Parameter.estimated(coef.get(i)));
            }
            LightBasicStructuralModel.Description description = LightBasicStructuralModel.Description.builder()
                    .series(sc)
                    .logTransformation(false)
                    .lengthOfPeriodTransformation(LengthOfPeriodType.None)
                    .specification(bsm.finalSpecification(false))
                    .variables(vars)
                    .build();

            LightBasicStructuralModel<Object> model = LightBasicStructuralModel.builder()
                    .description(description)
                    .estimation(estimation)
                    .bsmDecomposition(bsm.decompose())
                    .build();
            return new StsResults(preprocessing, model, log);
        } catch (Exception err) {
            log.error(err);
            return null;
        }
    }

}
