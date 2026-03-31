/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.r;

import jdplus.sts.base.core.Utility;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.sts.base.core.msts.CompositeModel;
import jdplus.sts.base.core.msts.MstsMonitor;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.ssf.dk.DefaultDiffuseFilteringResults;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.toolkit.base.core.ssf.composite.MultivariateCompositeSsf;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.sts.base.core.msts.MstsMapping;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.ssf.akf.AugmentedFilter;
import jdplus.toolkit.base.core.ssf.akf.AugmentedFilterInitializer;
import jdplus.toolkit.base.core.ssf.akf.DefaultAugmentedFilteringResults;
import jdplus.toolkit.base.core.ssf.akf.QAugmentation;
import jdplus.toolkit.base.core.ssf.akf.QPredictionErrorDecomposition;
import jdplus.toolkit.base.core.ssf.ckms.CkmsFilter;
import jdplus.toolkit.base.core.ssf.dk.DiffusePredictionErrorDecomposition;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;
import jdplus.toolkit.base.core.ssf.univariate.PredictionErrorDecomposition;
import jdplus.toolkit.base.core.stats.likelihood.Likelihood;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Algorithms {

    public double[] filter(ISsf model, double[] data, boolean normalized, boolean clean) {
        SsfData s = new SsfData(data);
        DefaultDiffuseFilteringResults rslt = DkToolkit.filter(model, s, false);
        return rslt.errors(normalized, clean).toArray();
    }

    public double[] sqrtFilter(ISsf model, double[] data, boolean normalized, boolean clean) {
        SsfData s = new SsfData(data);
        DefaultDiffuseSquareRootFilteringResults rslt = DkToolkit.sqrtFilter(model, s, false);
        return rslt.errors(normalized, clean).toArray();
    }

    public double[] akfFilter(ISsf model, double[] data, String qtype, boolean normalized, boolean clean) {
        QAugmentation.QType q = QAugmentation.QType.valueOf(qtype);
        SsfData s = new SsfData(data);
        DefaultAugmentedFilteringResults rslt = Utility.filter(model, s, true, q);
        return rslt.errors(normalized, clean).toArray();
    }

    public double diffuseLikelihood(ISsf model, double[] data) {
        try {
            SsfData s = new SsfData(data);
            DiffuseLikelihood dll = DkToolkit.likelihood(model, s, true, false);
            return dll.logLikelihood();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    public DiffuseLikelihood akfLikelihood(ISsf model, double[] d, String qtype, boolean collapsing, boolean scalingfactor) {
        QAugmentation.QType q = QAugmentation.QType.valueOf(qtype);
        QPredictionErrorDecomposition pe = new QPredictionErrorDecomposition(q, true);
        SsfData data = new SsfData(d);
        pe.prepare(model, data.length());
        if (!collapsing) {
            AugmentedFilter akf = new AugmentedFilter();
            if (!akf.process(model, data, pe)) {
                return null;
            }
        } else {
            AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(pe);
            OrdinaryFilter of = new OrdinaryFilter(initializer);
            if (!of.process(model, data, pe)) {
                return null;
            }
        }
        return pe.likelihood(scalingfactor);
    }

    public DiffuseLikelihood dkLikelihood(ISsf model, double[] d, boolean sqrt, boolean scalingfactor) {
        DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(true);
        SsfData data = new SsfData(d);
        pe.prepare(model, data.length());
        return DkToolkit.likelihoodComputer(sqrt, scalingfactor, true).compute(model, data);
    }

    public Likelihood ckmsLikelihood(ISsf model, double[] d, boolean scalingfactor) {
        PredictionErrorDecomposition pe = new PredictionErrorDecomposition(true);
        SsfData data = new SsfData(d);
        pe.prepare(model, data.length());
        CkmsFilter filter = new CkmsFilter();
        if (!filter.process(model, data, pe)) {
            return null;
        }
        return pe.likelihood(scalingfactor);
    }

    public Matrix smooth(ISsf model, double[] data, boolean all, String qtype) {
        QAugmentation.QType q = QAugmentation.QType.valueOf(qtype);
        SsfData s = new SsfData(data);
        DefaultSmoothingResults sr = Utility.smooth(model, s, all, true, q);
        int m = model.getStateDim();
        FastMatrix ss = FastMatrix.make(s.length(), all ? 2 * m : m);

        for (int i = 0; i < m; ++i) {
            ss.column(i).copy(sr.getComponent(i));
        }
        if (all) {
            for (int i = 0, j = m; i < m; ++i, j++) {
                ss.column(j).copy(sr.getComponentVariance(i).fastOp(x -> x <= 0 ? 0 : Math.sqrt(x)));
            }
        }
        return ss;
    }

    public double diffuseLikelihood(IMultivariateSsf model, FastMatrix data) {
        try {
            SsfMatrix s = new SsfMatrix(data);
            DiffuseLikelihood dll = DkToolkit.likelihood(model, s, true, false);
            return dll.logLikelihood();
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    public StateStorage smooth(IMultivariateSsf model, FastMatrix data, boolean all) {
        SsfMatrix s = new SsfMatrix(data);
        return DkToolkit.smooth(model, s, all, true);
    }

    public double diffuseLikelihood(CompositeModel model, FastMatrix data, double[] parameters) {
        MstsMapping mapping = model.mapping();
        MultivariateCompositeSsf mssf = mapping.map(Doubles.of(parameters));
        DiffuseLikelihood likelihood = DkToolkit.likelihood(mssf, new SsfMatrix(data), true, false);
        return likelihood.logLikelihood();
    }

    public double[] estimate(CompositeModel model, FastMatrix data) {
        MstsMapping mapping = model.mapping();
        MstsMonitor monitor = MstsMonitor.builder()
                .build();
        monitor.process(data, mapping, null);
        return monitor.fullParameters().toArray();
    }
}
