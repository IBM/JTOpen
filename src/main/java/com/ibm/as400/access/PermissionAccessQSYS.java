///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: PermissionAccessQSYS.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
// @A1 - 02/12/2008 - Updates to process QSYS IASP objects correctly.
// @A4 - 03/01/2008 - Additional iasp updates
// @A5 - 04/24/2008 - Fix to setAuthorizationList()
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.beans.PropertyVetoException;

/**
 * Retrieves the user's permission information.
 *
**/
class PermissionAccessQSYS extends PermissionAccess
{
    /**
     * Constructs a PermissionAccessQSYS object.
     * @param system 
     *
    **/
    public PermissionAccessQSYS(AS400 system)
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
        CommandCall addUser= getAddCommand(as400_,objName,permission);

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
      // @B4d return objName;  // Note: Quotes are not allowed in QSYS object names.
      return expandQuotes0(objName);      // @B4a
    }

    // @B4a - New method.
    /**
     * If the name contains double-quotes, wraps the name in three sets of single-quotes.
     * For example, we would end up with '''The"Name'''.
     * Otherwise, simply wraps the name in single-quotes.
     * This prepares the name for parsing by the IBM i Command Analyzer.
     * @param objName The name of an object.
     * @return A version of the name that is parsable by the Command Analyzer.
     *
    **/
    static String expandQuotes0(String objName)
    {
      StringBuffer buf = new StringBuffer(objName);
      // First, enclose the entire name in single-quotes.
      buf.insert(0,'\'');
      buf.append('\'');
      if (objName.indexOf('\"') != -1) {
        // Additionally enclose the entire name in two more sets of single-quotes.
        buf.insert(0,"''");
        buf.append("''");
      }
      return buf.toString();
    }

    /**
     * Returns the command to add an authorized user.
    **/
    private static CommandCall getAddCommand(AS400 sys, String objName,UserPermission permission)
    {
        QSYSObjectPathName objectPathName = new QSYSObjectPathName(objName);
        String objectType = objectPathName.getObjectType();
        boolean isAuthList = objectType.equals("AUTL");  // @B5a
        if (!isAuthList)                                 // @B5c
            return getChgCommand(sys,objName,permission);
        QSYSPermission qsysPermission = (QSYSPermission)permission;
        String userProfile=qsysPermission.getUserID();
        String object = objectPathName.getObjectName();

        try
        {
          object = CharConverter.convertIFSQSYSPathnameToJobPathname(object, sys.getCcsid());
        }
        catch(Exception e)
        {
          if (Trace.traceOn_) {
            Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
          }
        }
        String command = "ADDAUTLE"
                         +" AUTL("+object+")"
                         +" USER("+userProfile+")"
                         +" AUT("+qsysPermission.getAuthorities(isAuthList)+")"; // @B5c
        CommandCall cmd = new CommandCall(sys, command); //@A2C
//        cmd.setThreadSafe(false); // ADDAUTLE isn't threadsafe.  @A2A @A3C
        return cmd;               //@A2C
    }

    /**
     * Returns the command to clear an authorized user's permission.
    **/
    private static CommandCall getClrCommand(AS400 sys, String objName,UserPermission permission)
    {
        QSYSObjectPathName objectPathName = new QSYSObjectPathName(objName);
        QSYSPermission qsysPermission = (QSYSPermission)permission;
        String userProfile=qsysPermission.getUserID();
        String object = objectPathName.getObjectName();
        // Determine if the object type is an AUTL which must be treated uniquely. @A1A
        String objectType = objectPathName.getObjectType(); //@A1A
        boolean isAuthList = objectType.equals("AUTL"); 

        // Set the object equal to only the "lib" if no object type @A1A
        // Set the object equal to "lib/obj" if not an AUTL         @A1A
        if (object.equals(""))                                    //@A1A
        {
          object = objectPathName.getLibraryName();               //@A1A
        }
        else                                                      //@A1A
        {
          if (!isAuthList) // Do NOT prepend LIB name for AUTL objects
          {
            object = objectPathName.getLibraryName()+"/"+object;
          }
        }
        
        if (objectType.equalsIgnoreCase("MBR"))
            objectType = "FILE";

        String command;

        try
        {
          object = CharConverter.convertIFSQSYSPathnameToJobPathname(object, sys.getCcsid());
        }
        catch(Exception e)
        {
          if (Trace.traceOn_) {
            Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
          }
        }
        if (objectType.equals("AUTL"))
        {
            command = "CHGAUTLE"
                      +" AUTL("+object+")"
                      +" USER("+userProfile+")"
                      +" AUT(*EXCLUDE)";
        } else
        {
          // Add the ASPDEV() parameter if object is on IASP      //@A1A
          String aspParm = "";                                    //@A1A                                   
          String aspName = objectPathName.getAspName();           //@A1A
          if (!aspName.equals(""))                                //@A1A
          {
            aspParm = " ASPDEV("+aspName+")";                     //@A1A
          }
          
            command="GRTOBJAUT"
                    +" OBJ("+object+")"
                    +" OBJTYPE(*"+objectType+")"
                    +" USER("+userProfile+")"
                    +aspParm                                      //@A1A
                    +" AUT(*EXCLUDE)";
        }
        CommandCall cmd = new CommandCall(sys, command); //@A2C
//        cmd.setThreadSafe(false); // CHGAUTLE,GRTOBJAUT not threadsafe.  @A2A @A3C
        return cmd;               //@A2C
    }

    /**
     * Returns the command to change an authorized user's permission.
     * @param objName The object that the authorized information will be
     *  set to.
     * @param permission The permission that will be changed.
     * @return The command to remove an authorized user.
     *
    **/
    private static CommandCall getChgCommand(AS400 sys, String objName,UserPermission permission)
    {
        QSYSObjectPathName objectPathName = new QSYSObjectPathName(objName);
        QSYSPermission qsysPermission = (QSYSPermission)permission;
        String userProfile=qsysPermission.getUserID();
        String object = objectPathName.getObjectName();
        String objectType = objectPathName.getObjectType();
        boolean isAuthList = objectType.equals("AUTL");           
        
        if (object.equals(""))                                    //@A1A
          object = "QSYS/"+objectPathName.getLibraryName();    
        else 
          if (!isAuthList) // Do NOT prepend LIB name for AUTL objects //@A1A
            object = objectPathName.getLibraryName()+"/"+object;

        if (objectType.equalsIgnoreCase("MBR"))
            objectType = "FILE";
        
        String command;
        try
        {
          object = CharConverter.convertIFSQSYSPathnameToJobPathname(object, sys.getCcsid());
        }
        catch(Exception e)
        {
          if (Trace.traceOn_) {
            Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
          }
        }
        if (objectType.equals("AUTL"))
        {
            command = "CHGAUTLE"
                      +" AUTL("+object+")"
                      +" USER("+userProfile+")"
                      +" AUT("+qsysPermission.getAuthorities(isAuthList)+")";  // @B5c
        }
        else
        {
          // Add the ASPDEV() parameter if object is on IASP      //@A1A
          String aspParm = "";                                    //@A1A
          String aspName = objectPathName.getAspName();           //@A1A
          if (!aspName.equals(""))                                //@A1A
            aspParm = " ASPDEV("+aspName+")";                     //@A1A

            command="GRTOBJAUT"
                    +" OBJ("+object+")"
                    +" OBJTYPE(*"+objectType+")"
                    +" USER("+userProfile+")"
                    +aspParm                                      //@A1A
                    +" AUT("+qsysPermission.getAuthorities(isAuthList)+")"  // @B5c
                    +" REPLACE(*YES)";
        }
        CommandCall cmd = new CommandCall(sys, command); //@A2C
//        cmd.setThreadSafe(false); // CHGAUTLE,GRTOBJAUT not threadsafe.  @A2A @A3C
        return cmd;                //@A2C
    }

    /**
     * Returns the command to remove an authorized user.
     * @param objName The object the authorized user will be removed from.
     * @param userName The profile name of the authorized user.
     * @return The command to remove an authorized user.
     *
    **/
    private static CommandCall getRmvCommand(AS400 sys, String objName,String userName, boolean followSymbolicLinks)
    {
        String name = toUpperCasePath(objName);                           // @B6a
        String asp  = null;                                               // @B6a
                                                                          // @B6a
        int locationOfQSYS = name.toUpperCase().indexOf("/QSYS.LIB");                   // @B6a
                                                                          // @B6a
        if (locationOfQSYS > 0)  // if the name starts with an ASP        // @B6a
        {                                                                 // @B6a
           asp = objName.substring(0, locationOfQSYS);                    // @B6a
           objName = objName.substring(locationOfQSYS);                   // @B6a
        }                                                                 // @B6a

        QSYSObjectPathName objectPathName = new QSYSObjectPathName(objName);
        String objectType = objectPathName.getObjectType();
        String command,object;
        if (objectType.equals("AUTL"))
        {
            object = objectPathName.getObjectName();
            try
            {
              object = CharConverter.convertIFSQSYSPathnameToJobPathname(object, sys.getCcsid());
            }
            catch(Exception e)
            {
              if (Trace.traceOn_) {
                Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
              }
            }

            command = "RMVAUTLE"
                      +" AUTL("+object+")"
                      +" USER("+userName+")";
//            threadSafe = false; // RMVAUTLE isn't threadsafe.  @A2A @A3C
        }
        else if (objectType.equals("MBR"))
        {
            if (asp != null)                                                  // @B6a
               object = asp + "/QSYS.LIB/";                                   // @B6a
            else                                                              // @B6a
            {
              // Starting slash must be there or this will be treated like @A4C 
              // a relative path of the users working directory            @A4C
              object = "/QSYS.LIB/";                                     //@A4C
            }

            if (!objectPathName.getLibraryName().equals(""))
                object += objectPathName.getLibraryName()+".LIB/";
            object += objectPathName.getObjectName()+".FILE";

// The CHGAUT command doesn't need the path converted either, for some reason.
/*            try
            {
              object = CharConverter.convertIFSQSYSPathnameToJobPathname(object, sys.getCcsid());
            }
            catch(Exception e)
            {
              Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
            }
*/
            command="CHGAUT"
                    +" OBJ("+expandQuotes0(object)+")"       // @B4c
                    +" USER("+userName+")"
                    +" DTAAUT(*NONE)"
                    +" OBJAUT(*NONE)";
            if (!followSymbolicLinks)
            {
              command += " SYMLNK(*YES)";
            }
//            threadSafe = true; //@A2A
        }
        else
        {
            String localName = objName;                      // @B6a

            if (asp != null)                                 // @B6a
               localName = asp + localName;                  // @B6a

/*            try
            {
              localName = CharConverter.convertIFSQSYSPathnameToJobPathname(localName, sys.getCcsid());
            }
            catch(Exception e)
            {
              Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
            }
*/
            command="CHGAUT"
                    +" OBJ(" + expandQuotes0(localName) + ")"      // @B4c @B6c
                    +" USER("+userName+")"
                    +" DTAAUT(*NONE)"
                    +" OBJAUT(*NONE)";
            if (!followSymbolicLinks)
            {
              command += " SYMLNK(*YES)";
            }
//            threadSafe = true; //@A2A
        }

        CommandCall cmd = new CommandCall(sys, command); //@A2C
//        cmd.setThreadSafe(threadSafe);  //@A2A
        return cmd;                     //@A2C
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
        //String reserved1=((String)userRecord.getField("reserved1")).trim();
        String objOperational=((String)userRecord.getField("objOperational")).trim();
        String dataRead=((String)userRecord.getField("dataRead")).trim();
        String dataAdd=((String)userRecord.getField("dataAdd")).trim();
        String dataUpdate=((String)userRecord.getField("dataUpdate")).trim();
        String dataDelete=((String)userRecord.getField("dataDelete")).trim();
        String dataExecute=((String)userRecord.getField("dataExecute")).trim();
        String reserved2=((String)userRecord.getField("reserved2")).trim();
        QSYSPermission permission;
        permission =new QSYSPermission(profileName);
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
        CommandCall removeUser = getRmvCommand(as400_,objName,userName,followSymbolicLinks_);

        if (removeUser.run()!=true)
        {
           AS400Message[] msgList = removeUser.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;
    }

    /**
     * Sets the permission of a user for an object.
     * @param objName The object that the user permission will be set to.
     * @param permission The permission of the authorized user.
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
//        CommandCall setAuthority = getClrCommand(as400_,objName,permission);
//        if (setAuthority.run()!=true)
//        {
//           AS400Message[] msgList = setAuthority.getMessageList();
//           throw new AS400Exception(msgList);
//        }

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
     * @param objName The object that the authorization list will be set to.
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
        QSYSObjectPathName objectPathName = new QSYSObjectPathName(objName);
        String object = objectPathName.getObjectName();
        
        if (object.equals(""))                                  //@A5A
          object = "QSYS/"+objectPathName.getLibraryName();     //@A5A
        else                                                    //@A5A
          object = objectPathName.getLibraryName()+"/"+object;  //@A5A
        
        String objectType = objectPathName.getObjectType();
        if (objectType.trim().equalsIgnoreCase("MBR"))
            objectType = "FILE";

        CommandCall setAUTL=new CommandCall(as400_);
        String cmd;
        try
        {
          object = CharConverter.convertIFSQSYSPathnameToJobPathname(object, as400_.getCcsid());
        }
        catch(Exception e)
        {
          if (Trace.traceOn_) {
            Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
          }
        }
        // Add the ASPDEV() parameter if object is on IASP        //@A1A
        String aspParm = "";                                      //@A1A
        String aspName = objectPathName.getAspName();             //@A1A
        if (!aspName.equals(""))                                  //@A1A
          aspParm = " ASPDEV("+aspName+")";                       //@A1A
        if (!oldValue.equalsIgnoreCase("*NONE")&&
            autList.equalsIgnoreCase("*NONE"))
        {
            cmd = "RVKOBJAUT"
                  +" OBJ("+object+")"
                  +" OBJTYPE(*"+objectType+")"
                  +aspParm                                        //@A1A
                  +" AUTL("+oldValue+")";
        } else
        {

            cmd = "GRTOBJAUT"
                  +" OBJ("+object+")"
                  +" OBJTYPE(*"+objectType+")"
                  +aspParm                                        //@A1A
                  +" AUTL("+autList+")";
        }
        setAUTL.setCommand(cmd);
//        setAUTL.setThreadSafe(false); // RVKOBJAUT,GRTOBJAUT not threadsafe.  @A2A @A3C
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
        QSYSObjectPathName objectPathName = new QSYSObjectPathName(objName);
        String object = objectPathName.getObjectName();

        if (object.equals(""))                                  //@A5A
          object = "QSYS/"+objectPathName.getLibraryName();     //@A5A
        else                                                    //@A5A
          object = objectPathName.getLibraryName()+"/"+object;  //@A5A

        String objectType = objectPathName.getObjectType();
        if (objectType.equalsIgnoreCase("MBR"))
            objectType = "FILE";

        CommandCall fromAUTL=new CommandCall(as400_);
        String cmd;
        try
        {
          object = CharConverter.convertIFSQSYSPathnameToJobPathname(object, as400_.getCcsid());
        }
        catch(Exception e)
        {
          if (Trace.traceOn_) {
            Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
          }
        }
        // Add the ASPDEV() parameter if object is on IASP        //@A1A
        String aspParm = "";                                      //@A1A
        String aspName = objectPathName.getAspName();             //@A1A
        if (!aspName.equals(""))                                  //@A1A
          aspParm = " ASPDEV("+aspName+")";                       //@A1A
        if (fromAutl)
            cmd = "GRTOBJAUT"
                  +" OBJ("+object+")"
                  +" OBJTYPE(*"+objectType+")"
                  +" USER(*PUBLIC)"
                  +aspParm                                        //@A1A
                  +" AUT(*AUTL)";
        else
        {
            cmd = "GRTOBJAUT"
                  +" OBJ("+object+")"
                  +" OBJTYPE(*"+objectType+")"
                  +" USER(*PUBLIC)"
                  +aspParm                                        //@A1A
                  +" AUT(*EXCLUDE)";
        }
        fromAUTL.setCommand(cmd);
//        fromAUTL.setThreadSafe(false); // GRTOBJAUT isn't threadsafe.  @A2A @A3C
        if (fromAUTL.run()!=true)
        {
           AS400Message[] msgList = fromAUTL.getMessageList();
           throw new AS400Exception(msgList);
        }
        return;
    }


// For some reason, CHGOWN doesn't need the path name converted, but the other CL
// commands do. So we don't need to override this.
//
//    /**
//     * This is so we correctly convert variant QSYS characters.
//    **/
//    public void setOwner(String objName, String owner, boolean revokeOldAuthority)
//    throws AS400Exception,
//           AS400SecurityException,
//           ConnectionDroppedException,
//           ErrorCompletingRequestException,
//           InterruptedException,
//           IOException,
//           ServerStartupException,
//           UnknownHostException,
//           PropertyVetoException
//    {
//      try
//      {
//        objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, as400_.getCcsid());
//      }
//      catch(Exception e)
//      {
//        Trace.log(Trace.WARNING, "Unable to convert CL command to correct job CCSID.", e);
//      }
//      super.setOwner(objName, owner, revokeOldAuthority);
//    }

    /**
     * Sets the sensitivity level of the object.
     * @param objName The object that the sensitivity level will be set to.
     * @param sensitivityLevel The sensitivity level that will be set.
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

