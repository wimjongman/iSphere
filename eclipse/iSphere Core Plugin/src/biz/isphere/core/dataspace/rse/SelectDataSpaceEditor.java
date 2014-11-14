package biz.isphere.core.dataspace.rse;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import biz.isphere.base.jface.dialogs.XDialog;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.dataspaceeditor.model.DEditor;
import biz.isphere.core.internal.Size;

public class SelectDataSpaceEditor extends XDialog {

    private DEditor selectedEditor;

    private DEditor[] dEditors;

    private TableViewer tableViewer;

    public SelectDataSpaceEditor(Shell parentShell, DEditor[] dEditors) {
        super(parentShell);
        this.dEditors = dEditors;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.Select_editor);
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite mainArea = new Composite(parent, SWT.NONE);
        GridLayout mainAreaLayout = new GridLayout();
        mainArea.setLayout(mainAreaLayout);
        mainArea.setLayoutData(createGridDataFillAndGrab());

        tableViewer = new TableViewer(mainArea, SWT.FULL_SELECTION | SWT.BORDER);
        tableViewer.setLabelProvider(new LabelProviderTableViewer());
        tableViewer.setContentProvider(new ContentProviderTableViewer());
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (setSelectedItem()) {
                    close();
                }
            }
        });

        Table table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(createGridDataFillAndGrab());

        TableColumn columnName = new TableColumn(table, SWT.NONE);
        columnName.setWidth(Size.getSize(100));
        columnName.setText(Messages.Name);

        TableColumn columnDescription = new TableColumn(table, SWT.NONE);
        columnDescription.setWidth(Size.getSize(100));
        columnDescription.setText(Messages.Description);

        // dDialogsList.setItems(getListItems());
        tableViewer.setInput(getListItems());

        return dialogArea;
    }

    private String[] getListItems() {

        java.util.List<String> items = new ArrayList<String>();
        for (DEditor dEditor : dEditors) {
            items.add(dEditor.getName());
        }

        return items.toArray(new String[items.size()]);
    }

    public DEditor getSelectedDialog() {
        return selectedEditor;
    }

    private GridData createGridDataFillAndGrab() {
        GridData layoutData = new GridData();
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        return layoutData;
    }

    private boolean setSelectedItem() {
        if (tableViewer.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)tableViewer.getSelection();
            if (structuredSelection.getFirstElement() instanceof DEditor) {
                selectedEditor = (DEditor)structuredSelection.getFirstElement();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Messages.OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.Cancel, false);
    }

    @Override
    protected void okPressed() {
        if (setSelectedItem()) {
            super.okPressed();
        }
    }

    /**
     * Overridden to make this dialog resizable.
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * Overridden to provide a default size to {@link XDialog}.
     */
    @Override
    protected Point getDefaultSize() {
        // Point point = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        return new Point(250, 300);
    }

    /**
     * Overridden to let {@link XDialog} store the state of this dialog in a
     * separate section of the dialog settings file.
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return super.getDialogBoundsSettings(ISpherePlugin.getDefault().getDialogSettings());
    }

    private class ContentProviderTableViewer implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return dEditors;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private class LabelProviderTableViewer extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            DEditor dEditor = (DEditor)element;
            if (columnIndex == 0) {
                return dEditor.getName();
            } else if (columnIndex == 1) {
                return dEditor.getDescription();
            }
            return "*UNKNOWN"; //$NON-NLS-1$
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
}
