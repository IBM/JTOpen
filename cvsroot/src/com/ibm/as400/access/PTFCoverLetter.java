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

/**
 * Represents a cover letter for a Program Temporary Fix (PTF).
 * Use {@link com.ibm.as400.access.PTF#getCoverLetters PTF.getCoverLetters()}
 * to generate a PTFCoverLetter object.
**/
public class PTFCoverLetter
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private AS400 system_;
  private String nlv_;
  private String path_;
  private int preInstructions_;
  private int postInstructions_;
  private int ccsid_;

  /**
   * Constant indicating that the cover letter does not have any special instructions.
  **/
  public static final int SPECIAL_INSTRUCTIONS_NO = 0;

  /**
   * Constant indicating that the cover letter does have special instructions.
   * In the case of pre-apply or pre-remove considerations, this constant indicates
   * that the considerations should be followed regardless of how the PTF is applied
   * or removed (either immediately or during an IPL).
  **/
  public static final int SPECIAL_INSTRUCTIONS_YES = 1;

  /**
   * Constant indicating that the cover letter does have pre-apply or pre-remove 
   * special instructions, but only when the PTF is applied or removed immediately.
  **/
  public static final int SPECIAL_INSTRUCTIONS_IMMEDIATE = 2;

  /**
   * Constant indicating that the cover letter does have pre-apply or pre-remove
   * special instructions, but only when the PTF is applied or removed during an IPL.
  **/
  public static final int SPECIAL_INSTRUCTIONS_IPL = 3;

  /**
   * Constant indicating that it is not known if the cover letter has special instructions.
   * The most likely reasons are that the cover letter was created prior to V5R1M0,
   * or the cover letter was created using the System Manager licensed product.
  **/
  public static final int SPECIAL_INSTRUCTIONS_UNKNOWN = 9;



  /**
   * Cover letters are only constructed by PTF.getCoverLetters().
  **/
  PTFCoverLetter(AS400 system, String nlv, String path, int pre, int post)
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

  
  /**
   * Returns a Reader object that can be used to read the contents
   * of this cover letter. The text is automatically converted to
   * Unicode based on the NLV of this cover letter.
   * @return The cover letter reader.
  **/
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


  /**
   * Returns the National Language Version (NLV) for this cover letter.
   * @return The NLV (e.g. "2938").
  **/  
  public String getNLV()
  {
    return nlv_;
  }


  /**
   * Returns the full pathname to this cover letter on the system.
   * @return The path.
  **/
  public String getPath()
  {
    return path_;
  }


  /**
   * Returns whether the cover letter contains special instructions that should
   * be followed after applying or removing the PTF.
   * Possible values are:
   * <UL>
   * <LI>{@link #SPECIAL_INSTRUCTIONS_NO SPECIAL_INSTRUCTIONS_NO}
   * <LI>{@link #SPECIAL_INSTRUCTIONS_YES SPECIAL_INSTRUCTIONS_YES}
   * <LI>{@link #SPECIAL_INSTRUCTIONS_UNKNOWN SPECIAL_INSTRUCTIONS_UNKNOWN}
   * </UL>
   *  @return The type of post-apply or post-remove special instructions.
  **/ 
  public int getPostSpecialInstructions()
  {
    return postInstructions_;
  }
 

  /**
   * Returns whether the cover letter contains special instructions that should
   * be followed prior to applying or removing the PTF.
   * Possible values are:
   * <UL>
   * <LI>{@link #SPECIAL_INSTRUCTIONS_NO SPECIAL_INSTRUCTIONS_NO}
   * <LI>{@link #SPECIAL_INSTRUCTIONS_YES SPECIAL_INSTRUCTIONS_YES}
   * <LI>{@link #SPECIAL_INSTRUCTIONS_IMMEDIATE SPECIAL_INSTRUCTIONS_IMMEDIATE}
   * <LI>{@link #SPECIAL_INSTRUCTIONS_IPL SPECIAL_INSTRUCTIONS_IPL}
   * <LI>{@link #SPECIAL_INSTRUCTIONS_UNKNOWN SPECIAL_INSTRUCTIONS_UNKNOWN}
   * </UL>
   * @return The type of pre-apply or pre-remove special instructions.
  **/ 
  public int getPreSpecialInstructions()
  {
    return preInstructions_;
  }


  /**
   * Returns the system.
   * @return The system.
  **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
   * Returns a String representation of this cover letter. This is just the path to the cover letter.
   * @return The String object.
  **/
  public String toString()
  {
    return path_;
  }
}

