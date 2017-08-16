/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import biz.isphere.journalexplorer.core.model.File;
import biz.isphere.journalexplorer.core.model.JournalEntry;
import biz.isphere.journalexplorer.core.model.MetaDataCache;

public abstract class AbstractTypeDAO extends DAOBase implements ColumnsDAO {

    private File outputFile;

    public AbstractTypeDAO(File outputFile) throws Exception {
        super(outputFile.getConnectionName());
        
        this.outputFile = outputFile;
    }
    
    public List<JournalEntry> load() throws Exception {

        List<JournalEntry> journalEntries = new ArrayList<JournalEntry>();

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            String sqlStatement = String.format( getSqlStatement(),outputFile.getOutFileLibrary(), outputFile.getOutFileName());
            preparedStatement = prepareStatement(sqlStatement);
            resultSet = preparedStatement.executeQuery();
            resultSet.setFetchSize(50);

            if (resultSet != null) {
                
                JournalEntry journalEntry = null;

                while (resultSet.next()) {

                    journalEntry = new JournalEntry();
                    journalEntry.setOutFileLibrary(outputFile.getOutFileLibrary());
                    journalEntry.setOutFileName(outputFile.getOutFileName());
                    
                    journalEntries.add(populateJournalEntry(resultSet, journalEntry));

                    MetaDataCache.INSTANCE.prepareMetaData(journalEntry);
                }
            }

        } catch (Exception exception) {
            throw exception;
        } finally {
            super.destroy(preparedStatement);
            super.destroy(resultSet);
        }

        return journalEntries;
    }
    
    protected abstract String getSqlStatement();
    
    protected JournalEntry populateJournalEntry(ResultSet resultSet, JournalEntry journalEntry) throws Exception {
        
        journalEntry.setConnectionName(getConnectionName());
        journalEntry.setId(resultSet.getInt(RRN_OUTPUT_FILE));
        journalEntry.setCommitmentCycle(resultSet.getInt(JOCCID));
        journalEntry.setEntryLength(resultSet.getInt(JOENTL));
        journalEntry.setEntryType(resultSet.getString(JOENTT));
        journalEntry.setIncompleteData(resultSet.getString(JOINCDAT));
        journalEntry.setJobName(resultSet.getString(JOJOB));
        journalEntry.setJobNumber(resultSet.getInt(JONBR));
        journalEntry.setJobUserName(resultSet.getString(JOUSER));
        journalEntry.setCountRrn(resultSet.getInt(JOCTRR));
        journalEntry.setFlag(resultSet.getString(JOFLAG));
        journalEntry.setJournalCode(resultSet.getString(JOCODE));
        journalEntry.setMemberName(resultSet.getString(JOMBR));
        journalEntry.setMinimizedSpecificData(resultSet.getString(JOMINESD));
        journalEntry.setObjectLibrary(resultSet.getString(JOLIB));
        journalEntry.setObjectName(resultSet.getString(JOOBJ));
        journalEntry.setProgramName(resultSet.getString(JOPGM));
        journalEntry.setSequenceNumber(resultSet.getLong(JOSEQN));
        journalEntry.setSpecificData(resultSet.getBytes(JOESD));
        journalEntry.setStringSpecificData(resultSet.getString(JOESD));
        
        return journalEntry;
    }

}
