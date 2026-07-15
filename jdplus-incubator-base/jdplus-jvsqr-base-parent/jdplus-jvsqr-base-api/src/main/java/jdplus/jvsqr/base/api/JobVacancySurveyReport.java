/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.jvsqr.base.api;

import java.util.Arrays;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class JobVacancySurveyReport {

    int period;
    int nobs;
    TsPeriod start, end;
    boolean logs;
    int p, d, q, bp, bd, bq;
    boolean leapYear;
    boolean easter;
    int ntd;
    int noutliers;
    RegressionItem[] outliers;
    StatisticalTest ftd;
    StatisticalTest residualSeasonality;
    StatisticalTest residualTradingDaysEffect;
    double QStat;
    int finalHenderson;
    int stage2Henderson;
    String[] seasonalFilters;
    String JdQuality;
    TsData irregular;
    double maxAdjustment;
//    
    double dsaAC1;
    StatisticalTest fLin, kwLin, qsLin;

    boolean seasonality;

    public String csa() {
        if (seasonality) {
            if (leapYear || ntd > 0 || easter) {
                return "SCA";
            } else {
                return "SA";
            }
        } else {
            if (leapYear || ntd > 0 || easter) {
                return "CA";
            } else {
                return "NSA";
            }
        }
    }

    private double testScore(StatisticalTest test) {
        if (test == null) {
            return 0;
        } else if (test.getPvalue() < 0.05) {
            return 1;
        } else if (test.getPvalue() < 0.1) {
            return .5;
        } else {
            return 0;
        }
    }

    public double fSeasTests() {
        return testScore(fLin) + testScore(kwLin) + testScore(qsLin);

    }

    public String arima() {
        StringBuilder builder = new StringBuilder();
        builder.append('(').append(p).append(' ').append(d).append(' ').append(q).append(')');
        builder.append('(').append(bp).append(' ').append(bd).append(' ').append(bq).append(')');
        return builder.toString();
    }

    public JobVacancySurveyReport(Explorable rslts) {

        String adjust = rslts.getData(ADJUST, String.class);
        boolean adj = adjust != null && !adjust.equals("None");

        Double qstat = rslts.getData(X13_Q, Double.class);

        period = rslts.getData(PERIOD, Integer.class);
        nobs = rslts.getData(NOBS, Integer.class);
        start = rslts.getData(START, TsPeriod.class);
        end = rslts.getData(END, TsPeriod.class);
        p = rslts.getData(P, Integer.class);
        d = rslts.getData(D, Integer.class);
        q = rslts.getData(Q, Integer.class);
        bp = rslts.getData(BP, Integer.class);
        bd = rslts.getData(BD, Integer.class);
        bq = rslts.getData(BQ, Integer.class);
        leapYear = rslts.getData(LP, Integer.class) > 0 || adj;
        easter = rslts.getData(NMH, Integer.class) > 0;
        ntd = rslts.getData(NTD, Integer.class);
        noutliers = rslts.getData(NOUT, Integer.class);
        logs = rslts.getData(LOG, Integer.class) > 0;
        ftd = rslts.getData(FTD, StatisticalTest.class);
        fLin = rslts.getData(FSEASLIN, StatisticalTest.class);
        qsLin = rslts.getData(QSSEASLIN, StatisticalTest.class);
        kwLin = rslts.getData(KWSEASLIN, StatisticalTest.class);
        seasonality = rslts.getData(SEASONAL, Integer.class) > 0;
        QStat = qstat == null ? Double.NaN : qstat;
        outliers = new RegressionItem[noutliers];
        if (noutliers > 0) {
            fill(outliers, rslts);
        }
        residualSeasonality = rslts.getData(FSEASSA, StatisticalTest.class);
        residualTradingDaysEffect = rslts.getData(FTDSA, StatisticalTest.class);
        Integer H = rslts.getData(X13_HENDERSON, Integer.class);
        finalHenderson = H == null ? 0 : H;
        H = rslts.getData(X13_HENDERSON_D7, Integer.class);
        stage2Henderson = H == null ? 0 : H;
        seasonalFilters = rslts.getData(X13_SF, String[].class);
        TsData y = rslts.getData(Y, TsData.class);
        TsData sa = rslts.getData(SA, TsData.class);
        TsData trend = rslts.getData(TREND, TsData.class);
        irregular = TsData.subtract(sa, trend);
        TsData yadj = TsData.divide(TsData.subtract(y, sa), y);
        yadj = yadj.fn(x -> !Double.isFinite(x) ? 0 : Math.abs((x)));
        maxAdjustment = 100 * yadj.getValues().max();
        JdQuality = rslts.getData(QUALITY, String.class);
        Double ac1 = rslts.getData(SA_AC1, Double.class);
        if (ac1 == null) {
            dsaAC1 = Double.NaN;
        } else {
            dsaAC1 = ac1;
        }
    }

    private static void fill(RegressionItem[] outliers, Explorable rslts) {
        for (int i = 1; i <= outliers.length; ++i) {
            RegressionItem cur = rslts.getData("regression.outlier(" + i + ")", RegressionItem.class);
            outliers[i - 1] = cur;
        }
        if (outliers.length > 1) {
            Arrays.<RegressionItem>sort(outliers, (RegressionItem o1, RegressionItem o2) -> abst(o1) > abst(o2) ? -1 : 1);
        }
    }

    private static double abst(RegressionItem item) {
        return item.getStdError() == 0 ? 0
                : Math.abs(item.getCoefficient()) / item.getStdError();
    }

    private static final String PERIOD = "period", NOBS = "span.n", START = "span.start", END = "span.end", LOG = "log";
    private static final String P = "arima.p", D = "arima.d", Q = "arima.q", BP = "arima.bp", BD = "arima.bd", BQ = "arima.bq";
    private static final String SEASONAL = "seasonal", ADJUST = "adjust", LP = "regression.nlp", NMH = "regression.nmh", NTD = "regression.ntd", NOUT = "regression.nout";
    private static final String FTD = "diagnostics.td_f_ma", FSEASLIN = "diagnostics.seas-lin-f", KWSEASLIN = "diagnostics.seas-lin-kw", QSSEASLIN = "diagnostics.seas-lin-qs", FSEASSA = "diagnostics.seas-sa-f", FTDSA = "diagnostics.td-sa-all";
    private static final String X13_Q = "m-statistics.q", X13_HENDERSON = "decomposition.trend-filter", X13_HENDERSON_D7 = "decomposition.d7-trend-filter", X13_SF = "decomposition.seasonal-filters";
    private static final String QUALITY = "quality.summary", Y = "decomposition.y_cmp", SA = "decomposition.sa_cmp", TREND = "decomposition.t_cmp", SA_AC1 = "diagnostics.seas-sa-ac1";
}
