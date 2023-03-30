/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl.desktop.plugin.stl.ui.actions;

import jdplus.stl.desktop.plugin.stl.StlPlusDocumentManager;
import demetra.desktop.workspace.DocumentUIServices;
import demetra.desktop.workspace.WorkspaceItem;
import demetra.desktop.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.stl.base.core.stlplus.StlPlusDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.highfreq.ui.OpenStlPlusDoc")
@ActionRegistration(displayName = "#CTL_OpenStlPlusDoc")
@ActionReferences({
    @ActionReference(path = StlPlusDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenStlPlusDoc=Open")
public class OpenStlDoc implements ActionListener {

    private final WsNode context;

    public OpenStlDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<StlPlusDocument> doc = context.getWorkspace().searchDocument(context.lookup(), StlPlusDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(StlPlusDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
