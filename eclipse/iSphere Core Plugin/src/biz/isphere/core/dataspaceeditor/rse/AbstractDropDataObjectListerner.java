/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.dataspaceeditor.rse;

import java.io.UnsupportedEncodingException;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.part.PluginTransferData;

import biz.isphere.core.Messages;

/**
 * Class to let the user Drag & Drop a data space object (data area or user
 * space) into the <i>iSphere Data Space Editor Designer</i>. Designer.
 * <p>
 * The event flow of a drop operation is as followed:
 * <ul>
 * <li>dragEnter</li>
 * <li>dragOver</li>
 * <li>dragLeave</li>
 * <li>dragAccept</li>
 * <li>dropTarget</li>
 * </ul>
 * 
 * @see org.eclipse.rse.internal.ui.view.SystemTableTreeView#initDragAndDrop()
 * @see org.eclipse.rse.internal.ui.view.SystemViewDataDropAdapter
 */
public abstract class AbstractDropDataObjectListerner extends DropTargetAdapter implements IListOfRemoteObjectsReceiver {

    private IDropObjectListener editor;

    public AbstractDropDataObjectListerner(IDropObjectListener editor) {
        this.editor = editor;
    }

    public void dragEnter(DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
    }

    public void dragOver(DropTargetEvent event) {
    }

    public void dragLeave(DropTargetEvent event) {
    }

    public void dropAccept(DropTargetEvent event) {
    }

    public void drop(DropTargetEvent event) {

        if ((event.data instanceof PluginTransferData)) {
            PluginTransferData transferData = (PluginTransferData)event.data;

            byte[] result = transferData.getData();

            String str = null;
            try {
                str = new String(result, "UTF-8");
            } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
                str = new String(result);
            }

            // Split plug-in transfer data into objects
            String[] droppedObjects = str.split("\\|");
            loadRemoteObjectsAsync(droppedObjects, this, Messages.Loading_remote_objects);
        }
    }

    protected abstract void loadRemoteObjectsAsync(String[] objects, IListOfRemoteObjectsReceiver receiver, String jobName);

    public void setRemoteObjects(RemoteObject[] objects) {
        editor.setDataAsync(objects);
    }
}
