///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: OutputQueueImpl.java
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
  * The OutputQueueImpl interface defines a set of methods
  * needed for a full implementation of the OutputQueue class.
 **/

interface OutputQueueImpl extends PrintObjectImpl

{
    public abstract void clear(PrintParameterList clearOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException;



    public abstract void hold()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException;



    public abstract void release()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException;

}
