///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DBLobData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBLobData class represents the data retrieved using a LOB locator.
**/
class DBLobData
implements DBOverlay
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private byte[]  rawBytes_           = null;
    private int     offset_             = -1;
    private int     actualLength_       = -1;                               
    private boolean dataCompressed_     = false;                            
    private int     physicalLength_     = -1;                                   // @A1C

    // Note: The "actual length" is the length of the lob data.  The            // @A1A
    //       "physical length" is the number of bytes as they appear            // @A1A
    //       in the datastream.                                                 // @A1A


/**
Constructs a DBLobData object.  Use this when overlaying
on a reply datastream. 
**/
	public DBLobData (int actualLength, 
                      int physicalLength,                                       // @A1A
                      boolean dataCompressed)
	{ 
        actualLength_   = actualLength; 
        physicalLength_ = physicalLength;                                       // @A1A
        dataCompressed_ = dataCompressed;      
    }



    public int getOffset()
    {
        return offset_;
    }



/**
Positions the overlay structure.  This reads the cached data only
when it was not previously set by the constructor.
**/
    public void overlay (byte[] rawBytes, int offset)
    {
	    offset_             = offset;

        // If the data is compressed, then we need to uncompress it and store               
        // it in a new byte array. 
        if (dataCompressed_) {                                                              
            byte[] decompressedBytes = new byte[actualLength_];                 // @A1C
            JDUtilities.decompress(rawBytes, offset_, physicalLength_, decompressedBytes, 0);  
            rawBytes_           = decompressedBytes;                     
        }                                                                           
        else {                                                                              
            rawBytes_           = rawBytes;
        }                                                                                  
	}



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }
    


	public int getLength ()
	{
	    return actualLength_;
	}



    public byte[] getRawBytes ()
    {
        return rawBytes_;
    }



}

