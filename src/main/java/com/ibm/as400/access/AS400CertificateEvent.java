///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The AS400CertificateEvent class represents an AS400Certificate event.
**/

public class AS400CertificateEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  /**
   The AS400Certificate added event ID.  This event is delivered when a certificate has been added.
   **/
  public static final int CERTIFICATE_ADDED = 0;

  /**
   The AS400Certificate deleted event ID.  This event is delivered when a certificate has been deleted.
   **/
  public static final int CERTIFICATE_DELETED = 1;


  private int id_; // event identifier


  /**
   Constructs an AS400CertificateEvent object.
   @param source The object where the event originated.
   @param id The event identifier.
   **/
  public AS400CertificateEvent(Object source,
                      int    id)
  {
    super(source);

    if (id < CERTIFICATE_ADDED || id > CERTIFICATE_DELETED)
    {
      throw new ExtendedIllegalArgumentException("id", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    id_ = id;
  }

  /**
   Returns the identifier for this event.
   @return The identifier for this event.
   **/
  public int getID()
  {
    return id_;
  }
}




