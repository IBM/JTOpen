///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxReqSV.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;



/**
The PxReqSV class represents the server
portion of a request.
**/
abstract class PxReqSV 
extends PxCompDS
implements PxDSRV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private long    correlationId_  = -1;
    private boolean asynchronous_   = false;
    private long    clientId_ 	    = -1; //@B2A



/**
Constructs a PxReqSV object.

@param type The datastream type.  Valid values are listed in
            the ProxyConstants class.
**/
    public PxReqSV (short type)
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
	   output.println("   Client id = " + clientId_);  //@B2A
	   if (asynchronous_)
                output.println ("   Asynchronous");
        }

        String x = Copyright.copyright;
    }
  

    //@B2A Added for tunneling
    public long getClientId()	      
    {                                 
	return clientId_;             
    }
    
     
     
    public long getCorrelationId()
    {
        return correlationId_;
    }



/**
Indicates if the request is asynchronous.

@return true if the request is asynchronous, false if the
        request is synchronous.
**/
    public boolean isAsynchronous ()
    {
        return asynchronous_;
    }



/**
Processes the request.

@return The corresponding reply, or null if none.
**/
    public abstract PxRepSV process ();



/**
Loads this datastream by reading from an input stream.

@param input    The input stream.
@param factory  The datastream factory.  This is sometimes
                needed when datastreams are nested.

@exception IOException  If an error occurs.                
**/
    public void readFrom (InputStream input, PxDSFactory factory)
        throws IOException
    {
        super.readFrom (input, factory);
        DataInputStream dataInput = new DataInputStream (input);
        correlationId_  = dataInput.readLong();
        asynchronous_   = dataInput.readBoolean();
	clientId_       = dataInput.readLong();  //@B2A 
    }



}
