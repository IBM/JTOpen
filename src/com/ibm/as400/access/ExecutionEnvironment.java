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

import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

class ExecutionEnvironment
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    

    // Don't allow instances of this class
    private ExecutionEnvironment()
    {
    }

    private static Properties localeToCcsid = new Properties();
    private static Properties localeToNlv = new Properties();
    private static Properties associatedDbcsCcsid = new Properties();
    static
    {
    localeToCcsid = new LocaleCcsidMap ();
    /* $A1
     try
     {
         // Attempt to load file through ClassLoader
         InputStream is = ExecutionEnvironment.class.getResourceAsStream("LocaleCcsidMap.properties");
         if (is == null)
         {
          // Attempt to load file through CLASSPATH
          localeToCcsid.load(ClassLoader.getSystemResourceAsStream("com/ibm/as400/access/LocaleCcsidMap.properties"));
         }
         else
         {
          localeToCcsid.load(is);
         }
     }
     catch (IOException e)
     {
         e.printStackTrace();
         Trace.log(Trace.ERROR, "Error reading LocaleCcsidMap.properties");
         throw new InternalErrorException(InternalErrorException.UNKNOWN);
     }
     */

    localeToNlv = new LocaleNlvMap ();
    /* $A1
     try
     {
         InputStream is = ExecutionEnvironment.class.getResourceAsStream("LocaleNlvMap.properties");
         if (is == null)
         {
          localeToNlv.load(ClassLoader.getSystemResourceAsStream("com/ibm/as400/access/LocaleNlvMap.properties"));
         }
         else
         {
          localeToNlv.load(is);
         }
     }
     catch (IOException e)
     {
         e.printStackTrace();
         Trace.log(Trace.ERROR, "Error reading LocaleNlvMap.properties");
         throw new InternalErrorException(InternalErrorException.UNKNOWN);
     }
     */

    associatedDbcsCcsid = new AssociatedDbcsCcsidMap ();
    /* $A1
     try
     {
         InputStream is = ExecutionEnvironment.class.getResourceAsStream("AssociatedDbcsCcsidMap.properties");
         if (is == null)
         {
          associatedDbcsCcsid.load(ClassLoader.getSystemResourceAsStream("com/ibm/as400/access/AssociatedDbcsCcsidMap.properties"));
         }
         else
         {
          associatedDbcsCcsid.load(is);
         }
     }
     catch (IOException e)
     {
         e.printStackTrace();
         Trace.log(Trace.ERROR, "Error reading AssociatedDbcsCcsidMap.properties");
         throw new InternalErrorException(InternalErrorException.UNKNOWN);
     }
     */
    }


    /**
     * Get the associated DBCS CCSID.
     * @return The CCSID.  Negative one is returned if there is no associated DBCS CCSID.
     **/
    static int getAssociatedDbcsCcsid(int ccsid)
    {
     String prop = associatedDbcsCcsid.getProperty(String.valueOf(ccsid));
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
         while (true)
         {
          String cstr = localeToCcsid.getProperty(lstr);
          if (cstr != null)
          {
              return Integer.parseInt(cstr);
          }
          lstr = lstr.substring(0, lstr.lastIndexOf('_'));
         }
     }
     catch (Exception e)
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
         while (true)
         {
          String cstr = localeToNlv.getProperty(lstr);
          if (cstr != null)
          {
              return cstr;
          }
          lstr = lstr.substring(0, lstr.lastIndexOf('_'));
         }
     }
     catch (Exception e)
     {
         // If all else fails return 2924
         return "2924";
     }
    }
}
