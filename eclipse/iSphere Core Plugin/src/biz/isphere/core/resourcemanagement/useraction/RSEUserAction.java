/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.resourcemanagement.useraction;

import biz.isphere.core.resourcemanagement.AbstractResource;

public class RSEUserAction extends AbstractResource implements Comparable<RSEUserAction> {

    private RSEDomain domain;
    private String label;
    private String originalName;
    private String commandString;
    private boolean promptFirst;
    private boolean refreshAfter;
    private boolean showAction;
    private boolean singleSelection;
    private boolean invokeOnce;
    private String comment;
    private String[] fileTypes;
    private boolean isIBM;
    private String vendor;
    private int order;
    private Object origin;

    public RSEUserAction() {
        this(null, null, null, false, false, true, false, false, null, new String[0], false, null, null, 0, null);
    }

    public RSEUserAction(RSEDomain domain, String label, String commandString, boolean isPromptFirst, boolean isRefreshAfter, boolean isShowAction,
        boolean isSingleSelection, boolean isInvokeOnce, String comment, String[] fileTypes, boolean isIBM, String vendor, String originalName,
        int order, Object origin) {
        super(true);

        setDomain(domain);
        setLabel(label);
        setCommandString(commandString);
        setPromptFirst(isPromptFirst);
        setRefreshAfter(isRefreshAfter);
        setShowAction(isShowAction);
        setSingleSelection(isSingleSelection);
        setInvokeOnce(isInvokeOnce);
        setComment(comment);
        setFileTypes(fileTypes);
        setOriginalName(originalName);
        setOrder(order);
        setOrigin(origin);
        // must be called before setVendor()
        setIBM(isIBM);
        setVendor(vendor);
    }

    private String getDomainAsString() {
        return null;
    }

    public RSEDomain getDomain() {
        return domain;
    }

    public void setDomain(RSEDomain domain) {
        this.domain = domain;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = ensureNotNull(label);
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getOriginalName() {
        return ensureNotNull(originalName);
    }

    public String getCommandString() {
        return commandString;
    }

    public void setCommandString(String commandString) {
        this.commandString = ensureNotNull(commandString);
    }

    public boolean isPromptFirst() {
        return promptFirst;
    }

    public void setPromptFirst(boolean promptFirst) {
        this.promptFirst = promptFirst;
    }

    public boolean isRefreshAfter() {
        return refreshAfter;
    }

    public void setRefreshAfter(boolean refreshAfter) {
        this.refreshAfter = refreshAfter;
    }

    public boolean isShowAction() {
        return showAction;
    }

    public void setShowAction(boolean showAction) {
        this.showAction = showAction;
    }

    public boolean isSingleSelection() {
        return singleSelection;
    }

    public void setSingleSelection(boolean singleSelection) {
        this.singleSelection = singleSelection;
    }

    public boolean isInvokeOnce() {
        return invokeOnce;
    }

    public void setInvokeOnce(boolean invokeOnce) {
        this.invokeOnce = invokeOnce;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = ensureNotNull(comment);
    }

    public String[] getFileTypes() {
        return fileTypes;
    }

    public void setFileTypes(String[] fileTypes) {
        this.fileTypes = fileTypes;
    }

    public boolean isIBM() {
        return isIBM;
    }

    public void setIBM(boolean isIBM) {
        this.isIBM = isIBM;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = ensureNotNull(vendor);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Object getOrigin() {
        return origin;
    }

    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    @Override
    public String getKey() {
        return Integer.toString(domain.getDomainType()) + ":" + getLabel(); //$NON-NLS-1$
    }

    @Override
    public String getValue() {

        StringBuilder buffer = new StringBuilder();

        appendAttribute(buffer, label);
        appendAttribute(buffer, originalName);
        appendAttribute(buffer, commandString);
        appendAttribute(buffer, promptFirst);
        appendAttribute(buffer, refreshAfter);
        appendAttribute(buffer, showAction);
        appendAttribute(buffer, singleSelection);
        appendAttribute(buffer, invokeOnce);
        appendAttribute(buffer, comment);
        appendAttribute(buffer, fileTypes);
        appendAttribute(buffer, isIBM);
        appendAttribute(buffer, vendor);

        return buffer.toString();
    }

    private void appendAttribute(StringBuilder buffer, String value) {

        if (buffer.length() > 0) {
            buffer.append(":"); //$NON-NLS-1$
        }
        buffer.append(value);
    }

    private void appendAttribute(StringBuilder buffer, String[] values) {

        if (buffer.length() > 0) {
            buffer.append(":"); //$NON-NLS-1$
        }

        buffer.append("["); //$NON-NLS-1$
        boolean isFirstItem = true;
        for (String value : values) {
            if (!isFirstItem) {
                buffer.append(","); //$NON-NLS-1$
            } else {
                isFirstItem = false;
            }
            buffer.append(value);
        }
        buffer.append("]"); //$NON-NLS-1$
    }

    private void appendAttribute(StringBuilder buffer, boolean value) {

        if (buffer.length() > 0) {
            buffer.append(":"); //$NON-NLS-1$
        }
        buffer.append(Boolean.toString(value));
    }

    private String ensureNotNull(String value) {

        if (value == null) {
            return ""; //$NON-NLS-1$
        }

        return value;
    }

    @Override
    public String toString() {
        return getDomainAsString() + ": " + getLabel() + ": " + getCommandString(); //$NON-NLS-1$  //$NON-NLS-2$
    }

    public int compareTo(RSEUserAction other) {

        if (other == null || other.getKey() == null) {
            return 1;
        } else {
            return getKey().compareTo(other.getKey());
        }
    }
}