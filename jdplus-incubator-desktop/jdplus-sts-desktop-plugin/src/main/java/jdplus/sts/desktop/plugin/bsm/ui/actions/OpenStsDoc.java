/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts.desktop.plugin.bsm.ui.actions;

import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.sts.base.core.StsDocument;
import jdplus.sts.desktop.plugin.bsm.StsDocumentManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.highfreq.ui.OpenStsDoc")
@ActionRegistration(displayName = "#CTL_OpenStsDoc")
@ActionReferences({
    @ActionReference(path = StsDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenStsDoc=Open")
public class OpenStsDoc implements ActionListener {

    private final WsNode context;

    public OpenStsDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<StsDocument> doc = context.getWorkspace().searchDocument(context.lookup(), StsDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(StsDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
