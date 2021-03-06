/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import biz.isphere.core.internal.FilterUpdateType;
import biz.isphere.rse.Messages;
import biz.isphere.rse.resourcemanagement.filter.RSEFilterHelper;

import com.ibm.etools.iseries.comm.filters.ISeriesLibraryFilterString;
import com.ibm.etools.iseries.comm.filters.ISeriesMemberFilterString;
import com.ibm.etools.iseries.comm.filters.ISeriesObjectFilterString;
import com.ibm.etools.iseries.core.IISeriesFilterTypes;
import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.systems.filters.SystemFilter;
import com.ibm.etools.systems.filters.SystemFilterPool;
import com.ibm.etools.systems.filters.SystemFilterPoolManager;
import com.ibm.etools.systems.subsystems.SubSystem;

public class RSEExportToFilterHelper {

    public static SystemFilter createOrUpdateMemberFilter(String connectionName, String filterPoolName, String filterName,
        FilterUpdateType filterUpdateType, ISeriesMemberFilterString[] filterStrings) {

        Vector<String> _filterStrings = new Vector<String>();
        for (int idx = 0; idx < filterStrings.length; idx++) {
            _filterStrings.add(filterStrings[idx].toString());
        }

        return createFilter(connectionName, filterPoolName, IISeriesFilterTypes.FILTERTYPE_MEMBER, filterName, filterUpdateType, _filterStrings);

    }

    public static SystemFilter createOrUpdateObjectFilter(String connectionName, String filterPoolName, String filterName,
        FilterUpdateType filterUpdateType, ISeriesObjectFilterString[] filterStrings) {

        Vector<String> _filterStrings = new Vector<String>();
        for (int idx = 0; idx < filterStrings.length; idx++) {
            _filterStrings.add(filterStrings[idx].toString());
        }

        return createFilter(connectionName, filterPoolName, IISeriesFilterTypes.FILTERTYPE_OBJECT, filterName, filterUpdateType, _filterStrings);

    }

    public static SystemFilter createOrUpdateLibraryFilter(String connectionName, String filterPoolName, String filterName,
        FilterUpdateType filterUpdateType, ISeriesLibraryFilterString[] filterStrings) {

        Vector<String> _filterStrings = new Vector<String>();
        for (int idx = 0; idx < filterStrings.length; idx++) {
            _filterStrings.add(filterStrings[idx].toString());
        }

        return createFilter(connectionName, filterPoolName, IISeriesFilterTypes.FILTERTYPE_LIBRARY, filterName, filterUpdateType, _filterStrings);

    }

    private static SystemFilter createFilter(String connectionName, String filterPoolName, String filterType, String filterName,
        FilterUpdateType filterUpdateType, Vector<String> filterStrings) {

        SystemFilterPool filterPool = null;

        SystemFilterPool[] pools = RSEFilterHelper.getFilterPools(connectionName);
        if (pools != null && pools.length >= 1) {

            for (SystemFilterPool pool : pools) {
                if (filterPoolName != null) {
                    if (pool.getName().equals(filterPoolName)) {
                        filterPool = pool;
                    }
                } else {
                    if (pool.isDefault()) {
                        filterPool = pool;
                    }
                }
            }
        }

        if (filterPool == null) {
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.E_R_R_O_R,
                Messages.No_filter_pool_available);
            return null;
        }

        try {

            SubSystem subsystem = getSubSystem(getConnection(connectionName));
            SystemFilterPoolManager dftPoolMgr = subsystem.getFilterPoolReferenceManager().getDefaultSystemFilterPoolManager();

            if (filterExists(filterPool, filterName)) {
                if (filterUpdateType == FilterUpdateType.REPLACE) {
                    removeAllFilterStrings(filterPool, filterName);
                }
                updateFilterStrings(dftPoolMgr, filterPool, filterName, filterStrings);
            } else {
                return dftPoolMgr.createSystemFilter(filterPool, filterName, filterStrings, filterType);
            }

        } catch (Exception e) {
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.E_R_R_O_R, e.getLocalizedMessage());
        }

        return null;
    }

    private static void updateFilterStrings(SystemFilterPoolManager dftPoolMgr, SystemFilterPool filterPool, String filterName,
        Vector<String> filterStrings) {

        SystemFilter systemFilter = filterPool.getSystemFilter(filterName);
        boolean isCaseSensitive = systemFilter.areStringsCaseSensitive();
        String[] existingFiltersStrings = systemFilter.getFilterStrings();
        if (!isCaseSensitive) {
            for (int i = 0; i < existingFiltersStrings.length; i++) {
                existingFiltersStrings[i] = existingFiltersStrings[i].toLowerCase();
            }
        }

        Set<String> existingFiltersSet = new HashSet<String>(Arrays.asList(existingFiltersStrings));
        String tFilterString;
        for (String filterString : filterStrings) {
            if (!isCaseSensitive) {
                tFilterString = filterString.toLowerCase();
            } else {
                tFilterString = filterString;
            }
            if (!existingFiltersSet.contains(tFilterString)) {
                systemFilter.addFilterString(filterString);
            }
        }

        dftPoolMgr.getProvider().filterEventFilterUpdated(filterPool.getSystemFilter(filterName));
    }

    private static void removeAllFilterStrings(SystemFilterPool filterPool, String filterName) {

        SystemFilter filter = filterPool.getSystemFilter(filterName);
        int count = filter.getFilterStringCount();
        while (count > 0) {
            filter.removeFilterString(0);
            count--;
        }
    }

    private static boolean filterExists(SystemFilterPool filterPool, String filterName) {

        Vector<String> filterNames = filterPool.getSystemFilterNames();
        for (int idx = 0; idx < filterNames.size(); idx++) {
            if (filterNames.get(idx).equals(filterName)) {
                return true;
            }

        }

        return false;
    }

    private static ISeriesConnection getConnection(String connectionName) {

        ISeriesConnection connection = ISeriesConnection.getConnection(connectionName);

        return connection;

    }

    private static SubSystem getSubSystem(ISeriesConnection connection) {
        return connection.getISeriesFileSubSystem();
    }

}
