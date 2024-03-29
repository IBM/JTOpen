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
import java.io.UnsupportedEncodingException;

class ChangePasswordReq extends ClientAccessDataStream {
    ChangePasswordReq(byte[] userID, byte[] encryptedPw, byte[] oldPassword, int oldPasswordLength, byte[] newPassword,
            int newPasswordLength, int serverLevel, char[] additionalAuthenticationFactor) {
        super(new byte[(encryptedPw.length == 64 ? 107 : 63) + oldPassword.length + newPassword.length
                + (encryptedPw.length == 8 ? 0 : 42) + (serverLevel < 5 ? 0 : 7) + (serverLevel >= 18 && null != additionalAuthenticationFactor && 0 < additionalAuthenticationFactor.length ? 10 + additionalAuthenticationFactor.length :0)]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(1);
        setReqRepID(0x7005);

        // Password's always encrypted.
        // 0X07 QPWDLVL4, 0X03 SHA-1 Encrypted.
        data_[20] = (encryptedPw.length == 8) ? (byte) 0x01 : ((encryptedPw.length == 20) ? (byte) 0x03 : (byte) 0x07); // @AF2C

        // Set user ID.
        // LL
        set32bit(16, 21);
        // CP
        set16bit(0x1104, 25);
        // EBCDIC user ID.
        System.arraycopy(userID, 0, data_, 27, 10);

        // Set password.
        // LL
        set32bit(6 + encryptedPw.length, 37);
        // CP
        set16bit(0x1105, 41);
        // Password data.
        System.arraycopy(encryptedPw, 0, data_, 43, encryptedPw.length);

        // Set protected old password.
        // LL
        set32bit(6 + oldPassword.length, 43 + encryptedPw.length);
        // CP
        set16bit(0x110C, 47 + encryptedPw.length);
        // Old password data.
        System.arraycopy(oldPassword, 0, data_, 49 + encryptedPw.length, oldPassword.length);

        // Set protected new password.
        // LL
        set32bit(6 + newPassword.length, 49 + encryptedPw.length + oldPassword.length);
        // CP
        set16bit(0x110D, 53 + encryptedPw.length + oldPassword.length);
        // New password data.
        System.arraycopy(newPassword, 0, data_, 55 + encryptedPw.length + oldPassword.length, newPassword.length);

        if (encryptedPw.length != 8) // If we're using SHA-1 passwords.
        {
            // Set protected old password length.
            // LL
            set32bit(10, 55 + encryptedPw.length + oldPassword.length + newPassword.length);
            // CP
            set16bit(0x111C, 59 + encryptedPw.length + oldPassword.length + newPassword.length);
            // Old password length.
            set32bit(oldPasswordLength, 61 + encryptedPw.length + oldPassword.length + newPassword.length);

            // Set protected new password length.
            // LL
            set32bit(10, 65 + encryptedPw.length + oldPassword.length + newPassword.length);
            // CP
            set16bit(0x111D, 69 + encryptedPw.length + oldPassword.length + newPassword.length);
            // New password length.
            set32bit(newPasswordLength, 71 + encryptedPw.length + oldPassword.length + newPassword.length);

            // Set protected password CCSID.
            // LL
            set32bit(10, 75 + encryptedPw.length + oldPassword.length + newPassword.length);
            // CP
            set16bit(0x111E, 79 + encryptedPw.length + oldPassword.length + newPassword.length);
            // CCSID.
            set32bit(13488, 81 + encryptedPw.length + oldPassword.length + newPassword.length);
        }

        if (serverLevel >= 5) {
            int offset = (encryptedPw.length == 64 ? 107 : 63) + oldPassword.length + newPassword.length
                    + (encryptedPw.length == 8 ? 0 : 42);
            // Set return error messages.
            // LL
            set32bit(7, offset);
            // CP
            set16bit(0x1128, offset + 4);
            // Data.
            data_[offset + 6] = 0x01;

            if (serverLevel >= 18 && null != additionalAuthenticationFactor
                    && 0 < additionalAuthenticationFactor.length) {
                try {
                    int ccsid = 37;
                    CharConverter c = new CharConverter(ccsid);
                    byte[] aafBytes = c.stringToByteArray(new String(additionalAuthenticationFactor));

                    // LL
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

    void write(OutputStream out) throws IOException {
        if (Trace.traceOn_)
            Trace.log(Trace.DIAGNOSTIC, "Sending change password request...");
        super.write(out);
    }
}
