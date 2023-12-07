/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x12plus.desktop.plugin.x12.ui.actions;

import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.x12plus.base.core.x12.X12plusDocument;
import jdplus.x12plus.desktop.plugin.x12.X12plusDocumentManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.x12plus.ui.OpenX12plusDoc")
@ActionRegistration(displayName = "#CTL_OpenX12plusDoc")
@ActionReferences({
    @ActionReference(path = X12plusDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenX12plusDoc=Open")
public class OpenX12Doc implements ActionListener {

    private final WsNode context;

    public OpenX12Doc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<X12plusDocument> doc = context.getWorkspace().searchDocument(context.lookup(), X12plusDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(X12plusDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
