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
package jdplus.eurostat.base.core;

import jdplus.sa.base.api.SaDocument;
import jdplus.toolkit.base.api.processing.Output;
import jdplus.toolkit.base.api.util.Paths;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import jdplus.eurostat.base.api.JobVacancySurveyReport;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.information.formatters.BasicConfiguration;
import jdplus.toolkit.base.api.information.formatters.StringFormatter;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import nbbrd.design.SystemDependent;
import nbbrd.picocsv.Csv;

/**
 *
 * @author Kristof Bayens
 */
public class JvsMatrixOutput implements Output<SaDocument> {

    private static final AtomicReference<Character> CSV_SEPARATOR = new AtomicReference<>(initDefaultCsvSeparator());
    private static final AtomicReference<Locale> LOCALE = new AtomicReference<>(initLocale());

    @SystemDependent
    private static Character initDefaultCsvSeparator() {
        DecimalFormat fmt = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.getDefault());
        fmt.setMaximumFractionDigits(BasicConfiguration.getFractionDigits());
        fmt.setGroupingUsed(false);
        char sep = fmt.getDecimalFormatSymbols().getDecimalSeparator();
        return sep == ',' ? ';' : ',';
    }

    @SystemDependent
    private static Locale initLocale() {
        return Locale.getDefault();
    }

    public static char getCsvSeparator() {
        return CSV_SEPARATOR.get();
    }

    JvsMatrixOutputConfiguration config;
    List<SaDocument> infos;
    private File folder;

    public JvsMatrixOutput(JvsMatrixOutputConfiguration config) {
        this.config = (JvsMatrixOutputConfiguration) config.clone();
    }

    @Override
    public String getName() {
        return JvsMatrixOutputFactory.NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void start(Object context) {
        infos = new ArrayList<>();
        folder = Paths.folderFromContext(config.getFolder(), context);
    }

    @Override
    public void end(Object context) throws Exception {
        String file = Paths.concatenate(folder.getAbsolutePath(), config.getFileName());
        file = Paths.changeExtension(file, "csv");
        Csv.Format csvFormat = Csv.Format.DEFAULT
                .toBuilder()
                .separator(System.lineSeparator())
                .delimiter(CSV_SEPARATOR.get())
                .build();
        try (
                Writer writer = Files.newBufferedWriter(Path.of(file), config.getCharset())) {
            try (Csv.Writer csv = Csv.Writer.of(csvFormat, Csv.WriterOptions.DEFAULT, writer, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
                writeHeaders(csv);
                for (SaDocument cur : infos) {
                    write(csv, cur);
                }
            }
        }
        infos = null;
    }

    @Override
    public void process(SaDocument document) {
        infos.add(document);
    }

    private void writeHeaders(Csv.Writer writer) throws IOException {
        for (String s : HEADERS) {
            writer.writeField(s);
        }
        writer.writeEndOfLine();
    }

    private void write(Csv.Writer writer, SaDocument cur) throws IOException {
        writeRowHeader(writer, cur.getName(), config.isFullName());
        writer.writeField(cur.getSpecification().longDisplay());
        JobVacancySurveyReport.Builder builder = JobVacancySurveyReport.builder();
        fill(builder, cur.getResults());
//        switch (cur.getResults()) {
//
//            case TramoSeatsResults tsrslts ->
//                fill(builder, tsrslts);
//            case X13Results xrslts ->
//                fill(builder, xrslts);
//            default -> {
//            }
//        }
        writeReport(writer, builder.build());
        writer.writeEndOfLine();
    }

//    private void fill(JobVacancySurveyReport.Builder builder, TramoSeatsResults rslts) {
//        RegSarimaModel preprocessing = rslts.getPreprocessing();
//        fill(builder, preprocessing);
//    }
//
//    private void fill(JobVacancySurveyReport.Builder builder, X13Results rslts) {
//        RegSarimaModel preprocessing = rslts.getPreprocessing();
//        if (preprocessing != null) {
//            fill(builder, preprocessing);
//        }
//    }
    private void writeReport(Csv.Writer writer, JobVacancySurveyReport report) throws IOException {
        Locale locale = LOCALE.get();
        NumberFormat ifmt = NumberFormat.getIntegerInstance(locale);
        NumberFormat dfmt = NumberFormat.getNumberInstance(locale);

//    "Final Henderson Filter", "Stage 2 Henderson Filter", "Seasonal Filter", "Irregular Standard-Deviation", "Quality (for TS)", "Max-Adj", "Autocorrelation of order 1 of the SA series", 
//    "Ljung-Box Test (P-value)", "Autocorrelation negative and significant"};
        // Period
        writer.writeField(ifmt.format(report.getPeriod()));
        // Nobs
        writer.writeField(ifmt.format(report.getNobs()));
        // Start
        writer.writeField(report.getStart().start().toLocalDate().toString());
        // End
        writer.writeField(report.getEnd().end().toLocalDate().minusDays(1).toString());
        // Adjustment
        writer.writeField(report.csa());
        // Presence of Seasonality in the Raw Series
        writer.writeField("TODO");
        // Presence of TD effects
        writer.writeField(format(report.getFtd(), PVAL_THRESHOLD));
        // Log-Transformation
        writer.writeField(format(report.isLogs()));
        // ARIMA Model
        writer.writeField(report.arima());
        // LeapYear
        writer.writeField(format(report.isLeapYear()));
        // MovingHoliday
        writer.writeField(format(report.isEaster()));
        // NbTd
        writer.writeField(ifmt.format(report.getNtd()));
        // Noutliers
        writer.writeField(ifmt.format(report.getNoutliers()));
        // Outlier1
        writer.writeField("TODO");
        // Outlier2
        writer.writeField("TODO");
        // Outlier3
        writer.writeField("TODO");
        // Residual Seasonality in SA Series (F-test)
        writer.writeField("TODO");
        // Residual TD Effect
        writer.writeField("TODO");
        // Q-Stat (for X13)
        writer.writeField(formatQ(report.getQStat()));
    }

    private static String format(boolean b) {
        return b ? YES : NO;
    }

    private static String format(StatisticalTest test, double threshold) {
        if (test == null) {
            return NO;
        }
        return test.getPvalue() <= threshold ? YES : NO;
    }
    
    private static String formatQ(double q){
        if (Double.isNaN(q))
            return null;
        if (q <0.95)
            return GOOD;
        if (q<1.05)
            return UNCERTAIN;
        else
            return POOR;
    }

//    private void fill(JobVacancySurveyReport.Builder builder, RegSarimaModel preprocessing) {
//        GeneralLinearModel.Description<SarimaSpec> desc = preprocessing.getDescription();
//        SarimaModel arima = preprocessing.arima();
//        builder.period(preprocessing.getAnnualFrequency())
//                .logs(desc.isLogTransformation())
//                .start(desc.getSeries().getStart())
//                .end(desc.getSeries().getStart().plus(-1))
//                .p(arima.getP())
//                .d(arima.getD())
//                .q(arima.getQ())
//                .bp(arima.getBp())
//                .bd(arima.getBd())
//                .bq(arima.getBq());
//    }
    private void fill(JobVacancySurveyReport.Builder builder, Explorable rslts) {

        String adjust = rslts.getData(ADJUST, String.class);
        boolean adj = adjust != null && !adjust.equals("None");

        Double q = rslts.getData(X13_Q, Double.class);

        builder
                .period(rslts.getData(PERIOD, Integer.class))
                .nobs(rslts.getData(NOBS, Integer.class))
                .start(rslts.getData(START, TsPeriod.class))
                .end(rslts.getData(END, TsPeriod.class))
                .p(rslts.getData(P, Integer.class))
                .d(rslts.getData(D, Integer.class))
                .q(rslts.getData(Q, Integer.class))
                .bp(rslts.getData(BP, Integer.class))
                .bd(rslts.getData(BD, Integer.class))
                .bq(rslts.getData(BQ, Integer.class))
                .leapYear(rslts.getData(LP, Integer.class) > 0 || adj)
                .easter(rslts.getData(NMH, Integer.class) > 0)
                .ntd(rslts.getData(NTD, Integer.class))
                .noutliers(rslts.getData(NOUT, Integer.class))
                .logs(rslts.getData(LOG, Integer.class) > 0)
                .ftd(rslts.getData(FTD, StatisticalTest.class))
                .seasonality(rslts.getData(SEASONAL, Integer.class) > 0)
                .QStat(q == null ? Double.NaN : q);
    }

    private static final String YES = "Yes", NO = "No", GOOD="Good", POOR="Poor", UNCERTAIN="Uncertain";

    private static final String PERIOD = "period", NOBS = "span.n", START = "span.start", END = "span.end", LOG = "log";
    private static final String P = "arima.p", D = "arima.d", Q = "arima.q", BP = "arima.bp", BD = "arima.bd", BQ = "arima.bq";
    private static final String SEASONAL = "seasonal", ADJUST = "adjust", LP = "regression.nlp", NMH = "regression.nmh", NTD = "regression.ntd", NOUT = "regression.nout";
    private static final String FTD = "regression.td-ftest";
    private static final String X13_Q = "m-statistics.q", X13_HENDERSON = "decomposition.trend-filter";

    private static final double PVAL_THRESHOLD = 0.05;

    private static final String[] HEADERS = new String[]{
        "", "Method", "Period", "Nobs", "Start", "End", "Adjustment", "Presence of Seasonality in the Raw Series", "Presence of TD effects", "Log-Transformation", "ARIMA Model", "LeapYear",
        "MovingHoliday", "NbTD", "Noutliers", "Outlier1", "Outlier2", "Outlier3", "Residual Seasonality in SA Series (F-test)", "Residual TD Effect",
        "Q-Stat (for X13)", "Final Henderson Filter", "Stage 2 Henderson Filter", "Seasonal Filter", "Irregular Standard-Deviation", "Quality (for TS)", "Max-Adj", "Autocorrelation of order 1 of the SA series",
        "Ljung-Box Test (P-value)", "Autocorrelation negative and significant"};

    private static void writeRowHeader(Csv.Writer writer, String txt, boolean fullRowName) throws IOException {
        if (txt == null) {
            writer.writeField(null);
            return;
        }
        if (fullRowName) {
            txt = MultiLineNameUtil.join(txt, " * ");
        } else {
            txt = MultiLineNameUtil.last(txt);
        }
        txt = StringFormatter.cleanup(txt);
        writer.writeField(txt);
    }

}
