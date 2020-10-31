///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  Connection.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

import java.io.*;

/**
 * Interface that represents a connection to an IBM i server.
**/
public interface Connection
{
  /**
   * Closes the connection.
  **/
  public void close() throws IOException;

  /**
   * Returns true if this connection is closed.
  **/
  public boolean isClosed();

  /**
   * Returns the current authenticated user of this connection.
  **/
  public String getUser();

  /**
   * Returns the server job associated with this connection, if any.
  **/
  public String getJobName();
}
