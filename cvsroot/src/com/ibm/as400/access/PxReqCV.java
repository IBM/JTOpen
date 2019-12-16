///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxReqCV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;



/**
The PxReqCV class represents the client
portion of a request.
**/
abstract class PxReqCV
extends PxCompDS
implements PxDSWV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static long     nextCorrelationId_      = 0;
    private static Object   nextCorrelationIdLock_  = new Object();

    private long    correlationId_;
    private boolean asynchronous_;

    private long    clientId_ = -1;    // @D1a @D2C



/**
Constructs a PxReqCV object.

@param type The datastream type.  Valid values are listed in
            the ProxyConstants class.
**/
    public PxReqCV (short type)
    {
        this(type, false);
    }



/**
Constructs a PxReqCV object.

@param type The datastream type.  Valid values are listed in
            the ProxyConstants class.
@param asynchronous true if asynchronous, false otherwise.
**/
    public PxReqCV (short type, boolean asynchronous)
    {
        super (type);
        asynchronous_ = asynchronous;

        synchronized(nextCorrelationIdLock_) {
            correlationId_ = ++nextCorrelationId_;
        }
    }



/**
Dumps the datastream for debugging and tracing.

@param output   The print writer.
**/
    public void dump (PrintWriter output)
    {
        synchronized (output) {
            super.dump (output);
            output.println("   Correlation id = " + correlationId_);
            if (asynchronous_)
                output.println("   Asynchronous");

            // @D1 dump the client id
            output.println("   Client id = " + clientId_);  // @D2C
            // @D2D for (int i=0; i<clientId_.length; i++)
            // @D2D    output.print(clientId_[i]);
            // @D2D output.println();
        }
    }



    public long getCorrelationId()
    {
        return correlationId_;
    }



/**
Writes the contents of the datastream to an output stream.

@param output   The output stream.

@exception IOException  If an error occurs.
**/
    public void writeTo (OutputStream output)
        throws IOException
    {
        super.writeTo (output);
        DataOutputStream dataOutput = new DataOutputStream (output);
        dataOutput.writeLong(correlationId_);
        dataOutput.writeBoolean(asynchronous_);

        // If given a client ID, add it to the end of the stream
        // @D2D if (clientId_ != null)            // @D1a
           dataOutput.writeLong(clientId_);   // @D1a @D2C
    }


//
// @D1a New method ... @D2C
// Set the client ID to append to the end of the stream.
//

    void setClientId(long newValue)  // @D2C
    {
        clientId_ = newValue;
    }

}
