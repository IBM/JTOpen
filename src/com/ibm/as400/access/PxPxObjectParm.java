///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxPxObjectParm.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;



/**
The PxPxObjectParm class represents a
proxy object parameter in a proxy datastream.
**/
//
// Implementation note:  
//
// On the client side, there is no proxy table.  Therefore, the
// proxy table here will be null.  This works as long as these
// parameters are only used to pass objects from the client
// to the proxy server, but not the reverse.
//
class PxPxObjectParm 
extends PxDS
implements PxParm
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private long        proxyId_        = -1;
    private PxTable  proxyTable_     = null;



    public PxPxObjectParm ()
    { 
        super (ProxyConstants.DS_PROXY_OBJECT_PARM);
    }


     
/**
Constructs a PxSerializedObjectParm object.

@param proxyTable   The proxy table.
**/
    public PxPxObjectParm (PxTable proxyTable)
    { 
        super (ProxyConstants.DS_PROXY_OBJECT_PARM);
        proxyTable_ = proxyTable;
    }



/**
Constructs a PxSerializedObjectParm object.

@param proxyId      The proxy id.
**/
    public PxPxObjectParm (long proxyId)
    {
        super (ProxyConstants.DS_PROXY_OBJECT_PARM);
        proxyId_ = proxyId;
    }



/**
Returns the Object value.

@return The Object value.
**/
    public Object getObjectValue ()
    {
        return proxyTable_.get (proxyId_);
    }



/**
Returns the proxy id.

@return The proxy id.
**/
    public long getPxId ()
    {
        return proxyId_;
    }



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
        proxyId_ = dataInput.readLong ();
    }



/**
Returns the String representation of the datastream.

@return The String representation of the datastream. 
**/
    public String toString ()
    {
        
        return super.toString () + " (" + proxyId_ + ")";
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
        dataOutput.writeLong (proxyId_);
     }



}
