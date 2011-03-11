///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AboutToolbox.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
//
// AboutToolbox.  This program displays the Version, Release, and PTF 
// level of the user's IBM Toolbox for Java.
//
// This program will only report version information if the Toolbox is
// Modification 2 with PTF SF57202, or a later modification,
// otherwise a message is displayed informing the user why the version
// cannot be retrieved.
//
// Command syntax:
//    java utilities.AboutToolbox
//
//////////////////////////////////////////////////////////////////////////

package utilities;

import java.lang.reflect.*;

import com.ibm.as400.access.AS400JDBCDriver;

/**
 * Prints the current version of the Toolbox that is found in the user's CLASSPATH.
 * <P>
 * Syntax: java utilities.AboutToolbox
 * @see java.lang.Package
 * @see com.ibm.as400.access.Copyright
 **/
public class AboutToolbox
{
  public static void main(String args[]) 
  {
    String versionInfo = getVersionDescription();
    System.out.println(versionInfo);
  }

  /**
   Returns version information about the Toolbox.
   @return Version information about the Toolbox.
   **/
  public static String getVersionDescription() 
  {
    StringBuffer sbuf = new StringBuffer(200);
    sbuf.append("\nIBM Toolbox for Java:\n");

    try 
    {
      Class copyright = Class.forName("com.ibm.as400.access.Copyright");
      Field version = copyright.getDeclaredField("version");

      // Running with mod2 (with 2nd PTF) or later.
      sbuf.append(version.get(null));

      // JDBC version designators were added after JTOpen 6.1.
      try
      {
        Class driver = Class.forName("com.ibm.as400.access.AS400JDBCDriver");
        Field majorVersion = driver.getDeclaredField("JDBC_MAJOR_VERSION_");
        Field minorVersion = driver.getDeclaredField("JDBC_MINOR_VERSION_");
        sbuf.append("\nSupports JDBC version " +
                    majorVersion.getInt(null) + "." +
                    minorVersion.getInt(null));
      }
      catch(NoSuchFieldException e)
      {  
        // We're running JTOpen 6.1 or earlier, so JDBC 4.0 not supported yet.
      }

      try{
        //driver level
        AS400JDBCDriver d = new AS400JDBCDriver();
        int driverMajor = d.getMajorVersion();
        int driverMinor = d.getMinorVersion();

        sbuf.append("\nToolbox driver version " + driverMajor  + "." + driverMinor );
      }catch(Throwable t)
      {  
        // Skip if get any error
      }

    }
    catch(NoSuchFieldException e)
    {  
      // Running with an older version of Toolbox.
      sbuf.append("\nYour version of IBM Toolbox for Java is either:");
      sbuf.append("\n  - Modification 0,");
      sbuf.append("\n  - Modification 1, or");
      sbuf.append("\n  - Modification 2 without PTF SF57202\n");
      sbuf.append("\nIn order for \"AboutToolbox\" to more precisely determine your level " +
                  "of the Toolbox, you need to have at least Toolbox Modification 2 " +
                  "with PTF SF57202 or later.");
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      sbuf.append("\nUnexpected error occurred: " + e);
    }

    return sbuf.toString();
  }

}
