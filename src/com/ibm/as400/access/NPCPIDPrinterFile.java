///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPIDPrinterFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * NPCPIDPrinterFile is used to contain a printer file ID code point.
 * This code point has 2 values in it:
 *     NP_ATTR_PFILE  - printer file name
 *     NP_ATTR_PFLIB  - printer file library
 **/

class NPCPIDPrinterFile extends NPCPID implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


   /**
    * copy constructor
    **/
    NPCPIDPrinterFile(NPCPIDPrinterFile cp)
    {
       super(cp);
    }

   /**
    * basic constructor that takes the ID and no data - child class passes in correct ID
    **/
    NPCPIDPrinterFile()
    {
       super(NPCodePoint.PRINTER_FILE_ID);
    }

   /**
    * constructor that takes the ID and data - child class passes in correct ID
    * data should have the form described at the top of the class (nn len ID1...)
    **/
    NPCPIDPrinterFile( byte[] data )
    {
       super(NPCodePoint.PRINTER_FILE_ID, data);
    }

   /**
    * constructor that takes the ID as seperate items
    **/
    NPCPIDPrinterFile(String printerFileName,
                      String printerFileLib)
    {
       super(NPCodePoint.PRINTER_FILE_ID);
       setAttrValue(PrintObject.ATTR_PRTFILE, printerFileName);
       setAttrValue(PrintObject.ATTR_PRTFLIB, printerFileLib);
    }

    protected Object clone()
    {
       NPCPIDPrinterFile cp = new NPCPIDPrinterFile(this);
       return cp;
    }

    
    /**
     * get the printer file library
     **/
    String library()
    {
	return getStringValue(PrintObject.ATTR_PRTFLIB);
    }

    /**
     * get the printer file name
     **/
    String name()
    {
	return getStringValue(PrintObject.ATTR_PRTFILE);
    }

} // NPCPIDPrinterFile

