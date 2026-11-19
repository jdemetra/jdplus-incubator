/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions preliminarySpecOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy preliminarySpecOf the Licence at:
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

import jdplus.advancedsa.base.api.tdarima.Orders;
import jdplus.advancedsa.base.api.tdarima.TdEstimationSpec;
import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.advancedsa.base.core.regarima.FastKernel;
import jdplus.sa.base.core.PreliminaryChecks;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.modelling.regular.SeriesSpec;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 * @author Jean Palate
 */
public class TdModellingKernel {

    private static PreliminaryChecks.Tool preliminarySpecOf(TdModelSpec spec) {

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

    public TdModellingKernel() {}

    //    private DoubleSeq curP, curDelta;
    //    private FastMatrix curPCov, curDeltaCov;
    //
    /**
     * Raw estimation
     *
     * @param s variable
     * @param spec
     * @param context
     * @param log
     * @return
     */
    public TdModellingResults process(TsData s, TdModelSpec spec, ModellingContext context, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        try {
            spec = spec.setFrequency(s.getAnnualFrequency());
            // Step 0. Preliminary checks
            TsData sc = preliminarySpecOf(spec).check(s, log);
            // Step 1. Preprocessing
            RegSarimaModel preprocessing = null;
            FastMatrix X = null;
            DoubleSeq y = s.getValues();
            int period = s.getAnnualFrequency();
            //            X12plusPreadjustment preadjustment;
            // TODO : backcasts/forecasts
            Orders orders = Orders.airline();
            FastKernel preprocessor = FastKernel.of(spec.getPreprocessing(), context);
            if (preprocessor != null) {
                // we have to remove fixed effects preliminarySpecOf the transformed series
                preprocessing = preprocessor.process(sc, log);
                sc = preprocessing.transformedSeries();
                TsDomain domain = s.getDomain();
                TsData preadj = preprocessing.preadjustmentEffect(domain, v -> true);
                sc = TsData.subtract(sc, preadj);
                X = preprocessing.regressionMatrix(domain);
                y = sc.getValues();

                orders = Orders.of(preprocessing.specification());
            }
            TdEstimationSpec tdspec = spec.getTdarima();
            if (spec.isUsingPreprocessing()) {
                tdspec = tdspec.withOrders(orders);
            } else {
                tdspec = tdspec.withOrders(spec.getTdarima().getTdspec().getOrders());
            }

            TdKernel tdkernel = TdKernel.of(tdspec);
            TdResults drslt = tdkernel.process(y, period, false, X, log);

            TdDecompositionKernel dkernel = new TdDecompositionKernel();
            TdDecompositionResults frslt = dkernel.process(preprocessing, drslt.finalResults(), log);
            return TdModellingResults.builder()
                    .preprocessing(preprocessing)
                    .tdarima(drslt)
                    //                    .benchmarking(bench)
                    //                    .diagnostics(X13plusDiagnostics.preliminarySpecOf(preprocessing,
                    // preadjustment, xr, finals))
                    .decomposition(frslt)
                    .log(log)
                    .build();

        } catch (Exception err) {
            log.error(err);
            return null;
        }
    }
}
