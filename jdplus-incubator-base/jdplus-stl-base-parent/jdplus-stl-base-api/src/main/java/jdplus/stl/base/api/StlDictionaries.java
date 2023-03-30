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
package jdplus.stl.base.api;

import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.AtomicDictionary;
import jdplus.toolkit.base.api.dictionaries.ComplexDictionary;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.PrefixedDictionary;
import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class StlDictionaries {
    
    // Decomposition
    public final String STL = SaDictionaries.DECOMPOSITION, WEIGHTS="weights", FIT="fit";

    // finals
    public final String FINAL = "";
    
    public final AtomicDictionary STLDICTIONARY = AtomicDictionary.builder()
            .name("stl")
            .item(AtomicDictionary.Item.builder().name(WEIGHTS).description("weights").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(FIT).description("fit").outputClass(TsData.class).build())
            .build();

    public final Dictionary STLPLUSDICTIONARY=ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, RegArimaDictionaries.REGSARIMA))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.REGEFFECTS))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION_F))
            .dictionary(new PrefixedDictionary(STL, STLDICTIONARY))
//            .dictionary(new PrefixedDictionary(SEATS, SaDictionaries.CMPDECOMPOSITION))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.COMBINEDSEASONALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.GENERICSEASONALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.GENERICTRADINGDAYS))
            .build();
    
    public static final String S = "seas", SY="sy", SW="sw";
}
