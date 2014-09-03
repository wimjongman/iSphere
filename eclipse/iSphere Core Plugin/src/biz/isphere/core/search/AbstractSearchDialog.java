/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.search;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import biz.isphere.base.internal.StringHelper;
import biz.isphere.base.jface.dialogs.XDialog;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;

public abstract class AbstractSearchDialog extends XDialog implements Listener {

    private boolean _editor;
    private SearchArgumentsListEditor _listEditor;
    private Text textString;
    private Button buttonCaseNo;
    private Button buttonCaseYes;
    private Text textFromColumn;
    private Text textToColumn;
    private SearchOptions _searchOptions;
    private String _string;
    private String _case;
    private int _fromColumn;
    private int _toColumn;
    private Button okButton;
    private int maxColumns;

    // iSphere settings
    private static final String TO_COLUMN = "toColumn";
    private static final String FROM_COLUMN = "fromColumn";

    // CMOne settings
    private static final String TEXT_STRING = "textString";
    private static final String IGNORE_CASE = "ignoreCase";

    public AbstractSearchDialog(Shell parentShell, int maxColumns, boolean searchArgumentsListEditor) {
        super(parentShell);
        this.maxColumns = maxColumns;
        _fromColumn = 1;
        _toColumn = maxColumns;
        if (searchArgumentsListEditor) {
            _editor = ISpherePlugin.isSearchArgumentsListEditor();
        } else {
            _editor = false;
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite)super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        if (_editor) {

            /*
             * Use iSphere's SearchArgumentsListEditor
             */

            Composite _searchArguments = new Composite(container, SWT.NONE);
            _searchArguments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            _searchArguments.setLayout(new GridLayout());

            _listEditor = ISpherePlugin.getSearchArgumentsListEditorProvider().getListEditor();
            _listEditor.createControl(_searchArguments);
            _listEditor.setListener(this);

        }

        Group groupAttributes = new Group(container, SWT.NONE);
        groupAttributes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        groupAttributes.setText(Messages.Attributes);
        groupAttributes.setLayout(new GridLayout(2, false));

        if (!_editor) {

            /*
             * Create dialog for CMOne
             */

            String searchString = getSearchArgument();
            if (searchString.equals("")) {
                searchString = Messages.Enter_search_string_here;
            }

            Label labelString = new Label(groupAttributes, SWT.NONE);
            labelString.setText(Messages.String_colon);

            textString = new Text(groupAttributes, SWT.BORDER);
            textString.setText(searchString);
            textString.setTextLimit(40);
            textString.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            textString.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent event) {
                    setOKButtonEnablement();
                }
            });

            Label labelCaseSensitive = new Label(groupAttributes, SWT.NONE);
            labelCaseSensitive.setText(Messages.Case_sensitive_colon);

            Composite groupCaseSensitive = new Composite(groupAttributes, SWT.NONE);
            GridLayout editableLayout = new GridLayout();
            editableLayout.numColumns = 2;
            groupCaseSensitive.setLayout(editableLayout);

            buttonCaseNo = new Button(groupCaseSensitive, SWT.RADIO);
            buttonCaseNo.setText(Messages.No);
            buttonCaseNo.setSelection(true);

            buttonCaseYes = new Button(groupCaseSensitive, SWT.RADIO);
            buttonCaseYes.setText(Messages.Yes);
            buttonCaseYes.setSelection(false);

        }

        Label labelFromColumn = new Label(groupAttributes, SWT.NONE);
        labelFromColumn.setText(Messages.From_column_colon);

        textFromColumn = new Text(groupAttributes, SWT.BORDER);
        textFromColumn.setTextLimit(10);
        textFromColumn.setLayoutData(new GridData(50, SWT.DEFAULT));
        textFromColumn.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                _fromColumn = 0;
                if (textFromColumn.getText().toUpperCase().trim().equals("*START")) {
                    _fromColumn = 1;
                } else {
                    try {
                        _fromColumn = Integer.parseInt(textFromColumn.getText().trim());
                    } catch (NumberFormatException e1) {
                    }
                }
                setOKButtonEnablement();
            }
        });

        Label labelToColumn = new Label(groupAttributes, SWT.NONE);
        labelToColumn.setText(Messages.To_column_colon);

        textToColumn = new Text(groupAttributes, SWT.BORDER);
        textToColumn.setTextLimit(10);
        textToColumn.setLayoutData(new GridData(50, SWT.DEFAULT));
        textToColumn.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                _toColumn = 0;
                if (textToColumn.getText().toUpperCase().trim().equals("*END")) {
                    _toColumn = maxColumns;
                } else {
                    try {
                        _toColumn = Integer.parseInt(textToColumn.getText().trim());
                        if (_toColumn > maxColumns) {
                            _toColumn = maxColumns;
                        }
                    } catch (NumberFormatException e1) {
                    }
                }
                setOKButtonEnablement();
            }
        });

        addElements(container);

        Group groupArea = new Group(container, SWT.NONE);
        groupArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        groupArea.setText(Messages.Area);
        groupArea.setLayout(new FillLayout(SWT.HORIZONTAL));

        List listArea = new List(groupArea, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        listArea.setItems(getItems());

        loadScreenValues();

        return container;
    }

    public void setOKButtonEnablement() {
        if (okButton == null) {
            return;
        }

        if (!_editor) {
            if (StringHelper.isNullOrEmpty(textString.getText())) {
                okButton.setEnabled(false);
                return;
            }
        } else {
            if (!isSearchStringValid()) {
                okButton.setEnabled(false);
                return;
            }
        }

        if (_fromColumn == 0 || _toColumn == 0 || _fromColumn > _toColumn || _toColumn > maxColumns) {
            okButton.setEnabled(false);
            return;
        }

        if (!checkElements()) {
            okButton.setEnabled(false);
            return;
        }

        okButton.setEnabled(true);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        setOKButtonEnablement();
        return control;
    }

    @Override
    protected void okPressed() {

        storeScreenValues();

        if (_editor) {

            _searchOptions = new SearchOptions(_listEditor.getIsMatchAll(), true);
            java.util.List<SearchArgument> searchArguments = _listEditor.getSearchArguments(_fromColumn, _toColumn);
            for (SearchArgument searchArgument : searchArguments) {
                if (!StringHelper.isNullOrEmpty(searchArgument.getString())) {
                    _searchOptions.addSearchArgument(searchArgument);
                }
            }
            setElementsSearchOptions(_searchOptions);

            StringBuilder tBuffer = new StringBuilder();
            for (SearchArgument searchArgument : searchArguments) {
                if (searchArgument.getString().trim().length() > 0) {
                    if (tBuffer.length() > 0) {
                        tBuffer.append("/");
                    }
                    tBuffer.append(searchArgument.getString());
                }
            }
            _string = tBuffer.toString();

        } else {

            setSearchArgument(textString.getText());
            _string = textString.getText().trim();

            if (buttonCaseNo.getSelection()) {
                _case = SearchOptions.CASE_IGNORE;
            } else {
                _case = SearchOptions.CASE_MATCH;
            }

            _searchOptions = new SearchOptions(true, true);
            _searchOptions.addSearchArgument(new SearchArgument(getSearchArgument(), getFromColumn(), getToColumn(), getCase()));
            setElementsSearchOptions(_searchOptions);

        }

        super.okPressed();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID, Messages.OK, true);
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.Cancel, false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(getTitle());
    }

    public SearchOptions getSearchOptions() {
        return _searchOptions;
    }

    public String getString() {
        return _string;
    }

    public String getCase() {
        return _case;
    }

    public int getFromColumn() {
        return _fromColumn;
    }

    public int getToColumn() {
        return _toColumn;
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
        return getShell().computeSize(800, 600, true);
    }

    /**
     * Overridden to let {@link XDialog} store the state of this dialog in a
     * separate section of the dialog settings file.
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return super.getDialogBoundsSettings(ISpherePlugin.getDefault().getDialogSettings());
    }

    public void handleEvent(Event anEvent) {
        setOKButtonEnablement();
    }

    private boolean isSearchStringValid() {
        java.util.List<SearchArgument> tSearchArguments = _listEditor.getSearchArguments(0, 0);
        for (SearchArgument tSearchArgument : tSearchArguments) {
            if (StringHelper.isNullOrEmpty(tSearchArgument.getString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Restores the screen values of the last search search.
     */
    private void loadScreenValues() {
        if (_editor) {
            _listEditor.loadScreenValues(getDialogBoundsSettings());
        } else {
            textString.setText(loadValue(TEXT_STRING, Messages.Enter_search_string_here));
            buttonCaseNo.setSelection(loadBooleanValue(IGNORE_CASE, false));
            buttonCaseYes.setSelection(!buttonCaseNo.getSelection());
        }

        loadColumnButtonsSelection();

        loadElementValues();
    }

    private void loadColumnButtonsSelection() {
        textFromColumn.setText(loadValue(FROM_COLUMN, "*START"));
        textToColumn.setText(loadValue(TO_COLUMN, "*END"));
    }

    /**
     * Stores the screen values that are preserved for the next search.
     */
    private void storeScreenValues() {
        if (_editor) {
            _listEditor.storeScreenValues(getDialogBoundsSettings());
        } else {
            storeValue(TEXT_STRING, textString.getText());
            storeValue(IGNORE_CASE, buttonCaseNo.getSelection());
            buttonCaseYes.setSelection(!buttonCaseNo.getSelection());
        }

        saveColumnButtonsSelection();
        
        saveElementValues();
    }

    private void saveColumnButtonsSelection() {
        storeValue(FROM_COLUMN, textFromColumn.getText());
        storeValue(TO_COLUMN, textToColumn.getText());
    }

    public abstract String getTitle();

    public abstract String[] getItems();

    public abstract String getSearchArgument();

    public abstract void setSearchArgument(String argument);

    public void addElements(Composite container) {};
    
    public void loadElementValues() {};
    
    public void saveElementValues() {};

    public boolean checkElements() {
        return true;
    }
 
    public void setElementsSearchOptions(SearchOptions _searchOptions) {};
    
}
