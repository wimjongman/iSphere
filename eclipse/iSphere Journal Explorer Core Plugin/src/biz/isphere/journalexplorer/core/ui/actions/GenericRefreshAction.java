/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;

import biz.isphere.journalexplorer.core.ISphereJournalExplorerCorePlugin;
import biz.isphere.journalexplorer.core.Messages;

public abstract class GenericRefreshAction extends Action {

    private static final String IMAGE = ISphereJournalExplorerCorePlugin.IMAGE_REFRESH;

    public GenericRefreshAction() {
        super(Messages.JournalEntryView_ReloadEntries);

        setImageDescriptor(ISphereJournalExplorerCorePlugin.getDefault().getImageDescriptor(IMAGE));
    }

    public Image getImage() {
        return ISphereJournalExplorerCorePlugin.getDefault().getImage(IMAGE);
    }

    @Override
    public void run() {
        performRefresh();
    }

    protected void performRefresh() {
        postRunAction();
    }

    protected abstract void postRunAction();
}
