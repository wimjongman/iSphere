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

package biz.isphere.journalexplorer.core.model.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import biz.isphere.journalexplorer.core.Messages;
import biz.isphere.journalexplorer.core.model.MetaColumn;
import biz.isphere.journalexplorer.core.model.MetaTable;

public class MetaTableDAO extends DAOBase {

    public MetaTableDAO(String connectionName) throws Exception {
        super(connectionName);
    }

    // @formatter:off
    private static final String GET_TABLE_DEFINITION_SQL = 
          "    SELECT Tables.SYSTEM_TABLE_NAME, " //$NON-NLS-1$
        + "           Tables.SYSTEM_TABLE_SCHEMA, " //$NON-NLS-1$
        + "           Tables.COLUMN_COUNT, " //$NON-NLS-1$
        + "           Tables.TABLE_TEXT, " //$NON-NLS-1$
        + "           Tables.LONG_COMMENT," //$NON-NLS-1$
        + "           Columns.COLUMN_NAME,  " //$NON-NLS-1$
        + "           Columns.COLUMN_DEFAULT,  " //$NON-NLS-1$
        + "           Columns.DATA_TYPE," //$NON-NLS-1$
        + "           Columns.IS_NULLABLE," //$NON-NLS-1$
        + "           Columns.LONG_COMMENT as COLUMN_LONG_COMMENT," //$NON-NLS-1$
        + "           Columns.COLUMN_TEXT," //$NON-NLS-1$
        + "           Columns.SYSTEM_COLUMN_NAME,  " //$NON-NLS-1$
        + "           Columns.IS_IDENTITY," //$NON-NLS-1$
        + "           Columns.LENGTH," //$NON-NLS-1$
        + "           Columns.NUMERIC_SCALE," //$NON-NLS-1$
        + "           Columns.ORDINAL_POSITION " //$NON-NLS-1$
        + "      FROM QSYS2.SYSTABLES  Tables " //$NON-NLS-1$
        + "INNER JOIN QSYS2.SYSCOLUMNS Columns " //$NON-NLS-1$
        + "        ON Tables.SYSTEM_TABLE_NAME = Columns.SYSTEM_TABLE_NAME " //$NON-NLS-1$
        + "       AND Tables.SYSTEM_TABLE_SCHEMA = Columns.TABLE_SCHEMA " //$NON-NLS-1$
        + "     WHERE Tables.SYSTEM_TABLE_NAME = ? " //$NON-NLS-1$
        + "       AND Tables.SYSTEM_TABLE_SCHEMA = ?" //$NON-NLS-1$
        + " ORDER BY  Columns.ORDINAL_POSITION"; //$NON-NLS-1$
    // @formatter:on

    public void retrieveColumnsMetaData(MetaTable table) throws Exception {

        MetaColumn column = null;
        ResultSet resultSet = null;
        PreparedStatement sqlStatement = null;
        boolean nextColumn = true;

        try {

            sqlStatement = prepareStatement(GET_TABLE_DEFINITION_SQL);
            sqlStatement.setString(1, table.getDefinitionName());
            sqlStatement.setString(2, table.getDefinitionLibrary());

            resultSet = sqlStatement.executeQuery();

            if (resultSet == null) {

                throw new Exception(Messages.MetaTableDAO_NullResultSet);
            } else {

                if (resultSet.next()) {

                    while (nextColumn) {
                        column = new MetaColumn();
                        column.setName(resultSet.getString("SYSTEM_COLUMN_NAME")); //$NON-NLS-1$
                        column.setColumnText(resultSet.getString("COLUMN_TEXT")); //$NON-NLS-1$
                        column.setDataType(resultSet.getString("DATA_TYPE")); //$NON-NLS-1$
                        column.setSize(resultSet.getInt("LENGTH")); //$NON-NLS-1$
                        column.setPrecision(resultSet.getInt("NUMERIC_SCALE")); //$NON-NLS-1$

                        table.addColumn(column);

                        nextColumn = resultSet.next();
                    }

                } else {
                    throw new Exception(Messages.MetaTableDAO_TableDefinitionNotFound);
                }
            }

        } catch (Exception exception) {

            throw exception;

        } finally {

            super.destroy(resultSet);
            super.destroy(sqlStatement);
        }
    }
}
