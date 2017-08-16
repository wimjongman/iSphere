/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.journalexplorer.core.ISphereJournalExplorerCorePlugin;
import biz.isphere.journalexplorer.core.Messages;
import biz.isphere.journalexplorer.core.model.File;
import biz.isphere.journalexplorer.core.ui.dialogs.AddJournalDialog;

public abstract class OpenJournalOutfileAction extends Action {

    private static final String IMAGE = ISphereJournalExplorerCorePlugin.IMAGE_OPEN_JOURNAL_OUTFILE;

    private Shell shell;
    private File outputFile;

    public OpenJournalOutfileAction(Shell shell) {
        super(Messages.JournalExplorerView_OpenJournal);

        this.shell = shell;

        setImageDescriptor(ISphereJournalExplorerCorePlugin.getDefault().getImageDescriptor(IMAGE));
    }

    public Image getImage() {
        return ISphereJournalExplorerCorePlugin.getDefault().getImage(IMAGE);
    }

    @Override
    public void run() {
        performOpenJournal();
        postRunAction();
    }

    public File getOutputFile() {
        return outputFile;
    }

    private void performOpenJournal() {

        AddJournalDialog addJournalDialog = new AddJournalDialog(shell);
        addJournalDialog.create();
        int result = addJournalDialog.open();

        if (result == Window.OK) {
            outputFile = new File(addJournalDialog.getConnectionName(), addJournalDialog.getLibrary(), addJournalDialog.getFileName());
        } else {
            outputFile = null;
        }
    }

    protected abstract void postRunAction();
}
