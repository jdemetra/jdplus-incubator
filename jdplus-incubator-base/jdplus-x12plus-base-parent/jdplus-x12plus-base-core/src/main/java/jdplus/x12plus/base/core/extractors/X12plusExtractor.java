/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x12plus.base.core.extractors;

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.toolkit.base.api.modelling.ModellingDictionary;
import jdplus.x12plus.base.api.X12plusDictionaries;
import jdplus.x12plus.base.core.X11plusResults;
import jdplus.x12plus.base.core.x12.X12plusResults;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X12plusExtractor extends InformationMapping<X12plusResults> {

    private String decompositionItem(String key) {
        return Dictionary.concatenate(SaDictionaries.DECOMPOSITION, key);
    }

    private String preadjustItem(String key) {
        return Dictionary.concatenate(X12plusDictionaries.PREADJUST, key);
    }

    private String finalItem(String key) {
        return Dictionary.concatenate(X12plusDictionaries.FINAL, key);
    }

    public X12plusExtractor() {
//         MAPPING.set(FINAL + ModellingDictionary.Y, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));
//
//        MAPPING.set(FINAL + SaDictionaries.T, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + SaDictionaries.T + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));
//

        set(preadjustItem(X12plusDictionaries.A1), TsData.class, source
                -> source.getPreadjustment().getA1());
        set(preadjustItem(X12plusDictionaries.A1A), TsData.class, source
                -> source.getPreadjustment().getA1a());
        set(preadjustItem(X12plusDictionaries.A1B), TsData.class, source
                -> source.getPreadjustment().getA1b());
        set(preadjustItem(X12plusDictionaries.A6), TsData.class, source
                -> source.getPreadjustment().getA6());
        set(preadjustItem(X12plusDictionaries.A7), TsData.class, source
                -> source.getPreadjustment().getA7());
        set(preadjustItem(X12plusDictionaries.A8), TsData.class, source
                -> source.getPreadjustment().getA8());
        set(preadjustItem(X12plusDictionaries.A8I), TsData.class, source
                -> source.getPreadjustment().getA8i());
        set(preadjustItem(X12plusDictionaries.A8S), TsData.class, source
                -> source.getPreadjustment().getA8s());
        set(preadjustItem(X12plusDictionaries.A8T), TsData.class, source
                -> source.getPreadjustment().getA8t());
        set(preadjustItem(X12plusDictionaries.A9), TsData.class, source
                -> source.getPreadjustment().getA9());
        set(preadjustItem(X12plusDictionaries.A9SA), TsData.class, source
                -> source.getPreadjustment().getA9sa());
        set(preadjustItem(X12plusDictionaries.A9U), TsData.class, source
                -> source.getPreadjustment().getA9u());
        set(preadjustItem(X12plusDictionaries.A9SER), TsData.class, source
                -> source.getPreadjustment().getA9ser());

        set(SaDictionaries.S, TsData.class, source
                -> source.getFinals().getD16());
        set(SaDictionaries.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getD16a());
        set(SaDictionaries.S + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getD16b());
        set(SaDictionaries.SA, TsData.class, source
                -> source.getFinals().getD11final());
        set(SaDictionaries.SA + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getD11a());
        set(SaDictionaries.SA + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getD11b());
        set(SaDictionaries.T, TsData.class, source
                -> source.getFinals().getD12final());
        set(SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getD12a());
        set(SaDictionaries.T + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getD12b());
        set(SaDictionaries.I, TsData.class, source
                -> source.getFinals().getD13final());

        set(decompositionItem(SaDictionaries.Y_CMP), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getB1(), source.getDecomposition().getActualDomain()));
        set(decompositionItem(SaDictionaries.Y_CMP_F), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getB1(), source.getDecomposition().getForecastDomain()));
        set(decompositionItem(SaDictionaries.Y_CMP_B), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getB1(), source.getDecomposition().getBackcastDomain()));
        set(decompositionItem(SaDictionaries.S_CMP), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD10(), source.getDecomposition().getActualDomain()));
        set(decompositionItem(SaDictionaries.S_CMP + SeriesInfo.F_SUFFIX), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD10(), source.getDecomposition().getForecastDomain()));
        set(decompositionItem(SaDictionaries.S_CMP + SeriesInfo.B_SUFFIX), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD10(), source.getDecomposition().getBackcastDomain()));
        set(decompositionItem(SaDictionaries.SA_CMP), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD11(), source.getDecomposition().getActualDomain()));
        set(decompositionItem(SaDictionaries.SA_CMP + SeriesInfo.F_SUFFIX), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD11(), source.getDecomposition().getForecastDomain()));
        set(decompositionItem(SaDictionaries.SA_CMP + SeriesInfo.B_SUFFIX), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD11(), source.getDecomposition().getBackcastDomain()));
        set(decompositionItem(SaDictionaries.T_CMP), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD12(), source.getDecomposition().getActualDomain()));
        set(decompositionItem(SaDictionaries.T_CMP + SeriesInfo.F_SUFFIX), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD12(), source.getDecomposition().getForecastDomain()));
        set(decompositionItem(SaDictionaries.T_CMP + SeriesInfo.B_SUFFIX), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD12(), source.getDecomposition().getBackcastDomain()));
        set(decompositionItem(SaDictionaries.I_CMP), TsData.class, source
                -> source.getDecomposition().getD13());
        set(decompositionItem(SaDictionaries.SI_CMP), TsData.class, source
                -> TsData.fitToDomain(source.getDecomposition().getD8(), source.getDecomposition().getActualDomain()));

        set(SaDictionaries.Y, TsData.class, source
                -> source.getPreadjustment().getA1());
        set(SaDictionaries.Y + SaDictionaries.BACKCAST, TsData.class, source
                -> source.getPreadjustment().getA1b());
        set(SaDictionaries.Y + SaDictionaries.FORECAST, TsData.class, source
                -> source.getPreadjustment().getA1a());

        set(ModellingDictionary.CAL, TsData.class, source
                -> source.getPreprocessing() == null ? null
                : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getActualDomain()));
        set(ModellingDictionary.CAL + SaDictionaries.BACKCAST, TsData.class, source
                -> source.getPreprocessing() == null ? null
                : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getBackcastDomain()));
        set(ModellingDictionary.CAL + SaDictionaries.FORECAST, TsData.class, source
                -> source.getPreprocessing() == null ? null
                : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getForecastDomain()));

        set(finalItem(X12plusDictionaries.D11), TsData.class, source
                -> source.getFinals().getD11final());
        set(finalItem(X12plusDictionaries.D12), TsData.class, source
                -> source.getFinals().getD12final());
        set(finalItem(X12plusDictionaries.D13), TsData.class, source
                -> source.getFinals().getD13final());
        set(finalItem(X12plusDictionaries.D16), TsData.class, source
                -> source.getFinals().getD16());
        set(finalItem(X12plusDictionaries.D18), TsData.class, source
                -> source.getFinals().getD18());
        set(finalItem(X12plusDictionaries.D11A), TsData.class, source
                -> source.getFinals().getD11a());
        set(finalItem(X12plusDictionaries.D12A), TsData.class, source
                -> source.getFinals().getD12a());
        set(finalItem(X12plusDictionaries.D16A), TsData.class, source
                -> source.getFinals().getD16a());
        set(finalItem(X12plusDictionaries.D18A), TsData.class, source
                -> source.getFinals().getD18a());
        set(finalItem(X12plusDictionaries.D11B), TsData.class, source
                -> source.getFinals().getD11b());
        set(finalItem(X12plusDictionaries.D12B), TsData.class, source
                -> source.getFinals().getD12b());
        set(finalItem(X12plusDictionaries.D16B), TsData.class, source
                -> source.getFinals().getD16b());
        set(finalItem(X12plusDictionaries.D18B), TsData.class, source
                -> source.getFinals().getD18b());

        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());

        delegate(SaDictionaries.DECOMPOSITION, X11plusResults.class, source -> source.getDecomposition());

//        delegate(null, X13Diagnostics.class, source -> source.getDiagnostics());
        delegate(SaDictionaries.BENCHMARKING, SaBenchmarkingResults.class, source -> source.getBenchmarking());

    }

    @Override
    public Class getSourceClass() {
        return X12plusResults.class;
    }
}
