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
import jdplus.advancedsa.base.core.ssf.SsfWeights;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.math.matrices.MatrixStorage;
import jdplus.toolkit.base.core.ssf.arima.SsfUcarima;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
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
    @lombok.experimental.NonFinal
    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile MatrixStorage ucmWeights, tdUcmWeights;

    @lombok.Singular
    List<UcarimaModel> models;

    private SeriesDecomposition tdStochasticSeries, tdComponents;

    public boolean isTimeVarying() {
        return !models.isEmpty();
    }

    public MatrixStorage weights() {
        MatrixStorage w = ucmWeights;
        if (w != null) return w;
        TsData s = stochasticSeries.getSeries(ComponentType.Series, ComponentInformation.Value);
        int n = s == null ? 0 : s.length();
        if (n > 0) {
            synchronized (this) {
                CompositeSsf ssf = SsfUcarima.of(initialModel);
                w = SsfWeights.weights(ssf, n);
                ucmWeights = w;
            }
        }
        return ucmWeights;
    }

    public MatrixStorage tdWeights() {
        MatrixStorage w = tdUcmWeights;
        if (w != null) {
            return w;
        }
        if (!models.isEmpty()) {
            synchronized (this) {
                try {
                    int n = models.size();
                    CompositeSsf ssf = TdSsfUcarima.of(n, i -> models.get(i));
                    w = SsfWeights.weights(ssf, n);
                    tdUcmWeights = w;
                } catch (Exception err) {
                }
            }
        }
        return tdUcmWeights;
    }
}
