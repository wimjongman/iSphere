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

package biz.isphere.rse.internal;

import org.eclipse.ui.PlatformUI;

import biz.isphere.core.internal.IEditor;

import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.api.ISeriesMember;
import com.ibm.etools.iseries.core.resources.ISeriesEditableSrcPhysicalFileMember;
import com.ibm.etools.systems.core.messages.SystemMessageException;
import com.ibm.etools.systems.editor.SystemTextEditor;

public class Editor implements IEditor {

    public void openEditor(String connectionName, String library, String file, String member, int statement, String mode) {

        ISeriesConnection _connection = ISeriesConnection.getConnection(connectionName);

        try {

            ISeriesMember _member = _connection.getISeriesMember(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), library, file,
                member);

            if (_member != null) {

                ISeriesEditableSrcPhysicalFileMember mbr = null;

                if (mode.equals(IEditor.EDIT)) {
                    mbr = _member.open();
                } else if (mode.equals(IEditor.BROWSE)) {
                    mbr = _member.browse();
                }

                if (mbr != null && statement != 0) {
                    if (!mbr.openIsCanceled()) {
                        SystemTextEditor systemTextEditor = mbr.getEditor();
                        if (systemTextEditor != null) {
                            systemTextEditor.gotoLine(statement);
                        }
                    }
                }

            }

        }

        catch (SystemMessageException e) {
            e.printStackTrace();
        }

    }

}
