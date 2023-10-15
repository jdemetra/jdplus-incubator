/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.desktop.plugin.bsm;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.desktop.plugin.workspace.AbstractFileItemRepository;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemRepository;
import jdplus.toolkit.base.tsp.TsMeta;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jdplus.sts.base.core.StsDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public final class StsDocFileRepository extends AbstractFileItemRepository< StsDocument > {

    @Override
    public boolean load(WorkspaceItem<StsDocument> item) {
        return loadFile(item, (StsDocument o) -> {
            o.setLocked(true);
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<StsDocument> doc, DemetraVersion version) {
        StsDocument element = doc.getElement();
       
        Map<String, String> meta=new HashMap<>(element.getMetadata());
        TsMeta.TIMESTAMP.store(meta, LocalDateTime.now(Clock.systemDefaultZone()));
        element.updateMetadata(meta);
        
        return storeFile(doc, element, version, doc::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<StsDocument> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<StsDocument> getSupportedType() {
        return StsDocument.class;
    }

}
