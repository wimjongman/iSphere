/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.ui.preferencepages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import biz.isphere.core.swt.widgets.WidgetFactory;
import biz.isphere.journalexplorer.core.ISphereJournalExplorerCorePlugin;
import biz.isphere.journalexplorer.core.Messages;
import biz.isphere.journalexplorer.core.preferences.Preferences;
import biz.isphere.journalexplorer.core.ui.model.BaseTypeViewerFactory;
import biz.isphere.journalexplorer.core.ui.model.JournalEntryAppearance;
import biz.isphere.journalexplorer.core.ui.model.JournalEntryColumn;
import biz.isphere.journalexplorer.core.ui.widgets.JournalEntryAppearanceEditor;

public class JournalExplorerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private JournalEntryAppearanceEditor editor;
    private JournalEntryColumn[] columns;

    private Preferences preferences;

    private Group groupColors;
    private Button checkboxEnableColoring;

    public JournalExplorerPreferencePage() {
        super();

        this.preferences = Preferences.getInstance();

        setPreferenceStore(ISphereJournalExplorerCorePlugin.getDefault().getPreferenceStore());
        getPreferenceStore();
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        checkboxEnableColoring = WidgetFactory.createCheckbox(container);
        checkboxEnableColoring.setText(Messages.Enable_coloring);
        checkboxEnableColoring.setToolTipText(Messages.Enable_coloring_tooltip);
        checkboxEnableColoring.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        checkboxEnableColoring.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                validateAll();
                setControlsEnablement();
            }
        });

        groupColors = new Group(container, SWT.NONE);
        groupColors.setLayout(new GridLayout());
        groupColors.setLayoutData(new GridData(GridData.FILL, SWT.FILL, false, true));
        groupColors.setText(Messages.Colors);

        editor = new JournalEntryAppearanceEditor(groupColors);
        editor.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true));

        Button clearColors = WidgetFactory.createPushButton(groupColors, Messages.Clear_Colors);
        clearColors.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent event) {
                performClearColors();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });

        setScreenToValues();

        return container;
    }

    private void performClearColors() {

        columns = editor.getInput();

        for (JournalEntryColumn column : columns) {
            column.setColor(null);
        }

        editor.setInput(columns);
    }

    @Override
    protected void performApply() {
        setStoreToValues();
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        setScreenToDefaultValues();
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        setStoreToValues();
        return super.performOk();
    }

    protected void setStoreToValues() {

        preferences.setColoringEnabled(checkboxEnableColoring.getSelection());

        for (JournalEntryColumn column : columns) {
            preferences.setJounalEntryAppearance(new JournalEntryAppearance(column.getName(), column.getColor()));
        }
    }

    protected void setScreenToValues() {

        checkboxEnableColoring.setSelection(preferences.isColoringEnabled());

        BaseTypeViewerFactory factory = new BaseTypeViewerFactory();
        columns = factory.getAvailableTableColumns();

        setScreenValues();
    }

    protected void setScreenToDefaultValues() {

        checkboxEnableColoring.setSelection(preferences.getInitialColoringEnabled());

        for (JournalEntryColumn column : columns) {
            column.setColor(preferences.getInitialColumnColor(column.getName()));
        }

        setScreenValues();
    }

    protected void setScreenValues() {

        editor.setInput(columns);

        setControlsEnablement();
    }

    private void setControlsEnablement() {

        if (checkboxEnableColoring.getSelection()) {
            setCompositeEnablement(groupColors, true);
        } else {
            setCompositeEnablement(groupColors, false);
        }
    }

    private void setCompositeEnablement(Composite composite, boolean enabled) {

        composite.setEnabled(enabled);

        for (Control control : composite.getChildren()) {
            control.setEnabled(enabled);
        }
    }

    private boolean validateAll() {

        return clearError();
    }

    private boolean clearError() {

        setErrorMessage(null);
        setValid(true);

        return true;
    }

}
