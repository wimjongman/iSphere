/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.swt.widgets;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import biz.isphere.core.swt.widgets.WidgetFactory;
import biz.isphere.journalexplorer.core.Messages;

public class SqlEditor extends Composite {

    private ContentAssistText textSqlEditor;
    private Button btnExecute;

    public SqlEditor(Composite parent, int style) {
        super(parent, style);
        createContentArea();
    }

    public void setWhereClause(String whereClause) {

        if (textSqlEditor == null || textSqlEditor.isDisposed()) {
            return;
        }

        if (whereClause != null) {
            textSqlEditor.setText(whereClause);
        } else {
            textSqlEditor.setText(""); //$NON-NLS-1$
        }
    }

    public String getWhereClause() {
        return textSqlEditor.getText();
    }

    private void createContentArea() {

        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        Label labelWhere = new Label(this, SWT.NONE);
        labelWhere.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        labelWhere.setText(Messages.SqlEditor_WHERE);
        labelWhere.setToolTipText(Messages.Tooltip_SqlEditor_Text);

        textSqlEditor = new ContentAssistText(this);
        textSqlEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
        // textSqlEditor.setContentAssistProposals(new String[] { "FOO", "BAA",
        // "FOOBAA" });
        // textSqlEditor.setContentAssistProposalsLabels(new String[] {
        // "FOO-Label", "BAA-Label", "FOOBAA-Label" });

        textSqlEditor.getTextWidget().setToolTipText(Messages.Tooltip_SqlEditor_Text);
        textSqlEditor.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
            public void verifyKey(VerifyEvent event) {
                if (isCtrlEnter(event)) {
                    Event keyUpEvent = new Event();
                    keyUpEvent.character = event.character;
                    keyUpEvent.data = event.data;
                    keyUpEvent.display = event.display;
                    keyUpEvent.doit = event.doit;
                    keyUpEvent.keyCode = event.keyCode;
                    keyUpEvent.keyLocation = event.keyLocation;
                    keyUpEvent.stateMask = event.stateMask;
                    keyUpEvent.time = event.time;
                    keyUpEvent.widget = event.widget;
                    btnExecute.notifyListeners(SWT.Selection, keyUpEvent);
                    event.doit = false;
                }
            }

            private boolean isCtrlEnter(KeyEvent event) {
                return event.stateMask == SWT.CTRL && event.character == SWT.CR;
            }
        });

        textSqlEditor.getTextWidget().addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {

                if (!e.doit) {
                    return;
                }

                if (e.stateMask == SWT.CTRL) {
                    switch (e.character) {
                    case ' ':
                        textSqlEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
                        break;

                    case '\032':
                        textSqlEditor.doOperation(ITextOperationTarget.UNDO);
                    }

                }
            }
        });

        btnExecute = WidgetFactory.createPushButton(this, "&Execute");
        btnExecute.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
    }

    public void addSelectionListener(SelectionListener listener) {
        btnExecute.addSelectionListener(listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        btnExecute.removeSelectionListener(listener);
    }

    public void setContentAssistProposals(ContentAssistProposal[] contentAssistProposals) {
        textSqlEditor.setContentAssistProposals(contentAssistProposals);
    }

    @Override
    public void dispose() {
        textSqlEditor.dispose();
        super.dispose();
    }
}
