///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: FTPInputStream.java
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

class FTPInputStream extends FilterInputStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    private FTP client_;
    Socket  dataSocket_;
    private boolean open_ = false;


    FTPInputStream(Socket dataSocket, FTP client)
                   throws IOException
    {
       super(dataSocket.getInputStream());

       dataSocket_ = dataSocket;
       client_     = client;
       open_       = true;

       if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"Opening the input stream");
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
              Trace.log(Trace.DIAGNOSTIC,"Closing the input stream");
       }
    }


    protected void finalize()
                   throws Throwable
    {
       super.finalize();
       close();
    }




}

