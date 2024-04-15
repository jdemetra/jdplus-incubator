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
package jdplus.sts.base.core.splines;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarUtility;

/**
 * We suppose in this solution that day 58 (in base 0) appears twice in leap
 * years -> All years have 365 days A cycle starts at the first of January We
 * have to specify the start year and the positions of the days corresponding to
 * the nodes.
 *
 * For a more flexible solution, days should be identified by doubles in the
 * future
 *
 * @author palatej
 */
@lombok.Value
public class DailySpline implements SplineDefinition {

    int startYear;
    int[] days;

    @Override
    public double getPeriod() {
        return 365.0;
    }

    @Override
    public DoubleSeq nodes() {
        return DoubleSeq.onMapping(days.length, i -> days[i]);
    }

    @Override
    public IntSeq observations(int period) {
        // unoptimized code !
        int start = 0;
        int y = startYear + period, ycur = startYear;
        while (ycur < y) {
            start += CalendarUtility.isLeap(ycur) ? 366 : 365;
            ++ycur;
        }
        boolean lp = CalendarUtility.isLeap(y);
        return lp ? new DailyIntSeq(start) : IntSeq.sequential(start, start + 365);
    }

    private static final int LCYCLE = 4 * 365 + 1;

    @Override
    public int cycleFor(final int obs) {
        int obsc = obs % LCYCLE;
        int qobs = 4 * (obs / LCYCLE);
        int y = startYear;
        boolean leap = false;
        int i = 0;
        do {
            int n = 365;
            if (!leap) {
                leap = CalendarUtility.isLeap(y);
                if (leap) {
                    n = 366;
                }
            }
            if (obsc < n) {
                return qobs + i;
            }
            obsc -= n;
            ++y;
            ++i;
        } while (true);

    }

    private static class DailyIntSeq implements IntSeq {

        private final int start;

        DailyIntSeq(int start) {
            this.start = start;
        }

        @Override
        public int length() {
            return 366;
        }

        @Override
        public int pos(int idx) {
            return idx > 58 ? start + idx - 1 : start + idx;
        }

    }

}
