///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemValueInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

// The SystemValueInfo class represents all of the static properties of a system value.  The dynamic properties of a system value, such as its data and caching, are encapsulated by the SystemValue class.
class SystemValueInfo implements Serializable
{
    static final long serialVersionUID = 4L;

    // The name of this system value.
    String name_ = null;
    // The IBM i data type of this system value.
    // Valid values are SystemValueList.SERVER_TYPE_BINARY and SERVER_TYPE_CHAR.
    byte serverDataType_ = (byte)'\0';
    // The size in bytes of this system value on the system.
    int size_ = 0;
    // The number of actual data values this system value contains on the system.
    // Note:  arraySize_ is multiplied by size_ to get the total length of this system value on the datastream.
    // e.g. QACGLVL represents an 8-element array of 10-byte character values so size_ = 10 and arraySize_ = 8 for QACGLVL.
    int arraySize_ = 1;
    // The number of decimal positions if this is a type TYPE_DECIMAL value.
    int decimalPositions_ = -1;
    // The Java data type for this value.
    // Valid values are SystemValueList.TYPE_ARRAY, TYPE_DATE, TYPE_DECIMAL, TYPE_INTEGER, TYPE_STRING.
    int type_ = -1;
    // The group in which this system value belongs.
    int group_ = -1;
    // The earliest VRM release in which this value exists.
    int release_ = -1;
    // The text describing this system value.
    String description_ = null;
    // Indicates if this system value is read only.
    boolean readOnly_ = false;

    SystemValueInfo(String name, byte serverDataType, int size, int arraySize, int type, int group, int release, String description)
    {
        name_ = name;
        serverDataType_ = serverDataType;
        size_ = size;
        arraySize_ = arraySize;
        type_ = type;
        group_ = group;
        release_ = release;
        description_ = description;
    }

    // Constructor that takes a readOnly parameter.
    // @param  readOnly  Indicates if the system value is read only.
    SystemValueInfo(String name, byte serverDataType, int size, int arraySize, int type, int group, int release, String description, boolean readOnly)
    {
        name_ = name;
        serverDataType_ = serverDataType;
        size_ = size;
        arraySize_ = arraySize;
        type_ = type;
        group_ = group;
        release_ = release;
        description_ = description;
        readOnly_ = readOnly;
    }

    // Constructor that takes a decimalPositions parameter.
    SystemValueInfo(String name, byte serverDataType, int size, int decimalPositions, int arraySize, int type, int group, int release, String description)
    {
        name_ = name;
        serverDataType_ = serverDataType;
        size_ = size;
        arraySize_ = arraySize;
        decimalPositions_ = decimalPositions;
        type_ = type;
        group_ = group;
        release_ = release;
        description_ = description;
    }

    // Equals method for comparison of 2 SystemValueInfo objects.
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof SystemValueInfo)) return false;

        SystemValueInfo target = (SystemValueInfo)obj;
        return (name_ != null && name_.equals(target.name_));
    }

    // hashCode method, to complement the equals() method.
    public int hashCode()
    {
      return (name_ == null ? super.hashCode() : name_.hashCode());
    }
}
