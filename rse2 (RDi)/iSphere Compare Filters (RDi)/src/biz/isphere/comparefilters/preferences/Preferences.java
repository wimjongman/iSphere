/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.comparefilters.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.jface.preference.IPreferenceStore;

import biz.isphere.base.internal.StringHelper;
import biz.isphere.comparefilters.ISphereCompareFiltersPlugin;

/**
 * Class to manage access to the preferences of the plugin.
 * 
 * @author Thomas Raddatz
 */
public final class Preferences {

    private static final String TOKEN_SEPARATOR = "|"; //$NON-NLS-1$

    /**
     * The instance of this Singleton class.
     */
    private static Preferences instance;

    /**
     * Global preferences of the LPEX Task-Tags plugin.
     */
    private IPreferenceStore preferenceStore;

    private HashSet<String> fileExtensionsSet;

    /**
     * Base configuration key:
     */
    private static final String DOMAIN = ISphereCompareFiltersPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

    public static final String COMPARE_FILTER_FILE_EXTENSIONS = DOMAIN + "fileextensions"; //$NON-NLS-1$

    public static final String COMPARE_FILTER_IMPORT_EXPORT_LOCATION = DOMAIN + "importexportlocation"; //$NON-NLS-1$

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
            instance.preferenceStore = ISphereCompareFiltersPlugin.getDefault().getPreferenceStore();
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

    public String[] getFileExtensions() {
        return getFileExtensions(false);
    }

    public String[] getDefaultFileExtensions() {
        String tList = getDefaultFileExtensionsAsString();
        return StringHelper.getTokens(tList, TOKEN_SEPARATOR);
    }

    public boolean supportsFileExtension(String fileExtension) {
        if (getOrCreateFileExtensionsSet().contains(fileExtension.toUpperCase())) {
            return true;
        }
        return false;
    }

    public String getImportExportLocation() {
        return preferenceStore.getString(COMPARE_FILTER_IMPORT_EXPORT_LOCATION);
    }

    /*
     * Preferences: SETTER
     */

    public void setFileExtensions(String[] anExtensions) {
        saveFileExtensions(anExtensions);
    }

    public void setImportExportLocation(String aLocation) {
        saveImportExportLocation(aLocation);
    }

    /*
     * Preferences: Default Initializer
     */

    public void initializeDefaultPreferences() {
        preferenceStore.setDefault(COMPARE_FILTER_FILE_EXTENSIONS, getDefaultFileExtensionsAsString());
        preferenceStore.setDefault(COMPARE_FILTER_IMPORT_EXPORT_LOCATION, getDefaultImportExportLocation());
    }

    /*
     * Preferences: Default Values
     */

    public String getDefaultFileExtensionsAsString() {
        return "bnd,c,cle,cbl,cblle,clle,clp,cmd,dspf,lf,menu,mnu,mnucmd,mnudds,pf,pnlgrp,prtf,rexx,rpg,rpgle,sqlc,sqlcbl,sqlcblle,sqlcpp,sqlrpg,sqlrpgle,tbl,txt".replaceAll(",", TOKEN_SEPARATOR); //$NON-NLS-1$
    }

    public String getDefaultImportExportLocation() {
        return "";
    }

    /*
     * Preferences: Save Values
     */

    private void saveFileExtensions(String[] anExtensions) {
        preferenceStore.setValue(COMPARE_FILTER_FILE_EXTENSIONS, StringHelper.concatTokens(anExtensions, TOKEN_SEPARATOR));
        fileExtensionsSet = null;
    }

    private void saveImportExportLocation(String aLocation) {
        if (new File(aLocation).exists()) {
            preferenceStore.setValue(COMPARE_FILTER_IMPORT_EXPORT_LOCATION, aLocation);
        }
    }

    /*
     * Others.
     */

    private HashSet<String> getOrCreateFileExtensionsSet() {
        if (fileExtensionsSet == null) {
            fileExtensionsSet = new HashSet<String>(Arrays.asList(getFileExtensions(true)));
        }
        return fileExtensionsSet;
    }

    private String[] getFileExtensions(boolean anUpperCase) {
        String tList = preferenceStore.getString(COMPARE_FILTER_FILE_EXTENSIONS);
        if (anUpperCase) {
            return StringHelper.getTokens(tList.toUpperCase(), TOKEN_SEPARATOR);
        } else {
            return StringHelper.getTokens(tList, TOKEN_SEPARATOR);
        }
    }
}