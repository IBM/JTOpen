///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400JDBCInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private boolean         closed_;
    private ConverterImplRemote converter_;
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
    AS400JDBCInputStream (JDLobLocator locator, ConverterImplRemote converter, String encoding)
    {
        closed_         = false;
        converter_      = converter;
        encoding_       = encoding;
        locator_        = locator;
        offset_         = 0;
    }



/**
Returns the number of bytes that can be read without blocking.

@return The number of bytes that can be read without blocking.

@exception IOException      If an input/output error occurs.
**/
    public int available ()
        throws IOException
    {
        return 0;
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
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
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
        // If the stream is closed.
        if (closed_)
            throw new IOException ();

        // Retrieve the next byte of data.
        try {
            DBLobData data = locator_.retrieveData (offset_, 1);        // @B1C
            offset_ += 1;
            int length = data.getLength ();                             // @A1A
            if (length == 0) {                                          // @A1A
                closed_ = true;                                         // @A1A
                return -1;                                              // @A1A
            }
            else                                                        // @A1A
                return data.getRawBytes ()[data.getOffset ()];
        }
        catch (SQLException e) {
            closed_ = true;
            throw new IOException (e.getMessage());   // @A2C
        }
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
            throw new IOException ();

        // Validate the arguments.                                                 @A1A
        if ((start < 0) || (start > data.length))                               // @A1A
            throw new IndexOutOfBoundsException ("start");                      // @A1A
        if ((length < 0) || (start + length > data.length))                     // @A1A
            throw new IndexOutOfBoundsException ("length");                     // @A1A

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
            throw new IOException ();
        
        // Validate the arguments.                                                 @A1A
        if (length < 0)                                                         // @A1A
            throw new IllegalArgumentException ("length");                      // @A1A

        // Skip.
        offset_ += length;
        return length;
    }



}
