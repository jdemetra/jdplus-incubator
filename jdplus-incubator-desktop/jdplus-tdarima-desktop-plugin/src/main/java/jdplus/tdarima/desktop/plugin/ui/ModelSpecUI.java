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
import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
public class ModelSpecUI implements IObjectDescriptor<TdModelSpec> {

    private final TdModelSpecRoot root;

    @Override
    public TdModelSpec getCore() {
        return root.getCore();
    }

    public ModelSpecUI(TdModelSpec spec, boolean ro) {
        root = new TdModelSpecRoot(spec, ro);
    }

    public SeriesSpecUI getSeries() {
        return new SeriesSpecUI(root);
    }

    public boolean isPreprocessing() {
        return root.getPreprocessing().isEnabled();
    }

    public TransformSpecUI getTransform() {
        return new TransformSpecUI(root);
    }

    public RegressionSpecUI getRegression() {
        return new RegressionSpecUI(root);
    }

    public OutlierSpecUI getOutlier() {
        return new OutlierSpecUI(root);
    }

    public EstimateSpecUI getEstimate() {
        return new EstimateSpecUI(root);
    }

    public TdEstimationSpecUI getTdEstimation() {
        return new TdEstimationSpecUI(root);
    }

    public TdSarimaSpecUI getTdModel() {
        return new TdSarimaSpecUI(root);
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        // regression
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = seriesDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = transformDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = estimateDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = regressionDesc();
        if (desc != null) {
            descs.add(desc);
        }
        //        desc = stochasticDesc();
        //        if (desc != null) {
        //            descs.add(desc);
        //        }
        desc = outlierDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = tdModelDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = tdEstimationDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    ///////////////////////////////////////////////////////////////////////////
    private static final int SERIES_ID = 1,
            TRANSFORM_ID = 2,
            REGRESSION_ID = 3,
            OUTLIER_ID = 5,
            ESTIMATE_ID = 7,
            TDSARIMA_DEF_ID = 8,
            TDSARIMA_EST_ID = 9;

    @Messages({"TdModelSpecUI.regressionDesc.name=REGRESSION", "TdModelSpecUI.regressionDesc.desc="})
    private EnhancedPropertyDescriptor regressionDesc() {
        if (!root.isPreprocessing()) return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("regression", this.getClass(), "getRegression", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, REGRESSION_ID);
            desc.setDisplayName(Bundle.TdModelSpecUI_regressionDesc_name());
            desc.setShortDescription(Bundle.TdModelSpecUI_regressionDesc_desc());
            // edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"TdModelSpecUI.seriesDesc.name=SERIES", "TdModelSpecUI.seriesDesc.desc="})
    private EnhancedPropertyDescriptor seriesDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("transform", this.getClass(), "getSeries", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SERIES_ID);
            desc.setDisplayName(Bundle.TdModelSpecUI_seriesDesc_name());
            desc.setShortDescription(Bundle.TdModelSpecUI_seriesDesc_desc());
            // edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"TdModelSpecUI.transformDesc.name=TRANSFORM", "TdModelSpecUI.transformDesc.desc="})
    private EnhancedPropertyDescriptor transformDesc() {
        if (!root.isPreprocessing()) return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("transform", this.getClass(), "getTransform", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TRANSFORM_ID);
            desc.setDisplayName(Bundle.TdModelSpecUI_transformDesc_name());
            desc.setShortDescription(Bundle.TdModelSpecUI_transformDesc_desc());
            // edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"TdModelSpecUI.outlierDesc.name=OUTLIERS", "TdModelSpecUI.outlierDesc.desc="})
    private EnhancedPropertyDescriptor outlierDesc() {
        if (!root.isPreprocessing()) return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("outlier", this.getClass(), "getOutlier", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, OUTLIER_ID);
            desc.setDisplayName(Bundle.TdModelSpecUI_outlierDesc_name());
            desc.setShortDescription(Bundle.TdModelSpecUI_outlierDesc_desc());
            // edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"TdModelSpecUI.estimateDesc.name=ESTIMATE", "TdModelSpecUI.estimateDesc.desc="})
    private EnhancedPropertyDescriptor estimateDesc() {
        if (!root.isPreprocessing()) return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("estimate", this.getClass(), "getEstimate", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ESTIMATE_ID);
            desc.setDisplayName(Bundle.TdModelSpecUI_estimateDesc_name());
            desc.setShortDescription(Bundle.TdModelSpecUI_estimateDesc_desc());
            // edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "ModelSpecUI.tdModel.name=TD definition",
        "ModelSpecUI.tdModel.desc=Includes the settings relevant to the definition of the TD Sarima model"
    })
    private EnhancedPropertyDescriptor tdModelDesc() {
        try {
            PropertyDescriptor desc =
                    new PropertyDescriptor("TDSarima Estimation", this.getClass(), "getTdModel", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TDSARIMA_DEF_ID);
            desc.setDisplayName(Bundle.ModelSpecUI_tdModel_name());
            desc.setShortDescription(Bundle.ModelSpecUI_tdModel_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({
        "ModelSpecUI.tdEstimation.name=TD Estimation",
        "ModelSpecUI.tdEstimation.desc=Includes the settings relevant to the estimation of the TD Sarima model"
    })
    private EnhancedPropertyDescriptor tdEstimationDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TDSarima", this.getClass(), "getTdEstimation", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TDSARIMA_EST_ID);
            desc.setDisplayName(Bundle.ModelSpecUI_tdEstimation_name());
            desc.setShortDescription(Bundle.ModelSpecUI_tdEstimation_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages("TdEstimationSpecUI.getDisplayName=TDSarima estimation")
    @Override
    public String getDisplayName() {
        return Bundle.TdEstimationSpecUI_getDisplayName();
    }
}
