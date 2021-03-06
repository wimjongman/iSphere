/*******************************************************************************
 * Copyright (c) 2012-2015 Task Force IT-Consulting GmbH, Waltrop and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Task Force IT-Consulting GmbH - initial API and implementation
 *******************************************************************************/

package biz.isphere.rse.spooledfiles;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import biz.isphere.core.spooledfiles.ISpooledFileSubSystem;
import biz.isphere.core.spooledfiles.SpooledFile;
import biz.isphere.core.spooledfiles.SpooledFileBaseResourceAdapter;
import biz.isphere.core.spooledfiles.SpooledFileTextDecoration;

import com.ibm.etools.systems.core.ui.SystemMenuManager;
import com.ibm.etools.systems.core.ui.view.AbstractSystemViewAdapter;
import com.ibm.etools.systems.core.ui.view.ISystemRemoteElementAdapter;

public class SpooledFileResourceAdapter extends AbstractSystemViewAdapter implements ISystemRemoteElementAdapter {

    private SpooledFileBaseResourceAdapter base = new SpooledFileBaseResourceAdapter();

    @Override
    public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell parent, String menuGroup) {
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        if (object instanceof SpooledFileResource) {
            return base.getImageDescriptor(((SpooledFileResource)object).getSpooledFile());
        }
        return null;
    }

    @Override
    public boolean handleDoubleClick(Object object) {
        if (object instanceof SpooledFileResource) {
            return base.handleDoubleClick(((SpooledFileResource)object).getSpooledFile());
        }
        return false;
    }

    @Override
    public String getText(Object object) {
        if (object instanceof SpooledFileResource) {
            SpooledFile spooledFile = ((SpooledFileResource)object).getSpooledFile();
            SpooledFileTextDecoration decorationStyle = ((ISpooledFileSubSystem)getSubSystem(object)).getDecorationTextStyle();
            return base.getText(spooledFile, decorationStyle);
        }
        return "";
    }

    @Override
    public String getAbsoluteName(Object object) {
        if (object instanceof SpooledFileResource) {
            return base.getAbsoluteName(((SpooledFileResource)object).getSpooledFile());
        }
        return "";
    }

    @Override
    public String getType(Object object) {
        if (object instanceof SpooledFileResource) {
            return base.getType(((SpooledFileResource)object).getSpooledFile());
        }
        return "";
    }

    @Override
    public Object getParent(Object object) {
        return null;
    }

    @Override
    public boolean hasChildren(Object object) {
        return false;
    }

    @Override
    public boolean showRename(Object object) {
        return false;
    }

    @Override
    public boolean showDelete(Object object) {
        return false;
    }

    @Override
    public boolean showRefresh(Object object) {
        return false;
    }

    @Override
    public Object[] getChildren(Object object) {
        return new Object[0];
    }

    @Override
    protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
        return base.internalGetPropertyDescriptors();
    }

    @Override
    public Object internalGetPropertyValue(Object propKey) {
        return base.internalGetPropertyValue(((SpooledFileResource)propertySourceInput).getSpooledFile(), propKey);
    }

    public String getAbsoluteParentName(Object object) {
        if (object instanceof SpooledFileResource) {
            return base.getAbsoluteParentName(((SpooledFileResource)object).getSpooledFile());
        }
        return "";
    }

    public String getSubSystemFactoryId(Object object) {
        return base.getSubSystemFactoryId();
    }

    public String getRemoteTypeCategory(Object object) {
        if (object instanceof SpooledFileResource) {
            return base.getRemoteTypeCategory(((SpooledFileResource)object).getSpooledFile());
        }
        return "";
    }

    public String getRemoteType(Object object) {
        if (object instanceof SpooledFileResource) {
            return base.getRemoteType(((SpooledFileResource)object).getSpooledFile());
        }
        return "";
    }

    public String getRemoteSubType(Object object) {
        return null;
    }

    public boolean refreshRemoteObject(Object oldElement, Object newElement) {
        SpooledFileResource oldSpooledFile = (SpooledFileResource)oldElement;
        SpooledFileResource newSpooledFile = (SpooledFileResource)newElement;
        newSpooledFile.setSpooledFile(oldSpooledFile.getSpooledFile());
        return false;
    }

    public Object getRemoteParent(Shell shell, Object object) throws Exception {
        return null;
    }

    public String[] getRemoteParentNamesInUse(Shell shell, Object object) throws Exception {
        return null;

    }

    public boolean supportsUserDefinedActions(Object object) {
        return false;
    }
    
    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (name != null && "biz.isphere.rse.spooledfiles.SpooledFileResource.file".equals(name)) {
            return value.equalsIgnoreCase(base.getFile(getSpooledFile(target)));
        }
        return super.testAttribute(target, name, value);
    }

    private SpooledFile getSpooledFile(Object object) {
        return ((SpooledFileResource)object).getSpooledFile();
    }
}
