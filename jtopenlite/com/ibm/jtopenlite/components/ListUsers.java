///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListUsers.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

import com.ibm.jtopenlite.ddm.*;
import java.io.*;

/**
 * Represents the information returned by the WRKUSRPRF command.
**/
public class ListUsers
{
  private final ListUsersImpl impl_ = new ListUsersImpl();

  public ListUsers()
  {
  }

  /**
   * Returns an array of users, the way WRKUSRPRF does.
   * @param conn The connection to use.
  **/
  public UserInfo[] getUsers(final DDMConnection conn) throws IOException
  {
    return impl_.getUsers(conn);
  }
}

