///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMReadCallback.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

import java.io.*;

/**
 * Used by DDMConnection to pass the output of a read operation to the user in a memory-conscious fashion.
**/
public interface DDMReadCallback
{
  /**
   * Called by DDMConnection when a new record has been read.
  **/
  public void newRecord(DDMCallbackEvent event, DDMDataBuffer dataBuffer) throws IOException;

  /**
   * Called by DDMConnection when a keyed read returned no matching records.
  **/
  public void recordNotFound(DDMCallbackEvent event);

  /**
   * Called by DDMConnection when a read or position operation moved the cursor to before the first record or after the last record.
  **/
  public void endOfFile(DDMCallbackEvent event);
}
