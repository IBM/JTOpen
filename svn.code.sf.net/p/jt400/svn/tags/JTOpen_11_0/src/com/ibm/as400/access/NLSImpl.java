///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NLSImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.net.UnknownHostException;

// Abstract base class that supports native and remote implementations of central server function
abstract class NLSImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    AS400ImplRemote system_;
    
    void setSystem(AS400ImplRemote system)
    {
      system_ = system;
    }

    abstract void connect() throws ServerStartupException, UnknownHostException, AS400SecurityException, ConnectionDroppedException, InterruptedException, IOException;
    abstract void disconnect();
    abstract int getCcsid() throws IOException;
//@B0D    abstract char[] getTable(int fromCCSID, int toCCSID) throws ConnectionDroppedException, IOException, InterruptedException;
}
