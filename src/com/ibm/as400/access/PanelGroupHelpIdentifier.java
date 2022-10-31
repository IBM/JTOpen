///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PanelGroupHelpIdentifier.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * Represents information about
 * a specific help identifier for an OS/400 panel group (*PNLGRP) object.
 * Use {@link com.ibm.as400.access.PanelGroup#getHelpIdentifiers PanelGroup.getHelpIdentifiers()}
 * to create an instance of this class.
 * @see com.ibm.as400.access.PanelGroup
 * @see com.ibm.as400.access.Command
 * @see com.ibm.as400.util.CommandHelpRetriever
**/
public class PanelGroupHelpIdentifier
{
  /**
   * Constant indicating that the keyword specified for this help identifier and
   * panel group was not correct.
  **/
  public static final int STATUS_NAME_NOT_CORRECT = 0;

  /**
   * Constant indicating that the keyword specified for this help identifier and
   * panel group was found and information about the help identifier was retrieved.
  **/
  public static final int STATUS_FOUND = 1;

  /**
   * Constant indicating that the system encountered a problem while accessing
   * the panel group or specified keyword information. Check the remote command
   * host server job log for details.
  **/
  public static final int STATUS_OBJECT_ACCESS_FAILURE = 2;

  /**
   * Constant indicating that the system returned a status code that is not known.
  **/
  public static final int STATUS_UNKNOWN = -1;

  private String helpID_;
  private String path_;
  private String found_;
  private String anchor_;

  PanelGroupHelpIdentifier(String helpID, String object, String library, String type,
                           String found, String anchor)
  {
    helpID_ = helpID;
    if (!type.equalsIgnoreCase("PNLGRP"))
    {
      if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Panel group help identifier constructed with wrong object type: '"+type+"'");
      throw new ExtendedIllegalArgumentException("type("+type+")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    if (object.length() == 0 || library.length() == 0)
    {
      path_ = "";
      if (Trace.isTraceOn()) Trace.log(Trace.WARNING, "Panel group help identifier not fully specified: object = '"+object+"' and library = '"+library+"'");
    }
    else
    {
      path_ = QSYSObjectPathName.toPath(library, object, type);
    }
    found_ = found;
    anchor_ = anchor;
  }

  /**
   * Returns the name of the help identifier.
   * @return The help identifier name.
  **/
  public String getName()
  {
    return helpID_;
  }

  /**
   * Returns the path of the panel group from whence this help identifier came.
   * @return The path.
  **/
  public String getPath()
  {
    return path_;
  }

  /**
   * Returns the state of the information for this help identifier.
   * Possible values are:
   * <UL>
   * <LI>{@link #STATUS_NAME_NOT_CORRECT STATUS_NAME_NOT_CORRECT} - The system
   * could not find the help identifier.
   * <LI>{@link #STATUS_FOUND STATUS_FOUND} - The system found the help identifier
   * and retrieved its information.
   * <LI>{@link #STATUS_OBJECT_ACCESS_FAILURE} - The system could not retrieve
   * the help identifier information for some reason. See the remote command host
   * server job log for details.
   * <LI>{@link #STATUS_UNKNOWN STATUS_UNKNOWN} - The system returned a help
   * identifier status that is not known.
   * </UL>
   * @return The status.
  **/
  public int getStatus()
  {
    if (found_.equals("0")) return STATUS_NAME_NOT_CORRECT;
    if (found_.equals("1")) return STATUS_FOUND;
    if (found_.equals("2")) return STATUS_OBJECT_ACCESS_FAILURE;
    return STATUS_UNKNOWN;
  }

  /**
   * Returns the name of the anchor within the help document
   * for this help identifier.
   * @return The anchor name.
  **/
  public String getAnchor()
  {
    return anchor_;
  }

  /** 
   * Returns a String representation of this help identifier.
   * @return The string.
  **/
  public String toString()
  {
    return super.toString()+"["+getStatus()+","+getName()+"]";
  }
}


