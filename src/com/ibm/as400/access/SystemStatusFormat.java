///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SystemStatusFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;

/**
The SystemStatusFormat class is the parent class for the different
types of formats on the QWCRSSTS API.
**/
class SystemStatusFormat extends RecordFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  static AS400Bin4 bin4 = new AS400Bin4();
  AS400 system_;
  
  SystemStatusFormat(AS400 sys)
  {
    system_ = sys;
    addBin4("numberOfBytesAvailable");
    addBin4("numberOfBytesReturned");
    addFieldDescription(new HexFieldDescription(new AS400ByteArray(8), "currentDateAndTime"));
    addChar(8, "systemName");
  }
  
  /**
   * Adds a binary field description to this format.
  **/
  void addBin4(String name)
  {
    addFieldDescription(new BinaryFieldDescription(bin4, name));
  }
  
  /**
   * Adds a character field description to this format.
  **/
  void addChar(int length, String name)
  {
    addFieldDescription(new CharacterFieldDescription(new AS400Text(length, system_.getCcsid(), system_), name));
  }
  
  /**
   * Sets the name of this format.
  **/
  public void setName(String name)
  {
    try
    {
      super.setName(name);
    }
    catch(PropertyVetoException e)
    {
      // Quiet the compiler.
    }
  }
}

  
