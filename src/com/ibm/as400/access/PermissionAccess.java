///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PermissionAccess.java
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
import java.util.Vector;

/**
 * The PermissionAccess class is provided to retrieve the user's 
 * permission information.
 * 
**/
abstract class PermissionAccess 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    //AS400 object.
    AS400 as400_;
    private int ccsid_;         // @A4A
    private boolean gotCcsid_;  // @A4A

    //Default receiver length.
    private final int defaultLength_=600;

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
                   
    /**
     * Returns the AS/400 system
     * @return The AS/400 system object. 
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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ConnectionDroppedException If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicationg with the AS/400.
     * @exception ObjectDoesNotExistException If the AS/400 object does not exist.
     * @exception PropertyVetoException If the change is vetoed.
     * @exception UnknownHostException If the AS/400 system cannot be located.
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

        ProgramParameter[] parmList=new ProgramParameter[8];
        parmList=getParameters(defaultLength_,objName);

        ProgramCall rtvUsersAUT=new ProgramCall(as400_);
        rtvUsersAUT.setProgram(prgName.getPath(),parmList);

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
            // If there is no enough space provided, retrieve data again.
            parmList=getParameters(requiredLength+400,objName);
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
     * Returns the copyright.
    **/
    private static String getCopyright()
    {
        return Copyright.copyright;
    }
    
    /**
     * Returns the RecordFormat of the feedback informations.
     * @return The RecordFormat of the feedback informations.
     *
    **/
    private  RecordFormat getFeedbackRecordFormat()
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
    ProgramParameter[] getParameters(int length, String objName)
    {

        ProgramParameter[] parmList=new ProgramParameter[8];
        AS400Bin4 bin4=new AS400Bin4();
        parmList[0]=new ProgramParameter(length);

        parmList[1]=new ProgramParameter(bin4.toBytes(length));

        parmList[2]=new ProgramParameter(55);

        parmList[3]=new ProgramParameter(bin4.toBytes(55));

        AS400Text text8 = new AS400Text(8, getCcsid(), as400_); //@A2C
        parmList[4]=new ProgramParameter(text8.toBytes("RTUA0100"));

        int objnameLength=objName.length();
        AS400Text text = new AS400Text(objnameLength, getCcsid(), as400_); //@A2C

        //@A3A: Need to use uppercase name if it is a DLO object because
        // ccsid 5026 currently doesn't convert a lowercase name to an
        // uppercase one. Normally, case shouldn't matter since
        // the AS/400 uppercases any QDLS names for us automatically.
        if (objName.toUpperCase().startsWith("/QDLS/"))      //@A3A
          objName = objName.toUpperCase();                   //@A3A

        parmList[5]=new ProgramParameter(text.toBytes(objName));

        parmList[6]=new ProgramParameter(bin4.toBytes(objnameLength));

        byte[] errorInfo = new byte[32];
        parmList[7] = new ProgramParameter( errorInfo, 0 );

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
                   
    /**
     * Sets the sensitivity level of the object.
     * @param objName The object the sensitivity level will be set to.
     * @param sensitivityLevel The sensitivity level will be set.
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
     * @param   system The AS/400 system object.
     * @see     #getSystem
    **/
    public void setSystem(AS400 system)
    {
        if (system == null)
        {
            throw new NullPointerException("system");
        }
        as400_ = system;
        return;

    }

}


















