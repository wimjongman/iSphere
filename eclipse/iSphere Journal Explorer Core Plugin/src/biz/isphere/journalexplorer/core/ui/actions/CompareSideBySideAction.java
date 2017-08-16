/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.journalexplorer.core.ISphereJournalExplorerCorePlugin;
import biz.isphere.journalexplorer.core.Messages;
import biz.isphere.journalexplorer.core.model.JournalEntry;
import biz.isphere.journalexplorer.core.model.adapters.JournalProperties;
import biz.isphere.journalexplorer.core.ui.dialogs.SideBySideCompareDialog;

public class CompareSideBySideAction extends Action {

    private static final String IMAGE = ISphereJournalExplorerCorePlugin.IMAGE_HORIZONTAL_RESULTS_VIEW;

    private Shell shell;
    private JournalEntry[] selectedItems;

    public CompareSideBySideAction(Shell shell) {
        super(Messages.JournalEntryView_CompareSideBySide);

        this.shell = shell;

        setImageDescriptor(ISphereJournalExplorerCorePlugin.getDefault().getImageDescriptor(IMAGE));
    }

    public Image getImage() {
        return ISphereJournalExplorerCorePlugin.getDefault().getImage(IMAGE);
    }

    public void setSelectedItems(StructuredSelection selection) {

        List<JournalEntry> selectedItems = new ArrayList<JournalEntry>();
        for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof JournalEntry) {
                JournalEntry journalEntry = (JournalEntry)object;
                selectedItems.add(journalEntry);
            } else if (object instanceof JournalProperties) {
                JournalEntry journalEntry = ((JournalProperties)object).getJournalEntry();
                selectedItems.add(journalEntry);
            }
        }

        this.selectedItems = selectedItems.toArray(new JournalEntry[selectedItems.size()]);
    }

    public void setSelectedItems(JournalProperties[] selectedItems) {

        List<JournalEntry> journalProperties = new LinkedList<JournalEntry>();
        for (JournalProperties selectedItem : selectedItems) {
            JournalEntry journalEntry = selectedItem.getJournalEntry();
            journalProperties.add(journalEntry);
        }

        this.selectedItems = journalProperties.toArray(new JournalEntry[journalProperties.size()]);
    }

    public void setSelectedItems(JournalEntry[] selectedItems) {
        this.selectedItems = selectedItems;
    }

    @Override
    public void run() {
        performCompareSideBySideEntries(selectedItems);
    }

    protected void performCompareSideBySideEntries(JournalEntry[] selectedItems) {

        if (selectedItems == null || selectedItems.length != 2) {
            return;
        }

        SideBySideCompareDialog sideBySideCompareDialog = new SideBySideCompareDialog(shell);
        sideBySideCompareDialog.create();

        sideBySideCompareDialog.setInput(new JournalProperties(selectedItems[0]), new JournalProperties(selectedItems[1]));
        sideBySideCompareDialog.open();
    }
}
