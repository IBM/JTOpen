///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMACCSECRequestDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class DDMACCSECRequestDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  DDMACCSECRequestDataStream(boolean useEncryptedPassword, boolean useStrongEncryption) //@B0C
  {
    super();
    if (useEncryptedPassword)
    {
//@B1C - The SECTKN is only 8 bytes. It's the PASSWORD that is 20 bytes.
      data_ = new byte[28];
    }
    else
    {
      data_ = new byte[16];
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
      set16bit(22, 6); // Set total length remaining after header
    }
    else
    {
      set16bit(10, 6); // Set total length remaining after header
    }

    set16bit(DDMTerm.ACCSEC, 8); // Set ACCSEC code point
    set16bit(6, 10); // Set SECMEC length
    set16bit(DDMTerm.SECMEC, 12); // Set SECMEC code point
    if (useEncryptedPassword)
    {
      //@B0A: Use a value of SECMEC=8 for encrypted password.
      if (useStrongEncryption) //@B0A
      {
        set16bit(8, 14); //@B0A Set value of SECMEC parm
      }
      else //@B0A
      {
        // Use a value of SECMEC=6 for a substituted password
        set16bit(6, 14); // Set value of SECMEC parm
      }
      
      // Need to send a client seed as the security token
//@B1C - The SECTKN is 8 bytes and the PASSWORD is 20 bytes.
      set16bit(12, 16); // Set length of this+remaining SECTKN bytes
      set16bit(DDMTerm.SECTKN, 18); // Set SECTKN code point

      // This code taken from AS400XChgRandSeedDS constructor.  Generate the client seed.  We generate a "random" seed using the current time in milliseconds.  This seed will be used to encrypt the password.
      long t = System.currentTimeMillis();

      // Performance: break into 2 ints first and avoid long temporaries
      int high = (int)(t >>> 32);
      int low = (int)t;

      data_[20] = (byte)(high >>> 24);
      data_[21] = (byte)(high >>> 16);
      data_[22] = (byte)(high >>> 8);
      data_[23] = (byte)high;

      data_[24] = (byte)(low >>> 24);
      data_[25] = (byte)(low >>> 16);
      data_[26] = (byte)(low >>> 8);
      data_[27] = (byte)low;
      
      //@B0: If we are using strong encryption, the SECTKN is 20 bytes, but the seed is still only 8.
      // So, we leave the rest of the bytes set to 0. This is just to appease the DDM server.
      //@B1: Not anymore. The DDM server changed to correctly accept an 8-byte SECTKN. It reports
      // that a bad token was sent if we still try to send a 20-byte token.
    }
    else
    {
      set16bit(3, 14); // Set value of SECMEC parm
    }
  }

  byte[] getClientSeed()
  {
    byte[] seed = new byte[8];
    System.arraycopy(data_, 20, seed, 0, 8);
    return seed;
  }
}
