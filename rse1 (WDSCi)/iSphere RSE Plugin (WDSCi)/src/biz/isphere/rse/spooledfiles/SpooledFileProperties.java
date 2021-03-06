/*******************************************************************************
 * Copyright (c) 2012-2013 Task Force IT-Consulting GmbH, Waltrop and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Task Force IT-Consulting GmbH - initial API and implementation
 *******************************************************************************/

package biz.isphere.rse.spooledfiles;

import biz.isphere.core.spooledfiles.AbstractSpooledFileProperties;
import biz.isphere.core.spooledfiles.SpooledFile;

public class SpooledFileProperties extends AbstractSpooledFileProperties {

    @Override
    public SpooledFile getSpooledFile() {
        SpooledFileResource spooledFileResource = (SpooledFileResource)getElement();
        return spooledFileResource.getSpooledFile();
    }

}
