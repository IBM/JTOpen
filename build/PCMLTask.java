///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PCMLTask.java
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
 * ANT task used to generate serialized PCML files.
**/
public class PCMLTask extends MatchingTask
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
    
    Java java = (Java)project.createTask("java");
    java.clearArgs();
    java.setClassname("com.ibm.as400.data.ProgramCallDocument");
    java.setClasspath(classpath_);
    Commandline.Argument arg1 = java.createArg();
    arg1.setValue("-serialize");
    Commandline.Argument arg2 = java.createArg();
    for (int i=0; i<f.length; ++i)
    {
      File source = new File(srcDir_, f[i]);
      File dest = new File(destDir_, f[i]+".ser");
      if (!dest.exists() ||
          (dest.exists() && dest.lastModified() < source.lastModified()))
      {
        System.out.println("Processing "+f[i]);
      
        String name = f[i].replace('\\', '.').replace('/', '.');
        arg2.setValue(name);
        java.execute();

        Move move = (Move)project.createTask("move");
        String outname = new File(f[i]).getName()+".ser";
        File outfile = new File(outname);
        move.setFile(outfile);
        move.setTofile(dest);
        move.execute();
      }
    }
  }
}


