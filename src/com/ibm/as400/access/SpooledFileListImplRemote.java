///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileListImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The SpooledFileList class is used to build a list of AS/400 spooled file objects of type
 * SpooledFile.  The list can be filtered by formtype, output queue, user,
 * or user data.
 *
 *@see SpooledFile
 **/

class SpooledFileListImplRemote extends PrintObjectListImplRemote
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String x = Copyright.copyright;     // @A1C - Copyright change

    // static private binary data for default attribute to
    // retrieve on a spooled file when listing spooled files
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
        // 80 attributes
        0x00, 0x50,             // big endian(BE), number of attrs
        0x00, 0x02,             // BE - size in bytes of each ID
        0x00, 0x0B,             // ATTR_ALIGN
        0x00, 0x11,             // ATTR_BKMGN_ACR
        0x00, 0x12,             // ATTR_BKMGN_DWN
        0x00, 0x13,             // ATTR_BKOVRLLIB
        0x00, 0x14,             // ATTR_BKOVRLAY
        0x00, 0x15,             // ATTR_BKOVL_DWN
        0x00, 0x16,             // ATTR_BKOVL_ACR
        0x00, 0x17,             // ATTR_CPI
        0x00, 0x18,             // ATTR_CODEDFNTLIB
        0x00, 0x1A,             // ATTR_CODEDFNT
        0x00, 0x1C,             // ATTR_COPIES
        0x00, 0x1D,             // ATTR_COPIESLEFT
        0x00, 0x1E,             // ATTR_CURPAGE
        0x00, 0x22,             // ATTR_DATE
        0x00, (byte)0x99,       // ATTR_DBCSDATA
        0x00, (byte)0x9A,       // ATTR_DBCSEXTENSN
        0x00, (byte)0x9B,       // ATTR_DBCSROTATE
        0x00, (byte)0x9C,       // ATTR_DBCSCPI
        0x00, (byte)0x9D,       // ATTR_DBCSSISO
        0x00, 0x24,             // ATTR_PAGRTT
        0x00, 0x2B,             // ATTR_ENDPAGE
        0x00, 0x2C,             // ATTR_FILESEP
        0x00, 0x2D,             // ATTR_FOLDREC
        0x00, 0x2E,             // ATTR_FONTID
        0x00, (byte)0xB7,       // ATTR_FORMDEFLIB
        0x00, (byte)0xB6,       // ATTR_FORMDEF
        0x00, 0x2F,             // ATTR_FORMFEED
        0x00, 0x30,             // ATTR_FORMTYPE
        0x00, 0x31,             // ATTR_FTMGN_ACR
        0x00, 0x32,             // ATTR_FTMGN_DWN
        0x00, 0x33,             // ATTR_FTOVRLLIB
        0x00, 0x34,             // ATTR_FTOVRLAY
        0x00, 0x36,             // ATTR_FTOVL_ACR
        0x00, 0x35,             // ATTR_FTOVL_DWN
        0x00, 0x37,             // ATTR_CHAR_ID
        0x00, 0x38,             // ATTR_JUSTIFY
        0x00, 0x39,             // ATTR_HOLD
        0x00, 0x3B,             // ATTR_JOBNAME
        0x00, 0x3C,             // ATTR_JOBNUMBER
        0x00, 0x3E,             // ATTR_JOBUSER
        0x00, 0x3F,             // ATTR_LASTPAGE
        0x00, 0x40,             // ATTR_LPI
        0x00, 0x42,             // ATTR_MAXRCDS
        0x00, 0x4E,             // ATTR_PAGELEN
        0x00, 0x51,             // ATTR_PAGEWIDTH
        0x00, 0x4F,             // ATTR_MEASMETHOD
        0x00, 0x48,             // ATTR_OUTPTY
        0x00, 0x49,             // ATTR_OUTQUELIB
        0x00, 0x4A,             // ATTR_OUTQUE
        0x00, 0x4C,             // ATTR_OVERFLOW
        0x00, 0x52,             // ATTR_MULTIUP
        0x00, 0x53,             // ATTR_POINTSIZE
        0x00, 0x54,             // ATTR_FIDELITY
        0x00, 0x55,             // ATTR_DUPLEX
        0x00, 0x56,             // ATTR_PRTQUALITY
        0x00, 0x58,             // ATTR_PRTTEXT
        0x00, 0x5A,             // ATTR_PRTDEVTYPE
        0x00, 0x5B,             // ATTR_PRTFLIB
        0x00, 0x5C,             // ATTR_PRTFILE
        0x00, 0x5F,             // ATTR_RECLENGTH
        0x00, 0x61,             // ATTR_RPLUNPRT
        0x00, 0x62,             // ATTR_RPLCHAR
        0x00, 0x63,             // ATTR_RESTART
        0x00, 0x64,             // ATTR_SAVE
        0x00, 0x66,             // ATTR_SRCDRWR
        0x00, 0x68,             // ATTR_SPOOLFILE
        0x00, 0x69,             // ATTR_SPLFNUM
        0x00, 0x6A,             // ATTR_SPLFSTATUS
        0x00, 0x6B,             // ATTR_SCHEDULE
        0x00, 0x6C,             // ATTR_STARTPAGE
        0x00, 0x6E,             // ATTR_TIME
        0x00, 0x6F,             // ATTR_PAGES
        0x00, 0x72,             // ATTR_UNITOFMEAS
        0x00, 0x73,             // ATTR_USERCMT
        0x00, 0x74,             // ATTR_USERDATA
        0x00, (byte)0xA2,       // ATTR_USRDEFDATA
        0x00, (byte)0xA3,       // ATTR_USRDEFOPT
        0x00, (byte)0xA5,       // ATTR_USRDEFOBJ
        0x00, (byte)0xA4,       // ATTR_USRDEFOBJLIB
        0x00, (byte)0xA6,       // ATTR_USRDEFOBJTYP
    };

    private static final NPCPAttributeIDList defaultAttrIDsToList_ = new NPCPAttributeIDList(attrIDToList_);

    // register the spooled file return datastream for listing spooled files
    static
    {
        NPDataStream ds;
        NPCodePoint  cp;
        
        ds = new NPDataStream(NPConstants.SPOOLED_FILE); // @B1C
        cp = new NPCPIDSplF();
        ds.addCodePoint(cp);
        cp = new NPCPAttribute();
        ds.addCodePoint(cp);
        AS400Server.addReplyStream(ds, "as-netprt");
    }

    

    /**
      * Returns the default attributes to list.
      **/
    NPCPAttributeIDList getDefaultAttrsToList()
    {
        return defaultAttrIDsToList_;
    }

   

    /**
      * Creates a new SpooledFile object.
      **/
    /* @A5D
    PrintObject newNPObject(AS400 system, NPDataStream reply)
    {
        SpooledFile npSplF = null;
        NPCPIDSplF cpidSplF;
        NPCPAttribute cpAttrs;
        cpidSplF = (NPCPIDSplF)reply.getCodePoint(NPCodePoint.SPOOLED_FILE_ID);  // never should return null
        cpAttrs = (NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);   // may return null
        npSplF = new SpooledFile(system, cpidSplF, cpAttrs);
        return npSplF;
    }
    */


    // @A5A
    NPCPID newNPCPID(NPDataStream reply)
    {
        return (NPCPIDSplF)reply.getCodePoint(NPCodePoint.SPOOLED_FILE_ID);  // never should return null
    }



    /**
      * Sets the formtype list filter.
      * @param formTypeFilter The form type the spooled file must to be included
      * in the list.  It cannot be greater than 10 characters.
      * The value can be any specific value or any of these special values:
      * <ul>
      *  <li> *ALL - Spooled files with any form type will be included in the list.
      *  <li> *STD - Spooled files with the form type *STD will be included in the list.
      * </ul>
      * The default is *ALL.
      **/
    public void setFormTypeFilter(String formTypeFilter)
    {
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setFormType(formTypeFilter);
    }



    /**
      * Sets the output queue filter.
      * @param queueFilter The library and output queues on which to list spooled
      *  files.   The format of the queueFilter string must be in the
      *  format of /QSYS.LIB/libname.LIB/queuename.OUTQ where
      * <br>
      *   <I>libname</I> is the library name that contains the queues to search.
      *     It can be a specific name or one of these special values:
      * <ul>
      * <li> %CURLIB% - The server job's current library
      * <li> %LIBL%   - The server job's library list
      * <li> %ALL%    - All libraries are searched.  This value is only valid
      *                if the queuename is %ALL%.
      * </ul>
      *   <I>queuename</I> is the name of an output queues to search.
      *     It can be a specific name or the special value %ALL%.
      *     If it is %ALL%, then the libname must be %ALL%.
      **/
    public void setQueueFilter(String queueFilter)
    {
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setQueue(queueFilter);
    }



    /**
     * Sets the user ID list filter.
     *
     * @param userFilter The user or users for which to list spooled files.
     * The value cannot be greater than 10 characters.
     * The value can be any specific user ID or any of these special values:
     * <UL>
     * <LI>  *ALL - Spooled files created by all users will be included in the list.
     * <LI>  *CURRENT - Spooled files created by the current user only will be in the list.
     * </UL>
     * The default is *CURRENT.
     **/
    public void setUserFilter(String userFilter)
    {       
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setUser(userFilter);
    }



    /**
     * Sets the user data list filter.
     *
     * @param userDataFilter The user data the spooled file must
     *  have for it to be included in the list.  The value can be
     *  any specific value or the special value *ALL.  The value cannot be
     *  greater than 10 characters.
     *  The default is *ALL.
     **/
    public void setUserDataFilter(String userDataFilter)
    {
        NPCPSelSplF selectionCP = (NPCPSelSplF)getSelectionCP();
        selectionCP.setUserData(userDataFilter);
    }

} 
