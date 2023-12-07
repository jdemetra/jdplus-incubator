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
import jdplus.sts.base.api.ComponentUse;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;

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

    public ComponentUse getLevel() {
        return BsmSpec.use(spec().getLevelVar());
    }

    public void setLevel(ComponentUse use) {
        Parameter p=BsmSpec.ofUse(use, 0);
        root.update(spec().toBuilder().level(p, spec().getSlopeVar()).build());
    }

    public ComponentUse getSlope() {
       return BsmSpec.use(spec().getSlopeVar());
    }

    public void setSlope(ComponentUse use) {
        Parameter p=BsmSpec.ofUse(use, 0);
        root.update(spec().toBuilder().level(spec().getLevelVar(), p).build());
    }

    public ComponentUse getNoise() {
       return BsmSpec.use(spec().getNoiseVar());
    }

    public void setNoise(ComponentUse use) {
        Parameter p=BsmSpec.ofUse(use, 0);
        root.update(spec().toBuilder().noise(p).build());
    }

    public ComponentUse getCycle() {
       return BsmSpec.use(spec().getCycleVar());
    }

    public void setCycle(ComponentUse use) {
//        Parameter p=BsmSpec.ofUse(use, 0);
        root.update(spec().toBuilder().cycle(use != ComponentUse.Unused).build());
    }

    public SeasonalModel getModel() {
        return spec().getSeasonalModel();
    }

    public void setModel(SeasonalModel model) {
        root.update(spec().toBuilder().seasonal(model).build());
    }
    
//    public Parameter[] getCycleDumpingFactor(){
//        Parameter p=spec().getCycleDumpingFactor();
//        if (p == null)
//            p=Parameter.undefined();
//        return new Parameter[]{p};
//    }
//
//    public Parameter[] getCycleLength(){
//        Parameter p=core.getCyclicalPeriod();
//        if (p == null)
//            p=new Parameter();
//        return new Parameter[]{p};
//    }
    
//    public void setCycleDumpingFactor(Parameter[] p){
//        if (p != null && p.length == 1)
//            core.setCyclicalDumpingFactor(p[0]);
//    }
//
//    public void setCycleLength(Parameter[] p){
//        if (p != null && p.length == 1)
//            core.setCyclicalPeriod(p[0]);
//    }
//
    private EnhancedPropertyDescriptor lDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("level", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, L_ID);
            desc.setDisplayName(L_NAME);
            desc.setShortDescription(L_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("slope", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, S_ID);
            desc.setDisplayName(S_NAME);
            desc.setShortDescription(S_DESC);
            edesc.setReadOnly(getLevel() == ComponentUse.Unused);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor smDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("model", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SM_ID);
            desc.setDisplayName(SM_NAME);
            desc.setShortDescription(SM_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor nDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("noise", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, N_ID);
            desc.setDisplayName(N_NAME);
            desc.setShortDescription(N_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor cDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("cycle", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, C_ID);
            desc.setDisplayName(C_NAME);
            desc.setShortDescription(C_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

//    private EnhancedPropertyDescriptor cdDesc() {
//        try {
//            if (core.getCycleUse() == ComponentUse.Unused) {
//                return null;
//            }
//            PropertyDescriptor desc = new PropertyDescriptor("cycleDumpingFactor", this.getClass());
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, CDUMP_ID);
//            desc.setDisplayName(CDUMP_NAME);
//            desc.setShortDescription(CDUMP_DESC);
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
//
//    private EnhancedPropertyDescriptor clDesc() {
//        try {
//            if (core.getCycleUse() == ComponentUse.Unused) {
//                return null;
//            }
//            PropertyDescriptor desc = new PropertyDescriptor("cycleLength", this.getClass());
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, CLEN_ID);
//            desc.setDisplayName(CLEN_NAME);
//            desc.setShortDescription(CLEN_DESC);
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = lDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = sDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = cDesc();
        if (desc != null) {
            descs.add(desc);
        }
//        desc = cdDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
//        desc = clDesc();
//        if (desc != null) {
//            descs.add(desc);
//        }
        desc = nDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = smDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    public static final int L_ID = 0, S_ID = 1, C_ID = 2, CDUMP_ID = 3, CLEN_ID = 4, N_ID = 6, SM_ID = 10;
    public static final String L_NAME = "Level",
            S_NAME = "Slope",
            C_NAME = "Cycle",
            CLEN_NAME = "Cycle length",
            CDUMP_NAME = "Cycle dumping factor",
            N_NAME = "Noise",
            SM_NAME = "Seasonal model";
    public static final String L_DESC = "Level",
            S_DESC = "Slope",
            C_DESC = "Cycle",
            CLEN_DESC = "Cycle length",
            CDUMP_DESC = "Cycle dumping factor",
            N_DESC = "Noise",
            SM_DESC = "Seasonal model";

    @Override
    public String getDisplayName() {
        return "Basic structural model";
    }

    @Override
    public String toString() {
        return "";
    }
}
