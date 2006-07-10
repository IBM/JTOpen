///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AuthenticationSystem.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.security.auth;

import java.beans.PropertyVetoException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.InternalErrorException;
import com.ibm.as400.access.Trace;

// Common point of access for system information used during authentication of server principals and credentials.
class AuthenticationSystem
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    private static AS400 system_ = null;
    private static boolean onAS400_ = false;

    static
    {
        try
        {
            AuthenticationSystem.system_ = new AS400();
            AuthenticationSystem.system_.setGuiAvailable(false);
        }
        catch (PropertyVetoException e)
        {
            handleUnexpectedException(e);
        }

        try
        {
            String osName = System.getProperty("os.name");
            Trace.log(Trace.DIAGNOSTIC, "Detected os.name: " + osName);
            if (osName != null && osName.equalsIgnoreCase("OS/400")) AuthenticationSystem.onAS400_ = true;
        }
        catch (SecurityException e)
        {
            Trace.log(Trace.WARNING, "Error retrieving os.name:", e);
        }
    }

     // This class is not intended to be instantiated.
    private AuthenticationSystem()
    {
    }

    // Logs trace information and throws an InternalErrorException.
    // @param  t  The unexpected throwable.
    static void handleUnexpectedException(Throwable t)
    {
        Trace.log(Trace.ERROR, "Unexpected Exception:", t);
        throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
    }

    // Indicates if the given i5/OS system and its user ID match the current environment.
    // @return  true if matched; false otherwise.
    static boolean isLocal(AS400 system)
    {
        return system.isLocal() && system.getUserId().trim().equalsIgnoreCase(new AS400().getUserId());
    }

    // Returns a system object representing the local host.  This value is cached so that the same instance is returned on subsequent calls.
    // @return  The system object.
    static AS400 localHost()
    {
        return AuthenticationSystem.system_;
    }

    // Resets services for the local host; ignored if not running on an i5/OS system.  Since the cached system is always intended to represent the current system and user, it is always affected when a swap occurs.  A reset is also required to allow the correct evaluation of when to use native optimizations.
    static void resetLocalHost()
    {
        // Ignore if not running on i5/OS.
        if (!AuthenticationSystem.onAS400_) return;

        // Reset services for the cached system started under the old identity.
        try
        {
            // Disconnect and reset the state of the system object.
            AuthenticationSystem.system_.resetAllServices();
            // Reset the system user ID & password to force re-resolve.
            AuthenticationSystem.system_.setUserId("");
            AuthenticationSystem.system_.setPassword("");
            // Request a service port to take system out of unset state.
            AuthenticationSystem.system_.getServicePort(AS400.SIGNON);
        }
        catch (PropertyVetoException e)
        {
            handleUnexpectedException(e);
        }
    }
}
