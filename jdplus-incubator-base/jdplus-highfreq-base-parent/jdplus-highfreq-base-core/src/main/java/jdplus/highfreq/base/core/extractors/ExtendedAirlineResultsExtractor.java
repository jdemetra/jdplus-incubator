/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.base.core.extractors;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.highfreq.base.api.ExtendedAirlineDictionaries;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import java.util.Optional;
import jdplus.highfreq.base.core.extendedairline.decomposition.ExtendedAirlineDecomposition;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineResults;
import jdplus.highfreq.base.core.regarima.HighFreqRegArimaModel;
import static jdplus.toolkit.base.core.regarima.extractors.RegSarimaModelExtractors.NBCAST;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class ExtendedAirlineResultsExtractor extends InformationMapping<ExtendedAirlineResults> {

    public static final String FINAL = "";

    private ExtendedAirlineDecomposition.Step step(ExtendedAirlineResults rslt, int period) {
        Optional<ExtendedAirlineDecomposition.Step> any = rslt.getDecomposition().getSteps().stream().filter(s -> period == (int) s.getPeriod()).findAny();
        return any.orElse(null);
    }

    private String cmpItem(String key) {
        return Dictionary.concatenate(SaDictionaries.DECOMPOSITION, key);
    }

    public ExtendedAirlineResultsExtractor() {
        set(SaDictionaries.MODE, DecompositionMode.class, source -> source.getFinals().getMode());

        set(RegressionDictionaries.Y, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(SaDictionaries.T, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.SA, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionaries.S, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.I, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(RegressionDictionaries.Y_F, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        set(SaDictionaries.T+SaDictionaries.FORECAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(SaDictionaries.SA+SaDictionaries.FORECAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(SaDictionaries.S+SaDictionaries.FORECAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(SaDictionaries.I+SaDictionaries.FORECAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(RegressionDictionaries.Y_B, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        set(SaDictionaries.T+SaDictionaries.BACKCAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(SaDictionaries.SA+SaDictionaries.BACKCAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(SaDictionaries.S+SaDictionaries.BACKCAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(SaDictionaries.I+SaDictionaries.BACKCAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(cmpItem(SaDictionaries.T_CMP), TsData.class, source
                -> source.getComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(cmpItem(SaDictionaries.SA_CMP), TsData.class, source
                -> source.getComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(cmpItem(SaDictionaries.S_CMP), TsData.class, source
                -> source.getComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(cmpItem(SaDictionaries.I_CMP), TsData.class, source
                -> source.getComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(cmpItem(ExtendedAirlineDictionaries.T_CMP_F), TsData.class, source
                -> source.getComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(cmpItem(SaDictionaries.SA_CMP), TsData.class, source
                -> source.getComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(cmpItem(SaDictionaries.S_CMP), TsData.class, source
                -> source.getComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(cmpItem(SaDictionaries.I_CMP), TsData.class, source
                -> source.getComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(cmpItem(ExtendedAirlineDictionaries.SW_CMP), TsData.class, source -> {
            DoubleSeq data = source.getDecomposition().getFinalComponent(ExtendedAirlineDictionaries.SW_CMP);
            if (data.isEmpty()) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount(), nf = source.getDecomposition().getForecastsCount();
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod(), data.drop(nb, nf));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SW_CMP_B), TsData.class, source -> {
            int nb = source.getDecomposition().getBackcastsCount();
            if (nb == 0) {
                return null;
            }
            DoubleSeq data = source.getDecomposition().getFinalComponent(ExtendedAirlineDictionaries.SW_CMP);
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod().plus(-nb), data.range(0, nb));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SW_CMP_F), TsData.class, source -> {
            int nf = source.getDecomposition().getForecastsCount();
            if (nf == 0) {
                return null;
            }
            DoubleSeq data = source.getDecomposition().getFinalComponent(ExtendedAirlineDictionaries.SW_CMP);
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getEndPeriod(), data.range(data.length() - nf, data.length()));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_CMP), TsData.class, source -> {
            DoubleSeq data = source.getDecomposition().getFinalComponent(ExtendedAirlineDictionaries.SY_CMP);
            if (data.isEmpty()) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount(), nf = source.getDecomposition().getForecastsCount();
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod(), data.drop(nb, nf));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_CMP_B), TsData.class, source -> {
            int nb = source.getDecomposition().getBackcastsCount();
            if (nb == 0) {
                return null;
            }
            DoubleSeq data = source.getDecomposition().getFinalComponent(ExtendedAirlineDictionaries.SY_CMP);
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod().plus(-nb), data.range(0, nb));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_CMP_F), TsData.class, source -> {
            int nf = source.getDecomposition().getForecastsCount();
            if (nf == 0) {
                return null;
            }
            DoubleSeq data = source.getDecomposition().getFinalComponent(ExtendedAirlineDictionaries.SY_CMP);
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getEndPeriod(), data.range(data.length() - nf, data.length()));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SW_LIN), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 7);
            if (step == null) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getData();
            if (data.isEmpty()) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount(), nf = source.getDecomposition().getForecastsCount();
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod(), data.drop(nb, nf));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SW_LIN_B), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 7);
            if (step == null) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount();
            if (nb == 0) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getData();
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod().plus(-nb), data.range(0, nb));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SW_LIN_F), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 7);
            if (step == null) {
                return null;
            }
            int nf = source.getDecomposition().getForecastsCount();
            if (nf == 0) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getData();
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getEndPeriod(), data.range(data.length() - nf, data.length()));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SW_LIN_E), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 7);
            if (step == null) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getStde();
            if (data.isEmpty()) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount(), nf = source.getDecomposition().getForecastsCount();
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod(), data.drop(nb, nf));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SW_LIN_EB), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 7);
            if (step == null) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount();
            if (nb == 0) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getStde();
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod().plus(-nb), data.range(0, nb));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SW_LIN_EF), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 7);
            if (step == null) {
                return null;
            }
            int nf = source.getDecomposition().getForecastsCount();
            if (nf == 0) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getStde();
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getEndPeriod(), data.range(data.length() - nf, data.length()));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_LIN), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 365);
            if (step == null) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getData();
            if (data.isEmpty()) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount(), nf = source.getDecomposition().getForecastsCount();
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod(), data.drop(nb, nf));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_LIN_B), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 365);
            if (step == null) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount();
            if (nb == 0) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getData();
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod().plus(-nb), data.range(0, nb));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_LIN_F), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 365);
            if (step == null) {
                return null;
            }
            int nf = source.getDecomposition().getForecastsCount();
            if (nf == 0) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getData();
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getEndPeriod(), data.range(data.length() - nf, data.length()));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_LIN_E), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 365);
            if (step == null) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getStde();
            if (data.isEmpty()) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount(), nf = source.getDecomposition().getForecastsCount();
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod(), data.drop(nb, nf));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_LIN_EB), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 365);
            if (step == null) {
                return null;
            }
            int nb = source.getDecomposition().getBackcastsCount();
            if (nb == 0) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getStde();
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getStartPeriod().plus(-nb), data.range(0, nb));
        });
        set(cmpItem(ExtendedAirlineDictionaries.SY_LIN_EF), TsData.class, source -> {
            ExtendedAirlineDecomposition.Step step = step(source, 365);
            if (step == null) {
                return null;
            }
            int nf = source.getDecomposition().getForecastsCount();
            if (nf == 0) {
                return null;
            }
            DoubleSeq data = step.getComponent(1).getStde();
            if (data.isEmpty()) {
                return null;
            }
            TsDomain dom = source.getPreprocessing().getDescription().getDomain();
            return TsData.of(dom.getEndPeriod(), data.range(data.length() - nf, data.length()));
        });

        delegate(null, HighFreqRegArimaModel.class, source -> source.getPreprocessing());

        set(RegressionDictionaries.CAL, TsData.class, source -> source.getPreprocessing().getCalendarEffect(null));
        set(RegressionDictionaries.CAL + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getPreprocessing().getCalendarEffect(source.getPreprocessing().forecastDomain(source.getDecomposition().getForecastsCount())));
        setArray(RegressionDictionaries.CAL + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> source.getPreprocessing().getCalendarEffect(source.getPreprocessing().backcastDomain(source.getDecomposition().getBackcastsCount())));
    }

    @Override
    public Class<ExtendedAirlineResults> getSourceClass() {
        return ExtendedAirlineResults.class;
    }
}
