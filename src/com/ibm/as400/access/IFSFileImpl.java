///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 Specifies the methods which the implementation objects for the IFSFile class
 need to support.
 **/
interface IFSFileImpl
{
  int  canRead0() throws IOException, AS400SecurityException;
  int  canWrite0() throws IOException, AS400SecurityException;
  long created0() throws IOException, AS400SecurityException;           //@D3a
  int  createNewFile() throws IOException, AS400SecurityException;
  int  delete0() throws IOException, AS400SecurityException;
  int  exists0() throws IOException, AS400SecurityException;

  long getFreeSpace() throws IOException;
  int getCCSID() throws IOException, AS400SecurityException;            //@A2a
  String getSubtype() throws IOException, AS400SecurityException;      //@B5a
  int isDirectory0() throws IOException, AS400SecurityException;
  int isFile0() throws IOException, AS400SecurityException;
  boolean isHidden()   throws IOException, AS400SecurityException;      //@D1a
  boolean isReadOnly() throws IOException, AS400SecurityException;      //@D1a

  long lastAccessed0() throws IOException, AS400SecurityException;      //@D3a
  long lastModified0() throws IOException, AS400SecurityException;
  long length0() throws IOException, AS400SecurityException;
  String[] listDirectoryContents(String directoryPath)
    throws IOException, AS400SecurityException;
  //@A1A Added this method which is similar to listDirectoryContents(), but returns
  //an IFSFile array.
  IFSCachedAttributes[] listDirectoryDetails(String directoryPath, 
                                             int maximumGetCount,       // @D4A
                                             String restartName)        // @D4A
    throws IOException, AS400SecurityException;
  int mkdir0(String directory) throws IOException, AS400SecurityException;
  int mkdirs0() throws IOException, AS400SecurityException;
  int renameTo0(IFSFileImpl file)
    throws IOException, AS400SecurityException;

  boolean setFixedAttributes(int attributes) throws IOException;         //@D1a
  boolean setHidden(boolean attribute) throws IOException;               //@D1a
  boolean setLastModified(long time) throws IOException;
  boolean setReadOnly(boolean attribute) throws IOException;             //@D1a
  void setPath(String path);
  void setSystem(AS400Impl system);

}

