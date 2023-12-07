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

import jdplus.toolkit.base.core.math.functions.bfgs.Bfgs;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.functions.minpack.MinPackMinimizer;
import internal.toolkit.base.core.math.functions.riso.LbfgsMinimizer;
import jdplus.toolkit.base.core.ssf.likelihood.MarginalLikelihoodFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.composite.MultivariateCompositeSsf;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.stats.likelihood.Likelihood;
import jdplus.toolkit.base.api.math.functions.Optimizer;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodFunction;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodFunctionPoint;
import jdplus.toolkit.base.core.math.functions.FunctionMinimizer;
import jdplus.toolkit.base.core.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.toolkit.base.core.ssf.likelihood.AugmentedLikelihoodFunction;

/**
 *
 * @author palatej
 */
public class MstsMonitor {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final int MAXITER = 20, MAXITER_MIN = 500;
        private static final double SMALL_VAR = 1e-8;

        private boolean marginal = false;
        private SsfInitialization initialization = SsfInitialization.Diffuse;
        private Optimizer optimizer = Optimizer.LevenbergMarquardt;
        private double precision = 1e-9, smallVar = SMALL_VAR, precision2 = 1e-7, precision3 = 1e-3;
        private int maxIter = MAXITER;
        private int maxIterOptimzer = MAXITER_MIN;
        private boolean concentratedLikelihood = true;

        public Builder initialization(SsfInitialization initialization) {
            this.initialization = initialization;
            return this;
        }

        public Builder marginal(boolean marginal) {
            this.marginal = marginal;
            return this;
        }

        public Builder concentratedLikelihood(boolean cl) {
            this.concentratedLikelihood = cl;
            if (!cl) {
                optimizer = Optimizer.BFGS;
            }
            return this;
        }

        public Builder precision(double eps) {
            this.precision = eps;
            return this;
        }

        public Builder smallVariance(double var) {
            if (var <= 0) {
                throw new IllegalArgumentException();
            }
            this.smallVar = var;
            return this;
        }

        public Builder maxIter(int maxiter) {
            this.maxIter = maxiter;
            return this;
        }

        public Builder maxIterOptimizer(int maxiter) {
            this.maxIterOptimzer = maxiter;
            return this;
        }

        public Builder optimizer(Optimizer optimizer) {
            this.optimizer = optimizer;
            return this;
        }

        public MstsMonitor build() {
            return new MstsMonitor(this);
        }
    }
    private static final int NITER = 8;
    private final boolean concentratedLikelihood;
    private final double precision, precision2, precision3, smallStde;
    private final int maxIter;
    private final int maxIterOptimzer;
    private final boolean marginal;
    private final SsfInitialization initialization;
    private final Optimizer optimizer;

    private FastMatrix data;
    private MstsMapping model;
    private MultivariateCompositeSsf ssf;
    // fullp contains the current full parameters of the model
    private DoubleSeq fullp;
    private Likelihood ll;
    private VarianceInterpreter fixedVariance;
    private boolean converged;

    private final List<VarianceInterpreter> smallVariances = new ArrayList<>();
//    private final List<LoadingParameter> smallLoadings = new ArrayList<>();

    private MstsMonitor(Builder builder) {
        this.marginal = builder.marginal;
        this.initialization = builder.initialization;
        this.optimizer = builder.optimizer;
        this.maxIterOptimzer = builder.maxIterOptimzer;
        this.precision = builder.precision;
        this.precision2 = builder.precision2;
        this.precision3 = builder.precision3;
        this.concentratedLikelihood = builder.concentratedLikelihood;
        this.maxIter = builder.maxIter;
        this.smallStde = Math.sqrt(builder.smallVar);
    }

//    private LikelihoodFunction function(boolean concentrated) {
//        return data.getColumnsCount() == 1 ? sfunction(concentrated) : mfunction(concentrated);
//    }
//
    private LikelihoodFunction function(boolean concentrated) {
        SsfMatrix s = new SsfMatrix(data);
        boolean needres = (optimizer == Optimizer.LevenbergMarquardt || optimizer == Optimizer.MinPack) || initialization == SsfInitialization.Augmented || marginal;
        if (marginal) {
            return MarginalLikelihoodFunction.builder(s.asSsfData(), model, m -> m.asSsf())
                    .useParallelProcessing(true)
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentrated)
                    .residuals(needres)
                    .build();
        }
        return switch (initialization) {
            case Augmented -> AugmentedLikelihoodFunction.builder(s.asSsfData(), model, m -> m.asSsf())
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentrated)
                    .useFastAlgorithm(true)
                    .useParallelProcessing(true)
                    .residuals(needres)
                    .build();
            case Augmented_NoCollapsing -> AugmentedLikelihoodFunction.builder(s.asSsfData(), model, m -> m.asSsf())
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentrated)
                    .useFastAlgorithm(false)
                    .useParallelProcessing(true)
                    .useCollapsing(false)
                    .residuals(needres)
                    .build();
            case Augmented_Robust -> AugmentedLikelihoodFunction.builder(s.asSsfData(), model, m -> m.asSsf())
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentrated)
                    .useFastAlgorithm(false)
                    .useParallelProcessing(true)
                    .robust(true)
                    .residuals(needres)
                    .build();
            case Diffuse -> SsfFunction.builder(s.asSsfData(), model, m -> m.asSsf())
                    .useParallelProcessing(true)
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentrated)
                    .useFastAlgorithm(true)
                    .useSqrtInitialization(false)
                    .build();
            default -> SsfFunction.builder(s.asSsfData(), model, m -> m.asSsf())
                    .useParallelProcessing(true)
                    .useMaximumLikelihood(true)
                    .useScalingFactor(concentrated)
                    .useFastAlgorithm(true)
                    .useSqrtInitialization(true)
                    .build();
        };
    }

    private boolean needFixedVariance() {
        if (fixedVariance != null) {
            return true;
        }
        if (!concentratedLikelihood || !model.isScalable()) {
            return false;
        }
        // No fixed variance
        return ! hasFixedVariance();
    }
        
    private boolean hasFixedVariance(){
        return model.parameters()
                .anyMatch(p -> p.isFixed()
                && p instanceof VarianceInterpreter
                && ((VarianceInterpreter) p).stde() > 0);
    }

    public void process(FastMatrix data, MstsMapping model, DoubleSeq fullInitial) {
        this.data = data;
        this.model = model;
        ll = null;
        ssf = null;
        fixedVariance = null;
        if (fullInitial == null) {
            fullp = model.modelParameters(model.getDefaultParameters());
        } else {
            fullp = fullInitial;
        }

        // initial parameters of the likelihood function
        DoubleSeq p0 = model.functionParameters(fullp);
        // when we don't have initial parameters and when we don't  
        // concentrate the likelihood, it is often usefull to rescale the 
        // variances of the model
        if (fullInitial == null && !concentratedLikelihood && !hasFixedVariance()) {
            ll = function(false).evaluate(p0).getLikelihood();
            double factor = 10;
            int k = 0;
            do {
                DoubleSeq pcur = fullp;
                fullp = rescaleVariances(fullp, factor);
                p0 = model.functionParameters(fullp);
                try {
                    Likelihood nll = function(false).evaluate(p0).getLikelihood();
                    double ndll = nll.logLikelihood() - ll.logLikelihood();
                    if (ndll <= 0) {
                        break;
                    }
                    ll = nll;

                } catch (Exception err) {
                    fullp = pcur;
                    break;
                }
            } while (k++ < 10);
        } else {
            double[] mp0 = fullp.toArray();
            fixedVariance = model.fixMaxVariance(mp0, 1);
            fullp = DoubleSeq.of(mp0);
        }
        int fniter = 30;
        double curll = 0;
        for (int k = 0; k < NITER; ++k) {
            int niter = 0;
            do {
                converged = false;
                DoubleSeq p = model.functionParameters(fullp);
                LikelihoodFunction fn = function(concentratedLikelihood);
                LikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, curll == 0 ? precision3 : precision2, fniter, p);
                ll = rslt.getLikelihood();
                p = rslt.getParameters();
                fullp = model.modelParameters(p);
                if (!fixSmallVariance() && !freeSmallVariance()) {
                    break;
                }
            } while (niter++ < maxIter);

            if (needFixedVariance()) {
                VarianceInterpreter old = fixedVariance;
                double[] mp = fullp.toArray();
                fixedVariance = model.fixMaxVariance(mp, 1);
                if (old != fixedVariance) {
                    converged = false;
                    old.free();
                    // ?
                    for (VarianceInterpreter vp : smallVariances) {
                        vp.freeStde(smallStde);
                    }
                    smallVariances.clear();
                    fullp = DoubleSeq.of(mp);
                    DoubleSeq p = model.functionParameters(fullp);
                    ll = function(concentratedLikelihood).evaluate(p).getLikelihood();
                }
            }
            // Fix all variances and loadings
            List<ParameterInterpreter> fixedBlocks = model.parameters()
                    .filter(p -> !p.isFixed() && p.isScaleSensitive(true))
                    .collect(Collectors.toList());

            model.fixModelParameters(p -> p.isScaleSensitive(true), fullp);
            if (model.parameters().filter(p -> !p.isFixed()).count() > 0) {
                converged = false;
                LikelihoodFunction fn = function(concentratedLikelihood);
                DoubleSeq curp = model.functionParameters(fullp);
                LikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, curll == 0 ? precision3 : precision2, fniter, curp);
                ll = rslt.getLikelihood();
                DoubleSeq np = rslt.getParameters();
                fullp = model.modelParameters(np);
            }
            // Free the fixed parameters
            for (ParameterInterpreter p : fixedBlocks) {
                p.free();
            }
            if (curll != 0 && Math.abs(curll - ll.logLikelihood()) < precision2 && converged) {
                //            if (k>0 &&converged) {
                break;
            } else {
                curll = ll.logLikelihood();
            }
        }
        DoubleSeq p = model.functionParameters(fullp);
        // Final estimation. To do anyway
        LikelihoodFunction fn = function(concentratedLikelihood);
        converged = false;
        LikelihoodFunctionPoint rslt = min(fn, concentratedLikelihood, precision, this.maxIterOptimzer, p);
        ll = rslt.getLikelihood();
        p = rslt.getParameters();
        fullp = model.modelParameters(p);
        ssf = model.map(p);
    }

    private LikelihoodFunctionPoint min(LikelihoodFunction fn, boolean concentrated, double eps, int niter, DoubleSeq start) {
        if (fn.getDomain().getDim() == 0) {
            converged = true;
            return fn.evaluate(start);
        }
        Optimizer cur = optimizer;
        if (!concentrated && (cur == Optimizer.LevenbergMarquardt || cur == Optimizer.MinPack)) {
            cur = Optimizer.BFGS;
        }

        switch (cur) {
            case LBFGS -> {
                FunctionMinimizer m = LbfgsMinimizer
                        .builder()
                        .functionPrecision(eps)
                        .maxIter(niter)
                        .build();
                converged = m.minimize(fn.evaluate(start));
                return (LikelihoodFunctionPoint) m.getResult();
            }
            case BFGS -> {
                FunctionMinimizer m = Bfgs
                        .builder()
                        .functionPrecision(eps)
                        .maxIter(niter)
                        .build();
                converged = m.minimize(fn.evaluate(start));
                return (LikelihoodFunctionPoint) m.getResult();
            }
            case MinPack -> {
                SsqFunctionMinimizer lm = MinPackMinimizer.builder()
                        .functionPrecision(eps)
                        .maxIter(niter)
                        .build();
                converged = lm.minimize(fn.evaluate(start));
                return (LikelihoodFunctionPoint) lm.getResult();
            }
            default -> {
                SsqFunctionMinimizer lm = LevenbergMarquardtMinimizer.builder()
                        .functionPrecision(eps)
                        .maxIter(niter)
                        .build();
                converged = lm.minimize(fn.evaluate(start));
                return (LikelihoodFunctionPoint) lm.getResult();
            }

        }
    }

    private boolean freeSmallVariance() {
        if (smallVariances.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceInterpreter cur = null;
        double eps = smallVariance();
        for (VarianceInterpreter small : smallVariances) {
            small.fixStde(Math.sqrt(eps));
            try {
                DoubleSeq nprslts = model.functionParameters(fullp);
                Likelihood nll = function(concentratedLikelihood).evaluate(nprslts).getLikelihood();
                double d = nll.logLikelihood() - ll.logLikelihood();
                if (d > dll) {
                    dll = d;
                    cur = small;
                }
            } catch (Exception err) {
            }
            small.fixStde(0);
        }
        if (cur != null) {
            cur.freeStde(smallStde);
            smallVariances.remove(cur);
            return true;
        } else {
            return false;
        }

    }

    private Predicate<ParameterInterpreter> var() {
        return p -> !p.isFixed() && p instanceof VarianceInterpreter;
    }

    private double smallVariance() {
        if (concentratedLikelihood) {
            return 1e-6;
        } else {
            return 1e-6 * model.maxVariance(fullp);
        }
    }

    private DoubleSeq rescaleVariances(DoubleSeq q, double factor) {
        return model.rescale(factor, q, p -> p instanceof VarianceInterpreter && !p.isFixed());
    }

    private boolean fixSmallVariance() {
        List<VarianceInterpreter> svar = model.smallVariances(fullp, smallVariance());
        if (svar.isEmpty()) {
            return false;
        }
        double dll = 0;
        VarianceInterpreter cur = null;
        for (VarianceInterpreter small : svar) {
            double olde = small.fixStde(0);
            try {
                DoubleSeq p = model.functionParameters(fullp);
                Likelihood nll = function(concentratedLikelihood).evaluate(p).getLikelihood();
                double ndll = nll.logLikelihood() - ll.logLikelihood();
                if (ndll > dll) {
                    dll = ndll;
                    cur = small;
                }
            } catch (Exception err) {
            }
            small.freeStde(olde);
        }
        if (cur != null) {
            cur.fixStde(0);
            smallVariances.add(cur);
            return true;
        } else {
            return false;
        }

    }

    /**
     * @return the data
     */
    public FastMatrix getData() {
        return data;
    }

    /**
     * @return the model
     */
    public MstsMapping getModel() {
        return model;
    }

    /**
     * @return the ssf
     */
    public MultivariateCompositeSsf getSsf() {
        return ssf;
    }

    /**
     * @return the prslts
     */
    public DoubleSeq getParameters() {
        return model.functionParameters(fullp);
    }

    /**
     * @return the ll
     */
    public Likelihood getLikelihood() {
        return ll;
    }

    public DoubleSeq fullParameters() {
        return fullp;
    }
}
