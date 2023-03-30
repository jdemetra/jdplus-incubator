/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl.desktop.plugin.stl;

import jdplus.toolkit.base.api.DemetraVersion;
import demetra.desktop.workspace.AbstractFileItemRepository;
import demetra.desktop.workspace.WorkspaceItem;
import demetra.desktop.workspace.WorkspaceItemRepository;
import jdplus.toolkit.base.tsp.TsMeta;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jdplus.stl.base.core.stlplus.StlPlusDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public final class StlDocFileRepository extends AbstractFileItemRepository< StlPlusDocument > {

    @Override
    public boolean load(WorkspaceItem<StlPlusDocument> item) {
        return loadFile(item, (StlPlusDocument o) -> {
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<StlPlusDocument> doc, DemetraVersion version) {
        StlPlusDocument element = doc.getElement();
       
        Map<String, String> meta=new HashMap<>(element.getMetadata());
        TsMeta.TIMESTAMP.store(meta, LocalDateTime.now());
        element.updateMetadata(meta);
        
        return storeFile(doc, element, version, doc::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<StlPlusDocument> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<StlPlusDocument> getSupportedType() {
        return StlPlusDocument.class;
    }

}
