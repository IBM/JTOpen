///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPSelection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPSelection.class - abstract class for a selection code point used
  * with the network print server's data stream.
  * This class is derived from NPCPAttributeValue and will be used to build a code
  * point that has as its data a list of attributes used to filter a list with.
  * its ID, its length, its type and an offset to its data.
**/

class NPCPSelection  extends NPCPAttributeValue
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



   /**
    * copy constructor
    */
    NPCPSelection(NPCPSelection    cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    */
    NPCPSelection()
    {
       super(NPCodePoint.SELECTION);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    */
    NPCPSelection( byte[] data )
    {
       super(NPCodePoint.SELECTION, data);
    }

    

}

