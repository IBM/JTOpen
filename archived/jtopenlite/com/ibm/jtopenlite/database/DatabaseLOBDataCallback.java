///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DatabaseLOBDataCallback.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

public interface DatabaseLOBDataCallback
{
  public void newLOBLength(long length);

  public void newLOBData(int ccsid, int length);

  public byte[] getLOBBuffer();

  public void setLOBBuffer(byte[] buffer);

  public void newLOBSegment(byte[] buffer, int offset, int length);
}
