/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.model.dao;

import java.sql.ResultSet;

import biz.isphere.journalexplorer.core.model.File;
import biz.isphere.journalexplorer.core.model.JournalEntry;

public class Type2DAO extends Type1DAO {

    // @formatter:off
    private static final String GET_JOURNAL_DATA_2 =
        "    SELECT rrn(result) as ID, " +
        "           result.JOENTL,  " +
        "           result.JOSEQN,  " +
        "           result.JOCODE,  " +
        "           result.JOENTT,  " +
        "           result.JODATE,  " +
        "           result.JOTIME,  " +
        "           result.JOJOB,   " +
        "           result.JOUSER,  " +
        "           result.JONBR,   " + 
        "           result.JOPGM,   " +
        "           result.JOOBJ,   " +
        "           result.JOLIB,   " +
        "           result.JOMBR,   " +
        "           result.JOCTRR,  " +
        "           result.JOFLAG,  " +
        "           result.JOCCID,  " +
        "           result.JOUSPF,  " + //  added with TYPE2
        "           result.JOSYNM,  " + //  added with TYPE2
        "           result.JOINCDAT," +
        "           result.JOMINESD," +
                    // JORES - reserved
        "           SUBSTR(result.JOESD,1,5000) AS JOESD" + 
        "      FROM %s.%s as result";
    // @formatter:on
    
    public Type2DAO(File outputFile) throws Exception {
        super(outputFile);
    }

    protected String getSqlStatement() {
        return GET_JOURNAL_DATA_2;
    }

    @Override
    protected JournalEntry populateJournalEntry(ResultSet resultSet, JournalEntry journalEntry) throws Exception {
        
        journalEntry = super.populateJournalEntry(resultSet, journalEntry);
        
        journalEntry.setJobName(resultSet.getString("JOUSPF"));
        journalEntry.setJobName(resultSet.getString("JOSYNM"));

        return journalEntry;
    }

}
