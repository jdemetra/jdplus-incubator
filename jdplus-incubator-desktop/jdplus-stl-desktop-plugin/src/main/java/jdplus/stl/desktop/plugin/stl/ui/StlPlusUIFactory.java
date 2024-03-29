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
package jdplus.stl.desktop.plugin.stl.ui;

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.stl.base.api.StlPlusSpec;
import java.awt.Color;
import javax.swing.Icon;
import jdplus.stl.base.core.stlplus.StlPlusDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class StlPlusUIFactory implements DocumentUIServices<StlPlusSpec, StlPlusDocument> {

//    public static StlPlusUIFactory INSTANCE=new StlPlusUIFactory();
    @Override
    public IProcDocumentView<StlPlusDocument> getDocumentView(StlPlusDocument document) {
        return StlPlusViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<StlPlusSpec> getSpecificationDescriptor(StlPlusSpec spec) {
        return new StlPlusSpecUI(spec, false);
    }

    @Override
    public Class<StlPlusDocument> getDocumentType() {
        return StlPlusDocument.class;
    }

    @Override
    public Class<StlPlusSpec> getSpecType() {
        return StlPlusSpec.class;
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("jdplus/stl/desktop/plugin/tangent_red.png", false);
    }

    @Override
    public void showDocument(WorkspaceItem<StlPlusDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            StlPlusTopComponent view = new StlPlusTopComponent(item);
            view.open();
            view.requestActive();
        }
    }

}
