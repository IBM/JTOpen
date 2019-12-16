///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPCPSelOutQ.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * NPCPSelOutQ class - class for an attribute value list code point used with
  * the network print server's data stream.
  * This class is derived from NPCPSelection and will be used to build a code
  * point that has as its data a list of any attributes that can filter a outq list.
**/

class NPCPSelOutQ extends NPCPSelection implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



    protected Object clone()
    {
        NPCPSelOutQ cp = new NPCPSelOutQ(this);
        return cp;
    }

   /**
    * copy constructor
    **/
    NPCPSelOutQ(NPCPSelOutQ cp)
    {
        super(cp);
    }

   /**
    * basic constructor that creates an empty outq selection codepoint
    **/
    NPCPSelOutQ()
    {
        super();
    }


    /**
     * get output queue filter as an IFS path.
     * @returns The IFS path of the output queue filter or
     * an empty string if it isn't set.
     **/
    String getQueue()
    {
        String ifsQueue = getStringValue(PrintObject.ATTR_OUTPUT_QUEUE);
        if( ifsQueue == null )
        {
            return emptyString;
        } else {
            return ifsQueue;
        }
    }

    /**
      * set the output queue filter as an IFS path name.
      * Removes the filter if ifsQueue is null or "".
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

    
}

