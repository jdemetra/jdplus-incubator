/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.advancedsa.base.api.tdarima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import jdplus.toolkit.base.api.util.Validatable;
import lombok.NonNull;
import nbbrd.design.Development;

@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class LtdSarimaSpec implements Validatable<LtdSarimaSpec> {

    @Override
    public LtdSarimaSpec validate() throws IllegalArgumentException {
        if (validator != null) {
            validator.checkP(getP());
            validator.checkD(getD());
            validator.checkQ(getQ());
            validator.checkBp(getBp());
            validator.checkBd(getBd());
            validator.checkBq(getBq());
        }
        return this;
    }

    public static interface Validator {

        void checkP(int p);

        void checkD(int d);

        void checkQ(int q);

        void checkBp(int bp);

        void checkBd(int bd);

        void checkBq(int bq);
    }

    private Validator validator;
    /**
     * Period of the Arima model. Should be 0 when the period is unspecified
     */
    private int period;
    private int d, bd;
    private @NonNull
    Parameter[] phi, bphi, theta, btheta;
    Parameter[] dphi, dbphi, dtheta, dbtheta;
    Parameter dvar;
    

    // Defensive getters !
    public Parameter[] getPhi() {
        return phi == EMPTY ? EMPTY : phi.clone();
    }

    public Parameter[] getBphi() {
        return bphi == EMPTY ? EMPTY : bphi.clone();
    }

    public Parameter[] getTheta() {
        return theta == EMPTY ? EMPTY : theta.clone();
    }

    public Parameter[] getBtheta() {
        return btheta == EMPTY ? EMPTY : btheta.clone();
    }

    public Parameter[] getDeltaPhi() {
        return dphi == EMPTY ? EMPTY : dphi.clone();
    }

    public Parameter[] getDeltaBphi() {
        return dbphi == EMPTY ? EMPTY : dbphi.clone();
    }

    public Parameter[] getDeltaTheta() {
        return dtheta == EMPTY ? EMPTY : dtheta.clone();
    }

    public Parameter[] getDeltaBtheta() {
        return dbtheta == EMPTY ? EMPTY : dbtheta.clone();
    }

    private static final Parameter[] EMPTY = new Parameter[0];

    public static class Builder implements Validatable.Builder<LtdSarimaSpec> {

        private Builder() {
            phi = EMPTY;
            theta = EMPTY;
            bphi = EMPTY;
            btheta = EMPTY;
            dphi = EMPTY;
            dtheta = EMPTY;
            dbphi = EMPTY;
            dbtheta = EMPTY;
            dvar = Parameter.zero();
        }

        public Builder p(int value, boolean fixed) {
            phi = value == 0 ? EMPTY : Parameter.make(value);
            dphi = value == 0 || fixed ? EMPTY : Parameter.make(value);
            return this;
        }

        public Builder d(int value) {
            d = value;
            return this;
        }

        public Builder q(int value, boolean fixed) {
            theta = value == 0 ? EMPTY : Parameter.make(value);
            dtheta = value == 0 || fixed ? EMPTY : Parameter.make(value);
            return this;
        }

        public Builder bp(int value, boolean fixed) {
            bphi = value == 0 ? EMPTY : Parameter.make(value);
            dbphi = value == 0 || fixed ? EMPTY : Parameter.make(value);
            return this;
        }

        public Builder bd(int value) {
            bd = value;
            return this;
        }

        public Builder bq(int value, boolean fixed) {
            btheta = value == 0 ? EMPTY : Parameter.make(value);
            dbtheta = value == 0 || fixed ? EMPTY : Parameter.make(value);
            return this;
        }

        public Builder phi(Parameter[] value) {
            if (value == null || value.length == 0) {
                phi = EMPTY;
                dphi = EMPTY;
            } else {
                phi = value.clone();
                dphi = EMPTY;
            }
            return this;
        }

        // invalid settings
        private Builder dphi(Parameter[] value) {
            return this;
        }
        
        private Builder dbphi(Parameter[] value) {
            return this;
        }
        private Builder dtheta(Parameter[] value) {
            return this;
        }
        private Builder dbtheta(Parameter[] value) {
            return this;
        }

        public Builder phi(@NonNull Parameter[] value, @NonNull Parameter[] dvalue) {
            if (value.length != dvalue.length) {
                throw new IllegalArgumentException();
            } else {
                phi = value.clone();
                dphi = dvalue.clone();
            }
            return this;
        }

        public Builder theta(Parameter[] value) {
            if (value == null || value.length == 0) {
                theta = EMPTY;
                dtheta = EMPTY;
            } else {
                theta = value.clone();
                dtheta = EMPTY;
            }
            return this;
        }

        public Builder theta(@NonNull Parameter[] value, @NonNull Parameter[] dvalue) {
            if (value.length != dvalue.length) {
                throw new IllegalArgumentException();
            } else {
                theta = value.clone();
                dtheta = dvalue.clone();
            }
            return this;
        }

        public Builder bphi(Parameter[] value) {
            if (value == null || value.length == 0) {
                bphi = EMPTY;
                dbphi = EMPTY;
            } else {
                bphi = value.clone();
                dbphi = EMPTY;
            }
            return this;
        }

        public Builder bphi(@NonNull Parameter[] value, @NonNull Parameter[] dvalue) {
            if (value.length != dvalue.length) {
                throw new IllegalArgumentException();
            } else {
                bphi = value.clone();
                dbphi = dvalue.clone();
            }
            return this;
        }

        public Builder btheta(Parameter[] value) {
            if (value == null || value.length == 0) {
                btheta = EMPTY;
                dbtheta = EMPTY;
            } else {
                btheta = value.clone();
                dbtheta = EMPTY;
            }
            return this;
        }

        public Builder btheta(@NonNull Parameter[] value, @NonNull Parameter[] dvalue) {
            if (value.length != dvalue.length) {
                throw new IllegalArgumentException();
            } else {
                btheta = value.clone();
                dbtheta = dvalue.clone();
            }
            return this;
        }

        public Builder airline(boolean td) {
            phi = EMPTY;
            dphi = EMPTY;
            bphi = EMPTY;
            dbphi = EMPTY;
            theta = Parameter.make(1);
            btheta = Parameter.make(1);
            if (td) {
                dtheta = Parameter.make(1);
                dbtheta = Parameter.make(1);
            }
            d = 1;
            bd = 1;
            dvar = Parameter.zero();
            return this;
        }

        Builder free(LtdSarimaSpec ref) {
            if (ref == null) {
                phi = Parameter.freeParameters(phi);
                bphi = Parameter.freeParameters(bphi);
                theta = Parameter.freeParameters(theta);
                btheta = Parameter.freeParameters(btheta);
                dphi = Parameter.freeParameters(dphi);
                dbphi = Parameter.freeParameters(dbphi);
                dtheta = Parameter.freeParameters(dtheta);
                dbtheta = Parameter.freeParameters(dbtheta);
            } else {
                phi = Parameter.freeParameters(phi, ref.phi);
                bphi = Parameter.freeParameters(bphi, ref.bphi);
                theta = Parameter.freeParameters(theta, ref.theta);
                btheta = Parameter.freeParameters(btheta, ref.btheta);
                dphi = Parameter.freeParameters(dphi, ref.dphi);
                dbphi = Parameter.freeParameters(dbphi, ref.dbphi);
                dtheta = Parameter.freeParameters(dtheta, ref.dtheta);
                dbtheta = Parameter.freeParameters(dbtheta, ref.dbtheta);
            }
            return this;
        }

        Builder fix() {
            phi = Parameter.fixParameters(phi);
            bphi = Parameter.fixParameters(bphi);
            theta = Parameter.fixParameters(theta);
            btheta = Parameter.fixParameters(btheta);
            dphi = Parameter.fixParameters(dphi);
            dbphi = Parameter.fixParameters(dbphi);
            dtheta = Parameter.fixParameters(dtheta);
            dbtheta = Parameter.fixParameters(dbtheta);
            return this;
        }

        Builder reset(LtdSarimaSpec ref) {
            if (ref == null) {
                phi = Parameter.resetParameters(phi);
                bphi = Parameter.resetParameters(bphi);
                theta = Parameter.resetParameters(theta);
                btheta = Parameter.resetParameters(btheta);
                dphi = Parameter.resetParameters(dphi);
                dbphi = Parameter.resetParameters(dbphi);
                dtheta = Parameter.resetParameters(dtheta);
                dbtheta = Parameter.resetParameters(dbtheta);
            } else {
                phi = Parameter.resetParameters(phi, ref.phi);
                bphi = Parameter.resetParameters(bphi, ref.bphi);
                theta = Parameter.resetParameters(theta, ref.theta);
                btheta = Parameter.resetParameters(btheta, ref.btheta);
                dphi = Parameter.resetParameters(dphi, ref.dphi);
                dbphi = Parameter.resetParameters(dbphi, ref.dbphi);
                dtheta = Parameter.resetParameters(dtheta, ref.dtheta);
                dbtheta = Parameter.resetParameters(dbtheta, ref.dbtheta);
            }
            return this;
        }
    }

    public int getP() {
        return phi.length;
    }

    public int getQ() {
        return theta.length;
    }

    public int getBp() {
        return bphi.length;
    }

    public int getBq() {
        return btheta.length;
    }

    public int parametersCount() {
        return phi.length + bphi.length + theta.length + btheta.length + dphi.length + dbphi.length + dtheta.length + dbtheta.length;
    }

    public int freeParametersCount() {
        return Parameter.freeParametersCount(phi) + Parameter.freeParametersCount(bphi)
                + Parameter.freeParametersCount(theta) + Parameter.freeParametersCount(btheta)
                + Parameter.freeParametersCount(dphi) + Parameter.freeParametersCount(dbphi)
                + Parameter.freeParametersCount(dtheta) + Parameter.freeParametersCount(dbtheta);
    }

    public boolean hasFixedParameters() {
        return !Parameter.isFree(phi) || !Parameter.isFree(bphi)
                || !Parameter.isFree(theta) || !Parameter.isFree(btheta)
                || !Parameter.isFree(dphi) || !Parameter.isFree(dbphi)
                || !Parameter.isFree(dtheta) || !Parameter.isFree(dbtheta);
    }

    public boolean hasFreeParameters() {
        return Parameter.hasFreeParameters(phi) || Parameter.hasFreeParameters(bphi)
                || Parameter.hasFreeParameters(theta) || Parameter.hasFreeParameters(btheta)
                || Parameter.hasFreeParameters(dphi) || Parameter.hasFreeParameters(dbphi)
                || Parameter.hasFreeParameters(dtheta) || Parameter.hasFreeParameters(dbtheta);
    }

    public boolean hasEstimatedParameters() {
        return Parameter.hasParameters(phi, ParameterType.Estimated) || Parameter.hasParameters(bphi, ParameterType.Estimated)
                || !Parameter.hasParameters(theta, ParameterType.Estimated) || Parameter.hasParameters(btheta, ParameterType.Estimated);
    }

    public boolean isUndefined() {
        return Parameter.isDefault(phi) && Parameter.isDefault(theta)
                && Parameter.isDefault(bphi) && Parameter.isDefault(btheta)
                && Parameter.isDefault(dphi) && Parameter.isDefault(dtheta)
                && Parameter.isDefault(dbphi) && Parameter.isDefault(dbtheta);
    }

    public boolean isDefined() {
        return Parameter.isDefined(phi) && Parameter.isDefined(theta)
                && Parameter.isDefined(bphi) && Parameter.isDefined(btheta)
                && Parameter.isDefined(dphi) && Parameter.isDefined(dtheta)
                && Parameter.isDefined(dbphi) && Parameter.isDefined(dbtheta);
    }

    public LtdSarimaSpec withPeriod(int period) {
        if (this.period == period) {
            return this;
        }
        if (period == 1) {
            return new LtdSarimaSpec(validator, 1, d, 0, phi, EMPTY, theta, EMPTY, dphi, EMPTY, dtheta, EMPTY, dvar);
        } else {
            return new LtdSarimaSpec(validator, period, d, bd, phi, bphi, theta, btheta, dphi, dbphi, dtheta, dbtheta, dvar);
        }
    }

    public LtdSarimaSpec resetParameters(LtdSarimaSpec ref) {
        return toBuilder().reset(ref).build();
    }

    public LtdSarimaSpec freeParameters(LtdSarimaSpec ref) {
        return toBuilder().free(ref).build();
    }

    public LtdSarimaSpec fixParameters() {
        if (!hasFreeParameters()) {
            return this;
        }
        return toBuilder().fix().build();
    }

    public SarimaOrders orders() {
        SarimaOrders spec = new SarimaOrders(period);
        spec.setP(getP());
        spec.setD(d);
        spec.setQ(getQ());
        if (period != 1) {
            spec.setBp(getBp());
            spec.setBd(bd);
            spec.setBq(getBq());
        }
        return spec;
    }

    public int getDifferencingOrder() {
        return d + bd * period;
    }

    private static final LtdSarimaSpec AIRLINE = new LtdSarimaSpec(null, 0, 1, 1,
            EMPTY, EMPTY, Parameter.make(1), Parameter.make(1), EMPTY, EMPTY, Parameter.make(1), Parameter.make(1), Parameter.zero());

    public static LtdSarimaSpec airline() {
        return AIRLINE;
    }
}
