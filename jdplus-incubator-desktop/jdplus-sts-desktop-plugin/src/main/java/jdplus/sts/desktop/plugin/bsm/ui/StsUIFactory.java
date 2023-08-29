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

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import java.awt.Color;
import javax.swing.Icon;
import jdplus.sts.base.api.StsSpec;
import jdplus.sts.base.core.StsDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class StsUIFactory implements DocumentUIServices<StsSpec, StsDocument> {

//    public static StsUIFactory INSTANCE=new StsUIFactory();
    @Override
    public IProcDocumentView<StsDocument> getDocumentView(StsDocument document) {
        return StsViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<StsSpec> getSpecificationDescriptor(StsSpec spec) {
        return new StsSpecUI(spec, false);
    }

    @Override
    public Class<StsDocument> getDocumentType() {
        return StsDocument.class;
    }

    @Override
    public Class<StsSpec> getSpecType() {
        return StsSpec.class;
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
    public void showDocument(WorkspaceItem<StsDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            StsTopComponent view = new StsTopComponent(item);
            view.open();
            view.requestActive();
        }
    }

}
