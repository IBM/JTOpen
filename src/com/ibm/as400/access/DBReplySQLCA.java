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



    // Returns the SQLState
    // It needs to run thru EbcdicToAscii since it is a string
   final public String getSQLState (ConverterImplRemote converter) throws DBDataStreamException
   {
        if (length_ <= 6)
            return null;

        return converter.byteArrayToString (data_, offset_ + 131, 5);
   }




}	// End of DBReplySQLCA class


