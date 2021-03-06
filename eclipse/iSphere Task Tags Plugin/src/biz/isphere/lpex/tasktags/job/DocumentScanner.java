/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.lpex.tasktags.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import biz.isphere.lpex.tasktags.ISphereLpexTasksPlugin;
import biz.isphere.lpex.tasktags.model.LPEXTaskManager;

/**
 * This class represents a background job, that scans a given document for LPEX
 * task tags.
 * 
 * @author Thomas Raddatz
 */
public class DocumentScanner extends Job {

    private LPEXTaskManager manager;

    public DocumentScanner(String aName, LPEXTaskManager aManager) {
        super(aName);
        manager = aManager;
    }

    @Override
    protected IStatus run(IProgressMonitor arg0) {

        try {
            manager.removeMarkers();
            if (!manager.markerAreEnabled()) {
                return Status.OK_STATUS;
            }

            manager.createMarkers();
        } catch (Exception e) {
            ISphereLpexTasksPlugin.logError("Failed to process document: " + manager.getDocumentName(), e);
        }

        return Status.OK_STATUS;
    }

}
