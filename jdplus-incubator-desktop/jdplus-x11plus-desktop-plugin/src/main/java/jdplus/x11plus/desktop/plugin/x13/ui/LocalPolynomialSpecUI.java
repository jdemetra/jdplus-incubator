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
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.x11plus.base.api.X11plusSpec;
import org.openide.util.NbBundle;

/**
 *
 * @author palatej
 */
public class LocalPolynomialSpecUI implements IPropertyDescriptors {

    private LocalPolynomialFilterSpec core;
    private final boolean ro;
    private final Consumer<LocalPolynomialFilterSpec> callback;

    public LocalPolynomialSpecUI(LocalPolynomialFilterSpec core, boolean ro, Consumer<LocalPolynomialFilterSpec> callback) {
        this.core = core;
        this.ro = ro;
        this.callback = callback;
    }

    public int getHorizon() {
        return core.getFilterHorizon();
    }

    public int getDegree() {
        return core.getPolynomialDegree();
    }

    public int getLeftAsymmetricDegree() {
        return core.getLeftAsymmetricPolynomialDegree();
    }

    public int getRightAsymmetricDegree() {
        return core.getRightAsymmetricPolynomialDegree();
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

    public void setLeftAsymmetricDegree(int d) {
        if (core.getLeftAsymmetricPolynomialDegree() == d) {
            return;
        }
        core = core.toBuilder()
                .leftAsymmetricPolynomialDegree(d)
                .build();
        callback.accept(core);
    }

    public void setRightAsymmetricDegree(int d) {
        if (core.getRightAsymmetricPolynomialDegree() == d) {
            return;
        }
        core = core.toBuilder()
                .rightAsymmetricPolynomialDegree(d)
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
        "localPolynomialSpecUI.ladegree.name=Left Asymmetric deg",
        "localPolynomialSpecUI.ladegree.desc= Degree of the polynomials preserved by the asymmetric filters at the beginnig of the series"
    })
    private EnhancedPropertyDescriptor ladegDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("LeftAsymmetricDegree", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LADEG_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_ladegree_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_ladegree_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "localPolynomialSpecUI.radegree.name=Right Asymmetric deg",
        "localPolynomialSpecUI.radegree.desc= Degree of the polynomials preserved by the asymmetric filters at the end of the series"
    })
    private EnhancedPropertyDescriptor radegDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("RightAsymmetricDegree", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, RADEG_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.localPolynomialSpecUI_radegree_name());
            desc.setShortDescription(Bundle.localPolynomialSpecUI_radegree_desc());
            edesc.setReadOnly(ro);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    public static final int LENGTH_ID = 0, DEG_ID = 1, LIC_ID = 2, RIC_ID = 3, LADEG_ID = 4, RADEG_ID = 5;

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = horizonDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = degDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = ladegDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = radegDesc();
        if (desc != null) {
            descs.add(desc);
        }
//        desc = licDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
//        desc = ricDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
        return descs;
    }

    @NbBundle.Messages("localPolynomialSpecUI.getDisplayName=Local polynomial filter")
    @Override
    public String getDisplayName() {
        return Bundle.localPolynomialSpecUI_getDisplayName();
    }

}
