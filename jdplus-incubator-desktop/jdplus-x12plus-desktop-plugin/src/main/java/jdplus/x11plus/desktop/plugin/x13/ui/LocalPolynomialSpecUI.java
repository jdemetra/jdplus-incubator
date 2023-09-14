/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.x11plus.desktop.plugin.x13.ui;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.math.linearfilters.KernelOption;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import org.openide.util.NbBundle;

/**
 *
 * @author palatej
 */
public class LocalPolynomialSpecUI implements IPropertyDescriptors {

    private LocalPolynomialFilterSpec core;
    private final boolean ro;
    private final boolean seas;
    private final Consumer<LocalPolynomialFilterSpec> callback;

    public LocalPolynomialSpecUI(LocalPolynomialFilterSpec core, boolean ro, boolean seas, Consumer<LocalPolynomialFilterSpec> callback) {
        this.core = core;
        this.ro = ro;
        this.seas = seas;
        this.callback = callback;
    }

    public int getHorizon() {
        return core.getFilterHorizon();
    }

    public int getDegree() {
        return core.getPolynomialDegree();
    }

    public KernelOption getKernel() {
        return core.getKernel();
    }

    public int getAsymmetricDegree() {
        return core.getAsymmetricPolynomialDegree();
    }

    public int getLeftModelDegree() {
        return core.getLeftLinearModelCoefficients().length;
    }

    public double getLeftModelSlope() {
        double[] c = core.getLeftLinearModelCoefficients();
        if (c.length == 0) {
            return Double.NaN;
        } else {
            return c[0];
        }
    }

    public int getRightModelDegree() {
        return core.getRightLinearModelCoefficients().length;
    }

    public double getRightModelSlope() {
        double[] c = core.getRightLinearModelCoefficients();
        if (c.length == 0) {
            return Double.NaN;
        } else {
            return c[0];
        }
    }

    public void setHorizon(int h) {
        if (core.getFilterHorizon() == h) {
            return;
        }
        core = core.toBuilder()
                .filterHorizon(h)
                .build();
        callback.accept(core);
    }

    public void setDegree(int d) {
        if (core.getPolynomialDegree() == d) {
            return;
        }
        core = core.toBuilder()
                .polynomialDegree(d)
                .build();
        callback.accept(core);
    }

    public void setKernel(KernelOption k) {
        if (core.getKernel() == k) {
            return;
        }
        core = core.toBuilder()
                .kernel(k)
                .build();
        callback.accept(core);
    }

    public void setLeftModelDegree(int d) {
        double[] oldc = core.getLeftLinearModelCoefficients();
        if (oldc.length == d) {
            return;
        }
        if (d > 1 || d < 0) {
            throw new IllegalArgumentException("Should be 0 or 1");
        }
        double[] c = d == 0 ? Doubles.EMPTYARRAY : new double[]{LocalPolynomialFilterSpec.DEF_SLOPE};
        core = core.toBuilder()
                .leftLinearModelCoefficients(c)
                .build();
        callback.accept(core);
    }

    public void setRightModelDegree(int d) {
        double[] oldc = core.getRightLinearModelCoefficients();
        if (oldc.length == d) {
            return;
        }
        if (d > 1 || d < 0) {
            throw new IllegalArgumentException("Should be 0 or 1");
        }
        double[] c = d == 0 ? Doubles.EMPTYARRAY : new double[]{LocalPolynomialFilterSpec.DEF_SLOPE};
        core = core.toBuilder()
                .rightLinearModelCoefficients(c)
                .build();
        callback.accept(core);
    }

    public void setAsymmetricDegree(int d) {
        if (core.getAsymmetricPolynomialDegree() == d) {
            return;
        }
        if (d > 2 || d < 0) {
            throw new IllegalArgumentException("Should be 0 or 1");
        }
        core = core.toBuilder()
                .asymmetricPolynomialDegree(d)
                .build();
        callback.accept(core);
    }

    public void setLeftModelSlope(double s) {
        core = core.toBuilder()
                .leftLinearModelCoefficients(s)
                .build();
        callback.accept(core);
    }

    public void setRightModelSlope(double s) {
        core = core.toBuilder()
                .rightLinearModelCoefficients(s)
                .build();
        callback.accept(core);
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.horizon.name=Horizon",
        "localPolynomialSpecUI.horizon.desc= The filter is defined on [-horizon, horizon]."
    })
    private EnhancedPropertyDescriptor horizonDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Horizon", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LENGTH_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_horizon_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_horizon_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.kernel.name=Kernel",
        "localPolynomialSpecUI.kernel.desc= Kernel of the filter"
    })
    private EnhancedPropertyDescriptor kernelDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Kernel", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, KERNEL_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_kernel_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_kernel_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.degree.name=Degree",
        "localPolynomialSpecUI.degree.desc= Degree of the polynomials preserved by the central filter"
    })
    private EnhancedPropertyDescriptor degDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Degree", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DEG_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_degree_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_degree_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.adegree.name=Asymmetric preserv deg",
        "localPolynomialSpecUI.adegree.desc= Degree of the polynomials preserved by the asymmetric filters"
    })
    private EnhancedPropertyDescriptor adegDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("AsymmetricDegree", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ADEG_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_adegree_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_adegree_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.lmdegree.name=Left model deg",
        "localPolynomialSpecUI.lmdegree.desc= Degree of the model at the beginning of the series"
    })
    private EnhancedPropertyDescriptor lmdegDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("LeftModelDegree", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LMDEG_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_lmdegree_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_lmdegree_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.lmslope.name=Left slope",
        "localPolynomialSpecUI.lmslope.desc= Slope of the linear model at the beginning of the series"
    })
    private EnhancedPropertyDescriptor lmslopeDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("LeftModelSlope", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LMSLOPE_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_lmslope_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_lmslope_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.rmdegree.name=Right model deg",
        "localPolynomialSpecUI.rmdegree.desc= Degree of the model at the end of the series"
    })
    private EnhancedPropertyDescriptor rmdegDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("RightModelDegree", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, RMDEG_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_rmdegree_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_rmdegree_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.rmslope.name=Right slope",
        "localPolynomialSpecUI.rmslope.desc= Slope of the linear model at the end of the series"
    })
    private EnhancedPropertyDescriptor rmslopeDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("RightModelSlope", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, RMSLOPE_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_rmslope_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_rmslope_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    public static final int LENGTH_ID = 0, DEG_ID = 1, LIC_ID = 2, RIC_ID = 3, KERNEL_ID = 4, ADEG_ID = 8,
            LMDEG_ID = 10, RMDEG_ID = 11, LMSLOPE_ID = 10, RMSLOPE_ID = 11;

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = horizonDesc();
        if (desc != null) {
            descs.add(desc);
        }
        if (!seas) {
            desc = degDesc();
            if (desc != null) {
                descs.add(desc);
            }
        }
        desc = kernelDesc();
        if (desc != null) {
            descs.add(desc);
        }
        if (!seas) {
            desc = adegDesc();
            if (desc != null) {
                descs.add(desc);
            }
            desc = lmdegDesc();
            if (desc != null) {
                descs.add(desc);
            }
            desc = lmslopeDesc();
            if (desc != null) {
                descs.add(desc);
            }
            desc = rmdegDesc();
            if (desc != null) {
                descs.add(desc);
            }
            desc = rmslopeDesc();
            if (desc != null) {
                descs.add(desc);
            }
        }
        return descs;
    }

    @NbBundle.Messages("localPolynomialSpecUI.getDisplayName=Local polynomial filter")
    @Override
    public String getDisplayName() {
        return Bundle.localPolynomialSpecUI_getDisplayName();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.append("LP-").append(core.getFilterHorizon() * 2 + 1).toString();
    }
}
