/*
 * Copyright 2025 JDemetra+.
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
package jdplus.advancedsa.base.api.tdarima;

/**
 *
 * @author Jean Palate
 */
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
@lombok.Value
public class TdEstimationSpec {

    public TdEstimationSpec withOrders(Orders orders) {
        return toBuilder().tdspec(tdspec.withOrders(orders)).build();
    }

    public static enum Parametrization {
        START_END,
        MEAN_DELTA;
    }

    public static enum Transformation {
        LINEAR,
        CUBIC;
    }

    public static final double DEF_PRECISION = 1e-9;

    public static final double DEF_SIMPLIFICATION = 0.05;

    public static final Parametrization DEF_PARAMETRIZATION = Parametrization.MEAN_DELTA;

    public static final Transformation DEF_FN = Transformation.LINEAR;

    @lombok.NonNull @lombok.With
    private TdSarimaSpec tdspec;

    @lombok.With
    private double precision;

    @lombok.With
    @lombok.NonNull private Parametrization parametrization;

    @lombok.With
    @lombok.NonNull private Transformation transformation;

    @lombok.With
    private double simplificationThreshold;

    public static Builder builder() {
        return new Builder()
                .tdspec(TdSarimaSpec.DEFAULT)
                .precision(DEF_PRECISION)
                .simplificationThreshold(DEF_SIMPLIFICATION)
                .parametrization(DEF_PARAMETRIZATION)
                .transformation(DEF_FN);
    }

    public static TdEstimationSpec DEFAULT =
            builder().tdspec(TdSarimaSpec.DEFAULT).build();

    public static TdEstimationSpec FULL = builder().build();
}
