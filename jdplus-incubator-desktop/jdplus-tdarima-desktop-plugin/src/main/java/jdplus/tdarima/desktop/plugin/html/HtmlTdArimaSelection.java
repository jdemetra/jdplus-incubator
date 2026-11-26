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
package jdplus.tdarima.desktop.plugin.html;

import java.io.IOException;
import jdplus.advancedsa.base.core.tdarima.SarimaResults;
import jdplus.advancedsa.base.core.tdarima.TdResults;
import jdplus.advancedsa.base.core.tdarima.TdSarimaResults;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlStyle;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;

/**
 *
 * @author Jean Palate
 */
public class HtmlTdArimaSelection extends HtmlTdArima {

    private final TdResults results;
    private final boolean summary;

    public HtmlTdArimaSelection(TdResults results) {
        this.results = results;
        this.summary = true;
    }

    public HtmlTdArimaSelection(TdResults results, boolean summary) {
        this.results = results;
        this.summary = summary;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        SarimaResults start = results.getStart();
        stream.write(HtmlTag.HEADER1, "Initial model");
        stream.newLine();
        stream.write(HtmlTag.HEADER2, "Likelihood statistics");
        writeLikelihoodSummary(stream, start.getLikelihood());
        stream.write(HtmlTag.HEADER2, "Arima model");
        writeArima(stream, start, true);
        stream.write(HtmlTag.LINEBREAK);
        for (TdSarimaResults cur : results.getTdResults()) {
            stream.write(HtmlTag.HEADER1, "Model " + cur.getModel().spec().model())
                    .newLine();
            writeLikelihoodSummary(stream, cur.getLikelihood());
            HtmlTdArima.writeTdArima(stream, cur, true);
            stream.newLine();
            writeSimplificationTests(stream, cur);
            stream.write(HtmlTag.LINEBREAK);
        }
        if (!results.isTimeVarying()) {
            stream.write("No significant time dependent effect detected", HtmlStyle.Red, HtmlStyle.Bold);
        }
        //        TdSarimaResults finalResults = results.finalResults();
        //        stream.write(HtmlTag.HEADER1, "Final model");
        //        writeLikelihoodSummary(stream, finalResults.getLikelihood());
    }

    private void writeLikelihoodSummary(HtmlStream stream, LikelihoodStatistics stats) throws IOException {
        stream.write("Number of estimated parameters = ")
                .write(stats.getEstimatedParametersCount())
                .newLines(2);
        stream.write("Loglikelihood = ")
                .write(df6.format(stats.getLogLikelihood()))
                .newLine();
        stream.write("AIC = ").write(df6.format(stats.getAIC())).newLine();
        stream.write("BIC (corrected for length) = ")
                .write(df6.format(stats.getBICC()))
                .newLine();
    }

    private void writeSimplificationTests(HtmlStream stream, TdSarimaResults results) throws IOException {
        stream.write(HtmlTag.HEADER2, "Tests");
        StatisticalTest lr = results.getLikelihoodRatioTest();
        stream.write(HtmlTag.HEADER3, "Likelihood ratio test against the standard ARIMA model");
        stream.write("Distribution: " + lr.getDescription()).newLine();
        stream.write("Value: " + df4.format(lr.getValue())).newLine();
        stream.write("PValue: " + df4.format(lr.getPvalue())).newLines(2);
        StatisticalTest st = results.getStationarityTest();
        if (st != null && st.isValid()) {
            stream.write(HtmlTag.HEADER3, "Stationarity test");
            stream.write("Distribution: " + st.getDescription()).newLine();
            stream.write("Value: " + df4.format(st.getValue())).newLine();
            stream.write("PValue: " + df4.format(st.getPvalue())).newLines(2);
        }
    }
}
