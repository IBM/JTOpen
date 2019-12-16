///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileRecordDescriptionListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *The AS400FileRecordDescriptionListener interface provides the interface that
 *must be implemented to handle AS400FileRecordDescriptionEvent objects that are
 *fired by the {@link com.ibm.as400.access.AS400FileRecordDescription AS400FileRecordDescription}
 *class.
**/
public interface AS400FileRecordDescriptionListener extends java.util.EventListener
{
  /**
   *Invoked when the record formats for a file have been retrieved.
   *@param event The event fired.
   *@see com.ibm.as400.access.AS400FileRecordDescription#retrieveRecordFormat
  **/
  public void recordFormatRetrieved(AS400FileRecordDescriptionEvent event);

  /**
   *Invoked when the record format source code files for a file have been created.
   *@see com.ibm.as400.access.AS400FileRecordDescription#createRecordFormatSource
  **/
  public void recordFormatSourceCreated(AS400FileRecordDescriptionEvent event);
}
