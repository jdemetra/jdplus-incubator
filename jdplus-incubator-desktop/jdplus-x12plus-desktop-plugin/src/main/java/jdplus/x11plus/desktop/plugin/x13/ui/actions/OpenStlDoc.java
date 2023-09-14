/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus.desktop.plugin.x13.ui.actions;

import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.x11plus.base.core.x13.X13plusDocument;
import jdplus.x11plus.desktop.plugin.x13.X13plusDocumentManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.x13plus.ui.OpenX13plusDoc")
@ActionRegistration(displayName = "#CTL_OpenX13plusDoc")
@ActionReferences({
    @ActionReference(path = X13plusDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenX13plusDoc=Open")
public class OpenStlDoc implements ActionListener {

    private final WsNode context;

    public OpenStlDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<X13plusDocument> doc = context.getWorkspace().searchDocument(context.lookup(), X13plusDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(X13plusDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
