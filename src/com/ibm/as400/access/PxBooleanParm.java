///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxBooleanParm.java
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
The PxBooleanParm class represents a boolean
parameter in a proxy datastream.
**/
class PxBooleanParm 
extends PxDS
implements PxParm
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private boolean             value_;



/**
Constructs a PxBooleanParm object.
**/
    public PxBooleanParm ()
    { 
        super (ProxyConstants.DS_BOOLEAN_PARM);
    }



/**
Constructs a PxBooleanParm object.

@param value    The boolean value.
**/
    public PxBooleanParm (boolean value)
    {
        super (ProxyConstants.DS_BOOLEAN_PARM);
        value_ = value;
    }



/**
Constructs a PxBooleanParm object.

@param value    The boolean value.
**/
    public PxBooleanParm (Boolean value)
    {
        this (value.booleanValue ());
    }



/**
Returns the boolean value.

@return The boolean value.
**/
    public boolean getBooleanValue ()
    {
        return value_;
    }



/**
Returns the Object value.

@return The Object value.
**/
    public Object getObjectValue ()
    {
        return new Boolean (value_);
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
        value_ = dataInput.readBoolean ();
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
        dataOutput.writeBoolean (value_);
     }



}
