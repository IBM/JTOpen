///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: CurrentUser.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// The currentUser class interfaces with the native code for retrieving the current user's userid and encrypted password.
class CurrentUser
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private SocketContainer container = null;  // Interface object.
    static
    {
        try
        {
            // Load interface object.
            container = (SocketContainer)Class.forName("com.ibm.as400.access.SocketContainerUnix").newInstance();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Error constructing SocketContainer", e);
        }
    }

    // Returns current user ID.
    static String getUserID()
    {
        String userID = null;
        try
        {
            // Get user ID and convert to Java format.
            byte[] currentUser = container.getUser();
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID in EBCDIC: ", currentUser);
            userID = SignonConverter.byteArrayToString(currentUser);
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Current userID: '" + userID + "'");
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Error retrieving current userID", e);
        }
        return userID;
    }

    // Returns encrypted password.
    static byte[] getUserInfo(byte[] clientSeed, byte[] serverSeed) throws IOException
    {
        return (container == null) ? new byte[8] : container.getSubstPassword(clientSeed, serverSeed);
    }
}
