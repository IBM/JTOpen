///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PermissionAccessDLO.java
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
 * the PermissionAccessDLO class is provided to retrieve the 
 * user's permission information.
 * 
**/
class PermissionAccessDLO extends PermissionAccess
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    /**
     * Constructs a PermissionAccessDLO object.   
     *
    **/
    public PermissionAccessDLO(AS400 system)
    {
        super(system);
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
        CommandCall addUser= new CommandCall(as400_);
        String cmd = getAddCommand(objName.toUpperCase(),permission); //@A2C: toUpperCase()
        addUser.setCommand(cmd);

        if (addUser.run()!=true)
        {
           AS400Message[] msgList = addUser.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;    
        
    } 

    /**
     * Returns the command to add a authorized user.
     * @param objName The object the authorized user will be added to.
     * @param permission The permission of the new authorized user.
     * @return The command to add authorized user.
     *
    **/
    private String getAddCommand(String objName,UserPermission permission)
    {
        String name,folder,command;
        DLOPermission dloPermission = (DLOPermission)permission;
        String userProfile=dloPermission.getUserID();
        String authorityLevel=dloPermission.getDataAuthority();
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        name=objName.substring(index2+1);
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else 
            folder = "*NONE";
        command="ADDDLOAUT"
               +" DLO('"+name+"')"
               +" FLR('"+folder+"')"
               +" USRAUT(("+userProfile+" "+authorityLevel+"))";
        return command;
    }

    /**
     * Returns the command to change a authorized user's permission.
     * @param objName The object that the authorized information will be set to.
     * @param permission The user's permission that will be changed.
     * @return The command to change user's authority.
     *
    **/
    private String getChgCommand(String objName,UserPermission permission)
    {
        DLOPermission dloPermission = (DLOPermission)permission;
        String name,folder,command;
        String userProfile=dloPermission.getUserID();
        String authorityLevel=dloPermission.getDataAuthority();
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        name=objName.substring(index2+1);
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else 
            folder = "*NONE";
        command="CHGDLOAUT"
                +" DLO('"+name+"')"
                +" FLR('"+folder+"')"
                +" USRAUT(("+userProfile+" "+authorityLevel+"))";
        return command;
    }

    /** 
     * Returns the copyright.
    **/
    private static String getCopyright()
    {
        return Copyright.copyright;
    }
    
    /**
     * Returns the command to remove a authorized user.
     * @param objName The object that the authorized user will be removed from.
     * @param userName The profile name of the authorized user.
     * @return The command to remove a authorized user.
     *
    **/
    private String getRmvCommand(String objName,String userName)
    {
        String name,folder;
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        name=objName.substring(index2+1);
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else 
            folder = "*NONE";
        String command="RMVDLOAUT"
                       +" DLO('"+name+"')"
                       +" FLR('"+folder+"')"
                       +" USER(("+userName+"))";
        return command;
    } 

    /**
     * Returns the user's permission retrieved from the system. 
     * @return The user's permission retrieved from the system. 
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
        String objOperational=((String)userRecord.getField("objOperational")).trim();
        String dataRead=((String)userRecord.getField("dataRead")).trim();
        String dataAdd=((String)userRecord.getField("dataAdd")).trim();
        String dataUpdate=((String)userRecord.getField("dataUpdate")).trim();
        String dataDelete=((String)userRecord.getField("dataDelete")).trim();
        String dataExecute=((String)userRecord.getField("dataExecute")).trim();
        DLOPermission permission;
        permission =new DLOPermission(profileName);
        permission.setGroupIndicator(getIntValue(userOrGroup));

        permission.setAuthorizationListManagement(getBooleanValue(autListMgt));
        permission.setManagement(getBooleanValue(objMgt));
        permission.setExistence(getBooleanValue(objExistence));
        permission.setAlter(getBooleanValue(objAlter));
        permission.setReference(getBooleanValue(objRef));
        permission.setOperational(getBooleanValue(objOperational));
        permission.setRead(getBooleanValue(dataRead));
        permission.setAdd(getBooleanValue(dataAdd));
        permission.setUpdate(getBooleanValue(dataUpdate));
        permission.setDelete(getBooleanValue(dataDelete));
        permission.setExecute(getBooleanValue(dataExecute));
        if (dataAuthority.toUpperCase().equals("*AUTL"))
        {
            permission.setFromAuthorizationList(true);
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
        CommandCall rmvUser = new CommandCall(as400_);
        String cmd = getRmvCommand(objName.toUpperCase(),userName); //@A2C: toUpperCase()
        rmvUser.setCommand(cmd);

        if (rmvUser.run()!=true)
        {
           AS400Message[] msgList = rmvUser.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;    
        
    } 

    /**
     * Sets the authorized user's permissions.
     * @return The UserPermission object.
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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
        CommandCall setAuthority = new CommandCall(as400_);
        String cmd = getChgCommand(objName.toUpperCase(),permission); //@A2C: toUpperCase()
        setAuthority.setCommand(cmd);

        if (setAuthority.run()!=true)
        {
           AS400Message[] msgList = setAuthority.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;    
        
    } 

    /**
     * Sets the authorization list of the object.
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
        objName = objName.toUpperCase(); //@A2A
        String name,folder;
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        name=objName.substring(index2+1);
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else 
            folder = "*NONE";
        CommandCall setAUTL=new CommandCall(as400_);
        String cmd;
        if (autList.toUpperCase().equals("*NONE"))
        {
            cmd = "RMVDLOAUT"
                  +" DLO('"+name+"')"
                  +" FLR('"+folder+"')"
                  +" AUTL("+oldValue+")";
        } else 
        {
            cmd = "CHGDLOAUT"
                  +" DLO('"+name+"')"
                  +" FLR('"+folder+"')"
                  +" AUTL("+autList+")";
        }
        setAUTL.setCommand(cmd);
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
        objName = objName.toUpperCase(); //@A2A
        String name,folder;
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        name=objName.substring(index2+1);
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else 
            folder = "*NONE";
        CommandCall fromAUTL=new CommandCall(as400_);
        String cmd = "CHGDLOAUT"
                     +" DLO('"+name+"')"
                     +" FLR('"+folder+"')"
                     +" USRAUT((*PUBLIC *AUTL))";
        fromAUTL.setCommand(cmd);
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
     * @param sensitivityLevel The sensitivity level that will be set.
     * Possible values : 
     * <UL>
     *  <LI> *NONE - The object has no sensitivity restrictions.
     *  <LI> *PERSONAL - The object contains information intended for the user as an individual.
     *  <LI> *PRIVATE - The object contains information that should be accessed only by the owner.
     *  <LI> *CONFIDENTIAL - The object contains information that should be handled according to company procedures.
     * </UL>
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the AS/400 server cannot be started.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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
        objName = objName.toUpperCase(); //@A2A
        String name,folder;
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        name=objName.substring(index2+1);
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else 
            folder = "*NONE";
        CommandCall setSensitiv=new CommandCall(as400_);
        String sensitivity="";
        switch(sensitivityLevel)
        {
            case 1:
                sensitivity="*NONE";
                break;
            case 2:
                sensitivity="*PERSONAL";
                break;
            case 3:
                sensitivity="*PRIVATE";
                break;
            case 4:
                sensitivity="*CONFIDENTIAL";
                break;
            default :
                throw new ExtendedIllegalArgumentException("sensitivity",
                    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }            
        String cmd = "CHGDLOAUT"
                     +" DLO('"+name+"')"
                     +" FLR('"+folder+"')"
                     +" SENSITIV("+sensitivity+")";
        setSensitiv.setCommand(cmd);
        if(setSensitiv.run()!=true)
        {
              AS400Message[] msgList=setSensitiv.getMessageList();
              throw new AS400Exception(msgList);
        }
        return;
    } 

}    
