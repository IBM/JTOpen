///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserQueue.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Arrays;

/**
 The UserQueue class represents an IBM i user queue object.
 
 This class currently will only function when running on the IBM i using native Methods. 
 
  <p>As a performance optimization, when running directly on IBM i, it is possible to use native
    methods to access the user space from the current job.  To enable this support, use the 
    {@link #setMustUseNativeMethods setMustUseNativeMethods()} method. 

 **/

class UserQueueImplILE implements UserQueueImpl 
{
	static {
		/* Make sure the library is loaded */
		   NativeMethods.loadNativeLibraryQyjspart(); 
	}
	
   public UserQueueImplILE() {
	   // Nothing to do in constructor 
   }
   
   public int create(byte[] objectNameBytes,
			byte[] extendedAttributeBytes, byte queueType, int keyLength,
			int dataSize, int initialNumberOfMessages,
			int additionNumberOfMessages, byte[] publicAuthorityBytes,
			byte[] descriptionBytes, byte[] replaceBytes) {
	   return nativeCreate(objectNameBytes, extendedAttributeBytes, queueType, keyLength, dataSize, initialNumberOfMessages, additionNumberOfMessages, publicAuthorityBytes, descriptionBytes, replaceBytes);
	   
   }

   public int enqueue(int handle, byte[] enqMsgPrefix, byte[] value) {
		return nativeEnqueue(handle, enqMsgPrefix, value);
	}
   public int dequeue(int handle, byte[] deqMsgPrefix, byte[] outputBuffer) {
		return nativeDequeue(handle, deqMsgPrefix, outputBuffer);
	}
   public int delete(int handle, byte[] objectNameBytes) {
		return nativeDelete(handle, objectNameBytes); 
	}
   public int open(byte[] objectNameBytes) {
    	return nativeOpen(objectNameBytes); 
    }
   
   public int close(int handle) { 
	   return nativeClose(handle); 
   }

   public int getAttributes(int handle, byte[] outputBytes) {
	   return nativeGetAttributes(handle, outputBytes); 
   }

    native int nativeCreate(byte[] objectNameBytes,
			byte[] extendedAttributeBytes, byte queueType, int keyLength,
			int dataSize, int initialNumberOfMessages,
			int additionNumberOfMessages, byte[] publicAuthorityBytes,
			byte[] descriptionBytes, byte[] replaceBytes);
	native int nativeEnqueue(int handle, byte[] enqMsgPrefix, byte[] value);
	native int nativeDequeue(int handle, byte[] deqMsgPrefix, byte[] outputBuffer);
	native int nativeDelete(int handle, byte[] objectNameBytes);  
    native int nativeOpen(byte[] objectNameBytes); 
    native int nativeClose(int handle); 
    native int nativeGetAttributes(int handle, byte[] outputBytes);

    
    
}
