/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.internal.api.debugger.viewtext;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ProgramParameter;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.internal.api.APIErrorCode;
import biz.isphere.core.internal.api.APIProgramCallDocument;

public class IQSDRTVVT extends APIProgramCallDocument {

    public static final String SDVT0100 = "SDVT0100";
    public static final int ALL_LINES = 0;

    public IQSDRTVVT(AS400 system) {
        super(system, "IQSDRTVVT", "ISPHEREDVP");
    }

    public IQSDRTVVT(AS400 system, String connectionName) {
        super(system, "IQSDRTVVT", IBMiHostContributionsHandler.getISphereLibrary(connectionName));
    }

    public boolean execute(IQSDRTVVTResult iqsdrtvvtResult, int viewId, int startLine, int numLines, int lineLength) {

        try {

            if (!execute(createParameterList(iqsdrtvvtResult, viewId, startLine, numLines, lineLength))) {
                return false;
            }

            iqsdrtvvtResult.setBytes(getParameterList()[0].getOutputData());

            return true;

        } catch (Throwable e) {
            ISpherePlugin.logError(e.getLocalizedMessage(), e);
            return false;
        }
    }

    protected ProgramParameter[] createParameterList(IQSDRTVVTResult iqsdrtvvtResult, int viewId, int startLine, int numLines, int lineLength)
        throws Exception {

        ProgramParameter[] parameterList = new ProgramParameter[7];
        parameterList[0] = new ProgramParameter(iqsdrtvvtResult.getBytes().length); // io_rcvVar
        parameterList[1] = produceIntegerParameter(iqsdrtvvtResult.getBytes().length); // i_lenRcvVar
        parameterList[2] = produceIntegerParameter(viewId); // i_viewId
        parameterList[3] = produceIntegerParameter(startLine); // i_startLine
        parameterList[4] = produceIntegerParameter(numLines); // i_numLines
        parameterList[5] = produceIntegerParameter(lineLength); // i_lineLength
        parameterList[6] = produceByteParameter(new APIErrorCode().getBytes()); // io_errCode

        return parameterList;
    }
}