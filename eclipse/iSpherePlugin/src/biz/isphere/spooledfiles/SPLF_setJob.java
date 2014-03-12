/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.spooledfiles;

import com.ibm.as400.data.ProgramCallDocument;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;

public class SPLF_setJob {

	public int run(
			AS400 _as400,
			String _jobName,
			String _jobUser,
			String _jobNumber) {

		int errno = 0;
		
		try {
			
			ProgramCallDocument pcml = 
				new ProgramCallDocument(
						_as400, 
						"biz.isphere.spooledfiles.SPLF_setJob", 
						this.getClass().getClassLoader());

			pcml.setValue("SPLF_setJob.jobName", _jobName);
			pcml.setValue("SPLF_setJob.jobUser", _jobUser);
			pcml.setValue("SPLF_setJob.jobNumber", _jobNumber);
			
			boolean rc = pcml.callProgram("SPLF_setJob");

			if (rc == false) {
				
				AS400Message[] msgs = pcml.getMessageList("SPLF_setJob");
				for (int idx = 0; idx < msgs.length; idx++) {
					System.out.println(msgs[idx].getID() + " - " + msgs[idx].getText());
				}
				System.out.println("*** Call to SPLF_setJob failed. See messages above ***");
				
				errno = -1;
				
			}
			else {
				
				errno = 1;
				
			}
			
		}
		catch (PcmlException e) {
		
			errno = -1;
			
		//	System.out.println(e.getLocalizedMessage());    
		//	e.printStackTrace();
		//	System.out.println("*** Call to SPLF_setJob failed. ***");
		//	return null;
			
		}
		
		return errno;
		
	} 

}