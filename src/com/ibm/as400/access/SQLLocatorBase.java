///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;



abstract class SQLLocatorBase implements SQLLocator
{

  public abstract Object clone(); 

  // Utility method to read the inputStream and optionally write it to a
  // locator.

  static  byte[] readInputStream(InputStream stream, int length,
      JDLobLocator locator, boolean doubleByteOffset ) throws SQLException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int blockSize;
    if ((length <= 0) || (length >= AS400JDBCPreparedStatement.LOB_BLOCK_SIZE)) {
      blockSize = AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
    } else {
      blockSize = length;
    }
    byte[] byteBuffer = new byte[blockSize];
    try {
      int totalBytesRead = 0;
      int bytesRead = stream.read(byteBuffer, 0, blockSize);
      while (bytesRead > -1 && ((totalBytesRead < length) || (length < 0))) {
        baos.write(byteBuffer, 0, bytesRead);
        if (locator != null) {
          if (doubleByteOffset) { 
            locator.writeData(totalBytesRead / 2, byteBuffer, 0, bytesRead, true);
          } else { 
          locator.writeData(totalBytesRead, byteBuffer, 0, bytesRead, true);
          }
        }
        totalBytesRead += bytesRead;
        if (length > 0) {
          int bytesRemaining = length - totalBytesRead;
          if (bytesRemaining < blockSize) {
            blockSize = bytesRemaining;
          }
        } else {
          // block size stays the same
        }
        bytesRead = stream.read(byteBuffer, 0, blockSize);
      }
    } catch (IOException ie) {
      JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
    }

    return baos.toByteArray();

  }


}
