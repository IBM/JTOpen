///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemValueInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
The SystemValueInfo class represents all of the static properties of a
system value on an AS/400 system. The dynamic properties of a system value,
such as its data and caching, are encapsulated by the SystemValue class.
**/
class SystemValueInfo implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


  String name_ = null;   // The name of this system value
  char type_ = '\0';     // The AS400 data type of this system value
  int size_ = 0;         // The size in bytes of this system value on the 400
  int arraySize_ = 1;    // The number of actual data values this system value contains on the 400
    // Note: arraySize_ is multiplied by size_ to get the total length of this system value on the 400 datastream
    // e.g. QACGLVL represents an 8-element array of 10-byte character values
    // so size_ = 10 and arraySize_ = 8 for QACGLVL
  int decimalPositions_ = -1; // The number of decimal positions if this is
                              // a type TYPE_DECIMAL value
  int returnType_ = -1;       // The Java data type for this value
  int group_ = -1;            // The group this system value belongs to
  int release_ = -1;          // The earliest VRM release in which this value exists
  String description_ = null; // The text describing this system value
  boolean readOnly_ = false;  // Indicates if this system value is read only


  /**
  Constructor.
  **/
  SystemValueInfo(String name, char type, int size, int arraySize, int returnType,
                  int group, int release, String description)
  {
    name_ = name;
    type_ = type;
    size_ = size;
    arraySize_ = arraySize;
    returnType_ = returnType;
    group_ = group;
    release_ = release;
    description_ = description;
  }

  /**
  Constructor that takes a readOnly parameter.
    @param readOnly Indicates if the system value is read only.
  **/
  SystemValueInfo(String name, char type, int size, int arraySize, int returnType,
                  int group, int release, String description, boolean readOnly)
  {
    name_ = name;
    type_ = type;
    size_ = size;
    arraySize_ = arraySize;
    returnType_ = returnType;
    group_ = group;
    release_ = release;
    description_ = description;
    readOnly_ = readOnly;
  }

  /**
  Constructor that takes a decimalPositions parameter.
  **/
  SystemValueInfo(String name, char type, int size, int decimalPositions, int arraySize, int returnType,
                  int group, int release, String description)
  {
    name_ = name;
    type_ = type;
    size_ = size;
    arraySize_ = arraySize;
    decimalPositions_ = decimalPositions;
    returnType_ = returnType;
    group_ = group;
    release_ = release;
    description_ = description;
  }


  //@B0A
  /**
  Equals method for comparison of 2 SystemValueInfo objects.
  **/
  public boolean equals(Object obj)
  { 
    if (obj == null || !(obj instanceof SystemValueInfo))
      return false;

    SystemValueInfo target = (SystemValueInfo)obj;
    return name_.equals(target.name_);
  }
                
}

