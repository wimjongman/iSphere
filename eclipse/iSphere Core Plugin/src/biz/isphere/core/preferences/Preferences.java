/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.preferences;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import biz.isphere.base.internal.FileHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.dataqueue.action.MessageLengthAction;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.preferencepages.IPreferences;
import biz.isphere.core.sourcefilesearch.SearchResultManager;

/**
 * Class to manage access to the preferences of the plugin.
 * <p>
 * Eclipse stores the preferences as <i>diffs</i> to their default values in
 * directory
 * <code>[workspace]\.metadata\.plugins\org.eclipse.core.runtime\.settings\</code>.
 * 
 * @author Thomas Raddatz
 */
public final class Preferences {

    /**
     * The instance of this Singleton class.
     */
    private static Preferences instance;

    /**
     * Global preferences of the plugin.
     */
    private IPreferenceStore preferenceStore;

    /**
     * List of date formats.
     */
    private Map<String, String> dateFormats;

    /*
     * Preferences keys:
     */

    private static final String DOMAIN = ISpherePlugin.PLUGIN_ID + "."; //$NON-NLS-1$

    private static final String SPOOLED_FILES_SAVE_DIRECTORY = DOMAIN + "SPOOLED_FILES.SAVE.DIRECTORY"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_PDF_COMMAND = DOMAIN + "SPOOLED_FILES.CONVERSION_PDF.COMMAND"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_PDF_LIBRARY = DOMAIN + "SPOOLED_FILES.CONVERSION_PDF.LIBRARY"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_PDF = DOMAIN + "SPOOLED_FILES.CONVERSION_PDF"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_HTML_COMMAND = DOMAIN + "SPOOLED_FILES.CONVERSION_HTML.COMMAND"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_HTML_LIBRARY = DOMAIN + "SPOOLED_FILES.CONVERSION_HTML.LIBRARY"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_HTML = DOMAIN + "SPOOLED_FILES.CONVERSION_HTML"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_TEXT_COMMAND = DOMAIN + "SPOOLED_FILES.CONVERSION_TEXT.COMMAND"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_TEXT_LIBRARY = DOMAIN + "SPOOLED_FILES.CONVERSION_TEXT.LIBRARY"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_CONVERSION_TEXT = DOMAIN + "SPOOLED_FILES.CONVERSION_TEXT"; //$NON-NLS-1$

    private static final String SPOOLED_FILES_DEFAULT_FORMAT = DOMAIN + "SPOOLED_FILES.DEFAULT_FORMAT"; //$NON-NLS-1$

    private static final String SOURCEFILESEARCH_SEARCHSTRING = DOMAIN + "SOURCEFILESEARCH.SEARCHSTRING"; //$NON-NLS-1$

    private static final String MESSAGEFILESEARCH_SEARCHSTRING = DOMAIN + "MESSAGEFILESEARCH.SEARCHSTRING"; //$NON-NLS-1$

    private static final String ISPHERE_LIBRARY = DOMAIN + "LIBRARY"; //$NON-NLS-1$

    private static final String HOST_NAME = DOMAIN + "HOST_NAME"; //$NON-NLS-1$

    private static final String FTP_PORT_NUMBER = DOMAIN + "FTP_PORT_NUMBER"; //$NON-NLS-1$

    private static final String SEARCH_FOR_UPDATES = DOMAIN + "SEARCH_FOR_UPDATES"; //$NON-NLS-1$

    private static final String SEARCH_FOR_BETA_VERSIONS = DOMAIN + "SEARCH_FOR_BETA_VERSIONS"; //$NON-NLS-1$

    private static final String URL_FOR_UPDATES = DOMAIN + "URL_FOR_UPDATES"; //$NON-NLS-1$

    private static final String LAST_VERSION_FOR_UPDATES = DOMAIN + "LAST_VERSION_FOR_UPDATES"; //$NON-NLS-1$

    private static final String MONITOR = DOMAIN + "MONITOR."; //$NON-NLS-1$

    private static final String MONITOR_DTAQ = MONITOR + "DTAQ."; //$NON-NLS-1$

    public static final String MONITOR_DTAQ_LENGTH = MONITOR_DTAQ + "LENGTH"; //$NON-NLS-1$

    public static final String MONITOR_DTAQ_VIEW_IN_HEX = MONITOR_DTAQ + "VIEW_IN_HEX"; //$NON-NLS-1$

    public static final String MONITOR_DTAQ_DISPLAY_END_OF_DATA = MONITOR_DTAQ + "DISPLAY_END_OF_DATA"; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH_RESULTS = DOMAIN + "SOURCE_FILE_SEARCH_RESULTS."; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED = SOURCE_FILE_SEARCH_RESULTS + "IS_EDIT_ENABLED"; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY = SOURCE_FILE_SEARCH_RESULTS + "SAVE_DIRECTORY"; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME = SOURCE_FILE_SEARCH_RESULTS + "LAST_USED_FILE_NAME"; //$NON-NLS-1$

    public static final String SOURCE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED = SOURCE_FILE_SEARCH_RESULTS + "IS_AUTO_SAVE_ENABLED"; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE = SOURCE_FILE_SEARCH_RESULTS + "AUTO_SAVE_FILE"; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH_DIRECTORY = "sourceFileSearch"; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH_FILE_NAME = "SourceFileSearchResult"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_SEARCH_RESULTS = DOMAIN + "MESSAGE_FILE_SEARCH_RESULTS."; //$NON-NLS-1$

    private static final String MESSAGE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY = MESSAGE_FILE_SEARCH_RESULTS + "SAVE_DIRECTORY"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME = MESSAGE_FILE_SEARCH_RESULTS + "LAST_USED_FILE_NAME"; //$NON-NLS-1$

    public static final String MESSAGE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED = MESSAGE_FILE_SEARCH_RESULTS + "IS_AUTO_SAVE_ENABLED"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE = MESSAGE_FILE_SEARCH_RESULTS + "AUTO_SAVE_FILE"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_SEARCH_DIRECTORY = "messageFileSearch"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_SEARCH_FILE_NAME = "MessageFileSearchResult"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_COMPARE = DOMAIN + "MESSAGE_FILE_COMPARE."; //$NON-NLS-1$

    private static final String MESSAGE_FILE_COMPARE_LINE_WIDTH = MESSAGE_FILE_COMPARE + "LINE_WIDTH"; //$NON-NLS-1$

    private static final String APPEARANCE = DOMAIN + "APPEARANCE."; //$NON-NLS-1$

    private static final String APPEARANCE_DATE_FORMAT = APPEARANCE + "DATE_FORMAT"; //$NON-NLS-1$

    private static final String APPEARANCE_DATE_FORMAT_LOCALE = "*LOCALE"; //$NON-NLS-1$

    /**
     * Private constructor to ensure the Singleton pattern.
     */
    private Preferences() {
    }

    /**
     * Thread-safe method that returns the instance of this Singleton class.
     */
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
            instance.preferenceStore = ISpherePlugin.getDefault().getPreferenceStore();
        }
        return instance;
    }

    /**
     * Thread-safe method that disposes the instance of this Singleton class.
     * <p>
     * This method is intended to be call from
     * {@link org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)}
     * to free the reference to itself.
     */
    public static void dispose() {
        if (instance != null) {
            instance = null;
        }
    }

    /*
     * Preferences: GETTER
     */

    public String getISphereLibrary() {
        return preferenceStore.getString(ISPHERE_LIBRARY);
    }

    public String getHostName() {
        return preferenceStore.getString(HOST_NAME);
    }

    public int getFtpPortNumber() {
        return preferenceStore.getInt(FTP_PORT_NUMBER);
    }

    public boolean isSearchForUpdates() {
        return preferenceStore.getBoolean(SEARCH_FOR_UPDATES);
    }

    public boolean isSearchForBetaVersions() {
        return preferenceStore.getBoolean(SEARCH_FOR_BETA_VERSIONS);
    }

    public String getURLForUpdates() {
        return preferenceStore.getString(URL_FOR_UPDATES);
    }

    public String getLastVersionForUpdates() {
        return preferenceStore.getString(LAST_VERSION_FOR_UPDATES);
    }

    public String getMessageFileSearchString() {
        return preferenceStore.getString(MESSAGEFILESEARCH_SEARCHSTRING);
    }

    public String getSourceFileSearchString() {
        return preferenceStore.getString(SOURCEFILESEARCH_SEARCHSTRING);
    }

    public String getSpooledFileConversionDefaultFormat() {
        return preferenceStore.getString(SPOOLED_FILES_DEFAULT_FORMAT);
    }

    public String getSpooledFileConversionText() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_TEXT);
    }

    public String getSpooledFileConversionTextLibrary() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_TEXT_LIBRARY);
    }

    public String getSpooledFileConversionTextCommand() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_TEXT_COMMAND);
    }

    public String getSpooledFileConversionHTML() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_HTML);
    }

    public String getSpooledFileConversionHTMLLibrary() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_HTML_LIBRARY);
    }

    public String getSpooledFileConversionHTMLCommand() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_HTML_COMMAND);
    }

    public String getSpooledFileConversionPDF() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_PDF);
    }

    public String getSpooledFileConversionPDFLibrary() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_PDF_LIBRARY);
    }

    public String getSpooledFileConversionPDFCommand() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_PDF_COMMAND);
    }

    public String getSpooledFileSaveDirectory() {
        String directory = preferenceStore.getString(SPOOLED_FILES_SAVE_DIRECTORY);
        if (StringHelper.isNullOrEmpty(directory)) {
            return FileHelper.getDefaultRootDirectory();
        }
        return directory;
    }

    public int getDataQueueMaximumMessageLength() {
        return preferenceStore.getInt(MONITOR_DTAQ_LENGTH);
    }

    public boolean isDataQueueViewInHex() {
        return preferenceStore.getBoolean(MONITOR_DTAQ_VIEW_IN_HEX);
    }

    public boolean isDataQueueDisplayEndOfData() {
        return preferenceStore.getBoolean(MONITOR_DTAQ_DISPLAY_END_OF_DATA);
    }

    public boolean isSourceFileSearchResultsEditEnabled() {
        return preferenceStore.getBoolean(SOURCE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED);
    }

    public String getSourceFileSearchResultsSaveDirectory() {
        String directory = preferenceStore.getString(SOURCE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY);
        if (!directory.endsWith(File.separator)) {
            directory = directory + File.separator;
        }
        return directory;
    }

    public String getSourceFileSearchResultsLastUsedFileName() {
        String filename = preferenceStore.getString(SOURCE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME);
        if (!StringHelper.isNullOrEmpty(filename)) {
            File file = new File(filename);
            if (!file.exists()) {
                filename = null;
            } else {
                if (file.isDirectory()) {
                    filename = filename + SOURCE_FILE_SEARCH_FILE_NAME; //$NON-NLS-1$
                }
            }
        } else {
            filename = null;
        }

        if (filename == null) {
            return getDefaultSourceFileSearchResultsSaveDirectory() + SOURCE_FILE_SEARCH_FILE_NAME; //$NON-NLS-1$
        }

        return filename;
    }

    public boolean isSourceFileSearchResultsAutoSaveEnabled() {

        /*
         * Does not work, because we cannot create an AS400 object, when loading
         * a search result.
         */
        if (!IBMiHostContributionsHandler.hasContribution()) {
            return false;
        }

        return preferenceStore.getBoolean(SOURCE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED);
    }

    public String getSourceFileSearchResultsAutoSaveFileName() {
        return preferenceStore.getString(SOURCE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE);
    }

    public String getMessageFileSearchResultsSaveDirectory() {
        String directory = preferenceStore.getString(MESSAGE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY);
        if (!directory.endsWith(File.separator)) {
            directory = directory + File.separator;
        }
        return directory;
    }

    public String getMessageFileSearchResultsLastUsedFileName() {
        String filename = preferenceStore.getString(MESSAGE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME);
        if (!StringHelper.isNullOrEmpty(filename)) {
            File file = new File(filename);
            if (!file.exists()) {
                filename = null;
            } else {
                if (file.isDirectory()) {
                    filename = filename + MESSAGE_FILE_SEARCH_FILE_NAME; //$NON-NLS-1$
                }
            }
        } else {
            filename = null;
        }

        if (filename == null) {
            return getDefaultMessageFileSearchResultsSaveDirectory() + MESSAGE_FILE_SEARCH_FILE_NAME; //$NON-NLS-1$
        }

        return filename;
    }

    public boolean isMessageFileSearchResultsAutoSaveEnabled() {
        return preferenceStore.getBoolean(MESSAGE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED);
    }

    public String getMessageFileSearchResultsAutoSaveFileName() {
        return preferenceStore.getString(MESSAGE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE);
    }

    public int getMessageFileCompareLineWidth() {
        return preferenceStore.getInt(MESSAGE_FILE_COMPARE_LINE_WIDTH);
    }

    public String getDateFormatLabel() {
        return preferenceStore.getString(APPEARANCE_DATE_FORMAT);
    }

    /*
     * Preferences: SETTER
     */

    public void setISphereLibrary(String aLibrary) {
        preferenceStore.setValue(ISPHERE_LIBRARY, aLibrary.trim());
    }

    public void setHostName(String aHostName) {
        preferenceStore.setValue(HOST_NAME, aHostName);
    }

    public void setFtpPortNumber(int aPortNumber) {
        preferenceStore.setValue(FTP_PORT_NUMBER, aPortNumber);
    }

    public void setSearchForUpdates(boolean aSearchForUpdates) {
        preferenceStore.setValue(SEARCH_FOR_UPDATES, aSearchForUpdates);
    }

    public void setSearchForBetaVersions(boolean aSearchForUpdates) {
        preferenceStore.setValue(SEARCH_FOR_BETA_VERSIONS, aSearchForUpdates);
    }

    public void setURLForUpdates(String aURLForUpdates) {
        preferenceStore.setValue(URL_FOR_UPDATES, aURLForUpdates);
    }

    public void setLastVersionForUpdates(String aLastVersionForUpdates) {
        preferenceStore.setValue(LAST_VERSION_FOR_UPDATES, aLastVersionForUpdates);
    }

    public void setMessageFileSearchString(String aSearchString) {
        preferenceStore.setValue(MESSAGEFILESEARCH_SEARCHSTRING, aSearchString.trim());
    }

    public void setSourceFileSearchString(String aSearchString) {
        preferenceStore.setValue(SOURCEFILESEARCH_SEARCHSTRING, aSearchString.trim());
    }

    public void setSpooledFileDefaultFormat(String aFormat) {
        preferenceStore.setValue(SPOOLED_FILES_DEFAULT_FORMAT, aFormat);
    }

    public void setSpooledFileConversionText(String aConversionType) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_TEXT, aConversionType);
    }

    public void setSpooledFileConversionLibraryText(String aLibrary) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_TEXT_LIBRARY, aLibrary);
    }

    public void setSpooledFileConversionCommandText(String aCommand) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_TEXT_COMMAND, aCommand);
    }

    public void setSpooledFileConversionHTML(String aConversionType) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_HTML, aConversionType);
    }

    public void setSpooledFileConversionLibraryHTML(String aLibrary) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_HTML_LIBRARY, aLibrary);
    }

    public void setSpooledFileConversionCommandHTML(String aCommand) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_HTML_COMMAND, aCommand);
    }

    public void setSpooledFileConversionPDF(String aConversionType) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_PDF, aConversionType);
    }

    public void setSpooledFileConversionLibraryPDF(String aLibrary) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_PDF_LIBRARY, aLibrary);
    }

    public void setSpooledFileConversionCommandPDF(String aCommand) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_PDF_COMMAND, aCommand);
    }

    public void setSpooledFileSaveDirectory(String aDirectory) {
        preferenceStore.setValue(SPOOLED_FILES_SAVE_DIRECTORY, aDirectory);
    }

    public void setDataQueueMaximumMessageLength(int length) {
        preferenceStore.setValue(MONITOR_DTAQ_LENGTH, length);
    }

    public void setDataQueueViewInHex(boolean viewInHex) {
        preferenceStore.setValue(MONITOR_DTAQ_VIEW_IN_HEX, viewInHex);
    }

    public void setDataQueueDisplayEndOfData(boolean viewInHex) {
        preferenceStore.setValue(MONITOR_DTAQ_DISPLAY_END_OF_DATA, viewInHex);
    }

    public void setSourceFileSearchResultsEditEnabled(boolean editable) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED, editable);
    }

    public void setSourceFileSearchResultsSaveDirectory(String directory) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, directory);
    }

    public void setSourceFileSearchResultsLastUsedFileName(String directory) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, directory);
    }

    public void setSourceFileSearchResultsAutoSaveEnabled(boolean enabled) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, enabled);
    }

    public void setSourceFileSearchResultsAutoSaveFileName(String filename) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, filename);
    }

    public void setMessageFileSearchResultsSaveDirectory(String directory) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, directory);
    }

    public void setMessageFileSearchResultsLastUsedFileName(String directory) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, directory);
    }

    public void setMessageFileSearchResultsAutoSaveEnabled(boolean enabled) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, enabled);
    }

    public void setMessageFileSearchResultsAutoSaveFileName(String filename) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, filename);
    }

    public void setMessageFileCompareLineWidth(int lineWidth) {
        preferenceStore.setValue(MESSAGE_FILE_COMPARE_LINE_WIDTH, lineWidth);
    }

    public void setDateFormatLabel(String dateFormatLabel) {
        preferenceStore.setValue(APPEARANCE_DATE_FORMAT, dateFormatLabel);
    }

    /*
     * Preferences: Default Initializer
     */

    public void initializeDefaultPreferences() {
        preferenceStore.setDefault(ISPHERE_LIBRARY, getDefaultISphereLibrary());
        preferenceStore.setDefault(HOST_NAME, getDefaultHostName());
        preferenceStore.setDefault(FTP_PORT_NUMBER, getDefaultFtpPortNumber());

        preferenceStore.setDefault(SEARCH_FOR_UPDATES, getDefaultSearchForUpdates());
        preferenceStore.setDefault(SEARCH_FOR_BETA_VERSIONS, getDefaultSearchForBetaVersions());
        preferenceStore.setDefault(URL_FOR_UPDATES, getDefaultURLForUpdates());
        preferenceStore.setDefault(LAST_VERSION_FOR_UPDATES, getDefaultLastVersionForUpdates());

        preferenceStore.setDefault(SPOOLED_FILES_DEFAULT_FORMAT, getDefaultSpooledFileConversionDefaultFormat());

        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_TEXT, getDefaultSpooledFileConversionText());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_TEXT_COMMAND, getDefaultSpooledFileConversionTextCommand());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_TEXT_LIBRARY, getDefaultSpooledFileConversionTextLibrary());

        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_HTML, getDefaultSpooledFileConversionHTML());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_HTML_COMMAND, getDefaultSpooledFileConversionHTMLCommand());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_HTML_LIBRARY, getDefaultSpooledFileConversionHTMLLibrary());

        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_PDF, getDefaultSpooledFileConversionPDF());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_PDF_COMMAND, getDefaultSpooledFileConversionPDFCommand());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_PDF_LIBRARY, getDefaultSpooledFileConversionPDFLibrary());

        preferenceStore.setDefault(MONITOR_DTAQ_LENGTH, getDefaultDataQueueMaximumMessageLength());
        preferenceStore.setDefault(MONITOR_DTAQ_VIEW_IN_HEX, getDefaultDataQueueViewInHex());
        preferenceStore.setDefault(MONITOR_DTAQ_DISPLAY_END_OF_DATA, getDefaultDataQueueDisplayEndOfData());

        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED, getDefaultSourceFileSearchResultsEditEnabled());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, getDefaultSourceFileSearchResultsSaveDirectory());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, getDefaultSourceFileSearchResultsLastUsedFileName());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, getDefaultSourceFileSearchResultsAutoSaveEnabled());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, getDefaultSourceFileSearchResultsAutoSaveFileName());

        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, getDefaultMessageFileSearchResultsSaveDirectory());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, getDefaultMessageFileSearchResultsLastUsedFileName());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, getDefaultMessageFileSearchResultsAutoSaveEnabled());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, getDefaultMessageFileSearchResultsAutoSaveFileName());

        preferenceStore.setDefault(MESSAGE_FILE_COMPARE_LINE_WIDTH, getDefaultMessageFileCompareMinLineWidth());

        preferenceStore.setDefault(APPEARANCE_DATE_FORMAT, getDefaultDateFormatLabel());
    }

    /*
     * Preferences: Default Values
     */

    /**
     * Returns the default iSphere library name.
     * 
     * @return default iSphere library name
     */
    public String getDefaultISphereLibrary() {
        return "ISPHERE";
    }

    /**
     * Returns the default host name where to upload the iSphere library.
     * 
     * @return default host name
     */
    public String getDefaultHostName() {
        return "";
    }

    /**
     * Returns the default FTP port number.
     * 
     * @return default FTPport number
     */
    public int getDefaultFtpPortNumber() {
        return 21;
    }

    /**
     * Returns the default search for updates flag.
     * 
     * @return default search for updates flag.
     */
    public boolean getDefaultSearchForUpdates() {
        return true;
    }

    /**
     * Returns the default 'search for beta versions' flag.
     * 
     * @return default 'search for beta versions' flag.
     */
    public boolean getDefaultSearchForBetaVersions() {
        return false;
    }

    /**
     * Returns the default URL for updates.
     * 
     * @return default URL for updates.
     */
    public String getDefaultURLForUpdates() {
        return "http://sourceforge.net/p/isphere/code/HEAD/tree/trunk/build/iSphere%20Notifier/MANIFEST.MF?format=raw";
    }

    /**
     * Returns the default last version for updates.
     * 
     * @return default last version for updates.
     */
    public String getDefaultLastVersionForUpdates() {
        return "0.0.0.r";
    }

    /**
     * Returns the default format for spooled file conversion on double-click on
     * a spooled file.
     * 
     * @return default format on spooled file double-click
     */
    public String getDefaultSpooledFileConversionDefaultFormat() {
        return IPreferences.OUTPUT_FORMAT_TEXT;
    }

    /**
     * Returns the default conversion type for spooled file to text conversion.
     * 
     * @return default spooled file to TEXT conversion type
     */
    public String getDefaultSpooledFileConversionText() {
        return IPreferences.SPLF_CONVERSION_TRANSFORM;
    }

    /**
     * Return the default library name for user defined spooled file to text
     * conversion.
     * 
     * @return library name for user defined spooled file to text conversion
     */
    public String getDefaultSpooledFileConversionTextLibrary() {
        return "";
    }

    /**
     * Return the default command for user defined spooled file to text
     * conversion.
     * 
     * @return command for user defined spooled file to text conversion
     */
    public String getDefaultSpooledFileConversionTextCommand() {
        return "";
    }

    /**
     * Returns the default conversion type for spooled file to HTML conversion.
     * 
     * @return default spooled file to HTML conversion type
     */
    public String getDefaultSpooledFileConversionHTML() {
        return IPreferences.SPLF_CONVERSION_TRANSFORM;
    }

    /**
     * Return the default library name for user defined spooled file to HTML
     * conversion.
     * 
     * @return library name for user defined spooled file to HTML conversion
     */
    public String getDefaultSpooledFileConversionHTMLLibrary() {
        return "";
    }

    /**
     * Return the default command for user defined spooled file to HTML
     * conversion.
     * 
     * @return command for user defined spooled file to HTML conversion
     */
    public String getDefaultSpooledFileConversionHTMLCommand() {
        return "";
    }

    /**
     * Returns the default conversion type for spooled file to PDF conversion.
     * 
     * @return default spooled file to PDF conversion type
     */
    public String getDefaultSpooledFileConversionPDF() {
        return IPreferences.SPLF_CONVERSION_TRANSFORM;
    }

    /**
     * Return the default library name for user defined spooled file to PDF
     * conversion.
     * 
     * @return library name for user defined spooled file to PDF conversion
     */
    public String getDefaultSpooledFileConversionPDFLibrary() {
        return "";
    }

    /**
     * Return the default command for user defined spooled file to PDF
     * conversion.
     * 
     * @return command for user defined spooled file to PDF conversion
     */
    public String getDefaultSpooledFileConversionPDFCommand() {
        return "";
    }

    /**
     * Return the default command for user defined spooled file to PDF
     * conversion.
     * 
     * @return command for user defined spooled file to PDF conversion
     */
    public int getDefaultDataQueueMaximumMessageLength() {
        return 2048;
    }

    /**
     * Returns the default 'view hex' flag of the data queue view.
     * 
     * @return default 'view hex' flag.
     */
    public boolean getDefaultDataQueueViewInHex() {
        return true;
    }

    /**
     * Returns the default 'display end of data' flag of the data queue view.
     * 
     * @return default 'display end of data' flag.
     */
    public boolean getDefaultDataQueueDisplayEndOfData() {
        return false;
    }

    /**
     * Returns the default 'is edit mode' flag of the view search results view.
     * 
     * @return default 'is edit mode' flag.
     */
    public boolean getDefaultSourceFileSearchResultsEditEnabled() {
        return true;
    }

    /**
     * Returns the default 'source file search save path'.
     * 
     * @return default path for saving source file search results
     */
    public String getDefaultSourceFileSearchResultsSaveDirectory() {

        String path = ISpherePlugin.getDefault().getStateLocation().toFile().getAbsolutePath();
        path = path + File.separator + SOURCE_FILE_SEARCH_DIRECTORY + File.separator;

        try {
            FileHelper.ensureDirectory(path);
        } catch (Throwable e) {
            ISpherePlugin.logError("Failed to create directory: " + path, e); //$NON-NLS-1$
        }

        return path;
    }

    /**
     * Returns the default 'save path' that was last selected to save source
     * file search results.
     * 
     * @return default path last used for saving
     */
    public String getDefaultSourceFileSearchResultsLastUsedFileName() {
        return "";
    }

    /**
     * Returns the default 'is auto save' flag of the view search results view.
     * 
     * @return default 'is auto save' flag.
     */
    public boolean getDefaultSourceFileSearchResultsAutoSaveEnabled() {
        return false;
    }

    /**
     * Returns the default 'source file search auto save file name'.
     * 
     * @return default file name for saving search results
     */
    public String getDefaultSourceFileSearchResultsAutoSaveFileName() {

        return "iSphereSourceFileSearchResultAutoSave." + SearchResultManager.FILE_EXTENSION;
    }

    /**
     * Returns the default 'message file search save path'.
     * 
     * @return default path for saving message file search results
     */
    public String getDefaultMessageFileSearchResultsSaveDirectory() {

        String path = ISpherePlugin.getDefault().getStateLocation().toFile().getAbsolutePath();
        path = path + File.separator + MESSAGE_FILE_SEARCH_DIRECTORY + File.separator;

        try {
            FileHelper.ensureDirectory(path);
        } catch (Throwable e) {
            ISpherePlugin.logError("Failed to create directory: " + path, e); //$NON-NLS-1$
        }

        return path;
    }

    /**
     * Returns the default 'save path' that was last selected to save message
     * file search results.
     * 
     * @return default path last used for saving
     */
    public String getDefaultMessageFileSearchResultsLastUsedFileName() {
        return "";
    }

    /**
     * Returns the default 'is auto save' flag of the view search results view.
     * 
     * @return default 'is auto save' flag.
     */
    public boolean getDefaultMessageFileSearchResultsAutoSaveEnabled() {
        return false;
    }

    /**
     * Returns the default 'message file search auto save file name'.
     * 
     * @return default file name for saving search results
     */
    public String getDefaultMessageFileSearchResultsAutoSaveFileName() {

        return "iSphereMessageFileSearchResultAutoSave." + SearchResultManager.FILE_EXTENSION;
    }

    /**
     * Returns the default 'line width' for comparing message files.
     * 
     * @return default line width of first and second level text
     */
    public int getDefaultMessageFileCompareMinLineWidth() {

        return 70;
    }

    public String getDefaultDateFormatLabel() {
        return APPEARANCE_DATE_FORMAT_LOCALE;
    }

    /**
     * Returns an arrays of maximum lengths values for retrieving data queue
     * entries.
     * 
     * @return message length values
     */
    public int[] getDataQueueMaximumMessageLengthValues() {

        int[] lengths = new int[6];
        lengths[0] = -1;
        lengths[1] = 64;
        lengths[2] = 512;
        lengths[3] = 2048;
        lengths[4] = 8196;
        lengths[5] = MessageLengthAction.MAX_LENGTH;

        Arrays.sort(lengths);
        if (Arrays.binarySearch(lengths, getDataQueueMaximumMessageLength()) < 0) {
            lengths[0] = getDataQueueMaximumMessageLength();
            Arrays.sort(lengths);
            return lengths;
        }

        int[] lengths2 = new int[lengths.length - 1];
        System.arraycopy(lengths, 1, lengths2, 0, lengths2.length);
        return lengths2;
    }

    public DateFormat getDateFormatter() {

        String pattern = getDateFormatsMap().get(getDateFormatLabel());
        if (pattern == null) {
            pattern = getDateFormatsMap().get(getDefaultDateFormatLabel());
        }

        if (pattern == null) {
            return DateFormat.getDateInstance(DateFormat.SHORT);
        }

        return new SimpleDateFormat(pattern);
    }

    public String[] getDateFormatLabels() {

        Set<String> formats = getDateFormatsMap().keySet();

        String[] dateFormats = formats.toArray(new String[formats.size()]);
        Arrays.sort(dateFormats);

        return dateFormats;
    }

    public Map<String, String> getDateFormatsMap() {

        if (dateFormats != null) {
            return dateFormats;
        }

        dateFormats = new HashMap<String, String>();

        dateFormats.put(getDefaultDateFormatLabel(), null);
        dateFormats.put("de (dd.mm.yyyy)", "dd.MM.yyyy");
        dateFormats.put("us (mm/dd/yyyy)", "MM/dd/yyyy");

        return dateFormats;
    }
}