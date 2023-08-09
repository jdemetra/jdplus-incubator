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
import jdplus.toolkit.base.api.math.linearfilters.HendersonSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.x11plus.base.api.X11plusSpec;
import org.openide.util.NbBundle;

/**
 *
 * @author palatej
 */
public class HendersonSpecUI implements IPropertyDescriptors {

    private X11plusSpec spec() {
        return root.getX11();
    }

    private HendersonSpec henderson() {
        return (HendersonSpec) spec().getTrendFilter();
    }

    private void update(X11plusSpec nx11) {
        root.update(nx11);
    }

    private boolean isRo() {
        return root.isRo();
    }

    private final X13plusSpecRoot root;

    public HendersonSpecUI(X13plusSpecRoot root) {
        this.root = root;
    }

    public int getHorizon() {
        return henderson().getFilterHorizon();
    }

    public double getLeftIcRatio() {
        return henderson().getLeftIcRatio();
    }

    public double getRightIcRatio() {
        return henderson().getRightIcRatio();
    }

    public void setHorizon(int h) {
        HendersonSpec oldspec = henderson();
        if (oldspec.getFilterHorizon() == h) {
            return;
        }
        HendersonSpec nspec = new HendersonSpec(h, oldspec.getLeftIcRatio(), oldspec.getRightIcRatio());
        update(spec().toBuilder().trendFilter(nspec).build());
    }

    public void setLeftIcRatio(double c) {
        HendersonSpec oldspec = henderson();
        if (oldspec.getLeftIcRatio() == c) {
            return;
        }
        HendersonSpec nspec = new HendersonSpec(oldspec.getFilterHorizon(), c, oldspec.getRightIcRatio());
        update(spec().toBuilder().trendFilter(nspec).build());
    }

    public void setRightIcRatio(double c) {
        HendersonSpec oldspec = henderson();
        if (oldspec.getRightIcRatio() == c) {
            return;
        }
        HendersonSpec nspec = new HendersonSpec(oldspec.getFilterHorizon(), oldspec.getLeftIcRatio(), c);
        update(spec().toBuilder().trendFilter(nspec).build());
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = horizonDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = licDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = ricDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    public static final int LENGTH_ID = 0, LIC_ID = 1, RIC_ID = 2;

    @NbBundle.Messages({
        "hendersonSpecUI.horizon.name=Horizon",
        "hendersonSpecUI.horizon.desc= The filter is defined on [-horizon, horizon]."
    })
    private EnhancedPropertyDescriptor horizonDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Horizon", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LENGTH_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.hendersonSpecUI_horizon_name());
            desc.setShortDescription(Bundle.hendersonSpecUI_horizon_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "hendersonSpecUI.lic.name=IC at start",
        "hendersonSpecUI.lic.desc= IC-ratio at start"
    })
    private EnhancedPropertyDescriptor licDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("LeftIcRatio", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LIC_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.hendersonSpecUI_lic_name());
            desc.setShortDescription(Bundle.hendersonSpecUI_lic_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "hendersonSpecUI.ric.name=IC at end",
        "hendersonSpecUI.ric.desc= IC-ratio at end"
    })
    private EnhancedPropertyDescriptor ricDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("RightIcRatio", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, RIC_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(Bundle.hendersonSpecUI_ric_name());
            desc.setShortDescription(Bundle.hendersonSpecUI_ric_desc());
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages("hendersonSpecUI.getDisplayName=Henderson filter")
    @Override
    public String getDisplayName() {
        return Bundle.hendersonSpecUI_getDisplayName();
    }


}
