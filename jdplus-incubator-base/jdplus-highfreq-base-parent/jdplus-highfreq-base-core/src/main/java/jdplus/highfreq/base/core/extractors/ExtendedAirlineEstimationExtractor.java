/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.base.core.extractors;

import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineEstimation;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.modelling.OutlierDescriptor;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class ExtendedAirlineEstimationExtractor extends InformationMapping<ExtendedAirlineEstimation> {

    @Override
    public Class<ExtendedAirlineEstimation> getSourceClass() {
        return ExtendedAirlineEstimation.class;
    }

    private static final String PARAMETERS = "parameters", LL = "likelihood", RESIDUALS = "residuals", PCOV = "pcov", SCORE = "score", LOG = "log", MISSING = "missing",
            B = "b", T = "t", BVAR = "bvar", OUTLIERS = "outliers", LIN = "lin", REGRESSORS = "regressors", Y = "y", BNAMES = "variables", COMPONENT_AO = "component_ao", COMPONENT_LS = "component_ls", COMPONENT_WO = "component_wo", COMPONENT_OUTLIERS = "component_outliers", COMPONENT_USERDEF_REG_VARIABLES = "component_userdef_reg_variables";

    public ExtendedAirlineEstimationExtractor() {
        delegate(LL, LikelihoodStatistics.class, r -> r.getLikelihood());
        set(LOG, Boolean.class, source -> source.isLog());
        set(MISSING, int[].class, source -> source.getMissing());
        set(RESIDUALS, double[].class, source -> source.getResiduals().toArray());
        set(PCOV, Matrix.class, source -> source.getParametersCovariance());
        set(PARAMETERS, double[].class, source -> source.getParameters().toArray());
        set(SCORE, double[].class, source -> source.getScore().toArray());
        set(B, double[].class, source -> source.getCoefficients().toArray());
        set(T, double[].class, source -> source.tstats());
        set(BVAR, Matrix.class, source -> source.getCoefficientsCovariance());
        set(OUTLIERS, String[].class, source -> {
            OutlierDescriptor[] o = source.getOutliers();
            if (o == null) {
                return null;
            }
            String[] no = new String[o.length];
            for (int i = 0; i < o.length; ++i) {
                no[i] = o[i].toString();
            }
            return no;
        });
        set(REGRESSORS, Matrix.class, source -> source.getX());
        set(LIN, double[].class, source -> source.linearized());
        set(COMPONENT_AO, double[].class, source -> source.component_ao());
        set(COMPONENT_LS, double[].class, source -> source.component_ls());
        set(COMPONENT_WO, double[].class, source -> source.component_wo());
        set(COMPONENT_OUTLIERS, double[].class, source -> source.component_outliers());
        set(COMPONENT_USERDEF_REG_VARIABLES, double[].class, source -> source.component_userdef_reg_variables());
        set(Y, double[].class, source -> source.getY());
        set(BNAMES, String[].class, source -> {
            int nx = source.getNx();
            if (nx == 0) {
                return null;
            }
            String[] names = new String[nx];
            OutlierDescriptor[] outliers = source.getOutliers();
            int no = outliers == null ? 0 : outliers.length;
            for (int i = 0; i < nx - no; ++i) {
                names[i] = "x-" + (i + 1);
            }
            for (int i = nx - no, j = 0; i < nx; ++i, ++j) {
                names[i] = outliers[j].toString();
            }

            return names;
        });

    }
}
