///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileTreeElement.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.io.File;
import java.util.Vector;
import java.util.Properties;
import java.beans.PropertyVetoException;
import java.beans.PropertyChangeSupport;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.IFSJavaFile;
import com.ibm.as400.util.servlet.ServletHyperlink;


/**
*  The FileTreeElement class represents the Integrated File System within an HTMLTree view.
*
*  <P>This example creates an FileTreeElement object:
*
*  <P>
*  <PRE>
*  // Create a URLParser object.
*  URLParser urlParser = new URLParser(httpServletRequest.getRequestURI());
*  <p>
*  // Create an AS400 object.
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
*     element.setIconUrl(sl);
*     <p>
*     // Add the FileTreeElement to the tree.
*     tree.addElement(element);
*  }
*  </PRE>
*
*  Once the elements are added to an HTMLTree object, the FileTreeElements will look like this:
*  <P>
*
*  <table cellpadding="0" cellspacing="3">
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?a=e&hc=2050603#2050603" name="2050603">+</a>
*  </td>
*  <td>
*  include
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?a=e&hc=2050584#2050584" name="2050584">+</a>
*  </td>
*  <td>
*  locales
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?a=e&hc=2050485#2050485" name="2050485">+</a>
*  </td>
*  <td>
*  ProdData
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?a=e&hc=2050574#2050574" name="2050574">+</a>
*  </td>
*  <td>
*  Test Folder
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?a=e&hc=2050555#2050555" name="2050555">+</a>
*  </td>
*  <td>
*  UserData
*  </td>
*  </tr>
*  <tr>
*  <td>
*  <a href="/servlet/myServlet?a=e&hc=2050536#2050536" name="2050536">+</a>
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private File    file_;
   private boolean populated_ = false;


   /**
    *  Constructs a default FileTreeElement object.
    **/
   public FileTreeElement()
   {
   }


   /**
    *  Constructs an FileTreeElement with the specified <i>file</i>.
    *
    *  @param file The file.
    **/
   public FileTreeElement(File file)
   {
      setFile(file);
      setText(file.getName());
   }


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
         sl.setPathInfo(file_.getAbsolutePath().replace('\\', '/'));                // @A3C

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
    *  Indicates if the FileTreeElement is a leaf.
    *
    *  @return true if the element is a leaf, false otherwise.
    **/
   public boolean isLeaf()
   {
      // We don't want the user to have to add the path and list properties to
      // the TextUrl for the parent elements in the tree.  So this is the
      // First
      if (getTextUrl() != null && getTextUrl().getProperties() == null)
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
      changes_ = new PropertyChangeSupport(this);
   }


   /**
    *  Indicates which FileTreeElement is selected.  The <i>hashcode</i> is used
    *  to determine which element within the tree to expand or collapse.
    *
    *  @param hashcode The hashcode.
    **/
   public void selected(int hashcode)
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "   FileTreeElement has been selected.");

      if (hashcode == this.hashCode())
      {
         if ((file_.isDirectory()) && (!populated_))
         {
            DirFilter filter = new DirFilter();

            // Get the list of files that satisfy the directory filter.
            String[] list = file_.list(filter);                                    //$A1C

            File[] files = new File[list.length];                                  //$A1A

            // $A1D
            // We don't want to require webservers to use JDK1.2 because
            // most webserver JVM's are slower to upgrade to the latest JDK level.
            // The most efficient way to create these file objects is to use
            // the listFiles(filter) method in JDK1.2 which would be done
            // like the following, instead of using the list(filter) method
            // and then converting the returned string arrary into the appropriate
            // File array.
            // File[] files = file_.listFiles(filter);

            for (int j=0; j<list.length; ++j)                                      //$A1A
            {
               //$A1A
               if (file_ instanceof IFSJavaFile)                                   //$A1A
                  files[j] = new IFSJavaFile((IFSJavaFile)file_, list[j]);         //$A1A
               else                                                                //$A1A
                  files[j] = new File(file_, list[j]);                             //$A1A
            }                                                                      //$A1A  @A5C

            for (int i=0; i<files.length; i++)
            {
               FileTreeElement node = new FileTreeElement(files[i]);

               if (getTextUrl() != null)
               {
                  ServletHyperlink sl = (ServletHyperlink)getTextUrl().clone();
                  sl.setPathInfo(files[i].getAbsolutePath().replace('\\','/'));      // @A2C @A3C

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

      changes_.firePropertyChange("file", old, file_);
   }


}
