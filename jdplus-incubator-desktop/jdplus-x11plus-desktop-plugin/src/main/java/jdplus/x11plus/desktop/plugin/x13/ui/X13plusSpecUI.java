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
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.x11plus.base.api.X13plusSpec;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jean Palate
 */
public class X13plusSpecUI implements IObjectDescriptor<X13plusSpec> {
    
    private final X13plusSpecRoot root;
    
    @Override
    public X13plusSpec getCore(){
        return root.getCore();
    }

    public X13plusSpecUI(X13plusSpec spec, boolean ro) {
        root=new X13plusSpecRoot(spec, ro);
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

    @Messages({"x13plusSpecUI.regressionDesc.name=REGRESSION",
        "x13plusSpecUI.regressionDesc.desc="
    })
    private EnhancedPropertyDescriptor regressionDesc() {
        if (! root.isPreprocessing())
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("regression", this.getClass(), "getRegression", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, REGRESSION_ID);
            desc.setDisplayName(Bundle.x13plusSpecUI_regressionDesc_name());
            desc.setShortDescription(Bundle.x13plusSpecUI_regressionDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13plusSpecUI.seriesDesc.name=SERIES",
        "x13plusSpecUI.seriesDesc.desc="
    })
    private EnhancedPropertyDescriptor seriesDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("transform", this.getClass(), "getSeries", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SERIES_ID);
            desc.setDisplayName(Bundle.x13plusSpecUI_seriesDesc_name());
            desc.setShortDescription(Bundle.x13plusSpecUI_seriesDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13plusSpecUI.transformDesc.name=TRANSFORM",
        "x13plusSpecUI.transformDesc.desc="
    })
    private EnhancedPropertyDescriptor transformDesc() {
        if (! root.isPreprocessing())
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("transform", this.getClass(), "getTransform", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TRANSFORM_ID);
            desc.setDisplayName(Bundle.x13plusSpecUI_transformDesc_name());
            desc.setShortDescription(Bundle.x13plusSpecUI_transformDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages({"x13plusSpecUI.outlierDesc.name=OUTLIERS",
        "x13plusSpecUI.outlierDesc.desc="
    })
    private EnhancedPropertyDescriptor outlierDesc() {
        if (! root.isPreprocessing())
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("outlier", this.getClass(), "getOutlier", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, OUTLIER_ID);
            desc.setDisplayName(Bundle.x13plusSpecUI_outlierDesc_name());
            desc.setShortDescription(Bundle.x13plusSpecUI_outlierDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

//    @Messages({"x13plusSpecUI.stochasticDesc.name=MODEL",
//        "x13plusSpecUI.stochasticDesc.desc="
//    })
//    private EnhancedPropertyDescriptor stochasticDesc() {
//        try {
//            PropertyDescriptor desc = new PropertyDescriptor("stochastic", this.getClass(), "getStochastic", null);
//            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, STOCHASTIC_ID);
//            desc.setDisplayName(Bundle.x13plusSpecUI_stochasticDesc_name());
//            desc.setShortDescription(Bundle.x13plusSpecUI_stochasticDesc_desc());
//            //edesc.setReadOnly(true);
//            return edesc;
//        } catch (IntrospectionException ex) {
//            return null;
//        }
//    }
//
    @Messages({"x13plusSpecUI.estimateDesc.name=ESTIMATE",
        "x13plusSpecUI.estimateDesc.desc="
    })
    private EnhancedPropertyDescriptor estimateDesc() {
        if (! root.isPreprocessing())
            return null;
        try {
            PropertyDescriptor desc = new PropertyDescriptor("estimate", this.getClass(), "getEstimate", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ESTIMATE_ID);
            desc.setDisplayName(Bundle.x13plusSpecUI_estimateDesc_name());
            desc.setShortDescription(Bundle.x13plusSpecUI_estimateDesc_desc());
            //edesc.setReadOnly(true);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Messages("x13plusSpecUI.getDisplayName=Fractional airline decomposition")
    @Override
    public String getDisplayName() {
        return Bundle.x13plusSpecUI_getDisplayName();
    }

    public X11plusSpecUI getDecomposition() {
        return new X11plusSpecUI(root);
    }

    @Messages({"x13plusSpecUI.decompositionDesc.name=DECOMPOSITION",
        "x13plusSpecUI.decompositionDesc.desc=Includes the settings relevant to the decomposition step"
    })
    private EnhancedPropertyDescriptor decompositionDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("seats", this.getClass(), "getDecomposition", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DECOMPOSITION_ID);
            desc.setDisplayName(Bundle.x13plusSpecUI_decompositionDesc_name());
            desc.setShortDescription(Bundle.x13plusSpecUI_decompositionDesc_desc());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

}
