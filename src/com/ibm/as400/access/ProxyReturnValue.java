///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ProxyReturnValue.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The ProxyReturnValue class encapsulates all of the
information which is returned from a method or constructor
call.
**/
//
// Implementation note:
//
// The return value is stored in a PxParm object
// in order to avoid creating an extra object when the
// value is a primitive type.  
//
class ProxyReturnValue
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private PxParm      returnValue_;
    private PxParm[]    arguments_;



/**
Constructs a ProxyReturnValue object.

@param returnValue  The return value.
@param arguments    The arguments.
**/
    public ProxyReturnValue (PxParm returnValue, PxParm[] arguments)
    {
        returnValue_    = returnValue;
        arguments_      = arguments;

        
    }



/**
Returns an argument.

@param i    The index.
@return     The argument.
**/
    public Object getArgument (int i)
    {
        return arguments_[i].getObjectValue ();
    }



/**
Returns the return value.

@return The return value.
**/
    public Object getReturnValue ()
    {
        return returnValue_.getObjectValue ();
    }



/**
Returns the return value as a byte.

@return The return value.
**/
    public byte getReturnValueByte ()
    {
        return ((PxByteParm) returnValue_).getByteValue ();
    }



/**
Returns the return value as a short.

@return The return value.
**/
    public short getReturnValueShort ()
    {
        return ((PxShortParm) returnValue_).getShortValue ();
    }



/**
Returns the return value as a int.

@return The return value.
**/
    public int getReturnValueInt ()
    {
        return ((PxIntParm) returnValue_).getIntValue ();
    }



/**
Returns the return value as a long.

@return The return value.
**/
    public long getReturnValueLong ()
    {
        return ((PxLongParm) returnValue_).getLongValue ();
    }



/**
Returns the return value as a float.

@return The return value.
**/
    public float getReturnValueFloat ()
    {
        return ((PxFloatParm) returnValue_).getFloatValue ();
    }



/**
Returns the return value as a double.

@return The return value.
**/
    public double getReturnValueDouble ()
    {
        return ((PxDoubleParm) returnValue_).getDoubleValue ();
    }



/**
Returns the return value as a char.

@return The return value.
**/
    public char getReturnValueChar ()
    {
        return ((PxCharParm) returnValue_).getCharValue ();
    }



/**
Returns the return value as a boolean.

@return The return value.
**/
    public boolean getReturnValueBoolean ()
    {
        return ((PxBooleanParm) returnValue_).getBooleanValue ();
    }



/**
Returns the return value as a proxy id.

@return The return value, or -1 if the return value was null.
**/
    public long getReturnValuePxId ()
    {
        if (returnValue_ instanceof PxNullParm)
            return -1;
        else
            return ((PxPxObjectParm) returnValue_).getPxId ();
    }



}

