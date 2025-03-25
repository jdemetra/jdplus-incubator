/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x12plus.desktop.plugin.x12.ui;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.desktop.plugin.modelling.ForecastsFactory;
import jdplus.toolkit.desktop.plugin.modelling.InputFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelArimaFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelRegressorsFactory;
import jdplus.toolkit.desktop.plugin.modelling.NiidTestsFactory;
import jdplus.toolkit.desktop.plugin.modelling.OutOfSampleTestFactory;
import jdplus.toolkit.desktop.plugin.modelling.RegSarimaViews;
import jdplus.sa.desktop.plugin.processing.BenchmarkingUI;
import jdplus.sa.desktop.plugin.processing.SIFactory;
import jdplus.sa.desktop.plugin.ui.DemetraSaUI;
import jdplus.sa.desktop.plugin.ui.SaViews;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericTableUI;
import jdplus.toolkit.desktop.plugin.ui.processing.HtmlItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsDistUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.SpectrumUI;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlElements;
import jdplus.toolkit.desktop.plugin.html.HtmlHeader;
import jdplus.toolkit.desktop.plugin.html.core.HtmlDiagnosticsSummary;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.sa.base.api.StationaryVarianceDecomposition;
import jdplus.sa.desktop.plugin.html.HtmlSeasonalityDiagnostics;
import jdplus.sa.desktop.plugin.html.HtmlStationaryVarianceDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.sa.base.core.tests.SeasonalityTests;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.information.BasicInformationExtractor;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualChartUI;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualIds;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualTableUI;
import jdplus.x12plus.base.api.X11plusDictionaries;
import jdplus.x12plus.base.api.X12plusDictionaries;
import jdplus.x12plus.base.core.X11plusResults;
import jdplus.x12plus.base.core.x12.X12plusDiagnostics;
import jdplus.x12plus.base.core.x12.X12plusDocument;
import jdplus.x12plus.base.core.x12.X12plusResults;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
public class X12plusViewFactory extends ProcDocumentViewFactory<X12plusDocument> {

    public static final String X11 = "Decomposition (X11)";
    public static final String STVAR = "Stationary variance decomposition";
    public static final Id DECOMPOSITION_VAR = new LinearId(X11, STVAR);
    // X11 nodes
    public static final String A = "A-Table", B = "B-Table",
            C = "C-Table", D = "D-Table", D_FINAL = "D-Final-Table", E = "E-Table", M = "Quality measures", FINALFILTERS = "Final filters";
    public static final Id M_STATISTICS_SUMMARY = new LinearId(X11, M, SaViews.SUMMARY),
            M_STATISTICS_DETAILS = new LinearId(X11, M, SaViews.DETAILS),
            X11_FILTERS = new LinearId(X11, FINALFILTERS),
            A_TABLES = new LinearId(X11, A),
            B_TABLES = new LinearId(X11, B),
            C_TABLES = new LinearId(X11, C),
            D_TABLES = new LinearId(X11, D),
            D_FINAL_TABLES = new LinearId(X11, D_FINAL),
            E_TABLES = new LinearId(X11, E);

    private static final AtomicReference<IProcDocumentViewFactory<X12plusDocument>> INSTANCE = new AtomicReference();

    public static IProcDocumentViewFactory<X12plusDocument> getDefault() {
        IProcDocumentViewFactory<X12plusDocument> fac = INSTANCE.get();
        if (fac == null) {
            fac = new X12plusViewFactory();
            INSTANCE.lazySet(fac);
        }
        return fac;
    }

    public static void setDefault(IProcDocumentViewFactory<X12plusDocument> factory) {
        INSTANCE.set(factory);
    }

    public X12plusViewFactory() {
        registerFromLookup(X12plusDocument.class);
    }

    @Override
    public Id getPreferredView() {
        return SaViews.MAIN_CHARTS_LOW;
    }

    private final static Function<X12plusDocument, RegSarimaModel> MODELEXTRACTOR = source -> {
        X12plusResults tr = source.getResult();
        return tr == null ? null : tr.getPreprocessing();
    };

    private final static Function<X12plusDocument, X11plusResults> DECOMPOSITIONEXTRACTOR = source -> {
        X12plusResults tr = source.getResult();
        return tr == null ? null : tr.getDecomposition();
    };

    private final static Function<X12plusDocument, X12plusDiagnostics> DIAGSEXTRACTOR = source -> {
        X12plusResults tr = source.getResult();
        return tr == null ? null : tr.getDiagnostics();
    };

    private final static Function<X12plusDocument, TsData> RESEXTRACTOR = MODELEXTRACTOR
            .andThen(regarima -> regarima == null ? null : regarima.fullResiduals());

    private static String generateId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .now(id)
                .build().toString();
    }

    private static String generateSimpleId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .now(id)
                .build().toString();
    }

    public static String[] lowSeries(boolean x11) {
        if (x11) {
            return new String[]{
                generateSimpleId("Series", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.B1)),
                generateSimpleId("Seasonally adjusted", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.D11)),
                generateSimpleId("Trend", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.D12))
            };
        } else {
            return new String[]{
                generateId("Series", SaDictionaries.Y),
                generateId("Seasonally adjusted", SaDictionaries.SA),
                generateId("Trend", SaDictionaries.T)
            };
        }
    }

    public static String[] highSeries(boolean x11) {

        if (x11) {
            return new String[]{
                generateSimpleId("Seasonal", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.D10)),
                generateSimpleId("Irregular", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.D13))
            };
        } else {
            return new String[]{
                generateId("Seasonal (component)", BasicInformationExtractor.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.S_CMP)),
                generateId("Calendar effects", RegressionDictionaries.CAL),
                generateId("Irregular", SaDictionaries.I)
            };
        }
    }

    public static String[] finalSeries(boolean x11) {
        if (x11) {
            return new String[]{
                generateSimpleId("Series", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.B1)),
                generateSimpleId("Seasonally adjusted", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.D11)),
                generateSimpleId("Trend", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.D12)),
                generateSimpleId("Seasonal", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.D10)),
                generateSimpleId("Irregular", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11plusDictionaries.D13))
            };
        } else {
            return new String[]{
                generateId("Series", SaDictionaries.Y),
                generateId("Seasonally adjusted", SaDictionaries.SA),
                generateId("Trend", SaDictionaries.T),
                generateId("Seasonal", SaDictionaries.S),
                generateId("Irregular", SaDictionaries.I)
            };
        }
    }

//<editor-fold defaultstate="collapsed" desc="REGISTER SPEC">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1010)
//    public static class SpecFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {
//
//        public SpecFactory() {
//            super(X12plusDocument.class, RegSarimaViews.INPUT_SPEC,
//                    (X12plusDocument doc) -> {
//                        InformationSet info = X12plusSpecMapping.write(doc.getSpecification(), doc.getInput().getData().getDomain(), true);
//                        return new HtmlInformationSet(info);
//                    },
//                    new HtmlItemUI()
//            );
//        }
//
//        @Override
//        public int getPosition() {
//            return 1010;
//        }
//    }
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1000)
    public static class Input extends InputFactory<X12plusDocument> {

        public Input() {
            super(X12plusDocument.class, RegSarimaViews.INPUT_SERIES);
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }

//</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="REGISTER SUMMARY">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 100000 + 1000)
//    public static class SummaryFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {
//
//        public SummaryFactory() {
//            super(X12plusDocument.class, RegSarimaViews.MODEL_SUMMARY,
//                    source -> new HtmlFractionalAirlineModel(source.getResult(), false),
//                    new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 101000;
//        }
//    }
//</editor-fold>
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2000)
    public static class MainLowChart extends ProcDocumentItemFactory<X12plusDocument, ContextualIds<TsDocument>> {

        public MainLowChart() {
            super(X12plusDocument.class, SaViews.MAIN_CHARTS_LOW, s -> {
                if (s.getResult() == null) {
                    return null;
                }
                boolean x11 = s.getResult().getPreprocessing() == null;
                return new ContextualIds<>(lowSeries(x11), s);
            }, new ContextualChartUI(true));
        }

        @Override
        public int getPosition() {
            return 2000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2100)
    public static class MainHighChart extends ProcDocumentItemFactory<X12plusDocument, ContextualIds<TsDocument>> {

        public MainHighChart() {
            super(X12plusDocument.class, SaViews.MAIN_CHARTS_HIGH, s -> {
                if (s.getResult() == null) {
                    return null;
                }
                boolean x11 = s.getResult().getPreprocessing() == null;
                return new ContextualIds<>(highSeries(x11), s);
            }, new ContextualChartUI(true));
        }

        @Override
        public int getPosition() {
            return 2100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2200)
    public static class MainTable extends ProcDocumentItemFactory<X12plusDocument, ContextualIds<TsDocument>> {

        public MainTable() {
            super(X12plusDocument.class, SaViews.MAIN_TABLE, s -> {
                if (s.getResult() == null) {
                    return null;
                }
                boolean x11 = s.getResult().getPreprocessing() == null;
                return new ContextualIds<>(finalSeries(x11), s);
            }, new ContextualTableUI(true));
        }

        @Override
        public int getPosition() {
            return 2200;
        }

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2400)
    public static class MainSiFactory extends SIFactory<X12plusDocument> {

        public MainSiFactory() {
            super(X12plusDocument.class, SaViews.MAIN_SI, (X12plusDocument source) -> {
                X12plusResults result = source.getResult();
                if (result == null) {
                    return null;
                }
                return result.getDecomposition().asDecomposition();
            });
        }

        @Override
        public int getPosition() {
            return 2400;
        }
    }

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3000)
    public static class SummaryFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public SummaryFactory() {

            super(X12plusDocument.class, SaViews.PREPROCESSING_SUMMARY, MODELEXTRACTOR
                    .andThen(regarima -> regarima == null ? null
                    : new HtmlRegSarima(regarima, false)),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 3000;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-FORECASTS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3110)
    public static class ForecastsTable extends ProcDocumentItemFactory<X12plusDocument, TsDocument> {

        public ForecastsTable() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_FCASTS_TABLE, s -> s, new GenericTableUI(false, generateItems()));
        }

        @Override
        public int getPosition() {
            return 3110;
        }

        private static String[] generateItems() {
            return new String[]{RegressionDictionaries.Y_F, RegressionDictionaries.Y_EF};
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3100)
    public static class FCastsFactory extends ForecastsFactory<X12plusDocument> {

        public FCastsFactory() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_FCASTS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3120)
    public static class FCastsOutFactory extends OutOfSampleTestFactory<X12plusDocument> {

        public FCastsOutFactory() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3120;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-DETAILS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3200)
    public static class ModelRegsFactory extends ModelRegressorsFactory<X12plusDocument> {

        public ModelRegsFactory() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_REGS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3300)
    public static class ArimaFactory extends ModelArimaFactory {

        public ArimaFactory() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_ARIMA, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3300;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3400)
    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<X12plusDocument, TsDocument> {

        public PreprocessingDetFactory() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_DET, source -> source, new GenericTableUI(false,
                    RegressionDictionaries.YCAL,
                    RegressionDictionaries.YLIN,
                    RegressionDictionaries.DET,
                    RegressionDictionaries.CAL,
                    RegressionDictionaries.TDE,
                    RegressionDictionaries.EE,
                    RegressionDictionaries.OUT,
                    RegressionDictionaries.FULL_RES));
        }

        @Override
        public int getPosition() {
            return 3400;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-RESIDUALS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3500)
    public static class ModelResFactory extends ProcDocumentItemFactory<X12plusDocument, TsData> {

        public ModelResFactory() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_RES, RESEXTRACTOR,
                    new ResidualsUI()
            );
        }

        @Override
        public int getPosition() {
            return 3500;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3510)
    public static class ModelResStatsFactory extends NiidTestsFactory<X12plusDocument> {

        public ModelResStatsFactory() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_RES_STATS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3510;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3520)
    public static class ModelResDist extends ProcDocumentItemFactory<X12plusDocument, TsData> {

        public ModelResDist() {
            super(X12plusDocument.class, SaViews.PREPROCESSING_RES_DIST,
                    RESEXTRACTOR,
                    new ResidualsDistUI());

        }

        @Override
        public int getPosition() {
            return 3520;
        }
    }
//</editor-fold>

//
//<editor-fold defaultstate="collapsed" desc="REGISTER X11">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4010)
    public static class ATablesFactory extends ProcDocumentItemFactory<X12plusDocument, TsDocument> {

        public ATablesFactory() {
            super(X12plusDocument.class, A_TABLES, source -> source, new GenericTableUI(false,
                    BasicInformationExtractor.prefix(X12plusDictionaries.A_TABLE, X12plusDictionaries.PREADJUST)));
        }

        @Override
        public int getPosition() {
            return 4010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4020)
    public static class BTablesFactory extends ProcDocumentItemFactory<X12plusDocument, TsDocument> {

        public BTablesFactory() {
            super(X12plusDocument.class, B_TABLES, source -> source, new GenericTableUI(false,
                    BasicInformationExtractor.prefix(X11plusDictionaries.B_TABLE, X12plusDictionaries.X11)));
        }

        @Override
        public int getPosition() {
            return 4020;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4030)
    public static class CTablesFactory extends ProcDocumentItemFactory<X12plusDocument, TsDocument> {

        public CTablesFactory() {
            super(X12plusDocument.class, C_TABLES, source -> source, new GenericTableUI(false,
                    BasicInformationExtractor.prefix(X11plusDictionaries.C_TABLE, X12plusDictionaries.X11)));
        }

        @Override
        public int getPosition() {
            return 4030;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4040)
    public static class DTablesFactory extends ProcDocumentItemFactory<X12plusDocument, TsDocument> {

        static final String[] items = new String[]{
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D1),
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D4),
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D5),
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D6),
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D7),
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D8),
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D9),
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D10),
            Dictionary.concatenate(X12plusDictionaries.X11, X11plusDictionaries.D10A),
            Dictionary.concatenate(X12plusDictionaries.X11, X12plusDictionaries.D11),
            Dictionary.concatenate(X12plusDictionaries.X11, X12plusDictionaries.D11A),
            Dictionary.concatenate(X12plusDictionaries.X11, X12plusDictionaries.D12),
            Dictionary.concatenate(X12plusDictionaries.X11, X12plusDictionaries.D12A),
            Dictionary.concatenate(X12plusDictionaries.X11, X12plusDictionaries.D13)
        };

        public DTablesFactory() {
            super(X12plusDocument.class, D_TABLES, source -> source, new GenericTableUI(false, items));
        }

        @Override
        public int getPosition() {
            return 4040;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4050)
    public static class DFinalTablesFactory extends ProcDocumentItemFactory<X12plusDocument, TsDocument> {

        static final String[] items = new String[]{
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D11),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D11A),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D11B),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D12),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D12A),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D12B),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D13),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D16),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D16A),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D16B),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D18),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D18A),
            Dictionary.concatenate(X12plusDictionaries.FINAL, X12plusDictionaries.D18B)
        };

        public DFinalTablesFactory() {
            super(X12plusDocument.class, D_FINAL_TABLES, source -> source, new GenericTableUI(false, items));
        }

        @Override
        public int getPosition() {
            return 4050;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="BENCHMARKING">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4900)
    public static class BenchmarkingFactory extends ProcDocumentItemFactory<X12plusDocument, BenchmarkingUI.Input> {

        public BenchmarkingFactory() {
            super(X12plusDocument.class, SaViews.BENCHMARKING_SUMMARY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                SaBenchmarkingResults benchmarking = rslt.getBenchmarking();
                if (benchmarking == null) {
                    return null;
                }
                boolean mul = rslt.getDecomposition().getMode().isMultiplicative();
                return new BenchmarkingUI.Input(mul, benchmarking);
            }, new BenchmarkingUI());
        }

        @Override
        public int getPosition() {
            return 4900;
        }

    }
//</editor-fold>

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4500)
    public static class StationaryVarianceDecompositionFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public StationaryVarianceDecompositionFactory() {
            super(X12plusDocument.class, DECOMPOSITION_VAR, DIAGSEXTRACTOR.andThen(
                    (X12plusDiagnostics diags) -> {
                        StationaryVarianceDecomposition decomp = diags.getVarianceDecomposition();
                        if (decomp == null) {
                            return null;
                        }
                        return new HtmlStationaryVarianceDecomposition(decomp);
                    }),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4500;
        }
    }

//<editor-fold defaultstate="collapsed" desc="DIAGNOSTICS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5000)
    public static class DiagnosticsSummaryFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public DiagnosticsSummaryFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_SUMMARY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                SaProcessingFactory factory = SaManager.factoryFor(doc.getSpecification());
                List<ProcDiagnostic> diags = new ArrayList<>();
                factory.fillDiagnostics(diags, null, rslt);
                return new HtmlDiagnosticsSummary(diags);
            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5010)
    public static class OriginalSeasonalityFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public OriginalSeasonalityFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_OSEASONALITY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                TsData s;
                if (rslt.getPreprocessing() != null) {
                    s = rslt.getPreprocessing().transformedSeries();
                } else {
                    s = rslt.getDecomposition().getB1();
                }
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Original [transformed] series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), false));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5020)
    public static class LinSeasonalityFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public LinSeasonalityFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_LSEASONALITY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null || rslt.getPreprocessing() == null) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().linearizedSeries();
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Linearized series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), false));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5020;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5030)
    public static class ResSeasonalityFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public ResSeasonalityFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_RSEASONALITY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null || rslt.getPreprocessing() == null) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().fullResiduals();
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Full residuals", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5030;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5040)
    public static class SaSeasonalityFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public SaSeasonalityFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_SASEASONALITY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                TsData s = rslt.getDecomposition().getD11();
                if (s == null) {
                    return null;
                }
                if (rslt.getDecomposition().getMode().isMultiplicative()) {
                    s = s.log();
                }
                return new HtmlElements(new HtmlHeader(1, "[Linearized] seasonally adjusted series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5030;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5050)
    public static class IrrSeasonalityFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public IrrSeasonalityFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_ISEASONALITY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                TsData s = rslt.getDecomposition().getD13();
                if (s == null) {
                    return null;
                }
                if (rslt.getDecomposition().getMode().isMultiplicative()) {
                    s = s.log();
                }
                return new HtmlElements(new HtmlHeader(1, "[Linearized] irregular component", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5050;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5060)
    public static class LastResSeasonalityFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public LastResSeasonalityFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_LASTRSEASONALITY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null || rslt.getPreprocessing() == null) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().fullResiduals();
                if (s == null) {
                    return null;
                }
                StringBuilder header = new StringBuilder().append("Full residuals");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5060;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5070)
    public static class LastSaSeasonalityFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public LastSaSeasonalityFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_LASTSASEASONALITY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                TsData s = rslt.getDecomposition().getD11();
                if (s == null) {
                    return null;
                }
                StringBuilder header = new StringBuilder().append("[Linearized] seasonally adjusted series");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny - 1), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                if (rslt.getDecomposition().getMode().isMultiplicative()) {
                    s = s.log();
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5070;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5080)
    public static class LastIrrSeasonalityFactory extends ProcDocumentItemFactory<X12plusDocument, HtmlElement> {

        public LastIrrSeasonalityFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_LASTISEASONALITY, (X12plusDocument doc) -> {
                X12plusResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                TsData s = rslt.getDecomposition().getD13();
                if (s == null) {
                    return null;
                }
                StringBuilder header = new StringBuilder().append("[Linearized] irregular component");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                if (rslt.getDecomposition().getMode().isMultiplicative()) {
                    s = s.log();
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5080;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5310)
    public static class ModelResSpectrum extends ProcDocumentItemFactory<X12plusDocument, SpectrumUI.Information> {

        public ModelResSpectrum() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_RES,
                    RESEXTRACTOR.andThen(
                            res
                            -> res == null ? null
                                    : SpectrumUI.Information.builder()
                                            .series(res)
                                            .differencingOrder(0)
                                            .log(false)
                                            .mean(true)
                                            .whiteNoise(true)
                                            .build()),
                    new SpectrumUI());
        }

        @Override
        public int getPosition() {
            return 5310;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5320)
    public static class DiagnosticsSpectrumIFactory extends ProcDocumentItemFactory<X12plusDocument, SpectrumUI.Information> {

        public DiagnosticsSpectrumIFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_I,
                    DECOMPOSITIONEXTRACTOR.andThen(
                            (X11plusResults x11) -> {
                                if (x11 == null) {
                                    return null;
                                }
                                TsData s = x11.getD13();
                                return s == null ? null
                                        : SpectrumUI.Information.builder()
                                                .series(s)
                                                .differencingOrder(0)
                                                .log(x11.getMode().isMultiplicative())
                                                .mean(true)
                                                .whiteNoise(false)
                                                .build();
                            }),
                    new SpectrumUI());
        }

        @Override
        public int getPosition() {
            return 5320;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5330)
    public static class DiagnosticsSpectrumSaFactory extends ProcDocumentItemFactory<X12plusDocument, SpectrumUI.Information> {

        public DiagnosticsSpectrumSaFactory() {
            super(X12plusDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_SA,
                    DECOMPOSITIONEXTRACTOR.andThen(
                            (X11plusResults x11) -> {
                                if (x11 == null) {
                                    return null;
                                }
                                TsData s = x11.getD11();
                                return s == null ? null
                                        : SpectrumUI.Information.builder()
                                                .series(s)
                                                .differencingOrder(1)
                                                .differencingLag(1)
                                                .log(x11.getMode().isMultiplicative())
                                                .mean(true)
                                                .whiteNoise(false)
                                                .build();
                            }),
                    new SpectrumUI());
        }

        @Override
        public int getPosition() {
            return 5330;
        }
    }

//</editor-fold>
}
