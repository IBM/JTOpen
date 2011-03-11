///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxDSFactory.java
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
import java.util.Hashtable;



/**
The PxDSFactory class manufactures new instances
of datastream objects as they are read from an input stream.
**/
class PxDSFactory 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private Hashtable        factory_            = new Hashtable ();



/**
Returns the next datastream object from the input stream.

@param input    The input stream.
@return         The next datastream object.

@exception      If an error occurs.
**/    
    public PxDSRV getNextDS (InputStream input)
        throws IOException
    {
        // Read the type of the next datastream.
        DataInputStream dataInput = new DataInputStream (input);
        Short type = new Short (dataInput.readShort());

        // If we know how to deal with this type, then manufacture
        // a new instance.
        if (factory_.containsKey (type)) {
            PxDSRV template = (PxDSRV) factory_.get (type);                           
            if (Trace.isTraceProxyOn())
                Trace.log(Trace.PROXY, "Factory read ds type " + type + " (" + template + ").");

            // We can not use the datastream template directly from the 
            // hashtable, since we may need more than one at a time.  Instead,
            // we make a copy of it.
            PxDSRV datastream = null;
            try {
                datastream = (PxDSRV) template.clone ();
            }
            catch (CloneNotSupportedException e) { 
                if (Trace.isTraceErrorOn ())
                    Trace.log (Trace.ERROR, "Clone error in ds factory", e);
            }

            // Loads the datastream by reading data from the input stream.  The
            // actually datastream subclass implements the details.
            datastream.readFrom (input, this);
            return datastream;
        }

        // Otherwise, this is an internal error.  If this happens,
        // make sure that all datastreams that you are expecting
        // are registered with this factory.
        else {
            if (Trace.isTraceProxyOn())
                Trace.log(Trace.PROXY, "Factory read ds type " + type + ".");
            if (Trace.isTraceOn ())
                Trace.log (Trace.ERROR, "Ds type " + type + " not registered in factory.");
            throw new InternalErrorException (InternalErrorException.DATA_STREAM_UNKNOWN);
        }
    }



/**
Registers a datastream with this factory.  DSs must be
registered in order to be recognized when they are read.

@param datastream   The datastream.
**/
    public void register (PxDSRV datastream)
    {
        // Add the class to the factory.
        Short key = new Short (datastream.getType ());
        if (factory_.contains (key))
            throw new InternalErrorException (InternalErrorException.PROTOCOL_ERROR);
        factory_.put (key, datastream);

        
    }



}
