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
package jdplus.x11plus.desktop.plugin.mx13.ui;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.x11plus.base.api.MX11plusSpec;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
//public class MX11plusSpecUI implements IPropertyDescriptors {
//
//    private final MX11plusSpecRoot root;
//
//     public MX11plusSpecUI(MX11plusSpec spec, boolean ro) {
//        root = new MX11plusSpecUI(spec, ro);
//    }
//
//    public MStlSpecUI(MStlPlusSpecRoot root) {
//        this.root = root;
//    }
//    
//    public MStlSpec spec(){
//        return root.stl();
//    }
//
//    public LoessSpecUI getTrendFilter() {
//        return new LoessSpecUI(root.stl().getTrendSpec(), root.ro, spec -> {
//            root.update(root.stl().toBuilder().trendSpec(spec).build());
//        });
//    }
//
//    public AlgorithmUI getAlgorithm() {
//        return new AlgorithmUI(root);
//    }
//
//    public SeasonalsUI getSeasonalFilters() {
//        return new SeasonalsUI(root);
//    }
//
//    @Override
//    public List<EnhancedPropertyDescriptor> getProperties() {
//        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
//        EnhancedPropertyDescriptor desc = algDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
//        desc = trendDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
//        desc = seasDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
//        return descs;
//    }
////    ///////////////////////////////////////////////////////////////////////////
//    private static final int ALG_ID = 1, TREND_ID = 2, SEAS_ID = 3;
////
//
//    @NbBundle.Messages({
//        "mstlPlusSpecUI.trendDesc.name=TREND",
//        "mstlPlusSpecUI.trendDesc.desc=Trend specification."
//    })
//    private EnhancedPropertyDescriptor trendDesc() {
//        if (spec() == null)
//            return null;
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("TrendFilter", this.getClass(), "getTrendFilter", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
//            desc.setDisplayName(Bundle.mstlPlusSpecUI_trendDesc_name());
//            desc.setShortDescription(Bundle.mstlPlusSpecUI_trendDesc_desc());
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
//
//    @NbBundle.Messages({
//        "mstlPlusSpecUI.seasDesc.name=SEASONALS",
//        "mstlPlusSpecUI.seasDesc.desc=Seasonal specifications."
//    })
//    private EnhancedPropertyDescriptor seasDesc() {
//        if (spec() == null)
//            return null;
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("SeasonalFilters", this.getClass(), "getSeasonalFilters", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEAS_ID);
//            desc.setDisplayName(Bundle.mstlPlusSpecUI_seasDesc_name());
//            desc.setShortDescription(Bundle.mstlPlusSpecUI_seasDesc_desc());
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
//
//    @NbBundle.Messages({
//        "mstlPlusSpecUI.algDesc.name=ALGORITHM",
//        "mstlPlusSpecUI.algDesc.desc=STL+ Algorithm"
//    })
//    private EnhancedPropertyDescriptor algDesc() {
//        if (spec() == null)
//            return null;
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("Algorithm", this.getClass(), "getAlgorithm", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ALG_ID);
//            desc.setDisplayName(Bundle.mstlPlusSpecUI_algDesc_name());
//            desc.setShortDescription(Bundle.mstlPlusSpecUI_algDesc_desc());
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
//
//    @Messages("mstlPlusSpecUI.getDisplayName=STL+")
//    @Override
//    public String getDisplayName() {
//        return Bundle.mstlPlusSpecUI_getDisplayName();
//    }
//
//}
