///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: WriterJobImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * The WriterJobImplProxy class implements proxy versions of
 * the public methods defined in the WriterJobImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (WriterJobImplRemote).
 **/

class WriterJobImplProxy extends PrintObjectImplProxy
implements WriterJobImpl, ProxyImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    
    
    WriterJobImplProxy()
    {
        super("WriterJob");
    }



    public void end(String endType)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "end",
                                   new Class [] { String.class },
                                   new Object[] { endType });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public NPCPIDWriter start(AS400Impl system, // @A1C 
                  PrintObjectImpl printer,          // @A1C
                  PrintParameterList options,  
                  OutputQueueImpl outputQueue)  // @A1C
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException
    {
        try {
            // @A1C - changed parms to send Impls in; changed return
            //        type from WriterJob to NPCPIDWriter
            return (NPCPIDWriter) connection_.callMethod(pxId_, "start",
                    new Class [] { AS400Impl.class, PrintObjectImpl.class, 
                                   PrintParameterList.class, OutputQueueImpl.class },
                    new Object[] { system, printer, options, outputQueue }).getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow4(e);
        }
    }

}
