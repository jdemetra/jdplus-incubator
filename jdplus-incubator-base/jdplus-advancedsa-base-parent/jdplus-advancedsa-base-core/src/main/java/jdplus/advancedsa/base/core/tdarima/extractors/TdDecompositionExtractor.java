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
package jdplus.advancedsa.base.core.tdarima.extractors;

import java.util.List;
import jdplus.advancedsa.base.api.tdarima.TdArimaDictionaries;
import jdplus.advancedsa.base.core.tdarima.TdDecompositionResults;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class TdDecompositionExtractor extends InformationMapping<TdDecompositionResults> {

    @Override
    public Class<TdDecompositionResults> getSourceClass() {
        return TdDecompositionResults.class;
    }

    public static final String EVARS =
            Dictionary.concatenate(TdArimaDictionaries.TDSARIMA, TdArimaDictionaries.INNOVATIONS);

    public TdDecompositionExtractor() {
        delegate(
                Dictionary.concatenate(TdArimaDictionaries.SARIMA, TdArimaDictionaries.SACMPS),
                SeriesDecomposition.class,
                source -> source.getComponents());
        delegate(
                Dictionary.concatenate(TdArimaDictionaries.SARIMA, TdArimaDictionaries.SASTOCH),
                SeriesDecomposition.class,
                source -> source.getStochasticSeries());
        delegate(
                Dictionary.concatenate(TdArimaDictionaries.TDSARIMA, TdArimaDictionaries.SACMPS),
                SeriesDecomposition.class,
                source -> source.getTdComponents());
        delegate(
                Dictionary.concatenate(TdArimaDictionaries.TDSARIMA, TdArimaDictionaries.SASTOCH),
                SeriesDecomposition.class,
                source -> source.getTdStochasticSeries());
        set(Dictionary.concatenate(EVARS, SaDictionaries.T), TsData.class, source -> {
            if (!source.isTimeVarying()) {
                return null;
            }
            TsPeriod start = source.getStochasticSeries()
                    .getSeries(ComponentType.Series, ComponentInformation.Value)
                    .getStart();
            return TsData.ofInternal(start, vars(source.getModels(), 0));
        });
        set(Dictionary.concatenate(EVARS, SaDictionaries.S), TsData.class, source -> {
            if (!source.isTimeVarying()) {
                return null;
            }
            TsPeriod start = source.getStochasticSeries()
                    .getSeries(ComponentType.Series, ComponentInformation.Value)
                    .getStart();
            return TsData.ofInternal(start, vars(source.getModels(), 1));
        });
        set(Dictionary.concatenate(EVARS, SaDictionaries.I), TsData.class, source -> {
            if (!source.isTimeVarying()) {
                return null;
            }
            TsPeriod start = source.getStochasticSeries()
                    .getSeries(ComponentType.Series, ComponentInformation.Value)
                    .getStart();
            return TsData.ofInternal(start, vars(source.getModels(), 2));
        });
    }

    private static double[] vars(List<UcarimaModel> models, int cmp) {
        double[] v = new double[models.size()];
        int i = 0;
        for (UcarimaModel m : models) {
            v[i++] = Math.sqrt(m.getComponent(cmp).getInnovationVariance());
        }
        return v;
    }
}
