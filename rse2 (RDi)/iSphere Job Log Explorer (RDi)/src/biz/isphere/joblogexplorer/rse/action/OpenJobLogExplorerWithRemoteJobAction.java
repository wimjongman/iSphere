/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.rse.action;

import biz.isphere.joblogexplorer.action.rse.AbstractOpenJobLogExplorerWithSpooledFileAction;
import biz.isphere.joblogexplorer.rse.jobs.LoadRemoteJobLogJob;

import com.ibm.etools.iseries.comm.interfaces.ISeriesJobName;
import com.ibm.etools.iseries.subsystems.qsys.jobs.QSYSRemoteJob;

public class OpenJobLogExplorerWithRemoteJobAction extends AbstractOpenJobLogExplorerWithSpooledFileAction {

    public static final String ID = "biz.isphere.joblogexplorer.rse.action.OpenJobLogExplorerWithRemoteJobAction"; //$NON-NLS-1$

    @Override
    protected void execute(Object object) {
        if (object instanceof QSYSRemoteJob) {
            QSYSRemoteJob remoteJob = (QSYSRemoteJob)object;
            String connectionName = remoteJob.getRemoteJobContext().getJobSubsystem().getCmdSubSystem().getHostAliasName();
            ISeriesJobName jobName = new ISeriesJobName(remoteJob.getFullJobName());
            LoadRemoteJobLogJob job = new LoadRemoteJobLogJob(connectionName, jobName);
            job.run();
        }
    }

}
