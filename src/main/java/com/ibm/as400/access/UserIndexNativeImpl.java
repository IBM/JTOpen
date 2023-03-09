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


interface  UserIndexNativeImpl 
{
 
   /* native methods */ 
   void insertEntry(int handle, byte[] entryBytes, byte[] optionBytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException; 
   void findEntries(int handle, byte[] outputBytes, byte[] optionBytes, byte[] keyBytes) throws AS400SecurityException, ErrorCompletingRequestException, IOException, IllegalObjectTypeException, InterruptedException, ObjectDoesNotExistException; 
   int createAndOpen(
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
   void   closeHandle(int handle); 
   int    open(byte[] objectNameBytes); 
   int    delete(int handle, byte[] objectNameBytes);  
   int    getAttributes(byte[] outputBytes, byte[] formatBytes, byte[]objectNameBytes);  
    
    
}
