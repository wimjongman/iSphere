/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.clcommands;

import org.eclipse.swt.widgets.Shell;

import biz.isphere.core.clcommands.IClCommandPrompter;

import com.ibm.etools.iseries.rse.util.clprompter.CLPrompter;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

public class ClCommandPrompter implements IClCommandPrompter {

    private CLPrompter prompter;

    public ClCommandPrompter() throws Exception {
        this.prompter = new CLPrompter();
    }

    public void setCommandString(String commandString) {
        prompter.setCommandString(commandString);
    }

    public void setMode(int mode) {
        prompter.setMode(mode);
    }

    public void setConnection(String connectionName) {
        prompter.setConnection(IBMiConnection.getConnection(connectionName));
    }

    public void setParent(Shell parent) {
        prompter.setParent(parent);
    }

}
