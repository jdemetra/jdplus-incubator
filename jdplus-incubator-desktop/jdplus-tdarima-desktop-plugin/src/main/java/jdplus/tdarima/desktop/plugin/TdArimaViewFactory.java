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
package jdplus.tdarima.desktop.plugin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import jdplus.advancedsa.base.api.tdarima.TdArimaDictionaries;
import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.advancedsa.base.core.tdarima.TdArimaDocument;
import jdplus.advancedsa.base.core.tdarima.TdDecompositionResults;
import jdplus.advancedsa.base.core.tdarima.TdModellingResults;
import jdplus.advancedsa.base.core.tdarima.TdResults;
import jdplus.advancedsa.base.core.tdarima.TdSarimaResults;
import jdplus.advancedsa.base.core.tdarima.extractors.TdDecompositionExtractor;
import jdplus.sa.base.api.ComponentDescriptor;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.desktop.plugin.processing.SIFactory;
import jdplus.sa.desktop.plugin.ui.SaViews;
import jdplus.tdarima.desktop.plugin.html.HtmlTdArimaModel;
import jdplus.tdarima.desktop.plugin.html.HtmlTdArimaSelection;
import jdplus.tdarima.desktop.plugin.ui.KfSquaredGainsUI;
import jdplus.tdarima.desktop.plugin.ui.KfWeightsUI;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.toolkit.base.core.math.matrices.MatrixStorage;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import jdplus.toolkit.desktop.plugin.modelling.ForecastsFactory;
import jdplus.toolkit.desktop.plugin.modelling.InputFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelArimaFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelRegressorsFactory;
import jdplus.toolkit.desktop.plugin.modelling.NiidTestsFactory;
import jdplus.toolkit.desktop.plugin.modelling.OutOfSampleTestFactory;
import jdplus.toolkit.desktop.plugin.modelling.RegSarimaViews;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericChartUI;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericTableUI;
import jdplus.toolkit.desktop.plugin.ui.processing.HtmlItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsDistUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
public class TdArimaViewFactory extends ProcDocumentViewFactory<TdArimaDocument> {

    public static final String TDARIMA = "TD Arima",
            SARIMA = "Arima",
            SELECTION = "Selection",
            ESTIMATION = "Final model",
            STOCHASTIC = "Stochastic series",
            COMPONENTS = "Components",
            MODELBASED = "Model-based tests",
            KF_ANALYSIS = "Kalman filter",
            KF_FILTERS = "Filters",
            KF_GAINS = "Squared gains",
            KF_PHASE = "Preliminary estimators",
            SEAS_IRR = "Seas., irr.",
            STDERR = "Stde of the innovations";
    public static final Id DECOMPOSITION_SUMMARY = new LinearId(SaViews.DECOMPOSITION);
    public static final Id DECOMPOSITION_SARIMA_SERIES = new LinearId(SaViews.DECOMPOSITION, SARIMA, STOCHASTIC);
    public static final Id DECOMPOSITION_SARIMA_SERIES_LOW =
            new LinearId(SaViews.DECOMPOSITION, SARIMA, STOCHASTIC, SaViews.SA_TREND);
    public static final Id DECOMPOSITION_SARIMA_SERIES_HIGH =
            new LinearId(SaViews.DECOMPOSITION, SARIMA, STOCHASTIC, SEAS_IRR);
    public static final Id DECOMPOSITION_SARIMA_CMPSERIES = new LinearId(SaViews.DECOMPOSITION, SARIMA, COMPONENTS);
    public static final Id DECOMPOSITION_SARIMA_CMPSERIES_LOW =
            new LinearId(SaViews.DECOMPOSITION, SARIMA, COMPONENTS, SaViews.SA_TREND);
    public static final Id DECOMPOSITION_SARIMA_CMPSERIES_HIGH =
            new LinearId(SaViews.DECOMPOSITION, SARIMA, COMPONENTS, SEAS_IRR);
    public static final Id DECOMPOSITION_SARIMA_SI = new LinearId(SaViews.DECOMPOSITION, SARIMA, SaViews.SI_RATIO);
    public static final Id DECOMPOSITION_TDARIMA_SERIES = new LinearId(SaViews.DECOMPOSITION, TDARIMA, STOCHASTIC);
    public static final Id DECOMPOSITION_TDARIMA_SERIES_LOW =
            new LinearId(SaViews.DECOMPOSITION, TDARIMA, STOCHASTIC, SaViews.SA_TREND);
    public static final Id DECOMPOSITION_TDARIMA_SERIES_HIGH =
            new LinearId(SaViews.DECOMPOSITION, TDARIMA, STOCHASTIC, SEAS_IRR);
    public static final Id DECOMPOSITION_TDARIMA_CMPSERIES = new LinearId(SaViews.DECOMPOSITION, TDARIMA, COMPONENTS);
    public static final Id DECOMPOSITION_TDARIMA_CMPSERIES_LOW =
            new LinearId(SaViews.DECOMPOSITION, TDARIMA, COMPONENTS, SaViews.SA_TREND);
    public static final Id DECOMPOSITION_TDARIMA_CMPSERIES_HIGH =
            new LinearId(SaViews.DECOMPOSITION, TDARIMA, COMPONENTS, SEAS_IRR);
    public static final Id DECOMPOSITION_TDARIMA_SI = new LinearId(SaViews.DECOMPOSITION, TDARIMA, SaViews.SI_RATIO);
    public static final Id DECOMPOSITION_TDARIMA_VARS = new LinearId(SaViews.DECOMPOSITION, TDARIMA, STDERR);
    public static final Id DECOMPOSITION_SARIMA_KF_FILTERS =
            new LinearId(SaViews.DECOMPOSITION, SARIMA, KF_ANALYSIS, KF_FILTERS);
    public static final Id DECOMPOSITION_SARIMA_KF_GAINS =
            new LinearId(SaViews.DECOMPOSITION, SARIMA, KF_ANALYSIS, KF_GAINS);
    public static final Id DECOMPOSITION_TDARIMA_KF_FILTERS =
            new LinearId(SaViews.DECOMPOSITION, TDARIMA, KF_ANALYSIS, KF_FILTERS);
    public static final Id DECOMPOSITION_TDARIMA_KF_GAINS =
            new LinearId(SaViews.DECOMPOSITION, TDARIMA, KF_ANALYSIS, KF_GAINS);
    public static final Id DECOMPOSITION_TESTS = new LinearId(SaViews.DECOMPOSITION, MODELBASED);
    public static final Id MODEL_SELECTION = new LinearId(TDARIMA, SELECTION);
    public static final Id MODEL_ESTIMATION = new LinearId(TDARIMA, ESTIMATION);
    public static final Id MODEL_LIKELIHOOD = new LinearId(TDARIMA, SaViews.LIKELIHOOD);

    private static final AtomicReference<IProcDocumentViewFactory<TdArimaDocument>> INSTANCE = new AtomicReference();

    public static IProcDocumentViewFactory<TdArimaDocument> getDefault() {
        IProcDocumentViewFactory<TdArimaDocument> fac = INSTANCE.get();
        if (fac == null) {
            fac = new TdArimaViewFactory();
            INSTANCE.lazySet(fac);
        }
        return fac;
    }

    public static void setDefault(IProcDocumentViewFactory<TdArimaDocument> factory) {
        INSTANCE.set(factory);
    }

    public TdArimaViewFactory() {
        registerFromLookup(TdArimaDocument.class);
    }

    @Override
    public Id getPreferredView() {
        return MODEL_SELECTION;
    }

    private static final Function<TdArimaDocument, TdArimaDocument> VALIDEXTRACTOR = source -> {
        TdModellingResults tr = source.getResult();
        if (tr == null) {
            return null;
        }
        return tr.isValid() ? source : null;
    };

    private static final Function<TdArimaDocument, RegSarimaModel> MODELEXTRACTOR = source -> {
        TdModellingResults tr = source.getResult();
        return tr == null ? null : tr.getPreprocessing();
    };

    //    private final static Function<TdArimaDocument, StsDiagnostics> DIAGSEXTRACTOR = source -> {
    //        TdResults tr = source.getResult();
    //        return tr == null ? null : tr.getDiagnostics();
    //    };
    //
    private static final Function<TdArimaDocument, TdResults> TDARIMAEXTRACTOR = source -> {
        TdModellingResults tr = source.getResult();
        return tr == null ? null : tr.getTdarima();
    };

    private static final Function<TdArimaDocument, TdDecompositionResults> DECOMPOSITIONEXTRACTOR = source -> {
        TdModellingResults tr = source.getResult();
        return tr == null ? null : tr.getDecomposition();
    };

    private static final Function<TdArimaDocument, TdSarimaResults> TDFINALARIMAEXTRACTOR = source -> {
        TdModellingResults tr = source.getResult();
        if (tr == null || tr.getTdarima() == null || !tr.getTdarima().isTimeVarying()) {
            return null;
        }
        return tr.getTdarima().finalResults();
    };
    private static final Function<TdArimaDocument, TsData> RESEXTRACTOR =
            MODELEXTRACTOR.andThen(regarima -> regarima == null ? null : regarima.fullResiduals());

    private static String generateId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .now(id)
                .build()
                .toString();
    }

    //    public static String[] lowSeries() {
    //        return new String[]{
    //            generateId("Series", SaDictionaries.Y),
    //            generateId("Seasonally adjusted", SaDictionaries.SA),
    //            generateId("Trend", SaDictionaries.T)
    //        };
    //    }
    //
    private static final String[] highSeries() {
        return new String[] {
            generateId("Seasonal", SaDictionaries.S),
            generateId("Calendar effects", SaDictionaries.CAL),
            generateId("Irregular", SaDictionaries.I)
        };
    }

    private static final String[] finalSeries() {
        return new String[] {
            generateId("Series", SaDictionaries.Y),
            generateId("Seasonally adjusted", SaDictionaries.SA),
            generateId("Trend", SaDictionaries.T),
            generateId("Seasonal", SaDictionaries.S),
            generateId("Irregular", SaDictionaries.I)
        };
    }

    private static String[] highDecompositionSeries(boolean initial, boolean cmps) {
        String rslt = initial ? TdArimaDictionaries.SARIMA : TdArimaDictionaries.TDSARIMA;
        String decomp = cmps ? TdArimaDictionaries.SACMPS : TdArimaDictionaries.SASTOCH;
        String prefix = Dictionary.concatenate(SaDictionaries.DECOMPOSITION, rslt, decomp);
        return new String[] {
            generateId("Seasonal", Dictionary.concatenate(prefix, SaDictionaries.S)),
            generateId("Irregular", Dictionary.concatenate(prefix, SaDictionaries.I))
        };
    }

    private static String[] lowDecompositionSeries(boolean initial, boolean cmps) {
        String rslt = initial ? TdArimaDictionaries.SARIMA : TdArimaDictionaries.TDSARIMA;
        String decomp = cmps ? TdArimaDictionaries.SACMPS : TdArimaDictionaries.SASTOCH;
        String prefix = Dictionary.concatenate(SaDictionaries.DECOMPOSITION, rslt, decomp);
        return new String[] {
            generateId("Series", Dictionary.concatenate(prefix, SaDictionaries.Y)),
            generateId("Seasonally adjusted", Dictionary.concatenate(prefix, SaDictionaries.SA)),
            generateId("Trend", Dictionary.concatenate(prefix, SaDictionaries.T)),
        };
    }

    private static final String STDE = "_e";

    private static String[] decompositionSeries(boolean initial, boolean cmps) {
        String rslt = initial ? TdArimaDictionaries.SARIMA : TdArimaDictionaries.TDSARIMA;
        String decomp = cmps ? TdArimaDictionaries.SACMPS : TdArimaDictionaries.SASTOCH;
        String prefix = Dictionary.concatenate(SaDictionaries.DECOMPOSITION, rslt, decomp);

        return cmps
                ? new String[] {
                    generateId("Series", Dictionary.concatenate(prefix, SaDictionaries.Y)),
                    generateId("Seasonally adjusted", Dictionary.concatenate(prefix, SaDictionaries.SA)),
                    generateId("Trend", Dictionary.concatenate(prefix, SaDictionaries.T)),
                    generateId("Seasonal", Dictionary.concatenate(prefix, SaDictionaries.S)),
                    generateId("Irregular", Dictionary.concatenate(prefix, SaDictionaries.I))
                }
                : new String[] {
                    generateId("Series", Dictionary.concatenate(prefix, SaDictionaries.Y)),
                    generateId("Seasonally adjusted", Dictionary.concatenate(prefix, SaDictionaries.SA)),
                    generateId("Trend", Dictionary.concatenate(prefix, SaDictionaries.T)),
                    generateId("Seasonal", Dictionary.concatenate(prefix, SaDictionaries.S)),
                    generateId("Irregular", Dictionary.concatenate(prefix, SaDictionaries.I)),
                    generateId("Seasonally adjusted (stde)", Dictionary.concatenate(prefix, SaDictionaries.SA + STDE)),
                    generateId("Trend (stde)", Dictionary.concatenate(prefix, SaDictionaries.T + STDE)),
                    generateId("Seasonal (stde)", Dictionary.concatenate(prefix, SaDictionaries.S + STDE)),
                    generateId("Irregular (stde)", Dictionary.concatenate(prefix, SaDictionaries.I + STDE))
                };
    }

    //
    ////// <editor-fold defaultstate="collapsed" desc="REGISTER SPEC">
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1010)
    ////    public static class SpecFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement> {
    ////
    ////        public SpecFactory() {
    ////            super(TdArimaDocument.class, RegSarimaViews.INPUT_SPEC,
    ////                    (TdArimaDocument doc) -> {
    ////                        InformationSet info = StsSpecMapping.write(doc.getSpecification(),
    //// doc.getInput().getData().getDomain(), true);
    ////                        return new HtmlInformationSet(info);
    ////                    },
    ////                    new HtmlItemUI()
    ////            );
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 1010;
    ////        }
    ////    }
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1000)
    public static class Input extends InputFactory<TdArimaDocument> {

        public Input() {
            super(TdArimaDocument.class, RegSarimaViews.INPUT_SERIES);
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }

    ////// </editor-fold>
    //////
    ////// <editor-fold defaultstate="collapsed" desc="REGISTER SUMMARY">
    //////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 100000 + 1000)
    //////    public static class SummaryFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement> {
    //////
    //////        public SummaryFactory() {
    //////            super(TdArimaDocument.class, RegSarimaViews.MODEL_SUMMARY,
    //////                    source -> new HtmlFractionalAirlineModel(source.getResult(), false),
    //////                    new HtmlItemUI());
    //////        }
    //////
    //////        @Override
    //////        public int getPosition() {
    //////            return 101000;
    //////        }
    //////    }
    ////// </editor-fold>
    //    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2000)
    //    public static class MainLowChart extends ProcDocumentItemFactory<TdArimaDocument, TsDocument> {
    //
    //        public MainLowChart() {
    //            super(TdArimaDocument.class, SaViews.MAIN_CHARTS_LOW, s -> s, new GenericChartUI(false, lowSeries()));
    //        }
    //
    //        @Override
    //        public int getPosition() {
    //            return 2000;
    //        }
    //    }
    //
    //    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2100)
    //    public static class MainHighChart extends ProcDocumentItemFactory<TdArimaDocument, TsDocument> {
    //
    //        public MainHighChart() {
    //            super(TdArimaDocument.class, SaViews.MAIN_CHARTS_HIGH, s -> s, new GenericChartUI(false,
    // highSeries()));
    //        }
    //
    //        @Override
    //        public int getPosition() {
    //            return 2100;
    //        }
    //    }
    //
    //    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2200)
    //    public static class MainTable extends ProcDocumentItemFactory<TdArimaDocument, TsDocument> {
    //
    //        public MainTable() {
    //            super(TdArimaDocument.class, SaViews.MAIN_TABLE, s -> s, new GenericTableUI(false, finalSeries()));
    //        }
    //
    //        @Override
    //        public int getPosition() {
    //            return 2200;
    //        }
    //
    //    }
    //
    //    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2400)
    //    public static class MainSiFactory extends SIFactory<TdArimaDocument> {
    //
    //        public MainSiFactory() {
    //            super(TdArimaDocument.class, SaViews.MAIN_SI, (TdArimaDocument source) -> {
    //                TdResults result = source.getResult();
    //                if (result == null) {
    //                    return null;
    //                }
    //                return result.getComponents();
    //            });
    //        }
    //
    //        @Override
    //        public int getPosition() {
    //            return 2400;
    //        }
    //    }
    //
    //// <editor-fold defaultstate="collapsed" desc="PREPROCESSING">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3000)
    public static class SummaryFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement> {

        public SummaryFactory() {

            super(
                    TdArimaDocument.class,
                    SaViews.PREPROCESSING_SUMMARY,
                    MODELEXTRACTOR.andThen(regarima -> regarima == null ? null : new HtmlRegSarima(regarima, false)),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 3000;
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="PREPROCESSING-FORECASTS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3110)
    public static class ForecastsTable extends ProcDocumentItemFactory<TdArimaDocument, TsDocument> {

        public ForecastsTable() {
            super(
                    TdArimaDocument.class,
                    SaViews.PREPROCESSING_FCASTS_TABLE,
                    s -> s,
                    new GenericTableUI(false, generateItems()));
        }

        @Override
        public int getPosition() {
            return 3110;
        }

        private static String[] generateItems() {
            return new String[] {RegressionDictionaries.Y_F, RegressionDictionaries.Y_EF};
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3100)
    public static class FCastsFactory extends ForecastsFactory<TdArimaDocument> {

        public FCastsFactory() {
            super(TdArimaDocument.class, SaViews.PREPROCESSING_FCASTS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3120)
    public static class FCastsOutFactory extends OutOfSampleTestFactory<TdArimaDocument> {

        public FCastsOutFactory() {
            super(TdArimaDocument.class, SaViews.PREPROCESSING_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3120;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PREPROCESSING-DETAILS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3200)
    public static class ModelRegsFactory extends ModelRegressorsFactory<TdArimaDocument> {

        public ModelRegsFactory() {
            super(TdArimaDocument.class, SaViews.PREPROCESSING_REGS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3300)
    public static class ArimaFactory extends ModelArimaFactory {

        public ArimaFactory() {
            super(TdArimaDocument.class, SaViews.PREPROCESSING_ARIMA, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3300;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3400)
    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<TdArimaDocument, TsDocument> {

        public PreprocessingDetFactory() {
            super(
                    TdArimaDocument.class,
                    SaViews.PREPROCESSING_DET,
                    source -> source,
                    new GenericTableUI(
                            false,
                            Dictionary.concatenate(SaDictionaries.PREPROCESSING, SaDictionaries.YCAL),
                            Dictionary.concatenate(SaDictionaries.PREPROCESSING, RegressionDictionaries.YLIN),
                            Dictionary.concatenate(SaDictionaries.PREPROCESSING, RegressionDictionaries.DET),
                            Dictionary.concatenate(SaDictionaries.PREPROCESSING, SaDictionaries.CAL),
                            Dictionary.concatenate(SaDictionaries.PREPROCESSING, SaDictionaries.TDE),
                            Dictionary.concatenate(SaDictionaries.PREPROCESSING, SaDictionaries.EE),
                            Dictionary.concatenate(SaDictionaries.PREPROCESSING, RegressionDictionaries.OUT)));
        }

        @Override
        public int getPosition() {
            return 3400;
        }
    }

    // </editor-fold>
    //
    // <editor-fold defaultstate="collapsed" desc="PREPROCESSING-RESIDUALS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3500)
    public static class ModelResFactory extends ProcDocumentItemFactory<TdArimaDocument, TsData> {

        public ModelResFactory() {
            super(TdArimaDocument.class, SaViews.PREPROCESSING_RES, RESEXTRACTOR, new ResidualsUI());
        }

        @Override
        public int getPosition() {
            return 3500;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3510)
    public static class ModelResStatsFactory extends NiidTestsFactory<TdArimaDocument> {

        public ModelResStatsFactory() {
            super(TdArimaDocument.class, SaViews.PREPROCESSING_RES_STATS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3510;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3520)
    public static class ModelResDist extends ProcDocumentItemFactory<TdArimaDocument, TsData> {

        public ModelResDist() {
            super(TdArimaDocument.class, SaViews.PREPROCESSING_RES_DIST, RESEXTRACTOR, new ResidualsDistUI());
        }

        @Override
        public int getPosition() {
            return 3520;
        }
    }

    // </editor-fold>
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4000)
    public static class TdArimaSelectionFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement> {

        public TdArimaSelectionFactory() {

            super(
                    TdArimaDocument.class,
                    MODEL_SELECTION,
                    TDARIMAEXTRACTOR.andThen(
                            tdresults -> tdresults == null ? null : new HtmlTdArimaSelection(tdresults)),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4010)
    public static class TdArimaModelFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement> {

        public TdArimaModelFactory() {

            super(
                    TdArimaDocument.class,
                    MODEL_ESTIMATION,
                    source -> new HtmlTdArimaModel(source.getResult(), false),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6000)
    public static class DecompositionSarimaSeries extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionSarimaSeries() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_SARIMA_SERIES,
                    VALIDEXTRACTOR,
                    new GenericTableUI(true, decompositionSeries(true, false)));
        }

        @Override
        public int getPosition() {
            return 6000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6010)
    public static class DecompositionSarimaSeriesLow extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionSarimaSeriesLow() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_SARIMA_SERIES_LOW,
                    VALIDEXTRACTOR,
                    new GenericChartUI(true, lowDecompositionSeries(true, false)));
        }

        @Override
        public int getPosition() {
            return 6010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6020)
    public static class DecompositionSarimaSeriesHigh
            extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionSarimaSeriesHigh() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_SARIMA_SERIES_HIGH,
                    VALIDEXTRACTOR,
                    new GenericChartUI(false, highDecompositionSeries(true, false)));
        }

        @Override
        public int getPosition() {
            return 6020;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6100)
    public static class DecompositionSarimaCmps extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionSarimaCmps() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_SARIMA_CMPSERIES,
                    VALIDEXTRACTOR,
                    new GenericTableUI(true, decompositionSeries(true, true)));
        }

        @Override
        public int getPosition() {
            return 6000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6110)
    public static class DecompositionSarimaCmpsLow extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionSarimaCmpsLow() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_SARIMA_CMPSERIES_LOW,
                    VALIDEXTRACTOR,
                    new GenericChartUI(true, lowDecompositionSeries(true, true)));
        }

        @Override
        public int getPosition() {
            return 6110;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6120)
    public static class DecompositionSarimaCmpsHigh extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionSarimaCmpsHigh() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_SARIMA_CMPSERIES_HIGH,
                    VALIDEXTRACTOR,
                    new GenericChartUI(true, highDecompositionSeries(true, true)));
        }

        @Override
        public int getPosition() {
            return 6120;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6200)
    public static class DecompositionSarimaSiFactory extends SIFactory<TdArimaDocument> {

        public DecompositionSarimaSiFactory() {
            super(TdArimaDocument.class, DECOMPOSITION_SARIMA_SI, (TdArimaDocument source) -> {
                TdModellingResults result = source.getResult();
                if (result == null || !result.isValid()) {
                    return null;
                }
                return result.getDecomposition().getStochasticSeries();
            });
        }

        @Override
        public int getPosition() {
            return 6200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6400)
    public static class DecompositionSarimaFilters
            extends ProcDocumentItemFactory<TdArimaDocument, KfWeightsUI.Information> {

        public DecompositionSarimaFilters() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_SARIMA_KF_FILTERS,
                    (TdArimaDocument doc) -> {
                        TdModellingResults result = doc.getResult();
                        if (result == null || !result.isValid()) {
                            return null;
                        }
                        MatrixStorage w = result.getDecomposition().weights();

                        TsDomain domain = doc.getInput().getData().getDomain();
                        IntList items = new IntList();
                        List<String> names = new ArrayList<>();
                        filtersPosition(doc.getSpecification(), domain, items, names);

                        return new KfWeightsUI.Information(
                                w,
                                TdModelSpec.COMPONENTDESCRIPTORS.toArray(ComponentDescriptor[]::new),
                                items.toArray(),
                                names.toArray(String[]::new));
                    },
                    new KfWeightsUI());
        }

        @Override
        public int getPosition() {
            return 6400;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6410)
    public static class DecompositionSarimaFiltersGain
            extends ProcDocumentItemFactory<TdArimaDocument, KfSquaredGainsUI.Information> {

        public DecompositionSarimaFiltersGain() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_SARIMA_KF_GAINS,
                    (TdArimaDocument doc) -> {
                        TdModellingResults result = doc.getResult();
                        if (result == null || !result.isValid()) {
                            return null;
                        }
                        MatrixStorage w = result.getDecomposition().weights();

                        TsDomain domain = doc.getInput().getData().getDomain();
                        IntList items = new IntList();
                        List<String> names = new ArrayList<>();
                        filtersPosition(doc.getSpecification(), domain, items, names);

                        return new KfSquaredGainsUI.Information(
                                domain.getAnnualFrequency(),
                                w,
                                TdModelSpec.COMPONENTDESCRIPTORS.toArray(ComponentDescriptor[]::new),
                                items.toArray(),
                                names.toArray(String[]::new));
                    },
                    new KfSquaredGainsUI());
        }

        @Override
        public int getPosition() {
            return 6410;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7000)
    public static class DecompositionTdArimaSeries extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionTdArimaSeries() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_SERIES,
                    VALIDEXTRACTOR,
                    new GenericTableUI(true, decompositionSeries(false, false)));
        }

        @Override
        public int getPosition() {
            return 7000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7010)
    public static class DecompositionTdArimaSeriesLow
            extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionTdArimaSeriesLow() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_SERIES_LOW,
                    VALIDEXTRACTOR,
                    new GenericChartUI(true, lowDecompositionSeries(false, false)));
        }

        @Override
        public int getPosition() {
            return 7010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7020)
    public static class DecompositionTdArimaSeriesHigh
            extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionTdArimaSeriesHigh() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_SERIES_HIGH,
                    VALIDEXTRACTOR,
                    new GenericChartUI(true, highDecompositionSeries(false, false)));
        }

        @Override
        public int getPosition() {
            return 7020;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7100)
    public static class DecompositionTdArimaCmps extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionTdArimaCmps() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_CMPSERIES,
                    VALIDEXTRACTOR,
                    new GenericTableUI(true, decompositionSeries(false, true)));
        }

        @Override
        public int getPosition() {
            return 7100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7110)
    public static class DecompositionTdArimaCmpsLow extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionTdArimaCmpsLow() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_CMPSERIES_LOW,
                    VALIDEXTRACTOR,
                    new GenericChartUI(true, lowDecompositionSeries(false, true)));
        }

        @Override
        public int getPosition() {
            return 7110;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7120)
    public static class DecompositionTdArimaCmpsHigh extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionTdArimaCmpsHigh() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_CMPSERIES_HIGH,
                    VALIDEXTRACTOR,
                    new GenericChartUI(true, highDecompositionSeries(false, true)));
        }

        @Override
        public int getPosition() {
            return 7120;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7200)
    public static class DecompositionTdArimaSiFactory extends SIFactory<TdArimaDocument> {

        public DecompositionTdArimaSiFactory() {
            super(TdArimaDocument.class, DECOMPOSITION_TDARIMA_SI, (TdArimaDocument source) -> {
                TdModellingResults result = source.getResult();
                if (result == null || !result.isValid()) {
                    return null;
                }
                return result.getDecomposition().getTdStochasticSeries();
            });
        }

        @Override
        public int getPosition() {
            return 7200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7300)
    public static class DecompositionTdArimaVars extends ProcDocumentItemFactory<TdArimaDocument, TdArimaDocument> {

        public DecompositionTdArimaVars() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_VARS,
                    VALIDEXTRACTOR,
                    new GenericChartUI(true, innovationVariances()));
        }

        @Override
        public int getPosition() {
            return 7300;
        }
    }

    private static String[] innovationVariances() {
        String prefix = Dictionary.concatenate(SaDictionaries.DECOMPOSITION, TdDecompositionExtractor.EVARS);
        return new String[] {
            generateId("Trend", Dictionary.concatenate(prefix, SaDictionaries.T)),
            generateId("Seasonal", Dictionary.concatenate(prefix, SaDictionaries.S)),
            generateId("Irregular", Dictionary.concatenate(prefix, SaDictionaries.I))
        };
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7400)
    public static class DecompositionTdArimaFilters
            extends ProcDocumentItemFactory<TdArimaDocument, KfWeightsUI.Information> {

        public DecompositionTdArimaFilters() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_KF_FILTERS,
                    (TdArimaDocument doc) -> {
                        TdModellingResults result = doc.getResult();
                        if (result == null
                                || !result.isValid()
                                || !result.getDecomposition().isTimeVarying()) {
                            return null;
                        }
                        MatrixStorage w = result.getDecomposition().tdWeights();

                        TsDomain domain = doc.getInput().getData().getDomain();
                        IntList items = new IntList();
                        List<String> names = new ArrayList<>();
                        filtersPosition(doc.getSpecification(), domain, items, names);

                        return new KfWeightsUI.Information(
                                w,
                                TdModelSpec.COMPONENTDESCRIPTORS.toArray(ComponentDescriptor[]::new),
                                items.toArray(),
                                names.toArray(String[]::new));
                    },
                    new KfWeightsUI());
        }

        @Override
        public int getPosition() {
            return 7400;
        }
    }

    private static void filtersPosition(TdModelSpec spec, TsDomain domain, IntList items, List<String> names) {
        int n = domain.length();

        int idx = 0;
        int im = n / 2;
        items.add(idx);
        names.add("Start of the series");
        LocalDate start = spec.getStartArimaRamp();
        if (start != null) {
            idx = domain.indexOf(start.atStartOfDay());
            if (idx == im) {
                im = -1;
            } else if (im < idx) {
                items.add(im);
                names.add("Middle of the series");
                im = -1;
            }
            if (idx > 0 && idx < n - 1) {
                items.add(idx);
                names.add("Start of the ramp");
            }
        }
        LocalDate end = spec.getEndArimaRamp();
        if (end != null) {
            idx = domain.indexOf(end.atStartOfDay());
            if (im >= 0 && im < idx) {
                items.add(im);
                names.add("Middle of the series");
                im = -1;
            }
            if (idx > 0 && idx < n - 1) {
                items.add(idx);
                names.add("End of the ramp");
            }
        }
        idx = n - 1;
        if (im >= 0 && im < idx) {
            items.add(im);
            names.add("Middle of the series");
        }
        items.add(idx);
        names.add("End of the series");
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 7410)
    public static class DecompositionTdArimaFiltersGain
            extends ProcDocumentItemFactory<TdArimaDocument, KfSquaredGainsUI.Information> {

        public DecompositionTdArimaFiltersGain() {
            super(
                    TdArimaDocument.class,
                    DECOMPOSITION_TDARIMA_KF_GAINS,
                    (TdArimaDocument doc) -> {
                        TdModellingResults result = doc.getResult();
                        if (result == null
                                || !result.isValid()
                                || !result.getDecomposition().isTimeVarying()) {
                            return null;
                        }
                        MatrixStorage w = result.getDecomposition().tdWeights();

                        TsDomain domain = doc.getInput().getData().getDomain();
                        IntList items = new IntList();
                        List<String> names = new ArrayList<>();
                        filtersPosition(doc.getSpecification(), domain, items, names);

                        return new KfSquaredGainsUI.Information(
                                domain.getAnnualFrequency(),
                                w,
                                TdModelSpec.COMPONENTDESCRIPTORS.toArray(ComponentDescriptor[]::new),
                                items.toArray(),
                                names.toArray(String[]::new));
                    },
                    new KfSquaredGainsUI());
        }

        @Override
        public int getPosition() {
            return 7410;
        }
    }

    //// <editor-fold defaultstate="collapsed" desc="REGISTER FORECASTS">
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 500)
    ////    public static class ForecastsTable extends ProcDocumentItemFactory<FractionalAirlineDocument,
    //// TsDocument> {
    ////
    ////        public ForecastsTable() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_FCASTS_TABLE, s -> s, new
    //// GenericTableUI(false,
    //// generateItems()));
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 200500;
    ////        }
    ////
    ////        private static String[] generateItems() {
    ////            return new String[]{ModellingDictionary.Y + SeriesInfo.F_SUFFIX, ModellingDictionary.Y +
    //// SeriesInfo.EF_SUFFIX};
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 1000)
    ////    public static class FCastsFactory extends ForecastsFactory<FractionalAirlineDocument> {
    ////
    ////        public FCastsFactory() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_FCASTS, MODELEXTRACTOR);
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
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 202000;
    ////        }
    ////    }
    //// </editor-fold>
    ////
    //// <editor-fold defaultstate="collapsed" desc="REGISTER MODEL">
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 1000)
    ////    public static class ModelRegsFactory extends ModelRegressorsFactory<FractionalAirlineDocument> {
    ////
    ////        public ModelRegsFactory() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_REGS, MODELEXTRACTOR);
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
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_ARIMA, MODELEXTRACTOR);
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 302000;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 3000)
    ////    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<FractionalAirlineDocument,
    //// TdArimaDocument> {
    ////
    ////        public PreprocessingDetFactory() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_DET,
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
    //// </editor-fold>
    //// <editor-fold defaultstate="collapsed" desc="REGISTER RESIDUALS">
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 1000)
    ////    public static class ModelResFactory extends ProcDocumentItemFactory<FractionalAirlineDocument, TsData> {
    ////
    ////        public ModelResFactory() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_RES, RESEXTRACTOR,
    ////                    new ResidualsUI()
    ////            );
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 401000;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 2000)
    ////    public static class ModelResStatsFactory extends NiidTestsFactory<FractionalAirlineDocument> {
    ////
    ////        public ModelResStatsFactory() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_RES_STATS, MODELEXTRACTOR);
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 402000;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 3000)
    ////    public static class ModelResDist extends ProcDocumentItemFactory<TdArimaDocument, DoubleSeq> {
    ////
    ////        public ModelResDist() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_RES_DIST, RESEXTRACTOR,
    ////                    new DistributionUI());
    ////
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 403000;
    ////        }
    ////    }
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 4000)
    ////    public static class ModelResSpectrum extends ProcDocumentItemFactory<TdArimaDocument, DoubleSeq> {
    ////
    ////        public ModelResSpectrum() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_RES_SPECTRUM, RESEXTRACTOR,
    ////                    new PeriodogramUI());
    ////
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 404000;
    ////        }
    ////    }
    //// </editor-fold>
    //// <editor-fold defaultstate="collapsed" desc="REGISTER DETAILS">
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 500000)
    ////    public static class LFactory extends LikelihoodFactory<FractionalAirlineDocument> {
    ////
    ////        public LFactory() {
    ////            super(TdArimaDocument.class, RegSarimaViews.MODEL_LIKELIHOOD, MODELEXTRACTOR);
    ////            setAsync(true);
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 500000;
    ////        }
    ////    }
    //// </editor-fold>
    ////// <editor-fold defaultstate="collapsed" desc="BENCHMARKING">
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4900)
    ////    public static class BenchmarkingFactory extends ProcDocumentItemFactory<TdArimaDocument,
    //// BenchmarkingUI.Input> {
    ////
    ////        public BenchmarkingFactory() {
    ////            super(TdArimaDocument.class, SaViews.BENCHMARKING_SUMMARY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null) {
    ////                    return null;
    ////                }
    ////                SaBenchmarkingResults benchmarking = rslt.getBenchmarking();
    ////                if (benchmarking == null) {
    ////                    return null;
    ////                }
    ////                boolean mul = rslt.getFinals().getMode().isMultiplicative();
    ////                return new BenchmarkingUI.Input(mul, benchmarking);
    ////            }, new BenchmarkingUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 4900;
    ////        }
    ////
    ////    }
    ////// </editor-fold>
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4910)
    ////    public static class StationaryVarianceDecompositionFactory extends
    //// ProcDocumentItemFactory<TdArimaDocument,
    //// HtmlElement> {
    ////
    ////        public StationaryVarianceDecompositionFactory() {
    ////            super(TdArimaDocument.class, DECOMPOSITION_VAR, DIAGSEXTRACTOR.andThen(
    ////                    (StsDiagnostics diags) -> {
    ////                        StationaryVarianceDecomposition decomp = diags.getVarianceDecomposition();
    ////                        if (decomp == null) {
    ////                            return null;
    ////                        }
    ////                        return new HtmlStationaryVarianceDecomposition(decomp);
    ////                    }),
    ////                    new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 4910;
    ////        }
    ////    }
    ////
    ////// <editor-fold defaultstate="collapsed" desc="DIAGNOSTICS">
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5000)
    ////    public static class DiagnosticsSummaryFactory extends ProcDocumentItemFactory<TdArimaDocument,
    //// HtmlElement>
    //// {
    ////
    ////        public DiagnosticsSummaryFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_SUMMARY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null) {
    ////                    return null;
    ////                }
    ////                SaProcessingFactory factory = SaManager.factoryFor(doc.getSpecification());
    ////                List<ProcDiagnostic> diags = new ArrayList<>();
    ////                factory.fillDiagnostics(diags, rslt);
    ////                return new HtmlDiagnosticsSummary(diags);
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5000;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5010)
    ////    public static class OriginalSeasonalityFactory extends ProcDocumentItemFactory<TdArimaDocument,
    //// HtmlElement>
    //// {
    ////
    ////        public OriginalSeasonalityFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_OSEASONALITY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null) {
    ////                    return null;
    ////                }
    ////                TsData s;
    ////                if (rslt.getPreprocessing() != null) {
    ////                    s = rslt.getPreprocessing().transformedSeries();
    ////                } else {
    ////                    s = rslt.getDecomposition().getSeries();
    ////                }
    ////                if (s == null) {
    ////                    return null;
    ////                }
    ////                return new HtmlElements(new HtmlHeader(1, "Original [transformed] series", true),
    ////                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(),
    //// s.getAnnualFrequency(), 1, true, true), false));
    ////
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5010;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5020)
    ////    public static class LinSeasonalityFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement>
    //// {
    ////
    ////        public LinSeasonalityFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_LSEASONALITY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null || rslt.getPreprocessing() == null) {
    ////                    return null;
    ////                }
    ////                TsData s = rslt.getPreprocessing().linearizedSeries();
    ////                if (s == null) {
    ////                    return null;
    ////                }
    ////                return new HtmlElements(new HtmlHeader(1, "Linearized series", true),
    ////                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(),
    //// s.getAnnualFrequency(), 1, true, true), false));
    ////
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5020;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5030)
    ////    public static class ResSeasonalityFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement>
    //// {
    ////
    ////        public ResSeasonalityFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_RSEASONALITY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null || rslt.getPreprocessing() == null) {
    ////                    return null;
    ////                }
    ////                TsData s = rslt.getPreprocessing().fullResiduals();
    ////                if (s == null) {
    ////                    return null;
    ////                }
    ////                return new HtmlElements(new HtmlHeader(1, "Full residuals", true),
    ////                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(),
    //// s.getAnnualFrequency(), 0, false, true), true));
    ////
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5030;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5040)
    ////    public static class SaSeasonalityFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement> {
    ////
    ////        public SaSeasonalityFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_SASEASONALITY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null) {
    ////                    return null;
    ////                }
    ////                TsData s = rslt.getDecomposition().getSa();
    ////                if (s == null) {
    ////                    return null;
    ////                }
    ////                if (rslt.getDecomposition().isMultiplicative()) {
    ////                    s = s.log();
    ////                }
    ////                return new HtmlElements(new HtmlHeader(1, "[Linearized] seasonally adjusted series", true),
    ////                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(),
    //// s.getAnnualFrequency(), 1, true, true), true));
    ////
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5030;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5050)
    ////    public static class IrrSeasonalityFactory extends ProcDocumentItemFactory<TdArimaDocument, HtmlElement>
    //// {
    ////
    ////        public IrrSeasonalityFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_ISEASONALITY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null) {
    ////                    return null;
    ////                }
    ////                TsData s = rslt.getDecomposition().getIrregular();
    ////                if (s == null) {
    ////                    return null;
    ////                }
    ////                if (rslt.getDecomposition().isMultiplicative()) {
    ////                    s = s.log();
    ////                }
    ////                return new HtmlElements(new HtmlHeader(1, "[Linearized] irregular component", true),
    ////                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(),
    //// s.getAnnualFrequency(), 0, false, true), true));
    ////
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5050;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5060)
    ////    public static class LastResSeasonalityFactory extends ProcDocumentItemFactory<TdArimaDocument,
    //// HtmlElement>
    //// {
    ////
    ////        public LastResSeasonalityFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_LASTRSEASONALITY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null || rslt.getPreprocessing() == null) {
    ////                    return null;
    ////                }
    ////                TsData s = rslt.getPreprocessing().fullResiduals();
    ////                if (s == null) {
    ////                    return null;
    ////                }
    ////                StringBuilder header = new StringBuilder().append("Full residuals");
    ////                int ny = DemetraSaUI.get().getSeasonalityLength();
    ////                if (ny > 0) {
    ////                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
    ////                    header.append(" (last ").append(ny).append(" years)");
    ////                }
    ////                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
    ////                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(),
    //// s.getAnnualFrequency(), 0, false, true), true));
    ////
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5060;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5070)
    ////    public static class LastSaSeasonalityFactory extends ProcDocumentItemFactory<TdArimaDocument,
    //// HtmlElement> {
    ////
    ////        public LastSaSeasonalityFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_LASTSASEASONALITY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null) {
    ////                    return null;
    ////                }
    ////                TsData s = rslt.getDecomposition().getSa();
    ////                if (s == null) {
    ////                    return null;
    ////                }
    ////                StringBuilder header = new StringBuilder().append("[Linearized] seasonally adjusted
    //// series");
    ////                int ny = DemetraSaUI.get().getSeasonalityLength();
    ////                if (ny > 0) {
    ////                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny - 1), 0);
    ////                    header.append(" (last ").append(ny).append(" years)");
    ////                }
    ////                if (rslt.getDecomposition().isMultiplicative()) {
    ////                    s = s.log();
    ////                }
    ////                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
    ////                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(),
    //// s.getAnnualFrequency(), 1, true, true), true));
    ////
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5070;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5080)
    ////    public static class LastIrrSeasonalityFactory extends ProcDocumentItemFactory<TdArimaDocument,
    //// HtmlElement>
    //// {
    ////
    ////        public LastIrrSeasonalityFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_LASTISEASONALITY, (TdArimaDocument doc) -> {
    ////                TdResults rslt = doc.getResult();
    ////                if (rslt == null) {
    ////                    return null;
    ////                }
    ////                TsData s = rslt.getDecomposition().getIrregular();
    ////                if (s == null) {
    ////                    return null;
    ////                }
    ////                StringBuilder header = new StringBuilder().append("[Linearized] irregular component");
    ////                int ny = DemetraSaUI.get().getSeasonalityLength();
    ////                if (ny > 0) {
    ////                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
    ////                    header.append(" (last ").append(ny).append(" years)");
    ////                }
    ////                if (rslt.getDecomposition().isMultiplicative()) {
    ////                    s = s.log();
    ////                }
    ////                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
    ////                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(),
    //// s.getAnnualFrequency(), 0, false, true), true));
    ////
    ////            }, new HtmlItemUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5080;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5310)
    ////    public static class ModelResSpectrum extends ProcDocumentItemFactory<TdArimaDocument,
    //// SpectrumUI.Information> {
    ////
    ////        public ModelResSpectrum() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_RES,
    ////                    RESEXTRACTOR.andThen(
    ////                            res
    ////                            -> res == null ? null
    ////                                    : SpectrumUI.Information.builder()
    ////                                            .series(res)
    ////                                            .differencingOrder(0)
    ////                                            .log(false)
    ////                                            .mean(true)
    ////                                            .whiteNoise(true)
    ////                                            .build()),
    ////                    new SpectrumUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5310;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5320)
    ////    public static class DiagnosticsSpectrumIFactory extends ProcDocumentItemFactory<TdArimaDocument,
    //// SpectrumUI.Information> {
    ////
    ////        public DiagnosticsSpectrumIFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_I,
    ////                    DECOMPOSITIONEXTRACTOR.andThen(
    ////                            (StlResults stl) -> {
    ////                                if (stl == null) {
    ////                                    return null;
    ////                                }
    ////                                TsData s = stl.getIrregular();
    ////                               return s == null ? null
    ////                                        : SpectrumUI.Information.builder()
    ////                                                .series(s)
    ////                                                .differencingOrder(0)
    ////                                                .log(stl.isMultiplicative())
    ////                                                .mean(true)
    ////                                                .whiteNoise(false)
    ////                                                .build();
    ////                            }),
    ////                    new SpectrumUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5320;
    ////        }
    ////    }
    ////
    ////    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5330)
    ////    public static class DiagnosticsSpectrumSaFactory extends ProcDocumentItemFactory<TdArimaDocument,
    //// SpectrumUI.Information> {
    ////
    ////        public DiagnosticsSpectrumSaFactory() {
    ////            super(TdArimaDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_SA,
    ////                    DECOMPOSITIONEXTRACTOR.andThen(
    ////                            (StlResults stl) -> {
    ////                                if (stl == null) {
    ////                                    return null;
    ////                                }
    ////                                TsData s = stl.getSa();
    ////                                return s == null ? null
    ////                                        : SpectrumUI.Information.builder()
    ////                                                .series(s)
    ////                                                .differencingOrder(1)
    ////                                                .differencingLag(1)
    ////                                                .log(stl.isMultiplicative())
    ////                                                .mean(true)
    ////                                                .whiteNoise(false)
    ////                                                .build();
    ////                            }),
    ////                    new SpectrumUI());
    ////        }
    ////
    ////        @Override
    ////        public int getPosition() {
    ////            return 5330;
    ////        }
    ////    }
    ////
    ////// </editor-fold>
}
