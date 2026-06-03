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
package jdplus.eurostat.base.api;

import jdplus.sa.base.api.diagnostics.CombinedSeasonalityTest;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class JobVacancySurveyReport {
    
    String series;
    String method;
    int period;
    int nobs;
    TsPeriod start, end;
    boolean logs;
    int p, d, q, bp, bd, bq;
    boolean leapYear;
    boolean easter;
    int ntd;
    int noutliers;
    String[] mainOutliers;
    CombinedSeasonalityTest siTest;
    StatisticalTest ftd;
    StatisticalTest residualSeasonality;
    StatisticalTest residualTradingDaysEffect;
    double QStat;
    int finalHenderson;
    int stage2Henderson;
    String sesonalFilter;
    String JdQuality;
    double maxAdjustment;
    
    double dsaAC1;
    StatisticalTest dsaAutoCorrelation;
    
    boolean seasonality;
     
    public String csa(){
        if (seasonality){
            if (leapYear || ntd>0 || easter)
                return "CSA";
            else
                return "SA";
        }else{
             if (leapYear || ntd>0 || easter)
                return "CA";
            else
                return "NSA";
        }
    }
    
    private double saValue(StatisticalTest test){
        if (test == null)
            return 0;
        else if (test.getPvalue()<0.05)
            return 1;
        else if (test.getPvalue()<0.1)
            return .5;
        else
            return 0;
        
    }
    
    public String arima(){
        StringBuilder builder=new StringBuilder();
        builder.append('(').append(p).append(' ').append(d).append(' ').append(q).append(')');
        builder.append('(').append(bp).append(' ').append(bd).append(' ').append(bq).append(')');
        return builder.toString();
    }
    
}
