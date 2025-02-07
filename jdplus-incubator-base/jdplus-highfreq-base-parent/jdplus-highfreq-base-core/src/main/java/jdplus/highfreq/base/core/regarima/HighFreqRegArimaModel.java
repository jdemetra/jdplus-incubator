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
package jdplus.highfreq.base.core.regarima;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.stats.ProbabilityType;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.Constant;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.api.timeseries.regression.ResidualsType;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.dstats.T;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.toolkit.base.core.modelling.LightweightLinearModel;
import jdplus.toolkit.base.core.modelling.Residuals;
import jdplus.toolkit.base.core.modelling.regression.RegressionDesc;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
import jdplus.toolkit.base.core.stats.tests.NiidTests;

/**
 *
 * @author PALATEJ
 * @param <S>
 * @param <M>
 */
@lombok.Value
@lombok.Builder
public class HighFreqRegArimaModel<S extends IArimaModel, M extends ArimaDescription<S>>  implements GeneralLinearModel<M>, GenericExplorable {

    private static final MissingValueEstimation[] NOMISSING = new MissingValueEstimation[0];

    public static <S extends IArimaModel, M extends ArimaDescription<S>> HighFreqRegArimaModel of(ModelDescription<S, M> description, RegArimaEstimation<S> estimation, ProcessingLog log) {

        M stochasticSpec = description.getStochasticSpec();
        int free = stochasticSpec.freeParametersCount();
        RegArimaModel<S> model = estimation.getModel();
        ConcentratedLikelihoodWithMissing ll = estimation.getConcentratedLikelihood();

        List<Variable> vars = description.variables().sequential().collect(Collectors.toList());
        int nvars = (int) vars.size();
        if (description.isMean()) {
            ++nvars;
        }
        Variable[] variables = new Variable[nvars];
        DoubleSeqCursor cursor = estimation.getConcentratedLikelihood().coefficients().cursor();
        DoubleSeqCursor.OnMutable diag = estimation.getConcentratedLikelihood().unscaledCovariance().diagonal().cursor();
        int df = ll.degreesOfFreedom() - free;
        double vscale = ll.ssq() / df;
        T tstat = new T(df);

        int k = 0, pos = 0;

        List<RegressionDesc> regressionDesc = new ArrayList<>();
        if (description.isMean()) {
            ITsVariable cur = Constant.C;
            double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
            regressionDesc.add(new RegressionDesc("const", cur, 0, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
            variables[k++] = Variable.variable("const", cur)
                    .withCoefficient(Parameter.estimated(c));
        }
        // fill the free coefficients
        for (Variable var : vars) {
            int nfree = var.freeCoefficientsCount();
            if (nfree == var.dim()) {
                Parameter[] p = new Parameter[nfree];
                for (int j = 0; j < nfree; ++j) {
                    double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
                    p[j] = Parameter.estimated(c);
                    regressionDesc.add(new RegressionDesc(var.getName(), var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
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

        LightweightLinearModel.Description desc = LightweightLinearModel.Description.<M>builder()
                .series(description.getSeries())
                .logTransformation(description.isLogTransformation())
                .lengthOfPeriodTransformation(LengthOfPeriodType.None)
                .variables(variables)
                .stochasticComponent(stochasticSpec)
                .build();

        LogLikelihoodFunction.Point<RegArimaModel<S>, ConcentratedLikelihoodWithMissing> max = estimation.getMax();
        ParametersEstimation pestim;
        if (max == null) {
            pestim = new ParametersEstimation(Doubles.EMPTY, FastMatrix.EMPTY, Doubles.EMPTY, null);
        } else {
            pestim = new ParametersEstimation(max.getParameters(), max.asymptoticCovariance(), max.getScore(), "sarima (true signs)");
        }

        // complete for missings
        int nmissing = ll.nmissing();
        MissingValueEstimation[] missing = NOMISSING;
        if (nmissing > 0) {
            DoubleSeq y = model.getY();
            missing = new MissingValueEstimation[nmissing];
            DoubleSeqCursor cur = ll.missingCorrections().cursor();
            DoubleSeqCursor vcur = ll.missingUnscaledVariances().cursor();
            int[] pmissing = model.missing();
            for (int i = 0; i < nmissing; ++i) {
                double m = cur.getAndNext();
                double v = vcur.getAndNext();
                missing[i] = new MissingValueEstimation(pmissing[i], y.get(pmissing[i]) - m, Math.sqrt(v * vscale));
            }
        }
        DoubleSeq fullRes = RegArimaUtility.fullResiduals(model, ll);
        LightweightLinearModel.Estimation est = LightweightLinearModel.Estimation.builder()
                .domain(description.getEstimationDomain())
                .y(model.getY())
                .X(model.allVariables())
                .coefficients(ll.coefficients())
                .coefficientsCovariance(ll.covariance(free, true))
                .parameters(pestim)
                .statistics(estimation.statistics())
                .missing(missing)
                .logs(log.all())
                .build();

        int period = desc.getSeries().getAnnualFrequency();
        NiidTests niid = NiidTests.builder()
                .data(fullRes)
                .period(period)
                .hyperParametersCount(free)
                .build();
        TsPeriod start=description.getEstimationDomain().getEndPeriod().plus(-fullRes.length());
        Residuals residuals = Residuals.builder()
                .type(ResidualsType.QR_Transformed)
                .res(ll.e())
                .ssq(ll.ssq())
                .n(ll.dim())
                .df(ll.degreesOfFreedom())
                .dfc(ll.degreesOfFreedom()-free)
                .tsres(TsData.of(start, fullRes))
                .test(ResidualsDictionaries.MEAN, niid.meanTest())
                .test(ResidualsDictionaries.SKEW, niid.skewness())
                .test(ResidualsDictionaries.KURT, niid.kurtosis())
                .test(ResidualsDictionaries.DH, niid.normalityTest())
                .test(ResidualsDictionaries.NRUNS, niid.runsNumber())
                .test(ResidualsDictionaries.LRUNS, niid.runsLength())
                .test(ResidualsDictionaries.NUDRUNS, niid.upAndDownRunsNumbber())
                .test(ResidualsDictionaries.LUDRUNS, niid.upAndDownRunsLength())
                .build();

        return HighFreqRegArimaModel.builder()
                .description(desc)
                .estimation(est)
                .residuals(residuals)
                .regressionItems(regressionDesc)
                .independentResiduals(ll.e())
                .build();
    }
    Description<M> description;

    Estimation estimation;

    Residuals residuals;

    DoubleSeq independentResiduals;
    List<RegressionDesc> regressionItems;

    public RegressionItem regressionItem(Predicate<ITsVariable> pred, int item) {
        int curitem = 0;
        for (RegressionDesc desc : regressionItems) {
            if (pred.test(desc.getCore())) {
                if (item == curitem) {
                    return new RegressionItem(desc.getCoef(), desc.getStderr(), desc.getPvalue(), desc.getCore().description(desc.getItem(), estimation.getDomain()));
                } else {
                    ++curitem;
                }
            }
        }
        return null;
    }
}
