///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCResultSetMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;



/**
<p>The AS400JDBCResultSetMetaData class describes the
columns in a result set.
**/
//
// Implementation notes:
//
// * I lifted the restriction that a result set be open when
//   using this object, specifically so it could be used after
//   preparing but before executing (via PreparedStatement.getMetaData()).
//
// * I also removed the reference to the result set as private
//   data.  This is again because of the need to create this object
//   before executing a query (via PreparedStatement.getMetaData()).
//
public class AS400JDBCResultSetMetaData
implements ResultSetMetaData
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private String              catalog_;
    private int                 concurrency_;
    private String              cursorName_;
	private JDRow               row_;



/**
Constructs an AS400JDBCResultSetMetaData object.

@param  catalog         The catalog.
@param  concurrency     The result set concurrency.
@param  cursorName      The cursor name.
@param  row             The row.
**/
	AS400JDBCResultSetMetaData (String catalog,
	                            int concurrency,
                                String cursorName,
	                            JDRow row)
	{
	    catalog_        = catalog;
	    concurrency_    = concurrency;
        cursorName_     = cursorName;
	    row_            = row;
	}



/**
Throws an exception if the specified column index is not
valid.

@param  columnIndex   The column index (1-based).

@exception  SQLException    If the column index is not valid.
**/
    private void checkIndex (int columnIndex)
        throws SQLException
    {
        // Validate the column index.
        if ((columnIndex < 1) || (columnIndex > row_.getFieldCount()))
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID);
    }



/**
Returns the catalog name of the table for a column.

@param  columnIndex     The column index (1-based).
@return                 The catalog name.

@exception  SQLException    If the column index is not valid.
**/
    public String getCatalogName (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
        return catalog_;
    }



// JDBC 2.0
/**
Returns the name of a Java class whose instances are
created if ResultSet.getObject() is called to retrieve
from the column.  The actual class created may be a subclass
of the returned class.

@param  columnIndex     The column index (1-based).
@return                 The class name.

@exception  SQLException    If the column index is not valid.
**/
//
// Implementation note:
//
// * In the case where the column returns a byte array, this
//   returns "[B".  See the javadoc for java.lang.Class.getName()
//   for the reason why.  The JDBC 2.0 specification does not
//   really go into detail, so it is unclear if this is okay
//   to return.  I will assume so in the mean time.
//
    public String getColumnClassName (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
        SQLData sqlData = row_.getSQLType (columnIndex);
        String className = sqlData.toObject ().getClass ().getName ();
        return className;
    }



/**
Returns the number of columns in the result set.

@return     The number of columns.

@exception  SQLException    If an error occurs.
**/
    public int getColumnCount ()
      throws SQLException
    {
		return row_.getFieldCount ();
    }



/**
Returns the normal maximum width of a column.

@param  columnIndex     The column index (1-based).
@return                 The normal maximum width
                        (in characters).

@exception  SQLException    If the column index is not valid.
**/
    public int getColumnDisplaySize (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
        return row_.getSQLType (columnIndex).getDisplaySize ();
    }



/**
Returns the suggested label for use in printouts
or displays for a column.

@param  columnIndex     The column index (1-based).
@return                 The column label.

@exception  SQLException    If the column index is not valid.
**/
//
// Implementation note:
//
// For now, this is the same as the field name.  In
// order to get the field label, we would have to make
// an extra request to the ROI server and perform a lot
// of extra processing.  This performance hit is not
// worth it.  If the caller really needs the label, then
// they can use DatabaseMetaData.getColumns.
//
    public String getColumnLabel (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return row_.getFieldName (columnIndex);
    }



/**
Returns the name of a column.

@param  columnIndex     The column index (1-based).
@return                 The column name.

@exception  SQLException    If the column index is not valid.
**/
    public String getColumnName (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return row_.getFieldName (columnIndex);
    }



/**
Returns the type of a column.  If the type is a distinct type,
this returns the underlying type.

@param  columnIndex     The column index (1-based).
@return                 The SQL type code defined in java.sql.Types.


@exception  SQLException    If the column index is not valid.
**/
    public int getColumnType (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return row_.getSQLType (columnIndex).getType ();
    }



/**
Returns the type name of a column.  If the type is a distinct
type, this returns the underlying type name.

@param  columnIndex     The column index (1-based).
@return                 The column type name.

@exception  SQLException    If the column index is not valid.
**/
    public String getColumnTypeName (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return row_.getSQLType (columnIndex).getTypeName ();
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



/**
Returns the precision of a column.  This is the number
of decimal digits the column may hold.

@param  columnIndex     The column index (1-based).
@return                 The precision.

@exception  SQLException    If the column index is not valid.
**/
    public int getPrecision (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
        return row_.getSQLType (columnIndex).getPrecision ();
    }



/**
Returns the scale of a column.  This is number of digits
to the right of the decimal point.

@param  columnIndex     The column index (1-based).
@return                 The scale.

@exception  SQLException    If the column index is not valid.
**/
    public int getScale (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return row_.getSQLType (columnIndex).getScale ();
    }



/**
Returns the schema name of the table for a column.
This method is not supported.

@param  columnIndex     The column index (1-based).
@return                 "".

@exception  SQLException    If the column index is not valid.
**/
    public String getSchemaName (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return "";
    }



/**
Returns the column's table name.  This method
is not supported.

@param  columnIndex     The column index (1-based).
@return                 "".

@exception  SQLException    If the column index is not valid.
**/
    public String getTableName (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return "";
    }



/**
Indicates if the column is automatically numbered.

@param  columnIndex     The column index (1-based).
@return                 Always false.  DB2 for OS/400
                        does not support automatically
                        numbered columns.

@exception  SQLException    If the column index is not valid.
**/
    public boolean isAutoIncrement (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return false;
    }



/**
Indicates if the column is case sensitive.

@param  columnIndex     The column index (1-based).
@return                 true if the column is case sensitive;
                        false otherwise.

@exception  SQLException    If the column index is not valid.
**/
    public boolean isCaseSensitive (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);

        // In DB2 for OS/400, all text types
        // are case sensitive.
		return row_.getSQLType (columnIndex).isText ();
    }



/**
Indicates if the column is a currency value.

@param  columnIndex     The column index (1-based).
@return                 Always false.  DB2 for OS/400
                        does not directly support currency
                        values.

@exception  SQLException    If the column index is not valid.
**/
    public boolean isCurrency (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return false;
    }



/**
Indicates if a write on the column will definitely succeed.

@param  columnIndex     The column index (1-based).
@return                 Always false.  The driver does
                        not check if the user has the
                        necessary authority to write to
                        the column.

@exception  SQLException    If the column index is not valid.
**/
    public boolean isDefinitelyWritable (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return false;
    }



/**
Indicates if the column can contain an SQL NULL value.

@param  columnIndex     The column index (1-based).
@return                 true if the column is can contain
                        an SQL NULL value; false otherwise.

@exception  SQLException    If the column index is not valid.
**/
    public int isNullable (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return row_.isNullable (columnIndex);
    }



/**
Indicates if the column is read-only.

@param  columnIndex     The column index (1-based).
@return                 true if the column is read-only;
                        false otherwise.

@exception  SQLException    If the column index is not valid.
**/
    public boolean isReadOnly (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return (concurrency_ == ResultSet.CONCUR_READ_ONLY);
    }



/**
Indicates if the column be used in a where clause.

@param  columnIndex     The column index (1-based).
@return                 Always true.  All columns can
                        be used in a where clause.

@exception  SQLException    If the column index is not valid.
**/
    public boolean isSearchable (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return true;
    }



/**
Indicates if the column can contain a signed value.

@param  columnIndex     The column index (1-based).
@return                 true if the column is signed;
                        false otherwise.

@exception  SQLException    If the column index is not valid.
**/
    public boolean isSigned (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return (row_.getSQLType (columnIndex).isSigned ());
    }



/**
Indicates if it is possible for a write on the column
to succeed.

@param  columnIndex     The column index (1-based).
@return                 true if it is possible for a write on
                        the column to succeed; false otherwise.

@exception  SQLException    If the column index is not valid.
**/
    public boolean isWritable (int columnIndex)
      throws SQLException
    {
        checkIndex (columnIndex);
		return (concurrency_ != ResultSet.CONCUR_READ_ONLY);
    }



/**
Returns the name of the SQL cursor in use by this result set.

@return     The cursor name.
**/
    public String toString ()
    {
        return cursorName_;
    }



}
