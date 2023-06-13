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
package jdplus.highfreq.base.core.extendedairline.decomposition;

import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineKernelTest;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineMapping;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineResults;
import tck.demetra.data.MatrixSerializer;
import jdplus.highfreq.base.api.DecompositionSpec;
import jdplus.highfreq.base.api.ExtendedAirlineDecompositionSpec;
import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import jdplus.highfreq.base.api.ExtendedAirlineSpec;
import jdplus.toolkit.base.api.modelling.highfreq.HolidaysSpec;
import jdplus.toolkit.base.api.modelling.highfreq.OutlierSpec;
import jdplus.toolkit.base.api.modelling.highfreq.RegressionSpec;
import jdplus.toolkit.base.api.modelling.highfreq.TransformSpec;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.sa.base.api.ComponentType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.Calendar;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import jdplus.toolkit.base.api.timeseries.calendars.HolidaysOption;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDecompositionKernelTest {
    
    final static TsData EDF;

    static {
        TsData y;
        try {
            InputStream stream = ExtendedAirlineMapping.class.getResourceAsStream("edf.txt");
            Matrix edf = MatrixSerializer.read(stream);
            y = TsData.of(TsPeriod.daily(1996, 1, 1), edf.column(0));
        } catch (IOException ex) {
            y = null;
        }
        EDF = y;
    }
    
    public ExtendedAirlineDecompositionKernelTest() {
    }

    public static void main(String[] args){
        testEDF();
    }

    public static void testEDF() {
        Holiday[] france = ExtendedAirlineKernelTest.france();
        ModellingContext context=new ModellingContext();
        context.getCalendars().set("FR", new Calendar(france));
       // build the psec
        ExtendedAirlineModellingSpec spec=ExtendedAirlineModellingSpec.builder()
                .transform(TransformSpec.builder()
                        .function(TransformationType.Log)
                        .build())
                .stochastic(ExtendedAirlineSpec.DEFAULT_WD)
                .outlier(OutlierSpec.builder()
                        .criticalValue(6)
                        .ao(true)
                        .build())
                .regression(RegressionSpec.builder()
                        .holidays(HolidaysSpec.builder()
                                        .holidays("FR")
                                        .holidaysOption(HolidaysOption.Skip)
                                        .single(false)
                                        .build())
                        .build())
                .build();
        
        DecompositionSpec dspec = DecompositionSpec.builder()
                .periodicities(new double[]{7, 365.25})
                .build();
        ExtendedAirlineDecompositionSpec allspec = ExtendedAirlineDecompositionSpec.builder()
                .preprocessing(spec)
                .decomposition(dspec)
                .build();
        ExtendedAirlineDecompositionKernel kernel=new ExtendedAirlineDecompositionKernel(allspec, context);
        ExtendedAirlineResults rslts = kernel.process(EDF, null);
        List<TsData> main=new ArrayList<>();
        main.add(rslts.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
        main.add(rslts.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        main.add(rslts.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        main.add(rslts.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        main.add(rslts.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        System.out.println(TsDataTable.of(main));
       Map<String, Class> dictionary = rslts.getDictionary();
        dictionary.keySet().forEach(v->System.out.println(v));
    }
    
}
