///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SignonInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.util.GregorianCalendar;

class SignonInfo implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;
    int signonRC;
    GregorianCalendar currentSignonDate;
    GregorianCalendar lastSignonDate;
    GregorianCalendar expirationDate;
    ServerVersion version;
    int serverCCSID;
    // Note: not maintained as com.ibm.as400.security.auth.ProfileToken.  Class is currently not available in proxy environments, so don't want to force instantiation during signon.
    Object profileToken;
}
