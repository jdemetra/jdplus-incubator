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
import jdplus.advancedsa.base.api.tdarima.Orders;
import jdplus.advancedsa.base.api.tdarima.TdEstimationSpec;
import jdplus.advancedsa.base.api.tdarima.TdSarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
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
public class TdSarimaMapping1 implements TdSarimaMapping {

    private final int period;
    private final int n;
    private final int startArimaRamp, endArimaRamp, startVarianceRamp, endVarianceRamp;

    private final TdSarimaSpec spec;
    private final double eps, epsVar;
    private final TdEstimationSpec.Transformation fn;

    //    static final double MAX = 0.99999;
    //    public static final double STEP = Math.pow(2.220446e-16, 0.5), EVAR = 1e-6;
    public static Builder builder() {
        Builder builder = new Builder();
        builder.spec(TdSarimaSpec.DEFAULT)
                .fn(TdEstimationSpec.Transformation.LINEAR)
                .eps(STEP)
                .epsVar(EVAR);

        return builder;
    }

    @Override
    public TdSarimaModel map(DoubleSeq ds) {
        int np = spec.getOrders().getParametersCount();
        double[] pmodels = pmodels(ds);
        return TdSarimaModel.builder()
                .period(period)
                .n(n)
                .startArimaRamp(startArimaRamp)
                .endArimaRamp(endArimaRamp)
                .startVarianceRamp(startVarianceRamp)
                .endVarianceRamp(endVarianceRamp)
                .orders(spec.getOrders())
                .p0(DoubleSeq.of(pmodels, 0, np))
                .p1(DoubleSeq.of(pmodels, np, np))
                .var1(pmodels[pmodels.length - 1])
                .fn(fn)
                .build();
    }

    public int deltaParamsCount() {
        Orders orders = spec.getOrders();
        int dn = 0;
        if (spec.isVPhi()) {
            dn += orders.getP();
        }
        if (spec.isVBphi()) {
            dn += orders.getBp();
        }
        if (spec.isVTheta()) {
            dn += orders.getQ();
        }
        if (spec.isVBtheta()) {
            dn += orders.getBq();
        }
        return dn;
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        Orders orders = spec.getOrders();
        SarimaMapping mapping = new SarimaMapping(orders.asSarimaOrders(period), eps, true);
        DoubleSeq p0 = mapping.getDefaultParameters();
        int ns = deltaParamsCount();
        if (ns == 0 && !spec.isVVar()) {
            return p0;
        }
        int np = p0.length() + ns;
        if (spec.isVVar()) {
            ++np;
        }
        double[] p = new double[np];
        p0.copyTo(p, 0);
        //        if (spec.isVVar()) {
        //            p[p.length - 1] = 0;
        //        }
        return DoubleSeq.of(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inParams) {
        Orders orders = spec.getOrders();
        SarimaMapping mapping = new SarimaMapping(orders.asSarimaOrders(period), eps, false);
        int np = orders.getParametersCount();
        double[] p = pmodels(inParams);
        if (!mapping.checkBoundaries(DoubleSeq.of(p, 0, np))) {
            return false;
        }
        return mapping.checkBoundaries(DoubleSeq.of(p, np, np));
    }

    @Override
    public ParamValidation validate(DataBlock ioParams) {
        Orders orders = spec.getOrders();
        SarimaMapping mapping = new SarimaMapping(orders.asSarimaOrders(period), eps, true);
        int np = orders.getParametersCount();
        // step 1: we validate the mean
        ParamValidation v0 = mapping.validate(ioParams.range(0, np));

        boolean changed = v0 == ParamValidation.Changed;
        if (spec.isVVar()) {
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

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {

        if (spec.isVVar() && idx == inparams.length() - 1) {
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
        Orders orders = spec.getOrders();
        int dim = 0;
        int p = orders.getP();
        if (p > 0) {
            dim += spec.isVPhi() ? 2 * p : p;
        }
        int bp = orders.getBp();
        if (bp > 0) {
            dim += spec.isVBphi() ? 2 * bp : bp;
        }
        int q = orders.getQ();
        if (q > 0) {
            dim += spec.isVTheta() ? 2 * q : q;
        }
        int bq = orders.getBq();
        if (bq > 0) {
            dim += spec.isVBtheta() ? 2 * bq : bq;
        }
        if (spec.isVVar()) {
            ++dim;
        }
        return dim;
    }

    @Override
    public double lbound(int idx) {
        Orders orders = spec.getOrders();
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
        if (spec.isVPhi() && orders.getP() > 0) {
            if (idx < orders.getP()) {
                if (orders.getP() == 1) {
                    return -2 * MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (spec.isVBphi() && orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return -2 * MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (spec.isVTheta() && orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return -2 * MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        }
        if (spec.isVBtheta() && orders.getBq() > 0) {
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
    }

    @Override
    public double ubound(int idx) {
        Orders orders = spec.getOrders();
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
        if (spec.isVPhi() && orders.getP() > 0) {
            if (idx < orders.getP()) {
                if (orders.getP() == 1) {
                    return 2 * MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getP();
        }
        if (spec.isVBphi() && orders.getBp() > 0) {
            if (idx < orders.getBp()) {
                if (orders.getBp() == 1) {
                    return 2 * MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= orders.getBp();
        }
        if (spec.isVTheta() && orders.getQ() > 0) {
            if (idx < orders.getQ()) {
                if (orders.getQ() == 1) {
                    return 2 * MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
        if (spec.isVBtheta() && orders.getBq() > 0) {
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
        Orders orders = spec.getOrders();
        int np = orders.getParametersCount();
        double[] pm = new double[2 * np + 1];
        int ip = 0, istep = np;
        int p = orders.getP();
        if (p > 0) {
            DoubleSeq cp = pall.extract(ip, p);
            if (spec.isVPhi()) {
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
            if (spec.isVBphi()) {
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
            if (spec.isVTheta()) {
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
            if (spec.isVBtheta()) {
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
        if (spec.isVVar()) {
            double e = 1 + pall.get(pall.length() - 1);
            pm[2 * np] = e * e;
        } else {
            pm[2 * np] = 1;
        }
        return pm;
    }

    private void setDelta(DataBlock pdelta, IntToDoubleFunction delta) {
        Orders orders = spec.getOrders();
        if (pdelta.isEmpty()) {
            return;
        }
        int ip = 0, idelta = 0;
        int p = orders.getP();
        if (p > 0) {
            if (spec.isVPhi()) {
                int start = ip;
                pdelta.extract(idelta, p).set(i -> delta.applyAsDouble(start + i));
                idelta += p;
            }
            ip += p;
        }
        int bp = orders.getBp();
        if (bp > 0) {
            if (spec.isVBphi()) {
                int start = ip;
                pdelta.extract(idelta, bp).set(i -> delta.applyAsDouble(start + i));
                idelta += bp;
            }
            ip += bp;
        }
        int q = orders.getQ();
        if (q > 0) {
            if (spec.isVTheta()) {
                int start = ip;
                pdelta.extract(idelta, q).set(i -> delta.applyAsDouble(start + i));
                idelta += q;
            }
            ip += q;
        }
        int bq = orders.getBq();
        if (bq > 0) {
            if (spec.isVBtheta()) {
                int start = ip;
                pdelta.extract(idelta, bq).set(i -> delta.applyAsDouble(start + i));
            }
        }
    }

    @Override
    public DoubleSeq parametersOf(TdSarimaModel model) {
        Orders orders = spec.getOrders();
        int np = orders.getParametersCount(), nd = deltaParamsCount(), nv = spec.isVVar() ? 1 : 0;
        double[] p = new double[np + nd + nv];
        DoubleSeq p0 = model.getP0();
        DoubleSeq p1 = model.getP1();
        int n = model.getN() - 1;
        for (int i = 0; i < np; ++i) {
            p[i] = (p0.get(i) + p1.get(i)) / 2;
        }
        DataBlock P = DataBlock.of(p, np, np + nd, 1);
        setDelta(P, i -> (p1.get(i) - p0.get(i)));
        if (spec.isVVar()) {
            p[np + nd] = Math.sqrt(model.getVar1()) - 1;
        }
        return DoubleSeq.of(p);
    }
}
