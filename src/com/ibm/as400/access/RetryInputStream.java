///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RetryInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.access.Trace;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.SocketException;



/**
The RetryInputStream class filters another input stream.  If the
underlying input stream throws a SocketException, this automatically
retries the read.
**/
//
// Implementation note:  This class provides a workaround for
// a Netscape bug where the Netscape JVM throws sporadic SocketExceptions,
// even though the socket is not closed and successive reads still
// work.
//
class RetryInputStream
extends FilterInputStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    private static final int exceptionThreshold_ = 3;



    public RetryInputStream(InputStream in)
    {
        super(in);
    }



    public int read() throws IOException
    {
        int exceptionCount = 0;
        while(true) {
            try {
                return super.read();
            }
            catch(SocketException e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Socket closed prematurely, let's try again", e);
                if ((++exceptionCount) >= exceptionThreshold_)
                    throw e;
            }
        }
    }


    
    public int read(byte[] b) throws IOException
    {
        int exceptionCount = 0;
        while(true) {
            try {
                return super.read(b);
            }
            catch(SocketException e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Socket closed prematurely, let's try again", e);
                if ((++exceptionCount) >= exceptionThreshold_)
                    throw e;
            }
        }
    }



    public int read(byte[] b, int offset, int length) throws IOException
    {
        int exceptionCount = 0;
        while(true) {
            try {
                return super.read(b, offset, length);
            }
            catch(SocketException e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Socket closed prematurely, let's try again", e);
                if ((++exceptionCount) >= exceptionThreshold_)
                    throw e;
            }
        }
    }


}
