///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WriterJobListImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The WriterJobList class is used to build a list of AS/400 writer job objects of type
 * WriterJob.  The list can be filtered by writer job name or output queue.
 *
 * @see WriterJob
 **/

class WriterJobListImplRemote extends PrintObjectListImplRemote
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String x = Copyright.copyright;     // @A1C - Copyright change

    // static private binary data for default attribute to
    // retrieve on a writer jobs when listing writer jobs
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
        0x00, 0x04,         // big endian(BE), number of attrs
        0x00, 0x02,         // BE - size in bytes of each ID
        0x00, 0x79,         // writer job name
        0x00, 0x7A,         // writer job number
        0x00, 0x7B,         // writer job status
        0x00, 0x7C          // writer job user name
    };

    private static final NPCPAttributeIDList defaultAttrIDsToList_ = new NPCPAttributeIDList(attrIDToList_);

    // register the writer return datastream for listing writers
    static
    {
        NPDataStream ds;
        NPCodePoint  cp;
        
        ds = new NPDataStream(NPConstants.WRITER_JOB);  // @B1C
        cp = new NPCPIDWriter();
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
      * Creates a new Writer object.
      **/
    /* @A5D
    PrintObject newNPObject(AS400 system, NPDataStream reply)
    {
        WriterJob npObj = null;
        NPCPIDWriter cpid;
        NPCPAttribute cpAttrs;
        cpid = (NPCPIDWriter)reply.getCodePoint(NPCodePoint.WRITER_JOB_ID);  // never should return null
        cpAttrs = (NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);   // may return null
        npObj = new WriterJob(system, cpid, cpAttrs);
        return npObj;
    }
    */


    // @A5A
    NPCPID newNPCPID(NPDataStream reply)
    {
        return (NPCPIDWriter)reply.getCodePoint(NPCodePoint.WRITER_JOB_ID);  // never should return null
    }


    /**
      * Sets the output queue filter.  Only writers active for this output queue
      * will be listed.
      * @param queueFilter Specifies the library and output queue name for which the writer
      *  jobs will be listed.   The format of the queueFilter string must be in the
      *  format of /QSYS.LIB/libname.LIB/queuename.OUTQ, where
      * <br>
      *   <I>libname</I> is the library name that contains the queue for which to list writer
      *     jobs.  It must be a specific library name.
      *   <I>queuename</I> is the name of an output queue for which to list writer jobs.
      *     It must be a specific output queue name.
      **/
    public void setQueueFilter(String queueFilter)
    {
        NPCPSelWrtJ selectionCP = (NPCPSelWrtJ)getSelectionCP();
        selectionCP.setQueue(queueFilter);
    }



    /**
     * Sets writer list filter.
     * @param writerFilter The name of the writers to list.
     *   <I>writer</I> is the name of the writers to list.
     *     It can be a specific name, a generic name, or the special value *ALL.
     *  The default for the writerFilter is *ALL.
     **/
    public void setWriterFilter(String writerFilter)
    {
        NPCPSelWrtJ selectionCP = (NPCPSelWrtJ)getSelectionCP();
        selectionCP.setWriter(writerFilter);
    }

}

