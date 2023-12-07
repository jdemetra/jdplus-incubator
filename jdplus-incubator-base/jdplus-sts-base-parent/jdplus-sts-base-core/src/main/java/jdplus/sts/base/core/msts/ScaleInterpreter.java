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
public class ScaleInterpreter implements ParameterInterpreter {

    private static final double DEF_STDE = .1;

    private double scale;
    private boolean fixed;
    private final String name;
    private final boolean nullable;

    public ScaleInterpreter(final String name, boolean nullable) {
        this.name = name;
        this.nullable = nullable;
        scale = DEF_STDE;
        fixed = false;
    }

    public ScaleInterpreter(final String name, double stde, boolean fixed, boolean nullable) {
        this.scale = stde;
        this.fixed = fixed;
        this.name = name;
        this.nullable = nullable;
    }

    @Override
    public ScaleInterpreter duplicate() {
        ScaleInterpreter p = new ScaleInterpreter(name, nullable);
        p.scale = scale;
        p.fixed = fixed;
        return p;
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        return true;
    }

    @Override
    public int rescale(double factor, double[] buffer, int pos, Predicate<ParameterInterpreter> check) {
        if (check.test(this)) {
            buffer[pos] *= factor;
        }
        return pos + 1;
    }

    public void scale(double s) {
        scale = s;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isNullable() {
        return true;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        scale = Math.abs(reader.getAndNext());
        fixed = true;
    }

    @Override
    public void free() {
        fixed = false;
    }

    @Override
    public int dim() {
        return 1;
    }

    public double fixScale(double e) {
        double olde = scale;
        scale = Math.abs(e);
        fixed = true;
        return olde;
    }

    public void freeScale(double e) {
        fixed = false;
        scale = e;
    }

    public double scale() {
        return scale;
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
            double e = input.getAndNext();
            buffer[pos] = Math.abs(e);
        } else {
            buffer[pos] = scale;
        }
        return pos + 1;
    }

    @Override
    public int encode(DoubleSeqCursor input, double[] buffer, int pos) {
        double v = input.getAndNext();
        if (!fixed) {
            buffer[pos] = v;
            return pos + 1;
        } else {
            return pos;
        }
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = scale;
            return pos + 1;
        } else {
            return pos;
        }
    }

    static class Domain implements IParametersDomain {

        static final Domain INSTANCE = new Domain();

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            return true;
        }

        private static final double EPS = 1e-6;

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
