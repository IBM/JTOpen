///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DBStorage.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
class DBStorage
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    private byte[]  data_;
    private int     id_;



/**
Constructs a DBStorage object.

@param      id      an id assigned by the pool.
**/
	DBStorage (int id)
	{
	    // Initialize to 64 KB.
	    data_ = new byte[65536];

	    id_ = id;
    }



/**
Checks the size of the array and resizes the storage if needed.

@param  size        size that is needed.

@return     true if the storage was resize; false otherwise.
**/
    boolean checkSize (int size)
    {
        if (size > data_.length) {
            // Double the size each time.
            byte[] newdata = new byte[Math.max (data_.length * 2, size)]; // @C1C
            System.arraycopy (data_, 0, newdata, 0, data_.length);
            data_ = newdata;
            return true;
        }
        return false;
    }



/**
Clears the contents of the storage.
**/
    void clear ()
    {
        // No-op.
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
Returns the id.

@return     id
**/
    int getId ()
    {
        return id_;
    }



/**
Returns a reference to the enclosed byte array.

@return     a reference to the byte array.
**/
    byte[] getReference ()
    {
        return data_;
    }



}



