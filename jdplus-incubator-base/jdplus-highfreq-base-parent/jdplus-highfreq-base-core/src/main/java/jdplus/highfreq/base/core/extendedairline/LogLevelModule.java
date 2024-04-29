/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.highfreq.base.core.extendedairline;

import jdplus.highfreq.base.core.regarima.ArimaComputer;
import jdplus.highfreq.base.core.regarima.ModelDescription;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.regarima.GlsArimaProcessor;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;

/**
 * Identification of log/level transformation
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class LogLevelModule {

    public static final String LL = "log-level test";

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(LogLevelModule.class)
    public static class Builder {

        private double aiccdiff = -2;
        private double precision = 1e-5;

        /**
         * Precision used in the estimation of the models (1e-5 by default). In
         * most cases, the precision can be smaller than for the estimation of
         * the final model.
         *
         * @param eps
         * @return
         */
        public Builder estimationPrecision(double eps) {
            this.precision = eps;
            return this;
        }

        /**
         * Correction on the AICc of the model in logs. Negative corrections
         * will favour logs (-2 by default) Same as aiccdiff in the original
         * fortran program
         *
         * @param aiccdiff
         * @return
         */
        public Builder aiccLogCorrection(double aiccdiff) {
            this.aiccdiff = aiccdiff;
            return this;
        }

        public LogLevelModule build() {
            return new LogLevelModule(aiccdiff, precision);
        }

    }

    private final double aiccDiff;
    private final double precision;
    private RegArimaEstimation<ArimaModel> level, log;
    private double aiccLevel, aiccLog;

    private LogLevelModule(double aiccdiff, final double precision) {
        this.aiccDiff = aiccdiff;
        this.precision = precision;
    }

    public double getEpsilon() {
        return precision;
    }

    /**
     *
     * @return
     */
    public boolean isChoosingLog() {
        if (log == null) {
            return false;
        } else if (level == null) {
            return true;
        } else {
            // the best is the smallest (default aiccdiff is negative to favor logs)
            return aiccLevel > aiccLog + aiccDiff;
        }
    }

    public ProcessingResult process(ExtendedRegAirlineModelling modelling) {
        clear();
        ProcessingLog logs = modelling.getLog();
        try {
            logs.push(LL);
            ModelDescription<ArimaModel, ExtendedAirlineDescription> model = modelling.getDescription();
            if (model.getSeries().getValues().anyMatch(z -> z <= 0)) {
                return ProcessingResult.Unchanged;
            }
            level = model.estimate(new ArimaComputer(precision, false));

            ModelDescription logmodel = ModelDescription.copyOf(model);
            logmodel.setLogTransformation(true);
            log = logmodel.estimate(new ArimaComputer(precision, false));
            if (level != null) {
                aiccLevel = level.statistics().getAICC();
                logs.info("level", level.statistics());
            }
            if (log != null) {
                aiccLog = log.statistics().getAICC();
                logs.info("Log", log.statistics());
            }
            if (level == null && log == null) {
                return ProcessingResult.Failed;
            }

            if (isChoosingLog()) {
                modelling.set(logmodel, log);
                return ProcessingResult.Changed;
            } else {
                return ProcessingResult.Unchanged;
            }
        } finally {
            logs.pop();
        }
    }

    public TransformationType getTransformation() {
        return this.isChoosingLog() ? TransformationType.Log : TransformationType.None;
    }

    public double getAICcLevel() {
        return this.aiccLevel;
    }

    public double getAICcLog() {
        return this.aiccLog;
    }

    private void clear() {
        log = null;
        level = null;
        aiccLevel = 0;
        aiccLog = 0;
    }

    /**
     * @return the level_
     */
    public RegArimaEstimation<ArimaModel> getLevel() {
        return level;
    }

    /**
     * @return the log_
     */
    public RegArimaEstimation<ArimaModel> getLog() {
        return log;
    }

}
