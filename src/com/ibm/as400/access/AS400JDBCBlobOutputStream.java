///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCBlobOutputStream.java
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

final class AS400JDBCBlobOutputStream extends AS400JDBCOutputStream
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
  
  private AS400JDBCBlob blob_;

  AS400JDBCBlobOutputStream(AS400JDBCBlob blob, long position)
  {
    super(position);
    blob_ = blob;
  }

  int doWrite(long position, byte data) throws SQLException
  {
    return blob_.setByte(position, data);
  }

  int doWrite(long position, byte[] data, int offset, int length) throws SQLException
  {
    return blob_.setBytes(position, data, offset, length);
  }
}



