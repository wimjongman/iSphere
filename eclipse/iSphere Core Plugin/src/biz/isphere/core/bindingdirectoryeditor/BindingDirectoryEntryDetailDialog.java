/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.bindingdirectoryeditor;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.base.jface.dialogs.XDialog;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.internal.Size;

public class BindingDirectoryEntryDetailDialog extends XDialog {

    private String level;

    private int actionType;

    private BindingDirectoryEntry _bindingDirectoryEntry;

    private ArrayList<BindingDirectoryEntry> _bindingDirectoryEntries;

    private BindingDirectoryEntryDetail _bindingDirectoryEntryDetail;

    public BindingDirectoryEntryDetailDialog(Shell parentShell, String level, int actionType, BindingDirectoryEntry _bindingDirectoryEntry,
        ArrayList<BindingDirectoryEntry> _bindingDirectoryEntries) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.level = level;
        this.actionType = actionType;
        this._bindingDirectoryEntry = _bindingDirectoryEntry;
        this._bindingDirectoryEntries = _bindingDirectoryEntries;
    }

    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite)super.createDialogArea(parent);
        container.setLayout(new FillLayout());

        _bindingDirectoryEntryDetail = new BindingDirectoryEntryDetail(level, actionType, _bindingDirectoryEntry, _bindingDirectoryEntries);
        _bindingDirectoryEntryDetail.createContents(container);

        return container;
    }

    protected void okPressed() {
        if (_bindingDirectoryEntryDetail.processButtonPressed()) {
            super.okPressed();
        }
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Messages.getString("OK"), true);
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("Cancel"), false);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("Binding_Directory_Entry"));
    }

    /**
     * Overridden to provide a default size to {@link XDialog}.
     */
    protected Point getDefaultSize() {
        return getShell().computeSize(Size.getSize(450), SWT.DEFAULT, true);
    }

    /**
     * Overriden to let {@link XDialog} store the state of this dialog in a
     * separate section of the dialog settings file.
     */
    protected IDialogSettings getDialogBoundsSettings() {
        return super.getDialogBoundsSettings(ISpherePlugin.getDefault().getDialogSettings());
    }

}
