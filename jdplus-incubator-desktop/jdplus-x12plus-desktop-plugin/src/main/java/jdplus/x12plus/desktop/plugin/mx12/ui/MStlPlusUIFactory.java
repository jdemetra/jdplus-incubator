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
package jdplus.x12plus.desktop.plugin.mx12.ui;

//import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
//import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
//import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
//import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
//import jdplus.stl.base.api.MStlPlusSpec;
//
//import java.awt.Color;
//import javax.swing.Icon;
//import jdplus.stl.base.core.mstlplus.MStlPlusDocument;
//import org.openide.util.ImageUtilities;
//import org.openide.util.lookup.ServiceProvider;
//
///**
// *
// * @author PALATEJ
// */
//@ServiceProvider(service = DocumentUIServices.class)
//public class MStlPlusUIFactory implements DocumentUIServices<MStlPlusSpec, MStlPlusDocument> {
//
////    public static StlPlusUIFactory INSTANCE=new StlPlusUIFactory();
//    @Override
//    public IProcDocumentView<MStlPlusDocument> getDocumentView(MStlPlusDocument document) {
//        return MStlPlusViewFactory.getDefault().create(document);
//    }
//
//    @Override
//    public IObjectDescriptor<MStlPlusSpec> getSpecificationDescriptor(MStlPlusSpec spec) {
//        return new MStlPlusSpecUI(spec, false);
//    }
//
//    @Override
//    public Class<MStlPlusDocument> getDocumentType() {
//        return MStlPlusDocument.class;
//    }
//
//    @Override
//    public Class<MStlPlusSpec> getSpecType() {
//        return MStlPlusSpec.class;
//    }
//
//    @Override
//    public Color getColor() {
//        return Color.RED;
//    }
//
//    @Override
//    public Icon getIcon() {
//        return ImageUtilities.loadImageIcon("jdplus/stl/desktop/plugin/tangent_red.png", false);
//    }
//
//    @Override
//    public void showDocument(WorkspaceItem<MStlPlusDocument> item) {
//        if (item.isOpen()) {
//            item.getView().requestActive();
//        } else {
//            MStlPlusTopComponent view = new MStlPlusTopComponent(item);
//            view.open();
//            view.requestActive();
//        }
//    }
//
//}
