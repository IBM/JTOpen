///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordDescriptionEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *The RecordDescriptionEvent class represents a RecordDescriptionEvent.  This class is used to
 *fire events from the record description classes,
 *{@link com.ibm.as400.access.RecordFormat RecordFormat} and
 *{@link com.ibm.as400.access.Record Record}
 *to listeners that have implemented the
 *{@link com.ibm.as400.access.RecordDescriptionListener RecordDescriptionListener}
 *interface.
**/
public class RecordDescriptionEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  /**
   *Event id indicating that a field description has been added
   *to a RecordFormat object.
   *@see com.ibm.as400.access.RecordFormat#addFieldDescription
  **/
  public static final int FIELD_DESCRIPTION_ADDED = 1;
  /**
   *Event id indicating that a key field description has been added
   *to a RecordFormat object.
   *@see com.ibm.as400.access.RecordFormat#addKeyFieldDescription
  **/
  public static final int KEY_FIELD_DESCRIPTION_ADDED = 2;
  /**
   *Event id indicating that a field value has been modified
   *in a Record object.
   *@see com.ibm.as400.access.Record#setField
   *@see com.ibm.as400.access.Record#setContents
  **/
  public static final int FIELD_MODIFIED = 3;

  // Event identifier
  private int id_;

  /**
   *Constructs a RecordDescriptionEvent object. It uses the
   *source and ID specified.
   *@param source The object where the event originated.
   *@param id The event identifier.  The <i>id</i> must
   *be a valid event id for this class.
  **/
  public RecordDescriptionEvent(Object source, int id)
  {
    super(source);
    if (id < FIELD_DESCRIPTION_ADDED || id > FIELD_MODIFIED)
    {
      throw new ExtendedIllegalArgumentException("id",
                ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    id_ = id;
  }


  /**
   *Returns the identifier for this event.
   *@return The ID.
  **/
  public int getID()
  {
    return id_;
  }
}
