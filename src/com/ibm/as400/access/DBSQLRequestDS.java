///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBSQLRequestDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;                                            //@E9a


/**
The DBSQLRequestDS class represents a request datastream
to the SQL server.
**/
class DBSQLRequestDS
extends DBBaseRequestDS
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";




    // Function ids.
    public static final int	FUNCTIONID_CLEAR_PACKAGE     	 = 0x1810;
    public static final int	FUNCTIONID_CLOSE     	         = 0x180A;
    public static final int	FUNCTIONID_CONNECT     	         = 0x1809;
    public static final int	FUNCTIONID_COMMIT     	         = 0x1807;
    public static final int	FUNCTIONID_CREATE_PACKAGE     	 = 0x180F;
    public static final int	FUNCTIONID_DELETE_PACKAGE     	 = 0x1811;
    public static final int	FUNCTIONID_DESCRIBE    	         = 0x1801;
    public static final int	FUNCTIONID_DESCRIBE_PARM_MARKER  = 0x1802;
    public static final int	FUNCTIONID_END_STREAM_FETCH    	 = 0x1813;
    public static final int	FUNCTIONID_EXECUTE     	         = 0x1805;
    public static final int	FUNCTIONID_EXECUTE_IMMEDIATE     = 0x1806;
    public static final int	FUNCTIONID_EXECUTE_OPEN_DESCRIBE = 0x1812;
    public static final int	FUNCTIONID_FETCH     	         = 0x180B;
    public static final int	FUNCTIONID_OPEN_DESCRIBE         = 0x1804;
    public static final int	FUNCTIONID_OPEN_DESCRIBE_FETCH   = 0x180E;
    public static final int	FUNCTIONID_PREPARE     	         = 0x1800;
    public static final int	FUNCTIONID_PREPARE_DESCRIBE    	 = 0x1803;
    public static final int	FUNCTIONID_PREPARE_EXECUTE     	 = 0x180D;
    public static final int	FUNCTIONID_RETRIEVE_LOB_DATA   	 = 0x1816;
    public static final int	FUNCTIONID_RETURN_PACKAGE     	 = 0x1815;
    public static final int	FUNCTIONID_ROLLBACK     	     = 0x1808;
    public static final int	FUNCTIONID_STREAM_FETCH     	 = 0x180C;
    public static final int	FUNCTIONID_WRITE_LOB_DATA   	 = 0x1817;
    public static final int	FUNCTIONID_CANCEL   	         = 0x1818;      // @E2A




    // Fetch scroll options.
    public static final int FETCH_NEXT          = 0x0000;
    public static final int FETCH_PREVIOUS      = 0x0001;
    public static final int FETCH_FIRST         = 0x0002;
    public static final int FETCH_LAST          = 0x0003;
    public static final int FETCH_BEFORE_FIRST  = 0x0004;
    public static final int FETCH_AFTER_LAST    = 0x0005;
    public static final int FETCH_CURRENT       = 0x0006;
    public static final int FETCH_RELATIVE      = 0x0007;

    //@F6A Constants for cursor sensitivity property (for >= v5r2 servers)
    public static final int CURSOR_NOT_SCROLLABLE_ASENSITIVE  = 0;      
    public static final int CURSOR_SCROLLABLE_ASENSITIVE      = 1;          
    public static final int CURSOR_SCROLLABLE_INSENSITIVE     = 2; 
    public static final int CURSOR_SCROLLABLE_SENSITIVE       = 3;
    public static final int CURSOR_NOT_SCROLLABLE_SENSITIVE   = 4;       
    public static final int CURSOR_NOT_SCROLLABLE_INSENSITIVE = 5;    


/**
   Constructs a datastream for the SQL Server request functions.
   @param  requestId the 4 digit code that represents the function being called.
   @param  rpbId   the request parameter block id.
   @param  operationResultsBitmap the bitmap which describes how the results are to be returned.
   @param  parameterMarkerDescriptorHandle the Parameter marker descriptor handle identifier.
**/
	public DBSQLRequestDS(int requestId,
		        	int rpbId,
				int operationResultsBitmap,
				int parameterMarkerDescriptorHandle)

    {
	   // Create the datastream header and template
	   super(requestId, rpbId, operationResultsBitmap,
		     parameterMarkerDescriptorHandle);
	   setServerID(SERVER_SQL);
    }





/**
   Sets the Blocking Factor parameter in the data stream.
   @param value	the blocking factor to be used on the fetch.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setBlockingFactor(int value)
		throws DBDataStreamException
	{
		addParameter (0x380C, value);
	}


//@F2A
/**
   Sets the Column Index parameter in the data stream.
   @param value	the column index to be used on a Retrieve LOB Data.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setColumnIndex(int value)
		throws DBDataStreamException
	{
		addParameter (0x3828, value);
	}


/**
   Sets the Compression Indicator parameter in the data stream.
   @param value	the value that indicates whether the LOB data
   should be compressed.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setCompressionIndicator (int value)
		throws DBDataStreamException
	{
		addParameter (0x381B, (byte)value);
	}



	void setReturnCurrentLengthIndicator(int value)                     // @E1A
		throws DBDataStreamException                                    // @E1A
	{                                                                   // @E1A
		addParameter(0x3821, (byte)value);                              // @E1A
	}                                                                   // @E1A



/**
   Sets the Cursor Name parameter in the data stream.
   @param value	the name for the open cursor.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
   @exception SQLException If the SQL statement is too long.          
**/
    void setCursorName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x380B, converter, value);
	}



/**
   Sets the Describe Option parameter in the data stream.
   @param value	the describe option to be used.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setDescribeOption(int value)
		throws DBDataStreamException
	{
		addParameter (0x380A, (byte)value);
	}


    //@F5A
    /**
   Sets the Extended Column Descriptor Option parameter in the data stream.
   @param value	the extended column descriptor option to be used.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setExtendedColumnDescriptorOption(int value)
		throws DBDataStreamException
	{
		addParameter (0x3829, (byte)value);
	}



/**
   Sets the Fetch Scroll Option parameter in the data stream.
   @param value	the scroll option to used with the cursor.
   @param rows  the number of rows, when value is FETCH_RELATIVE.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFetchScrollOption(int value, int rows)
		throws DBDataStreamException
	{
	    // The server gives an error when we pass rows
	    // and value != FETCH_RELATIVE.
	    if (value == FETCH_RELATIVE)
    		addParameter (0x380E, (short)value, rows);
        else
            addParameter (0x380E, (short)value);
	}



/**
   Sets the Hold Indicator parameter in the data stream.
   @param value	the commit operation that is to be performed.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	 void setHoldIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x380F, (byte)value);
	}



     void setJobIdentifier(String value, ConvTable converter)  // @E2A @P0C
         throws DBDataStreamException, SQLException                      // @E9c @E2A
     {                                                                   // @E2A
         addParameter(0x3826, converter, value);                         // @E2A
     }                                                                   // @E2A
 
 
 
/**
   Sets the Library Name parameter in the data stream.
   @param value	the name of the library.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
   @exception SQLException If the SQL statement is too long.          
**/
   	void setLibraryName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3801, converter, value);
	}



/**
   Sets the LOB Allocate Locator Indicator parameter in the data stream.
   @param value	the value that indicates whether a LOB locator
   should be allocated for this request.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setLOBAllocateLocatorIndicator (int value)
		throws DBDataStreamException
	{
		addParameter (0x381C, (byte)value);
	}


/**
   Sets the LOB Data parameter in the data stream.
   @param value the LOB data.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setLOBData (byte[] value)
		throws DBDataStreamException
	{
  		addParameter (0x381D, value, true);            // @C1C
	}

  void setLOBData(byte[] value, int offset, int length) throws DBDataStreamException
  {
    addParameter(0x381D, value, offset, length, true);
  }


/**
   Sets the LOB Locator Handle parameter in the data stream.
   @param value the LOB locator handle.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setLOBLocatorHandle (int value)
		throws DBDataStreamException
	{
  		addParameter (0x3818, value);
	}



/**
   Sets the Open Attributes parameter in the data stream.
   @param value	the open attributes of the referenced file.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setOpenAttributes(int value)
		throws DBDataStreamException
	{
		addParameter (0x3809, (byte)value);
	}



/**
   Sets the SQL Package Name parameter in the data stream.
   @param value	the name of the SQL package to use.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
   @exception SQLException If the SQL statement is too long.          
**/
   	void setPackageName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3804, converter, value);
	}



// $F1 This parameter no longer needs to be passed on a data stream.  Package clearing
// $F1 and the decision for the threshold where package clearing is needed is now handled
// $F1 automatically by the database.  Passing this code point results in a no-op.
//@F1D /**
//@F1D    Sets the Package Threshold Value parameter in the data stream.
//@F1D    @param value	the value used to determine if the package
//@F1D    should be cleared.
//@F1D    @exception DBDataStreamException If there is not enough space left in the data byte array.
//@F1D **/
//@F1D	void setPackageThresholdValue(int value)
//@F1D		throws DBDataStreamException
//@F1D	{
//@F1D		addParameter (0x3813, (short)value);
//@F1D	}



/**
   Sets the Parameter Marker Block Indicator parameter in the data stream.
   @param value	the value that indicates whether the parameter
   marker data is intended for a block operation or a non-block operation.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   void setParameterMarkerBlockIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x3814, (short)value);
	}



/**
   Sets the Parameter Marker Data parameter in the data stream.
   @param value	the parameter marker data object.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setParameterMarkerData(DBData value)
		throws DBDataStreamException
	{
	    if (value instanceof DBOriginalData)
    		addParameter (0x3811, value);
        else if (value instanceof DBExtendedData)
            addParameter (0x381F, value);
        else
            throw new DBDataStreamException ();
	}



/**
   Sets the Prepare Option parameter in the data stream.
   @param value	the prepare option to use.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPrepareOption(int value)
		throws DBDataStreamException
	{
		addParameter (0x3808, (byte)value);
	}



/**
   Sets the Prepare Statement Name parameter in the data stream.
   @param value	the name of the parameter statement.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
   @exception SQLException If the SQL statement is too long.          
**/
   	void setPrepareStatementName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3806, converter, value);
	}



/**
   Sets the Requested Size parameter in the data stream.
   @param value the request size, in bytes.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setRequestedSize (int value)
		throws DBDataStreamException
	{
  		addParameter (0x3819, value);
	}



//@F3A
/**
   Sets the ResultSet Holdability Option parameter in the data stream.
   @param value	the value that contains the option to indicate how the cursor should
   be opened.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setResultSetHoldabilityOption(byte value)
		throws DBDataStreamException
	{
		addParameter (0x3830, value);
	}



/**
   Sets the Return Size parameter in the data stream.
   @param value	the value that indicates the number
   of megabytes of data to return from the package.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setReturnSize(int value)
		throws DBDataStreamException
	{
		addParameter (0x3815, value);
	}



/**
   Sets the Reuse Indicator parameter in the data stream.
   @param value	the value that indicates if the client
   intends to open the cursor again for the same statement or not.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setReuseIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x3810, (byte)value);
	}



/**
   Sets the Scrollable Cursor Flag parameter in the data stream.
   @param value	the value to indicate whether or not
   cursor scrolling is supported.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setScrollableCursorFlag(int value)
		throws DBDataStreamException
	{
		addParameter (0x380D, (short)value);
	}



/**
   Sets the Start Offset parameter in the data stream.
   @param value the start offset, in bytes.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setStartOffset (int value)
		throws DBDataStreamException
	{
  		addParameter (0x381A, value);
	}



/**
   Sets the Statement Text parameter in the data stream.
   @param value	the text for the SQL statement.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
   @exception SQLException If the SQL statement is too long.          
**/
   	void setStatementText (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                                // @E9c
	{
		addParameter (0x3807, converter, value);
	}



/**
   Sets the Statement Type parameter in the data stream.
   @param value	the statement type.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setStatementType (int value)
		throws DBDataStreamException
	{
		addParameter (0x3812, (short)value);
	}



/**
   Sets the Translate Indicator parameter in the data stream.
   @param value	the value that indicates if the data in the
   operational result set generated by this function should be
   translated to the client's CCSID before the data is returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setTranslateIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x3805, (byte)value);
	}


}







