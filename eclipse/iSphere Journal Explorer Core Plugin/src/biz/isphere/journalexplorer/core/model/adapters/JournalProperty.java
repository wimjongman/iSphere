/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Initial idea and development: Isaac Ramirez Herrera
 * Continued and adopted to iSphere: iSphere Project Team
 *******************************************************************************/

package biz.isphere.journalexplorer.core.model.adapters;

public class JournalProperty implements Comparable<JournalProperty> {

    public String name;
    public Object value;
    public Object parent;
    public boolean highlighted;

    private boolean errorParsing;

    public JournalProperty(String name, Object value, Object parent) {
        this.name = name;
        this.value = value;
        this.parent = parent;
    }

    public int compareTo(JournalProperty comparable) {

        if (name.equals(comparable.name) && value.equals(comparable.value)) {
            highlighted = comparable.highlighted = false;
            return 0;
        } else {
            highlighted = comparable.highlighted = true;
            return -1;
        }
    }

    public void setErrorParsing(boolean error) {
        this.errorParsing = error;
    }

    public boolean isErrorParsing() {
        return errorParsing;
    }

}
