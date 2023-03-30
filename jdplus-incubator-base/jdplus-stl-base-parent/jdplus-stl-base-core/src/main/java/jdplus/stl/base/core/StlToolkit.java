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

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.stl.base.api.IStlSpec;
import jdplus.stl.base.api.MStlSpec;
import jdplus.stl.base.api.SeasonalSpec;
import jdplus.stl.base.api.StlSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class StlToolkit {

    public StlResults process(TsData data, StlSpec spec) {
        RawStlKernel stl = new RawStlKernel(spec);
        RawStlResults decomp = stl.process(data.getValues());

        TsPeriod start = data.getStart();
        TsData trend = TsData.of(start, decomp.getTrend()),
                irr = TsData.of(start, decomp.getIrregular()),
                fit = TsData.of(start, decomp.getFit()),
                weights = TsData.of(start, decomp.getWeights()),
                sa = TsData.of(start, decomp.getSa()),
                seasonal = TsData.of(start, decomp.getSeasonal());

        return StlResults.builder()
                .multiplicative(spec.isMultiplicative())
                .series(data)
                .trend(trend)
                .irregular(irr)
                .sa(sa)
                .fit(fit)
                .weights(weights)
                .seasonal(spec.getSeasonalSpec().getPeriod(), seasonal)
                .build();
    }

    public StlResults process(TsData data, MStlSpec spec) {
        // We should add pre-processing
        MStlKernel stl = MStlKernel.of(spec);
        MStlResults decomp = stl.process(data.getValues());
        TsPeriod start = data.getStart();
        TsData trend = TsData.of(start, decomp.getTrend()),
                irr = TsData.of(start, decomp.getIrregular()),
                fit = TsData.of(start, decomp.getFit()),
                weights = TsData.of(start, decomp.getWeights()),
                sa = TsData.of(start, decomp.getSa());

        StlResults.Builder builder = StlResults.builder()
                .series(data)
                .trend(trend)
                .irregular(irr)
                .sa(sa)
                .fit(fit)
                .weights(weights);
        
        Iterator<DoubleSeq> seasons = decomp.getSeasons().values().iterator();
        List<SeasonalSpec> seasonalSpecs = spec.getSeasonalSpecs();
        for (SeasonalSpec sspec : seasonalSpecs){
            builder.seasonal(sspec.getPeriod(), TsData.of(start, seasons.next()));
        }
         return builder.build();
    }
    
    public StlResults process(TsData data, IStlSpec spec) {
        // We should add pre-processing
        
        MStlResults decomp = IStlKernel.process(data.getValues(), spec);
        TsPeriod start = data.getStart();
        TsData trend = TsData.of(start, decomp.getTrend()),
                irr = TsData.of(start, decomp.getIrregular()),
                fit = TsData.of(start, decomp.getFit()),
                weights = TsData.of(start, decomp.getWeights()),
                sa = TsData.of(start, decomp.getSa());

        StlResults.Builder builder = StlResults.builder()
                .series(data)
                .trend(trend)
                .irregular(irr)
                .sa(sa)
                .fit(fit)
                .weights(weights);
        
        Iterator<DoubleSeq> seasons = decomp.getSeasons().values().iterator();
        List<IStlSpec.PeriodSpec> perodSpec = spec.getPeriodSpecs();
        for (IStlSpec.PeriodSpec sspec : perodSpec){
            builder.seasonal(sspec.getSeasonalSpec().getPeriod(), TsData.of(start, seasons.next()));
        }
         return builder.build();
    }
    
}
