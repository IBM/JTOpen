///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxClassParm.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Hashtable;



/**
The PxClassParm class represents a Class
parameter in a proxy datastream.
**/
class PxClassParm 
extends PxStringParm 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static Hashtable primitiveClasses_;



/**
Static initializer.
**/
    static
    {
        primitiveClasses_ = new Hashtable ();
        primitiveClasses_.put ("byte", Byte.TYPE);
        primitiveClasses_.put ("short", Short.TYPE);
        primitiveClasses_.put ("int", Integer.TYPE);
        primitiveClasses_.put ("long", Long.TYPE);
        primitiveClasses_.put ("float", Float.TYPE);
        primitiveClasses_.put ("double", Double.TYPE);
        primitiveClasses_.put ("char", Character.TYPE);
        primitiveClasses_.put ("boolean", Boolean.TYPE);

        
    }



/**
Constructs a PxClassParm object.
**/
    public PxClassParm ()
    { 
        super (ProxyConstants.DS_CLASS_PARM);
    }



/**
Constructs a PxClassParm object.

@param value    The Class value.
**/
    public PxClassParm (Class value)
    {
        super (ProxyConstants.DS_CLASS_PARM, (value != null) ? value.getName () : "");
    }



/**
Returns the Class value.

@return The Class value.
**/
    public Class getClassValue ()
        throws ClassNotFoundException
    {
        String value = getStringValue ();
        if (value.length () == 0)
            return null;

        if (primitiveClasses_.containsKey (value))
            return (Class) primitiveClasses_.get (value);
        else
            return Class.forName (value);
    }



}
