///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListSpooledFiles.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.components;

import java.io.*;

import com.ibm.jtopenlite.command.CommandConnection;

/**
 * Represents the information returned by the WRKSPLF command, but uses the OpenListOfSpooledFiles classes to obtain it.
**/
public class ListSpooledFiles
{
  private final ListSpooledFilesImpl impl_ = new ListSpooledFilesImpl();

  public ListSpooledFiles()
  {
  }

  /**
   * Returns an array of spooled files for the current user, similar to the way WRKSPLF does.
   * The various SpooledFileInfo.toString() methods print the fields the way WRKSPLF does.
   * @param conn The connection to use.
  **/
  public SpooledFileInfo[] getSpooledFiles(final CommandConnection conn) throws IOException
  {
    return getSpooledFiles(conn, "*CURRENT");
  }

  public void getSpooledFiles(final CommandConnection conn, final SpooledFileInfoListener listener) throws IOException
  {
    getSpooledFiles(conn, "*CURRENT", listener);
  }

  /**
   * Returns an array of spooled files for the specified user, similar to the way WRKSPLF does.
   * The various SpooledFileInfo.toString() methods print the fields the way WRKSPLF does.
   * @param conn The connection to use.
   * @param user The user name, or *CURRENT, or *ALL.
  **/
  public SpooledFileInfo[] getSpooledFiles(final CommandConnection conn, String user) throws IOException
  {
    impl_.setSpooledFileInfoListener(impl_);
    return impl_.getSpooledFiles(conn, user);
  }

  public void getSpooledFiles(final CommandConnection conn, String user, final SpooledFileInfoListener listener) throws IOException
  {
    impl_.setSpooledFileInfoListener(listener);
    impl_.getSpooledFiles(conn, user);
  }
}

