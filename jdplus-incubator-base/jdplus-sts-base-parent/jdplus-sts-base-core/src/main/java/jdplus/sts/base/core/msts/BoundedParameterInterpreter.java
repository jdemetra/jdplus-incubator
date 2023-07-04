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
package jdplus.sts.base.core.msts;

import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.math.functions.IParametersDomain;
import jdplus.toolkit.base.core.math.functions.ParametersRange;

/**
 *
 * @author palatej
 */
public class BoundedParameterInterpreter implements ParameterInterpreter {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private double lbound = 0, ubound = Double.MAX_VALUE;
        private boolean open = true;
        private double value = .5;
        private boolean fixed = false;
        private String name = "";

        public Builder bounds(double lbound, double ubound, boolean open) {
            this.lbound = lbound;
            this.ubound = ubound;
            this.open = open;
            return this;
        }

        public Builder value(double value, boolean fixed) {
            this.value = value;
            this.fixed = fixed;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public BoundedParameterInterpreter build() {
            return new BoundedParameterInterpreter(name, value, fixed, lbound, ubound, open);
        }
    }

    private static final double EPS = 1e-8;

    private final ParametersRange range;
    private double value;
    private boolean fixed;
    private final String name;

    private BoundedParameterInterpreter(final String name, double value, boolean fixed, double lbound, double ubound, boolean open) {
        this.name = name;
        this.value = value;
        this.fixed = fixed;
        this.range = new ParametersRange(lbound, ubound, open);
    }

    @Override
    public BoundedParameterInterpreter duplicate() {
        return new BoundedParameterInterpreter(name, value, fixed, range.getA(), range.getB(), range.isOpen());
    }

    @Override
    public String getName() {
        return name;
    }

    public double fix(double val) {
        double oldval = value;
        value = val;
        fixed = true;
        return oldval;
    }

    @Override
    public void free() {
        fixed = false;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        value = reader.getAndNext();
        fixed = true;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    @Override
    public IParametersDomain getDomain() {
        return range;
    }

    @Override
    public int decode(DoubleSeqCursor input, double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = input.getAndNext();
        } else {
            buffer[pos] = value;
        }
        return pos + 1;
    }

    @Override
    public int encode(DoubleSeqCursor input, double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = input.getAndNext();
            return pos + 1;
        } else {
            input.skip(1);
            return pos;
        }
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = value;
            return pos + 1;
        } else {
            return pos;
        }
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        return false;
    }

    @Override
    public int rescaleVariances(double factor, double[] buffer, int pos) {
        return pos+1;
    }

}
