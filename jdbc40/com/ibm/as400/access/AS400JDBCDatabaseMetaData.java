///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCDatabaseMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;                            // @D0A
import java.io.IOException;                         // @J0A



// @E4C
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
the pattern, provided they exist on the system:
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

<p>Schema and table names that are passed as input to methods
in this class are implicitly uppercased unless enclosed in
double-quotes.
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
//             schemaPattern (library) - no value sent to system.
//					 System default of
//                                       *USRLIBL is used.
//             tablePattern (file)     - no value sent to system
//                                       system default of *ALL used
//     empty string is specified for:
//             schemaPattern (library) - empty result set is returned
//             tablePattern (file)     - empty result set is returned
//
//
//----------------------------------------------------------

public class AS400JDBCDatabaseMetaData extends ToolboxWrapper //@pdc jdbc40
implements DatabaseMetaData
{
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";


    static final int    JDBC_MAJOR_VERSION          = 4;        // @G4A //@pdc
    static final int    JDBC_MINOR_VERSION          = 0;        // @G4A

    //New constants for JDBC 3.0.
    public static final int sqlStateXOpen = 1;
    public static final int sqlStateSQL99 = 2;


    // Private data.
    private AS400JDBCConnection     connection_;
    private int                     id_;
    private SQLConversionSettings   settings_;




    /**
    Constructs an AS400JDBCDatabaseMetaData object.
    
    @param   connection  The connection to the system.
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



    //@G4A
    /**
    Returns a ResultSet containing a description of type attributes available in a 
    specified catalog. 
    
    This method only applies to the attributes of a 
    structured type.  Distinct types are stored in the datatypes 
    catalog, not the attributes catalog. Since DB2 for i5/OS does not support 
    structured types at this time, an empty ResultSet will always be returned
    for calls to this method.
    
    @param  catalog              The catalog name. 
    @param  schemaPattern        The schema name pattern.  
    @param  typeNamePattern      The type name pattern. 
    @param  attributeNamePattern The attribute name pattern.  
                            
    @return      The empty ResultSet
    
    @exception   SQLException  This exception is never thrown.
    @since Modification 5
    **/
    public ResultSet getAttributes (String catalog, String schemaPattern, 
                                    String typeNamePattern, String attributeNamePattern)
    throws SQLException
    {
        // We return an empty result set because this is not supported by our driver
        Statement statement = connection_.createStatement();
        return statement.executeQuery("SELECT VARCHAR('1', 128) AS TYPE_CAT, " +
                                      "VARCHAR('2', 128)  AS TYPE_SCHEM, " +
                                      "VARCHAR('3', 128)  AS TYPE_NAME, " +
                                      "VARCHAR('4', 128)  AS ATTR_NAME, " +
                                      "SMALLINT(5)        AS DATA_TYPE, " + 
                                      "VARCHAR('6', 128)  AS ATTR_TYPE_NAME, " + 
                                      "INT(7)             AS ATTR_SIZE, " + 
                                      "INT(8)             AS DECIMAL_DIGITS, " + 
                                      "INT(9)             AS NUM_PREC_RADIX, " + 
                                      "INT(10)            AS NULLABLE, " + 
                                      "VARCHAR('11', 128) AS REMARKS, " + 
                                      "VARCHAR('12', 128) AS ATTR_DEF, " + 
                                      "INT(13)            AS SQL_DATA_TYPE, " + 
                                      "INT(14)            AS SQL_DATETIME_SUB, " + 
                                      "INT(15)            AS CHAR_OCTET_LENGTH, " + 
                                      "INT(16)            AS ORDINAL_POSITION, " + 
                                      "VARCHAR('17', 128) AS IS_NULLABLE, " + 
                                      "VARCHAR('18', 128) AS SCOPE_CATALOG, " + 
                                      "VARCHAR('19', 128) AS SCOPE_SCHEMA, " + 
                                      "VARCHAR('20', 128) AS SCOPE_TABLE, " + 
                                      "SMALLINT(21)       AS SOURCE_DATA_TYPE " + 
                                      "FROM QSYS2" + getCatalogSeparator() + 
                                      "SYSTYPES WHERE 1 = 2 FOR FETCH ONLY ");
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
        try
        {
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
                     (connection_.getTransactionIsolation() != connection_.TRANSACTION_REPEATABLE_READ)))))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }


            else
            { // parameter values are valid, build request & send
              // Create a request
              //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {

                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_SPECIAL_COLUMN_INFO  ,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);



                    // Set the library name
                    if (schema == null)
                    {   // use default library or qgpl
                        request.setLibraryName(connection_.getDefaultSchema(), connection_.converter_); //@P0C
                    }
                    else request.setLibraryName(normalize(schema), connection_.converter_);       // @E4C @P0C

                    // Set the table name
                    request.setFileName(normalize(table), connection_.converter_);      // @E4C @P0C


                    // Set the Field Information to Return Bitmap
                    // Return library, table, and column

                    request.setSpecialColumnsReturnInfoBitmap(0x1F000000);


                    // Set the short / long file and field name indicator
                    request.setFileShortOrLongNameIndicator(0xF0); // Long

                    // Set if columns are nullable
                    request.setSpecialColumnsNullableIndicator(nullable ?
                                                               0xF1 : 0xF0);

                    //--------------------------------------------------------
                    //  Send the request and cache all results from the system
                    //--------------------------------------------------------

                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }

                    // Get the data format and result data
                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();
                    if (resultData != null)
                    {
                        JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                        JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache(row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));
                        JDFieldMap[] maps = new JDFieldMap[8];
                        maps[0] = new JDHardcodedFieldMap(new Short ((short) scope)); // scope
                        maps[1] = new JDSimpleFieldMap (1); // column name
                        maps[2] = new JDDataTypeFieldMap (2, 4, 3, 5, 0, connection_.getVRM(), connection_.getProperties());   // @M0C // data type - converted to short   //@KKB pass 0 for ccsid since cannot get ccsid from host server
                        maps[3] = new JDSimpleFieldMap (2);  // type name
                        maps[4] = new JDSimpleFieldMap (4); // column size (length)
                        maps[5] = new JDHardcodedFieldMap(new Integer (0)); // buffer length
                        maps[6] = new JDSimpleFieldMap (5); // decimal digits (scale)
                        maps[7] = new JDHardcodedFieldMap(new Short ((short) versionColumnNotPseudo));
                        JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                        rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                    }
                    else
                        rowCache = new JDSimpleRowCache(formatRow);
                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "BestRowIdentifier");
    }





    /**
    Returns the catalog name available in this database.  This
    will return a ResultSet with a single row, whose value is
    the i5/OS system name.
    
    @return      The ResultSet containing the i5/OS system name.
    
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
        boolean[][] nulls = {{false}};
        boolean[][] dataMappingErrors = {{false}};

        // If running to a system running OS/400 v5r2 or i5/OS the list can contain more than just the system
        // name (when IASPs are on the system).  Try to retrieve that list.  Note 
        // if getting the list fails we will still return a result set containing
        // one item -- the name of the system.  We just built that result set 
        // (the previous six lines of code) and that is what we will return.  That
        // result set will be consistent with the result set returned when connecting
        // to OS/400 v5r1 or earlier versions.  If getting the list works we will
        // build and return a new result set containing data retrieved from the system. 
        if (connection_.getVRM() >= JDUtilities.vrm520)                                  // @F1a
        {                                                                                // @F1a
            try
            {                                                                          // @F1a                                                                            // @F1a
                Vector RDBEntries = new Vector();                                        // @F1a
                Statement statement = connection_.createStatement();                     // @F1a
                ResultSet rs = statement.executeQuery("SELECT LOCATION FROM QSYS2" + getCatalogSeparator() + "SYSCATALOGS WHERE RDBTYPE = 'LOCAL' AND RDBASPSTAT='AVAILABLE' ");  // @F1a
                while (rs.next())                                                        // @F1a
                {                                                                        // @F1a
                    RDBEntries.add(rs.getString(1).trim());                              // @F1a
                }                                                                        // @F1a
                rs.close();                                                              // @F1a
                statement.close();                                                       // @F1a
                int count = RDBEntries.size();                                           // @F1a
                if (count > 0)                                                           // @F1a
                {                                                                        // @F1a
                    data = new Object[count][1];                                         // @F1a
                    nulls = new boolean[count][1];                                       // @F1a
                    dataMappingErrors = new boolean[count][1];
                    for (int i=0; i<count; i++)                                          // @F1a
                    {                                                                    // @F1a
                        data[i][0] = RDBEntries.elementAt(i);                            // @F1a
                        nulls[i][0] = false;                                             // @F1a
                        dataMappingErrors[i][0] = false;
                    }                                                                    // @F1a
                }                                                                        // @F1a
                else
                {                                                                     // @F1a                                                                        // @F1a
                    if (JDTrace.isTraceOn())                                             // @F1a
                        JDTrace.logInformation (this, "Could not retrieve list of RDBs from system (count = 0).");  // @F1a
                }                                                                        // @F1a
            }                                                                            // @F1a
            catch (Exception e)                                                          // @F1a
            {                                                                            // @F1a
                if (JDTrace.isTraceOn())                                                 // @F1a
                    JDTrace.logInformation (this, "Could not retrieve list of RDBs from system (exception).");     // @F1a
            }                                                                            // @F1a
        }                                                                                // @F1a

        JDSimpleRowCache rowCache = new JDSimpleRowCache (formatRow, data, nulls, dataMappingErrors);
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "Catalogs");
    }



    /**
    Returns the naming convention used when referring to tables.
    This depends on the naming convention specified in the connection
    properties.
    
    @return     If using SQL naming convention, "." is returned. If
                using system naming convention, "/" is returned.
    
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
    Returns the DB2 for i5/OS SQL term for "catalog".
    
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
                            no value is sent to the system and the system
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
                ((columnPattern != null) && (columnPattern.length()==0)))
        { // Return empty result set
            rowCache = new JDSimpleRowCache (formatRow);
            return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "ColumnPrivileges"); //@PDC
            
        }
        else if (connection_.getProperties().getString(JDProperties.METADATA_SOURCE).equals( JDProperties.METADATA_SOURCE_ROI))
        {
            // parameter values are valid, build request & send
            // Create a request
            //@P0C
            DBReturnObjectInformationRequestDS request = null;
            DBReplyRequestedDS reply = null;
            try
            {
                request = DBDSPool.getDBReturnObjectInformationRequestDS (
                        DBReturnObjectInformationRequestDS.FUNCTIONID_FIELD_INFO,
                        id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                        DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                        DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);
                // Set the library name
                if (schema == null)
                {   // use default library or qgpl
                    request.setLibraryName(normalize(connection_.getDefaultSchema()), connection_.converter_);  // @E4C @P0C
                }
                else request.setLibraryName(normalize(schema), connection_.converter_);       // @E4C @P0C
                
                // Set the table name
                request.setFileName(normalize(table), connection_.converter_);                  // @E4C @P0C
                
                
                // Set the column name and search pattern
                // If null, do not set parameter. The system default
                // value of *ALL is used.
                if (!(columnPattern==null))
                {
                    JDSearchPattern column = new JDSearchPattern(columnPattern);
                    request.setFieldName(column.getPatternString(), connection_.converter_); //@P0C
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
                //  Send the request and cache all results from the system
                //--------------------------------------------------------
                reply = connection_.sendAndReceive(request);
                
                
                // Check for errors - throw exception if errors were
                // returned
                int errorClass = reply.getErrorClass();
                if (errorClass !=0)
                {
                    int returnCode = reply.getReturnCode();
                    JDError.throwSQLException (this, connection_, id_,
                            errorClass, returnCode);
                }
                // Get the data format and result data
                DBDataFormat dataFormat = reply.getDataFormat();
                DBData resultData = reply.getResultData();   
                if (resultData != null)
                {
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                    JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache(row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));
                    // Create the mapped row format that is returned in the
                    // result set.
                    // This does not actual move the data, it just sets up
                    // the mapping.
                    JDFieldMap[] maps = new JDFieldMap[8];
                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[1] = new JDSimpleFieldMap (1); // library
                    maps[2] = new JDSimpleFieldMap (2); // table
                    maps[3] = new JDSimpleFieldMap (3); // column
                    maps[4] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true, false ); // grantor
                    maps[5] = new JDHardcodedFieldMap (getUserName ()); // grantee - return userid
                    maps[6] = new JDHardcodedFieldMap(""); // privilege
                    maps[7] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_),true, false ); // is_grantable
                    // Create the mapped row cache that is returned in the
                    // result set
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }
                else
                    rowCache = new JDSimpleRowCache(formatRow);
            } catch (DBDataStreamException e)
            {
                JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
            }
            finally
            {
                if (request != null) request.inUse_ = false;
                if (reply != null) reply.inUse_ = false;
            }
            // Return the results
            return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "ColumnPrivileges");
     
        }  // End of else to build and send request
        
        else
        {
            //@PDC change to use sysibm.sqlcolprivileges stored procedure 
            
            // Set the library name
            if (schema == null)
            {   // use default library or qgpl
                schema = normalize(connection_.getDefaultSchema());
            }
            else schema = normalize(schema);
            
            // Set the table name
            table = normalize(table);
            
            // Set the column name and search pattern
            // If null, do not set parameter. The system default
            // value of *ALL is used.
            
            CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator () + "SQLCOLPRIVILEGES (?, ?, ?, ?, ?)");
            
            cstmt.setString(1, catalog);
            cstmt.setString(2, schema);
            cstmt.setString(3, table);
            cstmt.setString(4, columnPattern);
            cstmt.setObject(5, "DATATYPE='JDBC';CURSORHOLD=1");  //@pdc options per db2 common design
            ResultSet rs = cstmt.executeQuery();
            return rs;
        }
             
    }




    /**
    Returns a description of the table's columns available in a
    catalog.  
    
    @param  catalog         The catalog name. If null is specified, this parameter
                            is ignored.  If empty string is specified,
                            an empty result set is returned.
    @param  schemaPattern   The schema name pattern.  If null is specified,
                            no value is sent to the system and the system
                            default of *USRLIBL is used.  If empty string
                            is specified, an empty result set is returned.
    @param  tablePattern    The table name pattern. If null is specified,
                            no value is sent to the system and the system
                            default of *ALL is used.  If empty string
                            is specified, an empty result set is returned.
    @param  columnPattern   The column name pattern.  If null is specified,
                            no value is sent to the system and the system
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


        boolean isJDBC3 = JDUtilities.JDBCLevel_ >= 30; //@F2A @j4a

        String[] fieldNames = null;               //@F2C
        SQLData[] sqlData = null;                 //@F2C
        int[] fieldNullables = null;              //@F2C
        //@F2A Result sets must be different depending on whether we are running under JDBC 3.0
        if (!isJDBC3)                             //@F2A
        {
            // Set up the result set in the format required by JDBC
            fieldNames = new String[] {"TABLE_CAT",
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
                "IS_NULLABLE",
            };

            sqlData = new SQLData[] { new SQLVarchar (128, settings_), // catalog
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
                new SQLVarchar ((connection_.getVRM() >= JDUtilities.vrm550) ? 2000 : 254, settings_),  // column def   //@550 Column default value support
                new SQLInteger (),  // sql data type
                new SQLInteger (),  // datetime sub
                new SQLInteger (),  // octet length
                new SQLInteger (),  // ordinal
                new SQLVarchar (254, settings_),  // is nullable
            };

            fieldNullables = new int[] {columnNullable, // catalog
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
        }
        else
        {                                      //@F2A
            // Set up the result set in the format required by JDBC
            fieldNames = new String[] {"TABLE_CAT",
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
                "IS_NULLABLE",
                "SCOPE_CATALOG",    //@G4A
                "SCOPE_SCHEMA",     //@G4A
                "SCOPE_TABLE",      //@G4A
                "SOURCE_DATA_TYPE"  //@G4A
            };

            sqlData = new SQLData[] { new SQLVarchar (128, settings_), // catalog
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
                new SQLVarchar ((connection_.getVRM() >= JDUtilities.vrm550) ? 2000 : 254, settings_),  // column def
                new SQLInteger (),  // sql data type
                new SQLInteger (),  // datetime sub
                new SQLInteger (),  // octet length
                new SQLInteger (),  // ordinal
                new SQLVarchar (254, settings_),  // is nullable
                new SQLVarchar (128, settings_),  // scope catalog       //@G4A
                new SQLVarchar (128, settings_),  // scope schema        //@G4A
                new SQLVarchar (128, settings_),  // scope table         //@G4A
                new SQLSmallint (), // source data type    //@G4A
            };

            fieldNullables = new int[] {columnNullable, // catalog
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
                columnNullable, // scope catalog    //@G4A
                columnNullable, // scope schema     //@G4A
                columnNullable, // scope table      //@G4A
                columnNullable, // source data type //@G4A
            };
        }

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null;  // Creates a set of rows that
        // are readable one at a time
        try
        {


            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException
            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // table is not null and is empty string
                ((tablePattern != null) && (tablePattern.length()==0))  ||

                // columnPattern is not null and is empty string
                ((columnPattern != null) && (columnPattern.length()==0)))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }


            //--------------------------------------------------
            // Set the parameters for the request
            //--------------------------------------------------


            else
            {  // parameter values are valid, continue to build request
                // Create a request
                //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {
                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_FIELD_INFO,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +

                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

                    // Set the Library Name and Library Name Search Pattern parameters
                    // If null, do not set parameter.  The system default value of
                    // *USRLIBL is used
                    if (schemaPattern != null)
                    {
                        JDSearchPattern schema = new JDSearchPattern(schemaPattern);
                        request.setLibraryName (normalize(schema.getPatternString()), connection_.converter_);  // @E4C @P0C
                        request.setLibraryNameSearchPatternIndicator(schema.getIndicator());
                    }



                    // Set the Table Name and Table Name Search Pattern parameters
                    // If null, do not set parameter.  The system default value of
                    // *ALL is used.
                    if (tablePattern!=null)
                    {
                        JDSearchPattern table = new JDSearchPattern(tablePattern);
                        request.setFileName (normalize(table.getPatternString()), connection_.converter_);      // @E4C @P0C
                        request.setFileNameSearchPatternIndicator(table.getIndicator());
                    }


                    // Set the Field Name and Field Name Search Pattern parameters
                    // If null, do not set parameter.  The system default value of
                    // *ALL is used.
                    if (columnPattern!=null)
                    {
                        JDSearchPattern field = new JDSearchPattern(columnPattern);
                        request.setFieldName (field.getPatternString(), connection_.converter_); //@P0C
                        request.setFieldNameSearchPatternIndicator(field.getIndicator());
                    }


                    // Set the short / long file and field name indicator
                    request.setFileShortOrLongNameIndicator(0xF0); // Long

                    // Set the Field Information to Return Bitmap
                    // Return everything but the reserved fields
                    if(connection_.getVRM() >= JDUtilities.vrm550)  //@550 column default value support
                        request.setFieldReturnInfoBitmap(0xEFF70000);   //@550 request column default, 16th bit
                    else                                                //@550
                        request.setFieldReturnInfoBitmap(0xEFF60000);   // @E3C   //@KKB changed from EFF20000 inorder to request CCSID


                    // Set the Field Information Order By Indicator parameter
                    // Order by: Schema and File and Ordinal Position.
                    request.setFieldInformationOrderByIndicator (2); // @E5C


                    //-------------------------------------------------------
                    // Send the request and cache all results from the system
                    //-------------------------------------------------------
                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }

                    // Get the data format and result data
                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();

                    // Put the data format into a row format object
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

                    // Put the result data into a row cache
                    JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache(row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));

                    // Create the mapped row format that is returned in the
                    // result set.
                    // This does not actual move the data, it just sets up
                    // the mapping.
                    JDFieldMap[] maps = null;   //@F2C
                    if (!isJDBC3)                  //@F2A
                        maps = new JDFieldMap[18];
                    else
                        maps = new JDFieldMap[22]; //@G4A

                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[1] = new JDSimpleFieldMap (1); // library
                    maps[2] = new JDSimpleFieldMap (3); // table
                    maps[3] = new JDSimpleFieldMap (4); // column
                    maps[4] = new JDDataTypeFieldMap (6, 7, 10, 11, 12, connection_.getVRM(), connection_.getProperties());    // @M0C  // Data type    //@KKB include ccsid
                    maps[5] = new JDLocalNameFieldMap (6, 7, 10, 11, 12, connection_.getVRM(), connection_.getProperties());   // @M0C  // Type name    //@KKB include ccsid
                    maps[6] = new JDPrecisionFieldMap (6, 7, 10, 11, 12, connection_.getVRM(), connection_.getProperties());   // @M0C  // column size (length) //@KKB include ccsid
                    maps[7] = new JDHardcodedFieldMap(new Integer(0)); // Buffer - not used
                    maps[8] = new JDScaleFieldMap (6, 7, 10, 11, 12, connection_.getVRM(), connection_.getProperties());       // @M0C  // decimal digits (scale)  //@KKB include ccsid
                    maps[9] = new JDSimpleFieldMap (9); // radix
                    maps[10] = new JDNullableIntegerFieldMap(8); // is null capable?

                    if (connection_.getProperties().equals (JDProperties.REMARKS, JDProperties.REMARKS_SQL))
                        maps[11] = new JDSimpleFieldMap (2);  // return remarks
                    else
                        maps[11] = new JDSimpleFieldMap (5);  // return text

                    // Always return null if V5R4 or earlier
                    if(connection_.getVRM() <= JDUtilities.vrm540)   //@550 column default value support
                        maps[12] = new JDHardcodedFieldMap (new SQLVarchar(0, settings_), true, false); // column def
                    else    //@550 return what we are returned
                        maps[12] = new JDSimpleFieldMap(14);

                    // Per JDBC api - not used - hardcode to 0
                    maps[13] = new JDHardcodedFieldMap (new Integer (0)); // SQL data type

                    // Per JDBC api - not used - hardcode to 0
                    maps[14] = new JDHardcodedFieldMap (new Integer (0)); // SQL datetime

                    maps[15] = new JDCharOctetLengthFieldMap(6, 7, 10, 11, 12, connection_.getVRM(), connection_.getProperties()); // octet // @M0C   //@KKB include ccsid

                    // If the server functional level is 7 or greater, then ordinal        @E3A
                    // position is supported.  Otherwise, just hardcode to -1.             @E3A
                    if (connection_.getServerFunctionalLevel() >= 7)                    // @E3A
                        maps[16] = new JDSimpleFieldMap(13);                            // @E3A //@KKB changed from 12 since requesting ccsid
                    else                                                                // @E3A
                        maps[16] = new JDHardcodedFieldMap(new Integer(-1));            

                    maps[17] = new JDNullableStringFieldMap(8);  // is Nullable

                    //@G4A The below fields will all return null.  They are not supported
                    //@G4A by our database. 
                    if (isJDBC3)         //@F2A
                    {
                        maps[18] = new JDHardcodedFieldMap ("", true, false);  // scope catalog    //@G4A
                        maps[19] = new JDHardcodedFieldMap ("", true, false);  // scope schema     //@G4A
                        maps[20] = new JDHardcodedFieldMap ("", true, false);  // scope table      //@G4A
                        maps[21] = new JDHardcodedFieldMap (new Short((short) 0)); // source data type //@G4A
                    }

                    // Create the mapped row cache that is returned in the
                    // result set
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }  // end of else blank

        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
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
    //   The system returns the following:
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
    //   Since the system groups together
    //   some of the values, all of the
    //   possible JDBC values can not be returned.
    //
    //   For Update Rule, the only values
    //   supported by the system are
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
    //    if 0 from system = importedKeyCascade
    //    if 1 from system = importedKeyRestrict
    //    if 2 from system = importedKeySetNull
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
        try
        {
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
                (foreignTable==null)  ||  (foreignTable.length()==0 ))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }

            else
            { // parameter values are valid, build request & send
              // Create a request
              //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {
                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_FOREIGN_KEY_INFO ,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

                    // Set the primary key file library name
                    if (primarySchema == null)
                    {   // use default library or qgpl
                        request.setPrimaryKeyFileLibraryName(normalize(connection_.getDefaultSchema()), connection_.converter_); // @E4C @P0C
                    }
                    else request.setPrimaryKeyFileLibraryName(normalize(primarySchema), connection_.converter_);              // @E4C @P0C

                    // Set the foreign key file library name
                    if (foreignSchema == null)
                    {   // use default library or qgpl
                        request.setForeignKeyFileLibraryName(normalize(connection_.getDefaultSchema()), connection_.converter_);    // @E4C @P0C
                    }
                    else request.setForeignKeyFileLibraryName(normalize(foreignSchema), connection_.converter_);                  // @E4C @P0C


                    // Set the primary key table name
                    request.setPrimaryKeyFileName(normalize(primaryTable), connection_.converter_);         // @E4C @P0C

                    // Set the foreign key table name
                    request.setForeignKeyFileName(normalize(foreignTable), connection_.converter_);         // @E4C @P0C

                    // Set the Foreign key Information to Return Bitmap
                    request.setForeignKeyReturnInfoBitmap(0xBBE00000);

                    //Get the long file name
                    request.setFileShortOrLongNameIndicator (0xF0); // @PDA Long table names.  

                    //--------------------------------------------------------
                    //  Send the request and cache all results from the system
                    //--------------------------------------------------------

                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }

                    // Get the data format and result data
                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();
                    if (resultData != null)
                    {

                        // Put the data format into a row format object
                        JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

                        // Put the result data into a row cache
                        JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache(row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));

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
                        maps[11] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true, false);
                        maps[12] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true, false);
                        maps[13] = new JDHardcodedFieldMap(new Short ((short) importedKeyNotDeferrable));

                        // Create the mapped row cache that is returned in the
                        // result set
                        JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                        rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                    }
                    else
                        rowCache = new JDSimpleRowCache(formatRow);

                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }


        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(), "CrossReference");

    }  // End of getCrossReference



    /**
    Returns the major version number of the database.
    
    @return     The major version number.
    @since Modification 5
    **/
    public int getDatabaseMajorVersion ()
    {
        return 5;
    }


    /**
    Returns the minor version number of the database.
    
    @return     The minor version number.
    @since Modification 5
    **/
    public int getDatabaseMinorVersion ()
    {
        return 0;
    }


    /**
  Returns the name of this database product.
  
  @return     The database product name.
  
  @exception  SQLException    This exception is never thrown.
  **/
    public String getDatabaseProductName ()
    throws SQLException
    {
        return AS400JDBCDriver.DATABASE_PRODUCT_NAME_; // @D2C
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
        try
        {
            int vrm = as400_.getVRM ();
            v = (vrm & 0xffff0000) >>> 16;                   // @D1C
            r = (vrm & 0x0000ff00) >>>  8;                   // @D1C
            m = (vrm & 0x000000ff);                          // @D1C
        }
        catch (Exception e)
        {
            JDError.throwSQLException (this, JDError.EXC_CONNECTION_NONE);
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
        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string


                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // table is null or empty string
                (table==null)  ||  (table.length()==0 ))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }

            else
            { // parameter values are valid, build request & send
              // Create a request
              //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {
                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_FOREIGN_KEY_INFO ,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);


                    // Set the primary key file library name
                    if (schema == null)
                    {   // use default library or qgpl
                        request.setPrimaryKeyFileLibraryName(normalize(connection_.getDefaultSchema()), connection_.converter_);    // @E4C @P0C
                    }
                    else request.setPrimaryKeyFileLibraryName(normalize(schema), connection_.converter_);                         // @E4C @P0C



                    // Set the primary key table name
                    request.setPrimaryKeyFileName(normalize(table), connection_.converter_);                    // @E4C @P0C


                    // Set the Foreign key Information to Return Bitmap
                    //@F4 As of base v4r4, host server can return primary and foreign key names.
                    //@F4 Even this has nothing to do with lobs, borrow the constant as
                    //@F4 it checks for v4r4.
                    if (connection_.getVRM() >= JDUtilities.vrm440)  //@F4A
                        request.setForeignKeyReturnInfoBitmap(0xBBF80000);           //@F4A
                    else                                                             //@F4A
                        request.setForeignKeyReturnInfoBitmap(0xBBE00000);

                    //Get the long file name
                    request.setFileShortOrLongNameIndicator (0xF0); // @KBA Long table names.        


                    //--------------------------------------------------------
                    //  Send the request and cache all results from the system
                    //--------------------------------------------------------

                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }

                    // Get the data format and result data
                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();

                    if (resultData != null)
                    {
                        JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                        JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache (row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));

                        JDFieldMap[] maps = new JDFieldMap[14];
                        maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                        maps[1] = new JDSimpleDelimitedFieldMap (1); // pk schema //@PDC code to remove quotes
                        maps[2] = new JDSimpleFieldMap (2); // pk table
                        maps[3] = new JDSimpleFieldMap (3); // pk column
                        maps[4] = new JDHardcodedFieldMap (connection_.getCatalog ());
                        maps[5] = new JDSimpleDelimitedFieldMap (4); // fk schema //@PDC code to remove quotes
                        maps[6] = new JDSimpleFieldMap (5); // fk table
                        maps[7] = new JDSimpleFieldMap (6); // fk column
                        maps[8] = new JDSimpleFieldMap (7); // key seq
                        maps[9] = new JDSimpleFieldMap (8); // update rule
                        maps[10] = new JDSimpleFieldMap (9);    // delete rule
                        if (connection_.getVRM() >= JDUtilities.vrm440)  //@F4A
                        {
                            maps[11] = new JDSimpleDelimitedFieldMap (10);    //@F4A //@PDC code to remove quotes
                            maps[12] = new JDSimpleDelimitedFieldMap (11);    //@F4A //@PDC code to remove quotes
                        }
                        else
                        {                                         //@F4A
                            maps[11] = new JDHardcodedFieldMap (new SQLVarchar(0, settings_), true, false);
                            maps[12] = new JDHardcodedFieldMap (new SQLVarchar(0, settings_), true, false);
                        }
                        maps[13] = new JDHardcodedFieldMap (new Short ((short) importedKeyNotDeferrable));

                        JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                        rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                    }
                    else
                        rowCache = new JDSimpleRowCache(formatRow);
                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }  
        }
        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
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
        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // table is null or empty string
                (table==null)  ||  (table.length()==0 ))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }

            else
            { // parameter values are valid, build request & send
              // Create a request
              //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {
                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_FOREIGN_KEY_INFO ,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);




                    // Set the foreign key file library name
                    if (schema == null)
                    {   // use default library or qgpl
                        request.setForeignKeyFileLibraryName(normalize(connection_.getDefaultSchema()), connection_.converter_);    // @E4C
                    }
                    else request.setForeignKeyFileLibraryName(normalize(schema), connection_.converter_); // @E4C


                    // Set the foreign key table name
                    request.setForeignKeyFileName(normalize(table), connection_.converter_);    // @E4C

                    // Set the Foreign key Information to Return Bitmap
                    //@F4 As of base v4r4, host server can return primary and foreign key names.
                    //@F4 Even this has nothing to do with lobs, borrow the constant as
                    //@F4 it checks for v4r4.
                    if (connection_.getVRM() >= JDUtilities.vrm440)  //@F4A
                        request.setForeignKeyReturnInfoBitmap(0xBBF80000);           //@F4A
                    else                                                             //@F4A
                        request.setForeignKeyReturnInfoBitmap(0xBBE00000);

                    // This is not documented in the LIPI, but it happens to work!           @E2A
                    request.setFileShortOrLongNameIndicator(0xF0);                        // @E2A

                    //--------------------------------------------------------
                    //  Send the request and cache all results from the system
                    //--------------------------------------------------------

                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }

                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();
                    if (resultData != null)
                    {
                        JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                        JDRowCache serverRowCache =  new JDSimpleRowCache(new JDServerRowCache(row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));
                        JDFieldMap[] maps = new JDFieldMap[14];
                        maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                        maps[1] = new JDSimpleDelimitedFieldMap (1); // pk schema //@PDC code to remove quotes
                        maps[2] = new JDSimpleFieldMap (2); // pk table
                        maps[3] = new JDSimpleFieldMap (3); // pk column
                        maps[4] = new JDHardcodedFieldMap (connection_.getCatalog ());
                        maps[5] = new JDSimpleDelimitedFieldMap (4); // fk schema //@PDC code to remove quotes
                        maps[6] = new JDSimpleFieldMap (5); // fk table
                        maps[7] = new JDSimpleFieldMap (6); // fk column
                        maps[8] = new JDSimpleFieldMap (7); // key seq
                        maps[9] = new JDSimpleFieldMap (8); // update rule
                        maps[10] = new JDSimpleFieldMap (9);    // delete rule
                        if (connection_.getVRM() >= JDUtilities.vrm440)  //@F4A
                        {
                            maps[11] = new JDSimpleDelimitedFieldMap (10); //@F4A  //@PDC code to remove quotes
                            maps[12] = new JDSimpleDelimitedFieldMap (11); //@F4A  //@PDC code to remove quotes
                        }
                        else
                        {                                      //@F4A
                            maps[11] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true, false);
                            maps[12] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true, false);
                        }
                        maps[13] = new JDHardcodedFieldMap(new Short ((short) importedKeyNotDeferrable));

                        JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                        rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                    }
                    else
                        rowCache = new JDSimpleRowCache(formatRow);

                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
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
        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // table is null or empty string
                (table==null)  ||  (table.length()==0 ))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }

            else
            { // parameter values are valid, build request & send
              // Create a request
              //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {
                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_INDEX_INFO ,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);
                    // Set the library name
                    if (schema == null)
                    {   // use default library or qgpl
                        request.setLibraryName(normalize(connection_.getDefaultSchema()), connection_.converter_);      // @E4C
                    }
                    else request.setLibraryName(normalize(schema), connection_.converter_);                           // @E4C


                    // Set the table name
                    request.setFileName(normalize(table), connection_.converter_);      // @E4C

                    // Set the long file name indicator
                    request.setFileShortOrLongNameIndicator (0xF0); // Long table names

                    // Set the index unique rule
                    if (unique)
                    {  // true - return indices for unique values
                        request.setIndexUniqueRule(0x01);
                    }
                    else
                    {
                        request.setIndexUniqueRule(0x04);
                    }

                    // Set the Index Information to Return Bitmap
                    request.setIndexReturnInfoBitmap(0xBDC00000);



                    //--------------------------------------------------------
                    //  Send the request and cache all results from the system
                    //--------------------------------------------------------

                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }

                    // Get the data format and result data
                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();
                    if (resultData != null)
                    {

                        // Put the data format into a row object
                        JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

                        // Put the result data into a row cache
                        JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache(row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));

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
                        maps[12] = new JDHardcodedFieldMap(new SQLVarchar(0, settings_), true, false);

                        // Create the mapped row cache that is returned in the
                        // result set
                        JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                        rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                    }
                    else
                        rowCache = new JDSimpleRowCache(formatRow);

                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "IndexInfo");

    } // End of getIndexInfo



    //@G4A
    /**
    Returns the JDBC major version number.
    
    @return     The JDBC major version number.
    
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public int getJDBCMajorVersion ()
    throws SQLException
    {
        return JDBC_MAJOR_VERSION;
    }



    //@G4A
    /**
    Returns the JDBC minor version number.
    
    @return     The JDBC minor version number.
    
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public int getJDBCMinorVersion ()
    throws SQLException
    {
        return JDBC_MINOR_VERSION;
    }




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
        if(connection_.getVRM() >= JDUtilities.vrm540)                              //@540 
            return 128;                                                             //@540 
        else
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
        if(connection_.getVRM() >= JDUtilities.vrm550)          //@550  max columns in group by support
            return 8000;                                        //@550
        else                                                    //@550
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
    
    @return     The maximum length.
    
    @exception  SQLException    This exception is never thrown.
    **/
    public int getMaxStatementLength ()
    throws SQLException
    {
        if(connection_.getVRM() >= JDUtilities.vrm540)      //@540 
            return 1048576;                                 //@540 Statement text is always sent in 2 byte Unicode, so the maximum statement length in characters will always be 1 MB
        else                                                //@540 
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
        return AS400JDBCConnection.MAX_STATEMENTS_; // @D3C
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
        if (connection_.getVRM() >= JDUtilities.vrm540)  // New in V5R4M0 @PDC
            return 1000;  //@pdc
        else
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
        // @J0A added try/catch because we are now sending the system VRM
        return JDEscapeClause.getNumericFunctions(connection_.getVRM());  // @J0M changed to send host version
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
        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // table is null or empty string
                (table==null)  ||  (table.length()==0 ))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }

            else
            { // parameter values are valid, build request & send
              // Create a request
              //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {
                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_PRIMARY_KEY_INFO ,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);


                    // Set the primary key file library name
                    if (schema == null)
                    {   // use default library or qgpl
                        request.setPrimaryKeyFileLibraryName(normalize(connection_.getDefaultSchema()), connection_.converter_);    // @E4C
                    }
                    else request.setPrimaryKeyFileLibraryName(normalize(schema), connection_.converter_);                         // @E4C


                    // Set the primary key table name
                    request.setPrimaryKeyFileName(normalize(table), connection_.converter_);        // @E4C

                    // Set the primary key Information to Return Bitmap
                    request.setPrimaryKeyReturnInfoBitmap(0xB8000000);

                    // This is not documented in the LIPI, but it happens to work!           @E2A
                    request.setFileShortOrLongNameIndicator(0xF0);                        // @E2A

                    //--------------------------------------------------------
                    //  Send the request and cache all results from the system
                    //--------------------------------------------------------

                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }

                    // Get the data format and result data
                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();
                    if (resultData != null)
                    {
                        JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                        JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache (row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));
                        // Create the mapped row format that is returned in the
                        // result set.
                        // This does not actual move the data, it just sets up
                        // the mapping.
                        boolean nullValue = true; // used when hardcoding null
                        JDFieldMap[] maps = new JDFieldMap[6];
                        maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                        maps[1] = new JDSimpleDelimitedFieldMap (1); // pk schema //@PDC code to remove quotes
                        maps[2] = new JDSimpleFieldMap (2); // pk table
                        maps[3] = new JDSimpleFieldMap (3); // pk column
                        maps[4] = new JDCharToShortFieldMap (4);    // key seq
                        maps[5] = new JDHardcodedFieldMap (new SQLVarchar (0, settings_), true, false);

                        // Create the mapped row cache that is returned in the
                        // result set
                        JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                        rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
                    }
                    else
                        rowCache = new JDSimpleRowCache(formatRow);

                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
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
        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // procedure is not null and is empty string
                ((procedurePattern != null)  &&  (procedurePattern.length()==0)) ||

                // column is not null and is empty string
                ((columnPattern != null)  &&  (columnPattern.length()==0)))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }

            else

            {  // Parameters are valid, build request and send
                StringBuffer selectStmt = new StringBuffer();
                selectStmt.append ("SELECT SPECIFIC_SCHEMA, SPECIFIC_NAME, PARAMETER_NAME, PARAMETER_MODE, ");
                selectStmt.append ("DATA_TYPE, NUMERIC_PRECISION, CHARACTER_MAXIMUM_LENGTH, NUMERIC_SCALE, ");
                selectStmt.append ("NUMERIC_PRECISION_RADIX, IS_NULLABLE, LONG_COMMENT ");
                selectStmt.append ("FROM QSYS2" + getCatalogSeparator() + "SYSPARMS "); // use . or /




                if (schemaPattern !=null)
                {
                    JDSearchPattern schema = new JDSearchPattern (schemaPattern);
                    String schemaWhereClause = schema.getSQLWhereClause("SPECIFIC_SCHEMA");
                    selectStmt.append("WHERE " + schemaWhereClause);
                }


                if (procedurePattern !=null)
                {
                    JDSearchPattern procedure = new JDSearchPattern (procedurePattern);
                    if (schemaPattern!=null)
                    { // Where clause already exists, add AND
                        selectStmt.append (" AND ");
                    }

                    else
                    {  // Where clause does not exist, add WHERE
                        selectStmt.append (" WHERE ");
                    }

                    String procedureWhereClause = procedure.getSQLWhereClause("SPECIFIC_NAME");
                    selectStmt.append(procedureWhereClause);
                }


                if (columnPattern!=null)
                {  // if null, no where clause is sent
                    JDSearchPattern column = new JDSearchPattern (columnPattern);
                    if ((schemaPattern!=null) || (procedurePattern!=null))
                    { // Where clause already exists, add AND
                        selectStmt.append (" AND ");
                    }
                    else
                    {  // Where clause does not exist, add WHERE
                        selectStmt.append (" WHERE ");
                    }

                    String columnWhereClause = column.getSQLWhereClause("PARAMETER_NAME");
                    selectStmt.append(columnWhereClause);

                }




                // Add order by
                selectStmt.append ("ORDER BY SPECIFIC_SCHEMA, SPECIFIC_NAME, ORDINAL_POSITION");        // Added ORDINAL_POSITION to fix JTOpen bug 3646, SYSPARMS table doesn't always have parameters in physical right order

                // Create statement object and do Execute Query
                AS400JDBCStatement statement_ = (AS400JDBCStatement)connection_.createStatement(); // caste needed
                AS400JDBCResultSet serverResultSet = (AS400JDBCResultSet) statement_.executeQuery (selectStmt.toString());

                JDRowCache serverRowCache = new JDSimpleRowCache(serverResultSet.getRowCache());
                statement_.close ();

                JDFieldMap[] maps = new JDFieldMap[13];

                maps[0] = new JDHardcodedFieldMap (connection_.getCatalog());
                maps[1] = new JDSimpleFieldMap (1); // schema
                maps[2] = new JDSimpleFieldMap (2); // procedure
                maps[3] = new JDHandleNullFieldMap (3, ""); // parameter name (col name)
                maps[4] = new JDParameterModeFieldMap(4); // Parameter mode (col type)

                maps[5] = new JDDataTypeFieldMap(5, 7, 6, 8, 0, connection_.getVRM(), connection_.getProperties()); // @M0C // data type - converts from string to short //@KKB  pass 0 for ccsid
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

        catch (SQLException e)
        {
            // System.out.println(e);
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
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
        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if ((!isCatalogValid(catalog)) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // procedure is not null and is empty string
                ((procedurePattern!=null)  &&  (procedurePattern.length()==0)))
            { // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }

            else
            {  // Parameters are valid, build request and send
                StringBuffer selectStmt = new StringBuffer();
                selectStmt.append ("SELECT SPECIFIC_SCHEMA, SPECIFIC_NAME, REMARKS, RESULTS ");
                selectStmt.append ("FROM QSYS2" + getCatalogSeparator() + "SYSPROCS "); // use . or /



                if (schemaPattern !=null)
                {
                    JDSearchPattern schema = new JDSearchPattern (schemaPattern);
                    String schemaWhereClause = schema.getSQLWhereClause("SPECIFIC_SCHEMA");
                    selectStmt.append("WHERE " + schemaWhereClause);
                }


                if (procedurePattern !=null)
                {
                    JDSearchPattern procedure = new JDSearchPattern (procedurePattern);
                    if (schemaPattern!=null)
                    { // Where clause already exists, add AND
                        selectStmt.append (" AND ");
                    }

                    else
                    {  // Where clause does not exist, add WHERE
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

                JDRowCache serverRowCache = new JDSimpleRowCache(serverResultSet.getRowCache());
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

        catch (SQLException e)
        {
            // System.out.println(e);
            // e.printStackTrace();  // method on throwable object
            // @B1D JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
            throw e; // @B1A
        }

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "Procedures");

    }



    /**
    Returns the DB2 for i5/OS SQL term for "procedure".
    
    @return     The term for "procedure".
    
    @exception  SQLException    This exception is never thrown.
    **/
    public String getProcedureTerm ()
    throws SQLException
    {
        return AS400JDBCDriver.getResource ("PROCEDURE_TERM");
    }



    //@G4A
    /**
    Retrieves the default holdability of this ResultSet object.  Holdability is 
    whether ResultSet objects are kept open when the statement is committed.
    
    @return     Always ResultSet.HOLD_CURSORS_OVER_COMMIT.
    
    @exception  SQLException    This exception is never thrown.
    
    @since Modification 5
    **/
    public int getResultSetHoldability ()
    throws SQLException
    {
        return AS400JDBCResultSet.HOLD_CURSORS_OVER_COMMIT;  
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
        return JDUtilities.getLibraries(this, connection_, settings_, false);  //@DELIMa
    }



    /**
    Returns the DB2 for i5/OS SQL term for "schema".
    
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
        return "AFTER,ALIAS,ALLOW,APPLICATION,ASSOCIATE,ASUTIME,AUDIT," +                 // @J2M
               "AUX,AUXILIARY,BEFORE,BINARY," +                                           // @J2A
               "BUFFERPOOL,CACHE,CALL,CALLED,CAPTURE,CARDINALITY,CCSID,CLUSTER," +        // @J2A
               "COLLECTION,COLLID,COMMENT,CONCAT,CONDITION,CONTAINS,COUNT_BIG," +         // @J2A
               "CURRENT_LC_CTYPE," +                                                      // @J2A
               "CURRENT_PATH,CURRENT_SERVER,CURRENT_TIMEZONE,CYCLE,DATA," +               // @J2A
               "DATABASE,DAYS," +                                                         // @J2A
               "DB2GENERAL,DB2GENRL,DB2SQL,DBINFO,DEFAULTS,DEFINITION," +                 // @J2A
               "DETERMINISTIC," +                                                         // @J2A
               "DISALLOW,DO,DSNHATTR,DSSIZE,DYNAMIC,EACH,EDITPROC,ELSEIF," +              // @J2A
               "ENCODING,END-EXEC1," +                                                    // @J2A
               "ERASE,EXCLUDING,EXIT,FENCED,FIELDPROC,FILE,FINAL,FREE,FUNCTION," +        // @J2A
               "GENERAL," +                                                               // @J2A
               "GENERATED,GRAPHIC,HANDLER,HOLD,HOURS,IF,INCLUDING,INCREMENT," +           // @J2A
               "INDEX," +                                                                 // @J2A
               "INHERIT,INOUT,INTEGRITY,ISOBID,ITERATE,JAR,JAVA,LABEL,LC_CTYPE," +        // @J2A
               "LEAVE," +                                                                 // @J2A
               "LINKTYPE,LOCALE,LOCATOR,LOCATORS,LOCK,LOCKMAX,LOCKSIZE,LONG,LOOP," +      // @J2A
               "MAXVALUE,MICROSECOND,MICROSECONDS,MINUTES,MINVALUE,MODE,MODIFIES," +      // @J2A
               "MONTHS," +                                                                // @J2A
               "NEW,NEW_TABLE,NOCACHE,NOCYCLE,NODENAME,NODENUMBER,NOMAXVALUE," +          // @J2A
               "NOMINVALUE," +                                                            // @J2A
               "NOORDER,NULLS,NUMPARTS,OBID,OLD,OLD_TABLE,OPTIMIZATION,OPTIMIZE," +       // @J2A
               "OUT,OVERRIDING,PACKAGE,PARAMETER,PART,PARTITION,PATH,PIECESIZE," +        // @J2A
               "PLAN," +                                                                  // @J2A
               "PRIQTY,PROGRAM,PSID,QUERYNO,READS,RECOVERY,REFERENCING,RELEASE," +        // @J2A
               "RENAME,REPEAT,RESET,RESIGNAL,RESTART,RESULT,RESULT_SET_LOCATOR," +        // @J2A
               "RETURN," +                                                                // @J2A
               "RETURNS,ROUTINE,ROW,RRN,RUN,SAVEPOINT,SCRATCHPAD,SECONDS,SECQTY," +       // @J2A
               "SECURITY,SENSITIVE,SIGNAL,SIMPLE,SOURCE,SPECIFIC,SQLID,STANDARD," +       // @J2A
               "START,STATIC,STAY,STOGROUP,STORES,STYLE,SUBPAGES,SYNONYM,SYSFUN," +       // @J2A
               "SYSIBM," +                                                                // @J2A
               "SYSPROC,SYSTEM,TABLESPACE,TRIGGER,TYPE,UNDO,UNTIL,VALIDPROC," +           // @J2A
               "VARIABLE," +                                                              // @J2A
               "VARIANT,VCAT,VOLUMES,WHILE,WLM,YEARS";                                    // @J2A
    }



    //@G4A
    /**
    Indicates whether the SQLSTATEs returned by SQLException.getSQLState is X/Open SQL CLI or 
    SQL99.
    
    @return     Always sqlStateSQL99.
    
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public int getSQLStateType ()
    throws SQLException
    {
        return AS400JDBCDatabaseMetaData.sqlStateSQL99;
    }



    /**
    Returns the list of supported string functions.
    
    @return     The list of supported string functions, separated by commas.
    
    @exception  SQLException    This exception is never thrown.
    **/
    public String getStringFunctions ()
    throws SQLException
    {
        // @J0A added try/catch because we are now sending the system VMR
        return JDEscapeClause.getStringFunctions(connection_.getVRM()); // @J0M changed to send host version
    }



    //@G4A
    /**
    Returns a ResultSet containing descriptions of the table hierarchies 
    in a schema.  
    
    This method only applies to the attributes of a 
    structured type.  Distinct types are stored in the datatypes 
    catalog, not the attributes catalog.  Since DB2 for i5/OS does not support 
    structured types at this time, an empty ResultSet will always be returned
    for calls to this method.
    
    @param  catalog         The catalog name. 
    @param  schemaPattern   The schema name pattern.  
    @param  typeNamePattern The type name pattern. 
    
    @return      The empty ResultSet
    
    @exception   SQLException  This exception is never thrown.
    @since Modification 5
    **/
    public ResultSet getSuperTables (String catalog, String schemaPattern, String typeNamePattern)
    throws SQLException
    {
        // We return an empty result set because this is not supported by our driver
        Statement statement = connection_.createStatement();
        return statement.executeQuery("SELECT VARCHAR('1', 128) AS TYPE_CAT, " +
                                      "VARCHAR('2', 128) AS TYPE_SCHEM, " +
                                      "VARCHAR('3', 128) AS TYPE_NAME, " +
                                      "VARCHAR('4', 128) AS SUPERTYPE_NAME " +
                                      "FROM QSYS2" + getCatalogSeparator() + 
                                      "SYSTYPES WHERE 1 = 2 FOR FETCH ONLY ");
    }



    //@G4A
    /**
    Returns a ResultSet containing descriptions of user-defined type hierarchies 
    in a schema. 
    
    This method only applies to the attributes of a 
    structured type.  Distinct types are stored in the datatypes 
    catalog, not the attributes catalog. Since DB2 for i5/OS does not support 
    structured types at this time, an empty ResultSet will always be returned
    for calls to this method.
    
    @param  catalog         The catalog name. 
    @param  schemaPattern   The schema name pattern.  
    @param  typeNamePattern The type name pattern. 
    
    @return      The empty result set
    
    @exception   SQLException  This exception is never thrown.
    @since Modification 5
    **/
    public ResultSet getSuperTypes (String catalog, String schemaPattern, String typeNamePattern)
    throws SQLException
    {
        // We return an empty result set because this is not supported by our driver
        Statement statement = connection_.createStatement();
        return statement.executeQuery("SELECT VARCHAR('1', 128) AS TYPE_CAT, " +
                                      "VARCHAR('2', 128) AS TYPE_SCHEM, " +
                                      "VARCHAR('3', 128) AS TYPE_NAME, " +
                                      "VARCHAR('4', 128) AS SUPERTYPE_CAT, " + 
                                      "VARCHAR('5', 128) AS SUPERTYPE_SCHEM, " +
                                      "VARCHAR('6', 128) AS SUPERTYPE_NAME " +
                                      "FROM QSYS2" + getCatalogSeparator() + 
                                      "SYSTYPES WHERE 1 = 2 FOR FETCH ONLY ");
    }



    /**
    Returns the list of supported system functions.
    
    @return     The list of supported system functions, separated by commas.
    
    @exception  SQLException    This exception is never thrown.
    **/
    public String getSystemFunctions ()
    throws SQLException
    {
        // @J0A added try/catch because we are now sending the system VMR
        return JDEscapeClause.getSystemFunctions(connection_.getVRM()); // @J0M changed to send host version
    }



    /**
    Returns the description of the access rights for each table
    available in a catalog.  Note that a table privilege applies
    to one or more columns in a table.
    
    @param  catalog             The catalog name. If null is specified, this parameter
                                is ignored.  If empty string is specified,
                                an empty result set is returned.
    @param  schemaPattern       The schema name pattern. If null is specified,
                                no value is sent to the system and the
                                system default of *USRLIBL is used.
                                If empty string is specified, an empty
                                result set is returned.
    @param  tablePattern        The table name. If null is specified,
                                no value is sent to the system and the system
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
        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException
            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // table is not null and is empty string
                ((tablePattern != null) && (tablePattern.length()==0)))
            {   // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }


            else
            { // parameter values are valid, build request & send
              // Create a request
              //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {
                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_FILE_INFO,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

                    // Set the library name and search pattern indicator
                    // If null, do not set parameter. The system default
                    // value of *USRLIBL is used.
                    if (schemaPattern!=null)
                    {
                        JDSearchPattern schema = new JDSearchPattern(schemaPattern);
                        request.setLibraryName(normalize(schema.getPatternString()), connection_.converter_);       // @E4C
                        request.setLibraryNameSearchPatternIndicator(schema.getIndicator());
                    }



                    // Set the table name and search pattern indicator
                    // If null, do not set parameter. The system default
                    // value of *ALL is used.
                    if (tablePattern!=null)
                    {
                        JDSearchPattern table = new JDSearchPattern(tablePattern);
                        request.setFileName(normalize(table.getPatternString()),connection_.converter_);    // @E4C
                        request.setFileNameSearchPatternIndicator(table.getIndicator());
                    }

                    request.setFileShortOrLongNameIndicator (0xF0); // Long table names


                    request.setFileReturnInfoBitmap (0xA1000000); // Return bitmap

                    // Order the results by table type, table schema, table name
                    request.setFileInfoOrderByIndicator (2);


                    //--------------------------------------------------------
                    //  Send the request and cache all results from the system
                    //--------------------------------------------------------

                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }


                    // Get the data format and result data
                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();

                    // Put the data format into a row object
                    JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);

                    // Put the result data into a row cache
                    JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache (row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));

                    // Create the mapped row format that is returned in the
                    // result set.
                    // This does not actual move the data, it just sets up
                    // the mapping.
                    JDFieldMap[] maps = new JDFieldMap[7];
                    maps[0] = new JDHardcodedFieldMap (connection_.getCatalog ());
                    maps[1] = new JDSimpleFieldMap (1); // library
                    maps[2] = new JDSimpleFieldMap (2); // table
                    maps[3] = new JDHardcodedFieldMap ("", true, false); // grantor
                    maps[4] = new JDHardcodedFieldMap (getUserName ()); // grantee - return userid
                    maps[5] = new JDPrivilegeFieldMap (3); // privilege
                    maps[6] = new JDHardcodedFieldMap ("", true, false); // is_grantable

                    // Create the mapped row cache that is returned in the
                    // result set
                    JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                    rowCache = new JDMappedRowCache (mappedRow, serverRowCache);

                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }

        // Return the results
        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "TablePrivileges");      //@G4C
    }



    /**
    Returns the description of the tables available in a catalog.
    
    @param  catalog        The catalog name. If null is specified, this parameter
                           is ignored.  If empty string is specified,
                           an empty result set is returned.
    @param  schemaPattern  The schema name pattern.  If null is specified,
                           no value is sent to the system and the system
                           default of *USRLIBL is used.  If empty string
                           is specified, an empty result set is returned.
    @param  tablePattern   The table name pattern. If null is specified,
                           no value is sent to the system and the system
                           default of *ALL is used.  If empty string
                           is specified, an empty result set is returned.
    @param  tableTypes     The list of table types to include, or null to
                           include all table types. Valid types are:
                           TABLE, VIEW, SYSTEM TABLE, MATERIALIZED QUERY TABLE, and ALIAS.
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

        boolean isJDBC3 = JDUtilities.JDBCLevel_ >= 30; //@F2A @j4a

        String[] fieldNames = null;               //@F2C
        SQLData[] sqlData = null;                 //@F2C
        int[] fieldNullables = null;              //@F2C
        if (!isJDBC3)                             //@F2A
        {
            fieldNames = new String[] { "TABLE_CAT",
                "TABLE_SCHEM",
                "TABLE_NAME",
                "TABLE_TYPE",
                "REMARKS"};

            sqlData = new SQLData[] { new SQLVarchar (128, settings_),
                new SQLVarchar (128, settings_),
                new SQLVarchar (128, settings_),
                new SQLVarchar (128, settings_),
                new SQLVarchar (254, settings_)}; 


            fieldNullables = new int[] {columnNullable,  // Table catalog
                columnNullable,   // Table schema
                columnNoNulls,    // Table name
                columnNoNulls,    // Table type
                columnNoNulls};   // Remarks 
        }
        else
        {
            fieldNames = new String[] { "TABLE_CAT",
                "TABLE_SCHEM",
                "TABLE_NAME",
                "TABLE_TYPE",
                "REMARKS",
                "TYPE_CAT",                       //@G4A
                "TYPE_SCHEM",                     //@G4A
                "TYPE_NAME",                      //@G4A
                "SELF_REFERENCING_COL_NAME",      //@G4A
                "REF_GENERATION"};                //@G4A

            sqlData = new SQLData[] { new SQLVarchar (128, settings_),
                new SQLVarchar (128, settings_),
                new SQLVarchar (128, settings_),
                new SQLVarchar (128, settings_),
                new SQLVarchar (254, settings_),
                new SQLVarchar (128, settings_),  //@G4A
                new SQLVarchar (128, settings_),  //@G4A
                new SQLVarchar (128, settings_),  //@G4A
                new SQLVarchar (128, settings_),  //@G4A
                new SQLVarchar (128, settings_)}; //@G4A


            fieldNullables = new int[] {columnNullable,  // Table catalog
                columnNullable,   // Table schema
                columnNoNulls,    // Table name
                columnNoNulls,    // Table type
                columnNoNulls,    // Remarks
                columnNullable,   // Types catalog                //@G4A
                columnNullable,   // Types schema                 //@G4A
                columnNullable,   // Type name                    //@G4A
                columnNullable,   // Self referencing column name //@G4A
                columnNullable};  // Reference generation         //@G4A
        }

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        JDRowCache rowCache = null; // Creates a set of rows that
        // are readable one at a time
        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException
            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schemaPattern != null) && (schemaPattern.length()==0)) ||

                // table is not null and is empty string
                ((tablePattern != null) && (tablePattern.length()==0)))
            {   // Return empty result set
                rowCache = new JDSimpleRowCache(formatRow);
            }


            else
            {  // schema, table, and catalog are valid, create and send request

                //-------------------------------------------------------
                // Set the file attribute parm of the Retrieve File
                // Information (ROI) function based on the types array
                // provided. ROI uses the following values:
                //  '0001'x = all files
                //  '0002'x = physical files and alias files                    //@K3C  changed to match host server LIPI
                //  '0003'x = logical files and alias files                     //@K3C
                //  '0004'x = tables and alias files                            //@K3C
                //  '0005'x = views and alias files                             //@K3C
                //  '0006'x = system tables
                //  '0007'x = tables and views and alias files                  //@K3C
                //  '0008'x = tables and system tables and alias files          //@K3C
                //  '0009'x = views and system tables and alias files           //@K3C
                //  '000A'x = alias files                                       //@K1A
                //  '000B'x = tables and materialized query tables              //@K1A
                //  '000C'x = views and materialized query tables               //@K1A
                //  '000D'x = system tables and materialized query tables       //@K1A
                //  '000E'x = tables, views, and materialized query tables      //@K1A
                //  '000F'x = tables, system tables, and materialized query tables  //@K1A
                //  '0010'x = views, system tables, and materizlized query tables   //@K1A
                //  '0011'x = materialized query tables                             //@K1A
                //
                //   Options '000B'x - '0011'x are for V5R3 or higher systems
                //
                // If null is specified, file attributes is set to 1.
                //
                // If none of the above values are specified, file
                // attribute is set to -1 and an empty result set will
                // be created. No request is sent to the system.
                //--------------------------------------------------------
                int fileAttribute;
                boolean needToRemoveAliases = true;         //@K3A
                if (tableTypes != null)
                {
                    boolean typeTable       = false;  // false = don't include table type
                    boolean typeView        = false;
                    boolean typeSystemTable = false;
                    boolean typeMQTable     = false;  //@K1A
                    boolean typeAlias       = false;  //@K3A

                    // Walk thru table types to determine which ones we need to include
                    for (int i = 0; i < tableTypes.length; ++i)
                    {
                        if (tableTypes[i].equalsIgnoreCase ("TABLE"))
                            typeTable = true; // Include tables
                        else if (tableTypes[i].equalsIgnoreCase ("VIEW"))
                            typeView = true;  // Include views
                        else if (tableTypes[i].equalsIgnoreCase ("SYSTEM TABLE"))
                            typeSystemTable = true;  // Include system tables
                        else if (tableTypes[i].equalsIgnoreCase ("MATERIALIZED QUERY TABLE") && connection_.getVRM() >= JDUtilities.vrm530)   //@K1A
                            typeMQTable = true;                                                 //@K1A
                        else if(tableTypes[i].equalsIgnoreCase("ALIAS"))    //@K3A
                            typeAlias = true;                               //@K3A
                    }   // end of for loop

                    if(typeAlias)                                           //@K3A
                        needToRemoveAliases = false;                              //@K3A

                    if (typeTable)
                    {
                        if (typeView)
                        {
                            if (typeSystemTable)
                                fileAttribute = 1;  // All
                            else if(typeMQTable)                                    //@K1A
                            {
                                fileAttribute = 14; //tables, views, and MQT's      //@K1A
                                needToRemoveAliases = false;      //@K3A no aliases are returned
                            }
                            else
                                fileAttribute = 7;  // Tables and views
                        }
                        else if(typeSystemTable)                                    //@K1A   Not Views
                        {                                                           //@K1A
                            if(typeMQTable)                                         //@K1A
                            {
                                fileAttribute = 15;                                 //@K1A
                                needToRemoveAliases = false;                              //@K3A no aliases are returned
                            }
                            else                                                    //@K1A  
                                fileAttribute = 8;  // Tables and system tables
                        }                                                           //@K1A
                        else if(typeMQTable)                                        //@K1A  Not Views and not system tables
                        {                                                           //@K1A
                            fileAttribute = 11;                                     //@K1A
                            needToRemoveAliases = false;                                  //@K3A no aliases are returned
                        }                                                           //@K1A
                        else
                            fileAttribute = 4;  // Tables
                    }   // end of if typeTable
                    else if(typeMQTable)                //@K1A
                    {                                   //@K1A
                        if(typeView)                    //@K1A
                        {                               //@K1A
                            if(typeSystemTable)         //@K1A
                                fileAttribute = 16;     //views, system tables, and MQT's   //@K1A
                            else                                                            //@K1A
                                fileAttribute = 12;     //views and MQT's                   //@K1A
                        }                                                                   //@K1A
                        else if(typeSystemTable)                                            //@K1A
                            fileAttribute = 13;         //system tables and MQT's           //@K1A
                        else                                                                //@K1A
                            fileAttribute = 17;         //MQT's                             //@K1A
                        needToRemoveAliases = false;                                              //@K3A no aliases are returned
                    }                                                                       //@K1A
                    else
                    {           // Not tables
                        if (typeView)
                        {
                            if (typeSystemTable)
                                fileAttribute = 9;  // Views and system tables
                            else
                                fileAttribute = 5;  // Views
                        }
                        else
                        {
                            if (typeSystemTable)
                            {
                                fileAttribute = 6;  // System tables
                                needToRemoveAliases = false;      //@K3A no aliases are returned
                            }
                            else if(typeAlias && connection_.getVRM() >= JDUtilities.vrm430)    //@K3A  Aliases are only supported on V4R3 and higher 
                                fileAttribute = 10;                                             //@K3A
                            else
                                fileAttribute = -1; // Unknown type
                            // Will return empty results
                        }
                    }   // End of not tables else
                } // End of if tables != nulls
                else
                {
                    // Table types was set to null which implies all
                    // types are to be returned
                    fileAttribute = 1;               // All
                }


                //------------------------------------------------
                // Create the request to Retrieve File Information
                //------------------------------------------------
                if (fileAttribute != -1)
                { // If -1, return empty set
                  // Create a request
                  //@P0C
                    DBReturnObjectInformationRequestDS request = null;
                    DBReplyRequestedDS reply = null;
                    try
                    {
                        request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                                 DBReturnObjectInformationRequestDS.FUNCTIONID_FILE_INFO, id_,
                                                                                 DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                                                                 + DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT
                                                                                 + DBBaseRequestDS.ORS_BITMAP_RESULT_DATA,0);


                        //--------------------------------------------------
                        // Set the parameters for the request
                        //--------------------------------------------------


                        // Set the Library Name and Library Name Search Pattern parameters
                        // If null, do not set parameter.  The system default value of
                        // *USRLIBL is used.
                        if (schemaPattern != null)
                        { // use default library or qgpl
                            JDSearchPattern schema = new JDSearchPattern(schemaPattern);
                            request.setLibraryName (normalize(schema.getPatternString()), connection_.converter_); // @E4C
                            request.setLibraryNameSearchPatternIndicator(schema.getIndicator());
                        }



                        // Set the Table Name and Table Name Search Pattern parameters
                        // If null, do not set parameter.  The system default value of
                        // *ALL is used.
                        if (tablePattern!=null)
                        {
                            JDSearchPattern table = new JDSearchPattern(tablePattern);
                            request.setFileName (normalize(table.getPatternString()), connection_.converter_); // @E4C
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
                            (JDProperties.REMARKS, JDProperties.REMARKS_SQL))
                        {
                            request.setFileReturnInfoBitmap (0xF0000000); // return remarks
                        }
                        else
                        {
                            request.setFileReturnInfoBitmap (0xB4000000); // return text
                        }

                        // Order the results by table type, table schema, table name
                        // This is the same as ordering by tables on the system
                        request.setFileInfoOrderByIndicator (2);


                        //-------------------------------------------------------
                        // Send the request and cache all results from the system
                        //-------------------------------------------------------

                        reply = connection_.sendAndReceive (request);

                        // Check for errors - throw exception if errors
                        // were returned
                        int errorClass = reply.getErrorClass();
                        if (errorClass != 0)
                        {
                            int returnCode = reply.getReturnCode();
                            JDError.throwSQLException (this, connection_, id_,
                                                       errorClass, returnCode);
                        }


                        // Get the data format and result data
                        // The result data is parsed via JDServerRow getData
                        DBDataFormat dataFormat = reply.getDataFormat ();
                        DBData resultData = reply.getResultData ();
                        if (resultData != null)
                        {
                            // If the user didn't request aliases
                            if(needToRemoveAliases)                                       //@K3A
                                parseResultData(resultData, dataFormat);            //@K3A

                            // Put the data format into a row format. Handles data types
                            JDServerRow row = new JDServerRow (connection_, id_, dataFormat, settings_);

                            // Put the result data into a row cache
                            // ServerRowCache needs rowFormat to get offset and other info
                            // Only need this with this type of row cache (not with simple)
                            JDRowCache serverRowCache = new JDSimpleRowCache(new JDServerRowCache(row, connection_, id_, 1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));

                            // This is not actually moving data, it just sets up the mapping
                            JDFieldMap[] maps = null;       //@F2C
                            if (!isJDBC3)                   //@F2A
                                maps = new JDFieldMap[5];
                            else
                                maps = new JDFieldMap[10];

                            if (connection_.getProperties().equals
                                (JDProperties.REMARKS, JDProperties.REMARKS_SQL))
                            {
                                maps[0] = new JDHardcodedFieldMap (connection_.getCatalog());
                                maps[1] = new JDSimpleFieldMap (1);    // schema // @A3C @E4C
                                maps[2] = new JDSimpleFieldMap (3);    // table
                                maps[3] = new JDTableTypeFieldMap (4); // table type
                                maps[4] = new JDSimpleFieldMap (2);    // remarks
                                //@G4A The below fields will all return null.  We have distinct types
                                //@G4A instead of abstract types here, so a request returns no 
                                //@G4A type information.
                                if (isJDBC3)
                                {
                                    maps[5] = new JDHardcodedFieldMap ("", true, false); // types catalog //@G4A
                                    maps[6] = new JDHardcodedFieldMap ("", true, false); // types schema  //@G4A
                                    maps[7] = new JDHardcodedFieldMap ("", true, false); // type name     //@G4A
                                    maps[8] = new JDHardcodedFieldMap ("", true, false); // self referencing col name //@G4A
                                    maps[9] = new JDHardcodedFieldMap ("", true, false); // ref generation //@G4A
                                }
                            }

                            else
                            {   // Get file text instead of remarks
                                maps[0] = new JDHardcodedFieldMap (connection_.getCatalog());
                                maps[1] = new JDSimpleFieldMap (1);     // schema  // @A3C @E4C
                                maps[2] = new JDSimpleFieldMap (2);     // table
                                maps[3] = new JDTableTypeFieldMap (3); // table type
                                maps[4] = new JDSimpleFieldMap (4);      // File text
                                //@G4A The below fields will all return null.  We have distinct types
                                //@G4A instead of abstract types here, so a request returns no 
                                //@G4A type information.
                                if (isJDBC3)
                                {
                                    maps[5] = new JDHardcodedFieldMap ("", true, false); // types catalog //@G4A
                                    maps[6] = new JDHardcodedFieldMap ("", true, false); // types schema  //@G4A
                                    maps[7] = new JDHardcodedFieldMap ("", true, false); // type name     //@G4A
                                    maps[8] = new JDHardcodedFieldMap ("", true, false); // self referencing col name //@G4A
                                    maps[9] = new JDHardcodedFieldMap ("", true, false); // ref generation //@G4A
                                }
                            }

                            JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                            rowCache = new JDMappedRowCache (mappedRow, serverRowCache);

                        }
                        else
                            rowCache = new JDSimpleRowCache(formatRow);

                    }
                    finally
                    {
                        if (request != null) request.inUse_ = false;
                        if (reply != null) reply.inUse_ = false;
                    }
                } // End of if file attribute != -1
                else
                { // result set should be empty.
                    rowCache = new JDSimpleRowCache(formatRow);
                }

            } // End of else to create and send request

        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
        }

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "Tables");
    }

//@K3A                                                           
// Parses the result data from the system to determine if any aliases were returned.
    void parseResultData(DBData resultData, DBDataFormat dataFormat) {
        try{
            byte[] rawBytes = resultData.getRawBytes();
            int rowCount = resultData.getRowCount();
            int columnOffset = dataFormat.getFieldLength(0);         // schema
            columnOffset += dataFormat.getFieldLength(1);            // table
            int tableTypeLength = dataFormat.getFieldLength(2);      // table type
            SQLChar tableType = new SQLChar(tableTypeLength, settings_);    // create an sql char ojbect to get the table type
            int aliasCount = 0;
            ConvTable ccsidConverter = connection_.getConverter(dataFormat.getFieldCCSID(2));
            for(int i=0; i<rowCount; i++){
                //loop through the rows until the table type is not an alias ('A'), aliases are returned at the beginning of the data
                tableType.convertFromRawBytes(rawBytes, resultData.getRowDataOffset(i) + columnOffset, ccsidConverter);
                if(tableType.getString().equals("A"))
                    aliasCount++;
                else      //There are no more aliases break out of loop
                    break;
            }

            resultData.resetRowCount(rowCount-aliasCount);  // We want to ignore the rows that contain aliases since the user didn't request them
            resultData.setAliasCount(aliasCount);           // Set the alias count so we know what row to actually start with
        }catch(Exception e){
            if (JDTrace.isTraceOn())                                             
                JDTrace.logInformation (this, "Error parsing result data for aliases:  " + e.getMessage());
        }
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
        Object[][] data = null;
        if(connection_.getVRM() < JDUtilities.vrm520)
        {
            Object[][] data0 = { { JDTableTypeFieldMap.TABLE_TYPE_TABLE},
                { JDTableTypeFieldMap.TABLE_TYPE_VIEW},
                { JDTableTypeFieldMap.TABLE_TYPE_SYSTEM_TABLE}};
             data = data0;
        }
        else if(connection_.getVRM() < JDUtilities.vrm530)
        {
            Object[][] data0 = { { JDTableTypeFieldMap.TABLE_TYPE_TABLE},
                { JDTableTypeFieldMap.TABLE_TYPE_VIEW},
                { JDTableTypeFieldMap.TABLE_TYPE_SYSTEM_TABLE},
                { JDTableTypeFieldMap.TABLE_TYPE_ALIAS}};
             data = data0;
        }
        else
        {
            Object[][] data0 = { { JDTableTypeFieldMap.TABLE_TYPE_TABLE},
                { JDTableTypeFieldMap.TABLE_TYPE_VIEW},
                { JDTableTypeFieldMap.TABLE_TYPE_SYSTEM_TABLE},
                { JDTableTypeFieldMap.TABLE_TYPE_ALIAS},
                { JDTableTypeFieldMap.TABLE_TYPE_MATERIALIZED_QUERY_TABLE}};        //@K1A
            data = data0;
        }

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);
        JDSimpleRowCache rowCache = new JDSimpleRowCache(formatRow, data);

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
        // @J0A added try/catch because we are now sending the system VMR
        return JDEscapeClause.getTimeDateFunctions(connection_.getVRM()); // @J0M changed to send host version
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


        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);

        //@5WXVJX  Determine if translate hex is set to character.  If translate hex is character, than we only want to display
        //CHAR() FOR BIT DATA and VARCHAR() FOR BIT DATA for the binary types.  If translate hex is binary, then we only want 
        //to display BINARY AND VARBINARY for the binary types.  BINARY AND VARBINARY are only supported in V5R3 or higher.
        boolean translateHexAsChar = connection_.getProperties().equals (JDProperties.TRANSLATE_HEX, JDProperties.TRANSLATE_HEX_CHARACTER); //@5WXVJX
        
        // Initialize the data that makes up the contents
        // of the result set.
        // I changed this from an array to a Vector in order to make it            // @D0C
        // easier to conditionally add types based on the release.                 // @D0C
        Vector typeSamples = new Vector();                                         // @D0C

        typeSamples.addElement(new SQLChar(32765, settings_));              // @D0C
        if((connection_.getVRM() < JDUtilities.vrm530) || (translateHexAsChar && (connection_.getVRM() >= JDUtilities.vrm530))) //@5WXVJX
            typeSamples.addElement(new SQLCharForBitData(32765, settings_));           // @M0A
        typeSamples.addElement(new SQLDate(settings_, -1));                            // @D0C @550C
        typeSamples.addElement(new SQLDecimal(31, 31, settings_, connection_.getVRM(), connection_.getProperties())); // @M0C
        typeSamples.addElement(new SQLDouble(settings_));                          // @D0C
        typeSamples.addElement(new SQLFloat(settings_));                           // @D0C
        typeSamples.addElement(new SQLGraphic(16382, settings_));
        typeSamples.addElement(new SQLInteger());                                  // @D0C
        typeSamples.addElement(new SQLNumeric(31, 31, settings_, connection_.getVRM(), connection_.getProperties())); // @M0C
        typeSamples.addElement(new SQLReal(settings_));                            // @D0C
        typeSamples.addElement(new SQLSmallint());                                 // @D0C
        typeSamples.addElement(new SQLTime(settings_, -1));                            // @D0C @550C
        typeSamples.addElement(new SQLTimestamp(settings_));                       // @D0C
        //typeSamples.addElement(new SQLLongVarchar(32739, settings_));        //Change to report LONG VARCHAR as VARCHAR to be consistent with other clients.
        typeSamples.addElement(new SQLLongVargraphic(16369, settings_));
        typeSamples.addElement(new SQLLongVarcharForBitData(32739, settings_));
        typeSamples.addElement(new SQLVarchar(32739, settings_));                  // @D0C
        if((connection_.getVRM() < JDUtilities.vrm530) || (translateHexAsChar && (connection_.getVRM() >= JDUtilities.vrm530))) //@5WXVJX
            typeSamples.addElement(new SQLVarcharForBitData(32739, settings_));        // @M0A
        typeSamples.addElement(new SQLVargraphic(16369, settings_));
        
        if (connection_.getVRM() >= JDUtilities.vrm440)
        {       // @B4D B5A @D0C
            typeSamples.addElement(new SQLDatalink(32717, settings_));
            typeSamples.addElement(new SQLBlob(2147483646, settings_));           // @B4D B5A @D0C
            typeSamples.addElement(new SQLClob(2147483646, settings_));           // @B4D B5A @D0C @E1C
            typeSamples.addElement(new SQLDBClob(1073741822, settings_));
        }                                                                       // @B4D B5A 
        
        if (connection_.getVRM() >= JDUtilities.vrm450)         // @D0A
            typeSamples.addElement(new SQLBigint());                               // @D0A
        
        // @M0A - added support for binary, varbinary, and rowid data types
        if(connection_.getVRM() >= JDUtilities.vrm520)
        {
            typeSamples.addElement(new SQLRowID(settings_));
        }
        
        if(connection_.getVRM() >= JDUtilities.vrm530)
        {
            if(!translateHexAsChar)  //@5WXVJX
            {
                typeSamples.addElement(new SQLBinary(32765, settings_));
                typeSamples.addElement(new SQLVarbinary(32739, settings_));
            }
        }
        // @M0A - end new support

        int numberOfTypes = typeSamples.size();                                 // @D0C
        int numberOfFields = sqlData.length;
        Object[][] data = new Object[numberOfTypes][];
        boolean[][] nulls = new boolean[numberOfTypes][];
        boolean[][] dataMappingErrors = new boolean[numberOfTypes][];
        for (int i = 0; i < numberOfTypes; ++i)
        {
            data[i] = new Object[numberOfFields];
            nulls[i] = new boolean[numberOfFields];
            dataMappingErrors[i] = new boolean[numberOfFields];

            SQLData typeSample = (SQLData) typeSamples.elementAt(i);            // @D0C
            data[i][0] = typeSample.getTypeName ();                             // @D0C
            data[i][1] = new Short ((short) typeSample.getType ());             // @D0C
            data[i][2] = new Integer (typeSample.getMaximumPrecision());        // @D0C

            String literalPrefix = typeSample.getLiteralPrefix ();              // @D0C
            if (literalPrefix == null)
            {
                data[i][3]  = "";
                nulls[i][3] = true;
            }
            else
                data[i][3] = literalPrefix;

            String literalSuffix = typeSample.getLiteralSuffix ();              // @D0C
            if (literalSuffix == null)
            {
                data[i][4]  = "";
                nulls[i][4] = true;
            }
            else
                data[i][4] = literalSuffix;

            String createParameters = typeSample.getCreateParameters ();        // @D0C
            if (createParameters == null)
            {
                data[i][5]  = "";
                nulls[i][5] = true;
            }
            else
                data[i][5] = createParameters;

            data[i][6]  = new Short ((short) typeNullable);
            data[i][7]  = new Boolean (typeSample.isText ());                   // @D0C
            data[i][8]  = new Short ((short) typeSearchable);
            data[i][9]  = new Boolean (! typeSample.isSigned ());               // @D0C
            data[i][10] = new Boolean (false);
            data[i][11] = new Boolean (false);

            String localName = typeSample.getLocalName ();                      // @D0C
            if (localName == null)
            {
                data[i][12]  = "";
                nulls[i][12] = true;
            }
            else
                data[i][12] = localName;

            data[i][13] = new Short ((short) typeSample.getMinimumScale ());    // @D0C
            data[i][14] = new Short ((short) typeSample.getMaximumScale ());    // @D0C
            data[i][15] = new Integer (0);
            data[i][16] = new Integer (0);
            data[i][17] = new Integer (typeSample.getRadix ());                 // @D0C
        }

        JDSimpleRowCache rowCache = new JDSimpleRowCache(formatRow, data, nulls, dataMappingErrors);

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
                            no value is sent to the system and the system
                            default of *USRLIBL is used.  If empty string
                            is specified, an empty result set is returned.
    @param  typeNamePattern The type name pattern. If null is specified,
                            no value is sent to the system and the system
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
    //    It turns out that this is not an issue.  In the "JDBC Tutorial and
    //    Reference", section 3.5.5 "Creating a DISTINCT type", it says:
    //    "A DISTINCT type is always based on another data type, which must
    //    be a predefined type.  ... a DISTINCT type cannot be based
    //    on a UDT."  ("UDT" means user-defined type.)
    //    So we can make the assumption that the source type
    //    always identifies a system predefined type.
    //
    public ResultSet getUDTs (String catalog,
                              String schemaPattern,
                              String typeNamePattern,
                              int[] types)
    throws SQLException
    {
        connection_.checkOpen ();

        boolean isJDBC3 = JDUtilities.JDBCLevel_ >= 30; //@F2A @j4a

        String[] fieldNames = null;               //@F2C
        SQLData[] sqlData = null;                 //@F2C
        int[] fieldNullables = null;              //@F2C
        if (!isJDBC3)                 //@F2A
        {
            // Set up the result set in the format required by JDBC
            fieldNames = new String[] { "TYPE_CAT",
                "TYPE_SCHEM",
                "TYPE_NAME",
                "CLASS_NAME",
                "DATA_TYPE",
                "REMARKS",
            };

            sqlData = new SQLData[] { new SQLVarchar (128, settings_),  // type catalog
                new SQLVarchar (128, settings_),  // type schema
                new SQLVarchar (128, settings_),  // type name
                new SQLVarchar (128, settings_),  // class name
                new SQLSmallint (),               // data type
                new SQLVarchar (2000, settings_), // remarks
            };

            fieldNullables = new int[] {  columnNullable,  // type catalog
                columnNullable,  // type schema
                columnNoNulls,   // type name
                columnNoNulls,   // class name
                columnNoNulls,   // data type
                columnNoNulls,   // remarks
            };
        }
        else
        {
            // Set up the result set in the format required by JDBC 3.0
            fieldNames = new String[] { "TYPE_CAT",
                "TYPE_SCHEM",
                "TYPE_NAME",
                "CLASS_NAME",
                "DATA_TYPE",
                "REMARKS",
                "BASE_TYPE"  //@G4A
            };

            sqlData = new SQLData[] { new SQLVarchar (128, settings_),  // type catalog
                new SQLVarchar (128, settings_),  // type schema
                new SQLVarchar (128, settings_),  // type name
                new SQLVarchar (128, settings_),  // class name
                new SQLSmallint (),               // data type
                new SQLVarchar (2000, settings_), // remarks
                new SQLSmallint (),               // base type  //@G4A
            };

            fieldNullables = new int[] {  columnNullable,  // type catalog
                columnNullable,  // type schema
                columnNoNulls,   // type name
                columnNoNulls,   // class name
                columnNoNulls,   // data type
                columnNoNulls,   // remarks
                columnNullable,  // base type  //@G4A
            };

        }

        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);
        JDRowCache rowCache = null;

        // We only support DISTINCT in the types array.  Determine
        // if this was passed as one of the elements.  If null is
        // passed, that is like looking for everything, so make that
        // work like DISTINCT was passed.
        boolean distinctPassed = false;
        if (types == null)
            distinctPassed = true;
        else
        {
            for (int i = 0; i < types.length; ++i)
                if (types[i] == Types.DISTINCT)
                {
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
        else
        {

            // Build up the query,
            StringBuffer select = new StringBuffer ();
            select.append ("SELECT USER_DEFINED_TYPE_SCHEMA, "      // @B2C
                           + "USER_DEFINED_TYPE_NAME, "             // @B2C
                           + "SOURCE_TYPE, REMARKS");
            if (isJDBC3)     //@F2A
            {
                select.append (", SMALLINT (CASE SOURCE_TYPE "          // @G4A
                               + "WHEN 'BIGINT'     THEN -5   WHEN 'CHAR'             THEN 1 "     // @G4A
                               + "WHEN 'CHARACTER'  THEN 1    WHEN 'GRAPHIC'          THEN 1 "     // @G4A
                               + "WHEN 'NUMERIC'    THEN 2    WHEN 'DECIMAL'          THEN 3 "     // @G4A
                               + "WHEN 'INTEGER'    THEN 4    WHEN 'SMALLINT'         THEN 5 "     // @G4A
                               + "WHEN 'REAL'       THEN 6    WHEN 'FLOAT'            THEN 8 "     // @G4A
                               + "WHEN 'DOUBLE'     THEN 8    WHEN 'DOUBLE PRECISION' THEN 8 "     // @G4A
                               + "WHEN 'VARCHAR'    THEN 12   WHEN 'VARGRAPHIC'       THEN 12 "    // @G4A
                               + "WHEN 'DATALINK'   THEN 70   WHEN 'DATE'             THEN 91 "    // @G4A
                               + "WHEN 'TIME'       THEN 92   WHEN 'TIMESTMP'         THEN 93 "    // @G4A
                               + "WHEN 'TIMESTAMP'  THEN 93   WHEN 'BLOB'             THEN 2004 "  // @G4A
                               + "WHEN 'CLOB'       THEN 2005 WHEN 'DBCLOB'           THEN 2005 "  // @G4A
                               + "ELSE NULL         END)");
            }

            select.append (" FROM QSYS2");  //@B2C @F2M
            select.append (getCatalogSeparator ());
            select.append ("SYSTYPES " );

            StringBuffer where = new StringBuffer ();
            if (schemaPattern != null)
            {
                JDSearchPattern searchPattern = new JDSearchPattern (schemaPattern);
                where.append (searchPattern.getSQLWhereClause ("USER_DEFINED_TYPE_SCHEMA"));    // @B2C
            }
            if (typeNamePattern != null)
            {
                JDSearchPattern searchPattern = new JDSearchPattern (typeNamePattern);
                if (where.length () > 0)
                    where.append (" AND ");
                where.append (searchPattern.getSQLWhereClause ("USER_DEFINED_TYPE_NAME"));      // @B2C
            }

            if (where.length () > 0)
            {
                select.append (" WHERE ");
                //@J4c JDK 1.4 added a StringBuffer.append(StringBuffer) method.  Do a toString()
                //     here just in case we compile against 1.4 but run against 1.3.  In that
                //     case we would get 'method not found'
                select.append (where.toString());  
            }

            select.append (" ORDER BY USER_DEFINED_TYPE_SCHEMA, USER_DEFINED_TYPE_NAME");       // @B2C

            // Run the query.
            try
            {
                Statement statement = connection_.createStatement();
                ResultSet serverResultSet = statement.executeQuery (select.toString());
                JDRowCache serverRowCache = new JDSimpleRowCache (((AS400JDBCResultSet) serverResultSet).getRowCache ());
                statement.close ();

                // Set up the maps.
                JDFieldMap[] maps = null;   //@F2C
                if (!isJDBC3)               //@F2A
                {
                    maps = new JDFieldMap[] {
                        new JDHardcodedFieldMap (connection_.getCatalog ()),                 // type catalog
                        new JDSimpleFieldMap (1),                                            // type schema
                        new JDSimpleFieldMap (2),                                            // type name
                        new JDClassNameFieldMap (3, settings_, connection_.getVRM(), connection_.getProperties()), // class name   // @B3C // @M0C
                        new JDHardcodedFieldMap (new Integer (Types.DISTINCT)),              // data type
                        new JDHandleNullFieldMap (4, ""),                                    // remarks      // @B3C 
                    };
                }
                else
                {
                    maps = new JDFieldMap[] {
                        new JDHardcodedFieldMap (connection_.getCatalog ()),                 // type catalog
                        new JDSimpleFieldMap (1),                                            // type schema
                        new JDSimpleFieldMap (2),                                            // type name
                        new JDClassNameFieldMap (3, settings_, connection_.getVRM(), connection_.getProperties()), // class name   // @B3C  // @M0C  
                        new JDHardcodedFieldMap (new Integer (Types.DISTINCT)),              // data type
                        new JDHandleNullFieldMap (4, ""),                                    // remarks      // @B3C
                        new JDSimpleFieldMap (5)                                             // base type    // @G4A  
                    };

                }

                JDMappedRow mappedRow = new JDMappedRow (formatRow, maps);
                rowCache = new JDMappedRowCache (mappedRow, serverRowCache);
            }
            catch (SQLException e)
            {

                // If the system does not have this table, then
                // force an empty result set.  This just means
                // that UDTs are not supported (which is true
                // for pre-V4R4 systems).
                if (e.getErrorCode () == -204)
                    rowCache = new JDSimpleRowCache (formatRow);
                else
                    JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
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


        try
        {
            // Check for conditions that would result in an empty result set
            // Must check for null first to avoid NullPointerException

            if (!isCatalogValid(catalog) ||     // catalog is empty string

                // schema is not null and is empty string
                ((schema != null) && (schema.length()==0)) ||

                // Table is null
                (table==null)      ||

                // Table is empty string
                (table.length()==0 ))
            { // Return empty result set
                rowCache = new JDSimpleRowCache (formatRow);
            }

            else
            { // parameter values are valid, build request & send
              // Create a request
              //@P0C
                DBReturnObjectInformationRequestDS request = null;
                DBReplyRequestedDS reply = null;
                try
                {
                    request = DBDSPool.getDBReturnObjectInformationRequestDS (
                                                                             DBReturnObjectInformationRequestDS.FUNCTIONID_SPECIAL_COLUMN_INFO  ,
                                                                             id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA +
                                                                             DBBaseRequestDS.ORS_BITMAP_DATA_FORMAT +
                                                                             DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);



                    // Set the library name
                    if (schema == null)
                    {   // use default library or qgpl
                        request.setLibraryName(normalize(connection_.getDefaultSchema()), connection_.converter_);  // @E4C
                    }
                    else request.setLibraryName(normalize(schema), connection_.converter_);                       // @E4C

                    // Set the table name
                    request.setFileName(normalize(table), connection_.converter_);  // @E4C


                    // Set the Field Information to Return Bitmap
                    // Return library, table, and column
                    request.setSpecialColumnsReturnInfoBitmap(0x1F000000);


                    // Set the short / long file and field name indicator
                    request.setFileShortOrLongNameIndicator(0xF0); // Long

                    // Set columns nullable indicator to allows nullable columns
                    request.setSpecialColumnsNullableIndicator(0xF1);


                    //--------------------------------------------------------
                    //  Send the request and cache all results from the system
                    //--------------------------------------------------------

                    reply = connection_.sendAndReceive(request);


                    // Check for errors - throw exception if errors were
                    // returned
                    int errorClass = reply.getErrorClass();
                    if (errorClass !=0)
                    {
                        int returnCode = reply.getReturnCode();
                        JDError.throwSQLException (this, connection_, id_,
                                                   errorClass, returnCode);
                    }

                    // Get the data format and result data
                    DBDataFormat dataFormat = reply.getDataFormat();
                    DBData resultData = reply.getResultData();
                    if (resultData != null)
                    {
                        JDServerRow row =  new JDServerRow (connection_, id_, dataFormat, settings_);
                        JDRowCache serverRowCache = new JDSimpleRowCache (new JDServerRowCache (row, connection_, id_,
                                                                                                1, resultData, true, ResultSet.TYPE_SCROLL_INSENSITIVE));

                        // Create the mapped row format that is returned in the
                        // result set.
                        // This does not actual move the data, it just sets up
                        // the mapping.
                        JDFieldMap[] maps = new JDFieldMap[8];
                        maps[0] = new JDHardcodedFieldMap (new Short ((short) 0)); // scope
                        maps[1] = new JDSimpleFieldMap (1); // column name
                        maps[2] = new JDDataTypeFieldMap (2, 3, 3, 5, 0, connection_.getVRM(), connection_.getProperties()); // @M0C  // data type - converted to short   //@KKB added 0 for ccsid since not given to us by host server
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

                }
                finally
                {
                    if (request != null) request.inUse_ = false;
                    if (reply != null) reply.inUse_ = false;
                }
            }  // End of else to build and send request
        } // End of try block

        catch (DBDataStreamException e)
        {
            JDError.throwSQLException (this, JDError.EXC_INTERNAL, e);
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

        return(catalog.equalsIgnoreCase (connection_.getCatalog())
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


    //@F3A
    /**
    Indicates if updateable LOB methods update a copy of the LOB or if updates
    are made directly to the LOB.  True is returned if updateable lob methods 
    update a copy of the LOB, false is returned if updates are made directly
    to the LOB.
    
    @return     Always true.    Updateable lob methods update a copy of the LOB. 
    ResultSet.updateRow() must be called to update the LOB in the DB2 for i5/OS database.
    
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public boolean locatorsUpdateCopy ()
    throws SQLException
    {
        return true;
    }



    // @E4A
    // The database is not case-sensitive except when names are quoted with double
    // quotes.  The host server flows are case-sensitive, so I will uppercase 
    // everything to save the caller from having to do so.
    private String normalize(String mixedCaseName)
    {
        if (mixedCaseName.length() > 2)
        {
            if (mixedCaseName.charAt(0) == '"')
                return JDUtilities.stripOutDoubleEmbededQuotes(mixedCaseName); //@PDC mixedCaseName.substring(1, mixedCaseName.length() - 1);
        }
        return mixedCaseName.toUpperCase();
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

        return(resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
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

        return(resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
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

        return(resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
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

        return(resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
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

        return(resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
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

        return(resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE);
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



    //@G4A
    /**
    Indicates if, after a statement is executed, auto-generated keys can be retrieved 
    using the method Statement.getGeneratedKeys().
    
    @return     True if the user is connecting to a system running OS/400 V5R2      
    or i5/OS, otherwise false.  Auto-generated keys are supported
    only if connecting to a system running OS/400 V5R2 or i5/OS.
        
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public boolean supportsGetGeneratedKeys ()
    throws SQLException
    {
        if (connection_.getVRM() >= JDUtilities.vrm520)
            return true;
        else
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



    //@G4A
    /**
    Indicates if multiple result sets can be returned from a 
    CallableStatement simultaneously.
            
    @return     Always false.  Multiple open result sets from a single execute
                are not supported by the Toolbox driver.
        
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public boolean supportsMultipleOpenResults ()
    throws SQLException
    {
        return false;
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



    //@G4A
    /**
    Indicates if using parameter names to specify parameters on 
    callable statements are supported.
        
    @return     Always true.  An application can use parameter names
    to specify parameters on callable statements.
        
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public boolean supportsNamedParameters ()
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
    <tr><td>TYPE_SCROLL_SENSITIVE</td><td>Yes</td><td>Yes</td></tr>
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
    // The unsupported combinations are dictated by the DB2
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
            JDError.throwSQLException (this, JDError.EXC_CONCURRENCY_INVALID);

        // Cases that we don't support.                                                         
        //@K2D if (((resultSetConcurrency == ResultSet.CONCUR_READ_ONLY)
        //@K2D     && (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE))
        //@K2D    || ((resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
        //@K2D        && (resultSetType == ResultSet.TYPE_SCROLL_INSENSITIVE)))
        if((resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)                        //@K2A
           && (resultSetType == ResultSet.TYPE_SCROLL_INSENSITIVE))                     //@K2A
            return false;

        // We support all other cases.
        return true;
    }



    //@G4A
    /**
    Indicates if a type of result set holdability is supported.  The two 
    types are ResultSet.HOLD_CURSORS_OVER_COMMIT and ResultSet.CLOSE_CURSORS_AT_COMMIT.
        
    @return     True if the user is connecting to a system running OS/400     
    V5R2 or i5/OS, otherwise false.  Both types of result set 
    holidability are supported if connecting to OS/400 V5R2 or i5/OS.
            
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public boolean supportsResultSetHoldability (int resultSetHoldability)
    throws SQLException
    {
        if (connection_.getVRM() >= JDUtilities.vrm520)
            return true;
        else
            return false;
    }



    //@G4A
    /**
    Indicates if savepoints are supported.
        
    @return     True if the user is connecting to a system running 
    OS/400 V5R2 or i5/OS, otherwise false.  Savepoints are supported
    only if connecting to OS/400 V5R2 or i5/OS.
            
    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public boolean supportsSavepoints ()
    throws SQLException
    {                     
        // Note we check only the system level.  We don't need to
        // check JDBC/JDK level because if running prior to JDBC 3.0
        // the app cannot call this method (it does not exist
        // in the interface).
        if (connection_.getVRM() >= JDUtilities.vrm520)
            return true;
        else
            return false;
    }



    // JDBC 2.0
    /**
    Indicates if the specified result set type is supported.
    
    @param resultSetType        The result set type.  Valid values are:
                                <ul>
                                  <li>ResultSet.TYPE_FORWARD_ONLY
                                  <li>ResultSet.TYPE_SCROLL_INSENSITIVE
                                  <li>ResultSet.TYPE_SCROLL_SENSITIVE
                                </ul>
    @return                     true for ResultSet.TYPE_FORWARD_ONLY 
                                ResultSet.TYPE_SCROLL_SENSITIVE. and 
                                ResultSet.TYPE_SCROLL_INSENSITIVE.
    
    @exception  SQLException    If the result set type is not valid.
    **/
    public boolean supportsResultSetType (int resultSetType)
    throws SQLException
    {
        switch(resultSetType)
        {
            case ResultSet.TYPE_FORWARD_ONLY:
            case ResultSet.TYPE_SCROLL_SENSITIVE:
            case ResultSet.TYPE_SCROLL_INSENSITIVE:
                return true;
            default:
                JDError.throwSQLException (this, JDError.EXC_CONCURRENCY_INVALID);
                return false;
        }
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



    //@B0A - We don't support this directly. Use package caching instead.
    /**
     * Indicates if statement pooling is supported.
     * @return Always false. Statement pooling is not supported at this time.
    **/
    public boolean supportsStatementPooling()
    {
        return false;
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
            JDError.throwSQLException (this, JDError.EXC_CONCURRENCY_INVALID);

        if(transactionIsolationLevel == Connection.TRANSACTION_NONE)
        {
            return false; // we have determined that we do not support JDBC's idea of TRANSACTION_NONE
        }
        else
        {
            return true;
        }
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
        try
        {
            return connection_.getCatalog();
        }
        catch (SQLException e)
        {
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

    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] {  "com.ibm.as400.access.AS400JDBCDatabaseMetaData", "java.sql.DatabaseMetaData" };
    }
  

    //@PDA jdbc40
    /**
     * Retrieves whether a <code>SQLException</code> thrown while autoCommit is <code>true</code> indicates 
     * that all open ResultSets are closed, even ones that are holdable.  When a <code>SQLException</code> occurs while
     * autocommit is <code>true</code>, it is vendor specific whether the JDBC driver responds with a commit operation, a 
     * rollback operation, or by doing neither a commit nor a rollback.  A potential result of this difference
     * is in whether or not holdable ResultSets are closed.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException
    {
        return false;  //toolbox returns false based on current behavoir
    }

    
    //@PDA jdbc40
    /**
     * Retrieves a list of the client info properties 
     * that the driver supports.  The result set contains the following columns
     * <p>
         * <ol>
     * <li><b>NAME</b> String=> The name of the client info property<br>
     * <li><b>MAX_LEN</b> int=> The maximum length of the value for the property<br>
     * <li><b>DEFAULT_VALUE</b> String=> The default value of the property<br>
     * <li><b>DESCRIPTION</b> String=> A description of the property.  This will typically 
     *                      contain information as to where this property is 
     *                      stored in the database.
     * </ol>
         * <p>
     * The <code>ResultSet</code> is sorted by the NAME column in ascending order
     * <p>
     * @return  A <code>ResultSet</code> object; each row is a supported client info
     * property
     * <p>
     *  @exception SQLException if a database access error occurs
     * <p>
     */
    public ResultSet getClientInfoProperties() throws SQLException
    {
        // Set up the result set.
        String[] fieldNames = { "NAME", "MAX_LEN", "DEFAULT_VALUE", "DESCRIPTION" };
        SQLData[] sqlData = { new SQLVarchar(32, settings_), new SQLInteger(), new SQLVarchar(32, settings_), new SQLVarchar(1024, settings_) };
        int[] fieldNullables = {columnNoNulls, columnNoNulls, columnNoNulls, columnNoNulls}; // table types can not be null
        
        Object[][] data =  { { "ApplicationName", new Integer(255), "", AS400JDBCDriver.getResource ("CLIENT_INFO_DESC_APPLICATIONNAME") }, 
                { "ClientUser", new Integer(255), "", AS400JDBCDriver.getResource ("CLIENT_INFO_DESC_CLIENTUSER")},
                { "ClientHostname", new Integer(255), "", AS400JDBCDriver.getResource ("CLIENT_INFO_DESC_CLIENTHOSTNAME")},
                { "ClientAccounting", new Integer(255), "", AS400JDBCDriver.getResource ("CLIENT_INFO_DESC_CLIENTACCOUNTING")}};
     
        JDSimpleRow formatRow = new JDSimpleRow (fieldNames, sqlData, fieldNullables);
        JDSimpleRowCache rowCache = new JDSimpleRowCache(formatRow, data);

        return new AS400JDBCResultSet (rowCache, connection_.getCatalog(),
                                       "Client Info");
    }


    //@PDA jdbc40
    /**
     * Indicates whether or not this data source supports the SQL <code>ROWID</code> type,
     * and if so  the lifetime for which a <code>RowId</code> object remains valid. 
     * <p>
     * The returned int values have the following relationship: 
     * <pre>
     *     ROWID_UNSUPPORTED < ROWID_VALID_OTHER < ROWID_VALID_TRANSACTION
     *         < ROWID_VALID_SESSION < ROWID_VALID_FOREVER
     * </pre>
     * so conditional logic such as 
     * <pre>
     *     if (metadata.getRowIdLifetime() > DatabaseMetaData.ROWID_VALID_TRANSACTION)
     * </pre>
     * can be used. Valid Forever means valid across all Sessions, and valid for 
     * a Session means valid across all its contained Transactions. 
     *
     * @throws SQLException if a database access error occurs
     */
    public RowIdLifetime getRowIdLifetime() throws SQLException
    {
        return RowIdLifetime.ROWID_VALID_FOREVER; //toolbox rowid is forever
    }


    //@PDA jdbc40
    /**
     * Retrieves the schema names available in this database.  The results
     * are ordered by schema name.
     *
     * <P>The schema column is:
     *  <OL>
     *  <LI><B>TABLE_SCHEM</B> String => schema name
     *  <LI><B>TABLE_CATALOG</B> String => catalog name (may be <code>null</code>)
     *  </OL>
     *
     *
     * @param catalog a catalog name; must match the catalog name as it is stored
     * in the database;"" retrieves those without a catalog; null means catalog
     * name should not be used to narrow down the search.
     * @param schemaPattern a schema name; must match the schema name as it is
     * stored in the database; null means
     * schema name should not be used to narrow down the search.
     * @return a <code>ResultSet</code> object in which each row is a
     *         schema decription
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
    {
        connection_.checkOpen();

        CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator() + "SQLTABLES  (?, ?, ?, ?, ?)");
        
        cstmt.setString(1, catalog);
        cstmt.setString(2, schemaPattern);
        cstmt.setString(3, null);
        cstmt.setString(4, null);
        cstmt.setObject(5, "DATATYPE='JDBC';GETSCHEMAS=2;CURSORHOLD=1");
        ResultSet rs = cstmt.executeQuery();
        return rs;
    }


    //@PDA jdbc40
    /**
     * Retrieves whether this database supports invoking user-defined or vendor functions 
     * using the stored procedure escape syntax.
     *
     * @return <code>true</code> if so; <code>false</code> otherwise 
     * @exception SQLException if a database access error occurs
     */
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
    {
        // toolbox does not support this
        return false;
    }
 

    //@PDA jdbc40
    /**
     * Retrieves a description of the user functions available in the given
     * catalog.
      * <P>
     * Only system and user function descriptions matching the schema and
     * function name criteria are returned.  They are ordered by
     * <code>FUNCTION_CAT</code>, <code>FUNCTION_SCHEM</code>,
     * <code>FUNCTION_NAME</code> and 
     * <code>SPECIFIC_ NAME</code>.
     *
     * <P>Each function description has the the following columns:
     *  <OL>
     *  <LI><B>FUNCTION_CAT</B> String => function catalog (may be <code>null</code>)
     *  <LI><B>FUNCTION_SCHEM</B> String => function schema (may be <code>null</code>)
     *  <LI><B>FUNCTION_NAME</B> String => function name.  This is the name 
     * used to invoke the function
     *  <LI><B>REMARKS</B> String => explanatory comment on the function
     * <LI><B>FUNCTION_TYPE</B> short => kind of function:
     *      <UL>
     *      <LI>functionResultUnknown - Cannot determine if a return value
     *       or table will be returned
     *      <LI> functionNoTable- Does not return a table
     *      <LI> functionReturnsTable - Returns a table
     *      </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  => the name which uniquely identifies 
     *  this function within its schema.  This is a user specified, or DBMS
     * generated, name that may be different then the <code>FUNCTION_NAME</code> 
     * for example with overload functions
     *  </OL>
     * <p>
     * A user may not have permissions to execute any of the functions that are
     * returned by <code>getFunctions</code>
     *
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow
     *        the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *        as it is stored in the database; "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow
     *        the search
     * @param functionNamePattern a function name pattern; must match the
     *        function name as it is stored in the database 
     * @return <code>ResultSet</code> - each row is a function description 
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException
    {
        connection_.checkOpen();

        if(connection_.getVRM() < JDUtilities.vrm550) //@pda HSTSRVR support not PTFing support to v5r4
        {
            JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
            return null;
        }
        /*
         SYSIBM.SQLFunctions(
         CatalogName     varchar(128),
         SchemaName      varchar(128),
         FunctionName        varchar(128),
         Options         varchar(4000))
        */
        
        CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator() + "SQLFUNCTIONS  ( ?, ?, ?, ?)");
        
        cstmt.setString(1, catalog);
        cstmt.setString(2, schemaPattern);
        cstmt.setString(3, functionNamePattern);
        cstmt.setObject(4, "DATATYPE='JDBC';CURSORHOLD=1");
        ResultSet rs = cstmt.executeQuery();
        return rs;
    }


    //@pda jdbc40
    /**
     * Retrieves a description of the given catalog's system or user 
     * function parameters and return type.
     *
     * <P>Only descriptions matching the schema,  function and
     * parameter name criteria are returned. They are ordered by
     * <code>FUNCTION_CAT</code>, <code>FUNCTION_SCHEM</code>,
     * <code>FUNCTION_NAME</code> and 
     * <code>SPECIFIC_ NAME</code>. Within this, the return value,
     * if any, is first. Next are the parameter descriptions in call
     * order. The column descriptions follow in column number order.
     *
     * <P>Each row in the <code>ResultSet</code> 
     * is a parameter description, column description or
     * return type description with the following fields:
     *  <OL>
     *  <LI><B>FUNCTION_CAT</B> String => function catalog (may be <code>null</code>)
     *  <LI><B>FUNCTION_SCHEM</B> String => function schema (may be <code>null</code>)
     *  <LI><B>FUNCTION_NAME</B> String => function name.  This is the name 
     * used to invoke the function
     *  <LI><B>COLUMN_NAME</B> String => column/parameter name 
     *  <LI><B>COLUMN_TYPE</B> Short => kind of column/parameter:
     *      <UL>
     *      <LI> functionColumnUnknown - nobody knows
     *      <LI> functionColumnIn - IN parameter
     *      <LI> functionColumnInOut - INOUT parameter
     *      <LI> functionColumnOut - OUT parameter
     *      <LI> functionColumnReturn - function return value
     *      <LI> functionColumnResult - Indicates that the parameter or column
     *  is a column in the <code>ResultSet</code>
     *      </UL>
     *  <LI><B>DATA_TYPE</B> int => SQL type from java.sql.Types
     *  <LI><B>TYPE_NAME</B> String => SQL type name, for a UDT type the
     *  type name is fully qualified
     *  <LI><B>PRECISION</B> int => precision
     *  <LI><B>LENGTH</B> int => length in bytes of data
     *  <LI><B>SCALE</B> short => scale -  null is returned for data types where  
     * SCALE is not applicable.
     *  <LI><B>RADIX</B> short => radix
     *  <LI><B>NULLABLE</B> short => can it contain NULL.
     *      <UL>
     *      <LI> functionNoNulls - does not allow NULL values
     *      <LI> functionNullable - allows NULL values
     *      <LI> functionNullableUnknown - nullability unknown
     *      </UL>
     *  <LI><B>REMARKS</B> String => comment describing column/parameter
     *  <LI><B>CHAR_OCTET_LENGTH</B> int  => the maximum length of binary 
     * and character based parameters or columns.  For any other datatype the returned value 
     * is a NULL
     *  <LI><B>ORDINAL_POSITION</B> int  => the ordinal position, starting 
     * from 1, for the input and output parameters. A value of 0
     * is returned if this row describes the function's return value. 
     * For result set columns, it is the
     * ordinal position of the column in the result set starting from 1.  
     *  <LI><B>IS_NULLABLE</B> String  => ISO rules are used to determine 
     * the nullability for a parameter or column.
     *       <UL>
     *       <LI> YES           --- if the parameter or column can include NULLs
     *       <LI> NO            --- if the parameter or column  cannot include NULLs
     *       <LI> empty string  --- if the nullability for the 
     * parameter  or column is unknown
     *       </UL>
     *  <LI><B>SPECIFIC_NAME</B> String  => the name which uniquely identifies 
     * this function within its schema.  This is a user specified, or DBMS
     * generated, name that may be different then the <code>FUNCTION_NAME</code> 
     * for example with overload functions
     *  </OL>
     * 
     * <p>The PRECISION column represents the specified column size for the given 
     * parameter or column. 
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters. 
     * For datetime datatypes, this is the length in characters of the String representation (assuming the 
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.  For the ROWID datatype, 
     * this is the length in bytes. Null is returned for data types where the
     * column size is not applicable.
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow
     *        the search
     * @param schemaPattern a schema name pattern; must match the schema name
     *        as it is stored in the database; "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow
     *        the search
     * @param functionNamePattern a procedure name pattern; must match the
     *        function name as it is stored in the database 
     * @param columnNamePattern a parameter name pattern; must match the 
     * parameter or column name as it is stored in the database 
     * @return <code>ResultSet</code> - each row describes a 
     * user function parameter, column  or return type
     *
     * @exception SQLException if a database access error occurs
     * @see #getSearchStringEscape 
     */
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException
    { 
        //@PDA add support to call stored procedure
        connection_.checkOpen();

        if(connection_.getVRM() < JDUtilities.vrm550) //@pda HSTSRVR support not PTFing support to v5r4
        {
            JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
            return null;
        }
        /*
         SQLFunctionCols(
          CatalogName     varchar(128),
          SchemaName      varchar(128),
          FuncName        varchar(128),
          ParamName         varchar(128),
          Options         varchar(4000))
        */
        
        CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator() + "SQLFUNCTIONCOLS  ( ?, ?, ?, ?, ?)");
        
        cstmt.setString(1, catalog);
        cstmt.setString(2, schemaPattern);
        cstmt.setString(3, functionNamePattern);
        cstmt.setString(4, columnNamePattern);
        cstmt.setObject(5, "DATATYPE='JDBC';CURSORHOLD=1");
        ResultSet rs = cstmt.executeQuery();
        return rs;
    }
  
      
}