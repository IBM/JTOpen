///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobLogEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.access.AS400;
import java.io.IOException;
import java.util.Enumeration;


/**
The JobLogEnumeration class implements an enumeration for
job log message lists.
**/
class JobLogEnumeration
implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private QueuedMessage[]         array_          = null;
    private JobLogParser            parser_         = null;
    private int                     index_          = 0;
    private int                     overallIndex_   = 0;
    private AS400                   system_         = null;



    JobLogEnumeration (AS400 system, JobLogParser parser,
                       byte[] listInfoData, byte[] oljlData)
        throws IOException
    {
        system_ = system;
        parser_ = parser;
        parser_.setSystem (system);
        parser_.parseLists (system, listInfoData, oljlData );
        array_ = parser_.getMessageList();
    }



    /**
     * Copyright.
     **/
  private static String getCopyright ()
  {
    return Copyright.copyright;
  }

    int getLength ()
    {
        return parser_.totalRecs_;
    }



    public boolean hasMoreElements ()
    {
        return overallIndex_ < parser_.totalRecs_;
    }



    public Object nextElement ()
    {
        // If we have read them all, then return null.
        if (overallIndex_ >= parser_.totalRecs_)
            return null;

        // If the row is not cached, then read the next chunk.
        if (index_ >= array_.length) {
            try {
                parser_.more (system_);
                index_ = 0;
                array_ = parser_.getMessageList ();

                // If this is the last element, then close the list.
                if (overallIndex_ == parser_.totalRecs_ - 1)
                    parser_.closeList (system_);
            }
            catch (AS400Exception e) {
                // Signal artificial end of list.
                overallIndex_ = parser_.totalRecs_;
                return null;
            }
        }

        Object element = array_[index_];
        ++index_;
        ++overallIndex_;
        return element;
    }

}
