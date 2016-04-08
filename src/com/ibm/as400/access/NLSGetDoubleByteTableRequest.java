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

// ----------------------------------------------------
// Retrieve Conversion Map
// ----------------------------------------------------
class NLSGetDoubleByteTableRequest extends NLSGetTableRequest
{
  private static final String copyright = "Copyright (C) 1997-2016 International Business Machines Corporation and others.";

  NLSGetDoubleByteTableRequest()
    {
      super();
      data_ = new byte [20+14+6+65536+65536];
      setLength(20+14+6+65536+65536);
      setHeaderID(0);
      setServerID(0xe000);
      setCSInstance(0);
      setCorrelation(0);
      setTemplateLen(14);
      setReqRepID(0x1201);        // retrieve conversion map 

      // template
      set16bit(0, 20);            // chain, not used
      set32bit(13488, 22);   // from CCSID
      set32bit(37, 26);     // to CCSID 
      set16bit(2, 30);            // mapping type (Substitution)
      set16bit(1, 32);            // parameter count
      // optional parameter (the table LLCP)
      set32bit(65536+65536+6, 34);        // LL
      set16bit(4, 38);            // CP
      for(int i=0; i<=255; i++)
      {
        for(int j=0; j<=255; j++)
        {
          data_[(i*256 + j)*2 + 40] = (byte) i;
          data_[(i*256 + j)*2 + 41] = (byte) j;
        }
      }
    }



    NLSGetDoubleByteTableRequest(int fromCcsid)
    {
      super();
      boolean fromUnicode = false;
      if (fromCcsid == 1200 || fromCcsid == 13488) {
	  fromUnicode = true; 
      } 
      data_ = new byte [20+14+6+65536+65536];
      setLength(20+14+6+65536+65536);
      setHeaderID(0);
      setServerID(0xe000);
      setCSInstance(0);
      setCorrelation(0);
      setTemplateLen(14);
      setReqRepID(0x1201);        // retrieve conversion map 

      // template
      set16bit(0, 20);            // chain, not used
      set32bit(fromCcsid, 22);   // from CCSID
      set32bit(37, 26);     // to CCSID 
      set16bit(2, 30);            // mapping type (Substitution)
      set16bit(1, 32);            // parameter count
      // optional parameter (the table LLCP)
      set32bit(65536+65536+6, 34);        // LL
      set16bit(4, 38);            // CP
      for(int i=0; i<=255; i++)
      {
        for(int j=0; j<=255; j++)
        {
	    if (fromUnicode && (i >= 0xD8 && i <= 0xDF)) {
		// surrogate -- dont convert
		data_[(i*256 + j)*2 + 40] = (byte) 0x00;
		data_[(i*256 + j)*2 + 41] = (byte) 0x1A;

	    } else {   
	      // Filter out the ranges that don't convert correctly
	      // in ccsid 16684
	      // Note, the range 0xecb0-0xecb4 converts 1 character to U'fffdfffd'
              // as illustrated by the following query (job ccsid must be 1399)
	      // select CAST(CAST(GX'ecb4' AS VARGRAPHIC(80) CCSID 16684)  AS VARGRAPHIC(80) CCSID 1200) from sysibm.sysdummy1
              // 00001
              // U'fffdfffd'
	      // This causes problems with the offsets of the mapping.
              // To avoid this problem, we skip these values
	      // 
	      if ((fromCcsid== 16684) && 
	          ((  (i*256+j) >= 0xecb0) &&
	              (i*256+j)  < 0xecb5)) { 
	        System.out.println("Filtering "+Integer.toHexString(i*256+j));
	        data_[(i*256 + j)*2 + 40] = (byte) 0xFe;
	        data_[(i*256 + j)*2 + 41] = (byte) 0xFE;
	        
	      } else { 
	          
		data_[(i*256 + j)*2 + 40] = (byte) i;
		data_[(i*256 + j)*2 + 41] = (byte) j;
	      }
		
		
	    }
        }
      }
    }




}

