///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: Util.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.workmgmt;

import com.ibm.jtopenlite.*;

final class Util
{
  private Util()
  {
  }

  static void readKeyData(final byte[] data, int numRead, final int key, final int lengthOfData, final boolean isBinary, JobKeyDataListener listener, final char[] c)
  {
    switch (lengthOfData)
    {
      case 1:
        String s1 = Conv.ebcdicByteArrayToString(data, numRead, 1, c);
        listener.newKeyData(key, s1, data, numRead);
        break;
      case 2:
        String s2 = Conv.ebcdicByteArrayToString(data, numRead, 2, c);
        listener.newKeyData(key, s2, data, numRead);
        break;
      case 4:
        if (isBinary)
        {
          int value = Conv.byteArrayToInt(data, numRead);
          listener.newKeyData(key, value);
        }
        else
        {
          String s4 = Conv.ebcdicByteArrayToString(data, numRead, 4, c);
          listener.newKeyData(key, s4, data, numRead);
        }
        break;
/*      case 8:
        if (isBinary)
        {
          long data = in.readLong();
          listener.newKeyData(key, data);
        }
        else
        {
          byte[] b = new byte[8];
          in.readFully(b);
          listener.newKeyData(key, new String(b, "Cp037"), b);
        }
        break;
*/
      case 10:
        String s10 = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
        listener.newKeyData(key, s10, data, numRead);
        break;
      case 20:
        String s20 = Conv.ebcdicByteArrayToString(data, numRead, 20, c);
        listener.newKeyData(key, s20, data, numRead);
        break;
      default:
        String s = (c.length >= lengthOfData) ? Conv.ebcdicByteArrayToString(data, numRead, lengthOfData, c) :
                                                Conv.ebcdicByteArrayToString(data, numRead, lengthOfData);
        listener.newKeyData(key, s, data, numRead);
        break;
    }
  }
}
