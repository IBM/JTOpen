///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserIndex.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;


class  UserIndexNativeImplILE implements UserIndexNativeImpl
{
 
	static {
		/* Make sure the library is loaded */
		   NativeMethods.loadNativeLibraryQyjspart(); 
	}

   /* native methods */ 
   public void insertEntry(int handle, byte[] entryBytes, byte[] optionBytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException {
	   nativeInsertEntry(handle, entryBytes, optionBytes);
   }
   public void findEntries(int handle, byte[] outputBytes, byte[] optionBytes, byte[] keyBytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException {
	   nativeFindEntries(handle, outputBytes, optionBytes, keyBytes);
   }
   public int createAndOpen(
		    byte[]objectNameBytes,
		    byte[] extendedAttributeBytes,
		    byte entryLengthAttribute,
		    int entryLength,
		    byte keyInsertion,
		    int keyLength,
		    byte immediateUpdate,
		    byte optimization,
		    byte[] publicAuthorityBytes,
		    byte[] descriptionBytes) {
	   return nativeCreateAndOpen(objectNameBytes, extendedAttributeBytes, entryLengthAttribute, entryLength, keyInsertion, keyLength, immediateUpdate, optimization, publicAuthorityBytes, descriptionBytes);
   }
   public void   closeHandle(int handle){
	   nativeCloseHandle(handle);
   }
   public int    open(byte[] objectNameBytes){
	   return nativeOpen(objectNameBytes);
   }
   public int    delete(int handle, byte[] objectNameBytes) {
	   return nativeDelete(handle, objectNameBytes);
   }
   public int getAttributes(byte[] outputBytes, byte[] formatBytes,	byte[] objectNameBytes) {
		   return nativeGetAttributes(outputBytes, formatBytes, objectNameBytes); 
	   }  
    
   private native void nativeInsertEntry(int handle, byte[] entryBytes, byte[] optionBytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException; 
   private native void nativeFindEntries(int handle, byte[] outputBytes, byte[] optionBytes, byte[] keyBytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException; 
   private native int nativeCreateAndOpen(
		    byte[]objectNameBytes,
		    byte[] extendedAttributeBytes,
		    byte entryLengthAttribute,
		    int entryLength,
		    byte keyInsertion,
		    int keyLength,
		    byte immediateUpdate,
		    byte optimization,
		    byte[] publicAuthorityBytes,
		    byte[] descriptionBytes) ;     
   private native void   nativeCloseHandle(int handle); 
   private native int    nativeOpen(byte[] objectNameBytes); 
   private native int    nativeDelete(int handle, byte[] objectNameBytes);
   private native int    nativeGetAttributes(byte[] outputBytes, byte[] formatBytes, byte[] objectNameBytes); 

    
}
