///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPSelPrtF.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPSelPrtF class - class for an attribute value list code point used with
  * the network print server's data stream.
  * This class is derived from NPCPSelection and will be used to build a code
  * point that has as its data a list of any attributes that can filter a
  * printer file list.
**/

class NPCPSelPrtF extends NPCPSelection implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   /**
    * copy constructor
    **/
    NPCPSelPrtF(NPCPSelPrtF cp)
    {
        super(cp);
    }

   /**
    * basic constructor that creates an empty printer selection codepoint
    **/
    NPCPSelPrtF()
    {
        super();
    }

    protected Object clone()
    {
        NPCPSelPrtF cp = new NPCPSelPrtF(this);
        return cp;
    }

    

   /**
    * gets the printer file filter
    * @returns The printer file filter or an empty string if not set.
    **/
    String getPrinterFile()
    {
        String printerFile = getStringValue(PrintObject.ATTR_PRINTER_FILE);
        if( printerFile == null )
        {
            return emptyString;
        } else {
            return printerFile;
        }
    }

   /**
    * sets the printer file filter using an IFS path for the printer file.
    * Removes the filter if ifsPrinterFile is "".
    **/
    void setPrinterFile(String ifsPrinterFile)
    {
        if( ifsPrinterFile.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_PRINTER_FILE);
        } else {
            setAttrValue(PrintObject.ATTR_PRINTER_FILE, ifsPrinterFile);
        }
    }

} // NPCPSelPrtF class

