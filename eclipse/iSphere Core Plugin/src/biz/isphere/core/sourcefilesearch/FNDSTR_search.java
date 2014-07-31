/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.sourcefilesearch;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.search.SearchArgument;
import biz.isphere.core.search.SearchOptions;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;

public class FNDSTR_search {

    public int run(AS400 _as400, int _handle, SearchOptions _searchOptions) {

        int errno = 0;

        try {

            // Debug options:
            // Trace.setTraceOn(true); // Turn on tracing function.
            // Trace.setTracePCMLOn(true); // Turn on PCML tracing.

            ProgramCallDocument pcml = new ProgramCallDocument(_as400, "biz.isphere.core.sourcefilesearch.FNDSTR_search", this.getClass()
                .getClassLoader());

            pcml.setIntValue("FNDSTR_search.handle", _handle);

            int[] indices = new int[1];
            pcml.setIntValue("FNDSTR_search.size", _searchOptions.getSearchArguments().size());

            for (indices[0] = 0; indices[0] < _searchOptions.getSearchArguments().size(); indices[0]++) {
                SearchArgument searchArgument = _searchOptions.getSearchArguments().get(indices[0]);
                pcml.setIntValue("FNDSTR_search.arguments.operator", indices, searchArgument.getOperator());
                pcml.setValue("FNDSTR_search.arguments.string.length", indices, searchArgument.getString().length());
                pcml.setValue("FNDSTR_search.arguments.string.value", indices, searchArgument.getString());
                pcml.setIntValue("FNDSTR_search.arguments.fromColumn", indices, searchArgument.getFromColumn());
                pcml.setIntValue("FNDSTR_search.arguments.toColumn", indices, searchArgument.getToColumn());
                pcml.setValue("FNDSTR_search.arguments.case", indices, searchArgument.getCaseSensitive());
            }

            if (_searchOptions.isShowAllItems()) {
                pcml.setValue("FNDSTR_search.showRecords", "1");
            } else {
                pcml.setValue("FNDSTR_search.showRecords", "0");
            }

            if (_searchOptions.isMatchAll()) {
                pcml.setValue("FNDSTR_search.matchAll", "1");
            } else {
                pcml.setValue("FNDSTR_search.matchAll", "0");
            }

            boolean rc = pcml.callProgram("FNDSTR_search");
            if (rc == false) {

                AS400Message[] msgs = pcml.getMessageList("FNDSTR_search");
                for (int idx = 0; idx < msgs.length; idx++) {
                    System.out.println(msgs[idx].getID() + " - " + msgs[idx].getText());
                }
                System.out.println("*** Call to FNDSTR_search failed. See messages above ***");

                errno = -1;

            } else {

                errno = 1;

            }

        } catch (PcmlException e) {

            errno = -1;

            ISpherePlugin.logError("Error on source file search: " + e.getLocalizedMessage(), e);
        }

        return errno;

    }

}