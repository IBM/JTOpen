///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NPCPSelWrtJ.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPSelWrtJ class - class for an attribute value list code point used with
  * the network print server's data stream.
  * This class is derived from NPCPSelection and will be used to build a code
  * point that has as its data a list of any attributes that can filter a
  * writer list.
**/

class NPCPSelWrtJ extends NPCPSelection implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // some strings we use for filtering output queues
    // we accept *ALL for the output queue library and we'll convert it
    // to "" for the host to use
    static final String STR_ALLOUTQLIBS = "*ALL";
    static final String STR_BLANKOUTQLIB = "";

   /**
    * copy constructor
    **/
    NPCPSelWrtJ(NPCPSelWrtJ cp)
    {
        super(cp);
    }

   /**
    * basic constructor that creates an empty WrtJ selection codepoint
    **/
    NPCPSelWrtJ()
    {
        super();
    }

    protected Object clone()
    {
        NPCPSelWrtJ cp = new NPCPSelWrtJ(this);
        return cp;
    }

    

   /**
     * get output queue filter as an IFS path.
     * @returns The IFS path of the output queue filter or
     * an empty string if it isn't set.
     **/
    String getQueue()
    {
        String queue = getStringValue(PrintObject.ATTR_OUTPUT_QUEUE);
        if( queue == null )
        {
            return emptyString;
        } else {
            return queue;
        }
    }

   /**
     * get writer filter.
     * @returns The writer job filter or an empty string if it isn't set.
     **/
    String getWriter()
    {
        String writer = getStringValue(PrintObject.ATTR_WTRJOBNAME);
        if( writer == null )
        {
            return emptyString;
        } else {
            return writer;
        }
    }

   /**
     * set the output queue filter as an IFS path name.
     * Removes the filter if ifsQueue is "".
     **/
    void setQueue(String ifsQueue)
    {
        // if the ifs path has a length of 0 (emtpy string) then
        // we will remove the filter completely.
        // If it has something in it, it had better be
        // a valid IFS path name.

        if( ifsQueue.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_OUTPUT_QUEUE);
        } else {
            setAttrValue(PrintObject.ATTR_OUTPUT_QUEUE, ifsQueue);
        }
    }

   /**
    * set writer filter. Removes the filter if writerName is "".
    **/
    void setWriter(String writerName)
    {
        if( writerName.length() == 0 )
        {
            removeAttribute(PrintObject.ATTR_WTRJOBNAME);
        } else {
            setAttrValue(PrintObject.ATTR_WTRJOBNAME, writerName);
        }
    }

}

