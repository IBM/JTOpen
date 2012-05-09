///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  Util.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;

class Util
{
  private Util()
  {
  }

  static ListInformation readOpenListInformationParameter(final byte[] data, final int maxLength) //throws IOException
  {
    ListInformation info = null;
    int numRead = 0;
    if (maxLength >= 12)
    {
      final int totalRecords = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      final int recordsReturned = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      final byte[] handle = new byte[4];
      //in.readFully(handle);
      System.arraycopy(data, numRead, handle, 0, 4);
      numRead += 4;

      int recordLength = 0;
      int infoCompleteType = ListInformation.TYPE_UNKNOWN;
      String creationDate = null;
      int status = ListInformation.STATUS_UNKNOWN;
      int lengthOfInfoReturned = 0;
      int firstRecord = 0;

      if (maxLength >= 31)
      {
        recordLength = Conv.byteArrayToInt(data, numRead);
        numRead += 4;
        final int infoComplete = data[numRead++] & 0x00FF;
        switch (infoComplete)
        {
          case 0x00C3: infoCompleteType = ListInformation.TYPE_COMPLETE; break;
          case 0x00C9: infoCompleteType = ListInformation.TYPE_INCOMPLETE; break;
          case 0x00D7: infoCompleteType = ListInformation.TYPE_PARTIAL; break;
        }
//        final byte[] created = new byte[13];
//        in.readFully(created);
//        creationDate = new String(created, "Cp037");
        creationDate = Conv.ebcdicByteArrayToString(data, numRead, 13);
        numRead += 13;
        final int listStatus = data[numRead++] & 0x00FF;
        switch (listStatus)
        {
          case 0x00F0: status = ListInformation.STATUS_PENDING; break;
          case 0x00F1: status = ListInformation.STATUS_BUILDING; break;
          case 0x00F2: status = ListInformation.STATUS_BUILT; break;
          case 0x00F3: status = ListInformation.STATUS_ERROR; break;
          case 0x00F4: status = ListInformation.STATUS_PRIMED; break;
          case 0x00F5: status = ListInformation.STATUS_OVERFLOW; break;
        }
        numRead += 19;
        if (maxLength >= 40)
        {
          //in.read();
          numRead++;
          lengthOfInfoReturned = Conv.byteArrayToInt(data, numRead);
          numRead += 4;
          firstRecord = Conv.byteArrayToInt(data, numRead);
          numRead += 4;
        }
      }
      info = new ListInformation(totalRecords, recordsReturned,
                                  handle, recordLength,
                                  infoCompleteType, creationDate,
                                  status, lengthOfInfoReturned, firstRecord);
    }
//    in.skipBytes(maxLength-numRead);
    return info;
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
          // listener.newKeyData(key, new String(b, "Cp037"), b);
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

