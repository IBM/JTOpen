///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectInputStreamImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

interface PrintObjectInputStreamImpl
{
    public abstract void createPrintObjectInputStream(SpooledFileImpl sf,
                                                      PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException;
//B1A
   public abstract void createPrintObjectInputStream(SpooledFileImpl sf,
                                                      PrintParameterList openOptions,
                                                      String acifImp)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException;

//B1A
    public abstract void createPrintObjectInputStream(PrintObjectImpl resource,
                                                      PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException;



    public abstract int available()
         throws IOException;



    public abstract void close()
       throws IOException;



    // NOTE: Interface methods cannot be synchronized...
    public abstract void mark(int readLimit);



    public abstract int read(byte data[], int dataOffset, int length)
        throws IOException;



    // NOTE: Interface methods cannot be synchronized...
    public abstract void reset()
        throws IOException;



    public abstract long skip(long bytesToSkip)
        throws IOException;

}
