/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.desktop.stl.ui.actions;

import demetra.desktop.mstl.MStlPlusDocumentManager;
import demetra.desktop.workspace.DocumentUIServices;
import demetra.desktop.workspace.WorkspaceItem;
import demetra.desktop.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.mstlplus.MStlPlusDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.stl.ui.OpenMStlDoc")
@ActionRegistration(displayName = "#CTL_OpenMStlDoc")
@ActionReferences({
    @ActionReference(path = MStlPlusDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenMStlDoc=Open")
public class OpenMStlDoc implements ActionListener {

    private final WsNode context;

    public OpenMStlDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<MStlPlusDocument> doc = context.getWorkspace().searchDocument(context.lookup(), MStlPlusDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(MStlPlusDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
