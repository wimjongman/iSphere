/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.internal.api.retrievemessagedescription;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.api.APIErrorCode;
import biz.isphere.core.internal.api.APIProgramCallDocument;
import biz.isphere.core.messagefileeditor.MessageDescription;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.ProgramParameter;

/**
 * This class retrieves the messages descriptions of a given message file.
 * 
 * @author Thomas Raddatz
 */
public class IQMHRTVM extends APIProgramCallDocument {

    public static final String RETRIEVE_FIRST = "*FIRST";
    public static final String RETRIEVE_NEXT = "*NEXT";
    public static final String RETRIEVE_MSGID = "*MSGID";

    private String messageFile;
    private String library;
    private String connectionName;

    /**
     * Constructs a IQMHRTVM object for a system and connection.
     * 
     * @param system - System that hosts the message file.
     * @param connectionName - Name of the RDi connection.
     * @throws PropertyVetoException
     */
    public IQMHRTVM(AS400 system, String connectionName) throws PropertyVetoException {
        super(system, "IQMHRTVM", "ISPHEREDVP");

        this.connectionName = connectionName;
    }

    /**
     * Sets the name of the message file and library.
     * 
     * @param name - name of the message file
     * @param library - name of the message file library
     */
    public void setMessageFile(String name, String library) {

        this.messageFile = name;
        this.library = library;

    }

    /**
     * Retrieves all message descriptions of the message file.
     * 
     * @return array of message descriptions
     */
    public MessageDescription[] retrieveAllMessageDescriptions() {

        List<MessageDescription> messages = new ArrayList<MessageDescription>();

        try {

            // long startTime = System.currentTimeMillis();
            // long numCalls = 0;

            int bufferSize = 1024 * 1024 * 1 / 2;
            IQMHRTVMResult result = retrieveMessageDescriptions(-1, bufferSize);

            while (result != null && result.getBytesAvailable() > 0 && result.getNumberOfMessagesReturned() > 0) {
                // numCalls++;
                messages.addAll(result.getMessages());
                result = retrieveMessageDescriptions(IQMHRTVM.RETRIEVE_NEXT, result.getLastMessageIdReturned(), -1, bufferSize);
            }

            // System.out.println("mSecs for " + messageFile +
            // " and buffer size " + (bufferSize / 1024) + "KB: "
            // + (System.currentTimeMillis() - startTime));
            // System.out.println("Loaded " + messages.size() +
            // " messages. Called the API " + numCalls + " times.");

        } catch (Exception e) {

            // System.out.println(e.getLocalizedMessage());
            // e.printStackTrace();
            // System.out.println("*** Call to QMHRTVM failed. ***");
            // return null;
            ISpherePlugin.logError("Fails to call the iSphere IQMHRTVM API", e);

        }

        return messages.toArray(new MessageDescription[messages.size()]);
    }

    /**
     * Retrieves a given number of message descriptions, starting at the first
     * description.
     * 
     * @param numMessages - number of messages to retrieve or -1 for all
     *        messages. The method may return less messages than specified, if
     *        the buffer is not large enough.
     * @param bufferSize - buffer to return the message descriptions
     * @return result of the retrieve operation
     * @throws Exception
     */
    public IQMHRTVMResult retrieveMessageDescriptions(int numMessages, int bufferSize) throws Exception {
        return retrieveMessageDescriptions(IQMHRTVM.RETRIEVE_FIRST, "", numMessages, bufferSize);
    }

    /**
     * Retrieves a given number of message descriptions, starting at a specified
     * message ID description.
     * 
     * @param retrieveOption - specifies the starting point. Must be one of
     *        {@link #RETRIEVE_FIRST}, {@link #RETRIEVE_NEXT} or
     *        {@link #RETRIEVE_MSGID    }.
     * @param messageID - message that defines the starting point. This
     *        parameter is ignored when <i>retrieveOption</i> is set to
     *        {@link #RETRIEVE_FIRST}.
     * @param numMessages - number of messages to retrieve or -1 for all
     *        messages. The method may return less messages than specified, if
     *        the buffer is not large enough.
     * @param bufferSize - buffer to return the message descriptions
     * @return result of the retrieve operation
     * @throws Exception
     */
    public IQMHRTVMResult retrieveMessageDescriptions(String retrieveOption, String messageID, int numMessages, int bufferSize) throws Exception {

        if (execute(createParameterList(retrieveOption, messageID, numMessages, bufferSize))) {
            return new IQMHRTVMResult(getSystem(), connectionName, messageFile, library, getParameterList()[0].getOutputData());
        }

        AS400Message[] msgList = getMessageList();
        for (int j = 0; j < msgList.length; j++) {
            System.out.println(msgList[j].getID() + " - " + msgList[j].getText()); //$NON-NLS-1$
        }

        return null;
    }

    /**
     * Produces the parameter list for calling the IQMHRTVM API.
     * 
     * @param retrieveOption - specifies the starting point. Must be one of
     *        {@link #RETRIEVE_FIRST}, {@link #RETRIEVE_NEXT} or
     *        {@link #RETRIEVE_MSGID    }.
     * @param messageID - message that defines the starting point. This
     *        parameter is ignored when <i>retrieveOption</i> is set to
     *        {@link #RETRIEVE_FIRST}.
     * @param numMessages - number of messages to retrieve or -1 for all
     *        messages. The method may return less messages than specified, if
     *        the buffer is not large enough.
     * @param bufferSize - buffer to return the message descriptions
     * @return parameter list
     * @throws Exception
     */
    protected ProgramParameter[] createParameterList(String retrieveOption, String messageID, int numMessages, int bufferSize) throws Exception {

        ProgramParameter[] parameterList = new ProgramParameter[9];
        parameterList[0] = new ProgramParameter(bufferSize); // Receiver
        parameterList[1] = produceIntegerParameter(bufferSize); // Length
        parameterList[2] = produceStringParameter("RTVM0300", 8); // Format
        parameterList[3] = produceStringParameter(messageID, 7); // MsgID
        parameterList[4] = produceQualifiedObjectName(messageFile, library); // Object
        parameterList[5] = produceStringParameter("*YES", 10); // Rtn Fmt Ctrl
        parameterList[6] = produceByteParameter(new APIErrorCode().getBytes());
        parameterList[7] = produceStringParameter(retrieveOption, 10);
        parameterList[8] = produceIntegerParameter(numMessages);

        return parameterList;
    }
}
