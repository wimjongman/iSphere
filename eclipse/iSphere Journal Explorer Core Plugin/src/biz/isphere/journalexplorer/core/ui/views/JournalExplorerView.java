/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Initial idea and development: Isaac Ramirez Herrera
 * Continued and adopted to iSphere: iSphere Project Team
 *******************************************************************************/

package biz.isphere.journalexplorer.core.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import biz.isphere.base.internal.ExceptionHelper;
import biz.isphere.journalexplorer.core.ISphereJournalExplorerCorePlugin;
import biz.isphere.journalexplorer.core.Messages;
import biz.isphere.journalexplorer.core.internals.SelectionProviderIntermediate;
import biz.isphere.journalexplorer.core.model.File;
import biz.isphere.journalexplorer.core.model.JournalEntry;
import biz.isphere.journalexplorer.core.model.MetaDataCache;
import biz.isphere.journalexplorer.core.model.MetaTable;
import biz.isphere.journalexplorer.core.ui.actions.CompareSideBySideAction;
import biz.isphere.journalexplorer.core.ui.actions.ConfigureParsersAction;
import biz.isphere.journalexplorer.core.ui.actions.GenericRefreshAction;
import biz.isphere.journalexplorer.core.ui.actions.OpenJournalOutfileAction;
import biz.isphere.journalexplorer.core.ui.actions.ToggleHighlightUserEntriesAction;
import biz.isphere.journalexplorer.core.ui.widgets.JournalEntriesViewer;

public class JournalExplorerView extends ViewPart implements ISelectionChangedListener, SelectionListener {

    public static final String ID = "biz.isphere.journalexplorer.core.ui.views.JournalExplorerView"; //$NON-NLS-1$

    private OpenJournalOutfileAction openJournalOutputFileAction;
    private CompareSideBySideAction compareSideBySideAction;
    private ToggleHighlightUserEntriesAction toggleHighlightUserEntriesAction;
    private ConfigureParsersAction configureParsersAction;
    private GenericRefreshAction reloadEntriesAction;

    private SelectionProviderIntermediate selectionProviderIntermediate;
    private ArrayList<JournalEntriesViewer> journalViewers;

    private CTabFolder tabs;

    public JournalExplorerView() {
        this.selectionProviderIntermediate = new SelectionProviderIntermediate();
        this.journalViewers = new ArrayList<JournalEntriesViewer>();
    }

    /**
     * Create contents of the view part.
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));

        tabs = new CTabFolder(container, SWT.TOP | SWT.CLOSE);
        tabs.addSelectionListener(this);
        tabs.addCTabFolder2Listener(new CTabFolder2Listener() {
            public void showList(CTabFolderEvent arg0) {
            }

            public void restore(CTabFolderEvent arg0) {
            }

            public void minimize(CTabFolderEvent arg0) {
            }

            public void maximize(CTabFolderEvent arg0) {
            }

            public void close(CTabFolderEvent event) {
                if (event.item instanceof JournalEntriesViewer) {
                    cleanupClosedTab((JournalEntriesViewer)event.item);
                    setActionEnablement(null);
                }

            }
        });

        createActions();
        initializeToolBar();
        getSite().setSelectionProvider(selectionProviderIntermediate);

        setActionEnablement(null);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Create the actions.
     */
    private void createActions() {

        openJournalOutputFileAction = new OpenJournalOutfileAction(getSite().getShell()) {
            @Override
            public void postRunAction() {
                File outputFile = openJournalOutputFileAction.getOutputFile();
                if (outputFile != null) {
                    createJournalTab(outputFile);
                }
            }
        };

        compareSideBySideAction = new CompareSideBySideAction(getSite().getShell());

        toggleHighlightUserEntriesAction = new ToggleHighlightUserEntriesAction() {
            @Override
            public void postRunAction() {
                refreshAllViewers();
            }
        };

        configureParsersAction = new ConfigureParsersAction(getSite().getShell()) {
            public void run() {
                super.run();
                // if (getButtonPressed() == Dialog.OK) {
                // performReloadJournalEntries();
                // }
            };
        };

        reloadEntriesAction = new GenericRefreshAction() {
            @Override
            protected void postRunAction() {
                performReloadJournalEntries();
            }
        };

    }

    private void createJournalTab(File outputFile) {

        JournalEntriesViewer journalEntriesViewer = null;

        try {

            journalEntriesViewer = new JournalEntriesViewer(tabs, outputFile);
            journalEntriesViewer.setAsSelectionProvider(selectionProviderIntermediate);
            journalEntriesViewer.addSelectionChangedListener(this);

            journalViewers.add(journalEntriesViewer);
            tabs.setSelection(journalEntriesViewer);

            performLoadJournalEntries(journalEntriesViewer);

            setActionEnablement(journalEntriesViewer);

        } catch (Exception exception) {

            MessageDialog.openError(getSite().getShell(), Messages.E_R_R_O_R, ExceptionHelper.getLocalizedMessage(exception));

            if (journalEntriesViewer != null) {
                journalEntriesViewer.removeAsSelectionProvider(selectionProviderIntermediate);
                journalEntriesViewer.dispose();
            }
        }
    }

    private void cleanupClosedTab(JournalEntriesViewer viewer) {

        viewer.removeAsSelectionProvider(selectionProviderIntermediate);
        journalViewers.remove(viewer);
    }

    private void refreshAllViewers() {
        for (JournalEntriesViewer viewer : journalViewers) {
            viewer.refreshTable();
        }
    }

    private void performReloadJournalEntries() {

        JournalEntriesViewer viewer = (JournalEntriesViewer)tabs.getSelection();
        performLoadJournalEntries(viewer);
    }

    private void performLoadJournalEntries(JournalEntriesViewer viewer) {

        try {
            viewer.openJournal();
        } catch (Exception e) {
            ISphereJournalExplorerCorePlugin.logError(ExceptionHelper.getLocalizedMessage(e), e);
            MessageDialog.openError(getSite().getShell(), Messages.E_R_R_O_R, ExceptionHelper.getLocalizedMessage(e));
        }
    }

    /**
     * Initialize the toolbar.
     */
    private void initializeToolBar() {

        IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
        tbm.add(openJournalOutputFileAction);
        tbm.add(toggleHighlightUserEntriesAction);
        tbm.add(compareSideBySideAction);
        tbm.add(configureParsersAction);
        tbm.add(reloadEntriesAction);
    }

    @Override
    public void setFocus() {
    }

    /**
     * Enables the actions for the current viewer,
     * 
     * @param viewer - the selected viewer (tab)
     */
    private void setActionEnablement(JournalEntriesViewer viewer) {

        Collection<MetaTable> joesdParser = MetaDataCache.INSTANCE.getCachedParsers();
        if (joesdParser == null || joesdParser.isEmpty()) {
            configureParsersAction.setEnabled(false);
        } else {
            configureParsersAction.setEnabled(true);
        }

        int numEntries = 0;
        StructuredSelection selection = new StructuredSelection(new JournalEntry[0]);
        if (viewer != null) {

            JournalEntry[] journalEntries = viewer.getInput();
            if (journalEntries != null) {
                numEntries = journalEntries.length;
            }

            selection = viewer.getSelection();
        }

        if (numEntries == 0) {
            reloadEntriesAction.setEnabled(false);
            toggleHighlightUserEntriesAction.setEnabled(false);
        } else {
            reloadEntriesAction.setEnabled(true);
            toggleHighlightUserEntriesAction.setEnabled(true);
        }

        if (selection != null && selection.size() == 2) {
            compareSideBySideAction.setEnabled(true);
        } else {
            compareSideBySideAction.setEnabled(false);
        }

        List<JournalEntry> selectedItems = new ArrayList<JournalEntry>();
        for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof JournalEntry) {
                JournalEntry journalEntry = (JournalEntry)object;
                selectedItems.add(journalEntry);
            }
        }

        compareSideBySideAction.setSelectedItems(selectedItems.toArray(new JournalEntry[selectedItems.size()]));
    }

    /**
     * Called by the viewer, when items are selected by the user.
     */
    public void selectionChanged(SelectionChangedEvent event) {
        setActionEnablement(getCurrentViewer());
    }

    /**
     * Called by the UI, when the user selects a different viewer (tab).
     */
    public void widgetSelected(SelectionEvent event) {

        setActionEnablement(getCurrentViewer());

        // Trigger a SWT selection event
        getCurrentViewer().setSelection(getCurrentViewer().getSelection());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
        widgetSelected(event);
    }

    /**
     * Returns the currently selected viewer (tab).
     * 
     * @return selected viewer
     */
    public JournalEntriesViewer getCurrentViewer() {

        CTabItem tabItem = tabs.getItem(tabs.getSelectionIndex());
        if (tabItem instanceof JournalEntriesViewer) {
            JournalEntriesViewer viewer = (JournalEntriesViewer)tabItem;
            return viewer;
        }

        return null;
    }
}
