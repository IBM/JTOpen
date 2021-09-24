///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserSpaceImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

// UserSpaceImpl defines the implementation interface for the UserSpace object.
class UserSpaceNativeReadWriteImplILE implements UserSpaceNativeReadWriteImpl
{
	static SecurityException loadSecurityException = null;
	static UnsatisfiedLinkError loadUnsatisfiedLinkError = null; 
	static {
		/* Make sure the library is loaded */
		/* Delay the error until actual usage */
		try { 
		   NativeMethods.loadNativeLibraryQyjspartThrowsException();
		} catch (SecurityException secEx) {
			loadSecurityException = secEx; 
		} catch (UnsatisfiedLinkError unsatisfiedLinkError) {
			loadUnsatisfiedLinkError = unsatisfiedLinkError;
		}
	}
	
	
	AS400 system_; 
	byte[] qualifiedUserSpaceName_ = null; 
	int handle_; 
	
	public UserSpaceNativeReadWriteImplILE(AS400 impl) throws SecurityException, UnsatisfiedLinkError{
		if (loadSecurityException != null ) throw loadSecurityException; 
		if (loadUnsatisfiedLinkError != null) throw loadUnsatisfiedLinkError; 
		system_ = impl; 
	}
	
	
	public void open(String library_, String name_) throws UnsupportedEncodingException, CharConversionException {
        // Get a converter for the system objects CCSID.
		Converter converter_ = new Converter(system_.getCcsid(), system_); 
        // The name and library of the user space used in program call.  Start with 20 EBCDIC spaces (" ").
        qualifiedUserSpaceName_ = new byte[] { 
        		0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 
        		0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        // Put the converted object name at the beginning of the array.
        converter_.stringToByteArray(name_, qualifiedUserSpaceName_, 0);
        // Put the converted library name at position ten.
        converter_.stringToByteArray(library_, qualifiedUserSpaceName_, 10);
        handle_= nativeAllocate(qualifiedUserSpaceName_); 
	}
	/* read bytes from the userSpace and return number of bytes read */ 
    public int read(byte dataBuffer[], int userSpaceOffset, int dataOffset, int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException {
    	return nativeRead(handle_, dataBuffer, userSpaceOffset, dataOffset, length); 
    }
    
    public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException {
    	nativeWrite(handle_, dataBuffer, userSpaceOffset,dataOffset,length, forceAuxiliary); 
    }
    
    public void close() {
    	if (handle_ != 0) { 
    	    nativeClose(handle_); 
    	    handle_=0; 
    	}
    }

    native int nativeAllocate(byte[] qualifiedUserSpaceName);
    native int nativeRead (int handle, byte dataBuffer[], int userSpaceOffset, int dataOffset, int length);
    native int nativeWrite(int handle, byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary);
    native int nativeClose(int handle); 
    
    protected void finalize() throws Throwable {
    	if (handle_ != 0) close(); 
    }
}
