///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCWriter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream; 
import java.io.Writer;
import java.sql.Clob;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
The AS400JDBCWriter class provides a stream
to write into large objects.  The data is valid only within the current
transaction.  Users get one of these objects by calling Clob.setCharacterStream()
which returns an object of type Writer.
**/
class AS400JDBCWriter extends Writer
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  private boolean closed_ = false;   // is the stream closed?
  private long position_;      // position from which the user wanted us to start writing
  private AS400JDBCClob clob_; // We have separate vars for clob and locator to remove build dependency on JDBC 3.0.
  private AS400JDBCClobLocator locator_;


  AS400JDBCWriter(AS400JDBCClob clob, long positionToStartWriting) 
  {
    clob_ = clob;
    position_ = positionToStartWriting;
  }


  AS400JDBCWriter(AS400JDBCClobLocator locator, long positionToStartWriting) 
  {
    locator_ = locator;
    position_ = positionToStartWriting;
  }


  // Method required by interface 'Appendable', new in J2SE 5.0.
  /**
   Appends the specified character to this writer.
   @param c - The 16-bit character to append. 
   @return This Writer 
   @exception IOException  If an I/O error occurs
   **/
  public Writer append(char c)
    throws IOException
  {
    write(c);
    return this;
  }


  // Method required by interface 'Appendable', new in J2SE 5.0.
  /**
   Appends the specified character sequence to this writer.
   @param csq  The character sequence to append. If csq is null, then the four characters "null" are appended to this writer. 
   @return This Writer 
   @exception IOException  If an I/O error occurs
   **/
  public Writer append(CharSequence csq)
    throws IOException
  {
    write(csq == null ? "null" : csq.toString());
    return this;
  }


  // Method required by interface 'Appendable', new in J2SE 5.0.
  /**
   Appends a subsequence of the specified character sequence to this writer.
   @param csq  The character sequence from which a subsequence will be appended. If csq is null, then characters will be appended as if csq contained the four characters "null".
   @param start  The index of the first character in the subsequence
   @param end The index of the character following the last character in the subsequence 
   @return This Writer 
   @exception IndexOutOfBoundsException If start or end are negative, start is greater than end, or end is greater than csq.length()
   @exception IOException  If an I/O error occurs
   **/
  public Writer append(CharSequence csq,
                       int start,
                       int end)
    throws IOException
  {
    write(csq == null ? new String("null").substring(start,Math.min(end,4))
          : csq.subSequence(start, end).toString());
    return this;
  }


  /*
  Close the writer.
  */
  public void close()
  {
    closed_ = true;
  }


  /*
  Flush the writer.
  */
  public void flush()
  {
    //no-op
  }



  /*
  Write a character array to the writer.
  
  @param cbuf The character byte array the user wants written to the writer.
  */
  public void write(char[] cbuf) throws IOException
  {
    if (cbuf == null) throw new NullPointerException("cbuf");

    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    write(cbuf, 0, cbuf.length);
  }



  /*
  Write a character array to the writer from offset off for len characters.

  @param cbuf      The character array the user wants written to the writer.
  @param off       The offset into the character array that the user wants written to the 
                   writer (1-based).
  @param len       The number of bytes the user wants written to the writer
                   from the byte array they passed in.  
  */
  public void write(char[] cbuf, int off, int len) throws IOException
  {
    if (cbuf == null) throw new NullPointerException("cbuf");
    if ((off < 0) || (off > len)) throw new ExtendedIllegalArgumentException("off", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    if (len < 0) throw new ExtendedIllegalArgumentException("len", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    write(new String(cbuf, off, len));
  }



  /*
  Write a character to the writer.

  @param cbuf      The character the user wants written to the writer.
  */
  public void write(int c) throws IOException
  {
    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    write(new String(new char[] { (char)c}));
  }



  /*
  Write a String to the writer from offset off for len characters.

  @param str       The string the user wants written to the writer.
  */
  public void write(String str) throws IOException
  {
    if (str == null) throw new NullPointerException("str");

    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    write(str, 0, str.length());
  }



  /*
  Write a String to the writer from offset off for len characters.

  @param str       The String the user wants written to the writer.
  @param off       The offset into the character array that the user wants written to the 
                   writer.
  @param len       The number of bytes the user wants written to the writer
                   from the byte array they passed in.  
  */
  public synchronized void write(String str, int off, int len) throws IOException
  {
    if (str == null) throw new NullPointerException("str");
    if ((off < 0) || (off > len)) throw new ExtendedIllegalArgumentException("off", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    if (len < 0) throw new ExtendedIllegalArgumentException("len", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    try
    {
      if (clob_ != null) clob_.setString(position_, str, off, len);
      else locator_.setString(position_, str, off, len);
      position_ += len;
    }
    catch (SQLException e)
    {
      if (JDTrace.isTraceOn()) e.printStackTrace(DriverManager.getLogStream());
      closed_ = true;
      throw new IOException(e.getMessage());
    }
  }
}
