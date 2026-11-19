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

import jdplus.advancedsa.base.api.tdarima.TdModelSpec;
import jdplus.advancedsa.base.core.tdarima.TdArimaDocument;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = WorkspaceItemManager.class, position = 3000)
public class TdArimaDocumentManager extends AbstractWorkspaceTsItemManager<TdModelSpec, TdArimaDocument> {

    public static final LinearId ID = new LinearId(TdModelSpec.FAMILY, "documents", TdModelSpec.METHOD);
    public static final String PATH = "tdarima.doc";
    public static final String ITEMPATH = "tdarima.doc.item";
    public static final String CONTEXTPATH = "tdarima.doc.context";

    @Override
    protected String getItemPrefix() {
        return "TdArimaDoc";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public TdArimaDocument createNewObject() {
        return new TdArimaDocument();
    }

    @Override
    public WorkspaceItemManager.ItemType getItemType() {
        return WorkspaceItemManager.ItemType.Doc;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    public WorkspaceItemManager.Status getStatus() {
        return WorkspaceItemManager.Status.Certified;
    }

    @Override
    public Class<TdArimaDocument> getItemClass() {
        return TdArimaDocument.class;
    }
}
