///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserEnumeration.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.util.NoSuchElementException;

// Helper class.  Used to wrap the User[] with an Enumeration.
// This class is used by UserList.
class UserEnumeration implements Enumeration
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    // The user list object from which to get the users.
    private UserList list_ = null;
    // The number of users in the list.
    private int length_ = 0;

    // Position in the user list.
    private int listPosition_ = 0;

    // Cache of user objects.
    private User[] userCache_ = null;
    // Position in the cache.
    private int cachePosition_ = 0;

    UserEnumeration(UserList list, int length)
    {
        list_ = list;
        length_ = length;
    }

    public final boolean hasMoreElements()
    {
        return listPosition_ < length_;
    }

    public final Object nextElement()
    {
        if (listPosition_ >= length_)
        {
            Trace.log(Trace.ERROR, "Next element not available in UserEnumeration.");
            throw new NoSuchElementException();
        }

        if (userCache_ == null || cachePosition_ >= userCache_.length)
        {
            try
            {
                userCache_ = list_.getUsers(listPosition_, 1000);
                cachePosition_ = 0;
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loaded next block in UserEnumeration: " + userCache_.length + " messages at offset " + listPosition_ + " out of " + length_ + " total.");
            }
            catch (Exception e)
            {
                Trace.log(Trace.ERROR, "Exception while loading nextElement() in UserEnumeration:", e);
                throw new NoSuchElementException();
            }
        }
        ++listPosition_;
        return userCache_[cachePosition_++];
    }
}
