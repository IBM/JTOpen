///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2016 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

// ----------------------------------------------------
// Retrieve Conversion Map for double byte mappings
// Coming from a mixed byte CCSID. 
// ----------------------------------------------------
class NLSGetMixedByteTableRequest extends NLSGetTableRequest
{
  private static final String copyright = "Copyright (C) 1997-2016 International Business Machines Corporation and others.";

  NLSGetMixedByteTableRequest()
    {
      super();
      data_ = new byte [20+14+6+65536+65536];
      setLength(20+14+6+2+65536+65536);
      setHeaderID(0);
      setServerID(0xe000);
      setCSInstance(0);
      setCorrelation(0);
      setTemplateLen(14);
      setReqRepID(0x1201);        // retrieve conversion map 

      // template
      set16bit(0, 20);            // chain, not used
      set32bit(1377, 22);   // from CCSID
      set32bit(1200, 26);     // to CCSID 
          
      set16bit(2, 30);            // mapping type (Substitution =2 )
      set16bit(1, 32);            // parameter count
      // optional parameter (the table LLCP)
      set32bit(65536+65536+6, 34);        // LL
      set16bit(4, 38);            // CP
      data_[40]=0x0E;
      for(int i=0; i<=255; i++)
      {
        for(int j=0; j<=255; j++)
        {
          data_[(i*256 + j)*2 + 41] = (byte) i;
          data_[(i*256 + j)*2 + 42] = (byte) j;
        }
      }
      data_[(256 *256 + 0)*2 + 41] = (byte) 0x0f;
    }



    NLSGetMixedByteTableRequest(int fromCcsid, int toCcsid) throws UnsupportedEncodingException
    {
      super();
      
      if (fromCcsid == 1200 || fromCcsid == 13488) {
	      throw new UnsupportedEncodingException("Unicode CCSID not supported"); 
      } 
      data_ = new byte [20+14+6+65536+65536+2];
      setLength(20+14+6+65536+65536+2);
      setHeaderID(0);
      setServerID(0xe000);
      setCSInstance(0);
      setCorrelation(0);
      setTemplateLen(14);
      setReqRepID(0x1201);        // retrieve conversion map 

      // template
      set16bit(0, 20);            // chain, not used
      set32bit(fromCcsid, 22);   // from CCSID
      set32bit(toCcsid, 26);     // to CCSID 
      set16bit(2, 30);            // mapping type (Substitution)
      set16bit(1, 32);            // parameter count
      // optional parameter (the table LLCP)
      set32bit(65536+65536+8, 34);        // LL
      set16bit(4, 38);            // CP
      data_[40]=0x0E;
    for (int i = 0; i <= 255; i++) {
      for (int j = 0; j <= 255; j++) {
        // See if start with SO or SI 
        if ((i < 0x40)) {
          // These are not valid EBCDIC double byte so replace with Substitution character
          data_[(i * 256 + j) * 2 + 41] = (byte) 0xFE;
          data_[(i * 256 + j) * 2 + 42] = (byte) 0xFE;
        } else { 
          if ( ((i == 0x40) && (j < 0x40)) ||
               ((i == 0x40) && (j > 0x40)) ||
               ((i > 0x40) && (j <= 40))) {
            data_[(i * 256 + j) * 2 + 41] = (byte) 0xFE;
            data_[(i * 256 + j) * 2 + 42] = (byte) 0xFE;
          } else { 
            data_[(i * 256 + j) * 2 + 41] = (byte) i;
            data_[(i * 256 + j) * 2 + 42] = (byte) j;
          }
        }
      }

    }
    data_[(256 *256 + 0)*2 + 41] = (byte) 0x0f;
  }


}

