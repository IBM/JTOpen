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
    boolean isStreamClosed_ = false;   // is the stream closed?
    long positionToStartWriting_;      // position from which the user wanted us to start writing
    Object lob_;                       // the lob object on which the user 


    AS400JDBCLobOutputStream (Object lob, long positionToStartWriting)
    {
        lob_ = lob;
        positionToStartWriting_ = positionToStartWriting;
    }

    public void close ()
    {
        isStreamClosed_ = true;
    }

    public void flush ()
    {
        //no-op
    }

    public void write (byte[] byteArray)
    throws IOException
    {
        if (isStreamClosed_)
            throw new IOException("Stream is closed");
        if (lob_ instanceof AS400JDBCClob)
        {
            String stringToWrite = new String(byteArray);   //convTable_.byteArrayToString
            try
            {
                ((AS400JDBCClob)lob_).setString(positionToStartWriting_, stringToWrite);
            }
            catch (SQLException e)
            {
                throw new IOException(e.getMessage());
            }
        }
        else if (lob_ instanceof AS400JDBCClobLocator)
        {
            String stringToWrite = new String(byteArray);   //convTable_.byteArrayToString
            try
            {
                ((AS400JDBCClobLocator)lob_).setString(positionToStartWriting_, stringToWrite);
            }
            catch (SQLException e)
            {
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
                throw new IOException(e.getMessage());
            }  
        }
    }

    public void write (byte[] byteArray, int off, int len)
    throws IOException
    {
        if (isStreamClosed_)
            throw new IOException("Stream is closed");
        byte[] bytesToPutIntoArray = new byte[len];
        System.arraycopy(byteArray, off, bytesToPutIntoArray, 0, len);
        write(bytesToPutIntoArray);
    }

    public void write(int b)
    throws IOException
    {
        if (isStreamClosed_)
            throw new IOException("Stream is closed");
        byte[] byteArray = new byte[1];
        byteArray[0] = (byte) b;
        write(byteArray);
    }

}
