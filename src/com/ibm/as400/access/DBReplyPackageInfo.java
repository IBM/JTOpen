///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBReplyPackageInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
import java.io.UnsupportedEncodingException;  //@F1A



/**
   Provides access to the package info portion of the reply data stream.
**/
class DBReplyPackageInfo {
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



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



    //---------------------------------------------------------
    // The following structure is used to determine the amount
    // needed to be added to the offset to get the appropriate
    // data.
    //
    //  offset+  field              length
    //
    //     0     total length       4  bytes
    //     4     ccsid              2  bytes
    //     6     default collection 18 bytes
    //    24     statement count    2  bytes
    //    26     reserved   		16 bytes
    //
    //    42     package entry info (repeated for each field)
    //           0   statement needs default collection      1  byte
    //           1   statement type                          2  bytes
    //           3   statement name                          18 bytes
    //           21  reserved                                19 bytes
    //           40  format offset                           4  bytes
    //           44  format length                           4  bytes
    //           48  text offset                             4  bytes
    //           52  text length                             4  bytes
    //           56  parameter marker format offset          4  bytes
    //           60  parameter marker format length          4  bytes
    //
    //           Note: these offsets are in terms of the start
    //                 of the LL for this parameter (i.e.
    //                 actually count from (offset_ - 6).
    //
    //     x     package statement text and formats
    //
    //---------------------------------------------------------



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



    public String getDefaultCollection (ConvTable converter) //@P0C
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



    public String getStatementName (int statementIndex, ConvTable converter) //@P0C
    throws DBDataStreamException
    {
        try                                           //@F1A
        {                                             
            //@F1A Should use job CCSID, not unicode, to interpret statement name from datastream.
            return ConvTable.getTable(jobCCSID_, null).byteArrayToString (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 3, 18);  //@F1A
            //@F1D return converter.byteArrayToString (data_, offset_ + getPackageEntryInfoOffset (statementIndex) + 3, 18);
            //@F1A Changed code in JDPackageManager to now pass in null for converter.
        }                                             
        catch (UnsupportedEncodingException uee)      //@F1A
        {    
            //@F1A Error shouldn't happen, but just in case, return "" so that
            //@F1A code in AS400JDBCStatement which checks the length of what is 
            //@F1A returned from here to see if it's 0 will correctly detect that 
            //@F1A we are unable to use this name and will prepare the statement
            //@F1A again on the server instead of taking a NullPointerException.
            return "";                                //@F1A 
        }                                             
    }



    public int getStatementNeedsDefaultCollection (int statementIndex)
    throws DBDataStreamException
    {
        return data_[offset_ + getPackageEntryInfoOffset (statementIndex)];
    }



    public String getStatementText (int statementIndex, ConvTable converter) //@P0C
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

