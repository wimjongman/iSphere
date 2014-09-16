/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.resourcemanagement;

import biz.isphere.core.Messages;

public abstract class AbstractResource {

	public static final String PUSH_TO_REPOSITORY = "Push_to_repository";
	public static final String PUSH_TO_WORKSPACE = "Push_to_workspace";
	public static final String DELETE_FROM_REPOSITORY = "Delete_from_repository";
	public static final String DELETE_FROM_WORKSPACE = "Delete_from_workspace";
	public static final String DELETE_FROM_BOTH = "Delete_from_both";

	private boolean editable;
	private String action;

	public AbstractResource(boolean editable) {
		this.editable = editable;
		action = null;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public static String getActionText(String action) {
	    if (action.equals(PUSH_TO_REPOSITORY)) {
	        return Messages.Push_to_repository;
	    }
	    else if (action.equals(PUSH_TO_WORKSPACE)) {
	        return Messages.Push_to_workspace;
	    }
	    else if (action.equals(DELETE_FROM_REPOSITORY)) {
	        return Messages.Delete_from_repository;
	    }
	    else if (action.equals(DELETE_FROM_WORKSPACE)) {
	        return Messages.Delete_from_workspace;
	    }
	    else if (action.equals(DELETE_FROM_BOTH)) {
	        return Messages.Delete_from_both;
        }
	    else {
	        return "*UNKNOWN";
	    }
	}
	
	public abstract String getKey();

	public abstract String getValue();

}
