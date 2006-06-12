///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.IOException;

/**
 *  The DataQueue class represents a data queue object.
 *  This class provides a modified subset of the functions available in 
 *  com.ibm.as400.access.DataQueue.
 *
 *  <P>The following example demonstrates the use of DataQueue:
 *  <br>
 *  <pre>
 *   AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
 *   try
 *   {
 *       // Write to the Data Queue.
 *       DataQueue.write(system, "/QSYS.LIB/FRED.LIB/MYDTAQ.DTAQ", "some text");
 *       
 *       // Read from the Data Queue.
 *       String txt = DataQueue.read(system, "/QSYS.LIB/FRED.LIB/MYDTAQ.DTAQ");
 *   }
 *   catch (Exception e)
 *   {
 *       // Handle the exception
 *   }
 *   // Done with the system object.
 *   system.disconnect();
 *  </pre>
 *
 *  @see com.ibm.as400.access.DataQueue
 **/
public final class DataQueue 
{
    /**
     *  Private DataQueue constructor.  The methods on DataQueue are static and
     *  therefore an object does not need to be created, hence the private constructor.
     **/
    private DataQueue()
    {  }


    /**
     *  Reads an entry from the data queue.
     *  
     *  @param  system  The system on which the data queue exists.
     *  @param  path  The fully qualified integrated file system path name of the data queue.
     *
     *  @return The bytes that were read.  If no entries were available, null is returned.
     *
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static byte[] readBytes(AS400 system, String path) throws IOException, MEException
    {

        if (system == null)
            throw new NullPointerException("system");
        
        if (path == null)
            throw new NullPointerException("path");

        synchronized(system)
        {
            system.connect();

            system.toServer_.writeInt(MEConstants.DATA_QUEUE_READ);
            system.toServer_.writeUTF(path);
            system.toServer_.writeInt(MEConstants.DATA_QUEUE_BYTES);
            system.toServer_.flush();

            int status = system.fromServer_.readInt();
            byte[]  buffer = null;
            
            if (status == MEConstants.DATA_QUEUE_ACTION_SUCCESSFUL)
            {
                buffer = new byte[system.fromServer_.readInt()];
                system.fromServer_.readFully(buffer);
                
                if (buffer.length == 0)
                    return null;
            }
            else if (status == MEConstants.EXCEPTION_OCCURRED)
            {
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg, rc);
            }

            return buffer;
        }
    }


    /**
     *  Reads an entry from the data queue.
     *  
     *  @param  system  The system on which the data queue exists.
     *  @param  path  The fully qualified integrated file system path name of the data queue.
     *
     *  @return The entry read from the queue. If no entries were available, null is returned.
     *
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static String readString(AS400 system, String path) throws IOException, MEException
    {
        if (system == null)
            throw new NullPointerException("system");

        if (path == null)
            throw new NullPointerException("path");

        synchronized(system)
        {
            system.connect();

            system.toServer_.writeInt(MEConstants.DATA_QUEUE_READ);
            system.toServer_.writeUTF(path);
            system.toServer_.writeInt(MEConstants.DATA_QUEUE_STRING);
            system.toServer_.flush();

            int status = system.fromServer_.readInt();

            String data = null;
            
            if (status == MEConstants.DATA_QUEUE_ACTION_SUCCESSFUL)
            {
                data = system.fromServer_.readUTF();
            }
            else if (status == MEConstants.EXCEPTION_OCCURRED)
            {
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg, rc);
            }

            if (data.equals(""))
                return null;
            else
                return data;
        }
    }


    /**
     *  Writes an entry to the data queue.
     *
     *  @param  system  The system on which the data queue exists.
     *  @param  path  The fully qualified integrated file system path name of the data queue.
     *  @param  data  The string to write to the queue.
     *
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static void write(AS400 system, String path, String data) throws IOException, MEException
    {
        if (system == null)
            throw new NullPointerException("system");
        
        if (path == null)
            throw new NullPointerException("path");
        
        if (data == null)
            throw new NullPointerException("data");

        synchronized(system)
        {
            system.connect();

            system.toServer_.writeInt(MEConstants.DATA_QUEUE_WRITE);
            system.toServer_.writeUTF(path);
            system.toServer_.writeInt(MEConstants.DATA_QUEUE_STRING);
            system.toServer_.writeUTF(data);
            system.toServer_.flush();

            int status = system.fromServer_.readInt();
            
            if (status == MEConstants.EXCEPTION_OCCURRED)
            {
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg, rc);
            }
        }
    }


    /**
     *  Writes an entry to the data queue.
     *
     *  @param  system  The system on which the data queue exists.
     *  @param  path  The fully qualified integrated file system path name of the data queue.
     *  @param  data  The array of bytes to write to the queue.
     *
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static void write(AS400 system, String path, byte[] data) throws IOException, MEException
    {
        if (system == null)
            throw new NullPointerException("system");
        
        if (path == null)
            throw new NullPointerException("path");
        
        if (data == null)
            throw new NullPointerException("data");

        synchronized(system)
        {
            system.connect();

            system.toServer_.writeInt(MEConstants.DATA_QUEUE_WRITE);
            system.toServer_.writeUTF(path);
            system.toServer_.writeInt(MEConstants.DATA_QUEUE_BYTES);
            system.toServer_.writeInt(data.length);
            system.toServer_.write(data, 0, data.length);
            system.toServer_.flush();

            int status = system.fromServer_.readInt();
            
            if (status == MEConstants.EXCEPTION_OCCURRED)
            {
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg, rc);
            }
        }
    }
}
