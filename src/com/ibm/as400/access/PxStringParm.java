///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxStringParm.java
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
The PxStringParm class represents a String
parameter in a proxy datastream.
**/
class PxStringParm 
extends PxDS
implements PxParm 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



//@A2M
    private static final String ENCODING_   = "UTF8";



    // Private data.
    private String          value_;



/**
Constructs a PxStringParm object.
**/
    public PxStringParm ()
    {
        super (ProxyConstants.DS_STRING_PARM);
    }



/**
Constructs a PxStringParm object.

@param value    The String value.
**/
    public PxStringParm (String value)
    {
        super (ProxyConstants.DS_STRING_PARM);
        value_ = value;
    }



/**
Constructs a PxStringParm object.

@param type     The datastream type.
**/
    protected PxStringParm (short type)
    {
        super (type);
    }



/**
Constructs a PxStringParm object.

@param type     The datastream type.
@param value    The String value.
**/
    protected PxStringParm (short type, String value)
    {
        super (type);
        value_ = value;
    }



/**
Returns a new copy of this datastream.

@return A new copy of this datastream.

@exception CloneNotSupportedException   If the object cannot be cloned.
**/
// 
// Implementation note:  This method is necessary in order to do 
//                       a deep copy of the internal object.  Otherwise,
//                       we run into problems with multiple threads.
    public Object clone ()
        throws CloneNotSupportedException
    {
        value_ = null;
        return super.clone();
    }



/**
Returns the Object value.

@return The Object value.
**/
    public Object getObjectValue ()
    {
        return value_;
    }



/**
Returns the String value.

@return The String value.
**/
    public String getStringValue ()
    {
        return value_;
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
        int length = dataInput.readInt ();
        if (length > 0) {
            byte[] buffer = new byte[length];
            
            // @A1D StringBuffer value = new StringBuffer();
            int leftToRead = length;
            int bytesRead = 0;                                              // @A1A
            while (leftToRead > 0) {            
                int count = input.read(buffer, bytesRead, leftToRead);      // @A1C
                // @A1D value.append(new String(buffer, 0, count, ENCODING_)); 
                leftToRead -= count;
                bytesRead += count;                                         // @A1A
            }
            value_ = new String(buffer, 0, length, ENCODING_);              // @A1C
        }
        else
            value_ = "";
    }



/**
Returns the String representation of the datastream.

@return The String representation of the datastream. 
**/
    public String toString ()
    {
        
        if (value_ != null)
            return super.toString () + " (\"" + value_ + "\")";
        else
            return super.toString ();
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
        if (value_ != null) {
            byte[] buffer = value_.getBytes (ENCODING_);
            dataOutput.writeInt (buffer.length);
            output.write (buffer);
        }
        else
            dataOutput.writeInt (0);
     }
}
