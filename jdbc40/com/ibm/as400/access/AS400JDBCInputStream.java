///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
/* ifdef JDBC40 */
import java.sql.DriverManager;
/* endif */ 
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
  static final String copyright = "Copyright (C) 1997-2010 International Business Machines Corporation and others.";

  private boolean         closed_;
  private JDLobLocator    locator_;
  private long             offset_;
  private long             length_; //@pda jdbc40

  private long mark_ = 0;

/**
Constructs an AS400JDBCInputStream object.  The data for the
binary stream will be retrieved as requested, directly from the
system, using the locator handle.

@param  locator             The locator.
**/
  AS400JDBCInputStream(JDLobLocator locator)
  {
    locator_        = locator;
    offset_         = 0;
    closed_         = false;
    try  //@pda jdbc40 set length (for sub-streams)
    {                           
        length_ = locator.getLength();
    }catch(SQLException e) 
    {
        length_ = 0;
        closed_ = true; 
    }
    
  }

  //@pda jdbc40 new constructor
  /**
  Constructs an AS400JDBCInputStream object.  The data for the
  binary stream will be retrieved as requested, directly from the
  system, using the locator handle.

  @param  locator             The locator.
  @param  pos                 The starting position.
  @param  length              The length of the stream.
  **/
    AS400JDBCInputStream(JDLobLocator locator, long pos, long length)
    {
      locator_        = locator;
      offset_         = pos;
      length_         = length;
      closed_         = false;
      long actualLen;
      try  
      {                           
          actualLen = locator.getLength();

      }catch(SQLException e) 
      {
          actualLen = 0;
          closed_ = true;
      }
      if(length_ > actualLen)
          length_ = actualLen;
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
   // try                                                          //@PDd jdbc40
   // {                                                            //@PDd jdbc40
      returnValue = length_ - offset_; //@PDC jdbc40

      if (returnValue < 0) returnValue = 0;
   // }                                                            //@PDd jdbc40
   // catch (SQLException e)                                       //@PDd jdbc40
   // {                                                            //@PDd jdbc40
   //   if (JDTrace.isTraceOn())                                   //@PDd jdbc40
   //  {                                                           //@PDd jdbc40
   //     JDTrace.logInformation(this, "Error in available");      //@PDd jdbc40
   //     e.printStackTrace(DriverManager.getLogStream());         //@PDd jdbc40
   //     closed_ = true;                                          //@PDd jdbc40
   //   }                                                          //@PDd jdbc40
   //   throw new IOException(e.getMessage());                     //@PDd jdbc40
   // }                                                            //@PDd jdbc40

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
Marks the current position in the stream.

@param readLimit    The read limit.
@see #reset()
**/
  public void mark(int readLimit)
  {
    mark_ = offset_;
  }



/**
Indicates if mark() and reset() are supported.

@return true
**/
  public boolean markSupported()
  {
    return true;
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
    if ((length < 0) || (start + length > data.length))//@pdc locator_.retrieveData(,len) does not fail if len is greater that available length
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
      if (offset_ >= length_) //@PDC jdbc40
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
        JDTrace.logInformation(this, "Error in read" + e.getMessage()); //@pdc
        //@pdd e.printStackTrace(DriverManager.getLogStream());
        closed_ = true;                                   
      }
      throw new IOException(e.getMessage());             
    }
  }



/**
Repositions to the marked position.
If mark() has not been called, repositions to the beginning of the stream.

@exception IOException      If an input/output error occurs.
@see #mark(int)
**/
  public synchronized void reset() throws IOException
  {
    offset_ = mark_;
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
    //try                                                    //@PDD jdbc40
    //{                                                      //@PDD jdbc40
      long newOffset = length + offset_;
      long len = length_;                                    //@PDC jdbc40

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
    //}                                                     //@PDD jdbc40
    //catch (SQLException e)                                //@PDD jdbc40
    //{                                                     //@PDD jdbc40
     // if (JDTrace.isTraceOn())                            //@PDD jdbc40
     // {                                                   //@PDD jdbc40
     //   JDTrace.logInformation(this, "Error in skip");    //@PDD jdbc40
     //   e.printStackTrace(DriverManager.getLogStream());  //@PDD jdbc40
     // }                                                   //@PDD jdbc40
     // closed_ = true;                                     //@PDD jdbc40
     // throw new IOException(e.getMessage());              //@PDD jdbc40
    //}                                                     //@PDD jdbc40
  }
}
