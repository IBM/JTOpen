///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: CADSPool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.LinkedList;

// This class is used to pool ClientAccessDataStream objects.
final class CADSPool
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private ClientAccessDataStream[] streams_ = new ClientAccessDataStream[4];
  // Performance optimization for linkedList of returned data streams.  
  private LinkedList linkedList_ = null;  
  private final Object streamsLock_ = new Object(); //@P1A
  private int searchStart_ = 0; 
  
  public CADSPool() {
    try {
        linkedList_ = new LinkedList(); 
    } catch (Throwable e) {
      // Ignore any errors if the linked list cannot be created
      // this will fall back to the older implemenation
      
    }
  }
  final ClientAccessDataStream getUnusedStream()
  {
    synchronized(streamsLock_) //@P1C
    {
      ClientAccessDataStream returnStream = null; 
      // Use the linked list if available
      if (linkedList_ != null) {
        
        while (linkedList_.size() > 0) {
          returnStream = (ClientAccessDataStream) linkedList_.removeFirst();
          if (returnStream.canUse()) {
            return returnStream; 
          }
          
        }
        
      }
      
      
      int max = streams_.length;
      for (int i= searchStart_; i<max; ++i)
      {
        if (streams_[i] == null)
        {
          streams_[i] = new ClientAccessDataStream(this, i);
          streams_[i].canUse();
          searchStart_ = i+1; 
          return streams_[i];
        }
          if (streams_[i].canUse())
          {
            searchStart_ = i+1; 
            return streams_[i];
          }
      }
      // Need more streams
      ClientAccessDataStream[] newStreams = new ClientAccessDataStream[max*2];
      System.arraycopy(streams_, 0, newStreams, 0, max);
      newStreams[max] = new ClientAccessDataStream(this, max);
      newStreams[max].canUse(); 
      streams_ = newStreams;
      searchStart_ = max+1; 
      return newStreams[max];
    }
  }

  /*
   * Returns a ClientAccessDataStream to the pool.  The stream must have previously
   * been marked as available. 
   */
  public void returnToPool(ClientAccessDataStream stream, int fromPoolIndex_) {
    synchronized(streamsLock_) { 
      if (linkedList_ != null) {
        linkedList_.add(stream); 
      }
      if (fromPoolIndex_ < searchStart_) {
        searchStart_ = fromPoolIndex_; 
      }
    } 
  }
  
  
}


