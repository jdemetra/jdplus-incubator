/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus.desktop.plugin.x13;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.desktop.plugin.workspace.AbstractFileItemRepository;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemRepository;
import jdplus.toolkit.base.tsp.TsMeta;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jdplus.x11plus.base.core.x13.X13plusDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public final class X13plusDocFileRepository extends AbstractFileItemRepository< X13plusDocument > {

    @Override
    public boolean load(WorkspaceItem<X13plusDocument> item) {
        return loadFile(item, (X13plusDocument o) -> {
            o.setLocked(true);
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<X13plusDocument> doc, DemetraVersion version) {
        X13plusDocument element = doc.getElement();
       
        Map<String, String> meta=new HashMap<>(element.getMetadata());
        TsMeta.TIMESTAMP.store(meta, LocalDateTime.now(Clock.systemDefaultZone()));
        element.updateMetadata(meta);
        
        return storeFile(doc, element, version, doc::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<X13plusDocument> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<X13plusDocument> getSupportedType() {
        return X13plusDocument.class;
    }

}
