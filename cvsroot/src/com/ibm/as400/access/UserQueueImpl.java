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

interface UserQueueImpl 
{
   public int create(byte[] objectNameBytes,
			byte[] extendedAttributeBytes, byte queueType, int keyLength,
			int dataSize, int initialNumberOfMessages,
			int additionNumberOfMessages, byte[] publicAuthorityBytes,
			byte[] descriptionBytes, byte[] replaceBytes);

   public int enqueue(int handle, byte[] enqMsgPrefix, byte[] value);
   public int dequeue(int handle, byte[] deqMsgPrefix, byte[] outputBuffer);
   public int delete(int handle, byte[] objectNameBytes);  
   public int open(byte[] objectNameBytes); 
   public int close(int handle); 
   public int getAttributes(int handle, byte[] outputBytes);
}
