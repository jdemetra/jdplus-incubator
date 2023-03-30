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

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.ModellingDictionary;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.stl.base.core.StlResults;
import jdplus.stl.base.core.stlplus.StlPlusDiagnostics;
import jdplus.stl.base.core.stlplus.StlPlusResults;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class StlPlusExtractor extends InformationMapping<StlPlusResults> {

    public static final String FINAL = "";

    public StlPlusExtractor() {
        set(SaDictionaries.MODE, DecompositionMode.class, source -> source.getFinals().getMode());

        set(SaDictionaries.SEASONAL, Integer.class, source -> {
            TsData s = source.getDecomposition().seasonal();
            if (s == null) {
                return 0;
            } else {
                return s.getValues().allMatch(x -> x == 0) ? 0 : 1;
            }
        }
        );
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

        delegate(SaDictionaries.DECOMPOSITION, StlResults.class, source -> source.getDecomposition());

        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());

        delegate(null, StlPlusDiagnostics.class, source -> source.getDiagnostics());

        delegate(SaDictionaries.BENCHMARKING, SaBenchmarkingResults.class, source -> source.getBenchmarking());
    }

    @Override
    public Class<StlPlusResults> getSourceClass() {
        return StlPlusResults.class;
    }
}
