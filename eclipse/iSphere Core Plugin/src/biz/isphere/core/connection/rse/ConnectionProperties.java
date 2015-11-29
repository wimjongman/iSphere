/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.connection.rse;

import java.util.Properties;

public class ConnectionProperties {

    public static final String CONNECTION_NAME = "connection.name";
    public static final String ISPHERE_LIBRARY_NAME = "connection.isphere.library";
    public static final String USE_CONNECTION_SPECIFIC_SETTINGS = "connection.use.settings";

    private Properties properties;

    public ConnectionProperties(Properties properties) {

        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getConnectionName() {
        return properties.getProperty(CONNECTION_NAME);
    }

    public String getISphereLibraryName() {
        return properties.getProperty(ISPHERE_LIBRARY_NAME);
    }

    public void setISphereLibraryName(String name) {
        properties.setProperty(ISPHERE_LIBRARY_NAME, name);
    }

    public boolean useISphereLibraryName() {
        return Boolean.parseBoolean(properties.getProperty(USE_CONNECTION_SPECIFIC_SETTINGS));
    }

    public void setUseISphereLibraryName(boolean enable) {
        properties.setProperty(USE_CONNECTION_SPECIFIC_SETTINGS, Boolean.toString(enable));
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("Connection: "); //$NON-NLS-1$
        buffer.append(getConnectionName());
        buffer.append(", library: "); //$NON-NLS-1$
        buffer.append(getISphereLibraryName());

        return super.toString();
    }
}
