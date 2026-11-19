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
import java.util.ArrayList;
import java.util.List;
import jdplus.advancedsa.base.core.tdarima.SarimaResults;
import jdplus.advancedsa.base.core.tdarima.TdSarimaResults;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.dstats.T;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.Bootstrap4;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTable;
import jdplus.toolkit.desktop.plugin.html.HtmlTableCell;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlLikelihood;

/**
 *
 * @author Jean Palate
 */
public abstract class HtmlTdArima extends AbstractHtmlElement {
    public static void writeArima(HtmlStream stream, SarimaResults start, boolean summary) throws IOException {
        LikelihoodStatistics ll = start.getLikelihood();
        int nobs = ll.getEffectiveObservationsCount(), nparams = ll.getEstimatedParametersCount();
        SarimaModel arima = start.getModel().arima();
        SarimaOrders sspec = arima.orders();
        stream.write('[').write(sspec.toString()).write(']').newLines(2);
        if (sspec.getParametersCount() == 0) {
            return;
        }
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Coefficients").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("T-Stat").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("P[|T| &gt t]").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.close(HtmlTag.TABLEROW);
        int P = sspec.getP();
        DoubleSeqCursor p = start.getParameters().cursor();
        DoubleSeqCursor vars = start.getParametersCovariance().diagonal().cursor();
        double ndf = nobs - nparams;
        T t = new T(ndf);
        List<String> headers = new ArrayList<>();
        for (int j = 0; j < P; ++j) {
            stream.open(HtmlTag.TABLEROW);
            String header = pheader("Phi", j);
            stream.write(new HtmlTableCell(header).withWidth(100));
            double val = p.getAndNext();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            double stde = Math.sqrt(vars.getAndNext());
            if (stde != 0) {
                headers.add(header);
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int BP = sspec.getBp();
        for (int j = 0; j < BP; ++j) {
            stream.open(HtmlTag.TABLEROW);
            String header = pheader("BPhi", j);
            stream.write(new HtmlTableCell(header).withWidth(100));
            double val = p.getAndNext();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            double stde = Math.sqrt(vars.getAndNext());
            if (stde != 0) {
                headers.add(header);
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int Q = sspec.getQ();
        for (int j = 0; j < Q; ++j) {
            stream.open(HtmlTag.TABLEROW);
            String header = pheader("Theta", j);
            stream.write(new HtmlTableCell(header).withWidth(100));
            double val = p.getAndNext();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            double stde = Math.sqrt(vars.getAndNext());
            if (stde != 0) {
                headers.add(header);
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int BQ = sspec.getBq();

        for (int j = 0; j < BQ; ++j) {
            stream.open(HtmlTag.TABLEROW);
            String header = pheader("BTheta", j);
            stream.write(new HtmlTableCell(header).withWidth(100));
            double val = p.getAndNext();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            double stde = Math.sqrt(vars.getAndNext());
            if (stde != 0) {
                headers.add(header);
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);
        if (summary) {
            return;
        }

        Matrix pcov = start.getParametersCovariance();

        if (!pcov.isEmpty()) {
            int size = pcov.getColumnsCount();
            stream.newLines(2);
            stream.write(HtmlTag.HEADER3, "Correlation of the estimates").newLine();
            stream.open(HtmlTag.TABLE);

            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("").withWidth(100));

            for (int i = 0; i < size; ++i) {
                stream.write(new HtmlTableCell(headers.get(i)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);

            for (int i = 0; i < size; ++i) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(headers.get(i)).withWidth(100));
                for (int j = 0; j < size; ++j) {
                    double vi = pcov.get(i, i), vj = pcov.get(j, j);
                    if (vi != 0 && vj != 0) {
                        double val = pcov.get(i, j) / Math.sqrt(vi * vj);
                        stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
                    } else {
                        stream.write(new HtmlTableCell("-").withWidth(100));
                    }
                }
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
            stream.newLine();
        }
    }

    private static String pheader(String name, int pos) {
        StringBuilder header = new StringBuilder();
        header.append(name).append('(').append(pos + 1).append(')');
        return header.toString();
    }

    public static void writeTdArima(HtmlStream stream, TdSarimaResults tdarima, boolean summary) throws IOException {
        LikelihoodStatistics ll = tdarima.getLikelihood();
        stream.write(HtmlTag.HEADER2, "Likelihood statistics");
        stream.write(new HtmlLikelihood(ll));
        int nobs = ll.getEffectiveObservationsCount(), nparams = ll.getEstimatedParametersCount();
        DoubleSeqCursor p = tdarima.getParameters().cursor();
        Matrix pcov = tdarima.getParametersCovariance();
        DoubleSeqCursor pdiag = pcov.diagonal().cursor();
        String[] pnames = tdarima.getParametersNames();

        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Coefficients").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("T-Stat").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("P[|T| &gt t]").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.close(HtmlTag.TABLEROW); //        int P = sspec.getP();

        double ndf = nobs - nparams;
        T t = new T(ndf);
        for (int i = 0; i < pnames.length; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(pnames[i]).withWidth(100));
            double val = p.getAndNext();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            double stde = Math.sqrt(pdiag.getAndNext());
            if (stde != 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
    }
}
