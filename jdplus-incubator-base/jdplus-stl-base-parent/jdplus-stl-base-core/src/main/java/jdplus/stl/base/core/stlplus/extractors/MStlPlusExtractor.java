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
package jdplus.stl.base.core.stlplus.extractors;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.ModellingDictionary;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.stl.base.api.StlDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import java.util.Map;
import jdplus.highfreq.base.core.regarima.HighFreqRegArimaModel;
import jdplus.stl.base.core.mstlplus.MStlPlusDiagnostics;
import jdplus.stl.base.core.mstlplus.MStlPlusResults;
import jdplus.stl.base.core.MStlResults;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class MStlPlusExtractor extends InformationMapping<MStlPlusResults> {

    public static final String FINAL = "";

    public MStlPlusExtractor() {
        set(SaDictionaries.MODE, DecompositionMode.class, source -> source.getFinals().getMode());

        set(ModellingDictionary.Y, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));

        set(SaDictionaries.T, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));

        set(SaDictionaries.SA, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));

        set(SaDictionaries.S, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));

        set(SaDictionaries.I, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));

        set(StlDictionaries.SW, TsData.class, source
                -> {
            DoubleSeq s = source.getDecomposition().getSeasons().get(7);
            if (s == null) {
                return null;
            } else {
                TsData series = source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value);
                return TsData.of(series.getStart(), s);
            }
        });

        set(StlDictionaries.SY, TsData.class, source
                -> {
            DoubleSeq s = source.getDecomposition().getSeasons().get(365);
            if (s == null) {
                return null;
            } else {
                TsData series = source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value);
                return TsData.of(series.getStart(), s);
            }
        });

        set(StlDictionaries.S, TsData.class, source
                -> {
            Map<Integer, DoubleSeq> seasons = source.getDecomposition().getSeasons();
            if (seasons.size() == 1) {
                Map.Entry<Integer, DoubleSeq> entry = seasons.entrySet().iterator().next();
                TsData series = source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value);
                return TsData.of(series.getStart(), entry.getValue());
            } else {
                return null;
            }
        });

        delegate(SaDictionaries.DECOMPOSITION, MStlResults.class, source -> source.getDecomposition());

        delegate(null, HighFreqRegArimaModel.class, source -> source.getPreprocessing());

        delegate(null, MStlPlusDiagnostics.class, source -> source.getDiagnostics());

    }

    @Override
    public Class<MStlPlusResults> getSourceClass() {
        return MStlPlusResults.class;
    }
}
