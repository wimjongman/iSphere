/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import biz.isphere.base.internal.StringHelper;
import biz.isphere.joblogexplorer.exceptions.InvalidJobLogFormatException;

public class JobLogParser {

    private static final int NUMBER_OF_LINES_SCANNED_FOR_FIRST_LINE_OF_JOIB_LOG = 3;
    private static final int IDLE = 1;
    private static final int PARSE_PAGE_HEADER = 2;
    private static final int PARSE_MESSAGE = 3;

    private JobLogParserConfiguration configuration;
    private int mode;
    private int headerCount;

    private JobLog jobLog;
    private JobLogPage jobLogPage;
    private JobLogMessage jobLogMessage;

    private String messageIndent;
    private List<String> messageAttributes;
    private int lastMessageAttribute;

    /**
     * Constructs a new JobLogParser object.
     */
    public JobLogParser() {

        this.configuration = new JobLogParserConfiguration();
        this.configuration.loadConfiguration(Locale.getDefault().getLanguage());
    }

    /**
     * This method loads a given job log from a plain-text stream file.
     * 
     * @param pathName - Name of the file.
     * @return the job log
     * @throws InvalidJobLogFormatException
     */
    public JobLog loadFromStmf(String pathName) throws InvalidJobLogFormatException {

        BufferedReader br = null;
        jobLog = new JobLog();
        messageIndent = null;
        messageAttributes = new LinkedList<String>();
        lastMessageAttribute = -1;

        int errCount = NUMBER_OF_LINES_SCANNED_FOR_FIRST_LINE_OF_JOIB_LOG;

        try {

            String line;

            br = new BufferedReader(new FileReader(pathName));

            mode = IDLE;
            while ((line = br.readLine()) != null && errCount > 0) {

                mode = checkForStartOfPage(line);
                if (mode == IDLE) {
                    errCount--;
                    continue;
                }

                switch (mode) {
                case PARSE_PAGE_HEADER:
                    mode = parsePageHeader(line);
                    break;

                case PARSE_MESSAGE:
                    mode = parseMessage(line);
                    break;

                default:
                    break;
                }

            }

            if (mode == IDLE) {
                throw new InvalidJobLogFormatException();
            }

            if (jobLogMessage != null && messageAttributes.size() > 0) {
                updateMessageAttributes(jobLogMessage, messageAttributes);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return jobLog;
    }

    /**
     * This method check the current line for the page number to start a new
     * page.
     * <p>
     * <code><ul>
     * <li>.5770SS1.V7R2M0.140418........................Display.Job.Log.......................GFD400....03.11.16..14:58:40.CET.....Page....1</li>
     * </ul></code>
     * 
     * @param line - current line of the job log.
     * @return new parser mode
     */
    private int checkForStartOfPage(String line) {

        if (line.length() <= 116) {
            return mode;
        }

        Matcher matcher = configuration.getStartOfPage().matcher(line);
        while (matcher.find()) {
            jobLogPage = jobLog.addPage();
            jobLogPage.setPageNumber(new Integer(matcher.group(8).trim()).intValue());
            jobLog.setSystemName(matcher.group(3).trim());
            if (!jobLog.isHeaderComplete()) {
                return PARSE_PAGE_HEADER;
            } else {
                return mode;
            }
        }

        return mode;
    }

    /**
     * This method parses the header data that are at the top of each page of
     * the job log.
     * <p>
     * <code><ul>
     * <li>From module . . . . . . . . : LIBL#LOAD</li>
     * <li>From procedure . . . . . . : LIBL#LOAD</li>
     * <li>Statement . . . . . . . . . : 5500</li>
     * <li>To module . . . . . . . . . : START#RZ</li>
     * <li>To procedure . . . . . . . : START#RZ</li>
     * <li>Statement . . . . . . . . . : 5300</li>
     * <li>Message . . . . : ISPHEREDVP PY27V5R4 ...</li>
     * <li>Cause . . . . . : Es ist kein zus�tzlicher ...</li> *
     * </ul></code>
     * 
     * @param line - current line of the job log.
     * @return new parser mode
     */
    private int parsePageHeader(String line) {

        if (jobLog.isHeaderComplete()) {
            return mode;
        }

        Matcher matcher = configuration.getPageHeader().matcher(line);
        while (matcher.find()) {
            headerCount++;
            switch (headerCount) {
            case 1:
                jobLog.setJobName(matcher.group(2));
                break;

            case 2:
                jobLog.setJobUserName(matcher.group(2));
                break;

            case 3:
                jobLog.setJobNumber(matcher.group(2));
                break;

            case 4:
                jobLog.setJobDescriptionName(matcher.group(2));
                break;

            case 5:
                jobLog.setJobDescriptionLibraryName(matcher.group(2));
                break;

            default:
                break;
            }
        }

        if (!jobLog.isHeaderComplete()) {
            return mode;
        }

        return PARSE_MESSAGE;
    }

    /**
     * This method parses a given line of the job log in order to:
     * <ul>
     * <li>Start a new message.</li>
     * <li>Collect the attributes of the current message.</li>
     * </ul>
     * 
     * @param line - current line of the job log.
     * @return new parser mode
     */
    private int parseMessage(String line) {

        Matcher matcher;

        if (line.trim().length() == 0) {
            return mode;
        }

        // Scan for the first line of the message
        matcher = configuration.getStartOfMessage().matcher(line);
        while (matcher.find()) {
            updateMessageAttributes(jobLogMessage, messageAttributes);
            jobLogMessage = jobLog.addMessage();
            jobLogMessage.setId(matcher.group(1));
            jobLogMessage.setType(matcher.group(2));
            if (matcher.group(3) != null) {
                jobLogMessage.setSeverity(matcher.group(3));
            } else {
                jobLogMessage.setSeverity(null);
            }
            jobLogMessage.setDate(matcher.group(4));
            jobLogMessage.setTime(matcher.group(5));
            jobLogMessage.setFromProgram(matcher.group(6));
            jobLogMessage.setFromLibrary(matcher.group(7));
            jobLogMessage.setFromStatement(matcher.group(8));
            jobLogMessage.setToProgram(matcher.group(9));
            jobLogMessage.setToLibrary(matcher.group(10));
            jobLogMessage.setToStatement(matcher.group(11));

            messageIndent = null;
            messageAttributes.clear();
            lastMessageAttribute = -1;

            return mode;
        }

        if (jobLogMessage == null) {
            return mode;
        }

        // Scan for message attributes such as:
        // From module
        // From procedure
        // From statement
        // ...
        matcher = configuration.getMessageAttribute().matcher(line);
        while (matcher.find()) {
            if (messageAttributes.size() == 0) {
                messageIndent = getMessageContinuationIndention(line);
            }
            messageAttributes.add(matcher.group(2));
            lastMessageAttribute = messageAttributes.size() - 1;
        }

        // Check line for message continuation
        if (messageIndent != null && line.startsWith(messageIndent) && lastMessageAttribute >= 0) {
            String attributeValue = concatenate(messageAttributes.get(lastMessageAttribute), line.substring(messageIndent.length()));
            messageAttributes.set(lastMessageAttribute, attributeValue);
        }

        return mode;
    }

    /**
     * Concatenates the given string and inserts a SPACE, when the first string
     * does not end with a special character.
     * 
     * @param string1 - First part of the string.
     * @param string2 - Second part of the string.
     * @return concatenated string
     */
    private String concatenate(String string1, String string2) {

        if (string1.length() == 0) {
            return string2;
        }

        // String lastChar = string1.substring(string1.length()-1,
        // string1.length());
        // if ("-_".indexOf(lastChar) >= 0) {
        // return string1 + string2;
        // }

        return string1 + " " + string2; //$NON-NLS-1$
    }

    /**
     * This method updates the attributes of the current message. It is
     * triggered when the parser detects the beginning of the next message.
     * 
     * @param jobLogMessage - current message
     * @param messageAttributes - attributes of the current message
     */
    private void updateMessageAttributes(JobLogMessage jobLogMessage, List<String> messageAttributes) {

        if (jobLogMessage == null && messageAttributes.size() > 0) {
            throw new RuntimeException("Parameter 'jobLogMessage' must not be null!"); //$NON-NLS-1$
        }

        if (messageAttributes.size() >= 7) {
            jobLogMessage.setFromModule(messageAttributes.get(0));
            jobLogMessage.setFromProcedure(messageAttributes.get(1));
            jobLogMessage.setFromStatement(messageAttributes.get(2));
            jobLogMessage.setToModule(messageAttributes.get(3));
            jobLogMessage.setToProcedure(messageAttributes.get(4));
            jobLogMessage.setToStatement(messageAttributes.get(5));
            jobLogMessage.setText(messageAttributes.get(6));
            if (messageAttributes.size() >= 8) {
                jobLogMessage.setHelp(messageAttributes.get(7));
            }
            return;
        }

        if (messageAttributes.size() >= 4) {
            jobLogMessage.setToModule(messageAttributes.get(0));
            jobLogMessage.setToProcedure(messageAttributes.get(1));
            jobLogMessage.setToStatement(messageAttributes.get(2));
            jobLogMessage.setText(messageAttributes.get(3));
            if (messageAttributes.size() >= 5) {
                jobLogMessage.setHelp(messageAttributes.get(4));
            }
            return;
        }

        if (messageAttributes.size() >= 1) {
            jobLogMessage.setText(messageAttributes.get(0));
            if (messageAttributes.size() >= 2) {
                jobLogMessage.setHelp(messageAttributes.get(1));
            }
            return;
        }
    }

    /**
     * Returns the indention string of message continuation lines.
     * 
     * @param string - line of the current message attribute
     * @return indention string
     */
    private String getMessageContinuationIndention(String string) {

        int count = string.length() - StringHelper.trimL(string).length();

        return StringHelper.getFixLength("", count + 2); //$NON-NLS-1$
    }

    /**
     * This method is used for testing purposes.
     * <p>
     * It parses the specified job log and prints the result.
     * 
     * @param args - none (not used)
     */
    public static void main(String[] args) throws Exception {

        String directory = "C:/workspaces/rdp_080/workspace/iSphere Job Log Explorer/temp/"; //$NON-NLS-1$

        JobLogParser main = new JobLogParser();
        // main.importFromStmf(directory +
        // "iSphere Joblog - English_GFD400.txt");
        // JobLog jobLog = main.loadFromStmf(directory + "QPJOBLOG_2_712703_RADDATZ_TRADDATZA1_GFD400.txt"); //$NON-NLS-1$
        // JobLog jobLog = main.loadFromStmf(directory +
        // "QPJOBLOG_440_712206_CMONE_FR_D0008UJ_GFD400.txt");
        // JobLog jobLog = main.loadFromStmf(directory + "Test Single Message.txt"); //$NON-NLS-1$
        // main.loadFromStmf(directory +
        // "iSphere_Spooled_File_QPJOBLOG_2_TRADDATZB1_RADDATZ_246474_WWSOBIDE_1160827_202522.txt");
        // main.loadFromStmf(directory +
        // "QPJOBLOG_440_712206_CMONE_FR_D0008UJ_GFD400.txt");
        // JobLog jobLog = main.loadFromStmf(directory + "QPJOBLOG.txt"); //$NON-NLS-1$
        JobLog jobLog = main.loadFromStmf(directory + "QPJOBLOG_Julian_Dates.txt"); //$NON-NLS-1$

        jobLog.dump();

    }

}
