/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.ibmi.contributions.extension.point;

import java.sql.Connection;
import java.util.List;

import org.eclipse.ui.IEditorPart;

import biz.isphere.core.clcommands.ICLPrompter;
import biz.isphere.core.internal.Member;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;

public interface IIBMiHostContributions {

    /**
     * Executes a given command for a given connection.
     * 
     * @param connectionName - connection used for executing the command
     * @param command - command that is executed
     * @param rtnMessages - list of error messages or <code>null</code>
     * @return error message text on error or <code>null</code> on success
     */
    public String executeCommand(String connectionName, String command, List<AS400Message> rtnMessages);

    /**
     * Checks whether a given library exists or not.
     * 
     * @param connectionName - connection that is checked for a given library
     * @param libraryName - library that is tested
     * @return <code>true</code>, when the library exists, else
     *         <code>false</code>.
     */
    public boolean checkLibrary(String connectionName, String libraryName);

    /**
     * Checks whether a given file exists or not.
     * 
     * @param connectionName - connection that is checked for a given library
     * @param libraryName - library that should contain the file
     * @param fileName - file that is tested
     * @return <code>true</code>, when the file exists, else <code>false</code>.
     */
    public boolean checkFile(String connectionName, String libraryName, String fileName);

    /**
     * Checks whether a given member exists or not.
     * 
     * @param connectionName - connection that is checked for a given library
     * @param libraryName - library that should contain the file
     * @param fileName - file that should contain the member
     * @param memberName - name of the member that is tested
     * @return <code>true</code>, when the library exists, else
     *         <code>false</code>.
     */
    public boolean checkMember(String connectionName, String libraryName, String fileName, String memberName);

    /**
     * Returns the name of the iSphere library that is associated to a given
     * connection.
     * 
     * @param connectionName - name of the connection the name of the iSphere
     *        library is returned for
     * @return name of the iSphere library
     */
    public String getISphereLibrary(String connectionName);

    /**
     * Returns an AS400 object for a given host name.
     * 
     * @param hostName - host name to identify the connection
     * @return AS400 object that is associated to the host
     */
    public AS400 findSystem(String hostName);

    /**
     * Returns an AS400 object for a given connection name.
     * 
     * @param connectionName - connection name to identify the connection
     * @return AS400 object that is associated to the connection
     */
    public AS400 getSystem(String connectionName);

    /**
     * Returns an AS400 object for a given profile and connection name.
     * 
     * @param profile - name of the profile, that hosts the connection
     * @param connectionName - connection name to identify the connection
     * @return AS400 object that is associated to the connection
     */
    public AS400 getSystem(String profile, String connectionName);

    /**
     * Returns an AS400 object for a given editor.
     * 
     * @param editor - that shows a remote file
     * @return AS400 object that is associated to editor
     */
    public AS400 getSystem(IEditorPart editor);

    /**
     * Returns the connection name of a given editor.
     * 
     * @param editor - that shows a remote file
     * @return name of the connection the file has been loaded from
     */
    public String getConnectionName(IEditorPart editor);

    /**
     * returns a list of configured connections.
     * 
     * @return names of configured connections
     */
    public String[] getConnectionNames();

    /**
     * Returns a JDBC connection for a given connection name.
     * 
     * @param connectionName - connection name to identify the connection
     * @return JDBC connection that is associated to the connection
     */
    public Connection getJdbcConnection(String connectionName);

    /**
     * Returns a JDBC connection for a given profile and connection name.
     * 
     * @param profile - name of the profile, that hosts the connection
     * @param connectionName - connection name to identify the connection
     * @return JDBC connection that is associated to the connection
     */
    public Connection getJdbcConnection(String profile, String connectionName);

    /**
     * Returns an ICLPrompter for a given connection name.
     * 
     * @param connectionName - connection name to identify the connection
     * @return ICLPrompter
     */
    public ICLPrompter getCLPrompter(String connectionName);

    /**
     * Returns a source member from an IBM i.
     * 
     * @param connectionName - Connection used to locate the member.
     * @param libraryName - Name of the library that contains the source file.
     * @param fileName - Name of the source file that contains the member.
     * @param memberName - Name of the member.
     * @return Member
     * @throws Exception
     */
    public Member getMember(String connectionName, String libraryName, String fileName, String memberName) throws Exception;

    /**
     * Opens the iSphere Compare&Merge editor for the selected members.
     * 
     * @param connectionName - Connection used to locate the member.
     * @param members - members that are compared
     * @throws Exception
     */
    public void compareSourceMembers(String connectionName, List<Member> members) throws Exception;
}
