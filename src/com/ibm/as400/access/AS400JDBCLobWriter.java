///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCLobWriter.java
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
import java.io.Writer;
import java.sql.SQLException;




// JDBC 3.0 support
/**
The AS400JDBCLobWriter class provides a stream
to write into large objects.  The data is valid only within the current
transaction.  Users get one of these objects by calling Clob.setCharacterStream()
which returns an object of type Writer.
**/
class AS400JDBCLobWriter
extends Writer
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    //Private data
    boolean isStreamClosed_ = false;   // is the stream closed?
    long positionToStartWriting_;      // position from which the user wanted us to start writing
    Object lob_;                       // the lob object on which the user 
    //CharConverter convTable_;          // convTable to convert bytes


    AS400JDBCLobWriter (Object lob, long positionToStartWriting) //, CharConverter convTable)
    {
        lob_ = lob;
        positionToStartWriting_ = positionToStartWriting;
        //convTable_ = convTable;
    }


    public void close ()
    {
        isStreamClosed_ = true;
    }


    public void flush ()
    {
        //no-op
    }


    public void write (char[] cbuf)
    throws IOException
    {
        if (isStreamClosed_)
            throw new IOException("Stream is closed");
        write(new String(cbuf));
    }


    public void write (char[] cbuf, int off, int len)
    throws IOException
    {
        if (isStreamClosed_)
            throw new IOException("Stream is closed");
        
        write(new String(cbuf, off, len));
    }


    public void write(int c)
    throws IOException
    {
        if (isStreamClosed_)
            throw new IOException("Stream is closed");
        char[] charArray = new char[1];
        charArray[0] = (char) c;        
        write(charArray);
    }


    public void write (String str)
    throws IOException
    {
        if (isStreamClosed_)
            throw new IOException("Stream is closed");
        if (lob_ instanceof AS400JDBCClob)
        {
            try
            {
                ((AS400JDBCClob)lob_).setString(positionToStartWriting_, str);
            }
            catch (SQLException e)
            {
                throw new IOException(e.getMessage());
            }
        } 
        else if (lob_ instanceof AS400JDBCClobLocator)
        {
            try
            {
                ((AS400JDBCClobLocator)lob_).setString(positionToStartWriting_, str);
            }
            catch (SQLException e)
            {
                throw new IOException(e.getMessage());
            }
        }
        else if (lob_ instanceof AS400JDBCBlob)
        {
            byte[] byteArray = str.getBytes();//convTable_.stringToByteArray(str);
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
            byte[] byteArray = str.getBytes();//convTable.stringToByteArray(str);
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
    public void write(String str, int off, int len)
    throws IOException
    {
        if (isStreamClosed_)
            throw new IOException("Stream is closed");
        String stringToWrite = str.substring(off, len);
        write(stringToWrite);
    }
}
