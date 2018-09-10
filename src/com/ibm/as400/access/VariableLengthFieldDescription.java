///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VariableLengthFieldDescription.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *The VariableLengthFieldDescription interface provides an interface
 *for FieldDescription subclasses that can contain
 *variable-length data.
**/
public interface VariableLengthFieldDescription
{
  /**
   *Indicates if the field is a variable-length field.  
   *@return true if the field is a variable-length field; false otherwise.
  **/
  public boolean isVariableLength();

  /**
   *Sets the value that indicates if the field is a variable-length field.
   *@param value true if the field is a variable-length field; false otherwise.
  **/
  public void setVariableLength(boolean value);
}
