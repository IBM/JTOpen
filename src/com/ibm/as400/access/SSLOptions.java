///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SSLOptions.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

// Class to move SSL configuration options from proxy client to proxy server.
class SSLOptions implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    static final long serialVersionUID = 4L;
    // Package and class name of key ring object, initialized to default.
    String keyRingName_ = "com.ibm.as400.access.KeyRing";
    // Password for keyring class, initialized to default.
    String keyRingPassword_ = "toolbox";
    // Data from keyring class.
    String keyRingData_ = null;
    // Legs of proxy server communications that should be encrypted.  Default is to encrypt all legs.
    int proxyEncryptionMode_ = SecureAS400.CLINT_TO_SERVER;
    boolean useSslight_ = false;
}
