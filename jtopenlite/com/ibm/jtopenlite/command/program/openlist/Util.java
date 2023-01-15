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
package com.ibm.jtopenlite.command.program.openlist;

import com.ibm.jtopenlite.*;


/**
 * Internal use.
**/
public final class Util
{
  private Util()
  {
  }

  public static ListInformation readOpenListInformationParameter(final byte[] data, final int maxLength) //throws IOException
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
}

