///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPIDLibrary.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * NPCPIDLibrary is used to contain a library ID code point.
 * This code point has 1 value in it:
 *     NP_ATTR_LIBRARY  - library name
 *
 **/

class NPCPIDLibrary extends NPCPID implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   /**
    * copy constructor
    **/
    NPCPIDLibrary(NPCPIDLibrary cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    **/
    NPCPIDLibrary()
    {
       super(NPCodePoint.LIBRARY_ID);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    **/
    NPCPIDLibrary( byte[] data )
    {
       super(NPCodePoint.LIBRARY_ID, data);
    }

   /**
    * constructor that takes the ID as seperate items
    **/
    NPCPIDLibrary(String library)
    {
       super(NPCodePoint.LIBRARY_ID);
       setAttrValue(PrintObject.ATTR_LIBRARY, library);
    }


    protected Object clone()
    {
       NPCPIDLibrary cp = new NPCPIDLibrary(this);
       return cp;
    }

    

  /**
   * get the library name
   **/
   String name()
   {
      return getStringValue(PrintObject.ATTR_LIBRARY);
   }

} // NPCPIDLibrary

