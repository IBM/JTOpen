///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PermissionAccessRoot.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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
        CommandCall addUser= getChgCommand(as400_,objName,permission);

        if (addUser.run()!=true)
        {
           AS400Message[] msgList = addUser.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;

    }

    /**
     * Returns the command to change a authorized user's permission.
     * @param objName The object that the authorized information will be set to.
     * @param permission The permission will be changed.
     * @return The command to remove a authorized user.
     *
    **/
    private static CommandCall getChgCommand(AS400 sys,String objName,UserPermission permission)
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
                         +" OBJ('"+objName+"')"
                         +" USER("+userProfile+")"
                         +" DTAAUT("+dataAuthority+")"
                         +" OBJAUT("+objAuthority+")";
        CommandCall cmd = new CommandCall(sys, command); //@A2C
        cmd.setThreadSafe(true);   //@A2A
        return cmd;                //@A2C
    }

    /**
     * Returns the command to remove a authorized user.
     * @param objName The object the authorized user will be removed from.
     * @param userName The profile name of the authorized user.
     * @return The command to remove a authorized user.
     *
    **/
    private static CommandCall getRmvCommand(AS400 sys,String objName,String userName)
    {
        String dataAuthority="*NONE";
        String objAuthority="*NONE";
        String command = "CHGAUT"
                         +" OBJ('"+objName+"')"
                         +" USER("+userName+")"
                         +" DTAAUT("+dataAuthority+")"
                         +" OBJAUT("+objAuthority+")";
        CommandCall cmd = new CommandCall(sys, command); //@A2C
        cmd.setThreadSafe(true);   //@A2A
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
        if (dataAuthority.toUpperCase().equals("*AUTL"))
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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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
        CommandCall removeUser = getRmvCommand(as400_,objName,userName);

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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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
        CommandCall setAuthority = getChgCommand(as400_,objName,permission);

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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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
                   +" OBJ('"+objName+"')"
                   +" AUTL("+autList+")";
        setAUTL.setCommand(cmd);
        setAUTL.setThreadSafe(true);  //@A2A
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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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
            cmd = "CHGAUT"
                  +" OBJ('"+objName+"')"
                  +" USER(*PUBLIC)"
                  +" DTAAUT(*AUTL)"
                  +" OBJAUT(*NONE)";
        else
            cmd = "CHGAUT"
                  +" OBJ('"+objName+"')"
                  +" USER(*PUBLIC)"
                  +" DTAAUT(*EXCLUDE)"
                  +" OBJAUT(*NONE)";
        fromAUTL.setCommand(cmd);
        fromAUTL.setThreadSafe(true);  //@A2A
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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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
