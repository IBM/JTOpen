///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DataQueueEntry.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

/**
 The DataQueueEntry class represents an entry on an AS/400 data queue.
 **/
public class DataQueueEntry
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Source dq, used for String conversion.
    BaseDataQueue dq_;
    // The data for this entry.
    byte[] data_;
    // Sender info associated with this entry.
    String senderInfo_ = "";

    // Constructs a DataQueueEntry object.
    // @param  data  The data of the entry read.
    // @param  senderInfo  The sender information of the entry read.  This may be null.
    DataQueueEntry(BaseDataQueue dq, byte[] data, String senderInfo)
    {
        super();
        dq_ = dq;
        data_ = data;
        if (senderInfo != null) senderInfo_ = senderInfo;
    }

    /**
     Returns the data for this data queue entry.
     @return  The data for this data queue entry.
     **/
    public byte[] getData()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting data queue entry data.");
        return data_;
    }

    /**
     Returns the data for this data queue entry as a string.
     @return  The data for this data queue entry as a string.
     @exception  UnsupportedEncodingException  If the <i>ccsid</i> is not supported.
     **/
    public String getString() throws UnsupportedEncodingException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting data queue entry data as String.");
        return dq_.byteArrayToString(data_);
    }

    /**
     Returns the sender information for this data queue entry.  If sender information is not available for this entry, an empty String ("") is returned.
     @return  The data for this data queue entry.
     **/
    public String getSenderInformation()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting data queue sender information.");
        return senderInfo_;
    }
}
