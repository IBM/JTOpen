///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NLSGetTableRequest.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// ----------------------------------------------------
// Retrieve Conversion Map
// ----------------------------------------------------
class NLSGetTableRequest extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    NLSGetTableRequest()
    {
        super();
        data_ = new byte [20+14+6+256];
        setLength(20+14+6+256);
        setHeaderID(0);
        setServerID(0xe000);
        setCSInstance(0);
        setCorrelation(0);
        setTemplateLen(14+6+256);
        setReqRepID(0x1201);

        // template
        set16bit(0, 20);            // chain, not used
        set32bit(37, 22);   // from CCSID
        set32bit(0xF200, 26);     // to CCSID
        set16bit(2, 30);            // mapping type (Substitution)
        set16bit(1, 32);            // parameter count
        // optional parameter (the table LLCP)
        set32bit(256+6, 34);        // LL
        set16bit(4, 38);            // CP
        int i;
        for (i=0; i<=255; i++)
        {
            data_[i+40] = (byte) i;
        }
    }

    void setCCSIDs( int fromCCSID, int toCCSID )
    {
        set32bit(fromCCSID, 22);   // from CCSID
        set32bit(toCCSID, 26);     // to CCSID
    }
}

