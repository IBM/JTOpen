///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMWriteCallback.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

/**
 * Used by DDMConnection to obtain the input for a write operation from the user in a memory-conscious fashion.
 * <p>Order of operations:
 * <ol>
 * <li>DDMConnection.write(file, callback)</li>
 * <li>--&gt; callback.getNumberOfRecords()</li>
 * <li>--&gt; begin loop</li>
 * <li>------&gt; callback.getRecordData()</li>
 * <li>------&gt; callback.getRecordDataOffset()</li>
 * <li>------&gt; callback.getNullFieldValues()</li>
 * <li>------&gt; Record is written</li>
 * <li>--&gt; end loop</li>
 * </ol>
**/
public interface DDMWriteCallback
{
  /**
   * Returns the number of records to write, which is how many times the DDMConnection will call getRecordData() for a given write operation.
  **/
  public int getNumberOfRecords(DDMCallbackEvent event);

  /**
   * Returns the record data to write.
  **/
  public byte[] getRecordData(DDMCallbackEvent event, int recordIndex);

  /**
   * Returns the offset into the byte array returned by the prior call to getRecordData() so that a single buffer can be used to write multiple records.
  **/
  public int getRecordDataOffset(DDMCallbackEvent event, int recordIndex);

  /**
   * Returns the array of null field values, one for each field in the record to be written.
   * Returning null indicates no fields should be written with a value of null.
  **/
  public boolean[] getNullFieldValues(DDMCallbackEvent event, int recordIndex);

}
