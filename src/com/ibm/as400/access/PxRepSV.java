///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxRepSV.java
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
The PxRepSV class represents the client
portion of a reply.
**/
abstract class PxRepSV
extends PxCompDS
implements PxDSWV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private long    correlationId_  = -1;
    private long    clientId_       = -1;  //@B2A

/**
Constructs a PxRepSV object.

@param type The datastream type.  Valid values are listed in
            the ProxyConstants class.
**/
    public PxRepSV (short type)
    {
        super (type);
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
           output.println("   Client id = " + clientId_); //@B2A
        }
    }


    //@B3A  Add for tunneling.
    public long getClientId()
    {
	return clientId_;
    }
    

    public void setCorrelationId(long correlationId)
    {
        correlationId_ = correlationId;
    }


    //@B2A  Add for tunneling.
    public void setClientId(long clientId)
    {
        clientId_ = clientId;
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
        dataOutput.writeLong(clientId_); //@B2A
    }



}
