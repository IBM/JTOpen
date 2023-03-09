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
    // The user list object from which to get the users.
    private UserList list_ = null;

    // The number of objects returned so far by nextElement()
    private int counter_;

    // The number of users in the list.
    private int length_ = 0;

    // Offset in the user list.
    private int listOffset_ = 0;

    // Cache of user objects.
    private User[] userCache_ = null;
    // Offset in the cache.
    private int cacheOffset_ = 0;

    UserEnumeration(UserList list, int length)
    {
        list_ = list;
        length_ = length;
    }

    public final boolean hasMoreElements()
    {
        return counter_ < length_;
    }

    public final Object nextElement()
    {
        if (counter_ >= length_)
        {
            Trace.log(Trace.ERROR, "Next element not available in UserEnumeration.");
            throw new NoSuchElementException();
        }

        if (userCache_ == null || cacheOffset_ >= userCache_.length)
        {
            try
            {
                userCache_ = list_.getUsers(listOffset_, 1000);
                cacheOffset_ = 0;
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loaded next block in UserEnumeration: " + userCache_.length + " messages at list offset " + listOffset_ + " out of " + length_ + " total.");
            }
            catch (Exception e)
            {
                Trace.log(Trace.ERROR, "Exception while loading nextElement() in UserEnumeration:", e);
                throw new NoSuchElementException();
            }

            // We have a freshly loaded cache, so reset to the beginning of the cache.
            cacheOffset_ = 0;

            // Set starting offset for next call to getUsers(),
            // in case another call is needed.
            listOffset_ += userCache_.length;
        }
        ++counter_;
        return userCache_[cacheOffset_++];
    }
}
