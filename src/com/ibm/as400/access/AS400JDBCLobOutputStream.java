///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCLobOutputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream; 
import java.sql.SQLException;
import java.sql.DriverManager;



// JDBC 3.0 support
/**
The AS400JDBCLobOutputStream class provides a stream
to write into large objects.  The data is valid only within the current
transaction.  Users get one of these objects by calling Clob.setAsciiStream() or
Blob.setBinaryStream which both return an object of type OutputStream
**/
class AS400JDBCLobOutputStream
extends OutputStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    //Private data
    private boolean closed_ = false;           // is the stream closed?
    private long positionToStartWriting_;      // position from which the user wanted us to start writing
    private Object lob_;                       // the lob object on which the user an output stream 



    /*
    Construct an AS400JDBCLobOutputStream object.  
    */
    AS400JDBCLobOutputStream (Object lob, long positionToStartWriting)
    {
        //We assume the caller has already validated the arguments
        lob_ = lob;
        positionToStartWriting_ = positionToStartWriting;
    }



    /*
    Close the output stream.  
    */
    public void close ()
    {
        closed_ = true;
    }



    /*
    Flush the output stream.  
    */
    public void flush ()
    {
        //no-op
    }



    /*
    Write a byte array to the output stream.
    
    @param byteArray The byte array the user wants written to the output stream.  
    */
    public void write (byte[] byteArray)
    throws IOException
    {
        // Validate arguments
        if (byteArray == null)
            throw new NullPointerException("byteArray");

        // If the stream is closed.
        if (closed_)
            throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

        write (byteArray, 0, byteArray.length);
    }



    /*
    Write part of a byte array to the output stream from offset <i>off</i> for len bytes.
    
    @param byteArray The byte array the user wants written to the output stream.
    @param off       The offset into the byte array that the user wants written to the 
                     output stream (1-based).
    @param len       The number of bytes the user wants written to the output stream
                     from the byte array they passed in.  
    */
    public void write (byte[] byteArray, int off, int len)
    throws IOException
    {
        // Validate arguments
        if (byteArray == null)
            throw new NullPointerException("byteArray");
        if ((off < 0) || (off > len))
            throw new ExtendedIllegalArgumentException("off", 
                                                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        if (len < 0)
            throw new ExtendedIllegalArgumentException("len", 
                                                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        // If the stream is closed.
        if (closed_)
            throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

        if (lob_ instanceof AS400JDBCClob)
        {
            String stringToWrite = new String(byteArray, "ASCII");
            try
            {
                ((AS400JDBCClob)lob_).setString(positionToStartWriting_, stringToWrite);
            }
            catch (SQLException e)
            {
                if (JDTrace.isTraceOn ())
                    e.printStackTrace (DriverManager.getLogStream ());
                closed_ = true;
                throw new IOException(e.getMessage());
            }
        }
        else if (lob_ instanceof AS400JDBCClobLocator)
        {
            String stringToWrite = new String(byteArray, "ASCII");
            try
            {
                ((AS400JDBCClobLocator)lob_).setString(positionToStartWriting_, stringToWrite);
            }
            catch (SQLException e)
            {
                if (JDTrace.isTraceOn ())
                    e.printStackTrace (DriverManager.getLogStream ());
                closed_ = true;
                throw new IOException(e.getMessage());
            }
        }
        else if (lob_ instanceof AS400JDBCBlob)
        {
            try
            {
                ((AS400JDBCBlob)lob_).setBytes(positionToStartWriting_, byteArray);
            }
            catch (SQLException e)
            {
                if (JDTrace.isTraceOn ())
                    e.printStackTrace (DriverManager.getLogStream ());
                closed_ = true;
                throw new IOException(e.getMessage());
            }
        }
        else if (lob_ instanceof AS400JDBCBlobLocator)
        {
            try
            {
                ((AS400JDBCBlobLocator)lob_).setBytes(positionToStartWriting_, byteArray);
            }
            catch (SQLException e)
            {
                if (JDTrace.isTraceOn ())
                    e.printStackTrace (DriverManager.getLogStream ());
                closed_ = true;
                throw new IOException(e.getMessage());
            }  
        }
    }



    /*
    Write a byte to the output stream.

    @param b         The byte the user wants written to the output stream.  The general contract 
    for write is that one byte is written to the output stream. The byte to be written is the eight 
    low-order bits of the argument b. The 24 high-order bits of b are ignored. 
    */
    public void write(int b)
    throws IOException
    {
        // If the stream is closed.
        if (closed_)
            throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

        write(new byte[] { (byte)b });
    }

}
