///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;



/**
The UserGroup class represents a user profile that is
a group profile.
@see com.ibm.as400.access.User
@see com.ibm.as400.access.UserList
@see com.ibm.as400.resource.RUser
@see com.ibm.as400.resource.RUserList
**/
public class UserGroup extends User
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 5L;



    // Private data.
    private UserList userList_ = null;


/**
Constructs a UserGroup object.
Note that this constructor no longer throws any of the
declared exceptions, but they remain for compatibility.
@param system   The system.
@param name     The group profile name.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
**/
    public UserGroup(AS400 system, String name)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   ObjectDoesNotExistException,
                   IOException,
                   UnsupportedEncodingException
    {
        super(system, name);
    }


    //@F0A Called by UserList.getUsers().
    UserGroup(AS400 system, String name, boolean hasMembers, String description)
    {
      super(system, name, hasMembers, description);
    }


/**
Returns the list of users that are members of this group.

@return An Enumeration of {@link com.ibm.as400.access.User User} objects.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception RequestNotSupportedException    If the AS/400 system is older than V3R7.
**/
    public Enumeration getMembers()
           throws AS400Exception,
                  AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException,
                  RequestNotSupportedException
    {
        if (userList_ == null)
            userList_ = new UserList(getSystem(), UserList.MEMBER, getName());
        return userList_.getUsers();
    }


}
