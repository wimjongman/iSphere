/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.sourcefilesearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.ISphereHelper;
import biz.isphere.core.search.SearchOptions;

import com.ibm.as400.access.AS400;

public class SearchExec {

    private class Search extends Job {

        private AS400 _as400;
        private Connection _jdbcConnection;
        private SearchOptions _searchOptions;
        private ArrayList<SearchElement> _searchElements;
        private ISearchPostRun _searchPostRun;
        private int _handle;
        private int _counter;
        private String iSphereLibrary;

        public Search(AS400 _as400, Connection _jdbcConnection, SearchOptions _searchOptions, ArrayList<SearchElement> _searchElements,
            ISearchPostRun _searchPostRun) {

            super("iSphere Source File Search"); //$NON-NLS-1$

            this._as400 = _as400;
            this._jdbcConnection = _jdbcConnection;
            this._searchOptions = _searchOptions;
            this._searchElements = _searchElements;
            this._searchPostRun = _searchPostRun;

            iSphereLibrary = ISpherePlugin.getISphereLibrary();

        }

        @Override
        public IStatus run(IProgressMonitor monitor) {

            IStatus _status = Status.OK_STATUS;

            String currentLibrary = null;
            try {
                currentLibrary = ISphereHelper.getCurrentLibrary(_as400);
            } catch (Exception e) {
            }

            if (currentLibrary != null) {

                boolean ok = false;
                try {
                    ok = ISphereHelper.setCurrentLibrary(_as400, iSphereLibrary);
                } catch (Exception e1) {
                }

                if (ok) {

                    _handle = new FNDSTR_getHandle().run(_as400);

                    if (_handle > 0) {

                        SearchElement.setSearchElements(_jdbcConnection, _handle, _searchElements);

                        new FNDSTR_resolveGenericSearchElements().run(_as400, _handle);

                        int _numberOfSearchElements = new FNDSTR_getNumberOfSearchElements().run(_as400, _handle);

                        monitor.beginTask("Searching", _numberOfSearchElements); //$NON-NLS-1$

                        new DoSearch(_as400, _handle, _searchOptions, monitor).start();

                        int _lastCounter = 0;

                        getStatus();

                        boolean _cancel = false;

                        while (_counter != -1) {

                            monitor.worked(_counter - _lastCounter);

                            _lastCounter = _counter;

                            if (monitor.isCanceled()) {
                                _cancel = true;
                                cancelJob();
                                _status = Status.CANCEL_STATUS;
                                break;
                            }

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            getStatus();

                        }

                        monitor.worked(_numberOfSearchElements - _lastCounter);

                        monitor.done();

                        if (!_cancel) {
                            _searchResults = getSearchResults(_jdbcConnection, _handle);
                        }

                        new FNDSTR_clear().run(_as400, _handle);

                    }

                    try {
                        ISphereHelper.setCurrentLibrary(_as400, currentLibrary);
                    } catch (Exception e) {
                    }

                }

            }

            if (_searchPostRun != null) {
                _searchPostRun.run(_searchResults);
            }

            return _status;

        }

        private SearchResult[] getSearchResults(Connection jdbcConnection, int handle) {

            String _separator;
            try {
                _separator = jdbcConnection.getMetaData().getCatalogSeparator();
            } catch (SQLException e) {
                _separator = "."; //$NON-NLS-1$
                e.printStackTrace();
            }

            ArrayList<SearchResult> arrayListSearchResults = new ArrayList<SearchResult>();

            PreparedStatement preparedStatementSelect = null;
            ResultSet resultSet = null;

            try {

                preparedStatementSelect = jdbcConnection.prepareStatement("SELECT * FROM " + ISpherePlugin.getISphereLibrary() + _separator //$NON-NLS-1$
                    + "FNDSTRO WHERE XOHDL = ? ORDER BY XOHDL, XOLIB, XOFILE, XOMBR", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //$NON-NLS-1$
                preparedStatementSelect.setString(1, Integer.toString(handle));
                resultSet = preparedStatementSelect.executeQuery();

                String _library = ""; //$NON-NLS-1$
                String _file = ""; //$NON-NLS-1$
                String _member = ""; //$NON-NLS-1$

                String library;
                String file;
                String member;

                SearchResult _searchResult = null;
                ArrayList<SearchResultStatement> alStatements = null;

                while (resultSet.next()) {

                    library = resultSet.getString("XOLIB").trim(); //$NON-NLS-1$
                    file = resultSet.getString("XOFILE").trim(); //$NON-NLS-1$
                    member = resultSet.getString("XOMBR").trim(); //$NON-NLS-1$

                    if (!_library.equals(library) || !_file.equals(file) || !_member.equals(member)) {

                        if (_searchResult != null) {

                            SearchResultStatement[] _statements = new SearchResultStatement[alStatements.size()];
                            alStatements.toArray(_statements);

                            _searchResult.setStatements(_statements);

                            arrayListSearchResults.add(_searchResult);

                        }

                        _library = library;
                        _file = file;
                        _member = member;

                        _searchResult = new SearchResult();
                        _searchResult.setLibrary(library);
                        _searchResult.setFile(file);
                        _searchResult.setMember(member);

                        alStatements = new ArrayList<SearchResultStatement>();

                    }

                    SearchResultStatement statement = new SearchResultStatement();
                    statement.setStatement(resultSet.getInt("XOSTMT")); //$NON-NLS-1$
                    statement.setLine(resultSet.getString("XOLINE")); //$NON-NLS-1$
                    alStatements.add(statement);

                }

                if (_searchResult != null) {

                    SearchResultStatement[] _statements = new SearchResultStatement[alStatements.size()];
                    alStatements.toArray(_statements);

                    _searchResult.setStatements(_statements);

                    arrayListSearchResults.add(_searchResult);

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (preparedStatementSelect != null) {
                try {
                    preparedStatementSelect.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            SearchResult[] _searchResults = new SearchResult[arrayListSearchResults.size()];
            arrayListSearchResults.toArray(_searchResults);
            return _searchResults;

        }

        private void getStatus() {

            String _separator;
            try {
                _separator = _jdbcConnection.getMetaData().getCatalogSeparator();
            } catch (SQLException e) {
                _separator = "."; //$NON-NLS-1$
                e.printStackTrace();
            }

            PreparedStatement preparedStatementSelect = null;
            ResultSet resultSet = null;
            try {
                preparedStatementSelect = _jdbcConnection.prepareStatement("SELECT XSCNT, XSCNL FROM " + iSphereLibrary + _separator //$NON-NLS-1$
                    + "FNDSTRS WHERE XSHDL = ?"); //$NON-NLS-1$
                preparedStatementSelect.setInt(1, _handle);
                resultSet = preparedStatementSelect.executeQuery();
                if (resultSet.next()) {
                    _counter = resultSet.getInt("XSCNT"); //$NON-NLS-1$
                }
            } catch (SQLException e) {
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e1) {
                }
            }
            if (preparedStatementSelect != null) {
                try {
                    preparedStatementSelect.close();
                } catch (SQLException e1) {
                }
            }

        }

        private void cancelJob() {

            String _separator;
            try {
                _separator = _jdbcConnection.getMetaData().getCatalogSeparator();
            } catch (SQLException e) {
                _separator = "."; //$NON-NLS-1$
                e.printStackTrace();
            }

            PreparedStatement preparedStatementUpdate = null;
            try {
                preparedStatementUpdate = _jdbcConnection.prepareStatement("UPDATE " + iSphereLibrary + _separator //$NON-NLS-1$ 
                    + "FNDSTRS SET XSCNL = '*YES' WHERE XSHDL = ?"); //$NON-NLS-1$
                preparedStatementUpdate.setInt(1, _handle);
                preparedStatementUpdate.executeUpdate();
            } catch (SQLException e) {
            }
            if (preparedStatementUpdate != null) {
                try {
                    preparedStatementUpdate.close();
                } catch (SQLException e1) {
                }
            }

        }

    }

    private class DoSearch extends Thread {

        private AS400 _as400;
        private int _handle;
        private SearchOptions _searchOptions;
        private IProgressMonitor _monitor;

        public DoSearch(AS400 _as400, int _handle, SearchOptions _searchOptions, IProgressMonitor _monitor) {
            this._as400 = _as400;
            this._handle = _handle;
            this._searchOptions = _searchOptions;
            this._monitor = _monitor;
        }

        @Override
        public void run() {
            if (new FNDSTR_search().run(_as400, _handle, _searchOptions) < 0) {
                _monitor.setCanceled(true);
            }
        }

    }

    private SearchResult[] _searchResults = null;

    // This method will be used by CMOne
    public SearchResult[] executeJoin(AS400 _as400, Connection _jdbcConnection, SearchOptions _searchOptions, ArrayList<SearchElement> _searchElements) {

        Search search = new Search(_as400, _jdbcConnection, _searchOptions, _searchElements, null);
        search.setUser(true);
        search.schedule();

        try {
            search.join();
        } catch (InterruptedException e) {
        }

        if (_searchResults == null) {
            return new SearchResult[0];
        } else {
            return _searchResults;
        }

    }

    public void execute(AS400 _as400, Connection _jdbcConnection, SearchOptions _searchOptions, ArrayList<SearchElement> _searchElements,
        ISearchPostRun _searchPostRun) {

        Search search = new Search(_as400, _jdbcConnection, _searchOptions, _searchElements, _searchPostRun);
        search.setUser(true);
        search.schedule();

    }

}
