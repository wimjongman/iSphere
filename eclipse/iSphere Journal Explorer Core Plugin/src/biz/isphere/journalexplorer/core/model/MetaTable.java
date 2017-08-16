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

package biz.isphere.journalexplorer.core.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import biz.isphere.journalexplorer.core.internals.QualifiedName;
import biz.isphere.journalexplorer.core.model.dao.JournalOutputType;

/**
 * This class represents the metatada of a table. It contains the name and
 * library of the table and a list of its fields. Also it contains the name and
 * library of the table used to retrieve its structure. Most of the time the
 * attributes name and library will be equal to definitionName and
 * definitionLibrary, but this allows to override the table and library from
 * used as reference to retrieve the metadata. This can be useful if the
 * programmer wants to parse a table row with a different structure Specifying a
 * different definitionName and definitionLibrary than name and library, can
 * generate unexpected results, use with caution
 * 
 * @author Isaac Ramirez Herrera
 */
public class MetaTable {

    private String name;

    private String library;

    private String definitionName;

    private String definitionLibrary;

    private LinkedList<MetaColumn> columns;
    private Map<String, MetaColumn> columnNames;

    private boolean loaded;

    private int parsingOffset;

    private boolean isHidden;

    private Integer outfileType;

    public MetaTable(String name, String library) {

        this.columns = new LinkedList<MetaColumn>();
        this.columnNames = new HashMap<String, MetaColumn>();
        this.name = this.definitionName = name.trim();
        this.library = this.definitionLibrary = library.trim();
        this.loaded = false;
        this.parsingOffset = 0;
    }

    public boolean isJournalOutputFile() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library.trim();
    }

    public void setDefinitionLibrary(String definitionLibrary) {
        this.definitionLibrary = definitionLibrary.trim();
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName.trim();
    }

    public String getDefinitionLibrary() {
        return definitionLibrary;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public int getParsingOffset() {
        return parsingOffset;
    }

    public void setParsingOffset(int parsingOffset) {
        this.parsingOffset = parsingOffset;
    }

    public void addColumn(MetaColumn column) {
        columns.add(column);
        columnNames.put(column.getName(), column);
    }

    public MetaColumn[] getColumns() {
        return columns.toArray(new MetaColumn[columns.size()]);
    }

    public void clearColumns() {
        this.columns.clear();
    }

    public boolean hasColumn(String columnName) {
        return columnNames.containsKey(columnName);
    }

    public String getQualifiedName() {
        return QualifiedName.getName(getLibrary(), getName());
    }

    public int getOutfileType() {

        if (outfileType == null) {

            if (hasColumn("JOPGMLIB")) {
                // Added with *TYPE5
                outfileType = new Integer(JournalOutputType.TYPE5);
            } else if (hasColumn("JOJID")) {
                // Added with *TYPE4
                outfileType = new Integer(JournalOutputType.TYPE4);
            } else if (hasColumn("JOTSTP")) {
                // Added with *TYPE3
                outfileType = new Integer(JournalOutputType.TYPE3);
            } else if (hasColumn("JOUSPF")) {
                // Added with *TYPE2
                outfileType = new Integer(JournalOutputType.TYPE2);
            } else {
                outfileType = new Integer(JournalOutputType.TYPE1);
            }

        }

        return outfileType.intValue();
    }
}
