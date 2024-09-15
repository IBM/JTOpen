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
 * The ProfileHandleImplNative class provides an implementation for behavior delegated by a ProfileHandleCredential
 * object.
 **/
public class ProfileHandleImplNative implements ProfileHandleImpl
{
    // Note: This class needs to be public, because it's referenced by class
    // com.ibm.as400.security.auth.ProfileHandleCredential
    private static final String CLASSNAME = "com.ibm.as400.access.ProfileHandleImplNative";
    static {
        if (Trace.traceOn_)
            Trace.logLoadPath(CLASSNAME);
    }

    private AS400Credential credential_ = null;
    static {
        NativeMethods.loadNativeLibraryQyjspart();
    }

    /**
     * Destroy or clear sensitive information maintained by the credential implementation.
     * <p>
     * Subsequent requests may result in a NullPointerException.
     * <p>
     * Subclasses should override as necessary to destroy or clear class-specific data.
     * 
     * @exception DestroyFailedException If errors occur while destroying or clearing credential implementation data.
     **/
    public void destroy() throws DestroyFailedException
    {
        releaseHandle(((ProfileHandleCredential) getCredential()).getHandle());
        credential_ = null;
        if (Trace.isTraceOn()) Trace.log(Trace.INFORMATION, "Credential implementation destroyed >> " + toString());
    }

    // Returns the credential delegating behavior to the implementation object.
    // @return  The associated credential.
    AS400Credential getCredential()
    {
        return credential_;
    }

    @Override
    public native byte[] getCurrentHandle() throws RetrieveFailedException;

    /**
     * Returns the number of seconds before the credential is due to expire.
     * <p>
     * Subclasses implementing timed credentials must override.
     * 
     * @return The number of seconds before expiration; zero (0) if already expired or if the credential is not
     *         identified as expiring based on time.
     * @exception RetrieveFailedException If errors occur while retrieving timeout information.
     **/
    public int getTimeToExpiration() throws RetrieveFailedException
    {
        // Profile handles do not expire based on time.
        return 0;
    }

    @Override
    public int getVersion()
    {
        return 1; // mod 3
    }

    @Override
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

    @Override
    public void refresh() throws RefreshFailedException
    {
        // Never called; credential is not renewable
    }

    /**
     * Releases OS resources for the given profile handle.
     * 
     * @param handle The handle bytes.
     * @exception DestroyFailedException If errors occur while releasing the handle.
     **/
    public native void releaseHandle(byte[] handle) throws DestroyFailedException;

    @Override
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
     * Sets the current thread identity based on the given profile handle.
     * 
     * @param handle The handle bytes.
     * @exception SwapFailedException If errors occur while generating the handle.
     **/
    public native void setCurrentHandle(byte[] handle) throws SwapFailedException;

    @Override
    public AS400Credential swap(boolean genRtnCr) throws SwapFailedException
    {
        setCurrentHandle(((ProfileHandleCredential)getCredential()).getHandle());
        return null;
    }
}
