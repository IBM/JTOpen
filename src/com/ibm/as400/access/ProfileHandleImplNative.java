///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileHandleImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.security.auth.*;

/**
 The ProfileHandleImplNative class provides an implementation for behavior delegated by a ProfileHandleCredential object.
 **/
public class ProfileHandleImplNative implements ProfileHandleImpl
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    private AS400Credential credential_ = null;
    static
    {
        System.load("/QSYS.LIB/QYJSPART.SRVPGM");
    }

    /**
     Destroy or clear sensitive information maintained by the credential implementation.
     <p>Subsequent requests may result in a NullPointerException.
     <p>Subclasses should override as necessary to destroy or clear class-specific data.
     @exception  DestroyFailedException  If errors occur while destroying or clearing credential implementation data.
     **/
    public void destroy() throws DestroyFailedException
    {
        releaseHandle(((ProfileHandleCredential)getCredential()).getHandle());
        credential_ = null;
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Credential implementation destroyed >> " + toString());
    }

    // Returns the credential delegating behavior to the implementation object.
    // @return  The associated credential.
    AS400Credential getCredential()
    {
        return credential_;
    }

    /**
     Generates and returns a profile handle based on the current thread identity.
     @return  The handle bytes.
     @exception  RetrieveFailedException  If errors occur while generating the handle.
     **/
    public native byte[] getCurrentHandle() throws RetrieveFailedException;

    /**
     Returns the number of seconds before the credential is due to expire.
     <p>Subclasses implementing timed credentials must override.
     @return  The number of seconds before expiration; zero (0) if already expired or if the credential is not identified as expiring based on time.
     @exception  RetrieveFailedException  If errors occur while retrieving timeout information.
     **/
    public int getTimeToExpiration() throws RetrieveFailedException
    {
        // Profile handles do not expire based on time.
        return 0;
    }

    /**
     Returns the version number for the implementation.
     <p>Used to ensure the implementation is valid for specific functions.
     @return  The version number.
     **/
    public int getVersion()
    {
        return 1; // mod 3
    }

    /**
     Indicates if the credential is still considered valid for authenticating to associated services or performing related actions.
     <p>An exception is not thrown on failure to remain consistent with the Refreshable interface (even though some credential classes currently avoid the dependency established by implementing the interface).
     @return  true if valid; false if not valid or if the operation fails.
     **/
    public boolean isCurrent()
    {
        try
        {
            return (!getCredential().isTimed() || getTimeToExpiration() > 0);
        }
        catch (RetrieveFailedException e)
        {
            Trace.log(Trace.ERROR, "Unable to retrieve credential time to expiration", e);
            return false;
        }
    }

    /**
     Updates or extends the validity period for the credential.
     @exception  RefreshFailedException  If the refresh attempt fails.
     **/
    public void refresh() throws RefreshFailedException
    {
        // Never called; credential is not renewable
    }

    /**
     Releases OS resources for the given profile handle.
     @param  handle  The handle bytes.
     @exception  DestroyFailedException If errors occur while releasing the handle.
     **/
    public native void releaseHandle(byte[] handle) throws DestroyFailedException;

    /**
     Sets the credential delegating behavior to the implementation object.
     @param  credential  The associated credential.
     **/
    public void setCredential(AS400Credential credential)
    {
        if (credential == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'credential' is null.");
            throw new NullPointerException("credential");
        }
        credential_ = credential;
    }

    /**
     Sets the current thread identity based on the given profile handle.
     @param  handle  The handle bytes.
     @exception  SwapFailedException  If errors occur while generating the handle.
     **/
    public native void setCurrentHandle(byte[] handle) throws SwapFailedException;

    /**
     Attempts to swap the thread identity based on this credential.
     @param  genRtnCr  Indicates whether a return credential should be generated, even if supported.  When appropriate, not generating a return credential can improve performance and avoid potential problems in creating the credential.
     @return  A credential capable of swapping back to the original identity; classes not supporting this capability will return null. This value will also be null if genRtnCr is false.
     @exception  SwapFailedException  If errors occur while swapping thread identity.
     @exception  SecurityException  If the caller does not have permission to modify the OS thread identity.
     **/
    public AS400Credential swap(boolean genRtnCr) throws SwapFailedException
    {
        setCurrentHandle(((ProfileHandleCredential)getCredential()).getHandle());
        return null;
    }
}
