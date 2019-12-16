///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListObjects.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

import com.ibm.jtopenlite.command.*;
import java.io.*;

/**
 * Represents the information returned by the WRKOBJ command, but uses the OpenListOfObjects classes to obtain it.
**/
public class ListObjects
{
  private final ListObjectsImpl impl_ = new ListObjectsImpl();

  public ListObjects()
  {
  }

  /**
   * Returns an array of objects, sorted by library list order and object name, the way WRKOBJ does.
   * ObjectInfo.toString() prints the fields the way WRKJOB does.
   * @param conn The connection to use.
   * @param name The object name for which to search. For example, "CRT*".
   * @param library The library name in which to search. For example, "*LIBL".
   * @param type The object type for which to search. For example, "*CMD".
  **/
  public ObjectInfo[] getObjects(final CommandConnection conn, final String name, final String library, final String type) throws IOException
  {
    return impl_.getObjects(conn, name, library, type);
  }
}

