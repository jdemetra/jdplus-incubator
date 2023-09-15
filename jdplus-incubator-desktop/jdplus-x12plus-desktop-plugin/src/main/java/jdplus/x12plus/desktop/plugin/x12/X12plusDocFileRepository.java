/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x12plus.desktop.plugin.x12;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.desktop.plugin.workspace.AbstractFileItemRepository;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemRepository;
import jdplus.toolkit.base.tsp.TsMeta;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jdplus.x12plus.base.core.x12.X12plusDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public final class X12plusDocFileRepository extends AbstractFileItemRepository< X12plusDocument > {

    @Override
    public boolean load(WorkspaceItem<X12plusDocument> item) {
        return loadFile(item, (X12plusDocument o) -> {
            o.setLocked(true);
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<X12plusDocument> doc, DemetraVersion version) {
        X12plusDocument element = doc.getElement();
       
        Map<String, String> meta=new HashMap<>(element.getMetadata());
        TsMeta.TIMESTAMP.store(meta, LocalDateTime.now(Clock.systemDefaultZone()));
        element.updateMetadata(meta);
        
        return storeFile(doc, element, version, doc::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<X12plusDocument> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<X12plusDocument> getSupportedType() {
        return X12plusDocument.class;
    }

}
