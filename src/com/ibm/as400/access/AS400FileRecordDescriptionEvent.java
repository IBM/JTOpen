///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileRecordDescriptionEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *The AS400FileRecordDescriptionEvent class represents a RecordDescriptionEvent.  This class is used to
 *fire events from the record description classes,
 *<a href="RecordFormat.html">RecordFormat</a> and
 *<a href="RecordFormat.html">Record</a>.
**/
public class AS400FileRecordDescriptionEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



  /**
   *Event ID indicating that the record formats for a file
   *have been retrieved.
   *@see com.ibm.as400.access.AS400FileRecordDescription#retrieveRecordFormat
  **/
  public static final int RECORD_FORMAT_RETRIEVED = 1;
  /**
   *Event ID indicating that source files containing the RecordFormat
   *classes for a file have been created.
   *@see com.ibm.as400.access.AS400FileRecordDescription#createRecordFormatSource
  **/
  public static final int RECORD_FORMAT_SOURCE_CREATED = 2;

  // Event identifier
  private int id_;

  /**
   *Constructs a AS400FileRecordDescriptionEvent object. It uses the specified
   *source and ID.
   *@param source The object where the event originated.
   *@param id The event identifier.  The <i>id</i> must
   *be a valid event id for this class.
  **/
  public AS400FileRecordDescriptionEvent(Object source, int id)
  {
    super(source);
    if (id < RECORD_FORMAT_RETRIEVED || id > RECORD_FORMAT_SOURCE_CREATED)
    {
      throw new ExtendedIllegalArgumentException("id",
                ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    id_ = id;
  }


  /**
   *Returns the identifier for this event.
   *@return The identifier for this event.
  **/
  public int getID()
  {
    return id_;
  }
}
