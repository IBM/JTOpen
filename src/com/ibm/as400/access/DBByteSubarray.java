///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBByteSubarray.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBLOBData class represents a piece of a byte array without
making a copy.
**/
class DBByteSubarray
implements DBOverlay
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



    // Private data.
    private byte[]  rawBytes_           = null;
    private int     length_             = -1;
    private int     offset_             = -1;



/**
Constructs a DBByteSubarray object.  Use this when overlaying
on a reply datastream.

@param length   The length.
**/
	public DBByteSubarray(int length)
	{
	    length_ = length;
	}



/**
Positions the overlay structure.  This reads the cached data only
when it was not previously set by the constructor.
**/
    public void overlay (byte[] rawBytes, int offset)
    {
	    rawBytes_           = rawBytes;
	    offset_             = offset;
	}



//E1D /**
//E1D Copyright.
//E1D **/
//E1D     static private String getCopyright ()
//E1D     {
//E1D         return Copyright.copyright;
//E1D     }
    


    public int getLength ()
    {
        return length_;
    }



	public int getOffset ()
	{
	    return offset_;
	}



    public byte[] getRawBytes ()
    {
        return rawBytes_;
    }


    //@E1A Added this for updateable lobs, because we actually change
    //@E1A the number of bytes while updating.
    public void setLength (int length)
    {
        length_ = length;
    }



}

