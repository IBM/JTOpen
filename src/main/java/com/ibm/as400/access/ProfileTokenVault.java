///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ProfileTokenVault.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.security.auth.ProfileTokenCredential;
import java.io.Serializable;

/**
 * A vault which holds a profile token.  The profile token represents the credentials
 * of an IBM i user profile for a system, and can be used for authenticating
 * to one or more IBM i host servers on that system.
 */
class ProfileTokenVault extends CredentialVault implements Cloneable, Serializable
{
    private ProfileTokenCredential profileTokenCredential_;

    /**
     * Constructs an ProfileTokenVault object that does not contain a credential
     */
    protected ProfileTokenVault()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault() called");
    }

    /**
     * Constructs an ProfileTokenVault object that contains the provided profile token credential.
     *
     * @param existingToken The profile token bytes
     */
    protected ProfileTokenVault(byte[] existingToken)
    {
        super(existingToken);
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault(byte[] existingToken) called");
    }

  /**
   * Constructs an ProfileTokenVault object that contains the provided profile token credential.
   *
   * @param existingCredential The profile token credential.  The raw bytes from the profile
   *                           token object are extracted and stored in the vault.
   */
    protected ProfileTokenVault(ProfileTokenCredential existingCredential)
    {
        super();
    
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault(ProfileTokenCredential existingCredential) called");

        // Note:  The logic to set the encodedCredential_ was move to getClearCredential
        //encodedCredential_ = store(existingCredential.getToken());
        // With a profile token that may be refreshed, we do not save the 
        // encodedCredential but must obtain it every time.    Setting the encodedCredential_
        // to null causes the overridden methods in this class to use the profileToken
        // instead of the encodedCredential.  @E1A
    
        encodedCredential_ = null; 
        storeProfileTokenCredential(existingCredential);
    }

    /**
     * Returns a copy of this ProfileTokenVault.  The new copy will be
     * an exact copy of this vault, which means the new copy will
     * contain the same profile token credential as this vault.
     *
     * @return A newly created ProfileTokenVault that is a copy of this one
     */
    @Override
    public ProfileTokenVault clone()
    {
        ProfileTokenVault vaultClone = (ProfileTokenVault)super.clone();
        vaultClone.profileTokenCredential_ = profileTokenCredential_;
        return vaultClone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getType() {
        return AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN;
    }
  
    /**
     * Cache ProfileTokenCredential object in vault.
     * 
     * @param credential The ProfileTokenCredential object.
     */
    public void storeProfileTokenCredential(ProfileTokenCredential credential) {
        this.profileTokenCredential_ = credential;
    }
    
    /**
     * Retrieve ProfileTokenCredential object in vault if one exists.
     * 
     * @return The ProfileTokenCredential object or null.
     */
    public ProfileTokenCredential getProfileTokenCredential() {
        return profileTokenCredential_;
    }
  
    /**
     * Retrieves unencoded credential from a vault
     */
    @Override
    protected synchronized byte[] getClearCredential()
    {
        if (profileTokenCredential_ != null)
        {
            byte[] encodedCredential = store(profileTokenCredential_.getToken());
            return resolve(encodedCredential);
        }
        
        return super.getClearCredential();
    }
  
    /**
     * Block the thread to refresh profile token credential from the vault.
     */
    public void preventRefresh()
    {
        if (profileTokenCredential_ == null)
            return;

        try {
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault preventRefresh called");
            profileTokenCredential_.preventRefresh();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
  
    /**
     * Notify the wait thread to refresh profile token credential from the vault.
     */
    public void allowRefresh()
    {
        if (profileTokenCredential_ == null)
            return;
        
        if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault allowRefresh called");
        profileTokenCredential_.allowRefresh();
    }
  
    /**
     * Queries the vault to see if it contains a credential.
     */
    @Override
    protected boolean isEmpty()
    {
        if (profileTokenCredential_ != null )
        {
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault isEmpty called");
            return false; 
        } 

        return super.isEmpty(); 
    }
    
    /**
     * Disposes of the credential, thus emptying the vault.
     */
    @Override
    protected void disposeOfCredential()
    {
        if (profileTokenCredential_ != null )
        {
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault disposeOfCredential called");
            profileTokenCredential_ = null; 
        }    
        
        super.disposeOfCredential();
    }

    /**
     * Provides a minimal amount of tracing information about the credential
     * vault in a nicely formatted string.
     *
     * @return The tracing information represented as a string
     */
    @Override
    protected String trace()
    {
        if (profileTokenCredential_ != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("type=");
            sb.append(getType());
            sb.append(" : profileTokenCredential=");
            sb.append(profileTokenCredential_.toString()); 

            return sb.toString();
        } 
  
        return super.trace();
    }
  
    /**
     * Decodes the credential bytes using the provided adder and mask.
     * The decoded credential is returned in a newly allocated byte array.
     *
     * @param adder Used for encoding
     * @param mask Used for encoding
     *
     * @return A newly allocated array of bytes representing the decoded credential
     */
    @Override
    protected byte[] decode(byte[] adder, byte[] mask)
    {
        if (profileTokenCredential_ != null )
        {
            if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "ProfileTokenVault decode called");
            byte[] encodedCredential = store(profileTokenCredential_.getToken());
            
            // encodedCredential is encoded profile token and added 16 byte additional data. 
            // we can not get the clear profile token by decode the encodedCredential. 
            // We need resolve it to get clear profile token.
            //return CredentialVault.decode(adder, mask, encodedCredential); //@AI8D
            return resolve(encodedCredential); //@AI8A
        }
        
        return super.decode(adder, mask); 
    }
}
