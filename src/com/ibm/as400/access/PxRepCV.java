///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxRepCV.java
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
import java.lang.reflect.InvocationTargetException;



/**
The PxRepCV class represents the client
portion of a reply.
**/
abstract class PxRepCV 
extends PxCompDS
implements PxDSRV
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private long    correlationId_  = -1;



/**
Constructs a PxRepCV object.

@param type The datastream type.  Valid values are listed in
            the ProxyConstants class.
**/
    public PxRepCV (short type)
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
        }

      
    }
  


    public long getCorrelationId()
    {
        return correlationId_;
    }


                                         
/**
Processes the reply.

@return The returned object, or null if none.
**/
    public abstract Object process ()
        throws InvocationTargetException;



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
    }



}
