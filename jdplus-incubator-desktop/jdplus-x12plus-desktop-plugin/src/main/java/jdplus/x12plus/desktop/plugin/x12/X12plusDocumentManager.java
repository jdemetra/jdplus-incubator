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
package jdplus.x12plus.desktop.plugin.x12;

import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.x12plus.base.api.MX11plusSpec;
import jdplus.x12plus.base.api.X12plusSpec;
import jdplus.x12plus.base.core.x12.X12plusDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 3000)
public class X12plusDocumentManager extends AbstractWorkspaceTsItemManager<X12plusSpec, X12plusDocument> {


    public static final LinearId ID = new LinearId(MX11plusSpec.FAMILY, "documents", X12plusSpec.METHOD);
    public static final String PATH = "x12plus.doc";
    public static final String ITEMPATH = "x12plus.doc.item";
    public static final String CONTEXTPATH = "x12plus.doc.context";

    @Override
    protected String getItemPrefix() {
        return "X12plusDoc";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public X12plusDocument createNewObject() {
        return new X12plusDocument();
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
    public Class<X12plusDocument> getItemClass() {
        return X12plusDocument.class;
    }

}
