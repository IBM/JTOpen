///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ChangePasswordReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

class ChangePasswordReq extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    ChangePasswordReq(byte[] userID, byte[] encryptedPw, byte[] oldPassword, int oldPasswordLength, byte[] newPassword, int newPasswordLength)
    {
        super(new byte[(encryptedPw.length == 8) ? 63 + oldPassword.length + newPassword.length : 105 + oldPassword.length + newPassword.length]);

        setLength(data_.length);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(1);
        setReqRepID(0x7005);

        // Password's always encrypted.
        data_[20] = (encryptedPw.length == 8) ? (byte)0x01 : (byte)0x03;

        // Set user ID.
        //   LL
        set32bit(16, 21);
        //   CP
        set16bit(0x1104, 25);
        //   EBCDIC user ID.
        System.arraycopy(userID, 0, data_, 27, 10);

        // Set password.
        //   LL
        set32bit(6 + encryptedPw.length, 37);
        //   CP
        set16bit(0x1105, 41);
        //   Password data.
        System.arraycopy(encryptedPw, 0, data_, 43, encryptedPw.length);

        // Set protected old password.
        //   LL
        set32bit(6 + oldPassword.length, 43 + encryptedPw.length);
        //   CP
        set16bit(0x110C, 47 + encryptedPw.length);
        //   Old password data.
        System.arraycopy(oldPassword, 0, data_, 49 + encryptedPw.length, oldPassword.length);

        // Set protected new password.
        //   LL
        set32bit(6 + newPassword.length, 49 + encryptedPw.length + oldPassword.length);
        //   CP
        set16bit(0x110D, 53 + encryptedPw.length + oldPassword.length);
        //   New password data.
        System.arraycopy(newPassword, 0, data_, 55 + encryptedPw.length + oldPassword.length, newPassword.length);

        if (encryptedPw.length != 8)  // If we're using SHA-1 passwords.
        {
            // Set protected old password length.
            //   LL
            set32bit(10, 55 + encryptedPw.length + oldPassword.length + newPassword.length);
            //   CP
            set16bit(0x111C, 59 + encryptedPw.length + oldPassword.length + newPassword.length);
            //   Old password length.
            set32bit(oldPasswordLength, 61 + encryptedPw.length + oldPassword.length + newPassword.length);

            // Set protected new password length.
            //   LL
            set32bit(10, 65 + encryptedPw.length + oldPassword.length + newPassword.length);
            //   CP
            set16bit(0x111D, 69 + encryptedPw.length + oldPassword.length + newPassword.length);
            //   New password length.
            set32bit(newPasswordLength, 71 + encryptedPw.length + oldPassword.length + newPassword.length);

            // Set protected password CCSID.
            //   LL
            set32bit(10, 75 + encryptedPw.length + oldPassword.length + newPassword.length);
            //   CP
            set16bit(0x111E, 79 + encryptedPw.length + oldPassword.length + newPassword.length);
            //   CCSID.
            set32bit(13488, 81 + encryptedPw.length + oldPassword.length + newPassword.length);
        }
    }

    void write(OutputStream out) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Sending change password request...");
        super.write(out);
    }
}
