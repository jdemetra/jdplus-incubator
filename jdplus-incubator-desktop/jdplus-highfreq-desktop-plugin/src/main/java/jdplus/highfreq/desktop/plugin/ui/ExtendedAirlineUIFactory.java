/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq.desktop.plugin.ui;

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineDocument;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import java.awt.Color;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class ExtendedAirlineUIFactory implements DocumentUIServices<ExtendedAirlineModellingSpec, ExtendedAirlineDocument> {
    
//    public static FractionalAirlineUIFactory INSTANCE=new FractionalAirlineUIFactory();

    @Override
    public IProcDocumentView<ExtendedAirlineDocument> getDocumentView(ExtendedAirlineDocument document) {
        return ExtendedAirlineViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<ExtendedAirlineModellingSpec> getSpecificationDescriptor(ExtendedAirlineModellingSpec spec) {
        return new ExtendedAirlineSpecUI(spec, false);
    }

    @Override
    public Class<ExtendedAirlineDocument> getDocumentType() {
        return ExtendedAirlineDocument.class; 
    }

    @Override
    public Class<ExtendedAirlineModellingSpec> getSpecType() {
        return ExtendedAirlineModellingSpec.class; 
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
    public void showDocument(WorkspaceItem<ExtendedAirlineDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            ExtendedAirlineTopComponent view = new ExtendedAirlineTopComponent(item);
            view.open();
            view.requestActive();
        }
    }

}
