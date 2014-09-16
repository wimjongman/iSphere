/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.resourcemanagement.filter;

import org.eclipse.swt.widgets.Composite;

import biz.isphere.core.Messages;
import biz.isphere.core.resourcemanagement.AbstractResource;

public class FilterEditingAreaRepository extends AbstractFilterEditingArea {

	public FilterEditingAreaRepository(Composite parent, AbstractResource[] resources, boolean both) {
		super(parent, resources, both);
	}

	@Override
	protected String[] getActions(boolean both) {
		return getActionsRepository(both);
	}
	
	public String getTitle() {
		return Messages.Filters + " " + getTitleRepository();
	}

}
