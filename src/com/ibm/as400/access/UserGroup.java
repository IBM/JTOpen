///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: UserGroup.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.io.UnsupportedEncodingException;


/**
 * The UserGroup class represents a special user whose user 
 * profile is a group profile. Here is a simple example 
 * explaining how to use UserGroup:
 * 
 * <P><blockquote><pre>

 * // Construct a AS400 system object.
 * AS400 system = new AS400();

   // Create a User Group
 * UserGroup userGroup = new UserGroup();
 *
  * ...
 * // Set the AS/400 system. 
 * userGroup.setSystem(system);

 * // Set the group profile name.
 * userGroup.setName("Fredgroup");

 * // Retrieve the users of this UserGroup.
 * for(Enumeration e = userGroup.getMembers();e.hasMoreElements())
 * {
 *     System.out.println((User)e.nextElement());
 * }
 * </pre></blockquote></p>
 *
**/
public class UserGroup extends User
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private UserList userlist_;

//@A2D    /**
//@A2D     * Constructs a UserGroup object.
//@A2D     * Before retrieving the information of a user, the methods <i>setSystem()</i>
//@A2D     * and <i>setName()</i> should be explicitly invoked. 
//@A2D    **/
//@A2D    public UserGroup()
//@A2D    {           
//@A2D    }

    /**
     * Constructs a UserGroup object. 
     *
     * @param as400 The AS/400 system in which the group information resides.
     * @param groupProfileName The user profile name.
     * @exception AS400Exception                  If the AS/400 system returns an error message.
     * @exception AS400SecurityException          If a security or authority error occurs.
     * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException            If this thread is interrupted.
     * @exception IOException                     If an error occurs while communicating with the AS/400.
     * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     * @exception UnsupportedEncodingException    If the character encoding is not supported. 
    **/
    public UserGroup(AS400 as400,String groupProfileName)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   ObjectDoesNotExistException,
                   IOException,
                   // @A3D PropertyVetoException,
                   UnsupportedEncodingException
    {   
        
        super(as400,groupProfileName);
                
        userlist_=new UserList(as400,"*MEMBER",groupProfileName);
    }

   /**
     Returns the copyright.
   **/
   private static String getCopyright()
   {
      return Copyright.copyright;
   }



    /**
    * Returns a list of users that are members of this group. The enumeration
    * contains the users of this group.
    * @return A list of users that are members of this group.
    *
    * @exception AS400Exception                  If the AS/400 system returns an error message.
    * @exception AS400SecurityException          If a security or authority error occurs.
    * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
    * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    * @exception InterruptedException            If this thread is interrupted.
    * @exception IOException                     If an error occurs while communicating with the AS/400.
    * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
    * @exception RequestNotSupportedException    If the AS/400 system is older than V3R7.
    **/
    public Enumeration getMembers()
           throws AS400Exception, 
                  AS400SecurityException, 
                  ErrorCompletingRequestException,
                  InterruptedException, 
                  IOException, 
                  ObjectDoesNotExistException,
                  // @A3D PropertyVetoException,
                  RequestNotSupportedException
    {
        Enumeration enumeration=null;
        enumeration=userlist_.getUsers();
        
        return enumeration;
    }
}    
