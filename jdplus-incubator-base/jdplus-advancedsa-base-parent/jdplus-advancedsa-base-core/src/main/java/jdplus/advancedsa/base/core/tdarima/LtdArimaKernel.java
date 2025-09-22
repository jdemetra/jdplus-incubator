/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.advancedsa.base.core.tdarima;

import jdplus.advancedsa.base.api.tdarima.LtdArimaSpec;
import java.util.function.Function;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.api.timeseries.regression.ResidualsType;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.Chi2;
import jdplus.toolkit.base.core.dstats.F;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import static jdplus.toolkit.base.core.math.matrices.SymmetricMatrix.LtL;
import static jdplus.toolkit.base.core.math.matrices.SymmetricMatrix.lcholesky;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArmaModel;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
import jdplus.toolkit.base.core.stats.linearmodel.JointTest;
import jdplus.toolkit.base.core.stats.tests.NiidTests;
import jdplus.toolkit.base.core.stats.tests.TestsUtility;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Jean Palate
 */
public class LtdArimaKernel {

    private final LtdArimaSpec spec;
    private final ParametersDetails pdetails;

    public static LtdArimaKernel of(LtdArimaSpec spec) {
        return new LtdArimaKernel(spec);
    }

    /**
     * Raw estimation
     *
     * @param s variable
     * @param period periodicity
     * @param mean
     * @param X regression matrix. Can be null
     * @return
     */
    public LtdArimaResults process(DoubleSeq s, int period, boolean mean, @Nullable FastMatrix X) {

        SarimaOrders orders = spec.getSarimaSpec()
                .withPeriod(period)
                .orders();

        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder()
                .y(s)
                .meanCorrection(mean)
                .arima(SarimaModel.builder(orders).build())
                .addX(X)
                .build();

        SarimaMapping mapping = SarimaMapping.of(orders);
        RegArimaEstimation<SarimaModel> initial = RegSarimaComputer.PROCESSOR.process(regarima, mapping);

        ConcentratedLikelihoodWithMissing ll = initial.getConcentratedLikelihood();
        DoubleSeq coefficients0 = ll.coefficients();
        FastMatrix covariance0 = ll.covariance(mapping.getDim(), true);
        LikelihoodStatistics ll0 = initial.statistics();

        DoubleSeq e = ll.e();
        TsResiduals res0 = residuals(e, period, ll0, mapping.getDim(), ResidualsType.QR_Transformed);

        LtdArimaResults.SarimaResults.Builder builder0 = LtdArimaResults.SarimaResults.builder();
        LtdArimaResults.LtdResults.Builder builder1 = LtdArimaResults.LtdResults.builder();
        builder0.model(initial.getModel().arima())
                .parameters(initial.getMax().getParameters())
                .parametersCovariance(covariance(initial.getMax().getInformation(), false))
                .ll(ll0)
                .residuals(res0)
                .coefficients(coefficients0)
                .covariance(covariance0);

        RegArimaModel<SarimaModel> model = initial.getModel();
        RegArmaModel<SarimaModel> dmodel = model.differencedModel();
        SarmaOrders storders = orders.doStationary();
        int n = dmodel.getY().length();

        LtdArimaMapping ltdmapping = stmapping(storders, n);
        SsfFunction<LtdArimaModel, Ssf> fn = SsfFunction.<LtdArimaModel, Ssf>builder(new SsfData(dmodel.getY()), ltdmapping, lmodel -> lmodel.ssf())
                .regression(dmodel.getX().isEmpty() ? null : dmodel.getX(), 0)
                .useScalingFactor(true)
                .useLog(false)
                .useParallelProcessing(true)
                .useFastAlgorithm(false)
                .build();
        int dim = ltdmapping.getDim();

        LtdArimaModel m0 = LtdArimaModel.of(SarimaOrders.of(storders, 0, 0), dmodel.getArma().parameters(), n);
        DoubleSeq p = ltdmapping.parametersOf(m0);
        SsfFunctionPoint<LtdArimaModel, Ssf> pt = fn.evaluate(p);
        LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer.builder()
                .functionPrecision(spec.getPrecision())
                .build();
        min.minimize(pt);
        pt = (SsfFunctionPoint<LtdArimaModel, Ssf>) min.getResult();

        DiffuseConcentratedLikelihood likelihood = pt.getLikelihood();

        DoubleSeq coefficients1 = likelihood.coefficients();
        FastMatrix covariance1 = likelihood.covariance(dim, true);

        if (X != null && !X.isEmpty()) {
            DataBlock regs0 = DataBlock.make(s.length()), regs1 = DataBlock.make(s.length());
            regs0.product(regarima.variables().rowsIterator(), DataBlock.of(mean ? coefficients0.drop(0, 1) : coefficients0));
            builder0.regsEffect(regs0);
            builder0.linearizedSeries(DoublesMath.subtract(s, regs0));
            regs1.product(regarima.variables().rowsIterator(), DataBlock.of(mean ? coefficients1.drop(0, 1) : coefficients1));
            builder1.regsEffect(regs1);
            builder1.linearizedSeries(DoublesMath.subtract(s, regs1));

        } else {
            builder0.regsEffect(DoubleSeq.empty());
            builder0.linearizedSeries(s);
            builder1.regsEffect(DoubleSeq.empty());
            builder1.linearizedSeries(s);
        }

        LikelihoodStatistics ll1 = LikelihoodStatistics.statistics(likelihood.logLikelihood(), model.getObservationsCount() - model.getMissingValuesCount())
                .llAdjustment(0)
                .differencingOrder(model.arima().getNonStationaryArOrder())
                //                .diffuseOrder(likelihood.ndiffuse()+likelihood.ndiffuseRegressors())
                .parametersCount(dim + model.getVariablesCount() + 1)
                .ssq(likelihood.ssq())
                .build();

        TsResiduals res1 = residuals(e, period, ll1, dim, ResidualsType.OneStepAHead);

//        double[] gradient = min.gradientAtMinimum().toArray();
//        FastMatrix hessian = min.curvatureAtMinimum();
//        double objective = pt.getSsqE();
//        int ndf = likelihood.degreesOfFreedom();
//        hessian.mul((.5 * ndf) / objective);
//        for (int i = 0; i < gradient.length; ++i) {
//            gradient[i] *= (-.5 * ndf) / objective;
//        }
        LogLikelihoodFunction<LtdArimaModel, DiffuseConcentratedLikelihood> fll = concentratedLogLikelihoodFunction(dmodel);
//        LogLikelihoodFunction.Point max = new LogLikelihoodFunction.Point(fll, pt.getParameters(), DoubleSeq.of(gradient), hessian);
        LogLikelihoodFunction.Point<LtdArimaModel, DiffuseConcentratedLikelihood> max = fll.point(pt.getParameters());
        FastMatrix pcovariance = covariance(max.getInformation(), true);
        Parameters parameters = new Parameters(max.getParameters(), pcovariance, n, pdetails, spec.getParametrization());
        StatisticalTest test = null;
        if (pcovariance != null && spec.getParametrization() == LtdArimaSpec.Parametrization.MEAN_DELTA) {
            test = stationaryTest(max.getParameters().drop(pdetails.n0, 0),
                    pcovariance.extract(pdetails.n0, pdetails.n1, pdetails.n0, pdetails.n1), likelihood.degreesOfFreedom());
        }
        StatisticalTest lrtest = TestsUtility.testOf(2 * (likelihood.logLikelihood() - ll0.getLogLikelihood()),
                new Chi2(pdetails.n1), TestType.Upper);
        builder1.model((LtdArimaModel) pt.getCore())
                .coefficients(coefficients1)
                .covariance(covariance1)
                .residuals(res1)
                .ll(ll1)
                .parameters(DoubleSeq.of(parameters.val))
                .parametersCovariance(parameters.cov)
                .parametersNames(pdetails.pnames)
                .derivedParameters(DoubleSeq.of(parameters.dval))
                .derivedParametersStderr(DoubleSeq.of(parameters.edval))
                .derivedParametersNames(pdetails.derivedpnames)
                .stationaryTest(test)
                .likelihoodRatioTest(lrtest);
        return new LtdArimaResults(builder0.build(), builder1.build());
    }

    private TsResiduals residuals(DoubleSeq e, int period, LikelihoodStatistics ll, int nhp, ResidualsType type) {
        NiidTests niid = NiidTests.builder()
                .data(e)
                .period(period)
                .hyperParametersCount(nhp)
                .build();

        return TsResiduals.builder()
                .type(ResidualsType.OneStepAHead)
                .res(e)
                .ssq(ll.getSsqErr())
                .n(ll.getEffectiveObservationsCount())
                .df(ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount() + nhp)
                .dfc(ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount())
                .test(ResidualsDictionaries.MEAN, niid.meanTest())
                .test(ResidualsDictionaries.SKEW, niid.skewness())
                .test(ResidualsDictionaries.KURT, niid.kurtosis())
                .test(ResidualsDictionaries.DH, niid.normalityTest())
                .test(ResidualsDictionaries.LB, niid.ljungBox())
                .test(ResidualsDictionaries.BP, niid.boxPierce())
                .test(ResidualsDictionaries.SEASLB, niid.seasonalLjungBox())
                .test(ResidualsDictionaries.SEASBP, niid.seasonalBoxPierce())
                .test(ResidualsDictionaries.LB2, niid.ljungBoxOnSquare())
                .test(ResidualsDictionaries.BP2, niid.boxPierceOnSquare())
                .test(ResidualsDictionaries.NRUNS, niid.runsNumber())
                .test(ResidualsDictionaries.LRUNS, niid.runsLength())
                .test(ResidualsDictionaries.NUDRUNS, niid.upAndDownRunsNumbber())
                .test(ResidualsDictionaries.LUDRUNS, niid.upAndDownRunsLength())
                .build();

    }

    private LtdArimaKernel(LtdArimaSpec spec) {
        this.spec = spec;
        pdetails = new ParametersDetails(spec);
    }

    private LogLikelihoodFunction<LtdArimaModel, DiffuseConcentratedLikelihood>
            concentratedLogLikelihoodFunction(RegArmaModel<SarimaModel> dmodel) {
        Function<LtdArimaModel, ConcentratedLikelihood> lfn = m -> {

            SarmaOrders storders = dmodel.getArma().orders().doStationary();
            int n = dmodel.getY().length();

            LtdArimaMapping ltdmapping = stmapping(storders, n);
            SsfFunction<LtdArimaModel, Ssf> fn = SsfFunction.<LtdArimaModel, Ssf>builder(new SsfData(dmodel.getY()), ltdmapping, lmodel -> lmodel.ssf())
                    .regression(dmodel.getX().isEmpty() ? null : dmodel.getX(), 0)
                    .useScalingFactor(true)
                    .useLog(true)
                    .useParallelProcessing(true)
                    .useFastAlgorithm(false)
                    .useSymmetricNumericalDerivatives(true)
                    .build();
            DoubleSeq p = ltdmapping.parametersOf(m);
            SsfFunctionPoint pt = fn.evaluate(p);
            return pt.getLikelihood();
        };

        LtdArimaMapping ltdmapping = mapping(dmodel.getArma().orders(), dmodel.getY().length());

        return new LogLikelihoodFunction(ltdmapping, lfn);
    }

    private LtdArimaMapping stmapping(SarmaOrders storders, int n) {
        return mapping(SarimaOrders.of(storders, 0, 0), n, spec.getParametrization());
    }

    private LtdArimaMapping stmapping(SarmaOrders storders, int n, LtdArimaSpec.Parametrization parametrization) {
        return mapping(SarimaOrders.of(storders, 0, 0), n, parametrization);
    }

    private LtdArimaMapping mapping(SarimaOrders orders, int n) {
        return mapping(orders, n, spec.getParametrization());
    }

    private LtdArimaMapping mapping(SarimaOrders orders, int n, LtdArimaSpec.Parametrization parametrization) {
        LtdArimaMapping ltdmapping = null;

        if (parametrization == LtdArimaSpec.Parametrization.MEAN_DELTA) {
            ltdmapping = LtdArimaMapping1.builder(orders)
                    .n(n)
                    .vPhi(spec.isVPhi())
                    .vBphi(spec.isVBphi())
                    .vTheta(spec.isVTheta())
                    .vBtheta(spec.isVBtheta())
                    .vVar(spec.isVVar())
                    .build();
        } else if (parametrization == LtdArimaSpec.Parametrization.START_END) {
            ltdmapping = LtdArimaMapping2.builder(orders)
                    .n(n)
                    .vPhi(spec.isVPhi())
                    .vBphi(spec.isVBphi())
                    .vTheta(spec.isVTheta())
                    .vBtheta(spec.isVBtheta())
                    .vVar(spec.isVVar())
                    .build();
        }
        return ltdmapping;
    }

    public static FastMatrix covariance(FastMatrix H, boolean nullIfFailed) {
        try {
            FastMatrix lower = H.deepClone();
            lcholesky(lower);
            lower = LowerTriangularMatrix.inverse(lower);
            return LtL(lower);
        } catch (MatrixException e) {
            if (nullIfFailed) {
                return null;
            } else {
                FastMatrix I = FastMatrix.square(H.getRowsCount());
                I.set(Double.NaN);
                return I;
            }
        }
    }

    private static final String PHI = "phi", BPHI = "bphi", THETA = "theta", BTHETA = "btheta",
            START = "start", END = "end", MEAN = "mean", DELTA = "delta", DERIVED = "[derived]";

    @lombok.ToString
    static class ParametersDetails {

        final int n0, n1, np, nv;
        final String[] pnames, derivedpnames;
        final int[] preorder;
        final boolean[] torescale;
        final int[] iderived;

        // work indexes
        int i, di, k0, k1;

        ParametersDetails(LtdArimaSpec spec) {
            this.np = spec.parametersCount();
            n0 = spec.getSarimaSpec().parametersCount();
            n1 = np - n0;
            pnames = new String[np];
            nv = (spec.isVVar() ? n1 - 1 : n1);
            int nd = n1 + nv;
            derivedpnames = new String[nd];
            iderived = new int[nv];
            preorder = new int[np];
            torescale = new boolean[np];
            i = 0;
            di = 0;
            k0 = 0;
            k1 = n0;
            SarimaSpec sarimaSpec = spec.getSarimaSpec();
            LtdArimaSpec.Parametrization parametrization = spec.getParametrization();
            int o = sarimaSpec.getP();
            if (o > 0) {
                fillNames(parametrization, spec.isVPhi(), PHI, o);
                fillDerivedNames(parametrization, spec.isVPhi(), PHI, o);
            }
            o = sarimaSpec.getBp();
            if (o > 0) {
                fillNames(parametrization, spec.isVBphi(), BPHI, o);
                fillDerivedNames(parametrization, spec.isVBphi(), BPHI, o);
            }
            o = sarimaSpec.getQ();
            if (o > 0) {
                fillNames(parametrization, spec.isVTheta(), THETA, o);
                fillDerivedNames(parametrization, spec.isVTheta(), THETA, o);
            }
            o = sarimaSpec.getBq();
            if (o > 0) {
                fillNames(parametrization, spec.isVBtheta(), BTHETA, o);
                fillDerivedNames(parametrization, spec.isVBtheta(), BTHETA, o);
            }
            fillVar(parametrization, spec.isVVar());
        }

        private void fillNames(LtdArimaSpec.Parametrization pspec, boolean var, String pname, int o) {
            if (var) {
                if (pspec == LtdArimaSpec.Parametrization.MEAN_DELTA) {
                    for (int j = 0; j < o; ++j, ++k0, ++k1) {
                        iderived[di / 2] = i;
                        preorder[i] = k0;
                        pnames[i++] = pname(pname, MEAN, j + 1, false);
                        torescale[i] = true;
                        preorder[i] = k1;
                        pnames[i++] = pname(pname, DELTA, j + 1, false);
                    }
                } else {
                    for (int j = 0; j < o; ++j, ++k0, ++k1) {
                        iderived[di / 2] = i;
                        preorder[i] = k0;
                        pnames[i++] = pname(pname, START, j + 1, false);
                        preorder[i] = k1;
                        pnames[i++] = pname(pname, END, j + 1, false);
                    }
                }
            } else {
                for (int j = 0; j < o; ++j, ++k0) {
                    preorder[i] = k0;
                    pnames[i++] = pname(pname, null, j + 1, false);
                }
            }
        }

        private void fillDerivedNames(LtdArimaSpec.Parametrization pspec, boolean var, String pname, int o) {
            if (var) {
                if (pspec == LtdArimaSpec.Parametrization.MEAN_DELTA) {
                    for (int j = 0; j < o; ++j) {
                        derivedpnames[di++] = pname(pname, START, j + 1, false);
                        derivedpnames[di++] = pname(pname, END, j + 1, false);
                    }
                } else {
                    for (int j = 0; j < o; ++j) {
                        derivedpnames[di++] = pname(pname, MEAN, j + 1, false);
                        derivedpnames[di++] = pname(pname, DELTA, j + 1, false);
                    }
                }
            }
        }

        private void fillVar(LtdArimaSpec.Parametrization pspec, boolean vVar) {
            if (vVar) {
                preorder[np - 1] = np - 1;
                if (pspec == LtdArimaSpec.Parametrization.MEAN_DELTA) {
                    pnames[np - 1] = "var-delta";
                    derivedpnames[n1 + nv - 1] = "var-end[derived]";
                    torescale[i] = true;
                } else {
                    pnames[np - 1] = "var-end";
                    derivedpnames[n1 + nv - 1] = "var-delta[derived]";
                }
            }
        }

        private String pname(String prefix, String suffix, int lag, boolean derived) {
            StringBuilder builder = new StringBuilder();
            builder.append(prefix).append('(').append(lag).append(')');
            if (suffix != null) {
                builder.append('-').append(suffix);
            }
            if (derived) {
                builder.append(DERIVED);
            }
            return builder.toString();
        }
    }

    @lombok.ToString
    static class Parameters {

        double[] val;
        FastMatrix cov;

        double[] dval;
        double[] edval;

        Parameters(DoubleSeq p, FastMatrix pcov, int m, ParametersDetails details, LtdArimaSpec.Parametrization parametrization) {
            double[] v = p.toArray();
            val = new double[v.length];
            cov = FastMatrix.square(v.length);
            // reorder
            for (int i = 0; i < v.length; ++i) {
                val[i] = v[details.preorder[i]];
                if (pcov != null) {
                    for (int j = 0; j < v.length; ++j) {
                        cov.set(i, j, pcov.get(details.preorder[i], details.preorder[j]));
                    }
                } else {
                    cov.set(Double.NaN);
                }
            }
            //derived
            int nd = details.derivedpnames.length;
            int ndc = details.nv;
            dval = new double[nd];
            edval = new double[nd];
            double m1 = m - 1;

            if (parametrization == LtdArimaSpec.Parametrization.MEAN_DELTA) {
                int j = 0;
                for (int i = 0; i < ndc; ++i) {
                    int k = details.iderived[i];
                    dval[j] = val[k] - val[k + 1] / 2;
                    double var = cov.get(k, k) + cov.get(k + 1, k + 1) / 4 - cov.get(k, k + 1);
                    edval[j++] = var > 0 ? Math.sqrt(var) : Double.NaN;
                    dval[j] = val[k] + val[k + 1] / 2;
                    var = cov.get(k, k) + cov.get(k + 1, k + 1) / 4 + cov.get(k, k + 1);
                    edval[j++] = var > 0 ? Math.sqrt(var) : Double.NaN;
                }
                if (j < nd) {
                    dval[j] = 1 + val[j];
                    double var = cov.get(j, j);
                    edval[j] = var > 0 ? Math.sqrt(var) : Double.NaN;
                }
            } else {
                int j = 0;
                for (int i = 0; i < ndc; ++i) {
                    int k = details.iderived[i];
                    dval[j] = (val[k] + val[k + 1]) / 2;
                    double var = (cov.get(k, k) + cov.get(k + 1, k + 1) + 2 * cov.get(k, k + 1)) / 4;
                    edval[j++] = var > 0 ? Math.sqrt(var) : Double.NaN;
                    dval[j] = val[k + 1] - val[k];
                    var = cov.get(k, k) + cov.get(k + 1, k + 1) - 2 * cov.get(k, k + 1);
                    edval[j++] = var > 0 ? Math.sqrt(var) : Double.NaN;
                }
                if (j < nd) {
                    dval[j] = val[j] - 1;
                    double var = cov.get(j, j);
                    edval[j] = var > 0 ? Math.sqrt(var) : Double.NaN;
                    if (parametrization == LtdArimaSpec.Parametrization.START_END) {
                        dval[j] /= m1;
                        edval[j] /= m1;
                    }
                }
            }
            //rescale
            for (int i = 0; i < v.length; ++i) {
                if (details.torescale[i]) {
                    val[i] /= m1;
                    cov.column(i).div(m1);
                    cov.row(i).div(m1);
                }
            }
            if (parametrization == LtdArimaSpec.Parametrization.START_END) {
                for (int i = 1; i < dval.length; i += 2) {
                    dval[i] /= m1;
                    edval[i] /= m1;
                }
            }

        }
    }

    private StatisticalTest stationaryTest(DoubleSeq z, FastMatrix cov, int df) {
        FastMatrix V = cov.deepClone();
        DataBlock Z = DataBlock.of(z);
        SymmetricMatrix.lcholesky(V);
        LowerTriangularMatrix.solveLx(V, Z);
        double f = (Z.ssq() / Z.length());
        F fdist = new F(Z.length(), df);
        return TestsUtility.testOf(f, fdist, TestType.Upper);

    }
}
