/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus.desktop.plugin.mx13.ui;

//import jdplus.toolkit.base.api.data.DoubleSeq;
//import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
//import jdplus.highfreq.desktop.plugin.ui.HtmlFractionalAirlineModel;
//import jdplus.toolkit.desktop.plugin.modelling.InputFactory;
//import jdplus.toolkit.desktop.plugin.modelling.RegSarimaViews;
//import jdplus.sa.desktop.plugin.ui.SaViews;
//import jdplus.toolkit.desktop.plugin.ui.processing.GenericChartUI;
//import jdplus.toolkit.desktop.plugin.ui.processing.GenericTableUI;
//import jdplus.toolkit.desktop.plugin.ui.processing.HtmlItemUI;
//import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentItemFactory;
//import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentViewFactory;
//import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
//import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentViewFactory;
//import jdplus.toolkit.desktop.plugin.ui.processing.stats.DistributionUI;
//import jdplus.toolkit.desktop.plugin.ui.processing.stats.PeriodogramUI;
//import jdplus.toolkit.desktop.plugin.html.HtmlElement;
//import jdplus.toolkit.base.api.modelling.ModellingDictionary;
//import jdplus.sa.base.api.SaDictionaries;
//import jdplus.stl.base.api.StlDictionaries;
//import jdplus.toolkit.base.api.timeseries.TsDocument;
//import jdplus.toolkit.base.api.util.Id;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.Function;
//import jdplus.highfreq.base.core.regarima.HighFreqRegArimaModel;
//import jdplus.stl.base.core.mstlplus.MStlPlusDocument;
//import org.openide.util.lookup.ServiceProvider;
//
///**
// *
// * @author Jean Palate
// */
//public class MStlPlusViewFactory extends ProcDocumentViewFactory<MStlPlusDocument> {
//
//    private static final AtomicReference<IProcDocumentViewFactory<MStlPlusDocument>> INSTANCE = new AtomicReference();
//    private final static Function<MStlPlusDocument, HighFreqRegArimaModel> MODELEXTRACTOR = doc -> doc.getResult().getPreprocessing();
//    private final static Function<MStlPlusDocument, DoubleSeq> RESEXTRACTOR = doc -> {
//        HighFreqRegArimaModel result = doc.getResult().getPreprocessing();
//        return result == null ? null : result.getResiduals().getRes();
//    };
//
//    public static IProcDocumentViewFactory<MStlPlusDocument> getDefault() {
//        IProcDocumentViewFactory<MStlPlusDocument> fac = INSTANCE.get();
//        if (fac == null) {
//            fac = new MStlPlusViewFactory();
//            INSTANCE.lazySet(fac);
//        }
//        return fac;
//    }
//
//    public static void setDefault(IProcDocumentViewFactory<MStlPlusDocument> factory) {
//        INSTANCE.set(factory);
//    }
//
//    public MStlPlusViewFactory() {
//        registerFromLookup(MStlPlusDocument.class);
//    }
//
//    @Override
//    public Id getPreferredView() {
//        return SaViews.MAIN_CHARTS_LOW;
//    }
//
//    private static String generateId(String name, String id) {
//        return TsDynamicProvider.CompositeTs.builder()
//                .name(name)
//                .now(id)
//                .build().toString();
//    }
//
//    public static String[] lowSeries() {
//        return new String[]{
//            generateId("Series", SaDictionaries.Y),
//            generateId("Seasonally adjusted", SaDictionaries.SA),
//            generateId("Trend", SaDictionaries.T)
//        };
//    }
//
//    public static String[] highSeries() {
//        return new String[]{
//            generateId("Seasonal", StlDictionaries.S),
//            generateId("Yearly seasonal", StlDictionaries.SY),
//            generateId("Weekly seasonal", StlDictionaries.SW),
//            generateId("Irregular", SaDictionaries.I),
//            generateId("Calendar effects", ModellingDictionary.CAL)};
//    }
//
//    public static String[] finalSeries() {
//        return new String[]{
//            generateId("Series", SaDictionaries.Y),
//            generateId("Seasonally adjusted", SaDictionaries.SA),
//            generateId("Trend", SaDictionaries.T),
//            generateId("Seasonal", SaDictionaries.S),
//            generateId("Irregular", SaDictionaries.I)
//        };
//    }
//
////<editor-fold defaultstate="collapsed" desc="REGISTER SPEC">
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 100010)
////    public static class SpecFactory extends ProcDocumentItemFactory<FractionalAirlineDocument, HtmlElement> {
////
////        public SpecFactory() {
////            super(StlPlusDocument.class, RegSarimaViews.INPUT_SPEC,
////                    (StlPlusDocument doc) -> {
////                        InformationSet info = FractionalAirlineSpecMapping.write(doc.getSpecification(), true);
////                        return new demetra.html.core.HtmlInformationSet(info);
////                    },
////                    new HtmlItemUI()
////            );
////        }
////
////        @Override
////        public int getPosition() {
////            return 100010;
////        }
////    }
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1000)
//    public static class Input extends InputFactory<MStlPlusDocument> {
//
//        public Input() {
//            super(MStlPlusDocument.class, RegSarimaViews.INPUT_SERIES);
//        }
//
//        @Override
//        public int getPosition() {
//            return 1000;
//        }
//    }
//
////</editor-fold>
////
////<editor-fold defaultstate="collapsed" desc="REGISTER SUMMARY">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3000)
//    public static class SummaryFactory extends ProcDocumentItemFactory<MStlPlusDocument, HtmlElement> {
//
//        public SummaryFactory() {
//            super(MStlPlusDocument.class, RegSarimaViews.MODEL_SUMMARY,
//                    source -> {
//                        HighFreqRegArimaModel preprocessing = source.getResult().getPreprocessing();
//                        if (preprocessing == null) {
//                            return null;
//                        }
//                        return new HtmlFractionalAirlineModel(preprocessing, false);
//                    },
//                    new HtmlItemUI());
//        }
//
//        @Override
//        public int getPosition() {
//            return 3000;
//        }
//    }
////</editor-fold>
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2000)
//    public static class MainLowChart extends ProcDocumentItemFactory<MStlPlusDocument, TsDocument> {
//
//        public MainLowChart() {
//            super(MStlPlusDocument.class, SaViews.MAIN_CHARTS_LOW, s -> s, new GenericChartUI(false, lowSeries()));
//        }
//
//        @Override
//        public int getPosition() {
//            return 2000;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2100)
//    public static class MainHighChart extends ProcDocumentItemFactory<MStlPlusDocument, TsDocument> {
//
//        public MainHighChart() {
//            super(MStlPlusDocument.class, SaViews.MAIN_CHARTS_HIGH, s -> s, new GenericChartUI(false, highSeries()));
//        }
//
//        @Override
//        public int getPosition() {
//            return 2100;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2200)
//    public static class MainTable extends ProcDocumentItemFactory<MStlPlusDocument, TsDocument> {
//
//        public MainTable() {
//            super(MStlPlusDocument.class, SaViews.MAIN_TABLE, s -> s, new GenericTableUI(false, finalSeries()));
//        }
//
//        @Override
//        public int getPosition() {
//            return 2200;
//        }
//
//    }
//
////<editor-fold defaultstate="collapsed" desc="REGISTER FORECASTS">
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 500)
////    public static class ForecastsTable extends ProcDocumentItemFactory<FractionalAirlineDocument, TsDocument> {
////
////        public ForecastsTable() {
////            super(StlPlusDocument.class, RegSarimaViews.MODEL_FCASTS_TABLE, s -> s, new GenericTableUI(false, generateItems()));
////        }
////
////        @Override
////        public int getPosition() {
////            return 200500;
////        }
////
////        private static String[] generateItems() {
////            return new String[]{ModellingDictionary.Y + SeriesInfo.F_SUFFIX, ModellingDictionary.Y + SeriesInfo.EF_SUFFIX};
////        }
////    }
////
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 1000)
////    public static class FCastsFactory extends ForecastsFactory<FractionalAirlineDocument> {
////
////        public FCastsFactory() {
////            super(StlPlusDocument.class, RegSarimaViews.MODEL_FCASTS, MODELEXTRACTOR);
////        }
////
////        @Override
////        public int getPosition() {
////            return 201000;
////        }
////    }
////
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 2000)
////    public static class FCastsOutFactory extends OutOfSampleTestFactory<FractionalAirlineDocument> {
////
////        public FCastsOutFactory() {
////            super(StlPlusDocument.class, RegSarimaViews.MODEL_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
////        }
////
////        @Override
////        public int getPosition() {
////            return 202000;
////        }
////    }
////</editor-fold>
////
////<editor-fold defaultstate="collapsed" desc="REGISTER MODEL">
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 1000)
////    public static class ModelRegsFactory extends ModelRegressorsFactory<FractionalAirlineDocument> {
////
////        public ModelRegsFactory() {
////            super(StlPlusDocument.class, RegSarimaViews.MODEL_REGS, MODELEXTRACTOR);
////        }
////
////        @Override
////        public int getPosition() {
////            return 301000;
////        }
////    }
////
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 2000)
////    public static class ArimaFactory extends ModelArimaFactory {
////
////        public ArimaFactory() {
////            super(StlPlusDocument.class, RegSarimaViews.MODEL_ARIMA, MODELEXTRACTOR);
////        }
////
////        @Override
////        public int getPosition() {
////            return 302000;
////        }
////    }
////
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 3000)
////    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<FractionalAirlineDocument, StlPlusDocument> {
////
////        public PreprocessingDetFactory() {
////            super(StlPlusDocument.class, RegSarimaViews.MODEL_DET,
////                    source -> source, new GenericTableUI(false,
////                            ModellingDictionary.Y_LIN, ModellingDictionary.DET,
////                            ModellingDictionary.CAL, ModellingDictionary.TDE, ModellingDictionary.EE,
////                            ModellingDictionary.OUT, ModellingDictionary.FULL_RES));
////        }
////
////        @Override
////        public int getPosition() {
////            return 303000;
////        }
////    }
////</editor-fold>
////<editor-fold defaultstate="collapsed" desc="REGISTER RESIDUALS">
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3100)
//    public static class ModelResDist extends ProcDocumentItemFactory<MStlPlusDocument, DoubleSeq> {
//
//        public ModelResDist() {
//            super(MStlPlusDocument.class, RegSarimaViews.MODEL_RES_DIST, RESEXTRACTOR,
//                    new DistributionUI());
//
//        }
//
//        @Override
//        public int getPosition() {
//            return 3100;
//        }
//    }
//
//    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3200)
//    public static class ModelResSpectrum extends ProcDocumentItemFactory<MStlPlusDocument, DoubleSeq> {
//
//        public ModelResSpectrum() {
//            super(MStlPlusDocument.class, RegSarimaViews.MODEL_RES_SPECTRUM, RESEXTRACTOR,
//                    new PeriodogramUI());
//
//        }
//
//        @Override
//        public int getPosition() {
//            return 3200;
//        }
//    }
////</editor-fold>
////<editor-fold defaultstate="collapsed" desc="REGISTER DETAILS">
////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 500000)
////    public static class LFactory extends LikelihoodFactory<FractionalAirlineDocument> {
////
////        public LFactory() {
////            super(StlPlusDocument.class, RegSarimaViews.MODEL_LIKELIHOOD, MODELEXTRACTOR);
////            setAsync(true);
////        }
////
////        @Override
////        public int getPosition() {
////            return 500000;
////        }
////    }
////</editor-fold>
//}
