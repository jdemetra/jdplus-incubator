/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.desktop.plugin.bsm.ui;

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
import jdplus.toolkit.desktop.plugin.ui.processing.GenericChartUI;
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
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.modelling.ModellingDictionary;
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
import jdplus.sts.base.core.BsmResults;
import jdplus.sts.base.core.StsDiagnostics;
import jdplus.sts.base.core.StsDocument;
import jdplus.sts.base.core.StsResults;
import jdplus.sts.base.core.extractors.StsResultsExtractor;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.desktop.plugin.html.core.HtmlInformationSet;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
public class StsViewFactory extends ProcDocumentViewFactory<StsDocument> {

    public static final String SELECTION = "STS estimation",
            STOCHASTIC = "Stochastic series",
            COMPONENTS = "Components",
            STM = "STS components",
            MODELBASED = "Model-based tests",
            WKANALYSIS = "WK analysis",
            WK_COMPONENTS = "Components",
            WK_FINALS = "Final estimators",
            WK_PRELIMINARY = "Preliminary estimators",
            WK_ERRORS = "Errors analysis";
    public static final Id DECOMPOSITION_SUMMARY = new LinearId(SaViews.DECOMPOSITION);
    public static final Id DECOMPOSITION_SERIES = new LinearId(SaViews.DECOMPOSITION, STOCHASTIC);
    public static final Id DECOMPOSITION_CMPSERIES = new LinearId(SaViews.DECOMPOSITION, COMPONENTS);
    public static final Id DECOMPOSITION_WK_COMPONENTS = new LinearId(SaViews.DECOMPOSITION, WKANALYSIS, WK_COMPONENTS);
    public static final Id DECOMPOSITION_WK_FINALS = new LinearId(SaViews.DECOMPOSITION, WKANALYSIS, WK_FINALS);
    public static final Id DECOMPOSITION_TESTS = new LinearId(SaViews.DECOMPOSITION, MODELBASED);
    public static final Id MODEL_SELECTION = new LinearId(SaViews.MODEL, SELECTION);
    public static final Id MODEL_LIKELIHOOD = new LinearId(SaViews.MODEL, SaViews.LIKELIHOOD);
    public static final Id MODEL_CMPS = new LinearId(SaViews.MODEL, STM);

    private static final AtomicReference<IProcDocumentViewFactory<StsDocument>> INSTANCE = new AtomicReference();

    public static IProcDocumentViewFactory<StsDocument> getDefault() {
        IProcDocumentViewFactory<StsDocument> fac = INSTANCE.get();
        if (fac == null) {
            fac = new StsViewFactory();
            INSTANCE.lazySet(fac);
        }
        return fac;
    }

    public static void setDefault(IProcDocumentViewFactory<StsDocument> factory) {
        INSTANCE.set(factory);
    }

    public StsViewFactory() {
        registerFromLookup(StsDocument.class);
    }

    @Override
    public Id getPreferredView() {
        return MODEL_SELECTION;
    }

    private final static Function<StsDocument, RegSarimaModel> MODELEXTRACTOR = source -> {
        StsResults tr = source.getResult();
        return tr == null ? null : tr.getPreprocessing();
    };

    private final static Function<StsDocument, StsResults> DECOMPOSITIONEXTRACTOR = source -> {
        return source.getResult();
    };

    private final static Function<StsDocument, StsDiagnostics> DIAGSEXTRACTOR = source -> {
        StsResults tr = source.getResult();
        return tr == null ? null : tr.getDiagnostics();
    };

   private final static Function<StsDocument, BsmResults> BSMEXTRACTOR = source -> {
        StsResults tr = source.getResult();
        return tr == null ? null : tr.getSts();
    };

   private final static Function<StsDocument, TsData> RESEXTRACTOR = MODELEXTRACTOR
            .andThen(regarima -> regarima == null ? null : regarima.fullResiduals());

    private static String generateId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .now(id)
                .build().toString();
    }

    public static String[] lowSeries() {
        return new String[]{
            generateId("Series", SaDictionaries.Y),
            generateId("Seasonally adjusted", SaDictionaries.SA),
            generateId("Trend", SaDictionaries.T)
        };
    }

    public static String[] highSeries() {
        return new String[]{
            generateId("Seasonal", SaDictionaries.S),
            generateId("Calendar effects", ModellingDictionary.CAL),
            generateId("Irregular", SaDictionaries.I)
        };
    }

    public static String[] finalSeries() {
        return new String[]{
            generateId("Series", SaDictionaries.Y),
            generateId("Seasonally adjusted", SaDictionaries.SA),
            generateId("Trend", SaDictionaries.T),
            generateId("Seasonal", SaDictionaries.S),
            generateId("Irregular", SaDictionaries.I)
        };
    }

////<editor-fold defaultstate="collapsed" desc="REGISTER SPEC">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1010)
//    public static class SpecFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public SpecFactory() {
//            super(StsDocument.class, RegSarimaViews.INPUT_SPEC,
//                    (StsDocument doc) -> {
//                        InformationSet info = StsSpecMapping.write(doc.getSpecification(), doc.getInput().getData().getDomain(), true);
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
    public static class Input extends InputFactory<StsDocument> {

        public Input() {
            super(StsDocument.class, RegSarimaViews.INPUT_SERIES);
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }

////</editor-fold>
////
////<editor-fold defaultstate="collapsed" desc="REGISTER SUMMARY">
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 100000 + 1000)
////    public static class SummaryFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
////
////        public SummaryFactory() {
////            super(StsDocument.class, RegSarimaViews.MODEL_SUMMARY,
////                    source -> new HtmlFractionalAirlineModel(source.getResult(), false),
////                    new HtmlItemUI());
////        }
////
////        @Override
////        public int getPosition() {
////            return 101000;
////        }
////    }
////</editor-fold>
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2000)
    public static class MainLowChart extends ProcDocumentItemFactory<StsDocument, TsDocument> {

        public MainLowChart() {
            super(StsDocument.class, SaViews.MAIN_CHARTS_LOW, s -> s, new GenericChartUI(false, lowSeries()));
        }

        @Override
        public int getPosition() {
            return 2000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2100)
    public static class MainHighChart extends ProcDocumentItemFactory<StsDocument, TsDocument> {

        public MainHighChart() {
            super(StsDocument.class, SaViews.MAIN_CHARTS_HIGH, s -> s, new GenericChartUI(false, highSeries()));
        }

        @Override
        public int getPosition() {
            return 2100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2200)
    public static class MainTable extends ProcDocumentItemFactory<StsDocument, TsDocument> {

        public MainTable() {
            super(StsDocument.class, SaViews.MAIN_TABLE, s -> s, new GenericTableUI(false, finalSeries()));
        }

        @Override
        public int getPosition() {
            return 2200;
        }

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2400)
    public static class MainSiFactory extends SIFactory<StsDocument> {

        public MainSiFactory() {
            super(StsDocument.class, SaViews.MAIN_SI, (StsDocument source) -> {
                StsResults result = source.getResult();
                if (result == null) {
                    return null;
                }
                return result.getComponents();
            });
        }

        @Override
        public int getPosition() {
            return 2400;
        }
    }

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3000)
    public static class SummaryFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {

        public SummaryFactory() {

            super(StsDocument.class, SaViews.PREPROCESSING_SUMMARY, MODELEXTRACTOR
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
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3110)
//    public static class ForecastsTable extends ProcDocumentItemFactory<StsDocument, TsDocument> {
//
//        public ForecastsTable() {
//            super(StsDocument.class, SaViews.PREPROCESSING_FCASTS_TABLE, s -> s, new GenericTableUI(false, generateItems()));
//        }
//
//        @Override
//        public int getPosition() {
//            return 3110;
//        }
//
//        private static String[] generateItems() {
//            return new String[]{RegressionDictionaries.Y_F, RegressionDictionaries.Y_EF};
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3100)
//    public static class FCastsFactory extends ForecastsFactory<StsDocument> {
//
//        public FCastsFactory() {
//            super(StsDocument.class, SaViews.PREPROCESSING_FCASTS, MODELEXTRACTOR);
//        }
//
//        @Override
//        public int getPosition() {
//            return 3100;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3120)
//    public static class FCastsOutFactory extends OutOfSampleTestFactory<StsDocument> {
//
//        public FCastsOutFactory() {
//            super(StsDocument.class, SaViews.PREPROCESSING_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
//        }
//
//        @Override
//        public int getPosition() {
//            return 3120;
//        }
//    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-DETAILS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3200)
    public static class ModelRegsFactory extends ModelRegressorsFactory<StsDocument> {

        public ModelRegsFactory() {
            super(StsDocument.class, SaViews.PREPROCESSING_REGS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3300)
    public static class ArimaFactory extends ModelArimaFactory {

        public ArimaFactory() {
            super(StsDocument.class, SaViews.PREPROCESSING_ARIMA, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3300;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3400)
    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<StsDocument, TsDocument> {

        public PreprocessingDetFactory() {
            super(StsDocument.class, SaViews.PREPROCESSING_DET, source -> source, new GenericTableUI(false,
                    Dictionary.concatenate(SaDictionaries.PREPROCESSING, ModellingDictionary.YCAL),
                    Dictionary.concatenate(SaDictionaries.PREPROCESSING, ModellingDictionary.Y_LIN),
                    Dictionary.concatenate(SaDictionaries.PREPROCESSING, ModellingDictionary.DET),
                    Dictionary.concatenate(SaDictionaries.PREPROCESSING, ModellingDictionary.CAL),
                    Dictionary.concatenate(SaDictionaries.PREPROCESSING, ModellingDictionary.TDE),
                    Dictionary.concatenate(SaDictionaries.PREPROCESSING, ModellingDictionary.EE),
                    Dictionary.concatenate(SaDictionaries.PREPROCESSING, ModellingDictionary.OUT)));
        }

        @Override
        public int getPosition() {
            return 3400;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-RESIDUALS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3500)
    public static class ModelResFactory extends ProcDocumentItemFactory<StsDocument, TsData> {

        public ModelResFactory() {
            super(StsDocument.class, SaViews.PREPROCESSING_RES, RESEXTRACTOR,
                    new ResidualsUI()
            );
        }

        @Override
        public int getPosition() {
            return 3500;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3510)
    public static class ModelResStatsFactory extends NiidTestsFactory<StsDocument> {

        public ModelResStatsFactory() {
            super(StsDocument.class, SaViews.PREPROCESSING_RES_STATS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3510;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3520)
    public static class ModelResDist extends ProcDocumentItemFactory<StsDocument, TsData> {

        public ModelResDist() {
            super(StsDocument.class, SaViews.PREPROCESSING_RES_DIST,
                    RESEXTRACTOR,
                    new ResidualsDistUI());

        }

        @Override
        public int getPosition() {
            return 3520;
        }
    }
//</editor-fold>
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4000)
    public static class StsSummaryFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {

        public StsSummaryFactory() {

            super(StsDocument.class, MODEL_SELECTION, DECOMPOSITIONEXTRACTOR
                    .andThen(sts -> sts == null ? null
                    : new HtmlBsm(sts.getBsm(), false)),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4000;
        }
    }
    
     @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4400)
    public static class StsSeriesFactory extends ProcDocumentItemFactory<StsDocument, TsDocument> {

        public StsSeriesFactory() {
            super(StsDocument.class, MODEL_CMPS, source -> source, new GenericTableUI(false,
                    StsResultsExtractor.LEVEL,
                    StsResultsExtractor.SLOPE, 
                    StsResultsExtractor.CYCLE,
                    StsResultsExtractor.SEASONAL,
                    StsResultsExtractor.NOISE
            ));
        }

        @Override
        public int getPosition() {
            return 4400;
        }
    }
   

//<editor-fold defaultstate="collapsed" desc="REGISTER FORECASTS">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 500)
//    public static class ForecastsTable extends ProcDocumentItemFactory<FractionalAirlineDocument, TsDocument> {
//
//        public ForecastsTable() {
//            super(StsDocument.class, RegSarimaViews.MODEL_FCASTS_TABLE, s -> s, new GenericTableUI(false, generateItems()));
//        }
//
//        @Override
//        public int getPosition() {
//            return 200500;
//        }
//
//        private static String[] generateItems() {
//            return new String[]{ModellingDictionary.Y + SeriesInfo.F_SUFFIX, ModellingDictionary.Y + SeriesInfo.EF_SUFFIX};
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 1000)
//    public static class FCastsFactory extends ForecastsFactory<FractionalAirlineDocument> {
//
//        public FCastsFactory() {
//            super(StsDocument.class, RegSarimaViews.MODEL_FCASTS, MODELEXTRACTOR);
//        }
//
//        @Override
//        public int getPosition() {
//            return 201000;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 2000)
//    public static class FCastsOutFactory extends OutOfSampleTestFactory<FractionalAirlineDocument> {
//
//        public FCastsOutFactory() {
//            super(StsDocument.class, RegSarimaViews.MODEL_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
//        }
//
//        @Override
//        public int getPosition() {
//            return 202000;
//        }
//    }
//</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="REGISTER MODEL">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 1000)
//    public static class ModelRegsFactory extends ModelRegressorsFactory<FractionalAirlineDocument> {
//
//        public ModelRegsFactory() {
//            super(StsDocument.class, RegSarimaViews.MODEL_REGS, MODELEXTRACTOR);
//        }
//
//        @Override
//        public int getPosition() {
//            return 301000;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 2000)
//    public static class ArimaFactory extends ModelArimaFactory {
//
//        public ArimaFactory() {
//            super(StsDocument.class, RegSarimaViews.MODEL_ARIMA, MODELEXTRACTOR);
//        }
//
//        @Override
//        public int getPosition() {
//            return 302000;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 3000)
//    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<FractionalAirlineDocument, StsDocument> {
//
//        public PreprocessingDetFactory() {
//            super(StsDocument.class, RegSarimaViews.MODEL_DET,
//                    source -> source, new GenericTableUI(false,
//                            ModellingDictionary.Y_LIN, ModellingDictionary.DET,
//                            ModellingDictionary.CAL, ModellingDictionary.TDE, ModellingDictionary.EE,
//                            ModellingDictionary.OUT, ModellingDictionary.FULL_RES));
//        }
//
//        @Override
//        public int getPosition() {
//            return 303000;
//        }
//    }
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="REGISTER RESIDUALS">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 1000)
//    public static class ModelResFactory extends ProcDocumentItemFactory<FractionalAirlineDocument, TsData> {
//
//        public ModelResFactory() {
//            super(StsDocument.class, RegSarimaViews.MODEL_RES, RESEXTRACTOR,
//                    new ResidualsUI()
//            );
//        }
//
//        @Override
//        public int getPosition() {
//            return 401000;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 2000)
//    public static class ModelResStatsFactory extends NiidTestsFactory<FractionalAirlineDocument> {
//
//        public ModelResStatsFactory() {
//            super(StsDocument.class, RegSarimaViews.MODEL_RES_STATS, MODELEXTRACTOR);
//        }
//
//        @Override
//        public int getPosition() {
//            return 402000;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 3000)
//    public static class ModelResDist extends ProcDocumentItemFactory<StsDocument, DoubleSeq> {
//
//        public ModelResDist() {
//            super(StsDocument.class, RegSarimaViews.MODEL_RES_DIST, RESEXTRACTOR,
//                    new DistributionUI());
//
//        }
//
//        @Override
//        public int getPosition() {
//            return 403000;
//        }
//    }
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 4000)
//    public static class ModelResSpectrum extends ProcDocumentItemFactory<StsDocument, DoubleSeq> {
//
//        public ModelResSpectrum() {
//            super(StsDocument.class, RegSarimaViews.MODEL_RES_SPECTRUM, RESEXTRACTOR,
//                    new PeriodogramUI());
//
//        }
//
//        @Override
//        public int getPosition() {
//            return 404000;
//        }
//    }
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="REGISTER DETAILS">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 500000)
//    public static class LFactory extends LikelihoodFactory<FractionalAirlineDocument> {
//
//        public LFactory() {
//            super(StsDocument.class, RegSarimaViews.MODEL_LIKELIHOOD, MODELEXTRACTOR);
//            setAsync(true);
//        }
//
//        @Override
//        public int getPosition() {
//            return 500000;
//        }
//    }
//</editor-fold>
////<editor-fold defaultstate="collapsed" desc="BENCHMARKING">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4900)
//    public static class BenchmarkingFactory extends ProcDocumentItemFactory<StsDocument, BenchmarkingUI.Input> {
//
//        public BenchmarkingFactory() {
//            super(StsDocument.class, SaViews.BENCHMARKING_SUMMARY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null) {
//                    return null;
//                }
//                SaBenchmarkingResults benchmarking = rslt.getBenchmarking();
//                if (benchmarking == null) {
//                    return null;
//                }
//                boolean mul = rslt.getFinals().getMode().isMultiplicative();
//                return new BenchmarkingUI.Input(mul, benchmarking);
//            }, new BenchmarkingUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 4900;
//        }
//
//    }
////</editor-fold>
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4910)
//    public static class StationaryVarianceDecompositionFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public StationaryVarianceDecompositionFactory() {
//            super(StsDocument.class, DECOMPOSITION_VAR, DIAGSEXTRACTOR.andThen(
//                    (StsDiagnostics diags) -> {
//                        StationaryVarianceDecomposition decomp = diags.getVarianceDecomposition();
//                        if (decomp == null) {
//                            return null;
//                        }
//                        return new HtmlStationaryVarianceDecomposition(decomp);
//                    }),
//                    new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 4910;
//        }
//    }
//
////<editor-fold defaultstate="collapsed" desc="DIAGNOSTICS">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5000)
//    public static class DiagnosticsSummaryFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public DiagnosticsSummaryFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_SUMMARY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null) {
//                    return null;
//                }
//                SaProcessingFactory factory = SaManager.factoryFor(doc.getSpecification());
//                List<ProcDiagnostic> diags = new ArrayList<>();
//                factory.fillDiagnostics(diags, rslt);
//                return new HtmlDiagnosticsSummary(diags);
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5000;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5010)
//    public static class OriginalSeasonalityFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public OriginalSeasonalityFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_OSEASONALITY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null) {
//                    return null;
//                }
//                TsData s;
//                if (rslt.getPreprocessing() != null) {
//                    s = rslt.getPreprocessing().transformedSeries();
//                } else {
//                    s = rslt.getDecomposition().getSeries();
//                }
//                if (s == null) {
//                    return null;
//                }
//                return new HtmlElements(new HtmlHeader(1, "Original [transformed] series", true),
//                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), false));
//
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5010;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5020)
//    public static class LinSeasonalityFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public LinSeasonalityFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_LSEASONALITY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null || rslt.getPreprocessing() == null) {
//                    return null;
//                }
//                TsData s = rslt.getPreprocessing().linearizedSeries();
//                if (s == null) {
//                    return null;
//                }
//                return new HtmlElements(new HtmlHeader(1, "Linearized series", true),
//                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), false));
//
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5020;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5030)
//    public static class ResSeasonalityFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public ResSeasonalityFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_RSEASONALITY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null || rslt.getPreprocessing() == null) {
//                    return null;
//                }
//                TsData s = rslt.getPreprocessing().fullResiduals();
//                if (s == null) {
//                    return null;
//                }
//                return new HtmlElements(new HtmlHeader(1, "Full residuals", true),
//                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));
//
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5030;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5040)
//    public static class SaSeasonalityFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public SaSeasonalityFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_SASEASONALITY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null) {
//                    return null;
//                }
//                TsData s = rslt.getDecomposition().getSa();
//                if (s == null) {
//                    return null;
//                }
//                if (rslt.getDecomposition().isMultiplicative()) {
//                    s = s.log();
//                }
//                return new HtmlElements(new HtmlHeader(1, "[Linearized] seasonally adjusted series", true),
//                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), true));
//
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5030;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5050)
//    public static class IrrSeasonalityFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public IrrSeasonalityFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_ISEASONALITY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null) {
//                    return null;
//                }
//                TsData s = rslt.getDecomposition().getIrregular();
//                if (s == null) {
//                    return null;
//                }
//                if (rslt.getDecomposition().isMultiplicative()) {
//                    s = s.log();
//                }
//                return new HtmlElements(new HtmlHeader(1, "[Linearized] irregular component", true),
//                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));
//
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5050;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5060)
//    public static class LastResSeasonalityFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public LastResSeasonalityFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_LASTRSEASONALITY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null || rslt.getPreprocessing() == null) {
//                    return null;
//                }
//                TsData s = rslt.getPreprocessing().fullResiduals();
//                if (s == null) {
//                    return null;
//                }
//                StringBuilder header = new StringBuilder().append("Full residuals");
//                int ny = DemetraSaUI.get().getSeasonalityLength();
//                if (ny > 0) {
//                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
//                    header.append(" (last ").append(ny).append(" years)");
//                }
//                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
//                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));
//
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5060;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5070)
//    public static class LastSaSeasonalityFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public LastSaSeasonalityFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_LASTSASEASONALITY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null) {
//                    return null;
//                }
//                TsData s = rslt.getDecomposition().getSa();
//                if (s == null) {
//                    return null;
//                }
//                StringBuilder header = new StringBuilder().append("[Linearized] seasonally adjusted series");
//                int ny = DemetraSaUI.get().getSeasonalityLength();
//                if (ny > 0) {
//                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny - 1), 0);
//                    header.append(" (last ").append(ny).append(" years)");
//                }
//                if (rslt.getDecomposition().isMultiplicative()) {
//                    s = s.log();
//                }
//                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
//                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), true));
//
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5070;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5080)
//    public static class LastIrrSeasonalityFactory extends ProcDocumentItemFactory<StsDocument, HtmlElement> {
//
//        public LastIrrSeasonalityFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_LASTISEASONALITY, (StsDocument doc) -> {
//                StsResults rslt = doc.getResult();
//                if (rslt == null) {
//                    return null;
//                }
//                TsData s = rslt.getDecomposition().getIrregular();
//                if (s == null) {
//                    return null;
//                }
//                StringBuilder header = new StringBuilder().append("[Linearized] irregular component");
//                int ny = DemetraSaUI.get().getSeasonalityLength();
//                if (ny > 0) {
//                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
//                    header.append(" (last ").append(ny).append(" years)");
//                }
//                if (rslt.getDecomposition().isMultiplicative()) {
//                    s = s.log();
//                }
//                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
//                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));
//
//            }, new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5080;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5310)
//    public static class ModelResSpectrum extends ProcDocumentItemFactory<StsDocument, SpectrumUI.Information> {
//
//        public ModelResSpectrum() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_RES,
//                    RESEXTRACTOR.andThen(
//                            res
//                            -> res == null ? null
//                                    : SpectrumUI.Information.builder()
//                                            .series(res)
//                                            .differencingOrder(0)
//                                            .log(false)
//                                            .mean(true)
//                                            .whiteNoise(true)
//                                            .build()),
//                    new SpectrumUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5310;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5320)
//    public static class DiagnosticsSpectrumIFactory extends ProcDocumentItemFactory<StsDocument, SpectrumUI.Information> {
//
//        public DiagnosticsSpectrumIFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_I,
//                    DECOMPOSITIONEXTRACTOR.andThen(
//                            (StlResults stl) -> {
//                                if (stl == null) {
//                                    return null;
//                                }
//                                TsData s = stl.getIrregular();
//                               return s == null ? null
//                                        : SpectrumUI.Information.builder()
//                                                .series(s)
//                                                .differencingOrder(0)
//                                                .log(stl.isMultiplicative())
//                                                .mean(true)
//                                                .whiteNoise(false)
//                                                .build();
//                            }),
//                    new SpectrumUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 5320;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5330)
//    public static class DiagnosticsSpectrumSaFactory extends ProcDocumentItemFactory<StsDocument, SpectrumUI.Information> {
//
//        public DiagnosticsSpectrumSaFactory() {
//            super(StsDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_SA,
//                    DECOMPOSITIONEXTRACTOR.andThen(
//                            (StlResults stl) -> {
//                                if (stl == null) {
//                                    return null;
//                                }
//                                TsData s = stl.getSa();
//                                return s == null ? null
//                                        : SpectrumUI.Information.builder()
//                                                .series(s)
//                                                .differencingOrder(1)
//                                                .differencingLag(1)
//                                                .log(stl.isMultiplicative())
//                                                .mean(true)
//                                                .whiteNoise(false)
//                                                .build();
//                            }),
//                    new SpectrumUI());
//        }
//        
//        @Override
//        public int getPosition() {
//            return 5330;
//        }
//    }
//
////</editor-fold>
}
