///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PTFCoverLetter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.util.*;

public class PTFCoverLetter
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private AS400 system_;
  private String nlv_;
  private String path_;
  private String preInstructions_;
  private String postInstructions_;
  private int ccsid_;

  PTFCoverLetter(AS400 system, String nlv, String path, String pre, String post)
  {
    system_ = system;
    nlv_ = nlv;
    path_ = path;
    preInstructions_ = pre;
    postInstructions_ = post;
    Enumeration locales = ConversionMaps.localeNlvMap_.keys();
    int ccsid = -1;
    while (locales.hasMoreElements() && ccsid == -1)
    {
      Object locale = locales.nextElement();
      Object nlver = ConversionMaps.localeNlvMap_.get(locale);
      if (nlver.equals(nlv_))
      {
        Enumeration ccsidLocales = ConversionMaps.localeCcsidMap_.keys();
        while (ccsidLocales.hasMoreElements() && ccsid == -1)
        {
          Object loc = ccsidLocales.nextElement();
          Object ccsidNum = ConversionMaps.localeCcsidMap_.get(loc);
          if (loc.equals(locale))
          {
            ccsid = Integer.parseInt((String)ccsidNum);
          }
        }
      }
    }
    //if (ccsid == -1) ccsid = ExecutionEnvironment.getBestGuessAS400Ccsid();
    if (ccsid == -1) ccsid = system_.getCcsid();
    ccsid_ = ccsid;
  }

  public Reader getContents()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    return new CoverLetterReader(system_, path_, ccsid_);
  }
  
  public String getNLV()
  {
    return nlv_;
  }

  public String getPath()
  {
    return path_;
  }

  public String getPostSpecialInstructions()
  {
    return postInstructions_;
  }
 
  public String getPreSpecialInstructions()
  {
    return preInstructions_;
  }

  public AS400 getSystem()
  {
    return system_;
  }

  public String toString()
  {
    return path_;
  }
}

