/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.strpreprc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import biz.isphere.base.internal.IntHelper;
import biz.isphere.core.clcommands.CLCommand;
import biz.isphere.core.clcommands.CLFormatter;
import biz.isphere.core.clcommands.CLParameter;
import biz.isphere.core.clcommands.CLParser;
import biz.isphere.strpreprc.preferences.Preferences;

import com.ibm.lpex.core.LpexDocumentLocation;
import com.ibm.lpex.core.LpexView;

public class StrPrePrcParser extends AbstractStrPrePrcParser {

    private CLParser clParser;

    private static final Set<String> REPLACEMENT_VARIABLES = new HashSet<String>(Arrays.asList(new String[] { "&LI", "&OB", "&TY", "&SL", "&SF",
        "&SM", "&TL", "&TO", "&TR", "&FL", "&FF", "&FM", "&U0", "&U1", "&U2", "&U3", "&U4", "&U5", "&U6", "&U7", "&U8", "&U9" }));

    private String memberType;

    private int width;
    private int firstLine;
    private int lastLine;
    private String createCommand;
    private Map<String, CLParameter> parameters;
    private LinkedList<String> baseParameters;
    private LinkedList<String> importantParameters;
    private LinkedList<String> compileParameters;
    private LinkedList<String> linkParameters;
    private LinkedList<String> preCommands;
    private LinkedList<String> postCommands;
    private LinkedList<String> parameterSequence;

    private boolean createTemplates;

    public StrPrePrcParser(String memberType) {

        this.memberType = memberType;

        this.clParser = new CLParser();
        this.parameters = new HashMap<String, CLParameter>();
        this.baseParameters = new LinkedList<String>();
        this.importantParameters = new LinkedList<String>();
        this.compileParameters = new LinkedList<String>();
        this.linkParameters = new LinkedList<String>();
        this.preCommands = new LinkedList<String>();
        this.postCommands = new LinkedList<String>();
        this.parameterSequence = new LinkedList<String>();

        this.createTemplates = false;

        initializeStore();
    }

    public static boolean isVariable(String variable) {
        return REPLACEMENT_VARIABLES.contains(variable);
    }

    public String getCommand() {
        return createCommand;
    }

    public String getFullCommand() {

        if (createCommand == null) {
            return null;
        }

        CLCommand clCommand = new CLCommand(createCommand);
        clCommand.setParameters(new ArrayList<CLParameter>(parameters.values()));

        return clCommand.toString();
    }

    public void setFullCommand(String fullCommandString) {

        initializeStore();

        if (fullCommandString == null) {
            throw new IllegalArgumentException("Parameter 'commandString' must not be null.");
        }

        CLCommand clCommand = clParser.parseCommand(fullCommandString);
        if (clCommand == null) {
            return;
        }

        createCommand = clCommand.getCommand();

        for (CLParameter clParameter : clCommand.getParameters()) {
            if (isBaseKeyword(clParameter)) {
                setBaseParameter(clParameter);
            } else {
                String defaultSection = Preferences.getInstance().getDefaultSection();
                if (Preferences.IMPORTANT.equals(defaultSection)) {
                    setImportantParameter(clParameter);
                } else if (Preferences.COMPILE.equals(defaultSection)) {
                    setCompileParameter(clParameter);
                } else {
                    setLinkParameter(clParameter);
                }
            }
        }
    }

    public void updateFullCommand(String commandString) {

        CLCommand clCommand = clParser.parseCommand(commandString);
        if (clCommand == null) {
            return;
        }

        if (createCommand == null) {
            throw new IllegalArgumentException("Current value of 'createCommand' must not be null.");
        }

        if (!createCommand.equals(clCommand.getCommand())) {
            throw new IllegalArgumentException("Current value of 'createCommand' must match command of parameter 'commandString'.");
        }

        updateParameters(clCommand.getParameters());
    }

    public String[] getHeader(CLFormatter formatter) {

        try {
            createTemplates = true;
            produceParameterSequence(getFullCommand(), formatter);
            return produceHeader(null);
        } finally {
            createTemplates = false;
        }
    }

    public boolean loadFromLpexView(LpexView view) {

        int numElements = view.elements();
        if (numElements <= 0) {
            return false;
        }

        List<String> textLines = new ArrayList<String>();
        for (int i = 1; i <= numElements; i++) {
            textLines.add(view.elementText(i));
        }

        parseLines(textLines);

        if (firstLine > 0 && lastLine > 0) {
            return true;
        }

        return false;
    }

    public boolean loadTemplate(String[] lines) {

        parseLines(Arrays.asList(lines));

        if (firstLine > 0 && lastLine > 0) {
            return true;
        }

        return false;
    }

    public boolean loadDefaultTemplate() {

        String[] lines = HeaderTemplates.getInstance().getTemplate(memberType);

        parseLines(Arrays.asList(lines));

        if (firstLine > 0 && lastLine > 0) {
            firstLine = 0;
            lastLine = 0;
            return true;
        }

        firstLine = 0;
        lastLine = 0;

        return false;
    }

    public void updateLpexView(LpexView view, CLFormatter formatter) {

        produceParameterSequence(getFullCommand(), formatter);

        // Remove existing header
        removeFromLpexView(view);

        // Add new header
        String[] headerLines = produceHeader(formatter);

        int cursorLine;
        if (firstLine <= 0) {
            cursorLine = IntHelper.tryParseInt(view.query("element"), 0) - 1;
        } else {
            cursorLine = 0;
        }

        int insertLine = Math.max(this.firstLine - 1, 0) + cursorLine;
        for (String line : headerLines) {
            if (insertLine <= 0) {
                view.doCommand(new LpexDocumentLocation(1, 0), "add before 1");
                view.setElementText(1, line);
            } else {
                view.doCommand(new LpexDocumentLocation(insertLine, 0), "insert " + line);
            }
            insertLine++;
        }

        view.doCommand("locate element " + Math.max(firstLine, 1));
    }

    private void produceParameterSequence(String fullCommand, CLFormatter formatter) {

        parameterSequence.clear();
        if (formatter == null) {
            parameterSequence.addAll(baseParameters);
            parameterSequence.addAll(importantParameters);
            parameterSequence.addAll(compileParameters);
            parameterSequence.addAll(linkParameters);
        } else {
            String commandString = formatter.format(getFullCommand());
            CLCommand clCommand = clParser.parseCommand(commandString);
            for (CLParameter clParameter : clCommand.getParameters()) {
                parameterSequence.add(clParameter.getKeyword());
            }
        }
    }

    public void removeFromLpexView(LpexView view) {

        if (firstLine == 0) {
            return;
        }

        // Remove existing header
        view.doCommand(new LpexDocumentLocation(firstLine, 0), "delete " + (lastLine - firstLine + 1));
    }

    private void updateParameters(CLParameter[] clParameters) {

        removeDeletedParameters(clParameters);

        List<CLParameter> remainingParameters = new ArrayList<CLParameter>();
        remainingParameters.addAll(Arrays.asList(clParameters));

        for (CLParameter clParameter : clParameters) {
            String keyword = clParameter.getKeyword();
            if (baseParameters.contains(keyword)) {
                setBaseParameter(clParameter);
                remainingParameters.remove(clParameter);
            } else if (importantParameters.contains(keyword)) {
                setImportantParameter(clParameter);
                remainingParameters.remove(clParameter);
            } else if (compileParameters.contains(keyword)) {
                setCompileParameter(clParameter);
                remainingParameters.remove(clParameter);
            } else if (linkParameters.contains(keyword)) {
                setLinkParameter(clParameter);
                remainingParameters.remove(clParameter);
            }
        }

        for (CLParameter clParameter : remainingParameters) {
            if (isBaseKeyword(clParameter)) {
                setBaseParameter(clParameter);
            } else {
                String defaultSection = Preferences.getInstance().getDefaultSection();
                if (Preferences.IMPORTANT.equals(defaultSection)) {
                    setImportantParameter(clParameter);
                } else if (Preferences.COMPILE.equals(defaultSection)) {
                    setCompileParameter(clParameter);
                } else {
                    setLinkParameter(clParameter);
                }
            }
        }
    }

    private void removeDeletedParameters(CLParameter[] clParameters) {

        Iterator<String> keywordsIterator = parameters.keySet().iterator();
        while (keywordsIterator.hasNext()) {
            String keyword = keywordsIterator.next();
            if (!hasParameter(keyword)) {
                removeParameter(keyword);
            }
        }
    }

    private boolean hasParameter(String keyword) {
        return parameters.containsKey(keyword);
    }

    @Override
    protected void initializeStore() {

        width = -1;
        firstLine = -1;
        lastLine = -1;
        createCommand = null;
        parameters.clear();
        baseParameters.clear();
        baseParameters.clear();
        importantParameters.clear();
        compileParameters.clear();
        preCommands.clear();
        postCommands.clear();
        parameterSequence.clear();
    }

    @Override
    protected void storeWidth(int width) {
        this.width = width;
    }

    @Override
    protected void storeFirstLine(int line) {
        this.firstLine = line;
    }

    @Override
    protected void storeLastLine(int line) {
        this.lastLine = line;
    }

    @Override
    protected void storeCreateCommand(String commandString) {

        if (commandString == null) {
            throw new IllegalArgumentException("Parameter 'commandString' must not be null.");
        }

        CLCommand clCommand = clParser.parseCommand(commandString);
        if (clCommand == null) {
            return;
        }

        createCommand = clCommand.getCommand();

        for (CLParameter clParameter : clCommand.getParameters()) {
            setBaseParameter(clParameter);
        }
    }

    @Override
    protected void storeImportantParameter(String parameterString) {

        CLParameter clParameter = clParser.parseParameter(parameterString);
        setImportantParameter(clParameter);
    }

    @Override
    protected void storeCompileParameter(String parameterString) {

        CLParameter clParameter = clParser.parseParameter(parameterString);
        setCompileParameter(clParameter);
    }

    @Override
    protected void storeLinkParameter(String parameterString) {

        CLParameter clParameter = clParser.parseParameter(parameterString);
        setLinkParameter(clParameter);
    }

    @Override
    protected void storePreCommand(String commandString) {
        if (commandString == null) {
            return;
        }
        preCommands.add(commandString);
    }

    @Override
    protected void storePostCommand(String commandString) {
        if (commandString == null) {
            return;
        }
        postCommands.add(commandString);
    }

    @Override
    protected int getWidth() {
        return width;
    }

    @Override
    protected String getCreateCommand(CLFormatter formatter) {

        LinkedList<String> tempParameters;
        if (useSections()) {
            tempParameters = baseParameters;
        } else {
            tempParameters = getMergedParameters();
        }

        if (formatter == null) {
            CLCommand clCommand = new CLCommand(createCommand);

            for (String keyword : tempParameters) {
                CLParameter parameter = parameters.get(keyword);
                clCommand.setParameter(parameter);
            }

            return clCommand.toString();
        }

        // We have to pass the full object creation command to the formatter,
        // because the QCAPCMD cannot format a command with missing required
        // parameter, such as a missing PGM parameter with a CRTCLPGM command.
        String fullCommandString = formatter.format(getFullCommand());

        // Afterwards the extra parameters have to be removed again.
        return keepBaseParametersOnly(fullCommandString);
    }

    private LinkedList<String> getMergedParameters() {

        LinkedList<String> tempParameters;
        tempParameters = new LinkedList<String>();
        tempParameters.addAll(baseParameters);
        tempParameters.addAll(importantParameters);
        tempParameters.addAll(compileParameters);
        tempParameters.addAll(linkParameters);

        return tempParameters;
    }

    private String keepBaseParametersOnly(String fullCommand) {

        CLParser parser = new CLParser();
        CLCommand clCommand = parser.parseCommand(fullCommand);
        for (CLParameter clParameter : clCommand.getParameters()) {
            String keyword = clParameter.getKeyword();
            if (!baseParameters.contains(keyword)) {
                // removeKeywords.add(keyword);
                clCommand.removeParameter(keyword);
            }
        }

        return clCommand.toString();
    }

    @Override
    protected String[] getImportantParameters() {
        if (useSections()) {
            return getParameters(importantParameters);
        } else {
            return new String[0];
        }
    }

    @Override
    protected String[] getCompileParameters() {
        if (useSections()) {
            return getParameters(compileParameters);
        } else {
            return new String[0];
        }
    }

    @Override
    protected String[] getLinkParameters() {
        if (useSections()) {
            return getParameters(linkParameters);
        } else {
            return new String[0];
        }
    }

    @Override
    protected String[] getPreCommands(CLFormatter formatter) {
        return getPreOrPostCommands(preCommands, formatter);
    }

    @Override
    protected String[] getPostCommands(CLFormatter formatter) {
        return getPreOrPostCommands(postCommands, formatter);
    }

    private String[] getPreOrPostCommands(LinkedList<String> commands, CLFormatter formatter) {

        if (formatter == null) {
            return commands.toArray(new String[postCommands.size()]);
        }

        LinkedList<String> formattedCommands = new LinkedList<String>();
        for (String command : commands) {
            formattedCommands.add(formatter.format(command));
        }

        return formattedCommands.toArray(new String[formattedCommands.size()]);
    }

    private String[] getParameters(LinkedList<String> parametersSet) {

        List<String> tempParameters = new ArrayList<String>();

        for (String keyword : parameterSequence) {
            if (parametersSet.contains(keyword)) {
                tempParameters.add(parameters.get(keyword).toString());
            }
        }

        return tempParameters.toArray(new String[tempParameters.size()]);
    }

    private void setBaseParameter(CLParameter clParameter) {
        setParameter(baseParameters, clParameter);
    }

    private void setImportantParameter(CLParameter clParameter) {
        setParameter(importantParameters, clParameter);
    }

    private void setCompileParameter(CLParameter clParameter) {
        setParameter(compileParameters, clParameter);
    }

    private void setLinkParameter(CLParameter clParameter) {
        setParameter(linkParameters, clParameter);
    }

    private void setParameter(LinkedList<String> parameterSet, CLParameter clParameter) {

        String keyword = clParameter.getKeyword();

        if (parameterSet.contains(keyword)) {
            parameters.get(keyword).setValue(clParameter.getValue());
        } else {
            removeParameter(keyword);
            parameters.put(keyword, clParameter);
            parameterSet.add(keyword);
        }
    }

    private void removeParameter(String keyword) {

        if (parameters.containsKey(keyword)) {
            parameters.remove(keyword);
            baseParameters.remove(keyword);
            importantParameters.remove(keyword);
            compileParameters.remove(keyword);
            linkParameters.remove(keyword);
        }
    }

    private boolean isBaseKeyword(CLParameter clParameter) {
        return HeaderTemplates.getInstance().isBaseKeyword(clParameter.getKeyword(), memberType);
    }

    private boolean useSections() {

        if (createTemplates) {
            return true;
        }

        return Preferences.getInstance().useParameterSections();
    }
}
