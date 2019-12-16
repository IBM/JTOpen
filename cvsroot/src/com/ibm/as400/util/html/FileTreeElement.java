///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileTreeElement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Properties;
import java.beans.PropertyVetoException;
import java.beans.PropertyChangeSupport;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.IFSJavaFile;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.util.servlet.ServletHyperlink;


/**
*  The FileTreeElement class represents the Integrated File System within an HTMLTree view.
*
*  <P>This example creates an FileTreeElement object:
*
*  <P>
*  <PRE>
*  // Create an HTMLTree object.
*  HTMLTree tree = new HTMLTree(httpServletRequest);
*  <p>
*  // Create a URLParser object.
*  URLParser urlParser = new URLParser(httpServletRequest.getRequestURI());
*  <p>
*  // Create a object to represent the connection to the system.
*  AS400 system = new AS400(mySystem, myUserId, myPassword);
*  <p>
*  // Create an IFS object.
*  IFSJavaFile root = new IFSJavaFile(system, "/QIBM");
*  <p>
*  // Create a DirFilter object and get the directories.
*  DirFilter filter = new DirFilter();
*  File[] dirList = root.listFiles(filter);
*  <p>
*
*  for (int i=0; i < dirList.length; i++)
*  {  <p>
*     // Create a FileTreeElement.
*     FileTreeElement node = new FileTreeElement(dirList[i]);
*     <p>
*     // Set the Icon URL.
*     ServletHyperlink sl = new ServletHyperlink(urlParser.getURI());
*     sl.setHttpServletResponse(resp);
*     node.setIconUrl(sl);
*     <p>
*     // Add the FileTreeElement to the tree.
*     tree.addElement(node);
*  }
*  </PRE>
*
*  Once the elements are added to an HTMLTree object, the FileTreeElements will look like this:
*  <P>
*
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?action=exapand&hashcode=2050603#2050603" name="2050603">+</a>
*  </td>
*  <td>
*  include
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?action=exapand&hashcode=2050584#2050584" name="2050584">+</a>
*  </td>
*  <td>
*  locales
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?action=exapand&hashcode=2050485#2050485" name="2050485">+</a>
*  </td>
*  <td>
*  ProdData
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?action=exapand&hashcode=2050574#2050574" name="2050574">+</a>
*  </td>
*  <td>
*  Test Folder
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?action=exapand&hashcode=2050555#2050555" name="2050555">+</a>
*  </td>
*  <td>
*  UserData
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?action=exapand&hashcode=2050536#2050536" name="2050536">+</a>
*  </td>
*  <td>
*  XML
*  </td>
*  </tr>
*  </table>
*  <P>
*  FileTreeElement objects generate the following events:
*  <ul>
*    <li><A HREF="ElementEvent.html">ElementEvent</A> - The events fired are:
*    <ul>
*       <li>elementAdded
*       <li>elementRemoved
*    </ul>
*    <li>PropertyChangeEvent
*  </ul>
*
*  @see com.ibm.as400.util.html.DirFilter
**/
public class FileTreeElement extends HTMLTreeElement implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";
  static final long serialVersionUID = 210948696422108527L;

  private File    file_;
  private boolean populated_ = false;
  private String parameter_; //@CRS

  private String shareName_;             // @B1A @CRS
  private String sharePath_;             // @B1A @CRS
  private int patternMatching_ = -1;     // @KKB


  /**
   *  Constructs a default FileTreeElement object.
   **/
  public FileTreeElement()
  {
  }


  /**
   *  Constructs a FileTreeElement with the specified <i>file</i>.
   *
   *  @param file The file.
   **/
  public FileTreeElement(File file)
  {
    setFile(file);
    setText(file.getName());
  }


  //@CRS
  /**
   *  Constructs a FileTreeElement with the specified <i>file</i>.
   *
   *  @param file The file.
   **/
  public FileTreeElement(File file, String parameter)
  {
    setFile(file);
    setText(file.getName());
    parameter_ = parameter;
  }


  /**
   *  Constructs a FileTreeElement with the specified <i>file</i>, 
   *  NetServer <i>shareName</i> and <i>sharePath</i>.
   *
   *  @param file The file.
   *  @param shareName The name of the NetServer share.
   *  @param sharePath The path of the NetServer share.
   **/
  public FileTreeElement(File file, String shareName, String sharePath)    // @B1A
  {                                                                        // @B1A
    setFile(file);                                                       // @B1A
    setShareName(shareName);                                             // @B1A
    setSharePath(sharePath);                                             // @B1A
  }                                                                        // @B1A

  //@CRS
  /**
   *  Constructs a FileTreeElement with the specified <i>file</i>, 
   *  NetServer <i>shareName</i> and <i>sharePath</i>.
   *
   *  @param file The file.
   *  @param shareName The name of the NetServer share.
   *  @param sharePath The path of the NetServer share.
   **/
  public FileTreeElement(File file, String shareName, String sharePath, String parameter)    // @B1A
  {                                                                        // @B1A
    setFile(file);                                                       // @B1A
    setShareName(shareName);                                             // @B1A
    setSharePath(sharePath);                                             // @B1A
    parameter_ = parameter;
  }                                                                        // @B1A


  /**
   *  Added the necessary properties to the text url so that if they
   *  click on the texturl and are using the FileListElement class,
   *  they will properly see the directory listing.  This will
   *  avoid externalizing the properties we are passing on the
   *  HttpServletRequest to show the FileListElements.  This only
   *  applies to FileTreeElements.
   **/
  private void addProperties()
  {
    if (getTextUrl() != null)
    {
      ServletHyperlink sl = (ServletHyperlink)getTextUrl().clone();

      // If a share name has been specified, then remove the actual path of the share from the  // @B1A
      // path info and replace it with just the share name and the directories after the share. // @B1A
      if (shareName_ != null)                                                                   // @B1A
      {
        // @B1A
        String absPath = file_.getAbsolutePath().replace('\\','/');                           // @B1A

        if (sharePath_.charAt(0) != '/')                                                      // @B1A
          absPath = absPath.substring(1);                                                   // @B1A

        if (Trace.isTraceOn())                                                                // @B1A
        {
          // @B1A
          Trace.log(Trace.INFORMATION, "FileTree absolute path: " + absPath);               // @B1A
          Trace.log(Trace.INFORMATION, "FileTree share path:    " + sharePath_);            // @B1A
        }                                                                                     // @B1A

        StringBuffer pathInfo = new StringBuffer(shareName_);                      // @B1A @CRS
        String remainingPath = absPath.substring(sharePath_.length()); //@CRS
        if (remainingPath.length() > 0 && remainingPath.charAt(0) != '/') pathInfo.append('/'); //@CRS  @KKC
        pathInfo.append(remainingPath);            // @B1A @CRS

        if (parameter_ != null) //@CRS
        {
          Properties parms = sl.getProperties(); //@CRS
          if (parms == null) parms = new Properties(); //@CRS
          parms.put(parameter_, pathInfo.toString()); //@CRS
          try { sl.setProperties(parms); } catch(PropertyVetoException pve) {} //@CRS
        }
        else //@CRS
        {
          sl.setPathInfo(pathInfo.toString());                                                  // @B1A
        }
      }                                                                                         // @B1A
      else
      {
        String pathInfo = file_.getAbsolutePath().replace('\\', '/'); //@CRS
        if (parameter_ != null) //@CRS
        {
          Properties parms = sl.getProperties(); //@CRS
          if (parms == null) parms = new Properties(); //@CRS
          parms.put(parameter_, pathInfo); //@CRS
          try { sl.setProperties(parms); } catch(PropertyVetoException pve) {} //@CRS
        }
        else //@CRS
        {
          sl.setPathInfo(pathInfo);                // @A3C @CRS
        }
      }

      if (Trace.isTraceOn())                                                                    // @B1A
      {
        Trace.log(Trace.INFORMATION, "FileTree path Info:    " + sl.getPathInfo());           // @B1A
      }

      try
      {
        sl.setText(file_.getName());
      }
      catch (PropertyVetoException e)
      { /* Ignore */
      }

      setTextUrl(sl);
    }
  }


  /**
   *  Returns the file represented by this FileTreeElement.
   **/
  public File getFile()
  {
    return file_;
  }


  /**
   *  Returns the NetServer share name.
   **/
  public String getShareName()                                // @B1A
  {                                                           // @B1A
    // Need to check for null
    // before performing a toString().
    if (shareName_ == null)
      return null;
    else
      return shareName_;                           // @B1A @CRS
  }


  /**
   *  Returns the NetServer share path.
   **/
  public String getSharePath()                                // @B1A
  {                                                           // @B1A           
    // Need to check for null
    // before performing a toString().
    if (sharePath_ == null)
      return null;
    else
      return sharePath_;                           // @B1A @CRS
  }


  /**
   *  Indicates if the FileTreeElement is a leaf.
   *
   *  @return true if the element is a leaf, false otherwise.
   **/
  public boolean isLeaf()
  {
    // We don't want the user to have to add the path and list properties to
    // the TextUrl for the parent elements in the tree.  
    if (getTextUrl() != null /*&& getTextUrl().getProperties() == null*/)     // @B2C
      addProperties();

    if (!populated_)
      return file_.isFile();
    else
      return super.isLeaf();
  }


  /**
   *  Deserializes and initializes transient data.
   **/
  private void readObject(java.io.ObjectInputStream in)
  throws java.io.IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    //@P2D changes_ = new PropertyChangeSupport(this);
  }


  /**
   *  Indicates which FileTreeElement is selected.  The <i>hashcode</i> is used
   *  to determine which element within the tree to expand or collapse.
   *
   *  @param hashcode The hashcode.
   **/
  public void selected(int hashcode)
  {
    // @C1D

    if (hashcode == this.hashCode())
    {
      if ((file_.isDirectory()) && (!populated_))
      {
        File[] files;

        if (file_ instanceof IFSJavaFile)                                   //$A1A
        {
            if(patternMatching_ != -1)   //@KKB specify the pattern matching to be used
            {
                try{
                    ((IFSJavaFile)file_).setPatternMatching(patternMatching_);
                }
                catch(IOException e)
                { 
                    // error occured setting pattern matching for the file, default value will be used
                    if(Trace.isTraceOn())
                        Trace.log(Trace.ERROR, e);
                }
            }

          // @B6A
          // When we are using IFSJavaFile objects, we can use
          // the listFiles() method becuase it is not dependant on any
          // JDK1.2 code.  Using listFiles() will also cache information
          // like if it is a directory, so we don't flow another call to the 
          // system to find that out.  We can then build both the 
          // directory and file list at the same time.
          File[] filesAndDirs = ((IFSJavaFile) file_).listFiles();       // @B6A

          // The vector of directories.
          Vector dv = new Vector();                                         // @B6A

          for (int i=0; i<filesAndDirs.length; i++)                         // @B6A
          {
            // Determine if the file is a directory or not and       // @B6A
            // add it to the appropriate directory.                     // @B6A
            if (filesAndDirs[i].isDirectory())                               // @B6A
              dv.addElement(filesAndDirs[i]);                          // @B6A
          }

          // Initialize the File arraya.                                        // @B6A
          files = new File[dv.size()];                                          // @B6A

          // Copy the vectors into their appropriate array.          // @B6A
          dv.copyInto(files);                                                    // @B6A
        }
        else  // If we are dealing with normal File objects and not IFSJavaFile objects.      //$A1A
        {
          // $A1D
          // We don't want to require webservers to use JDK1.2 because
          // most webserver JVM's are slower to upgrade to the latest JDK level.
          // The most efficient way to create these file objects is to use
          // the listFiles(filter) method in JDK1.2 which would be done
          // like the following, instead of using the list(filter) method
          // and then converting the returned string arrary into the appropriate
          // File array.
          // File[] dirList = file.listFiles(dirFilter);
          //
          // @B6A
          // We can however, use the listFiles() method on an IFSJavaFile
          // object because that is not dependant on any JDK1.2 code.
          // Using the listFiles() method on IFSJavaFile objects will
          // also cache information (ie - is it a directory) so we don't
          // have to flow another call to the system to find that information
          // out all the time.  

          // Get the list of files that satisfy the directory filter.
          // Build the File array of Directories.
          String[] list = file_.list(new DirFilter());

          files = new File[list.length];

          for (int i=0; i<list.length; ++i)
          {
            files[i] = new File(file_, list[i]);                             //$A1A
          }
        }

        for (int i=0; i<files.length; i++)
        {
          FileTreeElement node;

          // If a share name has been specified, create a FileTreeElement
          // object for the list of directories with the share name.
          if (shareName_ != null)
            node = new FileTreeElement(files[i],                            // @B1C
                                       shareName_,               // @B1C @CRS
                                       sharePath_, //@CRS
                                       parameter_);              // @B1C @CRS
          else
            node = new FileTreeElement(files[i], parameter_); //@CRS
                                                              // 
          if(patternMatching_ != -1)    // @KKB set file pattern matching for new node
              node.setPatternMatching(patternMatching_);

          if (getTextUrl() != null)
          {
            ServletHyperlink sl = (ServletHyperlink)getTextUrl().clone();

            // If a share name has been specified, then remove the actual path of the share from the        // @B1A
            // path info and replace it with just the share name and the directories after the share.       // @B1A
            if (shareName_ != null)                                                                         // @B1A
            {
              // @B1A
              String absPath = file_.getAbsolutePath().replace('\\','/');                                 // @B1A

              if (sharePath_.charAt(0) != '/')                                                            // @B1A
                absPath = absPath.substring(1);                                                         // @B1A

              if (Trace.isTraceOn())                                                                      // @B1A
                Trace.log(Trace.INFORMATION, "FileTree absolute path: " + absPath);                     // @B1A

              StringBuffer pathInfo = new StringBuffer(shareName_);                            // @B1A @CRS
              String remainingPath = absPath.substring(sharePath_.length()); //@CRS
              if (remainingPath.charAt(0) != '/') pathInfo.append('/'); //@CRS
              pathInfo.append(remainingPath);                  // @B1A @CRS

              if (parameter_ != null) //@CRS
              {
                Properties parms = sl.getProperties(); //@CRS
                if (parms == null) parms = new Properties(); //@CRS
                parms.put(parameter_, pathInfo.toString()); //@CRS
                try { sl.setProperties(parms); } catch(PropertyVetoException pve) {} //@CRS
              }
              else //@CRS
              {
                sl.setPathInfo(pathInfo.toString()); // @B1A
              }
            }                                                                                               // @B1A
            else
            {                                                                                            // @B1A
              String pathInfo = files[i].getAbsolutePath().replace('\\', '/'); //@CRS
              if (parameter_ != null) //@CRS
              {
                Properties parms = sl.getProperties(); //@CRS
                if (parms == null) parms = new Properties(); //@CRS
                parms.put(parameter_, pathInfo); //@CRS
                try { sl.setProperties(parms); } catch(PropertyVetoException pve) {} //@CRS
              }
              else //@CRS
              {
                sl.setPathInfo(pathInfo);      // @A2C @A3C @CRS
              }
            }

            try
            {
              sl.setText(files[i].getName());
            }
            catch (PropertyVetoException e)
            { /* Ignore */
            }

            node.setTextUrl(sl);
          }
          node.setIconUrl((ServletHyperlink)getIconUrl().clone());

          addElement(node);
        }
        populated_ = true;
      }
    }

    super.selected(hashcode);
  }


  /**
   *  Sets the file represented by this FileTreeElement.
   *
   *  @param file The File.
   **/
  public void setFile(File file)
  {
    if (file == null)
      throw new NullPointerException("file");

    File old = file_;

    file_ = file;

    if (changes_ != null) changes_.firePropertyChange("file", old, file_); //@P2C
  }


  /**
   *  Sets the name of the NetServer share.
   *
   *  @param shareName The share name..
   **/
  public void setShareName(String shareName)                                                               // @B1A
  {                                                                                                        // @B1A
    if (shareName == null)                                                                                // @B1A
      throw new NullPointerException("shareName");                                                       // @B1A
                                                                                                         // @B1A
    String old = shareName_;                                                                        // @B1A @CRS
                                                                                                          // @B1A
    shareName_ = shareName;                                                             // @B1A @CRS
                                                                                                          // @B1A
    if (changes_ != null) changes_.firePropertyChange("shareName", old, shareName_);   // @B1A @P2C @CRS
  }


  /**
   *  Sets the NetServer share path.
   *
   *  @param sharePath The share path.
   **/
  public void setSharePath(String sharePath)                                                               // @B1A
  {                                                                                                        // @B1A
    if (sharePath == null)                                                                                // @B1A
      throw new NullPointerException("sharePath");                                                       // @B1A
                                                                                                         // @B1A
    String old = sharePath_;                                                                        // @B1A @CRS
                                                                                                          // @B1A
    sharePath_ = sharePath;                                                             // @B1A @CRS
                                                                                                          // @B1A
//@CRS    if (Trace.isTraceOn())                                                                                // @B1A
//@CRS      Trace.log(Trace.INFORMATION, "FileTree sharePath: " + sharePath_);                                // @B1A
                                                                                                        // @B1A
    if (changes_ != null) changes_.firePropertyChange("sharePath", old, sharePath_);   // @B1A @P2C @CRS
  }


  // @KKB give users a way to specify what pattern should be used when listing files
  /**
  * Sets the pattern-matching behavior used when files are listed.  The default is PATTERN_POSIX.
  * @param patternMatching Either {@link IFSFile#PATTERN_POSIX PATTERN_POSIX}, {@link IFSFile#PATTERN_POSIX_ALL PATTERN_POSIX_ALL}, or {@link IFSFile#PATTERN_OS2 PATTERN_OS2}
  */
  public void setPatternMatching(int patternMatching){
      if(patternMatching < IFSFile.PATTERN_POSIX || patternMatching > IFSFile.PATTERN_OS2)
          throw new ExtendedIllegalArgumentException("patternMatching",
                         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      patternMatching_ = patternMatching;
  }


}
