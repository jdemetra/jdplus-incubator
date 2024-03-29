/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.stl.base.core;

import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class StlResults implements GenericExplorable{
    boolean multiplicative;
    TsData series;
    TsData sa;
    TsData trend;
    @lombok.Singular
    Map<Integer, TsData> seasonals;
    TsData irregular;
    TsData fit;
    TsData weights;
    
    public TsData seasonal(){
        if (seasonals.isEmpty())
            return null;
        TsData all=null;
        for (TsData s :seasonals.values()){
            all=multiplicative ? TsData.multiply(all, s) : TsData.add(all, s);
        }
        return all;            
    }
    
    public SeriesDecomposition asDecomposition(){
        return SeriesDecomposition.builder(multiplicative ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .add(series, ComponentType.Series)
                .add(trend, ComponentType.Trend)
                .add(sa, ComponentType.SeasonallyAdjusted)
                .add(irregular, ComponentType.Irregular)
                .add(seasonal(), ComponentType.Seasonal)
                .build();
    }
}
