///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ArrayFieldDescription.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

/**
 *The  ArrayFieldDescription class represents the description of an array of data.
 *It allows:
 *<ul>
 *<li>The user to describe an array of server data.
 *<li>The Java program to describe an array of server data to the user.
 *</ul>
**/
public class ArrayFieldDescription extends FieldDescription implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;


  /**
   *Constructs an ArrayFieldDescription object.
  **/
  public ArrayFieldDescription()
  {
  }

  /**
   *Constructs an ArrayFieldDescription object. It uses the specified data type
   *and name of the field.
   *@param dataType Describes the field and provides
   *                the conversion capability for the contents of the field.
   *@param name The name of the field.
  **/
  public ArrayFieldDescription(AS400Array dataType, String name)
  {
    super(dataType, name);
  }

  
  /**
   *Returns the DDS description for the field.
   *@return Always null since there is no DDS type corresponding to an array.
  **/
  String[] getDDSDescription()
  {
    return null;
  }

  /**
   *Sets the AS400DataType object describing this field.
   *@param dataType The AS400DataType that describes this field.
  **/
  public void setDataType(AS400Array dataType)
  {
    // Verify parameters
    if (dataType == null)
    {
      throw new NullPointerException("dataType");
    }
    dataType_ = dataType;
    // Set the length of the field based on the data type
    length_ = dataType.getByteLength();
  }
}
