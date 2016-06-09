/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.sourcefilesearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import biz.isphere.base.internal.ClipboardHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.internal.IEditor;
import biz.isphere.core.sourcemembercopy.rse.CopyMemberDialog;
import biz.isphere.core.sourcemembercopy.rse.CopyMemberService;

public class SearchResultViewer {

    private String connectionName;
    private String searchString;
    private SearchResult[] _searchResults;
    private TableViewer tableViewerMembers;
    private Table tableMembers;
    private Object[] selectedItemsMembers;
    private Shell shell;
    private TableViewer tableViewerStatements;
    private Table tableStatements;
    private String[] statements;
    private boolean isEditMode;

    private class LabelProviderTableViewerMembers extends LabelProvider implements ITableLabelProvider {

        private static final String UNKNOWN = "*UNKNOWN"; //$NON-NLS-1$

        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return ((SearchResult)element).getLibrary() + "-" + ((SearchResult)element).getFile() + "(" + ((SearchResult)element).getMember() //$NON-NLS-1$ //$NON-NLS-2$
                    + ")" + " - \"" + ((SearchResult)element).getDescription() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            return UNKNOWN;
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private class ContentProviderTableViewerMembers implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            return _searchResults;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class SorterTableViewerMembers extends ViewerSorter {

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            int result = ((SearchResult)e1).getLibrary().compareTo(((SearchResult)e2).getLibrary());
            if (result == 0) {
                result = ((SearchResult)e1).getFile().compareTo(((SearchResult)e2).getFile());
                if (result == 0) {
                    result = ((SearchResult)e1).getMember().compareTo(((SearchResult)e2).getMember());
                }
            }
            return result;
        }

    }

    private class LabelProviderStatements extends LabelProvider implements ITableLabelProvider {

        private static final String UNKNOWN = "*UNKNOWN"; //$NON-NLS-1$

        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return (String)element;
            }
            return UNKNOWN;
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    private class ContentProviderStatements implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            return statements;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class TableMembersMenuAdapter extends MenuAdapter {

        private Menu menuTableMembers;
        private MenuItem menuItemOpenEditor;
        private MenuItem menuItemOpenViewer;
        private MenuItem menuItemSelectAll;
        private MenuItem menuItemDeselectAll;
        private MenuItem menuItemInvertSelection;
        private MenuItem menuCopySelected;
        private MenuItem menuItemRemove;
        private MenuItem menuItemSeparator;
        private MenuItem menuCopySelectedMembers;

        public TableMembersMenuAdapter(Menu menuTableMembers) {
            this.menuTableMembers = menuTableMembers;
        }

        @Override
        public void menuShown(MenuEvent event) {
            retrieveSelectedTableItems();
            destroyMenuItems();
            createMenuItems();
        }

        public void destroyMenuItems() {
            dispose(menuItemOpenEditor);
            dispose(menuItemOpenViewer);
            dispose(menuItemSelectAll);
            dispose(menuItemDeselectAll);
            dispose(menuItemInvertSelection);
            dispose(menuCopySelected);
            dispose(menuItemRemove);
            dispose(menuItemSeparator);
            dispose(menuCopySelectedMembers);
        }

        private void dispose(MenuItem menuItem) {
            if (!((menuItem == null) || (menuItem.isDisposed()))) {
                menuItem.dispose();
            }
        }

        public void createMenuItems() {

            if (hasSelectedItems()) {
                menuItemOpenEditor = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemOpenEditor.setText(Messages.Open_for_edit);
                menuItemOpenEditor.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_OPEN_EDITOR));
                menuItemOpenEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemOpenEditor(0);
                    }
                });

                menuItemOpenViewer = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemOpenViewer.setText(Messages.Open_for_browse);
                menuItemOpenViewer.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_OPEN_VIEWER));
                menuItemOpenViewer.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemOpenViewer(0);
                    }
                });
            }

            if (hasItems()) {
                menuItemSelectAll = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemSelectAll.setText(Messages.Select_all);
                menuItemSelectAll.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_SELECT_ALL));
                menuItemSelectAll.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemSelectAll();
                    }
                });

                menuItemDeselectAll = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemDeselectAll.setText(Messages.Deselect_all);
                menuItemDeselectAll.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_DESELECT_ALL));
                menuItemDeselectAll.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemDeselectAll();
                    }
                });
            }

            if (hasSelectedItems()) {
                menuItemInvertSelection = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemInvertSelection.setText(Messages.Invert_selection);
                menuItemInvertSelection.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_INVERT_SELECTION));
                menuItemInvertSelection.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemInvertSelectedItems();
                    }
                });

                menuCopySelected = new MenuItem(menuTableMembers, SWT.NONE);
                menuCopySelected.setText(Messages.Copy_selected);
                menuCopySelected.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_COPY_TO_CLIPBOARD));
                menuCopySelected.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemCopySelectedItems();
                    }
                });

                menuItemRemove = new MenuItem(menuTableMembers, SWT.NONE);
                menuItemRemove.setText(Messages.Remove);
                menuItemRemove.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_REMOVE));
                menuItemRemove.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemRemoveSelectedItems();
                    }
                });

                if (IBMiHostContributionsHandler.hasContribution()) {

                    menuItemSeparator = new MenuItem(menuTableMembers, SWT.SEPARATOR);

                    menuCopySelectedMembers = new MenuItem(menuTableMembers, SWT.NONE);
                    menuCopySelectedMembers.setText("Copy members");
                    menuCopySelectedMembers.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_COPY_MEMBERS_TO));
                    menuCopySelectedMembers.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            executeMenuItemCopySelectedMembers();
                        }
                    });
                }
            }

        }
    }

    private class TableStatementsMenuAdapter extends MenuAdapter {

        private Menu menuTableStatements;
        private MenuItem menuItemOpenEditor;
        private MenuItem menuItemOpenViewer;

        public TableStatementsMenuAdapter(Menu menuTableStatements) {
            this.menuTableStatements = menuTableStatements;
        }

        @Override
        public void menuShown(MenuEvent event) {
            retrieveSelectedTableItems();
            destroyMenuItems();
            createMenuItems();
        }

        public void destroyMenuItems() {
            if (!((menuItemOpenEditor == null) || (menuItemOpenEditor.isDisposed()))) {
                menuItemOpenEditor.dispose();
            }
            if (!((menuItemOpenViewer == null) || (menuItemOpenViewer.isDisposed()))) {
                menuItemOpenViewer.dispose();
            }
        }

        public void createMenuItems() {

            if (hasSelectedItems()) {
                menuItemOpenEditor = new MenuItem(menuTableStatements, SWT.NONE);
                menuItemOpenEditor.setText(Messages.Open_for_edit);
                menuItemOpenEditor.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_OPEN_EDITOR));
                menuItemOpenEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemOpenEditor(getStatementLine());
                    }
                });

                menuItemOpenViewer = new MenuItem(menuTableStatements, SWT.NONE);
                menuItemOpenViewer.setText(Messages.Open_for_browse);
                menuItemOpenViewer.setImage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_OPEN_VIEWER));
                menuItemOpenViewer.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        executeMenuItemOpenViewer(getStatementLine());
                    }
                });
            }
        }
    }

    public SearchResultViewer(String connectionName, String searchString, SearchResult[] _searchResults) {
        this.connectionName = connectionName;
        this.searchString = searchString;
        this._searchResults = _searchResults;
    }

    /**
     * @wbp.parser.entryPoint
     */
    public void createContents(Composite parent) {

        shell = parent.getShell();

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        final SashForm sashFormSearchResult = new SashForm(container, SWT.BORDER);
        sashFormSearchResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tableViewerMembers = new TableViewer(sashFormSearchResult, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        tableViewerMembers.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                retrieveSelectedTableItems();
                setStatements();
            }
        });
        tableViewerMembers.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (tableViewerMembers.getSelection() instanceof IStructuredSelection) {

                    IStructuredSelection structuredSelection = (IStructuredSelection)tableViewerMembers.getSelection();
                    SearchResult _searchResult = (SearchResult)structuredSelection.getFirstElement();

                    IEditor editor = ISpherePlugin.getEditor();

                    if (editor != null) {

                        editor.openEditor(connectionName, _searchResult.getLibrary(), _searchResult.getFile(), _searchResult.getMember(), 0,
                            getEditMode());

                    }
                }
            }
        });
        tableViewerMembers.setSorter(new SorterTableViewerMembers());
        tableViewerMembers.setLabelProvider(new LabelProviderTableViewerMembers());
        tableViewerMembers.setContentProvider(new ContentProviderTableViewerMembers());

        tableMembers = tableViewerMembers.getTable();
        tableMembers.setLinesVisible(true);
        tableMembers.setHeaderVisible(true);
        tableMembers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final TableColumn tableColumnMember = new TableColumn(tableMembers, SWT.NONE);
        tableColumnMember.setWidth(800);
        tableColumnMember.setText(Messages.Member);

        final Menu menuTableMembers = new Menu(tableMembers);
        menuTableMembers.addMenuListener(new TableMembersMenuAdapter(menuTableMembers));
        tableMembers.setMenu(menuTableMembers);

        tableViewerMembers.setInput(new Object());

        tableViewerStatements = new TableViewer(sashFormSearchResult, SWT.FULL_SELECTION | SWT.BORDER);
        tableViewerStatements.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {

                if (selectedItemsMembers != null && selectedItemsMembers.length == 1) {

                    SearchResult _searchResult = (SearchResult)selectedItemsMembers[0];

                    if (tableViewerStatements.getSelection() instanceof IStructuredSelection) {

                        IStructuredSelection structuredSelection = (IStructuredSelection)tableViewerStatements.getSelection();

                        String statement = (String)structuredSelection.getFirstElement();
                        int statementLine = 0;
                        int startAt = statement.indexOf('(');
                        int endAt = statement.indexOf(')');
                        if (startAt != -1 && endAt != -1 && startAt < endAt) {
                            String _statementLine = statement.substring(startAt + 1, endAt);
                            try {
                                statementLine = Integer.parseInt(_statementLine);
                            } catch (NumberFormatException e1) {
                            }
                        }

                        IEditor editor = ISpherePlugin.getEditor();

                        if (editor != null) {

                            editor.openEditor(connectionName, _searchResult.getLibrary(), _searchResult.getFile(), _searchResult.getMember(),
                                statementLine, getEditMode());

                        }

                    }

                }

            }
        });
        tableViewerStatements.setLabelProvider(new LabelProviderStatements());
        tableViewerStatements.setContentProvider(new ContentProviderStatements());

        tableStatements = tableViewerStatements.getTable();
        tableStatements.setHeaderVisible(true);
        tableStatements.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final TableColumn tableColumnStatement = new TableColumn(tableStatements, SWT.NONE);
        tableColumnStatement.setWidth(800);
        tableColumnStatement.setText(Messages.Statement);

        final Menu menuTableStatement = new Menu(tableStatements);
        menuTableStatement.addMenuListener(new TableStatementsMenuAdapter(menuTableStatement));
        tableStatements.setMenu(menuTableStatement);

        setStatements();
        tableViewerStatements.setInput(new Object());

        sashFormSearchResult.setWeights(new int[] { 1, 1 });

    }

    private int getStatementLine() {

        int statementLine = 0;

        if (selectedItemsMembers != null && selectedItemsMembers.length == 1) {

            if (tableViewerStatements.getSelection() instanceof IStructuredSelection) {

                IStructuredSelection structuredSelection = (IStructuredSelection)tableViewerStatements.getSelection();

                String statement = (String)structuredSelection.getFirstElement();
                int startAt = statement.indexOf('(');
                int endAt = statement.indexOf(')');
                if (startAt != -1 && endAt != -1 && startAt < endAt) {
                    String _statementLine = statement.substring(startAt + 1, endAt);
                    try {
                        statementLine = Integer.parseInt(_statementLine);
                    } catch (NumberFormatException e1) {
                    }
                }
            }
        }

        return statementLine;
    }

    private String getEditMode() {

        if (isEditMode) {
            return IEditor.EDIT;
        } else {
            return IEditor.BROWSE;
        }
    }

    private void retrieveSelectedTableItems() {
        if (tableViewerMembers.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)tableViewerMembers.getSelection();
            selectedItemsMembers = structuredSelection.toArray();
        } else {
            selectedItemsMembers = new Object[0];
        }
    }

    private void executeMenuItemSelectAll() {

        Object[] objects = new Object[tableMembers.getItemCount()];
        for (int idx = 0; idx < tableMembers.getItemCount(); idx++) {
            objects[idx] = tableViewerMembers.getElementAt(idx);
        }
        tableViewerMembers.setSelection(new StructuredSelection(objects), true);
        selectedItemsMembers = objects;
        tableMembers.setFocus();

        setStatements();

    }

    private void executeMenuItemDeselectAll() {

        tableViewerMembers.setSelection(new StructuredSelection(), true);
        selectedItemsMembers = new Object[0];

        setStatements();

    }

    private void executeMenuItemOpenEditor(int statement) {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {

            for (int idx = 0; idx < selectedItemsMembers.length; idx++) {

                SearchResult _searchResult = (SearchResult)selectedItemsMembers[idx];
                editor.openEditor(connectionName, _searchResult.getLibrary(), _searchResult.getFile(), _searchResult.getMember(), statement,
                    IEditor.EDIT);
            }
        }
    }

    private void executeMenuItemOpenViewer(int statement) {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {

            for (int idx = 0; idx < selectedItemsMembers.length; idx++) {

                SearchResult _searchResult = (SearchResult)selectedItemsMembers[idx];
                editor.openEditor(connectionName, _searchResult.getLibrary(), _searchResult.getFile(), _searchResult.getMember(), statement,
                    IEditor.BROWSE);
            }
        }
    }

    private void executeMenuItemInvertSelectedItems() {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {
            ContentProviderTableViewerMembers contentProvider = (ContentProviderTableViewerMembers)tableViewerMembers.getContentProvider();
            List<Object> allItems = new ArrayList<Object>(Arrays.asList(contentProvider.getElements(null)));
            allItems.removeAll(Arrays.asList(selectedItemsMembers));
            executeMenuItemDeselectAll();
            tableViewerMembers.setSelection(new StructuredSelection(allItems), true);
        }
    }

    private void executeMenuItemCopySelectedItems() {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {
            if (selectedItemsMembers.length > 0) {
                StringBuilder list = new StringBuilder();
                list.append(Messages.Library);
                list.append("\t");
                list.append(Messages.File);
                list.append("\t");
                list.append(Messages.Member);
                list.append("\t");
                list.append(Messages.Description);
                list.append("\n");
                for (Object item : selectedItemsMembers) {
                    SearchResult searchResult = (SearchResult)item;
                    list.append(searchResult.getLibrary());
                    list.append("\t");
                    list.append(searchResult.getFile());
                    list.append("\t");
                    list.append(searchResult.getMember());
                    list.append("\t");
                    list.append(searchResult.getDescription());
                    list.append("\n");
                }
                ClipboardHelper.setText(list.toString());
            }
        }
    }

    private void executeMenuItemCopySelectedMembers() {

        CopyMemberService jobDescription = new CopyMemberService(connectionName);

        for (int idx = 0; idx < selectedItemsMembers.length; idx++) {
            SearchResult _searchResult = (SearchResult)selectedItemsMembers[idx];
            jobDescription.addItem(_searchResult.getFile(), _searchResult.getLibrary(), _searchResult.getMember());
        }

        CopyMemberDialog dialog = new CopyMemberDialog(getShell());
        dialog.setContent(jobDescription);
        dialog.open();
    }

    private void executeMenuItemRemoveSelectedItems() {

        IEditor editor = ISpherePlugin.getEditor();

        if (editor != null) {
            List<SearchResult> searchResult = new ArrayList<SearchResult>(Arrays.asList(_searchResults));
            searchResult.removeAll(Arrays.asList(selectedItemsMembers));
            _searchResults = searchResult.toArray(new SearchResult[searchResult.size()]);
            tableViewerMembers.remove(selectedItemsMembers);
        }
    }

    private void setStatements() {
        if (selectedItemsMembers == null || selectedItemsMembers.length == 0) {
            statements = new String[1];
            statements[0] = Messages.No_selection;
        } else if (selectedItemsMembers.length == 1) {
            SearchResult _searchResult = (SearchResult)selectedItemsMembers[0];
            SearchResultStatement[] _statements = _searchResult.getStatements();
            statements = new String[_statements.length];
            for (int idx = 0; idx < _statements.length; idx++) {
                statements[idx] = "(" + Integer.toString(_statements[idx].getStatement()) + ") " + _statements[idx].getLine(); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            statements = new String[1];
            statements[0] = Messages.Multiple_selection;
        }
        tableViewerStatements.refresh();

    }

    public String getConnectionName() {
        return connectionName;
    }

    public String getSearchString() {
        return searchString;
    }

    public SearchResult[] getSearchResults() {
        return _searchResults;
    }

    public boolean hasItems() {

        if (tableViewerMembers != null && tableViewerMembers.getContentProvider() != null) {
            ContentProviderTableViewerMembers contentProvider = (ContentProviderTableViewerMembers)tableViewerMembers.getContentProvider();
            if (contentProvider.getElements(null).length > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSelectedItems() {

        if (selectedItemsMembers != null && selectedItemsMembers.length > 0) {
            return true;
        }
        return false;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        tableViewerMembers.addSelectionChangedListener(listener);
    }

    public void removeSelectedItems() {
        executeMenuItemRemoveSelectedItems();
    }

    public void invertSelectedItems() {
        executeMenuItemInvertSelectedItems();
    }

    public void setEditEnabled(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    public boolean isEditEnabled() {
        return this.isEditMode;
    }

    private Shell getShell() {
        return shell;
    }
}
