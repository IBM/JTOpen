///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DDMSECCHKRequestDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class DDMSECCHKRequestDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static String getCopyright()
    {
	return Copyright.copyright;
    }

    DDMSECCHKRequestDataStream(byte[] userIDbytes, byte[] passwordBytes, boolean useEncryptedPassword)
    {
	super();
	if (useEncryptedPassword)
	{
	    data_ = new byte[42];
	}
	else
	{
	    data_ = new byte[44];
	}
	setLength(data_.length);

        // Initialize the header:
        //  Don't continue on error, not chained, GDS id = D0, type = RQSDSS,
        //  no same request correlation.
	setContinueOnError(false);
	setIsChained(false);
	setGDSId((byte)0xD0);
	setHasSameRequestCorrelation(false);
	setType(1);

	if (useEncryptedPassword)
	{
	    set16bit(36, 6);  // Set LL for SECCHK term
	}
	else
	{
	    set16bit(38, 6);  // Set LL for SECCHK term
	}
	set16bit(DDMTerm.SECCHK, 8);  // Set code point for SECCHK
	set16bit(6, 10);              // Set LL for SECMEC term
	set16bit(DDMTerm.SECMEC, 12); // Set code point for SECMEC
	if (useEncryptedPassword)
	{
	    set16bit(6, 14);              // Set value for SECMEC term
	}
	else
	{
	    set16bit(3, 14);              // Set value for SECMEC term
	}

	set16bit(14, 16);  // set LL for USRID term
	set16bit(DDMTerm.USRID, 18);  // Set code point for USRID
	// Set the userid
	System.arraycopy(userIDbytes, 0, data_, 20, 10);

	if (useEncryptedPassword)
	{
	    set16bit(12, 30);  // set LL for PASSWORD term
	}
	else
	{
	    set16bit(14, 30);  // set LL for PASSWORD term
	}
	set16bit(DDMTerm.PASSWORD, 32);  // Set code point for PASSWORD
	// Set the password
	if (useEncryptedPassword)
	{
	    System.arraycopy(passwordBytes, 0, data_, 34, 8);
	}
	else
	{
	    System.arraycopy(passwordBytes, 0, data_, 34, 10);
	}
    }
}
