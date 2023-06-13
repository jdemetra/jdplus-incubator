/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq.desktop.plugin.ui;

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.highfreq.base.core.extendedairline.decomposition.ExtendedAirlineDecompositionDocument;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.highfreq.base.api.ExtendedAirlineDecompositionSpec;
import java.awt.Color;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class ExtendedAirlineDecompositionUIFactory implements DocumentUIServices<ExtendedAirlineDecompositionSpec, ExtendedAirlineDecompositionDocument> {
    
//    public static FractionalAirlineUIFactory INSTANCE=new FractionalAirlineUIFactory();

    @Override
    public IProcDocumentView<ExtendedAirlineDecompositionDocument> getDocumentView(ExtendedAirlineDecompositionDocument document) {
        return ExtendedAirlineDecompositionViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<ExtendedAirlineDecompositionSpec> getSpecificationDescriptor(ExtendedAirlineDecompositionSpec spec) {
        return new ExtendedAirlineDecompositionSpecUI(spec, false);
    }

    @Override
    public Class<ExtendedAirlineDecompositionDocument> getDocumentType() {
        return ExtendedAirlineDecompositionDocument.class; 
    }

    @Override
    public Class<ExtendedAirlineDecompositionSpec> getSpecType() {
        return ExtendedAirlineDecompositionSpec.class; 
    }

    @Override
    public Color getColor() {
        return Color.GREEN; 
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("jdplus/highfreq/desktop/plugin/tangent_green.png", false);
    }

    @Override
    public void showDocument(WorkspaceItem<ExtendedAirlineDecompositionDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            ExtendedAirlineDecompositionTopComponent view = new ExtendedAirlineDecompositionTopComponent(item);
            view.open();
            view.requestActive();
        }
    }

}
