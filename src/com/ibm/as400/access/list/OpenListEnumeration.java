///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OpenListEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

import com.ibm.as400.access.*;
import java.util.*;

/**
 * Helper class. Used to wrap the OpenList objects with an Enumeration.
**/
final class OpenListEnumeration implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private Object[] objectCache_;
  private OpenList list_;
  private int counter_;
  private int numObjects_;
  private int listOffset_ = 0;
  private int cachePos_ = 0;
  private boolean closed_ = false;

  OpenListEnumeration(OpenList list, int length)
  {
    list_ = list;
    numObjects_ = length;
  }

  // Called by OpenList when someone closes it.
  // This invalidates us.
  synchronized void close()
  {
    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "OpenList closed enumeration: "+this);
    }
    closed_ = true;
  }

  public boolean hasMoreElements()
  {
    return (closed_ && counter_ < listOffset_) || (!closed_ && counter_ < numObjects_);
  }

  public synchronized Object nextElement()
  {
    // If we are closed, but we still have objects in the cache, then
    // we might as well return them.
    if ((closed_ && counter_ >= listOffset_) ||
        (!closed_ && counter_ >= numObjects_))
    {
      throw new NoSuchElementException();
    }

    if (objectCache_ == null || cachePos_ >= objectCache_.length)
    {
      try
      {
        int blockSize = list_.getEnumerationBlockSize();
        objectCache_ = list_.getItems(listOffset_, blockSize);
        if (Trace.isTraceOn())
        {
          Trace.log(Trace.DIAGNOSTIC, "Loaded next block in OpenListEnumeration: "+objectCache_.length+" messages at offset "+listOffset_+" out of "+numObjects_+" total, using block size "+blockSize+".");
        }
      }
      catch (Exception e)
      {
        if (Trace.isTraceOn())
        {
          Trace.log(Trace.ERROR, "Exception while loading nextElement() in OpenListEnumeration:", e);
        }
        throw new NoSuchElementException();
      }
      cachePos_ = 0;
      listOffset_ += objectCache_.length;
    }
    ++counter_;
    Object obj = objectCache_[cachePos_];
    objectCache_[cachePos_++] = null; // Set to null to reduce memory usage as nextElement() is called.

    // When we reach the end, our Enumeration is now useless.
    // We "close" ourselves and notify our Open List that we're used up.
    // This aids in garbage collection.
    if (counter_ >= numObjects_)
    {
      closed_ = true;
      list_.remove(this);
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "OpenListEnumeration reached last element: "+this);
      }
    }

    return obj;
  }
}



