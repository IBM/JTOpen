///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBReplySQLCA.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;




/**
   Provides access to the SQLCA portion of the reply
   data stream.
**/
class DBReplySQLCA
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    private byte[]  data_;
    private int     offset_;
    private int     length_;



	public DBReplySQLCA (byte[] data,
					   int offset,
					   int length)
	{
	    data_ = data;
	    offset_ = offset;
	    length_ = length;
	}



    // @E4A
    final public int getErrd1 () throws DBDataStreamException           // @E4A
    {                                                                   // @E4A
        if (length_ <= 6)                                               // @E4A
            return 0;                                                   // @E4A
                                                                        // @E4A
        return BinaryConverter.byteArrayToInt (data_, offset_ + 96);    // @E4A
    }                                                                   // @E4A



    final public int getErrd2 () throws DBDataStreamException
    {
        if (length_ <= 6)                                               // @D1A
            return 0;                                                   // @D1A

        return BinaryConverter.byteArrayToInt (data_, offset_ + 100);
    }



    final public int getErrd3 () throws DBDataStreamException
    {
        if (length_ <= 6)                                               // @D1A
            return 0;                                                   // @D1A

        return BinaryConverter.byteArrayToInt (data_, offset_ + 104);
    }



    final public int getErrd4 () throws DBDataStreamException           // @E6A
    {                                                                   // @E6A
        if (length_ <= 6)                                               // @E6A
            return 0;                                                   // @E6A
                                                                        // @E6A
        return BinaryConverter.byteArrayToInt (data_, offset_ + 108);   // @E6A
    }                                                                   // @E6A



    // @E3A
    final public String getErrmc(ConverterImplRemote converter) throws DBDataStreamException  // @E3A
    {                                                                          // @E3A
        if (length_ <= 6)                                                      // @E3A
            return "";                                                         // @E3A
        short errml = BinaryConverter.byteArrayToShort(data_, offset_ + 16);   // @E3A
        return converter.byteArrayToString(data_, offset_ + 18, errml);        // @E3A
    }                                                                          // @E3A



    // @E5A
    final public String getErrmc(int substitutionVariable,                     // @E5A
                                 ConverterImplRemote converter) throws DBDataStreamException  // @E5A
    {                                                                          // @E5A
        if (length_ <= 6)                                                      // @E5A
            return "";                                                         // @E5A
        short errml = BinaryConverter.byteArrayToShort(data_, offset_ + 16);   // @E5A
        int currentVariable = 1;                                               // @E5A
        int i = offset_ + 18;                                                  // @E5A
        int j = 0;                                                             // @E5A
        short currentLength;                                                   // @E5A
        while((currentVariable < substitutionVariable) && (j < errml)) {       // @E5A
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
        return ((b & (byte)0x02) > 0);                                          // @E2A
    }                                                                           // @E2A


    // Returns the SQLState
    // It needs to run thru EbcdicToAscii since it is a string
   final public String getSQLState (ConverterImplRemote converter) throws DBDataStreamException
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



}	// End of DBReplySQLCA class


