///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordDescriptionListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *The RecordDescriptionListener interface provides the interface that
 *must be implemented to handle RecordDescriptionEvent objects that are
 *fired by the record description classes,
 *{@link com.ibm.as400.access.RecordFormat RecordFormat} and
 *{@link com.ibm.as400.access.Record Record}.
**/
public interface RecordDescriptionListener extends java.util.EventListener
{
  /**
   *Invoked when a field description has been added to a RecordFormat object.
   *@param event The event fired.
   *@see com.ibm.as400.access.RecordFormat#addFieldDescription
  **/
  public void fieldDescriptionAdded(RecordDescriptionEvent event);

  /**
   *Invoked when a field value has been changed in a Record object.
   *@param event The event fired.
   *@see com.ibm.as400.access.Record#setField
   *@see com.ibm.as400.access.Record#setContents
  **/
  public void fieldModified(RecordDescriptionEvent event);

  /**
   *Invoked when a key field description has been added to a RecordFormat
   *object.
   *@param event The event fired.
   *@see com.ibm.as400.access.RecordFormat#addKeyFieldDescription
  **/
  public void keyFieldDescriptionAdded(RecordDescriptionEvent event);
}
