///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBReturnObjectInformationRequestDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
   Create a Return Object Information (ROI) Request Datastream
**/

class DBReturnObjectInformationRequestDS
extends DBBaseRequestDS
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	public static final int	FUNCTIONID_FIELD_INFO              = 0x1807;
	public static final int	FUNCTIONID_FILE_INFO               = 0x1804;
	public static final int	FUNCTIONID_FILE_MBR_INFO           = 0x1805;
	public static final int	FUNCTIONID_FOREIGN_KEY_INFO        = 0x1809;
	public static final int	FUNCTIONID_INDEX_INFO              = 0x1808;
	public static final int	FUNCTIONID_PACKAGE_INFO            = 0x1802;
	public static final int	FUNCTIONID_PACKAGE_STATEMENT_INFO  = 0x1803;
	public static final int	FUNCTIONID_PRIMARY_KEY_INFO        = 0x180A;
	public static final int	FUNCTIONID_RECORD_FORMAT_INFO      = 0x1806;
	public static final int	FUNCTIONID_RETRIEVE_LIBRARY_INFO   = 0x1800;
	public static final int	FUNCTIONID_RETRIEVE_RELATIONAL_DB_INFO = 0x1801;
	public static final int	FUNCTIONID_SPECIAL_COLUMN_INFO     = 0x180B;
	public static final int	FUNCTIONID_USER_PROFILE_INFO       = 0x180C;


/**
   Constructs a datastream for the ROI Server functions.
   @param  requestId the 4 digit code that represents the function being called.
   @param  rpbId   the request parameter block id.
   @param  operationResultsBitmap the bitmap which describes how the results are to be returned.
   @param  parameterMarkerDescriptorHandle the Parameter marker descriptor handle identifier.
**/
   	public DBReturnObjectInformationRequestDS(int requestId,
		        			      int rpbId,
				    		      int operationResultsBitmap,
					      	      int parameterMarkerDescriptorHandle)

    {
	   // Create the data stream header and template
	   super(requestId, rpbId, operationResultsBitmap,
		     parameterMarkerDescriptorHandle);
	   setServerID(SERVER_ROI);
    }


// Returns the copyright.
  private static String getCopyright()
  {
    return Copyright.copyright;
  }


    //--------------------------------------------------//
    // Create the data stream optional /                //
    // variable length data section via addParameters   //
    //--------------------------------------------------//


/**
   Sets the Field Information Order By Indicator parameter in the data stream.
   @param value	the value that indicates how the returned rows
   will be ordered
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFieldInformationOrderByIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x382E, (short)value);
	}



/**
   Sets the Field Name parameter in the data stream.
   @param value	the name of the field.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFieldName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x380C, converter, value);
	}



/**
   Sets the Field Name Search Pattern Indicator parameter in the data stream.
   @param value	the value used to indicate if search pattern
   and escape character checking should be done on the field.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFieldNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x381B, (byte)value);
	}



/**
   Sets the Field Information to Return bitmap parameter in the data stream.
   @param value	the value used to determine what field
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFieldReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3824, value);
	}



/**
   Sets the File Attribute parameter in the data stream.
   @param value	the file types to obtain information about.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFileAttribute(int value)
		throws DBDataStreamException
	{
		addParameter (0x3809, (short)value);
	}



/**
   Sets the File Info Order By Indicator parameter in the data stream.
   @param value	the value used to determine how the rows
   will be ordered.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFileInfoOrderByIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x382D, (short)value);
	}



/**
   Sets the File Name parameter in the data stream.
   @param value	the name of the file.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setFileName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3802, converter, value);
	}



/**
   Sets the File Name Search Pattern Indicator parameter in the data stream.
   @param value	 the value used to indicate if search pattern
   and escape character checking should be done on the file name.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFileNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x3817, (byte)value);
	}



/**
   Sets the File Owner Name parameter in the data stream.
   @param value	the name of the file owner.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setFileOwnerName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3808, converter, value);
	}



/**
   Sets the File Owner Name Search Pattern Indicator parameter in the data stream.
   @param value	the value used to indicate if search pattern
   and escape character checking should be done on the file owner name.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFileOwnerNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x3818, (byte)value);
	}



/**
   Sets the File Information to Return Bitmap parameter in the data stream.
   @param value	the value used to determine what file
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFileReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3821, value);
	}



/**
   Sets the File Short / long Name Indicator parameter in the data stream.
   @param value	the value used to determine if the file name
   specified is to be used as a short or long file name.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFileShortOrLongNameIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x382A, (byte)value);
	}



/**
   Sets the Foreign Key File Library Name parameter in the data stream.
   @param value	the name of the library which contains
   the foreign key file.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setForeignKeyFileLibraryName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3811, converter, value);
	}



/**
   Sets the Foreign Key File Name parameter in the data stream.
   @param value	the name of the foreign key file.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setForeignKeyFileName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3813, converter, value);
	}



/**
   Sets the Foreign Key File Owner parameter in the data stream.
   @param value	the name of the foreign key file owner.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setForeignKeyFileOwner(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3812, converter, value);
	}



/**
   Sets the Foreign Keys Information to Return bitmap parameter in the data stream.
   @param value	the value used to determine what foreign key
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setForeignKeyReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3826, value);
	}



/**
   Sets the Format Name parameter in the data stream.
   @param value	the name of the file records.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFormatName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x380B, converter, value);
	}



/**
   Sets the Format Name Search Pattern Indicator parameter in the data stream.
   @param value	the value used to indicate if search pattern
   and escape character checking should be done on the format name.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFormatNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x381A, (byte)value);
	}



/**
   Sets the Record Format Information to Return Bitmap parameter in the data stream.
   @param value	the value used to determine what record format
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFormatReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3823, value);
	}



/**
   Sets the Index Information to Return Bitmap parameter in the data stream.
   @param value	the value used to determine what index
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setIndexReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3825, value);
	}



/**
   Sets the Index Unique Rule parameter to the data stream.
   @param value	the index rule to follow.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setIndexUniqueRule(int value)
		throws DBDataStreamException
	{
		addParameter (0x380D, (short)value);
	}



/**
   Sets the library name parameter in the data stream.
   @param value	the name of the library.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setLibraryName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3801, converter, value);
	}



/**
   Sets the Library Name Search Pattern Indicator parameter in the data stream.
   @param value	the value used to indicate if search pattern
   and escape character checking should be done on the library name.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setLibraryNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x3816, (byte)value);
	}



/**
   Sets the Library Information to Return Bitmap parameter in the data stream.
   @param value	the value used to determine what library
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setLibraryReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x381D, value);
	}



/**
   Sets the Member Name parameter in the data stream.
   @param value	the name of the member.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/

	void setMemberName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3803, converter, value);
	}



/**
   Sets the Member Name Search Pattern Indicator parameter in the data stream.
   @param value	the value used to indicate if search pattern
   and escape character checking should be done on the member name.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setMemberNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x3819, (byte)value);
	}



/**
   Sets the Member Information to Return bitmap parameter in the data stream.
   @param value the value used to determine what member
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setMemberReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3822, value);
	}



/**
   Sets the Package Name parameter in the data stream.
   @param value	the name of the package.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPackageName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3804, converter, value);
	}



/**
   Sets the Package Name Search Pattern Indicator parameter in the data stream.
   @param value the value used to indicate if search pattern
   and escape character checking should be done on the package name.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPackageNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x382C, (byte)value);
	}



/**
   Sets the Package Information to Return bitmap parameter in the data stream.
   @param value	 the value used to determine what package
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPackageReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x381F, value);
	}



/**
   Sets the Package Statement Type parameter in the data stream.
   @param value	the type of package statement to obtain
   information about.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPackageStatementType(int value)
		throws DBDataStreamException
	{
		addParameter (0x3807, (short)value);
	}



/**
   Sets the Physical File Type parameter in the data stream.
   @param value	the type of physical file to obtain
   information about.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPhysicalFileType(int value)
		throws DBDataStreamException
	{
		addParameter (0x380A, (short)value);
	}



/**
   Sets the Primary Key File Library Name parameter in the data stream.
   @param value	the library which contains the primary key
   file.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPrimaryKeyFileLibraryName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x380E, converter, value);
	}



/**
   Sets the Primary Key File Name parameter in the data stream.
   @param value	the name of the primary key file.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPrimaryKeyFileName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3810, converter, value);
	}



/**
   Sets the Primary Key File Owner parameter in the data stream.
   @param value	the name of the primary key file owner.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPrimaryKeyFileOwner(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x380F, converter, value);
	}



/**
   Sets the Primary Keys Information to Return bitmap parameter in the data stream.
   @param value	the value used to determine what primary key
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setPrimaryKeyReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3827, value);
	}



/**
   Sets the Relational Database Name parameter in the data stream.
   @param value	the name of the relational database.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setRelationalDatabaseName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3806, converter, value);
	}



/**
   Sets the Relational Database Name Search Pattern Indicator parameter in the data stream.
   @param value	the value used to indicate if search pattern
   and escape character checking should be done on the relational
   database.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setRelationalDatabaseNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x382B, (byte)value);
	}



/**
   Sets the Relational Database Information to Return bitmap parameter in the data stream.
   @param value	the value used to determine what relation database
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setRealtionalDatabaseReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x381E, value);
	}



/**
   Sets the Special Columns Nullable Indicator parameter in the data stream.
   @param value	the value which indicates if columns or
   nullable or not.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setSpecialColumnsNullableIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x3814, (byte)value);
	}



/**
   Sets the Special Columns Information to Return bitmap parameter in the data stream.
   @param value	the value used to determine what special columns
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setSpecialColumnsReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3828, value);
	}



/**
   Sets the Statement Information to Return bitmap parameter in the data stream.
   @param value	the value used to determine what statement
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setStatementReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3820, value);
	}



/**
   Sets the Statement Text Length To Return parameter in the data stream.
   @param value	the length of the statement text to be
   returned to the client.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setStatementTextLengthToReturn(int value)
		throws DBDataStreamException
	{
		addParameter (0x382F, (short)value);
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



/**
   Sets the User Profile Name parameter in the data stream.
   @param value	the name of the user profile.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setUserProfileName(String value, ConverterImplRemote converter)
		throws DBDataStreamException
	{
		addParameter (0x3815, converter, value);
	}



/**
   Sets the User Profile Name Search Pattern Indicator parameter in the data stream.
   @param value	the value used to indicate if search pattern
   and escape character checking should be done on the user profile.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setUserProfileNameSearchPatternIndicator(int value)
		throws DBDataStreamException
	{
		addParameter (0x381C, (byte)value);
	}



/**
   Sets the User Profile Information to Return bitmap parameter in the data stream.
   @param value	the value used to determine what user profile
   information the client wants returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setUserProfileReturnInfoBitmap(int value)
		throws DBDataStreamException
	{
		addParameter (0x3829, value);
	}



}  // End of DBReturnObjectInformationRequestDS class






