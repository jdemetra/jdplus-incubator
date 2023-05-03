/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.desktop.plugin;

import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineDocument;
import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.highfreq.base.api.ExtendedAirlineModellingSpec;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 2000)
public class ExtendedAirlineDocumentManager extends AbstractWorkspaceTsItemManager<ExtendedAirlineModellingSpec, ExtendedAirlineDocument> {


    public static final LinearId ID = new LinearId(ExtendedAirlineModellingSpec.FAMILY, "documents", ExtendedAirlineModellingSpec.METHOD);
    public static final String PATH = "extendedairline.doc";
    public static final String ITEMPATH = "extendedairline.doc.item";
    public static final String CONTEXTPATH = "extendedairline.doc.context";

    @Override
    protected String getItemPrefix() {
        return "ExtendedAirlineDoc";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public ExtendedAirlineDocument createNewObject() {
        return new ExtendedAirlineDocument();
    }

    @Override
    public ItemType getItemType() {
        return ItemType.Doc;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    public Status getStatus() {
        return Status.Certified;
    }

    @Override
    public Class<ExtendedAirlineDocument> getItemClass() {
        return ExtendedAirlineDocument.class;
    }

}
