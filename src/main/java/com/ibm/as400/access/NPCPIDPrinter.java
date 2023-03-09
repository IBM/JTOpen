///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPIDPrinter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * NPCPIDPrinter is used to contain a printer ID code point.
 * This code point has 1 value in it:
 *     NP_ATTR_PRINTER  - printer device name
 **/

class NPCPIDPrinter extends NPCPID implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


   /**
    * copy constructor
    */
    NPCPIDPrinter(NPCPIDPrinter cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    **/
    NPCPIDPrinter()
    {
       super(NPCodePoint.PRINTER_DEVICE_ID);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    **/
    NPCPIDPrinter( byte[] data )
    {
       super(NPCodePoint.PRINTER_DEVICE_ID, data);
    }

   /**
    * constructor that takes the ID as seperate items
    **/
    NPCPIDPrinter(String printer)
    {
       super(NPCodePoint.PRINTER_DEVICE_ID);
       setAttrValue(PrintObject.ATTR_PRINTER, printer);
    }

    protected Object clone()
    {
       NPCPIDPrinter cp = new NPCPIDPrinter(this);
       return cp;
    }

   

  /**
   * get the printer name
   **/
   String name()
   {
      return getStringValue(PrintObject.ATTR_PRINTER);
   }

} // NPCPIDPrinter

