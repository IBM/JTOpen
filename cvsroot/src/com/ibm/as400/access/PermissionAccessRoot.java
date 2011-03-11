///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: PermissionAccessRoot.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

/**
 * The PermissionAccessRoot class is provided to retrieve the user's permission
 * information.
 *
**/
class PermissionAccessRoot extends PermissionAccess
{
    /**
     * Constructs a PermissionAccessRoot object.
     *
    **/
    public PermissionAccessRoot(AS400 system)
    {
        super(system);
        return;
    }

    /**
     * Adds the authorized user or user permission.
     * @param objName The object the authorized user will be added to.
     * @param permission The permission of the new authorized user.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    public void addUser(String objName,UserPermission permission)
         throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException
    {
        CommandCall addUser= getChgCommand(as400_,objName,permission,followSymbolicLinks_);

        if (addUser.run()!=true)
        {
           AS400Message[] msgList = addUser.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;

    }


    // @B3a - New method.
    /**
     * Prepares the object name for parsing by the IBM i Command Analyzer.
     * @param objName The name of an object.
     * @return A version of the name that is parsable by the Command Analyzer.
     *
    **/
    protected final String expandQuotes(String objName)
    {
      return expandQuotes0(objName);
    }

    // @B3a - New method.
    /**
     * If the name contains single- or double-quotes, doubles up the quotes and encloses the entire name in double-quotes.
     * Regardless, encloses the entire name in single-quotes.
     * This prepares the name for parsing by the IBM i Command Analyzer.
     * @param objName The name of an object.
     * @return A version of the name that is parsable by the Command Analyzer.
     *
    **/
    static String expandQuotes0(String objName)
    {  // @B4c
      StringBuffer buf = new StringBuffer(objName);
      // See if the name contains any single- or double-quotes.
      if (objName.indexOf('\'') != -1 ||
          objName.indexOf('\"') != -1) {
        // Double-up all single- and double-quotes.
        for (int i=objName.length()-1; i>=0; i--) {
          if (buf.charAt(i) == '\'') { buf.insert(i,'\''); }
          else if (buf.charAt(i) == '\"') { buf.insert(i,'\"'); }
        }
        // Now enclose the entire name in double-quotes.
        buf.insert(0,'\"');
        buf.append('\"');
      }
      // Finally, enclose the entire name in single-quotes.
      buf.insert(0,'\'');
      buf.append('\'');

      return buf.toString();
    }

    /**
     * Returns the command to change a authorized user's permission.
     * @param objName The object that the authorized information will be set to.
     * @param permission The permission will be changed.
     * @return The command to remove a authorized user.
     *
    **/
    private static CommandCall getChgCommand(AS400 sys,String objName,UserPermission permission, boolean followSymbolicLinks)
    {
        RootPermission rootPermission = (RootPermission)permission;
        String userProfile=rootPermission.getUserID();
        String dataAuthority=rootPermission.getDataAuthority();

        boolean objMgt = rootPermission.isManagement();
        boolean objExist = rootPermission.isExistence();
        boolean objAlter = rootPermission.isAlter();
        boolean objRef = rootPermission.isReference();
        String objAuthority="";
        if(objMgt==true)
             objAuthority=objAuthority+"*OBJMGT ";
        if(objExist==true)
             objAuthority=objAuthority+"*OBJEXIST ";
        if(objAlter==true)
             objAuthority=objAuthority+"*OBJALTER ";
        if(objRef==true)
             objAuthority=objAuthority+"*OBJREF";
        if((objMgt==false)&&(objExist==false)&&(objAlter==false)&&(objRef==false))
        {
             objAuthority="*NONE";
             if (dataAuthority.equals("*NONE"))
                 dataAuthority = "*EXCLUDE";
        }

        String command = "CHGAUT"
                         +" OBJ("+expandQuotes0(objName)+")"     // @B3c @B4c
                         +" USER("+userProfile+")"
                         +" DTAAUT("+dataAuthority+")"
                         +" OBJAUT("+objAuthority+")";
        if (!followSymbolicLinks)
        {
          command += " SYMLNK(*YES)";
        }
        CommandCall cmd = new CommandCall(sys, command); //@A2C
//        cmd.setThreadSafe(true);   //@A2A
        return cmd;                //@A2C
    }

    /**
     * Returns the command to remove a authorized user.
     * @param objName The object the authorized user will be removed from.
     * @param userName The profile name of the authorized user.
     * @return The command to remove a authorized user.
     *
    **/
    private static CommandCall getRmvCommand(AS400 sys,String objName,String userName, boolean followSymbolicLinks)
    {
        String dataAuthority="*NONE";
        String objAuthority="*NONE";
        String command = "CHGAUT"
                         +" OBJ("+expandQuotes0(objName)+")"          // @B3c @B4c
                         +" USER("+userName+")"
                         +" DTAAUT("+dataAuthority+")"
                         +" OBJAUT("+objAuthority+")";
        if (!followSymbolicLinks)
        {
          command += " SYMLNK(*YES)";
        }
        CommandCall cmd = new CommandCall(sys, command); //@A2C
//        cmd.setThreadSafe(true);   //@A2A
        return cmd;                //@A2C
    }

    /**
     * Returns the user's permission retrieve from the system.
     * @return The user's permission retrieve from the system.
     * @exception UnsupportedEncodingException The Character Encoding is not supported.
     *
    **/
    public UserPermission getUserPermission(Record userRecord)
         throws UnsupportedEncodingException
    {
        String profileName=((String)userRecord.getField("profileName")).trim();
        String userOrGroup=((String)userRecord.getField("userOrGroup")).trim();
        String dataAuthority=((String)userRecord.getField("dataAuthority")).trim();
        String autListMgt=((String)userRecord.getField("autListMgt")).trim();
        String objMgt=((String)userRecord.getField("objMgt")).trim();
        String objExistence=((String)userRecord.getField("objExistence")).trim();
        String objAlter=((String)userRecord.getField("objAlter")).trim();
        String objRef=((String)userRecord.getField("objRef")).trim();
        RootPermission permission;
        permission =new RootPermission(profileName);
        permission.setGroupIndicator(getIntValue(userOrGroup));

        permission.setAuthorizationListManagement(getBooleanValue(autListMgt));
        permission.setManagement(getBooleanValue(objMgt));
        permission.setExistence(getBooleanValue(objExistence));
        permission.setAlter(getBooleanValue(objAlter));
        permission.setReference(getBooleanValue(objRef));
        if (dataAuthority.equalsIgnoreCase("*AUTL"))
        {
            permission.setDataAuthority("*EXCLUDE");
            permission.setFromAuthorizationList(true);
        } else
        {
            permission.setDataAuthority(dataAuthority);
        }
        return permission;
    }

    /**
     * Removes the authorized user.
     * @param objName The object the authorized user will be removed from.
     * @param userName The profile name of the authorized user.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    public void removeUser(String objName,String userName)
         throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException
    {
        CommandCall removeUser = getRmvCommand(as400_,objName,userName,followSymbolicLinks_);

        if (removeUser.run()!=true)
        {
           AS400Message[] msgList = removeUser.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;
    }

    /**
     * Returns authorized users' permissions.
     * @return a vector of authorized users' permission.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    public synchronized void setAuthority(String objName,UserPermission permission)
         throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException
    {
        CommandCall setAuthority = getChgCommand(as400_,objName,permission,followSymbolicLinks_);

        if (setAuthority.run()!=true)
        {
           AS400Message[] msgList = setAuthority.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;
    }

    /**
     * Sets authorization list of the object.
     * @param objName The object that the authorized list will be set to.
     * @param autList The authorization list that will be set.
     * @param oldValue The old authorization list will be replaced.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    public synchronized void setAuthorizationList(String objName,String autList,String oldValue)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException
    {
        CommandCall setAUTL=new CommandCall(as400_);
        String cmd="CHGAUT"
                   +" OBJ("+expandQuotes0(objName)+")"          // @B3c @B4c
                   +" AUTL("+autList+")";
        if (!followSymbolicLinks_)
        {
          cmd += " SYMLNK(*YES)";
        }
        setAUTL.setCommand(cmd);
//        setAUTL.setThreadSafe(true);  //@A2A
        if (setAUTL.run()!=true)
        {
           AS400Message[] msgList = setAUTL.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;
    }

    /**
     * Sets from authorization list of the object.
     * @param objName The object the authorized list will be set to.
     * @param fromAutl true if the permission is from the authorization list;
     * false otherwise.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    public synchronized void setFromAuthorizationList(String objName,boolean fromAutl)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException
    {
        CommandCall fromAUTL=new CommandCall(as400_);
        String cmd;
        if (fromAutl)
        {
            cmd = "CHGAUT"
                  +" OBJ("+expandQuotes0(objName)+")"                   // @B3c @B4c
                  +" USER(*PUBLIC)"
                  +" DTAAUT(*AUTL)"
                  +" OBJAUT(*NONE)";
        }
        else
        {
            cmd = "CHGAUT"
                  +" OBJ("+expandQuotes0(objName)+")"                   // @B3c @B4c
                  +" USER(*PUBLIC)"
                  +" DTAAUT(*EXCLUDE)"
                  +" OBJAUT(*NONE)";
        }
        if (!followSymbolicLinks_)
        {
          cmd += " SYMLNK(*YES)";
        }
        fromAUTL.setCommand(cmd);
//        fromAUTL.setThreadSafe(true);  //@A2A
        if (fromAUTL.run()!=true)
        {
           AS400Message[] msgList = fromAUTL.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;
    }

    /**
     * Sets the sensitivity level of the object.
     * @param objName The object that the sensitivity level will be set to.
     * @param sensitivityLevel The Sensitivity level that will be set.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    public synchronized void setSensitivity(String objName,int sensitivityLevel)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException
    {
        return;
    }

}
