///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputQueueListImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The OutputQueueList class is used to build a list of AS/400 output queue objects of type OutputQueue.
 * The list can be filtered by library and queue name.
 *
 * @see OutputQueue
 **/

class OutputQueueListImplRemote extends PrintObjectListImplRemote
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String x = Copyright.copyright;     // @A1C - Copyright change

    // static private binary data for default attribute to
    // retrieve on a output queue file when listing output queues
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
        0x00, 0x1B,           // big endian(BE), number of attrs
        0x00, 0x02,           // BE - size in bytes of each ID
        0x00, 0x0E,           // ATTR_AUTHCHCK
        0x00, 0x20,           // ATTR_DATAQUELIB
        0x00, 0x21,           // ATTR_DATAQUE
        0x00, 0x29,           // ATTR_DISPLAYANY
        0x00, 0x3D,           // ATTR_JOBSEPRATR
        0x00, 0x45,           // ATTR_NUMFILES
        0x00, (byte)0x91,     // ATTR_NUMWRITERS
        0x00, 0x46,           // ATTR_OPCNTRL
        0x00, 0x47,           // ATTR_ORDER
        0x00, 0x49,           // ATTR_OUTQUELIB
        0x00, 0x4A,           // ATTR_OUTQUE
        0x00, 0x4B,           // ATTR_OUTQSTS
        0x00, 0x59,           // ATTR_PRINTER
        0x00, (byte)0xA1,     // ATTR_SEPPAGE
        0x00, 0x6D,           // ATTR_DESCRIPTION
        0x00, (byte)0xA3,     // ATTR_USRDEFOPT    
        0x00, (byte)0xA4,     // ATTR_USRDEFOBJLIB 
        0x00, (byte)0xA5,     // ATTR_USRDEFOBJ    
        0x00, (byte)0xA6,     // ATTR_USRDEFOBJTYP 
        0x00, (byte)0xA7,     // ATTR_USRTFMLIB    
        0x00, (byte)0xA8,     // ATTR_USRTFM       
        0x00, (byte)0xAA,     // ATTR_USRDRVLIB   
        0x00, (byte)0xAB,     // ATTR_USERDRV      
        0x00, 0x79,           // ATTR_WTRJOBNAME
        0x00, 0x7A,           // ATTR_WTRJOBNUM
        0x00, 0x7B,           // ATTR_WTRJOBSTS
        0x00, 0x7C            // ATTR_WTRJOBUSER
    };

    private static final NPCPAttributeIDList defaultAttrIDsToList_ = new NPCPAttributeIDList(attrIDToList_);
     
    // register the output queue return datastream for listing output queues
    static
    {
        NPDataStream ds;
        NPCodePoint  cp;
        
        ds = new NPDataStream(NPConstants.OUTPUT_QUEUE); // @B1C
        cp = new NPCPIDOutQ();
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
      * Creates a new OutputQueue object.
      **/
    /* @A5D
    PrintObject newNPObject(AS400 system, NPDataStream reply)
    {
        OutputQueue npObj = null;
        NPCPIDOutQ cpid;
        NPCPAttribute cpAttrs;
        cpid = (NPCPIDOutQ)reply.getCodePoint(NPCodePoint.OUTPUT_QUEUE_ID);  // never should return null
        cpAttrs = (NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);   // may return null
        npObj = new OutputQueue(system, cpid, cpAttrs);
        return npObj;
    }
    */



    // @A5A
    NPCPID newNPCPID(NPDataStream reply)
    {
        return (NPCPIDOutQ)reply.getCodePoint(NPCodePoint.OUTPUT_QUEUE_ID);  // never should return null
    }



    /**
     * Sets the output queue list filter.
     * @param queueFilter The library and output queues to list.
     *  The format of the queueFilter string must be in the
     *  format of /QSYS.LIB/libname.LIB/queuename.OUTQ, where
     * <br>
     *   <I>libname</I> is the library name that contains the queues to search.
     *     It can be a specific name, a generic name, or one of these special values:
     * <ul>
     * <li> %ALL%     - All libraries are searched.
     * <li> %ALLUSR%  - All user-defined libraries, plus libraries containing user data
     *                 and having names starting with the letter Q.
     * <li> %CURLIB%  - The server job's current library.
     * <li> %LIBL%    - The server job's library list.
     * <li> %USRLIBL% - The user portion of the server job's library list.
     * </ul>
     *   <I>queuename</I> is the name of the output queues to list.
     *     It can be a specific name, a generic name, or the special value %ALL%.
     *  The default for the library is %LIBL% and for the queue name is %ALL%.
     *
     **/
    public void setQueueFilter(String queueFilter)
    {    
        NPCPSelOutQ selectionCP = (NPCPSelOutQ)getSelectionCP();
        selectionCP.setQueue(queueFilter);
    }

} 

