///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBStorage.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;



/**
<p>This class manages a large byte arrays for use
in creating request datastreams.  This enables reuse of
the byte array, so that it does not have to be reallocated
repeatedly.
**/
final class DBStorage //@P0C
{
  public final static int DEFAULT_SIZE = 1024; 	
  private byte[] data_ = new byte[DEFAULT_SIZE]; //@P0C
  //@P0D private int     id_;

  private boolean inUse_ = false; //@P0A

/**
Constructs a DBStorage object.

@param      id      an id assigned by the pool.
**/
//@P0D  DBStorage (int id)
//@P0D  {
    // Initialize to 63 KB.  This used to be 64K 
    // The IBM i JVM adds 24 bytes of
    // overhead to each object so a 64K byte array really
    // takes 64K + 24 bytes.  The IBM i JVM has a boundary
    // at 64K.  Objects 64K or smaller go into the 64K 
    // segment pool.  Objects 64K + 1 byte or larger go into the
    // 1 meg pool.  We used to allocate a 64K byte array
    // but that ended up in the 1 meg pool because of the 
    // added JVM overhead.  This wasted a lot of heap because
    // 1 meg was allocated but only 64K + 24 was used.  Making
    // the buffer smaller puts us back into the 64K segment
    // greatly reducing heap loss.  This object automatically
    // increases the size of the byte array if necessary so if any 
    // caller really needs 64K the byte array will grow to that size.  
//@P0 - The data is initialized to 1K now, so this isn't an issue.
// The data streams rarely need as much as 64K, so it's worth the
// memory savings.
//@P0D    data_ = new byte[64512];                                      // @D1C

//@P0D    id_ = id;
//@P0D  }



/**
Checks the size of the array and resizes the storage if needed.

@param  size        size that is needed.

@return     true if the storage was resize; false otherwise.
**/
  final synchronized boolean checkSize(final int size) //@P0C
  {
    if (size > data_.length)
    {
      // Double the size each time, until the size is greater then 4 meg.  
      // Then just go up 4 meg at a time to avoid excessive storage consumption @A8A
      int increment = data_.length; 
      if (increment > 4096 * 1024 ) increment = 4096 * 1024; 
      byte[] newdata = new byte[Math.max(data_.length + increment, size)]; // @C1C
      System.arraycopy(data_, 0, newdata, 0, data_.length);
      data_ = newdata;
      return true;
    }
    return false;
  }

/**
 * Reduce the size of the allocated buffer if needed.   @A8A
 */
public synchronized void reclaim(int length) {
	  if(data_.length>length && length >= DEFAULT_SIZE ) {
		  byte[] oldData = data_; 
		  data_ = new byte[length];
		  // The two byte client server ID is only set in the constructor, NOT in initialize.
		  // This caused a failure unless those 2 bytes are preserved
		  System.arraycopy(oldData, 6, data_, 6, 2);
	  }
}

/**
 * Set the inUse_ flags so that the storage can be returned to the pool
 */
public synchronized void returnToPool() {
	// Fill the array with garbage before it is reused. 
	// This is to catch the case where the array is still being 
	// used but has been returned to the pool. 
	// if (data_ != null) {
	//   Arrays.fill(data_, (byte) 0xeb); 
	// }
	inUse_ = false; 
}

/**
 * Can this be used.  If not, false is returned.
 * If it can be used, then inUse_ is set to return and true is returned
 * @return
 */
public synchronized boolean canUse() {
   if (inUse_) {
	   return false; 
   } else {
	   inUse_ = true; 
	   return true; 
   }
}


/**
 * get the data buffer
 */
public synchronized byte[] getData() {
	// if (!inUse_) {
		// Error case for now just trace it
  	    // Debugging code. 
    	// if (! DataStream.traceOpened) { 
      	//	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
      	//	try { 
       	//	Trace.setFileName("/tmp/toolboxTrace."+sdf.format(new Date())+".txt");
      	//	} catch (Exception e) { 
      	//		
      	//	}
      	//   DataStream.traceOpened=true; 
       	//}

    	//boolean traceTurnedOn = false; 
      	//if (!Trace.traceOn_) {
      	//	traceTurnedOn = true;
      	//	Trace.setTraceAllOn(true); 
      	//	Trace.setTraceOn(true); 
      	//}
      	// Exception e = new Exception("getData() called when inUse_ = "+inUse_);
        // Trace.log(Trace.ERROR, "Debug0601: DBStorage.getData() called when DBStorage not in use", e); 
     	// if (traceTurnedOn) { 
  	  	//  Trace.setTraceAllOn(false); 
  		//  Trace.setTraceOn(false); 
  	    //}

	// }
	return data_;
}
/**
Clears the contents of the storage.
**/
//@P0D  void clear ()
//@P0D  {
    // No-op.
//@P0D  }



/**
Returns the id.

@return     id
**/
//@P0D  int getId ()
//@P0D  {
//@P0D    return id_;
//@P0D  }



/**
Returns a reference to the enclosed byte array.

@return     a reference to the byte array.
**/
//@P0D  byte[] getReference ()
//@P0D  {
//@P0D    return data_;
//@P0D  }



}


