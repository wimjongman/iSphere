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

package biz.isphere.journalexplorer.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "biz.isphere.journalexplorer.core.messages"; //$NON-NLS-1$

    public static String E_R_R_O_R;
    public static String AddJournalDialog_AllDataRequired;
    public static String AddJournalDialog_Conection;
    public static String AddJournalDialog_FileName;
    public static String AddJournalDialog_Library;
    public static String AddJournalDialog_OpenJournal;
    public static String ConfigureParsersDialog_DefinitionLibrary;
    public static String ConfigureParsersDialog_DefinitionObject;
    public static String ConfigureParsersDialog_JournalObject;
    public static String ConfigureParsersDialog_ParsingOffset;
    public static String ConfigureParsersDialog_SetDefinitions;
    public static String JoesdParser_CLOBNotSupported;
    public static String JoesdParser_TableMetadataDontMatchEntry;
    public static String Journal_RecordNum;
    public static String JournalEntryView_CompareEntries;
    public static String JournalEntryView_ConfigureParsers;
    public static String JournalEntryView_ReloadEntries;
    public static String JournalEntryView_CompareSideBySide;
    public static String JournalEntryView_UncomparableEntries;
    public static String JournalEntryViewer_Property;
    public static String JournalEntryViewer_Value;
    public static String JournalExplorerView_OpenJournal;
    public static String JournalExplorerView_HighlightUserEntries;
    public static String JournalProperties_JOCODE;
    public static String JournalProperties_JOENTL;
    public static String JournalProperties_JOENTT;
    public static String JournalProperties_JOESD;
    public static String JournalProperties_JOSEQN;
    public static String JournalProperties_RRN_OutputFile;
    public static String MetaTableDAO_NullResultSet;
    public static String MetaTableDAO_TableDefinitionNotFound;
    public static String SelectEntriesToCompareDialog_3;
    public static String SelectEntriesToCompareDialog_ChooseBothRecordsToCompare;
    public static String SelectEntriesToCompareDialog_ChooseEntriesToCompare;
    public static String SelectEntriesToCompareDialog_ChooseLeftRecord;
    public static String SelectEntriesToCompareDialog_ChooseRightRecord;
    public static String SideBySideCompareDialog_SideBySideComparison;

    public static String ColLabel_OutputFile_Rrn;

    public static String Tooltip_OutputFile_Rrn;
    public static String Tooltip_JOENTL;
    public static String Tooltip_JOSEQN;
    public static String Tooltip_JOCODE;
    public static String Tooltip_JOENTT;
    public static String Tooltip_JODATE;
    public static String Tooltip_JOTIME;
    public static String Tooltip_JOJOB;
    public static String Tooltip_JOUSER;
    public static String Tooltip_JONBR;
    public static String Tooltip_JOPGM;
    public static String Tooltip_JOPGMLIB;
    public static String Tooltip_JOPGMDEV;
    public static String Tooltip_JOPGMASP;
    public static String Tooltip_JOOBJ;
    public static String Tooltip_JOLIB;
    public static String Tooltip_JOMBR;
    public static String Tooltip_JOCTRR;
    public static String Tooltip_JOFLAG;
    public static String Tooltip_JOCCID;
    public static String Tooltip_JOUSPF;
    public static String Tooltip_JOSYNM;
    public static String Tooltip_JOJID;
    public static String Tooltip_JORCST;
    public static String Tooltip_JOTGR;
    public static String Tooltip_JOINCDAT;
    public static String Tooltip_JOIGNAPY;
    public static String Tooltip_JOMINESD;
    public static String Tooltip_JOOBJIND;
    public static String Tooltip_JOSYSSEQ;
    public static String Tooltip_JORCV;
    public static String Tooltip_JORCVLIB;
    public static String Tooltip_JORCVDEV;
    public static String Tooltip_JORCVASP;
    public static String Tooltip_JOARM;
    public static String Tooltip_JOTHDX;
    public static String Tooltip_JOADF;
    public static String Tooltip_JORPORT;
    public static String Tooltip_JORADR;
    public static String Tooltip_JOLUW;
    public static String Tooltip_JOXID;
    public static String Tooltip_JOOBJTYP;
    public static String Tooltip_JOFILTYP;
    public static String Tooltip_JOCMTLVL;
    public static String Tooltip_JOESD;

    public static String Colors;
    public static String Enable_coloring;
    public static String Enable_coloring_tooltip;
    public static String Clear_Colors;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
