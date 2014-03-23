/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.spooledfiles;

import com.ibm.as400.data.ProgramCallDocument;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;

public class SPLF_setDateTime {

	public int run(
			AS400 _as400,
			int startDate,
			int startTime,
			int endDate,
			int endTime) {

		int errno = 0;
		
		try {
			
			ProgramCallDocument pcml = 
				new ProgramCallDocument(
						_as400, 
						"biz.isphere.core.spooledfiles.SPLF_setDateTime", 
						this.getClass().getClassLoader());

			pcml.setIntValue("SPLF_setDateTime.startDate", startDate);
			pcml.setIntValue("SPLF_setDateTime.startTime", startTime);
			pcml.setIntValue("SPLF_setDateTime.endDate", endDate);
			pcml.setIntValue("SPLF_setDateTime.endTime", endTime);
			
			boolean rc = pcml.callProgram("SPLF_setDateTime");

			if (rc == false) {
				
				AS400Message[] msgs = pcml.getMessageList("SPLF_setDateTime");
				for (int idx = 0; idx < msgs.length; idx++) {
					System.out.println(msgs[idx].getID() + " - " + msgs[idx].getText());
				}
				System.out.println("*** Call to SPLF_setDateTime failed. See messages above ***");
				
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
		//	System.out.println("*** Call to SPLF_setDateTime failed. ***");
		//	return null;
			
		}
		
		return errno;
		
	} 

}