///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PxCompDS.java
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
import java.io.PrintWriter;
import java.util.Vector;



/**
The PxCompDS class represents a datastream
which contains 0 or more PxParms.
**/
abstract class PxCompDS
extends PxDS
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private Vector                  parameters_         = new Vector ();



/**
Constructs a PxCompDS object.

@param type The datastream type.  Valid values are listed in
            the ProxyConstants class.
**/
    public PxCompDS (short type)
    {
        super (type);
    }



    public void addObjectParm(Object object)
    {
        addObjectParm(null, object);
    }



    public void addObjectParm(PxTable pxTable, Object object)
    {
        PxParm parameter;

        // If the object is null, add it as such.
        if (object == null)
            parameter = new PxNullParm ();

        // If the object is a primitive type, add it as such.
        else if (object instanceof Byte)
            parameter = new PxByteParm (((Byte) object));
        else if (object instanceof Short)
            parameter = new PxShortParm (((Short) object));
        else if (object instanceof Integer)
            parameter = new PxIntParm (((Integer) object));
        else if (object instanceof Long)
            parameter = new PxLongParm (((Long) object));
        else if (object instanceof Float)
            parameter = new PxFloatParm (((Float) object));
        else if (object instanceof Double)
            parameter = new PxDoubleParm (((Double) object));
        else if (object instanceof Character)
            parameter = new PxCharParm (((Character) object));
        else if (object instanceof Boolean)
            parameter = new PxBooleanParm (((Boolean) object));

        // If the object is a String, add it as such.
        else if (object instanceof String)
            parameter = new PxStringParm ((String) object);

        // If the object is a proxy object, then send only the proxy
        // id.  Otherwise, if it is a non-proxy Toolbox object, send
        // it as a PxToolboxParm, since it may contain a proxy object,
        // it can't be serialized simply.  Anything else (non-Toolbox)
        // gets to be serialized.
        else {
            long proxyId = -1;
            if (object instanceof ProxyImpl)
                proxyId = ((ProxyImpl) object).getPxId();
            else if (pxTable != null) 
                proxyId = pxTable.get (object);
            
            if (proxyId >= 0)
                parameter = new PxPxObjectParm (proxyId);            
            else if (object.getClass().getName().startsWith("com.ibm.as400.access"))
                parameter = new PxToolboxObjectParm(object);
            else 
                parameter = new PxSerializedObjectParm (object); 
        }

        addParm (parameter);
    }



/**
Appends a parameter to the datastream.

@param parameter    The parameter.
**/
    public void addParm (PxParm parameter)
    {
        parameters_.addElement (parameter);
    }



/**
Clears the parameters.
**/
    public void clearParms ()
    {
        parameters_.removeAllElements ();
    }



/**
Returns a new copy of this datastream.

@return A new copy of this datastream.

@exception CloneNotSupportedException   If the object cannot be cloned.
**/
// 
// Implementation note:  This method is necessary in order to do 
//                       a deep copy of the internal Vector.  Otherwise,
//                       we run into problems with multiple threads.
    public Object clone ()
        throws CloneNotSupportedException
    {
        PxCompDS clone = (PxCompDS)super.clone();
        clone.parameters_ = (Vector)parameters_.clone();
        return clone;
    }



/**
Dumps the datastream for debugging and tracing.

@param output   The print writer.
**/
    public void dump (PrintWriter output)
    {
        synchronized (output) {
            super.dump (output);
            int numberOfParms = parameters_.size ();
            for (int j = 0; j < numberOfParms; ++j) {
                output.println ("   " + parameters_.elementAt (j).toString ());
            }
        }
       
    }
  


/**
Returns a parameter.

@param i    The parameter index (0-based).
@return     The parameter.
**/
    public PxParm getParm (int i)
    {
        return (PxParm) parameters_.elementAt (i);
    }



/**
Returns the parameter count.

@return The parameter count.
**/
    public int getParmCount ()
    {
        return parameters_.size ();
    }




//@A1M
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
        clearParms ();
        int numberOfParms = dataInput.readInt ();
        for (int i = 0; i < numberOfParms; ++i) {
            PxParm parameter = (PxParm) factory.getNextDS (input);
            addParm (parameter);
        }
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
        int numberOfParms = getParmCount ();
        dataOutput.writeInt (numberOfParms);
        for (int i = 0; i < numberOfParms; ++i) {
            getParm (i).writeTo (output);
        }
    }


}
