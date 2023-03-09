///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMReadCallbackAdapter.java
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
 * Helper class for implementing a {@link DDMReadCallback DDMReadCallback}.
**/
public abstract class DDMReadCallbackAdapter implements DDMReadCallback
{
  private boolean done_;

  public DDMReadCallbackAdapter()
  {
  }

  /**
   * Returns true after an operation calls {@link #endOfFile endOfFile()} or {@link #recordNotFound recordNotFound()}.
   * To reset the state, call {@link #reset reset()}.
  **/
  public boolean isDone()
  {
    return done_;
  }

  /**
   * Called by the other newRecord().
  **/
  public abstract void newRecord(int recordNumber, byte[] recordData, boolean[] nullFields) throws IOException;

  public void newRecord(DDMCallbackEvent event, DDMDataBuffer dataBuffer) throws IOException
  {
    newRecord(dataBuffer.getRecordNumber(), dataBuffer.getRecordDataBuffer(), dataBuffer.getNullFieldValues());
  }

  public void recordNotFound(DDMCallbackEvent event)
  {
    done_ = true;
  }

  public void endOfFile(DDMCallbackEvent event)
  {
    done_ = true;
  }

  /**
   * Resets the state of this callback adapter.
   * @see #isDone
  **/
  public void reset()
  {
    done_ = false;
  }
}


