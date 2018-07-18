/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.resourcemanagement.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import biz.isphere.core.resourcemanagement.command.CommandXmlComparator;
import biz.isphere.core.resourcemanagement.command.RSECommand;
import biz.isphere.core.resourcemanagement.command.RSECompileType;
import biz.isphere.core.resourcemanagement.filter.RSEProfile;
import biz.isphere.rse.Messages;
import biz.isphere.rse.resourcemanagement.AbstractXmlHelper;

public class XMLCommandHelper extends AbstractXmlHelper {

    private static final String CURRENT_VERSION = "1.0.0";
    private static final String MIN_VERSION = "1.0.0";

    private static final String COMPILE_TYPES = "compiletypes";
    private static final String COMPILE_TYPE = "compiletype";
    private static final String COMPILE_TYPE_TYPE = "type";

    private static final String COMMANDS = "commands";
    private static final String ID = "id";
    private static final String COMMAND = "command";
    private static final String COMMAND_STRING_EDITABLE = "isCommandEditable";
    private static final String ORDER = "order";
    private static final String LABEL = "label";
    private static final String LABEL_EDITABLE = "isLabelEditable";
    private static final String DEFAULT_COMMAND_STRING = "defaultCommandString";
    private static final String CURRENT_COMMAND_STRING = "currentCommandString";
    private static final String NATURE = "nature";
    private static final String MENU_OPTION = "menuOption";

    public static void saveCommandsToXML(File toFile, boolean singleCompileType, RSECommand[] commands) throws Exception {

        Arrays.sort(commands, new CommandXmlComparator());

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(toFile));

        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t");

        eventWriter.add(eventFactory.createStartDocument());
        eventWriter.add(end);

        if (singleCompileType) {

            startContainer(eventWriter, eventFactory, CURRENT_VERSION, end);

            createCommands(eventWriter, eventFactory, end, tab, commands);

            endContainer(eventWriter, eventFactory, end);

        } else {

            startContainer(eventWriter, eventFactory, CURRENT_VERSION, end);

            eventWriter.add(eventFactory.createStartElement("", "", COMPILE_TYPES));
            eventWriter.add(end);

            Map<String, List<RSECommand>> _pools = new TreeMap<String, List<RSECommand>>();
            for (int idx = 0; idx < commands.length; idx++) {
                String _pool = commands[idx].getCompileType().getType();
                List<RSECommand> _commands = (List<RSECommand>)_pools.get(_pool);
                if (_commands == null) {
                    _commands = new LinkedList<RSECommand>();
                    _pools.put(_pool, _commands);
                }
                _commands.add(commands[idx]);
            }

            for (Map.Entry<String, List<RSECommand>> entry : _pools.entrySet()) {

                eventWriter.add(eventFactory.createStartElement("", "", COMPILE_TYPE));
                eventWriter.add(end);

                createNode(eventWriter, eventFactory, end, tab, COMPILE_TYPE_TYPE, entry.getKey());

                RSECommand[] _commands = new RSECommand[entry.getValue().size()];
                entry.getValue().toArray(_commands);
                createCommands(eventWriter, eventFactory, end, tab, _commands);

                eventWriter.add(eventFactory.createEndElement("", "", COMPILE_TYPE));
                eventWriter.add(end);

            }

            eventWriter.add(eventFactory.createEndElement("", "", COMPILE_TYPES));
            eventWriter.add(end);

            endContainer(eventWriter, eventFactory, end);

        }

        eventWriter.add(eventFactory.createEndDocument());

        eventWriter.flush();
        eventWriter.close();

    }

    private static void createCommands(XMLEventWriter eventWriter, XMLEventFactory eventFactory, XMLEvent end, XMLEvent tab, RSECommand[] commands)
        throws XMLStreamException {

        eventWriter.add(eventFactory.createStartElement("", "", COMMANDS));
        eventWriter.add(end);

        for (int idx1 = 0; idx1 < commands.length; idx1++) {

            eventWriter.add(eventFactory.createStartElement("", "", COMMAND));
            eventWriter.add(end);

            createNode(eventWriter, eventFactory, end, tab, ID, commands[idx1].getId());
            createNode(eventWriter, eventFactory, end, tab, ORDER, integerToXml(commands[idx1].getOrder()));
            createNode(eventWriter, eventFactory, end, tab, LABEL, commands[idx1].getLabel());
            createNode(eventWriter, eventFactory, end, tab, LABEL_EDITABLE, commands[idx1].isLabelEditable());
            createNode(eventWriter, eventFactory, end, tab, DEFAULT_COMMAND_STRING, commands[idx1].getDefaultCommandString());
            createNode(eventWriter, eventFactory, end, tab, CURRENT_COMMAND_STRING, commands[idx1].getCurrentCommandString());
            createNode(eventWriter, eventFactory, end, tab, COMMAND_STRING_EDITABLE, commands[idx1].isCommandStringEditable());
            createNode(eventWriter, eventFactory, end, tab, NATURE, commands[idx1].getNature());
            createNode(eventWriter, eventFactory, end, tab, MENU_OPTION, commands[idx1].getMenuOption());

            eventWriter.add(eventFactory.createEndElement("", "", COMMAND));
            eventWriter.add(end);

        }

        eventWriter.add(eventFactory.createEndElement("", "", COMMANDS));
        eventWriter.add(end);

    }

    public static RSECommand[] restoreCommandsFromXML(File fromFile, boolean singleCompileType, RSEProfile profile, RSECompileType compileType)
        throws Exception {

        ArrayList<RSECommand> items = new ArrayList<RSECommand>();

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        InputStream in = new FileInputStream(fromFile);
        XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

        RSECompileType type = null;
        if (singleCompileType) {
            type = compileType;
        }
        RSECommand command = null;
        StringBuilder elementData = new StringBuilder();

        boolean isValidated = false;
        String versionNumber = null;

        while (eventReader.hasNext()) {

            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                if (isContainerStartElement(event)) {
                    versionNumber = getVersionNumber(event);
                    isValidated = validateVersionNumber(event, MIN_VERSION);
                } else if (event.asStartElement().getName().getLocalPart().equals(COMPILE_TYPE)) {
                    type = new RSECompileType(profile);
                } else if (event.asStartElement().getName().getLocalPart().equals(COMPILE_TYPE_TYPE)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(COMMAND)) {
                    command = new RSECommand();
                    command.setCompileType(type);
                } else if (event.asStartElement().getName().getLocalPart().equals(ID)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(ORDER)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(LABEL)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(LABEL_EDITABLE)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(DEFAULT_COMMAND_STRING)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(CURRENT_COMMAND_STRING)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(COMMAND_STRING_EDITABLE)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(NATURE)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(MENU_OPTION)) {
                    startElementCharacters(elementData, event);
                } else {
                    clearElementCharacters(elementData);
                }
            } else if (event.isEndElement()) {
                if (!isValidated) {
                    throw new Exception(Messages.bind(Messages.Cannot_load_the_selected_repository, versionNumber));
                }
                if (event.asEndElement().getName().getLocalPart().equals(COMPILE_TYPE_TYPE)) {
                    type.setType(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(ID)) {
                    command.setId(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(ORDER)) {
                    command.setOrder(xmlToInteger(elementData.toString()));
                } else if (event.asEndElement().getName().getLocalPart().equals(LABEL)) {
                    command.setLabel(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(LABEL_EDITABLE)) {
                    command.setLabelEditable(xmlToBoolean(elementData.toString(), true));
                } else if (event.asEndElement().getName().getLocalPart().equals(DEFAULT_COMMAND_STRING)) {
                    command.setDefaultCommandString(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(CURRENT_COMMAND_STRING)) {
                    command.setCurrentCommandString(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(COMMAND_STRING_EDITABLE)) {
                    command.setCommandStringEditable(xmlToBoolean(elementData.toString(), true));
                } else if (event.asEndElement().getName().getLocalPart().equals(NATURE)) {
                    command.setNature(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(MENU_OPTION)) {
                    command.setMenuOption(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(COMMAND)) {
                    items.add(command);
                }
                clearElementCharacters(elementData);
            } else if (event.isCharacters()) {
                collectElementCharacters(elementData, event);
            }

        }

        RSECommand[] commands = new RSECommand[items.size()];
        items.toArray(commands);

        return commands;
    }

}
