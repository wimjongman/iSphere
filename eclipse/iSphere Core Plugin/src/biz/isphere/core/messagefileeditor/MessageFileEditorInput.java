/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.messagefileeditor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.RemoteObject;
import biz.isphere.core.objecteditor.AbstractObjectEditorInput;

import com.ibm.as400.access.AS400;

public class MessageFileEditorInput extends AbstractObjectEditorInput {

    private static final String OBJECT_TYPE = "MSGF";

    public MessageFileEditorInput(AS400 anAS400, RemoteObject remoteObject, String aMode) {
        super(anAS400, remoteObject, aMode, ISpherePlugin.IMAGE_MESSAGE_FILE);
    }

    // TODO: CMOne - remove method
    @Deprecated
    public MessageFileEditorInput(AS400 anAS400, String aConnectionName, String aLibrary, String aMessageFile, String aMode) {
        super(anAS400, aConnectionName, aLibrary, aMessageFile, OBJECT_TYPE, aMode, ISpherePlugin.IMAGE_MESSAGE_FILE);
    }

    public boolean exists() {
        return false;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        return null;
    }

    public String getMessageFile() {
        return super.getObjectName();
    }

}
