/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.resourcemanagement.useraction;

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

import biz.isphere.core.resourcemanagement.filter.RSEProfile;
import biz.isphere.core.resourcemanagement.useraction.RSEDomain;
import biz.isphere.core.resourcemanagement.useraction.RSEUserAction;
import biz.isphere.rse.resourcemanagement.AbstractXmlHelper;

public class XMLUserActionHelper extends AbstractXmlHelper {

    private static final String DOMAINS = "domains";
    private static final String DOMAIN = "domain";
    private static final String DOMAIN_NAME = "name";
    private static final String DOMAIN_TYPE = "type";

    private static final String USER_ACTIONS = "userActions";
    private static final String USER_ACTION = "userAction";
    private static final String ORDER = "order";
    private static final String LABEL = "label";
    private static final String ORIGINAL_NAME = "originalName";
    private static final String COMMAND_STRING = "commandString";
    private static final String PROMPT_FIRST = "promptFirst";
    private static final String REFRESH_AFTER = "refreshAfter";
    private static final String SHOW_ACTION = "showAction";
    private static final String SINGLE_SELECTION = "singleSelection";
    private static final String INVOKE_ONCE = "invokeOnce";
    private static final String IS_IBM = "isIBM";
    private static final String VENDOR = "vendor";
    private static final String COMMENT = "comment";
    private static final String FILE_TYPES = "fileTypes";

    public static void saveUserActionsToXML(File toFile, boolean singleDomain, RSEUserAction[] userActions) throws Exception {

        Arrays.sort(userActions);

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(toFile));

        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t");

        eventWriter.add(eventFactory.createStartDocument());

        if (singleDomain) {

            createUserActions(eventWriter, eventFactory, end, tab, userActions);

        } else {

            eventWriter.add(eventFactory.createStartElement("", "", DOMAINS));
            eventWriter.add(end);

            Map<RSEDomain, List<RSEUserAction>> _pools = new TreeMap<RSEDomain, List<RSEUserAction>>();
            for (int idx = 0; idx < userActions.length; idx++) {
                RSEDomain _domain = userActions[idx].getDomain();
                List<RSEUserAction> _userActions = (List<RSEUserAction>)_pools.get(_domain);
                if (_userActions == null) {
                    _userActions = new LinkedList<RSEUserAction>();
                    _pools.put(_domain, _userActions);
                }
                _userActions.add(userActions[idx]);
            }

            for (Map.Entry<RSEDomain, List<RSEUserAction>> entry : _pools.entrySet()) {

                eventWriter.add(eventFactory.createStartElement("", "", DOMAIN));
                eventWriter.add(end);

                createNode(eventWriter, eventFactory, end, tab, DOMAIN_TYPE, integerToXml(entry.getKey().getDomainType()));
                createNode(eventWriter, eventFactory, end, tab, DOMAIN_NAME, entry.getKey().getName());

                RSEUserAction[] _userActions = new RSEUserAction[entry.getValue().size()];
                entry.getValue().toArray(_userActions);
                createUserActions(eventWriter, eventFactory, end, tab, _userActions);

                eventWriter.add(eventFactory.createEndElement("", "", DOMAIN));
                eventWriter.add(end);

            }

            eventWriter.add(eventFactory.createEndElement("", "", DOMAINS));
            eventWriter.add(end);

        }

        eventWriter.add(eventFactory.createEndDocument());

        eventWriter.close();

    }

    private static void createUserActions(XMLEventWriter eventWriter, XMLEventFactory eventFactory, XMLEvent end, XMLEvent tab,
        RSEUserAction[] userActions) throws XMLStreamException {

        eventWriter.add(eventFactory.createStartElement("", "", USER_ACTIONS));
        eventWriter.add(end);

        for (int idx1 = 0; idx1 < userActions.length; idx1++) {

            eventWriter.add(eventFactory.createStartElement("", "", USER_ACTION));
            eventWriter.add(end);

            createNode(eventWriter, eventFactory, end, tab, ORDER, integerToXml(userActions[idx1].getOrder()));
            createNode(eventWriter, eventFactory, end, tab, LABEL, userActions[idx1].getLabel());
            createNode(eventWriter, eventFactory, end, tab, ORIGINAL_NAME, userActions[idx1].getOriginalName());
            createNode(eventWriter, eventFactory, end, tab, COMMAND_STRING, userActions[idx1].getCommandString());
            createNode(eventWriter, eventFactory, end, tab, PROMPT_FIRST, userActions[idx1].isPromptFirst());
            createNode(eventWriter, eventFactory, end, tab, REFRESH_AFTER, userActions[idx1].isRefreshAfter());
            createNode(eventWriter, eventFactory, end, tab, SHOW_ACTION, userActions[idx1].isShowAction());
            createNode(eventWriter, eventFactory, end, tab, SINGLE_SELECTION, userActions[idx1].isSingleSelection());
            createNode(eventWriter, eventFactory, end, tab, INVOKE_ONCE, userActions[idx1].isInvokeOnce());
            createNode(eventWriter, eventFactory, end, tab, IS_IBM, userActions[idx1].isIBM());
            createNode(eventWriter, eventFactory, end, tab, VENDOR, userActions[idx1].getVendor());
            createNode(eventWriter, eventFactory, end, tab, COMMENT, userActions[idx1].getComment());
            createNode(eventWriter, eventFactory, end, tab, FILE_TYPES, arrayToXml(userActions[idx1].getFileTypes()));

            eventWriter.add(eventFactory.createEndElement("", "", USER_ACTION));
            eventWriter.add(end);

        }

        eventWriter.add(eventFactory.createEndElement("", "", USER_ACTIONS));
        eventWriter.add(end);

    }

    public static RSEUserAction[] restoreUserActionsFromXML(File fromFile, boolean singleDomain, RSEProfile profile, RSEDomain domain)
        throws Exception {

        ArrayList<RSEUserAction> items = new ArrayList<RSEUserAction>();

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        InputStream in = new FileInputStream(fromFile);
        XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

        RSEDomain _domain = null;
        if (singleDomain) {
            _domain = domain;
        }
        RSEUserAction userAction = null;
        StringBuilder elementData = new StringBuilder();

        while (eventReader.hasNext()) {

            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                if (event.asStartElement().getName().getLocalPart().equals(DOMAIN)) {
                    _domain = new RSEDomain(profile);
                } else if (event.asStartElement().getName().getLocalPart().equals(DOMAIN_NAME)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(DOMAIN_TYPE)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(USER_ACTION)) {
                    userAction = new RSEUserAction();
                    userAction.setDomain(_domain);
                } else if (event.asStartElement().getName().getLocalPart().equals(ORDER)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(LABEL)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(ORIGINAL_NAME)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(COMMAND_STRING)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(PROMPT_FIRST)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(REFRESH_AFTER)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(SHOW_ACTION)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(SINGLE_SELECTION)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(INVOKE_ONCE)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(IS_IBM)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(VENDOR)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(COMMENT)) {
                    startElementCharacters(elementData, event);
                } else if (event.asStartElement().getName().getLocalPart().equals(FILE_TYPES)) {
                    startElementCharacters(elementData, event);
                } else {
                    clearElementCharacters(elementData);
                }
            } else if (event.isEndElement()) {
                if (event.asEndElement().getName().getLocalPart().equals(DOMAIN_NAME)) {
                    _domain.setName(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(DOMAIN_TYPE)) {
                    _domain.setDomainType(xmlToInteger(elementData.toString()));
                } else if (event.asEndElement().getName().getLocalPart().equals(ORDER)) {
                    userAction.setOrder(xmlToInteger(elementData.toString()));
                } else if (event.asEndElement().getName().getLocalPart().equals(LABEL)) {
                    userAction.setLabel(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(ORIGINAL_NAME)) {
                    userAction.setOriginalName(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(COMMAND_STRING)) {
                    userAction.setCommandString(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(PROMPT_FIRST)) {
                    userAction.setPromptFirst(xmlToBoolean(elementData.toString(), false));
                } else if (event.asEndElement().getName().getLocalPart().equals(REFRESH_AFTER)) {
                    userAction.setRefreshAfter(xmlToBoolean(elementData.toString(), false));
                } else if (event.asEndElement().getName().getLocalPart().equals(SHOW_ACTION)) {
                    userAction.setShowAction(xmlToBoolean(elementData.toString(), true));
                } else if (event.asEndElement().getName().getLocalPart().equals(SINGLE_SELECTION)) {
                    userAction.setSingleSelection(xmlToBoolean(elementData.toString(), false));
                } else if (event.asEndElement().getName().getLocalPart().equals(INVOKE_ONCE)) {
                    userAction.setInvokeOnce(xmlToBoolean(elementData.toString(), false));
                } else if (event.asEndElement().getName().getLocalPart().equals(IS_IBM)) {
                    userAction.setIBM(xmlToBoolean(elementData.toString(), false));
                } else if (event.asEndElement().getName().getLocalPart().equals(VENDOR)) {
                    userAction.setVendor(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(COMMENT)) {
                    userAction.setComment(elementData.toString());
                } else if (event.asEndElement().getName().getLocalPart().equals(FILE_TYPES)) {
                    userAction.setFileTypes(xmlToArray(elementData.toString()));
                } else if (event.asEndElement().getName().getLocalPart().equals(USER_ACTION)) {
                    items.add(userAction);
                }
                clearElementCharacters(elementData);
            } else {
                collectElementCharacters(elementData, event);
            }

        }

        RSEUserAction[] userActions = new RSEUserAction[items.size()];
        items.toArray(userActions);

        return userActions;
    }

}