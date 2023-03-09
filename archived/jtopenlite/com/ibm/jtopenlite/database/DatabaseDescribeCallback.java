///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DatabaseDescribeCallback.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

public interface DatabaseDescribeCallback
{
  public void resultSetDescription(int numFields, int dateFormat, int timeFormat, int dateSeparator, int timeSeparator, int recordSize);

  public void fieldDescription(int fieldIndex, int type, int length, int scale, int precision, int ccsid, int joinRefPosition, int attributeBitmap, int lobMaxSize);

  public void fieldName(int fieldIndex, String name);

  public void udtName(int fieldIndex, String name);

  public void baseColumnName(int fieldIndex, String name);

  public void baseTableName(int fieldIndex, String name);

  public void columnLabel(int fieldIndex, String name);

  public void baseSchemaName(int fieldIndex, String name);

  public void sqlFromTable(int fieldIndex, String name);

  public void sqlFromSchema(int fieldIndex, String name);

  public void columnAttributes(int fieldIndex, int updateable, int searchable,
                               boolean isIdentity, boolean isAlwaysGenerated,
                               boolean isPartOfAnyIndex, boolean isLoneUniqueIndex,
                               boolean isPartOfUniqueIndex, boolean isExpression,
                               boolean isPrimaryKey, boolean isNamed,
                               boolean isRowID, boolean isRowChangeTimestamp);


}
