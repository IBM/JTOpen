///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  NativeException.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

class NativeException extends Exception
{
    static final long serialVersionUID = 4L;
    byte[] data = null;
    int errno_ = 0;

    NativeException(byte[] data)
    {
        this.data = data;
    }

    NativeException(int errno, byte[] data)
    {
        errno_ = errno;
        this.data = data;
    }
    
    public String toString() { 
      StringBuffer sb = new StringBuffer("com.ibm.as400.access.NativeException:");
      if (errno_ != 0) sb.append(" errno="+errno_);
      if (data != null) { 
        if (data.length == 4) { 
          int rc = (data[0] << 24) + (data[1] << 16) + (data[2] << 8) + data[3];
          sb.append(" rc="+rc); 
        } else {
          if (isAscii(data)) { 
            String asciiString = "";
            try {
              asciiString = new String(data,"UTF-8");
            } catch (UnsupportedEncodingException e) {
            } 
             sb.append("string data="+asciiString); 
          } else {
            sb.append("hex data=");
            for (int i = 0; i < data.length; i++) { 
              byte b = data[i]; 
              String digits = Integer.toHexString(b);
              if (b < 0x10) sb.append("0");
              sb.append(digits); 
            }
          }
        }
      }
      return sb.toString(); 
    }

    private boolean isAscii(byte[] thisData) {
      for (int i = 0; i < thisData.length; i++) {
        if (thisData[i] > 0x7F || thisData[i] < 0x20 ) return false; 
      }
      return true;
    }
}
