///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SerializableReader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharArrayReader;
import java.io.IOException;
import java.sql.SQLException;


/**
The SerializableReader class can be used to encapsulate a Reader object
such that it is serializable.
**/
class SerializableReader
extends java.io.Reader
implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  // Private data.
  private char[] charArray_;
  private transient CharArrayReader reader_;




  public SerializableReader (java.io.Reader reader, int length)
    throws IOException, SQLException
  {
    // Assume that the arguments have been validated.
    charArray_ = new char[length];
    int numChars = reader.read (charArray_);
    if (numChars != length ||
        reader.read() != -1)
    {
      if (JDTrace.isTraceOn())
        JDTrace.logInformation (this, "Length parameter does not match actual length of buffer.");
      throw new SQLException ();
    }
    reader_ = new CharArrayReader (charArray_);
  }

  public void close()
  { reader_.close (); }

  public void mark(int readAheadLimit) throws IOException
  { reader_.mark (readAheadLimit); }

  public boolean markSupported()
  { return reader_.markSupported (); }

  public int read() throws IOException
  { return reader_.read (); }

  public int read(char b[],
                  int off,
                  int len) throws IOException
  { return reader_.read (b, off, len); }

  // This is called when the object is deserialized.
  // Note that charArray_ gets serialized/deserialized.
  private void readObject (java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    // Restore the non-static and non-transient fields.
    in.defaultReadObject ();

    reader_ = new CharArrayReader (charArray_);
  }

  public boolean ready() throws IOException
  { return reader_.ready (); }

  public void reset() throws IOException
  { reader_.reset (); }

  public long skip(long n) throws IOException
  { return reader_.skip (n); }



}
