/*******************************************************************************
 * Copyright (c) 2012-2019 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.ibmi.contributions.extension.handler;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.clcommands.ICLPrompter;
import biz.isphere.core.ibmi.contributions.extension.point.IIBMiHostContributions;
import biz.isphere.core.internal.Member;
import biz.isphere.core.internal.api.retrievememberdescription.MBRD0100;
import biz.isphere.core.internal.api.retrievememberdescription.QUSRMBRD;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;

public class IBMiHostContributionsHandler {

    private static final String EXTENSION_ID = "biz.isphere.core.ibmi.contributions.extension.point.IIBMiHostContributions"; //$NON-NLS-1$

    private static IIBMiHostContributions factory;

    public static boolean hasContribution() {

        if (getContributionsFactory() == null) {
            return false;
        }

        return true;
    }

    public static boolean isRseSubsystemInitialized(String connectionName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return false;
        }

        return factory.isRseSubsystemInitialized(connectionName);
    }

    public static boolean isKerberosAuthentication() {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return false;
        }

        return factory.isKerberosAuthentication();
    }

    public static boolean isSubSystemOffline(String connectionName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return false;
        }

        return factory.isSubSystemOffline(connectionName);
    }

    public static String executeCommand(String connectionName, String command) {
        return executeCommand(connectionName, command, null);
    }

    public static String executeCommand(String connectionName, String command, List<AS400Message> rtnMessages) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return "RDi plug-in not installed."; //$NON-NLS-1$
        }

        return factory.executeCommand(connectionName, command, rtnMessages);
    }

    public static boolean checkLibrary(String connectionName, String libraryName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return false;
        }

        return factory.checkLibrary(connectionName, libraryName);
    }

    public static boolean checkFile(String connectionName, String libraryName, String fileName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return false;
        }

        return factory.checkFile(connectionName, libraryName, fileName);
    }

    public static String resolveMemberName(String connectionName, String libraryName, String fileName, String memberName) throws AS400Exception,
        AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException {

        try {

            AS400 system = IBMiHostContributionsHandler.getSystem(connectionName);
            MBRD0100 mbrd0100 = new MBRD0100(system);

            QUSRMBRD memberDescription = new QUSRMBRD(system);
            memberDescription.setFile(fileName, libraryName, memberName);
            if (memberDescription.execute(mbrd0100)) {
                return mbrd0100.getMemberName();
            }

        } catch (PropertyVetoException e) {
            ISpherePlugin.logError("*** Failed to retrieve member description " + libraryName + "/" + fileName + "(" + memberName + ")" + " ***", e);
        }

        return null;
    }

    public static boolean checkMember(String connectionName, String libraryName, String fileName, String memberName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return false;
        }

        String tMemberName = memberName;

        if (memberName.startsWith("*")) {
            try {
                tMemberName = resolveMemberName(connectionName, libraryName, fileName, tMemberName);
            } catch (Exception e) {
                return false;
            }
        }

        return factory.checkMember(connectionName, libraryName, fileName, tMemberName);
    }

    public static String getISphereLibrary(String connectionName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getISphereLibrary(connectionName);
    }

    public static AS400 findSystem(String hostName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.findSystem(hostName);
    }

    public static AS400 getSystem(String connectionName) {
        return getSystem(null, connectionName);
    }

    public static AS400 getSystem(String profileName, String connectionName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getSystem(profileName, connectionName);
    }

    public static AS400 getSystem(IEditorPart editor) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getSystem(editor);
    }

    /**
     * Returns the connection name of a given editor.
     * 
     * @param editor - that shows a remote file
     * @return name of the connection the file has been loaded from
     */
    public static String getConnectionName(IEditorPart editor) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getConnectionName(editor);
    }

    public static int getSystemCcsid(String connectionName) {

        AS400 system = getSystem(connectionName);
        if (system == null) {
            return -1;
        }

        return system.getCcsid();
    }

    /**
     * Returns the connection name of a given i Project.
     * 
     * @param projectName - name of an i Project
     * @return name of the connection the file has been loaded from
     */
    public static String getConnectionNameOfIProject(String projectName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getConnectionNameOfIProject(projectName);
    }

    /**
     * Returns the connection name of a given TCP/IP Address.
     * 
     * @param projectName - TCP/IP address
     * @param isConnected - specifies whether the connection must be connected
     * @return name of the connection
     */
    public static String getConnectionNameByIPAddr(String tcpIpAddr, boolean isConnected) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getConnectionNameByIPAddr(tcpIpAddr, isConnected);
    }

    /**
     * Returns the name of the associated library of a given i Project.
     * 
     * @param projectName - name of an i Project
     * @return name of the associated library
     */
    public static String getLibraryName(String projectName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getLibraryName(projectName);
    }

    public static String[] getConnectionNames() {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getConnectionNames();
    }

    public static Connection getJdbcConnection(String connectionName) {
        return getJdbcConnection(null, connectionName, null);
    }

    public static Connection getJdbcConnection(String connectionName, Properties properties) {
        return getJdbcConnection(null, connectionName, properties);
    }

    public static Connection getJdbcConnection(String profileName, String connectionName) {
        return getJdbcConnection(profileName, connectionName, null);
    }

    public static Connection getJdbcConnection(String profileName, String connectionName, Properties properties) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getJdbcConnection(profileName, connectionName);
    }

    /**
     * Returns an ICLPrompter for a given connection name.
     * 
     * @param connectionName - connection name to identify the connection
     * @return ICLPrompter
     */
    public static ICLPrompter getCLPrompter(String connectionName) {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getCLPrompter(connectionName);
    }

    public static Member getMember(String connectionName, String libraryName, String fileName, String memberName) throws Exception {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getMember(connectionName, libraryName, fileName, memberName);
    }

    public static void compareSourceMembers(String connectionName, List<Member> members, boolean enableEditMode) throws Exception {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return;
        }

        factory.compareSourceMembers(connectionName, members, enableEditMode);
    }

    public IFile getLocalResource(String connectionName, String libraryName, String fileName, String memberName, String srcType) throws Exception {

        IIBMiHostContributions factory = getContributionsFactory();

        if (factory == null) {
            return null;
        }

        return factory.getLocalResource(connectionName, libraryName, fileName, memberName, srcType);
    }

    /**
     * Returns the RDi contributions if there is a registered extension for
     * that.
     * 
     * @return RDi contributions factory or null
     */
    private static IIBMiHostContributions getContributionsFactory() {

        if (factory == null) {

            IExtensionRegistry tRegistry = Platform.getExtensionRegistry();
            IConfigurationElement[] configElements = tRegistry.getConfigurationElementsFor(EXTENSION_ID);

            if (configElements != null && configElements.length > 0) {
                try {
                    final Object tempDialog = configElements[0].createExecutableExtension("class");
                    if (tempDialog instanceof IIBMiHostContributions) {
                        factory = (IIBMiHostContributions)tempDialog;
                    }
                } catch (CoreException e) {
                }
            }

        }

        return factory;
    }

}
