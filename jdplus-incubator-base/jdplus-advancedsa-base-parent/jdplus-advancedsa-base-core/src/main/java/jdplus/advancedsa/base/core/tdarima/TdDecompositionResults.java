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
package jdplus.advancedsa.base.core.tdarima;

import java.util.List;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
@lombok.Builder(builderClassName = "Builder")
@lombok.Value
public class TdDecompositionResults implements GenericExplorable {

    private UcarimaModel initialModel;
    private SeriesDecomposition stochasticSeries, components;

    @lombok.Singular
    List<UcarimaModel> models;

    private SeriesDecomposition tdStochasticSeries, tdComponents;

    public boolean isTimeVarying() {
        return !models.isEmpty();
    }
}
