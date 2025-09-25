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

import java.util.function.IntToDoubleFunction;
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
@lombok.Value
public class LtdArimaMapping1 implements LtdArimaMapping {

    private final int n;
    @lombok.With
    private final SarimaOrders orders;
    private final boolean vPhi, vBphi, vTheta, vBtheta, vVar;
    private final double eps, epsVar;

//    static final double MAX = 0.99999;
//    public static final double STEP = Math.pow(2.220446e-16, 0.5), EVAR = 1e-6;
    public static Builder builder(SarimaOrders orders) {
        Builder builder = new Builder();
        builder.orders(orders)
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
    public LtdArimaModel map(DoubleSeq ds) {
        int np = orders.getParametersCount();
        double[] pmodels = pmodels(ds);
        return LtdArimaModel.builder()
                .n(n)
                .spec(orders)
                .p0(DoubleSeq.of(pmodels, 0, np))
                .p1(DoubleSeq.of(pmodels, np, np))
                .var1(pmodels[pmodels.length - 1])
                .build();
    }

    public int deltaParamsCount() {
        int dn = 0;
        if (vPhi) {
            dn += orders.getP();
        }
        if (vBphi) {
            dn += orders.getBp();
        }
        if (vTheta) {
            dn += orders.getQ();
        }
        if (vBtheta) {
            dn += orders.getBq();
        }
        return dn;
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        SarimaMapping mapping = new SarimaMapping(orders, eps, true);
        DoubleSeq p0 = mapping.getDefaultParameters();
        int ns = deltaParamsCount();
        if (ns == 0 && !vVar) {
            return p0;
        }
        int np = p0.length() + ns;
        if (vVar) {
            ++np;
        }
        double[] p = new double[np];
        p0.copyTo(p, 0);
//        if (vVar) {
//            p[p.length - 1] = 0;
//        }
        return DoubleSeq.of(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inParams) {
        SarimaMapping mapping = new SarimaMapping(orders, eps, false);
        int np = orders.getParametersCount();
        double[] p = pmodels(inParams);
        if (!mapping.checkBoundaries(DoubleSeq.of(p, 0, np))) {
            return false;
        }
        if (!mapping.checkBoundaries(DoubleSeq.of(p, np, np))) {
            return false;
        }
        return p[2 * np] >= -1;
    }

    @Override
    public ParamValidation validate(DataBlock ioParams) {
        SarimaMapping mapping = new SarimaMapping(orders, eps, true);
        int np = orders.getParametersCount();
        int nd = deltaParamsCount();
        // step 1: we validate the mean
        ParamValidation v0 = mapping.validate(ioParams.range(0, np));

        boolean changed = v0 == ParamValidation.Changed;
        if (vVar) {
            double v = ioParams.getLast();
            if (v < -1) {
                double w = -1 / (v + 1); // new var
                ioParams.setLast(w - 1);
                changed = true;
            }
        }

        // adapt delta if need be
//        double alpha = 1, mu = .5;
//        DoubleSeq mean = mean(ioParams);
//        DoubleSeq delta = delta(ioParams);
        double[] pm = pmodels(ioParams);

        ParamValidation v1 = mapping.validate(DataBlock.of(pm, 0, np));
        ParamValidation v2 = mapping.validate(DataBlock.of(pm, np, 2 * np));

        if (v1 != ParamValidation.Valid || v2 != ParamValidation.Valid) {
            // ok with the new mean
            changed = true;
            // set new means
            ioParams.range(0, np).set(i -> (pm[i] + pm[i + np]) / 2);
            // set new deltas
            setDelta(ioParams.drop(np, 0), i -> pm[i + np] - pm[i]);
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

//    @Override
//    public ParamValidation validate(DataBlock ioParams) {
//        SarimaMapping mapping = new SarimaMapping(orders, eps, true);
//        int np = orders.getParametersCount();
//        int nd = deltaParamsCount();
//        // step 1: with validate the mean
//        ParamValidation v0 = mapping.validate(ioParams.range(0, np));
//
//        boolean changed = v0 == ParamValidation.Changed;
//        if (vVar) {
//            double v = ioParams.getLast();
//            if (v < 0) {
//                ioParams.setLast(-1 / v);
//                changed = true;
//            }
//        }
//
//        // adapt delta if need be
//        double alpha = 1, mu = .5;
//        DoubleSeq mean = mean(ioParams);
//        DoubleSeq delta = delta(ioParams);
//
//        int iter = 0;
//        do {
//            double c = alpha;
//            DoubleSeq p0 = DoubleSeq.onMapping(np, i -> mean.get(i) - c * delta.get(i) / 2);
//            DoubleSeq p1 = DoubleSeq.onMapping(np, i -> mean.get(i) + c * delta.get(i) / 2);
//            if (mapping.checkBoundaries(p0) && mapping.checkBoundaries(p1)) {
//                if (iter == 0) {
//                    return v0;
//                } else {
//                    ioParams.range(np, np + nd).mul(c);
//                    return ParamValidation.Changed;
//                }
//
//            } else {
//                alpha *= mu;
//            }
//        } while (iter++ < 3);
//        double[] pm = pmodels(ioParams);
//        ParamValidation v1 = mapping.validate(DataBlock.of(pm, 0, np));
//        ParamValidation v2 = mapping.validate(DataBlock.of(pm, np, 2 * np));
//        if (v1 == ParamValidation.Invalid || v2 == ParamValidation.Invalid) {
//            return ParamValidation.Invalid;
//        }
//        if (v1 == ParamValidation.Changed || v2 == ParamValidation.Changed) {
//            changed = true;
//            // set new means
//            ioParams.range(0, np).set(i -> (pm[i] + pm[i + np]) / 2);
//            // set new deltas
//            setDelta(ioParams.drop(np, 0), i -> pm[i + np] - pm[i]);
//        }
//        return changed ? ParamValidation.Changed : ParamValidation.Valid;
//    }
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
                    return -2 * MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (vBphi && orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return -2 * MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (vTheta && orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return -2 * MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        }
        if (vBtheta && orders.getBq() > 0) {
            if (idx < orders.getBq()) {
                if (orders.getBq() == 1) {
                    return -2 * MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        }
        // var
        return -1;
        //return Double.NEGATIVE_INFINITY;
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
                    return 2 * MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (vBphi && orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return 2 * MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (vTheta && orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return 2 * MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
        if (vBtheta && orders.getBq() > 0) {
            if (idx < orders.getBq()) {
                if (orders.getBq() == 1) {
                    return 2 * MAX;
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
                for (int i = 0; i < p; ++i) {
                    double m = cp.get(i);
                    double d = dp.get(i) / 2;
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
                for (int i = 0; i < bp; ++i) {
                    double m = cp.get(i);
                    double d = dp.get(i) / 2;
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
                for (int i = 0; i < q; ++i) {
                    double m = cp.get(i);
                    double d = dp.get(i) / 2;
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
            DoubleSeq cp = pall.extract(ip, bq);
            if (vBtheta) {
                DoubleSeq dp = pall.extract(istep, bq);
                for (int i = 0; i < bq; ++i) {
                    double m = cp.get(i);
                    double d = dp.get(i) / 2;
                    pm[ip + i] = m - d;
                    pm[np + ip + i] = m + d;
                }
            } else {
                cp.copyTo(pm, ip);
                cp.copyTo(pm, ip + np);
            }
        }
        if (vVar) {
            double e = 1 + pall.get(pall.length() - 1);
            pm[2 * np] = e * e;
        }else{
            pm[2 * np] = 1;
            
        }
        return pm;
    }

    // p in mean-delta, meam+delta
    private DoubleSeq mean(DoubleSeq pall) {
        return pall.range(0, orders.getParametersCount());
    }

    private DoubleSeq delta(DoubleSeq pall) {
        int np = orders.getParametersCount();
        int nd = deltaParamsCount();
        if (np == nd) {
            return pall.range(np, 2 * np);
        }
        double[] dm = new double[np];
        int ip = 0, istep = np;
        int p = orders.getP();
        if (p > 0) {
            if (vPhi) {
                DoubleSeq dp = pall.extract(istep, p);
                for (int i = 0; i < p; ++i) {
                    double d = dp.get(i) / 2;
                    dm[ip + i] = d;
                }
                istep += p;
            }
            ip += p;
        }
        int bp = orders.getBp();
        if (bp > 0) {
            if (vBphi) {
                DoubleSeq dp = pall.extract(istep, bp);
                for (int i = 0; i < bp; ++i) {
                    double d = dp.get(i) / 2;
                    dm[ip + i] = d;
                }
                istep += bp;
            }
            ip += bp;
        }
        int q = orders.getQ();
        if (q > 0) {
            if (vTheta) {
                DoubleSeq dp = pall.extract(istep, q);
                for (int i = 0; i < q; ++i) {
                    double d = dp.get(i) / 2;
                    dm[ip + i] = d;
                }
                istep += q;
            }
            ip += q;
        }
        int bq = orders.getBq();
        if (bq > 0) {
            if (vBtheta) {
                DoubleSeq dp = pall.extract(istep, bq);
                for (int i = 0; i < bq; ++i) {
                    double d = dp.get(i) / 2;
                    dm[ip + i] = d;
                }
            }
        }
        return DoubleSeq.of(dm);
    }

    private void setDelta(DataBlock pdelta, IntToDoubleFunction delta) {
        if (pdelta.isEmpty()) {
            return;
        }
        int ip = 0, idelta = 0;
        int p = orders.getP();
        if (p > 0) {
            if (vPhi) {
                int start = ip;
                pdelta.extract(idelta, p).set(i -> delta.applyAsDouble(start + i));
                idelta += p;
            }
            ip += p;
        }
        int bp = orders.getBp();
        if (bp > 0) {
            if (vBphi) {
                int start = ip;
                pdelta.extract(idelta, bp).set(i -> delta.applyAsDouble(start + i));
                idelta += bp;
            }
            ip += bp;
        }
        int q = orders.getQ();
        if (q > 0) {
            if (vTheta) {
                int start = ip;
                pdelta.extract(idelta, q).set(i -> delta.applyAsDouble(start + i));
                idelta += q;
            }
            ip += q;
        }
        int bq = orders.getBq();
        if (bq > 0) {
            if (vBtheta) {
                int start = ip;
                pdelta.extract(idelta, bq).set(i -> delta.applyAsDouble(start + i));
            }
        }
    }

    @Override
    public DoubleSeq parametersOf(LtdArimaModel model) {
        int np = orders.getParametersCount(), nd = deltaParamsCount(), nv = vVar ? 1 : 0;
        double[] p = new double[np + nd + nv];
        DoubleSeq p0 = model.getP0();
        DoubleSeq p1 = model.getP1();
        int n = model.getN() - 1;
        for (int i = 0; i < np; ++i) {
            p[i] = (p0.get(i) + p1.get(i)) / 2;
        }
        DataBlock P = DataBlock.of(p, np, np + nd, 1);
        setDelta(P, i -> (p1.get(i) - p0.get(i)));
        if (vVar) {
            p[np + nd] = Math.sqrt(model.getVar1()) - 1;
        }
        return DoubleSeq.of(p);
    }
}
