/*
 * Copyright 2013 National Bank of Belgium
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
package jdplus.sts.base.core;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.sts.base.api.RawBsmDecomposition;
import jdplus.sts.base.api.BsmEstimationSpec;
import jdplus.sts.base.api.BsmSpec;
import jdplus.sts.base.api.Component;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.normalizer.AbsMeanNormalizer;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.math.functions.FunctionMinimizer;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.math.functions.IFunctionPoint;
import jdplus.toolkit.base.core.math.functions.TransformedFunction;
import jdplus.toolkit.base.core.math.functions.bfgs.Bfgs;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.functions.minpack.MinPackMinimizer;
import internal.toolkit.base.core.math.functions.riso.LbfgsMinimizer;
import jdplus.toolkit.base.core.math.functions.ssq.ProxyMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BsmKernel {

    private final BsmEstimationSpec estimationSpec;

    private DoubleSeq z;
    private double[] y;
    private int period;
    private Matrix X;
    private double factor = 1;

    // mapper definition
    private BsmSpec modelSpec;
    private Component fixedVar = Component.Undefined;
    private BsmData bsm;
    private boolean converged = false;

    private DiffuseConcentratedLikelihood likelihood;
    private SsfFunction<BsmData, SsfBsm2> fn;
    private SsfFunctionPoint<BsmData, SsfBsm2> fnmax;

    private void clear() {
        z = null;
        y = null;
        X = null;
        period = 0;
        modelSpec = null;
        bsm = null;
        converged = false;
        fixedVar = Component.Undefined;
        likelihood = null;
        fn = null;
        fnmax = null;
        factor = 1;
    }

    /**
     *
     * @param espec
     */
    public BsmKernel(BsmEstimationSpec espec) {
        this.estimationSpec = espec == null ? BsmEstimationSpec.DEFAULT : espec;
    }

    private boolean _estimate() {
        converged = false;

        if (bsm == null) {
            bsm = initialize();
            modelSpec = specOf(bsm);
        }

        fn = null;
        fnmax = null;

        if (isScaling()) {
            FunctionMinimizer fmin = minimizer(estimationSpec.getPrecision(), 10);
            for (int i = 0; i < 3; ++i) {
                BsmMapping mapping = new BsmMapping(modelSpec, period, fixedVar);
                fn = buildFunction(mapping, true);
                DoubleSeq parameters = mapping.map(bsm);
                converged = fmin.minimize(fn.evaluate(parameters));
                fnmax = (SsfFunctionPoint<BsmData, SsfBsm2>) fmin.getResult();
                bsm = fnmax.getCore();
                likelihood = fnmax.getLikelihood();

                BsmData.ComponentVariance max = bsm.maxVariance();
                bsm = bsm.scaleVariances(1 / max.getVariance());
                modelSpec = specOf(bsm);
                if (fixedVar != max.getComponent()) {
                    fixedVar = max.getComponent();
                } else {
                    break;
                }
            }
        }

        if (!isScaling() || !converged) {
            FunctionMinimizer fmin = minimizer(estimationSpec.getPrecision(), 100);
            BsmMapping mapping = new BsmMapping(modelSpec, period, isScaling() ? fixedVar : null);
            fn = buildFunction(mapping, isScaling());
            DoubleSeq parameters = mapping.map(bsm);
            converged = fmin.minimize(fn.evaluate(parameters));
            fnmax = (SsfFunctionPoint<BsmData, SsfBsm2>) fmin.getResult();
            bsm = fnmax.getCore();
            likelihood = fnmax.getLikelihood();
            if (isScaling()) {
                BsmData.ComponentVariance max = bsm.maxVariance();
                bsm = bsm.scaleVariances(1 / max.getVariance());
                if (fixedVar != max.getComponent()) {
                    fixedVar = max.getComponent();
                }
            }
            modelSpec = specOf(bsm);
        }

        boolean ok = converged;
        if (fixSmallVariance(bsm)) {
            // update the bsm and the likelihood !
            BsmMapping mapping = new BsmMapping(modelSpec, period, isScaling() ? fixedVar : null);
            fn = buildFunction(mapping, isScaling());
            DoubleSeq parameters = mapping.map(bsm);
            fnmax = (SsfFunctionPoint<BsmData, SsfBsm2>) fn.evaluate(parameters);
            likelihood = fnmax.getLikelihood();
            bsm = fnmax.getCore();
            modelSpec = specOf(bsm);
            ok = false;
        }
        return ok;
    }

    private FunctionMinimizer minimizer(double eps, int niter) {
        FunctionMinimizer.Builder builder = minimizerBuilder();
        return builder
                .functionPrecision(eps)
                .maxIter(niter)
                .build();
    }

    private SsfFunction<BsmData, SsfBsm2> buildFunction(BsmMapping mapping, boolean scaling) {
        SsfData data = new SsfData(y);

        return SsfFunction.builder(data, mapping, model -> SsfBsm2.of(model))
                .regression(X, diffuseItems())
                .useFastAlgorithm(true)
                .useParallelProcessing(false)
                .useLog(!scaling)
                .useScalingFactor(scaling)
                .build();

    }

    private int diffuseItems() {
        if (X != null && estimationSpec.isDiffuseRegression()) {
            return X.getColumnsCount();
        }
        return 0;
    }

    private boolean estimate() {
        for (int i = 0; i < 4; ++i) {
            if (_estimate()) {
                return true;
            }
        }
        return true;

    }

    private boolean fixSmallVariance(BsmData model) {
        // return false;
        double vmin = estimationSpec.getLikelihoodRatioThreshold();
        int imin = -1;
        BsmMapping mapping = new BsmMapping(modelSpec, period, isScaling() ? fixedVar : null);
        SsfFunction<BsmData, SsfBsm2> fn = buildFunction(mapping, isScaling());
        DoubleSeq p = mapping.map(model);
        SsfFunctionPoint instance = new SsfFunctionPoint(fn, p);
        double ll = instance.getLikelihood().logLikelihood();
        int nvars = mapping.varsCount();
        for (int i = 0; i < nvars; ++i) {
            if (p.get(i) < 0.2) {
                DataBlock np = DataBlock.of(p);
                np.set(i, 0);
                instance = new SsfFunctionPoint(fn, np);
                double llcur = instance.getLikelihood().logLikelihood();
                double v = 2 * (ll - llcur);
                if (v < vmin) {
                    vmin = v;
                    imin = i;
                }
            }
        }

        if (imin < 0) {
            return false;
        }

        Component cmp = mapping.varPosition(imin);
        modelSpec = modelSpec.fixComponent(cmp, 0);
        return true;
    }

    /**
     *
     * @return
     */
    public DiffuseConcentratedLikelihood getLikelihood() {
        return likelihood;
    }

    /**
     *
     * @param raw
     * @return
     */
    public BsmData result(boolean raw) {
        if (bsm == null) {
            return null;
        }
        if (raw || fixedVar == null || fixedVar == Component.Undefined) {
            return bsm;
        } else {
            return bsm.scaleVariances(likelihood.sigma2());
        }

    }

    /**
     *
     * @param raw
     * @return
     */
    public BsmSpec finalSpecification(boolean raw) {
        if (raw) {
            return modelSpec;
        } else {
            return specOf(result(false));
        }
    }

    /**
     *
     * @return
     */
    public boolean hasConverged() {
        return converged;
    }

    public static final double RVAR = 5;

    private SsfFunctionPoint<BsmData, SsfBsm2> ll(Component cmp) {
        BsmMapping mapping = new BsmMapping(modelSpec, period, cmp);
        SsfFunction<BsmData, SsfBsm2> fn = buildFunction(mapping, true);
        Bfgs bfgs = Bfgs.builder()
                .functionPrecision(1e-5)
                .maxIter(10)
                .build();
        bfgs.minimize(fn.evaluate(mapping.getDefaultParameters()));
        return (SsfFunctionPoint<BsmData, SsfBsm2>) bfgs.getResult();
    }

    private BsmData initialize2() {
        // Set default values
        BsmMapping mapping = new BsmMapping(modelSpec, period, null);
        bsm = mapping.map(mapping.getDefaultParameters());
        modelSpec = specOf(bsm);
        BsmData bsm0 = bsm;
        //
        double llmax = 0;
        Component cmax = Component.Undefined;
        if (modelSpec.hasNoise()) {
            SsfFunctionPoint<BsmData, SsfBsm2> lcur = ll(Component.Noise);
            llmax = lcur.getLikelihood().logLikelihood();
            bsm0 = lcur.getCore();
            cmax = Component.Noise;
        }
        if (modelSpec.hasLevel()) {
            SsfFunctionPoint<BsmData, SsfBsm2> lcur = ll(Component.Level);
            double ll = lcur.getLikelihood().logLikelihood();
            if (bsm0 == null || ll > llmax) {
                llmax = ll;
                bsm0 = lcur.getCore();
                cmax = Component.Level;
            }
        }
        if (modelSpec.hasSlope()) {
            SsfFunctionPoint<BsmData, SsfBsm2> lcur = ll(Component.Slope);
            double ll = lcur.getLikelihood().logLikelihood();
            if (bsm0 == null || ll > llmax) {
                llmax = ll;
                bsm0 = lcur.getCore();
                cmax = Component.Slope;
            }
        }
        if (modelSpec.hasSeasonal()) {
            SsfFunctionPoint<BsmData, SsfBsm2> lcur = ll(Component.Seasonal);
            double ll = lcur.getLikelihood().logLikelihood();
            if (bsm0 == null || ll > llmax) {
                llmax = ll;
                bsm0 = lcur.getCore();
                cmax = Component.Seasonal;
            }
        }
        if (modelSpec.hasCycle()) {
            SsfFunctionPoint<BsmData, SsfBsm2> lcur = ll(Component.Cycle);
            double ll = lcur.getLikelihood().logLikelihood();
            if (bsm0 == null || ll > llmax) {
                llmax = ll;
                bsm0 = lcur.getCore();
                cmax = Component.Cycle;
            }
        }
        this.fixedVar = cmax;
        return bsm0;
    }

    private BsmData initialize() {
        BsmMapping mapping = new BsmMapping(modelSpec, period, null);
        DoubleSeq p = mapping.getDefaultParameters();
        BsmData start = mapping.map(p);
        if (!isScaling()) {
            return start;
        }

        SsfFunction<BsmData, SsfBsm2> fn = buildFunction(mapping,
                true);

        SsfFunctionPoint instance = new SsfFunctionPoint(fn, p);
        double lmax = instance.getLikelihood().logLikelihood();
        int imax = -1;
        int nvars = mapping.varsCount();
        DoubleSeq refp = p;
        for (int i = 0; i < nvars; ++i) {
            DataBlock np = DataBlock.of(p);
            np.mul(i, RVAR);
            instance = new SsfFunctionPoint(fn, np);
            double nll = instance.getLikelihood().logLikelihood();
            if (nll > lmax) {
                lmax = nll;
                imax = i;
                refp = np;
            }
        }
        if (imax < 0) {
            if (modelSpec.hasNoise()) {
                fixedVar = Component.Noise;
            } else if (modelSpec.hasLevel()) {
                fixedVar = Component.Level;
            } else {
                fixedVar = mapping.varPosition(0);
            }
            return start;
        } else {
            BsmData nbsm = mapping.map(refp);
            fixedVar = mapping.varPosition(imax);
            return nbsm;
        }
    }

    /**
     *
     * @param y
     * @param period
     * @param model
     * @return
     */
    public boolean process(DoubleSeq y, int period, BsmSpec model) {
        return process(y, null, period, model);
    }

    /**
     *
     * @param y
     * @param x
     * @param period
     * @param model
     * @return
     */
    public boolean process(DoubleSeq y, Matrix x, int period, BsmSpec model) {
        clear();
        this.z = y;
        this.y = y.toArray();
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        factor = normalizer.normalize(DataBlock.of(this.y));
        this.X = FastMatrix.of(x);
        this.period = period;
        modelSpec = model;
        boolean rslt = estimate();
        if (rslt) {
            likelihood = likelihood.rescale(factor, null);
        }
        return rslt;
    }

    private boolean isScaling() {
        return estimationSpec.isScalingFactor() && modelSpec.isScalable();
    }

    private FunctionMinimizer.Builder minimizerBuilder() {
        if (!isScaling()) {
            return Bfgs.builder();
        } else {
            switch (estimationSpec.getOptimizer()) {
                case LevenbergMarquardt:
                    return ProxyMinimizer.builder(LevenbergMarquardtMinimizer.builder());
                case MinPack:
                    return ProxyMinimizer.builder(MinPackMinimizer.builder());
                case LBFGS:
                    return LbfgsMinimizer.builder();
                default:
                    return Bfgs.builder();
            }
        }
    }

    public RawBsmDecomposition decompose() {
        if (bsm == null) {
            return null;
        }
        // linearized series
        DataBlock lin = DataBlock.of(z);
        if (X != null) {
            DoubleSeqCursor b = likelihood.coefficients().cursor();
            for (int i = 0; i < X.getColumnsCount(); ++i) {
                lin.addAY(-b.getAndNext(), X.column(i));
            }
        }
        SsfBsm ssf = SsfBsm.of(bsm);
        DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, new SsfData(lin), true, true);
        RawBsmDecomposition.Builder builder = RawBsmDecomposition.builder();
        builder.cmp(Component.Series, lin);
        int pos = SsfBsm.searchPosition(bsm, Component.Level);
        if (pos >= 0) {
            builder.cmp(Component.Level, sr.getComponent(pos));
            builder.ecmp(Component.Level, sr.getComponentVariance(pos).sqrt());
        }
        pos = SsfBsm.searchPosition(bsm, Component.Slope);
        if (pos >= 0) {
            builder.cmp(Component.Slope, sr.getComponent(pos));
            builder.ecmp(Component.Slope, sr.getComponentVariance(pos).sqrt());
        }
        pos = SsfBsm.searchPosition(bsm, Component.Cycle);
        if (pos >= 0) {
            builder.cmp(Component.Cycle, sr.getComponent(pos));
            builder.ecmp(Component.Cycle, sr.getComponentVariance(pos).sqrt());
        }
        pos = SsfBsm.searchPosition(bsm, Component.Seasonal);
        if (pos >= 0) {
            builder.cmp(Component.Seasonal, sr.getComponent(pos));
            builder.ecmp(Component.Seasonal, sr.getComponentVariance(pos).sqrt());
        }
        pos = SsfBsm.searchPosition(bsm, Component.Noise);
        if (pos >= 0) {
            builder.cmp(Component.Noise, sr.getComponent(pos));
            builder.ecmp(Component.Noise, sr.getComponentVariance(pos).sqrt());
        }
        return builder.build();
    }

    public IFunction likelihoodFunction() {
        BsmMapping mapper = new BsmMapping(modelSpec, period, fixedVar);
        SsfFunction<BsmData, SsfBsm2> fn = buildFunction(mapper, false);
        double a = (likelihood.dim() - likelihood.ndiffuse());
//        double a = (likelihood.dim() - likelihood.ndiffuse()) * Math.log(m_factor);
        return new TransformedFunction(fn, TransformedFunction.linearTransformation(-a, 1));
    }

    public IFunctionPoint maxLikelihoodFunction() {
        BsmMapping mapper = new BsmMapping(modelSpec, period, fixedVar);
        IFunction ll = likelihoodFunction();
        return ll.evaluate(mapper.map(bsm));
    }

    private BsmSpec specOf(BsmData bsmData) {
        return modelSpec.toBuilder()
                .level(nparam(modelSpec.getLevelVar(), bsmData.getLevelVar()), nparam(modelSpec.getSlopeVar(), bsmData.getSlopeVar()))
                .seasonal(modelSpec.getSeasonalModel(), nparam(modelSpec.getSeasonalVar(), bsmData.getSeasonalVar()))
                .noise(nparam(modelSpec.getNoiseVar(), bsmData.getNoiseVar()))
                .cycle(nparam(modelSpec.getCycleVar(), bsmData.getCycleVar()),
                        nparam(modelSpec.getCycleDumpingFactor(), bsmData.getCycleDumpingFactor()),
                        nparam(modelSpec.getCycleLength(), bsmData.getCycleLength()))
                .build();

    }

    private Parameter nparam(Parameter p, double c) {
        if (p == null || p.isFixed()) {
            return p;
        } else {
            return Parameter.estimated(c);
        }
    }

}
