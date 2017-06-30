///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DDMSECCHKRequestDataStream.java
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

class DDMSECCHKRequestDataStream extends DDMDataStream
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  DDMSECCHKRequestDataStream(byte[] userIDbytes, byte[] authenticationBytes,
      byte[] iasp, int byteType) {
    
   
    super(new byte[authenticationBytes.length + 
                   userIDbytes.length +
                   (iasp == null ? 0 : 22) + 
        ((byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD) ||
         (byteType == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD)
            ? 24 : 16)]);  /*@U4C*/ 

    // Initialize the header: Don't continue on error, not chained, GDS id = D0,
    // type = RQSDSS, no same request correlation.
    setGDSId((byte) 0xD0);
    if ((byteType != AS400.AUTHENTICATION_SCHEME_PASSWORD)
        && (byteType != AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD)) {  /*@U4C*/ 
      setLength(16);
      setIsChained(true);
      // setContinueOnError(false);
      setHasSameRequestCorrelation(true);
      setType(1);
      set16bit(10, 6); // Set total length remaining after header.
      set16bit(DDMTerm.SECCHK, 8); // Set code point for SECCHK.
      set16bit(6, 10); // Set LL for SECMEC term.
      set16bit(DDMTerm.SECMEC, 12); // Set code point for SECMEC.
      // Set value of SECMEC parm.
      if (byteType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN) {
        set16bit(11, 14);
      } else {
        set16bit(16, 14);
      }
      set16bit(authenticationBytes.length + 10, 16); // Set LL for SECCHK term.
      set16bit(0xD003, 18);

      set16bit(authenticationBytes.length + 4, 22); // Set LL for SECCHK term.
      set16bit(0x11DC, 24); // Set LL for SECMEC term.
      System.arraycopy(authenticationBytes, 0, data_, 26,
          authenticationBytes.length);
    } else {
      boolean useRDB = (iasp != null);
      // setIsChained(false);
      // setContinueOnError(false);
      // setHasSameRequestCorrelation(false);
      setType(1);
      set16bit(authenticationBytes.length + userIDbytes.length+ (useRDB ? 40 : 18), 6); // Set LL
                                                                    // for
                                                                    // SECCHK
                                                                    // term.  @U4C
      set16bit(DDMTerm.SECCHK, 8); // Set code point for SECCHK.
      set16bit(6, 10); // Set LL for SECMEC term.
      set16bit(DDMTerm.SECMEC, 12); // Set code point for SECMEC.
      if (byteType == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD) {
        set16bit(DDMTerm.EUSRIDPWD, 14); // Set value for SECMEC term. @U4A
      } else {
        if (authenticationBytes.length == 20) {
          set16bit(8, 14); // Set value for SECMEC term.
        } else {
          set16bit(DDMTerm.USRSBSPWD, 14); // Set value for SECMEC term.
                                           // USRSBSPWD - User ID with
                                           // Substitute Password
        }
      }
      if (byteType == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD) {  /*@U4A*/ 
        set16bit(userIDbytes.length + 4, 16); // Set LL for USRID term as SECTKN
        set16bit(DDMTerm.SECTKN, 18); // Set code point for USRID as SECTKN
        // Set the user ID.
        System.arraycopy(userIDbytes, 0, data_, 20, userIDbytes.length);

        set16bit(authenticationBytes.length + 4, 20 + userIDbytes.length); // Set LL for PASSWORD term.
        set16bit(DDMTerm.SECTKN, 22+userIDbytes.length); // Set code point for PASSWORD.
        // Set the password.
        System.arraycopy(authenticationBytes, 0, data_, 24+userIDbytes.length,
            authenticationBytes.length);
        
      } else {
        set16bit(14, 16); // Set LL for USRID term.
        set16bit(DDMTerm.USRID, 18); // Set code point for USRID.
        // Set the user ID.
        System.arraycopy(userIDbytes, 0, data_, 20, 10);

        set16bit(authenticationBytes.length + 4, 30); // Set LL for PASSWORD
                                                      // term.
        set16bit(DDMTerm.PASSWORD, 32); // Set code point for PASSWORD.
        // Set the password.
        System.arraycopy(authenticationBytes, 0, data_, 34,
            authenticationBytes.length);
      }
      if (useRDB) {
        if (Trace.traceOn_)
          Trace.log(Trace.DIAGNOSTIC,
              "Sending SECCHK request with RDB / IASP name.");
        int offset = 24 + authenticationBytes.length+userIDbytes.length;
        set16bit(22, offset);
        set16bit(DDMTerm.RDBNAM, offset + 2);
        System.arraycopy(iasp, 0, data_, offset + 4, iasp.length);
      }
    }
  }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending DDM SECCHK request...");
        super.write(out);
    }
}
