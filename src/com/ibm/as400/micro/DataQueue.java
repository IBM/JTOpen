///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DataQueue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.IOException;
import com.ibm.as400.access.MEConstants;

/**
 The DataQueue class represents an iSeries data queue object.
 <P>The following example demonstrates the use of DataQueue:
 <br>
 <pre>
    AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
    try
    {
        // Write to the Data Queue.
        DataQueue.write(system, "/QSYS.LIB/FRED.LIB/MYDTAQ.DTAQ", "some text");
        
        // Read from the Data Queue.
        String txt = DataQueue.read(system, "/QSYS.LIB/FRED.LIB/MYDTAQ.DTAQ");
    }
    catch (Exception e)
    {
        System.out.println("Data Queue read/wirte failed!");
        e.printStackTrace();
    }
    // Done with the system.
    system.disconnect();
 </pre>
 **/
public final class DataQueue 
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    /**
     *  Private DataQueue constructor.  The methods on DataQueue are static and
     *  therefore an object does not need to be created, hence the private constructor.
     **/
    private DataQueue()
    {  }


    /**
     *  Reads an entry from the data queue.  An exception is thrown when running to a pre-V4R5 
     *  server if the maximum length of a message on the queue is greater than 31744 bytes.
     *  
     *  @param  system  The system on which the data queue exists.
     *  @param  path  The fully qualified integrated file system path name of the data queue.
     *  @param  b  the buffer into which the data is read.
     *
     *  @return the total number of bytes read into the buffer.
     *
     *  @exception  IOException  If an error occurs while communicating with the system.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static int readBytes(AS400 system, String path, byte[] b) throws IOException, MEException
    {
        synchronized(system)
        {
            system.signon();

            system.toServer_.writeInt(MEConstants.DATA_QUEUE_READ);
            system.toServer_.writeUTF(path);
            system.toServer_.writeInt(MEConstants.DATA_QUEUE_BYTES);
            system.toServer_.flush();

            int status = system.fromServer_.readInt();
            int len = 0;

            if (status == MEConstants.DATA_QUEUE_WRITE_READ_SUCCESSFUL)
            {
                len = system.fromServer_.readInt();
                
                if (len != 0)
                {
                    for (int i=0; i<len; ++i)
                    {
                        b[i] = system.fromServer_.readByte();
                    }
                }
            }
            else if (status == MEConstants.EXCEPTION_OCCURRED)
            {
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg, rc);
            }

            return len;
        }
    }


    /**
     *  Reads an entry from the data queue.  An exception is thrown when running to a pre-V4R5 
     *  server if the maximum length of a message on the queue is greater than 31744 bytes.
     *  
     *  @param  system  The system on which the data queue exists.
     *  @param  path  The fully qualified integrated file system path name of the data queue.
     *
     *  @return The entry read from the queue. If no entries were available, null is returned.
     *
     *  @exception  IOException  If an error occurs while communicating with the system.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static String readString(AS400 system, String path) throws IOException, MEException
    {
        synchronized(system)
        {
            system.signon();

            system.toServer_.writeInt(MEConstants.DATA_QUEUE_READ);
            system.toServer_.writeUTF(path);
            system.toServer_.writeInt(MEConstants.DATA_QUEUE_STRING);
            system.toServer_.flush();

            int status = system.fromServer_.readInt();

            String data = null;
            
            if (status == MEConstants.DATA_QUEUE_WRITE_READ_SUCCESSFUL)
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
     *  @exception  IOException  If an error occurs while communicating with the system.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static void write(AS400 system, String path, String data) throws IOException, MEException
    {
        synchronized(system)
        {
            system.signon();

            system.toServer_.writeInt(MEConstants.DATA_QUEUE_WRITE);
            system.toServer_.writeUTF(path);
            system.toServer_.writeInt(MEConstants.DATA_QUEUE_STRING);
            system.toServer_.writeUTF(data);
            system.toServer_.flush();

            int status = system.fromServer_.readInt();
            
            if (status == MEConstants.DATA_QUEUE_WRITE_READ_SUCCESSFUL)
            {
            }
            else if (status == MEConstants.EXCEPTION_OCCURRED)
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
     *  @exception  IOException  If an error occurs while communicating with the system.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static void write(AS400 system, String path, byte[] data) throws IOException, MEException
    {
        synchronized(system)
        {
            system.signon();

            system.toServer_.writeInt(MEConstants.DATA_QUEUE_WRITE);
            system.toServer_.writeUTF(path);
            system.toServer_.writeInt(MEConstants.DATA_QUEUE_BYTES);
            system.toServer_.writeInt(data.length);
            system.toServer_.write(data, 0, data.length);
            system.toServer_.flush();

            int status = system.fromServer_.readInt();
            
            if (status == MEConstants.DATA_QUEUE_WRITE_READ_SUCCESSFUL)
            {
            }
            else if (status == MEConstants.EXCEPTION_OCCURRED)
            {
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg, rc);
            }
        }
    }
}
