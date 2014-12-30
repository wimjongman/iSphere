package biz.isphere.core.dataspaceeditordesigner.listener;

import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

import biz.isphere.core.dataspaceeditordesigner.model.DEditor;
import biz.isphere.core.dataspaceeditordesigner.rse.IDialogEditor;

public class TreeViewerSelectionChangedListener implements ISelectionChangedListener {

    private Set<ToolItem> toolItems;
    private IDialogEditor editor;

    public TreeViewerSelectionChangedListener(Set<ToolItem> toolItems, IDialogEditor editor) {
        this.toolItems = toolItems;
        this.editor = editor;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        
        DEditor[] selectedEditors = editor.getSelectedDataSpaceEditors();
        
        if (selectedEditors.length > 0) {
            setToolItemsEnablement(true);
        } else {
            setToolItemsEnablement(false);
        }
        
        if (selectedEditors.length == 1) {
            editor.setDescription(selectedEditors[0].getDescription());
        } else {
            editor.setDescription("");
        }
    }

    private void setToolItemsEnablement(boolean isEnabled) {
        for (ToolItem toolItem : toolItems) {
            toolItem.setEnabled(isEnabled);
        }
    }
}
