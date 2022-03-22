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
interface UserSpaceNativeReadWriteImpl
{
	public void open(String library_, String name_) throws UnsupportedEncodingException, CharConversionException;
    public int read(byte dataBuffer[], int userSpaceOffset, int dataOffset, int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;
    public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;
    public void close(); 
}
