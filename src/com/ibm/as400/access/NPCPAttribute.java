///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPAttribute.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPAttribute   class - class for an attribute value list code point used with
  * the network print server's data stream.
  * This class is derived from NPCPAttribute  Value and will be used to build a code
  * point that has as its data a list of any attributes.  Each attribute consist of
  * its ID, its length, its type and an offset to its data.
  **/

class NPCPAttribute extends NPCPAttributeValue implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    protected Object clone()
    {
       NPCPAttribute cp = new NPCPAttribute(this);
       return cp;
    }

   /**
    * copy constructor
    */
    NPCPAttribute(NPCPAttribute   cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    */
    NPCPAttribute()
    {
       super(NPCodePoint.ATTRIBUTE_VALUE);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    */
    NPCPAttribute( byte[] data )
    {
       super(NPCodePoint.ATTRIBUTE_VALUE, data);
    }

    
}
