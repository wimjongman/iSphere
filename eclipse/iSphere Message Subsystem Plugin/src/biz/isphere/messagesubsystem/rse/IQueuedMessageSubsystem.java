/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     iSphere Project Owners - Maintenance and enhancements
 *******************************************************************************/

package biz.isphere.messagesubsystem.rse;

public interface IQueuedMessageSubsystem {

    public String getHostName();

    public String getVendorAttribute(String key);

    public void setVendorAttribute(String key, String value);

    public void restartMessageMonitoring();

}
