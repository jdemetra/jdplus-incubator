/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sts.base.core.extractors;

import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sts.base.api.Component;
import jdplus.sts.base.core.BsmData;
import jdplus.sts.base.core.StsResults;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class StsResultsExtractor extends InformationMapping<StsResults> {

    public static final String RESIDUALS = "residuals",
            SERIES_LIN = "series", LEVEL = "level", CYCLE = "cycle", SLOPE = "slope", NOISE = "noise", SEASONAL= "seasonal", BSM="bsm";

    public StsResultsExtractor() {
        set(SaDictionaries.Y, TsData.class, source -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(SaDictionaries.T, TsData.class, source -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.S, TsData.class, source -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.I, TsData.class, source -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(SaDictionaries.SA, TsData.class, source -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(LEVEL, TsData.class, source -> source.getSts().getDecomposition().getSeries(Component.Level, false));
        set(SLOPE, TsData.class, source -> source.getSts().getDecomposition().getSeries(Component.Slope, false));
        set(CYCLE, TsData.class, source -> source.getSts().getDecomposition().getSeries(Component.Cycle, false));
        set(NOISE, TsData.class, source -> source.getSts().getDecomposition().getSeries(Component.Noise, false));
        set(SEASONAL, TsData.class, source -> source.getSts().getDecomposition().getSeries(Component.Seasonal, false));
        delegate(BSM, BsmData.class, source->source.getSts().getBsm());
        delegate(SaDictionaries.PREPROCESSING, RegSarimaModel.class, source -> source.getPreprocessing());
    }

    @Override
    public Class getSourceClass() {
        return StsResults.class;
    }
}
