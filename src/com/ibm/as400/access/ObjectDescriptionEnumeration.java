///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ObjectDescriptionEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;

/**
 * Helper class. Used to wrap the ObjectDescription[] with an Enumeration.
 * This class is used by ObjectList.
**/
class ObjectDescriptionEnumeration implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private ObjectDescription[] objectCache_;
  private ObjectList list_;
  private int counter_;
  private int numObjects_;
  private int listOffset_ = 0;
  private int cachePos_ = 0;

  ObjectDescriptionEnumeration(ObjectList list, int length)
  {
    list_ = list;
    numObjects_ = length;
  }

  public final boolean hasMoreElements()
  {
    return counter_ < numObjects_;
  }

  public final Object nextElement()
  {
    if (counter_ >= numObjects_)
    {
      throw new NoSuchElementException();
    }

    if (objectCache_ == null || cachePos_ >= objectCache_.length)
    {
      try
      {
        objectCache_ = list_.getObjects(listOffset_, 1000);
        if (Trace.traceOn_)
        {
          Trace.log(Trace.DIAGNOSTIC, "Loaded next block in ObjectDescriptionEnumeration: "+objectCache_.length+" messages at offset "+listOffset_+" out of "+numObjects_+" total.");
        }
      }
      catch (Exception e)
      {
        if (Trace.traceOn_)
        {
          Trace.log(Trace.ERROR, "Exception while loading nextElement() in ObjectDescriptionEnumeration:", e);
        }
        throw new NoSuchElementException();
      }
      cachePos_ = 0;
      listOffset_ += objectCache_.length;
    }
    ++counter_;
    return objectCache_[cachePos_++];
  }
}



