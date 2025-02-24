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
public class MAInterpreter implements ParameterInterpreter {

    private static final double DEF_MA = -.6;

    private double th;
    private boolean fixed;
    private final String name;

    public MAInterpreter(final String name, boolean nullable) {
        this.name = name;
        th = DEF_MA;
        fixed = false;
    }

    public MAInterpreter(final String name, double th, boolean fixed) {
        this.th = th;
        this.fixed = fixed;
        this.name = name;
    }

    @Override
    public MAInterpreter duplicate() {
        return new MAInterpreter(name, th, fixed);
    }

    @Override
    public boolean isScaleSensitive(boolean variance) {
        return false;
    }

    @Override
    public int rescale(double factor, double[] buffer, int pos, Predicate<ParameterInterpreter> check) {
        return pos + 1;
    }

    @Override
    public int dim() {
        return 1;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isNullable() {
        return false;
    }

    @Override
    public void fixModelParameter(DoubleSeqCursor reader) {
        th = reader.getAndNext();
        if (Math.abs(th) > 1) {
            th = 1 / th;
        }
        fixed = true;
    }

    @Override
    public void free() {
        fixed = false;
    }

    public double value() {
        return th;
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
            buffer[pos] = Math.abs(e) > 1 ? 1 / e : e;
        } else {
            buffer[pos] = th;
        }
        return pos + 1;
    }

    @Override
    public int encode(DoubleSeqCursor input, double[] buffer, int pos) {
        double p = input.getAndNext();
        if (!fixed) {
            buffer[pos] = p;
            return pos + 1;
        } else {
            return pos;
        }
    }

    @Override
    public int fillDefault(double[] buffer, int pos) {
        if (!fixed) {
            buffer[pos] = th;
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
            return EPS;
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
