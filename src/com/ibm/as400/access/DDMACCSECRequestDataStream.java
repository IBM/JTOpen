///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DDMACCSECRequestDataStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;

import javax.crypto.interfaces.DHPublicKey;

class DDMACCSECRequestDataStream extends DDMDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

      // keyPair parameter needed to AS400.AUTHENTICATION_SCHEME_DDM_USERIDWD @U4C
    DDMACCSECRequestDataStream(boolean useStrongEncryption, int byteType, byte[] iasp, KeyPair keyPair, boolean useAES)
    {
        
        super(new byte[(byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD  || byteType == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD)? 
                        ((byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD) ? 28 : ((useAES)? 90 : 52 )) : 
                        16]); /*@U4C*/ 

        // Initialize the header:  Don't continue on error, not chained, GDS id = D0, type = RQSDSS, no same request correlation.
        setGDSId((byte)0xD0);
        // setIsChained(false);
        // setContinueOnError(false);
        // setHasSameRequestCorrelation(false);
        setType(1);
        // Set total length remaining after header.
        switch (byteType) {
          case AS400.AUTHENTICATION_SCHEME_PASSWORD:
            set16bit(22, 6); break;
          case AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD : /*@U4A*/ 
            if (useAES) { 
              set16bit(46+32+6, 6); 
            } else { 
              set16bit(46, 6); 
            } 
              break;
          default:             
            set16bit(10, 6);
        }

        set16bit(DDMTerm.ACCSEC, 8); // Set ACCSEC code point.
        set16bit(6, 10); // Set SECMEC length.
        set16bit(DDMTerm.SECMEC, 12); // Set SECMEC code point.
        // Set value of SECMEC parm.
        switch (byteType)
        {
            case AS400.AUTHENTICATION_SCHEME_GSS_TOKEN:
                set16bit(11, 14);
                break;
            case AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN:
                set16bit(16, 14);
                break;
            case AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD:  /* @U4A*/ 
              set16bit(DDMTerm.EUSRIDPWD, 14); 
              // Set the security token
              if (useAES) {
                set16bit(0x44, 16); // Set length of this+remaining SECTKN bytes.
              } else {
                set16bit(0x24, 16); // Set length of this+remaining SECTKN bytes.
              }
              set16bit(DDMTerm.SECTKN, 18); // Set SECTKN code point.
              // Set the token from the public key
              int pubKeyOffset = 0;
              int keysize = 32; 
              if (useAES) {
                keysize = 64; 
              }
              byte[] publicKey = ((DHPublicKey)keyPair.getPublic()).getY().toByteArray();
              // Sometimes the public key has a leading 00 or in a rare case could only 31 bytes. 
              pubKeyOffset = publicKey.length - keysize; 
              Trace.log(Trace.DIAGNOSTIC, "DMMACCSECRequestDataStream  clientPublicKey:", publicKey); 
              for (int i = 0; i < keysize; i++) { 
                if ((pubKeyOffset + i) < 0) { 
                   data_[20+i] = 0; 
                } else { 
                   data_[20+i] = publicKey[pubKeyOffset + i];
                }
              }
              if (useAES) {
                  set16bit(6, 84);
                  set16bit(DDMTerm.ENCALC,86);
                  set16bit(DDMTerm.AES, 88); 
              }
              break; 
            default:
              if (useStrongEncryption)
                {
                    // Use a value of SECMEC=8 for encrypted password.
                    set16bit(8, 14);
                }
                else
                {
                    // Use a value of SECMEC=6 for a substituted password.
                    set16bit(6, 14);
                }

                // Need to send a client seed as the security token.
                // The SECTKN is 8 bytes and the PASSWORD is 20 bytes.
                set16bit(12, 16); // Set length of this+remaining SECTKN bytes.
                set16bit(DDMTerm.SECTKN, 18); // Set SECTKN code point.

                // This code taken from AS400XChgRandSeedDS constructor.  Generate the client seed.  We generate a "random" seed using the current time in milliseconds.  This seed will be used to encrypt the password.
                long t = System.currentTimeMillis();

                // Performance: break into 2 ints first and avoid long temporaries.
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
        }        
    }

    byte[] getClientSeed()
    {
        byte[] seed = new byte[8];
        System.arraycopy(data_, 20, seed, 0, 8);
        return seed;
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending DDM ACCSEC request...");
        super.write(out);
    }
}
