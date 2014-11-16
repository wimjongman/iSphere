/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.dataspaceeditordesigner.listener;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import biz.isphere.core.dataspace.rse.DE;
import biz.isphere.core.dataspaceeditordesigner.gui.dialog.DWidgetDialog;
import biz.isphere.core.dataspaceeditordesigner.model.AbstractDWidget;
import biz.isphere.core.dataspaceeditordesigner.model.DEditor;
import biz.isphere.core.dataspaceeditordesigner.model.DataSpaceEditorManager;
import biz.isphere.core.dataspaceeditordesigner.rse.IDialogEditor;

public class NewWidgetListener extends SelectionAdapter {

    private IDialogEditor editor;
    private Shell shell;
    private DEditor dialog;

    private DataSpaceEditorManager manager;

    public NewWidgetListener(IDialogEditor editor, Shell parent, DEditor dialog) {
        this.editor = editor;
        this.shell = parent;
        this.dialog = dialog;
        this.manager = new DataSpaceEditorManager();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        super.widgetDefaultSelected(e);
    }

    @Override
    public void widgetSelected(SelectionEvent event) {
        if (event.getSource() instanceof Widget) {
            performAddWidget(event);
        }
    }

    @SuppressWarnings("unchecked")
    private void performAddWidget(SelectionEvent event) {
        Widget widget = (Widget)event.getSource();
        Object object = widget.getData(DE.KEY_DWIDGET_CLASS);
        if (object instanceof Class) {
            Class<AbstractDWidget> clazz = (Class<AbstractDWidget>)object;
            DWidgetDialog newDWidgetDialog = new DWidgetDialog(shell, clazz);
            if (newDWidgetDialog.open() == Dialog.OK) {
                AbstractDWidget dWidget = manager.createWidgetFromTemplate(newDWidgetDialog.getWidget());
                editor.addWidget(dialog, dWidget);
            }
        }
    }
}
