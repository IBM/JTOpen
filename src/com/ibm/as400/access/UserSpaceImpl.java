///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserSpaceImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

// UserSpaceImpl defines the implementation interface for the UserSpace object.
interface UserSpaceImpl
{
    public void close() throws IOException;

    public void create(byte[] domainBytes, int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public byte getInitialValue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public int getLength() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public boolean isAutoExtendible() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public int read(byte dataBuffer[], int userSpaceOffset, int dataOffset, int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public void setAutoExtendible(boolean autoExtendibility) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public void setInitialValue(byte initialValue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public void setLength(int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;

    public void setProperties(AS400Impl system, String path, String name, String library, boolean mustUseProgramCall);

    public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException;
}
