///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileSystemView.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSJavaFile;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.filechooser.FileSystemView;
import javax.swing.UIManager;
import javax.swing.Icon;


/**
  * IFSFileSystemView provides a gateway to the i5/OS integrated file system,
  * for use when constructing javax.swing.JFileChooser objects.
  * <p>JFileChooser is a standard Java way to build dialogs for navigating
  * and choosing files, and is the recommended replacement for
  * {@link IFSFileDialog IFSFileDialog}.
  * <p>
  * The following example demonstrates the use of IFSFileSystemView.
  * <UL>
  * <pre>
  * 
  * import com.ibm.as400.access.AS400;
  * import com.ibm.as400.access.IFSJavaFile;
  * import com.ibm.as400.vaccess.IFSFileSystemView;
  * import javax.swing.JFileChooser;
  * import java.awt.Frame;
  * 
  * // Work with directory /Dir on the system myAS400.
  * AS400 system = new AS400("myAS400");
  * IFSJavaFile dir = new IFSJavaFile(system, "/Dir");
  * JFileChooser chooser = new JFileChooser(dir, new IFSFileSystemView(system));
  * Frame parent = new Frame();
  * int returnVal = chooser.showOpenDialog(parent);
  * if (returnVal == JFileChooser.APPROVE_OPTION) {
  *    IFSJavaFile chosenFile = (IFSJavaFile)(chooser.getSelectedFile());
  *    System.out.println("You selected the file named " +
  *                        chosenFile.getName());
  * }
  * </pre>
  * </UL>
  *
  * @see com.ibm.as400.vaccess.IFSFileDialog
  * @see com.ibm.as400.access.IFSJavaFile
  * @deprecated Use <tt>com.ibm.as400.access.IFSSystemView</tt> instead.
 **/

public class IFSFileSystemView extends FileSystemView
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private static final boolean DEBUG = false;
    private AS400 system_;
    private static final String ROOT_PATH = IFSJavaFile.separator;

    private static final String DEFAULT_FOLDER_NAME = "NewFolder";

    private static final String NEW_FOLDER_STRING0 =
    UIManager.getString("FileChooser.other.newFolder");
    private static final String NEW_FOLDER_NEXT_STRING0  =
    UIManager.getString("FileChooser.other.newFolder.subsequent");
    private static final String NEW_FOLDER_STRING = ((NEW_FOLDER_STRING0 == null || NEW_FOLDER_STRING0.length() == 0) ? DEFAULT_FOLDER_NAME : NEW_FOLDER_STRING0);
    private static final String NEW_FOLDER_NEXT_STRING = ((NEW_FOLDER_NEXT_STRING0 == null || NEW_FOLDER_NEXT_STRING0.length() == 0) ? DEFAULT_FOLDER_NAME+".{0}" : NEW_FOLDER_NEXT_STRING0);

    private static Icon             serverIcon32_           = ResourceLoader.getIcon ("AS40032.gif");


    /**
     Constructs an IFSFileSystemView object.
     @param system The system that contains the file.
     **/
    public IFSFileSystemView(AS400 system)
    {
        if (system == null) throw new NullPointerException("system");

        system_ = system;
    }

    /**
     Returns a File object constructed in directory from the given filename.
     <br>Note: This method does not create an actual file in the file system.
     @param containingDir The directory in which to create the file.
     <br>containingDir is assumed to represent an existing directory on the system.  If null, it is ignored.
     @param name The file name.
     @return a File object representing the new file.
     **/
    public File createFileObject(File containingDir, String name)
    {
      if (name == null) throw new NullPointerException("name");

      IFSJavaFile file;

      if (containingDir != null) {
        IFSJavaFile ifsDir;
        if (DEBUG) {
          ifsDir = convertToIFSJavaFile(containingDir, "createFileObject");
        }
        else {
          ifsDir = convertToIFSJavaFile(containingDir);
        }
        file = new IFSJavaFile(ifsDir, name);
      }
      else {
        file = new IFSJavaFile(system_, name);
      }

      if (isFileSystemRoot(file)) return createFileSystemRoot(file);
      else return file;
    }

    /**
     Returns a File object constructed from the given path string. 
     <br>Note: This method does not create an actual file in the file system.
     @param path The file path name.
     @return the File object.
     **/
    public File createFileObject(String path)
    {
      if (path == null) throw new NullPointerException("path");
      IFSJavaFile file = new IFSJavaFile(system_, path);
      // Note: There is logic in javax.swing.plaf.basic.BasicFileChooserUI#ApproveSelectionAction.actionPerformed() that queries if the file is "absolute".  If not absolute, the logic prepends the directory path.  For that reason, each instance of IFSJavaFile needs to retain an awareness of whether it was created with an absolute path or not.
      if (isFileSystemRoot(file)) {
        return createFileSystemRoot(file);
      }
      else return file;
    }


    // Note: The method createFileSystemRoot() was added to the FileSystemView class in JDK 1.4.
    // We provide an implementation here to swallow the NoSuchMethodError if running on an older JDK.
    protected File createFileSystemRoot(File f) {
      try {
        return super.createFileSystemRoot(f);
      }
      catch (NoSuchMethodError e) {  // method added in JDK 1.4
        if (Trace.isTraceOn()) {
          Trace.log(Trace.DIAGNOSTIC, e);
        }
        return f;
      }
    }

    /**
     Creates a new folder with a default name.
     <br>Note: In the context of this class, "folder" is synonymous with "directory".
     @param containingDir The parent directory in which to create the folder.
     <br>containingDir is assumed to represent an existing directory on the system.
     @return a File object representing the new folder.
     **/
    public File createNewFolder(File containingDir)
    throws IOException
    {
      if (containingDir == null) throw new IOException("Containing directory is null:");  // This is what javax.swing.filechooser.FileSystemView does.

      IFSJavaFile ifsDir;
      if (DEBUG) {
        ifsDir = convertToIFSJavaFile(containingDir, "createNewFolder");
      }
      else {
        ifsDir = convertToIFSJavaFile(containingDir);
      }
      IFSJavaFile newFolder = new IFSJavaFile(ifsDir, NEW_FOLDER_STRING);
      int i=1;
      while (newFolder.exists() && (i<100))
      {
        newFolder = new IFSJavaFile(ifsDir,
                                    MessageFormat.format(
                                      NEW_FOLDER_NEXT_STRING, new Object[] { new Integer(i++)}));
      }

      if (newFolder.exists())
      {
        throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
      }
      else
      {
        newFolder.mkdirs();
        return newFolder;
      }
    }


    // The parent's implementation works for us.  If not, here's what we'd do:
    //  public File getChild(File parent,
    //                 String fileName)
    //  {
    //  if (parent == null) throw new NullPointerException("parent");
    //  if (fileName == null) throw new NullPointerException("fileName");
    //
    //  return createFileObject(parent, fileName);
    //  }


    /**
     * Returns the user's default starting directory for the file chooser.
     * This will represent the 'root' directory on the system.
     * @return A <code>File</code> object representing the default
     *         starting folder.
     **/
    public File getDefaultDirectory()
    {
      return createFileSystemRoot(new IFSJavaFile(system_, ROOT_PATH));
    }


    /**
     Gets the list of shown (that is, not hidden) files in the directory.
     @param directory The directory to search.
     @param useFileHiding This parameter is ignored.
     @return The list of files.
     **/
    public File[] getFiles(File directory, boolean useFileHiding)
    {
      if (directory == null) throw new NullPointerException("directory");

      IFSJavaFile ifsDir;
      if (DEBUG) {
        ifsDir = convertToIFSJavaFile(directory, "getFiles");
      }
      else {
        ifsDir = convertToIFSJavaFile(directory);
      }
      return ifsDir.listFiles();
    }

    /**
     Returns the home directory.
     <br>The i5/OS integrated file system has one home directory, the "/" directory.
     @return the home directory.
     **/
    public File getHomeDirectory()
    {
      return (new IFSJavaFile(system_, ROOT_PATH));
    }

    /**
     Returns the parent directory of <tt>dir</tt>. 
     @param dir The directory being queried.
     <br><tt>dir</tt> is assumed to represent an existing directory on the system.
     @return the parent directory of <tt>dir</tt>, or null if <tt>dir</tt> is null.
     **/
    public File getParentDirectory(File dir)
    {
      if (dir == null) return null;

      IFSJavaFile ifsDir;
      if (DEBUG) {
        ifsDir = convertToIFSJavaFile(dir, "getParentDirectory");
      }
      else {
        ifsDir = convertToIFSJavaFile(dir);
      }
      IFSJavaFile parent = (IFSJavaFile)ifsDir.getParentFile();
      if (isFileSystemRoot(parent)) {
        return createFileSystemRoot(parent);
      }
      else return parent;
    }

    /**
     Returns all root partitions on this system.
     <br>The i5/OS integrated file system has one root partition, the "/" directory.
     @return all root partitions on this system.
     **/
    public File[] getRoots()
    {
      return new File[] { createFileSystemRoot(new IFSJavaFile(system_, ROOT_PATH)) };
    }

    /**
     * Returns the name of a file, directory, or folder as it would be displayed in
     * a system file browser.
     *
     * @param f A <code>File</code> object.
     * @return The file name as it would be displayed by a native file chooser.
     **/
    public String getSystemDisplayName(File f)
    {
      if (f == null) return null;

      return f.getName();
    }

    /**
     * Always returns an icon representing an i5/OS system.
     **/
    public Icon getSystemIcon(File f)
    {
      return serverIcon32_;
    }

    /**
     * Returns a type description for a file, directory, or folder as it would be displayed in
     * a system file browser.
     * @param f A <code>File</code> object.
     * @return The file type description as it would be displayed by a native file chooser
     * or null if no native information is available.
     **/
    public String getSystemTypeDescription(File f)
    {
      return system_.getSystemName();
    }

    /**
     Always returns false.
     **/
    public boolean isComputerNode(File dir)
    {
      return false;  // no "computer nodes" in IFS
    }

    /**
     Always returns false.
     **/
    public boolean isDrive(File dir)
    {
      return false;  // no "drives" in IFS
    }

    /**
     Always returns false.
     **/
    public boolean isFloppyDrive(File dir)
    {
      return false;  // no "floppy drives" in IFS
    }

    // The parent's implementation works for us.  If not, here's what we'd do:
    //  public boolean isFileSystem(File file)
    //  {
    //    if (file == null) throw new NullPointerException("file");
    //
    //    return (!file.getPath().startsWith("ShellFolder"));
    //  }


    /**
     * Returns true if f represents the root directory on the system ("/"),
     * and false otherwise.
     *
     * @param f A <code>File</code> object representing a directory.
     * @return <code>true</code> if <code>f</code> is a root of a filesystem
     **/
    public boolean isFileSystemRoot(File f)
    {
      if (f == null) return false;

      return (f.getAbsolutePath().equals(ROOT_PATH));
    }


    // The parent's implementation works for us.  If not, here's what we'd do:
    //  public boolean isHiddenFile(File file)
    //  {
    //    if (file == null) throw new NullPointerException("file");
    //
    //    return file.isHidden();
    //  }


    // The parent's implementation works for us.  If not, here's what we'd do:
    //  public boolean isParent(File folder, File file)
    //  {
    //    if (folder == null || file == null) return false;
    //
    //    return folder.equals(file.getParentFile());
    //  }


    /**
     Determines if the given file is a root in the navigatable tree(s).
     <br>The i5/OS integrated file system has one root, the "/" directory.
     @param file A File object representing a directory.
     @return true if file is a root in the navigatable tree.
     **/
    public boolean isRoot(File file)
    {
      // Note: Normally we'd just use the parent's method, but older JDK's (pre-1.4) seem to have difficulty unless we provide our own.
      if (file == null || !file.isAbsolute()) {
        return false;
      }

      return (file.getAbsolutePath().equals(ROOT_PATH));
    }


    // The parent's implementation works for us.  If not, here's what we'd do:
    //  public Boolean isTraversable(File file)
    //  {
    //    if (file == null) throw new NullPointerException("file");
    //
    //    return Boolean.valueOf(file.isDirectory());
    //
    //  }


    // Utility method.
    private IFSJavaFile convertToIFSJavaFile(File file)
    {
      if (file instanceof IFSJavaFile) {
        return (IFSJavaFile)file;
      }

      if (Trace.isTraceOn()) {
        Trace.log(Trace.DIAGNOSTIC,
                  "File is not an IFSJavaFile.  File is of type " + file.getClass().getName());
      }
      // Create an IFSJavaFile using the path contained in the File object.
      return new IFSJavaFile(system_, file.getPath());
    }


    // Utility method.  Additionally accepts a methodName, for more-descriptive tracing.
    private IFSJavaFile convertToIFSJavaFile(File file, String methodName)
    {
      if (DEBUG) {
        if (file instanceof IFSJavaFile) {
          return (IFSJavaFile)file;
        }

        if (Trace.isTraceOn()) {
          Trace.log(Trace.DIAGNOSTIC,
                    "File is not an IFSJavaFile.  File is of type " + file.getClass().getName() + ". Caller was " + methodName + ".");
        }
        // Create an IFSJavaFile using the path contained in the File object.
        return new IFSJavaFile(system_, file.getPath());
      }
      else return convertToIFSJavaFile(file);
    }

}
