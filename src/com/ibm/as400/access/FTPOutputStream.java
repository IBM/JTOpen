///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FTPOutputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.net.*;
import java.util.*;

class FTPOutputStream extends FilterOutputStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    private FTP client_;
    private Socket    dataSocket_;
    private boolean   open_ = false;


    FTPOutputStream(Socket dataSocket, FTP client)
                    throws IOException
    {
       super(dataSocket.getOutputStream());
       dataSocket_ = dataSocket;
       client_ = client;
       open_ = true;

       if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"Opening the output stream");
    }


    public void close() throws IOException
    {
       if (open_)
       {
          super.close();
          dataSocket_.close();
          client_.readReply();
          open_ = false;

          if (Trace.isTraceOn())
              Trace.log(Trace.DIAGNOSTIC,"Closing the output stream");
       }
    }


    protected void finalize()
                   throws Throwable
    {
       super.finalize();
       close();
    }


    //@A1A: The performance of the default write() method in
    // FilterOutputStream is atrocious. The javadoc says it only
    // writes one byte at a time to the underlying stream and that
    // subclasses should provide a more efficient implementation.
    // We override it here to make it faster.
    public void write(byte[] b, int off, int len) throws IOException //@A1A
    {                                                                //@A1A
      out.write(b, off, len); // out is inherited from the parent      @A1A
    }                                                                //@A1A
}

