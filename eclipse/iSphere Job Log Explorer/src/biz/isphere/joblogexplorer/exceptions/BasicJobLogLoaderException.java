/*******************************************************************************
 * Copyright (c) 2012-2020 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.exceptions;

public abstract class BasicJobLogLoaderException extends Exception {

    private static final long serialVersionUID = 4831720546583300208L;

    public BasicJobLogLoaderException(String message) {
        super(message);
    }
}