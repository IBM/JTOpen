///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: TokenManager.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

class TokenManager
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    static byte[] getGSSToken(String systemName) throws Exception
    {
        GSSManager manager = GSSManager.getInstance();
        if (Trace.isTraceOn())
        {
            Oid[] mechs = manager.getMechs();
            Trace.log(Trace.DIAGNOSTIC, "GSS number of mechs available: ", mechs.length);
            for (int i = 0; i < mechs.length; ++i) Trace.log(Trace.DIAGNOSTIC, mechs[i].toString());
        }
        Oid krb5Mech = new Oid("1.2.840.113554.1.2.2");
        GSSName serverName = manager.createName("krbsvr400@" + systemName, GSSName.NT_HOSTBASED_SERVICE, krb5Mech);
        GSSCredential credential = manager.createCredential(GSSCredential.INITIATE_ONLY);
        GSSContext context = manager.createContext(serverName, krb5Mech, credential, GSSCredential.DEFAULT_LIFETIME);
        return context.initSecContext(null, 0, 0);
    }
}
