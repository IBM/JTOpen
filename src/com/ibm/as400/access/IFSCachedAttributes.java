///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSCachedAttributes.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
import java.io.Serializable;


/**
Store cached attributes.
**/
class IFSCachedAttributes implements Serializable 
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  static final int FA_READONLY  = 0x01;                               
  static final int FA_HIDDEN    = 0x02;                              

  long accessDate_;
  long creationDate_;
  int fixedAttributes_;
  boolean isDirectory_;
  boolean isFile_;
  long modificationDate_;
  String name_;
  int objectType_;
  String path_;
  long size_;                   // @A1c

/**
Construct listCachedAttributes object from a list of attributes.
**/
  IFSCachedAttributes(long accessDate, long creationDate, int fixedAttributes, 
                      long modificationDate, int objectType, long size, 
                      String name, String path, boolean isDirectory, boolean isFile) // @A1c
  {
    accessDate_ = accessDate;
    creationDate_ = creationDate;
    fixedAttributes_ = fixedAttributes;
    isDirectory_ = isDirectory;
    isFile_ = isFile;
    modificationDate_ = modificationDate;
    name_ =  name;
    objectType_ = objectType;
    path_ = path;
    size_ = size;
  }

/**
Return access date.
**/
  long getAccessDate()
  {
      return accessDate_;
  }

/**
Return creation date.
**/
  long getCreationDate()
  {
      return creationDate_;
  }

/**
Return fixed attributes.
**/
  int getFixedAttributes()
  {
      return fixedAttributes_;
  }

/**
Return isDir_
**/
  boolean getIsDirectory()
  {
      return isDirectory_;
  }

/**
Return fixed attributes.
**/
  boolean getIsFile()
  {
      return isFile_;
  }

/**
Return modification date.
**/
  long getModificationDate()
  {
      return modificationDate_;
  }

/**
Return name.
**/
  String getName()
  {
      return name_;
  }

/**
Return object type.
**/
  int getObjectType()
  {
      return objectType_;
  }

/**
Return path.
**/
  String getPath()
  {
      return path_;
  }

/**
Return size.
**/
  long getSize()     // @A1c
  {
      return size_;
  }
}
