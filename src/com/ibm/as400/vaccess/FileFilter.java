///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileFilter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;
/**
   The FileFilter class provides file filtering support.
**/

public class FileFilter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String description_;
    private String pattern_;

    /**
       Constructs a FileFilter object.
    **/
    public FileFilter()
    {
        super();
    }

    /**
       Constructs a FileFilter object.
    **/
    public FileFilter(String description, String pattern)
    {
        super();
        description_ = description;
        pattern_ = pattern;
    }

   /**
      Returns the description.
      @return The description.
   **/
    public String getDescription()
    {
        return description_;
    }

    /**
       Returns the pattern.
       @return The pattern.
    **/
    public String getPattern()
    {
        return pattern_;
    }

    /**
       Sets the description.
       @param description The description.
    **/
    public void setDescription(String description)
    {
        description_ = description;
    }

    /**
       Sets the pattern.
       @param pattern The pattern.
    **/
    public void setPattern(String pattern)
    {
        pattern_ = pattern;
    }


}

