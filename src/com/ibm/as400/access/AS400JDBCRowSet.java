///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCRowSet.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.MalformedURLException;      //@G4A JDBC 3.0
import java.net.URL;                        //@G4A JDBC 3.0
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Hashtable;  //@A2A
import java.util.Map;

/**
*  The AS400JDBCRowSet class represents a connected rowset that encapsulates an JDBC result set.
*  The database connection is maintained while in use.
*  <P>
*  Either the url or dataSourceName property must be set to specify how the database connection
*  is established.  The command property must be set to specify how to create the PreparedStatement.
*
*  <p>This example creates an AS400JDBCRowSet object, populates it, and then updates its contents.
*  <pre><blockquote>
*  DriverManager.registerDriver(new AS400JDBCDriver());
*  AS400JDBCRowSet rowset = new AS400JDBCRowSet("jdbc:as400://mySystem","myUser", "myPassword");
*
*  // Set the command used to populate the list.
*  rowset.setCommand("SELECT * FROM MYLIB.DATABASE");
*
*  // Populate the rowset.
*  rowset.execute();
*
*  // Update the customer balances.
*  while (rowset.next())
*  {
*     double newBalance = rowset.getDouble("BALANCE") + july_statements.getPurchases(rowset.getString("CUSTNUM"));
*     rowset.updateDouble("BALANCE", newBalance);
*     rowset.updateRow();
*  }
*  </blockquote></pre>
*
*  <p>This example creates an AS400JDBCRowSet object, sets the data source and command parameters and then
*  populates it.
*
*  <pre><blockquote>
*  // Get the data source that is registered in JNDI (assumes JNDI environment is set).
*  Context context = new InitialContext();
*  AS400JDBCDataSource dataSource = (AS400JDBCDataSource) context.lookup("jdbc/customer");
*
*  AS400JDBCRowSet rowset = new AS400JDBCRowSet();
*  rowset.setDataSourceName("jdbc/customer");
*  rowset.setUsername("myuser");
*  rowset.setPassword("myPasswd");
*
*  // Set the prepared statement and initialize the parameters.
*  rowset.setCommand("SELECT * FROM MYLIBRARY.MYTABLE WHERE STATE = ? AND BALANCE > ?");
*  rowset.setString(1, "MINNESOTA");
*  rowset.setDouble(2, MAXIMUM_LIMIT);
*
*  // Populate the rowset.
*  rowset.execute();
*  </blockquote></pre>
*
*  <P>AS400JDBCRowSet objects generate the following events:
*  <ul>
*    <li>RowSetEvent</a> - The events fired are:
*      <ul>
*       <li>cursorMoved</li>
*       <li>rowChanged</li>
*       <li>rowSetChanged</li>
*       </ul>
*    </li>
*  </ul>
**/
public class AS400JDBCRowSet extends Object implements RowSet, Serializable             // @A3C
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



    private static final String className_ = "AS400JDBCRowSet";
    private String command_;                        // The command used to create the result set.
    private String dataSourceName_;                 // The name of the data source.
    private boolean useDataSource_ = true;          // Whether the dataSource specified is used.
    private String url_;                            // The user defined URL used to make the connection.
    private String username_;                           // The user name used to make the connection.
    private String password_;                       // The password used to make the connection.

    // Toolbox classes.
    private Connection connection_;             // The JDBC connection.
    private DataSource dataSource_;             // The dataSource used to make the connection.
    private PreparedStatement statement_;       // The prepared statement.
    private AS400JDBCResultSet resultSet_;      // The result set.  @G4C
    private transient AS400JDBCRowSetEventSupport eventSupport_;    // RowSetListener support.  @A3C
    private Context context_ = null;  //@A1A    // The JNDI naming context which specifies how naming
    // and directory services are accessed.
    private Hashtable environment_ = null; //@A2A   // The jndi environment properties.

    // Connection properties.
    private boolean isReadOnly_ = false;
    private int transactionIsolation_ = Connection.TRANSACTION_READ_UNCOMMITTED;
    private Map typeMap_;

    // Statement properties.
    private boolean createNewStatement_ = true;
    private int concurrency_ = ResultSet.CONCUR_READ_ONLY;
    private boolean escapeProcessing_ = true;
    private int type_ = ResultSet.TYPE_FORWARD_ONLY;

    private transient PropertyChangeSupport changes_;  // @A3C

    /**
    *  Constructs a default AS400JDBCRowSet object.
    **/
    public AS400JDBCRowSet()
    {
        // @A3D eventSupport_ = new AS400JDBCRowSetEventSupport();
        initializeTransient();                                           // @A3A
    }

    /**
    *  Constructs an AS400JDBCRowset with the specified <i>dataSourceName</i>.
    *  @param dataSourceName The name of the data source used to make the connection.
    **/
    public AS400JDBCRowSet(String dataSourceName)
    {
        this();
        setDataSourceName(dataSourceName);
    }

    /**
    *  Constructs an AS400JDBCRowSet with the specified parameters.
    *  @param url The url used to make the connection.
    *  @param username The user name.
    *  @param password The password.
    **/
    public AS400JDBCRowSet(String url, String username, String password)
    {
        this();
        setUrl(url);
        setUsername(username);
        setPassword(password);
    }

    /**
    *  Positions the cursor to an absolute row number.
    *
    *  <p>Attempting to move beyond the first row positions the
    *  cursor before the first row. Attempting to move beyond the last
    *  row positions the cursor after the last row.
    *
    *  <p>If an InputStream from the current row is open, it is
    *  implicitly closed.  In addition, all warnings and pending updates
    *  are cleared.
    *
    *  @param  rowNumber   The absolute row number (1-based).  If the absolute row
    *                      number is positive, this positions the cursor
    *                      with respect to the beginning of the result set.
    *                      If the absolute row number is negative, this
    *                      positions the cursor with respect to the end
    *                      of result set.
    *  @return             true if the requested cursor position is
    *                      valid; false otherwise.
    *
    *  @exception SQLException  If the result set is not open,
    *                           the result set is not scrollable,
    *                           the row number is 0,
    *                           or an error occurs.
    */
    public boolean absolute (int rowNumber) throws SQLException
    {
        validateResultSet();
        boolean status = resultSet_.absolute(rowNumber);

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
        return status;
    }

    /**
    *  Adds a PropertyChangeListener.
    *  @param listener The PropertyChangeListener.
    *  @see #removePropertyChangeListener
    **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        changes_.addPropertyChangeListener(listener);
    }

    /**
    *  Adds a RowSetListener to the list.
    *  @param listener The RowSetListener object.
    **/
    public void addRowSetListener(RowSetListener listener)
    {
        eventSupport_.addRowSetListener(listener);
    }

    /**
    *  Positions the cursor after the last row.
    *  If an InputStream from the current row is open, it is implicitly closed.
    *  In addition, all warnings and pending updates are cleared.
    *
    *  @exception SQLException If the result set is not open, the result set is not scrollable, or an error occurs.
    **/
    public void afterLast () throws SQLException
    {
        validateResultSet();
        resultSet_.afterLast();

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
    }

    /**
    *  Positions the cursor before the first row.
    *  If an InputStream from the current row is open, it is implicitly closed.
    *  In addition, all warnings and pending updates are cleared.
    *
    *  @exception SQLException If the result set is not open, the result set is not scrollable, or an error occurs.
    **/
    public void beforeFirst () throws SQLException
    {
        validateResultSet();
        resultSet_.beforeFirst();

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
    }


    /**
    *  Cancels all pending updates that have been made since the last
    *  call to updateRow().  If no updates have been made or updateRow()
    *  has already been called, then this method has no effect.
    *
    *  @exception  SQLException    If the result set is not open
    *                              or the result set is not updatable.
    **/
    public void cancelRowUpdates () throws SQLException
    {
        validateResultSet();
        resultSet_.cancelRowUpdates();
    }

    /**
    *  Clears the columns for the current row and releases all associated resources.
    *  @exception SQLException If a database error occurs.
    **/
    public void clearParameters() throws SQLException
    {
        if (statement_ != null)
            statement_.clearParameters();
    }

    /**
    *  Clears all warnings that have been reported for the result set.
    *  After this call, getWarnings() returns null until a new warning
    *  is reported for the result set.
    *
    *  @exception SQLException If an error occurs.
    **/
    public void clearWarnings () throws SQLException
    {
        validateResultSet();
        resultSet_.clearWarnings();
    }

    /**
    *  Releases the rowset resources immediately instead of waiting for them to be automatically released.
    *  This closes the connection to the database.
    *
    *  @exception SQLException If an error occurs.
    **/
    public void close () throws SQLException
    {                  

        // @D4 change -- don't throw exceptions.  Instead, catch and log them.  The
        // user called close so we won't return until we tried to close all
        // resources.
        try
        {                                                                          // @J4a                                                                            // @J4a
            if (resultSet_ != null)
                resultSet_.close();
        }                                                                            // @J4a
        catch (SQLException e)                                                       // @J4a
        {                                                                            // @J4a
            if (JDTrace.isTraceOn())                                                  // @J4a
                JDTrace.logInformation (this, "Closing result set while closing the row set failed: " + e.getMessage()); // @j4a
        }                                                                            // @J4a



        try
        {                                                                          // @J4a                                                                            // @J4a
            if (statement_ != null)
                statement_.close();
        }                                                                            // @J4a
        catch (SQLException e)                                                       // @J4a
        {                                                                            // @J4a
            if (JDTrace.isTraceOn())                                                  // @J4a
                JDTrace.logInformation (this, "Closing statement set while closing the row set failed: " + e.getMessage()); // @j4a
        }                                                                            // @J4a



        if (connection_ != null)
            connection_.close();
    }



    /**
    *  Connects to the database.
    *  @exception SQLException If database errors creating the connection.
    **/
    private void connect() throws SQLException
    {
        if (JDTrace.isTraceOn ())
            JDTrace.logInformation (this, "connect()");

        if (isUseDataSource())
        {
            if (JDTrace.isTraceOn ())
                JDTrace.logInformation (this, "using JDBC DataSource");

            if (dataSourceName_ == null)
                throw new ExtendedIllegalStateException("dataSourceName", ExtendedIllegalStateException.PROPERTY_NOT_SET);

            try
            {
                if (context_ == null)                //@A1A
                {
                    if (environment_ == null)               //@A2A
                        context_ = new InitialContext();      //@A1C
                    else                          //@A2A
                        context_ = new InitialContext(environment_);    //@A2A
                }
                dataSource_ = (AS400JDBCDataSource)context_.lookup(dataSourceName_);
            }
            catch (NamingException ne)
            {
                if (JDTrace.isTraceOn ())
                {
                    JDTrace.logInformation(this, "Cannot find JNDI data source.");
                    ne.printStackTrace(DriverManager.getLogStream());
                }
                throw new ExtendedIllegalStateException("dataSourceName", ExtendedIllegalStateException.OBJECT_CANNOT_BE_FOUND);  //@A2C
            }
            connection_ = (AS400JDBCConnection)dataSource_.getConnection(username_, password_);
        }
        else
        {                          // Use the url to make the connection.
            if (JDTrace.isTraceOn ())
                JDTrace.logInformation (this, "using JDBC url");

            if (url_ == null)
                throw new ExtendedIllegalStateException("url", ExtendedIllegalStateException.PROPERTY_NOT_SET);

            if (username_ != null && password_ != null)
                connection_ = DriverManager.getConnection(url_, username_, password_);
            else
                connection_ = DriverManager.getConnection(url_);
        }

        // Set the connection properties.
        connection_.setReadOnly(isReadOnly_);
        if (transactionIsolation_ != Connection.TRANSACTION_READ_UNCOMMITTED)
            connection_.setTransactionIsolation(transactionIsolation_);
    }

    /**
    *  Creates a statement.
    *  @exception SQLException If a database error occurs creating the statement.
    **/
    private void createStatement() throws SQLException
    {
        if (JDTrace.isTraceOn ())
            JDTrace.logInformation (this, "createStatement()");

        if (command_ == null)
            throw new ExtendedIllegalStateException("command", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        if (connection_ == null)
            connect();

        // set the parameters.
        int fetchDirection=0, fetchSize=0, maxFieldSize=0, maxRows=0, queryTimeout=0;
        boolean setParameters = false;
        if (statement_ != null)
        {
            fetchDirection = statement_.getFetchDirection();
            fetchSize = statement_.getFetchSize();
            maxFieldSize = statement_.getMaxFieldSize();
            maxRows = statement_.getMaxRows();
            queryTimeout = statement_.getQueryTimeout();
            setParameters = true;
        }
        statement_ = (PreparedStatement)connection_.prepareStatement(command_, type_, concurrency_);

        if (setParameters)
        {
            statement_.setFetchDirection(fetchDirection);
            statement_.setFetchSize(fetchSize);
            statement_.setMaxFieldSize(maxFieldSize);
            statement_.setMaxRows(maxRows);
            statement_.setQueryTimeout(queryTimeout);
        }
        createNewStatement_ = false;
    }

    /**
    *  Deletes the current row from the result set and the database.
    *  After deleting a row, the cursor position is no longer valid,
    *  so it must be explicitly repositioned.
    *
    *  @exception SQLException If the result set is not open,
    *                          the result set is not updatable,
    *                          the cursor is not positioned on a row,
    *                          the cursor is positioned on the insert row,
    *                          or an error occurs.
    **/
    public void deleteRow () throws SQLException
    {
        validateResultSet();
        resultSet_.deleteRow();
    }

    /**
    *  Executes the command and fills the rowset with data.  Any previous contents are erased.
    *
    *  The following properties may be used to create a connection for reading data:
    *  <ul>
    *  <li>dataSource (Required if isUseDataSource = true)
    *  <li>url (Required if isUseDataSource = false)
    *  <li>user name <li>password <li>transaction isolation <li>type map
    *  </ul>
    *  The following properties may be used to create a statement to execute a command:
    *  <ul>
    *  <li>command (Required)
    *  <li>read only <li>maximum field size <li>maximum rows <li>escape processing <li>query timeout
    *  </ul>
    *
    *  @exception SQLException If a database error occurs.
    **/
    public void execute() throws SQLException
    {
        if (JDTrace.isTraceOn ())
            JDTrace.logInformation (this, "execute()");

        if (createNewStatement_)
            createStatement();

        if (command_.toUpperCase().indexOf("SELECT") != -1)        // Fix for JTOpen Bug 4121
        {
            resultSet_ = (AS400JDBCResultSet)statement_.executeQuery();  //@G4C

            // Notify the listeners.
            eventSupport_.fireRowSetChanged(new RowSetEvent(this));
        }
        else
        {
            statement_.executeUpdate();
        }

    }

    /**
    *  Closes the Statement and Connection.
    *  @exception SQLException If a database error occurs.
    **/
    protected void finalize() throws SQLException
    {
        try
        {
            close();
        }
        catch (SQLException e)
        {
            JDError.throwSQLException (JDError.EXC_SERVER_ERROR);
        }
    }

    /**
    *  Returns the column index for the specified column name.
    *
    *  @param      columnName      The column name.
    *  @return                     The column index (1-based).
    *
    *  @exception  SQLException    If the result set is not open
    *                              or the column name is not found.
    **/
    public int findColumn (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.findColumn(columnName);
    }

    /**
    *  Positions the cursor to the first row.
    *  If an InputStream from the current row is open, it is implicitly closed.
    *  In addition, all warnings and pending updates are cleared.
    *
    *  @return true if the requested cursor position is valid; false otherwise.
    *  @exception SQLException If the result set is not open, the result set is not scrollable, or an error occurs.
    **/
    public boolean first () throws SQLException
    {
        validateResultSet();
        boolean status = resultSet_.first();

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
        return status;
    }


    /**
    *  Returns the value of a column as an Array object.
    *  DB2 for i5/OS does not support arrays.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    Always thrown because DB2 for i5/OS does not support arrays.
    **/
    public Array getArray (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getArray(columnIndex);
    }

    /**
    *  Returns the value of a column as an Array object.
    *  DB2 for i5/OS does not support arrays.
    *
    *  @param  columnName    The column name.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    Always thrown because DB2 for i5/OS does not support arrays.
    **/
    public Array getArray (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getArray(columnName);
    }

    /**
    *  Returns the value of a column as a stream of ASCII
    *  characters.  This can be used to get values from columns
    *  with SQL types CHAR, VARCHAR, BINARY, VARBINARY, CLOB, and
    *  BLOB.  All of the data in the returned stream must be read
    *  prior to getting the value of any other column.  The next
    *  call to a get method implicitly closes the stream.
    *
    *  @param  columnIndex     The column index (1-based).
    *  @return                 The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              or the requested conversion is not valid.
    **/
    public InputStream getAsciiStream (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getAsciiStream(columnIndex);
    }

    /**
    *  Returns the value of a column as a stream of ASCII
    *  characters. This can be used to get values from columns
    *  with SQL types CHAR, VARCHAR, BINARY, VARBINARY, CLOB, and
    *  BLOB.  All of the data in the returned stream must be read
    *  prior to getting the value of any other column.  The next
    *  call to a get method implicitly closes the stream.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public InputStream getAsciiStream (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getAsciiStream(columnName);
    }

    /**
    *  Returns the value of a column as a BigDecimal object.  This
    *  can be used to get values from columns with SQL types
    *  SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex     The column index (1-based).
    *  @return                 The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              or the requested conversion is not valid.
    **/
    public BigDecimal getBigDecimal (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBigDecimal(columnIndex);
    }

    /**
    *  Returns the value of a column as a BigDecimal object. This
    *  can be used to get values from columns with SQL types
    *  SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found,
    *                              or the requested conversion is not valid.
    **/
    public BigDecimal getBigDecimal (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBigDecimal(columnName);
    }

    /**
    *  Returns the value of a column as a BigDecimal object.  This
    *  can be used to get values from columns with SQL types
    *  SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex     The column index (1-based).
    *  @param  scale           The number of digits after the decimal.
    *  @return                 The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              the scale is not valid, or the
    *                              requested conversion is not valid.
    *
    *  @deprecated Use getBigDecimal(int) instead.
    *  @see #getBigDecimal(int)
    **/
    public BigDecimal getBigDecimal (int columnIndex, int scale) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBigDecimal(columnIndex, scale);
    }

    /**
    *  Returns the value of a column as a BigDecimal object. This
    *  can be used to get values from columns with SQL types
    *  SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @param  scale       The number of digits after the decimal.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found,
    *                              the scale is not valid, or the
    *                              requested conversion is not valid.
    *
    *  @deprecated Use getBigDecimal(String) instead.
    *  @see #getBigDecimal(String)
    **/
    public BigDecimal getBigDecimal (String columnName, int scale) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBigDecimal(columnName, scale);
    }

    /**
    *  Returns the value of a column as a stream of uninterpreted
    *  bytes.  This can be used to get values from columns
    *  with SQL types BINARY, VARBINARY, and BLOB.  All of the data in
    *  the returned stream must be read prior to getting the
    *  value of any other column.  The next call to a get method
    *  implicitly closes the stream.
    *
    *  @param  columnIndex     The column index (1-based).
    *  @return                 The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public InputStream getBinaryStream (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBinaryStream(columnIndex);
    }

    /**
    *  Returns the value of a column as a stream of uninterpreted
    *  bytes.  This can be used to get values from columns
    *  with SQL types BINARY, VARBINARY, and BLOB.  All of the data in
    *  the returned stream must be read prior to getting the
    *  value of any other column.  The next call to a get method
    *  implicitly closes the stream.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public InputStream getBinaryStream (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBinaryStream(columnName);
    }


    /**
    *  Returns the value of a column as a Blob object.
    *  This can be used to get values from columns with SQL
    *  types BINARY, VARBINARY, and BLOB.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public Blob getBlob (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBlob(columnIndex);
    }

    /**
    *  Returns the value of a column as a Blob object.
    *  This can be used to get values from columns with SQL
    *  types BINARY, VARBINARY, and BLOB.
    *
    *  @param  columnName    The column name.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public Blob getBlob (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBlob(columnName);
    }


    /**
    *  Returns the value of a column as a Java boolean value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or false if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public boolean getBoolean (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBoolean(columnIndex);
    }

    /**
    *  Returns the value of a column as a Java boolean value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or false if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public boolean getBoolean (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBoolean(columnName);
    }

    /**
    *  Returns the value of a column as a Java byte value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex     The column index (1-based).
    *  @return                 The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public byte getByte (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getByte(columnIndex);
    }


    /**
    *  Returns the value of a column as a Java byte value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public byte getByte (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getByte(columnName);
    }

    /**
    *  Returns the value of a column as a Java byte array.
    *  This can be used to get values from columns with SQL
    *  types BINARY and VARBINARY.
    *
    *  <p>This can also be used to get values from columns
    *  with other types.  The values are returned in their
    *  native i5/OS format.  This is not supported for
    *  result sets returned by a DatabaseMetaData object.
    *
    *  @param  columnIndex     The column index (1-based).
    *  @return                 The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public byte[] getBytes (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBytes(columnIndex);
    }



    /**
    *  Returns the value of a column as a Java byte array.
    *  This can be used to get values from columns with SQL
    *  types BINARY and VARBINARY.
    *
    *  <p>This can also be used to get values from columns
    *  with other types.  The values are returned in their
    *  native i5/OS format.  This is not supported for
    *  result sets returned by a DatabaseMetaData object.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public byte[] getBytes (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getBytes(columnName);
    }


    /**
    *  Returns the value of a column as a character stream.
    *  This can be used to to get values from columns with SQL
    *  types CHAR, VARCHAR, BINARY, VARBINARY, CLOB, and BLOB.
    *  All of the data in the returned stream must be read prior to
    *  getting the value of any other column.  The next call to a get
    *  method implicitly closes the stream.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    */
    public Reader getCharacterStream (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getCharacterStream(columnIndex);
    }


    /**
    *  Returns the value of a column as a character stream.
    *  This can be used to to get values from columns with SQL
    *  types CHAR, VARCHAR, BINARY, VARBINARY, CLOB, and BLOB.
    *  All of the data in the returned stream must be read prior
    *  to getting the value of any other column.  The next call
    *  to a get method implicitly closes the stream.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not valid, or the
    *                              requested conversion is not valid.
    */
    public Reader getCharacterStream (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getCharacterStream(columnName);
    }


    /**
    *  Returns the value of a column as a Clob object.
    *  This can be used to get values from columns with SQL
    *  types CHAR, VARCHAR, BINARY, VARBINARY, BLOB, and CLOB.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public Clob getClob (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getClob(columnIndex);
    }

    /**
    *  Returns the value of a column as a Clob object.
    *  This can be used to get values from columns with SQL
    *  types CHAR, VARCHAR, BINARY, VARBINARY, BLOB, and CLOB.
    *
    *  @param  columnName    The column name.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public Clob getClob (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getClob(columnName);
    }

    /**
    *  Returns the command used by the statement the generate the rowset.
    *  This should be set before calling the execute method.
    *  @return The command.  The default value is null.
    **/
    public String getCommand()
    {
        return command_;
    }

    /**
    *  Returns the result set concurrency.
    *
    *  @return The result set concurrency.
    *  Valid values are:
    *  <ul>
    *     <li>ResultSet.CONCUR_READ_ONLY
    *     <li>ResultSet.CONCUR_UPDATABLE
    *  </ul>
    *  @exception SQLException If the result set is not open.
    **/
    public int getConcurrency () throws SQLException
    {
        if (resultSet_ != null)
            concurrency_ = resultSet_.getConcurrency();
        return concurrency_;
    }

    //@A1A
    /**
    *  Returns the JNDI naming context which provides name-to-object bindings
    *  and methods for retrieving and updating naming and directory services.
    *
    *  @return             The context or null if the value has not been set.
    *
    **/
    public Context getContext ()
    {
        return context_;
    }

    /**
    *  Returns the name of the SQL cursor in use by the result set.
    *  In SQL, results are retrieved through a named cursor.  The
    *  current row of a result can be updated or deleted using a
    *  positioned UPDATE or DELETE statement that references a cursor name.
    *
    *  @return     The cursor name.
    *  @exception  SQLException    If the result is not open.
    **/
    public String getCursorName() throws SQLException
    {
        validateResultSet();
        return resultSet_.getCursorName();
    }

    /**
    *  Returns the name of the data source as identified in JNDI.
    *  @return The data source name.  The default value is null.
    **/
    public String getDataSourceName()
    {
        return dataSourceName_;
    }

    /**
    *  Returns the value of a column as a java.sql.Date object using
    *  the default calendar.  This can be used to get values from columns
    *  with SQL types CHAR, VARCHAR, DATE, and TIMESTAMP.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public Date getDate (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getDate(columnIndex);
    }



    /**
    *  Returns the value of a column as a java.sql.Date object using
    *  the default calendar.  This can be used to get values from columns
    *  with SQL types CHAR, VARCHAR, DATE, and TIMESTAMP.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public Date getDate (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getDate(columnName);
    }


    /**
    *  Returns the value of a column as a java.sql.Date object using
    *  a calendar other than the default.  This can be used to get values
    *  from columns with SQL types CHAR, VARCHAR, DATE, and TIMESTAMP.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  calendar      The calendar.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              the calendar is null, or the
    *                              requested conversion is not valid.
    **/
    public Date getDate (int columnIndex, Calendar calendar) throws SQLException
    {
        validateResultSet();
        return resultSet_.getDate(columnIndex, calendar);
    }


    /**
    *  Returns the value of a column as a java.sql.Date object using
    *  a calendar other than the default.  This can be used to get values
    *  from columns with SQL types CHAR, VARCHAR, DATE, and TIMESTAMP.
    *
    *  @param  columnName  The column name.
    *  @param  calendar    The calendar.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found,
    *                              the calendar is null, or the
    *                              requested conversion is not valid.
    **/
    public Date getDate (String columnName, Calendar calendar) throws SQLException
    {
        validateResultSet();
        return resultSet_.getDate(columnName, calendar);
    }


    /**
    *  Returns the value of a column as a Java double value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public double getDouble (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getDouble(columnIndex);
    }


    /**
    *  Returns the value of a column as a Java double value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public double getDouble (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getDouble(columnName);
    }


    //@A2A
    /**
    *  Returns a hashtable of standard JNDI environment properties.
    *
    *  @return             The environment properties or null if the value has not been set.
    *
    **/
    public Hashtable getEnvironment ()
    {
        return environment_;
    }

    /**
    *  Indicates if escape processing is enabled (default).
    *  If enabled, escape substitution is done before committing the data.
    *  @return true if enabled; false otherwise.
    **/
    public boolean getEscapeProcessing()
    {
        return escapeProcessing_ ;
    }

    /**
    *  Returns the fetch direction.
    *
    *  @return The fetch direction.
    *  Valid values are:
    *  <ul>
    *    <li>ResultSet.FETCH_FORWARD  (default)
    *    <li>ResultSet.FETCH_REVERSE
    *    <li>ResultSet.FETCH_UNKNOWN
    *  </ul>
    *
    *  @exception  SQLException    If the result is not open.
    **/
    public int getFetchDirection() throws SQLException
    {
        if (resultSet_ != null)
            return resultSet_.getFetchDirection();

        if (statement_ == null)
            return ResultSet.FETCH_FORWARD;

        return statement_.getFetchDirection();

    }

    /**
    *  Returns the number of rows to be fetched from the database when more rows are needed.
    *  The number of rows specified only affects result sets created using this statement.
    *  If the value specified is zero, then the driver will choose an appropriate fetch size.
    *
    *  This setting only affects statements that meet the criteria specified in the "block criteria" property.
    *  The fetch size is only used if the "block size" property is set to "0".
    *
    *  @return The fetch size.
    *  @exception  SQLException    If the result is not open.
    **/
    public int getFetchSize() throws SQLException
    {
        if (resultSet_ != null)
            return resultSet_.getFetchSize();

        if (statement_ == null)
            return 0;

        return statement_.getFetchSize();
    }

    /**
    *  Returns the value of a column as a Java float value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public float getFloat (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getFloat(columnIndex);
    }


    /**
    *  Returns the value of a column as a Java float value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public float getFloat (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getFloat(columnName);
    }

    /**
    *  Returns the value of a column as a Java int value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public int getInt (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getInt(columnIndex);
    }


    /**
    *  Returns the value of a column as a Java int value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public int getInt (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getInt(columnName);
    }


    /**
    *  Returns the value of a column as a Java long value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public long getLong (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getLong(columnIndex);
    }


    /**
    *  Returns the value of a column as a Java long value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public long getLong (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getLong(columnName);
    }

    /**
    *  Returns the maximum column size.
    *  This property is only used with column types:
    *  <ul>
    *  <LI>BINARY <LI>VARBINARY <LI>LONGVARBINARY <LI>CHAR <LI>VARCHAR <LI>LONGVARCHAR
    *  </ul>
    *  @return The maximum size.  The default zero of zero indicates no maximum.
    *  @exception SQLException If a database error occurs.
    **/
    public int getMaxFieldSize() throws SQLException
    {
        if (statement_ == null)
            return 0;
        return statement_.getMaxFieldSize();
    }

    /**
    *  Returns the maximum number of rows for the rowset.
    *  @return The maximum.  The default value of zero indicates no maximum.
    *  @exception SQLException If a database error occurs.
    **/
    public int getMaxRows() throws SQLException
    {
        if (statement_ == null)
            return 0;
        return statement_.getMaxRows();
    }

    /**
    *  Returns the ResultSetMetaData object that describes the
    *  result set's columns.
    *
    *  @return     The metadata object.
    *  @exception  SQLException    If an error occurs.
    **/
    public ResultSetMetaData getMetaData () throws SQLException
    {
        validateResultSet();
        return resultSet_.getMetaData();
    }



    /**
    *  Returns the value of a column as a Java Object.
    *  This can be used to get values from columns with all
    *  SQL types.   If the column is a user-defined type, then the
    *  connection's type map is used to created the object.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public Object getObject (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getObject(columnIndex);
    }



    /**
    *  Returns the value of a column as a Java Object.
    *  This can be used to get values from columns with all
    *  SQL types.   If the column is a user-defined type, then the
    *  connection's type map is used to created the object.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public Object getObject (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getObject(columnName);
    }


    /**
    *  Returns the value of a column as a Java Object.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  typeMap       The type map.  This is not used.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              the type map is null, or the
    *                              requested conversion is not valid.
    **/
    public Object getObject (int columnIndex, Map typeMap) throws SQLException
    {
        validateResultSet();
        return resultSet_.getObject(columnIndex, typeMap);
    }


    /**
    *  Returns the value of a column as a Java Object.
    *
    *  @param  columnName    The column name.
    *  @param  typeMap       The type map.  This is not used.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found,
    *                              the type map is null, or the
    *                              requested conversion is not valid.
    **/
    public Object getObject (String columnName, Map typeMap) throws SQLException
    {
        validateResultSet();
        return resultSet_.getObject(columnName, typeMap);
    }

    /**
    *  Returns the password used to create the connection.
    *  @return An empty String.  For security the password information cannot be accessed.
    **/
    public String getPassword()
    {
        return "";
    }

    /**
    *  Returns the maximum wait time in seconds for a statement to execute.
    *  @return The timeout value in seconds.  The default value of zero indicates no maximum.
    *  @exception SQLException If a database error occurs.
    **/
    public int getQueryTimeout() throws SQLException
    {
        validateStatement();
        return statement_.getQueryTimeout();
    }

    /**
    *  Returns the value of a column as a Ref object.
    *  DB2 for i5/OS does not support structured types.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    Always thrown because DB2 for i5/OS does not support structured types.
    **/
    public Ref getRef (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getRef(columnIndex);
    }


    /**
    *  Returns the value of a column as a Ref object.
    *  DB2 for i5/OS does not support structured types.
    *
    *  @param  columnName    The column name.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    Always thrown because DB2 for i5/OS does not support structured types.
    **/
    public Ref getRef (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getRef(columnName);
    }

    /**
    *  Returns the current row number.
    *
    *  @return The current row number (1-based). If there is no current
    *          row or if the cursor is positioned on the insert row,
    *          0 is returned.
    *
    *  @exception SQLException If the result set is not open.
    **/
    public int getRow () throws SQLException
    {
        validateResultSet();
        return resultSet_.getRow();
    }

    /**
    *  Returns the value of a column as a Java short value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public short getShort (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getShort(columnIndex);
    }


    /**
    *  Returns the value of a column as a Java short value.
    *  This can be used to get values from columns with SQL
    *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
    *  NUMERIC, CHAR, and VARCHAR.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or 0 if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public short getShort (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getShort(columnName);
    }


    /**
    *  Returns the statement for this result set.
    *
    *  @return The statement for this result set, or null if the
    *        result set was returned by a DatabaseMetaData
    *        catalog method.
    *
    *  @exception SQLException If an error occurs.
    **/
    public Statement getStatement() throws SQLException
    {
        validateResultSet();
        return resultSet_.getStatement();
    }

    /**
    *  Returns the value of a column as a String object.
    *  This can be used to get values from columns with any SQL
    *  type.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public String getString (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getString(columnIndex);
    }



    /**
    *  Returns the value of a column as a String object.
    *  This can be used to get values from columns with any SQL
    *  type.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public String getString (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getString(columnName);
    }



    /**
    *  Returns the value of a column as a java.sql.Time object using the
    *  default calendar.  This can be used to get values from columns
    *  with SQL types CHAR, VARCHAR, TIME, and TIMESTAMP.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public Time getTime (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getTime(columnIndex);
    }



    /**
    *  Returns the value of a column as a java.sql.Time object using the
    *  default calendar.  This can be used to get values from columns
    *  with SQL types CHAR, VARCHAR, TIME, and TIMESTAMP.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public Time getTime (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getTime(columnName);
    }


    /**
    *  Returns the value of a column as a java.sql.Time object using a
    *  calendar other than the default.  This can be used to get values
    *  from columns with SQL types CHAR, VARCHAR, TIME, and TIMESTAMP.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  calendar      The calendar.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              the calendar is null, or the
    *                              requested conversion is not valid.
    **/
    public Time getTime (int columnIndex, Calendar calendar) throws SQLException
    {
        validateResultSet();
        return resultSet_.getTime(columnIndex, calendar);
    }


    /**
    *  Returns the value of a column as a java.sql.Time object using a
    *  calendar other than the default.  This can be used to get values
    *  from columns with SQL types CHAR, VARCHAR, TIME, and TIMESTAMP.
    *
    *  @param  columnName  The column name.
    *  @param  calendar    The calendar.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found,
    *                              the calendar is null, or the
    *                              requested conversion is not valid.
    **/
    public Time getTime (String columnName, Calendar calendar) throws SQLException
    {
        validateResultSet();
        return resultSet_.getTime(columnName, calendar);
    }



    /**
    *  Returns the value of a column as a java.sql.Timestamp object
    *  using the default calendar.  This can be used to get values
    *  from columns with SQL types CHAR, VARCHAR, DATE, and TIMESTAMP.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public Timestamp getTimestamp (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getTimestamp(columnIndex);
    }



    /**
    *  Returns the value of a column as a java.sql.Timestamp object
    *  using the default calendar.  This can be used to get values
    *  from columns with SQL types CHAR, VARCHAR, DATE, and TIMESTAMP.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public Timestamp getTimestamp (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getTimestamp(columnName);
    }


    /**
    *  Returns the value of a column as a java.sql.Timestamp object
    *  using a calendar other than the default.  This can be used to
    *  get values from columns with SQL types CHAR, VARCHAR, DATE,
    *  and TIMESTAMP.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  calendar      The calendar.
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              the calendar is null, or the
    *                              requested conversion is not valid.
    **/
    public Timestamp getTimestamp (int columnIndex, Calendar calendar) throws SQLException
    {
        validateResultSet();
        return resultSet_.getTimestamp(columnIndex, calendar);
    }


    /**
    *  Returns the value of a column as a java.sql.Timestamp object
    *  using a calendar other than the default.  This can be used to
    *  get values from columns with SQL types CHAR, VARCHAR, DATE,
    *  and TIMESTAMP.
    *
    *  @param  columnName  The column name.
    *  @param  calendar    The calendar.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found,
    *                              the calendar is null, or the
    *                              requested conversion is not valid.
    **/
    public Timestamp getTimestamp (String columnName, Calendar calendar) throws SQLException
    {
        validateResultSet();
        return resultSet_.getTimestamp(columnName, calendar);
    }

    /**
    *  Returns the transaction isolation level.
    *  Possible values are:
    *  <ul>
    *  <li>Connection.TRANSACTION_READ_UNCOMMITTED <li>Connection.TRANSACTION_READ_COMMITTED
    *  <li>Connection.TRANSACTION_REPEATABLE_READ <li>Connection.TRANSACTION_SERIALIZABLE
    *  </ul>
    *  @return The transaction isolation level.
    **/
    public int getTransactionIsolation()
    {
        try
        {
            if (connection_ != null)
                return connection_.getTransactionIsolation();
            else
                return transactionIsolation_;
        }
        catch (SQLException e)
        {
            JDTrace.logInformation (this, "getTransactionIsolation() database error");  // @G5C
            return transactionIsolation_;
        }
    }

    /**
    *  Returns the result set type.
    *
    *  @return The result set type. Valid values are:
    *  <ul>
    *    <li>ResultSet.TYPE_FORWARD_ONLY
    *    <li>ResultSet.TYPE_SCROLL_INSENSITIVE
    *    <li>ResultSet.TYPE_SCROLL_SENSITIVE
    *  </ul>
    *
    *
    *  @exception SQLException If the result set is not open.
    **/
    public int getType() throws SQLException
    {
        if (resultSet_ != null)
            return resultSet_.getType();
        return type_;
    }

    /**
    *  Returns the type map.
    *  @return The type map.  The default value is null.
    *  @exception SQLException If a database error occurs.
    **/
    public Map getTypeMap() throws SQLException
    {
        if (connection_ != null)
            return connection_.getTypeMap();
        return typeMap_;
    }

    /**
    *  Returns the value of a column as a stream of Unicode
    *  characters.  This can be used to get values from columns
    *  with SQL types CHAR, VARCHAR, BINARY, VARBINARY, CLOB, and
    *  BLOB.  All of the data in the returned stream must be read
    *  prior to getting the value of any other column.  The next
    *  call to a get method implicitly closes the stream.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @return               The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    *
    *  @deprecated Use getCharacterStream(int) instead.
    *  @see #getCharacterStream(int)
    **/
    public InputStream getUnicodeStream (int columnIndex) throws SQLException
    {
        validateResultSet();
        return resultSet_.getUnicodeStream(columnIndex);
    }

    /**
    *  Returns the value of a column as a stream of Unicode
    *  characters.  This can be used to get values from columns
    *  with SQL types CHAR, VARCHAR, BINARY, VARBINARY, CLOB,
    *  and BLOB.  All of the data in the returned stream must be
    *  read prior to getting the value of any other column.  The
    *  next call to a get method implicitly closes the stream.
    *
    *  @param  columnName  The column name.
    *  @return             The column value or null if the value is SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    *
    *  @deprecated Use getCharacterStream(String) instead.
    *  @see #getCharacterStream(String)
    **/
    public InputStream getUnicodeStream (String columnName) throws SQLException
    {
        validateResultSet();
        return resultSet_.getUnicodeStream(columnName);
    }

    /**
    *  Returns the url used in getting a connection.
    *  Either this property or the dataSource property must be set before calling the execute command.
    *  @return The url.  The default value is null.
    **/
    public String getUrl()
    {
        return url_;
    }


    //@G4A JDBC 3.0
    /**
    Returns the value of an SQL DATALINK output parameter as a
    java.net.URL object.
        
    @param  columnIndex     The column index (1-based).
    @return                 The parameter value or null if the value is SQL NULL.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter name is
                                not registered as an output parameter,
                                the statement was not executed or
                                the requested conversion is not valid.
    @since Modification 5
    **/
    public URL getURL (int columnIndex)
    throws SQLException
    {
        validateResultSet();
        return resultSet_.getURL(columnIndex);
    }



    //@G4A JDBC 3.0
    /**
    Returns the value of an SQL DATALINK output parameter as a
    java.net.URL object.
        
    @param  columnName      The column name.
    @return                 The parameter value or null if the value is SQL NULL.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter name is
                                not registered as an output parameter,
                                the statement was not executed or
                                the requested conversion is not valid.
    **/
    public URL getURL (String columnName)
    throws SQLException
    {
        validateResultSet();
        return resultSet_.getURL(columnName);
    }


    /**
    *  Returns the user used to create the connection.
    *  @return The user.  The default is null.
    **/
    public String getUsername()
    {
        return username_;
    }

    /**
    *  Returns the first warning reported for the result set.
    *  Subsequent warnings may be chained to this warning.
    *
    *  @return     The first warning or null if no warnings
    *             have been reported.
    *
    *  @exception  SQLException    If an error occurs.
    **/
    public SQLWarning getWarnings() throws SQLException
    {
        validateResultSet();
        return resultSet_.getWarnings();
    }

    // @A3A
    /**
    Initializes all transient data.
    **/
    private void initializeTransient ()
    {
        eventSupport_ = new AS400JDBCRowSetEventSupport();
        changes_ = new PropertyChangeSupport(this); 
    }

    /**
    *  Inserts the contents of the insert row into the result set
    *  and the database.
    *
    *  @exception SQLException If the result set is not open,
    *                          the result set is not updatable,
    *                          the cursor is not positioned on the insert row,
    *                          a column that is not nullable was not specified,
    *                          or an error occurs.
    **/
    public void insertRow () throws SQLException
    {
        validateResultSet();
        resultSet_.insertRow();
    }

    /**
    *  Indicates if the cursor is positioned after the last row.
    *
    *  @return true if the cursor is positioned after the last row;
    *          false if the cursor is not positioned after the last
    *          row or if the result set contains no rows.
    *
    *  @exception SQLException If the result set is not open.
    **/
    public boolean isAfterLast () throws SQLException
    {
        validateResultSet();
        return resultSet_.isAfterLast();
    }


    /**
    *  Indicates if the cursor is positioned before the first row.
    *
    *  @return true if the cursor is positioned before the first row;
    *          false if the cursor is not positioned before the first
    *          row or if the result set contains no rows.
    *
    *  @exception SQLException If the result set is not open.
    **/
    public boolean isBeforeFirst () throws SQLException
    {
        validateResultSet();
        return resultSet_.isBeforeFirst();
    }

    /**
    *  Indicates if the cursor is positioned on the first row.
    *
    *  @return true if the cursor is positioned on the first row;
    *          false if the cursor is not positioned on the first
    *          row or the row number can not be determined.
    *
    *  @exception SQLException If the result set is not open.
    **/
    public boolean isFirst () throws SQLException
    {
        validateResultSet();
        return resultSet_.isFirst();
    }

    /**
    *  Indicates if the cursor is positioned on the last row.
    *
    *  @return true if the cursor is positioned on the last row;
    *          false if the cursor is not positioned on the last
    *          row or the row number can not be determined.
    *
    *  @exception SQLException If the result set is not open.
    **/
    public boolean isLast () throws SQLException
    {
        validateResultSet();
        return resultSet_.isLast();
    }

    /**
    *  Indicates if the rowset is read-only.
    *  @return true if read-only; false otherwise.  The default value is false, allowing updates.
    **/
    public boolean isReadOnly()
    {
        if (connection_ != null)
        {
            try
            {
                return connection_.isReadOnly();
            }
            catch (SQLException e) { /* return local value */
            }
        }
        return isReadOnly_;
    }

    /**
    *  Indicates if the data source is used to make a connection to the database.
    *  @return true if the data source is used; false if the url is used.  The default value is true.
    **/
    public boolean isUseDataSource()
    {
        return useDataSource_;
    }

    /**
    *  Positions the cursor to the last row.
    *  If an InputStream from the current row is open, it is
    *  implicitly closed.  In addition, all warnings and pending updates
    *  are cleared.
    *
    *  @return             true if the requested cursor position is
    *                      valid; false otherwise.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not scrollable,
    *                              or an error occurs.
    **/
    public boolean last () throws SQLException
    {
        validateResultSet();
        boolean status = resultSet_.last();

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
        return status;
    }

    /**
    *  Positions the cursor to the current row.  This is the row
    *  where the cursor was positioned before moving it to the insert
    *  row.  If the cursor is not on the insert row, then this
    *  has no effect.
    *
    *  <p>If an InputStream from the current row is open, it is
    *  implicitly closed.  In addition, all warnings and pending updates
    *  are cleared.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not scrollable,
    *                              or an error occurs.
    **/
    public void moveToCurrentRow () throws SQLException
    {
        validateResultSet();
        resultSet_.moveToCurrentRow();

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
    }

    /**
    *  Positions the cursor to the insert row.
    *  If an InputStream from the current row is open, it is
    *  implicitly closed.  In addition, all warnings and pending updates
    *  are cleared.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not scrollable,
    *                              the result set is not updatable,
    *                              or an error occurs.
    **/
    public void moveToInsertRow () throws SQLException
    {
        validateResultSet();
        resultSet_.moveToInsertRow();

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
    }

    /**
    *  Positions the cursor to the next row.
    *  If an InputStream from the current row is open, it is
    *  implicitly closed.  In addition, all warnings and pending updates
    *  are cleared.
    *
    *  @return     true if the requested cursor position is valid; false
    *              if there are no more rows.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              or an error occurs.
    **/
    public boolean next () throws SQLException
    {
        validateResultSet();
        boolean status = resultSet_.next();

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
        return status;
    }

    /**
    *  Positions the cursor to the previous row.
    *  If an InputStream from the current row is open, it is implicitly
    *  closed.  In addition, all warnings and pending updates
    *  are cleared.
    *
    *  @return             true if the requested cursor position is
    *                      valid; false otherwise.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not scrollable,
    *                              or an error occurs.
    **/
    public boolean previous () throws SQLException
    {
        validateResultSet();
        boolean status = resultSet_.previous();

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
        return status;
    }



    // @A3A
    /**
      *Deserializes and initializes transient data.
      */
    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        initializeTransient();
    }



    /**
    *  Refreshes the current row from the database and cancels all
    *  pending updates that have been made since the last call to
    *  updateRow().  This method provides a way for an application
    *  to explicitly refetch a row from the database.  If an InputStream
    *  from the current row is open, it is implicitly closed.  In
    *  addition, all warnings and pending updates are cleared.
    *
    *  @exception SQLException If the result set is not open,
    *                          the result set is not scrollable,
    *                          the cursor is not positioned on a row,
    *                          the cursor is positioned on the
    *                          insert row or an error occurs.
    **/
    public void refreshRow () throws SQLException
    {
        validateResultSet();
        resultSet_.refreshRow();
    }

    /**
    *  Positions the cursor to a relative row number.
    *
    *  <p>Attempting to move beyond the first row positions the
    *  cursor before the first row. Attempting to move beyond the last
    *  row positions the cursor after the last row.
    *
    *  <p>If an InputStream from the current row is open, it is
    *  implicitly closed.  In addition, all warnings and pending updates
    *  are cleared.
    *
    *  @param  rowNumber   The relative row number.  If the relative row
    *                      number is positive, this positions the cursor
    *                      after the current position.  If the relative
    *                      row number is negative, this positions the
    *                      cursor before the current position.  If the
    *                      relative row number is 0, then the cursor
    *                      position does not change.
    *  @return             true if the requested cursor position is
    *                      valid, false otherwise.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not scrollable,
    *                              the cursor is not positioned on a valid row,
    *                              or an error occurs.
    */
    public boolean relative (int rowNumber) throws SQLException
    {
        validateResultSet();
        boolean status = resultSet_.relative(rowNumber);

        eventSupport_.fireCursorMoved(new RowSetEvent(this));
        return status;
    }

    /**
    *  Removes a PropertyChangeListener.
    *  @param listener The PropertyChangeListener.
    *  @see #addPropertyChangeListener
    **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException("listener");
        changes_.removePropertyChangeListener(listener);
    }

    /**
    *  Removes the RowSetListener from the list.
    *  @param listener The RowSetListener object.
    **/
    public void removeRowSetListener(RowSetListener listener)
    {
        eventSupport_.removeRowSetListener(listener);
    }

    /**
    *  Indicates if the current row has been deleted. A result set
    *  of type TYPE_SCROLL_INSENSITIVE may contain rows that have
    *  been deleted.
    *
    *  @return true if current row has been deleted; false otherwise.
    *
    *  @exception SQLException If an error occurs.
    **/
    public boolean rowDeleted () throws SQLException
    {
        validateResultSet();
        return resultSet_.rowDeleted();
    }


    /**
    *  Indicates if the current row has been inserted.  This driver does
    *  not support this method.
    *
    *  @return Always false.
    *
    *  @exception SQLException If an error occurs.
    **/
    public boolean rowInserted () throws SQLException
    {
        validateResultSet();
        return resultSet_.rowInserted();
    }


    /**
    *  Indicates if the current row has been updated.   This driver does
    *  not support this method.
    *
    *  @return Always false.
    *
    *  @exception SQLException If an error occurs.
    **/
    public boolean rowUpdated () throws SQLException
    {
        validateResultSet();
        return resultSet_.rowUpdated();
    }

    /**
    *  Sets the array <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Array object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setArray(int parameterIndex, Array value) throws SQLException
    {
        validateStatement();
        statement_.setArray(parameterIndex, value);
    }


    /**
    *  Sets the <i>inputStream</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  The driver reads the data from the stream as needed until no more bytes are available.
    *  The converts this to an SQL VARCHAR value.
    *
    *  @param parameterIndex The parameter index (1-based).
    *  @param  inputStream   The input stream or null to update the value to SQL NULL.
    *  @param length The number of bytes in the stream.
    *  @exception  SQLException    If the result set is not open, the result set is not updatable,
    *       the cursor is not positioned on a row, the column index is not valid, or the requested
    *       conversion is not valid, the length is not valid, or an error happens while reading the
    *       input stream.
    **/
    public void setAsciiStream(int parameterIndex, InputStream inputStream, int length) throws SQLException
    {
        validateStatement();
        statement_.setAsciiStream(parameterIndex, inputStream, length);
    }

    /**
    *  Sets the BigDecimal <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The BigDecimal object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setBigDecimal(int parameterIndex, BigDecimal value) throws SQLException
    {
        validateStatement();
        statement_.setBigDecimal(parameterIndex, value);
    }

    /**
    *  Sets the binary stream value using a <i>inputStream</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  The driver reads the data from the stream as needed until no more bytes are available.
    *  The driver converts this to an SQL VARBINARY value.
    *
    *  @param parameterIndex The parameter index (1-based).
    *  @param  inputStream   The input stream or null to update the value to SQL NULL.
    *  @param length The number of bytes in the stream.
    *  @exception  SQLException    If the result set is not open, the result set is not updatable,
    *       the cursor is not positioned on a row, the column index is not valid, or the requested
    *       conversion is not valid, the length is not valid, or an error happens while reading the
    *       input stream.
    **/
    public void setBinaryStream(int parameterIndex, InputStream inputStream, int length) throws SQLException
    {
        validateStatement();
        statement_.setBinaryStream(parameterIndex, inputStream, length);
    }

    /**
    *  Sets the Blob <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Blob object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setBlob(int parameterIndex, Blob value) throws SQLException
    {
        validateStatement();
        statement_.setBlob(parameterIndex, value);
    }

    /**
    *  Sets the boolean <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The boolean value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setBoolean(int parameterIndex, boolean value) throws SQLException
    {
        validateStatement();
        statement_.setBoolean(parameterIndex, value);
    }

    /**
    *  Sets the byte <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The byte value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setByte(int parameterIndex, byte value) throws SQLException
    {
        validateStatement();
        statement_.setByte(parameterIndex, value);
    }

    /**
    *  Sets the byte array <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The byte array.
    *  @exception SQLException If a database error occurs.
    **/
    public void setBytes(int parameterIndex, byte[] value) throws SQLException
    {
        validateStatement();
        statement_.setBytes(parameterIndex, value);
    }

    /**
    *  Sets a column in the current row using a Reader value.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  The driver reads the data from the Reader as needed until no more characters are available.
    *  The driver converts this to an SQL VARCHAR value.
    *
    *  @param parameterIndex The parameter index (1-based).
    *  @param  reader   The reader or null to update the value to SQL NULL.
    *  @param length The number of characters in the stream.
    *  @exception  SQLException    If the result set is not open, the result set is not updatable,
    *                        the cursor is not positioned on a row, the column index is not valid,
    *                        or the requested conversion is not valid, the length is not valid, or
    *                        an error happens while reading the input stream.
    **/
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
    {
        validateStatement();
        statement_.setCharacterStream(parameterIndex, reader, length);
    }

    /**
    *  Sets the Clob <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Clob object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setClob(int parameterIndex, Clob value) throws SQLException
    {
        validateStatement();
        statement_.setClob(parameterIndex, value);
    }

    /**
    *  Sets the command used by the execute statement to populate the rowset.
    *  This property is required to create the PreparedStatement.
    *  Resetting the command creates a new PreparedStatement and clears all
    *  existing input parameters.
    *  @param command The command.
    *  @exception SQLException If a database error occurs.
    **/
    public void setCommand(String command) throws SQLException
    {
        String property = "command";
        if (command == null)
            throw new NullPointerException(property);

        String old = command_;
        command_ = command;
        changes_.firePropertyChange(property, old, command);

        createNewStatement_ = true;

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, property, command);
    }

    /**
    *  Sets the concurrency type for the result set.
    *  Valid values include:
    *  <ul>
    *  <li>ResultSet.CONCUR_READ_ONLY <LI>ResultSet.CONCUR_UPDATABLE
    *  </ul>
    *  @param concurrency The concurrency type.
    **/
    public void setConcurrency(int concurrency)
    {
        String property = "concurrency";
        switch (concurrency)
        {
        case ResultSet.CONCUR_READ_ONLY: break;
        case ResultSet.CONCUR_UPDATABLE: break;
        default: throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        Integer oldValue = new Integer(concurrency_);
        Integer newValue = new Integer(concurrency);

        concurrency_ = concurrency;
        changes_.firePropertyChange(property, oldValue, newValue);

        createNewStatement_ = true;

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, "concurrency", concurrency);
    }

    //@A1A
    /**
    *  Sets the JNDI naming context which provides name-to-object bindings
    *  and methods for retrieving and updating naming and directory services.
    *  If this is not set, a default InitialContext will be created.
    *  @param context A JNDI naming context.
    **/
    public void setContext(Context context)
    {
        String property = "context";
        if (context == null)
            throw new NullPointerException(property);

        Context oldValue = getContext();
        Context newValue = context;

        context_ = context;
        changes_.firePropertyChange(property, oldValue, newValue);
    }

    /**
    *  Sets the name of the data source.
    *  Note:  This property is not supported.  The setDataSource method
    *  should be used for setting the data source.
    *  @param dataSourceName The data source name.
    **/
    public void setDataSourceName(String dataSourceName)
    {
        String property = "dataSourceName";
        if (dataSourceName == null)
            throw new NullPointerException(property);

        String old = getDataSourceName();
        dataSourceName_ = dataSourceName;
        changes_.firePropertyChange(property, old, dataSourceName);
    }

    /**
    *  Sets the Date <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Date object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setDate(int parameterIndex, Date value) throws SQLException
    {
        validateStatement();
        statement_.setDate(parameterIndex, value);
    }

    /**
    *  Sets the Date <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Date object.
    *  @param calendar The Calendar object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setDate(int parameterIndex, Date value, Calendar calendar) throws SQLException
    {
        validateStatement();
        statement_.setDate(parameterIndex, value, calendar);
    }

    /**
    *  Sets the double <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The double value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setDouble(int parameterIndex, double value) throws SQLException
    {
        validateStatement();
        statement_.setDouble(parameterIndex, value);
    }

    //@A2A
    /**
    *  Sets the standard JNDI environment properties.
    *  If this is not set, a default set of properties will be used.
    *  @param environment A Hashtable of JNDI environment properties.
    **/
    public void setEnvironment(Hashtable environment)
    {
        String property = "enviroment";
        if (environment == null)
            throw new NullPointerException(property);

        Hashtable oldValue = getEnvironment();
        Hashtable newValue = environment;

        environment_ = environment;
        changes_.firePropertyChange(property, oldValue, newValue);
    }

    /**
    *  Sets whether the escape scanning is enabled for escape substitution processing.
    *  @param enable true if enabled; false otherwise.  The default value is true.
    *  @exception SQLException If a database error occurs.
    **/
    public void setEscapeProcessing(boolean enable) throws SQLException
    {
        Boolean old = new Boolean(getEscapeProcessing());

        validateStatement();
        statement_.setEscapeProcessing(enable);

        escapeProcessing_ = enable;      // save it, since it can't be retrieved anywhere else.

        changes_.firePropertyChange("escapeProcessing", old, new Boolean(enable));
    }

    /**
    *  Sets the direction in which the rows in a result set are
    *  processed.
    *
    *  @param      fetchDirection  The fetch direction for processing rows.
    *  Valid values are:
    *  <ul>
    *   <li>ResultSet.FETCH_FORWARD
    *   <li>ResultSet.FETCH_REVERSE
    *   <li>ResultSet.FETCH_UNKNOWN
    *  </ul>
    *  The default is the statement's fetch direction.
    *
    *  @exception          SQLException    If the result set is not open, the result set is scrollable
    *                                      and the input value is not ResultSet.FETCH_FORWARD,
    *                                      or the input value is not valid.
    **/
    public void setFetchDirection(int fetchDirection) throws SQLException
    {
        Integer old = new Integer(getFetchDirection());

        if (resultSet_ != null)
            resultSet_.setFetchDirection(fetchDirection);

        validateStatement();
        statement_.setFetchDirection(fetchDirection);

        changes_.firePropertyChange("fetchDirection", old, new Integer(fetchDirection));
    }


    /**
    *  Sets the number of rows to be fetched from the database when more
    *  rows are needed.  This may be changed at any time. If the value
    *  specified is zero, then the driver will choose an appropriate
    *  fetch size.
    *
    *  <p>This setting only affects statements that meet the criteria
    *  specified in the "block criteria" property.  The fetch size
    *  is only used if the "block size" property is set to "0".
    *
    *  @param fetchSize    The number of rows.  This must be greater than
    *                      or equal to 0 and less than or equal to the
    *                      maximum rows limit.  The default is the
    *                      statement's fetch size.
    *
    *  @exception          SQLException    If the result set is not open
    *                                      or the input value is not valid.
    **/
    public void setFetchSize (int fetchSize) throws SQLException
    {
        Integer old = new Integer(getFetchSize());

        if (resultSet_ != null)
            resultSet_.setFetchSize(fetchSize);

        validateStatement();
        statement_.setFetchSize(fetchSize);

        changes_.firePropertyChange("fetchSize", old, new Integer(fetchSize));
    }

    /**
    *  Sets the float <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The float value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setFloat(int parameterIndex, float value) throws SQLException
    {
        validateStatement();
        statement_.setFloat(parameterIndex, value);
    }

    /**
    *  Sets the integer <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The integer value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setInt(int parameterIndex, int value) throws SQLException
    {
        validateStatement();
        statement_.setInt(parameterIndex, value);
    }

    /**
    *  Sets the long <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The long value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setLong(int parameterIndex, long value) throws SQLException
    {
        validateStatement();
        statement_.setLong(parameterIndex, value);
    }

    /**
    *  Sets the maximum column size.  The default size is zero indicating no maximum value.
    *  This property is only used with column types:
    *  <ul>
    *  <LI>BINARY <LI>VARBINARY <LI>LONGVARBINARY <LI>CHAR <LI>VARCHAR <LI>LONGVARCHAR
    *  </ul>
    *  @param maxFieldSize The maximum column size.
    *  @exception SQLException If a database error occurs.
    **/
    public void setMaxFieldSize(int maxFieldSize) throws SQLException
    {
        String property = "maxFieldSize";
        validateStatement();

        Integer oldValue = new Integer(getMaxFieldSize());
        Integer newValue = new Integer(maxFieldSize);

        statement_.setMaxFieldSize(maxFieldSize);
        changes_.firePropertyChange(property, oldValue, newValue);
    }

    /**
    *  Sets the maximum row limit for the rowset.  The default value is zero indicating no maximum value.
    *  @param maxRows The maximum number of rows.
    *  @exception SQLException If a database error occurs.
    **/
    public void setMaxRows(int maxRows) throws SQLException
    {
        String property = "maxRows";
        validateStatement();

        Integer oldValue = new Integer(getMaxRows());
        Integer newValue = new Integer(maxRows);

        statement_.setMaxRows(maxRows);
        changes_.firePropertyChange(property, oldValue, newValue);
    }

    /**
    *  Sets the type at the specified <i>parameterIndex</i> to SQL NULL.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param sqlType The SQL type.
    *  @exception SQLException If a database error occurs.
    **/
    public void setNull(int parameterIndex, int sqlType) throws SQLException
    {
        validateStatement();
        statement_.setNull(parameterIndex, sqlType);
    }

    /**
    *  Sets the user-named type or REF type at the specified <i>parameterIndex</i> to SQL NULL.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param sqlType The SQL type.
    *  @param typeName The fully qualified name of an SQL user-named type.  This parameter is not used if the type is REF.
    *  @exception SQLException If a database error occurs.
    **/
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
    {
        validateStatement();
        statement_.setNull(parameterIndex, sqlType, typeName);
    }

    /**
    *  Sets the object <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Object value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setObject(int parameterIndex, Object value) throws SQLException
    {
        validateStatement();
        statement_.setObject(parameterIndex, value);
    }

    /**
    *  Sets the object <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Object value.
    *  @param targetSqlType The SQL type.
    *  @exception SQLException If a database error occurs.
    **/
    public void setObject(int parameterIndex, Object value, int targetSqlType) throws SQLException
    {
        validateStatement();
        statement_.setObject(parameterIndex, value, targetSqlType);
    }

    /**
    *  Sets the object <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Object value.
    *  @param targetSqlType The SQL type.
    *  @param scale The number of digits after the decimal point.  This parameter is used only for SQL types Decimal or Numeric.
    *  @exception SQLException If a database error occurs.
    **/
    public void setObject(int parameterIndex, Object value, int targetSqlType, int scale) throws SQLException
    {
        validateStatement();
        statement_.setObject(parameterIndex, value, targetSqlType, scale);
    }

    /**
    *  Sets the password used to make the connection.
    *  Note: This property has no effect unless the useDataSource property is set to false.
    *  @param password The password.
    **/
    public void setPassword(String password)
    {
        String property = "password";
        if (password == null)
            throw new NullPointerException(property);
        validateConnection();

        password_ = password;
        changes_.firePropertyChange(property, "", password);
    }

    /**
    *  Sets the maximum wait time in seconds for a statement to execute.
    *  @param timeout The timeout value in seconds.  The default value is zero indicating no maximum value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setQueryTimeout(int timeout) throws SQLException
    {
        String property = "timeout";
        validateStatement();

        Integer oldValue = new Integer(getQueryTimeout());
        Integer newValue = new Integer(timeout);

        statement_.setQueryTimeout(timeout);
        changes_.firePropertyChange(property, oldValue, newValue);
    }

    /**
    *  Sets whether the rowset is read-only.  The default value is false indicating updates are allowed.
    *  @param readOnly true if read-only; false otherwise.
    *  @exception SQLException If a database error occurs.
    **/
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        String property = "readOnly";

        Boolean oldValue = new Boolean(isReadOnly());
        Boolean newValue = new Boolean(readOnly);

        if (connection_ != null)
            connection_.setReadOnly(readOnly);
        isReadOnly_ = readOnly;

        changes_.firePropertyChange(property, oldValue, newValue);
    }

    /**
    *  Sets Ref <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Ref object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setRef(int parameterIndex, Ref value) throws SQLException
    {
        validateStatement();
        statement_.setRef(parameterIndex, value);
    }

    /**
    *  Sets the short <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The short value.
    *  @exception SQLException If a database error occurs.
    **/
    public void setShort(int parameterIndex, short value) throws SQLException
    {
        validateStatement();
        statement_.setShort(parameterIndex, value);
    }

    /**
    *  Sets the string <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The String object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setString(int parameterIndex, String value) throws SQLException
    {
        validateStatement();
        statement_.setString(parameterIndex, value);
    }

    /**
    *  Sets the time <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Time object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setTime(int parameterIndex, Time value) throws SQLException
    {
        validateStatement();
        statement_.setTime(parameterIndex, value);
    }

    /**
    *  Sets the time <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Time object.
    *  @param calendar The Calendar object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setTime(int parameterIndex, Time value, Calendar calendar) throws SQLException
    {
        validateStatement();
        statement_.setTime(parameterIndex, value, calendar);
    }

    /**
    *  Sets the timestamp <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Timestamp object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setTimestamp(int parameterIndex, Timestamp value) throws SQLException
    {
        validateStatement();
        statement_.setTimestamp(parameterIndex, value);
    }

    /**
    *  Sets the timestamp <i>value</i> at the specified <i>parameterIndex</i>.
    *  This parameter is used by the internal statement to populate the rowset via the execute method.
    *  @param parameterIndex The parameter index (1-based).
    *  @param value The Timestamp object.
    *  @param calendar The Calendar object.
    *  @exception SQLException If a database error occurs.
    **/
    public void setTimestamp(int parameterIndex, Timestamp value, Calendar calendar) throws SQLException
    {
        validateStatement();
        statement_.setTimestamp(parameterIndex, value, calendar);
    }

    /**
    *  Sets the transaction isolation level.
    *  @param level The transaction isolation level.
    *  Possible values are:
    *  <ul>
    *  <li>Connection.TRANSACTION_READ_UNCOMMITTED <li>Connection.TRANSACTION_READ_COMMITTED
    *  <li>Connection.TRANSACTION_REPEATABLE_READ <li>Connection.TRANSACTION_SERIALIZABLE
    *  </ul>
    *  @exception SQLException If a database error occurs.
    **/
    public void setTransactionIsolation(int level) throws SQLException
    {
        String property = "transactionIsolation";

        Integer oldValue = new Integer(getTransactionIsolation());
        Integer newValue = new Integer(level);

        if (connection_ != null)
            connection_.setTransactionIsolation(level);
        transactionIsolation_ = level;

        changes_.firePropertyChange(property, oldValue, newValue);
    }

    /**
    *  Sets the result set type.
    *  Valid values are:
    *  <ul>
    *  <LI>ResultSet.TYPE_FORWARD_ONLY <LI>ResultSet.TYPE_SCROLL_INSENSITIVE <LI>ResultSet.TYPE_SCROLL_SENSITIVE
    *  </ul>
    *  @param type The type.
    *  @exception SQLException If a database error occurs.
    **/
    public void setType(int type) throws SQLException
    {
        String property = "type";
        switch (type)
        {
        case ResultSet.TYPE_FORWARD_ONLY: break;
        case ResultSet.TYPE_SCROLL_INSENSITIVE: break;
        case ResultSet.TYPE_SCROLL_SENSITIVE: break;
        default: throw new ExtendedIllegalArgumentException(property, ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        Integer oldValue = new Integer(getType());
        Integer newValue = new Integer(type_);

        type_ = type;
        changes_.firePropertyChange(property, oldValue, newValue);

        createNewStatement_ = true;

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, property, type);
    }

    /**
    *  Sets the type map to be used for distinct and structured types.
    *
    *  Note: Distinct types are supported by DB2 for i5/OS, but are not externalized by the IBM Toolbox for Java JDBC driver. In other words, distinct types
    *  behave as if they are the underlying type. Structured types are not supported by DB2 for i5/OS. Consequently, this driver does not support the type map.
    *
    *  @param map The type map.
    *  @exception SQLException If a database error occurs.
    **/
    public void setTypeMap(Map map) throws SQLException
    {
        if (map == null)
            throw new NullPointerException("map");

        if (connection_ != null)
            connection_.setTypeMap(map);
        typeMap_ = map;
    }

    /**
    *  Sets the URL used for getting a connection.
    *  Either this property or the dataSource property must be set before a connection can be made.
    *  This sets setUseDataSource to false.
    *  @param url The URL.
    *  @see #setUseDataSource
    **/
    public void setUrl(String url)
    {
        String property = "url";
        if (url == null)
            throw new NullPointerException(property);

        validateConnection();

        String old = getUrl();
        url_ = url;
        changes_.firePropertyChange(property, old, url);

        useDataSource_ = false;

        if (JDTrace.isTraceOn())
            JDTrace.logProperty (this, property, url);
    }

    /**
    *  Sets whether the data source is used to make a connection to the database.
    *  @param useDataSource true if the data source is used; false if the URL is used.
    *  The default value is true.
    **/
    public void setUseDataSource(boolean useDataSource)
    {
        String property = "useDataSource";
        validateConnection();

        Boolean oldValue = new Boolean(isUseDataSource());
        useDataSource_ = useDataSource;
        changes_.firePropertyChange(property, oldValue, new Boolean(useDataSource) );
    }

    /**
    *  Sets the user name used to make the connection.
    *  Note: This property has no effect unless the useDataSource property is set to false.
    *  @param username The user name.
    **/
    public void setUsername(String username)
    {
        String property = "username";
        if (username == null)
            throw new NullPointerException(property);
        validateConnection();

        String old = getUsername();

        username_ = username;
        changes_.firePropertyChange(property, old, username);
    }

    /**
    *  Returns the name of the SQL cursor in use by the result set.
    *
    *  @return     The cursor name.
    **/
    public String toString ()
    {
        if (resultSet_ != null)
            return resultSet_.toString();
        else
            return "";
    }


    //@G4A JDBC 3.0
    /**
    Updates a column in the current row using an Array value.
    DB2 for i5/OS does not support arrays.
    
    @param  columnIndex   The column index (1-based).
    @param  columnValue   The column value or null if the value is SQL NULL.
    
    @exception  SQLException    Always thrown because DB2 for i5/OS does not support arrays.
    @since Modification 5
    **/
    public void updateArray (int columnIndex, Array columnValue)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    }



    //@G4A JDBC 3.0
    /**
    Updates a column in the current row using an Array value.
    DB2 for i5/OS does not support arrays.
    
    @param  columnName    The column name.
    @param  columnValue   The column value or null if the value is SQL NULL.
    
    @exception  SQLException    Always thrown because DB2 for i5/OS does not support arrays.
    **/
    public void updateArray (String columnName, Array columnValue)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    }


    /**
    *  Updates a column in the current row using an ASCII stream value.
    *  The driver reads the data from the stream as needed until no more
    *  bytes are available.  The driver converts this to an SQL VARCHAR
    *  value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update the value to SQL NULL.
    *  @param  length        The length.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, the
    *                              requested conversion is not valid,
    *                              the length is not
    *                              valid, the input stream does not contain
    *                              ASCII characters, or an error happens
    *                              while reading the input stream.
    **/
    public void updateAsciiStream (int columnIndex, InputStream columnValue, int length) throws SQLException
    {
        validateResultSet();
        resultSet_.updateAsciiStream(columnIndex, columnValue, length);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using an ASCII stream value.
    *  The driver reads the data from the stream as needed until no more
    *  bytes are available.  The driver converts this to an SQL VARCHAR value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update the value to SQL NULL.
    *  @param  length        The length.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, the
    *                              requested conversion is not valid,
    *                              the length is not valid,
    *                              the input stream does not contain
    *                              ASCII characters, or an error happens
    *                              while reading the input stream.
    **/
    public void updateAsciiStream (String columnName,
                                   InputStream columnValue,
                                   int length)
    throws SQLException
    {
        validateResultSet();
        resultSet_.updateAsciiStream(columnName, columnValue, length);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a BigDecimal value.  The
    *  driver converts this to an SQL NUMERIC value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateBigDecimal (int columnIndex, BigDecimal columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateBigDecimal(columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a BigDecimal value.  The
    *  driver converts this to an SQL NUMERIC value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateBigDecimal (String columnName, BigDecimal columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateBigDecimal (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a binary stream value.
    *  The driver reads the data from the stream as needed until no more
    *  bytes are available.  The driver converts this to an SQL VARBINARY value.
    *
    *  <p>This does not update the database directly.  Instead, it updates a copy
    *  of the data in memory.  Call updateRow() or insertRow() to update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update the value to SQL NULL.
    *  @param  length        The length.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid,
    *                              the length is not valid, or an error
    *                              happens while reading the input stream.
    **/
    public void updateBinaryStream (int columnIndex, InputStream columnValue, int length) throws SQLException
    {
        validateResultSet();
        resultSet_.updateBinaryStream(columnIndex, columnValue, length);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a binary stream value.
    *  The driver reads the data from the stream as needed until no more
    *  bytes are available.  The driver converts this to an SQL
    *  VARBINARY value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *   a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *  @param  length        The length.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid,
    *                              the length is not valid, or an error
    *                              happens while reading the input stream.
    **/
    public void updateBinaryStream (String columnName,
                                    InputStream columnValue,
                                    int length)
    throws SQLException
    {
        validateResultSet();
        resultSet_.updateBinaryStream (columnName, columnValue, length);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    //@G4A JDBC 3.0
    /**
    Updates a column in the current row using a Java Blob value.
    The driver converts this to an SQL BLOB value.
    
    <p>This does not update the database directly.  Instead, it updates
    a copy of the data in memory.  Call updateRow() or insertRow() to
    update the database.
    
    @param  columnIndex   The column index (1-based).
    @param  columnValue   The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    @since Modification 5
    **/
    public void updateBlob (int columnIndex, Blob columnValue)
    throws SQLException
    {
        validateResultSet();
        resultSet_.updateBlob(columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }



    //@G4A JDBC 3.0
    /**
    Updates a column in the current row using a Java Blob value.
    The driver converts this to an SQL BLOB value.
    
    <p>This does not update the database directly.  Instead, it updates
    a copy of the data in memory.  Call updateRow() or insertRow() to
    update the database.
    
    @param  columnName    The column name.
    @param  columnValue   The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBlob (String columnName, Blob columnValue)
    throws SQLException
    {
        validateResultSet();
        resultSet_.updateBlob(columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    /**
    *  Updates a column in the current row using a Java boolean value.
    *  The driver converts this to an SQL SMALLINT value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateBoolean (int columnIndex, boolean columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateBoolean(columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java boolean value.
    *  The driver converts this to an SQL SMALLINT value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateBoolean (String columnName, boolean columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateBoolean (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java byte value.
    *  The driver converts this to an SQL SMALLINT value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateByte (int columnIndex, byte columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateByte (columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java byte value.
    *  The driver converts this to an SQL SMALLINT value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateByte (String columnName, byte columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateByte (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    /**
    *  Updates a column in the current row using a Java byte array value.
    *  The driver converts this to an SQL VARBINARY value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateBytes (int columnIndex, byte[] columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateBytes (columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java byte array value.
    *  The driver converts this to an SQL VARBINARY value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateBytes (String columnName, byte[] columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateBytes (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Reader value.
    *  The driver reads the data from the Reader as needed until no more
    *  characters are available.  The driver converts this to an SQL VARCHAR value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *  @param  length        The length.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid,
    *                              the length is not valid, or an error
    *                              happens while reading the input stream.
    **/
    public void updateCharacterStream (int columnIndex,
                                       Reader columnValue,
                                       int length)
    throws SQLException
    {
        validateResultSet();
        resultSet_.updateCharacterStream (columnIndex, columnValue, length);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Reader value.
    *  The driver reads the data from the Reader as needed until no more
    *  characters are available.  The driver converts this to an SQL VARCHAR
    *  value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *  @param  length        The length.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid,
    *                              the length is not valid, or an error
    *                              happens while reading the input stream.
    **/
    public void updateCharacterStream (String columnName,
                                       Reader columnValue,
                                       int length)
    throws SQLException
    {
        validateResultSet();
        resultSet_.updateCharacterStream (columnName, columnValue, length);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    //@G4A JDBC 3.0
    /**
    Updates a column in the current row using a Java Clob value.
    The driver converts this to an SQL CLOB value.
    
    <p>This does not update the database directly.  Instead, it updates
    a copy of the data in memory.  Call updateRow() or insertRow() to
    update the database.
    
    @param  columnIndex    The column index (1-based).
    @param  columnValue    The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    @since Modification 5
    **/
    public void updateClob (int columnIndex, Clob columnValue)
    throws SQLException
    {
        validateResultSet();
        resultSet_.updateClob (columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));   
    }



    //@G4A JDBC 3.0
    /**
    Updates a column in the current row using a Java Clob value.
    The driver converts this to an SQL CLOB value.
    
    <p>This does not update the database directly.  Instead, it updates
    a copy of the data in memory.  Call updateRow() or insertRow() to
    update the database.
    
    @param  columnName    The column name.
    @param  columnValue   The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateClob (String columnName, Clob columnValue)
    throws SQLException
    {
        validateResultSet();
        resultSet_.updateClob (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    /**
    *  Updates a column in the current row using a java.sql.Date value.
    *  The driver converts this to an SQL DATE value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateDate (int columnIndex, Date columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateDate (columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a java.sql.Date value.
    *  The driver converts this to an SQL DATE value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateDate (String columnName, Date columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateDate (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java double value.
    *  The driver converts this to an SQL DOUBLE value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateDouble (int columnIndex, double columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateDouble (columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java double value.
    *  The driver converts this to an SQL DOUBLE value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateDouble (String columnName, double columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateDouble (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java float value.
    *  The driver converts this to an SQL REAL value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateFloat (int columnIndex, float columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateFloat (columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java float value.
    *  The driver converts this to an SQL REAL value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateFloat (String columnName, float columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateFloat (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    /**
    *  Updates a column in the current row using a Java int value.
    *  The driver converts this to an SQL INTEGER value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateInt (int columnIndex, int columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateInt (columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java int value.
    *  The driver converts this to an SQL INTEGER value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateInt (String columnName, int columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateInt (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java long value.
    *  If the connected system supports SQL BIGINT data, the driver
    *  converts this to an SQL BIGINT value.  Otherwise, the driver
    *  converts this to an SQL INTEGER value.  SQL BIGINT data is
    *  supported on V4R5 and later.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateLong (int columnIndex, long columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateLong (columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java long value.
    *  If the connected system supports SQL BIGINT data, the driver
    *  converts this to an SQL BIGINT value.  Otherwise, the driver
    *  converts this to an SQL INTEGER value.  SQL BIGINT data is
    *  supported on V4R5 and later.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateLong (String columnName, long columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateLong (columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    /**
    *  Updates a column in the current row using SQL NULL.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateNull (int columnIndex) throws SQLException
    {
        validateResultSet();
        resultSet_.updateNull (columnIndex);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using SQL NULL.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName  The column name.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateNull (String columnName) throws SQLException
    {
        validateResultSet();
        resultSet_.updateNull (columnName);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using an Object value.
    *  The driver converts this to a value of an SQL type, depending on
    *  the type of the specified value.  The JDBC specification defines
    *  a standard mapping from Java types to SQL types.  In the cases
    *  where an SQL type is not supported by DB2 for i5/OS, the
    *  <a href="doc-files/SQLTypes.html#unsupported">next closest matching type</a>
    *  is used.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              or the requested conversion is not valid.
    **/
    public void updateObject (int columnIndex, Object columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateObject(columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    /**
    *  Updates a column in the current row using an Object value.
    *  The driver converts this to a value of an SQL type, depending on
    *  the type of the specified value.  The JDBC specification defines
    *  a standard mapping from Java types to SQL types.  In the cases
    *  where an SQL type is not supported by DB2 for i5/OS, the
    *  <a href="doc-files/SQLTypes.html#unsupported">next closest matching type</a>
    *  is used.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found,
    *                              or the requested conversion is not valid.
    **/
    public void updateObject (String columnName, Object columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateObject(columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using an Object value.
    *  The driver converts this to a value of an SQL type, depending on
    *  the type of the specified value.  The JDBC specification defines
    *  a standard mapping from Java types to SQL types.  In the cases
    *  where an SQL type is not supported by DB2 for i5/OS, the
    *  <a href="doc-files/SQLTypes.html#unsupported">next closest matching type</a>
    *  is used.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *  @param  scale         The number of digits after the decimal
    *                        if SQL type is DECIMAL or NUMERIC.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              the scale is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateObject (int columnIndex, Object columnValue, int scale) throws SQLException
    {
        validateResultSet();
        resultSet_.updateObject(columnIndex, columnValue, scale);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using an Object value.
    *  The driver converts this to a value of an SQL type, depending on
    *  the type of the specified value.  The JDBC specification defines
    *  a standard mapping from Java types to SQL types.  In the cases
    *  where an SQL type is not supported by DB2 for i5/OS, the
    *  <a href="doc-files/SQLTypes.html#unsupported">next closest matching type</a>
    *  is used.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *  @param  scale         The number of digits after the decimal
    *                        if SQL type is DECIMAL or NUMERIC.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found,
    *                              the scale is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateObject (String columnName, Object columnValue, int scale) throws SQLException
    {
        validateResultSet();
        resultSet_.updateObject(columnName, columnValue, scale);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    /**
    *  Updates the database with the new contents of the current row.
    *
    *  @exception SQLException If the result set is not open,
    *                          the result set is not updatable,
    *                          the cursor is not positioned on a row,
    *                          the cursor is positioned on the insert row,
    *                          or an error occurs.
    **/
    public void updateRow () throws SQLException
    {
        validateResultSet();
        resultSet_.updateRow();

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    //@G4A JDBC 3.0
    /**
    Updates a column in the current row using an Ref value.
    DB2 for i5/OS does not support structured types.
       
    @param  columnIndex     The column index (1-based).
    @param  columnValue     The column value or null to update
                                      the value to SQL NULL.
    @return                 The parameter value or 0 if the value is SQL NULL.
        
    @exception  SQLException    Always thrown because DB2 for i5/OS does not support REFs.
    @since Modification 5
    **/
    public void updateRef (int columnIndex, Ref columnValue)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    }



    //@G4A JDBC 3.0
    /**
    Updates a column in the current row using an Ref value.
    DB2 for i5/OS does not support structured types.
       
    @param  columnName      The column name.
    @param  columnValue     The column value or null to update
                            the value to SQL NULL.
    @return                 The parameter value or 0 if the value is SQL NULL.
        
    @exception  SQLException    Always thrown because DB2 for i5/OS does not support REFs.
    **/
    public void updateRef (String columnName, Ref columnValue)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    }



    /**
    *  Updates a column in the current row using a Java short value.
    *  The driver converts this to an SQL SMALLINT value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateShort (int columnIndex, short columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateShort(columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a Java short value.
    *  The driver converts this to an SQL SMALLINT value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateShort (String columnName, short columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateShort(columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a String value.
    *  The driver converts this to an SQL VARCHAR value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid,
    *                              or the requested conversion is not valid.
    **/
    public void updateString (int columnIndex, String columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateString(columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a String value.
    *  The driver converts this to an SQL VARCHAR value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateString (String columnName, String columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateString(columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a java.sql.Time value.
    *  The driver converts this to an SQL TIME value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateTime (int columnIndex, Time columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateTime(columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a java.sql.Time value.
    *  The driver converts this to an SQL TIME value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateTime (String columnName, Time columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateTime(columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }


    /**
    *  Updates a column in the current row using a java.sql.Timestamp value.
    *  The driver converts this to an SQL TIMESTAMP value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnIndex   The column index (1-based).
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column index is not valid, or the
    *                              requested conversion is not valid.
    **/
    public void updateTimestamp (int columnIndex, Timestamp columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateTimestamp(columnIndex, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Updates a column in the current row using a java.sql.Timestamp value.
    *  The driver converts this to an SQL TIMESTAMP value.
    *
    *  <p>This does not update the database directly.  Instead, it updates
    *  a copy of the data in memory.  Call updateRow() or insertRow() to
    *  update the database.
    *
    *  @param  columnName    The column name.
    *  @param  columnValue   The column value or null to update
    *                          the value to SQL NULL.
    *
    *  @exception  SQLException    If the result set is not open,
    *                              the result set is not updatable,
    *                              the cursor is not positioned on a row,
    *                              the column name is not found, or the
    *                              requested conversion is not valid.
    **/
    public void updateTimestamp (String columnName, Timestamp columnValue) throws SQLException
    {
        validateResultSet();
        resultSet_.updateTimestamp(columnName, columnValue);

        eventSupport_.fireRowChanged(new RowSetEvent(this));
    }

    /**
    *  Validates if the connection has been made.
    **/
    private void validateConnection()
    {
        if (connection_ != null)
            throw new ExtendedIllegalStateException("connection", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    /**
    *  Validates if the resultSet has been created.
    **/
    private void validateResultSet()
    {
        if (resultSet_ == null)
            throw new ExtendedIllegalStateException("resultSet", ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
    }

    /**
    *  Validates the statement has been created.
    *  @exception SQLException If a database error occurs.
    **/
    private void validateStatement() throws SQLException
    {
        if (statement_ == null)
            createStatement();
    }

    /**
    *  Indicates if the last column read has the value of SQL NULL.
    *
    *  @return     true if the value is SQL NULL; false otherwise.
    *  @exception  SQLException If the result set is not open.
    **/
    public boolean wasNull () throws SQLException
    {
        validateResultSet();
        return resultSet_.wasNull();
    }
}
