///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NLSExchangeAttrRequest.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// ----------------------------------------------------
// Exchange Attributes
// ----------------------------------------------------
class NLSExchangeAttrRequest extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    NLSExchangeAttrRequest()
    {
        super();
        data_ = new byte [38];
        setLength(38);
        setHeaderID(0);
        setServerID(0xe000);
        setCSInstance(0);
        setCorrelation(0);
        setTemplateLen(18);
        setReqRepID(0x1301);

        set16bit(0, 20);        // chain, not used
        set16bit(0, 22);        // datastream level
        set32bit(13488, 24);    // client CCSID
        set32bit(0x0310, 28);   // client version, not currently used
                                // by server, always set to v3r1m0
        set16bit(0, 32);        // license management level
        set16bit(0, 34);        // system management level
        set16bit(0, 36);        // NLS level
    }
}

