///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PDMLTask.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import java.io.*;
import java.util.*;

/**
 * ANT task used to generated serialized PDML files.
**/
public class PDMLTask extends MatchingTask
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  private File srcDir_;
  private File destDir_;
  private Path classpath_;

  public void setClasspath(Path s)
  {
    classpath_ = s;
  }

  public void setSrcdir(File srcDir)
  {
    srcDir_ = srcDir;
  }

  public void setDestdir(File destDir)
  {
    destDir_ = destDir;
  }

  public void execute() throws BuildException
  {
    DirectoryScanner scanner = getDirectoryScanner(srcDir_);
    String[] f = scanner.getIncludedFiles();
    
    for (int i=0; i<f.length; ++i)
    {
      System.out.println("Processing "+f[i]);
      
      Copy copy = (Copy)project.createTask("copy");
      File original = new File(scanner.getBasedir(), f[i]);
      copy.setFile(original);
      File dest = new File(destDir_, f[i]);
      copy.setTofile(dest);
      copy.execute();

      Java java = (Java)project.createTask("java");
      java.clearArgs();
      java.setFork(true);
      java.setClassname("com.ibm.as400.ui.tools.RC2XML");
      java.setClasspath(classpath_);
      Commandline.Argument arg1 = java.createArg();
      arg1.setValue("-serialize");
      Commandline.Argument arg2 = java.createArg();
      arg2.setValue(dest.getPath());
      java.execute();

      Delete delete = (Delete)project.createTask("delete");
      delete.setFile(dest);
      delete.execute();

    }
  }
}


