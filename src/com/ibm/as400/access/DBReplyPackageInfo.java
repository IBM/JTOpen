///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DBReplyPackageInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
   Provides access to the package info portion of the reply data stream.
**/
class DBReplyPackageInfo {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    private byte[]      data_;
    private int         offset_;
    private int         length_;
    private int         jobCCSID_;                                          // @D1A


    /**
       Constructs a new DBReplyPackageInfo object.  It initializes
       the variables using the data provided.
    **/
    public DBReplyPackageInfo (byte[] data,
                               int offset,
                               int length,
                               int jobCCSID)                                // @D1A
    {
        data_ = data;
        offset_ = offset;
        length_ = length;
        jobCCSID_ = jobCCSID;                                               // @D1A
    }



    // Returns the copyright.
    private static String getCopyright()
    {
        return Copyright.copyright;
    }


    public DBDataFormat getDataFormat (int statementIndex)
    throws DBDataStreamException
    {
        int offset = BinaryConverter.byteArrayToInt (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 40);
        int length = BinaryConverter.byteArrayToInt (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 44);
        if ((length != 6) && (length != 0)) {                           // @C0C
            DBDataFormat dataFormat = new DBSQLDADataFormat(jobCCSID_); // @D1C
            dataFormat.overlay (data_, (offset_ - 6) + offset);
            return dataFormat;
        }
        else
            return null;
    }



    public String getDefaultCollection (ConverterImplRemote converter)
    throws DBDataStreamException
    {
        return converter.byteArrayToString (data_, offset_ + 6, 18);
    }



    public int getCCSID ()
    throws DBDataStreamException
    {
        return BinaryConverter.byteArrayToShort (data_, offset_ + 4);
    }



    private int getPackageEntryInfoOffset (int statementIndex)
    {
        return 42 + 64 * (statementIndex);
    }



    public DBDataFormat getParameterMarkerFormat (int statementIndex)
    throws DBDataStreamException
    {
        int offset = BinaryConverter.byteArrayToInt (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 56);
        int length = BinaryConverter.byteArrayToInt (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 60);
        if ((length != 6) && (length != 0)) {                                       // @C0C
            DBDataFormat parameterMarkerFormat = new DBSQLDADataFormat(jobCCSID_);  // @D1C
            parameterMarkerFormat.overlay (data_, (offset_ - 6) + offset);
            return parameterMarkerFormat;
        }
        else
            return null;
    }



    public int getStatementCount ()
    throws DBDataStreamException
    {
        return BinaryConverter.byteArrayToShort (data_, offset_ + 24);
    }



    public String getStatementName (int statementIndex, ConverterImplRemote converter)
    throws DBDataStreamException
    {
        return converter.byteArrayToString (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 3, 18);
    }



    public int getStatementNeedsDefaultCollection (int statementIndex)
    throws DBDataStreamException
    {
        return data_[offset_ + getPackageEntryInfoOffset (statementIndex)];
    }



    public String getStatementText (int statementIndex, ConverterImplRemote converter)
    throws DBDataStreamException
    {
        int offset = BinaryConverter.byteArrayToInt (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 48);
        int length = BinaryConverter.byteArrayToInt (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 52);
        return converter.byteArrayToString (data_, (offset_ - 6) + offset, length);
    }



    public int getStatementTextLength (int statementIndex)
    throws DBDataStreamException
    {
        return BinaryConverter.byteArrayToInt (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 52);
    }



    public int getStatementType (int statementIndex)
    throws DBDataStreamException
    {
        return BinaryConverter.byteArrayToShort (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 1);
    }



    public int getTotalLength ()
    throws DBDataStreamException
    {
        return BinaryConverter.byteArrayToInt (data_, offset_);
    }



}

