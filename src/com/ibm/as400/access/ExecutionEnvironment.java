///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ExecutionEnvironment.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Locale;

abstract class ExecutionEnvironment
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";
  
  
    /**
     * Get the associated DBCS CCSID.
     * @return The CCSID.  Negative one is returned if there is no associated DBCS CCSID.
     **/
    static int getAssociatedDbcsCcsid(int ccsid)
    {
     String prop = (String)ConversionMaps.associatedDbcsCcsidMap_.get(String.valueOf(ccsid));
     if (prop == null)
     {
         return -1;
     }
     return Integer.parseInt(prop);
    }


  /**
   * Get the "best guess" CCSID for the AS/400 based on the default locale.
   * @return The CCSID.
   **/
  static int getBestGuessAS400Ccsid()
  {
    try
    {
      String lstr = Locale.getDefault().toString();
      // Search from most specific to most general
      while(true)
      {
        //@B0D String cstr = localeToCcsid.getProperty(lstr);
        String cstr = (String)ConversionMaps.localeCcsidMap_.get(lstr); //@B0A
        if(cstr != null)
        {
          return Integer.parseInt(cstr);
        }
        lstr = lstr.substring(0, lstr.lastIndexOf('_'));
      }
    }
    catch(Exception e)
    {
      // If all else fails return 37
      return 37;
    }
  }

  
  /**
   * Get the CCSID for this execution environment.
   * @return The CCSID.
   **/
  static int getCcsid()
  {
    return 13488;  // Unicode
  }


  //@B0A
  /**
   * Get the corresponding ccsid number as a String.
  **/
  static int getCcsid(String encoding)
  {
    return Integer.parseInt((String)ConversionMaps.encodingCcsid_.get(encoding));
  }
  
  
  //@B0A
  /**
   * Get the corresponding encoding from a ccsid.
  **/
  static String getEncoding(int ccsid)
  {
    return (String)ConversionMaps.ccsidEncoding_.get(String.valueOf(ccsid));
  }
   
  
  /**
   * Get the NLV for this execution environment
   * @return String that represents the national language version
   **/
  static String getNlv()
  {
    try
    {
      String lstr = Locale.getDefault().toString();
      // Search from most specific to most general
      while(true)
      {
        //@B0D String cstr = localeToNlv.getProperty(lstr);
        String cstr = (String)ConversionMaps.localeNlvMap_.get(lstr); //@B0A
        if(cstr != null)
        {
          return cstr;
        }
        lstr = lstr.substring(0, lstr.lastIndexOf('_'));
      }
    }
    catch(Exception e)
    {
      // If all else fails return 2924
      return "2924";
    }
  }  
}
