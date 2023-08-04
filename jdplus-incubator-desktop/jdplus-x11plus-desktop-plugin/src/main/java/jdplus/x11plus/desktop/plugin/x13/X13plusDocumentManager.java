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
package jdplus.x11plus.desktop.plugin.x13;

import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.x11plus.base.api.MX11plusSpec;
import jdplus.x11plus.base.api.X13plusSpec;
import jdplus.x11plus.base.core.x13.X13plusDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 3000)
public class X13plusDocumentManager extends AbstractWorkspaceTsItemManager<X13plusSpec, X13plusDocument> {


    public static final LinearId ID = new LinearId(MX11plusSpec.FAMILY, "documents", X13plusSpec.METHOD);
    public static final String PATH = "stlplus.doc";
    public static final String ITEMPATH = "stlplus.doc.item";
    public static final String CONTEXTPATH = "stlplus.doc.context";

    @Override
    protected String getItemPrefix() {
        return "X13plusDoc";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public X13plusDocument createNewObject() {
        return new X13plusDocument();
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
    public Class<X13plusDocument> getItemClass() {
        return X13plusDocument.class;
    }

}
