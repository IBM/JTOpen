///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;



/**
The PxDS is the super class for all proxy datastreams.
**/
abstract class PxDS 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private short                    type_               = -1;



/**
Constructs a PxDS object.

@param  type    The datastream type.   Valid values are
                in the ProxyConstants class.  It is assumed that
                the caller is passing a valid type.
**/
    public PxDS (short type)
    {
        type_ = type;
    }



/**
Returns a new copy of this datastream.

@return A new copy of this datastream.

@exception CloneNotSupportedException   If the object cannot be cloned.
**/
    public Object clone ()
        throws CloneNotSupportedException
    {
        return super.clone ();
    }



/**
Dumps the datastream for debugging and tracing.

@param output   The print writer to which to dump the datastream.
**/
    public void dump (PrintWriter output)
    {
        output.println ("DS: " + this);
        
    }



/**
Returns the datastream type.

@return The datastream type.
**/
    public short getType ()
    {
        return type_;
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
    }



/**
Returns the String representation of the datastream.

@return The String representation of the datastream. 
**/
    public String toString ()
    {
        String fullyQualifiedClassName = getClass ().getName ();
        return fullyQualifiedClassName.substring (fullyQualifiedClassName.lastIndexOf ('.') + 1);
    }



/**
Writes the contents of the datastream to an output stream.

@param output   The output stream.

@exception IOException  If an error occurs.                
**/
    public void writeTo (OutputStream output)
        throws IOException
    {
        DataOutputStream dataOutput = new DataOutputStream (output);
        dataOutput.writeShort (getType ());
    }


}
