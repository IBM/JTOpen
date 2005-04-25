///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBOriginalData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBOriginalData class is an implementation of DBData which
describes the data used in datastreams for V4R3 and previous
servers.
**/
class DBOriginalData
implements DBData
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



    // Private data.
    private byte[]  rawBytes_           = null;
    private int     offset_             = -1;
    private int     actualLength_       = -1;                               // @D0A
    private boolean dataCompressed_     = false;                            // @D0A

    private int     rowCount_           = -1;
    private int     columnCount_        = -1;
    private int     indicatorSize_      = -1;
    private int     rowSize_            = -1;

    private int     indicatorOffset_    = -1;
    private int     dataOffset_         = -1;
    private int     length_             = -1;

    private int     aliasCount_         = 0;                                //@K3A



/**
Constructs a DBOriginalData object.  Use this when overlaying
on a reply datastream.  The cached data will be set when overlay()
is called.
**/
	public DBOriginalData (int actualLength, boolean dataCompressed)        // @D0C
	{ 
        actualLength_   = actualLength;                                     // @D0A
        dataCompressed_ = dataCompressed;                                   // @D0A      
    }



/**
Constructs a DBOriginalData object.  Use this when overlaying
on a request datastream.  This sets the cached data so that
the total length can be calculated before calling overlay().
**/
	public DBOriginalData (int rowCount,
	                       int columnCount,
	                       int indicatorSize,
	                       int rowSize)
        throws DBDataStreamException
	{
        rowCount_       = rowCount;
	    columnCount_    = columnCount;
	    indicatorSize_  = indicatorSize;
	    rowSize_        = rowSize;

	    length_         = 14 + rowCount_ * (columnCount_ * indicatorSize_
	                        + rowSize_);
	}



/**
Positions the overlay structure.  This reads the cached data only
when it was not previously set by the constructor.
**/
    public void overlay (byte[] rawBytes, int offset)
    {
	    offset_             = offset;

        if (rowCount_ == -1) {
            rowCount_       = BinaryConverter.byteArrayToInt (rawBytes, offset_ + 4);
    	    columnCount_    = BinaryConverter.byteArrayToShort (rawBytes, offset_ + 8);
	        indicatorSize_  = BinaryConverter.byteArrayToShort (rawBytes, offset_ + 10);
	        rowSize_        = BinaryConverter.byteArrayToShort (rawBytes, offset_ + 12);
	        length_         = 14 + rowCount_ * (columnCount_ * indicatorSize_ + rowSize_);

            // If the data is compressed, then we need to uncompress it and store               // @D0A
            // it in a new byte array.  Note that only the indicators and data are              // @D0A
            // compressed.                                                                      // @D0A
            if (dataCompressed_) {                                                              // @D0A
                byte[] decompressedBytes = new byte[length_];                                   // @D0A
                System.arraycopy(rawBytes, offset_, decompressedBytes, 0, 14);                  // @D0A
                JDUtilities.decompress(rawBytes, offset_ + 14, actualLength_ - 20,              // @D0A
                                       decompressedBytes, 14);                                  // @D0A
                                                                                                
                rawBytes_           = decompressedBytes;                                        // @D0A
                indicatorOffset_    = 14;                                                       // @D0A
            }                                                                                   // @D0A
            else {                                                                              // @D0A
                rawBytes_           = rawBytes;
                indicatorOffset_    = offset_ + 14;
            }                                                                                   // @D0A
	    }
	    else {
            rawBytes_           = rawBytes;
            indicatorOffset_    = offset_ + 14;

	        setRowCount (rowCount_);
	        setColumnCount (columnCount_);
	        setIndicatorSize (indicatorSize_);
	        setRowSize (rowSize_);
	    }

	    dataOffset_         = indicatorOffset_ + (rowCount_ * columnCount_ * indicatorSize_);
	}
    


	public int getLength ()
	{
	    return length_;
	}



	public int getConsistencyToken ()
	{
		return BinaryConverter.byteArrayToInt (rawBytes_, offset_);
	}



	public int getRowCount ()
	{
		return rowCount_;
	}



	public int getColumnCount ()
	{
		return columnCount_;
	}



	public int getIndicatorSize ()
	{
		return indicatorSize_;
	}



	public int getRowSize ()
	{
		return rowSize_;
	}



	public int getIndicator (int rowIndex, int columnIndex)
	{
		if (indicatorSize_ == 0)
			return 0;
		else
			return BinaryConverter.byteArrayToShort (rawBytes_,
			    indicatorOffset_ + indicatorSize_
			    * ((rowIndex+aliasCount_) * columnCount_ + columnIndex));           //@K3C If aliasCount_ > 0, then DatabaseMetaData.getTables(...) was called and our result data contains aliases.  We want to skip the rows that are aliases.
	}



	public int getRowDataOffset (int rowIndex)
	{
	    return dataOffset_ + ((rowIndex+aliasCount_) * rowSize_);        //@K3C If aliasCount_ > 0, then DatabaseMetaData.getTables(...) was called and our result data contains aliases.  We want to skip the rows that are aliases.
	}



    public byte[] getRawBytes ()
    {
        return rawBytes_;
    }



    public void setConsistencyToken (int consistencyToken)
    {
        BinaryConverter.intToByteArray (consistencyToken, rawBytes_,
            offset_);
    }



    public void setRowCount (int rowCount)
    {
        BinaryConverter.intToByteArray (rowCount, rawBytes_,
            offset_ + 4);
    }



    public void setColumnCount (int columnCount)
    {
        BinaryConverter.shortToByteArray ((short) columnCount, rawBytes_,
            offset_ + 8);
    }



    public void setIndicatorSize (int indicatorSize)
    {
        BinaryConverter.shortToByteArray ((short) indicatorSize, rawBytes_,
            offset_ + 10);
    }



    public void setRowSize (int rowSize)
    {
        BinaryConverter.shortToByteArray ((short) rowSize, rawBytes_,
            offset_ + 12);
    }



    public void setIndicator (int rowIndex, int columnIndex, int indicator)
    {
		if (indicatorSize_ != 0)
			BinaryConverter.shortToByteArray ((short) indicator,
			    rawBytes_,
			    indicatorOffset_ + indicatorSize_
			    * (rowIndex * columnCount_ + columnIndex));
    }


    //This will always return false, because variable-length field compression only applies
    //to information returned in a result set for V5R3 and later servers.  This class only
    //applies to V4R3 and previous servers.
    public boolean isVariableFieldsCompressed(){                       //@K54
        return false;
    }

    // Resets the number of rows to the total number of rows minus the number of rows that contain aliases if DatabaseMetaData.getTables(...) was called.
    // This method is called by AS400JDBCDatabaseMetaData.parseResultData().
    public void resetRowCount(int rowCount) //@K3A
    {
        rowCount_ = rowCount;
    }

    // Sets the number of aliases the result data contains if DatabaseMetaData.getTables(...) was called.
    // This method is called by AS400JDBCDatabaseMetaData.parseResultData().
    public void setAliasCount(int aliasCount)   //@K3A
    {
        aliasCount_ = aliasCount;
    }

}

