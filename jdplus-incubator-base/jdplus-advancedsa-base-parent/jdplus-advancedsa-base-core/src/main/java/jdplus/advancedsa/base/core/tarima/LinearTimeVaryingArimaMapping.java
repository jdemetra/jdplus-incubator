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
package jdplus.advancedsa.base.core.tarima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;

/**
 * The parameters are organized as follows: p_mean for all sarima parameters,
 * p_step for all variable sarima parameters (phi, bphi, theta, btheta), true
 * signs + var_end (var_start = 1)
 *
 * @author Jean Palate
 */
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class LinearTimeVaryingArimaMapping implements IParametricMapping<LinearTimeVaryingArimaModel> {

    @lombok.With
    private final SarimaOrders orders;
    private final int n;
    private final boolean vPhi, vBphi, vTheta, vBtheta, vVar;
    private final double eps, epsVar;

    static final double MAX = 0.99999;
    public static final double STEP = Math.pow(2.220446e-16, 0.5), EVAR = 1e-6;

    public static Builder builder(SarimaOrders orders) {
        Builder builder = new Builder();
        builder.orders(orders)
                .n(0)
                .vPhi(false)
                .vBphi(false)
                .vTheta(false)
                .vBtheta(false)
                .vVar(false)
                .eps(STEP)
                .epsVar(EVAR);

        return builder;
    }

    @Override
    public LinearTimeVaryingArimaModel map(DoubleSeq ds) {
        int np=orders.getParametersCount();
        double[] pmodels = pmodels(ds);
        return LinearTimeVaryingArimaModel.builder()
                .spec(orders)
                .p0(DoubleSeq.of(pmodels, 0, np))
                .p1(DoubleSeq.of(pmodels, np, np))
                .var1(pmodels[pmodels.length-1])
                .build();
    }
    
    private int stepParamsCount(){
        int n=0;
        if (vPhi)
            n+=orders.getP();
        if (vBphi)
            n+=orders.getBp();
        if (vTheta)
            n+=orders.getQ();
        if (vBtheta)
            n+=orders.getBq();
        return n;
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        SarimaMapping mapping = new SarimaMapping(orders, eps, true);
        DoubleSeq p0 = mapping.getDefaultParameters();
        int ns=stepParamsCount();
        if (ns == 0 && ! vVar)
            return p0;
        int np =p0.length()+ns;
        if (vVar)
            ++np;
        double[] p=new double[np];
        p0.copyTo(p, 0);
        if (vVar)
            p[p.length-1]=1;
        return DoubleSeq.of(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inParams) {
        SarimaMapping mapping = new SarimaMapping(orders, eps, true);
        int np = orders.getParametersCount();
        double[] p = pmodels(inParams);
        if (!mapping.checkBoundaries(DoubleSeq.of(p, 0, np))) {
            return false;
        }
        if (!mapping.checkBoundaries(DoubleSeq.of(p, np, np))) {
            return false;
        }
        return p[2 * np] >= 0;
    }

    @Override
    public ParamValidation validate(DataBlock ioParams) {
        SarimaMapping mapping = new SarimaMapping(orders, eps, true);
        int np = orders.getParametersCount();
        double[] pm = pmodels(ioParams);
        ParamValidation v0 = mapping.validate(DataBlock.of(pm, 0, np));
        ParamValidation v1 = mapping.validate(DataBlock.of(pm, np, 2 * np));
        if (v0 == ParamValidation.Invalid || v1 == ParamValidation.Invalid) {
            return ParamValidation.Invalid;
        }
        boolean changed = false;
        if (v0 == ParamValidation.Changed || v1 == ParamValidation.Changed) {
            changed = true;
            // set new means
            for (int i = 0; i < np; ++i) {
                ioParams.set(i, (pm[i] + pm[i + np]) / 2);
            }
            // set delta
            int ip = 0, istep = np;
            int p = orders.getP();
            if (p > 0) {
                if (vPhi) {
                    for (int i = 0; i < p; ++i) {
                        ioParams.set(np + istep, (pm[np + ip + i] - pm[ip + i]) / (n - 1));
                    }
                    istep += p;
                }
                ip += p;
            }
            int bp = orders.getBp();
            if (bp > 0) {
                if (vBphi) {
                    for (int i = 0; i < bp; ++i) {
                        ioParams.set(np + istep, (pm[np + ip + i] - pm[ip + i]) / (n - 1));
                    }
                    istep += bp;
                }
                ip += bp;
            }
            int q = orders.getQ();
            if (q > 0) {
                if (vTheta) {
                    for (int i = 0; i < q; ++i) {
                        ioParams.set(np + istep, (pm[np + ip + i] - pm[ip + i]) / (n - 1));
                    }
                    istep += q;
                }
                ip += q;
            }
            int bq = orders.getBq();
            if (bq > 0) {
                if (vBtheta) {
                    for (int i = 0; i < q; ++i) {
                        ioParams.set(np + istep, (pm[np + ip + i] - pm[ip + i]) / (n - 1));
                    }
                }
            }
        }
        if (vVar) {
            double v = ioParams.getLast();
            if (v < 0) {
                ioParams.setLast(-1 / v);
                changed = true;
            }
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {

        if (vVar && idx == inparams.length() - 1) {
            return epsVar;
        }
        double p = inparams.get(idx);
        if (p < 0) {
            return eps * Math.max(1, -p);
        } else {
            return -eps * Math.max(1, p);
        }
    }

    @Override
    public int getDim() {
        int dim = 0;
        int p = orders.getP();
        if (p > 0) {
            dim += vPhi ? 2 * p : p;
        }
        int bp = orders.getBp();
        if (bp > 0) {
            dim += vBphi ? 2 * bp : bp;
        }
        int q = orders.getQ();
        if (q > 0) {
            dim += vTheta ? 2 * q : q;
        }
        int bq = orders.getBq();
        if (bq > 0) {
            dim += vBtheta ? 2 * bq : bq;
        }
        if (vVar) {
            ++dim;
        }
        return dim;
    }

    @Override
    public double lbound(int idx) {
        if (orders.getP() > 0) {
            if (idx < orders.getP()) {
                if (orders.getP() == 1) {
                    return -MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return -MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return -MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        }
        if (orders.getBq() > 0) {
            if (idx < orders.getBq()) {
                if (orders.getBq() == 1) {
                    return -MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getBq();
        }
        if (vPhi && orders.getP() > 0) {
            if (idx < orders.getP()) {
                if (orders.getP() == 1) {
                    return -2 * MAX / (n - 1);
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (vBphi && orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return -2 * MAX / (n - 1);
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (vTheta && orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return -2 * MAX / (n - 1);
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        }
        if (vBtheta && orders.getBq() > 0) {
            if (idx < orders.getBq()) {
                if (orders.getBq() == 1) {
                    return -2 * MAX / (n - 1);
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        }
        // var
        return 0;
    }

    @Override
    public double ubound(int idx) {
        if (orders.getP() > 0) {
            if (idx < orders.getP()) {
                if (orders.getP() == 1) {
                    return MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
        if (orders.getBq() > 0) {
            if (idx < orders.getBq()) {
                if (orders.getBq() == 1) {
                    return MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getBq();
        }
        if (vPhi && orders.getP() > 0) {
            if (idx < orders.getP()) {
                if (orders.getP() == 1) {
                    return 2 * MAX / (n - 1);
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (vBphi && orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return 2 * MAX / (n - 1);
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (vTheta && orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return 2 * MAX / (n - 1);
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
        if (vBtheta && orders.getBq() > 0) {
            if (idx < orders.getBq()) {
                if (orders.getBq() == 1) {
                    return 2 * MAX / (n - 1);
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
        // var
        return Double.POSITIVE_INFINITY;
    }

    private double[] pmodels(DoubleSeq pall) {
        int np = orders.getParametersCount();
        double[] pm = new double[2 * np + 1];
        int ip = 0, istep = np;
        int p = orders.getP();
        if (p > 0) {
            DoubleSeq cp = pall.extract(ip, p);
            if (vPhi) {
                DoubleSeq dp = pall.extract(istep, p);
                double s = .5 * (n - 1);
                for (int i = 0; i < p; ++i) {
                    double m = cp.get(i);
                    double d = dp.get(i) * s;
                    pm[ip + i] = m - d;
                    pm[np + ip + i] = m + d;
                }
                istep += p;
            } else {
                cp.copyTo(pm, ip);
                cp.copyTo(pm, ip + np);

            }
            ip += p;
        }
        int bp = orders.getBp();
        if (bp > 0) {
            DoubleSeq cp = pall.extract(ip, bp);
            if (vBphi) {
                DoubleSeq dp = pall.extract(istep, bp);
                double s = .5 * (n - 1);
                for (int i = 0; i < bp; ++i) {
                    double m = cp.get(i);
                    double d = dp.get(i) * s;
                    pm[ip + i] = m - d;
                    pm[np + ip + i] = m + d;
                }
                istep += bp;
            } else {
                cp.copyTo(pm, ip);
                cp.copyTo(pm, ip + np);
            }
            ip += bp;
        }
        int q = orders.getQ();
        if (q > 0) {
            DoubleSeq cp = pall.extract(ip, q);
            if (vTheta) {
                DoubleSeq dp = pall.extract(istep, q);
                double s = .5 * (n - 1);
                for (int i = 0; i < q; ++i) {
                    double m = cp.get(i);
                    double d = dp.get(i) * s;
                    pm[ip + i] = m - d;
                    pm[np + ip + i] = m + d;
                }
                istep += q;
            } else {
                cp.copyTo(pm, ip);
                cp.copyTo(pm, ip + np);
            }
            ip += q;
        }
        int bq = orders.getBq();
        if (bq > 0) {
            DoubleSeq cp = pall.extract(ip, q);
            if (vBtheta) {
                DoubleSeq dp = pall.extract(istep, bq);
                double s = .5 * (n - 1);
                for (int i = 0; i < bq; ++i) {
                    double m = cp.get(i);
                    double d = dp.get(i) * s;
                    pm[ip + i] = m - d;
                    pm[np + ip + i] = m + d;
                }
            } else {
                cp.copyTo(pm, ip);
                cp.copyTo(pm, ip + np);
            }
        }
        pm[2 * np] = vVar ? pall.get(pall.length() - 1) : 1;
        return pm;
    }

}
