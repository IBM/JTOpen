///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBExtendedData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBExtendedData class is an implementation of DBData which
describes the data used in datastreams for V4R4 and later
servers.
**/
class DBExtendedData
implements DBData
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



    // Private data.
    private byte[]  rawBytes_           = null;
    private int     offset_             = -1;
    private int     actualLength_       = -1;                               // @D0A
    private boolean dataCompressed_     = false;                            // @D0A
    private boolean vlfCompressed_      = false;                            // @K54

    private int     rowCount_           = -1;
    private int     columnCount_        = -1;
    private int     indicatorSize_      = -1;
    private int     rowSize_            = -1;

    private int     indicatorOffset_    = -1;
    private int     dataOffset_         = -1;
    private int     length_             = -1;

    private int     offsetToRowInformationHeader_ = -1;                 //@K54
    private int     offsetToRowInformationArray_  = -1;                 //@K54


/**
Constructs a DBExtendedData object.  Use this when overlaying
on a reply datastream.  The cached data will be set when overlay()
is called.
**/
	public DBExtendedData (int actualLength, boolean dataCompressed)        // @D0C
	{ 
        actualLength_   = actualLength;                                     // @D0A
        dataCompressed_ = dataCompressed;                                   // @D0A      
    }


//@K54
/**
Constructs a DBExtendedData object.  Use this when overlaying
on a reply datastream.  The cached data will be set when overlay()
is called.
**/
public DBExtendedData (int actualLength, boolean dataCompressed, boolean vlfCompressed)       
{
    actualLength_   = actualLength;                                     
    dataCompressed_ = dataCompressed;                                         
    vlfCompressed_ = vlfCompressed;
}

/**
Constructs a DBExtendedData object.  Use this when overlaying
on a request datastream.  This sets the cached data so that
the total length can be calculated before calling overlay().
**/
	public DBExtendedData (int rowCount,
	                        int columnCount,
	                        int indicatorSize,
	                        int rowSize)
        throws DBDataStreamException
	{
        rowCount_       = rowCount;
	    columnCount_    = columnCount;
	    indicatorSize_  = indicatorSize;
	    rowSize_        = rowSize;

	    length_         = 20 + rowCount_ * (columnCount_ * indicatorSize_
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
	        rowSize_        = BinaryConverter.byteArrayToInt (rawBytes, offset_ + 16);
	        length_         = 20 + rowCount_ * (columnCount_ * indicatorSize_ + rowSize_);

            // If the data is compressed, then we need to uncompress it and store               // @D0A
            // it in a new byte array.  Note that only the indicators and data are              // @D0A
            // compressed.                                                                      // @D0A
            if (dataCompressed_) {                                                              // @D0A
		length_ = length_ + offset;							         //@E0A
                byte[] decompressedBytes = new byte[length_];                                   // @D0A
                System.arraycopy(rawBytes, 0, decompressedBytes, 0, offset_ + 20);              // @D0A	 //@E0C
                JDUtilities.decompress(rawBytes, offset_ + 20, actualLength_ - 26,              // @D0A
                                       decompressedBytes, offset_ + 20);                        // @D0A	 //@E0C
                                                                                                
                rawBytes_           = decompressedBytes;                                        // @D0A
                indicatorOffset_    = offset_ + 20;                                             // @D0A	 //@E0C
            }                                                                                   // @D0A
            else {                                                                              // @D0A
                rawBytes_           = rawBytes;
                indicatorOffset_    = offset_ + 20;
            }                                                                                   // @D0A
            //If the variable-length fields are compressed, then we need to know where each row starts   //@K54
            // Note:  only the data is compressed                                                       //@K54
            //Format is:
            // Consistency Token    4 bytes     (will match data format token for returned data, unused on parameter marker data)
            // Row count            4 bytes     (number of rows being sent.  This must be 1 for the open function.)
            // Column count         2 bytes     (number of columns/parameter markers in 1 row)
            // Indicator size       2 bytes     (0 if no indicators, otherwise 2)
            // Reserved             4 bytes
            // Row size             4 bytes     (size of 1 row in bytes)
            // Indicators           x bytes     (x = column count * row count * Indicator size)
            //   *********** above remained unchanged *************
            // Row Information Header:
            //      Row-Information-Array-Offset        4 bytes
            //      Number-of-rows-fetched              4 bytes
            // <data for row 1>
            // <data for row 2>
            // <data for row 3>
            // Row Information Array:
            //      Offset-to-row-data(1)               4 bytes
            //      Offset-to-row-data(2)               4 bytes
            //      Offset-to-row-data(3)               4 bytes
            if(vlfCompressed_){                                                                 //@K54
                offsetToRowInformationHeader_ = indicatorOffset_ + (columnCount_ * rowCount_ * indicatorSize_);
                //offset is calculated from the offset to Row Information header
                offsetToRowInformationArray_ = offsetToRowInformationHeader_ + BinaryConverter.byteArrayToInt(rawBytes, offsetToRowInformationHeader_);
            }
	    }
	    else {
            rawBytes_           = rawBytes;
            indicatorOffset_    = offset_ + 20;

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
			    * (rowIndex * columnCount_ + columnIndex));
	}



	public int getRowDataOffset (int rowIndex)
	{
            //if variable-length fields are comrpessed return row offset
            if(vlfCompressed_)                                      //@K54
            {
                if((rowIndex >= rowCount_) || rowIndex < 0)                //We do not have data for that row.
                    return -1;
                else
                    return offsetToRowInformationHeader_ + BinaryConverter.byteArrayToInt(rawBytes_, offsetToRowInformationArray_ + (rowIndex*4));//return rowOffset_[rowIndex];                        //@K54
            }
            else                                                    //@K54
            {
                return dataOffset_ + (rowIndex * rowSize_);
            }
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
        BinaryConverter.intToByteArray (rowSize, rawBytes_,
            offset_ + 16);
    }



    public void setIndicator (int rowIndex, int columnIndex, int indicator)
    {
		if (indicatorSize_ != 0)
			BinaryConverter.shortToByteArray ((short) indicator,
			    rawBytes_,
			    indicatorOffset_ + indicatorSize_
			    * (rowIndex * columnCount_ + columnIndex));
    }


    //@K54
    /*
    Specifies if variable-length fields are compressed.
    @return true if variable-length fields are compressed, false otherwise.
    */
    public boolean isVariableFieldsCompressed()
    {
        return vlfCompressed_;
    }

}

