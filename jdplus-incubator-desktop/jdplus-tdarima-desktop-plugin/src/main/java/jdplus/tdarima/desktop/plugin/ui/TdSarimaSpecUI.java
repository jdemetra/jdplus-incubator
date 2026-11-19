/*
 * Copyright 2026 JDemetra+.
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
package jdplus.tdarima.desktop.plugin.ui;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.advancedsa.base.api.tdarima.Orders;
import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.advancedsa.base.api.tdarima.TdSarimaSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;
import org.openide.util.NbBundle;

/**
 *
 * @author Jean Palate
 */
public class TdSarimaSpecUI implements IPropertyDescriptors {

    private final TdModelSpecRoot root;

    public TdSarimaSpecUI(TdModelSpecRoot root) {
        this.root = root;
    }

    public TdModelSpec core() {
        return root.getCore();
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = preprocessingDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = pDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = dDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = qDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = bpDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = bdDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = bqDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = innovationDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = tdPhiDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = tdThetaDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = tdBphiDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = tdBthetaDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    public boolean isPreprocessing() {
        return root.isUsingPreprocessing();
    }

    public void setPreprocessing(boolean p) {
        if (p != root.isUsingPreprocessing()) {
            root.setUsingPreprocessing(p);
        }
    }

    @NbBundle.Messages("TdModelSpecUI.getDisplayName=TDSarima model")
    @Override
    public String getDisplayName() {
        return Bundle.TdModelSpecUI_getDisplayName();
    }

    private EnhancedPropertyDescriptor preprocessingDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("preprocessing", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PR_ID);
            desc.setDisplayName(PR_NAME);
            desc.setShortDescription(PR_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    public boolean isInnovation() {
        return core().getTdarima().getTdspec().isVVar();
    }

    public void setInnovation(boolean b) {
        if (b == core().getTdarima().getTdspec().isVVar()) {
            return;
        }
        TdSarimaSpec nspec = root.getTdarima().getTdspec().toBuilder().vVar(b).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    private EnhancedPropertyDescriptor innovationDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("innovation", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, IVAR_ID);
            desc.setDisplayName(IVAR_NAME);
            desc.setShortDescription(IVAR_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    public int getP() {
        return core().getTdarima().getTdspec().getOrders().getP();
    }

    public void setP(int p) {
        if (p == core().getTdarima().getTdspec().getOrders().getP()) {
            return;
        }
        Orders norders =
                core().getTdarima().getTdspec().getOrders().toBuilder().p(p).build();
        TdSarimaSpec nspec =
                root.getTdarima().getTdspec().toBuilder().orders(norders).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public int getD() {
        return core().getTdarima().getTdspec().getOrders().getD();
    }

    public void setD(int d) {
        if (d == core().getTdarima().getTdspec().getOrders().getD()) {
            return;
        }
        Orders norders =
                core().getTdarima().getTdspec().getOrders().toBuilder().d(d).build();
        TdSarimaSpec nspec =
                root.getTdarima().getTdspec().toBuilder().orders(norders).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public int getQ() {
        return core().getTdarima().getTdspec().getOrders().getQ();
    }

    public void setQ(int q) {
        if (q == core().getTdarima().getTdspec().getOrders().getQ()) {
            return;
        }
        Orders norders =
                core().getTdarima().getTdspec().getOrders().toBuilder().q(q).build();
        TdSarimaSpec nspec =
                root.getTdarima().getTdspec().toBuilder().orders(norders).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public int getBp() {
        return core().getTdarima().getTdspec().getOrders().getBp();
    }

    public void setBp(int bp) {
        if (bp == core().getTdarima().getTdspec().getOrders().getBp()) {
            return;
        }
        Orders norders =
                core().getTdarima().getTdspec().getOrders().toBuilder().bp(bp).build();
        TdSarimaSpec nspec =
                root.getTdarima().getTdspec().toBuilder().orders(norders).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public int getBd() {
        return core().getTdarima().getTdspec().getOrders().getBd();
    }

    public void setBd(int bd) {
        if (bd == core().getTdarima().getTdspec().getOrders().getBd()) {
            return;
        }
        Orders norders =
                core().getTdarima().getTdspec().getOrders().toBuilder().bd(bd).build();
        TdSarimaSpec nspec =
                root.getTdarima().getTdspec().toBuilder().orders(norders).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public int getBq() {
        return core().getTdarima().getTdspec().getOrders().getBq();
    }

    public void setBq(int bq) {
        if (bq == core().getTdarima().getTdspec().getOrders().getBq()) {
            return;
        }
        Orders norders =
                core().getTdarima().getTdspec().getOrders().toBuilder().bq(bq).build();
        TdSarimaSpec nspec =
                root.getTdarima().getTdspec().toBuilder().orders(norders).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public boolean isTdTheta() {
        return root.getTdarima().getTdspec().isVTheta();
    }

    public void setTdTheta(boolean b) {
        if (b == root.getTdarima().getTdspec().isVTheta()) {
            return;
        }
        TdSarimaSpec nspec = root.getTdarima().getTdspec().toBuilder().vTheta(b).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public boolean isTdBtheta() {
        return core().getTdarima().getTdspec().isVBtheta();
    }

    public void setTdBtheta(boolean b) {
        if (b == core().getTdarima().getTdspec().isVBtheta()) {
            return;
        }
        TdSarimaSpec nspec =
                root.getTdarima().getTdspec().toBuilder().vBtheta(b).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public boolean isTdPhi() {
        return core().getTdarima().getTdspec().isVPhi();
    }

    public void setTdPhi(boolean b) {
        if (b == core().getTdarima().getTdspec().isVPhi()) {
            return;
        }
        TdSarimaSpec nspec = root.getTdarima().getTdspec().toBuilder().vPhi(b).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    public boolean isTdBphi() {
        return core().getTdarima().getTdspec().isVBphi();
    }

    public void setTdBphi(boolean b) {
        if (b == core().getTdarima().getTdspec().isVBphi()) {
            return;
        }
        TdSarimaSpec nspec = root.getTdarima().getTdspec().toBuilder().vBphi(b).build();
        root.update(root.getTdarima().toBuilder().tdspec(nspec).build());
    }

    private EnhancedPropertyDescriptor pDesc() {
        if (isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("p", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, P_ID);
            desc.setDisplayName(P_NAME);
            desc.setShortDescription(P_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor dDesc() {
        if (isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("d", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, D_ID);
            desc.setDisplayName(D_NAME);
            desc.setShortDescription(D_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor qDesc() {
        if (isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("q", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, Q_ID);
            desc.setDisplayName(Q_NAME);
            desc.setShortDescription(Q_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor bpDesc() {
        if (isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("bp", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BP_ID);
            desc.setDisplayName(BP_NAME);
            desc.setShortDescription(BP_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor bdDesc() {
        if (isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("bd", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BD_ID);
            desc.setDisplayName(BD_NAME);
            desc.setShortDescription(BD_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor bqDesc() {
        if (isPreprocessing()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("bq", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BQ_ID);
            desc.setDisplayName(BQ_NAME);
            desc.setShortDescription(BQ_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor tdThetaDesc() {
        if (isPreprocessing() || getQ() == 0) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TdTheta", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, THETAVAR_ID);
            desc.setDisplayName(THETAVAR_NAME);
            desc.setShortDescription(THETAVAR_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor tdBthetaDesc() {
        if (isPreprocessing() || getBq() == 0) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TdBtheta", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BTHETAVAR_ID);
            desc.setDisplayName(BTHETAVAR_NAME);
            desc.setShortDescription(BTHETAVAR_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor tdPhiDesc() {
        if (isPreprocessing() || getP() == 0) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TdPhi", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PHIVAR_ID);
            desc.setDisplayName(PHIVAR_NAME);
            desc.setShortDescription(PHIVAR_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor tdBphiDesc() {
        if (isPreprocessing() || getBp() == 0) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TdBphi", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BPHIVAR_ID);
            desc.setDisplayName(BPHIVAR_NAME);
            desc.setShortDescription(BPHIVAR_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int PR_ID = 0,
            IVAR_ID = 1,
            THETAVAR_ID = 2,
            BTHETAVAR_ID = 3,
            PHIVAR_ID = 4,
            BPHIVAR_ID = 5,
            P_ID = 10,
            D_ID = 11,
            Q_ID = 12,
            BP_ID = 13,
            BD_ID = 14,
            BQ_ID = 15;
    private static final String PR_NAME = "Use preprocessing",
            IVAR_NAME = "TD innovation",
            THETAVAR_NAME = "TD Q",
            BTHETAVAR_NAME = "TD BQ",
            PHIVAR_NAME = "TD P",
            BPHIVAR_NAME = "TD BP",
            P_NAME = "P",
            D_NAME = "D",
            Q_NAME = "Q",
            BP_NAME = "BP",
            BD_NAME = "BD",
            BQ_NAME = "BQ";
    private static final String PR_DESC = "Use preprocessing",
            IVAR_DESC = "Time dependent innovation",
            THETAVAR_DESC = "Time dependent Q",
            BTHETAVAR_DESC = "Time dependent BQ",
            PHIVAR_DESC = "Time dependent P",
            BPHIVAR_DESC = "Time dependent BQ",
            P_DESC = "P",
            D_DESC = "D",
            Q_DESC = "Q",
            BP_DESC = "BP",
            BD_DESC = "BD",
            BQ_DESC = "BQ";
    ;
}
