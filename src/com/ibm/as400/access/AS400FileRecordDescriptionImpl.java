///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400FileRecordDescriptionImpl.java
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
The AS400FileRecordDescriptionImpl interface defines the methods
needed for a full implementation of the AS400FileRecordDescription class.
**/
interface AS400FileRecordDescriptionImpl
{
  /**
   *Retrieves the file description for the file, and creates a file containing the Java source for
   *a class extending from RecordFormat that represents the record format for the file.  If the
   *file contains more than one record format (for example, is a multiple format logical file), a Java
   *source file for each record format in the file is created; each file will contain the class
   *definition for a single record format.<br>
   *The name of the class is the name of the record format retrieved with the string "Format"
   *appended to it.  The name of the file is the name of the class with the extension .java.<br>
   *The source files generated can be compiled and used as input to the
   *<a href="com.ibm.as400.access.AS400File.html#setRecordFormat">AS400File.setRecordFormat()</a> method.<br>
   *The AS/400 system to which to connect and the integrated file system
   *pathname for the file must be set prior to invoking this method.
   *@see AS400FileRecordDescription#AS400FileRecordDescription(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400FileRecordDescription#setPath
   *@see AS400FileRecordDescription#setSystem
   *@param filePath The path in which to create the file.  If <i>filePath</i> is null,
   *the file is created in the current working directory.
   *@param packageName The name of the package in which the class belongs. The <i>packageName</i>
   *is used to specify the package statement in the source code for the class.
   * If this value is null, no package statement is specified in the source code for the class.

   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   *@exception InterruptedException If this thread is interrupted.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
 
  **/
  public abstract String[] createRecordFormatSource(String packageName)
    throws AS400Exception,
           AS400SecurityException,
           IOException,
           InterruptedException;


  /**
   *Retrieves the file description for the file, and creates a RecordFormat
   *object for each record format, which can be used as input to the
   *<a href="com.ibm.as400.access.AS400File.html#setRecordFormat">AS400File.setRecordFormat()</a>
   *method.  If the file is a physical file, the RecordFormat array returned
   *contains one
   *RecordFormat object.  If the file is a multiple format logical file, the
   *RecordFormat array may contain
   *more than one RecordFormat object.
   *The AS/400 system to which to connect and the integrated file system
   *pathname for the file must be set prior to invoking this method.
   *@see AS400FileRecordDescription#AS400FileRecordDescription(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400FileRecordDescription#setPath
   *@see AS400FileRecordDescription#setSystem

   *@return The record format(s) for the file.

   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   *@exception InterruptedException If this thread is interrupted.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.

  **/
  public abstract RecordFormat[] retrieveRecordFormat()
    throws AS400Exception,
           AS400SecurityException,
           IOException,
           InterruptedException;


  /**
   *Sets the <a href="ipnpgmgd.html">integrated file system path name</a> for
   *the file.
   *@param name The <a href="ipnpgmgd.html">integrated file system path name</a>
   *of the file.  If a member is not specified in <i>name</i>, the first
   *member of the file is used.
  **/
  public abstract void setPath(String name);


  /**
   *Sets the system to which to connect.
   *@param system The system to which to conenct.
  **/
  public abstract void setSystem(AS400Impl system); //@B5C

}
