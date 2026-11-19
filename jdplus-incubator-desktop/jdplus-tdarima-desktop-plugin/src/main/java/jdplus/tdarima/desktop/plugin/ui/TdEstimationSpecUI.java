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
package jdplus.tdarima.desktop.plugin.ui;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.advancedsa.base.api.tdarima.TdEstimationSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IPropertyDescriptors;

/**
 *
 * @author Jean Palate
 */
public class TdEstimationSpecUI implements IPropertyDescriptors {

    private final TdModelSpecRoot root;

    public TdEstimationSpecUI(TdModelSpecRoot root) {
        this.root = root;
    }

    public TdEstimationSpec spec() {
        return root.getTdarima();
    }

    public TdSarimaSpecUI getTdSarima() {
        return new TdSarimaSpecUI(root);
    }

    private TdEstimationSpec core() {
        return root.getTdarima();
    }

    public double getSimplificationThreshold() {
        return core().getSimplificationThreshold();
    }

    public double getPrecision() {
        return core().getPrecision();
    }

    public TdEstimationSpec.Parametrization getParametrization() {
        return core().getParametrization();
    }

    public TdEstimationSpec.Transformation getFunction() {
        return core().getTransformation();
    }

    public void setSimplificationThreshold(double p) {
        if (p != core().getSimplificationThreshold()) {
            root.update(core().toBuilder().simplificationThreshold(p).build());
        }
    }

    public void setPrecision(double p) {
        if (p != core().getPrecision()) {
            root.update(core().toBuilder().precision(p).build());
        }
    }

    public void setParametrization(TdEstimationSpec.Parametrization p) {
        if (p != core().getParametrization()) {
            root.update(core().toBuilder().parametrization(p).build());
        }
    }

    public void setFunction(TdEstimationSpec.Transformation t) {
        if (t != core().getTransformation()) {
            root.update(core().toBuilder().transformation(t).build());
        }
    }

    private EnhancedPropertyDescriptor precisionDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("precision", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, EPS_ID);
            desc.setDisplayName(EPS_NAME);
            desc.setShortDescription(EPS_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor thresholdDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("simplificationThreshold", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TRESHOLD_ID);
            desc.setDisplayName(TRESHOLD_NAME);
            desc.setShortDescription(TRESHOLD_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor parametrizationDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("parametrization", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PARAM_ID);
            desc.setDisplayName(PARAM_NAME);
            desc.setShortDescription(PARAM_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor fnDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("function", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FN_ID);
            desc.setDisplayName(FN_NAME);
            desc.setShortDescription(FN_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = parametrizationDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = fnDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = thresholdDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = precisionDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    public static final int PARAM_ID = 1, FN_ID = 2, EPS_ID = 3, TRESHOLD_ID = 4;
    public static final String PARAM_NAME = "Parametrization",
            FN_NAME = "Function",
            EPS_NAME = "Precision",
            TRESHOLD_NAME = "Simplification threshold";
    public static final String PARAM_DESC = "Parametrization",
            FN_DESC = "Function",
            EPS_DESC = "Precision",
            TRESHOLD_DESC = "Simplification threshold";

    @Override
    public String getDisplayName() {
        return Bundle.TdEstimationSpecUI_getDisplayName();
    }

    @Override
    public String toString() {
        return "";
    }
}
