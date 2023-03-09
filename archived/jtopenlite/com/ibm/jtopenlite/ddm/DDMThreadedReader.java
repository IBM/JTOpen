///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMThreadedReader.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

/**
 * A special kind of {@link DDMReadCallback DDMReadCallback} you can use when you want multiple
 * threads to simultaneously process data being read out of the same file and connection.  The data is read by the main thread,
 * but the conversion is done by one or more processing threads.  Subclass this class and implement the
 * {@link #process process()} method to read record data off-thread from the main I/O thread.
 * This gives the performance advantage of streaming data from the server in parallel with processing said data.
 * It is important to note that using more than one thread will likely cause the records to be processed out-of-order.
**/
public abstract class DDMThreadedReader implements DDMReadCallback
{
  private final DDMFile file_;
  private final DDMReaderRunner[] runners_;
  private final Thread[] threads_;
  private boolean done_;

  private long sequence_;

  /**
   * Constructs a multi-threaded reader to process data being read from the specified file
   * using the specified record format.
   * @param format The record format to copy and give to each thread for it to pass to {@link #process process()}.
   * @param file The file being read.
   * @param numThreads The number of threads to use. This number is capped by the number of buffers in the file object, so
   * that each thread always has at least one buffer to process, to avoid contention. Having more than one buffer per thread is fine.
  **/
  public DDMThreadedReader(final DDMRecordFormat format, final DDMFile file, int numThreads)
  {
    file_ = file;
    done_ = false;

    final int numBuffers = file.getBufferCount();
    if (numThreads > numBuffers) numThreads = numBuffers;
    runners_ = new DDMReaderRunner[numThreads];
    threads_ = new Thread[numThreads];
    for (int i=0; i<numThreads; ++i)
    {
      runners_[i] = new DDMReaderRunner(this, format, i, numThreads, numBuffers);
      threads_[i] = new Thread(runners_[i], "DDMThreadedReader-"+i);
      threads_[i].start();
    }
  }

  final DDMDataBuffer getDataBuffer(final int index)
  {
    return file_.getDataBuffer(index);
  }

  /**
   * Do not call this method directly; it is implemented for DDMConnection to call.
  **/
  public final void newRecord(final DDMCallbackEvent event, final DDMDataBuffer buffer)
  {
    buffer.startProcessing();
    final DDMFile file = event.getFile();
    if (file == file_)
    {
      DDMDataBuffer nextBuffer = file.getNextDataBuffer();
      // Wait to use the next buffer until our background thread is done with it.
      while (nextBuffer.isProcessing()) // Uses volatile variable, faster than synchronization, at the cost of a CPU loop here.
      {
        file.nextBuffer(); // Advance.
        nextBuffer = file.getNextDataBuffer(); // Check the next one.
      }
    }
  }

  /**
   * Do not call this method directly; it is implemented for DDMConnection to call.
  **/
  public final void recordNotFound(final DDMCallbackEvent event)
  {
    finish();
  }

  /**
   * Do not call this method directly; it is implemented for DDMConnection to call.
  **/
  public final void endOfFile(final DDMCallbackEvent event)
  {
    finish();
  }

  /**
   * Indicates if end-of-file has been reached and our threads have been shutdown.
  **/
  public final boolean isDone()
  {
    return done_;
  }

  private final void finish()
  {
    for (int i=0; i<file_.getBufferCount(); ++i)
    {
      while (file_.getDataBuffer(i).isProcessing());
    }

    for (int i=0; i<runners_.length; ++i)
    {
      runners_[i].done();
    }
    for (int i=0; i<threads_.length; ++i)
    {
      try
      {
        threads_[i].join();
      }
      catch (InterruptedException ie)
      {
      }
    }
    done_ = true;
  }

  /**
   * Override this method with your own record processing logic.
  **/
  public abstract void process(final DDMRecordFormat format, final DDMDataBuffer dataBuffer);
}
