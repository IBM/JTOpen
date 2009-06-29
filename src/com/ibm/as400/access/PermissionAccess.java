///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: PermissionAccess.java
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
import java.util.Vector;

/**
 * The PermissionAccess class is provided to retrieve the user's
 * permission information.
 *
**/
abstract class PermissionAccess
{
    AS400 as400_;
    private int ccsid_;         // @A4A
    private boolean gotCcsid_;  // @A4A

    //Default receiver length.
    private static final int DEFAULT_LENGTH=600;

    protected boolean followSymbolicLinks_ = true;

    /**
     * Constructs a PermissionAccess object.
     *
    **/
    public PermissionAccess(AS400 system)
    {
        if (system==null)
        {
            throw new NullPointerException("system");
        }
        as400_=system;

    }

    /**
     * Adds the authorized user or the UserPermission.
     * @param objName The object the authorized user will be added to.
     * @param permission The permission of the new authorized user.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the host server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    public abstract void addUser(String objName,UserPermission permission)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   PropertyVetoException,
                   UnknownHostException;


    // @B3a - New Method.
    /**
     * Prepares the object name for parsing by the IBM i Command Analyzer.
     * @param objName The name of an object.
     * @return A version of the name that is parsable by the Command Analyzer.
     *
    **/
    protected abstract String expandQuotes(String objName);

    /**
     * Returns the server
     * @return The server object.
     * @see #setSystem
     *
    **/
    public AS400 getSystem()
    {
        return as400_;
    }

    /**
     * Returns authorized users' permissions.
     * @return A vector of authorized users' permission.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception ObjectDoesNotExistException If the server object does not exist.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    public Vector getAuthority(String objName)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException,
                   PropertyVetoException
    {
        // @B6 Note -- objName is an IFS-style name which is what
        //     the QSYRTVUS pgm requires.  For objects in QSYS, the name
        //     is "/QSYS.LIB/...".  If the object is on an ASP, the asp name
        //     must be prepended to the path (/aspName/QSYS.LIB/...).  Our
        //     caller must correctly build the name.

        // The vector store the information retrieved from system.
        Vector vector=new Vector();

        // Data contains the feedback information.
        byte[] feedbackData;

        // Data contains the users' permissions information.
        byte[] usersData;

        //Record format.
        RecordFormat recordFormat;

        //Record format for user.
        RecordFormat userRecordFormat;

        // Constructs ProgramParameters and ProgramCall.
        QSYSObjectPathName prgName=new QSYSObjectPathName("QSYS","QSYRTVUA","PGM");

        int vrm = as400_.getVRM();
        ProgramParameter[] parmList= getParameters(DEFAULT_LENGTH, objName, vrm >= 0x050300);

        ProgramCall rtvUsersAUT=new ProgramCall(as400_);
        rtvUsersAUT.setProgram(prgName.getPath(),parmList);
//        rtvUsersAUT.setThreadSafe(false); // API isn't threadsafe as of V4R4. @A5A

        if (rtvUsersAUT.run()!=true)
        {
            // If any error message return.
            AS400Message[] msgList = rtvUsersAUT.getMessageList();
            throw new AS400Exception(msgList);
        }
        else
        {
            // Gets returned data.
            feedbackData=parmList[2].getOutputData();
            usersData = parmList[0].getOutputData();
        }

        // Gets format of returned records feedback information.
        recordFormat=getFeedbackRecordFormat();

        // Gets the record contains fields.
        Record record0=new Record(recordFormat);

        // Sets the contents of this record from the specified byte array.
        record0.setContents(feedbackData);

        Integer bytesAvailable=(Integer)record0.getField("BytesAvailable");
        Integer bytesReturn=(Integer)record0.getField("BytesReturn");
        int requiredLength=bytesAvailable.intValue();
        int receiverLength=bytesReturn.intValue();

        if(requiredLength>receiverLength)
        {
            // If there is not enough space provided, retrieve data again.
            parmList = getParameters(requiredLength+400, objName, vrm >= 0x050300);
            rtvUsersAUT.setProgram(prgName.getPath(),parmList);
            if (rtvUsersAUT.run()!=true)
            {
                AS400Message[] msgList = rtvUsersAUT.getMessageList();
                throw new AS400Exception(msgList);
            }
            else
            {
                // Gets returned data.
                feedbackData=parmList[2].getOutputData();
                usersData = parmList[0].getOutputData();
                // Sets the contents of feedback record again.
                record0.setContents(feedbackData);
            }
        }

        // Gets the values of the fields in the record.
        String owner=((String)record0.getField("owner")).trim();
        String primaryGroup=((String)record0.getField("primaryGroup")).trim();
        String authorizationList=((String)record0.getField("authorizationList")).trim();
        String sensitivityLev=((String)record0.getField("sensitivityLevel")).trim();
        Integer sensitivityLevel=new Integer(getIntValue(sensitivityLev));

        // Adds information to vector.
        vector.addElement(owner);
        vector.addElement(primaryGroup);
        vector.addElement(authorizationList);
        vector.addElement(sensitivityLevel);

        Integer usersNumber=(Integer)record0.getField("usersNumber");
        Integer userEntryLength=(Integer)record0.getField("userEntryLength");
        int totalUsers=usersNumber.intValue();
        int length=userEntryLength.intValue();

        // Gets the information record format for each user.
        userRecordFormat=getUserRecordFormat();

        // Gets the information for each user.
        for(int i=0;i<totalUsers;i++)
        {
            UserPermission permission;
            Record userRecord;
            userRecord=userRecordFormat.getNewRecord(usersData,i*length);

            // Constructs user permission.
            permission=getUserPermission(userRecord);

            // Adds information to vector.
            vector.addElement(permission);
        }

        return vector;

    }

    /**
     * Converts the return value's type from String to boolean.
    **/
    boolean getBooleanValue(String string)
    {
        if(string.equals("1"))
             return true;
        else
             return false;
    }

    // @A4A
    /**
     * Gets the CCSID for the AS400.
     *
    **/
    private int getCcsid()
    {
      if (!gotCcsid_) {
        ccsid_ = as400_.getCcsid();
        gotCcsid_ = true;
      }
      return ccsid_;
    }

    /**
     * Returns the RecordFormat of the feedback informations.
     * @return The RecordFormat of the feedback informations.
     *
    **/
    private RecordFormat getFeedbackRecordFormat()
    {
        BinaryFieldDescription[] bfd;
        bfd=new BinaryFieldDescription[6];
        AS400Bin4 bin4 = new AS400Bin4(); //@A2A
        AS400Text text10 = new AS400Text(10, getCcsid(), as400_); //@A2C
        bfd[0]=new BinaryFieldDescription(bin4,"fbBytesReturn"); //@A2C
        bfd[1]=new BinaryFieldDescription(bin4,"fbBytesAvailable"); //@A2C
        bfd[2]=new BinaryFieldDescription(bin4,"BytesReturn"); //@A2C
        bfd[3]=new BinaryFieldDescription(bin4,"BytesAvailable"); //@A2C
        bfd[4]=new BinaryFieldDescription(bin4,"usersNumber"); //@A2C
        bfd[5]=new BinaryFieldDescription(bin4,"userEntryLength"); //@A2C
        CharacterFieldDescription[] cfd;
        cfd=new CharacterFieldDescription[4];
        cfd[0]=new CharacterFieldDescription(text10,"owner"); //@A2C
        cfd[1]=new CharacterFieldDescription(text10,"primaryGroup"); //@A2C
        cfd[2]=new CharacterFieldDescription(text10,"authorizationList"); //@A2C
        cfd[3]=new CharacterFieldDescription(new AS400Text(1, getCcsid(), as400_),"sensitivityLevel"); //@A2C
        RecordFormat rf0=new RecordFormat();
        for(int i=0;i<6;i++)
            rf0.addFieldDescription(bfd[i]);
        for(int i=0;i<4;i++)
            rf0.addFieldDescription(cfd[i]);
        return rf0;
    }

    /**
     * Converts the return value's type from String to int.
     *
    **/
    int getIntValue(String string)
    {
        return Integer.parseInt(string);
    }

    /**
     * Returns the program parameters of the program call.
     * @return The program parameters of the program call.
     *
    **/
    ProgramParameter[] getParameters(int length, String objName, boolean useUnicode)
      throws UnsupportedEncodingException
    {
        final int numParms = (followSymbolicLinks_ ? 8 : 9); // to not follow links, need extra parm
        ProgramParameter[] parmList=new ProgramParameter[numParms];

        parmList[0]=new ProgramParameter(length);

        parmList[1]=new ProgramParameter(BinaryConverter.intToByteArray(length));

        parmList[2]=new ProgramParameter(55);

        parmList[3]=new ProgramParameter(BinaryConverter.intToByteArray(55));

        AS400Text text8 = new AS400Text(8, getCcsid(), as400_); //@A2C
        parmList[4]=new ProgramParameter(text8.toBytes("RTUA0100"));

        if (!useUnicode)
        {
          // Old way.
          if (Trace.traceOn_) {
            Trace.log(Trace.DIAGNOSTIC, "PermissionAccess creating QSYRTVUA parameters using job CCSID.");
          }

          //@A3A: Need to use uppercase name if it is a DLO object because
          // ccsid 5026 currently doesn't convert a lowercase name to an
          // uppercase one. Normally, case shouldn't matter since
          // the server uppercases any QDLS names for us automatically.
          if (objName.toUpperCase().startsWith("/QDLS/"))      //@A3A
          {
            objName = objName.toUpperCase();                   //@A3A
            try
            {
              objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, as400_.getCcsid());
            }
            catch(Exception e)
            {
              if (Trace.traceOn_) {
                Trace.log(Trace.WARNING, "Unable to convert QDLS pathname to correct job CCSID.", e);
              }
            }
          }
          byte[] objnameBytes = CharConverter.stringToByteArray(getCcsid(), as400_, objName);
          parmList[5]=new ProgramParameter(objnameBytes);

          parmList[6]=new ProgramParameter(BinaryConverter.intToByteArray(objnameBytes.length));
        }
        else
        {
          // New way (V5R3 and higher).
          if (Trace.traceOn_) {
            Trace.log(Trace.DIAGNOSTIC, "PermissionAccess creating QSYRTVUA parameters using UTF-16 (CCSID 1200).");
          }
          // This allows us to pass a Unicode path so we can access permissions
          // for objects that have characters that aren't in our job CCSID. Hurray!
          // We use CCSID 1200 for UTF-16.
          String upperName = objName.toUpperCase();
          if (upperName.startsWith("/QDLS/"))
          {
            objName = upperName;
            try
            {
              objName = CharConverter.convertIFSQSYSPathnameToJobPathname(objName, as400_.getCcsid());
            }
            catch(Exception e)
            {
              if (Trace.traceOn_) {
                Trace.log(Trace.WARNING, "Unable to convert QDLS pathname to correct job CCSID.", e);
              }
            }
          }
          byte[] pathNameBytes = null;
          try
          {
            pathNameBytes = CharConverter.stringToByteArray(1200, objName);
          }
          catch(UnsupportedEncodingException uee)
          {
            if (Trace.traceOn_) {
              Trace.log(Trace.WARNING, "PermissionAccess could not load converter table for CCSID 1200. Manually converting path name.");
            }
            int pathLen = objName.length();
            pathNameBytes = new byte[pathLen*2]; // CCSID 1200: 2 bytes per char
            for (int bc=0; bc<pathLen; ++bc)
            {
              char pathChar = objName.charAt(bc);
              pathNameBytes[bc*2] = (byte)(pathChar >> 8);
              pathNameBytes[bc*2+1] = (byte)(pathChar);
            }
          }

          byte[] qlgPathNameTStructure = new byte[32 + pathNameBytes.length];
          BinaryConverter.intToByteArray(1200, qlgPathNameTStructure, 0); // CCSID
          // 2-byte country or region ID... x0000 = use current job settings
          // 3-byte language ID... 0x000000 = use current job settings
          // 3 bytes reserved
          BinaryConverter.intToByteArray(2, qlgPathNameTStructure, 12); // path type indicator: 2 means pathname is a character string and has a two-byte path delimiter
          BinaryConverter.intToByteArray(pathNameBytes.length, qlgPathNameTStructure, 16); // length of path name
          char delimiter = '/'; // path name delimiter
          qlgPathNameTStructure[20] = (byte)(delimiter >> 8); // high-byte
          qlgPathNameTStructure[21] = (byte)delimiter; // low-byte
          // 10 bytes reserved
          System.arraycopy(pathNameBytes, 0, qlgPathNameTStructure, 32, pathNameBytes.length); // path name

          parmList[5] = new ProgramParameter(qlgPathNameTStructure);
          parmList[6] = new ProgramParameter(BinaryConverter.intToByteArray(-1));
        }

        byte[] errorInfo = new byte[32];
        parmList[7] = new ProgramParameter( errorInfo, 0 );

        // If the caller wants to retrieve attributes for the link itself,
        // specify optional parameter "Symbolic link" as "*YES".
        if (!followSymbolicLinks_)
        {
          if (Trace.traceOn_) {
            Trace.log(Trace.DIAGNOSTIC, "Adding 'Symbolic link: *YES' parameter for QSYRTVUA.");
          }
          AS400Text text10 = new AS400Text(10, getCcsid(), as400_);
          parmList[8]= new ProgramParameter(text10.toBytes("*YES")); // default is *NO
        }

        return parmList;
    }


    /**
     * Returns the record format of the user's permission.
     * @return The record format of the user's permission.
     *
    **/
    RecordFormat getUserRecordFormat()
    {
        CharacterFieldDescription[] cfd;
        cfd=new CharacterFieldDescription[16];
        AS400Text text1 = new AS400Text(1, getCcsid(), as400_); //@A2A
        AS400Text text10 = new AS400Text(10, getCcsid(), as400_); //@A2A
        cfd[0]  =new CharacterFieldDescription(text10,"profileName"); //@A2C
        cfd[1]  =new CharacterFieldDescription(text1,"userOrGroup"); //@A2C
        cfd[2]  =new CharacterFieldDescription(text10,"dataAuthority"); //@A2C
        cfd[3]  =new CharacterFieldDescription(text1,"autListMgt"); //@A2C
        cfd[4]  =new CharacterFieldDescription(text1,"objMgt"); //@A2C
        cfd[5]  =new CharacterFieldDescription(text1,"objExistence"); //@A2C
        cfd[6]  =new CharacterFieldDescription(text1,"objAlter"); //@A2C
        cfd[7]  =new CharacterFieldDescription(text1,"objRef"); //@A2C
        cfd[8]  =new CharacterFieldDescription(text10,"reserved1"); //@A2C
        cfd[9]  =new CharacterFieldDescription(text1,"objOperational"); //@A2C
        cfd[10] =new CharacterFieldDescription(text1,"dataRead"); //@A2C
        cfd[11] =new CharacterFieldDescription(text1,"dataAdd"); //@A2C
        cfd[12] =new CharacterFieldDescription(text1,"dataUpdate"); //@A2C
        cfd[13] =new CharacterFieldDescription(text1,"dataDelete"); //@A2C
        cfd[14] =new CharacterFieldDescription(text1,"dataExecute"); //@A2C
        cfd[15] =new CharacterFieldDescription(text10,"reserved2"); //@A2C
        RecordFormat userrf=new RecordFormat();
        for(int i=0;i<16;i++)
            userrf.addFieldDescription(cfd[i]);
        return userrf;
    }

    /**
     * Returns the user's permission retrieved from the system.
     * @return The user's permission retrieved from the system.
     * @exception UnsupportedEncodingException The Character Encoding is not supported.
     *
    **/
    abstract public UserPermission getUserPermission(Record userRecord)
         throws UnsupportedEncodingException;


    /**
     * Returns whether symbolic links are resolved when changing or retrieving permissions.
     * @return Whether symbolic links are resolved.
     * @see #setFollowSymbolicLinks
     *
    **/
    public boolean isFollowSymbolicLinks()
    {
      return followSymbolicLinks_;
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
     * @exception ServerStartupException If the host server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    abstract public void removeUser(String objName,String userName)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException;

    /**
     * Sets authorized information.
     * @param objName The object the authorized information will be set to.
     * @param permission The permission will be set.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the host server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    abstract public void setAuthority(String objName,UserPermission permission)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException;

    /**
     * Sets authorization list of the object.
     * @param objName The object the authorized list will be set to.
     * @param autList The authorization list will be set.
     * @param oldValue The old authorization list will be replaced.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the host server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    abstract public void setAuthorizationList(String objName,String autList,String oldValue)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException;

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
     * @exception ServerStartupException If the host server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    abstract public void setFromAuthorizationList(String objName,boolean fromAutl)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException;

    // @B2a
    /**
     * Sets the owner of the object.
     * @param objName The object whose ownership is being reset.
     * @param owner The owner of the object.
     * @param revokeOldAuthority Specifies whether the authorities for the current
     * owner are revoked when ownership is transferred to the new owner.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the host server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
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
      // @B6 Note -- objName is an IFS-style name which is what
      //     the CHGOWN command requires.  For objects in QSYS, the name
      //     is "/QSYS.LIB/...".  If the object is on an ASP, the asp name
      //     must be prepended to the path (/aspName/QSYS.LIB/...).  Our
      //     caller must correctly build the name.

      objName = objName.toUpperCase();
      CommandCall cmd = new CommandCall(as400_);
      String revokeOldAut;
      if (revokeOldAuthority) revokeOldAut = "*YES";
      else                    revokeOldAut = "*NO";
      String cmdString = "CHGOWN " +
        "OBJ("+expandQuotes(objName)+") " +                 // @B3c @B4c
        "NEWOWN("+owner+") " +
        "RVKOLDAUT("+revokeOldAut+")";
      if (!followSymbolicLinks_)
      {
        cmdString += " SYMLNK(*YES)";
      }
      cmd.setCommand(cmdString);
//      cmd.setThreadSafe(false); // CHGOWN isn't threadsafe.
      if(cmd.run()!=true)
      {
        AS400Message[] msgList=cmd.getMessageList();
        throw new AS400Exception(msgList);
      }
    }


    /**
     * Sets the primary group of the object.
     * @param objName The object whose primary group is being reset.
     * @param primaryGroup The primary group.
     * @param revokeOldAuthority Specifies whether the authorities for the current
     * primary group are revoked when the primary group is changed to the new value.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the host server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
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
      // Note -- objName is an IFS-style name which is what
      //     the CHGPGP command requires.  For objects in QSYS, the name
      //     is "/QSYS.LIB/...".  If the object is on an ASP, the asp name
      //     must be prepended to the path (/aspName/QSYS.LIB/...).  Our
      //     caller must correctly build the name.

      objName = objName.toUpperCase();
      CommandCall cmd = new CommandCall(as400_);
      String revokeOldAut;
      if (revokeOldAuthority) revokeOldAut = "*YES";
      else                    revokeOldAut = "*NO";
      String cmdString = "CHGPGP " +
        "OBJ("+expandQuotes(objName)+") " +
        "NEWPGP("+primaryGroup+") " +
        "RVKOLDAUT("+revokeOldAut+")";
      cmd.setCommand(cmdString);
//      cmd.setThreadSafe(false); // CHGPGP isn't threadsafe.
      if(cmd.run()!=true)
      {
        AS400Message[] msgList=cmd.getMessageList();
        throw new AS400Exception(msgList);
      }
    }


    /**
     * Sets whether to resolve symbolic links when changing or retrieving permissions.
     * The default value is true; that is, symbolic links are always resolved.
     * By default, if the IBM i object is a symbolic link, then the requested action
     * is performed on the object ultimately <em>pointed to</em> by the symbolic link,
     * rather than on the symbolic link itself.
     * <br>Note: This method is effective only for IBM i release V5R4 and higher.
     * For earlier releases, symbolic links are always resolved, and this method is ignored.
     * @param followLinks Whether symbolic links are resolved.
     * @see #isFollowSymbolicLinks
     *
    **/
    public void setFollowSymbolicLinks(boolean followLinks)
    {
      // Assume that the caller has already verified that we're running to V5R4 or higher.
      // Note to programmer: If this class ever becomes public, add a VRM check here,
      // as in Permission.setFollowSymbolicLinks().
      followSymbolicLinks_ = followLinks;
    }


    /**
     * Sets the sensitivity level of the object.
     * @param objName The object the sensitivity level will be set to.
     * @param sensitivityLevel The sensitivity level.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception ServerStartupException If the host server cannot be started.
     * @exception UnknownHostException If the server cannot be located.
     *
    **/
    abstract public void setSensitivity(String objName,int sensitivityLevel)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ServerStartupException,
                   UnknownHostException,
                   PropertyVetoException;

    /**
     * Sets the system where object authority information resides.
     *
     * @param   system The server object.
     * @see     #getSystem
    **/
    public void setSystem(AS400 system)
    {
        if (system == null)
        {
            throw new NullPointerException("system");
        }
        as400_ = system;
    }

}


















