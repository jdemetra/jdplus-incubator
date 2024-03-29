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
package jdplus.advancedsa.base.core.movingtd;

import java.util.Arrays;
import java.util.Optional;
import jdplus.advancedsa.base.api.movingtd.TimeVaryingSpec;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ExtendedSsfData;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
public class TimeVaryingEstimator {

    private final TimeVaryingSpec spec;
    private RegSarimaModel model;
    private FastMatrix td, tdCoefficients, stdeTdCoefficients;
    private TsData partialLinearizedSeries, tdEffect;
    private TsDomain domain;
    private Variable[] variables;
    private SarimaModel arima0, arima;
    private double aic, aic0;

    /**
     * *
     *
     * @param spec
     */
    public TimeVaryingEstimator(TimeVaryingSpec spec) {
        this.spec = spec;
    }

    public TimeVaryingCorrection process(RegSarimaModel model, int bcasts, int fcasts) {
        this.model = model;
        try {
            if (!processInitialModel(bcasts, fcasts)) {
                return null;
            }
            if (!compute(bcasts, fcasts)) {
                return null;
            }
            return TimeVaryingCorrection.builder()
                    .aic(aic)
                    .aic0(aic0)
                    .arima(arima)
                    .arima0(arima0)
                    .partialLinearizedSeries(partialLinearizedSeries)
                    .tdEffect(tdEffect)
                    .tdCoefficients(tdCoefficients)
                    .stdeTdCoefficients(stdeTdCoefficients)
                    .build();

        } catch (Exception err) {
            return null;
        } finally {
            cleanUp();
        }
    }

//    public static FastMatrix generateVar(DayClustering dc, boolean onContrast) {
//        int groupsCount = dc.getGroupsCount() - 1;
//        FastMatrix M = FastMatrix.square(groupsCount);
//        M.diagonal().set(1);
//        if (!onContrast) {
//            int[] D = new int[groupsCount];
//            int d = dc.getGroupCount(0);
//            int sd2 = d * d;
//            for (int i = 0; i < groupsCount; ++i) {
//                d = dc.getGroupCount(i + 1);
//                sd2 += d * d;
//                D[i] = d;
//            }
//            double d2 = sd2 / 49.0;
//            M.add(d2);
//            for (int i = 0; i < groupsCount; ++i) {
//                M.add(i, i, -2 * D[i] / 7.0);
//                for (int j = 0; j < i; ++j) {
//                    double c = (D[i] + D[j]) / 7.0;
//                    M.add(i, j, -c);
//                    M.add(j, i, -c);
//                }
//            }
//        }
//        return M;
//    }
    public static FastMatrix generateVar(DayClustering dc, boolean onContrast) {
        // q(i)=b(i)*D(i), var(b(i))= 1/(D(i)*D(i))->var(q(i)) =1
        // m=avg(q(i)) = sum(q(i))/7, var(m) = ngroups/49
        // p(i) = b(i)-m
        // cov(p(i), p(j)) = ngroups/49 - (1/D(i) + 1/D(j))/7
        // var(pi) = ngroups/49 + 1/(D(i)*D(i)) - 2/(D(i)*7)
        int groupsCount = dc.getGroupsCount() - 1;
        FastMatrix M = FastMatrix.square(groupsCount);
        int[] D = new int[groupsCount];
        for (int i = 0; i < groupsCount; ++i) {
            D[i] = dc.getGroupCount(i + 1);
        }
        M.diagonal().set(i -> 1.0 / (D[i] * D[i]));

        if (!onContrast) {
            double vm = (1 + groupsCount) / 49.0;
            M.add(vm);
            for (int i = 0; i < groupsCount; ++i) {
                M.add(i, i, -2.0 / (D[i] * 7.0));
                for (int j = 0; j < i; ++j) {
                    double c = (1.0 / D[i] + 1.0 / D[j]) / 7.0;
                    M.add(i, j, -c);
                    M.add(j, i, -c);
                }
            }
        }
        return M;
    }

    private boolean compute(int bcasts, int fcasts) {
        try {
            FastMatrix cov = null;
            Optional<Variable> otd = Arrays.stream(variables)
                    .filter(var -> var.isFree() && ModellingUtility.isTradingDays(var)).findFirst();
            Variable v = otd.orElseThrow();
            if (v.getCore() instanceof GenericTradingDaysVariable gtd) {
                cov = generateVar(gtd.getClustering(), spec.isOnContrast());
            } else if (v.getCore() instanceof HolidaysCorrectedTradingDays htd) {
                cov = generateVar(htd.getClustering(), spec.isOnContrast());
            }

            ISsfData data = new SsfData(partialLinearizedSeries.getValues());
            if (bcasts > 0 || fcasts > 0) {
                data = new ExtendedSsfData(data, bcasts, fcasts);
            }
            // step 0 fixed model
            int period = partialLinearizedSeries.getAnnualFrequency();
            TDvarData tdVar0 = new TDvarData(SarimaModel.builder(SarimaOrders.airline(period)).setDefault(0, -.6).build(), td, null);
            TDvarMapping mapping0 = new TDvarMapping(tdVar0);
            DoubleSeq p0 = mapping0.getDefaultParameters();
            // Create the function
            SsfFunction<TDvarData, Ssf> fn0 = SsfFunction.<TDvarData, Ssf>builder(data, mapping0, q -> q.toSsf())
                    .useSqrtInitialization(true)
                    .useScalingFactor(true)
                    .useLog(false)
                    .build();
            LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer.builder()
                    .functionPrecision(1e-9)
                    .build();
            min.minimize(fn0.evaluate(p0));
            SsfFunctionPoint<TDvarData, Ssf> rfn0 = (SsfFunctionPoint<TDvarData, Ssf>) min.getResult();
            arima0 = rfn0.getCore().getArima();

            TDvarData tdVar1 = new TDvarData(arima0, td, cov);
            TDvarMapping mapping1 = new TDvarMapping(tdVar1);
            // Create the function
            SsfFunction<TDvarData, Ssf> fn1 = SsfFunction.<TDvarData, Ssf>builder(data, mapping1, q -> q.toSsf())
                    .useSqrtInitialization(true)
                    .useScalingFactor(true)
                    .useLog(false)
                    .build();
            min.minimize(fn1.evaluate(mapping1.getDefaultParameters()));
            SsfFunctionPoint<TDvarData, Ssf> rfn1 = (SsfFunctionPoint<TDvarData, Ssf>) min.getResult();
            arima = rfn1.getCore().getArima();

            ISsf ssf;
            aic0 = rfn0.getLikelihood().AIC(2);
            aic = rfn1.getLikelihood().AIC(3);
//            if (aic + spec.getDiffAIC() < aic0) {
            ssf = rfn1.getSsf();
//            } else {
//                ssf = rfn0.getSsf();
//            }

            DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, true, true);

            FastMatrix c = FastMatrix.make(td.getRowsCount(), td.getColumnsCount());
            FastMatrix ec = FastMatrix.make(td.getRowsCount(), td.getColumnsCount());

            int del = period + 2;
            for (int i = 0; i < td.getColumnsCount(); ++i) {
                c.column(i).copy(sr.getComponent(del + i));
                ec.column(i).copy(sr.getComponentVariance(del + i).fn(z -> z <= 0 ? 0 : Math.sqrt(z)));
            }

            tdCoefficients = c;
            stdeTdCoefficients = ec;

            double[] t = new double[c.getRowsCount()];
            for (int i = 0; i < t.length; ++i) {
                t[i] = c.row(i).dot(td.row(i));
            }
            tdEffect = TsData.ofInternal(this.domain.getStartPeriod(), t);

            return true;
        } catch (Exception err) {
            return false;
        }
    }

    private void cleanUp() {
        model = null;
        td = null;
        tdCoefficients = null;
        partialLinearizedSeries = null;
        tdEffect = null;
        domain = null;
        variables = null;

    }

    private boolean processInitialModel(int bcasts, int fcasts) {
        variables = model.getDescription().getVariables();
        Optional<Variable> otd = Arrays.stream(variables)
                .filter(var -> !var.isPreadjustment() && ModellingUtility.isTradingDays(var)).findFirst();
        domain = model.getDescription().getDomain().extend(bcasts, fcasts);
        Variable vtd = otd.orElseThrow();
        if (!vtd.isFree()) {
            return false;
        }
        td = Regression.matrix(domain, vtd.getCore());
        if (td.isEmpty()) {
            return false;
        }
        TsData tdfixed = model.regressionEffect(domain, var -> ModellingUtility.isTradingDays(var));
        TsData ls = model.linearizedSeries();
        partialLinearizedSeries = TsData.add(ls, tdfixed);
        return true;
    }

    @lombok.Value
    private static class TDvarData {

        @lombok.NonNull
        private SarimaModel arima;
        @lombok.NonNull
        private FastMatrix td; // regression variable
        private FastMatrix nvar; // unscaled covariance matrix for var coefficients

        boolean hasVar() {
            return nvar != null;
        }

        Ssf toSsf() {
            Ssf ssf = SsfArima.ssf(arima);
            if (nvar != null) {
                return RegSsf.timeVaryingSsf(ssf, td, nvar);
            } else {
                return RegSsf.ssf(ssf, td);
            }

        }
    }

    private static class TDvarMapping implements IParametricMapping<TDvarData> {

        private static final SarimaMapping AIRLINEMAPPING;

        static {
            SarimaOrders spec = SarimaOrders.airline(12);
            AIRLINEMAPPING = SarimaMapping.of(spec);
        }

        private final TDvarData data;

        TDvarMapping(TDvarData data) {
            this.data = data;
        }

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            if (data.getNvar() != null) {
                return inparams.get(2) >= 0 && inparams.get(2) < 10 && AIRLINEMAPPING.checkBoundaries(inparams.extract(0, 2));
            } else {
                return AIRLINEMAPPING.checkBoundaries(inparams.extract(0, 2));
            }
        }

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.epsilon(inparams, idx);
            }
            return Math.max(inparams.get(2) * .001, 1e-9);
        }

        @Override
        public int getDim() {
            return data.getNvar() == null ? 2 : 3;
        }

        @Override
        public double lbound(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.lbound(idx);
            } else {
                return 0;
            }
        }

        @Override
        public double ubound(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.ubound(idx);
            } else {
                return 10;
            }
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            ParamValidation pv = ParamValidation.Valid;
            if (data.hasVar() && ioparams.get(2) < 0) {
                pv = ParamValidation.Changed;
                ioparams.set(2, Math.min(10, -ioparams.get(2)));
            }
            if (data.hasVar() && ioparams.get(2) > 10) {
                pv = ParamValidation.Changed;
                ioparams.set(2, 10);
            }
            ParamValidation pv2 = AIRLINEMAPPING.validate(ioparams.extract(0, 2));
            if (pv == ParamValidation.Valid && pv2 == ParamValidation.Valid) {
                return ParamValidation.Valid;
            }
            if (pv == ParamValidation.Invalid || pv2 == ParamValidation.Invalid) {
                return ParamValidation.Invalid;
            }
            return ParamValidation.Changed;
        }

        @Override
        public String getDescription(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.getDescription(idx);
            } else {
                return "noise stdev";
            }
        }

        @Override
        public TDvarData map(DoubleSeq p) {
            SarimaModel arima = data.getArima().toBuilder()
                    .theta(1, p.get(0))
                    .btheta(1, p.get(1))
                    .build();
            FastMatrix v = null;
            if (data.hasVar()) {
                double nv = p.get(2);
                v = data.getNvar().deepClone();
                v.mul(nv);
            }
            return new TDvarData(arima, data.getTd(), v);
        }

        @Override
        public DoubleSeq getDefaultParameters() {
            double[] p = new double[getDim()];
            p[0] = -.6;
            p[1] = -.6;
            if (p.length > 2) {
                p[2] = .1;
            }
            return DoubleSeq.of(p);
        }
    }

}
