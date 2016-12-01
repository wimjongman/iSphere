/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.rse.action;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

import biz.isphere.joblogexplorer.action.rse.AbstractOpenJobLogExplorerWithRemoteFileAction;
import biz.isphere.joblogexplorer.rse.jobs.LoadIRemoteFileJob;

public class OpenJobLogExplorerWithRemoteFileAction extends AbstractOpenJobLogExplorerWithRemoteFileAction {

    public static final String ID = "biz.isphere.joblogexplorer.rse.action.OpenJobLogExplorerWithRemoteFileAction"; //$NON-NLS-1$

    @Override
    protected void execute(Object object) {

        if (object instanceof IRemoteFile) {
            IRemoteFile remoteFile = (IRemoteFile)object;
            LoadIRemoteFileJob job = new LoadIRemoteFileJob(remoteFile);
            job.run();
        }
    }

}
