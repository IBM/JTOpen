///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DBStoragePool.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.BitSet;
import java.util.Vector;



/**
<p>This class manages a pool of large byte arrays for use
in creating request datastreams.  This enables reduction in
the number of allocations and the amount of synchronization
involved in sending request datastreams.
**/
class DBStoragePool
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    private int     count_;
    private BitSet  lockState_;
    private Vector  pool_;



/**
Constructs a DBStoragePool object.
**/
	DBStoragePool ()
	{
	    count_ = 0;

	    // The initial capacity is 128.  This is pretty
	    // large and would only be hit if we had 128 data
	    // streams being created at the same time.  Even
	    // if this does happen (an extremely stressed
	    // scenario), the resizing should not happen too
	    // often.
	    lockState_ = new BitSet (128);
	    pool_ = new Vector (128, 128);
    }



/**
Frees a DBStorage object for reuse.

@param      a DBStorage object.
**/
    synchronized void freeStorage (DBStorage storage) // @B1C
    {
        lockState_.clear (storage.getId ());
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
Returns an unused, pre-allocated DBStorage object.  If none
are available, a brand new one will be allocated.

@return     a DBStorage object.
**/
//
// Note: This method must be synchronized to make it
//       threadsafe.
//
    synchronized DBStorage getUnusedStorage () // @B0C @B1C
    {
        DBStorage storage;

        // Find an unused storage object.
        for (int i = 0; i < count_; ++i) {
            if (lockState_.get (i) == false) {
                lockState_.set (i);
                storage = (DBStorage) pool_.elementAt (i);
                storage.clear ();
                return storage;
            }
        }

        // If all are being used, then allocate a new one.
        storage = new DBStorage (count_);
        pool_.addElement (storage);
        lockState_.set (count_);
        ++count_;

        return storage;
    }



}



