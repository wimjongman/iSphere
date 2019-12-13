/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.internal;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;
import com.ibm.etools.iseries.comm.interfaces.ISeriesHostObjectLock;

public abstract class Member {

    private static final int BLOCKING_SIZE = 5000;

    /** Record field */
    public static final int SRCSEQ_INDEX = 0;
    /** Record field */
    public static final int SRCDAT_INDEX = 1;
    /** Record field */
    public static final int SRCDTA_INDEX = 2;

    public abstract String getConnection();

    public abstract String getLibrary();

    public abstract String getSourceFile();

    public abstract String getMember();

    public abstract boolean exists();

    public abstract boolean download(IProgressMonitor monitor) throws Exception;

    public abstract String upload(IProgressMonitor monitor) throws Exception;

    public abstract IFile getLocalResource();

    public abstract String[] getContents() throws Exception;

    public abstract void setContents(String[] contents) throws Exception;

    public abstract String getDescription();

    public abstract String getSourceType();

    public abstract String getLabel();

    public abstract void setLabel(String label);

    public abstract boolean isArchive();

    public abstract void setArchive(boolean archive);

    public abstract String getArchiveLibrary();

    public abstract void setArchiveLibrary(String archiveLibrary);

    public abstract String getArchiveFile();

    public abstract void setArchiveFile(String archiveFile);

    public abstract String getArchiveMember();

    public abstract void setArchiveMember(String archiveMember);

    public abstract String getArchiveDate();

    public abstract void setArchiveDate(String archiveDate);

    public abstract String getArchiveTime();

    public abstract void setArchiveTime(String archiveTime);

    public abstract ISeriesHostObjectLock queryLocks() throws Exception;

    public abstract String getMemberLockedMessages(ISeriesHostObjectLock lock);

    public boolean hasSequenceNumbersAndDateFields() {
        return true;
    }

    public void openStream() throws Exception {
    }

    public void closeStream() throws Exception {
    }

    public void addIgnoreFile() {
    }

    public void removeIgnoreFile() {
    }

    public SourceLine[] downloadSourceMember(IProgressMonitor monitor) throws Exception {

        SequentialFile file = getSequentialFile(getLibrary(), getSourceFile(), getMember(), getConnection());

        try {

            AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(file.getSystem(), file.getPath());
            RecordFormat[] format = recordDescription.retrieveRecordFormat();
            file.setRecordFormat(format[0]);
            file.open(AS400File.READ_ONLY, 0, AS400File.COMMIT_LOCK_LEVEL_NONE);

            List<SourceLine> sourceLines = new LinkedList<SourceLine>();

            Record record;
            while ((record = file.readNext()) != null) {
                BigDecimal sourceSequence = (BigDecimal)record.getField(SRCSEQ_INDEX);
                BigDecimal sourceDate = (BigDecimal)record.getField(SRCDAT_INDEX);
                String sourceData = (String)record.getField(SRCDTA_INDEX);
                sourceLines.add(new SourceLine(sourceSequence, sourceDate, sourceData));
            }

            return sourceLines.toArray(new SourceLine[sourceLines.size()]);

        } finally {
            if (file.isOpen()) {
                file.close();
            }
        }
    }

    public String uploadSourceMember(SourceLine[] sourceLines, IProgressMonitor monitor) throws Exception {

        SequentialFile file = getSequentialFile(getLibrary(), getSourceFile(), getMember(), getConnection());

        try {

            ISeriesHostObjectLock lock = queryLocks();
            if (lock != null) {
                return getMemberLockedMessages(lock);
            }

            AS400FileRecordDescription recordDescription = new AS400FileRecordDescription(file.getSystem(), file.getPath());
            RecordFormat[] format = recordDescription.retrieveRecordFormat();
            file.setRecordFormat(format[0]);
            file.open(AS400File.WRITE_ONLY, 0, AS400File.COMMIT_LOCK_LEVEL_CHANGE);

            int targetDataLength = format[0].getFieldDescription(SRCDTA_INDEX).getLength();

            int countRemaining = sourceLines.length;
            Record[] records = createBlockingBuffer(countRemaining);

            int i = 0;
            Record record;
            while (countRemaining > 0) {

                record = new Record(format[0]);
                record.setField(SRCSEQ_INDEX, sourceLines[i].getSourceSequence());
                record.setField(SRCDAT_INDEX, sourceLines[i].getSourceDate());

                if (targetDataLength < sourceLines[i].getSourceData().length()) {
                    record.setField(SRCDTA_INDEX, sourceLines[i].getSourceData().substring(0, targetDataLength));
                } else {
                    record.setField(SRCDTA_INDEX, sourceLines[i].getSourceData());
                }

                records[i] = record;

                if (i == records.length - 1) {
                    file.write(records);
                    countRemaining = countRemaining - records.length;
                    records = createBlockingBuffer(countRemaining);
                    i = 0;
                } else {
                    i++;
                }
            }

            file.commit();

        } finally {
            if (file.isOpen()) {
                file.close();
            }
        }

        return null;
    }

    private Record[] createBlockingBuffer(int countRemaining) {
        return new Record[Math.min(countRemaining, BLOCKING_SIZE)];
    }

    private SequentialFile getSequentialFile(String sourceLibrary, String sourceFile, String sourceMember, String connectionName) {

        AS400 system = IBMiHostContributionsHandler.getSystem(connectionName);
        SequentialFile file = new SequentialFile(system, new QSYSObjectPathName(sourceLibrary, sourceFile, sourceMember, "MBR").getPath());

        return file;
    }

}
