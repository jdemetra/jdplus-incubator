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

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.core.ssf.SsfException;
import jdplus.toolkit.base.core.ssf.composite.MultivariateCompositeSsf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public class MstsMapping implements IParametricMapping<MultivariateCompositeSsf> {

    private final List<IMstsBuilder> builders = new ArrayList<>();
    private final List<ParameterInterpreter> parameters = new ArrayList<>();
    private boolean scalable = true;

    public void add(IMstsBuilder decoder) {
        this.builders.add(decoder);
    }

    public void add(ParameterInterpreter block) {
        this.parameters.add(block);
    }

    public void addAll(List<ParameterInterpreter> blocks) {
        this.parameters.addAll(blocks);
    }

    public Stream<ParameterInterpreter> parameters() {
        return parameters.stream();
    }

    public List<ParameterInterpreter> allParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public List<VarianceInterpreter> smallVariances(DoubleSeq cur, double eps) {
        List<VarianceInterpreter> small = new ArrayList<>();
        double max = maxVariance(cur);
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            int dim = p.getDomain().getDim();
            if (!p.isFixed() && dim == 1 && p instanceof VarianceInterpreter) {
                VarianceInterpreter vp = (VarianceInterpreter) p;
                if (vp.isNullable()) {
                    double v = cur.get(pos);
                    if (v < eps) {
                        small.add(vp);
                    }
                }
            }

            pos += dim;
        }
        return small;
    }

    public void fixModelParameters(Predicate<ParameterInterpreter> selection, DoubleSeq fullParameters) {
        ParameterInterpreter.fixModelParameters(parameters, selection, fullParameters);
    }

    public double maxVariance(DoubleSeq cur) {
        double max = 0;
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            int dim = p.getDomain().getDim();
            if (dim == 1 && !p.isFixed() && p instanceof VarianceInterpreter) {
                double v = cur.get(pos);
                if (v > max) {
                    max = v;
                }
            }
            pos += dim;
        }
        return max;
    }

    public VarianceInterpreter fixMaxVariance(double[] pcur, double var) {
        double max = 0;
        int pos = 0;
        VarianceInterpreter mvar = null;
        for (ParameterInterpreter p : parameters) {
            int dim = p.dim();
            if (dim == 1 && p instanceof VarianceInterpreter) {
                double v = pcur[pos];
                if (v > max) {
                    max = v;
                    mvar = (VarianceInterpreter) p;
                }
            }
            pos += dim;
        }
        if (mvar != null && ! mvar.isFixed()) {
            mvar.fixStde(Math.sqrt(var));
            if (max != var) {
                double ratio = Math.sqrt(var / max);
                rescale(ratio, pcur, p -> p instanceof VarianceInterpreter);
            }
        }
        return mvar;
    }

    /**
     * Rescale the parameters
     *
     * @param factor Scaling factor
     * @param curp Buffer (in/out)
     * @param check Condition
     */
    public void rescale(double factor, double[] curp, Predicate<ParameterInterpreter> check) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            pos = p.rescale(factor, curp, pos, check);
        }
    }

    /**
     * Rescale the parameters
     *
     * @param factor Scaling factor
     * @param curp Current parameters
     * @param check Condition
     * @return New (rescaled) parameters
     */
    public DoubleSeq rescale(double factor, DoubleSeq curp, Predicate<ParameterInterpreter> check) {
        double[] c = curp.toArray();
        rescale(factor, c, check);
        return DoubleSeq.of(c);
    }

    /**
     * From function parameters to model parameters
     *
     * @param input
     * @return
     */
    public DoubleSeq modelParameters(DoubleSeq input) {
        return DoubleSeq.of(ParameterInterpreter.decode(parameters, input));
    }

    /**
     * From model parameters to function parameters
     *
     * @param input
     * @return
     */
    public DoubleSeq functionParameters(DoubleSeq input) {
        return DoubleSeq.of(ParameterInterpreter.encode(parameters, input));
    }

    @Override
    public MultivariateCompositeSsf map(DoubleSeq p) {
        MultivariateCompositeSsf.Builder builder = MultivariateCompositeSsf.builder();
        DoubleSeq fp = modelParameters(p);
        for (IMstsBuilder decoder : builders) {
            int np = decoder.decode(fp, builder);
            fp = fp.drop(np, 0);
        }
        return builder.build();
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        double[] buffer = new double[getDim()];
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            pos = p.fillDefault(buffer, pos);
        }
        return DoubleSeq.of(buffer);
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (!p.getDomain().checkBoundaries(inparams.extract(pos, dim))) {
                    return false;
                }
                pos += dim;
            }
        }
        return true;
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (idx < pos + dim) {
                    return p.getDomain().epsilon(inparams.extract(pos, dim), idx - pos);
                }
                pos += dim;
            }
        }
        throw new SsfException(SsfException.MODEL);
    }

    @Override
    public int getDim() {
        return ParameterInterpreter.functionDim(parameters.stream());
    }

    @Override
    public double lbound(int idx) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (idx < pos + dim) {
                    return p.getDomain().lbound(idx - pos);
                }
                pos += dim;
            }
        }
        throw new SsfException(SsfException.MODEL);
    }

    @Override
    public double ubound(int idx) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (idx < pos + dim) {
                    return p.getDomain().ubound(idx - pos);
                }
                pos += dim;
            }
        }
        throw new SsfException(SsfException.MODEL);
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        boolean changed = false, invalid = false;
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                switch (p.getDomain().validate(ioparams.extract(pos, dim))) {
                    case Changed ->
                        changed = true;
                    case Invalid ->
                        invalid = true;
                }
                pos += dim;
            }
        }
        if (invalid) {
            return ParamValidation.Invalid;
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    /**
     * @return the scalable
     */
    public boolean isScalable() {
        return scalable;
    }

    /**
     * @param scalable the scalable to set
     */
    public void setScalable(boolean scalable) {
        this.scalable = scalable;
    }

    public String[] parametersName() {
        List<String> names = new ArrayList<>();
        for (ParameterInterpreter block : parameters) {
            int n = block.getDomain().getDim();
            if (n == 1) {
                names.add(block.getName());
            } else {
                for (int i = 1; i <= n; ++i) {
                    names.add(block.getName() + "_" + i);
                }
            }
        }
        return names.toArray(String[]::new);
    }

}
