///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCBlobLocatorOutputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.sql.*;

final class AS400JDBCBlobLocatorOutputStream extends AS400JDBCOutputStream
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private AS400JDBCBlobLocator blob_;

  AS400JDBCBlobLocatorOutputStream(AS400JDBCBlobLocator blob, long position)
  {
    super(position);
    blob_ = blob;
  }

  int doWrite(long position, byte data) throws SQLException
  {
    synchronized(blob_)
    {
      JDLobLocator locator = blob_.locator_;
      synchronized(locator)
      {
        return locator.writeData(position-1, data, true);               //@K1A
      }
    }
  }

  int doWrite(long position, byte[] data, int offset, int length) throws SQLException
  {
    synchronized(blob_)
    {
      JDLobLocator locator = blob_.locator_;
      synchronized(locator)
      {
        return locator.writeData(position-1, data, offset, length, true);       //@K1A
      }
    }
  }
}



