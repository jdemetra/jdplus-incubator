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
package jdplus.sts.desktop.plugin.bsm.ui;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.sts.base.api.BsmSpec;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
public class BsmSpecUI implements IPropertyDescriptors {

    private final StsSpecRoot root;

    public BsmSpecUI(StsSpecRoot root) {
        this.root = root;
    }

    public BsmSpec spec() {
        return root.getBsm();
    }



    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = defDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
//    ///////////////////////////////////////////////////////////////////////////
    private static final int DEF_ID = 0, ALG_ID = 1, TREND_ID = 2, SEAS_ID = 3;
//

    @NbBundle.Messages({
        "bsmSpecUI.defDesc.name=Default",
        "bsmSpecUI.defDesc.desc=Is Default?"
    })
    private EnhancedPropertyDescriptor defDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Default", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
            desc.setDisplayName(Bundle.bsmSpecUI_defDesc_name());
            desc.setShortDescription(Bundle.bsmSpecUI_defDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

//    @NbBundle.Messages({
//        "bsmSpecUI.trendDesc.name=TREND",
//        "bsmSpecUI.trendDesc.desc=Trend specification."
//    })
//    private EnhancedPropertyDescriptor trendDesc() {
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("TrendFilter", this.getClass(), "getTrendFilter", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
//            desc.setDisplayName(Bundle.bsmSpecUI_trendDesc_name());
//            desc.setShortDescription(Bundle.bsmSpecUI_trendDesc_desc());
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
//
//    @NbBundle.Messages({
//        "bsmSpecUI.seasDesc.name=SEASONAL",
//        "bsmSpecUI.seasDesc.desc=Seasonal specification."
//    })
//    private EnhancedPropertyDescriptor seasDesc() {
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("SeasonalFilter", this.getClass(), "getSeasonalFilters", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEAS_ID);
//            desc.setDisplayName(Bundle.bsmSpecUI_seasDesc_name());
//            desc.setShortDescription(Bundle.bsmSpecUI_seasDesc_desc());
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
//
//    @NbBundle.Messages({
//        "bsmSpecUI.algDesc.name=ALGORITHM",
//        "bsmSpecUI.algDesc.desc=STL+ Algorithm"
//    })
//    private EnhancedPropertyDescriptor algDesc() {
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("Algorithm", this.getClass(), "getAlgorithm", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ALG_ID);
//            desc.setDisplayName(Bundle.bsmSpecUI_algDesc_name());
//            desc.setShortDescription(Bundle.bsmSpecUI_algDesc_desc());
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }

    @Messages("bsmSpecUI.getDisplayName=BSM decomposition")
    @Override
    public String getDisplayName() {
        return Bundle.bsmSpecUI_getDisplayName();
    }

}
