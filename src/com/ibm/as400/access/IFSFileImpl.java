///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2007 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @D7 - 07/25/2007 - Add allowSortedRequests to the listDirectoryDetails()
//                    method to resolve problem of issuing PWFS List Attributes 
//                    request with both "Sort" indication and "RestartByID" 
//                    which is documented to be an invalid combination.
// @D8 - 04/03/2008 - Add clearCachedAttributes() to clear impl cache attributes. 
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
  int  canRead() throws IOException, AS400SecurityException;
  int  canWrite() throws IOException, AS400SecurityException;
  void clearCachedAttributes();                                        //@D8A
  boolean copyTo(String path, boolean replace) throws IOException, AS400SecurityException, ObjectAlreadyExistsException;
  long created() throws IOException, AS400SecurityException;           //@D3a
  int  createNewFile() throws IOException, AS400SecurityException;
  int  delete() throws IOException, AS400SecurityException;
  int  exists() throws IOException, AS400SecurityException;

  long getFreeSpace() throws IOException, AS400SecurityException;
  int getCCSID() throws IOException, AS400SecurityException;            //@A2a
  String getOwnerName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException;
  long getOwnerUID()  throws IOException, AS400SecurityException;       //@B7a @C0c
  String getSubtype() throws IOException, AS400SecurityException;      //@B5a
  int isDirectory() throws IOException, AS400SecurityException;
  int isFile() throws IOException, AS400SecurityException;
  boolean isHidden()   throws IOException, AS400SecurityException;      //@D1a
  boolean isReadOnly() throws IOException, AS400SecurityException;      //@D1a
  boolean isSymbolicLink() throws IOException, AS400SecurityException;

  long lastAccessed() throws IOException, AS400SecurityException;      //@D3a
  long lastModified() throws IOException, AS400SecurityException;
  long length() throws IOException, AS400SecurityException;
  String[] listDirectoryContents(String directoryPath)
    throws IOException, AS400SecurityException;
  //@A1A Added this method which is similar to listDirectoryContents(), but returns
  //an IFSFile array.
  IFSCachedAttributes[] listDirectoryDetails(String directoryPattern,
                                             String directoryPath, 
                                             int maximumGetCount,       // @D4A
                                             String restartName)        // @D4A
    throws IOException, AS400SecurityException;
  //@C3a
  IFSCachedAttributes[] listDirectoryDetails(String directoryPattern,
                                             String directoryPath, 
                                             int maximumGetCount,
                                             byte[] restartID,            //@D7C
                                             boolean allowSortedRequests) //@D7A
    throws IOException, AS400SecurityException;
  int mkdir(String directory) throws IOException, AS400SecurityException;
  int mkdirs() throws IOException, AS400SecurityException;
  int renameTo(IFSFileImpl file)
    throws IOException, AS400SecurityException;

  boolean setCCSID(int ccsid) throws IOException, AS400SecurityException;
  boolean setFixedAttributes(int attributes) throws IOException;         //@D1a
  boolean setHidden(boolean attribute) throws IOException;               //@D1a
  boolean setLastModified(long time) throws IOException;
  boolean setLength(int length) throws IOException, AS400SecurityException;    //@B8a
  void setPatternMatching(int patternMatching);
  boolean setReadOnly(boolean attribute) throws IOException;             //@D1a
  void setPath(String path);
  void setSorted(boolean sort);
  void setSystem(AS400Impl system);

}

