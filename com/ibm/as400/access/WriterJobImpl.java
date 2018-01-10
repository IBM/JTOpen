///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WriterJobImpl.java
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
  * The WriterJobImpl interface defines a set of methods
  * needed for a full implementation of the WriterJob class.
 **/

interface WriterJobImpl extends PrintObjectImpl
{
    public void end(String endType)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException;



    public  NPCPIDWriter start(AS400Impl system, 
                               PrintObjectImpl printer,
                               PrintParameterList options,
                               OutputQueueImpl outputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException;
}
