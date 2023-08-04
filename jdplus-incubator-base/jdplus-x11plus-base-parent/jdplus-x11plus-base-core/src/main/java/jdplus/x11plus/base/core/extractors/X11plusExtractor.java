/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.x11plus.base.core.extractors;

import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;
import jdplus.x11plus.base.api.X11plusDictionaries;
import jdplus.x11plus.base.core.X11plusResults;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X11plusExtractor extends InformationMapping<X11plusResults> {

    public X11plusExtractor() {

//        set(SaDictionaries.T_CMP, TsData.class, source -> source.getD12());
//        set(SaDictionaries.SA_CMP, TsData.class, source -> source.getD11());
//        set(SaDictionaries.S_CMP, TsData.class, source -> source.getD10());
////        set(SaDictionaries.S_CMP+ SeriesInfo.F_SUFFIX, TsData.class, source->source.getD10a());
//        set(SaDictionaries.I_CMP, TsData.class, source -> source.getD13());

        set(SaDictionaries.MODE, String.class, source ->source.getMode().name());
        set(X11plusDictionaries.B1, TsData.class, source -> source.getB1());
        set(X11plusDictionaries.B2, TsData.class, source -> source.getB2());
        set(X11plusDictionaries.B3, TsData.class, source -> source.getB3());
        set(X11plusDictionaries.B4, TsData.class, source -> source.getB4());
        set(X11plusDictionaries.B5, TsData.class, source -> source.getB5());
        set(X11plusDictionaries.B6, TsData.class, source -> source.getB6());
        set(X11plusDictionaries.B7, TsData.class, source -> source.getB7());
        set(X11plusDictionaries.B8, TsData.class, source -> source.getB8());
        set(X11plusDictionaries.B9, TsData.class, source -> source.getB9());
        set(X11plusDictionaries.B10, TsData.class, source -> source.getB10());
        set(X11plusDictionaries.B11, TsData.class, source -> source.getB11());
        set(X11plusDictionaries.B13, TsData.class, source -> source.getB13());
        set(X11plusDictionaries.B17, TsData.class, source -> source.getB17());
        set(X11plusDictionaries.B20, TsData.class, source -> source.getB20());
        set(X11plusDictionaries.C1, TsData.class, source -> source.getC1());
        set(X11plusDictionaries.C2, TsData.class, source -> source.getC2());
        set(X11plusDictionaries.C4, TsData.class, source -> source.getC4());
        set(X11plusDictionaries.C5, TsData.class, source -> source.getC5());
        set(X11plusDictionaries.C6, TsData.class, source -> source.getC6());
        set(X11plusDictionaries.C7, TsData.class, source -> source.getC7());
        set(X11plusDictionaries.C9, TsData.class, source -> source.getC9());
        set(X11plusDictionaries.C10, TsData.class, source -> source.getC10());
        set(X11plusDictionaries.C11, TsData.class, source -> source.getC11());
        set(X11plusDictionaries.C13, TsData.class, source -> source.getC13());
        set(X11plusDictionaries.C17, TsData.class, source -> source.getC17());
        set(X11plusDictionaries.C20, TsData.class, source -> source.getC20());
        set(X11plusDictionaries.D1, TsData.class, source -> source.getD1());
        set(X11plusDictionaries.D2, TsData.class, source -> source.getD2());
        set(X11plusDictionaries.D4, TsData.class, source -> source.getD4());
        set(X11plusDictionaries.D5, TsData.class, source -> source.getD5());
        set(X11plusDictionaries.D6, TsData.class, source -> source.getD6());
        set(X11plusDictionaries.D7, TsData.class, source -> source.getD7());
        set(X11plusDictionaries.D8, TsData.class, source -> source.getD8());
        set(X11plusDictionaries.D10, TsData.class, source -> source.getD10());
        set(X11plusDictionaries.D11, TsData.class, source -> source.getD11());
        set(X11plusDictionaries.D12, TsData.class, source -> source.getD12());
        set(X11plusDictionaries.D13, TsData.class, source -> source.getD13());
    }

    @Override
    public Class<X11plusResults> getSourceClass() {
        return X11plusResults.class;
    }

}
