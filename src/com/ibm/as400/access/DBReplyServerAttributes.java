///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBReplyServerAttributes.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   Provides access to the Server Attributes portion of the
   reply data stream.
**/
class DBReplyServerAttributes
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



    private byte[]      data_;
    private int         offset_;
    private int         length_;



	public DBReplyServerAttributes (byte[] data,
					           int offset,
        					   int length)
        throws DBDataStreamException
	{
	    data_ = data;
	    offset_ = offset;
	    length_ = length;
	}



	final public int getCommitmentControlLevelPO ()  throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 14);
	}



	final public int getDateFormatPO () throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_);
	}


	final public int getDateSeparatorPO () throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 2);      // @D1c (used to be "offset_ + 14")
	}


        final public int getDecimalSeparatorPO ()	throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 8);
	}



	final public String getDefaultSQLLibraryName (ConvTable converter) throws DBDataStreamException //@P0C
	{
		return converter.byteArrayToString (data_, offset_ + 78, 10);
	}


    //@128sch
	/* method for retrieving default schema of lengh up to 128 */
	//adding this method now eventhough it is not called anywhere yet. (same as getDefaultSQLLibraryName)
    final public String getDefaultSQLSchemaName (ConvTable converter) throws DBDataStreamException
    {
        int schemaLen = BinaryConverter.byteArrayToShort (data_, offset_ + 114); //orig doc says bin(15), but should be bin(16)?
        return converter.byteArrayToString (data_, offset_ + 116, schemaLen);
    }

    

	final public int getDRDAPackageSize ()  throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 16);
	}



	final public int getIgnoreDecimalDataErrorPO ()	throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 12);
	}



	final public String getLanguageFeatureCode (ConvTable converter) throws DBDataStreamException //@P0C
	{
		return converter.byteArrayToString (data_, offset_ + 46, 4);
	}



	final public int getNamingConvetionPO ()  throws DBDataStreamException
    {
		return BinaryConverter.byteArrayToShort (data_, offset_ + 10);
	}



    final public String getRelationalDBName (ConvTable converter) throws DBDataStreamException //@P0C
	{
		return converter.byteArrayToString (data_, offset_ + 60, 18);
	}



	final public int getServerCCSID ()  throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToUnsignedShort (data_, offset_ + 19); //@P1C
	}



	final public String getServerFunctionalLevel (ConvTable converter) throws DBDataStreamException //@P0C
	{
		return converter.byteArrayToString (data_, offset_ + 50, 10);
	}



    final public String getServerJobIdentifier(ConvTable converter) throws DBDataStreamException  // @E1A @P0C
	{                                                                                                       // @E1A
		return converter.byteArrayToString(data_, offset_ + 88, 26);                                        // @E1A
	}                                                                                                       // @E1A



	final public String getServerLanguageId (ConvTable converter)   throws DBDataStreamException //@P0C
	{
		return converter.byteArrayToString (data_, offset_ + 23, 3);
	}



	final public String getServerLanguageTable (ConvTable converter)	 throws DBDataStreamException //@P0C
	{
		return converter.byteArrayToString (data_, offset_ + 26, 10);
	}



	final public String getServerLanguageTableLibrary (ConvTable converter) //@P0C
	{
		return converter.byteArrayToString (data_, offset_ + 36, 10);
	}


    final public int getServerNLSSValue () throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 21);
	}



	final public int getTimeFormatPO ()  throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 4);
	}



	final public int getTimeSeparatorPO ()  throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 6);
	}



	final public int getTranslationIndicator ()
	{
		return data_[offset_ + 18];
	}


 }

