///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBReplyRequestedDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
 * Creates requested reply data streams.
**/
final class DBReplyRequestedDS extends DBBaseReplyDS
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  public DBReplyRequestedDS()
  {
    super();
  }


  final public int hashCode()
  {
    return 0x2800;
  }


  /**
   * Returns a DBReplyRequestedDS
  **/
  final public Object getNewDataStream()
  {
    //@P0D return new DBReplyRequestedDS ();
    return DBDSPool.getDBReplyRequestedDS(); //@P0A
  }
}




