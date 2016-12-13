/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.model;

import java.io.UnsupportedEncodingException;

import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.joblogexplorer.api.listjoblog.JobLogListener;
import biz.isphere.joblogexplorer.api.listjoblog.MessageSelectionInformation;
import biz.isphere.joblogexplorer.api.listjoblog.OLJL0100;
import biz.isphere.joblogexplorer.api.listjoblog.QGYCLST;
import biz.isphere.joblogexplorer.api.listjoblog.QGYGTLE;
import biz.isphere.joblogexplorer.api.listjoblog.QGYOLJBL;
import biz.isphere.joblogexplorer.api.retrievejobinformation.JOBI0400;
import biz.isphere.joblogexplorer.api.retrievejobinformation.QUSRJOBI;
import biz.isphere.joblogexplorer.api.retrievenetworkattributes.QWCRNETA;
import biz.isphere.joblogexplorer.exceptions.JobLogNotLoadedException;
import biz.isphere.joblogexplorer.exceptions.JobNotFoundException;

import com.ibm.as400.access.AS400;

public class JobLogReader implements JobLogListener {

    private static final String JOB_NOT_FOUND_MSGID = "CPF3C53"; //$NON-NLS-1$

    private JobLog jobLog;

    /**
     * This method retrieves the job log of a given server job.
     * 
     * @param connectionName - Name of the RSE connection.
     * @param jobName - Job name.
     * @param jobUser - Job user name.
     * @param jobNumber - Job number.
     * @return the job log
     * @throws JobNotFoundException
     * @throws JobLogNotLoadedException
     */
    public JobLog loadFromJob(String connectionName, String jobName, String jobUser, String jobNumber) throws JobNotFoundException,
        JobLogNotLoadedException {

        AS400 as400 = IBMiHostContributionsHandler.getSystem(connectionName);
        return loadFromJob(as400, jobName, jobUser, jobNumber);
    }

    /**
     * This method retrieves the job log of a given server job.
     * 
     * @param as400 - IBM i system object.
     * @param jobName - Job name.
     * @param jobUser - Job user name.
     * @param jobNumber - Job number.
     * @return the job log
     * @throws JobLogNotLoadedException
     */
    public JobLog loadFromJob(AS400 as400, String jobName, String jobUser, String jobNumber) throws JobNotFoundException, JobLogNotLoadedException {

        jobLog = new JobLog();
        jobLog.setSystemName(as400.getSystemName());
        jobLog.setJobName(jobName);
        jobLog.setJobUserName(jobUser);
        jobLog.setJobNumber(jobNumber);

        JOBI0400 jobi0400 = new JOBI0400(as400);
        QUSRJOBI qusrjobi = new QUSRJOBI(as400);
        qusrjobi.setJob(jobLog.getJobName(), jobLog.getJobUserName(), jobLog.getJobNumber());

        QWCRNETA qwcrneta = new QWCRNETA(as400);

        QGYOLJBL qgyljbl = new QGYOLJBL(as400);
        qgyljbl.addKey(MessageSelectionInformation.KEY_MESSAGE_WITH_REPLACEMENT_DATA);
        qgyljbl.addKey(MessageSelectionInformation.KEY_MESSAGE_HELP_WITH_REPLACEMENT_DATA);
        qgyljbl.addKey(MessageSelectionInformation.KEY_RECEIVING_PROGRAM_LIBRARY);
        qgyljbl.addKey(MessageSelectionInformation.KEY_RECEIVING_PROGRAM_NAME);
        qgyljbl.addKey(MessageSelectionInformation.KEY_RECEIVING_MODULE_NAME);
        qgyljbl.addKey(MessageSelectionInformation.KEY_RECEIVING_PROCEDURE_NAME);
        qgyljbl.addKey(MessageSelectionInformation.KEY_RECEIVING_STATEMENT_NUMBER);
        qgyljbl.addKey(MessageSelectionInformation.KEY_SENDING_PROGRAM_LIBRARY);
        qgyljbl.addKey(MessageSelectionInformation.KEY_SENDING_PROGRAM_NAME);
        qgyljbl.addKey(MessageSelectionInformation.KEY_SENDING_MODULE_NAME);
        qgyljbl.addKey(MessageSelectionInformation.KEY_SENDING_PROCEDURE_NAME);
        qgyljbl.addKey(MessageSelectionInformation.KEY_SENDING_STATEMENT_NUMBER);
        qgyljbl.setJob(jobLog.getJobName(), jobLog.getJobUserName(), jobLog.getJobNumber());
        qgyljbl.addListener(this);

        String requestHandle = null;

        try {

            boolean rc;

            rc = qusrjobi.execute(jobi0400);
            if (!rc) {
                String errorID = qusrjobi.getErrorMessageID();
                if (JOB_NOT_FOUND_MSGID.equals(errorID)) {
                    // Job not found
                    throw new JobNotFoundException(qusrjobi.getErrorMessage());
                }
                throw new JobLogNotLoadedException(qusrjobi.getErrorMessage());
            }

            jobLog.setJobDescriptionName(jobi0400.getJobDescriptionName());
            jobLog.setJobDescriptionLibraryName(jobi0400.getJobDescriptionLibraryName());

            rc = qwcrneta.execute(QWCRNETA.Key.SYSNAME);
            if (rc) {
                jobLog.setSystemName(qwcrneta.getCharValue());
            } else {
                jobLog.setSystemName("*ERROR");//$NON-NLS-1$
            }

            rc = qgyljbl.execute(4096);
            if (!rc) {
                String errorID = qusrjobi.getErrorMessageID();
                if (JOB_NOT_FOUND_MSGID.equals(errorID)) {
                    // Job not found
                    throw new JobNotFoundException(qgyljbl.getErrorMessage());
                }
                throw new JobLogNotLoadedException(qgyljbl.getErrorMessage());
            }

            requestHandle = qgyljbl.getRequestHandle();

            boolean isPending = qgyljbl.isPending();
            int totalNumberOfRecords = qgyljbl.getTotalNumberOfRecords();

            QGYGTLE qgygtle = null;
            while (rc && (isPending || jobLog.getMessages().size() < totalNumberOfRecords)) {

                if (jobLog.getFirstPage() == null) {
                    jobLog.addPage();
                }

                if (qgygtle == null) {
                    qgygtle = new QGYGTLE(as400, requestHandle);
                    qgygtle.addListener(this);
                }

                rc = qgygtle.execute(jobLog.getMessages().size() + 1);
                if (!rc) {
                    break;
                }

                isPending = qgygtle.isPending();
                totalNumberOfRecords = qgygtle.getTotalNumberOfRecords();
            }

        } finally {

            if (requestHandle != null) {
                QGYCLST qgyclst = new QGYCLST(as400, requestHandle);
                qgyclst.execute();
            }

        }

        return jobLog;
    }

    public void addNewEntry(OLJL0100 oljl0100) {

        try {

            String type = oljl0100.getMessageType();

            JobLogMessage message = jobLog.addMessage();

            message.setType(type);

            if ("Request".equals(type)) {
                message.setId("*NONE");
                message.setSeverity("");
                message.setText("-" + oljl0100.getFieldData(MessageSelectionInformation.KEY_MESSAGE_WITH_REPLACEMENT_DATA));
                message.setHelp("");
            } else {
                message.setId(oljl0100.getMessageId());
                message.setSeverity(oljl0100.getMessageSeverity());
                message.setText(oljl0100.getFieldData(MessageSelectionInformation.KEY_MESSAGE_WITH_REPLACEMENT_DATA));
                message.setHelp(oljl0100.getFieldData(MessageSelectionInformation.KEY_MESSAGE_HELP_WITH_REPLACEMENT_DATA));
            }

            message.setDate(oljl0100.getDateSent());
            message.setTime(oljl0100.getTimeSent());

            message.setToLibrary(oljl0100.getFieldData(MessageSelectionInformation.KEY_RECEIVING_PROGRAM_LIBRARY));
            message.setToProgram(oljl0100.getFieldData(MessageSelectionInformation.KEY_RECEIVING_PROGRAM_NAME));
            message.setToModule(oljl0100.getFieldData(MessageSelectionInformation.KEY_RECEIVING_MODULE_NAME));
            message.setToProcedure(oljl0100.getFieldData(MessageSelectionInformation.KEY_RECEIVING_PROCEDURE_NAME));
            message.setToStatement(oljl0100.getFieldData(MessageSelectionInformation.KEY_RECEIVING_STATEMENT_NUMBER));

            message.setFromLibrary(oljl0100.getFieldData(MessageSelectionInformation.KEY_SENDING_PROGRAM_LIBRARY));
            message.setFromProgram(oljl0100.getFieldData(MessageSelectionInformation.KEY_SENDING_PROGRAM_NAME));
            message.setFromModule(oljl0100.getFieldData(MessageSelectionInformation.KEY_SENDING_MODULE_NAME));
            message.setFromProcedure(oljl0100.getFieldData(MessageSelectionInformation.KEY_SENDING_PROCEDURE_NAME));
            message.setFromStatement(oljl0100.getFieldData(MessageSelectionInformation.KEY_SENDING_STATEMENT_NUMBER));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is used for testing purposes.
     * <p>
     * It parses the specified job log and prints the result.
     * 
     * @param args - none (not used)
     */
    public static void main(String[] args) throws Exception {

        String hostname = System.getProperty("isphere.junit.as400");
        String user = System.getProperty("isphere.junit.username");
        String password = System.getProperty("isphere.junit.password");

        AS400 as400 = new AS400(hostname, user, password);

        JobLogReader main = new JobLogReader();
        JobLog jobLog = main.loadFromJob(as400, "TRADDATZA1", "RADDATZ", "791807");

        jobLog.dump();

    }

}
