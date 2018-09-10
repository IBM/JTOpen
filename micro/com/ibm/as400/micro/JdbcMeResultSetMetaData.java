///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeResultSetMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.sql.*;

/**
 * JdbcMeResultSetMetaData implements the java.sql.ResultSetMetaData
 * interface as provided for a Java 2 Micro-Edition device.
 *  
 *  <p><b>Note:</b> Since Java 2 Micro-Edition does not include java.sql,
 *  JdbcMeResultSetMetaData implements the java.sql package that is also part 
 *  of this driver.
 **/
public class JdbcMeResultSetMetaData implements ResultSetMetaData 
{
    private int numColumns_;
    private int columnTypes_[];

    /**
     *  Default constructor.
     **/
    private JdbcMeResultSetMetaData()
    { }


    /**
     *  Constructs an JdbcMeResultSetMetaData object.
     *
     *  @param numColumns The number of columns
     *  @param columnTypes The columnTypes for a result set.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    JdbcMeResultSetMetaData(int numColumns, int columnTypes[]) throws JdbcMeException 
    {
        numColumns_ = numColumns;
        columnTypes_ = columnTypes;
    }


    /**
     *  Returns the number of columns in the result set.
     *
     *  @return     The number of columns.
     *
     *  @exception  JdbcMeException    If an error occurs.
     **/
    public int getColumnCount() throws JdbcMeException 
    {
        return numColumns_;
    }


    /**
     *  Returns the type of a column.  If the type is a distinct type,
     *  this returns the underlying type.
     *  
     *  @param  column     The column index (1-based).
     *
     *  @return                 The SQL type code defined in java.sql.Types.
     *
     *  @exception  JdbcMeException    If the column index is not valid.
     **/
    public int getColumnType(int column) throws JdbcMeException 
    {
        if (column < 1 || column > numColumns_)
            throw new JdbcMeException("Bad column index", null);

        return columnTypes_[column-1];
    }


	public String getCatalogName(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public String getColumnClassName(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public int getColumnDisplaySize(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public String getColumnLabel(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public String getColumnName(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public String getColumnTypeName(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public int getPrecision(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public int getScale(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public String getSchemaName(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public String getTableName(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public boolean isAutoIncrement(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public boolean isCaseSensitive(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public boolean isCurrency(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public boolean isDefinitelyWritable(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public int isNullable(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public boolean isReadOnly(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public boolean isSearchable(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public boolean isSigned(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}


	public boolean isWritable(int column) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}
}
