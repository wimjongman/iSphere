/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.dataspaceeditor.rse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wb.swt.SWTResourceManager;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.dataspace.rse.AbstractWrappedDataSpace;
import biz.isphere.core.dataspace.rse.DE;
import biz.isphere.core.dataspaceeditor.gui.designer.PopupEditor;
import biz.isphere.core.dataspaceeditor.gui.designer.PopupTreeViewer;
import biz.isphere.core.dataspaceeditor.gui.designer.PopupWidget;
import biz.isphere.core.dataspaceeditor.gui.designer.TreeViewContentProvider;
import biz.isphere.core.dataspaceeditor.gui.designer.TreeViewLabelProvider;
import biz.isphere.core.dataspaceeditor.gui.designer.TreeViewSorter;
import biz.isphere.core.dataspaceeditor.listener.AddReferencedObjectListener;
import biz.isphere.core.dataspaceeditor.listener.CollapseAllListener;
import biz.isphere.core.dataspaceeditor.listener.ControlBackgroundPainter;
import biz.isphere.core.dataspaceeditor.listener.DisplayHelpListener;
import biz.isphere.core.dataspaceeditor.listener.DropVetoListerner;
import biz.isphere.core.dataspaceeditor.listener.ExpandAllListener;
import biz.isphere.core.dataspaceeditor.listener.NewDataSpaceEditorListener;
import biz.isphere.core.dataspaceeditor.listener.NewWidgetListener;
import biz.isphere.core.dataspaceeditor.listener.TreeViewerDoubleClickListener;
import biz.isphere.core.dataspaceeditor.listener.TreeViewerSelectionChangedListener;
import biz.isphere.core.dataspaceeditor.model.AbstractDWidget;
import biz.isphere.core.dataspaceeditor.model.DBoolean;
import biz.isphere.core.dataspaceeditor.model.DDataSpaceValue;
import biz.isphere.core.dataspaceeditor.model.DDecimal;
import biz.isphere.core.dataspaceeditor.model.DEditor;
import biz.isphere.core.dataspaceeditor.model.DInteger;
import biz.isphere.core.dataspaceeditor.model.DLongInteger;
import biz.isphere.core.dataspaceeditor.model.DReferencedObject;
import biz.isphere.core.dataspaceeditor.model.DShortInteger;
import biz.isphere.core.dataspaceeditor.model.DTemplateReferencedObject;
import biz.isphere.core.dataspaceeditor.model.DText;
import biz.isphere.core.dataspaceeditor.model.DTinyInteger;
import biz.isphere.core.dataspaceeditor.model.DataSpaceEditorManager;
import biz.isphere.core.dataspaceeditor.repository.DataSpaceEditorRepository;
import biz.isphere.core.internal.ColorHelper;
import biz.isphere.core.internal.ISeries;
import biz.isphere.core.internal.MessageDialogAsync;
import biz.isphere.core.internal.exception.DeleteFileException;
import biz.isphere.core.internal.exception.SaveFileException;

public abstract class AbstractDataSpaceEditorDesigner extends EditorPart implements IDialogEditor, IDropObjectListener {

    public static final String ID = "biz.isphere.rse.dataspaceeditor.rse.DataSpaceEditorDesigner";

    DataSpaceEditorManager manager;
    DataSpaceEditorRepository repository;
    Set<DEditor> dirtyDialogs;
    Set<DEditor> deletedDialogs;

    private TreeViewer treeViewer;
    private Set<Button> newWidgetButtons;
    private Set<ToolItem> assignToolItems;
    private Composite editorArea;
    private Composite editorComposite;
    private DEditor currentEditedEditor;
    private DDataSpaceValue currentSampleData;

    public AbstractDataSpaceEditorDesigner() {
        manager = new DataSpaceEditorManager();
        repository = DataSpaceEditorRepository.getInstance();
        dirtyDialogs = new HashSet<DEditor>();
        deletedDialogs = new HashSet<DEditor>();
        newWidgetButtons = new HashSet<Button>();
        assignToolItems = new HashSet<ToolItem>();
    }

    public void addDataSpaceEditor(DEditor dEditor) {
        treeViewer.add(treeViewer.getInput(), dEditor);
        setEditorDirty(dEditor);
        setDataSpaceEditor(dEditor);
    }

    public void deleteDataSpaceEditors() {
        DEditor[] dEditors = getSelectedEditors();
        for (DEditor dEditor : dEditors) {
            treeViewer.remove(treeViewer.getInput(), new Object[] { dEditor });
            setEditorDeleted(dEditor);
            if (dEditor == currentEditedEditor) {
                setDataSpaceEditor(null);
            }
        }
    }

    public void addWidget(DEditor dEditor, AbstractDWidget widget) {

        manager.createWidgetControlAndAddToParent(editorComposite, widget);
        manager.addWidgetToEditor(dEditor, widget);
        setEditorDirty(dEditor);

        // Refresh the editor
        setDataSpaceEditor(dEditor);
    }

    public void deleteWidget(DEditor dEditor, AbstractDWidget widget) {
        manager.removeWidgetFromEditor(dEditor, widget);
        setEditorDirty(dEditor);

        // Refresh the editor
        setDataSpaceEditor(dEditor);
    }

    public DEditor[] getSelectedDataSpaceEditors() {
        return getSelectedEditors();
    }

    public DReferencedObject[] getSelectedReferencedObjects() {
        return getSelectedReferences();
    }

    public void addReferencedObjectToSelectedEditors(DTemplateReferencedObject template) {
        DReferencedObject referencedObject = manager.createReferencedObjectFromTemplate(template);
        for (DEditor dEditor : getSelectedEditors()) {
            manager.addReferencedObject(dEditor, referencedObject);
            treeViewer.refresh(dEditor);
            treeViewer.setExpandedElements(new Object[] { dEditor });
            setEditorDirty(dEditor);
        }
    }

    public void removeSelectedReferencedObject() {
        DReferencedObject[] referencedObjects = getSelectedReferencedObjects();
        for (DReferencedObject referencedObject : referencedObjects) {
            DEditor dEditor = manager.detachFromParent(referencedObject);
            treeViewer.refresh(dEditor);
            setEditorDirty(dEditor);
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {

        Set<DEditor> processedItems;

        processedItems = new HashSet<DEditor>();
        try {
            for (DEditor dEditor : dirtyDialogs) {
                try {
                    repository.updateOrAddDialog(dEditor);
                    processedItems.add(dEditor);
                } catch (SaveFileException e) {
                    MessageDialog.openError(getShell(), Messages.E_R_R_O_R, e.getLocalizedMessage());
                    return;
                }
            }
        } finally {
            dirtyDialogs.removeAll(processedItems);
        }

        processedItems = new HashSet<DEditor>();
        try {
            for (DEditor dEditor : deletedDialogs) {
                try {
                    repository.deleteEditor(dEditor);
                    processedItems.add(dEditor);
                } catch (DeleteFileException e) {
                    MessageDialog.openError(getShell(), Messages.E_R_R_O_R, e.getLocalizedMessage());
                    return;
                }
            }
        } finally {
            deletedDialogs.removeAll(processedItems);
        }

        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        setPartName("");
    }

    @Override
    public boolean isDirty() {
        int numChanges = dirtyDialogs.size() + deletedDialogs.size();
        if (numChanges > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {

        Composite mainArea = new Composite(parent, SWT.NONE);
        mainArea.setLayout(createGridLayoutNoMargin());
        disableDropSupportOnComposite(mainArea);

        SashForm sashForm = new SashForm(mainArea, SWT.NONE);
        GridData sashFormLayoutData = createGridDataFillAndGrab();
        sashForm.setLayoutData(sashFormLayoutData);

        createLeftPanel(sashForm);
        createRightPanel(sashForm);
        sashForm.setWeights(new int[] { 2, 3 });

        setDataSpaceEditor(null);
    }

    private void createLeftPanel(SashForm sashForm) {

        Composite panel = new Composite(sashForm, SWT.BORDER);
        panel.setLayout(createGridLayoutWithMargin(2));

        createTreeViewer(panel);
        createButtonsPanel(panel);
    }

    private void createTreeViewer(Composite panel) {

        /*
         * Toolbar
         */
        Composite toolbarArea = new Composite(panel, SWT.NONE);
        toolbarArea.setLayout(createGridLayoutNoMargingSimple(2));
        GridData toolbarAreaLayoutData = createGridDataSimple(2);
        toolbarArea.setLayoutData(toolbarAreaLayoutData);

        ToolBar toolBar = new ToolBar(toolbarArea, SWT.FLAT | SWT.RIGHT);

        ToolItem newEditorItem = new ToolItem(toolBar, SWT.NONE);
        newEditorItem.setImage(ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_NEW_DIALOG).createImage());
        newEditorItem.setToolTipText(Messages.New_Editor);
        new ToolItem(toolBar, SWT.SEPARATOR);

        createAssignDataSpaceToolItems(toolBar);
        new ToolItem(toolBar, SWT.SEPARATOR);

        ToolItem expandAllItem = new ToolItem(toolBar, SWT.NONE);
        expandAllItem.setToolTipText(Messages.Expand_all);
        expandAllItem.setImage(ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_EXPAND_ALL).createImage());

        ToolItem collapseAllItem = new ToolItem(toolBar, SWT.NONE);
        collapseAllItem.setToolTipText(Messages.Collapse_all);
        collapseAllItem.setImage(ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_COLLAPSE_ALL).createImage());

        /*
         * Viewer
         */
        Composite treeViewerArea = new Composite(panel, SWT.NONE);
        treeViewerArea.setLayout(new FillLayout(SWT.VERTICAL));
        treeViewerArea.setLayoutData(createGridDataFillAndGrab());

        treeViewer = new TreeViewer(treeViewerArea, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        treeViewer.setContentProvider(new TreeViewContentProvider());
        treeViewer.setLabelProvider(new TreeViewLabelProvider());
        treeViewer.setSorter(new TreeViewSorter());
        treeViewer.setInput(DataSpaceEditorRepository.getInstance().getDataSpaceEditors());
        treeViewer.addDropSupport(DND.DROP_NONE, new Transfer[] {}, new DropVetoListerner());

        Menu treeViewerMenu = new Menu(treeViewer.getControl());
        treeViewerMenu.addMenuListener(new PopupTreeViewer(this));
        treeViewer.getControl().setMenu(treeViewerMenu);

        /*
         * Listeners
         */
        treeViewer.addDoubleClickListener(new TreeViewerDoubleClickListener(this));
        treeViewer.addSelectionChangedListener(new TreeViewerSelectionChangedListener(assignToolItems, this));
        newEditorItem.addSelectionListener(new NewDataSpaceEditorListener(getShell(), this));
        expandAllItem.addSelectionListener(new ExpandAllListener(treeViewer));
        collapseAllItem.addSelectionListener(new CollapseAllListener(treeViewer));
    }

    private void createAssignDataSpaceToolItems(ToolBar toolBar) {

        createAssignDataSpaceToolItem(toolBar, Messages.Assign_data_area, ISpherePlugin.IMAGE_ADD_DATA_AREA, ISeries.DTAARA);
        createAssignDataSpaceToolItem(toolBar, Messages.Assign_user_space, ISpherePlugin.IMAGE_ADD_USER_SPACE, ISeries.USRSPC);
    }

    private void createAssignDataSpaceToolItem(ToolBar parent, String tooltip, String image, String objectType) {

        ToolItem toolItem = new ToolItem(parent, SWT.NONE);
        toolItem.setImage(ISpherePlugin.getImageDescriptor(image).createImage());
        toolItem.setToolTipText(tooltip);
        toolItem.setEnabled(false);
        toolItem.addSelectionListener(new AddReferencedObjectListener(getShell(), this, objectType));

        assignToolItems.add(toolItem);
    }

    private void createButtonsPanel(Composite panel) {

        Composite buttonsArea = new Composite(panel, SWT.NONE);
        buttonsArea.setLayout(createGridLayoutWithMargin());
        GridData newWidgetButtonsLayoutData = createGridDataSimple();
        buttonsArea.setLayoutData(newWidgetButtonsLayoutData);

        Label newHeadline = new Label(buttonsArea, SWT.NONE);
        newHeadline.setText(Messages.Add_field_colon);

        createNewWidgetButton(buttonsArea, Messages.Data_type_Boolean, DBoolean.class);
        createNewWidgetButton(buttonsArea, Messages.Data_type_Decimal, DDecimal.class);
        createNewWidgetButton(buttonsArea, Messages.Data_type_Text, DText.class);
        createNewWidgetButton(buttonsArea, Messages.Data_type_Integer_8_byte, DLongInteger.class);
        createNewWidgetButton(buttonsArea, Messages.Data_type_Integer_4_byte, DInteger.class);
        createNewWidgetButton(buttonsArea, Messages.Data_type_Integer_2_byte, DShortInteger.class);
        createNewWidgetButton(buttonsArea, Messages.Data_type_Integer_1_byte, DTinyInteger.class);
    }

    private void createNewWidgetButton(Composite parent, String label, Class<? extends AbstractDWidget> widgetClass) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        GridData newTextButtonLayoutData = createGridDataSimple();
        newTextButtonLayoutData.widthHint = 80;
        button.setLayoutData(newTextButtonLayoutData);
        button.setData(DE.KEY_DWIDGET_CLASS, widgetClass);

        newWidgetButtons.add(button);
    }

    private void createRightPanel(SashForm sashForm) {
        
        Composite mainArea = new Composite(sashForm, SWT.BORDER);
        mainArea.setLayout(createGridLayoutWithMargin());
        mainArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        ToolBar toolBar = new ToolBar(mainArea, SWT.FLAT | SWT.RIGHT);
        toolBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));

        ToolItem helpItem = new ToolItem(toolBar, SWT.NONE);
        helpItem.setImage(ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_SYSTEM_HELP).createImage());
        helpItem.addSelectionListener(new DisplayHelpListener());

        Label separator = new Label(mainArea, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(createGridDataFillAndGrab(1));

        editorArea = new Composite(mainArea, SWT.NONE);
        editorArea.setLayout(createGridLayoutNoMargin());
        editorArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    @Override
    public void setFocus() {
    }

    public void setDataSpaceEditor(DEditor dialog) {
        Control[] controls = editorArea.getChildren();
        for (Control control : controls) {
            control.dispose();
        }

        if (dialogHasChanged(dialog)) {
            currentSampleData = null;
        }

        editorComposite = createDataSpaceEditor(editorArea, dialog);
        currentEditedEditor = dialog;

        if (currentSampleData != null) {
            copySampleDataToControls(currentSampleData);
        }

        refreshEditor();
    }

    public void setDataAsync(RemoteObject[] remoteObjects) {

        if (remoteObjects == null || remoteObjects.length == 0) {
            MessageDialogAsync.displayError(getShell(), Messages.Dropped_object_does_not_match_expected_type);
            return;
        }

        if (remoteObjects.length > 1) {
            MessageDialogAsync.displayError(getShell(), Messages.Only_one_data_space_object_must_be_selected_to_provide_sample_data);
            return;
        }

        RemoteObject remoteObject = remoteObjects[0];
        if (!(ISeries.DTAARA.equals(remoteObject.getObjectType()) || ISeries.USRSPC.equals(remoteObject.getObjectType()))) {
            MessageDialogAsync.displayError(getShell(), Messages.Only_character_data_areas_or_user_spaces_are_allowed_to_provide_sample_data);
            return;
        }

        try {
            final AbstractWrappedDataSpace dataArea = createDataSpaceWrapper(remoteObject);

            if (ISeries.DTAARA.equals(remoteObject.getObjectType())) {
                if (!AbstractWrappedDataSpace.CHARACTER.equals(dataArea.getDataType())) {
                    MessageDialogAsync.displayError(getShell(), Messages.Only_character_data_areas_or_user_spaces_are_allowed_to_provide_sample_data);
                    return;
                }
            }

            byte[] bytes = dataArea.getBytes();
            final DDataSpaceValue dataAreaValue = DDataSpaceValue.getCharacterInstance(remoteObject, dataArea.getCCSIDEncoding(), bytes);
            if (dataAreaValue == null) {
                return;
            }

            UIJob job = new UIJob(getShell().getDisplay(), Messages.Loading_sample_data) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    copySampleDataToControls(dataAreaValue);
                    return Status.OK_STATUS;
                }
            };
            job.schedule();

        } catch (Exception e) {
            // TODO: show error message
        }

    }

    protected abstract AbstractWrappedDataSpace createDataSpaceWrapper(RemoteObject remoteObject) throws Exception;

    private void copySampleDataToControls(DDataSpaceValue dataAreaValue) {

        Control[] controls = editorComposite.getChildren();
        for (Control control : controls) {
            manager.setControlValue(control, dataAreaValue);
        }

        currentSampleData = dataAreaValue;
    }

    private DEditor[] getSelectedEditors() {
        List<DEditor> tempList = new ArrayList<DEditor>();
        TreeSelection items = (TreeSelection)treeViewer.getSelection();
        for (Iterator<?> iter = items.iterator(); iter.hasNext();) {
            Object item = iter.next();
            if (item instanceof DEditor) {
                tempList.add((DEditor)item);
            }
        }

        return tempList.toArray(new DEditor[tempList.size()]);
    }

    private DReferencedObject[] getSelectedReferences() {
        List<DReferencedObject> tempList = new ArrayList<DReferencedObject>();
        TreeSelection items = (TreeSelection)treeViewer.getSelection();
        for (Iterator<?> iter = items.iterator(); iter.hasNext();) {
            Object item = iter.next();
            if (item instanceof DReferencedObject) {
                tempList.add((DReferencedObject)item);
            }
        }

        return tempList.toArray(new DReferencedObject[tempList.size()]);
    }

    private boolean dialogHasChanged(DEditor dataSpaceEditor) {
        if (dataSpaceEditor == null || currentEditedEditor == null || dataSpaceEditor.getKey().compareTo(currentEditedEditor.getKey()) != 0) {
            return true;
        }
        return false;
    }

    private Composite createDataSpaceEditor(Composite parent, DEditor dEditor) {

        ScrolledComposite scrollableArea = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NONE);
        scrollableArea.setLayout(new GridLayout(1, false));
        scrollableArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrollableArea.setExpandHorizontal(true);
        scrollableArea.setExpandVertical(true);

        int numColumns;
        if (dEditor == null) {
            numColumns = 3;

        } else {
            numColumns = dEditor.getColumns() * 3;
        }
        Composite dialogEditor = manager.createDialogArea(scrollableArea, numColumns);

        if (dEditor != null) {
            addDropSupportOnComposite(dialogEditor);
            Menu dialogAreaMenu = new Menu(dialogEditor);
            dialogAreaMenu.addMenuListener(new PopupEditor(this, getShell(), dEditor));
            dialogEditor.setMenu(dialogAreaMenu);

            AbstractDWidget[] widgets = dEditor.getWidgets();
            Color color = ColorHelper.getBackgroundColorOfSelectedControls();
            for (AbstractDWidget widget : widgets) {
                Control control = manager.createWidgetControlAndAddToParent(dialogEditor, widget);
                if (control instanceof Text) {
                    control.addMouseTrackListener(new ControlBackgroundPainter(color));
                } else if (control instanceof Button) {
                    control.addMouseTrackListener(new ControlBackgroundPainter(color));
                }

                createOffsetAndLengthInfo(dialogEditor, widget);

                Menu controlMenu = new Menu(dialogEditor);
                controlMenu.addMenuListener(new PopupWidget(this, dEditor, manager.getPayloadFromControl(control)));
                control.setMenu(controlMenu);
            }

            Label separator = new Label(editorArea, SWT.SEPARATOR | SWT.HORIZONTAL);
            separator.setLayoutData(createGridDataFillAndGrab(numColumns));

            Label dragDropInfo = new Label(editorArea, SWT.NONE);
            dragDropInfo.setText(Messages.Drag_drop_sample_data_from_the_RSE_tree);
            dragDropInfo.setLayoutData(createGridDataFillAndGrab(numColumns));

        } else {

            Color color = SWTResourceManager.getColor(SWT.COLOR_WHITE);
            dialogEditor.setBackground(color);
            Label label = new Label(dialogEditor, SWT.NONE);
            label.setText(Messages.Double_click_existing_editor_for_editing_or_create_a_new_one);
            label.setBackground(color);
        }

        dialogEditor.layout();
        scrollableArea.setContent(dialogEditor);
        scrollableArea.setMinSize(dialogEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        return dialogEditor;
    }

    private void createOffsetAndLengthInfo(Composite parent, AbstractDWidget widget) {
        StringBuffer sb = new StringBuffer();
        sb.append(widget.getOffset());
        sb.append(" / ");
        sb.append(widget.getLength());

        Label label = new Label(parent, SWT.NONE);
        label.setText(sb.toString());
        label.setToolTipText(Messages.Offset_length_of_selected_data);
    }

    private void addDropSupportOnComposite(Composite dialogEditorComposite) {

        Transfer[] transferTypes = new Transfer[] { PluginTransfer.getInstance() };
        int operations = DND.DROP_MOVE | DND.DROP_COPY;
        DropTargetListener listener = createDropListener(this);

        DropTarget dropTarget = new DropTarget(dialogEditorComposite, operations);
        dropTarget.setTransfer(transferTypes);
        dropTarget.addDropListener(listener);
        dropTarget.setData(this);
    }

    protected abstract AbstractDropDataObjectListerner createDropListener(IDropObjectListener editor);

    private void disableDropSupportOnComposite(Composite dialogEditorComposite) {

        Transfer[] transferTypes = new Transfer[] {};
        int operations = DND.DROP_NONE;
        DropTargetListener listener = new DropVetoListerner();

        DropTarget dropTarget = new DropTarget(dialogEditorComposite, operations);
        dropTarget.setTransfer(transferTypes);
        dropTarget.addDropListener(listener);
    }

    private void refreshEditor() {

        if (currentEditedEditor == null) {
            setPartName(getEditorInput().getName());
        } else {
            setPartName(currentEditedEditor.getName() + " - " + currentEditedEditor.getDescription());
        }

        setControlEnablement();
        editorArea.layout();
    }

    private void setControlEnablement() {

        boolean hasEditor;
        if (currentEditedEditor == null) {
            hasEditor = false;
        } else {
            hasEditor = true;
        }

        // dragDropInfo.setVisible(hasEditor);

        for (Button button : newWidgetButtons) {
            button.setEnabled(hasEditor);
            removeSelectionListener(button);
            if (hasEditor) {
                SelectionListener listener = new NewWidgetListener(this, getShell(), currentEditedEditor);
                button.addSelectionListener(listener);
                button.setData(DE.KEY_SELECTION_LISTENER, listener);
            }
        }
    }

    private void removeSelectionListener(Button button) {
        SelectionListener listener;
        listener = (SelectionListener)button.getData(DE.KEY_SELECTION_LISTENER);
        if (listener != null) {
            button.removeSelectionListener(listener);
            button.setData(DE.KEY_SELECTION_LISTENER, null);
        }
    }

    protected Shell getShell() {
        return this.getSite().getShell();
    }

    private void setEditorDirty(DEditor dialog) {
        dirtyDialogs.add(dialog);
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    private void setEditorDeleted(DEditor dialog) {
        deletedDialogs.add(dialog);
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    private GridLayout createGridLayoutNoMargin() {
        return createGridLayoutNoMargingSimple(1);
    }

    private GridLayout createGridLayoutNoMargingSimple(int columns) {
        GridLayout editorLayout = new GridLayout(columns, false);
        editorLayout.marginHeight = 0;
        editorLayout.marginWidth = 0;
        return editorLayout;
    }

    private GridLayout createGridLayoutWithMargin() {
        return createGridLayoutWithMargin(1);
    }

    private GridLayout createGridLayoutWithMargin(int columns) {
        GridLayout treeAreaLayout = createGridLayoutNoMargingSimple(columns);
        treeAreaLayout.marginHeight = 10;
        treeAreaLayout.marginWidth = 10;
        return treeAreaLayout;
    }

    private GridData createGridDataSimple() {
        return new GridData();
    }

    private GridData createGridDataSimple(int columns) {
        GridData gridData = createGridDataSimple();
        gridData.horizontalSpan = columns;
        return gridData;
    }

    private GridData createGridDataFillAndGrab() {
        GridData layoutData = createGridDataSimple();
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        return layoutData;
    }

    private GridData createGridDataFillAndGrab(int numColumns) {
        GridData layoutData = createGridDataFillAndGrab();
        layoutData.horizontalSpan = numColumns;
        layoutData.grabExcessVerticalSpace = false;
        return layoutData;
    }
}
