///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBStoragePool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.ref.SoftReference;

//@P0D import java.util.BitSet;
//@P0D import java.util.Vector;



/**
<p>This class manages a pool of large byte arrays for use
in creating request datastreams.  This enables reduction in
the number of allocations and the amount of synchronization
involved in sending request datastreams.
**/
class DBStoragePool
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  static final int MAX_POOL_INCREMENT = 4096;  //@B5A

  private static final int STORAGE_POOL_LOCALITY = 256;  //@B5A


  //@P0D private int     count_;
  //@P0D private BitSet  lockState_;
  //@P0D private Vector  pool_;

  // 
  // Use soft references to avoid running the JVM out of memory
  // 
  private SoftReference[] pool_ = new SoftReference[16]; //@P0A
  private int lastReturned_;                                                   // @B5A
  private int searches_ = 0;                                                  // @B5A
  
/**
Constructs a DBStoragePool object.
**/
//@P0D	DBStoragePool ()
//@P0D	{
//@P0D	    count_ = 0;

  // The initial capacity is 128.  This is pretty
  // large and would only be hit if we had 128 data
  // streams being created at the same time.  Even
  // if this does happen (an extremely stressed
  // scenario), the resizing should not happen too
  // often.
//@P0D	    lockState_ = new BitSet (128);
//@P0D	    pool_ = new Vector (128, 128);
//@P0D    }

  int getSize() { return pool_.length; }                 // @B5A

/**
Frees a DBStorage object for reuse.

@param      a DBStorage object.
**/
//@P0D    synchronized void freeStorage (DBStorage storage) // @B1C
//@P0D    {
//@P0D        lockState_.clear (storage.getId ());
//@P0D    }


  /**
  Returns an unused DBStorage object.  

  @return     a DBStorage object.
  **/
  

  final synchronized DBStorage getUnpooledStorage() {
	  DBStorage storage = new DBStorage(-1, this); 
      storage.canUse(); 
	  return storage; 
  } //@B5A

/**
Returns an unused, pre-allocated DBStorage object.  If none
are available, a brand new one will be allocated.

@return     a DBStorage object.
**/
//
// Note: This method must be synchronized to make it
//       threadsafe.
//
  final synchronized DBStorage getUnusedStorage() // @B0C @B1C @P0C
  {
//@P0D        DBStorage storage;
    int max = pool_.length; //@P0A
    
    // Start the search at the last returned location @B5A
    int searchStart = lastReturned_;
    searches_++; 
    if (searches_ > MAX_POOL_INCREMENT) {
    	searchStart = 0; 
    	searches_ = 0; 
    }
    // Find an unused storage object.
    for (int i=searchStart; i<max; ++i) //@P0C
    {
      /*@P0D
        if (lockState_.get (i) == false) {
            lockState_.set (i);
            storage = (DBStorage) pool_.elementAt (i);
            storage.clear ();
            return storage;
        }
      *///@P0D

      	
      DBStorage storage;  //@P0A  Local variables are faster.
      if (pool_[i] == null) {
    	  storage=null; 
      } else {
    	  storage= (DBStorage) pool_[i].get(); 
      }
      if (storage == null) //@P0A
      {
        storage = new DBStorage(i, this); //@P0A
        storage.canUse(); //@P0A
        pool_[i] = new SoftReference(storage); //@P0A
        lastReturned_ = i+1; 
        return storage; //@P0A
      }
      else {
    	  if (storage.canUse()) //@P0A
            {
              lastReturned_ = i+1; 
             return storage; //@P0A

            }
      }
    }

    // If all are being used, then allocate a new one.
    if (JDTrace.isTraceOn())                                                     // @B2C
      JDTrace.logInformation(this, "Creating new DBStoragePool of size "+max*2); // @P0A @B2C

    int increment = max;                                                                                                  //@B5A
    if (max > MAX_POOL_INCREMENT) increment = MAX_POOL_INCREMENT;             // @B5A
    
    SoftReference[] tempPool = new SoftReference[max+increment]; //@P0A@B5C

    for (int i=0; i<max; ++i) //@P0A
    {
      tempPool[i] = pool_[i]; //@P0A
    }
    DBStorage storage = new DBStorage(max, this); //@P0A
    storage.canUse(); //@P0A
    tempPool[max] = new SoftReference(storage); //@P0A
    pool_ = tempPool; //@P0A
                      
    lastReturned_ = 0; // Always start the search at zero when expanding 
    /*@P0D
    storage = new DBStorage (count_);
    pool_.addElement (storage);
    lockState_.set (count_);
    ++count_;
    *///@P0D
    
    return storage;
  }

  public synchronized void returned(int id_) {
	if (id_ < lastReturned_ && (id_ >= (lastReturned_ - STORAGE_POOL_LOCALITY))) { 
	   lastReturned_ = id_; 
	}
  }
}


