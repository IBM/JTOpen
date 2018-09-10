///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SerializableInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ByteArrayInputStream;


/**
The SerializableInputStream class can be used to encapsulate an InputStream
such that it is serializable.
**/
class SerializableInputStream
extends java.io.InputStream
implements java.io.Serializable
{
    static final long serialVersionUID = 4L;

  // Private data.
  private byte[] byteArray_;
  private transient ByteArrayInputStream iStream_;


  public SerializableInputStream (java.io.InputStream iStream)
    throws IOException
  {
    // Assume that the arguments have been validated.
    byteArray_ = new byte[iStream.available()];
    int bytesRead = iStream.read (byteArray_);
    if (bytesRead < 1) {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Initial read() of 'iStream' into internal buffer returned " + bytesRead);
    }
    iStream_ = new ByteArrayInputStream (byteArray_);
  }

  public int available() throws IOException
  { return iStream_.available (); }

  public void close() throws IOException
  { iStream_.close (); }

  public synchronized void mark(int readlimit)
  { iStream_.mark (readlimit); }

  public boolean markSupported()
  { return iStream_.markSupported (); }

  public int read() throws IOException
  { return iStream_.read (); }

  public int read(byte b[]) throws IOException
  { return iStream_.read (b); }

  public int read(byte b[],
                  int off,
                  int len) throws IOException
  { return iStream_.read (b, off, len); }

  // This is called when the object is deserialized.
  // Note that byteArray_ gets serialized/deserialized.
  private void readObject (java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    // Restore the non-static and non-transient fields.
    in.defaultReadObject ();

    iStream_ = new ByteArrayInputStream (byteArray_);
  }

  public synchronized void reset() throws IOException
  { iStream_.reset (); }

  public long skip(long n) throws IOException
  { return iStream_.skip (n); }


}
