///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserGroup.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Enumeration;

/**
 The UserGroup class represents a user profile that is a group profile.
 @see  com.ibm.as400.access.User
 @see  com.ibm.as400.access.UserList
 **/
public class UserGroup extends User
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 5L;

    // Private data.
    private UserList userList_ = null;

    /**
     Constructs a UserGroup object.  Note that this constructor no longer throws any of the declared exceptions, but they remain for compatibility.
     @param  system  The system object representing the server on which the group profile exists.
     @param  name  The group profile name.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     **/
    public UserGroup(AS400 system, String name) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        super(system, name);
    }

    // Called by UserList.getUsers().
    UserGroup(AS400 system, String name, boolean groupHasMember, String description)
    {
        super(system, name, groupHasMember, description);
    }

    /**
     Returns the list of users that are members of this group.
     @return  An Enumeration of {@link com.ibm.as400.access.User User} objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the server.
     @exception  ObjectDoesNotExistException  If the object does not exist on the server.
     @exception  RequestNotSupportedException  If the requested function is not supported because the server is not at the correct level.
     **/
    public Enumeration getMembers() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, RequestNotSupportedException
    {
        if (userList_ == null)
        {
            userList_ = new UserList(getSystem(), UserList.MEMBER, getName());
        }
        return userList_.getUsers();
    }
}
