///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

    ChangePasswordReq(byte[] userID, byte[] encryptedPw, byte[] oldPassword, byte[] newPassword)
    {
        super();

        int dsLength = 63 + oldPassword.length + newPassword.length;

        data_ = new byte[dsLength];

        setLength(dsLength);
        // setHeaderID(0x0000);
        setServerID(0xE009);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(1);
        setReqRepID(0x7005);

        // Password's always encrypted.
        data_[20] = 1;

        // Set user ID.
        //   LL
        set32bit(16, 21);
        //   CP
        set16bit(0x1104, 25);
        //   EBCDIC user ID.
        System.arraycopy(userID, 0, data_, 27, 10);

        // Set password.
        //   LL
        set32bit(14, 37);
        //   CP
        set16bit(0x1105, 41);
        //   Password data.
        System.arraycopy(encryptedPw, 0, data_, 43, 8);

        // Set Protected old password.
        //   LL
        set32bit(6 + oldPassword.length, 51);
        //   CP
        set16bit(0x110C, 55);
        //   Old password data.
        System.arraycopy(oldPassword, 0, data_, 57, oldPassword.length);

        // Set protected new password.
        //   LL
        set32bit(6 + newPassword.length, 57 + oldPassword.length);
        //   CP
        set16bit(0x110D, 61 + oldPassword.length);
        //   New password data.
        System.arraycopy(newPassword, 0, data_, 63 + oldPassword.length, newPassword.length);
    }

    void write(OutputStream out) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Sending change password request...");
        super.write(out);
    }
}
