///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLFileFilter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import com.ibm.as400.access.IFSJavaFile;


/**
*  The HTMLFileFilter class determines if a File object is a file.
*
*  <P>
*  This example creates a HTMLFileFilter object to determine which IFSJavaFile objects
*  are files.
*
*  <pre>
*  // Create an IFSJavaFile object.
*  IFSJavaFile root = new IFSJavaFile(system, "/QIBM");
*  <p>
*  // Create a HTMLFileFilter object.
*  HTMLFileFilter filter = new HTMLFileFilter();
*  <p>
*  // Get the list of File objects.
*  File[] files = root.listFiles(filter);
*  </pre>
*
*  @see com.ibm.as400.util.html.FileListElement
*
**/
public class HTMLFileFilter implements FilenameFilter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   /**
    *  Constructs a default HTMLFileFilter object.
    **/
     public HTMLFileFilter()
     {
     }


   /**
    *  Determines if a <i>file</i> should be included in a file list.
    *
    *  @param file The directory in which the file was found.
    *  @param filename The name of the file.
    *
    *  @return true if the File is a file; false otherwise.
    **/
     public boolean accept(File file, String filename)
     {
          File f;

      if (file instanceof IFSJavaFile)
               f = new IFSJavaFile((IFSJavaFile)file, filename);
          else
               f = new File(file, filename);

          if (!f.isFile())
               return false;
          else
               return true;
     }
}
