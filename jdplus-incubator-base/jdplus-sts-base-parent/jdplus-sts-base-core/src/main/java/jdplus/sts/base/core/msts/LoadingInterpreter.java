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

import java.util.function.Predicate;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.math.functions.IParametersDomain;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public final class LoadingInterpreter implements ParameterInterpreter {

    private static final double DEF_VALUE = .1;

    private double c;
    private boolean fixed;
    private final String name;

    public LoadingInterpreter(final String name) {
        this.name = name;
        c = DEF_VALUE;
        fixed = false;
    }

    public LoadingInterpreter(final String name, double loading, boolean fixed) {
        this.name = name;
        this.c = loading;
        this.fixed = fixed;
    }

    @Override
    public LoadingInterpreter duplicate() {
        return new LoadingInterpreter(name, c, fixed);
    }

    @Override
    public String getName() {
        return name;
    }

    public double fix(double val) {
        double oldval = c;
        c = val;
        fixed = true;
        return oldval;
    }

    public double value() {
        return c;
    }

    @Override
    public void free() {
        fixed = false;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        c = reader.getAndNext();
        fixed = true;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    @Override
    public IParametersDomain getDomain() {
        return Domain.INSTANCE;
    }

    @Override
    public int decode(DoubleSeqCursor input, double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = input.getAndNext();
        } else {
            buffer[pos] = c;
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
            buffer[pos] = c;
            return pos + 1;
        } else {
            return pos;
        }
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        return true;
    }

    @Override
    public int rescale(double factor, double[] buffer, int pos, Predicate<ParameterInterpreter> check) {
        // Never changed if fixed. Variances should be changed instead.
        if (!fixed && check.test(this) ) {
            buffer[pos] *= factor;
        }
        return pos + 1;
    }

    @Override
    public int dim() {
        return 1;
    }

    static class Domain implements IParametersDomain {

        static final Domain INSTANCE = new Domain();

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            return true;
        }

        private static final double EPS = 1e-4;

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            double c = inparams.get(0);
            if (c >= 0) {
                return Math.max(EPS, c * EPS);
            } else {
                return -Math.max(EPS, -c * EPS);
            }
        }

        @Override
        public int getDim() {
            return 1;
        }

        @Override
        public double lbound(int idx) {
            return -Double.MAX_VALUE;
        }

        @Override
        public double ubound(int idx) {
            return Double.MAX_VALUE;
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            return ParamValidation.Valid;
        }
    }

}
