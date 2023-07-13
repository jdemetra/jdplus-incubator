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

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import java.awt.Color;
import javax.swing.Icon;
import jdplus.x11plus.base.api.X13plusSpec;
import jdplus.x11plus.base.core.x13.X13plusDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class X13plusUIFactory implements DocumentUIServices<X13plusSpec, X13plusDocument> {

//    public static X13plusUIFactory INSTANCE=new X13plusUIFactory();
    @Override
    public IProcDocumentView<X13plusDocument> getDocumentView(X13plusDocument document) {
        return X13plusViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<X13plusSpec> getSpecificationDescriptor(X13plusSpec spec) {
        return new X13plusSpecUI(spec, false);
    }

    @Override
    public Class<X13plusDocument> getDocumentType() {
        return X13plusDocument.class;
    }

    @Override
    public Class<X13plusSpec> getSpecType() {
        return X13plusSpec.class;
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
    public void showDocument(WorkspaceItem<X13plusDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            X13plusTopComponent view = new X13plusTopComponent(item);
            view.open();
            view.requestActive();
        }
    }

}
