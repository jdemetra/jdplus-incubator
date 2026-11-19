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
package jdplus.tdarima.desktop.plugin;

import java.awt.Color;
import javax.swing.Icon;
import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.advancedsa.base.core.tdarima.TdArimaDocument;
import jdplus.tdarima.desktop.plugin.ui.ModelSpecUI;
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class TdArimaUIFactory implements DocumentUIServices<TdModelSpec, TdArimaDocument> {

    //    public static TdArimaUIFactory INSTANCE=new TdArimaUIFactory();
    @Override
    public IProcDocumentView<TdArimaDocument> getDocumentView(TdArimaDocument document) {
        return TdArimaViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<TdModelSpec> getSpecificationDescriptor(TdModelSpec spec) {
        return new ModelSpecUI(spec, false);
    }

    @Override
    public Class<TdArimaDocument> getDocumentType() {
        return TdArimaDocument.class;
    }

    @Override
    public Class<TdModelSpec> getSpecType() {
        return TdModelSpec.class;
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("jdplus/advancedqa/desktop/plugin/tangent_red.png", false);
    }

    @Override
    public void showDocument(WorkspaceItem<TdArimaDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            TdArimaTopComponent view = new TdArimaTopComponent(item);
            view.open();
            view.requestActive();
        }
    }
}
