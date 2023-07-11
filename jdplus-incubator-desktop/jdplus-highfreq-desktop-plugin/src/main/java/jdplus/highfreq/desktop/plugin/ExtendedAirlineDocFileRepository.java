/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.desktop.plugin;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.desktop.plugin.workspace.AbstractFileItemRepository;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemRepository;
import jdplus.toolkit.base.tsp.TsMeta;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jdplus.highfreq.base.core.extendedairline.ExtendedAirlineDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public final class ExtendedAirlineDocFileRepository extends AbstractFileItemRepository< ExtendedAirlineDocument > {

    @Override
    public boolean load(WorkspaceItem<ExtendedAirlineDocument> item) {
        return loadFile(item, (ExtendedAirlineDocument o) -> {
            o.setLocked(true);
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<ExtendedAirlineDocument> doc, DemetraVersion version) {
        ExtendedAirlineDocument element = doc.getElement();
       
        Map<String, String> meta=new HashMap<>(element.getMetadata());
        TsMeta.TIMESTAMP.store(meta, LocalDateTime.now(Clock.systemDefaultZone()));
        element.updateMetadata(meta);
        
        return storeFile(doc, element, version, doc::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<ExtendedAirlineDocument> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<ExtendedAirlineDocument> getSupportedType() {
        return ExtendedAirlineDocument.class;
    }

}
