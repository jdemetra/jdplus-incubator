/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.advancedsa.base.api.modelling;

import jdplus.toolkit.base.api.util.Validatable;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author Jean Palate, Mats Maggi
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class AutoModelSpec implements Validatable<AutoModelSpec> {

    public static final AutoModelSpec DEFAULT_DISABLED = AutoModelSpec.builder().build();
    public static final AutoModelSpec DEFAULT_ENABLED =
            AutoModelSpec.builder().enabled(true).build();

    public static final double DEF_TSIG = 1;
    public static final boolean DEF_AIRLINE = false, DEF_FAST = false;

    private double tsig;
    private boolean enabled, acceptDefault, fast;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .enabled(false)
                .tsig(DEF_TSIG)
                .acceptDefault(DEF_AIRLINE)
                .fast(DEF_FAST);
    }

    @Override
    public AutoModelSpec validate() throws IllegalArgumentException {
        if (tsig <= .5) {
            throw new IllegalArgumentException("TSIG should be higher than 0.5");
        }
        return this;
    }

    /**
     * The default is enabled, with default parameters
     * @return
     */
    public boolean isDefault() {
        return this.equals(DEFAULT_ENABLED);
    }

    public static class Builder implements Validatable.Builder<AutoModelSpec> {}
}
