///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;



/**
<p>The AS400JDBCInputStream class provides access to binary data
using an input stream.  The data is valid only within the current
transaction.
**/
//
// Implementation notes:
//
// 1.  This implementation deals specifically with binary data
//     that is retrieved upon request with the locator handle.
//     The case where the binary data comes in full with the result
//     set can be stored directly in a ByteArrayInputStream.
//
// 2.  We do not worry about buffering or caching, since the caller
//     can just wrap this in a BufferedInputStream if they want that.
//
class AS400JDBCInputStream extends InputStream
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private boolean         closed_;
  private JDLobLocator    locator_;
  private long             offset_;

/**
Constructs an AS400JDBCInputStream object.  The data for the
binary stream will be retrieved as requested, directly from the
server, using the locator handle.

@param  locator             The locator.
**/
  AS400JDBCInputStream(JDLobLocator locator)
  {
    locator_        = locator;
    offset_         = 0;
    closed_         = false;
  }


/**
Returns the number of bytes that can be read without blocking.

@return The number of bytes that can be read without blocking.

@exception IOException      If an input/output error occurs.
**/
  public synchronized int available() throws IOException
  {
    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    long returnValue = 0;
    try
    {
      returnValue = locator_.getLength() - offset_;

      if (returnValue < 0) returnValue = 0;
    }
    catch (SQLException e)
    {
      if (JDTrace.isTraceOn())
      {
        JDTrace.logInformation(this, "Error in available");
        e.printStackTrace(DriverManager.getLogStream());
        closed_ = true;
      }
      throw new IOException(e.getMessage());  
    }

    if (returnValue > 0x7FFFFFFF) returnValue = 0x7FFFFFFF;

    return(int)returnValue;
  }



/**
Closes the stream and releases any associated system resources.

@exception IOException      If an input/output error occurs.
**/
  public synchronized void close() throws IOException
  {
    closed_ = true;
  }



/**
Marks the current position in the stream.  This is not supported.

@param readLimit    The read limit.
**/
  public void mark(int readLimit)
  {
    // Not supported.
  }



/**
Indicates if mark() and reset() are supported.

@return     Always false.  mark() and reset() are not supported.
**/
  public boolean markSupported()
  {
    return false;
  }



/**
Reads the next byte of data.  This method blocks until data is
available, the end of the stream is detected, or an exception
is thrown.

@return The next byte of data as an int in the range 0 to 255,
        or -1 if no more data is available.

@exception IOException      If an input/output error occurs.
**/
  public synchronized int read() throws IOException
  {
    int returnValue = -1;

    byte[] data = new byte[1]; // Yes, we allocate each time, but the user shouldn't be calling this method if they want performance to be good.
    if (read(data, 0, 1) > 0) returnValue = data[0] & 0x00FF;

    return returnValue;
  }



/**
Reads bytes of data.  This method blocks until
data is available, the end of the stream is detected, or an
exception is thrown.

@param  data    The byte array to fill with data.  This method
                will read as much data as possible to fill
                the array.
@return         The number of bytes of data read,
                or -1 if no more data is available.

@exception IOException      If an input/output error occurs.
**/
  public synchronized int read(byte[] data) throws IOException
  {
    return read(data, 0, data.length);
  }



/**
Reads bytes of data.  This method blocks until
data is available, the end of the stream is detected, or an
exception is thrown.

@param  data    The byte array to fill with data.  This method
                will read as much data as possible to fill
                the array. All data may not be available to
                read from the stream at once, only when this
                method returns -1 can you be sure all of the
                data was read.
@param  start   The start position in the array.
@param  length  The maximum number of bytes to read.
@return         The number of bytes of data read,
                or -1 if no more data is available.

@exception IOException      If an input/output error occurs.
**/
  public synchronized int read(byte[] data, int start, int length) throws IOException
  {
    if (data == null) throw new NullPointerException("data");

    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    if ((start < 0) || (start > data.length))
    {
      throw new ExtendedIllegalArgumentException("start", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if ((length < 0) || (start + length > data.length))
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Added this to prevent trying to read past the end of the stream.
    if (length == 0)
    {
      return 0;
    }

    try
    {
      if (offset_ >= locator_.getLength())
      {
        //@CRS - Is this really the end? Or is the end getMaxLength()?
        return -1;                          
      }

      //@CRS: Can lengthRead ever be greater than length?
      if (locator_.isGraphic()) length = length / 2;
      DBLobData lobData = locator_.retrieveData(offset_, length);
      int lengthRead = lobData.getLength();
      if (lengthRead == 0)
      {
        closed_ = true;                  
        return -1;                       
      }
      else
      {
        System.arraycopy(lobData.getRawBytes(), lobData.getOffset(), data, start, lengthRead);
        offset_ += locator_.isGraphic() ? lengthRead / 2: lengthRead;
        return lengthRead;
      }
    }
    catch (SQLException e)
    {
      if (JDTrace.isTraceOn())
      {
        JDTrace.logInformation(this, "Error in read");
        e.printStackTrace(DriverManager.getLogStream());
        closed_ = true;                                   
      }
      throw new IOException(e.getMessage());             
    }
  }



/**
Repositions to the marked position.  This is not supported.

@exception IOException      If an input/output error occurs.
**/
  public void reset() throws IOException
  {
    // Not supported.
  }


/**
Skips over and discards data.

@param  length  The maximum number of bytes to skip.  If negative,
                no bytes are skipped.
@return         The number of bytes skipped.

@exception IOException      If an input/output error occurs.
**/
  public synchronized long skip(long length) throws IOException
  {
    if (closed_)
    {
      throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);
    }
    if (length < 0)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (length == 0) return 0;
    try
    {
      long newOffset = length + offset_;
      long len = locator_.getLength();

      if (newOffset > len)
      {
        length = len - offset_;
        offset_ = len;
      }
      else
      {
        offset_ = offset_ + length;
      }  
      return length;         
    }
    catch (SQLException e)
    {
      if (JDTrace.isTraceOn())
      {
        JDTrace.logInformation(this, "Error in skip");
        e.printStackTrace(DriverManager.getLogStream());         
      }
      closed_ = true;
      throw new IOException(e.getMessage());  
    }
  }
}
