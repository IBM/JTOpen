///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PTFExitProgram.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 * Represents an exit program for a Program Temporary Fix (PTF). 
 * Use {@link com.ibm.as400.access.PTF#getExitPrograms PTF.getExitPrograms()}
 * to generate a PTFExitProgram object.
**/
public class PTFExitProgram
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  private String path_;
  private String runOption_;
  private String userData_;

  /**
   * Constant indicating that the exit program is called to determine if there
   * is action necessary to make the PTF active or inactive.
  **/
  public static final String STAGE_ACTION = "*ACTION";

  /**
   * Constant indicating that the exit program will be run at the end of apply
   * and remove processing.
  **/
  public static final String STAGE_BOTH = "*BOTH";

  /**
   * Constant indicating that the exit program will be run at the end of apply
   * processing.
  **/
  public static final String STAGE_APPLY = "*APPLY";

  /**
   * Constant indicating that the exit program will be run at the end of remove
   * processing.
  **/
  public static final String STAGE_REMOVE = "*REMOVE";

  /**
   * Constant indicating that the exit program will be run before the PTF is applied
   * and at the end of apply processing.
  **/
  public static final String STAGE_PRE_APPLY = "*PREAPY";

  /**
   * Constant indicating that the exit program will be run before the PTF is removed
   * and at the end of remove processing.
  **/
  public static final String STAGE_PRE_REMOVE = "*PRERMV";

  /**
   * Constant indicating that the exit program will be run before the PTF is removed
   * and at the end of remove processing. It is also run before the PTF is applied and
   * at the end of apply processing.
  **/
  public static final String STAGE_PRE_BOTH = "*PREBTH";



  /**
   * Exit programs are only constructed by PTF.getExitPrograms().
  **/
  PTFExitProgram(String path, String runOption, String userData)
  {
    path_ = path;
    runOption_ = runOption;
    userData_ = userData;
  }


  /**
   * Returns the full pathname of the exit program.
   * @return The path.
  **/
  public String getPath()
  {
    return path_;
  }


  /**
   * Returns the stage of the PTF process in which the exit program
   * will be run. Possible values are:
   * <UL>
   * <LI>{@link #STAGE_ACTION STAGE_ACTION}
   * <LI>{@link #STAGE_BOTH STAGE_BOTH}
   * <LI>{@link #STAGE_APPLY STAGE_APPLY}
   * <LI>{@link #STAGE_REMOVE STAGE_REMOVE}
   * <LI>{@link #STAGE_PRE_APPLY STAGE_PRE_APPLY}
   * <LI>{@link #STAGE_PRE_REMOVE STAGE_PRE_REMOVE}
   * <LI>{@link #STAGE_PRE_BOTH STAGE_PRE_BOTH}
   * </UL>
   * @return The run stage.
  **/
  public String getRunStage()
  {
    return runOption_;
  }


  /**
   * Returns any user data associated with this exit program.
   * @return The user data.
  **/
  public String getUserData()
  {
    return userData_;
  }
}
