/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.spooledfiles;

import biz.isphere.core.ISpherePlugin;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;

public class SPLF_getErrorMessage {

    public String run(AS400 _as400) {

        String message = "";
        
        try {

            ProgramCallDocument pcml = new ProgramCallDocument(_as400, "biz.isphere.core.spooledfiles.SPLF_getErrorMessage", this.getClass().getClassLoader());

            boolean rc = pcml.callProgram("SPLF_getErrorMessage");

            if (rc == false) {

                AS400Message[] msgs = pcml.getMessageList("SPLF_getErrorMessage");
                for (int idx = 0; idx < msgs.length; idx++) {
                    ISpherePlugin.logError(msgs[idx].getID() + " - " + msgs[idx].getText(), null);
                }
                ISpherePlugin.logError("*** Call to SPLF_getErrorMessage failed. See messages above ***", null);

            } else {

                message = pcml.getStringValue("SPLF_getErrorMessage.message");
                
            }

        } catch (PcmlException e) {

            // System.out.println(e.getLocalizedMessage());
            // e.printStackTrace();
            // System.out.println("*** Call to SPLF_getErrorMessage failed. ***");
            // return null;
            ISpherePlugin.logError("*** Call to SPLF_getErrorMessage failed. See messages above ***", e);
        }

        return message;

    }

}