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
import jdplus.toolkit.base.api.information.formatters.BasicConfiguration;
import jdplus.toolkit.base.api.information.formatters.StringFormatter;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.core.dstats.Chi2;
import jdplus.toolkit.base.core.stats.DescriptiveStatistics;
import jdplus.toolkit.base.core.stats.tests.TestsUtility;
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
        JobVacancySurveyReport report = new JobVacancySurveyReport(cur.getResults());
        writeReport(writer, report);
        writer.writeEndOfLine();
    }
    
    private void writeReport(Csv.Writer writer, JobVacancySurveyReport report) throws IOException {
        Locale locale = LOCALE.get();
        NumberFormat ifmt = NumberFormat.getIntegerInstance(locale);
        NumberFormat dfmt = NumberFormat.getNumberInstance(locale);
        dfmt.setMaximumFractionDigits(3);
        dfmt.setGroupingUsed(false);

//    
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
        writer.writeField(formatSeas(report.fSeasTests()));
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
        
        RegressionItem[] outliers = report.getOutliers();
        // Outlier1
        if (outliers.length > 0) {
            writer.writeField(outliers[0].getDescription());
        } else {
            writer.writeField(null);
        }
        // Outlier2
        if (outliers.length > 1) {
            writer.writeField(outliers[1].getDescription());
        } else {
            writer.writeField(null);
        }
        // Outlier3
        if (outliers.length > 2) {
            writer.writeField(outliers[2].getDescription());
        } else {
            writer.writeField(null);
        }
        // Residual Seasonality in SA Series (F-test)
        writer.writeField(format(report.getResidualSeasonality(), PVAL_THRESHOLD));
        // Residual TD Effect
        writer.writeField(format(report.getResidualTradingDaysEffect(), PVAL_THRESHOLD));
        // Q-Stat (for X13)
        writer.writeField(formatQ(report.getQStat()));
        // Henderson filter
        writer.writeField(formatHenderson(report.getFinalHenderson()));
        // Stage 2 Henderson Filter
        writer.writeField(formatHenderson(report.getStage2Henderson()));
        // Seasonal Filter
        String[] sf = report.getSeasonalFilters();
        if (sf == null) {
            writer.writeField(null);
        } else if (sf.length == 1) {
            writer.writeField(sf[0]);
        } else {
            writer.writeField("Period-specific filters");
        }
        // Irregular Standard-Deviation
        TsData irr = report.getIrregular();
        double stdev = DescriptiveStatistics.of(irr.getValues()).getStdev();
        writer.writeField(dfmt.format(stdev));
        // Quality (for TS)
        writer.writeField(formatQuality(report.getJdQuality()));
        // Max-Adj
        double madj = report.getMaxAdjustment();
        if (Double.isFinite(madj)) {
            writer.writeField(dfmt.format(madj));
        } else {
            writer.writeField(null);
        }
        // Autocorrelation of order 1 of the SA series
        double ac1 = report.getDsaAC1();
        if (!Double.isFinite(ac1)) {
            writer.writeField(null);
//            writer.writeField(null);
//            writer.writeField(null);
        } else {
            writer.writeField(dfmt.format(ac1));
            // Ljung-Box Test (P-value)", "Autocorrelation negative and significant";
            StatisticalTest chi = chi(ac1, report.getNobs());
            writer.writeField(dfmt.format(chi.getPvalue()));
            if (chi.getPvalue()<PVAL_THRESHOLD){
                writer.writeField(WARNING);
            }else{
                writer.writeField(null);
            }
            
        }
        
    }
    
    private StatisticalTest chi(double ac, int n) {
        double val = ac * ac / (n - 1) * n * (n + 2);
        Chi2 chi = new Chi2(1);
        return TestsUtility.testOf(val, chi, TestType.Upper);
    }
    
    private static String formatQuality(String q) {
        if (q.equalsIgnoreCase("good")) {
            return GOOD;
        } else if (q.equalsIgnoreCase("uncertain")) {
            return UNCERTAIN;
        } else {
            return POOR;
        }
    }
    
    private static String formatHenderson(int h) {
        if (h == 0) {
            return null;
        } else {
            return "H" + h;
        }
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
    
    private static String formatQ(double q) {
        if (Double.isNaN(q)) {
            return null;
        }
        if (q < 0.95) {
            return GOOD;
        }
        if (q < 1.05) {
            return UNCERTAIN;
        } else {
            return POOR;
        }
    }
    
    private static String formatSeas(double q) {
        if (Double.isNaN(q)) {
            return null;
        }
        if (q >= 2) {
            return YES;
        }
        if (q >= 1) {
            return UNCERTAIN;
        } else {
            return NO;
        }
    }
    
    private static final String YES = "Yes", NO = "No", GOOD = "Good",
            POOR = "Poor", UNCERTAIN = "Uncertain", WARNING = "Warning";
    private static final double PVAL_THRESHOLD = 0.05;
    private static final int MAX_OUT = 3;
    
    private static final String[] HEADERS = new String[]{
        "", "Method", "Period", "Nobs", "Start", "End", "Adjustment", "Presence of Seasonality in the Raw Series", "Presence of TD effects", "Log-Transformation", "ARIMA Model", "LeapYear",
        "MovingHoliday", "NbTD", "Noutliers", "Outlier1", "Outlier2", "Outlier3", "Residual Seasonality in SA Series (F-test)", "Residual TD Effect",
        "Q-Stat (for X13)", "Final Henderson Filter", "Stage 2 Henderson Filter", "Seasonal Filter", "Irregular Standard-Deviation", "Quality (for TS)", "Max-Adj", "Autocorrelation of order 1 of the SA series"
    , "Ljung-Box Test (P-value)", "Autocorrelation negative and significant"
    };
    
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
