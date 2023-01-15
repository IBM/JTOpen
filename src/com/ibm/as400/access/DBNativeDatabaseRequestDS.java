///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBNativeDatabaseRequestDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;                                            //@E9a

/**
   Create a Native Data Base (NDB) Request Datastream.
**/

class DBNativeDatabaseRequestDS
extends DBBaseRequestDS
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  public static final int FUNCTIONID_ADD_DATABASE_FILE_MBR    = 0x1802;
  public static final int FUNCTIONID_ADD_LIBRARY_LIST         = 0x180C;
  public static final int FUNCTIONID_CLEAR_DATABASE_FILE_MBR  = 0x1803;
  public static final int FUNCTIONID_CLEAR_SAVE_FILE          = 0x1808;
  public static final int FUNCTIONID_CREATE_DATABASE_FILE     = 0x1801;
  public static final int FUNCTIONID_CREATE_SAVE_FILE         = 0x1807;
  public static final int FUNCTIONID_CREATE_SRC_PHYSICAL_FILE = 0x1800;
  public static final int FUNCTIONID_DELETE_DATABASE_FILE_MBR = 0x1804;
  public static final int FUNCTIONID_DELETE_FILE              = 0x1809;
  public static final int FUNCTIONID_DELETE_OVERRIDE_DB_FILE  = 0x1806;
  public static final int FUNCTIONID_OVERRIDE_DATABASE_FILE   = 0x1805;
  public static final int FUNCTIONID_READ_FROM_SAVE_FILE      = 0x180A;
  public static final int FUNCTIONID_WRITE_TO_SAVE_FILE       = 0x180B;


/**
   Constructs a datastream for the NDB Server request functions.
   @param  requestId the 4 digit code that represents the function being called.
   @param  rpbId   the request parameter block id.
   @param  operationResultsBitmap the bitmap which describes how the results are to be returned.
   @param  parameterMarkerDescriptorHandle the Parameter marker descriptor handle identifier.
**/
    public DBNativeDatabaseRequestDS(int requestId,
		   			int rpbId,
				    	int operationResultsBitmap,
					int parameterMarkerDescriptorHandle)

  {
	// Create the datastream header and template
	super(requestId, rpbId, operationResultsBitmap,
		     parameterMarkerDescriptorHandle);
	setServerID(SERVER_NDB);
  }



       //--------------------------------------------------//
       // Create the data stream optional /         	   //
       // variable length data section via addParameters   //
       //--------------------------------------------------//


/**
   Sets the Authority parameter in the data stream.
   @param value	the public access authority.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setAuthority (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3808, converter, value);
	}


/**
   Sets the Based File Name parameter in the data stream.
   @param value	the name of the file from which the file
   to created is based.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setBasedFileName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x380B, converter, value);
	}


/**
   Sets the Based Library Name parameter in the data stream.
   @param value	the name of the library where the
   based on file is located.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setBasedLibraryName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x380C, converter, value);
	}



/**
   Sets the Copy Based File Data parameter in the data stream.
   @param value	 the value that indicates if the data in the
   based on file should be copied into the file just created.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setCopyBasedFileData(int value)
		throws DBDataStreamException
	{
		addParameter (0x3811, (byte)value);
	}


/**
   Sets the File Description parameter in the data stream.
   @param value	 the description of the file.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setFileDescription (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3809, converter, value);
	}



/**
   Sets the File Name parameter in the data stream.
   @param value	 the name of the file.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setFileName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3802, converter, value);
	}



/**
   Sets the Library Name parameter in the data stream.
   @param value	 the name of the library.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setLibraryName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3801, converter, value);
	}



/**
   Sets the List of Libraries parameter in the data stream.
   @param indicators the list of indicators that describe
   where to add the libraries to the list.
   @param libraries  the list of libraries to add to
   the job's library list.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   void setListOfLibraries (char[] indicators,
       						String[] libraries,
       						ConvTable converter) //@P0C
       		throws DBDataStreamException
    {
      addParameter (0x3813, converter, indicators, libraries);
    }



/**
   Sets the Maxmimum Number of Members parameter to the data stream.
   @param value	the values that indicates the maximum
   number of members the created source physical file can have.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setMaxNumberOfMembers (int value)
       	throws DBDataStreamException
    {
      addParameter (0x3807, (short) value);
    }



/**
   Sets the Member Description parameter in the data stream.
   @param value	the description of the member.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setMemberDescription (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x380A, converter, value);
	}



/**
   Sets the Member Name parameter in the data stream.
   @param value	the name of the member.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setMemberName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3803, converter, value);
	}



/**
   Sets the Override File Name parameter in the data stream.
   @param value	the name of the file to be overridden.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setOverrideFileName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x380D, converter, value);
	}



/**
   Sets the Override Library Name parameter in the data stream.
   @param value	the name of the library where the overriden
   file is located.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setOverrideLibraryName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x380E, converter, value);
	}



/**
   Sets the Override Member Name parameter in the data stream.
   @param value	the name of the overridden member.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setOverrideMemberName (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x380F, converter, value);
	}



/**
   Sets the Record Length parameter in the data stream.
   @param value	 the record length.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setRecordLength (int value)
       		throws DBDataStreamException
    {
      addParameter (0x3806, (short) value);
    }



/**
   Sets the Save File Data parameter in the data stream.
   @param value	 the data that is to be written to the
   specified save file.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setSaveFileData (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3812, converter, value);
	}



/**
   Sets the Save File Description parameter in the data stream.
   @param value	the description of the save file.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
   	void setSaveFileDescription (String value, ConvTable converter) //@P0C
		throws DBDataStreamException, SQLException                      // @E9c
	{
		addParameter (0x3810, converter, value);
	}



/**
   Sets the Translate Indicator parameter in the data stream.
   @param value	the value that indicates if the data in the
   operational result set generated by the function should be translated
   to the client's CCSID before the data is returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setTranslateIndicator (int value)
       		throws DBDataStreamException
    {
      addParameter (0x3805, (byte) value);
    }


}





