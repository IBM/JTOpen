///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobFormat.java
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
The JobFormat class is the parent class for the different
types of formats on the QUSRJOBI API.
**/
class JobFormat extends RecordFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  static AS400Bin4 bin4 = new AS400Bin4();
  
  private AS400 system_;
  
  JobFormat(AS400 sys)
  {
    system_ = sys;
    addBin4("numberOfBytesReturned");
    addBin4("numberOfBytesAvailable");
    addChar(10, "jobName");
    addChar(10, "userName");
    addChar(6, "jobNumber");
    addChar(16, "internalJobIdentifier");
    addChar(10, "jobStatus");
    addChar(1, "jobType");
    addChar(1, "jobSubtype");
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
      // Ignore.
    }
  }
}

  
