/*
 * Copyright 2024 JDemetra+.
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
package jdplus.highfreq.base.core.extractors;

import jdplus.highfreq.base.api.ExtendedAirline;
import jdplus.highfreq.base.core.extendedairline.LightExtendedAirlineEstimation;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(InformationExtractor.class)
public class ExtendedAirlineExtractor extends InformationMapping<ExtendedAirline> {

    public static String PERIODS = "periods", DIFF = "differencing", AR = "ar",
            PHI = "phi", THETA = "theta", BTHETA = "btheta", PARAMETERS = "parameters";

    @Override
    public Class<ExtendedAirline> getSourceClass() {
        return ExtendedAirline.class;
    }

    public ExtendedAirlineExtractor() {
        set(PARAMETERS, double[].class, source -> source.getP().toArray());
        set(PERIODS, double[].class, source -> source.getPeriodicities());
        set(DIFF, Integer.class, source -> source.getNdifferencing());
        set(AR, Boolean.class, source -> source.isAr());
        set(BTHETA, double[].class, source -> source.getP().drop(1, 0).toArray());
        set(PHI, Double.class, source -> source.isAr() ? source.getP().get(0) : Double.NaN);
        set(THETA, Double.class, source -> source.isAr() ? Double.NaN : source.getP().get(0));
    }
}
