///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ProductLicenseEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The ProductLiceneEvent class represents a ProductLicense event.
**/

public class ProductLicenseEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  /**
   The product license released event ID.  This event is delivered when a product license is released.
   **/
  public static final int PRODUCT_LICENSE_RELEASED = 0;

  /**
   The product license requested event ID.  This event is delivered when a product license is requested.
   **/
  public static final int PRODUCT_LICENSE_REQUESTED = 1;


  private int id_; // event identifier


  /**
   Constructs a ProductLiceneEvent object. It uses the specified source and ID.
   @param source The object where the event originated.
   @param id The event identifier.
   **/
  public ProductLicenseEvent(Object source,
                             int    id)
  {
    super(source);

    if (id < PRODUCT_LICENSE_RELEASED || id > PRODUCT_LICENSE_REQUESTED)
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




