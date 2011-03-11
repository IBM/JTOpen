///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: PermissionAccessDLO.java
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
 * the PermissionAccessDLO class is provided to retrieve the
 * user's permission information.
 *
**/
class PermissionAccessDLO extends PermissionAccess
{
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
        objName = toUpperCasePath(objName); //@A2C: toUpperCase()
        CommandCall addUser= getAddCommand(as400_,objName,permission);

        if (addUser.run()!=true)
        {
           AS400Message[] msgList = addUser.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;

    }


    // @B3a - New Method.
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


    // @B3a - New Method.
    /**
     * If the name contains single-quotes, doubles up the single-quotes.
     * Regardless, encloses the entire name in single-quotes.
     * This prepares the name for parsing by the IBM i Command Analyzer.
     * @param objName The name of an object.
     * @return A version of the name that is parsable by the Command Analyzer.
     *
    **/
    static String expandQuotes0(String objName)
    {  // @B4c
      StringBuffer buf = new StringBuffer(objName);
      // First, if the name contains single-quotes, double-up the quotes.
      if (objName.indexOf('\'') != -1) {
        for (int i=objName.length()-1; i>=0; i--) {
          if (buf.charAt(i) == '\'') { buf.insert(i,'\''); }
        }
      }
      // Finally, enclose the entire name in single-quotes.
      buf.insert(0,'\'');
      buf.append('\'');

      return buf.toString();
    }

    /**
     * Returns the command to add a authorized user.
     * @param objName The object the authorized user will be added to.
     * @param permission The permission of the new authorized user.
     * @return The command to add authorized user.
     *
    **/
    private static CommandCall getAddCommand(AS400 sys, String objName,UserPermission permission)
    {
        DLOPermission dloPermission = (DLOPermission)permission;
        String userProfile=dloPermission.getUserID();
        String authorityLevel=dloPermission.getDataAuthority();
        try
        {
          objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, sys.getCcsid());
        }
        catch(Exception e)
        {
          Trace.log(Trace.WARNING, "Unable to convert QDLS pathname to correct job CCSID.", e);
        }
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        String name=objName.substring(index2+1);
        String folder;
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else
            folder = "*NONE";
        String command="ADDDLOAUT"
               +" DLO("+expandQuotes0(name)+")"                    // @B3c @B4c
               +" FLR('"+folder+"')"
               +" USRAUT(("+userProfile+" "+authorityLevel+"))";
        CommandCall cmd = new CommandCall(sys, command); //@A3C
//        cmd.setThreadSafe(false); // ADDDLOAUT isn't threadsafe.  @A3A @A4C
        return cmd;
    }

    /**
     * Returns the command to change a authorized user's permission.
     * @param objName The object that the authorized information will be set to.
     * @param permission The user's permission that will be changed.
     * @return The command to change user's authority.
     *
    **/
    private static CommandCall getChgCommand(AS400 sys, String objName,UserPermission permission)
    {
        DLOPermission dloPermission = (DLOPermission)permission;
        String userProfile=dloPermission.getUserID();
        String authorityLevel=dloPermission.getDataAuthority();
        try
        {
          objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, sys.getCcsid());
        }
        catch(Exception e)
        {
          Trace.log(Trace.WARNING, "Unable to convert QDLS pathname to correct job CCSID.", e);
        }
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        String name=objName.substring(index2+1);
        if(name.equals("QDLS"))     // @1JUA
            name = "*ROOT";         // @1JUA - we are changing the public authority for the QDLS ROOT folder
        String folder;
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else
            folder = "*NONE";
        String command="CHGDLOAUT"
                +" DLO("+expandQuotes0(name)+")"                   // @B3c @B4c
                +" FLR('"+folder+"')"
                +" USRAUT(("+userProfile+" "+authorityLevel+"))";
        CommandCall cmd = new CommandCall(sys, command); //@A3C
//        cmd.setThreadSafe(false); // CHGDLOAUT isn't threadsafe.  @A3A @A4C
        return cmd;
    }

    /**
     * Returns the command to remove a authorized user.
     * @param objName The object that the authorized user will be removed from.
     * @param userName The profile name of the authorized user.
     * @return The command to remove a authorized user.
     *
    **/
    private static CommandCall getRmvCommand(AS400 sys, String objName,String userName)
    {
        String name,folder;
        try
        {
          objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, sys.getCcsid());
        }
        catch(Exception e)
        {
          Trace.log(Trace.WARNING, "Unable to convert QDLS pathname to correct job CCSID.", e);
        }
        int index1 = objName.indexOf('/',1);
        int index2=objName.lastIndexOf('/');
        name=objName.substring(index2+1);
        if (index1+1<index2)
            folder = objName.substring(index1+1,index2);
        else
            folder = "*NONE";
        String command="RMVDLOAUT"
                       +" DLO("+expandQuotes0(name)+")"                   // @B3c @B4c
                       +" FLR('"+folder+"')"
                       +" USER(("+userName+"))";
        CommandCall cmd = new CommandCall(sys, command); //@A3C
//        cmd.setThreadSafe(false); // RMVDLOAUT isn't threadsafe.  @A3A @A4C
        return cmd;
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
        if (dataAuthority.equalsIgnoreCase("*AUTL"))
        {
            permission.setFromAuthorizationList(true);
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
        objName = toUpperCasePath(objName); //@A2C: toUpperCase()
        CommandCall rmvUser = getRmvCommand(as400_,objName,userName);

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
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
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
        objName = toUpperCasePath(objName); //@A2C: toUpperCase()
        CommandCall setAuthority = getChgCommand(as400_,objName,permission);

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
        objName = toUpperCasePath(objName); //@A2A
        try
        {
          objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, as400_.getCcsid());
        }
        catch(Exception e)
        {
          Trace.log(Trace.WARNING, "Unable to convert QDLS pathname to correct job CCSID.", e);
        }
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
        if (autList.equalsIgnoreCase("*NONE"))
        {
            cmd = "RMVDLOAUT"
                  +" DLO("+expandQuotes0(name)+")"                   // @B3c @B4c
                  +" FLR('"+folder+"')"
                  +" AUTL("+oldValue+")";
        } else
        {
            cmd = "CHGDLOAUT"
                  +" DLO("+expandQuotes0(name)+")"                   // @B3c @B4c
                  +" FLR('"+folder+"')"
                  +" AUTL("+autList+")";
        }
        setAUTL.setCommand(cmd);
//        setAUTL.setThreadSafe(false); // RMV/CHGDLOAUT not threadsafe. @A3A @A4C
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
        objName = toUpperCasePath(objName); //@A2A
        try
        {
          objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, as400_.getCcsid());
        }
        catch(Exception e)
        {
          Trace.log(Trace.WARNING, "Unable to convert QDLS pathname to correct job CCSID.", e);
        }
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
                     +" DLO("+expandQuotes0(name)+")"                   // @B3c @B4c
                     +" FLR('"+folder+"')"
                     +" USRAUT((*PUBLIC *AUTL))";
        fromAUTL.setCommand(cmd);
//        fromAUTL.setThreadSafe(false); // CHGDLOAUT isn't threadsafe.  @A3A @A4C
        if (fromAUTL.run()!=true)
        {
           AS400Message[] msgList = fromAUTL.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;
    }

    /**
     * This is so we correctly convert variant QSYS characters.
    **/
    public void setPrimaryGroup(String objName, String primaryGroup, boolean revokeOldAuthority)
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
      try
      {
        objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, as400_.getCcsid());
      }
      catch(Exception e)
      {
        Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
      }
      super.setPrimaryGroup(objName, primaryGroup, revokeOldAuthority);
    }

    /**
     * This is so we correctly convert variant QSYS characters.
    **/
    public void setOwner(String objName, String owner, boolean revokeOldAuthority)
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
      try
      {
        objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, as400_.getCcsid());
      }
      catch(Exception e)
      {
        Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
      }
      super.setOwner(objName, owner, revokeOldAuthority);
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
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
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
        objName = toUpperCasePath(objName); //@A2A
        try
        {
          objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, as400_.getCcsid());
        }
        catch(Exception e)
        {
          Trace.log(Trace.WARNING, "Unable to convert QDLS pathname to correct job CCSID.", e);
        }
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
                     +" DLO("+expandQuotes0(name)+")"                   // @B3c @B4c
                     +" FLR('"+folder+"')"
                     +" SENSITIV("+sensitivity+")";
        setSensitiv.setCommand(cmd);
//        setSensitiv.setThreadSafe(false); // CHGDLOAUT isn't threadsafe.  @A3A @A4C
        if(setSensitiv.run()!=true)
        {
              AS400Message[] msgList=setSensitiv.getMessageList();
              throw new AS400Exception(msgList);
        }
        return;
    }

}
