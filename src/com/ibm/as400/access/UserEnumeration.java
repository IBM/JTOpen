///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;

/**
 * Helper class. Used to wrap the User[] with an Enumeration.
 * This class is used by UserList.
**/
class UserEnumeration implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  private User[] userCache_;
  private UserList list_;  
  private int counter_;
  private int numUsers_;
  private int listOffset_ = 0;
  private int cachePos_ = 0;

  UserEnumeration(UserList list, int length)
  {
    list_ = list;
    numUsers_ = length;
  }

  public final boolean hasMoreElements()
  {
    return counter_ < numUsers_;
  }

  public final Object nextElement()
  {
    if (counter_ >= numUsers_)
    {
      throw new NoSuchElementException();
    }

    if (userCache_ == null || cachePos_ >= userCache_.length)
    {
      try
      {
        userCache_ = list_.getUsers(listOffset_, 1000);
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "Loaded next block in UserEnumeration: "+userCache_.length+" messages at offset "+listOffset_+" out of "+numUsers_+" total.");
        }
      }
      catch (Exception e)
      {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.ERROR, "Exception while loading nextElement() in UserEnumeration:", e);
        }
        throw new NoSuchElementException();
      }
      cachePos_ = 0;
      listOffset_ += userCache_.length;
    }
    ++counter_;
    return userCache_[cachePos_++];
  }
}



