///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxByteParm.java
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
The PxByteParm class represents a byte
parameter in a proxy datastream.
**/
class PxByteParm 
extends PxDS
implements PxParm 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private byte             value_;


         
/**
Constructs a PxByteParm object.
**/
    public PxByteParm ()
    { 
        super (ProxyConstants.DS_BYTE_PARM);
    }



/**
Constructs a PxByteParm object.

@param value    The byte value.
**/
    public PxByteParm (byte value)
    {
        super (ProxyConstants.DS_BYTE_PARM);
        value_ = value;
    }



/**
Constructs a PxByteParm object.

@param value    The byte value.
**/
    public PxByteParm (Byte value)
    {
        this (value.byteValue ());
    }



/**
Returns the byte value.

@return The byte value.
**/
    public byte getByteValue ()
    {
        return value_;
    }



/**
Returns the Object value.

@return The Object value.
**/
    public Object getObjectValue ()
    {
        return new Byte (value_);
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
        value_ = dataInput.readByte ();
    }



/**
Returns the String representation of the datastream.

@return The String representation of the datastream. 
**/
    public String toString ()
    {
        
        return super.toString () + " (" + value_ + ")";
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
        dataOutput.writeByte (value_);
     }



}
