/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.strpreprc.model;

public enum StrPrePrcCommandType {
    CREATE ("*CREATE-COMMAND"),
    PRE ("*PRE-COMMAND"),
    POST ("*POST-COMMAND");

    private String type;

    private StrPrePrcCommandType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
