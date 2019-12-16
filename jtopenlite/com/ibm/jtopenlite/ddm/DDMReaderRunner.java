///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMReaderRunner.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

final class DDMReaderRunner implements Runnable
{
  private boolean done_;
  private final DDMThreadedReader reader_;
  private final DDMRecordFormat format_;
  private final int resetIndex_;
  private final int skipCount_;
  private final int total_;

  DDMReaderRunner(DDMThreadedReader reader, DDMRecordFormat format, int reset, int skip, int total)
  {
    reader_ = reader;
    format_ = format.newCopy();
    resetIndex_ = reset;
    skipCount_ = skip;
    total_ = total;
  }

  public final void done()
  {
    done_ = true;
  }

  public final void run()
  {
    int currentIndex = resetIndex_;

    while (!done_ && !Thread.currentThread().isInterrupted())
    {
      while (!done_ && !Thread.currentThread().isInterrupted() &&
             !reader_.getDataBuffer(currentIndex).isProcessing())
      {
        currentIndex += skipCount_;
        if (currentIndex >= total_)
        {
          currentIndex = resetIndex_;
        }
      }
      if (!done_ && !Thread.currentThread().isInterrupted())
      {
        final DDMDataBuffer buffer = reader_.getDataBuffer(currentIndex);
        reader_.process(format_, buffer);
        buffer.doneProcessing();
      }
    }
  }
}
