///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ImplNative.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


class AS400ImplNative
{
    private static final String CLASSNAME = "com.ibm.as400.access.AS400ImplNative";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);

        NativeMethods.loadNativeLibraryQyjspart(); 
    }

    static native byte[] signonNative(byte[] userId) throws NativeException;
    /* Swap to a user profile using the userid and password */
    /* The swapToPH is the profile handle generated from the userid and passowrd */
    /* The swapFromPH is the profile handle to get back to the original profile handle */
    static native void swapToNative(byte[] userId, byte[] bytes, byte[] swapToPH, byte[] swapFromPH) throws NativeException;
    /* Swap using a profile handle and free the old handle */
    /* swapToPH is the old profile handle that will be released.  This handle will also be released. */
    /* swapFromPH is the profile to swap to */ 
    static native void swapBackNative(byte[] swapToPH, byte[] swapFromPH) throws NativeException;
    

     
    /* Create profile handle with additional authentication information. 
     * This method is only available after IBM 7.5. 
     * This profile handle should be released when the AS400 object is done with it.  */
    static native void createProfileHandle2Native(byte[] profileHandle, 
    		String userId, byte[] bytes, char[] additionalAuthenticationFactor,
    		String verificationId, String remoteIpAddress, int jRemotePort,   String localIpAddress, int jLocalPort ) throws NativeException;
    
   
    /* Swap to the user associated with swapToPH.  The swapFromPH is the handle for the original user, 
     * which is used with swapping back using swapBackAndReleaseNative.
     * This method is only available after IBM 7.5. 
      */ 
    static native void swapToProfileHandleNative(byte[] swapToPH, byte[] swapFromPH) throws NativeException;

    /* Swap back to the original profile handle and free it.. This method is only available after IBM 7.5.  */ 
    static native void swapBackAndReleaseNative(byte[] swapFromPH) throws NativeException;

  
    /* Release the originally allocated handle.  This method is only available after IBM 7.5.   */ 
    static native void releaseProfileHandleNative(byte[] profileHandle) throws NativeException;

  
    
}
