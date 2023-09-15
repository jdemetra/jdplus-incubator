/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sts.desktop.plugin.bsm;

import jdplus.sts.base.api.StsSpec;
import jdplus.sts.base.core.StsDocument;
import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 3000)
public class StsDocumentManager extends AbstractWorkspaceTsItemManager<StsSpec, StsDocument> {


    public static final LinearId ID = new LinearId(StsSpec.FAMILY, "documents", StsSpec.METHOD);
    public static final String PATH = "sts.doc";
    public static final String ITEMPATH = "sts.doc.item";
    public static final String CONTEXTPATH = "sts.doc.context";

    @Override
    protected String getItemPrefix() {
        return "StsDoc";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public StsDocument createNewObject() {
        return new StsDocument();
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
    public Class<StsDocument> getItemClass() {
        return StsDocument.class;
    }

}
