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
import jdplus.highfreq.base.core.extendedairline.decomposition.ExtendedAirlineDecompositionDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public final class ExtendedAirlineDecompositionDocFileRepository extends AbstractFileItemRepository< ExtendedAirlineDecompositionDocument > {

    @Override
    public boolean load(WorkspaceItem<ExtendedAirlineDecompositionDocument> item) {
        return loadFile(item, (ExtendedAirlineDecompositionDocument o) -> {
            o.setLocked(true);
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<ExtendedAirlineDecompositionDocument> doc, DemetraVersion version) {
        ExtendedAirlineDecompositionDocument element = doc.getElement();
       
        Map<String, String> meta=new HashMap<>(element.getMetadata());
        TsMeta.TIMESTAMP.store(meta, LocalDateTime.now(Clock.systemDefaultZone()));
        element.updateMetadata(meta);
        
        return storeFile(doc, element, version, doc::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<ExtendedAirlineDecompositionDocument> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<ExtendedAirlineDecompositionDocument> getSupportedType() {
        return ExtendedAirlineDecompositionDocument.class;
    }

}
