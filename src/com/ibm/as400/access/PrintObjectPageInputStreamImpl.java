///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PrintObjectPageInputStreamImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

interface PrintObjectPageInputStreamImpl
{
    public abstract void createPrintObjectPageInputStream(SpooledFileImpl sf,
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



    public abstract int getCurrentPageNumber();



    public abstract int getNumberOfPages();



    public abstract boolean isPagesEstimated();



    // NOTE: Interface methods cannot be synchronized...
    public abstract /* synchronized */  void mark(int readLimit);



    public boolean nextPage()
        throws IOException;



    public boolean previousPage()
        throws IOException;



    public abstract int read(byte data[], int dataOffset, int length)
        throws IOException;



    // NOTE: Interface methods cannot be synchronized...
    public abstract /* synchronized */ void reset()
        throws IOException;



    public abstract boolean selectPage(int page)
        throws IOException, IllegalArgumentException;



    public abstract long skip(long bytesToSkip)
        throws IOException;

}
