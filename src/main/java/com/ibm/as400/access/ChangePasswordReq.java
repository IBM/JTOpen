///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ChangePasswordReq.java
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

class ChangePasswordReq extends ClientAccessDataStream
{
    ChangePasswordReq(byte[] userID, byte[] encryptedPw, byte[] oldPassword, int oldPasswordLength, 
            byte[] newPassword, int newPasswordLength, int serverLevel, byte[] addAuthFactor)
    {
        super(new byte[(encryptedPw.length == 64 ? 107 : 63) 
                       + oldPassword.length + newPassword.length
                       + (encryptedPw.length == 8 ? 0 : 42) 
                       + (serverLevel < 5 ? 0 : 7) 
                       + (serverLevel >= 18 && null != addAuthFactor && 0 < addAuthFactor.length ? 10 + addAuthFactor.length :0)]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(1);
        setReqRepID(0x7005);

        // Password's always encrypted.
        // 0X07 QPWDLVL4, 0X03 SHA-1 Encrypted.
        data_[20] = (encryptedPw.length == 8) ? (byte) 0x01 : ((encryptedPw.length == 20) ? (byte) 0x03 : (byte) 0x07);

        int offset = 21;

        // Set user ID.
        // LL
        set32bit(16, offset);
        // CP
        set16bit(0x1104, offset + 4);
        // EBCDIC user ID.
        System.arraycopy(userID, 0, data_, offset + 6, 10);

        offset += 16;
        
        // Set password.
        // LL
        set32bit(6 + encryptedPw.length, offset);
        // CP
        set16bit(0x1105, offset + 4);
        // Password data.
        System.arraycopy(encryptedPw, 0, data_, offset + 6, encryptedPw.length);

        offset += 6 + encryptedPw.length;
        
        // Set protected old password.
        // LL
        set32bit(6 + oldPassword.length, offset);
        // CP
        set16bit(0x110C, offset + 4);
        // Old password data.
        System.arraycopy(oldPassword, 0, data_, offset + 6, oldPassword.length);

        offset += 6 + oldPassword.length;

        // Set protected new password.
        // LL
        set32bit(6 + newPassword.length, offset);
        // CP
        set16bit(0x110D, offset + 4);
        // New password data.
        System.arraycopy(newPassword, 0, data_, offset + 6, newPassword.length);
        
        offset += 6 + newPassword.length;


        if (encryptedPw.length != 8) // If we're using SHA-1 passwords.
        {
            // Set protected old password length.
            // LL
            set32bit(10, offset);
            // CP
            set16bit(0x111C, offset + 4);
            // Old password length.
            set32bit(oldPasswordLength, offset + 6);
            
            offset += 10;

            // Set protected new password length.
            // LL
            set32bit(10, offset);
            // CP
            set16bit(0x111D, offset + 4);
            // New password length.
            set32bit(newPasswordLength, offset + 6);
            
            offset += 10;

            // Set protected password CCSID.
            // LL
            set32bit(10, offset);
            // CP
            set16bit(0x111E, offset + 4);
            // CCSID.
            set32bit(13488, offset + 6);
            
            offset += 10;
        }

        if (serverLevel >= 5)
        {            
            // Set return error messages.
            // LL
            set32bit(7, offset);
            // CP
            set16bit(0x1128, offset + 4);
            // Data.
            data_[offset + 6] = 0x01;
            
            offset += 7;

            if (serverLevel >= 18 && null != addAuthFactor && 0 < addAuthFactor.length)
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
        }
    }

    @Override
    void write(OutputStream out) throws IOException {
        if (Trace.traceOn_)
            Trace.log(Trace.DIAGNOSTIC, "Sending change password request...");
        super.write(out);
    }
}
