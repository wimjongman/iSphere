/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Initial idea and development: Isaac Ramirez Herrera
 * Continued and adopted to iSphere: iSphere Project Team
 *******************************************************************************/

package biz.isphere.journalexplorer.core.model.adapters;

import java.util.ArrayList;

import biz.isphere.base.internal.ExceptionHelper;
import biz.isphere.journalexplorer.core.Messages;
import biz.isphere.journalexplorer.core.internals.JoesdParser;
import biz.isphere.journalexplorer.core.model.JournalEntry;
import biz.isphere.journalexplorer.core.model.MetaColumn;
import biz.isphere.journalexplorer.core.model.MetaDataCache;
import biz.isphere.journalexplorer.core.model.MetaTable;

import com.ibm.as400.access.Record;

public class JOESDProperty extends JournalProperty {

    private JournalEntry journal;

    private MetaTable metatable;

    private ArrayList<JournalProperty> specificProperties;

    private Record parsedJOESD;

    public JOESDProperty(String name, Object value, Object parent, JournalEntry journal) {

        super(name, "", parent); //$NON-NLS-1$
        this.journal = journal;
        setErrorParsing(false);

        this.executeParsing();
    }

    public void executeParsing() {
        try {
            initialize();
            parseJOESD();
        } catch (Exception exception) {
            value = ExceptionHelper.getLocalizedMessage(exception);
            setErrorParsing(true);
        }
    }

    private void initialize() throws Exception {

        setErrorParsing(false);
        value = ""; //$NON-NLS-1$

        metatable = null;

        parsedJOESD = null;

        if (specificProperties != null) {
            specificProperties.clear();
        } else {
            specificProperties = new ArrayList<JournalProperty>();
        }
    }

    private void parseJOESD() throws Exception {

        String columnName;

        metatable = MetaDataCache.INSTANCE.retrieveMetaData(journal);

        parsedJOESD = new JoesdParser(metatable).execute(journal);

        for (MetaColumn column : metatable.getColumns()) {
            columnName = column.getName().trim();
            if (column.getText() != null && column.getText().trim().length() != 0) {
                columnName += " (" + column.getText().trim() + ")"; //$NON-NLS-1$  //$NON-NLS-2$
            }

            if (MetaColumn.DataType.UNKNOWN.equals(column.getType())) {
                JournalProperty journalProperty = new JournalProperty(columnName, Messages.Error_Unknown_data_type, this);
                journalProperty.setErrorParsing(true);
                specificProperties.add(journalProperty);
            } else if (MetaColumn.DataType.LOB.equals(column.getType())) {
                specificProperties.add(new JournalProperty(columnName, parsedJOESD.getField(column.getName()).toString().trim(), this));
            } else {
                specificProperties.add(new JournalProperty(columnName, parsedJOESD.getField(column.getName()).toString(), this));
            }
        }
    }

    public Object[] toPropertyArray() {
        if (specificProperties != null) {
            return specificProperties.toArray();
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(JournalProperty comparable) {

        if (comparable instanceof JOESDProperty) {
            JOESDProperty joesdSpecificProperty = (JOESDProperty)comparable;

            if (joesdSpecificProperty.parsedJOESD.getNumberOfFields() != parsedJOESD.getNumberOfFields()) {
                highlighted = comparable.highlighted = true;
                return -1;

            } else {
                int status = 0;

                for (int i = 0; i < specificProperties.size(); i++) {

                    if (specificProperties.get(i).compareTo(joesdSpecificProperty.specificProperties.get(i)) != 0) {
                        status = -1;
                    }
                }
                return status;
            }
        } else {
            return -1;
        }
    }
}
