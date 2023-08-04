/*
 * Copyright 2023 National Bank of Belgium
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

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.x11plus.base.api.SeasonalFilterOption;
import jdplus.x11plus.base.api.X11plusSpec;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
public class X11plusSpecUI implements IPropertyDescriptors {

    private final X13plusSpecRoot root;

    public X11plusSpecUI(X13plusSpecRoot root) {
        this.root = root;
    }

    public X11plusSpec spec() {
        return root.getX11();
    }

    public boolean isDefault() {
        return spec() == null;
    }

    public void setDefault(boolean def) {
        if (isDefault() == def) {
            return;
        }
        X11plusSpec spec = null;
        if (!def) {
            TsDomain domain = UserInterfaceContext.INSTANCE.getDomain();
            if (domain != null) {
                spec = X11plusSpec.createDefault(true, domain.getAnnualFrequency(), SeasonalFilterOption.S3X5);
            }
        }
        root.update(spec);
    }

//    public LoessSpecUI getTrendFilter() {
//        X11plusSpec spec = spec();
//        if (spec == null) {
//            return null;
//        }
//        return new LoessSpecUI(spec.getTrendSpec(), root.ro, tspec -> {
//            X11plusSpec nspec = spec.toBuilder().trendSpec(tspec).build();
//            root.update(nspec);
//        });
//    }

//    public SeasonalSpecUI getSeasonalFilter() {
//        X11plusSpec spec = spec();
//        if (spec == null) {
//            return null;
//        }
//        return new SeasonalSpecUI(spec.getSeasonalSpec(), root.ro, sspec -> {
//            X11plusSpec nspec = spec.toBuilder().seasonalSpec(sspec).build();
//            root.update(nspec);
//        });
//    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = defDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = trendDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = seasDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
//    ///////////////////////////////////////////////////////////////////////////
    private static final int DEF_ID = 0, ALG_ID = 1, TREND_ID = 2, SEAS_ID = 3;
//

    @NbBundle.Messages({
        "stlSpecUI.defDesc.name=Default",
        "stlSpecUI.defDesc.desc=Is Default?"
    })
    private EnhancedPropertyDescriptor defDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Default", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
            desc.setDisplayName(Bundle.stlSpecUI_defDesc_name());
            desc.setShortDescription(Bundle.stlSpecUI_defDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "stlSpecUI.trendDesc.name=TREND",
        "stlSpecUI.trendDesc.desc=Trend specification."
    })
    private EnhancedPropertyDescriptor trendDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TrendFilter", this.getClass(), "getTrendFilter", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
            desc.setDisplayName(Bundle.stlSpecUI_trendDesc_name());
            desc.setShortDescription(Bundle.stlSpecUI_trendDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @NbBundle.Messages({
        "stlSpecUI.seasDesc.name=SEASONAL",
        "stlSpecUI.seasDesc.desc=Seasonal specification."
    })
    private EnhancedPropertyDescriptor seasDesc() {
        if (isDefault()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SeasonalFilter", this.getClass(), "getSeasonalFilters", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEAS_ID);
            desc.setDisplayName(Bundle.stlSpecUI_seasDesc_name());
            desc.setShortDescription(Bundle.stlSpecUI_seasDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages("stlSpecUI.getDisplayName=STL decomposition")
    @Override
    public String getDisplayName() {
        return Bundle.stlSpecUI_getDisplayName();
    }

}
