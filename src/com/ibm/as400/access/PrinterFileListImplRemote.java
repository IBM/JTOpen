///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PrinterFileListImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The  PrinterFileList class is used to build a list of AS/400 printer file objects of type PrinterFile.
 * The list can be filtered by library and printer file name.
 *
 *@see PrinterFile
 **/

class PrinterFileListImplRemote extends PrintObjectListImplRemote
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String x = Copyright.copyright;     // @A1C - Copyright change

    // static private binary data for default attribute to
    // retrieve on a printer file when listing printer files
    // format is:
    //    ---------------------------------------------------
    //    |nn | LEN | ID1 | ID2 | ID3 | ID4 | ....... | IDnn|
    //   ---------------------------------------------------
    //       nn   - two byte total # of attributes in code point
    //       LEN  - two byte length of each attribute entry, right
    //              now this will be 2 (0x02).
    //       IDx  - two byte attribute ID

    private static final byte[] attrIDToList_ =
    {
        0x00, 0x03,           // big endian(BE), number of attrs
        0x00, 0x02,           // BE - size in bytes of each ID
        0x00, 0x5B,           // ATTR_PRTFLIB
        0x00, 0x5C,           // ATTR_PRTFILE
        0x00, 0x6D            // ATTR_DESCRIPTION
    };

    private static final NPCPAttributeIDList defaultAttrIDsToList_ = new NPCPAttributeIDList(attrIDToList_);

    // register the printer file return datastream for listing printer files
    static
    {
        NPDataStream ds;
        NPCodePoint  cp;
        
        ds = new NPDataStream(NPConstants.PRINTER_FILE); // @B1C
        cp = new NPCPIDPrinterFile();
        ds.addCodePoint(cp);
        cp = new NPCPAttribute();
        ds.addCodePoint(cp);
        AS400Server.addReplyStream(ds, "as-netprt");
    }

   

    /**
      * Returns the default attributes to list.
      * @return The default attributes to list. 
      **/
    NPCPAttributeIDList getDefaultAttrsToList()
    {
        return defaultAttrIDsToList_;
    }



    /**
      * Create a new Printer File object.
      **/
    /* @A5D
    PrintObject newNPObject(AS400 system, NPDataStream reply)
    {
        PrinterFile npObj = null;
        NPCPIDPrinterFile cpid;
        NPCPAttribute cpAttrs;
        cpid = (NPCPIDPrinterFile)reply.getCodePoint(NPCodePoint.PRINTER_FILE_ID);  // never should return null
        cpAttrs = (NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);   // may return null 
        npObj = new PrinterFile(system, cpid, cpAttrs);
        return npObj;
    }
    */



    // @A5A
    NPCPID newNPCPID(NPDataStream reply)
    {
        return (NPCPIDPrinterFile)reply.getCodePoint(NPCodePoint.PRINTER_FILE_ID);  // never should return null
    }



    /**
      * Sets the printer file list filter.
      * @param printerFileFilter The library and printer files to list.
      *  The format of the printerFileFilter string must be in the
      *  format of /QSYS.LIB/libname.LIB/printerfilename.FILE, where
      * <br>
      *   <I>libname</I> is the library name that contains the printer files to search.
      *     It can be a specific name or one of these special values:
      * <ul>
      * <li> %ALL%     - All libraries are searched.
      * <li> %ALLUSR%  - All user-defined libraries, plus libraries containing user data
      *                 and having names starting with the letter Q.
      * <li> %CURLIB%  - The server job's current library.
      * <li> %LIBL%    - The server job's library list.
      * <li> %USRLIBL% - The user portion of the server job's library list.
      * </ul>
      *   <I>printerfilename</I> is the name of the printer files to list.
      *     It can be a specific name, a generic name, or the special value %ALL%.
      *  The default for the library is %LIBL% and for the printer file name is %ALL%.
      *
      **/
    public void setPrinterFileFilter(String printerFileFilter)
    { 
        NPCPSelPrtF selectionCP = (NPCPSelPrtF)getSelectionCP();
        selectionCP.setPrinterFile(printerFileFilter);
    }

} 

