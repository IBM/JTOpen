///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UpdateACSJar.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2018 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util; 

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import com.ibm.as400.access.AS400JDBCDriver;
import com.ibm.as400.access.Copyright;


public class UpdateACSJar    {
  static final String copyright = "Copyright (C) 2018 International Business Machines Corporation and others.";
  
    public static void main(String args[]) {
      try {
        System.out.println("Usage: java -cp jt400.jar com.ibm.as400.util.UpdateACSJar <acsbundle.jar>"); 
        System.out.println(" This program updates acsbundle.jar with the current jt400.jar file.");
        System.out.println(" If acsbundle.jar is not specified, the default locations of acsbundle.jar are used. ");
        System.out.println(" This is provided so that ACS Run SQL Scripts can utilize the latest features of JTOpen.");
        System.out.println(" ");
        System.out.println(" Note:  Official support for ACS is not available if this tool is used since ");
        System.out.println("   official testing has not been done on the resultant combination.");
        System.out.println("   If you find ACS does not work after the update, delete acsbundle.jar and replace it "); 
        System.out.println("   with the backup file that was created by this program. ");
        System.out.println(""); 
        System.out.println(" Please report problems with jt400.jar on the JTOpen bug forum: https://sourceforge.net/p/jt400/bugs/ ");
        System.out.println(""); 
        System.out.println(" This version creates an enableClientAffinitiesList jdbc configuration to use the new "); 
        System.out.println(" ClientAffinities property."); 
        System.out.println(""); 
        
        
         String version = Copyright.version; 
         String acsJar = null; 
         if (args.length > 0) {
           acsJar = args[0]; 
         }
         File newJt400JarFile = locateCurrentJt400JarFile(); 
         System.out.println("Updating acsbundle.jar"); 
         updateAcsBundle(newJt400JarFile, acsJar); 
         System.out.println("Checking jdbc configuration"); 
         createEnableClientAffinitiesList();     
         
         System.out.println("Tool ended successfully with acsbundle.jar updated with the following jar.");
         System.out.println("   "+ version);
         System.out.println("To verify jt400.jar version, start ACS, then RSS, then run the query"); 
         System.out.println("values {fn jtopeninfo()};");
         System.out.println("The returned information should match the version information above"); 
         
      } catch (Exception e) {
        e.printStackTrace(System.out); 
      }

    }


    
    
  private static File locateCurrentJt400JarFile() throws Exception {

    if (AS400JDBCDriver.JDBC_MAJOR_VERSION_ == 4
        && AS400JDBCDriver.JDBC_MINOR_VERSION_ == 0) {
      String loadPath = "unknown";
      ClassLoader loader = UpdateACSJar.class.getClassLoader();
      if (loader != null) {
        String resourceName = UpdateACSJar.class.getName().replace('.', '/')
            + ".class";
        java.net.URL resourceUrl = loader.getResource(resourceName);
        if (resourceUrl != null) {
          loadPath = resourceUrl.getPath();
          if (loadPath.indexOf("file:") == 0) {
            loadPath = loadPath.substring(5); 
            int exclIndex = loadPath.indexOf('!');
            if (exclIndex > 0) {
              loadPath = loadPath.substring(0, exclIndex);
            }
            // Make sure load path is a jar file
            if (loadPath.endsWith(".jar")) {
              return new File(loadPath);
            } else {
              throw new Exception("Resource " + loadPath + " is not jar file ");
            }
          } else {
            throw new Exception("Rosource " + loadPath + " is not a file");
          }
        } else {
          throw new Exception("Unable to find resourceUrl for " + resourceName);
        }
      } else {
        throw new Exception("Unable to find classloader for "
            + UpdateACSJar.class.toString());
      }

    } else {
      throw new Exception("Current jar file is JDBC version "
          + AS400JDBCDriver.JDBC_MAJOR_VERSION_ + "."
          + AS400JDBCDriver.JDBC_MINOR_VERSION_
          + ".  Should be JDBC 4.0 (java6)");
    }
  }

  private static void createEnableClientAffinitiesList() throws Exception {
    File jdbcConfigFile = getJdbcConfigFile();
    if (jdbcConfigFile.exists()) {
      System.out.println("jdbc configuration '" + jdbcConfigFile.toString()
          + "' already exists and was not updated.");
    } else {
      PrintWriter pw = new PrintWriter(new FileWriter(jdbcConfigFile));
      pw.println("# Created by com.ibm.as400.access.util.UpdateACSJar");
      pw.println("naming=sql");
      pw.println("time\\ format=iso");
      pw.println("date\\ format=iso");
      pw.println("enableClientAffinitiesList=1");
      pw.println("maxRetriesForClientReroute=10");
      pw.println("retryIntervalForClientReroute=6");
      pw.close(); 
      System.out.println("Create jdbc configuration '" + jdbcConfigFile.toString()
          + "'.");
    }
  }

    private static File getJdbcConfigFile() throws Exception {
      
      String defaultLocation = System.getProperty("user.home")
          + "\\Documents\\IBM\\iAccessClient\\RunSQLScripts\\JDBC";
      
      if (java.lang.System.getProperty("os.name").indexOf("Mac")>=0 ) {
         defaultLocation = System.getProperty("user.home") + "/IBM/iAccessClient/RunSQLScripts/JDBC";
      }
      
      File checkLocation = new File(defaultLocation); 
      if (!checkLocation.exists()) {
        // The location does not exist if RSS has not been used.  Create it. 
        System.out.println("Creating JDBC configuration location at "+defaultLocation); 
        checkLocation.mkdir(); 
      }
      if (!checkLocation.isDirectory()) {
        throw new Exception("JDBC configuration location '"+defaultLocation+"' is not a directory."); 
      }
      return new File(checkLocation+File.separator+"enableClientAffinitiesList.jdbc"); 
    }

  private static void updateAcsBundle(File newJT400JarFile, String acsJar) throws Exception {
    //
    
    File[] acsBundles;
    if (acsJar == null) { 
      acsBundles = findAcsBundles();
    } else {
      File acsBundle = new File(acsJar); 
      if (! acsBundle.exists()) {
        throw new Exception("Error "+acsJar+" does not exist"); 
      } else {
        acsBundles = new File[1]; 
        acsBundles[0]  = acsBundle; 
      }
    }
    
    for (int i = 0; i < acsBundles.length; i++) {
      File acsBundle = acsBundles[i];
      if (acsBundle != null) {
        System.out.println("Updating "+acsBundle.getPath()+" with "+newJT400JarFile.getPath()); 
        File oldBundle = backupBundle(acsBundle);
        JarInputStream jis = new JarInputStream(new FileInputStream(oldBundle));

        byte[] buffer = new byte[4096];
        Manifest manifest = jis.getManifest();
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(
            acsBundle), manifest);

        JarEntry entry = jis.getNextJarEntry();
        while (entry != null) {
          String name = entry.getName();
          if (name.equals("lib/jt400.jar")) {
            // System.out.println("Found jt400.jar");
            // Skipping to add it later

          } else {
            jos.putNextEntry(new JarEntry(name));
            int len;
            while ((len = jis.read(buffer)) > 0) {
              jos.write(buffer, 0, len);
            }
          }
          entry = jis.getNextJarEntry();
        }
        jis.close();

        InputStream in = new FileInputStream(newJT400JarFile);
        jos.putNextEntry(new JarEntry("lib/jt400.jar"));
        int len;
        while ((len = in.read(buffer)) > 0) {
          jos.write(buffer, 0, len);
        }
        in.close();
        jos.closeEntry();

        jos.close();
      }
    }

  }

    private static File backupBundle(File acsBundle) throws Exception {
      String filename = acsBundle.getAbsolutePath();
      File oldFile = new File(filename+"."+currentDateString());
        
      if (oldFile.exists()) {
        oldFile.delete(); 
      }
      
      boolean renameWorked = acsBundle.renameTo(oldFile); 
      if (!renameWorked) {
        throw new Exception("\n  Unable to rename "+filename+" to "+oldFile+".\n  Have all instances of ACS and RSS been stopped?"); 
      } else {
        System.out.println(" Renamed old acsbundle.jar to "+oldFile); 
      }
      
      return oldFile; 
    }

    private static String currentDateString() {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
      return sdf.format(new Date()); 
    }

    /* Returns an array of possible file.  If a array entry is null */
    /* that means that the file was not found */ 
    /* This throws an exception if a bundle cannot be found */ 
  private static File[] findAcsBundles() throws Exception {

    String[] possibleLocations = {
        "C:\\Users\\Public\\IBM\\ClientSolutions\\acsbundle.jar",
        System.getProperty("user.home")  + "\\IBM\\ClientSolutions\\acsbundle.jar", 
        "/Applications/IBM i Access Client Solutions.app/acsbundle.jar", 
    };
    boolean found = false;
    File[] acsBundles = new File[possibleLocations.length];
    for (int i = 0; i < possibleLocations.length; i++) {
      File bundleFile = new File(possibleLocations[i]);
      if (bundleFile.exists()) {
        acsBundles[i] = bundleFile;
        found = true;
      } /* if exists */
    } /* for i */
    if (!found) {
      System.out.println("ERROR:  Could not find bundle file");
      for (int i = 0; i < possibleLocations.length; i++) {
        System.out.println("..Looked for " + possibleLocations[i]);
      }
      throw new Exception("Could not find bundle file.");
    }
    return acsBundles;
  }
}
