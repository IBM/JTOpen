///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPIDOutQ.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * NPCPIDOutQ is used to contain an output queue ID code point.
 * This code point has 2 values in it:
 *     NP_ATTR_OUTQUE    - output queue name
 *     NP_ATTR_OUTQUELIB - output queue library
 **/

class NPCPIDOutQ extends NPCPID implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


   /**
    * copy constructor
    **/
    NPCPIDOutQ(NPCPIDOutQ cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    **/
    NPCPIDOutQ()
    {
       super(NPCodePoint.OUTPUT_QUEUE_ID);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    **/
    NPCPIDOutQ( byte[] data )
    {
       super(NPCodePoint.OUTPUT_QUEUE_ID, data);
    }

   /**
    * constructor that takes the ID as seperate items
    **/
    NPCPIDOutQ(String queueName,
                      String queueLib)
    {
       super(NPCodePoint.OUTPUT_QUEUE_ID);
       setAttrValue(PrintObject.ATTR_OUTQUE, queueName);
       setAttrValue(PrintObject.ATTR_OUTQUELIB, queueLib);
    }


    protected Object clone()
    {
	NPCPIDOutQ cp = new NPCPIDOutQ(this);
	return cp;
    }

    

    /**
     * get the queue library
     */
    String library()
    {
	return getStringValue(PrintObject.ATTR_OUTQUELIB);
    }

    /**
     * get the queue name
     */
    String name()
    {
	return getStringValue(PrintObject.ATTR_OUTQUE);
    }

} // NPCPIDOutQ

