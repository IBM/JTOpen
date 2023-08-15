///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SignonInfoReq.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

class SignonInfoReq extends ClientAccessDataStream
{
    SignonInfoReq(byte[] userIDbytes, byte[] authenticationBytes, int byteType,  int serverLevel, char[] additionalAuthenticationFactor)
    {
        super(new byte[37 + authenticationBytes.length + (userIDbytes == null ? 0 : 16) + (serverLevel < 5 ? 0 : 7) + (serverLevel >= 18 && null != additionalAuthenticationFactor && 0 < additionalAuthenticationFactor.length ? 10 + additionalAuthenticationFactor.length :0)]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(0x0001);
        setReqRepID(0x7004);

        // Password's always encrypted.
        //@AF2A Start 
        if (byteType == AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN) 
        	data_[20] = (byte)0x06;
        else 
        	data_[20] = (byte)0x02;
        
        if (byteType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
        	data_[20] = (byte)0x05;
        
        if (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD) {
        	if (authenticationBytes.length == 8) {
        		data_[20] = (byte)0x01;
        	} else if (authenticationBytes.length == 20) {
        		data_[20] = (byte)0x03;
        	} else {
        		data_[20] = (byte)0x07;
        	}
        }
        //data_[20] = (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD) ? (authenticationBytes.length == 8) ? (byte)0x01 : (byte)0x03 : (byteType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN) ? (byte)0x05 : (byteType == AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN) ? (byte)0x06 : (byte)0x02;
        //@AF2A End
        
        // Client CCSID.
        //   LL
        set32bit(10, 21);
        //   CP
        set16bit(0x1113, 25);
        //   CCSID
        set32bit(1200, 27);    // Client CCSID.

        // Set password or authentication token.
        //   LL
        set32bit(6 + authenticationBytes.length, 31);
        //   CP
        if (byteType == AS400.AUTHENTICATION_SCHEME_PASSWORD)
        {
            set16bit(0x1105, 35);
        }
        else
        {
            set16bit(0x1115, 35);
        }
        //   Data.
        System.arraycopy(authenticationBytes, 0, data_, 37, authenticationBytes.length);

        if (userIDbytes != null)
        {
            // Set user ID info.
            //   LL
            set32bit(16, 37 + authenticationBytes.length);
            //   CP
            set16bit(0x1104, 41 + authenticationBytes.length);
            //   EBCDIC user ID.
            System.arraycopy(userIDbytes, 0, data_, 43 + authenticationBytes.length, 10);
        }

        if (serverLevel >= 5)
        {
            int offset = 37 + authenticationBytes.length + (userIDbytes == null ? 0 : 16);
            // Set return error messages.
            //   LL
            set32bit(7, offset);
            offset += 4;
            //   CP
            set16bit(0x1128, offset);
            offset += 2;
            //   Data.
            data_[offset] = 0x01;
            offset += 1;
                    
            if (serverLevel >= 18 && null != additionalAuthenticationFactor
                    && 0 < additionalAuthenticationFactor.length) {
                try {
                    int ccsid = 37;
                    CharConverter c = new CharConverter(ccsid);
                    byte[] aafBytes = c.stringToByteArray(new String(additionalAuthenticationFactor));

                    //LL
                    set32bit(aafBytes.length + 4 + 2 + 4, offset);
                    offset += 4;
                    // CP
                    set16bit(0x112F, offset);
                    offset += 2;
                    // CCSID
                    set32bit(ccsid, offset);
                    offset += 4;
                    // data 
                    System.arraycopy(aafBytes, 0, data_, offset, aafBytes.length);
                } catch (UnsupportedEncodingException e) {
                    if (Trace.traceOn_)
                        Trace.log(Trace.DIAGNOSTIC, e);
                }
            }
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending retrieve signon information request...");
        super.write(out);
    }

    public void clear() {
       CredentialVault.clearArray(data_); 
    }
}
