///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400GenAuthTknDS.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

// The AS400GenAuthTknDS class represents the data stream for the 'Generate authentication token' request.
class AS400GenAuthTknDS extends ClientAccessDataStream
{
    AS400GenAuthTknDS(byte[] userIDbytes, byte[] authenticationBytes, int authScheme, int profileTokenType, int profileTokenTimeout, int serverLevel,
            byte[] addAuthFactor, byte[] verificationID, byte[] clientIPAddr)
    {
    	super(new byte[45 + authenticationBytes.length 
    	               + ((userIDbytes == null || authScheme == 1|| authScheme ==2) ? 0:16)
    	               + (serverLevel < 5 ? 0 : 7)
                       + ((serverLevel >= 18 && null != addAuthFactor && 0 < addAuthFactor.length) ? addAuthFactor.length + 10: 0)
                       + ((serverLevel >= 18 && null != verificationID) ? verificationID.length + 10: 0)
                       + ((serverLevel >= 18 && null != clientIPAddr) ? clientIPAddr.length + 10: 0)
    	               ]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(0x0002);
        setReqRepID(0x7007);

        // Type of authentication bytes.
        if (authScheme == AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN) 
        	data_[20] = (byte)0x06;
        else 
        	data_[20] = (byte)0x02;
        
        if (authScheme == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
        	data_[20] = (byte)0x05;
        
        if (authScheme == AS400.AUTHENTICATION_SCHEME_PASSWORD)
        {
        	if (authenticationBytes.length == 8)
        		data_[20] = (byte)0x01;
        	else if (authenticationBytes.length == 20)
        		data_[20] = (byte)0x03;
        	else
        		data_[20] = (byte)0x07;
        }
        
        // Return type, 0x01 = profile token.
        data_[21] = 0x01;

        // Profile token type.
        set32bit(7, 22);
        set16bit(0x1116, 26);
        data_[28] = (byte)(0xF0 | profileTokenType);

        // Expiration interval.
        set32bit(10, 29);
        set16bit(0x1117, 33);
        set32bit(profileTokenTimeout, 35);

        // Set password or authentication token.
        //   LL
        set32bit(6 + authenticationBytes.length, 39);
        //   CP
        if (authScheme == 0)
            set16bit(0x1105, 43);
        else
            set16bit(0x1115, 43);
        
        //   Data.
        System.arraycopy(authenticationBytes, 0, data_, 45, authenticationBytes.length);
        
        int offset = 45 + authenticationBytes.length;

        if (userIDbytes != null && authScheme != 1 && authScheme != 2)
        {
            // Set user ID info.
            //   LL
            set32bit(16, offset);
            //   CP
            set16bit(0x1104, offset + 4);
            //   EBCDIC user ID.
            System.arraycopy(userIDbytes, 0, data_, offset + 6, 10);
            
            offset += 6 + 10;
        }
        
        
        if (serverLevel >= 5)
        {
            // Set return error messages.
            //   LL
            set32bit(7, offset);
            //   CP
            set16bit(0x1128, offset + 4);
            //   Data.
            data_[offset + 6] = 0x01;
            
            offset += 6 + 1;
        }
        
        if (serverLevel >= 18)
        {
            if (null != addAuthFactor && 0 < addAuthFactor.length)
            {
                // LL
                set32bit(addAuthFactor.length + 4 + 2 + 4, offset);
                // CP
                set16bit(0x112F, offset + 4);
                // CCSID
                set32bit(1208, offset + 6);
                // data
                System.arraycopy(addAuthFactor, 0, data_, offset + 10, addAuthFactor.length);
                
                offset += 10 + addAuthFactor.length;
            }
            
            if (null != verificationID && 0 < verificationID.length)
            {
                // LL
                set32bit(verificationID.length + 4 + 2 + 4, offset);
                // CP
                set16bit(0x1130, offset + 4);
                // CCSID
                set32bit(1208, offset + 6);
                // data
                System.arraycopy(verificationID, 0, data_, offset + 10, verificationID.length);
                
                offset += 10 + verificationID.length;
            }
            
            if (null != clientIPAddr && 0 < clientIPAddr.length)
            {
                // LL
                set32bit(clientIPAddr.length + 4 + 2 + 4, offset);
                // CP
                set16bit(0x1131, offset + 4);
                // CCSID
                set32bit(1208, offset + 6);
                // data
                System.arraycopy(clientIPAddr, 0, data_, offset + 10, clientIPAddr.length);
                
                offset += 10 + clientIPAddr.length;
            }
        }
    }

    @Override
    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending generate authentication token request...");
        super.write(out);
    }

    /* clear out sensitive information */ 
    public void clear() {
     CredentialVault.clearArray(data_);
    }
}
