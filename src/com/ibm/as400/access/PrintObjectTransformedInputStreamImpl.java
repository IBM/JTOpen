///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectTransformedInputStreamImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

interface PrintObjectTransformedInputStreamImpl
{
    public abstract void createPrintObjectTransformedInputStream(SpooledFileImpl sf,
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



    public abstract int read(byte data[], int dataOffset, int length)
        throws IOException;



    public abstract long skip(long bytesToSkip)
        throws IOException;

}
