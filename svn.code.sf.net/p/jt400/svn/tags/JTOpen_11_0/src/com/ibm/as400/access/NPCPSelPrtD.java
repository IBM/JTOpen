///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPSelPrtD.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPSelPrtD class - class for an attribute value list code point used with
  * the network print server's data stream.
  * This class is derived from NPCPSelection and will be used to build a code
  * point that has as its data a list of any attributes that can filter a
  * printer device list.
**/

class NPCPSelPrtD extends NPCPSelection implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


   /**
    * copy constructor
    **/
    NPCPSelPrtD(NPCPSelPrtD cp)
    {
        super(cp);
    }

   /**
    * basic constructor that creates an empty printer selection codepoint
    **/
    NPCPSelPrtD()
    {
        super();
    }

    protected Object clone()
    {
        NPCPSelPrtD cp = new NPCPSelPrtD(this);
        return cp;
    }

   

   /**
    * gets the printer filter.
    * @returns the printer filter or an empty string
    * if it has not been set
    **/
    String getPrinter()
    {
        String printer = getStringValue(PrintObject.ATTR_PRINTER);
        if( printer == null )
        {
            return emptyString;
        } else {
            return printer;
        }
    }

   /**
    * set printer filter
    * Removes the filter if printer is "".
    **/
    void setPrinter(String printer)
    {
        if( printer.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_PRINTER);
        } else {
            setAttrValue(PrintObject.ATTR_PRINTER, printer);
        }
    }

} // NPCPSelPrtD class
