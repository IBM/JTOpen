///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

/**
  * IFSFileSystemView provides a gateway to the iSeries integrated file system,
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
 **/

public class IFSFileSystemView extends FileSystemView
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private AS400 system_;
    private static final String ROOT_PATH = IFSJavaFile.separator;

    private static final String DEFAULT_FOLDER_NAME = "NewDirectory";

    private static final String NEW_FOLDER_STRING0 =
    UIManager.getString("FileChooser.other.newFolder");
    private static final String NEW_FOLDER_NEXT_STRING0  =
    UIManager.getString("FileChooser.other.newFolder.subsequent");
    private static final String NEW_FOLDER_STRING = ((NEW_FOLDER_STRING0 == null || NEW_FOLDER_STRING0.length() == 0) ? DEFAULT_FOLDER_NAME : NEW_FOLDER_STRING0);
    private static final String NEW_FOLDER_NEXT_STRING = ((NEW_FOLDER_NEXT_STRING0 == null || NEW_FOLDER_NEXT_STRING0.length() == 0) ? DEFAULT_FOLDER_NAME+"{0}" : NEW_FOLDER_NEXT_STRING0);

    /**
     Constructs an IFSFileSystemView object.
     @param system The iSeries system that contains the file.
     **/
    public IFSFileSystemView(AS400 system)
    {
        if(system == null)
        {
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Returns a File object constructed in directory from the given filename.
     <br>Note: This method does not create an actual file in the file system.
     @param containingDir The directory in which to create the file.
     <br>containingDir must be of type {@link com.ibm.as400.access.IFSJavaFile IFSJavaFile}.
     @param name The file name.
     @return a File object representing the new file.
     **/
    public File createFileObject(File containingDir, String name)
    {
        try
        {
            return new IFSJavaFile((IFSJavaFile)containingDir, name);
        }
        catch(ClassCastException e)
        {
            Trace.log(Trace.ERROR, "Argument must be of type IFSJavaFile.");
            throw new ExtendedIllegalArgumentException("containingDir",
                                                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }

    /**
     Returns a File object constructed from the given path string. 
     <br>Note: This method does not create an actual file in the file system.
     @param path The file path name.
     @return the File object.
     **/
    public File createFileObject(String path)
    {
        return new IFSJavaFile(system_, path);
    }

    /**
     Creates a new folder with a default name.
     <br>Note: In the context of this class, "folder" is synonymous with "directory".
     @param containingDir The parent directory in which to create the folder.
      Must be of type {@link com.ibm.as400.access.IFSJavaFile IFSJavaFile}.
     @return a File object representing the new folder.
     **/
    public File createNewFolder(File containingDir)
    throws IOException
    {
        try
        {
            IFSJavaFile newFolder = new IFSJavaFile((IFSJavaFile)containingDir, NEW_FOLDER_STRING);
            int i=1;
            while(newFolder.exists() && (i<100))
            {
                newFolder = new IFSJavaFile((IFSJavaFile)containingDir, MessageFormat.format(
                                                                                            NEW_FOLDER_NEXT_STRING, new Object[] { new Integer(i++)}));
            }

            if(newFolder.exists())
            {
                throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
            }
            else
            {
                newFolder.mkdirs();
                return newFolder;
            }
        }
        catch(ClassCastException e)
        {
            Trace.log(Trace.ERROR, "Argument must be of type IFSJavaFile.");
            throw new ExtendedIllegalArgumentException("containingDir",
                                                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }

    /**
     Gets the list of shown (that is, not hidden) files in the directory.
     @param directory The directory to search.
     @param useFileHiding This parameter is ignored.
     @return The list of files.
     **/
    public File[] getFiles(File directory, boolean useFileHiding)
    {
        return directory.listFiles();
    }

    /**
     Returns the home directory.
     <br>The iSeries integrated file system has one home directory, the "/" directory.
     @return the home directory.
     **/
    public File getHomeDirectory()
    {
        return new IFSJavaFile(system_, ROOT_PATH);
    }

    /**
     Returns the parent directory of specified directory. 
     @param directory The directory being queried.
     <br>directory must be of type {@link com.ibm.as400.access.IFSJavaFile IFSJavaFile}.
     @return the parent directory of specified directory, or null if specified directory is null.
     **/
    public File getParentDirectory(File directory)
    {
        if(directory == null) return null;
        else return directory.getParentFile();
    }

    /**
     Returns all root partitions on this system.
     <br>The iSeries integrated file system has one root partition, the "/" directory.
     @return all root partitions on this system.
     **/
    public File[] getRoots()
    {
        return new IFSJavaFile[] { new IFSJavaFile(system_, ROOT_PATH)};
    }

    /**
     Returns whether a file is hidden or not. 
     @param File The file.
     @return true if the file is hidden.
     **/
    public boolean isHiddenFile(File file)
    {
        return file.isHidden();
    }

    /**
     Determines if the given file is a root in the navigatable tree(s).
     <br>The iSeries integrated file system has one root, the "/" directory.
     @param file A File object representing a directory.
     @return true if file is a root in the navigatable tree.
     **/
    public boolean isRoot(File file)
    {
        if(file.getAbsolutePath().equals(ROOT_PATH)) return true;
        else return false;
    }

}
