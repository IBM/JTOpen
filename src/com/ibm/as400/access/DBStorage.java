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



/**
<p>This class manages a large byte arrays for use
in creating request datastreams.  This enables reuse of
the byte array, so that it does not have to be reallocated
repeatedly.
**/
final class DBStorage //@P0C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  byte[] data_ = new byte[1024]; //@P0C
  //@P0D private int     id_;

  boolean inUse_ = false; //@P0A

/**
Constructs a DBStorage object.

@param      id      an id assigned by the pool.
**/
//@P0D  DBStorage (int id)
//@P0D  {
    // Initialize to 63 KB.  This used to be 64K 
    // The AS/400 or iSeries JVM adds 24 bytes of
    // overhead to each object so a 64K byte array really
    // takes 64K + 24 bytes.  The AS/400 or iSeries JVM has a boundry
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
  final boolean checkSize(final int size) //@P0C
  {
    if (size > data_.length)
    {
      // Double the size each time.
      byte[] newdata = new byte[Math.max(data_.length * 2, size)]; // @C1C
      System.arraycopy(data_, 0, newdata, 0, data_.length);
      data_ = newdata;
      return true;
    }
    return false;
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


