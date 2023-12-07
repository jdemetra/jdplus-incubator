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
package jdplus.x12plus.desktop.plugin.x12.ui;

import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.x12plus.base.api.X12plusSpec;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
public class X12plusSpecUI implements IObjectDescriptor<X12plusSpec> {
    
    private final X12plusSpecRoot root;
    
    @Override
    public X12plusSpec getCore(){
        return root.getCore();
    }

    public X12plusSpecUI(X12plusSpec spec, boolean ro) {
        root=new X12plusSpecRoot(spec, ro);
    }

   public SeriesSpecUI getSeries() {
        return new SeriesSpecUI(root);
    }
   
   public boolean isPreprocessing(){
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
        desc = decompositionDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    ///////////////////////////////////////////////////////////////////////////
    private static final int SERIES_ID=1, TRANSFORM_ID = 2, REGRESSION_ID = 3, STOCHASTIC_ID = 4, OUTLIER_ID = 5, ESTIMATE_ID = 7, DECOMPOSITION_ID=8;

    @Messages({"x12plusSpecUI.regressionDesc.name=REGRESSION",
        "x12plusSpecUI.regressionDesc.desc="
    })
    private EnhancedPropertyDescriptor regressionDesc() {
        if (! root.isPreprocessing())
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("regression", this.getClass(), "getRegression", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, REGRESSION_ID);
            desc.setDisplayName(Bundle.x12plusSpecUI_regressionDesc_name());
            desc.setShortDescription(Bundle.x12plusSpecUI_regressionDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x12plusSpecUI.seriesDesc.name=SERIES",
        "x12plusSpecUI.seriesDesc.desc="
    })
    private EnhancedPropertyDescriptor seriesDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("transform", this.getClass(), "getSeries", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SERIES_ID);
            desc.setDisplayName(Bundle.x12plusSpecUI_seriesDesc_name());
            desc.setShortDescription(Bundle.x12plusSpecUI_seriesDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x12plusSpecUI.transformDesc.name=TRANSFORM",
        "x12plusSpecUI.transformDesc.desc="
    })
    private EnhancedPropertyDescriptor transformDesc() {
        if (! root.isPreprocessing())
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("transform", this.getClass(), "getTransform", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TRANSFORM_ID);
            desc.setDisplayName(Bundle.x12plusSpecUI_transformDesc_name());
            desc.setShortDescription(Bundle.x12plusSpecUI_transformDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x12plusSpecUI.outlierDesc.name=OUTLIERS",
        "x12plusSpecUI.outlierDesc.desc="
    })
    private EnhancedPropertyDescriptor outlierDesc() {
        if (! root.isPreprocessing())
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("outlier", this.getClass(), "getOutlier", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, OUTLIER_ID);
            desc.setDisplayName(Bundle.x12plusSpecUI_outlierDesc_name());
            desc.setShortDescription(Bundle.x12plusSpecUI_outlierDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

//    @Messages({"x12plusSpecUI.stochasticDesc.name=MODEL",
//        "x12plusSpecUI.stochasticDesc.desc="
//    })
//    private EnhancedPropertyDescriptor stochasticDesc() {
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("stochastic", this.getClass(), "getStochastic", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, STOCHASTIC_ID);
//            desc.setDisplayName(Bundle.x12plusSpecUI_stochasticDesc_name());
//            desc.setShortDescription(Bundle.x12plusSpecUI_stochasticDesc_desc());
//            //edesc.setReadOnly(true);
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
//
    @Messages({"x12plusSpecUI.estimateDesc.name=ESTIMATE",
        "x12plusSpecUI.estimateDesc.desc="
    })
    private EnhancedPropertyDescriptor estimateDesc() {
        if (! root.isPreprocessing())
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("estimate", this.getClass(), "getEstimate", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ESTIMATE_ID);
            desc.setDisplayName(Bundle.x12plusSpecUI_estimateDesc_name());
            desc.setShortDescription(Bundle.x12plusSpecUI_estimateDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages("x12plusSpecUI.getDisplayName=Fractional airline decomposition")
    @Override
    public String getDisplayName() {
        return Bundle.x12plusSpecUI_getDisplayName();
    }

    public X11plusSpecUI getDecomposition() {
        return new X11plusSpecUI(root);
    }

    @Messages({"x12plusSpecUI.decompositionDesc.name=DECOMPOSITION",
        "x12plusSpecUI.decompositionDesc.desc=Includes the settings relevant to the decomposition step"
    })
    private EnhancedPropertyDescriptor decompositionDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("seats", this.getClass(), "getDecomposition", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DECOMPOSITION_ID);
            desc.setDisplayName(Bundle.x12plusSpecUI_decompositionDesc_name());
            desc.setShortDescription(Bundle.x12plusSpecUI_decompositionDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

}
