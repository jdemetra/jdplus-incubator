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
package jdplus.sts.base.core.msts;

import jdplus.sts.base.core.splines.DailySpline;
import jdplus.sts.base.core.splines.RegularSpline;
import jdplus.sts.base.core.splines.SplineData;
import jdplus.sts.base.core.msts.internal.ArItem;
import jdplus.sts.base.core.msts.internal.ArItem2;
import jdplus.sts.base.core.msts.internal.ArimaItem;
import jdplus.sts.base.core.msts.internal.ArmaItem;
import jdplus.sts.base.core.msts.internal.CycleItem;
import jdplus.sts.base.core.msts.internal.LocalLevelItem;
import jdplus.sts.base.core.msts.internal.LocalLinearTrendItem;
import jdplus.sts.base.core.msts.internal.MsaeItem;
import jdplus.sts.base.core.msts.internal.MsaeItem2;
import jdplus.sts.base.core.msts.internal.MsaeItem3;
import jdplus.sts.base.core.msts.internal.NoiseItem;
import jdplus.sts.base.core.msts.internal.RegressionItem;
import jdplus.sts.base.core.msts.internal.SaeItem;
import jdplus.sts.base.core.msts.internal.SarimaItem;
import jdplus.sts.base.core.msts.internal.SeasonalComponentItem;
import jdplus.sts.base.core.msts.internal.TdRegressionItem;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.sts.base.core.msts.internal.PeriodicItem;
import jdplus.sts.base.core.msts.internal.VarLocalLevelItem;
import jdplus.sts.base.core.msts.internal.VarLocalLinearTrendItem;
import jdplus.sts.base.core.msts.internal.VarSeasonalComponentItem;
import jdplus.sts.base.core.msts.internal.VarNoiseItem;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.sts.base.core.msts.internal.SplineItem;
import jdplus.sts.base.core.msts.internal.VarRegressionItem;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class AtomicModels {

    public StateItem arma(final String name, double[] ar, boolean fixedar, double[] ma, boolean fixedma, double var, boolean fixedvar) {
        return new ArmaItem(name, ar, fixedar, ma, fixedma, var, fixedvar);
    }

    public StateItem sarima(final String name, int period, int[] orders, int[] seasonal, double[] parameters, boolean fixed, double var, boolean fixedvar) {
        return new SarimaItem(name, period, orders, seasonal, parameters, fixed, var, fixedvar);
    }

    public StateItem localLevel(String name, final double lvar, final boolean fixed, final double initial) {
        return new LocalLevelItem(name, lvar, fixed, initial);
    }

    public StateItem localLevel(String name, final double[] lstd, final double lscale, final boolean fixed, final double initial) {
        return new VarLocalLevelItem(name, lstd, lscale, fixed, initial);
    }

    public StateItem localLinearTrend(final String name, double lvar, double svar, boolean lfixed, boolean sfixed) {
        return new LocalLinearTrendItem(name, lvar, svar, lfixed, sfixed);
    }

    public StateItem localLinearTrend(final String name, double[] lstd, double[] sstd, double lscale, double sscale, boolean lfixed, boolean sfixed) {
        return new VarLocalLinearTrendItem(name, lstd, sstd, lscale, sscale, lfixed, sfixed);
    }

    public StateItem seasonalComponent(String name, String smodel, int period, double seasvar, boolean fixed) {
        return new SeasonalComponentItem(name, smodel, period, seasvar, fixed);
    }

    public StateItem seasonalComponent(String name, String smodel, int period, double[] std, double scale, boolean fixed) {
        return new VarSeasonalComponentItem(name, smodel, period, std, scale, fixed);
    }

    public StateItem noise(String name, double var, boolean fixed) {
        return new NoiseItem(name, var, fixed);
    }

    public StateItem noise(String name, double[] std, double scale, boolean fixed) {
        return new VarNoiseItem(name, std, scale, fixed);
    }

    public StateItem regression(String name, Matrix x) {
        return new RegressionItem(name, x, null, true);
    }

    public StateItem timeVaryingRegression(String name, Matrix x, double var, boolean fixed) {
        return new RegressionItem(name, x, new double[]{var}, fixed);
    }

    public StateItem timeVaryingRegression(String name, Matrix x, final double[] vars, final boolean fixed) {
        return new RegressionItem(name, x, vars, fixed);
    }

    public StateItem timeVaryingRegression(String name, double[] x, final double[] stde, final double scale, final boolean fixed) {
        return new VarRegressionItem(name, x, stde, scale, fixed);
    }

    public StateItem tdRegression(String name, TsDomain domain, int[] groups, final boolean contrast, final double var, final boolean fixed) {
        return new TdRegressionItem(name, domain, groups, contrast, var, fixed);
    }

    public StateItem ar(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, boolean zeroinit) {
        return new ArItem(name, ar, fixedar, var, fixedvar, nlags, zeroinit);
    }

    public StateItem sae(String name, double[] ar, boolean fixedar, int lag, boolean zeroinit) {
        return new SaeItem(name, ar, fixedar, lag, zeroinit);
    }

    public StateItem waveSpecificSurveyError(String name, int nwaves, Matrix ar, boolean fixedar, int lag) {
        return new MsaeItem(name, nwaves, ar, fixedar, lag);
    }

    public StateItem waveSpecificSurveyError(String name, double[] var, boolean fixedVar, Matrix ar, boolean fixedar, int lag) {
        return new MsaeItem2(name, var, fixedVar, ar, fixedar, lag);
    }

    public StateItem waveSpecificSurveyError(String name, double[] var, boolean fixedVar, double[] ar, boolean fixedar, Matrix k, int lag) {
        return new MsaeItem3(name, var, fixedVar, ar, fixedar, k, lag);
    }

    public StateItem ar(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, int nfcasts) {
        return new ArItem2(name, ar, fixedar, var, fixedvar, nlags, nfcasts);
    }

    public StateItem arima(String name, double[] ar, boolean fixedar, double[] diff, double[] ma, boolean fixedma, double var, boolean fixedvar) {
        return new ArimaItem(name, ar, fixedar, diff, ma, fixedma, var, fixedvar);
    }

    public StateItem cycle(String name, double dumpingFactor, double cyclicalPeriod, boolean fixedcycle, double cvar, boolean fixedvar) {
        return new CycleItem(name, dumpingFactor, cyclicalPeriod, fixedcycle, cvar, fixedvar);
    }

    public StateItem periodicComponent(String name, double period, int[] k, double cvar, boolean fixedvar) {
        return new PeriodicItem(name, period, k, cvar, fixedvar);
    }

    public StateItem regularSplines(String name, double period, int startpos, double cvar, boolean fixedvar) {
        RegularSpline rs = RegularSpline.of(period);
        SplineData sd = new SplineData(rs);
        return new SplineItem(name, sd, startpos, cvar, fixedvar);
    }

    public StateItem regularSplines(String name, double period, int nnodes, int startpos, double cvar, boolean fixedvar) {
        RegularSpline rs = RegularSpline.of(period, nnodes);
        SplineData sd = new SplineData(rs);
        return new SplineItem(name, sd, startpos, cvar, fixedvar);
    }

    public StateItem regularSplines(String name, double period, double[] nodes, int startpos, double cvar, boolean fixedvar) {
        RegularSpline rs = RegularSpline.of(period, DoubleSeq.of(nodes));
        SplineData sd = new SplineData(rs);
        return new SplineItem(name, sd, startpos, cvar, fixedvar);
    }

    public StateItem dailySplines(String name, int startYear, int[] pos, int startpos, double cvar, boolean fixedvar) {
        DailySpline rs = new DailySpline(startYear, pos);
        SplineData sd = new SplineData(rs);
        return new SplineItem(name, sd, startpos, cvar, fixedvar);
    }
}
