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

import jdplus.advancedsa.base.api.tdarima.LtdSarimaSpec;
import jdplus.advancedsa.base.api.tdarima.LtdSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public interface LtdSarimaMapping extends IParametricMapping<LtdSarimaModel> {

    public int getN();

    public LtdSarimaSpec.Orders getOrders();

    static final double MAX = 0.99999;
    public static final double STEP = Math.pow(2.220446e-16, 0.5), EVAR = 1e-6;

    DoubleSeq parametersOf(LtdSarimaModel model);

    @lombok.ToString
    static class Parameters {

        double[] val;
        double[] dval;
        FastMatrix cov, dcov;

        Parameters(
                DoubleSeq p,
                FastMatrix pcov,
                int m,
                ParametersDetails details,
                LtdSpec.Parametrization parametrization) {
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
            // derived
            int nd = details.derivedpnames.length;
            int ndc = details.nv;
            dval = new double[nd];
            dcov = FastMatrix.square(nd);
            double m1 = m - 1;

            // compute also the full dcov: apply the same transformation on the columns and then on the rows
            FastMatrix R = FastMatrix.make(nd, details.np);
            if (parametrization == LtdSpec.Parametrization.MEAN_DELTA) {
                int j = 0;
                for (int i = 0; i < ndc; ++i, j += 2) {
                    int k = details.iderived[i];
                    dval[j] = val[k] - val[k + 1] / 2;
                    dval[j + 1] = val[k] + val[k + 1] / 2;
                    R.set(j, k, 1);
                    R.set(j, k + 1, -0.5);
                    R.set(j + 1, k, 1);
                    R.set(j + 1, k + 1, 0.5);
                }
                if (j < nd) {
                    R.set(j, details.np - 1, 1);
                    dval[j] = val[details.np - 1];
                }
            } else {
                int j = 0;
                for (int i = 0; i < ndc; ++i, j += 2) {
                    int k = details.iderived[i];
                    dval[j] = (val[k] + val[k + 1]) / 2;
                    dval[j + 1] = val[k + 1] - val[k];
                    R.set(j, k, 0.5);
                    R.set(j, k + 1, 0.5);
                    R.set(j + 1, k, -1);
                    R.set(j + 1, k + 1, 1);
                }
                if (j < nd) {
                    dval[j] = val[details.np - 1];
                    R.set(j, details.np - 1, 1);
                }
            }
            dcov = SymmetricMatrix.XSXt(cov, R);

            // rescale and update stde(last)
            for (int i = 0; i < v.length; ++i) {
                if (details.torescale[i]) {
                    val[i] /= m1;
                    cov.column(i).div(m1);
                    cov.row(i).div(m1);
                }
            }
            if (parametrization == LtdSpec.Parametrization.START_END) {
                for (int i = 1; i < nd; i += 2) {
                    dval[i] /= m1;
                    dcov.column(i).div(m1);
                    dcov.row(i).div(m1);
                }
                if (nd % 2 == 1) {
                    dval[nd - 1] /= m1;
                    dcov.column(nd - 1).div(m1);
                    dcov.row(nd - 1).div(m1);
                    val[val.length - 1] += 1;
                }
            } else if (nd % 2 == 1) {
                dval[nd - 1] += 1;
            }
        }
    }

    @lombok.ToString
    static class ParametersDetails {

        final int n0, n1, np, nv;
        final String[] pnames, derivedpnames;
        final int[] preorder;
        final boolean[] torescale;
        final int[] iderived;

        // work indexes
        int i, di, k0, k1;

        ParametersDetails(LtdSarimaSpec spec, LtdSpec.Parametrization parametrization) {
            this.np = spec.parametersCount();
            n0 = spec.getOrders().getParametersCount();
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
            LtdSarimaSpec.Orders orders = spec.getOrders();
            int o = orders.getP();
            if (o > 0) {
                fillNames(parametrization, spec.isVPhi(), PHI, o);
                fillDerivedNames(parametrization, spec.isVPhi(), PHI, o);
            }
            o = orders.getBp();
            if (o > 0) {
                fillNames(parametrization, spec.isVBphi(), BPHI, o);
                fillDerivedNames(parametrization, spec.isVBphi(), BPHI, o);
            }
            o = orders.getQ();
            if (o > 0) {
                fillNames(parametrization, spec.isVTheta(), THETA, o);
                fillDerivedNames(parametrization, spec.isVTheta(), THETA, o);
            }
            o = orders.getBq();
            if (o > 0) {
                fillNames(parametrization, spec.isVBtheta(), BTHETA, o);
                fillDerivedNames(parametrization, spec.isVBtheta(), BTHETA, o);
            }
            fillVar(parametrization, spec.isVVar());
        }

        private void fillNames(LtdSpec.Parametrization pspec, boolean var, String pname, int o) {
            if (var) {
                if (pspec == LtdSpec.Parametrization.MEAN_DELTA) {
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

        private void fillDerivedNames(LtdSpec.Parametrization pspec, boolean var, String pname, int o) {
            if (var) {
                if (pspec == LtdSpec.Parametrization.MEAN_DELTA) {
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

        private void fillVar(LtdSpec.Parametrization pspec, boolean vVar) {
            if (vVar) {
                preorder[np - 1] = np - 1;
                if (pspec == LtdSpec.Parametrization.MEAN_DELTA) {
                    pnames[np - 1] = "stde-delta";
                    derivedpnames[n1 + nv - 1] = "stde-end[derived]";
                    torescale[i] = true;
                } else {
                    pnames[np - 1] = "stde-end";
                    derivedpnames[n1 + nv - 1] = "stde-delta[derived]";
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

    static final String PHI = "phi",
            BPHI = "bphi",
            THETA = "theta",
            BTHETA = "btheta",
            START = "start",
            END = "end",
            MEAN = "mean",
            DELTA = "delta",
            STDE = "stde",
            DERIVED = "[derived]";
}
