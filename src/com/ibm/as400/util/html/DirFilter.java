///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DirFilter.java
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
*  The DirFilter class determines if a File object is a directory.
*
*  <P>
*  This example creates a DirFilter object to determine which IFSJavaFile objects
*  are directories.
*  
*  <pre>
*  // Create an IFSJavaFile object.
*  IFSJavaFile root = new IFSJavaFile(system, "/QIBM");
*  <p>
*  // Create a DirFilter object.
*  DirFilter filter = new DirFilter();
*  <p>
*  // Get the list of directories.
*  File[] dirList = root.listFiles(filter);
*  <p>
*  // Create a tree element with each directory.
*  for (int i=0; i < dirList.length; i++)
*  {  
*     FileTreeElement node = new FileTreeElement(dirList[i]);                       
*     <p>
*     ServletHyperlink sl = new ServletHyperlink(urlParser.getURI());
*     sl.setHttpServletResponse(resp);
*     node.setIconUrl(sl);        
*     <p>
*     tree.addElement(node);				
*  }
*  </pre>
*
* 
*  @see com.ibm.as400.util.html.FileTreeElement
*
**/
public class DirFilter implements FilenameFilter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   /**
    *  Constructs a default DirFilter object.
    **/
	public DirFilter()
	{  
	}

   /**
    *  Determines if a <i>file</i> should be included in a list of directories.
    *
    *  @param file The directory in which the file was found. 
    *  @param filename The name of the file.
    *
    *  @return true if the file is a directory; false otherwise. 
    **/ 
	public boolean accept(File file, String filename)
	{  
		File f;
		
      if (file instanceof IFSJavaFile)
			f = new IFSJavaFile((IFSJavaFile)file, filename);
		else
			f = new File(file, filename);
		
		if (!f.isFile())    
			return true;
		else    
			return false;
   }
}
