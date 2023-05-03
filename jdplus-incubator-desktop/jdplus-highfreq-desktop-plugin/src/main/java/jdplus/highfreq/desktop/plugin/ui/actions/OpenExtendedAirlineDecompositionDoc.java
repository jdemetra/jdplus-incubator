/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.desktop.plugin.ui.actions;

import jdplus.highfreq.desktop.plugin.ExtendedAirlineDecompositionDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.highfreq.base.core.extendedairline.decomposiiton.ExtendedAirlineDecompositionDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.tramoseats.ui.OpenExtendedAirlineDecompositionDoc")
@ActionRegistration(displayName = "#CTL_OpenExtendedAirlineDecompositionDoc")
@ActionReferences({
    @ActionReference(path = ExtendedAirlineDecompositionDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenExtendedAirlineDecompositionDoc=Open")
public class OpenExtendedAirlineDecompositionDoc implements ActionListener {

    private final WsNode context;

    public OpenExtendedAirlineDecompositionDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<ExtendedAirlineDecompositionDocument> doc = context.getWorkspace().searchDocument(context.lookup(), ExtendedAirlineDecompositionDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(ExtendedAirlineDecompositionDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
