///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBReplyServerAttributes.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



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



    // Returns the copyright.
    private static String getCopyright()
    {
       return Copyright.copyright;
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
		return BinaryConverter.byteArrayToShort (data_, offset_ + 14);
	}


        final public int getDecimalSeparatorPO ()	throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 8);
	}



	final public String getDefaultSQLLibraryName (ConverterImplRemote converter) throws DBDataStreamException
	{
		return converter.byteArrayToString (data_, offset_ + 78, 10);
	}



	final public int getDRDAPackageSize ()  throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 16);
	}



	final public int getIgnoreDecimalDataErrorPO ()	throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 12);
	}



	final public String getLanguageFeatureCode (ConverterImplRemote converter) throws DBDataStreamException
	{
		return converter.byteArrayToString (data_, offset_ + 46, 4);
	}



	final public int getNamingConvetionPO ()  throws DBDataStreamException
    {
		return BinaryConverter.byteArrayToShort (data_, offset_ + 10);
	}



    final public String getRelationalDBName (ConverterImplRemote converter) throws DBDataStreamException
	{
		return converter.byteArrayToString (data_, offset_ + 60, 18);
	}



	final public int getServerCCSID ()  throws DBDataStreamException
	{
		return BinaryConverter.byteArrayToShort (data_, offset_ + 19);
	}



	final public String getServerFunctionalLevel (ConverterImplRemote converter) throws DBDataStreamException
	{
		return converter.byteArrayToString (data_, offset_ + 50, 10);
	}



    final public String getServerJobIdentifier(ConverterImplRemote converter) throws DBDataStreamException  // @E1A
	{                                                                                                       // @E1A
		return converter.byteArrayToString(data_, offset_ + 88, 26);                                        // @E1A
	}                                                                                                       // @E1A



	final public String getServerLanguageId (ConverterImplRemote converter)   throws DBDataStreamException
	{
		return converter.byteArrayToString (data_, offset_ + 23, 3);
	}



	final public String getServerLanguageTable (ConverterImplRemote converter)	 throws DBDataStreamException
	{
		return converter.byteArrayToString (data_, offset_ + 26, 10);
	}



	final public String getServerLanguageTableLibrary (ConverterImplRemote converter)
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

