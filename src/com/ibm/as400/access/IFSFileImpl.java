///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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
  int canRead0() throws IOException, AS400SecurityException;
  int canWrite0() throws IOException, AS400SecurityException;
  int delete0() throws IOException, AS400SecurityException;
  int exists0(String name) throws IOException, AS400SecurityException;

  long getFreeSpace() throws IOException;
  int isDirectory0() throws IOException, AS400SecurityException;
  int isFile0() throws IOException, AS400SecurityException;

  long lastModified0() throws IOException, AS400SecurityException;
  long length0() throws IOException, AS400SecurityException;
  String[] listDirectoryContents(String directoryPath)
    throws IOException, AS400SecurityException;
  int mkdir0(String directory) throws IOException, AS400SecurityException;
  int mkdirs0() throws IOException, AS400SecurityException;
  int renameTo0(IFSFileImpl file)
    throws IOException, AS400SecurityException;

  boolean setLastModified(long time) throws IOException;
  void setPath(String path);
  void setSystem(AS400Impl system);

}
