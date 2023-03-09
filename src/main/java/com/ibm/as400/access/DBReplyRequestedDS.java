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

import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.io.StringWriter;



/**
 * Creates requested reply data streams.
**/
final class DBReplyRequestedDS extends DBBaseReplyDS
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  private static long firstUsed = 0;          // @B5A
  long                lastUsed = 0;                      // @B5A
  Exception allocatedLocation = null;           // @B5A
  int       poolIndex = -1;                              // @B5A


  public DBReplyRequestedDS(boolean setAllocatedLocation, int poolIndex)
  {
    super();
    if (setAllocatedLocation) {
    	setAllocatedLocation();
    } /*@B5A*/
    this.poolIndex = poolIndex;  /*@B5A*/
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

  void setAllocatedLocation() {
	if (firstUsed == 0) {
		firstUsed = System.currentTimeMillis();
	}
	lastUsed = System.currentTimeMillis() - firstUsed;
    allocatedLocation = new Exception("location");
    storage_.setAllocatedLocation();


  }/* @B5A*/

  String getAllocatedLocation() {
	  if (allocatedLocation == null) {
		return "NONE";
	  } else {
	  StringWriter sw = new StringWriter();
	  PrintWriter pw = new PrintWriter(sw);
	  allocatedLocation.printStackTrace(pw);
	  String result = sw.toString();
	  result = result.replace('\n', ' ');
	  return "Used at "+lastUsed+" "+result;
	  }
 } /*@B5A*/

  void returnToPool() {
	  if (poolIndex >= 0) {
		  DBDSPool.returnToDBReplyRequestedPool(poolIndex);
	  }
	  super.returnToPool();
	} /*@B5A*/


//  void setPoolIndex(int poolIndex) {
//	  if (DBDSPool.monitor) {
//		  System.out.println("Using pool at index "+poolIndex);
//	  }
//	  this.poolIndex = poolIndex;
//	  allocatedLocation = null;
//  }

}




