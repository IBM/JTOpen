///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterListImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The PrinterList class is used to build a list of i5/OS printer objects of type Printer.
 * The list can be filtered by printer name.
 *
 *@see Printer
 **/

class PrinterListImplRemote extends PrintObjectListImplRemote
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // static private binary data for default attribute to
    // retrieve on a printer when listing printer devices
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
        0x00, 0x1A,             // big endian(BE), number of attrs
        0x00, 0x02,             // BE - size in bytes of each ID
        0x00, 0x0A,             // ATTR_AFP
        0x00, 0x19,             // ATTR_CODEPAGE
        0x00, 0x26,             // ATTR_DEVCLASS
        0x00, 0x07,             // ATTR_DEVMODEL
        0x00, 0x08,             // ATTR_DEVTYPE
        0x00, 0x2A,             // ATTR_DRWRSEP
        0x00, 0x2E,             // ATTR_FONTID
        0x00, (byte)0xB7,       // ATTR_FORMDEFLIB
        0x00, (byte)0xB6,       // ATTR_FORMDEF
        0x00, 0x2F,             // ATTR_FORMFEED
        0x00, 0x37,             // ATTR_CHAR_ID
        0x00, 0x41,             // ATTR_MFGTYPE
        0x00, 0x44,             // ATTR_MSGQUELIB
        0x00, 0x5E,             // ATTR_MSGQUE
        0x00, 0x53,             // ATTR_POINTSIZE
        0x00, 0x59,             // ATTR_PRINTER
        0x00, 0x6D,             // ATTR_DESCRIPTION
        0x00, (byte)0xA3,       // ATTR_USRDEFOPT
        0x00, (byte)0xA5,       // ATTR_USRDEFOBJ
        0x00, (byte)0xA4,       // ATTR_USRDEFOBJLIB
        0x00, (byte)0xA6,       // ATTR_USRDEFOBJTYP
        0x00, (byte)0xA7,       // ATTR_USRTFMLIB
        0x00, (byte)0xA8,       // ATTR_USRTFM
        0x00, (byte)0xAA,       // ATTR_USRDRVLIB
        0x00, (byte)0xAB,       // ATTR_USERDRV
        0x00, 0x71,             // ATTR_SCS2ASCII
    };

    private static final NPCPAttributeIDList defaultAttrIDsToList_ = new NPCPAttributeIDList(attrIDToList_);

    // register the printer file return datastream for listing printers
    static
    {
        NPDataStream ds;
        NPCodePoint  cp; 
        
        ds = new NPDataStream(NPConstants.PRINTER_DEVICE);
        cp = new NPCPIDPrinter();
        ds.addCodePoint(cp);
        cp = new NPCPAttribute();
        ds.addCodePoint(cp);
        AS400Server.addReplyStream(ds, "as-netprt");
    }
    


    /**
     * Returns the default attributes to list.
     *
     * @return The default attribute ID list.
     **/
    NPCPAttributeIDList getDefaultAttrsToList()
    {
	return defaultAttrIDsToList_;
    }



    /**
     * Creates a new Printer object.
     *
     * @param system The server on which the printer devices exist.
     * @param reply The datastream with which to reply.
     *
     * @return The newly created Printer object.
     **/
    /*
    PrintObject newNPObject(AS400 system, NPDataStream reply)
    {
        Printer npObj = null;
        NPCPIDPrinter cpid;
        NPCPAttribute cpAttrs;
        cpid = (NPCPIDPrinter)reply.getCodePoint(NPCodePoint.PRINTER_DEVICE_ID);  // never should return null
        cpAttrs = (NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);   // may return null
        npObj = new Printer(system, cpid, cpAttrs);
        return npObj;
    }
    */



    NPCPID newNPCPID(NPDataStream reply)
    {
        return (NPCPIDPrinter)reply.getCodePoint(NPCodePoint.PRINTER_DEVICE_ID);  // never should return null
    }



    /**
      * Sets printer list filter.
      * @param printerFilter The name of the printers to list.
      * It cannot be greater than 10 characters in length.
      * It can be a specific name, a generic name, or the special
      * value *ALL. The default for the printerFilter is *ALL.
      **/
    public void setPrinterFilter(String printerFilter)
    {
        NPCPSelPrtD selectionCP = (NPCPSelPrtD)getSelectionCP();
        selectionCP.setPrinter(printerFilter);
    }

}

