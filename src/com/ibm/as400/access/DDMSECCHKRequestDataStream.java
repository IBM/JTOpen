///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

  DDMSECCHKRequestDataStream(byte[] userIDbytes, byte[] passwordBytes, boolean useEncryptedPassword)
  {
    super();
    
    boolean useStrongEncryption = useEncryptedPassword && (passwordBytes.length > 8); //@B0A
    
    if (useEncryptedPassword)
    {
      if (useStrongEncryption) //@B0A
      {
        data_ = new byte[54]; //@B0A
      }
      else //@B0A
      {
        data_ = new byte[42];
      }
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
      if (useStrongEncryption) //@B0A
      {
        set16bit(48, 6); //@B0A Set LL for SECCHK term
      }
      else //@B0A
      {
        set16bit(36, 6);  // Set LL for SECCHK term
      }
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
      if (useStrongEncryption) //@B0A
      {
        set16bit(8, 14); //@B0A Set value for SECMEC term
      }
      else //@B0A
      {
        set16bit(6, 14);              // Set value for SECMEC term
      }
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
      if (useStrongEncryption) //@B0A
      {
        set16bit(24, 30); //@B0A set LL for PASSWORD term
      }
      else //@B0A
      {
        set16bit(12, 30);  // set LL for PASSWORD term
      }
    }
    else
    {
      set16bit(14, 30);  // set LL for PASSWORD term
    }

    set16bit(DDMTerm.PASSWORD, 32);  // Set code point for PASSWORD

    // Set the password
    if (useEncryptedPassword)
    {
      if (useStrongEncryption) //@B0A
      {
        System.arraycopy(passwordBytes, 0, data_, 34, 20); //@B0A
      }
      else //@B0A
      {
        System.arraycopy(passwordBytes, 0, data_, 34, 8);
      }
    }
    else
    {
      System.arraycopy(passwordBytes, 0, data_, 34, 10);
    }
  }
}
