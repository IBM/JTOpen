///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CoverLetterReader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.beans.*;

final class CoverLetterReader extends Reader
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private static final RecordFormat coverLetterRF_ = new RecordFormat("QAPZCOVER");
  static
  {
    coverLetterRF_.addFieldDescription(new HexFieldDescription(new AS400ByteArray(80), "QAPZCOVER"));
  }

  private final SequentialFile file_ = new SequentialFile();

  private String currentLine_;
  private int currentOffset_;
  private ConvTable table_;

  CoverLetterReader(AS400 system, String path, int ccsid)
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    super(coverLetterRF_);
    try
    {
      file_.setSystem(system);
      file_.setPath(path);
      file_.setRecordFormat(coverLetterRF_);
    }
    catch(PropertyVetoException pve) {}
    table_ = ConvTable.getTable(ccsid, null);
    file_.open(AS400File.READ_ONLY, 10, AS400File.COMMIT_LOCK_LEVEL_NONE);
    readRecord();
  }

  protected void finalize()
  {
    try
    {
      file_.close();
    }
    catch(Exception e)
    {
    }
  }

  public int read() throws IOException
  {
    try
    {
      synchronized(lock)
      {
        if (currentLine_ == null) return -1;
        if (currentOffset_ >= currentLine_.length())
        {
          readRecord();
        }
        return currentLine_.charAt(currentOffset_++);
      }
    }
    catch(AS400Exception e)
    {
      handleException(e);
    }
    catch(AS400SecurityException e)
    {
      handleException(e);
    }
    catch(ErrorCompletingRequestException e)
    {
      handleException(e);
    }
    catch(InterruptedException e)
    {
      handleException(e);
    }
    catch(ObjectDoesNotExistException e)
    {
      handleException(e);
    }
    return -1;
  }

  public int read(char[] cbuf) throws IOException
  {
    try
    {
      synchronized(lock)
      {
        if (currentLine_ == null) return -1;
        int cbufOffset = 0;
        while (currentLine_ != null && cbufOffset < cbuf.length)
        {
          char[] cur = currentLine_.toCharArray();
          int max = cur.length-currentOffset_;
          if (max > cbuf.length) max = cbuf.length;
          System.arraycopy(cur, currentOffset_, cbuf, cbufOffset, max);
          currentOffset_ += max;
          cbufOffset += max;
          if (currentOffset_ >= currentLine_.length())
          {
            readRecord();
          }
        }
        return cbufOffset;
      }
    }
    catch(AS400Exception e)
    {
      handleException(e);
    }
    catch(AS400SecurityException e)
    {
      handleException(e);
    }
    catch(ErrorCompletingRequestException e)
    {
      handleException(e);
    }
    catch(InterruptedException e)
    {
      handleException(e);
    }
    catch(ObjectDoesNotExistException e)
    {
      handleException(e);
    }
    return -1;
  }

  public int read(char[] cbuf, int off, int len) throws IOException
  {
    try
    {
      synchronized(lock)
      {
        if (currentLine_ == null) return -1;
        int numChars = 0;
        int cbufOffset = off;
        while (currentLine_ != null && cbufOffset < (off+len))
        {
          char[] cur = currentLine_.toCharArray();
          int max = cur.length-currentOffset_;
          if (max > (len-cbufOffset)) max = (len-cbufOffset);
          System.arraycopy(cur, currentOffset_, cbuf, cbufOffset, max);
          cbufOffset += max;
          currentOffset_ += max;
          if (currentOffset_ >= currentLine_.length())
          {
            readRecord();
          }
        }
        return cbufOffset-off;
      }
    }
    catch(AS400Exception e)
    {
      handleException(e);
    }
    catch(AS400SecurityException e)
    {
      handleException(e);
    }
    catch(ErrorCompletingRequestException e)
    {
      handleException(e);
    }
    catch(InterruptedException e)
    {
      handleException(e);
    }
    catch(ObjectDoesNotExistException e)
    {
      handleException(e);
    }
    return -1;
  }

  private void handleException(Exception e) throws IOException
  {
    if (Trace.traceOn_)
    {
      Trace.log(Trace.ERROR, "Error on SequentialFileReader:", e);
    }
    throw new IOException();
  }

  public boolean ready() throws IOException
  {
    synchronized(lock)
    {
      return currentLine_ != null && currentOffset_ < currentLine_.length();
    }
  }

  public void reset() throws IOException
  {
    try
    {
      synchronized(lock)
      {
        file_.positionCursorBeforeFirst();
        readRecord();
      }
    }
    catch(AS400Exception e)
    {
      handleException(e);
    }
    catch(AS400SecurityException e)
    {
      handleException(e);
    }
    catch(ErrorCompletingRequestException e)
    {
      handleException(e);
    }
    catch(InterruptedException e)
    {
      handleException(e);
    }
    catch(ObjectDoesNotExistException e)
    {
      handleException(e);
    }
  }

  public long skip(long n) throws IOException
  {
    try
    {
      synchronized(lock)
      {
        if (currentLine_ == null) return -1;
        long numChars = 0;
        while (currentLine_ != null && numChars < n)
        {
          int max = currentLine_.length()-currentOffset_;
          if (max > n) max = (int)n;
          numChars += max;
          currentOffset_ += max;
          if (currentOffset_ >= currentLine_.length())
          {
            readRecord();
          }
        }
        return numChars;
      }
    }
    catch(AS400Exception e)
    {
      handleException(e);
    }
    catch(AS400SecurityException e)
    {
      handleException(e);
    }
    catch(ErrorCompletingRequestException e)
    {
      handleException(e);
    }
    catch(InterruptedException e)
    {
      handleException(e);
    }
    catch(ObjectDoesNotExistException e)
    {
      handleException(e);
    }
    return -1;
  }

  private void readRecord()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    Record r = file_.readNext();
    if (r == null)
    {
      currentLine_ = null;
    }
    else
    {
      byte[] contents = r.getContents();
      currentLine_ = table_.byteArrayToString(contents, 0, contents.length)+'\n';
      //currentLine_ = ((String)r.getField(0))+'\n';
    }
    currentOffset_ = 0;
  }

  public void close() throws IOException
  {
    try
    {
      synchronized(lock)
      {
        file_.close();
      }
    }
    catch(AS400Exception e)
    {
      handleException(e);
    }
    catch(AS400SecurityException e)
    {
      handleException(e);
    }
    catch(ErrorCompletingRequestException e)
    {
      handleException(e);
    }
    catch(InterruptedException e)
    {
      handleException(e);
    }
  }
}



