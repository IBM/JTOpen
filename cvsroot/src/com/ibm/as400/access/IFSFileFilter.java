///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileFilter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
The IFSFileFilter interface provides an interface for filtering files.
It can be used to filter directory listings in the list method of the class IFSFile.
**/
public interface IFSFileFilter 
{
 /**
    Tests if a specified file should be in a file list.
 **/ 
  public abstract boolean accept(IFSFile file);
}




