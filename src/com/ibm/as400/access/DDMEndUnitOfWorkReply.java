///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMEndUnitOfWorkReply.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 *Represents the ENDUOWRM DDM data stream.  This reply stream is returned
 *when an commit or rollback is done
**/
class DDMEndUnitOfWorkReply extends DDMReplyDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  int uowDsp_ = -1;  // uowDsp_ to -1 to indicate that it has not been set
  int svrCode_ = -1; // Set svrCode_ to -1 to indicate that it has not been set

  /**
   *Constructs an ENDUOWRM reply message data stream.
   *@param data the data with which to populate this object
  **/
  DDMEndUnitOfWorkReply(byte[] data)
  {
    super(data);
    parseDataStream();
  }

  /**
   *Returns the value of the UOWDSP parameter of this ENDUOWRM reply
   *@return the status returned in this ENDUOWRM data stream.
  **/
  int getStatus()
  {
    return uowDsp_;
  }

  /**
   *Returns the value of the SVRCOD parameter of this ENDUOWRM reply
   *@return the severity codes returned in this ENDUOWRM data stream.
  **/
  int getSeverityCode()
  {
    return svrCode_;
  }

  /**
   *Extracts the UOWDSP and SVRCOD parameter values from the data.
  **/
  void parseDataStream()
  {
    int offset = 6;  // Start after header
    int length = get16bit(offset);       // Total length of the data stream after the header.
    int codePoint = get16bit(8);         // Code point; should be D202 = ENDUOWRM
    if (codePoint != DDMTerm.ENDUOWRM)
    {
      return;
    }
    if (length <= 4)
    {
      return;
    }

    offset += 4; // Get to the first code point after ENDUOWRM
    while (!(offset > data_.length - 4))
    {
      length = get16bit(offset);         // Get length of term
      codePoint = get16bit(offset + 2);  // get code point of this term

      switch(codePoint)
      {
        case DDMTerm.SVRCOD:
        {
          svrCode_ = get16bit(offset + 4);
          break;
        }
        case DDMTerm.UOWDSP:
        {
          uowDsp_ = data_[offset + 4];
          break;
        }
      }
      offset += length;
    }
  }
}
