///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: AS400JDBCDatabaseMetaData.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2010 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ibm.jtopenlite.SystemInfo;





/**
The JDBCDatabaseMetaData class provides information
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
//             schema (library) - use default SQL schema
//                                The default SQL schema can be
//                                set in the URL. If not
//                                specified in URL, the first
//                                library specified in the library
//                                properties is used as the
//                                default SQL schema.
//                                If no default SQL schema exists,
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

public class JDBCDatabaseMetaData

implements DatabaseMetaData
{
  static final String copyright = "Copyright (C) 1997-2010 International Business Machines Corporation and others.";

    // Private data.
    private JDBCConnection     connection_;

    // misc constants for sysibm stored procedures
    final static int SQL_NO_NULLS            = 0;
    final static int SQL_NULLABLE            = 1;
    final static int SQL_NULLABLE_UNKNOWN    = 2;
    final static int SQL_BEST_ROWID          = 1;
    final static int SQL_ROWVER              = 2;
    static final String EMPTY_STRING         = "";
    static final String MATCH_ALL            = "%";


    private static final String VIEW          = "VIEW";
    private static final String TABLE         = "TABLE";
    private static final String SYSTEM_TABLE  = "SYSTEM TABLE";
    private static final String ALIAS         = "ALIAS";
    private static final String MQT           = "MATERIALIZED QUERY TABLE";
    private static final String SYNONYM       = "SYNONYM";
    private static final String FAKE_VALUE    = "QCUJOFAKE";
    private static final int  SQL_ALL_TYPES   = 0;

    // the DB2 SQL reference says this should be 2147483647 but we return 1 less to allow for NOT NULL columns
    static final int MAX_LOB_LENGTH           = 2147483646;


    static int javaVersion = 0;
    static {
    	String javaVersionString = System.getProperty("java.version");
    	if (javaVersionString != null) {
    	    int dotIndex = javaVersionString.indexOf('.');
    	    if (dotIndex > 0) {
    	      int secondDotIndex = javaVersionString.indexOf('.', dotIndex+1);
    	      if (secondDotIndex > 0) {
    	        String firstDigit = javaVersionString.substring(0,dotIndex);
    	        String secondDigit = javaVersionString.substring(dotIndex+1, secondDotIndex);
    	        javaVersion = Integer.parseInt(firstDigit)*10 + Integer.parseInt(secondDigit);
    	      }
    	    }
    	}
    }

    /**
    Constructs an JDBCDatabaseMetaData object.

    @param   connection  The connection to the system.
    **/
    JDBCDatabaseMetaData (JDBCConnection connection)
    throws SQLException
    {
        connection_ = connection;
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
    Returns a ResultSet containing a description of type attributes available in a
    specified catalog.

    This method only applies to the attributes of a
    structured type.  Distinct types are stored in the datatypes
    catalog, not the attributes catalog. Since DB2 for IBM i does not support
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


        ResultSet rs = statement.executeQuery("SELECT VARCHAR('1', 128) AS TYPE_CAT, " +
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

        return rs;
    }



    /**
  Returns a description of a table's optimal set of columns
  that uniquely identifies a row.


  @param  catalog        The catalog name. If null is specified, this parameter
                       is ignored.  If empty string is specified,
                       an empty result set is returned.
  @param  schema         The schema name. If null is specified, the
                       default SQL schema specified in the URL is used.
                       If null is specified and a default SQL schema was not
                       specified in the URL, the first library specified
                       in the libraries properties file is used.
                       If null is specified and a default SQL schema was
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

            CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator () + "SQLSPECIALCOLUMNS(?,?,?,?,?,?,?)");

            cstmt.setShort(1, (short)SQL_BEST_ROWID);
            cstmt.setString(2, normalize(catalog));
            cstmt.setString(3, normalize(schema));
            cstmt.setString(4, normalize(table));
            cstmt.setShort(5, (short) scope);
            if (nullable) {
                cstmt.setShort(6, (short) SQL_NULLABLE);
            } else {
                cstmt.setShort(6, (short) SQL_NO_NULLS);
            }
            cstmt.setString(7,
                "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            cstmt.execute();

            ResultSet rs = cstmt.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cstmt.close();

            return rs;

    }



    /**
    Returns the catalog name available in this database.  This
    will return a ResultSet with a single row, whose value is
    the IBM i system name.

    @return      The ResultSet containing the IBM i system name.

    @exception  SQLException    If the connection is not open
                                or an error occurs.
    **/
    public ResultSet getCatalogs ()
    throws SQLException
    {


            CallableStatement cstmt = connection_.prepareCall("CALL SYSIBM" + getCatalogSeparator() + "SQLTABLES(?,?,?,?,?)");

            cstmt.setString(1, "%");
            cstmt.setString(2, "%");
            cstmt.setString(3, "%");
            cstmt.setString(4, "%");
            cstmt.setString(5, "DATATYPE='JDBC';GETCATALOGS=1;CURSORHOLD=1");
            cstmt.execute();
            ResultSet rs = cstmt.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cstmt.close();

            return rs;
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
	catalogSeparator = ".";
        return catalogSeparator;
    }



    /**
    Returns the DB2 for IBM i SQL term for "catalog".

    @return     The term "Database".

    @exception  SQLException    This exception is never thrown.
    **/
    public String getCatalogTerm ()
    throws SQLException
    {
        return "Database";
    }



    /**
    Returns a description of the access rights for a table's columns.

    @param  catalog         The catalog name. If null is specified, this parameter
                            is ignored.  If empty string is specified,
                            an empty result set is returned.
    @param  schema          The schema name. If null is specified, the
                            default SQL schema specified in the URL is used.
                            If null is specified and a default SQL schema was not
                            specified in the URL, the first library specified
                            in the libraries properties file is used.
                            If null is specified and a default SQL schema was
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

            // Set the column name and search pattern
            // If null, do not set parameter. The system default
            // value of *ALL is used.

            CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator () + "SQLCOLPRIVILEGES (?, ?, ?, ?, ?)");

            cstmt.setString(1, normalize(catalog));
            cstmt.setString(2, normalize(schema));
            cstmt.setString(3, normalize(table));
            cstmt.setString(4, normalize(columnPattern));
            cstmt.setObject(5, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            ResultSet rs = cstmt.executeQuery();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cstmt.close();

            return rs;

    }




    /**
    Returns a description of the table's columns available in a
    catalog.

    @param  catalog         The catalog name. If null is specified, this parameter
                            is ignored.  If empty string is specified,
                            an empty result set is returned.
    @param  schemaPattern   The schema name pattern.
                            If the "metadata source" connection property is set to 0
                            and null is specified, no value is sent to the system and
                            the default of *USRLIBL is used.
                            If the "metadata source" connection property is set to 1
                            and null is specified, then information from all schemas
                            will be returned.
                            If an empty string
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





	CallableStatement cs = connection_.prepareCall("CALL SYSIBM" + getCatalogSeparator() + "SQLCOLUMNS(?,?,?,?,?)");

	cs.setString(1, normalize(catalog));
	cs.setString(2, normalize(schemaPattern));
	cs.setString(3, normalize(tablePattern));
	cs.setString(4, normalize(columnPattern));

	if (javaVersion > 16) {
	    cs.setString(5, "DATATYPE='JDBC';JDBCVER='4.1';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
	} else if (javaVersion > 15)  {
	    cs.setString(5, "DATATYPE='JDBC';JDBCVER='4.0';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
	} else {
	    cs.setString(5, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
	}
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)

                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;

    }  // End of getColumns



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
                             default SQL schema specified in the URL is used.
                             If null is specified and a default SQL schema was not
                             specified in the URL, the first library specified
                             in the libraries properties file is used.
                             If null is specified,a default SQL schema was
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
                             default SQL schema specified in the URL is used.
                             If null is specified and a default SQL schema was not
                             specified in the URL, the first library specified
                             in the libraries properties file is used.
                             If null is specified, a default SQL schema was
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

    //-------------------------------------------------
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


            CallableStatement cs = connection_.prepareCall(
              "CALL SYSIBM"+ getCatalogSeparator() +"SQLFOREIGNKEYS(?,?,?,?,?,?,?)");

            cs.setString(1, normalize(primaryCatalog));
            cs.setString(2, normalize(primarySchema));
            cs.setString(3, normalize(primaryTable));
            cs.setString(4, normalize(foreignCatalog));
            cs.setString(5, normalize(foreignSchema));
            cs.setString(6, normalize(foreignTable));
            cs.setString(7, "DATATYPE='JDBC';EXPORTEDKEY=0;IMPORTEDKEY=0;DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;

    }  // End of getCrossReference



    /**
    Returns the major version number of the database.

    @return     The major version number.
    @since Modification 5
    **/
    public int getDatabaseMajorVersion ()
    {

        int defaultVersion = 0; //since we do not want to change signature to throw exception, have default as 0
        try
        {
            String v = getDatabaseProductVersion();
            int dotIndex = v.indexOf('.');
            if (dotIndex > 0)
            {
                v = v.substring(0,dotIndex);
                defaultVersion = Integer.parseInt(v);
            }
        }catch(Exception e)
        {
            //should not happen
        }

        return defaultVersion;
    }


    /**
    Returns the minor version number of the database.

    @return     The minor version number.
    @since Modification 5
    **/
    public int getDatabaseMinorVersion ()
    {
        int defaultVersion = 0;
        try
        {
            String v = getDatabaseProductVersion();
            int dotIndex = v.indexOf('.');
            if (dotIndex > 0)
            {
                v = v.substring(dotIndex+1);
                dotIndex = v.indexOf('.');
                if (dotIndex > 0)
                {
                    v = v.substring(0,dotIndex);
                }

                defaultVersion = Integer.parseInt(v);

            }
        }catch(Exception e)
        {
            //should not happen
        }
        return defaultVersion;

    }


    /**
  Returns the name of this database product.

  @return     The database product name.

  @exception  SQLException    This exception is never thrown.
  **/
    public String getDatabaseProductName ()
    throws SQLException
    {
        return JDBCDriver.DATABASE_PRODUCT_NAME_; // @D2C
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
        int v, r, m;
        int vrm = connection_.getServerVersion ();
        v = (vrm & 0xffff0000) >>> 16;                   // @D1C
        r = (vrm & 0x0000ff00) >>>  8;                   // @D1C
        m = (vrm & 0x000000ff);                          // @D1C

        StringBuffer buffer = new StringBuffer ();
        if (v < 10)  buffer.append("0");
        buffer.append(v);
        buffer.append (".");
        if (r < 10)  buffer.append("0");
        buffer.append(r);
        buffer.append (".");
        if (m < 1000) buffer.append("0");
        if (m < 100) buffer.append("0");
        if (m < 10)  buffer.append("0");
        buffer.append(m);
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
    	// Default value for other drivers.
    	return Connection.TRANSACTION_READ_UNCOMMITTED;
    }



    /**
    Returns the major version number for this JDBC driver.

    @return     The major version number.
    **/
    public int getDriverMajorVersion ()
    {
        return JDBCDriver.MAJOR_VERSION_;
    }



    /**
    Returns the minor version number for this JDBC driver.

    @return     The minor version number.
    **/
    public int getDriverMinorVersion ()
    {
        return JDBCDriver.MINOR_VERSION_;
    }



    /**
    Returns the name of this JDBC driver.

    @return     The driver name.

    @exception  SQLException    This exception is never thrown.
    **/
    public String getDriverName ()
    throws SQLException
    {
        return JDBCDriver.DRIVER_NAME_; // @D2C
    }



    /**
    Returns the version of this JDBC driver.

    @return     The driver version.

    @exception  SQLException    This exception is never thrown.
    **/
    public String getDriverVersion ()
    throws SQLException
    {
        return JDBCDriver.MAJOR_VERSION_+ "."+ JDBCDriver.MINOR_VERSION_;
    }



    /**
    Returns a description of the foreign key columns that
    reference a table's primary key columns.  This is the
    foreign keys exported by a table.

    @param  catalog        The catalog name. If null is specified, this parameter
                           is ignored.  If empty string is specified,
                           an empty result set is returned.
    @param  schema         The schema name. If null is specified, the
                           default SQL schema specified in the URL is used.
                           If null is specified and a default SQL schema was not
                           specified in the URL, the first library specified
                           in the libraries properties file is used.
                           If null is specified, a default SQL schema was
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



            CallableStatement cs = connection_.prepareCall("CALL SYSIBM" + getCatalogSeparator() + "SQLFOREIGNKEYS(?,?,?,?,?,?,?)");

            cs.setString(1, normalize(catalog));
            cs.setString(2, normalize(schema));
            cs.setString(3, normalize(table));
            cs.setString(4, normalize(catalog));
            cs.setString(5, EMPTY_STRING);
            cs.setString(6, EMPTY_STRING);
            cs.setString(7, "DATATYPE='JDBC';EXPORTEDKEY=1; CURSORHOLD=1");
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;
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
                           default SQL schema specified in the URL is used.
                           If null is specified and a default SQL schema was not
                           specified in the URL, the first library specified
                           in the libraries properties file is used.
                           If null is specified, a default SQL schema was
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




            CallableStatement cs = connection_.prepareCall("CALL SYSIBM"+ getCatalogSeparator() +"SQLFOREIGNKEYS(?,?,?,?,?,?,?)");

            cs.setString(1, normalize(catalog));
            cs.setString(2, null);
            cs.setString(3, null);
            cs.setString(4, normalize(catalog));
            cs.setString(5, normalize(schema));
            cs.setString(6, normalize(table));
            cs.setString(7, "DATATYPE='JDBC';IMPORTEDKEY=1; CURSORHOLD=1");
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;

    }



    /**
    Returns a description of a table's indexes and statistics.

    @param  catalog      The catalog name. If null is specified, this parameter
                         is ignored.  If empty string is specified,
                         an empty result set is returned.
    @param  schema       The schema name. If null is specified, the
                         default SQL schema specified in the URL is used.
                         If null is specified and a default SQL schema was not
                         specified in the URL, the first library specified
                         in the libraries properties file is used.
                         If null is specified, a default SQL schema was
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



        	short iUnique;
        	short reserved = 0;

        	if (unique)
        		iUnique = 0;
        	else
        		iUnique = 1;

            //Set the library name
        	if(schema != null)
                schema = normalize(schema);

            // Set the table name
        	if(table != null)
                table = normalize(table);

        	/*
        	  sysibm.SQLStatistics(
               CatalogName     varchar(128),
               SchemaName      varchar(128),
               TableName       varchar(128),
               Unique          Smallint,
               Reserved        Smallint,
               Options         varchar(4000))
        	 */
        	CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator () + "SQLSTATISTICS(?,?,?,?,?,?)");

        	cstmt.setString(1, normalize(catalog));
        	cstmt.setString(2, normalize(schema));
        	cstmt.setString(3, normalize(table));
        	cstmt.setShort(4,  iUnique);
        	cstmt.setShort(5,  reserved);
        	cstmt.setString(6, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
        	cstmt.execute();

            ResultSet rs = cstmt.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cstmt.close();

            return rs;


    } // End of getIndexInfo



    /**
    Returns the JDBC major version number.

    @return     The JDBC major version number.

    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public int getJDBCMajorVersion ()
    throws SQLException
    {
    	if (javaVersion >= 16) {
    		return 4;
    	} else {
    		return 3;
    	}
    }



    /**
    Returns the JDBC minor version number.

    @return     The JDBC minor version number.

    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public int getJDBCMinorVersion ()
    throws SQLException
    {
    	if (javaVersion >= 17) {
    		return 1;
    	} else {
    		return 0;
    	}
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
        if(connection_.getServerVersion() >= SystemInfo.VERSION_540)
            return 128;
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
        if(connection_.getServerVersion() >= SystemInfo.VERSION_610)
            return 8000;
        else
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
        if(connection_.getServerVersion() >= SystemInfo.VERSION_610)
            return 128;
        else
            return 18;
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
        if(connection_.getServerVersion() >= SystemInfo.VERSION_710)
            return 128;
        else
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
        if(connection_.getServerVersion() >= SystemInfo.VERSION_540)
            return 1048576;
        else
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
        return 9999;
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
        if (connection_.getServerVersion() >= SystemInfo.VERSION_540)
            return 1000;
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
            return "abs,acos,asin,atan,atan2,ceiling,cos,cot,degrees,exp,floor,log,log10,mod,pi,power,radians,rand,round,sin,sign,sqrt,tan,truncate";
    }



    /**
    Returns a description of the primary key columns.
    @param  catalog      The catalog name. If null is specified, this parameter
                         is ignored.  If empty string is specified,
                         an empty result set is returned.
    @param  schema       The schema name. If null is specified, the
                         default SQL schema specified in the URL is used.
                         If null is specified and a default SQL schema was not
                         specified in the URL, the first library specified
                         in the libraries properties file is used.
                         If null is specified, a default SQL schema was
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


            CallableStatement cs = connection_.prepareCall("CALL SYSIBM"+ getCatalogSeparator () +"SQLPRIMARYKEYS(?,?,?,?)");

            cs.setString(1, normalize(catalog));
            cs.setString(2, normalize(schema));
            cs.setString(3, normalize(table));
            cs.setString(4, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;
    }



    /**
    Returns a description of a catalog's stored procedure
    parameters and result columns.
    <p> Note:  For this function to work with procedure names
    longer than 10 characters, the metadata source=1 property must be used on the connection.
    This is the default when connecting to a V7R1 or later system.


    @param  catalog            The catalog name. If null is specified, this parameter
                               is ignored.  If empty string is specified,
                               an empty result set is returned.
    @param  schemaPattern      The schema name pattern.   If null is specified,
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

            CallableStatement cs = connection_.prepareCall("CALL SYSIBM"+ getCatalogSeparator () + "SQLPROCEDURECOLS(?,?,?,?,?)");

            cs.setString(1, normalize(catalog));
            cs.setString(2, normalize(schemaPattern));
            cs.setString(3, normalize(procedurePattern));
            cs.setString(4, normalize(columnPattern));
            if (javaVersion >= 16) {
            	cs.setString(5, "DATATYPE='JDBC';JDBCVER='4.0';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            } else {
            	cs.setString(5, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            }
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;


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

            CallableStatement cs = connection_.prepareCall("CALL SYSIBM"+ getCatalogSeparator () + "SQLPROCEDURES(?,?,?,?)");

            cs.setString(1, normalize(catalog));
            cs.setString(2, normalize(schemaPattern));
            cs.setString(3, normalize(procedurePattern));
            cs.setString(4, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;

    }



    /**
    Returns the DB2 for IBM i SQL term for "procedure".

    @return     The term for "procedure".

    @exception  SQLException    This exception is never thrown.
    **/
    public String getProcedureTerm ()
    throws SQLException
    {
        return "Procedure";
    }




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
        return JDBCResultSet.HOLD_CURSORS_OVER_COMMIT;
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

            CallableStatement cs = connection_.prepareCall("CALL SYSIBM"+ getCatalogSeparator () + "SQLTABLES(?,?,?,?,?)");

            cs.setString(1, "%");
            cs.setString(2, "%");
            cs.setString(3, "%");
            cs.setString(4, "%");
            cs.setString(5, "DATATYPE='JDBC';GETSCHEMAS=1;CURSORHOLD=1");
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;
    }



    /**
    Returns the DB2 for IBM i SQL term for "schema".

    @return     The term for schema.

    @exception  SQLException    This exception is never thrown.
    **/
    public String getSchemaTerm ()
    throws SQLException
    {
        return "Library";
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
        return "\\";
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
        return JDBCDatabaseMetaData.sqlStateSQL99;
    }



    /**
    Returns the list of supported string functions.

    @return     The list of supported string functions, separated by commas.

    @exception  SQLException    This exception is never thrown.
    **/
    public String getStringFunctions ()
    throws SQLException
    {
        return "char,concat,difference,insert,lcase,left,length,locate,ltrim,repeat,replace,right,rtrim,soundex,space,substring,ucase";
    }




    /**
    Returns a ResultSet containing descriptions of the table hierarchies
    in a schema.

    This method only applies to the attributes of a
    structured type.  Distinct types are stored in the datatypes
    catalog, not the attributes catalog.  Since DB2 for IBM i does not support
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




    /**
    Returns a ResultSet containing descriptions of user-defined type hierarchies
    in a schema.

    This method only applies to the attributes of a
    structured type.  Distinct types are stored in the datatypes
    catalog, not the attributes catalog. Since DB2 for IBM i does not support
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
        return "database,ifnull,user";
    }



    /**
    Returns the description of the access rights for each table
    available in a catalog.  Note that a table privilege applies
    to one or more columns in a table.

    @param  catalog             The catalog name. If null is specified, this parameter
                                is ignored.  If empty string is specified,
                                an empty result set is returned.
    @param  schemaPattern       The schema name pattern.
                                If the "metadata source" connection property is set to 0
                                and null is specified, no value is sent to the system and
                                the default of *USRLIBL is used.
                                If the "metadata source" connection property is set to 1
                                and null is specified, then information from all schemas
                                will be returned.

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

            CallableStatement cs = connection_.prepareCall("CALL SYSIBM"+ getCatalogSeparator () + "SQLTABLEPRIVILEGES(?,?,?,?)");

            cs.setString(1, normalize(catalog));
            cs.setString(2, normalize(schemaPattern));
            cs.setString(3, normalize(tablePattern));
            cs.setString(4, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;
    }



    /**
    Returns the description of the tables available in a catalog.

    @param  catalog        The catalog name. If null is specified, this parameter
                           is ignored.  If empty string is specified,
                           an empty result set is returned.
    @param  schemaPattern  The schema name pattern.
                           If the "metadata source" connection property is set to 0
                           and null is specified, no value is sent to the system and
                           the default of *USRLIBL is used.
                           If the "metadata source" connection property is set to 1
                           and null is specified, then information from all schemas
                           will be returned.  If an empty string
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


       // Verify that a connection
        // is available for use. Exception
        // is thrown if not available

            // Handle processing the array of table types.
            //bite the bullet and follow Native JDBC logic
            boolean rsEmpty = false;
            String typeString = EMPTY_STRING;
            if (!rsEmpty) {
                int i;
                int stringsInList = 0;

                if (tableTypes != null) {
                    for (i = 0; i < tableTypes.length; i++) {
                        String check = tableTypes[i];

                        if ((check.equalsIgnoreCase(VIEW))  ||
                                (check.equalsIgnoreCase(TABLE)) ||
                                (check.equalsIgnoreCase(SYSTEM_TABLE)) ||
                                (check.equalsIgnoreCase(ALIAS)) ||
                                (check.equalsIgnoreCase(SYNONYM)) ||
                                (check.equalsIgnoreCase(MQT)))
                        {

                            if (check.equalsIgnoreCase(SYNONYM)) {
                                check = ALIAS;
                            }
                            stringsInList++;
                            if (stringsInList > 1)
                                typeString = typeString.concat(",");
                            typeString = typeString.concat(check);
                        }
                    }

                    // If there were no valid types, ensure an empty result set.
                    if (stringsInList == 0)
                        rsEmpty = true;
                }
            }

            // If an empty result set is to be generated, produce the values to
            // do so here.
            if (rsEmpty) {
                schemaPattern = FAKE_VALUE;
                tablePattern = FAKE_VALUE;
                typeString = typeString.concat(TABLE);
            }


            CallableStatement cs = connection_.prepareCall("CALL SYSIBM" + getCatalogSeparator ()
                    + "SQLTABLES(?,?,?,?,?)");

            cs.setString(1, normalize(catalog));
            cs.setString(2, normalize(schemaPattern));
            cs.setString(3, normalize(tablePattern));
            cs.setString(4, normalize(typeString));
            cs.setString(5,
                    "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;

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


          CallableStatement cs = connection_.prepareCall("CALL SYSIBM" + getCatalogSeparator() + "SQLTABLES(?,?,?,?,?)");

          cs.setString(1, "%");
          cs.setString(2, "%");
          cs.setString(3, "%");
          cs.setString(4, "%");
          cs.setString(5, "DATATYPE='JDBC';GETTABLETYPES=1;CURSORHOLD=1");
          cs.execute();
          ResultSet rs = cs.getResultSet();
          if(rs != null)
              ((JDBCResultSet)rs).isMetadataResultSet_ = true;
          else
              cs.close();

          return rs;

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
        return "curdate,curtime,dayname,dayofmonth,dayofweek,dayofyear,hour,minute,month,monthname,now,quarter,second,timestampdiff,week,year";
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


            PreparedStatement cs = connection_.prepareStatement("CALL SYSIBM" +getCatalogSeparator() + "SQLGETTYPEINFO(?,?)");

            cs.setShort(1, (short) SQL_ALL_TYPES);
            if (javaVersion >= 16) {
            cs.setString(2, "DATATYPE='JDBC';JDBCVER='4.0';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            } else {
            cs.setString(2, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            }
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;


    }



    // JDBC 2.0
    /**
    Returns the description of the user-defined types
    available in a catalog.

    @param  catalog         The catalog name. If null is specified, this parameter
                            is ignored.  If empty string is specified,
                            an empty result set is returned.
    @param  schemaPattern   The schema name pattern.
                            If the "metadata source" connection property is set to 0
                            and null is specified, no value is sent to the system and
                            the default of *USRLIBL is used.
                            If the "metadata source" connection property is set to 1
                            and null is specified, then information from all schemas
                            will be returned.
                            If an empty string
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





            CallableStatement cs = connection_.prepareCall("CALL SYSIBM" + getCatalogSeparator()+ "SQLUDTS(?,?,?,?,?)");

            cs.setString(1, normalize(catalog));
            cs.setString(2, normalize(schemaPattern));
            cs.setString(3, normalize(typeNamePattern));
            StringBuffer typesStringBuffer = new StringBuffer();
            int stringsInList = 0;

            if (types != null) {
                for (int i = 0; i < types.length; i++) {
                    if (stringsInList > 0) {
                        typesStringBuffer.append(",");
                    }
                    typesStringBuffer.append(types[i]);
                    stringsInList++;
                }
            }

            cs.setString(4, typesStringBuffer.toString());
            if (javaVersion >= 16) {
            	cs.setString(5, "DATATYPE='JDBC';JDBCVER='4.0';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            } else {
            	cs.setString(5, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            }
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;
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

        return connection_.getUserName();
    }



    /**
    Returns a description of a table's columns that are automatically
    updated when any value in a row is updated.
    @param  catalog        The catalog name. If null is specified, this parameter
                           is ignored.  If empty string is specified,
                           an empty result set is returned.
    @param  schema         The schema name. If null is specified, the
                           default SQL schema specified in the URL is used.
                           If null is specified and a default SQL schema was not
                           specified in the URL, the first library specified
                           in the libraries properties file is used.
                           If null is specified, a default SQL schema was
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




            CallableStatement cs =   connection_.prepareCall("CALL SYSIBM" + getCatalogSeparator() + "SQLSPECIALCOLUMNS(?,?,?,?, ?,?,?)");

            cs.setShort(1, (short) SQL_ROWVER);
            cs.setString(2, normalize(catalog));
            cs.setString(3, normalize(schema));
            cs.setString(4, normalize(table));
            cs.setShort(5, (short) 0);
            cs.setShort(6, (short) 1);
            cs.setString(7, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
            cs.execute();

            ResultSet rs = cs.getResultSet();
            if(rs != null)
                ((JDBCResultSet)rs).isMetadataResultSet_ = true;
            else
                cs.close();

            return rs;
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
    Indicates if the database is in read-only mode.

    @return     true if in read-only mode; false otherwise.

    @exception  SQLException    If the connection is not open
                                or an error occurs.
    **/
    public boolean isReadOnly ()
    throws SQLException
    {

        return connection_.isReadOnly ();
    }



    /**
    Indicates if updateable LOB methods update a copy of the LOB or if updates
    are made directly to the LOB.  True is returned if updateable lob methods
    update a copy of the LOB, false is returned if updates are made directly
    to the LOB.

    @return     Always true.    Updateable lob methods update a copy of the LOB.
    ResultSet.updateRow() must be called to update the LOB in the DB2 for IBM i database.

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
        if(mixedCaseName == null)
            return null;

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

    @return     Always false. The database does not treat mixed case, quoted
                SQL identifiers as case insensitive and store them in
                mixed case.

    @exception  SQLException    This exception is never thrown.
    **/
    public boolean storesMixedCaseQuotedIdentifiers ()
    throws SQLException
    {
        // @A2C changed from false to true
        return false;
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
        return false;
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
        return false;
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

    @return     Always true. Expression in ORDER BY lists are supported.

    @exception  SQLException    This exception is never thrown.
    **/
    public boolean supportsExpressionsInOrderBy ()
    throws SQLException
    {
        return true;
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
        if(connection_.getServerVersion() >= SystemInfo.VERSION_610)
            return true;
        else
            return false;
    }




    /**
    Indicates if, after a statement is executed, auto-generated keys can be retrieved
    using the method Statement.getGeneratedKeys().

    @return     True if the user is connecting to a system running OS/400 V5R2
    or IBM i, otherwise false.  Auto-generated keys are supported
    only if connecting to a system running OS/400 V5R2 or IBM i.

    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public boolean supportsGetGeneratedKeys ()
    throws SQLException
    {
    	return true;
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
	public boolean supportsResultSetConcurrency(int resultSetType,
			int resultSetConcurrency) throws SQLException {
		// Validate the result set type.
		if (supportsResultSetType(resultSetType)) {
			// jtopenlite only supports READONLY cursors
			if ((resultSetConcurrency == ResultSet.CONCUR_READ_ONLY)) {
				return true;
			} if ( resultSetConcurrency == ResultSet.CONCUR_UPDATABLE) {
				return false;
			} else {
				JDBCError.throwSQLException (JDBCError.EXC_CONCURRENCY_INVALID);
				return false;
			}
		} else {
			return false;
		}
	}




    /**
    Indicates if a type of result set holdability is supported.  The two
    types are ResultSet.HOLD_CURSORS_OVER_COMMIT and ResultSet.CLOSE_CURSORS_AT_COMMIT.

    @return     True if the user is connecting to a system running OS/400
    V5R2 or IBM i, otherwise false.  Both types of result set
    holidability are supported if connecting to OS/400 V5R2 or IBM i.

    @exception  SQLException    This exception is never thrown.
    @since Modification 5
    **/
    public boolean supportsResultSetHoldability (int resultSetHoldability)
    throws SQLException
    {
            return true;
    }




    /**
    Indicates if savepoints are supported.

    @return     False.  The toolboxlite driver does not support savepoints.
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
            	return true;
            case ResultSet.TYPE_SCROLL_SENSITIVE:
            case ResultSet.TYPE_SCROLL_INSENSITIVE:
                return false;
            default:
            	throw new SQLException("resultSetType invalid: "+resultSetType);
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
            && (transactionIsolationLevel != Connection.TRANSACTION_SERIALIZABLE)) {
        	throw new SQLException("transactionIsolationLevel invalid : "+transactionIsolationLevel);
        }

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
    	// Not supported in toolbox lite driver
    	throw new NotImplementedException();
    }



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

        CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator() + "SQLTABLES  (?, ?, ?, ?, ?)");

        cstmt.setString(1, normalize(catalog));
        cstmt.setString(2, normalize(schemaPattern));
        cstmt.setString(3, "%");
        cstmt.setString(4, "%");
        cstmt.setObject(5, "DATATYPE='JDBC';GETSCHEMAS=2;CURSORHOLD=1");
        cstmt.execute();
        ResultSet rs = cstmt.getResultSet();
        if(rs != null)
            ((JDBCResultSet)rs).isMetadataResultSet_ = true;
        else
            cstmt.close();

        return rs;
    }


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

        CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator() + "SQLFUNCTIONS  ( ?, ?, ?, ?)");

        cstmt.setString(1, normalize(catalog));
        cstmt.setString(2, normalize(schemaPattern));
        cstmt.setString(3, normalize(functionNamePattern));
        cstmt.setObject(4, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
        cstmt.execute();
        ResultSet rs = cstmt.getResultSet();
        if(rs != null)
            ((JDBCResultSet)rs).isMetadataResultSet_ = true;
        else
            cstmt.close();

        return rs;
    }


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

        CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator() + "SQLFUNCTIONCOLS  ( ?, ?, ?, ?, ?)");

        cstmt.setString(1, normalize(catalog));
        cstmt.setString(2, normalize(schemaPattern));
        cstmt.setString(3, normalize(functionNamePattern));
        cstmt.setString(4, normalize(columnNamePattern));
        if (javaVersion > 16) {
        cstmt.setObject(5, "DATATYPE='JDBC';JDBCVER='4.0';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1"); //
        } else {
        	cstmt.setObject(5, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
        }
        cstmt.execute();
        ResultSet rs = cstmt.getResultSet();
        if(rs != null)
            ((JDBCResultSet)rs).isMetadataResultSet_ = true;
        else
            cstmt.close();

        return rs;
    }


    // JDBC 4.1
    /**
     * Retrieves whether a generated key will always be returned if the column name(s) or index(es)
     * specified for the auto generated key column(s) are valid and the statement succeeds.
     * @return true if so; false otherwise
     * @exception SQLException - if a database access error occurs
     */

    public boolean generatedKeyAlwaysReturned() throws SQLException {
      return false;
    }



    // JDBC 4.1
    /**
     * Retrieves a description of the pseudo or hidden columns available in a given table within the specified
     * catalog and schema. Pseudo or hidden columns may not always be stored within a table and are not
     * visible in a ResultSet unless they are specified in the query's outermost SELECT list. Pseudo or hidden
     * columns may not necessarily be able to be modified. If there are no pseudo or hidden columns, an empty
     * ResultSet is returned.
     * <p>Only column descriptions matching the catalog, schema, table and column name criteria are returned.
     * They are ordered by TABLE_CAT,TABLE_SCHEM, TABLE_NAME and COLUMN_NAME.
     * <p>Each column description has the following columns:
     * <ol>
     * <li>TABLE_CAT String => table catalog (may be null)
     * <li>TABLE_SCHEM String => table schema (may be null)
     * <li>TABLE_NAME String => table name
     * <li>COLUMN_NAME String => column name
     * <li>DATA_TYPE int => SQL type from java.sql.Types
     * <li>COLUMN_SIZE int => column size.
     * <li>DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable.
     * <li>NUM_PREC_RADIX int => Radix (typically either 10 or 2)
     * <li>COLUMN_USAGE String => The allowed usage for the column. The value returned will correspond to the enum name returned by PseudoColumnUsage.name()
     * <li>REMARKS String => comment describing column (may be null)
     * <li>CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column
     * <li>IS_NULLABLE String => ISO rules are used to determine the nullability for a column.
     * <ul>
     * <li>YES --- if the column can include NULLs
     * <li>NO --- if the column cannot include NULLs
     * <li>empty string --- if the nullability for the column is unknown
     * </ul>
     * </ol>
     * <p> The COLUMN_SIZE column specifies the column size for the given column. For numeric data, this is the
     * maximum precision. For character data, this is the length in characters. For datetime datatypes,
     * this is the length in characters of the String representation (assuming the maximum allowed precision of the
     * fractional seconds component). For binary data, this is the length in bytes. For the ROWID datatype,
     * this is the length in bytes. Null is returned for data types where the column size is not applicable.
     *
     * @param catalog - a catalog name; must match the catalog name as it is stored in the database;
     * "" retrieves those without a catalog; null means that the catalog name should not be used to narrow the search
     * @param schemaPattern - a schema name pattern; must match the schema name as it is stored in the database;
     * "" retrieves those without a schema; null means that the schema name should not be used to narrow the search
     * @param tableNamePattern - a table name pattern; must match the table name as it is stored in the database
     * @param columnNamePattern - a column name pattern; must match the column name as it is stored in the database
     * @return  ResultSet - each row is a column description
     * @exception  SQLException - if a database access error occurs
     * @see PseudoColumnUsage
     */

    public ResultSet getPseudoColumns(String catalog, String schemaPattern,
        String tableNamePattern, String columnNamePattern)
        throws SQLException {


      CallableStatement cstmt = connection_.prepareCall("call SYSIBM" + getCatalogSeparator() + "SQLPSEUDOCOLUMNS  ( ?, ?, ?, ?, ?)");

      cstmt.setString(1, normalize(catalog));
      cstmt.setString(2, normalize(schemaPattern));
      cstmt.setString(3, normalize(tableNamePattern));
      cstmt.setString(4, normalize(columnNamePattern));
      if (javaVersion >= 16) {
         cstmt.setObject(5, "DATATYPE='JDBC';JDBCVER='4.0';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
      } else {
    	  cstmt.setObject(5, "DATATYPE='JDBC';DYNAMIC=0;REPORTPUBLICPRIVILEGES=1;CURSORHOLD=1");
      }
      cstmt.execute();
      ResultSet rs = cstmt.getResultSet();
      if(rs != null)
          ((JDBCResultSet)rs).isMetadataResultSet_ = true;
      else
          cstmt.close();

      return rs;
    }








}
