///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400ToolboxInstaller.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package utilities;

import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
/**
 * <p>The AS400ToolboxInstaller class is used to
 * install, update, compare, and uninstall the IBM Toolbox for Java
 * packages.  Note that this class writes to the local file system,
 * so it may fail if used in an applet.
 *
 * <p>The AS400ToolboxInstaller class can be included in the user's program,
 * or it can be run as a command line program, as follows:
 * <blockquote>
 * <pre>
 * <strong>java utilities.AS400ToolboxInstaller</strong> [ options ]
 * </pre>
 * </blockquote>
 *
 * <p>
 * <b><a name="inoptions">Options</a></b>
 *
 * <p>
 * <dl>
 * <dt><b><code>-install</code></b>
 * <dd>Indicates that the specified packages are to be installed.
 *     If the -package option is not specified, all packages will be installed.
 *     The source and target options must be specified when using this option.
 *     The -install option may be abbreviated to -i.
 *
 * <dt><b><code>-uninstall</code></b>
 * <dd>Indicates that the specified packages are to be removed.
 *     The package and target options must be specified when using this option.
 *     The -uninstall option may be abbreviated to -u.
 *
 * <dt><b><code>-compare</code></b>
 * <dd>Indicates that the source package with be compared with the
 *     target package to determine if
 *     an update is needed. The package, source, and target options must
 *     be specified when using this option.
 *     The -compare option may be abbreviated to -c.
 *
 *
 * <dt><b><code>-package</code></b> <var>package1[,package2[, ...]]</var>
 * <dd>Specifies the package to install, compare, or uninstall.
 *     If -package is specified, at
 *     least one package name must be specified.
 *     The -package option may be abbreviated to -p.
 *
 *
 * <dt><b><code>-source</code></b> <var>sourceURL</var>
 * <dd>Specifies the location of the source files. The HTTP server is used
 *     to access the files.  The system name or the fully-qualified URL
 *     may be specified. If the system name is specified, it will automatically
 *     be converted to the URL where the licensed program installed the files.
 *     For example, if mySystem is specified, http://mySystem/QIBM/ProdData/HTTP/Public/jt400/
 *     will be used.
 *     The -source option may be abbreviated to -s.
 *
 * <dt><b><code>-target</code></b> <var>targetDirectory</var>
 * <dd>Specifies the fully-qualified path name of where to store the files.
 *     The -target option may be abbreviated to -t.
 *
 * <dt><b><code>-prompt</code></b>
 * <dd>Specifies that the user will be prompted before updating the packages on
 *     the workstation.  If not specified, the packages will be updated.
 *     The -prompt option may be abbreviated to -pr.
 *
 * <dt><b><code>-?</code></b>
 * <dd>Displays the help text.
 *
 * <dt><b><code>-help</code></b>
 * <dd>Displays the help text.
 *     The -help parameter may be abbreviated to -h.
 * </dl>
 *
 * <a name="InstallEx"> </a>
 * <p><strong>Install Example</strong></p>
 * <p>The following example will install all packages.
 * <ul>
 * <pre>
 * java AS400ToolboxInstaller -install
 *                            -source http://myAS400/QIBM/ProdData/HTTP/Public/jt400/
 *                            -target c:\java\
 *
 * </pre>
 * </UL>
 * <p>The following examples will install the access package.
 * <ul>
 * <pre>
 * java AS400ToolboxInstaller -install -package ACCESS
 *                            -source http://myAS400/QIBM/ProdData/HTTP/Public/jt400/
 *                            -target c:\java\
 *
 * java AS400ToolboxInstaller -install -package ACCESS
 *                            -source mySystem
 *                            -target c:\java\
 *
 * java AS400ToolboxInstaller -i -p ACCESS
 *                            -s mySystem
 *                            -t c:\java\
 * </pre>
 * </UL>
 *
 *
 * <p><strong>Uninstall Example</strong></p>
 * <p>This example will remove the access package.
 * <ul>
 * <pre>
 * java AS400ToolboxInstaller -uninstall -package ACCESS -target c:\java\
 * </pre>
 * </UL>
 *
 *
 * <a name="CompareEx"> </a>
 * <p><strong>Compare Example</strong></p>
 * <p>This example will compare the current level of the OPNAV package.
 * <ul>
 * <pre>
 * java AS400ToolboxInstaller -compare
 *                            -package OPNAV
 *                            -source http://myAS400/QIBM/ProdData/HTTP/Public/jt400/
 *                            -target c:\java\
 * </pre>
 * </ul>
 *
 * <p><strong>Help Example</strong></p>
 * <P>The following will display help information:
 * <UL>
 * java AS400ToolboxInstaller -? <br>
 * java AS400ToolboxInstaller -help <br>
 * java AS400ToolboxInstaller -h
 * </UL>
 *
 * @deprecated This class is no longer being enhanced, since it has no known users.
**/
public class AS400ToolboxInstaller {
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";


private static final int INSTALL = 0;   // when installing      @A2a
private static final int UNINSTALL = 1; // when uninstalling    @A2a
private static final int COMPARE = 2;   // when comparing       @A2a

private static boolean isPrompt_ =false; // indicate if -pr is specified @A2a
// Changes required to the classpath as a result of the latest
// install operation.
private static Vector classpathAdditions_ = new Vector();
private static Vector classpathRemovals_ = new Vector();
private static Vector unexpandedFiles_ = new Vector();

// Return values for classpathContains method.
private static final int NOT_IN_CLASSPATH=0;
private static final int IN_CLASSPATH=1;
private static final int UNKNOWN_CLASSPATH=2; // could not examine classpath

// $D1a constants for the difference between the code on the server
//      and the code on the client
private static final int NO_CHANGE = 0;
private static final int SERVER_HAS_NEW_PTF = 1;
private static final int SERVER_HAS_NEW_RELEASE = 2;

// Byte array used to store data read from files.  Size is arbitrary.
private static byte[] data_ = new byte[2560];

// Where MRI comes from.
private static ResourceBundle resources_;

// Control files use a forward slash for a path separator.  This flag
// says whether the slash has to be changed to a backward slash when
// writing to the local file system.
private static boolean changeSlash_ = !File.separator.equals("/");

// To prevent instantiation.  All methods in this class are static.
private AS400ToolboxInstaller()
{}



/**
Adds a trailing separator to the path if it does not already have one.
If the path is blank,
a separator is not added, since that would change the meaning of the
path to be the root directory rather than the curent directory.

@param     path        The path to which to add a separator.

@return    The path with a trailing separator.
**/
private static String addTrailingSeparator(String path)
{
    if (path.length() > 0 &&
         path.substring(path.length()-1).equals(File.separator));
    else if (path.length() > 0)
        path = path + File.separator;
    return path;
}


/**
Checks to see if <i>path</i> is in the CLASSPATH.  If the CLASSPATH
is not accessible, UNKNOWN_CLASSPATH will be returned.  This method
is not case-sensitive.

@param     path   The path for which to check the CLASSPATH.

@return    NOT_IN_CLASSPATH if <i>path</i> is not in the CLASSPATH,
           IN_CLASSPATH if <i>path</i> is in the CLASSPATH,
           UNKNOWN_CLASSPATH if the CLASSPATH cannot be accessed.
**/
private static int classpathContains(String path)
{
    try
    {
        // Search with leading and trailing semicolons to make sure
        // paths match exactly.  Uppercase to make our check case-
        // insensitive.
        String separator = System.getProperty("path.separator");
        String classpath = separator +
                           System.getProperty("java.class.path") +
                           separator;
        classpath = classpath.toUpperCase();
        path = path.toUpperCase();
        if (classpath.lastIndexOf(separator + path + separator) != -1)
            return IN_CLASSPATH;
        else
            return NOT_IN_CLASSPATH;
    }
    catch(Exception e)
    {
        return UNKNOWN_CLASSPATH;
    }
}




// @D1a The following routine (comparePackageFiles) is new.

/**
Determines if an update is needed.

@param     sourceControlFile  File that contains VRM info from the server
@param     targetControlFile  File that contains VRM info on the client.
@return    updateNeeded       Int that indicates if the files are the same,
                              the server has a new PTF, or the server has
                              a new release.

<P>
This routine compares the first line of the control files.  VRM
information must be the first line of the file and must be in VnRnMnxx
format.  If VRM information is not in the first line, the original
check is used.  That check assumes the only difference between the
source and target is PTF level.  This means if the source is larger
than the target, the source has a new PTF.
<P>
An example of the control file is on a system with two PTFs:
<XMP>
V3R2M0
V3R2M01
V3R2M02
</XMP>

**/

private static int comparePackageFiles(String serverFile, String clientFile)
{
   int result = NO_CHANGE;
   boolean useOriginalCheck = true;

   try
   {
      StringTokenizer parseServerFile = new StringTokenizer(serverFile, "\n\r");
      StringTokenizer parseClientFile = new StringTokenizer(clientFile, "\n\r");

      // get the first line out of each file
      String ServerVRM = parseServerFile.nextToken().toUpperCase();
      String ClientVRM = parseClientFile.nextToken().toUpperCase();

      // If the first line looks like a VRM string then check the contents
      // of the strings.
      if ((ServerVRM.charAt(0) == 'V') && (ClientVRM.charAt(0) == 'V') &&
          (ServerVRM.charAt(2) == 'R') && (ClientVRM.charAt(2) == 'R') &&
          (ServerVRM.charAt(4) == 'M') && (ClientVRM.charAt(4) == 'M'))
      {
         // If the server has a newer version indicate an update is needed
         if (ServerVRM.charAt(1) > ClientVRM.charAt(1))
         {
            result = SERVER_HAS_NEW_RELEASE;
            useOriginalCheck = false;
         }

         // if the client has a newer version then return saying no
         // updated needed.
         else if (ServerVRM.charAt(1) < ClientVRM.charAt(1))
         {
            useOriginalCheck = false;
         }

         // else the version is the same ...
         else
         {
            // if the server has a newer release indicate an update is needed
            if (ServerVRM.charAt(3) > ClientVRM.charAt(3))
            {
               result = SERVER_HAS_NEW_RELEASE;
               useOriginalCheck = false;
            }

            // if the client has a newer release then return saying no
            // update is needed.
            else if (ServerVRM.charAt(3) < ClientVRM.charAt(3))
            {
               useOriginalCheck = false;
            }

            // else the releases are the same, now check the mod levels
            else
            {
               // if the server has a newer mod level then return indicating
               // an updated is needed.
               if (ServerVRM.charAt(5) > ClientVRM.charAt(5))
               {
                  result = SERVER_HAS_NEW_RELEASE;
                  useOriginalCheck = false;
               }

               // if the client has a newer mod level then return
               // saying no update is needed.
               else if (ServerVRM.charAt(5) < ClientVRM.charAt(5))
               {
                  useOriginalCheck = false;
               }

               // note there is no else.  This is the case where
               // the Version, Release and first digit of the mods are
               // the same.  We will use the old check to see if a
               // PTF is needed.
            }
         }
      }
   }
   catch (Exception e) {}


   // else we cannot find VRM info in the server and/or client
   // file.  Use the original check -- determine if an update is
   // needed by comparing the lengths of the package files.  If
   // the server's file is larger assume a PTF has been applied
   // to the server.
   if (useOriginalCheck)
   {
      if (serverFile.length() > clientFile.length())
         result = SERVER_HAS_NEW_PTF;
   }

   return result;
}


/**
Copies the file pointed to by <i>sourceURL</i> to <i>targetFile</i>.
If the target file exists, it will be replaced, not appended.

@param     targetFile  The file (on the local system) in which to write the data.
@param     sourceURL   The location of file to copy.

@exception IOException If an error occurs while communicating to the server.
**/
private static void copyFile(String targetFile, URL sourceURL)
    throws IOException
{
    InputStream in = sourceURL.openStream();

    // Make sure the target directory exists.
    if ((new File(targetFile)).getParent() != null)
    {
      File dir = new File((new File(targetFile)).getParent());
      if (!dir.exists())
      {
        if (!dir.mkdirs() && !dir.isDirectory())                   // @D8C
        {
            throw new IOException("CANNOT_CREATE_DIRECTORY");
        }
      }
    }

    FileOutputStream out = new FileOutputStream(targetFile);

    int n = in.read(data_);
    while (n != -1)
    {
        out.write(data_, 0, n);
        n = in.read(data_);
    }

    in.close();
    out.close();
}


/**
Returns the names of all the IBM Toolbox for Java packages.

@param     source      The directory in which the Toolbox exists.

@return    The vector of Strings which contain the names of all the
           packages in the IBM Toolbox for Java.

@exception IOException If the package list file cannot be found.
**/
private static Vector getAllPackageNames(String source)
    throws IOException
{
    // Get contents of source package list
    String packageList = readFile(new FileInputStream(source + "JT400.PKG"));

    // Loop through each of the packages in the package list.
    StringTokenizer packages = new StringTokenizer(packageList);
    Vector list = new Vector();
    while (packages.hasMoreTokens())
    {
        list.addElement(packages.nextToken());
    }
    return list;
}


/**
Returns the names of all the IBM Toolbox for Java packages installed
on the source.

@param     source      The location of the Toolbox.

@return    The vector of Strings which contain the names of all the
           packages in the IBM Toolbox for Java.

@exception IOException If the package list file cannot be found.
**/
private static Vector getAllPackageNames(URL source)
    throws IOException
{
    // Get contents of source package list
    String packageList = readFile(
          new URL(source.toExternalForm() + "JT400.PKG"));

    // Loop through each of the packages in the package list.
    StringTokenizer packages = new StringTokenizer(packageList);
    Vector list = new Vector();
    while (packages.hasMoreTokens())
    {
        list.addElement(packages.nextToken());
    }
    return list;
}


/**
Returns the set of paths that should be added to the CLASSPATH as a
result of the latest install or uninstall operation.  The returned
vector will always be empty after an uninstall.

@return    The vector of Strings which contain the paths that should be
           added to the CLASSPATH
           as a result of the latest install or uninstall operation.
**/
public static Vector getClasspathAdditions()
{
    return classpathAdditions_;
}


/**
Returns the set of paths that should be removed from the CLASSPATH as a
result of the latest install or uninstall operation.
<br><u>Note:</u>
Removing directories from the classpath may cause Java programs
to fail if the directory contains code required by the Java program.
Extraneous directories in the CLASSPATH
do no harm.  Therefore, you may not wish to remove any CLASSPATH
entries, or remove only directories which do not contain any Java
code.

@return    The vector of Strings which contain the paths that should be
           removed from the CLASSPATH
           as a result of the latest install or uninstall operation.
**/
public static Vector getClasspathRemovals()
{
    return classpathRemovals_;
}



/**
Loads the resource bundle if not already done.

@return The resource bundle for this class.
**/
private static ResourceBundle getMRIResource()
{
  // Initialize resource bundle if not already done.
  if (resources_ == null)
      resources_ = ResourceBundle.getBundle("utilities.INMRI");
  return resources_;
}


/**
Returns the set of files that should be expanded as a
result of the latest install or uninstall operation.
Note that not all *.zip files will be in this list.  Only those files
designated to be expanded by the package will be included.  The returned
vector will always be empty after an uninstall.

@return    The vector of Strings which contain the files that should be
           expanded as a result of the latest install or uninstall operation.
**/
public static Vector getUnexpandedFiles()
{
    return unexpandedFiles_;
}


/**
Installs/updates an IBM Toolbox for Java package.
If the package is already installed,
it will be updated if needed.  This method just copies files, it will
not modify the CLASSPATH, or expand any 'zipped' files.
<pre>
URL sourceURL = new URL("http://myAS400/QIBM/ProdData/HTTP/Public/jt400/");
AS400ToolboxInstaller.install("ACCESS", "C:\\java\\", sourceURL);
</pre>

@param     packageName The package which to install.
                       "*ALL" can be used to install all the IBM
                       Toolbox for Java packages.
@param     targetPath  The path in which to install.  The directory will be
                       created if it does not exist.
@param     source      The URL which contains the location which contains the
                       current package.  File names will be appended
                       to this location, so a trailing path separator
                       is required.

@return    true if an install/update occurred, false if no updates were
           needed.

@exception IOException If an error occurs while communicating with the server.
**/
public static boolean install(String packageName,
                              String targetPath,
                              URL source)
    throws IOException
{
    // Verify parms
    if (packageName == null)
        throw new NullPointerException("packageName");
    if (targetPath == null)
        throw new NullPointerException("targetPath");
    if (source == null)
        throw new NullPointerException("source");

    // If *ALL passed for the package, get list of packages
    // and call self with 'real' package names.
    if (packageName.equalsIgnoreCase("*ALL"))                     //@D5C
    {
        Vector packageList = getAllPackageNames(source);
        boolean results = false;
        Vector cpa = new Vector();
        Vector cpr = new Vector();
        Vector uf = new Vector();
        int size = packageList.size();
        for (int i=0; i<size; ++i)
        {
            results = install((String)packageList.elementAt(i), targetPath, source)
                      || results;
            // Save classpath removals
            for (int j=0; j<classpathRemovals_.size(); ++j)
            {
                if (!cpr.contains(classpathRemovals_.elementAt(j)))
                    cpr.addElement(classpathRemovals_.elementAt(j));
            }
            // Save classpath additions
            for (int j=0; j<classpathAdditions_.size(); ++j)
            {
                if (!cpa.contains(classpathAdditions_.elementAt(j)))
                    cpa.addElement(classpathAdditions_.elementAt(j));
            }
            // Save unexpanded files
            for (int j=0; j<unexpandedFiles_.size(); ++j)
            {
                if (!uf.contains(unexpandedFiles_.elementAt(j)))
                    uf.addElement(unexpandedFiles_.elementAt(j));
            }
        }
        classpathAdditions_ = cpa;
        classpathRemovals_ = cpr;
        unexpandedFiles_ = uf;
        return results;
    }

    // Initialize
    classpathAdditions_.removeAllElements();
    classpathRemovals_.removeAllElements();
    unexpandedFiles_.removeAllElements();

// Note: The following logic is (almost) exactly
// the same as code in isUpdateNeeded().  Updates here should also
// probably be made there.

    // Make sure the target path has a trailing separator.
    targetPath = addTrailingSeparator(targetPath);

    // Get contents of source package change list
    StringBuffer sourcePackageURL = new StringBuffer(source.toExternalForm());
    sourcePackageURL.append(packageName);
    sourcePackageURL.append(".LVL");
    String sourcePackageFile = readFile(new URL(sourcePackageURL.toString()));

    // Get contents of target package change list
    StringBuffer targetPackageFileName = new StringBuffer(targetPath);
    targetPackageFileName.append(packageName);
    targetPackageFileName.append(".LVL");
    String targetPackageFile = "";
    try {
        targetPackageFile =
          readFile(new FileInputStream(targetPackageFileName.toString()));
    }
    catch(Exception e)
    {
        // assume file doesn't exist
        targetPackageFile = "";
        // Make sure the target directory exists if it is a subdirectory.
        if (targetPath.length() > 1)
        {
          File parent = new File(targetPath.substring(0, targetPath.length()-1));
          if (!parent.exists() || !parent.isDirectory())
          {
            if (!parent.mkdirs() && !parent.isDirectory())             // @D8C
            {
                throw new IOException("(" + parent.toString() + ") " +
                    getMRIResource().getString("EXC_CANNOT_CREATE_DIRECTORY"));
            }
          }
        }
    }

    // @D1c See if an update is needed.
    int updateType = comparePackageFiles(sourcePackageFile, targetPackageFile);

    // return false if no update is needed.
    if (updateType == NO_CHANGE)
      return false;

// End of code that is a copy (almost) of code in isUpdateNeeded().

    StringTokenizer sourceUpdates = new StringTokenizer(sourcePackageFile);
    StringTokenizer targetUpdates = new StringTokenizer(targetPackageFile);

    // @D1a Adjust the files only if a new PTF is on the server.  We need
    // the entire file if the server has a new release.

    if (updateType == SERVER_HAS_NEW_PTF)
    {
       // Loop through each of the updates in the source package file.
       // Throw away any updates that have a corresponding line in the
       // target package file.  The package files contain a line for
       // each update.  We depend on the package files being
       // left alone by the user.  If this is true then the only
       // lines in the package files are the list of updates.  The top
       // section of the source and target file should match.  By
       // deleting lines, when the target file is emply all that remains
       // in the source file is the set of updates to make.
       while (targetUpdates.hasMoreTokens())
       {
           sourceUpdates.nextToken();
           targetUpdates.nextToken();
       }
    }

    // Process updates which have not been applied to the target.
    // This combines all the updates that have not been applied to
    // the target into one set of updates in order to minimize the
    // amount of downloads from the remote source system.
    String updateFile, update, prevUpdate, fileName, oldAction, newAction;
    StringBuffer updateFileURL, workStr;
    StringTokenizer updates;
    Hashtable finalUpdates = new Hashtable();
    Hashtable classpathUpdates = new Hashtable();
    while (sourceUpdates.hasMoreTokens())
    {
        // Read contents of source update file
        updateFileURL = new StringBuffer(source.toExternalForm());
        updateFileURL.append(sourceUpdates.nextToken());
        updateFileURL.append(".LST");
        updateFile = readFile(new URL(updateFileURL.toString()));

        // Loop through lines in update file, determining
        // files that need updating.
        // The format of the update file is as follows:
        //    Column        Description
        //    ------        ----------------
        //     1-8          Package affected
        //     9            Reserved                              //@D4A
        //     10-13        ADD, UPD, RMV, PADD, or PRMV
        //     14           Reserved                              //@D4A
        //     15           Expand file?         Y or N
        //     16           Classpath affected?  Y or N
        //     17-19        Reserved                              //@D4A
        //     20-end       File name, with possible subdirectory
        updates = new StringTokenizer(updateFile, "\n\r");
        while (updates.hasMoreTokens())
        {
            update = updates.nextToken();
            // Determine if this line affects our package.
            String str = update.substring(0,8).trim();
            if (str.equalsIgnoreCase(packageName))                //@D5C
            {
              newAction = update.substring(9, 13);

              if (update.length() > 19)                           //@D4A
              {                                                   //@D4A
                // get file name
                fileName = update.substring(19);
              } else                                              //@D4A
                fileName = "";                                    //@D4A

              // if we are dealing with a class path action...
              if (newAction.equalsIgnoreCase("PADD") || newAction.equalsIgnoreCase("PRMV")) //@D5C
              {
                    // We don't need to check the hashtable to check
                    // if the classpath is already contained in it
                    // since we always want the latest action to
                    // be the action that we take.
                    // This will set the action as follows:
                    //   Old action   New action   Final action
                    //      PADD         PADD          PADD
                    //      PADD         PRMV          PRMV
                    //      PRMV         PADD          PADD
                    //      PRMV         PRMV          PRMV
                    if (fileName.length() > 0)               //@D4A
                    {                                        //@D4A
                      // add to set of classpath updates
                      classpathUpdates.put(fileName, update);
                    }                                        //@D4A
              }
              // else we are dealing with a file action
              else
              {
                // Determine if this file is already in the set of updates.
                prevUpdate = (String)finalUpdates.get(fileName);
                if (prevUpdate != null)
                {
                    // Determine if we want to change the action.
                    // This set of code will set the action as follows:
                    //   Old action   New action   Final action
                    //       ADD         ADD          ADD
                    //       ADD         RMV          RMV
                    //       ADD         UPD          ADD
                    //       RMV         ADD          UPD
                    //       RMV         RMV          RMV
                    //       RMV         UPD          UPD
                    //       UPD         ADD          UPD
                    //       UPD         RMV          RMV
                    //       UPD         UPD          UPD
                    oldAction = prevUpdate.substring(9, 13);
                    // if new action is RMV, change to RMV
                    if (newAction.equalsIgnoreCase("RMV "))        //@D5C
                    {
                      finalUpdates.put(fileName, update);
                    }
                    // if old action is ADD, leave as is
                    else if (oldAction.equalsIgnoreCase("ADD "))   //@D5C
                    {}
                    // if new action is ADD...
                    else if (newAction.equalsIgnoreCase("ADD "))   //@D5C
                    {
                        // change action to update
                        workStr = new StringBuffer(update);
                        workStr.setCharAt(9, 'U');
                        workStr.setCharAt(10, 'P');
                        workStr.setCharAt(11, 'D');
                        workStr.setCharAt(12, ' ');
                        finalUpdates.put(fileName, workStr.toString());
                    }
                    // otherwise take the new action
                    else
                    {
                      finalUpdates.put(fileName, update);
                    }
                }
                else
                {
                    // add file to set of updates
                    finalUpdates.put(fileName, update);
                }
              }
            }
        }
    }


    // Copy the package list control file to the target.
    // Do this first, so an uninstall(*ALL) can be done if the install
    // bombs midstream.
    copyFile(targetPath.toString() + "JT400.PKG",
        new URL(source.toExternalForm() + "JT400.PKG"));

    // Copy the package file list control file to the target.
    // Do this first, so an uninstall can be done if the install bombs
    // midstream.
    copyFile(targetPath.toString() + packageName + ".LST",
        new URL(source.toExternalForm() + packageName + ".LST"));

    // Make needed updates to the files on the target.
    String temp;
    for (Enumeration e = finalUpdates.elements(); e.hasMoreElements();)
    {
      update = (String)e.nextElement();
      fileName = update.substring(19);
      // remove the file if action is RMV
      String urlFile = fileName;  // save filename with forward slashes
      if (changeSlash_)            // change slashes if needed
        fileName = fileName.replace('/', '\\');
      if (update.substring(9, 13).equalsIgnoreCase("RMV "))       //@D5C
      {
        try
        {
          File file = new File(targetPath.toString() + fileName);
          file.delete();
        }
        catch(Exception ex) {} // don't worry if can't delete file
        // Note that the expand flag is ignored for the RMV action.
        // Check if classpath should be updated.
        if (update.charAt(15) == 'Y')  // remove file from CP
        {
          // See if it is contained in the classpath
          temp = targetPath.toString() + fileName;
          temp = (new File(temp)).getAbsolutePath();
          if (classpathContains(temp)!=NOT_IN_CLASSPATH)
            classpathRemovals_.addElement(temp);
        }
      }
      else
      if ((update.substring(9, 13).equalsIgnoreCase("ADD "))      //@D5A
      ||  (update.substring(9, 13).equalsIgnoreCase("UPD "))      //@D5A
      ||  (update.substring(9, 13).equalsIgnoreCase("OADD")))     //@D5A
      {
        // copy the file
        boolean miaFile = false;
        try
        {
          copyFile(targetPath.toString() + fileName,
                   new URL(source.toExternalForm() + urlFile));
        }
        catch (IOException ex)
        {   // assume error is file not found, which is OK if
            // this is a .properties or an optional (OADD) file    //@D5C
          if ((update.substring(9, 13).equalsIgnoreCase("OADD"))  //@D5A
          || ((fileName.length() > 11
          &&   fileName.substring(fileName.length() - 11).equalsIgnoreCase(".properties")))) //@D5C
          {
           miaFile = true;   // mark file as missing so not added to Vectors
          } else  // otherwise this is a problem, rethrow the error
          {
            throw ex;
          }
        }

        // Only add file to unexpanded vector if it existed.
        if (!miaFile)
        {
          // Check if file needs to be expanded.
          if (update.charAt(14) == 'Y')
          {
            temp = targetPath.toString() + fileName;
            temp = (new File(temp)).getAbsolutePath();
            unexpandedFiles_.addElement(temp);
          }
          // For ADDs, check if classpath should be updated.
          if ((update.substring(9, 13).equalsIgnoreCase("ADD ")) || //@D5C
              (update.substring(9, 13).equalsIgnoreCase("OADD")))   //@D7a
          {
            if (update.charAt(15) == 'Y')  // add file to CP
            {
              // See if it is contained in the classpath
              temp = targetPath.toString() + fileName;
              temp = (new File(temp)).getAbsolutePath();
              if (classpathContains(temp)!=IN_CLASSPATH)
                classpathAdditions_.addElement(temp);
            }
          }
        }
      } else
      {
       // we don't recognize this tag.
      }
    }

    // Make needed updates to the classpath arrays.
    for (Enumeration e = classpathUpdates.elements(); e.hasMoreElements();)
    {
      update = (String)e.nextElement();

      if (update.length() > 19)              //@D4A
      {                                      //@D4A
        // get file name
        fileName = update.substring(19);
      } else                                 //@D4A
        fileName = "";                       //@D4A

      fileName = update.substring(19).trim();
      if (changeSlash_)            // change slashes if needed
        fileName = fileName.replace('/', '\\');
      // get full path name
      if (fileName.length() > 0)
      {
        temp = targetPath.toString() + fileName;
        temp = (new File(temp)).getAbsolutePath();
      } else  // no path, need to add target directory to classpath
      {
        if (targetPath.length() > 1)
        {
          temp = targetPath.substring(0,targetPath.length()-1);
        } else
        {
          temp = "";
        }
        temp = (new File(temp)).getAbsolutePath();
      }
      // If this is a classpath addition
      if (update.substring(9, 13).equalsIgnoreCase("PADD"))       //@D5C
      {
        // See if it is contained in the classpath
        if (classpathContains(temp)!=IN_CLASSPATH)
          classpathAdditions_.addElement(temp);
      }
        // else is a classpath removal
      else
      {
        // See if it is contained in the classpath
        if (classpathContains(temp)!=NOT_IN_CLASSPATH)
          classpathRemovals_.addElement(temp);
      }
    }

    // Copy the source package file to the target.
    // Do this last, after all other updates have been successful,
    // so if there is an error midstream, another update can be
    // attempted.
    FileOutputStream out = new FileOutputStream(
            targetPackageFileName.toString() );
    int numBytes;
    int position = 0;
/*    while (position < sourcePackageFile.length())
    {
       numBytes =
          (data_.length < sourcePackageFile.length() - position ?
              data_.length :
              sourcePackageFile.length() - position);
        sourcePackageFile.getBytes(position, position+numBytes, data_, 0);
        out.write(data_, 0, numBytes);
        position += numBytes;
    }
replaced with following 2 lines */
    byte[] data = sourcePackageFile.getBytes();
    out.write(data, 0, data.length);
    out.close();

    return true;
}


/**
Indicates if the package is installed.  Installation is determined
by examining the target directory for the *.LVL file.  This method
does not check for a partial installation, if installation is
incomplete, this method may return true or false.
<pre>
AS400ToolboxInstaller.isInstalled("ACCESS", "C:\\java\\");
</pre>

@param     packageName The package which will be checked.
                       Note that *ALL is not supported.
@param     targetPath  The path in which the package is installed.

@return    true if the package has been installed, false if the
           package has not been installed.
**/
public static boolean isInstalled(String packageName,
                                  String targetPath)
{
    // Verify parms
    if (packageName == null)
        throw new NullPointerException("packageName");
    if (targetPath == null)
        throw new NullPointerException("targetPath");

    // Make sure the target path has a trailing separator.
    targetPath = addTrailingSeparator(targetPath);

    // Look for the <package>.LVL file in the target directory.
    // If it is there, return true, if not, return false.
    StringBuffer filename = new StringBuffer(targetPath);
    filename.append(packageName);
    filename.append(".LVL");
    File file = new File(filename.toString());
    if (file.exists())
        return true;
    return false;
}


/**
Returns whether the package is downlevel.  This method will return
true if the package is not installed.
<pre>
URL sourceURL = new URL("http://myAS400/QIBM/ProdData/HTTP/Public/jt400/");
AS400ToolboxInstaller.isUpdateNeeded("ACCESS", "C:\\java\\", sourceURL);
</pre>

@param     packageName The package which will be checked.
                       "*ALL" can be used to check all the IBM
                       Toolbox for Java packages.
@param     targetPath  The path in which the package is installed.
@param     source      The URL which contains the location which contains the
                       current package.  File names will be appended
                       to this location, so a trailing path separator
                       is required.

@return    true if an update is needed, false if no updates are
           needed.

@exception IOException If an error occurs while communicating with the server.
**/
public static boolean isUpdateNeeded(String packageName,
                                     String targetPath,
                                     URL source)
    throws IOException
{
    // Verify parms
    if (packageName == null)
        throw new NullPointerException("packageName");
    if (targetPath == null)
        throw new NullPointerException("targetPath");
    if (source == null)
        throw new NullPointerException("source");

    // If *ALL passed for the package, get list of packages
    // and call self with 'real' package names.
    if (packageName.equalsIgnoreCase("*ALL"))                     //@D5C
    {
        Vector packageList = getAllPackageNames(source);
        int size = packageList.size();
        for (int i=0; i<size; ++i)
        {
            if (isUpdateNeeded((String)packageList.elementAt(i), targetPath, source))
                return true;
        }
        return false;
    }


// Note: the remaining logic in this method is (almost) exactly
// the same as code in install().  Updates here should also
// probably be made there.

    // Make sure the target path has a trailing separator.
    targetPath = addTrailingSeparator(targetPath);
    // Get contents of source package change list
    StringBuffer sourcePackageURL = new StringBuffer(source.toExternalForm());
    sourcePackageURL.append(packageName);
    sourcePackageURL.append(".LVL");
    String sourcePackageFile = readFile(new URL(sourcePackageURL.toString()));

    // Get contents of target package change list
    StringBuffer targetPackageFileName = new StringBuffer(targetPath);
    targetPackageFileName.append(packageName);
    targetPackageFileName.append(".LVL");
    String targetPackageFile = "";
    try {
        targetPackageFile =
          readFile(new FileInputStream(targetPackageFileName.toString()));
    }
    catch(Exception e)
    {
        // assume file doesn't exist
        targetPackageFile = "";
    }

    // @D1c determine if new files are on the server
    if (comparePackageFiles(sourcePackageFile, targetPackageFile) == NO_CHANGE)
      return false;
    else
      return true;
}



// @A2a
/**
Performs the actions specified in the invocation arguments.  See
the class description for information on the command line parameters.

@param args The command line arguments.
**/
public static void main(String[] args)
{
    Vector packagesV=null;
    String tgtPath=null;
    String src=null;
    Integer requestedAction=null;
    String firstElement=null;
    Hashtable argsHashtable;

    Vector unExpandFileV = null;                                        //@D6a
    Vector classpathAddV = null;                                        //@D6a
    Vector classpathRmvV = null;                                        //@D6a

    argsHashtable = parseArgs (args);

    if (argsHashtable!=null)
    {
        packagesV = (Vector)argsHashtable.get("packages");
        tgtPath   = (String)argsHashtable.get("target");
        requestedAction = (Integer)argsHashtable.get("isinstall");
        src       = (argsHashtable.containsKey("source") ?
                    (String)argsHashtable.get("source") : null);
        firstElement = (String)packagesV.firstElement();

        URL sourceURL = null;
        try
        {
            if (src!=null)
            {
               boolean httpIt = false;

               if (src.length() < 5)
                  httpIt = true;
               else if ( ! ((src.toLowerCase().startsWith("http")) ||
                            (src.toLowerCase().startsWith("file"))))
                  httpIt = true;

               if (httpIt)
                  src = "http://" + src + "/QIBM/ProdData/HTTP/Public/jt400/";
               else if (src.toLowerCase().startsWith("file"))
                  src = addTrailingSeparator(src);

               sourceURL = new URL(src);
            }
        }
        catch(MalformedURLException e)
        {
            System.out.println (getMRIResource().getString("ERR_NOT_VALID_URL")+" "+src);
            printUsage(System.out);
            System.exit(0);
        }

        if (requestedAction.intValue()==INSTALL)  // install packages
        {
            try
            {
                if((firstElement.equalsIgnoreCase("*all")) && (packagesV.size()==1))
                {
                    Vector packageList = getAllPackageNames(sourceURL);
                    if(isPrompt_==false)
                    {
                       install(firstElement,tgtPath,sourceURL);
                       for(Enumeration e = packageList.elements(); e.hasMoreElements();)
                       {
                            String message = getMRIResource().getString("RESULT_PACKAGE_INSTALLED");
                            message = substitute(message,(String)e.nextElement());
                            System.out.println(message);
                       }
                    }else
                    {
                       for(Enumeration e = packageList.elements(); e.hasMoreElements();)
                       {
                          String pkg = (String)e.nextElement();

                          // the following line of code (the if check) is new -- @E1A
                          if (isInstalled(pkg, tgtPath))
                          {
                             if(isUpdateNeeded(pkg,tgtPath,sourceURL))
                             {
                                String str = getMRIResource().getString("PROMPT_IF_REPLACE_ONE");
                                if(prompt(str,pkg))
                                   install(pkg,tgtPath,sourceURL);
                             }
                             else
                             {
                                String message = getMRIResource().getString("RESULT_PACKAGE_NOT_NEED_UPDATED");
                                message = substitute(message,pkg);
                                System.out.println(message);
                             }
                          }
                          // the following else block is new @E1a
                          else
                          {
                             String str = getMRIResource().getString("PROMPT_IF_REPLACE_TWO");
                             if(prompt(str,pkg))
                                install(pkg,tgtPath,sourceURL);
                          }
                       }
                    }
                }
                else
                {
                    for(Enumeration e = packagesV.elements(); e.hasMoreElements();)
                    {
                        String pkg = (String)e.nextElement();
                        if(isPrompt_==false)
                        {
                            install(pkg,tgtPath,sourceURL);
                            String message = getMRIResource().getString("RESULT_PACKAGE_INSTALLED");
                            message = substitute(message,pkg);
                            System.out.println(message);
                        }
                        else
                        {
                            // the following line of code (the if check) is new -- @E1A
                            if (isInstalled(pkg, tgtPath))
                            {
                               if(isUpdateNeeded(pkg,tgtPath,sourceURL))
                               {
                                  String str = getMRIResource().getString("PROMPT_IF_REPLACE_ONE");
                                  if(prompt(str,pkg))
                                     install(pkg,tgtPath,sourceURL);
                               }
                               else
                               {
                                  String message = getMRIResource().getString("RESULT_PACKAGE_NOT_NEED_UPDATED");
                                  message = substitute(message,pkg);
                                  System.out.println(message);
                               }
                            }
                            // the following else block is new @E1a
                            else
                            {
                               String str = getMRIResource().getString("PROMPT_IF_REPLACE_TWO");
                               if (prompt(str,pkg))
                                   install(pkg,tgtPath,sourceURL);
                            }
                        }

                        if (classpathAddV == null)                                          //@D6a
                           classpathAddV = new Vector();                                    //@D6a
                                                                                            //@D6a
                        for (int j=0; j<classpathAdditions_.size(); ++j)                    //@D6a
                        {                                                                   //@D6a
                           if (! classpathAddV.contains(classpathAdditions_.elementAt(j)))  //@D6a
                              classpathAddV.addElement(classpathAdditions_.elementAt(j));   //@D6a
                        }                                                                   //@D6a
                                                                                            //@D6a
                        if (classpathRmvV == null)                                          //@D6a
                           classpathRmvV = new Vector();                                    //@D6a
                                                                                            //@D6a
                        for (int j=0; j<classpathRemovals_.size(); ++j)                     //@D6a
                        {                                                                   //@D6a
                           if (! classpathRmvV.contains(classpathRemovals_.elementAt(j)))   //@D6a
                              classpathRmvV.addElement(classpathRemovals_.elementAt(j));    //@D6a
                        }                                                                   //@D6a
                                                                                            //@D6a
                        if (unExpandFileV == null)                                          //@D6a
                           unExpandFileV = new Vector();                                    //@D6a
                                                                                            //@D6a
                        for (int j=0; j<unexpandedFiles_.size(); ++j)                       //@D6a
                        {                                                                   //@D6a
                           if (! unExpandFileV.contains(unexpandedFiles_.elementAt(j)))     //@D6a
                              unExpandFileV.addElement(unexpandedFiles_.elementAt(j));      //@D6a
                        }                                                                   //@D6a

                    }
                }

            }
            catch(IOException e)
            {
                System.out.println (e.toString ());
            }

            if (unExpandFileV == null)                   //@D6a
               unExpandFileV = getUnexpandedFiles();     //@D6c

            if(!unExpandFileV.isEmpty())
            {
                String unExpandFileStr="";
                for(Enumeration e=unExpandFileV.elements(); e.hasMoreElements();)
                {
                    unExpandFileStr +="    "+(String)e.nextElement()+"\n";
                }
                System.out.println(getMRIResource().getString("RESULT_UNEXPANDED_FILES").trim()
                                +"\n "+unExpandFileStr);
            }


            if (classpathAddV == null)                   //@D6a
               classpathAddV = getClasspathAdditions();  //@D6c

            if(!classpathAddV.isEmpty())
            {
                String classpathAddStr="";
                for(Enumeration e=classpathAddV.elements(); e.hasMoreElements();)
                {
                    classpathAddStr +="    "+(String)e.nextElement()+"\n";
                }
                System.out.println(getMRIResource().getString("RESULT_ADD_CLASSPATHS")
                                  +"\n"+classpathAddStr);
            }


            if (classpathRmvV == null)                   //@D6a
               classpathRmvV = getClasspathRemovals();   //@D6c

            if(!classpathRmvV.isEmpty())
            {
                String classpathRmvStr="";
                for(Enumeration e=classpathRmvV.elements(); e.hasMoreElements();)
                {
                    classpathRmvStr +="    "+(String)e.nextElement()+"\n";
                }
                System.out.println(getMRIResource().getString("RESULT_REMOVE_CLASSPATHS").trim()
                                +"\n"+classpathRmvStr);
            }

        }

        else if (requestedAction.intValue()==UNINSTALL) //uninstall packages
        {
            try
            {
                if((firstElement.equalsIgnoreCase("*all")) && (packagesV.size()==1))
                {
                    Vector packageList = null;
                    Vector tempList = new Vector();
                    try
                    {
                        packageList = getAllPackageNames(addTrailingSeparator(tgtPath));
                        for(Enumeration e = packageList.elements(); e.hasMoreElements();)
                        {
                            String temp = (String)e.nextElement();
                            File pkg = new File(addTrailingSeparator(tgtPath) + temp + ".LVL");
                            if (pkg.exists())
                            {
                                tempList.addElement(temp);
                            }
                        }
                        packageList = tempList;
                    }
                    catch (FileNotFoundException e)
                    {
                        System.out.println(getMRIResource().getString("EXC_NO_PACKAGES_INSTALLED"));
                        System.exit(0);
                    }
                    unInstall((String)packagesV.firstElement(),tgtPath);
                    for(Enumeration e = packageList.elements(); e.hasMoreElements();)
                    {
                        String pkg = (String)e.nextElement();
                        String message = getMRIResource().getString("RESULT_PACKAGE_UNINSTALLED");
                        message = substitute(message,pkg);
                        System.out.println(message);
                    }
                }
                else
                {
                    for(Enumeration e = packagesV.elements(); e.hasMoreElements();)
                    {
                        String pkg = (String)e.nextElement();
                        unInstall(pkg,tgtPath);

                        if (classpathRmvV == null)                                          //@D6a
                           classpathRmvV = new Vector();                                    //@D6a
                                                                                            //@D6a
                        for (int j=0; j<classpathRemovals_.size(); ++j)                     //@D6a
                        {                                                                   //@D6a
                           if (! classpathRmvV.contains(classpathRemovals_.elementAt(j)))   //@D6a
                              classpathRmvV.addElement(classpathRemovals_.elementAt(j));    //@D6a
                        }                                                                   //@D6a

                        String message = getMRIResource().getString("RESULT_PACKAGE_UNINSTALLED");
                        message = substitute(message,pkg);
                        System.out.println(message);
                    }
                }
            }
            catch(IOException e)
            {
                System.out.println (e.toString ());
            }

            if (classpathRmvV == null)                   //@D6a
               classpathRmvV = getClasspathRemovals();   //@D6c

            if(!classpathRmvV.isEmpty())
            {
                String classpathRmvStr="";
                for(Enumeration e=classpathRmvV.elements(); e.hasMoreElements();)
                {
                    classpathRmvStr +="    "+(String)e.nextElement()+"\n";
                }
                System.out.println(getMRIResource().getString("RESULT_REMOVE_CLASSPATHS").trim()
                                +"\n"+classpathRmvStr);
            }
        }

        else                              //compare packages betweem soure and target
        {
            try
            {
                if((firstElement.equalsIgnoreCase("*all")) && (packagesV.size()==1))
                {
                    Vector packageList = getAllPackageNames(sourceURL);
                    for(Enumeration e = packageList.elements(); e.hasMoreElements();)
                    {
                        String pkgStr = (String)e.nextElement();
                        if(isUpdateNeeded(pkgStr,tgtPath,sourceURL))
                        {
                             String message = getMRIResource().getString("RESULT_PACKAGE_NEEDS_UPDATED");
                             message = substitute(message,pkgStr);
                             System.out.println(message);
                        }
                        else
                        {
                             String message = getMRIResource().getString("RESULT_PACKAGE_NOT_NEED_UPDATED");
                             message = substitute(message,pkgStr);
                             System.out.println(message);
                        }
                    }
                }
                else
                {
                    for(Enumeration e = packagesV.elements(); e.hasMoreElements();)
                    {
                        String pkgStr=(String)e.nextElement();
                        if(isUpdateNeeded(pkgStr,tgtPath,sourceURL))
                        {
                            String message = getMRIResource().getString("RESULT_PACKAGE_NEEDS_UPDATED");
                            message = substitute(message,pkgStr);
                            System.out.println(message);
                        }
                        else
                        {
                             String message = getMRIResource().getString("RESULT_PACKAGE_NOT_NEED_UPDATED");
                             message = substitute(message,pkgStr);
                             System.out.println(message);
                        }
                    }
                }
            }
            catch(IOException e)
            {
                System.out.println (e.toString ());
            }

        }
  }

  System.exit (0);
}

// @A2a
/**
Prints the usage information.

@param output   The output stream.
**/
private static void printUsage (PrintStream output)
{
      String blankSpace = "  ";
      String help02 = getMRIResource().getString("HELP02");
      help02 = help02.trim();
      StringBuffer temp = new StringBuffer();
      for(int i=0; i<(help02.length()+1); i++)
         temp.append(" ");
      String longBlank = temp.toString();


      output.println ("");
      output.println (getMRIResource().getString("HELP01"));
      output.println (blankSpace+help02+" "+
                                            getMRIResource().getString("HELP03"));
      output.println (blankSpace+longBlank+ getMRIResource().getString("HELP04"));
      output.println (blankSpace+longBlank+ getMRIResource().getString("HELP05"));
      output.println (blankSpace+longBlank+ getMRIResource().getString("HELP06"));
      output.println (blankSpace+longBlank+ getMRIResource().getString("HELP07"));
      output.println (blankSpace+longBlank+ getMRIResource().getString("HELP08"));
      output.println (blankSpace+longBlank+ getMRIResource().getString("HELP09"));
      output.println (blankSpace+longBlank+ getMRIResource().getString("HELP10"));
      output.println (blankSpace+           getMRIResource().getString("HELP11"));
      output.println (blankSpace+blankSpace+getMRIResource().getString("HELP12"));
      output.println (blankSpace+           getMRIResource().getString("HELP13"));
      output.println (blankSpace+blankSpace+getMRIResource().getString("HELP14"));
      output.println (blankSpace+           getMRIResource().getString("HELP15"));
      output.println (blankSpace+blankSpace+getMRIResource().getString("HELP16"));
      output.println ("");
      output.println (blankSpace+           getMRIResource().getString("HELP17"));
      output.println (blankSpace+           getMRIResource().getString("HELP18"));
      output.println (blankSpace+           getMRIResource().getString("HELP19"));
      output.println (                      getMRIResource().getString("HELP20"));
      output.println (blankSpace+           getMRIResource().getString("HELP21"));

}

/**
Parses and validates the arguments specified on the command line.

@param args The command line arguments.
@return An hashtable contains the arguments supplied to install(),unInstall() and isUpdateNeeded().
**/

private static Hashtable parseArgs(String[] args)
{
    boolean successful = true;
    String delimiter = ",";

    Hashtable arguments = new Hashtable();

    getMRIResource();

    if (args.length==0)
    {
        printUsage (System.out);
        return null;
    }
    else
    for (int i=0;i<args.length;i++)
    {
        if (args[i].charAt(0)=='-') // The argument is a keyword.
        {
            if (args[i].equalsIgnoreCase("-p")||
                args[i].equalsIgnoreCase("-package"))
            {
             // Only one 'package' parameter available.
                if(arguments.containsKey("packages"))
                {
                   System.out.println(getMRIResource().getString("ERR_TOO_MANY_OPTIONS").trim()
                            +" "+args[i]);
                   successful = false;
                }
                else
                {
                    Vector packages = new Vector();
                    StringTokenizer packstring;
                    if (i<args.length-1 &&
                        args[i+1].charAt(0)!='-')
                    {
                        packstring = new StringTokenizer(args[i+1],delimiter);

                        while (packstring.hasMoreTokens())
                        {
                            String packageName = packstring.nextToken();
                            packages.addElement(packageName);
                        }
                        arguments.put("packages",packages);
                    }
                    else // No value is available.
                    {
                       System.out.println(getMRIResource().getString("ERR_MISSING_OPTION_VALUE").trim()
                            +" "+args[i]);
                        successful = false;
                    }
                }
            }
            else
            if (args[i].equalsIgnoreCase("-s") ||
                args[i].equalsIgnoreCase("-source"))
            {
                if(arguments.containsKey("source"))
                {
                    System.out.println(getMRIResource().getString("ERR_TOO_MANY_OPTIONS").trim()
                        +" "+args[i]);
                    successful = false;
                }
                else
                if (i<args.length-1 &&
                    args[i+1].charAt(0)!='-')
                {
                    arguments.put("source",args[i+1]);
                }
                else // No value is available.
                {
                    System.out.println(getMRIResource().getString("ERR_MISSING_OPTION_VALUE").trim()
                        +" "+args[i]);
                    successful = false;
                }
            }
            else
            if (args[i].equalsIgnoreCase("-t")||
                args[i].equalsIgnoreCase("-target"))
            {
                if(arguments.containsKey("target"))
                {
                    System.out.println(getMRIResource().getString("ERR_TOO_MANY_OPTIONS").trim()
                        +" "+args[i]);
                    successful = false;
                }
                else
                if (i<args.length-1 &&
                    args[i+1].charAt(0)!='-')
                {
                    arguments.put("target",args[i+1]);
                }
                else // No value is available.
                {
                    System.out.println(getMRIResource().getString("ERR_MISSING_OPTION_VALUE").trim()
                        +" "+args[i]);
                    successful = false;
                }
            }
            else
            if (args[i].equalsIgnoreCase("-i") ||
                args[i].equalsIgnoreCase("-install"))
            {

                if(arguments.containsKey("isinstall"))
                {
                    if(((Integer)arguments.get("isinstall")).intValue()==INSTALL)
                    {
                        System.out.println(getMRIResource().getString("ERR_TOO_MANY_OPTIONS").trim()
                            +" "+args[i]);
                    }
                    else
                        System.out.println(getMRIResource().getString("ERR_OPTION_NOT_COMPATIBLE").trim()
                                    +" "+args[i]);
                    successful = false;
                }
                else
                {
                    arguments.put("isinstall",new Integer(INSTALL));
                }

            }
            else
            if (args[i].equalsIgnoreCase("-u") ||
                args[i].equalsIgnoreCase("-uninstall"))
            {
                if(arguments.containsKey("isinstall"))
                {
                    if(((Integer)arguments.get("isinstall")).intValue()==UNINSTALL)
                    {
                        System.out.println(getMRIResource().getString("ERR_TOO_MANY_OPTIONS").trim()
                            +" "+args[i]);
                    }
                    else
                        System.out.println(getMRIResource().getString("ERR_OPTION_NOT_COMPATIBLE").trim()
                                    +" "+args[i]);
                    successful = false;
                }
                else
                {
                    arguments.put("isinstall",new Integer(UNINSTALL));
                }
            }
            else
            if (args[i].equalsIgnoreCase("-c") ||
                args[i].equalsIgnoreCase("-compare"))
            {
                if(arguments.containsKey("isinstall"))
                {
                    if(((Integer)arguments.get("isinstall")).intValue()==COMPARE)
                    {
                        System.out.println(getMRIResource().getString("ERR_TOO_MANY_OPTIONS").trim()
                            +" "+args[i]);
                    }
                    else
                        System.out.println(getMRIResource().getString("ERR_OPTION_NOT_COMPATIBLE").trim()
                                    +" "+args[i]);
                    successful = false;
                }
                else
                {
                    arguments.put("isinstall",new Integer(COMPARE));
                }
            }
            else
            if(args[i].equalsIgnoreCase("-pr")||
               args[i].equalsIgnoreCase("-prompt"))
            {
                if(isPrompt_)
                {
                    System.out.println(getMRIResource().getString("ERR_TOO_MANY_OPTIONS").trim()
                        +" "+args[i]);
                    successful = false;
                }
                else
                    isPrompt_=true;
            }
            else
            if (args[i].equalsIgnoreCase("-?") ||
                args[i].equalsIgnoreCase("-help")||args[i].equalsIgnoreCase("-h"))
            {
                arguments = null;
                printUsage (System.out);
                return arguments;
            }
            else
            {
                System.out.println(getMRIResource().getString("ERR_UNEXPECTED_OPTION").trim()
                        +" "+args[i]);
                successful = false;
            }
        }
        else
        {
            if(i==0)
            {
                System.out.println(getMRIResource().getString("ERR_INVALID_ARGUMENT")
                                   +" "+args[i]);
                successful = false;
            }
            else
            if(!(args[i-1].equalsIgnoreCase("-package") ||args[i-1].equalsIgnoreCase("-p")||
                 args[i-1].equalsIgnoreCase("-source")  ||args[i-1].equalsIgnoreCase("-s")||
                 args[i-1].equalsIgnoreCase("-target")  ||args[i-1].equalsIgnoreCase("-t"))
              )
            {
                System.out.println(getMRIResource().getString("ERR_INVALID_ARGUMENT")
                                   +" "+args[i]);
                successful = false;
            }
        }
    }
    if (successful == false)
    {
        printUsage (System.out);
        return arguments = null;
    }

    if(!arguments.containsKey("isinstall"))
    {
        System.out.println(getMRIResource().getString("ERR_NO_I_U_C"));
        successful=false;
    }
    else
    if (((Integer)arguments.get("isinstall")).intValue() == INSTALL||
        ((Integer)arguments.get("isinstall")).intValue() == COMPARE)
    {
        if(!arguments.containsKey("source"))
        {
            System.out.println(getMRIResource().getString("ERR_MISSING_OPTION").trim()
                            +" "+"-s");
            successful=false;
        }
        if(!arguments.containsKey("target"))
        {
            System.out.println(getMRIResource().getString("ERR_MISSING_OPTION").trim()
                            +" "+"-t");
            successful=false;
        }
        if(!arguments.containsKey("packages"))
        {
            Vector packages = new Vector();
            packages.addElement("*ALL");
            arguments.put("packages",packages);
        }
    }
    else
    {
        if (arguments.containsKey("source"))
        {
            System.out.println(getMRIResource().getString("WARNING_SOURCE_URL_NOT_USED"));
        }
        if(!arguments.containsKey("target"))
        {
            System.out.println(getMRIResource().getString("ERR_MISSING_OPTION").trim()
                            +" "+"-t");
            successful=false;
        }
        if(!arguments.containsKey("packages"))
        {
            Vector packages = new Vector();
            packages.addElement("*ALL");
            arguments.put("packages",packages);
        }

    }
    if (successful == false)
    {
        printUsage (System.out);
        arguments = null;
    }
    return arguments;
}
// @A2a
/**
Indicates if user has selected char y(Y) or n(N).

@param  promptMsg    Message to promt.
@param  pkg          Package name.
@return true if user select y(Y), false if user select n(N).
**/

private static boolean prompt(String promptMsg, String pkg)
{
    BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
    boolean select = false;
    String message = substitute(promptMsg,pkg);

    try
    {
        while(true)
        {
            System.out.print(message);
            String resp = d.readLine();
            if(resp.equalsIgnoreCase("y")||resp.equals("1"))
            {
                select=true;
                break;
            }
            else
            if(resp.equalsIgnoreCase("n")||resp.equals("0"))
            {
                select=false;
                break;
            }
        }
    }
    catch(Exception e)
    {
        System.out.println("\n"+getMRIResource().getString("EXC_INSTALLATION_ABORTED"));
    }
    return select;
}

// @A2a
/**
Replaces a substitution variable in a string.
@param  text  The text string, with a substitution variable (e.g. "Error &0 has occurred.")
@param  value  The replacement value.
@return  The text string with the substitution variable replaced.
**/

static String substitute(String text, String value)
{
    String result = text;
    String variable = "&0";
    int j = result.indexOf (variable);
    if (j >= 0)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(result.substring(0, j));
        buffer.append(value);
        buffer.append(result.substring(j + variable.length ()));
        result = buffer.toString ();
    }
    return result;
}

/**
Returns the contents of the file.

@param     file  The file to read.

@return    The contents of the file

@exception IOException If an error occurs while communicating with the server.
**/
private static String readFile(FileInputStream file)
    throws IOException
{
    StringBuffer s = new StringBuffer();
    int n = file.read(data_);
    while (n != -1)
    {
      s.append(new String(data_, 0, n));
      n = file.read(data_);
    }
    file.close();
    return s.toString();
}


/**
Returns the contents of the file located at the URL <i>url</i>.

@param     url  The URL of the file to read.

@return    The contents of the file

@exception IOException If an error occurs while communicating with the server.
**/
private static String readFile(URL url)
    throws IOException
{
    InputStream in = url.openStream();
    String s = "";
    int n = in.read(data_);
    while ( n!= -1)
    {
        s += new String(data_, 0, n);
        n = in.read(data_);
    }
    in.close();
    return s;
}


/**
Removes a package from the target.  This method deletes all the files
in the package, and deletes directories if they are empty.
It does not modify the CLASSPATH.
<pre>
AS400ToolboxInstaller.unInstall("ACCESS", "C:\\java\\");
</pre>

@param     packageName The package to remove.
                       "*ALL" can be used to remove all the IBM
                       Toolbox for Java packages.
@param     targetPath  The path from which to remove the package.

@return    The vector of Strings which contain the names (including path) of
           files and directories which exist but could not be deleted.

@exception IOException If the package is not installed.
**/
// Note that if packages are removed individually, the JT400.PKG file
// will never be deleted from the client.  This file is only deleted
// when *ALL is specified for the package name.
public static Vector unInstall(String packageName,
                               String targetPath)
    throws IOException
{
    // Verify parms
    if (packageName == null)
        throw new NullPointerException("packageName");
    if (targetPath == null)
        throw new NullPointerException("targetPath");

    // Make sure the target path has a trailing separator.
    targetPath = addTrailingSeparator(targetPath);

    // If *ALL passed for the package, get list of packages
    // and call self with 'real' package names.
    if (packageName.equalsIgnoreCase("*ALL"))                     //@D5C
    {
        Vector packageList;
        try {
            packageList = getAllPackageNames(targetPath);
        }
        catch (FileNotFoundException e)
        {
            throw new IOException(
                getMRIResource().getString("EXC_NO_PACKAGES_INSTALLED"));
        }
        boolean packageFound = false;  // was any package installed
        Vector results = new Vector();
        Vector tempResults = new Vector();
        Vector cpr = new Vector();
        int size = packageList.size();
        for (int i=0; i<size; ++i)
        {
            // ignore errors - it's OK if not all packages installed
            try {
                tempResults = unInstall((String)packageList.elementAt(i), targetPath);
                packageFound = true;
            }
            catch (IOException e) {}
            // Add to results vector
            for (int j=0; j<tempResults.size(); ++j)
            {
                results.addElement(tempResults.elementAt(j));
            }
            // Save classpath removals
            for (int j=0; j<classpathRemovals_.size(); ++j)
            {
                if (!cpr.contains(classpathRemovals_.elementAt(j)))
                    cpr.addElement(classpathRemovals_.elementAt(j));
            }
        }

        // If no packages were found, throw an exception
        if (!packageFound)
        {
            throw new IOException(
                getMRIResource().getString("EXC_NO_PACKAGES_INSTALLED"));
        }

        classpathRemovals_ = cpr;

        // Remove package list.
        File file = new File(targetPath + "JT400.PKG");
        if (file.exists()) // don't want to fail if file doesn't exist
        {
            if (!file.delete())
                results.addElement(file.getAbsolutePath());
        }
        // Delete the target path itself
        if (targetPath.length() > 1)
        {
            file = new File(targetPath.substring(0,targetPath.length()-1));
            file.delete();
        }
        return results;
    }

    // Initialize
    classpathAdditions_.removeAllElements();
    classpathRemovals_.removeAllElements();
    unexpandedFiles_.removeAllElements();
    Vector errors = new Vector();

    // Get contents of package file list file.  An exception will
    // be thrown if the file does not exist.
    StringBuffer listFileName = new StringBuffer(targetPath);
    listFileName.append(packageName);
    listFileName.append(".LST");
    String listFile = "";
    try {
        listFile = readFile(new FileInputStream(listFileName.toString()));
    }
    catch(IOException e)
    {
        throw new IOException("(" + packageName + ") " +
            getMRIResource().getString("EXC_PACKAGE_NOT_INSTALLED"));
    }

    // Delete the package change list file if it exists.
    // Do this first, in case the uninstall bombs midstream, user
    // can recover by reinstalling.
    File file = new File(targetPath + packageName + ".LVL");
    if (file.exists())
    {
        if (!file.delete())
            errors.addElement(file.getAbsolutePath());
    }

    // Loop through lines in the list file, deleting files.
    StringTokenizer deletes = new StringTokenizer(listFile, "\n\r");
    String name, type, temp;
    Vector paths = new Vector(); // keep track of directories
    while (deletes.hasMoreTokens())
    {
        name = deletes.nextToken();
        if (changeSlash_)
            name = name.replace('/', '\\');
        file = new File(targetPath + name);
        if (file.exists()) // don't want to fail if file doesn't exist
        {
            if (!file.delete())
                errors.addElement(file.getAbsolutePath());
        }
        // Add any zip or jar files to the classpath removal list
        // if they are in the classpath, and not already in the list.
        type = name.substring(name.lastIndexOf('.')+1).toUpperCase();
        temp = (new File(targetPath + name)).getAbsolutePath();
        if ((type.equalsIgnoreCase("JAR") || type.equalsIgnoreCase("ZIP")) && //@D5C
            classpathContains(temp)!=NOT_IN_CLASSPATH &&
            !classpathRemovals_.contains(temp))
            classpathRemovals_.addElement(temp);
        // Keep track of the subdirectories so they can be deleted
        // and added to classpath removals.
        temp = file.getParent();
        if (temp != null)
        {
            if (!paths.contains(temp))
                paths.addElement(temp);
        }
    }

    // Add targetpath to the classpath removal list,
    // without trailing delimiter, if in the classpath,
    // and not already in list.
    if (targetPath.length() > 1)
    {
        temp = targetPath.substring(0,targetPath.length()-1);
        temp = (new File(temp)).getAbsolutePath();
        if (classpathContains(temp)!=NOT_IN_CLASSPATH &&
            !classpathRemovals_.contains(temp))
            classpathRemovals_.addElement(temp);
    }

    // Delete the package file list file itself.
    // Do this last, only if there are no errors, so uninstall can
    // be attempted again if we only partially uninstalled.
    if (errors.size() == 0)
    {
        file = new File(listFileName.toString());
        if (!file.delete())
            errors.addElement(file.getAbsolutePath());
    }

    // Delete all subdirectories of the target path.
    // Note a delete will fail if the directory is not empty.
    // Add subdirectories to the classpath removals if in
    // the classpath, and not already in the list.
    int size = paths.size();
    String dir;
    if (targetPath.length() > 1)
        temp = targetPath.substring(0,targetPath.length()-1);
    else
        temp = "";

    for (int j=0; j<size; ++j)
    {
        dir = (String)paths.elementAt(j);
        file = new File(dir);
        while(!file.toString().equalsIgnoreCase(temp))            //@D5C
        {
            file.delete();
            if (classpathContains(dir)!=NOT_IN_CLASSPATH &&
                    !classpathRemovals_.contains(dir))
                classpathRemovals_.addElement(dir);
            if (file.getParent() == null)
                break;
            file = new File(file.getParent());
        }
    }
    // Delete the target path itself
    if (targetPath.length() > 1)
    {
        file = new File(targetPath.substring(0,targetPath.length()-1));
        file.delete();
    }

    return errors;
}


} // end class AS400ToolboxInstaller
