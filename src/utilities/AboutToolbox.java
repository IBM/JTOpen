//////////////////////////////////////////////////////////////////////////
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
//
// This code have not been thoroughly tested under all conditions.
// IBM, therefore, cannot guarantee or imply reliability, serviceability,
// or function of these programs.
//
// All programs contained herein are provided to you "AS IS" without any
// warranties of any kind. The implied warranties of merchantability and
// fitness for a particular purpose are expressly disclaimed.
//
// IBM Toolbox for Java
// (C) Copyright IBM Corp. 1999
// All rights reserved.
// US Government Users Restricted Rights -
// Use, duplication, or disclosure restricted
// by GSA ADP Schedule Contract with IBM Corp.
//
//////////////////////////////////////////////////////////////////////////
package utilities;

import java.lang.reflect.*;

public class AboutToolbox
{
   public static void main(String args[]) 
   {
      System.out.println("\n" + "IBM Toolbox for Java:\n");
      try 
      {
         Class copyright = Class.forName("com.ibm.as400.access.Copyright");
         Field version = copyright.getDeclaredField("version");

         //Runnning with mod2 (with 2nd PTF) or later.
         System.out.println(version.get(null));
      }
      catch(NoSuchFieldException e)
      {  
         //Running with an older version of Toolbox
         System.out.println("Your version of IBM Toolbox for Java is either:");
         System.out.println("  - Modification 0,");
         System.out.println("  - Modification 1, or");
         System.out.println("  - Modification 2 without PTF SF57202\n");
         System.out.println("In order for \"AboutToolbox\" to more precisely determine your level " +
                            "of the Toolbox, you need to have at least Toolbox Modification 2 " +
                            "with PTF SF57202 or later.");
      }
      catch(Exception e)
      {  e.printStackTrace();
         System.out.println("Unexpected Error Occurred: " + e);
      }
   }
}
