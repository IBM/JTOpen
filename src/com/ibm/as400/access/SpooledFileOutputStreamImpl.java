///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileOutputStreamImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
  * The SpooledFileOutputStreamImpl interface defines a set of methods
  * needed for a full implementation of the SpooledFileOutputStream class.
 **/

interface SpooledFileOutputStreamImpl
{
   public abstract void createSpooledFileOutputStream(AS400Impl system, 
                                        PrintParameterList options,
                                        PrinterFileImpl printerFile,
                                        OutputQueueImpl outputQueue)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException;



    public abstract void close()
       throws IOException;



    public abstract void flush()
        throws IOException;



    public abstract /* synchronized */ NPCPIDSplF getSpooledFile() 
       throws IOException;



    public abstract /* synchronized */ void write(byte data[], int offset, int length)
        throws IOException;
}
