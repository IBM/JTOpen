///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBReplySQLCA.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
import java.math.BigDecimal;   //@F3A




/**
   Provides access to the SQLCA portion of the reply
   data stream.
**/
class DBReplySQLCA
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



    private byte[]  data_;
    private int     offset_;
    private int     length_;
    // Add 96 to offset_ for Errd1, 100 to offset_ for Errd2, etc.
    private static final int[]   locationFromOffset_ = {96, 100, 104, 108, 112};  //@F1A @F4C



    public DBReplySQLCA (byte[] data,
                         int offset,
                         int length)
    {
        data_ = data;
        offset_ = offset;
        length_ = length;    
    }



    //@F1A Combined getErrd1, getErrd2, getErrd3, getErrd4 methods
    final public int getErrd (int requestedErrd) throws DBDataStreamException
    {
        if (length_ <= 6)
            return 0;

        return BinaryConverter.byteArrayToInt (data_, 
                                               offset_ + locationFromOffset_[requestedErrd-1]);
    }



    // @E4A
    //@F1D final public int getErrd1 () throws DBDataStreamException           // @E4A
    //@F1D {                                                                   // @E4A
    //@F1D     if (length_ <= 6)                                               // @E4A
    //@F1D         return 0;                                                   // @E4A
    //@F1D                                                                     // @E4A
    //@F1D     return BinaryConverter.byteArrayToInt (data_, offset_ + 96);    // @E4A
    //@F1D }                                                                   // @E4A



    //@F1D final public int getErrd2 () throws DBDataStreamException
    //@F1D {
    //@F1D     if (length_ <= 6)                                               // @D1A
    //@F1D         return 0;                                                   // @D1A
    //@F1D 
    //@F1D     return BinaryConverter.byteArrayToInt (data_, offset_ + 100);
    //@F1D }



    //@F1D final public int getErrd3 () throws DBDataStreamException
    //@F1D {
    //@F1D     if (length_ <= 6)                                               // @D1A
    //@F1D         return 0;                                                   // @D1A
    //@F1D 
    //@F1D     return BinaryConverter.byteArrayToInt (data_, offset_ + 104);
    //@F1D }



    //@F1D final public int getErrd4 () throws DBDataStreamException           // @E6A
    //@F1D {                                                                   // @E6A
    //@F1D     if (length_ <= 6)                                               // @E6A
    //@F1D         return 0;                                                   // @E6A
    //@F1D                                                                     // @E6A
    //@F1D     return BinaryConverter.byteArrayToInt (data_, offset_ + 108);   // @E6A
    //@F1D }                                                                   // @E6A



    // @E3A
    final public String getErrmc(ConvTable converter) throws DBDataStreamException  // @E3A @P0C
    {                                                                          // @E3A
        if (length_ <= 6)                                                      // @E3A
            return "";                                                         // @E3A
        short errml = BinaryConverter.byteArrayToShort(data_, offset_ + 16);   // @E3A
        return converter.byteArrayToString(data_, offset_ + 18, errml);        // @E3A
    }                                                                          // @E3A



    // @E5A
    final public String getErrmc(int substitutionVariable,                     // @E5A
                                 ConvTable converter) throws DBDataStreamException  // @E5A @P0C
    {                                                                          // @E5A
        if (length_ <= 6)                                                      // @E5A
            return "";                                                         // @E5A
        short errml = BinaryConverter.byteArrayToShort(data_, offset_ + 16);   // @E5A
        int currentVariable = 1;                                               // @E5A
        int i = offset_ + 18;                                                  // @E5A
        int j = 0;                                                             // @E5A
        short currentLength;                                                   // @E5A
        while ((currentVariable < substitutionVariable) && (j < errml)) {       // @E5A
            ++currentVariable;                                                 // @E5A
            currentLength = (short)(BinaryConverter.byteArrayToShort(data_, i) + 2); // @E5A
            i += currentLength;                                                // @E5A
            j += currentLength;                                                // @E5A
        }                                                                      // @E5A
        currentLength = BinaryConverter.byteArrayToShort(data_, i);            // @E5A
        return converter.byteArrayToString(data_, i+2, currentLength);         // @E5A
    }                                                                          // @E5A



    // @E2A
    final public boolean getEyecatcherBit54() throws DBDataStreamException      // @E2A
    {                                                                           // @E2A
        if (length_ <= 6)                                                       // @E2A
            return false;                                                       // @E2A
        // It is actually 6 of Byte 6.                                          // @E2A
        byte b = data_[offset_ + 6];                                            // @E2A
        return((b & (byte)0x02) > 0);                                          // @E2A
    }                                                                           // @E2A


    // @F2A Added method for auto-generated key support.
    //
    // ONLY call this method after checking if sqlState != 0 because otherwise the error 
    // message will be in this space.
    //
    // Note:  Although we check the length above in getErrmc, we do not have in this method
    // because the auto-generated key will ALWAYS be in bytes 55 through 70 even if the length
    // in SQLERRML reports a length other than 70.  This is bytes 72 through 87 from our offset.
    //
    // We shouldn't need a "throws DBDataStreamException" because we are not handling 
    // SIGNAL 443 cases here.
    final public BigDecimal getGeneratedKey() //@P0C @F3C
    {  
        AS400PackedDecimal typeConverter = new AS400PackedDecimal (30, 0);      //@F3A
        try                                                                     //@F3A
        {                                                                       //@F3A
            return((BigDecimal) typeConverter.toObject (data_, offset_ + 72));  //@F3A
        }                                                                       //@F3A
        catch (NumberFormatException nfe)                                       //@F3A
        {                                                                       //@F3A
            //If we got a bad number back from the database, don't return it    //@F3A
            //to the user                                                       //@F3A
            return null;                                                        //@F3A
        }                                                                       //@F3A
        //@F3D return converter.byteArrayToString(data_, offset_ + 72, 16);        
    }      


    // Returns the SQLState
    // It needs to run thru EbcdicToAscii since it is a string
    final public String getSQLState (ConvTable converter) throws DBDataStreamException //@P0C
    {
        if (length_ <= 6)
            return null;

        return converter.byteArrayToString (data_, offset_ + 131, 5);
    }



    final public byte getWarn5() throws DBDataStreamException            // @E1A
    {                                                                    // @E1A
        if (length_ <= 6)                                                // @E1A
            return 0;                                                    // @E1A
        return data_[offset_ + 124];                                     // @E1A
    }                                                                    // @E1A



}   // End of DBReplySQLCA class


