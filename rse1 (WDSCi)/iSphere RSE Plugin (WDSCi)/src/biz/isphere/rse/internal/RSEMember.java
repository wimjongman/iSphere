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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import biz.isphere.core.internal.Member;

import com.ibm.etools.iseries.core.ISeriesTempFileListener;
import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.api.ISeriesMember;
import com.ibm.etools.iseries.core.resources.ISeriesEditableSrcPhysicalFileMember;

public class RSEMember extends Member {

    private ISeriesMember _member;
    private ISeriesEditableSrcPhysicalFileMember _editableMember;
    private String label;
    private boolean archive;
    private String archiveLibrary;
    private String archiveFile;
    private String archiveMember;
    private String archiveDate;
    private String archiveTime;

    public RSEMember(ISeriesMember _member) throws Exception {
        super();
        this._member = _member;
        if (_member != null) {
            _editableMember = new ISeriesEditableSrcPhysicalFileMember(_member);
        }
        label = null;
        archive = false;
        archiveLibrary = null;
        archiveFile = null;
        archiveMember = null;
        archiveDate = null;
        archiveTime = null;
    }

    public ISeriesConnection getRSEConnection() {
        return _member.getISeriesConnection();
    }

    @Override
    public String getConnection() {
        return _member.getISeriesConnection().getConnectionName();
    }

    @Override
    public String getLibrary() {
        return _member.getLibrary();
    }

    @Override
    public String getSourceFile() {
        return _member.getFile();
    }

    @Override
    public String getMember() {
        return _member.getName();
    }

    @Override
    public boolean exists() {
        if (_editableMember == null) {
            return false;
        }
        return _editableMember.exists();
    }

    @Override
    public boolean download(IProgressMonitor monitor) throws Exception {
        return _editableMember.download(monitor);
    }

    @Override
    public void upload(IProgressMonitor monitor) throws Exception {
        _editableMember.upload(monitor);
    }

    @Override
    public IFile getLocalResource() {
        return _editableMember.getLocalResource();
    }

    @Override
    public void setContents(String[] contents) throws Exception {
        _editableMember.setContents(null, contents, false);
    }
    
    @Override
    public String[] getContents() throws Exception {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(getLocalResource().getContents()));
        List<String> contents = new ArrayList<String>();
        String line = null;
        while ((line = br.readLine()) != null) {
            contents.add(line.substring(12)); // strip seq. number and date
        }

        return contents.toArray(new String[contents.size()]);
    }

    public String getDescription() {
        return _editableMember.getMember().getDescription();
    }

    public String getSourceType() {
        return _editableMember.getMember().getSourceType();
    }
    
    @Override
    public void openStream() throws Exception {
        _editableMember.openStream();
    }

    @Override
    public void closeStream() throws Exception {
        _editableMember.closeStream();
    }

    @Override
    public void addIgnoreFile() {
        ISeriesTempFileListener.getListener().addIgnoreFile(_editableMember.getLocalResource());
    }

    @Override
    public void removeIgnoreFile() {
        ISeriesTempFileListener.getListener().removeIgnoreFile(_editableMember.getLocalResource());
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean isArchive() {
        return archive;
    }

    @Override
    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    @Override
    public String getArchiveLibrary() {
        return archiveLibrary;
    }

    @Override
    public void setArchiveLibrary(String archiveLibrary) {
        this.archiveLibrary = archiveLibrary;
    }

    @Override
    public String getArchiveFile() {
        return archiveFile;
    }

    @Override
    public void setArchiveFile(String archiveFile) {
        this.archiveFile = archiveFile;
    }

    @Override
    public String getArchiveMember() {
        return archiveMember;
    }

    @Override
    public void setArchiveMember(String archiveMember) {
        this.archiveMember = archiveMember;
    }

    @Override
    public String getArchiveDate() {
        return archiveDate;
    }

    @Override
    public void setArchiveDate(String archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public String getArchiveTime() {
        return archiveTime;
    }

    @Override
    public void setArchiveTime(String archiveTime) {
        this.archiveTime = archiveTime;
    }

}
