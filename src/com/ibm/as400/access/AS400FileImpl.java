///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400FileImpl.java
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
This interface is provided so that the original AS400FileImpl, which has
been renamed to AS400FileImplBase, can exist completely on the proxy server,
so as not to inflate the proxy client JAR file.
**/
interface AS400FileImpl
{

  /**
   *Returns any explicit locks that have been obtained for this file.
   *Any locks that have been obtained through the lock(int) method are returned.
   *@see AS400File#lock
   *@return The explicit file locks held for this file.
   *        Possible lock values are:
   *        <ul>
   *        <li>READ_EXCLUSIVE_LOCK
   *        <li>READ_ALLOW_SHARED_READ_LOCK
   *        <li>READ_ALLOW_SHARED_WRITE_LOCK
   *        <li>WRITE_EXCLUSIVE_LOCK
   *        <li>WRITE_ALLOW_SHARED_READ_LOCK
   *        <li>WRITE_ALLOW_SHARED_WRITE_LOCK
   *        </ul>
   *If no explicit locks have been obtained for the file, an array of size zero
   *is returned.
   **/
  int[] getExplicitLocks();

  String[] openFile2(int openType, int bf, int level, boolean access)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException;

  /* @A0: Proxy support - the "doIt" methods
     Since there are so many methods in the AS400File, KeyedFile,
     and SequentialFile classes that needed to be proxified, I found
     it to be more efficient to consolidate those methods that
     1) threw the same exceptions and
     2) were of the same return type.
     This had the effect of reducing the number of proxified methods
     by a large amount, thereby reducing the size of the .class files.
     The doIt() methods take a methodName as a parameter and use
     core reflection in AS400FileImplBase to invoke that method, in 
     the same way that the actual Proxy classes do.
     
     While it is true that the logic required to do the core reflection
     increases class file size, that code does not exist in the proxy
     jar file, yielding the benefit of reduced class file size due to
     the reduction in methods for the proxy jar file. 
  */
  void doIt(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException;
  
  void doItNoExceptions(String methodName, Class[] classes, Object[] objects);
  
  Record doItRecord(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException;
  
  Record[] doItRecordArray(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException;
  
  RecordFormat doItRecordFormat(String methodName, Class[] classes, Object[] objects) //@B2A
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException;          //@B2A
  
  int doItInt(String methodName);
  
  boolean doItBoolean(String methodName);
}




