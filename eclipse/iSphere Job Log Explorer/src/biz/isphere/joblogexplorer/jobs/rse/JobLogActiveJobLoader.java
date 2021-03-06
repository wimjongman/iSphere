/*******************************************************************************
 * Copyright (c) 2012-2020 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.jobs.rse;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.QualifiedJobName;
import biz.isphere.joblogexplorer.editor.JobLogExplorerJobInput;

public class JobLogActiveJobLoader extends AbstractJobLogLoader {

    private String connectionName;
    private String jobName;
    private String userName;
    private String jobNumber;

    public JobLogActiveJobLoader(String connectionName, QualifiedJobName qualifiedJobName) {

        this.connectionName = connectionName;
        this.jobName = qualifiedJobName.getJob();
        this.userName = qualifiedJobName.getUser();
        this.jobNumber = qualifiedJobName.getNumber();
    }

    public JobLogActiveJobLoader(String connectionName, String jobName, String userName, String jobNumber) {
        this.connectionName = connectionName;
        this.jobName = jobName;
        this.userName = userName;
        this.jobNumber = jobNumber;
    }

    public void run() {

        try {

            JobLogExplorerJobInput editorInput = new JobLogExplorerJobInput(connectionName, jobName, userName, jobNumber);
            openJobLogExplorerView(editorInput);

        } catch (Throwable e) {
            ISpherePlugin.logError("*** Failed to open job log explorer with active job input ***", e); //$NON-NLS-1$
        }
    }
}
