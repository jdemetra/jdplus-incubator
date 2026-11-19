/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.advancedsa.base.core.extractors;

import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class SeriesDecompositionExtractor extends InformationMapping<SeriesDecomposition> {

    private static final String E = "_e";

    public SeriesDecompositionExtractor() {
        set(SaDictionaries.Y, TsData.class, s -> s.getSeries(ComponentType.Series, ComponentInformation.Value));
        set(
                SaDictionaries.SA,
                TsData.class,
                s -> s.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionaries.T, TsData.class, s -> s.getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.S, TsData.class, s -> s.getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.I, TsData.class, s -> s.getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(
                SaDictionaries.SA + E,
                TsData.class,
                s -> s.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev));
        set(SaDictionaries.T + E, TsData.class, s -> s.getSeries(ComponentType.Trend, ComponentInformation.Stdev));
        set(SaDictionaries.S + E, TsData.class, s -> s.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev));
        set(SaDictionaries.I + E, TsData.class, s -> s.getSeries(ComponentType.Irregular, ComponentInformation.Stdev));
    }

    @Override
    public Class getSourceClass() {
        return SeriesDecomposition.class;
    }
}
