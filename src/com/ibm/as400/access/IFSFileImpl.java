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
  boolean canExecute() throws IOException, AS400SecurityException;
  boolean  canRead() throws IOException, AS400SecurityException;
  boolean  canWrite() throws IOException, AS400SecurityException;
  void clearCachedAttributes();                                        //@D8A
  boolean copyTo(String path, boolean replace) throws IOException, AS400SecurityException, ObjectAlreadyExistsException;
  long created() throws IOException, AS400SecurityException;           //@D3a
  int  createNewFile() throws IOException, AS400SecurityException;
  int  delete() throws IOException, AS400SecurityException;
  int  exists() throws IOException, AS400SecurityException;

  long getAvailableSpace(boolean forUserOnly) throws IOException, AS400SecurityException;
  long getTotalSpace(boolean forUserOnly) throws IOException, AS400SecurityException;
  int getCCSID() throws IOException, AS400SecurityException;
  int getCCSIDByUserHandle() throws IOException, AS400SecurityException;            //@A2a //@SCc //@V4A
  String getOwnerName() throws IOException, AS400SecurityException;
  String getOwnerNameByUserHandle() throws IOException, AS400SecurityException; //@SCc //@V4A
  String getOwnerNameByUserHandle(boolean forceRetrieve) throws IOException, AS400SecurityException; //@AC7 
  int getASP() throws IOException, AS400SecurityException;//@RDA @SAD
  String getFileSystemType()throws IOException, AS400SecurityException;//@SAA //@V4A
  long getOwnerUID()  throws IOException, AS400SecurityException;       //@B7a @C0c
  String getPathPointedTo() throws IOException, AS400SecurityException;
  String getSubtype() throws IOException, AS400SecurityException;      //@B5a
  int isDirectory() throws IOException, AS400SecurityException;
  int isFile() throws IOException, AS400SecurityException;
  boolean isHidden()   throws IOException, AS400SecurityException;      //@D1a
  boolean isReadOnly() throws IOException, AS400SecurityException;      //@D1a
  boolean isSourcePhysicalFile() throws IOException, AS400SecurityException, AS400Exception;
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
  boolean setAccess(int accessType, boolean enableAccess, boolean ownerOnly) throws IOException, AS400SecurityException;
  boolean setFixedAttributes(int attributes) throws IOException, AS400SecurityException;         //@D1a
  boolean setHidden(boolean attribute) throws IOException, AS400SecurityException;               //@D1a
  boolean setLastModified(long time) throws IOException, AS400SecurityException;
  boolean setLength(int length) throws IOException, AS400SecurityException;    //@B8a
  void setPatternMatching(int patternMatching);
  boolean setReadOnly(boolean attribute) throws IOException, AS400SecurityException;             //@D1a
  void setPath(String path);
  void setSorted(boolean sort);
  void setSystem(AS400Impl system);
  //@AC7 Start
  String getFileSystemType(boolean retrieveAll)throws IOException, AS400SecurityException;
  int getASP(boolean retrieveAll) throws IOException, AS400SecurityException;
  int getCCSID(boolean retrieveAll) throws IOException, AS400SecurityException;
  String getOwnerName(boolean retrieveAll) throws IOException, AS400SecurityException;
  //@AC7 End

}

