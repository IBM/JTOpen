///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.sql.DriverManager;          // @A1A
import java.sql.SQLException;



// JDBC 2.0
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
public class AS400JDBCInputStream
extends InputStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private boolean         closed_;
    private ConvTable converter_; //@P0C
    private String          encoding_;
    private JDLobLocator    locator_;
    private int             offset_;



/**
Constructs an AS400JDBCInputStream object.  The data for the
binary stream will be retrieved as requested, directly from the
server, using the locator handle.

@param  locator             The locator.
**/
    AS400JDBCInputStream (JDLobLocator locator)
    {
        closed_         = false;
        converter_      = null;
        encoding_       = null;
        locator_        = locator;
        offset_         = 0;
    }



/**
Constructs an AS400JDBCInputStream object.  The data for the
binary stream will be retrieved as requested, directly from the
server, using the locator handle.

@param  locator             The locator.
@param  converter           The converter.
@param  encoding            The encoding. 
**/
    AS400JDBCInputStream (JDLobLocator locator, ConvTable converter, String encoding) //@P0C
    {
        closed_         = false;
        converter_      = converter;
        encoding_       = encoding;
        locator_        = locator;
        offset_         = 0;
    }


// @C4 method re-worked to return a value
/**
Returns the number of bytes that can be read without blocking.

@return The number of bytes that can be read without blocking.

@exception IOException      If an input/output error occurs.
**/
    public int available ()
        throws IOException
    {
        long returnValue = 0;

        if (closed_)
            throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);
        
        try
        {
           returnValue = locator_.getLength() - offset_;
           
           if (returnValue < 0)
              returnValue = 0;
        }
        catch (SQLException e) 
        { 
           if (JDTrace.isTraceOn ()) 
           {
              JDTrace.logInformation(this, "Error in available");
              e.printStackTrace (DriverManager.getLogStream ());         
              closed_ = true;
           }
           throw new IOException (e.getMessage());  
        }          
                                
        // Make sure we don't return a negative number when casting the long
        // to an int.
        if (returnValue > 0x7FFF)
           returnValue = 0x7FFF;

        return (int) returnValue;
    }



/**
Closes the stream and releases any associated system resources.

@exception IOException      If an input/output error occurs.
**/
    public void close ()
        throws IOException
    {
        closed_ = true;
    }



   


/**
Marks the current position in the stream.  This is not supported.

@param readLimit    The read limit.
**/
    public void mark (int readLimit)
    {
        // Not supported.
    }



/**
Indicates if mark() and reset() are supported.

@return     Always false.  mark() and reset() are not supported.
**/
    public boolean markSupported ()
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
    public int read ()
        throws IOException
    {

        // @D1d old:
        // byte[] data = new byte[1];                                      // @E1A
        // return (read(data, 0, 1) == 1) ? data[0] : -1;                  // @E1A
   
        // @D1a new.  Manually convert the byte to int.  If java does
        // it negative bytes will be sign extended to be negative
        // ints.  If we do it the resulting int will be between 0
        // and 255 which matches was the spec (and our javadoc) says.
        int returnValue = -1;                                           // @d1a
        byte[] data = new byte[1];                                      // @d1A
        
        if (read(data, 0, 1) > 0)                                       // @D1a
           returnValue = data[0] & 0x000000FF;                          // @D1a

        return returnValue;                                             // @D1a
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
    public int read (byte[] data)
        throws IOException
    {
        return read (data, 0, data.length);
    }



/**
Reads bytes of data.  This method blocks until
data is available, the end of the stream is detected, or an
exception is thrown.

@param  data    The byte array to fill with data.  This method
                will read as much data as possible to fill
                the array.
@param  start   The start position in the array.
@param  length  The maximum number of bytes to read.
@return         The number of bytes of data read,
                or -1 if no more data is available.

@exception IOException      If an input/output error occurs.
**/
    public int read (byte[] data, int start, int length)
        throws IOException
    {
        // If the stream is closed.
        if (closed_)
            throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE); // @C4a

        // Validate the arguments.                                                 @A1A
        if (data == null)                    
            throw new NullPointerException("data");

        if ((start < 0) || (start > data.length))                               // @A1A
            throw new ExtendedIllegalArgumentException("start", 
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);  // @A1A

        if ((length < 0) || (start + length > data.length))                     // @A1A
            throw new ExtendedIllegalArgumentException("length", 
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);  // @A1A

        // Retrieve the next bytes of data.
        try {
            DBLobData lobData = locator_.retrieveData (offset_, length);        // @B1C
            int lengthRead = lobData.getLength ();                              // @A1A @B1C
            if (lengthRead == 0) {                                              // @A1A
                if (length == 0)                                                // @A1A
                    return 0;                                                   // @A1A
                else {                                                          // @A1A
                    closed_ = true;                                             // @A1A
                    return -1;                                                  // @A1A
                }                                                               // @A1A
            }                                                                   // @A1A
            else {                                                              // @A1A
                // @A1D offset_ += 1;
                int actualLength;
                if (converter_ == null) {                                       // @A1C
                    actualLength = Math.min (lengthRead, length);               // @A1C
                    System.arraycopy (lobData.getRawBytes (),                   // @B1C
                                      lobData.getOffset (),                     // @B1C
                                      data, start, actualLength);
                }
                else {
                    String s = converter_.byteArrayToString (lobData.getRawBytes (),    // @B1C
                                                             lobData.getOffset (),      // @B1C
                                                             lengthRead);       // @A1C
                    byte[] converted = s.getBytes (encoding_);
                    actualLength = Math.min (converted.length, length);
                    System.arraycopy (converted, 0, data, start, actualLength);                
                }
    
    
                offset_ += actualLength;                                        // @A1A
                return actualLength;
            }                                                                   // @A1A
        }
        catch (SQLException e) {
            if (JDTrace.isTraceOn ())                                           // @A1A
                e.printStackTrace (DriverManager.getLogStream ());              // @A1A
            closed_ = true;
            throw new IOException (e.getMessage());   // @A2C
        }
    }



/**
Repositions to the marked position.  This is not supported.

@exception IOException      If an input/output error occurs.
**/
    public void reset ()
        throws IOException
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
    public long skip (long length)
        throws IOException
    {
        // If the stream is closed.
        if (closed_)
            throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE); // @C4c
        
        // Validate the arguments.                                                 @A1A
        if (length < 0)                                                         // @A1A
            throw new ExtendedIllegalArgumentException("length", 
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);  // @A1A

          // Skip.                        
        try
        {
          long newOffset = length + offset_;
          long len = locator_.getLength();
         
           if (newOffset > len)
           {
             length = len - offset_;
             offset_ = (int) len;
           }
           else
           {
             offset_ = offset_ + (int) length;
           }  
           return length;         
        }
        catch (SQLException e) 
        { 
           if (JDTrace.isTraceOn ()) 
           {
              JDTrace.logInformation(this, "Error in skip");
              e.printStackTrace (DriverManager.getLogStream ());         
              closed_ = true;
           }
           throw new IOException (e.getMessage());  
        } 
    }



}
