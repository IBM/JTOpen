///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPID.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPID class - class for an attribute value list code point used with
  * the network print server's data stream to identify AS/400 objects.
  * This class is derived from NPCPAttributeValue and will be used to build a code
  * point that has as its data a list of certain attributes that ID an object.
  * This is an abstract class that will do the general enforcement of what can
  * and cannot be set for this codepoint.
  **/

abstract class NPCPID extends NPCPAttributeValue
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   /**
    * copy constructor
    */
    NPCPID(NPCPID cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    */
    NPCPID(int ID)
    {
       super(ID);                            // construct codepoint with this ID
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    */
    NPCPID( int ID, byte[] data )
    {
       super(ID, data);                     // construct codepoint with this ID
    }

    
} // end NPCPID

