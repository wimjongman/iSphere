/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.dataareaeditor.delegates;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import biz.isphere.core.Messages;
import biz.isphere.core.dataareaeditor.DataAreaEditor;

/**
 * Editor delegate that edits a *LGL data area.
 * <p>
 * The delegate implements all the specific stuff that is needed to edit a data
 * area of type *LGL.
 */
public class LogicalDataAreaEditorDelegate extends AbstractDataAreaEditorDelegate {

    private Button dataAreaText;

    public LogicalDataAreaEditorDelegate(DataAreaEditor aDataAreaEditor) {
        super(aDataAreaEditor);
    }

    @Override
    public void createPartControl(Composite aParent) {

        Composite editorArea = createEditorArea(aParent, 3);

        Label lblValue = new Label(editorArea, SWT.NONE);
        GridData lblValueLayoutData = new GridData();
        lblValueLayoutData.widthHint = DataAreaEditor.VALUE_LABEL_WIDTH_HINT;
        lblValueLayoutData.verticalAlignment = GridData.BEGINNING;
        lblValue.setLayoutData(lblValueLayoutData);
        lblValue.setText(Messages.Value_colon);

        Composite horizontalSpacer = new Composite(editorArea, SWT.NONE);
        GridData horizontalSpacerLayoutData = new GridData();
        horizontalSpacerLayoutData.widthHint = DataAreaEditor.SPACER_WIDTH_HINT;
        horizontalSpacerLayoutData.heightHint = 1;
        horizontalSpacer.setLayoutData(horizontalSpacerLayoutData);

        dataAreaText = new Button(editorArea, SWT.CHECK);
        GridData dataAreaTextLayoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        dataAreaTextLayoutData.widthHint = 160;
        dataAreaText.setLayoutData(dataAreaTextLayoutData);

        Composite verticalSpacer = new Composite(aParent, SWT.NONE);
        GridData verticalSpacerLayoutData = new GridData();
        verticalSpacerLayoutData.grabExcessHorizontalSpace = true;
        verticalSpacerLayoutData.grabExcessVerticalSpace = true;
        verticalSpacerLayoutData.horizontalSpan = 2;
        verticalSpacer.setLayoutData(verticalSpacerLayoutData);

        // Set screen value
        dataAreaText.setSelection(getWrappedDataArea().getBooleanValue());

        // Add 'dirty' listener
        dataAreaText.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

            public void widgetSelected(SelectionEvent arg0) {
                setEditorDirty();
            }
        });
    }

    @Override
    public void doSave(IProgressMonitor aMonitor) {
        Throwable exception = getWrappedDataArea().setValue(dataAreaText.getSelection());
        handleSaveResult(aMonitor, exception);
    }

    @Override
    public void setInitialFocus() {
        dataAreaText.setFocus();
    }

}
