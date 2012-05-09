///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCResultSetMetaData.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import com.ibm.jtopenlite.database.*;
import java.sql.*;
import java.util.Calendar;

public class JDBCResultSetMetaData implements ResultSetMetaData, DatabaseDescribeCallback
{
  private Column[] columns_;
  private int offset_;
  private String catalog_;

  private final int serverCCSID_;

  private final Calendar calendar_;

  public JDBCResultSetMetaData(int serverCCSID, Calendar calendarUsedForConversions, String catalog)
  {
    serverCCSID_ = serverCCSID;
    calendar_ = calendarUsedForConversions;
    catalog_ = catalog;
  }

  public void resultSetDescription(int numFields, int dateFormat, int timeFormat, int dateSeparator, int timeSeparator, int recordSize)
  {
    columns_ = new Column[numFields];
    for (int i=0; i<numFields; ++i)
    {
      columns_[i] = new Column(calendar_, i+1, false);
      columns_[i].setDateFormat(dateFormat);
      columns_[i].setTimeFormat(timeFormat);
      columns_[i].setDateSeparator(dateSeparator);
      columns_[i].setTimeSeparator(timeSeparator);
    }
    offset_ = 0;
  }

  public void fieldDescription(int fieldIndex, int type, int length, int scale, int precision, int ccsid, int joinRefPosition, int attributeBitmap, int lobMaxSize)
  {
    columns_[fieldIndex].setType(type);
    columns_[fieldIndex].setLength(length);
    columns_[fieldIndex].setScale(scale);
    columns_[fieldIndex].setPrecision(precision);
    // Looks like this was set to translate binary.. Keep this out for now...
    // A translate binary property could be added later.
    // columns_[fieldIndex].setCCSID(ccsid == 65535 ? serverCCSID_ : ccsid);
    columns_[fieldIndex].setCCSID(ccsid);
    columns_[fieldIndex].setOffset(offset_);
    columns_[fieldIndex].setLobMaxSize(lobMaxSize);
    offset_ += length;
  }

  public void fieldName(int fieldIndex, String name)
  {
    columns_[fieldIndex].setName(name);
  }

  public void udtName(int fieldIndex, String name) {
	    columns_[fieldIndex].setUdtName(name);
  }

  public void baseColumnName(int fieldIndex, String name)
  {
  }

  public void baseTableName(int fieldIndex, String name)
  {
    columns_[fieldIndex].setTable(name);
  }

  public void columnLabel(int fieldIndex, String name)
  {
    columns_[fieldIndex].setLabel(name);
  }

  public void baseSchemaName(int fieldIndex, String name)
  {
    columns_[fieldIndex].setSchema(name);
  }

  public void sqlFromTable(int fieldIndex, String name)
  {
  }

  public void sqlFromSchema(int fieldIndex, String name)
  {
  }

  public void columnAttributes(int fieldIndex, int updateable, int searchable,
                               boolean isIdentity, boolean isAlwaysGenerated,
                               boolean isPartOfAnyIndex, boolean isLoneUniqueIndex,
                               boolean isPartOfUniqueIndex, boolean isExpression,
                               boolean isPrimaryKey, boolean isNamed,
                               boolean isRowID, boolean isRowChangeTimestamp)
  {
      if (columns_ != null) {
	  columns_[fieldIndex].setAutoIncrement(isIdentity); //TODO
	  columns_[fieldIndex].setDefinitelyWritable(updateable == 0xF1);
	  columns_[fieldIndex].setReadOnly(updateable == 0xF0);
	  columns_[fieldIndex].setSearchable(searchable != 0xF0);
	  columns_[fieldIndex].setWritable(updateable != 0xF0);
      }
  }

  /**
   * Caches the last Date returned by ResultSet.getDate(<i>column</i>), and returns that same object
   * on the next call to ResultSet.getDate(<i>column</i>) if the value returned from the database is identical.
   * This also works for repeated calls to ResultSet.getDate(<i>column</i>) when the ResultSet has not changed rows.
  **/
  public void setUseDateCache(int column, boolean b)
  {
    Column c = getColumn(column-1);
    c.setUseDateCache(b);
  }

  /**
   * You know you want this, if you're going to be calling getDate() a lot.
  **/
  public void setUseDateCache(String column, boolean b)
  {
    Column c = getColumn(column);
    c.setUseDateCache(b);
  }

  /**
   * Caches the last Time returned by ResultSet.getTime(<i>column</i>), and returns that same object
   * on the next call to ResultSet.getTime(<i>column</i>) if the value returned from the database is identical.
   * This also works for repeated calls to ResultSet.getTime(<i>column</i>) when the ResultSet has not changed rows.
  **/
  public void setUseTimeCache(int column, boolean b)
  {
    Column c = getColumn(column-1);
    c.setUseTimeCache(b);
  }

  /**
   * You know you want this, if you're going to be calling getTime() a lot.
  **/
  public void setUseTimeCache(String column, boolean b)
  {
    Column c = getColumn(column);
    c.setUseTimeCache(b);
  }

  /**
   * Caches all unique Strings returned by ResultSet.getString(<i>column</i>). Any subsequent call to
   * ResultSet.getString(<i>column</i>) will attempt to return a previously cached object if the value
   * returned from the database matches something in the cache. This also works for repeated calls to ResultSet.getString(<i>column</i>)
   * when the ResultSet has not changed rows. Note this will cache *ALL* Strings for this column, so unless you know your
   * column values will only ever be a finite set, you should also call {@link #setCacheLastOnly setCacheLastOnly()} and use an ORDER BY clause.
  **/
  public void setUseStringCache(int column, boolean b)
  {
    Column c = getColumn(column-1);
    c.setUseStringCache(b);
  }

  /**
   * You know you want this, if you're going to be calling getString() a lot.
  **/
  public void setUseStringCache(String column, boolean b)
  {
    Column c = getColumn(column);
    c.setUseStringCache(b);
  }

  /**
   * Caches the last String returned by ResultSet.getString(<i>column</i>), and returns that same object
   * on the next call to ResultSet.getString(<i>column</i>) if the value returned from the database is identical.
   * This also works for repeated calls to ResultSet.getString(<i>column</i>) when the ResultSet has not changed rows.
   * This setting only takes effect if {@link #setUseStringCache setUseStringCache()} was called with a value of <i>true</i>
   * for this column.
  **/
  public void setCacheLastOnly(int column, boolean b)
  {
    Column c = getColumn(column-1);
    c.setCacheLastOnly(b);
  }

  /**
   * You know you want this, if you're going to be calling getString() a lot.
  **/
  public void setCacheLastOnly(String column, boolean b)
  {
    Column c = getColumn(column);
    c.setCacheLastOnly(b);
  }

  Column getColumn(int fieldIndex)
  {
    return columns_[fieldIndex];
  }

  Column getColumn(String name)
  {
    for (int i=0; i<columns_.length; ++i)
    {
      if (columns_[i].getName().equals(name)) return columns_[i];
    }
    return null;
  }

  int getColumnIndex(String name)
  {
    for (int i=0; i<columns_.length; ++i)
    {
      if (columns_[i].getName().equalsIgnoreCase(name)) return i;
    }
    return -1;
  }

  ////////////////////////////
  //
  // ResultSetMetaData methods
  //
  ////////////////////////////

  /**
   * Not implemented.
  **/
  public String getCatalogName(int column) throws SQLException
  {
    checkColumn(column);

    return catalog_;
  }

  /**
   * Not implemented.
  **/
  public String getColumnClassName(int column) throws SQLException
  {
      switch (getColumnType(column)) {
      case Types.SMALLINT:            // The spec says that SMALLINT also
          // return "java.lang.Short"; //returns java.lang.Integer objects.
      case Types.INTEGER:
          return "java.lang.Integer";
      case Types.BIGINT:
          return "java.lang.Long";
      case Types.FLOAT:
          return "java.lang.Double";
      case Types.REAL:
          return "java.lang.Float";
      case Types.DOUBLE:
          return "java.lang.Double";
      case Types.OTHER:          /* DECFLOAT */
      case Types.NUMERIC:
      case Types.DECIMAL:
          return "java.math.BigDecimal";
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
          return "java.lang.String";
      case Types.BINARY:
      case Types.VARBINARY:
          return "[B";
      case Types.TIME:
          return "java.sql.Time";
      case Types.DATE:
          return "java.sql.Date";
      case Types.TIMESTAMP:
          return "java.sql.Timestamp";
      case Types.BLOB:
          return "com.ibm.db2.jdbc.app.DB2Blob";
      case Types.CLOB:
          return "com.ibm.db2.jdbc.app.DB2Clob";
      case Types.DATALINK:
	    return "java.net.URL";

      case  2009:  /*SQLXML */
    	  return "java.sql.SQLXML";

      default:
          return "UNKNOWN";
      }

  }

  public int getColumnCount() throws SQLException
  {
    return columns_.length;
  }

  /**
   * Not implemented.
  **/
  public int getColumnDisplaySize(int column) throws SQLException
  {
    switch (getColumnType(column)) {
    case Types.SMALLINT:
        return 6;
    case Types.INTEGER:
        return 11;
    case Types.BIGINT:
        return 20;
    case Types.REAL:
        return 13;
    case Types.DOUBLE:
    case Types.FLOAT:
        return 22;
    case Types.DATE:
        return 10;
    case Types.TIME:
        return 8;
    case Types.TIMESTAMP:
        return 26;
    case Types.CLOB:
    {
        return getPrecision(column) ;
    }

    case Types.DECIMAL:
    case Types.NUMERIC:
        return(getPrecision(column) + 2);  // sign and decimal point must be added.
    case Types.OTHER:    /* DECFLOAT */
    {
          int precision =  getPrecision(column);
	      switch (precision) {
	      	case 16:
		     /* add sign,decimalpoint,e,+/-,3-digitexponent */
		    precision = 23;
		  break;
	      case 34:
		  /* add sign,decimalpoint,e,+/-,4-digitexponent */
		  precision = 42;
		  break;
	      default:
	    	  precision += 2;
	      }
	  return( precision);
    }
    default:
        return getPrecision(column);
    }
  }

  public String getColumnLabel(int column) throws SQLException
  {
      checkColumn(column);
      String label =columns_[column-1].getLabel();
      if (label == null) label = columns_[column-1].getName();
      return label ;
  }

  public String getColumnName(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].getName();
  }

  public int getColumnType(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].getSQLType();
  }

  public String getColumnTypeName(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].getSQLTypeName();
  }

  public int getPrecision(int column) throws SQLException
  {
    checkColumn(column);
    return JDBCColumnMetaData.getPrecision(columns_[column-1]);

  }

  public int getScale(int column) throws SQLException
  {
      checkColumn(column);
      return JDBCColumnMetaData.getScale(columns_[column-1]);
  }

  public String getSchemaName(int column) throws SQLException
  {

      checkColumn(column);
    return columns_[column-1].getSchema();
  }

  public String getTableName(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].getTable();
  }

  public boolean isAutoIncrement(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].isAutoIncrement();
  }

  public boolean isCaseSensitive(int column) throws SQLException
  {
      switch (getColumnType(column)) {
      case Types.BIT:
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.FLOAT:
      case Types.REAL:
      case Types.DOUBLE:
      case Types.NUMERIC:
          // case Types.BINARY:     Because they are really char data, this comes back as yes it is case sensitive.
          // case Types.VARBINARY:
      case Types.DATE:
      case Types.TIME:
      case Types.TIMESTAMP:
      case Types.DECIMAL:
  case Types.OTHER:   /* DECFLOAT @C2A */
          return false;
      default:
          return true;
      }
  }

   /**
    Indicates if the column is a currency value.
    @param  columnIndex     The column index (1-based).
    @return                 Always false.  DB2 for IBM i
                            does not directly support currency
                            values.
    @exception  SQLException    If the column index is not valid.
    **/

  public boolean isCurrency(int column) throws SQLException
  {
      checkColumn(column);
      return false;
  }

  public boolean isDefinitelyWritable(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].isDefinitelyWritable();
  }

  /**
   * Not implemented.
  **/
  public int isNullable(int column) throws SQLException
  {
      checkColumn(column);
      return columns_[column-1].isNullable();
  }

  public boolean isReadOnly(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].isReadOnly();
  }

  public boolean isSearchable(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].isSearchable();
  }

  /**
   * Not implemented.
  **/
  public boolean isSigned(int column) throws SQLException
  {
      switch (getColumnType(column)) {
      case Types.BIT:
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.FLOAT:
      case Types.REAL:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.DECIMAL:
      case Types.OTHER:
          return true;
      default:
          return false;
      }

  }

  public boolean isWritable(int column) throws SQLException
  {
      checkColumn(column);
    return columns_[column-1].isWritable();
  }

  private void checkColumn(int column) throws SQLException {
      if ((column < 1) || (column > columns_.length)) {
	  JDBCError.throwSQLException(JDBCError.EXC_DESCRIPTOR_INDEX_INVALID);
      }
  }

}


