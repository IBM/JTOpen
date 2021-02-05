///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxSerializedObjectParm.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;



/**
The PxSerializedObjectParm class represents a
serialized object parameter in a proxy datastream.
**/
class PxSerializedObjectParm 
extends PxDS
implements PxParm
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private Object      value_;



/**
Constructs a PxSerializedObjectParm object.
**/
    public PxSerializedObjectParm ()
    { 
        super (ProxyConstants.DS_SERIALIZED_OBJECT_PARM);
    }



/**
Constructs a PxSerializedObjectParm object.

@param value    The Object value.
**/
    public PxSerializedObjectParm (Object value)
    {
        super (ProxyConstants.DS_SERIALIZED_OBJECT_PARM);
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


    public static String[][] permittedIoExceptions = {
        {"com.ibm.as400.access.ConvTableReader", null },
        {"java.io.Reader", "mark"},
        {"java.io.Reader", "reset"},
    };

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
        ObjectInputStream objectInput = new ObjectInputStream (input);
        try {
            value_ = objectInput.readObject ();
            
            if (value_ instanceof IOException) { 
              if (value_ instanceof ExtendedIOException) {
                // Do not throw ExtendedIOException since toolbox APIS return that. 
              } else {
                // 
                // There are some cases where valid IO exceptions are returned.. 
                // Let those exceptions through
                
                boolean found = false; 
          StackTraceElement[] stackTrace = ((IOException) value_)
              .getStackTrace();
          for (int i = 0; i < stackTrace.length && (!found); i++) {
            for (int j = 0; j < permittedIoExceptions.length
                && (!found); j++) {
              if (stackTrace[i].getClassName()
                  .equals(permittedIoExceptions[j][0])) {
                if ((permittedIoExceptions[j][1] == null) || 
                    stackTrace[i].getMethodName()
                    .equals(permittedIoExceptions[j][1])) {
                  found = true;
                }
              }
            }
          }
          if (!found) {
            if (Trace.isTraceErrorOn())
              Trace.log(Trace.ERROR,
                  "Throwing deserializing IOException " + value_);
            throw (IOException) value_;
          }
        }
      }
        }
        catch (ClassNotFoundException e) {
            if (Trace.isTraceErrorOn())
                Trace.log(Trace.ERROR, "Class for deserializing not found", e);
            IOException throwException =new IOException ("Class not found:" + e.getMessage ());
            try {
              throwException.initCause(e); 
            } catch (Throwable t) {} 
            throw throwException;  
        }
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
	    ObjectOutputStream objectOutput = new ObjectOutputStream (output);
	    if (value_ instanceof Serializable) {
	      objectOutput.writeObject (value_);
	    } else {
	      Exception e = new java.io.NotSerializableException(value_.getClass().getName()); 
	      objectOutput.writeObject(e);
	    }

       // We need to flush because ObjectOutputStream seems
       // to be buffering.
	    objectOutput.flush ();
     }


}
