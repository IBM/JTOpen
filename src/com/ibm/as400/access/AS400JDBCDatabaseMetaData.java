///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400JDBCDatabaseMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;                            // @D0A



/**
The AS400JDBCDatabaseMetaData class provides information
about the database as a whole.

<p>Some of the methods in this class take arguments that are
pattern strings.  Such arguments are suffixed with "Pattern".
Within a pattern string, "%" means match any substring of zero
or more characters, and "_" means match exactly one character.
Only entries matching the pattern string are returned.

<p>For example, if the schemaPattern argument for getTables()
is "H%WO_LD", then the following schemas might match
the pattern, provided they exist on the server:
<pre>
HELLOWORLD
HIWORLD
HWORLD
HELLOWOULD
HIWOULD
</pre>

<p>Many of the methods here return lists of information in
result sets.  You can use the normal ResultSet methods to
retrieve data from these result sets.  The format of the
result sets are described in the JDBC interface specification.
**/

//-----------------------------------------------------------
// Using nulls and empty strings for catalog functions
//
//   When the parameter is NOT search pattern capable and:
//     null is specified for:
//             catalog (system) - parameter is ignored
//             schema (library) - use default library
//                                The default library can be
//                                set in the URL. If not
//                                specified in URL, the first
//                                library specified in the library
//                                properties is used as the
//                                default library.
//                                If no default library exists,
//                                QGPL is used.
//             table (file)     - empty result set is returned
//             column (field)   - empty result set is returned
//     empty string is specified for:
//             catalog (system) - empty result set is returned
//             schema (library) - empty result set is returned
//             table (file)     - empty result set is returned
//             column (field)   - empty result set is returned
//
//
//   When the parameter is search pattern capable and:
//     null is specified for:
//             schemaPattern (library) - no value sent to server.
//					 Server default of
//                                       *USRLIBL is used.
//             tablePattern (file)     - no value sent to server
//                                       server default of *ALL used
//     empty string is specified for:
//             schemaPattern (library) - empty result set is returned
//             tablePattern (file)     - empty result set is returned
//
//
//----------------------------------------------------------

public class AS400JDBCDatabaseMetaData
implements DatabaseMetaData {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private AS400JDBCConnection     connection_;
    private int                     id_;
    private SQLConversionSettings   settings_;




/**
Constructs an AS400JDBCDatabaseMetaData object.

@param   connection  The connection to the server.
@param   id          The ID the caller has assigned to this
                     AS400JDBCDatabaseMetaData.
**/
    AS400JDBCDatabaseMetaData (AS400JDBCConnection connection, int id)
    throws SQLException
    {
        connection_ = connection;
        settings_ = new SQLConversionSettings (connection);
        id_ = id;
    }



/**
Indicates if all of the procedures returned by getProcedures() can be
called by the current user.

@return     Always false.   This driver cannot determine if all of the procedures
                            can be called by the current user.
@exception  SQLException    This exception is never thrown.
**/
    public boolean allProceduresAreCallable ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if all of the tables returned by getTables() can be
SELECTed by the current user.

@return     Always false. This driver cannot determine if all of the tables
            returned by getTables() can be selected by the current user.
@exception  SQLException    This exception is never thrown.
**/
    public boolean allTablesAreSelectable ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if a data definition statement within a transaction
can force the transaction to commit.

@return     Always false. A data definition statement within a transaction
            does not force the transaction to commit.
@exception  SQLException    This exception is never thrown.
**/
    public boolean dataDefinitionCausesTransactionCommit ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if a data definition statement within a transaction is
ignored.

@return     Always false. A data definition statement within a
            transaction is not ignored.
@exception  SQLException    This exception is never thrown.
**/
    public boolean dataDefinitionIgnoredInTransactions ()
    throws SQLException
    {
        return false;
    }



// JDBC 2.0
/**
Indicates if visible deletes to a result set of the specified type
can be detected by calling ResultSet.rowDeleted().  If visible
deletes cannot be detected, then rows are removed from the
result set as they are deleted.

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     Always false.  Deletes can not be detected
                            by calling ResultSet.rowDeleted().

@exception  SQLException    If the result set type is not valid.
**/
    public boolean deletesAreDetected (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return false;
    }



/**
Indicates if getMaxRowSize() includes blobs when computing the
maximum length of a single row.

@return     Always true. getMaxRowSize() does include blobs when
            computing the maximum length of a single row.
@exception  SQLException    This exception is never thrown.
**/
    public boolean doesMaxRowSizeIncludeBlobs ()
    throws SQLException
    {
        return true;
    }



/**
Returns a description of a table's optimal set of columns
that uniquely identifies a row.


@param  catalog        The catalog name. If null is specified, this parameter
                       is ignored.  If empty string is specified,
                       an empty result set is returned.
@param  schema         The schema name. If null is specified, the
                       default library specified in the URL is used.
                       If null is specified and a default library was not
                       specified in the URL, the first library specified
                       in the libraries properties file is used.
                       If null is specified and a default library was
                       not specified in the URL and a library was not
                       specified in the libraries properties file,
                       QGPL is used.
                       If empty string is specified, an empty result set will
                       be returned.
@param  table          The table name. If null or empty string is specified,
                       an empty result set is returned.
@param  scope          The scope of interest. Valid values are:
                       bestRowTemporary and bestRowTransaction.
                       bestRowSession is not allowed because
                       it cannot be guaranteed that
                       the row will remain valid for the session.
                       If bestRowSession is specified, an empty result
                       set is returned.
                       If bestRowTransaction is specified,
                       autocommit is false, and transaction is set to repeatable read,
                       then results is returned; otherwise, an empty result set
                       is returned.
@param  nullable       The value indicating if columns that are nullable should be included.
@return                The ResultSet containing a table's optimal
                       set of columns that uniquely identify a row.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getBestRowIdentifier (String catalog,
                                           String schema,
                                           String table,
                                           int scope,
                                           boolean nullable)
    throws SQLException
    {
        connection_.checkOpen ();

        // Initialize the format of the result set.
        String[] fieldNames = { "SCOPE",
            "COLUMN_NAME",
            "DATA_TYPE",
            "TYPE_NAME",
            "COLUMN_SIZE",
            "BUFFER_LENGTH",
            "DECIMAL_DIGITS",
            "PSEUDO_COLUMN",
        };

        SQLData[] sqlData = { new SQLSmallint (), // scope
            new SQLVarchar (128, settings_),  // column name
            new SQLSmallint (),    // data type
            new SQLVarchar (128, settings_),  // type name
            new SQLInteger (),     // column size
            new SQLInteger (),     // buffer length
            new SQLSmallint (),    // decimal digits
            new SQLSmallint (),    // pseudo column
        };

        int[] fieldNullables = { columnNoNulls,  // scope
            columnNoNulls,  // column name
            columnNoNulls,  // data type
            columnNoNulls,  // type name
            columnNoNulls,  // column size
            columnNoNulls,  // buffer length
            columnNoNulls,  // decimal digits
            columnNoNulls,  // pseudo column
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // Table is null
                (table==null)      ||

                // Table is empty string
                (table.length()==0 ) ||

                // Scope.
                // If bestRowSession is specified, return empty set
                // since it can not be guaranteed that the row will
                // remain valid for the session.
                // If bestRowTemporary is specified, return results.
                // If bestRowTransaction is specified and autocommit
                // is true or transaction is not repeatableRead, return
                // empty result set

                ((scope == bestRowSession) ||

                 ( (scope == bestRowTransaction) &&
                   ( (connection_.getAutoCommit()==true) ||
                     (connection_.getTransactionIsolation() != connection_.TRANSACTION_REPEATABLE_READ))))) { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }


            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_SPECIAL_COLUMN_INFO  ,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);



                // Set the library name
                if (schema == null) {   // use default library or qgpl
                    request.setLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setLibraryName(schema, connection_.getConverter());

                // Set the table name
                request.setFileName(table, connection_.getConverter());


                // Set the Field Information to Return Bitmap
                // Return library, table, and column

                request.setSpecialColumnsReturnInfoBitmap(0x1F000000);


                // Set the short / long file and field name indicator
                request.setFileShortOrLongNameIndicator(0xF0); // Long

                // Set if columns are nullable
                request.setSpecialColumnsNullableIndicator(nullable ?
                                                           0xF1 : 0xF0);

                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();
                if (resultData != null) {
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                    JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,                                                                                     1, resultData, true));
                    JDFieldMap[] maps = new JDFieldMap[8];
                    maps[0] = new JDHardcodedFieldMap(new Short ((short) scope)); // scope
                    maps[1] = new JDSimpleFieldMap (1); // column name
                    maps[2] = new JDDataTypeFieldMap (2, 4, 3, 5);   // data type - converted to short
                    maps[3] = new JDSimpleFieldMap (2);  // type name
                    maps[4] = new JDSimpleFieldMap (4); // column size (length)
                    maps[5] = new JDHardcodedFieldMap(new Integer (0)); // buffer length
                    maps[6] = new JDSimpleFieldMap (5); // decimal digits (scale)
                    maps[7] = new JDHardcodedFieldMap(new Short ((short) versionColumnNotPseudo));
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }
                else
                    rowCache = new JDSimpleRowCache (formatRow);
            }
        }
        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "BestRowIdentifier");
    }





/**
Returns the catalog name available in this database.  This
will return a ResultSet with a single row, whose value is
the server name.

@return      The ResultSet containing the server name.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getCatalogs ()
    throws SQLException
    {
        connection_.checkOpen ();

        String[] fieldNames = {"TABLE_CAT"};
        SQLData[] sqlData   = { new SQLVarchar (128, settings_)};
        int[] fieldNullables = {columnNoNulls};    // Catalog Name
        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        Object[][] data = { { connection_.getCatalog()}};
        boolean[][] nulls = { { false}};
        JDSimpleRowCache rowCache = new JDSimpleRowCache (formatRow, data, nulls);

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "Catalogs");
    }



/**
Returns the naming convention used when referring to tables.
This depends on the naming convention specified in the connection
properties.

@return     If using SQL naming convention, "." is returned. If
            using AS/400 system naming convention, "/" is returned.

@exception  SQLException    This exception is never thrown.
**/
    public String getCatalogSeparator ()
    throws SQLException
    {
        String catalogSeparator;
        if (connection_.getProperties().equals (JDProperties.NAMING, JDProperties.NAMING_SQL))
            catalogSeparator = ".";
        else
            catalogSeparator = "/";

        return catalogSeparator;
    }



/**
Returns the DB2 for OS/400 SQL term for "catalog".

@return     The term "System".

@exception  SQLException    This exception is never thrown.
**/
    public String getCatalogTerm ()
    throws SQLException
    {
        return AS400JDBCDriver.getResource ("CATALOG_TERM");
    }



/**
Returns a description of the access rights for a table's columns.

@param  catalog         The catalog name. If null is specified, this parameter
                        is ignored.  If empty string is specified,
                        an empty result set is returned.
@param  schema          The schema name. If null is specified, the
                        default library specified in the URL is used.
                        If null is specified and a default library was not
                        specified in the URL, the first library specified
                        in the libraries properties file is used.
                        If null is specified and a default library was
                        not specified in the URL and a library was not
                        specified in the libraries properties file,
                        QGPL is used.
                        If empty string is specified, an empty result set will
                        be returned.
@param  table           The table name. If null or empty string is specified,
                        an empty result set is returned.
@param  columnPattern   The column name pattern.  If null is specified,
                        no value is sent to the server and the server
                        default of *all is used.  If empty string
                        is specified, an empty result set is returned.

@return                 The ResultSet containing access rights for a
                        table's columns.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/

    public ResultSet getColumnPrivileges (String catalog,
                                          String schema,
                                          String table,
                                          String columnPattern)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = {"TABLE_CAT",
            "TABLE_SCHEM",
            "TABLE_NAME",
            "COLUMN_NAME",
            "GRANTOR",
            "GRANTEE",
            "PRIVILEGE",
            "IS_GRANTABLE",
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_), // catalog
            new SQLVarchar (128, settings_),  // library
            new SQLVarchar (128, settings_),  // table
            new SQLVarchar (128, settings_),  // column
            new SQLVarchar (128, settings_),  // grantor
            new SQLVarchar (128, settings_),  // grantee
            new SQLVarchar (128, settings_),  // privilege
            new SQLVarchar (3, settings_),    // is_grantable
        };
        int[] fieldNullables = {columnNullable,  // catalog
            columnNullable,  // library
            columnNoNulls,   // table
            columnNoNulls,   // column
            columnNullable,  // grantor
            columnNoNulls,   // grantee
            columnNoNulls,   // privilege
            columnNullable,  // is_grantable
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows
                                     // that are readable one at a time.
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException
            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // Table is null
                table==null      ||

                // Table is empty string
                table.length()==0  ||

                // columnPattern is not null and is empty string
                ((columnPattern != null) && (columnPattern.length()==0))) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_FIELD_INFO,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

                // Set the library name
                if (schema == null) {   // use default library or qgpl
                    request.setLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setLibraryName(schema, connection_.getConverter());

                // Set the table name
                request.setFileName(table, connection_.getConverter());


                // Set the column name and search pattern
                // If null, do not set parameter. The server default
                // value of *ALL is used.
                if (!(columnPattern==null)) {
                    JDSearchPattern column = new JDSearchPattern(columnPattern);
                    request.setFieldName(column.getPatternString(), connection_.getConverter());
                    request.setFieldNameSearchPatternIndicator(column.getIndicator());
                }

                // Set the Field Information to Return Bitmap
                // Return library, table, and column
                request.setFieldReturnInfoBitmap(0xA8000000);


                // Set the short / long file and field name indicator
                request.setFileShortOrLongNameIndicator(0xF0); // Long


                // Set the Field Information Order By Indicator parameter
                // Order by: Schema and File
                request.setFileInfoOrderByIndicator (2);


                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();   
                if (resultData != null) {
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                    JDRowCache serverRowCache = new JDSimpleRowCache (
                                                                 new JDServerRowCache(row, connection_, id_,
                                                                                      1, resultData, true));
                    // Create the mapped row format that is returned in the
                    // result set.
                    // This does not actual move the data, it just sets up
                    // the mapping.
                    JDFieldMap[] maps = new JDFieldMap[8];
                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[1] = new JDSimpleFieldMap (1); // library
                    maps[2] = new JDSimpleFieldMap (2); // table
                    maps[3] = new JDSimpleFieldMap (3); // column
                    maps[4] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true ); // grantor
                    maps[5] = new JDHardcodedFieldMap (getUserName ()); // grantee - return userid
                    maps[6] = new JDHardcodedFieldMap(""); // privilege
                    maps[7] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_),true ); // is_grantable

                    // Create the mapped row cache that is returned in the
                    // result set
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);

                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }                
                else
                    rowCache = new JDSimpleRowCache (formatRow);

            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }


        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "ColumnPrivileges");
    }




/**
Returns a description of the table's columns available in a
catalog.  

<p>The following column in the description is not currently
supported:
<ul>
  <li>ORDINAL_POSITION
</ul>

@param  catalog         The catalog name. If null is specified, this parameter
                        is ignored.  If empty string is specified,
                        an empty result set is returned.
@param  schemaPattern   The schema name pattern.  If null is specified,
                        no value is sent to the server and the server
                        default of *USRLIBL is used.  If empty string
                        is specified, an empty result set is returned.
@param  tablePattern    The table name pattern. If null is specified,
                        no value is sent to the server and the server
                        default of *ALL is used.  If empty string
                        is specified, an empty result set is returned.
@param  columnPattern   The column name pattern.  If null is specified,
                        no value is sent to the server and the server
                        default of *ALL is used.  If empty string
                        is specified, an empty result set is returned.

@return                 The ResultSet containing the table's columns available
                        in a catalog.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getColumns (String catalog,
                                 String schemaPattern,
                                 String tablePattern,
                                 String columnPattern)
    throws SQLException
    {

        connection_.checkOpen ();

        // Set up the result set in the format required by JDBC
        String[] fieldNames = {"TABLE_CAT",
            "TABLE_SCHEM",
            "TABLE_NAME",
            "COLUMN_NAME",
            "DATA_TYPE",
            "TYPE_NAME",
            "COLUMN_SIZE",
            "BUFFER_LENGTH",
            "DECIMAL_DIGITS",
            "NUM_PREC_RADIX",
            "NULLABLE",
            "REMARKS",
            "COLUMN_DEF",
            "SQL_DATA_TYPE",
            "SQL_DATETIME_SUB",
            "CHAR_OCTET_LENGTH",
            "ORDINAL_POSITION",
            "IS_NULLABLE"
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_), // catalog
            new SQLVarchar (128, settings_),  // library
            new SQLVarchar (128, settings_),  // table
            new SQLVarchar (128, settings_),  // column
            new SQLSmallint (), // data type
            new SQLVarchar (128, settings_),  // type name
            new SQLInteger (),  // column size
            new SQLInteger (),  // buffer length
            new SQLInteger (), // decimal digits
            new SQLInteger (), // radix
            new SQLInteger (), // nullable
            new SQLVarchar (254, settings_),  // remarks
            new SQLVarchar (254, settings_),  // column def
            new SQLInteger (),  // sql data type
            new SQLInteger (),  // datetime sub
            new SQLInteger (),  // octet length
            new SQLInteger (),  // ordinal
            new SQLVarchar (254, settings_),  // is nullable
        };

        int[] fieldNullables = {columnNullable, // catalog
            columnNullable, // library
            columnNoNulls,  // table
            columnNoNulls,  // column
            columnNoNulls,  // data type
            columnNoNulls,  // type name
            columnNoNulls,  // column size
            columnNoNulls,  // buffer length
            columnNoNulls,  // decimal digits
            columnNoNulls,  // radix
            columnNoNulls,  // nullable
            columnNullable,  // remarks
            columnNullable,  // column def
            columnNoNulls,  // sql data type
            columnNoNulls,  // datetime sub
            columnNoNulls,  // octet length
            columnNoNulls,  // ordinal
            columnNoNulls,  // is nullable
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows that
        // are readable one at a time
        try {


            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException
            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // table is not null and is empty string
                ((tablePattern != null) && (tablePattern.length()==0))  ||

                // columnPattern is not null and is empty string
                ((columnPattern != null) && (columnPattern.length()==0))) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }


            //--------------------------------------------------
            // Set the parameters for the request
            //--------------------------------------------------


            else {  // parameter values are valid, continue to build request
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_FIELD_INFO,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);


                // Set the Library Name and Library Name Search Pattern parameters
                // If null, do not set parameter.  The server default value of
                // *USRLIBL is used
                if (schemaPattern != null) {
                    JDSearchPattern schema = new JDSearchPattern(schemaPattern);
                    request.setLibraryName (schema.getPatternString(), connection_.getConverter());
                    request.setLibraryNameSearchPatternIndicator(schema.getIndicator());
                }



                // Set the Table Name and Table Name Search Pattern parameters
                // If null, do not set parameter.  The server default value of
                // *ALL is used.
                if (tablePattern!=null) {
                    JDSearchPattern table = new JDSearchPattern(tablePattern);
                    request.setFileName (table.getPatternString(), connection_.getConverter());
                    request.setFileNameSearchPatternIndicator(table.getIndicator());
                }


                // Set the Field Name and Field Name Search Pattern parameters
                // If null, do not set parameter.  The server default value of
                // *ALL is used.
                if (columnPattern!=null) {
                    JDSearchPattern field = new JDSearchPattern(columnPattern);
                    request.setFieldName (field.getPatternString(), connection_.getConverter());
                    request.setFieldNameSearchPatternIndicator(field.getIndicator());
                }


                // Set the short / long file and field name indicator
                request.setFileShortOrLongNameIndicator(0xF0); // Long

                // Set the Field Information to Return Bitmap
                // Return everything but the reserved fields
                request.setFieldReturnInfoBitmap(0xEFF00000);


                // Set the Field Information Order By Indicator parameter
                // Order by: Schema and File
                request.setFileInfoOrderByIndicator (2);


                //-------------------------------------------------------
                // Send the request and cache all results from the server
                //-------------------------------------------------------
                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();

                // Put the data format into a row format object
                JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

                // Put the result data into a row cache
                JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                        1, resultData, true));

                // Create the mapped row format that is returned in the
                // result set.
                // This does not actual move the data, it just sets up
                // the mapping.
                JDFieldMap[] maps = new JDFieldMap[18];

                maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                maps[1] = new JDSimpleFieldMap (1); // library
                maps[2] = new JDSimpleFieldMap (3); // table
                maps[3] = new JDSimpleFieldMap (4); // column
                maps[4] = new JDDataTypeFieldMap (6, 7, 10, 11);    // Data type
                maps[5] = new JDLocalNameFieldMap (6, 7, 10, 11);     // Type name
                maps[6] = new JDPrecisionFieldMap (6, 7, 10, 11); // column size (length)
                maps[7] = new JDHardcodedFieldMap(new Integer(0)); // Buffer - not used
                maps[8] = new JDScaleFieldMap (6, 7, 10, 11); // decimal digits (scale)
                maps[9] = new JDSimpleFieldMap (9); // radix
                maps[10] = new JDNullableIntegerFieldMap(8); // is null capable?

                if (connection_.getProperties().equals (JDProperties.REMARKS, JDProperties.REMARKS_SQL))
                    maps[11] = new JDSimpleFieldMap (2);  // return remarks
                else
                    maps[11] = new JDSimpleFieldMap (5);  // return text

                // Always return null
                maps[12] = new JDHardcodedFieldMap (new SQLVarchar(0, settings_), true); // column def

                // Per JDBC api - not used - hardcode to 0
                maps[13] = new JDHardcodedFieldMap (new Integer (0)); // SQL data type

                // Per JDBC api - not used - hardcode to 0
                maps[14] = new JDHardcodedFieldMap (new Integer (0)); // SQL datetime

                maps[15] = new JDCharOctetLengthFieldMap (6, 7, 10, 11); // octet

                // Currently not returned by server
                // Always return -1
                maps[16] = new JDHardcodedFieldMap (new Integer (-1)); // ordinal position

                maps[17] = new JDNullableStringFieldMap(8);  // is Nullable


                // Create the mapped row cache that is returned in the
                // result set
                JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                rowCache = new JDMappedRowCache (mappedRow, serverRowCache);

            }  // end of else blank

        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "Columns");

    }  // End of getColumns



// JDBC 2.0
/**
Returns the connection for this metadata.

@return The connection for this metadata.

@exception  SQLException    This exception is never thrown.
**/
    public Connection getConnection ()
    throws SQLException
    {
        return connection_;
    }



    // Returns the copyright.
    private static String getCopyright()
    {
        return Copyright.copyright;
    }



/**
Returns a description of the foreign key columns in the
foreign key table that references the primary key columns
of the primary key table.  This is a description of how
the primary table imports the foreign table's key.

@param  primaryCatalog   The catalog name. If null is specified,
                         this parameter is ignored.  If
                         empty string is specified, an empty
                         result set is returned.
@param  primarySchema    The name of the schema where the primary table
                         is located.
                         If null is specified, the
                         default library specified in the URL is used.
                         If null is specified and a default library was not
                         specified in the URL, the first library specified
                         in the libraries properties file is used.
                         If null is specified,a default library was
                         not specified in the URL, and a library was not
                         specified in the libraries properties file,
                         QGPL is used.
                         If empty string is specified, an empty result
                         set is returned.
@param  primaryTable     The primary table name. If null or empty string
                         is specified, an empty result set is returned.
@param  foreignCatalog   The catalog name. If null is specified,
                         this parameter is ignored.  If
                         empty string is specified, an empty
                         result set is returned.
@param  foreignSchema    The name of the schema where the primary table
                         is located. If null is specified, the
                         default library specified in the URL is used.
                         If null is specified and a default library was not
                         specified in the URL, the first library specified
                         in the libraries properties file is used.
                         If null is specified, a default library was
                         not specified in the URL, and a library was not
                         specified in the libraries properties file,
                         QGPL is used.
                         If empty string is specified,
                         an empty result set is returned.
@param  foreignTable     The foreign table name. If null or empty string
                         is specified, an empty result set is returned.
@return                  The ResultSet containing the description of the
                         foreign key columns in the foreign key table that
                         references the primary key columns of the primary
                         key table.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/

    //-------------------------------------------------//
    //   The server returns the following:
    //   0 = cascade
    //   1 = No action or restrict
    //   2 = set null or set default
    //
    //   JDBC has 5 possible values:
    //     importedKeyNoAction
    //     importedKeyCascade
    //     importedKeySetNull
    //     importedKeySetDefault
    //     importedKeyRestrict
    //
    //   Since the server groups together
    //   some of the values, all of the
    //   possible JDBC values can not be returned.
    //
    //   For Update Rule, the only values
    //   supported by the server are
    //   no action and restrict.  Since
    //   the value of 1 is returned for
    //   both no action and restrict,
    //   the value of importKeyRestrict
    //   will always be returned for the
    //   update rule.
    //
    //   For Delete Rule
    //   the following is returned.  It is
    //   consistent with the ODBC implementation.
    //    if 0 from server = importedKeyCascade
    //    if 1 from server = importedKeyRestrict
    //    if 2 from server = importedKeySetNull
    //
    //
    //    importedKeyNoAction and importedKeySetDefault
    //    will not be returned.
    //-------------------------------------------------//

    public ResultSet getCrossReference (String primaryCatalog,
                                        String primarySchema,
                                        String primaryTable,
                                        String foreignCatalog,
                                        String foreignSchema,
                                        String foreignTable)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = {"PKTABLE_CAT",
            "PKTABLE_SCHEM",
            "PKTABLE_NAME",
            "PKCOLUMN_NAME",
            "FKTABLE_CAT",
            "FKTABLE_SCHEM",
            "FKTABLE_NAME",
            "FKCOLUMN_NAME",
            "KEY_SEQ",
            "UPDATE_RULE",
            "DELETE_RULE",
            "FK_NAME",
            "PK_NAME",
            "DEFERRABILITY",
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_), // pk catalog
            new SQLVarchar (128, settings_),  // pk schema
            new SQLVarchar (128, settings_),  // pk table
            new SQLVarchar (128, settings_),  // pk column
            new SQLVarchar (128, settings_),  // fk catalog
            new SQLVarchar (128, settings_),  // fk schema
            new SQLVarchar (128, settings_),  // fk table
            new SQLVarchar (128, settings_),  // fk column
            new SQLSmallint (),    // key seq
            new SQLSmallint (),    // update rule
            new SQLSmallint (),    // delete rule
            new SQLVarchar (128, settings_),  // fk name
            new SQLVarchar (128, settings_),  // pk name
            new SQLSmallint (),    // deferrability
        };

        int[] fieldNullables = {columnNullable,  // pk catalog
            columnNullable,  // pk schema
            columnNoNulls,  // pk table
            columnNoNulls,  // pk column
            columnNullable,  // fk catalog
            columnNullable,  // fk schema
            columnNoNulls,  // fk table
            columnNoNulls,  // fk column
            columnNoNulls,  // key seq
            columnNoNulls,  // update rule
            columnNoNulls,  // delete rule
            columnNullable,  // fk name
            columnNullable,  // pk name
            columnNoNulls,  // deferrability
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows
        // that are readable one at a time.
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(primaryCatalog)) ||     // primarycatalog is empty string
                (!isCatalogValid(foreignCatalog)) ||     // foreigncatalog is empty string

                // primarySchema is not null and is empty string
                ((primarySchema != null) && (primarySchema.length()==0)) ||
                // foreignSchema is not null and is empty string
                ((foreignSchema != null) && (foreignSchema.length()==0)) ||

                // primaryTable is null or empty string
                (primaryTable==null)  ||  (primaryTable.length()==0 ) ||
                // foreignTable is null or empty string
                (foreignTable==null)  ||  (foreignTable.length()==0 )) { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }

            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_FOREIGN_KEY_INFO ,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

                // Set the primary key file library name
                if (primarySchema == null) {   // use default library or qgpl
                    request.setPrimaryKeyFileLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setPrimaryKeyFileLibraryName(primarySchema, connection_.getConverter());

                // Set the foreign key file library name
                if (foreignSchema == null) {   // use default library or qgpl
                    request.setForeignKeyFileLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setForeignKeyFileLibraryName(foreignSchema, connection_.getConverter());


                // Set the primary key table name
                request.setPrimaryKeyFileName(primaryTable, connection_.getConverter());

                // Set the foreign key table name
                request.setForeignKeyFileName(foreignTable, connection_.getConverter());

                // Set the Foreign key Information to Return Bitmap
                request.setForeignKeyReturnInfoBitmap(0xBBE00000);


                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();
                if (resultData != null) {

                    // Put the data format into a row format object
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

                    // Put the result data into a row cache
                    JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                        1, resultData, true));

                    // Create the mapped row format that is returned in the
                    // result set.
                    // This does not actual move the data, it just sets up
                    // the mapping.

                    JDFieldMap[] maps = new JDFieldMap[14];
                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());

                    maps[1] = new JDSimpleFieldMap (1); // pk schema
                    maps[2] = new JDSimpleFieldMap (2); // pk table
                    maps[3] = new JDSimpleFieldMap (3); // pk column
                    maps[4] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[5] = new JDSimpleFieldMap (4); // fk schema
                    maps[6] = new JDSimpleFieldMap (5); // fk table
                    maps[7] = new JDSimpleFieldMap (6); // fk column
                    maps[8] = new JDSimpleFieldMap (7); // key seq
                    maps[9] = new JDUpdateDeleteRuleFieldMap (8);   // update rule
                    maps[10] = new JDUpdateDeleteRuleFieldMap (9);  // delete rule
                    maps[11] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true);
                    maps[12] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true);
                    maps[13] = new JDHardcodedFieldMap(new Short ((short) importedKeyNotDeferrable));

                    // Create the mapped row cache that is returned in the
                    // result set
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }                
                else
                    rowCache = new JDSimpleRowCache (formatRow);

            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }


        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "CrossReference");

    }  // End of getCrossReference



/**
Returns the name of this database product.

@return     The database product name.

@exception  SQLException    This exception is never thrown.
**/
    public String getDatabaseProductName ()
    throws SQLException
    {
        return AS400JDBCDriver.DATABASE_PROCUCT_NAME_; // @D2C
    }



/**
Returns the version of this database product.

@return     The product version.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public String getDatabaseProductVersion ()
    throws SQLException
    {
        // The ODBC specification suggests the
        // format "vv.rr.mmmm product-specific".
        // Although the JDBC specification does not
        // mention this format, I will adopt it anyway.
        //
        // The format of the product-specific part is
        // "VxRxmx".  I am not sure why the "m" is lowercase,
        // but somebody somewhere said this is normal, and
        // its a nit anyway.
        connection_.checkOpen();
        AS400ImplRemote as400_ = (AS400ImplRemote) connection_.getAS400 ();
        int v, r, m;
        try {
            int vrm = as400_.getVRM ();
            v = (vrm & 0xffff0000) >>> 16;                   // @D1C
            r = (vrm & 0x0000ff00) >>>  8;                   // @D1C
            m = (vrm & 0x000000ff);                          // @D1C
        } catch (Exception e) {
            JDError.throwSQLException (JDError.EXC_CONNECTION_NONE);
            return null;
        }

        StringBuffer buffer = new StringBuffer ();
        buffer.append (JDUtilities.padZeros (v, 2));
        buffer.append (".");
        buffer.append (JDUtilities.padZeros (r, 2));
        buffer.append (".");
        buffer.append (JDUtilities.padZeros (m, 4));
        buffer.append (" V");
        buffer.append (v);
        buffer.append ("R");
        buffer.append (r);
        buffer.append ("m");
        buffer.append (m);
        return buffer.toString ();
    }



/**
Returns the default transaction isolation level.

@return     The default transaction isolation level.

@exception  SQLException    This exception is never thrown.
**/
    public int getDefaultTransactionIsolation ()
    throws SQLException
    {
        String levelAsString = connection_.getProperties ().getString (JDProperties.TRANSACTION_ISOLATION);
        return JDTransactionManager.mapStringToLevel (levelAsString);
    }



/**
Returns the major version number for this JDBC driver.

@return     The major version number.
**/
    public int getDriverMajorVersion ()
    {
        return AS400JDBCDriver.MAJOR_VERSION_;
    }



/**
Returns the minor version number for this JDBC driver.

@return     The minor version number.
**/
    public int getDriverMinorVersion ()
    {
        return AS400JDBCDriver.MINOR_VERSION_;
    }



/**
Returns the name of this JDBC driver.

@return     The driver name.

@exception  SQLException    This exception is never thrown.
**/
    public String getDriverName ()
    throws SQLException
    {
        return AS400JDBCDriver.DRIVER_NAME_; // @D2C
    }



/**
Returns the version of this JDBC driver.

@return     The driver version.

@exception  SQLException    This exception is never thrown.
**/
    public String getDriverVersion ()
    throws SQLException
    {
        return AS400JDBCDriver.MAJOR_VERSION_+ "."+ AS400JDBCDriver.MINOR_VERSION_;
    }



/**
Returns a description of the foreign key columns that
reference a table's primary key columns.  This is the
foreign keys exported by a table.

@param  catalog        The catalog name. If null is specified, this parameter
                       is ignored.  If empty string is specified,
                       an empty result set is returned.
@param  schema         The schema name. If null is specified, the
                       default library specified in the URL is used.
                       If null is specified and a default library was not
                       specified in the URL, the first library specified
                       in the libraries properties file is used.
                       If null is specified, a default library was
                       not specified in the URL, and a library was not
                       specified in the libraries properties file,
                       QGPL is used.
                       If empty string is specified, an empty result set will
                       be returned.
@param  table          The table name. If null or empty string is specified,
                       an empty result set is returned.

@return                The ResultSet containing the description of the
                       foreign key columns that reference a table's
                       primary key columns.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getExportedKeys (String catalog,
                                      String schema,
                                      String table)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------


        String[] fieldNames = {"PKTABLE_CAT",
            "PKTABLE_SCHEM",
            "PKTABLE_NAME",
            "PKCOLUMN_NAME",
            "FKTABLE_CAT",
            "FKTABLE_SCHEM",
            "FKTABLE_NAME",
            "FKCOLUMN_NAME",
            "KEY_SEQ",
            "UPDATE_RULE",
            "DELETE_RULE",
            "FK_NAME",
            "PK_NAME",
            "DEFERRABILITY",
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_), // pk catalog
            new SQLVarchar (128, settings_),  // pk schema
            new SQLVarchar (128, settings_),  // pk table
            new SQLVarchar (128, settings_),  // pk column
            new SQLVarchar (128, settings_),  // fk catalog
            new SQLVarchar (128, settings_),  // fk schema
            new SQLVarchar (128, settings_),  // fk table
            new SQLVarchar (128, settings_),  // fk column
            new SQLSmallint (),    // key seq
            new SQLSmallint (),    // update rule
            new SQLSmallint (),    // delete rule
            new SQLVarchar (128, settings_),  // fk name
            new SQLVarchar (128, settings_),  // pk name
            new SQLSmallint (),    // deferrability
        };
        int[] fieldNullables = {columnNullable,  // pk catalog
            columnNullable,  // pk schema
            columnNoNulls,  // pk table
            columnNoNulls,  // pk column
            columnNullable,  // fk catalog
            columnNullable,  // fk schema
            columnNoNulls,  // fk table
            columnNoNulls,  // fk column
            columnNoNulls,  // key seq
            columnNoNulls,  // update rule
            columnNoNulls,  // delete rule
            columnNullable,  // fk name
            columnNullable,  // pk name
            columnNoNulls,  // deferrability
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows
                                     // that are readable one at a time.
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string


                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // table is null or empty string
                (table==null)  ||  (table.length()==0 )) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_FOREIGN_KEY_INFO ,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);


                // Set the primary key file library name
                if (schema == null) {   // use default library or qgpl
                    request.setPrimaryKeyFileLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setPrimaryKeyFileLibraryName(schema, connection_.getConverter());



                // Set the primary key table name
                request.setPrimaryKeyFileName(table, connection_.getConverter());


                // Set the Foreign key Information to Return Bitmap
                request.setForeignKeyReturnInfoBitmap(0xBBE00000);


                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();

                if (resultData != null) {
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                    JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                        1, resultData, true));

                    JDFieldMap[] maps = new JDFieldMap[14];
                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[1] = new JDSimpleFieldMap (1); // pk schema
                    maps[2] = new JDSimpleFieldMap (2); // pk table
                    maps[3] = new JDSimpleFieldMap (3); // pk column
                    maps[4] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[5] = new JDSimpleFieldMap (4); // fk schema
                    maps[6] = new JDSimpleFieldMap (5); // fk table
                    maps[7] = new JDSimpleFieldMap (6); // fk column
                    maps[8] = new JDSimpleFieldMap (7); // key seq
                    maps[9] = new JDSimpleFieldMap (8); // update rule
                    maps[10] = new JDSimpleFieldMap (9);    // delete rule
                    maps[11] = new JDHardcodedFieldMap (new SQLVarchar(0, settings_), true);
                    maps[12] = new JDHardcodedFieldMap (new SQLVarchar(0, settings_), true);
                    maps[13] = new JDHardcodedFieldMap (new Short ((short) importedKeyNotDeferrable));

                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }
                else
                    rowCache = new JDSimpleRowCache (formatRow);
            }  
        } 
        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "ExportedKeys");
    }



/**
Returns all of the "extra" characters that can be used in
unquoted identifier names (those beyond a-z, A-Z, 0-9,
and _).

@return     The String containing the "extra" characters.

@exception  SQLException    This exception is never thrown.
**/
    public String getExtraNameCharacters ()
    throws SQLException
    {
        return "$@#";
    }



/**
Returns the string used to quote SQL identifiers.

@return     The quote string.

@exception  SQLException    This exception is never thrown.
**/
    public String getIdentifierQuoteString ()
    throws SQLException
    {
        return "\"";
    }



/**
Returns a description of the primary key columns that are
referenced by a table's foreign key columns.  This is the
primary keys imported by a table.

@param  catalog        The catalog name. If null is specified, this parameter
                       is ignored.  If empty string is specified,
                       an empty result set is returned.
@param  schema         The schema name. If null is specified, the
                       default library specified in the URL is used.
                       If null is specified and a default library was not
                       specified in the URL, the first library specified
                       in the libraries properties file is used.
                       If null is specified, a default library was
                       not specified in the URL, and a library was not
                       specified in the libraries properties file,
                       QGPL is used.
                       If empty string is specified, an empty result set will
                       be returned.
@param  table          The table name. If null or empty string is specified,
                       an empty result set is returned.

@return                The ResultSets containing the description of the primary
                       key columns that are referenced by a table's foreign
                       key columns.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getImportedKeys (String catalog,
                                      String schema,
                                      String table)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = {"PKTABLE_CAT",
            "PKTABLE_SCHEM",
            "PKTABLE_NAME",
            "PKCOLUMN_NAME",
            "FKTABLE_CAT",
            "FKTABLE_SCHEM",
            "FKTABLE_NAME",
            "FKCOLUMN_NAME",
            "KEY_SEQ",
            "UPDATE_RULE",
            "DELETE_RULE",
            "FK_NAME",
            "PK_NAME",
            "DEFERRABILITY",
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_), // pk catalog
            new SQLVarchar (128, settings_),  // pk schema
            new SQLVarchar (128, settings_),  // pk table
            new SQLVarchar (128, settings_),  // pk column
            new SQLVarchar (128, settings_),  // fk catalog
            new SQLVarchar (128, settings_),  // fk schema
            new SQLVarchar (128, settings_),  // fk table
            new SQLVarchar (128, settings_),  // fk column
            new SQLSmallint (),    // key seq
            new SQLSmallint (),    // update rule
            new SQLSmallint (),    // delete rule
            new SQLVarchar (128, settings_),  // fk name
            new SQLVarchar (128, settings_),  // pk name
            new SQLSmallint (),    // deferrability
        };

        int[] fieldNullables = {columnNullable,  // pk catalog
            columnNullable,  // pk schema
            columnNoNulls,  // pk table
            columnNoNulls,  // pk column
            columnNullable,  // fk catalog
            columnNullable,  // fk schema
            columnNoNulls,  // fk table
            columnNoNulls,  // fk column
            columnNoNulls,  // key seq
            columnNoNulls,  // update rule
            columnNoNulls,  // delete rule
            columnNullable,  // fk name
            columnNullable,  // pk name
            columnNoNulls,  // deferrability
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows
        // that are readable one at a time.
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // table is null or empty string
                (table==null)  ||  (table.length()==0 )) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_FOREIGN_KEY_INFO ,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);




                // Set the foreign key file library name
                if (schema == null) {   // use default library or qgpl
                    request.setForeignKeyFileLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setForeignKeyFileLibraryName(schema, connection_.getConverter());


                // Set the foreign key table name
                request.setForeignKeyFileName(table, connection_.getConverter());

                // Set the Foreign key Information to Return Bitmap
                request.setForeignKeyReturnInfoBitmap(0xBBE00000);

                // This is not documented in the LIPI, but it happens to work!           @E2A
                request.setFileShortOrLongNameIndicator(0xF0);                        // @E2A

                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();
                if (resultData != null) {
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                    JDRowCache serverRowCache =  new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                         1, resultData, true));
                    JDFieldMap[] maps = new JDFieldMap[14];
                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[1] = new JDSimpleFieldMap (1); // pk schema
                    maps[2] = new JDSimpleFieldMap (2); // pk table
                    maps[3] = new JDSimpleFieldMap (3); // pk column
                    maps[4] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[5] = new JDSimpleFieldMap (4); // fk schema
                    maps[6] = new JDSimpleFieldMap (5); // fk table
                    maps[7] = new JDSimpleFieldMap (6); // fk column
                    maps[8] = new JDSimpleFieldMap (7); // key seq
                    maps[9] = new JDSimpleFieldMap (8); // update rule
                    maps[10] = new JDSimpleFieldMap (9);    // delete rule
                    maps[11] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true);
                    maps[12] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true);
                    maps[13] = new JDHardcodedFieldMap(new Short ((short) importedKeyNotDeferrable));

                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }
                else
                    rowCache = new JDSimpleRowCache (formatRow);

            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "ImportedKeys");
    }



/**
Returns a description of a table's indexes and statistics.

@param  catalog      The catalog name. If null is specified, this parameter
                     is ignored.  If empty string is specified,
                     an empty result set is returned.
@param  schema       The schema name. If null is specified, the
                     default library specified in the URL is used.
                     If null is specified and a default library was not
                     specified in the URL, the first library specified
                     in the libraries properties file is used.
                     If null is specified, a default library was
                     not specified in the URL, and a library was not
                     specified in the libraries properties file,
                     QGPL is used.
                     If empty string is specified, an empty result set will
                     be returned.
@param  table        The table name. If null or empty string is specified,
                     an empty result set is returned.
@param  unique       The value indicating if unique indexes should be returned.
                     If true, only indexes for unique values is returned.
                     If false, all indexes is returned.
@param  approximate  The value indicating if the result is allowed to reflect
                     approximate or out-of-date values.  This value is ignored.
@return              The ResultSet containing the description of a table's indexes
                     and statistics.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getIndexInfo (String catalog,
                                   String schema,
                                   String table,
                                   boolean unique,
                                   boolean approximate)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = {"TABLE_CAT",
            "TABLE_SCHEM",
            "TABLE_NAME",
            "NON_UNIQUE",
            "INDEX_QUALIFIER",
            "INDEX_NAME",
            "TYPE",
            "ORDINAL_POSITION",
            "COLUMN_NAME",
            "ASC_OR_DESC",
            "CARDINALITY",
            "PAGES",
            "FILTER_CONDITION",
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_), // catalog
            new SQLVarchar (128, settings_),  // schema
            new SQLVarchar (128, settings_),  // table
            // when instantiating the non-unique small int
            // pass in a boolean and it will give it the
            // right value
            new SQLSmallint (),    // non-unique - boolean
            new SQLVarchar (128, settings_),  // index qualifier
            new SQLVarchar (128, settings_),  // index name
            new SQLSmallint (),    // type
            new SQLSmallint (),    // ordinal position
            new SQLVarchar (128, settings_),  // column name
            new SQLVarchar (1, settings_),    // sort sequence
            new SQLInteger  (),    // cardinality
            new SQLInteger  (),    // pages
            new SQLVarchar (128, settings_),  // filter condition
        };

        int[] fieldNullables = {  columnNullable,  // catalog
            columnNullable,  // schema
            columnNoNulls,   // table
            columnNoNulls,   // non-unique
            columnNullable,  // index qualifier
            columnNullable,  // index name
            columnNoNulls,   // type
            columnNoNulls,   // ordinal position
            columnNullable,  // column name
            columnNullable,  // sort sequence
            columnNoNulls,   // cardinality
            columnNoNulls,   // pages
            columnNullable,  // filter condition
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows
        // that are readable one at a time.
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // table is null or empty string
                (table==null)  ||  (table.length()==0 )) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_INDEX_INFO ,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

                // Set the library name
                if (schema == null) {   // use default library or qgpl
                    request.setLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setLibraryName(schema, connection_.getConverter());


                // Set the table name
                request.setFileName(table, connection_.getConverter());

                // Set the long file name indicator
                request.setFileShortOrLongNameIndicator (0xF0); // Long table names

                // Set the index unique rule
                if (unique) {  // true - return indices for unique values
                    request.setIndexUniqueRule(0x01);
                } else {
                    request.setIndexUniqueRule(0x04);
                }

                // Set the Index Information to Return Bitmap
                request.setIndexReturnInfoBitmap(0xBDC00000);



                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();
                if (resultData != null) {

                    // Put the data format into a row object
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

                    // Put the result data into a row cache
                    JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                        1, resultData, true));

                    // Create the mapped row format that is returned in the
                    // result set.
                    // This does not actual move the data, it just sets up
                    // the mapping.
                    JDFieldMap[] maps = new JDFieldMap[13];
                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());

                    maps[1] = new JDSimpleFieldMap (1); // schema
                    maps[2] = new JDSimpleFieldMap (2); // table
                    maps[3] = new JDNonUniqueFieldMap (3);  // non-unique
                    maps[4] = new JDSimpleFieldMap (4); // index library name
                    maps[5] = new JDSimpleFieldMap (5); // index name
                    maps[6] = new JDHardcodedFieldMap(new Short ((short) tableIndexOther)); // type

                    maps[7] = new JDSimpleFieldMap (7); // ordinal position
                    maps[8] = new JDSimpleFieldMap (6); // column name
                    maps[9] = new JDSimpleFieldMap (8); // sort sequence (collation)
                    // cardinality unknown
                    maps[10] = new JDHardcodedFieldMap(new Integer(-1));
                    // number of pages unknown
                    maps[11] = new JDHardcodedFieldMap(new Integer(-1));
                    maps[12] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true);

                    // Create the mapped row cache that is returned in the
                    // result set
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }
                else
                    rowCache = new JDSimpleRowCache (formatRow);

            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "IndexInfo");

    } // End of getIndexInfo



/**
Returns the maximum length for an inline binary literal.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxBinaryLiteralLength ()
    throws SQLException
    {
        return 32739;
    }



/**
Returns the maximum length for a catalog name.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxCatalogNameLength ()
    throws SQLException
    {
        return 10;
    }



/**
Returns the maximum length for a character literal.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxCharLiteralLength ()
    throws SQLException
    {
        return 32739;
    }



/**
Returns the maximum length for a column name.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxColumnNameLength ()
    throws SQLException
    {
        return 30;
    }



/**
Returns the maximum number of columns in a GROUP BY clause.

@return     The maximum number of columns.

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxColumnsInGroupBy ()
    throws SQLException
    {
        return 120;
    }



/**
Returns the maximum number of columns allowed in an index.

@return     The maximum number of columns.

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxColumnsInIndex ()
    throws SQLException
    {
        return 120;
    }



/**
Returns the maximum number of columns in an ORDER BY clause.

@return     The maximum number of columns.

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxColumnsInOrderBy ()
    throws SQLException
    {
        return 10000;
    }



/**
Returns the maximum number of columns in a SELECT list.

@return     The maximum number of columns.

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxColumnsInSelect ()
    throws SQLException
    {
        return 8000;
    }



/**
Returns the maximum number of columns in a table.

@return     The maximum number of columns.

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxColumnsInTable ()
    throws SQLException
    {
        return 8000;
    }



/**
Returns the number of active connections you can have at a time
to this database.

@return     The maximum number of connections or 0
            if no limit.

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxConnections ()
    throws SQLException
    {
        // There is no limit.  The specification does
        // not come right out and say "0 means no limit",
        // but that is how ODBC and many other parts
        // of JDBC work.
        return 0;
    }



/**
Returns the maximum cursor name length.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxCursorNameLength ()
    throws SQLException
    {
        return AS400JDBCStatement.MAX_CURSOR_NAME_LENGTH;
    }



/**
Returns the maximum length of an index.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxIndexLength ()
    throws SQLException
    {
        return 2000;
    }



/**
Returns the maximum length of a procedure name.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxProcedureNameLength ()
    throws SQLException
    {
        return 128;
    }



/**
Returns the maximum length of a single row.

@return     The maximum length (in bytes).
@exception  SQLException    This exception is never thrown.
**/
    public int getMaxRowSize ()
    throws SQLException
    {
        return 32766;
    }



/**
Returns the maximum length allowed for a schema name.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxSchemaNameLength ()
    throws SQLException
    {
        return 10;
    }



/**
Returns the maximum length of an SQL statement.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxStatementLength ()
    throws SQLException
    {
        return 32767;
    }



/**
Returns the number of active statements you can have open at one
time.

@return     The maximum number of statements or 0
            if no limit.

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxStatements ()
    throws SQLException
    {
        return AS400JDBCConnection.MAX_STATEMENTS_; // @E4C
    }



/**
Returns the maximum length of a table name.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxTableNameLength ()
    throws SQLException
    {
        return 128;
    }



/**
Returns the maximum number of tables in a SELECT.

@return     The maximum number of tables.

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxTablesInSelect ()
    throws SQLException
    {
        return 32;
    }



/**
Returns the maximum length of a user name.

@return     The maximum length (in bytes).

@exception  SQLException    This exception is never thrown.
**/
    public int getMaxUserNameLength ()
    throws SQLException
    {
        return 10;
    }



/**
Returns the list of supported math functions.

@return     The list of supported math functions, separated by commas.

@exception  SQLException    This exception is never thrown.
**/
    public String getNumericFunctions ()
    throws SQLException
    {
        return JDEscapeClause.getNumericFunctions ();
    }



/**
Returns a description of the primary key columns.
@param  catalog      The catalog name. If null is specified, this parameter
                     is ignored.  If empty string is specified,
                     an empty result set is returned.
@param  schema       The schema name. If null is specified, the
                     default library specified in the URL is used.
                     If null is specified and a default library was not
                     specified in the URL, the first library specified
                     in the libraries properties file is used.
                     If null is specified, a default library was
                     not specified in the URL, and a library was not
                     specified in the libraries properties file,
                     QGPL is used.
                     If empty string is specified, an empty result set will
                     be returned.
@param  table        The table name. If null or empty string is specified,
                     an empty result set is returned.

@return              The ResultSet containing the description of the primary
                     key columns.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getPrimaryKeys (String catalog,
                                     String schema,
                                     String table)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = {"TABLE_CAT",
            "TABLE_SCHEM",
            "TABLE_NAME",
            "COLUMN_NAME",
            "KEY_SEQ",
            "PK_NAME",
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_), // pk catalog
            new SQLVarchar (128, settings_),  // pk schema
            new SQLVarchar (128, settings_),  // pk table
            new SQLVarchar (128, settings_),  // pk column
            new SQLSmallint (),    // key seq
            new SQLVarchar (128, settings_),  // pk name
        };

        int[] fieldNullables = {columnNullable,  // pk catalog
            columnNullable,  // pk schema
            columnNoNulls,  // pk table
            columnNoNulls,  // pk column
            columnNoNulls,  // key seq
            columnNullable,  // pk name
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows
        // that are readable one at a time.
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // table is null or empty string
                (table==null)  ||  (table.length()==0 )) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_PRIMARY_KEY_INFO ,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);


                // Set the primary key file library name
                if (schema == null) {   // use default library or qgpl
                    request.setPrimaryKeyFileLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setPrimaryKeyFileLibraryName(schema, connection_.getConverter());


                // Set the primary key table name
                request.setPrimaryKeyFileName(table, connection_.getConverter());

                // Set the primary key Information to Return Bitmap
                request.setPrimaryKeyReturnInfoBitmap(0xB8000000);

                // This is not documented in the LIPI, but it happens to work!           @E2A
                request.setFileShortOrLongNameIndicator(0xF0);                        // @E2A

                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();
                if (resultData != null) {
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                    JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                       1, resultData, true));
                    // Create the mapped row format that is returned in the
                    // result set.
                    // This does not actual move the data, it just sets up
                    // the mapping.
                    boolean nullValue = true; // used when hardcoding null
                    JDFieldMap[] maps = new JDFieldMap[6];
                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[1] = new JDSimpleFieldMap (1); // pk schema
                    maps[2] = new JDSimpleFieldMap (2); // pk table
                    maps[3] = new JDSimpleFieldMap (3); // pk column
                    maps[4] = new JDCharToShortFieldMap (4);    // key seq
                    maps[5] = new JDHardcodedFieldMap (new SQLVarchar (0, settings_), true);

                    // Create the mapped row cache that is returned in the
                    // result set
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }
                else
                    rowCache = new JDSimpleRowCache (formatRow);

            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "PrimaryKeys");
    } 



/**
Returns a description of a catalog's stored procedure
parameters and result columns.

@param  catalog            The catalog name. If null is specified, this parameter
                           is ignored.  If empty string is specified,
                           an empty result set is returned.
@param  schemaPattern      The schema name pattern.  If null is specified,
                           it will not be included in the selection
                           criteria. If empty string
                           is specified, an empty result set is returned.
@param  procedurePattern   The procedure name pattern. If null is specified,
                           it will not be included in the selection criteria.
                           If empty string
                           is specified, an empty result set is returned.
@param  columnPattern      The column name pattern.  If null is specified,
                           it will not be included in the selection criteria.
                           If empty string
                           is specified, an empty result set is returned.

@return                    The ResultSet containing the description of the
                           catalog's stored procedure parameters and result
                           columns.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getProcedureColumns (String catalog,
                                          String schemaPattern,
                                          String procedurePattern,
                                          String columnPattern)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = { "PROCEDURE_CAT",
            "PROCEDURE_SCHEM",
            "PROCEDURE_NAME",
            "COLUMN_NAME",
            "COLUMN_TYPE",
            "DATA_TYPE",
            "TYPE_NAME",
            "PRECISION",
            "LENGTH",
            "SCALE",
            "RADIX",
            "NULLABLE",
            "REMARKS"
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_),  // catalog
            new SQLVarchar (128, settings_),  // schema
            new SQLVarchar (128, settings_),  // procedure
            new SQLVarchar (128, settings_),  // column name
            new SQLSmallint (),    // column type
            new SQLSmallint (),    // data type
            new SQLVarchar (128, settings_),  // type name
            new SQLInteger (),     // precision
            new SQLInteger (),     // length
            new SQLSmallint (),    // scale
            new SQLInteger (),    // radix
            new SQLSmallint (),    // nullable
            new SQLVarchar (2000, settings_)  // remarks
        };

        int[] fieldNullables = {
            columnNullable,  // catalog
            columnNullable,  // schema
            columnNoNulls,   // Procedure name
            columnNoNulls,   // column name
            columnNoNulls,   // column type
            columnNoNulls,   // data type
            columnNoNulls,   // type name
            columnNoNulls,   // precision
            columnNoNulls,   // length
            columnNoNulls,   // scale
            columnNoNulls,   // radix
            columnNoNulls,   // nullable
            columnNoNulls    // remarks
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // procedure is not null and is empty string
                ((procedurePattern != null)  &&  (procedurePattern.length()==0)) ||

                // column is not null and is empty string
                ((columnPattern != null)  &&  (columnPattern.length()==0))) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else

            {  // Parameters are valid, build request and send
                StringBuffer selectStmt = new StringBuffer();
                selectStmt.append ("SELECT SPECIFIC_SCHEMA, SPECIFIC_NAME, PARAMETER_NAME, PARAMETER_MODE, ");
                selectStmt.append ("DATA_TYPE, NUMERIC_PRECISION, CHARACTER_MAXIMUM_LENGTH, NUMERIC_SCALE, ");
                selectStmt.append ("NUMERIC_PRECISION_RADIX, IS_NULLABLE, LONG_COMMENT ");
                selectStmt.append ("FROM QSYS2" + getCatalogSeparator() + "SYSPARMS "); // use . or /




                if (schemaPattern !=null) {
                    JDSearchPattern schema = new JDSearchPattern (schemaPattern);
                    String schemaWhereClause = schema.getSQLWhereClause("SPECIFIC_SCHEMA");
                    selectStmt.append("WHERE " + schemaWhereClause);
                }


                if (procedurePattern !=null) {
                    JDSearchPattern procedure = new JDSearchPattern (procedurePattern);
                    if (schemaPattern!=null) { // Where clause already exists, add AND
                        selectStmt.append (" AND ");
                    }

                    else {  // Where clause does not exist, add WHERE
                        selectStmt.append (" WHERE ");
                    }

                    String procedureWhereClause = procedure.getSQLWhereClause("SPECIFIC_NAME");
                    selectStmt.append(procedureWhereClause);
                }


                if (columnPattern!=null) {  // if null, no where clause is sent
                    JDSearchPattern column = new JDSearchPattern (columnPattern);
                    if ((schemaPattern!=null) || (procedurePattern!=null)) { // Where clause already exists, add AND
                        selectStmt.append (" AND ");
                    } else {  // Where clause does not exist, add WHERE
                        selectStmt.append (" WHERE ");
                    }

                    String columnWhereClause = column.getSQLWhereClause("PARAMETER_NAME");
                    selectStmt.append(columnWhereClause);

                }




                // Add order by
                selectStmt.append ("ORDER BY SPECIFIC_SCHEMA, SPECIFIC_NAME");

                // Create statement object and do Execute Query
                AS400JDBCStatement statement_ = (AS400JDBCStatement)connection_.createStatement(); // caste needed
                AS400JDBCResultSet serverResultSet = (AS400JDBCResultSet) statement_.executeQuery (selectStmt.toString());

                JDRowCache serverRowCache = new JDSimpleRowCache (serverResultSet.getRowCache());
                statement_.close ();

                JDFieldMap[] maps = new JDFieldMap[13];

                maps[0] = new JDHardcodedFieldMap (connection_.getCatalog());
                maps[1] = new JDSimpleFieldMap (1); // schema
                maps[2] = new JDSimpleFieldMap (2); // procedure
                maps[3] = new JDHandleNullFieldMap (3, ""); // parameter name (col name)
                maps[4] = new JDParameterModeFieldMap(4); // Parameter mode (col type)

                maps[5] = new JDDataTypeFieldMap(5, 7, 6, 8); // data type - converts from string to short
                maps[6] = new JDSimpleFieldMap (5); // type name

                maps[7] = new JDHandleNullFieldMap (6, new Integer (0));  // precision
                maps[8] = new JDHandleNullFieldMap (7, new Integer (0));  // length

                maps[9] = new JDHandleNullFieldMap (8, new Short ((short) 0));  // scale
                maps[10] = new JDHandleNullFieldMap (9, new Short ((short) 0)); // radix

                maps[11] = new JDNullableSmallintFieldMap(10);  // nullable - is Nullable
                maps[12] = new JDHandleNullFieldMap (11, "");  // remarks - long comment

                JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                rowCache = new JDMappedRowCache (mappedRow, serverRowCache);

            } // End of else build request and send


        } // End of try block

        catch (SQLException e) {
            // System.out.println(e);
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }


        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "ProcedureColumns");

    } // End of getProcedureColumns



/**
Returns the description of the stored procedures available
in a catalog.

@param  catalog            The catalog name. If null is specified, this parameter
                           is ignored.  If empty string is specified,
                           an empty result set is returned.
@param  schemaPattern      The schema name pattern.  If null is specified,
                           it will not be included in the selection
                           criteria.  If empty string
                           is specified, an empty result set is returned.
@param  procedurePattern   The procedure name pattern. If null is specified,
                           it will not be included in the selection
                           criteria.  If empty string
                           is specified, an empty result set is returned.

@return                    The ResultSet containing the description of the
                           stored procedures available in the catalog.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getProcedures (String catalog,
                                    String schemaPattern,
                                    String procedurePattern)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = { "PROCEDURE_CAT",
            "PROCEDURE_SCHEM",
            "PROCEDURE_NAME",
            "RESERVED1",
            "RESERVED2",
            "RESERVED3",
            "REMARKS",
            "PROCEDURE_TYPE"
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_),  // catalog
            new SQLVarchar (128, settings_),  // schema
            new SQLVarchar (128, settings_),  // procedure
            new SQLInteger (),     // reserved
            new SQLInteger (),     // reserved
            new SQLInteger (),     // reserved
            new SQLVarchar (2000, settings_),  // remarks
            new SQLSmallint ()     // procedure type
        };

        int[] fieldNullables = {
            columnNullable,  // Procedure catalog
            columnNullable,  // Procedure schema
            columnNoNulls,   // Procedure name
            columnNullable,  // Reserved 1
            columnNullable,  // Reserved 2
            columnNullable,  // Reserved 3
            columnNoNulls,   // Remarks
            columnNoNulls    // Procedure type
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // procedure is not null and is empty string
                ((procedurePattern!=null)  &&  (procedurePattern.length()==0))) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else {  // Parameters are valid, build request and send
                StringBuffer selectStmt = new StringBuffer();
                selectStmt.append ("SELECT SPECIFIC_SCHEMA, SPECIFIC_NAME, REMARKS, RESULTS ");
                selectStmt.append ("FROM QSYS2" + getCatalogSeparator() + "SYSPROCS "); // use . or /



                if (schemaPattern !=null) {
                    JDSearchPattern schema = new JDSearchPattern (schemaPattern);
                    String schemaWhereClause = schema.getSQLWhereClause("SPECIFIC_SCHEMA");
                    selectStmt.append("WHERE " + schemaWhereClause);
                }


                if (procedurePattern !=null) {
                    JDSearchPattern procedure = new JDSearchPattern (procedurePattern);
                    if (schemaPattern!=null) { // Where clause already exists, add AND
                        selectStmt.append (" AND ");
                    }

                    else {  // Where clause does not exist, add WHERE
                        selectStmt.append (" WHERE ");
                    }

                    String procedureWhereClause = procedure.getSQLWhereClause("SPECIFIC_NAME");
                    selectStmt.append(procedureWhereClause);
                }



                // Add order by
                selectStmt.append (" ORDER BY SPECIFIC_SCHEMA, SPECIFIC_NAME");


                // Create statement object and do Execute Query
                AS400JDBCStatement statement_ = (AS400JDBCStatement)connection_.createStatement(); // caste needed


                AS400JDBCResultSet serverResultSet = (AS400JDBCResultSet) statement_.executeQuery (selectStmt.toString());

                JDRowCache serverRowCache = new JDSimpleRowCache (serverResultSet.getRowCache());
                statement_.close ();

                JDFieldMap[] maps = new JDFieldMap[8];
                maps[0] = new JDHardcodedFieldMap (connection_.getCatalog());
                maps[1] = new JDSimpleFieldMap (1); // schema
                maps[2] = new JDSimpleFieldMap (2); // procedure
                maps[3] = new JDHardcodedFieldMap (new Integer (0));
                maps[4] = new JDHardcodedFieldMap (new Integer (0));
                maps[5] = new JDHardcodedFieldMap (new Integer (0));
                maps[6] = new JDHandleNullFieldMap (3, ""); // remarks
                maps[7] = new JDProcTypeFieldMap (4);

                JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                rowCache = new JDMappedRowCache (mappedRow, serverRowCache);

            } // End of else build request and send


        } // End of try block

        catch (SQLException e) {
            // System.out.println(e);
            // e.printStackTrace();  // method on throwable object
            // @B1D JDError.throwSQLException (JDError.EXC_INTERNAL, e);
            throw e; // @B1A
        }

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "Procedures");

    }



/**
Returns the DB2 for OS/400 SQL term for "procedure".

@return     The term for "procedure".

@exception  SQLException    This exception is never thrown.
**/
    public String getProcedureTerm ()
    throws SQLException
    {
        return AS400JDBCDriver.getResource ("PROCEDURE_TERM");
    }



/**
Returns the schema names available in this database.
This will return a ResultSet with a list of all the
libraries.

@return             The ResultSet containing the list of all the libraries.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getSchemas ()
    throws SQLException
    {
        // Schema = library
        connection_.checkOpen ();

        JDRowCache rowCache = null;  // Creates a set of rows that
        // are readable one at a time

        try {
            // Create a request
            DBReturnObjectInformationRequestDS request =
            new DBReturnObjectInformationRequestDS (
                                                   DBReturnObjectInformationRequestDS.FUNCTIONID_RETRIEVE_LIBRARY_INFO,
                                                   id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                   DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                   DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);


            // Return list of all libraries on the AS400
            request.setLibraryName("%", connection_.getConverter());
            request.setLibraryNameSearchPatternIndicator(0xF1);


            // Set the Library Information to Return Bitmap
            // Return only the library name
            request.setLibraryReturnInfoBitmap(0x80000000);

            // Send the request and cache all results from the server
            DBReplyRequestedDS reply = connection_.sendAndReceive(request);


            // Check for errors - throw exception if errors were
            // returned
            int errorClass = reply.getErrorClass();
            if (errorClass !=0) {
                int returnCode = reply.getReturnCode();
                JDError.throwSQLException (connection_, id_, errorClass, returnCode);
            }

            // Get the data format and result data
            DBDataFormat dataFormat = reply.getDataFormat();
            DBData resultData = reply.getResultData();

            // Put the result data into a row cache
            JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

            // Put the data format into a row format object
            JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                    1, resultData, true));

            // Set up the result set in the format required by JDBC
            String[] fieldNames = {"TABLE_SCHEM"};

            SQLData[] sqlData = {new SQLVarchar (128, settings_)};

            int[] fieldNullables = {columnNoNulls};

            // Create the mapped row format that is returned in the
            // result set.
            // This does not actual move the data, it just sets up
            // the mapping.
            JDFieldMap[] maps = new JDFieldMap[1];
            maps[0] = new JDStripQuotesFieldMap (1); // @A3C

            // Create the mapped row cache that is returned in the
            // result set
            JDMappedRow mappedRow = new JDMappedRow (fieldNames, sqlData,
                                                     fieldNullables, maps);
            rowCache = new JDMappedRowCache (mappedRow,
                                             serverRowCache);

        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache,
                                       connection_.getCatalog(), "Schemas");

    }



/**
Returns the DB2 for OS/400 SQL term for "schema".

@return     The term for schema.

@exception  SQLException    This exception is never thrown.
**/
    public String getSchemaTerm ()
    throws SQLException
    {
        return AS400JDBCDriver.getResource ("SCHEMA_TERM");
    }



/**
Returns the string used to escape wildcard characters.
This is the string that can be used to escape '_' or '%'
in string patterns.

@return     The escape string.

@exception  SQLException    This exception is never thrown.
**/
    public String getSearchStringEscape ()
    throws SQLException
    {
        return JDSearchPattern.getEscape ();
    }



/**
Returns the list of all of the database's SQL keywords
that are not also SQL92 keywords.

@return     The list of SQL keywords, separated by commas.

@exception  SQLException    This exception is never thrown.
**/
    public String getSQLKeywords ()
    throws SQLException
    {
        return "CCSID,COLLECTION,CONCAT,DATABASE,PACKAGE,PROGRAM,RESET,ROW,RUN,VARIABLE";
    }



/**
Returns the list of supported string functions.

@return     The list of supported string functions, separated by commas.

@exception  SQLException    This exception is never thrown.
**/
    public String getStringFunctions ()
    throws SQLException
    {
        return JDEscapeClause.getStringFunctions ();
    }



/**
Returns the list of supported system functions.

@return     The list of supported system functions, separated by commas.

@exception  SQLException    This exception is never thrown.
**/
    public String getSystemFunctions ()
    throws SQLException
    {
        return JDEscapeClause.getSystemFunctions ();
    }



/**
Returns the description of the access rights for each table
available in a catalog.  Note that a table privilege applies
to one or more columns in a table.

@param  catalog             The catalog name. If null is specified, this parameter
                            is ignored.  If empty string is specified,
                            an empty result set is returned.
@param  schemaPattern       The schema name pattern. If null is specified,
                            no value is sent to the server and the
                            server default of *USRLIBL is used.
                            If empty string is specified, an empty
                            result set is returned.
@param  tablePattern        The table name. If null is specified,
                            no value is sent to the server and the server
                            default of *ALL is used.  If empty string
                            is specified, an empty result set is returned.
@return                     The ResultSet containing the description of the
                            access rights for each table available in the
                            catalog.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getTablePrivileges (String catalog,
                                         String schemaPattern,
                                         String tablePattern)
    throws SQLException
    {
        connection_.checkOpen ();

        //-----------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = {"TABLE_CAT",
            "TABLE_SCHEM",
            "TABLE_NAME",
            "GRANTOR",
            "GRANTEE",
            "PRIVILEGE",
            "IS_GRANTABLE",
        };


        SQLData[] sqlData = { new SQLVarchar (128, settings_), // catalog
            new SQLVarchar (128, settings_),  // library
            new SQLVarchar (128, settings_),  // table
            new SQLVarchar (128, settings_),  // grantor
            new SQLVarchar (128, settings_),  // grantee
            new SQLVarchar (128, settings_),  // privilege
            new SQLVarchar (3, settings_),    // is_grantable
        };

        int[] fieldNullables = {columnNullable,  // catalog
            columnNullable,  // library
            columnNoNulls,   // table
            columnNullable,  // grantor
            columnNoNulls,   // grantee
            columnNoNulls,   // privilege
            columnNullable,  // is_grantable
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows
                                     // that are readable one at a time.
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException
            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // table is not null and is empty string
                ((tablePattern != null) && (tablePattern.length()==0))) {   // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }


            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_FILE_INFO,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

                // Set the library name and search pattern indicator
                // If null, do not set parameter. The server default
                // value of *USRLIBL is used.
                if (schemaPattern!=null) {
                    JDSearchPattern schema = new JDSearchPattern(schemaPattern);
                    request.setLibraryName(schema.getPatternString(), connection_.getConverter());
                    request.setLibraryNameSearchPatternIndicator(schema.getIndicator());
                }



                // Set the table name and search pattern indicator
                // If null, do not set parameter. The server default
                // value of *ALL is used.
                if (tablePattern!=null) {
                    JDSearchPattern table = new JDSearchPattern(tablePattern);
                    request.setFileName(table.getPatternString(),connection_.getConverter());
                    request.setFileNameSearchPatternIndicator(table.getIndicator());
                }

                request.setFileShortOrLongNameIndicator (0xF0); // Long table names


                request.setFileReturnInfoBitmap (0xA1000000); // Return bitmap

                // Order the results by table type, table schema, table name
                request.setFileInfoOrderByIndicator (2);


                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }


                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();

                // Put the data format into a row object
                JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

                // Put the result data into a row cache
                JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                        1, resultData, true));

                // Create the mapped row format that is returned in the
                // result set.
                // This does not actual move the data, it just sets up
                // the mapping.
                JDFieldMap[] maps = new JDFieldMap[7];
                maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                maps[1] = new JDSimpleFieldMap (1); // library
                maps[2] = new JDSimpleFieldMap (2); // table
                maps[3] = new JDHardcodedFieldMap ("", true); // grantor
                maps[4] = new JDHardcodedFieldMap (getUserName ()); // grantee - return userid
                maps[5] = new JDPrivilegeFieldMap (3); // privilege
                maps[6] = new JDHardcodedFieldMap ("", true); // is_grantable

                // Create the mapped row cache that is returned in the
                // result set
                JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                rowCache = new JDMappedRowCache (mappedRow, serverRowCache);

            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "TablePrivleges");
    }



/**
Returns the description of the tables available in a catalog.

@param  catalog        The catalog name. If null is specified, this parameter
                       is ignored.  If empty string is specified,
                       an empty result set is returned.
@param  schemaPattern  The schema name pattern.  If null is specified,
                       no value is sent to the server and the server
                       default of *USRLIBL is used.  If empty string
                       is specified, an empty result set is returned.
@param  tablePattern   The table name pattern. If null is specified,
                       no value is sent to the server and the server
                       default of *ALL is used.  If empty string
                       is specified, an empty result set is returned.
@param  tableTypes     The list of table types to include, or null to
                       include all table types. Valid types are:
                       TABLE, VIEW, and SYSTEM TABLE.
@return                The ResultSet containing the description of the
                       tables available in the catalog.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getTables (String catalog,
                                String schemaPattern,
                                String tablePattern,
                                String tableTypes[])
    throws SQLException
    {

        connection_.checkOpen ();// Verify that a connection
        // is available for use. Exception
        // is thrown if not available

        //-----------------------------------------------------
        // Set up the result set in the format required by JDBC
        //-----------------------------------------------------

        String[] fieldNames = { "TABLE_CAT",
            "TABLE_SCHEM",
            "TABLE_NAME",
            "TABLE_TYPE",
            "REMARKS"};

        SQLData[] sqlData = { new SQLVarchar (128, settings_),
            new SQLVarchar (128, settings_),
            new SQLVarchar (128, settings_),
            new SQLVarchar (128, settings_),
            new SQLVarchar (254, settings_)};


        int[] fieldNullables = {columnNullable,  // Table catalog
            columnNullable,   // Table schema
            columnNoNulls,    // Table name
            columnNoNulls,    // Table type
            columnNoNulls};   // Remarks

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null; // Creates a set of rows that
        // are readable one at a time
        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException
            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // table is not null and is empty string
                ((tablePattern != null) && (tablePattern.length()==0))) {   // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }


            else {  // schema, table, and catalog are valid, create and send request

                //-------------------------------------------------------
                // Set the file attribute parm of the Retrieve File
                // Information (ROI) function based on the types array
                // provided. ROI uses the following values:
                //  '0001'x = all files
                //  '0002'x = physical files
                //  '0003'x = logical files
                //  '0004'x = tables
                //  '0005'x = views
                //  '0006'x = system tables
                //  '0007'x = tables and views
                //  '0008'x = tables and system tables
                //  '0009'x = views and system tables
                //
                // If null is specified, file attributes is set to 1.
                //
                // If none of the above values are specified, file
                // attribute is set to -1 and an empty result set will
                // be created. No request is sent to the server.
                //--------------------------------------------------------
                int fileAttribute;
                if (tableTypes != null) {
                    boolean typeTable       = false;  // false = don't include table type
                    boolean typeView        = false;
                    boolean typeSystemTable = false;

                    // Walk thru table types to determine which ones we need to include
                    for (int i = 0; i < tableTypes.length; ++i) {
                        if (tableTypes[i].equalsIgnoreCase ("TABLE"))
                            typeTable = true; // Include tables
                        else if (tableTypes[i].equalsIgnoreCase ("VIEW"))
                            typeView = true;  // Include views
                        else if (tableTypes[i].equalsIgnoreCase ("SYSTEM TABLE"))
                            typeSystemTable = true;  // Include system tables
                    }   // end of for loop


                    if (typeTable) {
                        if (typeView) {
                            if (typeSystemTable)
                                fileAttribute = 1;  // All
                            else
                                fileAttribute = 7;  // Tables and views
                        } else {             // Not views
                            if (typeSystemTable)
                                fileAttribute = 8;  // Tables and system tables
                            else
                                fileAttribute = 4;  // Tables
                        }
                    }   // end of if typeTable
                    else {           // Not tables
                        if (typeView) {
                            if (typeSystemTable)
                                fileAttribute = 9;  // Views and system tables
                            else
                                fileAttribute = 5;  // Views
                        } else {
                            if (typeSystemTable)
                                fileAttribute = 6;  // System tables
                            else
                                fileAttribute = -1; // Unknown type
                            // Will return empty results
                        }
                    }   // End of not tables else
                } // End of if tables != nulls
                else {
                    // Table types was set to null which implies all
                    // types are to be returned
                    fileAttribute = 1;               // All
                }


                //------------------------------------------------
                // Create the request to Retrieve File Information
                //------------------------------------------------
                if (fileAttribute != -1) { // If -1, return empty set
                    // Create a request
                    DBReturnObjectInformationRequestDS request =
                    new DBReturnObjectInformationRequestDS (
                                                           DBReturnObjectInformationRequestDS.FUNCTIONID_FILE_INFO, id_,
                                                           DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                           + DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT
                                                           + DBBaseRequestDS.ORS_BITMAP_RESULT_DATA,0);


                    //--------------------------------------------------
                    // Set the parameters for the request
                    //--------------------------------------------------


                    // Set the Library Name and Library Name Search Pattern parameters
                    // If null, do not set parameter.  The server default value of
                    // *USRLIBL is used.
                    if (schemaPattern != null) { // use default library or qgpl
                        JDSearchPattern schema = new JDSearchPattern(schemaPattern);
                        request.setLibraryName (schema.getPatternString(), connection_.getConverter());
                        request.setLibraryNameSearchPatternIndicator(schema.getIndicator());
                    }



                    // Set the Table Name and Table Name Search Pattern parameters
                    // If null, do not set parameter.  The server default value of
                    // *ALL is used.
                    if (tablePattern!=null) {
                        JDSearchPattern table = new JDSearchPattern(tablePattern);
                        request.setFileName (table.getPatternString(), connection_.getConverter());
                        request.setFileNameSearchPatternIndicator(table.getIndicator());
                    }



                    // Set other parameters
                    request.setFileShortOrLongNameIndicator (0xF0); // Long table names.
                    request.setFileAttribute (fileAttribute);

                    // Set the information to return.  Always return the
                    // library name, file name, and file attribute (ODBC
                    // type).  Also return either the remarks or the file
                    // text depending on what the "remarks" attribute
                    // is set to.

                    // Get the current value for the "remarks" property
                    // and check to see if it is sql or system
                    // connection_.getProperties() returns the JDProperties
                    // object


                    if (connection_.getProperties().equals
                        (JDProperties.REMARKS, JDProperties.REMARKS_SQL)) {
                        request.setFileReturnInfoBitmap (0xF0000000); // return remarks
                    } else {
                        request.setFileReturnInfoBitmap (0xB4000000); // return text
                    }

                    // Order the results by table type, table schema, table name
                    // This is the same as ordering by tables on the server
                    request.setFileInfoOrderByIndicator (2);


                    //-------------------------------------------------------
                    // Send the request and cache all results from the server
                    //-------------------------------------------------------

                    DBReplyRequestedDS reply = connection_.sendAndReceive (request);

                    // Check for errors - throw exception if errors
                    // were returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass != 0) {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (connection_, id_,
                                                   errorClass, returnCode);
                    }


                    // Get the data format and result data
                    // The result data is parsed via JDServerRow getData
                    DBDataFormat dataFormat = reply.getDataFormat ();
                    DBData resultData = reply.getResultData ();
                    if (resultData != null) {

                        // Put the data format into a row format. Handles data types
                        JDServerRow row = new JDServerRow (connection_, id_, dataFormat, settings_);

                        // Put the result data into a row cache
                        // ServerRowCache needs rowFormat to get offset and other info
                        // Only need this with this type of server (not with simple)
                        JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (
                                                                                               row, connection_, id_, 1, resultData, true));

                        // This is not actually moving data, it just sets up the mapping
                        JDFieldMap[] maps = new JDFieldMap[5];
                        if (connection_.getProperties().equals
                            (JDProperties.REMARKS, JDProperties.REMARKS_SQL)) {
                            maps[0] = new JDHardcodedFieldMap (connection_.getCatalog());
                            maps[1] = new JDStripQuotesFieldMap (1);    // schema // @A3C
                            maps[2] = new JDSimpleFieldMap (3);    // table
                            maps[3] = new JDTableTypeFieldMap (4); // table type
                            maps[4] = new JDSimpleFieldMap (2);    // remarks
                        }

                        else {   // Get file text instead of remarks
                            maps[0] = new JDHardcodedFieldMap (connection_.getCatalog());
                            maps[1] = new JDStripQuotesFieldMap (1);     // schema  // @A3C
                            maps[2] = new JDSimpleFieldMap (2);     // table
                            maps[3] = new JDTableTypeFieldMap (3); // table type
                            maps[4] = new JDSimpleFieldMap (4);      // File text
                        }

                        JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                        rowCache = new JDMappedRowCache (mappedRow, serverRowCache);

                    } else
                        rowCache = new JDSimpleRowCache (formatRow);

                } // End of if file attribute != -1
                else { // result set should be empty.
                    rowCache = new JDSimpleRowCache (formatRow);
                }

            } // End of else to create and send request

        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "Tables");
    }



/**
Returns the table types available in this database.

@return     The ResultSet containing the table types available in
            this database.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getTableTypes ()
    throws SQLException
    {
        // Set up the result set.
        String[] fieldNames      = {"TABLE_TYPE"};
        SQLData[] sqlData = { new SQLVarchar (128, settings_)};
        int[] fieldNullables = {columnNoNulls}; // table types can not be null

        Object[][] data = { { JDTableTypeFieldMap.TABLE_TYPE_TABLE},
            { JDTableTypeFieldMap.TABLE_TYPE_VIEW},
            { JDTableTypeFieldMap.TABLE_TYPE_SYSTEM_TABLE}};

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);
        JDSimpleRowCache rowCache = new JDSimpleRowCache (formatRow, data);

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "Table Types");
    }



/**
Returns the list of supported time and date functions.

@return     The list of supported time and data functions,
            separated by commas.

@exception  SQLException    This exception is never thrown.
**/
    public String getTimeDateFunctions ()
    throws SQLException
    {
        return JDEscapeClause.getTimeDateFunctions ();
    }



/**
Returns a description of all of the standard SQL types
supported by this database.

@return    The ResultSet containing the description of all
           the standard SQL types supported by this
           database.

@exception  SQLException    This exception is never thrown.
**/
    public ResultSet getTypeInfo ()
    throws SQLException
    {
        // Initialize a row to describe the format of the result set.
        String[] fieldNames = { "TYPE_NAME",
            "DATA_TYPE",
            "PRECISION",
            "LITERAL_PREFIX",
            "LITERAL_SUFFIX",
            "CREATE_PARAMS",
            "NULLABLE",
            "CASE_SENSITIVE",
            "SEARCHABLE",
            "UNSIGNED_ATTRIBUTE",
            "FIXED_PREC_SCALE",
            "AUTO_INCREMENT",
            "LOCAL_TYPE_NAME",
            "MINIMUM_SCALE",
            "MAXIMUM_SCALE",
            "SQL_DATA_TYPE",
            "SQL_DATETIME_SUB",
            "NUM_PREC_RADIX"
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_),  // Table name.
            new SQLSmallint (),               // Data type.
            new SQLInteger (),                // Precision.
            new SQLVarchar (128, settings_),  // Literal prefix.
            new SQLVarchar (128, settings_),  // Literal suffix.
            new SQLVarchar (128, settings_),  // Create parameters.
            new SQLSmallint (),               // Nullable.
            new SQLSmallint (),               // Case sensitive.
            new SQLSmallint (),               // Searchable.
            new SQLSmallint (),               // Unsigned.
            new SQLSmallint (),               // Currency.
            new SQLSmallint (),               // Auto increment.
            new SQLVarchar (128, settings_),  // Local type name.
            new SQLSmallint (),               // Minimum scale.
            new SQLSmallint (),               // Maximum scale.
            new SQLInteger (),                // Unused.
            new SQLInteger (),                // Unused.
            new SQLInteger ()                 // Radix.
        };


        int[] fieldNullables = { columnNoNulls,   // Table name.
            columnNoNulls,   // Data type.
            columnNoNulls,   // Precision.
            columnNullable,  // Literal prefix.
            columnNullable,  // Literal suffix.
            columnNullable,  // Create parameters.
            columnNoNulls,   // Nullable.
            columnNoNulls,   // Case sensitive.
            columnNoNulls,   // Aearchable.
            columnNoNulls,   // Unsigned.
            columnNoNulls,   // Currency.
            columnNoNulls,   // Auto increment.
            columnNullable,  // Local type name.
            columnNoNulls,   // Minimum scale.
            columnNoNulls,   // Maximum scale.
            columnNoNulls,   // Unused.
            columnNoNulls,   // Unused.
            columnNoNulls    // Radix.
        };


        JDSimpleRow formatRow = new JDSimpleRow (fieldNames,
                                                 sqlData, fieldNullables);

        // Initialize the data that makes up the contents
        // of the result set.
        // I changed this from an array to a Vector in order to make it         // @D0C
        // easier to conditionally add types based on the release.              // @D0C
        Vector typeSamples = new Vector();                                      // @D0C
        if (connection_.getVRM() >= AS400JDBCConnection.BIGINT_SUPPORTED_)      // @D0A
            typeSamples.addElement(new SQLBigint());                            // @D0A
        typeSamples.addElement(new SQLBinary(32765, settings_));                // @D0C
        typeSamples.addElement(new SQLChar(32756, false, settings_));           // @D0C
        typeSamples.addElement(new SQLDate(settings_));                         // @D0C
        typeSamples.addElement(new SQLDecimal(31, 31, settings_));              // @D0C
        typeSamples.addElement(new SQLDouble(settings_));                       // @D0C
        typeSamples.addElement(new SQLFloat(settings_));                        // @D0C
        typeSamples.addElement(new SQLInteger());                               // @D0C
        typeSamples.addElement(new SQLNumeric(31, 31, settings_));              // @D0C
        typeSamples.addElement(new SQLReal(settings_));                         // @D0C
        typeSamples.addElement(new SQLSmallint());                              // @D0C
        typeSamples.addElement(new SQLTime(settings_));                         // @D0C
        typeSamples.addElement(new SQLTimestamp(settings_));                    // @D0C
        typeSamples.addElement(new SQLVarbinary(32739, false, settings_));      // @D0C
        typeSamples.addElement(new SQLVarchar(32739, settings_));               // @D0C
        if (connection_.getVRM() >= AS400JDBCConnection.LOB_SUPPORTED_) {       // @B4D B5A @D0C
            typeSamples.addElement(new SQLBlob(15728640, settings_));           // @B4D B5A @D0C
            typeSamples.addElement(new SQLClob(15728640, settings_));           // @B4D B5A @D0C
            typeSamples.addElement(new SQLDatalink(32739, settings_));          // @B4D B5A @D0C
        }                                                                       // @B4D B5A 

        int numberOfTypes = typeSamples.size();                                 // @D0C
        int numberOfFields = sqlData.length;
        Object[][] data = new Object[numberOfTypes][];
        boolean[][] nulls = new boolean[numberOfTypes][];
        for (int i = 0; i < numberOfTypes; ++i) {
            data[i] = new Object[numberOfFields];
            nulls[i] = new boolean[numberOfFields];

            SQLData typeSample = (SQLData) typeSamples.elementAt(i);            // @D0C
            data[i][0] = typeSample.getTypeName ();                             // @D0C
            data[i][1] = new Short ((short) typeSample.getType ());             // @D0C
            data[i][2] = new Integer (typeSample.getMaximumPrecision());        // @D0C

            String literalPrefix = typeSample.getLiteralPrefix ();              // @D0C
            if (literalPrefix == null) {
                data[i][3]  = "";
                nulls[i][3] = true;
            } else
                data[i][3] = literalPrefix;

            String literalSuffix = typeSample.getLiteralSuffix ();              // @D0C
            if (literalSuffix == null) {
                data[i][4]  = "";
                nulls[i][4] = true;
            } else
                data[i][4] = literalSuffix;

            String createParameters = typeSample.getCreateParameters ();        // @D0C
            if (createParameters == null) {
                data[i][5]  = "";
                nulls[i][5] = true;
            } else
                data[i][5] = createParameters;

            data[i][6]  = new Short ((short) typeNullable);
            data[i][7]  = new Boolean (typeSample.isText ());                   // @D0C
            data[i][8]  = new Short ((short) typeSearchable);
            data[i][9]  = new Boolean (! typeSample.isSigned ());               // @D0C
            data[i][10] = new Boolean (false);
            data[i][11] = new Boolean (false);

            String localName = typeSample.getLocalName ();                      // @D0C
            if (localName == null) {
                data[i][12]  = "";
                nulls[i][12] = true;
            } else
                data[i][12] = localName;

            data[i][13] = new Short ((short) typeSample.getMinimumScale ());    // @D0C
            data[i][14] = new Short ((short) typeSample.getMaximumScale ());    // @D0C
            data[i][15] = new Integer (0);
            data[i][16] = new Integer (0);
            data[i][17] = new Integer (typeSample.getRadix ());                 // @D0C
        }

        JDSimpleRowCache rowCache = new JDSimpleRowCache (formatRow,
                                                          data, nulls);

        return new AS400JDBCResultSet (rowCache,
                                       connection_.getCatalog(), "Type Info");




    }



// JDBC 2.0
/**
Returns the description of the user-defined types
available in a catalog.

@param  catalog         The catalog name. If null is specified, this parameter
                        is ignored.  If empty string is specified,
                        an empty result set is returned.
@param  schemaPattern   The schema name pattern.  If null is specified,
                        no value is sent to the server and the server
                        default of *USRLIBL is used.  If empty string
                        is specified, an empty result set is returned.
@param  typeNamePattern The type name pattern. If null is specified,
                        no value is sent to the server and the server
                        default of *ALL is used.  If empty string
                        is specified, an empty result set is returned.
@param  types           The list of user-defined types to include, or null to
                        include all user-defined types. Valid types are:
                        JAVA_OBJECT, STRUCT, and DISTINCT.
@return                 The ResultSet containing the description of the
                        user-defined available in the catalog.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
//
// Implementation note:
//
// 1. I was worried about cases where one distinct type is created
//    based on another distinct type.  This would cause problems
//    because the source type would no longer identify a system
//    predefined type and we would have to follow the chain until
//    we found a system predefined type.
//
//    It turns out that this is not an issue.  Tony Poirer assured
//    me that (at least in V4R4), the database will not support this.
//    So we can make the assumption for now that the source type
//    always identifies a system predefined type.
//
    public ResultSet getUDTs (String catalog,
                              String schemaPattern,
                              String typeNamePattern,
                              int[] types)
    throws SQLException
    {
        connection_.checkOpen ();

        // Set up the result set in the format required by JDBC
        String[] fieldNames = { "TYPE_CAT",
            "TYPE_SCHEM",
            "TYPE_NAME",
            "CLASS_NAME",
            "DATA_TYPE",
            "REMARKS"
        };

        SQLData[] sqlData = { new SQLVarchar (128, settings_),  // type catalog
            new SQLVarchar (128, settings_),  // type schema
            new SQLVarchar (128, settings_),  // type name
            new SQLVarchar (128, settings_),  // class name
            new SQLSmallint (),               // data type
            new SQLVarchar (2000, settings_)  // remarks
        };

        int[] fieldNullables = {  columnNullable,  // type catalog
            columnNullable,  // type schema
            columnNoNulls,   // type name
            columnNoNulls,   // class name
            columnNoNulls,   // data type
            columnNoNulls,   // remarks
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);
        JDRowCache rowCache = null;

        // We only support DISTINCT in the types array.  Determine
        // if this was passed as one of the elements.  If null is
        // passed, that is like looking for everything, so make that
        // work like DISTINCT was passed.
        boolean distinctPassed = false;
        if (types == null)
            distinctPassed = true;
        else {
            for (int i = 0; i < types.length; ++i)
                if (types[i] == Types.DISTINCT) {
                    distinctPassed = true;
                    break;
                }
        }

        // Check for conditions that would result in an empty
        // result set.
        if ((! isCatalogValid (catalog))
            || ((schemaPattern != null) && (schemaPattern.length () == 0))
            || ((typeNamePattern != null) && (typeNamePattern.length () == 0))
            || (distinctPassed == false))
            rowCache = new JDSimpleRowCache (formatRow);

        // Otherwise, build a request and send.  There is
        // no builtin request for UDTs in the ROI server,
        // so we implement this using a query on a catalog.
        else {

            // Build up the query,
            StringBuffer select = new StringBuffer ();
            select.append ("SELECT USER_DEFINED_TYPE_SCHEMA, "      // @B2C
                           + "USER_DEFINED_TYPE_NAME, "             // @B2C
                           + "SOURCE_TYPE, REMARKS FROM QSYS2");    // @B2C
            select.append (getCatalogSeparator ());
            select.append ("SYSTYPES " );

            StringBuffer where = new StringBuffer ();
            if (schemaPattern != null) {
                JDSearchPattern searchPattern = new JDSearchPattern (schemaPattern);
                where.append (searchPattern.getSQLWhereClause ("USER_DEFINED_TYPE_SCHEMA"));    // @B2C
            }
            if (typeNamePattern != null) {
                JDSearchPattern searchPattern = new JDSearchPattern (typeNamePattern);
                if (where.length () > 0)
                    where.append (" AND ");
                where.append (searchPattern.getSQLWhereClause ("USER_DEFINED_TYPE_NAME"));      // @B2C
            }

            if (where.length () > 0) {
                select.append (" WHERE ");
                select.append (where);
            }

            select.append (" ORDER BY USER_DEFINED_TYPE_SCHEMA, USER_DEFINED_TYPE_NAME");       // @B2C

            // Run the query.
            try {
                Statement statement = connection_.createStatement();
                ResultSet serverResultSet = statement.executeQuery (select.toString());
                JDRowCache serverRowCache = new JDSimpleRowCache (((AS400JDBCResultSet) serverResultSet).getRowCache ());
                statement.close ();

                // Set up the maps.
                JDFieldMap[] maps = {
                    new JDHardcodedFieldMap (connection_.getCatalog ()),    // type catalog
                    new JDSimpleFieldMap (1),                               // type schema
                    new JDSimpleFieldMap (2),                               // type name
                    new JDClassNameFieldMap (3, settings_),                 // class name   // @B3C
                    new JDHardcodedFieldMap (new Integer (Types.DISTINCT)), // data type
                    new JDHandleNullFieldMap (4, "")                        // remarks      // @B3C
                };

                JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
            } catch (SQLException e) {

                // If the system does not have this table, then
                // force an empty result set.  This just means
                // that UDTs are not supported (which is true
                // for pre-V4R4 systems).
                if (e.getErrorCode () == -204)
                    rowCache = new JDSimpleRowCache (formatRow);
                else
                    JDError.throwSQLException (JDError.EXC_INTERNAL, e);
            }
        }

        // Return the result set.
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "UDTs");
    }



/**
Returns the URL for this database.

@return     The URL for this database.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public String getURL ()
    throws SQLException
    {
        connection_.checkOpen ();
        return connection_.getURL();
    }



/**
Returns the current user name as known to the database.

@return     The current user name as known to the database.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public String getUserName ()
    throws SQLException
    {
        connection_.checkOpen ();
        return connection_.getUserName();
    }



/**
Returns a description of a table's columns that are automatically
updated when any value in a row is updated.
@param  catalog        The catalog name. If null is specified, this parameter
                       is ignored.  If empty string is specified,
                       an empty result set is returned.
@param  schema         The schema name. If null is specified, the
                       default library specified in the URL is used.
                       If null is specified and a default library was not
                       specified in the URL, the first library specified
                       in the libraries properties file is used.
                       If null is specified, a default library was
                       not specified in the URL, and a library was not
                       specified in the libraries properties file,
                       QGPL is used.
                       If empty string is specified, an empty result set will
                       be returned.
@param  table          The table name. If null or empty string is specified,
                       an empty result set is returned.

@return                The ResultSet containing the description of the
                       table's columns that are automatically updated
                       when any value in a row is updated.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public ResultSet getVersionColumns (String catalog,
                                        String schema,
                                        String table)
    throws SQLException
    {
        connection_.checkOpen ();

        //--------------------------------------------------------
        //  Set up the result set in the format required by JDBC
        //--------------------------------------------------------

        String[] fieldNames = {"SCOPE",
            "COLUMN_NAME",
            "DATA_TYPE",
            "TYPE_NAME",
            "COLUMN_SIZE",
            "BUFFER_LENGTH",
            "DECIMAL_DIGITS",
            "PSEUDO_COLUMN",
        };

        SQLData[] sqlData = { new SQLSmallint (), // scope
            new SQLVarchar (128, settings_),  // column name
            new SQLSmallint (),    // data type
            new SQLVarchar (128, settings_),  // type name
            new SQLInteger (),     // column size
            new SQLInteger (),     // buffer length
            new SQLSmallint (),    // decimal digits
            new SQLSmallint (),    // pseudo column
        };

        int[] fieldNullables = {columnNoNulls,  // scope
            columnNoNulls,  // column name
            columnNoNulls,  // data type
            columnNoNulls,  // type name
            columnNoNulls,  // column size
            columnNoNulls,  // buffer length
            columnNoNulls,  // decimal digits
            columnNoNulls,  // pseudo column
        };

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames,
                                                 sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows
        // that are readable one at a time.


        try {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // Table is null
                (table==null)      ||

                // Table is empty string
                (table.length()==0 ) ) { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else { // parameter values are valid, build request & send
                // Create a request
                DBReturnObjectInformationRequestDS request =
                new DBReturnObjectInformationRequestDS (
                                                       DBReturnObjectInformationRequestDS.FUNCTIONID_SPECIAL_COLUMN_INFO  ,
                                                       id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                       DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                       DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);



                // Set the library name
                if (schema == null) {   // use default library or qgpl
                    request.setLibraryName(connection_.getDefaultSchema(), connection_.getConverter());
                } else request.setLibraryName(schema, connection_.getConverter());

                // Set the table name
                request.setFileName(table, connection_.getConverter());


                // Set the Field Information to Return Bitmap
                // Return library, table, and column
                request.setSpecialColumnsReturnInfoBitmap(0x1F000000);


                // Set the short / long file and field name indicator
                request.setFileShortOrLongNameIndicator(0xF0); // Long

                // Set columns nullable indicator to allows nullable columns
                request.setSpecialColumnsNullableIndicator(0xF1);


                //--------------------------------------------------------
                //  Send the request and cache all results from the server
                //--------------------------------------------------------

                DBReplyRequestedDS reply = connection_.sendAndReceive(request);


                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0) {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (connection_, id_,
                                               errorClass, returnCode);
                }

                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();
                if (resultData != null) {
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                    JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                        1, resultData, true));
                
                    // Create the mapped row format that is returned in the
                    // result set.
                    // This does not actual move the data, it just sets up
                    // the mapping.
                    JDFieldMap[] maps = new JDFieldMap[8];
                    maps[0] = new JDHardcodedFieldMap (new Short ((short) 0)); // scope
                    maps[1] = new JDSimpleFieldMap (1); // column name
                    maps[2] = new JDDataTypeFieldMap (2, 3, 3, 5);   // data type - converted to short
                    maps[3] = new JDSimpleFieldMap (2);  // type name
                    maps[4] = new JDSimpleFieldMap (3); // column size (precision)
                    maps[5] = new JDSimpleFieldMap(4); // buffer length
                    maps[6] = new JDSimpleFieldMap (5); // decimal digits (scale)
                    maps[7] = new JDHardcodedFieldMap (new Short ((short) versionColumnNotPseudo));
            
                    // Create the mapped row cache that is returned in the
                    // result set
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }
                else
                    rowCache = new JDSimpleRowCache (formatRow);

            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "VersionColumns");
    }



// JDBC 2.0
/**
Indicates if visible inserts to a result set of the specified type
can be detected by calling ResultSet.rowInserted().

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     Always false.  Inserts can not be detected
                            by calling ResultSet.rowInserted().

@exception  SQLException    If the result set type is not valid.
**/
    public boolean insertsAreDetected (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return false;
    }



/**
Indicates if a catalog appears at the start or the end of
a qualified name.

@return     Always true. A catalog appears at the start of a
            qualified name.

@exception  SQLException    This exception is never thrown.
**/
    public boolean isCatalogAtStart ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if the specified catalog is valid for a query.
If false is returned, any query using that catalog will
be empty since the catalog does not refer to the current
connection's catalog.

@param  catalog     The catalog name. "" for no catalog
                    or null to drop catalog from the
                    selection criteria.
@return             true if catalog name is valid; false otherwise

@exception  SQLException    This exception is never thrown.
**/
    private boolean isCatalogValid (String catalog)
    throws SQLException
    {
        if (catalog == null) 
            return true;
        
        return (catalog.equalsIgnoreCase (connection_.getCatalog())
                || (catalog.equalsIgnoreCase ("localhost")));
    }



/**
Indicates if the database is in read-only mode.

@return     true if in read-only mode; false otherwise.

@exception  SQLException    If the connection is not open
                            or an error occurs.
**/
    public boolean isReadOnly ()
    throws SQLException
    {
        connection_.checkOpen ();
        return connection_.isReadOnly ();
    }



/**
Indicates if concatenations between null and non-null values
are null.


@return     Always true. Concatenations between null and non-null
            values are null.

@exception  SQLException    This exception is never thrown.
**/
    public boolean nullPlusNonNullIsNull ()
    throws SQLException
    {
        return true;
    }



/**
 Indicates if null values are sorted at the end regardless of sort
 order.

@return     Always false. Null values are not sorted at the end
            regardless of sort order.

@exception  SQLException    This exception is never thrown.
**/
    public boolean nullsAreSortedAtEnd ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if null values are sorted at the start regardless of sort
order.

@return     Always false. Null values are not sorted at the start
            regardless of sort order.

@exception  SQLException    This exception is never thrown.
**/
    public boolean nullsAreSortedAtStart ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if null values are sorted high.

@return     Always true. Null values are sorted high.

@exception  SQLException    This exception is never thrown.
**/
    public boolean nullsAreSortedHigh ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if null values are sorted low.

@return     Always false. Null values are not sorted low.

@exception  SQLException    This exception is never thrown.
**/
    public boolean nullsAreSortedLow ()
    throws SQLException
    {
        return false;
    }



// JDBC 2.0
/**
Indicates if deletes made by others are visible.

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     true if deletes made by others
                            are visible; false otherwise.

@exception  SQLException    If the result set type is not valid.
**/
    public boolean othersDeletesAreVisible (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
    }



// JDBC 2.0
/**
Indicates if inserts made by others are visible.

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     true if inserts made by others
                            are visible; false otherwise.

@exception  SQLException    If the result set type is not valid.
**/
    public boolean othersInsertsAreVisible (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
    }



// JDBC 2.0
/**
Indicates if updates made by others are visible.

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     true if updates made by others
                            are visible; false otherwise.

@exception  SQLException    If the result set type is not valid.
**/
    public boolean othersUpdatesAreVisible (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
    }



// JDBC 2.0
/**
Indicates if a result set's own deletes are visible.

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     true if the result set's own deletes
                            are visible; false otherwise.

@exception  SQLException    If the result set type is not valid.
**/
    public boolean ownDeletesAreVisible (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
    }



// JDBC 2.0
/**
Indicates if a result set's own inserts are visible.

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     true if the result set's own inserts
                            are visible; false otherwise.

@exception  SQLException    If the result set type is not valid.
**/
    public boolean ownInsertsAreVisible (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
    }



// JDBC 2.0
/**
Indicates if a result set's own updates are visible.

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     true if the result set's own updates
                            are visible; false otherwise.

@exception  SQLException    If the result set type is not valid.
**/
    public boolean ownUpdatesAreVisible (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
    }



/**
Indicates if the database treats mixed case, unquoted SQL identifiers
as case insensitive and stores them in lowercase.

@return     Always false. The database does not treat mixed case,
            unquoted SQL identifiers as case insensitive and store
            them in lowercase.

@exception  SQLException    This exception is never thrown.
**/
    public boolean storesLowerCaseIdentifiers ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if the database treats mixed case, quoted SQL identifiers
as case insensitive and stores them in lowercase.

@return     Always false. The database does not treat mixed case, quoted
            SQL identifiers as case insensitive and store them in
            lowercase.

@exception  SQLException    This exception is never thrown.
*/
    public boolean storesLowerCaseQuotedIdentifiers ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if the database treats mixed case, unquoted SQL identifiers
as case insensitive and stores them in mixed case.

@return     Always false. The database does not treat mixed case, unquoted
            SQL identifiers as case insensitive and store them in
            mixed case.

@exception  SQLException    This exception is never thrown.
**/
    public boolean storesMixedCaseIdentifiers ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if the database treats mixed case, quoted SQL identifiers
as case insensitive and stores them in mixed case.

@return     Always true. The database does treat mixed case, quoted
            SQL identifiers as case insensitive and store them in
            mixed case.

@exception  SQLException    This exception is never thrown.
**/
    public boolean storesMixedCaseQuotedIdentifiers ()
    throws SQLException
    {
        // @A2C changed from false to true
        return true;
    }



/**
Indicates if the database treats mixed case, unquoted SQL identifiers
as case insensitive and stores them in uppercase.

@return     Always true. The database does treat mixed case, unquoted
            SQL identifiers as case insensitive and store them
            in uppercase.

@exception  SQLException    This exception is never thrown.
**/
    public boolean storesUpperCaseIdentifiers ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if the database treats mixed case, quoted SQL identifiers
as case insensitive and stores them in uppercase.

@return     Always false. The database does not treat mixed case, quoted
            SQL identifiers as case insensitive and store them
            in uppercase.

@exception  SQLException    This exception is never thrown.
**/
    public boolean storesUpperCaseQuotedIdentifiers ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if ALTER TABLE with ADD COLUMN is supported.

@return     Always true.   ALTER TABLE with ADD COLUMN is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsAlterTableWithAddColumn ()
    throws SQLException
    {
        // @A2C Changed from false to true
        return true;
    }



/**
Indicates if ALTER TABLE with DROP COLUMN is supported.

@return     Always true. ALTER TABLE with DROP COLUMN is not supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsAlterTableWithDropColumn ()
    throws SQLException
    {
        // @A2C Changed from false to true
        return true;
    }



/**
Indicates if the ANSI92 entry-level SQL grammar is supported.

@return     Always true. The ANSI92 entry-level SQL grammar is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsANSI92EntryLevelSQL ()
    throws SQLException
    {
        // ANSI92EntryLevelSQL is supported for V4R2 and beyond
        // true is always returned since it is checked for compliance.
        return true;

    }



/**
Indicates if the ANSI92, full SQL grammar is supported.

@return     Always false. ANSI92, full SQL grammar is not supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsANSI92FullSQL ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if the ANSI92 intermediate-level SQL grammar is supported.

@return     Always false. ANSI92 intermediate-level SQL grammar is not supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsANSI92IntermediateSQL ()
    throws SQLException
    {
        return false;
    }



// JDBC 2.0
/**
Indicates if the batch updates are supported.

@return     Always true. Batch updates are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsBatchUpdates ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if a catalog name can be used in a data manipulation
statement.

@return     Always false. A catalog name can not be used in a data manipulation
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsCatalogsInDataManipulation ()
    throws SQLException
    {
        // @A2 Changed from true to false.
        return false;
    }



/**
Indicates if a catalog name can be used in an index definition
statement.

@return     Always false. A catalog name can not be used in an index definition
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsCatalogsInIndexDefinitions ()
    throws SQLException
    {
        // @A2C Changed from true to false
        return false;
    }



/**
Indicates if a catalog name can be used in a privilege definition
statement.

@return     Always false. A catalog name can not be used in a privilege definition
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsCatalogsInPrivilegeDefinitions ()
    throws SQLException
    {
        // @A2C Changed from true to false
        return false;
    }



/**
Indicates if a catalog name can be used in a procedure call
statement.

@return     Always false. A catalog name can not be used in a procedure call
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsCatalogsInProcedureCalls ()
    throws SQLException
    {
        // @A2C Changed from true to false
        return false;
    }



/**
Indicates if a catalog name can be used in a table definition
statement.

@return     Always false. A catalog name can not be used in a table definition
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsCatalogsInTableDefinitions ()
    throws SQLException
    {
        // @A2C Changed from true to false
        return false;
    }



/**
Indicates if column aliasing is supported.  Column aliasing means
that the SQL AS clause can be used to provide names for
computed columns or to provide alias names for column
as required.

@return     Always true. Column aliasing is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsColumnAliasing ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if the CONVERT function between SQL types is supported.

@return     true if the CONVERT function between SQL types is supported;
            false otherwise.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsConvert ()
    throws SQLException
    {
        return JDEscapeClause.supportsConvert ();
    }



/**
Indicates if CONVERT between the given SQL types is supported.

@param      fromType        The SQL type code defined in java.sql.Types.
@param      toType          The SQL type code defined in java.sql.Types.
@return     true if CONVERT between the given SQL types is supported;
            false otherwise.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsConvert (int fromType, int toType)
    throws SQLException
    {
        return JDEscapeClause.supportsConvert (fromType, toType);
    }



/**
Indicates if the ODBC Core SQL grammar is supported.

@return     Always true. The ODBC Core SQL grammar is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsCoreSQLGrammar ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if the correlated subqueries are supported.

@return     Always true. Correlated subqueries are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsCorrelatedSubqueries ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if both data definition and data manipulation statements
are supported within a transaction.

@return     Always true. Data definition and data manipulation statements
            are both supported within a transaction.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsDataDefinitionAndDataManipulationTransactions ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if data manipulation statements are supported within a transaction.

@return     Always false.  Data manipulation statements are not supported within
            a transaction.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsDataManipulationTransactionsOnly ()
    throws SQLException
    {
        // @A2C Changed from true to false
        return false;
    }



/**
Indicates if table correlation names are supported, and if so, are they
restricted to be different from the names of the tables.

@return     Always false.  Table correlation names are not restricted
            to be different from the names of the tables.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsDifferentTableCorrelationNames ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if expressions in ORDER BY lists are supported.

@return     Always false. Expression in ORDER BY lists are not supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsExpressionsInOrderBy ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if the ODBC Extended SQL grammar is supported.

@return     Always false. The ODBC Extended SQL grammar is not supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsExtendedSQLGrammar ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if full nested outer joins are supported.

@return     Always false. Full nested outer joins are not supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsFullOuterJoins ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if some form of the GROUP BY clause is supported.

@return     Always true. Some form of GROUP BY clause is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsGroupBy ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if a GROUP BY clause can add columns not in the SELECT
provided it specifies all of the columns in the SELECT.

@return     Always true. A GROUP BY clause can add columns not in the SELECT
            provided it specifies all of the columns in the SELECT.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsGroupByBeyondSelect ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if a GROUP BY clause can use columns not in the SELECT.

@return     Always true.  A GROUP BY clause can use columns not in the SELECT.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsGroupByUnrelated ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if the SQL Integrity Enhancement Facility is supported.

@return     Always false. The SQL Integrity Enhancement Facility is not
            supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsIntegrityEnhancementFacility ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if the escape character in LIKE clauses is supported.

@return     Always true. The escape character in LIKE clauses is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsLikeEscapeClause ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if there is limited support for outer joins.

@return     Always true. There is limited support for outer joins.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsLimitedOuterJoins ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if the ODBC Minimum SQL grammar is supported.

@return     Always true. The ODBC Minimum SQL grammar is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsMinimumSQLGrammar ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if the database treats mixed case, unquoted SQL
identifiers as case sensitive and stores
them in mixed case.

@return     Always false. The database does not treat mixed case,
            unquoted SQL identifiers as case sensitive and as
            a result store them in mixed case.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsMixedCaseIdentifiers ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if the database treats mixed case, quoted SQL
identifiers as case sensitive and as a result stores
them in mixed case.

@return     Always true. The database does treat mixed case, quoted SQL
            identifiers as case sensitive and stores
            them in mixed case.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsMixedCaseQuotedIdentifiers ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if multiple result sets from a single execute are
supported.

@return     Always true. Multiple result sets from a single execute
            are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsMultipleResultSets ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if multiple transactions can be open at once (on
different connections).

@return     Always true. Multiple transactions can be open at
            once on different connections.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsMultipleTransactions ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if columns can be defined as non-nullable.

@return     Always true. Columns can be defined as non-nullable.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsNonNullableColumns ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if cursors can remain open across commits.

@return     Always true. Cursors can remain open across commits.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsOpenCursorsAcrossCommit ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if cursors can remain open across rollback.

@return     Always true. Cursors can remain open across rollback.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsOpenCursorsAcrossRollback ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if statements can remain open across commits.

@return     Always true. Statements can remain open across commits.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsOpenStatementsAcrossCommit ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if statements can remain open across rollback.

@return     Always true. Statements can remain open across rollback.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsOpenStatementsAcrossRollback ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if an ORDER BY clause can use columns not in the SELECT.

@return     Always false. ORDER BY cannot use columns not in the SELECT.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsOrderByUnrelated ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if some form of outer join is supported.

@return     Always true. Some form of outer join is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsOuterJoins ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if positioned DELETE is supported.

@return     Always true.  Positioned DELETE is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsPositionedDelete ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if positioned UPDATE is supported.

@return     Always true. Positioned UPDATE is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsPositionedUpdate ()
    throws SQLException
    {
        return true;
    }



// JDBC 2.0  // @C0C
/**
Indicates if the specified result set concurrency is supported
for the specified result set type.

<p>This chart describes the combinations of result set concurrency
and type that this driver supports:
<br> <br>
<table border=1>
<tr><th><br></th><th>CONCUR_READ_ONLY</th><th>CONCUR_UPDATABLE</th></tr>
<tr><td>TYPE_FORWARD_ONLY</td><td>Yes</td><td>Yes</td></tr>
<tr><td>TYPE_SCROLL_INSENSITIVE</td><td>Yes</td><td>No</td></tr>
<tr><td>TYPE_SCROLL_SENSITIVE</td><td>No</td><td>Yes</td></tr>
</table>
<br>

@param resultSetType            The result set type.  Valid values are:
                                <ul>
                                  <li>ResultSet.TYPE_FORWARD_ONLY
                                  <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                                  <li>ResultSet.TYPE_SCROLL_SENSITIVE
                                </ul>
@param resultSetConcurrency     The result set concurrency.  Valid values are:
                                <ul>
                                  <li>ResultSet.CONCUR_READ_ONLY
                                  <li>ResultSet.CONCUR_UPDATABLE
                                </ul>
@return                         true if the specified result set
                                concurrency is supported for the specified
                                result set type; false otherwise.

@exception  SQLException        If the result set type or result set
                                concurrency is not valid.
**/
//
// Implementation note:
//
// The unsupported combinations are dictated by the AS/400 DB2
// cursor support.
//
    public boolean supportsResultSetConcurrency (int resultSetType, int resultSetConcurrency)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        // Validate the result set concurrency.
        if ((resultSetConcurrency != ResultSet.CONCUR_READ_ONLY)
            && (resultSetConcurrency != ResultSet.CONCUR_UPDATABLE))
            JDError.throwSQLException (JDError.EXC_CONCURRENCY_INVALID);

        // Cases that we don't support.
        if (((resultSetConcurrency == ResultSet.CONCUR_READ_ONLY)
             && (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE))
            || ((resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
                && (resultSetType == ResultSet.TYPE_SCROLL_INSENSITIVE)))
            return false;

        // We support all other cases.
        return true;
    }



// JDBC 2.0
/**
Indicates if the specified result set type is supported.

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     Always true.  All result set types
                            are supported.

@exception  SQLException    If the result set type is not valid.
**/
    public boolean supportsResultSetType (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        if ((resultSetType != ResultSet.TYPE_FORWARD_ONLY)
            && (resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE)
            && (resultSetType != ResultSet.TYPE_SCROLL_SENSITIVE))
            JDError.throwSQLException (JDError.EXC_CONCURRENCY_INVALID);

        return true;
    }



/**
Indicates if a schema name can be used in a data manipulation
statement.

@return     Always true. A schema name can be used in a data
            manipulation statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSchemasInDataManipulation ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if a schema name can be used in an index definition
statement.

@return     Always true. A schema name can be used in an index definition
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSchemasInIndexDefinitions ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if a schema name be can used in a privilege definition
statement.

@return     Always true. A schema name can be used in a privilege definition
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSchemasInPrivilegeDefinitions ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if a schema name be can used in a procedure call
statement.

@return     Always true. A schema name can be used in a procedure call
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSchemasInProcedureCalls ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if a schema name can be used in a table definition
statement.

@return     Always true. A schema name can be used in a table definition
            statement.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSchemasInTableDefinitions ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if SELECT for UPDATE is supported.

@return     Always true. SELECT for UPDATE is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSelectForUpdate ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if stored procedure calls using the stored procedure
escape syntax are supported.

@return     Always true. Stored procedure calls using the stored
            procedure escape syntax are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsStoredProcedures ()
    throws SQLException
    {
        return true;
    }




/**
Indicates if subqueries in comparisons are supported.

@return     Always true. Subqueries in comparisons are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSubqueriesInComparisons ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if subqueries in EXISTS expressions are supported.

@return     Always true. Subqueries in EXISTS expressions are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSubqueriesInExists ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if subqueries in IN expressions are supported.

@return     Always true. Subqueries in IN expressions are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSubqueriesInIns ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if subqueries in quantified expressions are supported.

@return     Always true. Subqueries in quantified expressions are
            supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsSubqueriesInQuantifieds ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if table correlation names are supported.

@return     Always true. Table correlation names are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsTableCorrelationNames ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if the database supports the given transaction
isolation level.

@param      transactionIsolationLevel   One of the Connection.TRANSACTION_*
                                        values.
@return                                 Always true.  All transaction isolation
                                        levels are supported.

@exception  SQLException  If the transaction isolation level is not valid.
**/
    public boolean supportsTransactionIsolationLevel (int transactionIsolationLevel)
    throws SQLException
    {
        // Validate the transaction isolation level.
        if ((transactionIsolationLevel != Connection.TRANSACTION_NONE)
            && (transactionIsolationLevel != Connection.TRANSACTION_READ_UNCOMMITTED)
            && (transactionIsolationLevel != Connection.TRANSACTION_READ_COMMITTED)
            && (transactionIsolationLevel != Connection.TRANSACTION_REPEATABLE_READ)
            && (transactionIsolationLevel != Connection.TRANSACTION_SERIALIZABLE))
            JDError.throwSQLException (JDError.EXC_CONCURRENCY_INVALID);

        return true;
    }



/**
Indicates if transactions are supported.

@return     Always true. Transactions are supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsTransactions ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if SQL UNION is supported.

@return     Always true. SQL UNION is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsUnion ()
    throws SQLException
    {
        return true;
    }



/**
Indicates if SQL UNION ALL is supported.

@return     Always true. SQL UNION ALL is supported.

@exception  SQLException    This exception is never thrown.
**/
    public boolean supportsUnionAll ()
    throws SQLException
    {
        return true;
    }



/**
Returns the name of the catalog.

@return        The name of the catalog.
**/
    public String toString ()
    {
        try {
            return connection_.getCatalog();
        } catch (SQLException e) {
            return super.toString ();
        }
    }



// JDBC 2.0
/**
Indicates if visible updates to a result set of the specified type
can be detected by calling ResultSet.rowUpdated().

@param resultSetType        The result set type.  Value values are:
                            <ul>
                              <li>ResultSet.TYPE_FORWARD_ONLY
                              <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                              <li>ResultSet.TYPE_SCROLL_SENSITIVE
                            </ul>
@return                     Always false.  Updates can not be detected
                            by calling ResultSet.rowUpdated().

@exception  SQLException    If the result set type is not valid.
**/
    public boolean updatesAreDetected (int resultSetType)
    throws SQLException
    {
        // Validate the result set type.
        supportsResultSetType (resultSetType);

        return false; 
    }



/**
Indicates if the database uses a file for each table.

@return     Always false. The database does not use a file for each
            table.

@exception  SQLException    This exception is never thrown.
**/
    public boolean usesLocalFilePerTable ()
    throws SQLException
    {
        return false;
    }



/**
Indicates if the database stores tables in a local file.

@return     Always false. The database does not store tables in a local
            file.

@exception  SQLException    This exception is never thrown.
**/
    public boolean usesLocalFiles ()
    throws SQLException
    {
        return false;
    }



}
