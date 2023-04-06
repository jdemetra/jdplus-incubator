/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.base.r;

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

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Algorithms {

    public double[] filter(ISsf model, double[] data) {
        SsfData s = new SsfData(data);
        DefaultDiffuseFilteringResults rslt = DkToolkit.filter(model, s, false);
        return rslt.errors().toArray();
    }

    public double[] sqrtFilter(ISsf model, double[] data) {
        SsfData s = new SsfData(data);
        DefaultDiffuseSquareRootFilteringResults rslt = DkToolkit.sqrtFilter(model, s, false);
        return rslt.errors().toArray();
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

    public StateStorage smooth(ISsf model, double[] data, boolean all) {
        SsfData s = new SsfData(data);
        return DkToolkit.sqrtSmooth(model, s, all, true);
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
    
    public double diffuseLikelihood(CompositeModel model, FastMatrix data, double[] parameters){
        MstsMapping mapping = model.mapping();
        MultivariateCompositeSsf mssf = mapping.map(Doubles.of(parameters));
        DiffuseLikelihood likelihood = DkToolkit.likelihood(mssf, new SsfMatrix(data), true, false);
        return likelihood.logLikelihood();
    }
    
    public double[] estimate(CompositeModel model, FastMatrix data){
        MstsMapping mapping = model.mapping();
        MstsMonitor monitor=MstsMonitor.builder()
                .build();
        monitor.process(data, mapping, null);
        return monitor.fullParameters().toArray();
    }
}
