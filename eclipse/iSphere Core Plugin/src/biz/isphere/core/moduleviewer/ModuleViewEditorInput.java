/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.moduleviewer;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.api.debugger.moduleviews.DebuggerView;

public class ModuleViewEditorInput implements IStorageEditorInput {

    private ModuleViewStorage storage;

    public ModuleViewEditorInput(String systemName, DebuggerView debuggerView, String[] lines) throws Exception {

        String program = debuggerView.getObject();
        String library = debuggerView.getLibrary();
        String objectType = debuggerView.getObjectType();
        String module = debuggerView.getModule();
        int viewId = debuggerView.getId();
        String description = debuggerView.getDescription();

        storage = new ModuleViewStorage(systemName, program, library, objectType, module, viewId, description, lines);
    }

    public IStorage getStorage() throws CoreException {
        return storage;
    }

    public boolean exists() {
        return false;
    }

    public String getName() {
        return storage.getName();
    }

    public String getToolTipText() {
        return storage.getFullQualifiedName();
    }

    public String getContentDescription() {
        return storage.getSystemName() + ":" + storage.getName() + " - " + storage.getDescription(); //$NON-NLS-1$ //$NON-NLS-1$
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public ImageDescriptor getImageDescriptor() {
        return ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_DISPLAY_MODULE_VIEW);
    }

    public Object getAdapter(Class arg0) {
        return null;
    }

    public boolean isSameModuleView(IEditorInput editorInput) throws CoreException {

        if (editorInput == this) {
            return true;
        }

        if (!(editorInput instanceof ModuleViewEditorInput)) {
            return false;
        }

        ModuleViewEditorInput otherEditorInput = (ModuleViewEditorInput)editorInput;

        return getName().equals(otherEditorInput.getName());
    }

    public ModuleViewEditor findEditor(IWorkbenchPage aPage) {
        if (aPage == null) {
            return null;
        }

        IEditorReference[] tEditors = aPage.getEditorReferences();
        for (IEditorReference tEditorReference : tEditors) {
            try {
                if (isSameModuleView(tEditorReference.getEditorInput())) {
                    IEditorPart tEditor = tEditorReference.getEditor(true);
                    if (tEditor instanceof ModuleViewEditor) {
                        return (ModuleViewEditor)tEditor;
                    }
                }
            } catch (Exception e) {
                ISpherePlugin.logError("Could not find 'ModuleViewEditor'.", e); //$NON-NLS-1$
            }
        }
        return null;
    }
}