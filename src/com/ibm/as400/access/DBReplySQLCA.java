///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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




    // Returns the copyright.
    private static String getCopyright()
    {
      return Copyright.copyright;
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



    // @E3A
    final public String getErrmc(ConverterImplRemote converter) throws DBDataStreamException  // @E3A
    {                                                                          // @E3A
        if (length_ <= 6)                                                      // @E3A
            return "";                                                         // @E3A
        short errml = BinaryConverter.byteArrayToShort(data_, offset_ + 16);   // @E3A
        return converter.byteArrayToString(data_, offset_ + 18, errml);        // @E3A
    }                                                                          // @E3A



    // Returns the SQLState
    // It needs to run thru EbcdicToAscii since it is a string
   final public String getSQLState (ConverterImplRemote converter) throws DBDataStreamException
   {
        if (length_ <= 6)
            return null;

        return converter.byteArrayToString (data_, offset_ + 131, 5);
   }




}	// End of DBReplySQLCA class


