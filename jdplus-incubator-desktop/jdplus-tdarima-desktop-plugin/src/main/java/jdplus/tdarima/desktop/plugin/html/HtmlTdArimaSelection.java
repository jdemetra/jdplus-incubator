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
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlLikelihood;

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
        stream.write(new HtmlLikelihood(start.getLikelihood()));
        stream.write(HtmlTag.HEADER2, "Arima model");
        writeArima(stream, start, summary);
        stream.write(HtmlTag.LINEBREAK);
        if (!results.isTimeVarying()) {
            stream.write("No significant time dependent effect detected");
            return;
        }
        stream.write(HtmlTag.HEADER1, "Final model");
        writeTdArima(stream, results.finalResults(), true);
        stream.write(HtmlTag.LINEBREAK);
    }
}
