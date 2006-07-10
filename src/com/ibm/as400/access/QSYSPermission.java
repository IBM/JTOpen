///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: QSYSPermission.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;                                          // @A2a

/**
<P>The QSYSPermission class represents the permissions for the specified user 
of an object in the traditional i5/OS library structure stored in QSYS.LIB.  
<P>A object stored in QSYS.LIB can set its authorities by setting a single
 object authority value or by setting the individual object and data authorities.
<P>Use <i>getObjectAuthority()</i> to display the current object authority or
<i>setObjectAuthority()</i> to set the current object authority using a single value.
Valid values are: *ALL, *AUTL, *CHANGE, *EXCLUDE, and *USE.  

<P>The detailed object authority can be set to one or more of the following
values: alter, exist, management, operational, reference.  Use the 
appropriate set methods (<i>setAlter()</i>, <i>setExistence()</i>, 
<i>setManamagement()</i>, <i>setOperational()</i>, or <i>setReference()</i>) 
to set the value on or off.  After all values are set, use the <i>commit()</i> 
method from the Permission class to send the changes to the server.


<P>The data authority can be set to one or more of the following values: 
add, delete, execute, read, or update. Use the appropriate
set methods (<i>setAdd()</i>, <i>setDelete()</i>, <i>setExecute()</i>, 
<i>setRead()</i>, or <i>setUpdate()</i>) to set the value on or off. After all 
the values are set, use the <i>commit()</i> method from the Permission class 
to send the changes to the server. 

<P>The single authority actually represents a combination of the detailed object
authorities and the data authorities.  Selecting a single authority will 
automatically turn on the appropriate detailed authorities.  Likewise, selecting
 various detailed authorities will change the appropriate single authority values.


<P>For more information on object authority commands, refer to i5/OS CL 
commands GRTOBJAUT (Grant object authority) and EDTOBJAUT (Edit object authority).
**/

public class QSYSPermission extends UserPermission
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    

    static final long serialVersionUID = 4L;


    /**
     * Constructs a QSYSPermission object. This is the permission of the 
     * specified user.
     * @param userProfileName The name of the user profile.
     *
    **/
    public QSYSPermission(String userProfileName)
    {
        super(userProfileName);
        dataAuthority_ = "*EXCLUDE";
        objectAuthority_ = BASIC_EXCLUDE;
        authorities_ = new boolean[10];
        for (int i=0;i<10;i++)
        {
            authorities_[i] = false;
        }
        return;
    }


    // @A2a
    /**
     * Pad or truncate the given string to the
     * given length. If the given String is null, an empty String
     * of the correct length will be returned.
     *
     * @param s The string to pad or truncate.
     * @param desiredLength The length to pad or truncate the given string to.
     * @return The padded or truncated string.
     */
    static String adjustLength(String s, int desiredLength)
    {
      if (s == null) s = "";
      int sLength = s.length();
      if (sLength == desiredLength) return s;
      else if (sLength < desiredLength)
      {
        StringBuffer buffer = new StringBuffer(s);
        for(int i = sLength; i < desiredLength; i++)
          buffer.append(' ');
        return buffer.toString();
      }
      else return s.substring(0, desiredLength);
    }



    // @A3a - This logic was formerly in PermissionAccessQSYS.getChgCommand().
   /** 
     * Returns the current object authorities and data authorities.
     * @param objectIsAuthList true if the object is an AUTL object; false otherwise.
     * @return The current authorities, as a blank-separated list.
     *
    **/
    String getAuthorities(boolean objectIsAuthList)
    {
        String authority="";

        if (isManagement())
             authority=authority+"*OBJMGT ";
        if (isExistence())
             authority=authority+"*OBJEXIST ";
        if (isAlter())
             authority=authority+"*OBJALTER ";
        if (isReference())
             authority=authority+"*OBJREF ";
        if (isOperational())
             authority=authority+"*OBJOPR ";
        if (isAdd())
             authority=authority+"*ADD ";
        if (isDelete())
             authority=authority+"*DLT ";
        if (isRead())
             authority=authority+"*READ ";
        if (isUpdate())
             authority=authority+"*UPD ";
        if (isExecute())
             authority=authority+"*EXECUTE ";
        if (objectIsAuthList && isAuthorizationListManagement())
             authority=authority+"*AUTLMGT";

        if (authority.equals(""))
            authority = "*EXCLUDE";

        return authority;
    }


    /**
     * Returns the object authority of the user specified as a single value.
     * @return The object authority of the user specified as a single value. 
     * The possible values are:
       <UL>
       <LI>*ALL The user can perform all operations on the object
           except for those limited to the owner or controlled by 
           authorization list management authority.  The user can control
           the object's existence, specify the security for the object, change
           the object, and perform basic functions on the object.  The user
           can also change ownership of the object.  
       <LI>*AUTL The public authority of the authorization list securing the
           object will be used. This is valid only if *PUBLIC is the user
           specified.
       <LI>*CHANGE The user can perform all operations on the object
           except those limited to the owner or controlled by object existence
           authority and object management authority.
       <LI>*EXCLUDE The user cannot access the object.
       <LI>*USE The user has object operational authority, read authority,
       and execute authority.
       </UL>
     * @see #setObjectAuthority(String)   
    **/
    public String getObjectAuthority()
    {
        parseBasic();
        if (isFromAuthorizationList())
            return "*AUTL";
        switch (objectAuthority_)
        {
            case BASIC_ALL :
                return "*ALL";
            case BASIC_EXCLUDE :
                return "*EXCLUDE";
            case BASIC_USE :
                return "*USE";
            case BASIC_CHANGE :
                return "*CHANGE";
            default :
                return "USER DEFINED";
        }
    }


    // @A2a
    /**
     * Determines if the user has the given authorities to the object on the server.
     * Returns true if the user has <em>at least all</em> of the specified authorities,
     * and false otherwise.
     * @param system The server.
     * @param userProfileName The name of the user profile.
     * @param objectPath The full path of the object. For example, "/QSYS.LIB/FRED.LIB".
     * @param authorityList The list of authorities.  At least one authority must be specified.
     * Possible authorities include:
     * <pre>
     * *EXCLUDE
     * *ALL
     * *CHANGE
     * *USE
     * *AUTLMGT
     * *OBJALTER
     * *OBJOPR
     * *OBJMGT
     * *OBJEXIST
     * *OBJREF
     * *READ
     * *ADD
     * *UPD
     * *DLT
     * *EXECUTE
     * </pre>
     * @return true if the user has all the specified authorities to the object.
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException If this thread is interrupted.
     * @exception IOException If an error occurs while communicating with the server.
     */
    public static boolean hasObjectAuthorities(AS400 system, String userProfileName, String objectPath, String[] authorityList)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
    {
      if (system == null) throw new NullPointerException("system");
      if (userProfileName == null) throw new NullPointerException("userProfileName");
      if (objectPath == null) throw new NullPointerException("objectPath");
      if (authorityList == null) throw new NullPointerException("authorityList");
      if (authorityList.length == 0) {
        Trace.log (Trace.ERROR, "No authorities were specified.");
        throw new ExtendedIllegalArgumentException("authorityList",
                      ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }

      boolean rcStatus = true;

      String program   = "/QSYS.LIB/QSYCUSRA.PGM";
      String profile   = adjustLength(userProfileName, 10);
      QSYSObjectPathName qsysObjPath = new QSYSObjectPathName(objectPath);
      String attribute = "*" + adjustLength(qsysObjPath.getObjectType(), 9);
      String qualObj   = adjustLength(qsysObjPath.getObjectName(), 10) + adjustLength(qsysObjPath.getLibraryName(), 10);

      String authorities = "";
      for (int i=0; i< authorityList.length; i++)
        authorities += adjustLength(authorityList[i], 10);

      try
      {
        ProgramCall newCall = new ProgramCall(system);
        ProgramParameter paramList[] = new ProgramParameter[8];

        paramList[0] = new ProgramParameter(1);
        paramList[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        paramList[1] = new ProgramParameter(CharConverter.stringToByteArray(system, profile));
        paramList[2] = new ProgramParameter(CharConverter.stringToByteArray(system, qualObj));
        paramList[3] = new ProgramParameter(CharConverter.stringToByteArray(system, attribute));
        paramList[4] = new ProgramParameter(CharConverter.stringToByteArray(system, authorities));
        paramList[5] = new ProgramParameter(BinaryConverter.intToByteArray(authorityList.length)); // # of Authorities
        paramList[6] = new ProgramParameter(BinaryConverter.intToByteArray(1));  // Call Level

        // Input/Output parameter. Returns information as follows :
        // First four bytes = size of the error code structure.
        // Next four bytes = bytes available
        // Next seven bytes = message id
        // Last byte = reserved / Never used.
        byte [] parmByt7 = new byte[16];
        System.arraycopy(BinaryConverter.intToByteArray(16), 0, parmByt7, 0, 4);

        paramList[7] = new ProgramParameter(parmByt7, 16);
        paramList[7].setParameterType(ProgramParameter.PASS_BY_REFERENCE);

        newCall.setProgram(program, paramList);

        if (newCall.run() != true)
        {
          // If any error message return.
          AS400Message[] msgList = newCall.getMessageList();
          throw new AS400Exception(msgList);
        }

        byte [] errCode = paramList[7].getOutputData();

        // Get the available bytes from the input/output parameter (8th param).
        // If the available bytes is greater than 0 there was an error.
        if (BinaryConverter.byteArrayToInt(errCode, 4) > 0)
        {
          byte[] message = new byte[7];
          System.arraycopy(errCode, 8, message, 0, 7);
          String msgId = CharConverter.byteArrayToString(system, message);
          Trace.log(Trace.ERROR, "Error code from QSYCUSRA: " + msgId);
          throw new AS400Exception(new AS400Message(msgId,""));
        }

        String output = CharConverter.byteArrayToString(system, paramList[0].getOutputData());

        if (!output.startsWith("Y"))
          rcStatus = false;
      }
      catch (java.beans.PropertyVetoException e)
      {
        // This should never happen.
        Trace.log(Trace.ERROR, e.toString());
      }

      return rcStatus;
    }


   /**
     * Indicates if the user has the data authority of add.
     * @return true if the user has the data authority of add; 
     * false otherwise.
     *
    **/
    public boolean isAdd()
    {
        return authorities_[DATA_ADD];
    }

   
    /**
     * Indicates if the user has the object authority of alter.
     * @return true if the user has the object authority of alter;
     * false otherwise.
     *
    **/
    public boolean isAlter()
    {
        return authorities_[OBJECT_ALTER];
    }

    
   /**
     * Indicates if the user has the data authority of delete.
     * @return true if the user has the data authority of delete;
     * false otherwise.
     *
    **/
    public boolean isDelete()
    {
        return authorities_[DATA_DELETE];
    }    

  /**
     * Indicates if the user has the data authority of execute.
     * @return true if the user has the data authority of execute;
     * false otherwise.
     *
    **/
    public boolean isExecute()
    {
        return authorities_[DATA_EXECUTE];
    }
    /**
     * Indicates if the user has the object authority of existence.
     * @return true if the user has the object authority of existence;
     * false otherwise.
     *
    **/
     public boolean isExistence()
    {
        return authorities_[OBJECT_EXIST];
    }
    

    /**
     * Indicates if the user has the object authority of management.
     * @return true if the user has the object authority of management;
     * false otherwise.
     *
    **/
    
    public boolean isManagement()
    {
        return authorities_[OBJECT_MANAGEMENT];
    }

    
   /**
     * Indicates if the user has the object authority of operational.
     * @return true if the user has the object authority of operational;
     * false otherwise.
     *
    **/
    
    public boolean isOperational()
    {
        return authorities_[OBJECT_OPERATION];
    }

  /**
     * Indicates if the user has the data authority of read.
     * @return true if the user has the data authority of read;
     * false otherwise.
     *
    **/
    public boolean isRead()
    {
        return authorities_[DATA_READ];
    }

    /**
     * Indicates if the user has the object authority of reference.
     * @return true if the user has the object authority of reference;
     * false otherwise.
     *
    **/
    
    public boolean isReference()
    {
        return authorities_[OBJECT_REFERENCE];
    }

    /**
     * Indicates if the user has the data authority of update.
     * @return true if the user has the data authority of update;
     * false otherwise.
     *
    **/
    public boolean isUpdate()
    {
        return authorities_[DATA_UPDATE];
    }
    
    /**
     * Parses the basic authority type.
    **/
    private synchronized void parseBasic()
    {
        for (int j=0;j<4;j++)
        {
            boolean match = true;
            for (int i=0;i<10;i++)
            {
                if (authorities_[i] != basicAutMapping[j][i])
                {
                    match = false;
                    break;
                }
            }
            if (match == true)
            {
                objectAuthority_= j;
                return;
            }
        }
        objectAuthority_= -1;
        return;
    }

   /**
     * Sets the add data authority.
     * @param authority  true to set the data authority of add on; 
     * false to set the data authority of add off.
     *
    **/
    public synchronized void setAdd(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_ADD] = authority;
        return;
    }


   /**
     * Sets the alter object authority.
     * @param authority  true to set the object authority of alter on; 
     * false to set the object authority of alter off.
     *
    **/
    public synchronized void setAlter(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_ALTER] = authority;
        return;
    }

    /**
     * Sets the delete data authority.
     * @param authority  true to set the data authority of delete on;
     * false to set the data authority of delete off.
     *
    **/
    public synchronized void setDelete(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_DELETE] = authority;
        return;
    }


 /**
     * Sets the execute data authority.
     * @param authority  true to set the data authority of execute on;
     * false to set the data authority of execute off.
     *
    **/
    public synchronized void setExecute(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_EXECUTE] = authority;
        return;
    }


    /**
     * Sets the existence object authority.
     * @param authority  true to set the object authority of existence on;
     * false to set the object authority of existence off.
     *
    **/
    
    public synchronized void setExistence(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_EXIST] = authority;
        return;
    }


   /**
     * Sets the management object authority.
     * @param authority  true to set the object authority of management on;
     * false to set the object authority of management off.
     *
    **/
    
    public synchronized void setManagement(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_MANAGEMENT] = authority;
        return;
    }

  /**
     * Sets the object authority of the user using a single value.
     * @param authority The object authority of the user.
       <P>Valid values are:
       <UL>
       <LI>*ALL The user can perform all operations on the object
           except for those limited to the owner or controlled by 
           authorization list management authority.  The user can control
           the object's existence, specify the security for the object, change
           the object, and perform basic functions on the object.  The user
           can also change ownership of the object.  
       <LI>*AUTL The public authority of the authorization list securing the
           object will be used. This is valid only if *PUBLIC is the user
           specified.
       <LI>*CHANGE The user can perform all operations on the object
           except those limited to the owner or controlled by object existence
           authority and object management authority.
       <LI>*EXCLUDE The user cannot access the object.
       <LI>*USE The user has object operational authority, read authority,
       and execute authority.
       </UL>
    
**/
    public synchronized void setObjectAuthority(String authority)
    {
        if (authority == null)
            throw new NullPointerException("authority");
        String aut = authority.trim().toUpperCase();
        changeAuthority();
        if (getObjectAuthority().equals(aut) == true)
            return;
        if (aut.equals("*ALL"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_ALL][i];
            }
            objectAuthority_ = BASIC_ALL;
        } else if (aut.equals("*EXCLUDE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_EXCLUDE][i];
            }
            objectAuthority_ = BASIC_EXCLUDE;
        } else if (aut.equals("*USE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_USE][i];
            }
            objectAuthority_ = BASIC_USE;
        } else if (aut.equals("*CHANGE"))
        {
            for (int i=0;i<10;i++)
            {
                authorities_[i] = basicAutMapping[BASIC_CHANGE][i];
            }
            objectAuthority_ = BASIC_CHANGE;
        } else if (aut.equals("*AUTL"))          // @A4a
        {
            setFromAuthorizationList(true);      // @A4a
        } else                                   // @A4a
        {
          Trace.log (Trace.ERROR, "Invalid object authority was specified: " + authority);  // @A4a
          throw new ExtendedIllegalArgumentException("authority",
                         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);     // @A4a
        }
      
        return;
    }

  /**
     * Sets the operational object authority.
     * @param authority  true to set the object authority of operational on;
     * false to set the object authority of operational off.
     *
    **/
    
    public synchronized void setOperational(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_OPERATION] = authority;
        return;
    }

   /**
     * Sets the read data authority.
     * @param authority  true to set the data authority of read on;
     * false to set the data authority of read off.
     *
    **/
    public synchronized void setRead(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_READ] = authority;
        return;
    }

    /**
     * Sets the reference object authority.
     * @param authority true to set the object authority of reference on;
     * false to set the object authority of reference off.
     *
    **/
    public synchronized void setReference(boolean authority)
    {
        changeAuthority();
        authorities_[OBJECT_REFERENCE] = authority;
        return;
    }

   
    /**
     * Sets the update data authority.
     * @param authority  true to set the data authority of update on;
     * false to set the data authority of update off.
     *
    **/
    public synchronized void setUpdate(boolean authority)
    {
        changeAuthority();
        authorities_[DATA_UPDATE] = authority;
        return;
    }

}
     
