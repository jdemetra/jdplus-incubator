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

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import java.awt.Color;
import javax.swing.Icon;
import jdplus.x12plus.base.api.X12plusSpec;
import jdplus.x12plus.base.core.x12.X12plusDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class X12plusUIFactory implements DocumentUIServices<X12plusSpec, X12plusDocument> {

//    public static X12plusUIFactory INSTANCE=new X12plusUIFactory();
    @Override
    public IProcDocumentView<X12plusDocument> getDocumentView(X12plusDocument document) {
        return X12plusViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<X12plusSpec> getSpecificationDescriptor(X12plusSpec spec) {
        return new X12plusSpecUI(spec, false);
    }

    @Override
    public Class<X12plusDocument> getDocumentType() {
        return X12plusDocument.class;
    }

    @Override
    public Class<X12plusSpec> getSpecType() {
        return X12plusSpec.class;
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("jdplus/x11plus/desktop/plugin/tangent_red.png", false);
    }

    @Override
    public void showDocument(WorkspaceItem<X12plusDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            X12plusTopComponent view = new X12plusTopComponent(item);
            view.open();
            view.requestActive();
        }
    }

}
