///////////////////////////////////////////////////////////////////////////////

//JTOpen (IBM Toolbox for Java - OSS version)                                 

//Filename: AS400JDBCArrayResultSet.java

//The source code contained herein is licensed under the IBM Public License   
//Version 1.0, which has been approved by the Open Source Initiative.         
//Copyright (C) 2009-2009 International Business Machines Corporation and     
//others. All rights reserved.                                                

///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;


import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
 
//@array new class
/** AS400JDBCArrayResultSet is a JDBC ResultSet that contains Array data.  This is a client-side only object.  This is used to navigating through
 * returned data from IBM i DB2 using Toolbox JDBC.  No updates will be functional nor will they be sent back to the host server.
 * Note that this ResultSet is limited in its functionality since it is not tied back to a cursor in the database.
 * Its primary purpose is for retrieving data back from the database.
 **/
public class AS400JDBCArrayResultSet  
/* ifdef JDBC40 */
extends ToolboxWrapper 
/* endif */ 
implements ResultSet
{

    private int holdability_; // Used by JDBC 40
    private int concurrency_;
    private int fetchDirection_;
    private int fetchSize_;
    private int type_;
    /* same as in AS400JDBCArray, the data_ array contains either Objects or SQLData.  If SQLData, then it will
     * do any needed conversion between types.  If the data is an Ojbect (like Integer), then we will not do any
     * conversion.
     */
    private Object[][] data_; // column based data.
    private int numberOfColumns_;
    private int numberOfRows_;
    private java.util.HashMap columnNameToIndexCache_;
 

    //////Info from AS400JDBCArray
  
    private SQLData contentTemplate_;
    private boolean isSQLData_;
 
    private int vrm_;    
    AS400JDBCConnection con_; //future use
    ///////////////////

    
    private boolean openOnClient_;
    private int currentRowInRowset_;
    private int wasNull_;

 
    private java.util.Calendar calendar_;
    static final private int WAS_NULL_UNSET = 0;
    static final private int WAS_NULL = 1;
    static final private int WAS_NOT_NULL = 2;

    /**
    Constructs an AS400JDBCArrayResultSet object.
 
    @param  contents         An java array of data.
    @param  contentTemplate  An instance of SQLData child class.
    @param  isSQLData        Specifies if contents array content is an SQLData subclass type.
    @param  dataType         Data type.
    @param  vrm              Version
    @param  con              Connection.
    **/
    AS400JDBCArrayResultSet (  Object[] contents , SQLData contentTemplate, boolean isSQLData, int dataType, int vrm, AS400JDBCConnection con)
    {
        Object[][] data = new Object[2][];
        // initialize "INDEX" column
        if(isSQLData)
        {
            data[0] = new SQLInteger[contents.length]; //@arrayrs //since array data will be sqlX, then make the index sqlInteger also
            for (int i = 0; i < contents.length; i++)
            {
                try{
                    SQLInteger si = (SQLInteger)SQLDataFactory.newData("INTEGER", Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 37, null, vrm_, null);
                    data[0][i] = si;
                    ((SQLInteger)data[0][i]).set(i + 1);
                }catch(Exception e){
                    //should neve happen
                    
                }
            }
        }else
        {
            data[0] = new Integer[contents.length]; 
            for (int i = 0; i < contents.length; i++)
            {
                try{
                    data[0][i] = new Integer(i + 1);
                }catch(Exception e){
                    //should neve happen
                  
                }
            }
        }
        // initialize "VALUE" column
        data[1] = contents;
        contentTemplate_ = contentTemplate;
        isSQLData_ = isSQLData;
       
        vrm_ = vrm;
        con_ = con;

        String[] columnNames = new String[] { "INDEX", "VALUE" };

        init ( CONCUR_READ_ONLY, TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.FETCH_FORWARD, 1, data, columnNames);
    }

    /**
    This method initializes this object.
 
    @param  concurrency       The concurrency of the resultset.
    @param  type              The resultset type.
    @param  fetchDirection    The Direction of the resultset.
    @param  fetchSize         Size of fetch.
    @param  data              Array of data objects
    @param  columnNames       Names of columns.
    **/
    void init (  int concurrency, int type, int fetchDirection, int fetchSize,
            Object[][] data, String[] columnNames)
    {
        holdability_ = java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT;
        concurrency_ = concurrency;
        fetchDirection_ = fetchDirection;
        fetchSize_ = fetchSize;
        type_ = type; //TYPE_SCROLL_INSENSITIVE
        data_ = data;
        numberOfColumns_ = data.length;
        // if it's an empty result set, there will be zero columns
        // and thus zero rows.
        if (numberOfColumns_ > 0) numberOfRows_ = data[0].length;

        columnNameToIndexCache_ = new java.util.HashMap ();
        for (int i = 0; i < columnNames.length; i++)
            columnNameToIndexCache_.put (columnNames[i], new Integer (i + 1));
         

        openOnClient_ = true;
        currentRowInRowset_ = -1;

    }

    /**
    Closes this ResultSet
    
    @exception SQLException If an error occurs.
    **/
    public void close () throws java.sql.SQLException
    {
        openOnClient_ = false;
    }

    private java.util.Calendar getCalendar (java.util.TimeZone timeZone)
    {
        calendar_ = (calendar_ != null) ? calendar_ : new java.util.GregorianCalendar ();
        calendar_.setTimeZone (timeZone);
        return calendar_;
    }

    private final void checkThatResultSetTypeIsScrollable () throws java.sql.SQLException
    {
        if (type_ == java.sql.ResultSet.TYPE_FORWARD_ONLY)
            JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
    }

    // ---------------------- cursor position methods ----------------------

    /**
    Indicates if the cursor is positioned before the first row.
    
    @return true if the cursor is positioned before the first row;
            false if the cursor is not positioned before the first
            row or if the result set contains no rows.
    
    @exception SQLException If the result set is not open.
    **/
    public boolean isBeforeFirst () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        boolean result = (currentRowInRowset_ == -1);
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "isBeforeFirst");
        return result;
    }

    /**
    Indicates if the cursor is positioned after the last row.
    
    @return true if the cursor is positioned after the last row;
            false if the cursor is not positioned after the last
            row or if the result set contains no rows.
    
    @exception SQLException If the result set is not open.
    **/
    public boolean isAfterLast () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        boolean result = (currentRowInRowset_ == numberOfRows_);
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "isAfterLast");
        return result;
    }

    /**
    Indicates if the cursor is positioned on the first row.
    
    @return true if the cursor is positioned on the first row;
            false if the cursor is not positioned on the first
            row or the row number can not be determined.
    
    @exception SQLException If the result set is not open.
    **/
    public boolean isFirst () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        boolean result = (currentRowInRowset_ == 0);
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "isFirst");
        return result;
    }

    /**
    Indicates if the cursor is positioned on the last row.
    
    @return true if the cursor is positioned on the last row;
            false if the cursor is not positioned on the last
            row or the row number can not be determined.
    
    @exception SQLException If the result set is not open.
    **/
    public boolean isLast () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        boolean result = (currentRowInRowset_ == (numberOfRows_ - 1));
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "isLast");
        return result;
    }

    /**
    Sets cursor position before the first row.
       
    @exception SQLException If the result set is not open.
    **/
    public void beforeFirst () throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "beforeFirst");
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        wasNull_ = WAS_NULL_UNSET;
        currentRowInRowset_ = -1;
    }

    /**
    Positions the cursor after the last row.
      
    @exception  SQLException    If the result set is not open,
                                the result set is not scrollable,
                                or an error occurs.
    **/
    public void afterLast () throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "afterLast");
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        wasNull_ = WAS_NULL_UNSET;
        currentRowInRowset_ = numberOfRows_;
    }

    /**
    Positions the cursor to the first row.
       
    @return             true if the requested cursor position is
                        valid; false otherwise.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not scrollable,
                                or an error occurs.
    **/
    public boolean first () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        wasNull_ = WAS_NULL_UNSET;
        boolean isValidCursorPosition;
        if (numberOfRows_ == 0)
            isValidCursorPosition = false;
        else {
            isValidCursorPosition = true;
            currentRowInRowset_ = 0;
        }
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "first");
        return isValidCursorPosition;
    }

    /**
    Positions the cursor to the last row.
    
    @return             true if the requested cursor position is
                        valid; false otherwise.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not scrollable,
                                or an error occurs.
    **/
    public boolean last () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        wasNull_ = WAS_NULL_UNSET;
        boolean isValidCursorPosition;
        if (numberOfRows_ == 0)
            isValidCursorPosition = false;
        else {
            isValidCursorPosition = true;
            currentRowInRowset_ = numberOfRows_ - 1;
        }
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "last");
        return isValidCursorPosition;
    }

    /**
    Positions the cursor to the previous row.
   
    @return             true if the requested cursor position is
                        valid; false otherwise.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not scrollable,
                                or an error occurs.
    **/
    public boolean previous () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();
        wasNull_ = WAS_NULL_UNSET;
        boolean isValidCursorPosition;
        currentRowInRowset_--;
        if (currentRowInRowset_ >= 0)
            isValidCursorPosition = true;
        else {
            isValidCursorPosition = false;
            currentRowInRowset_ = -1;
        }
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "previous");
        return isValidCursorPosition;
    }

    /**
    Positions the cursor to the next row.
    
    @return     true if the requested cursor position is valid; false
                if there are no more rows.
    
    @exception  SQLException    If the result set is not open,
                                or an error occurs.
    **/
    public boolean next () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        wasNull_ = WAS_NULL_UNSET;
        boolean isValidCursorPosition;
        currentRowInRowset_++;
        if (currentRowInRowset_ <= (numberOfRows_ - 1))
            isValidCursorPosition = true;
        else {
            isValidCursorPosition = false;
            currentRowInRowset_ = numberOfRows_;
        }
        if (!isValidCursorPosition && type_ == java.sql.ResultSet.TYPE_FORWARD_ONLY) 
            close ();    
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "next");
        return isValidCursorPosition;
    }

    /**
    Positions the cursor to an absolute row number.
    
    @param  row         The absolute row number.  If the absolute row
                        number is positive, this positions the cursor
                        with respect to the beginning of the result set.
                        If the absolute row number is negative, this
                        positions the cursor with respect to the end
                        of result set.
    @return             true if the requested cursor position is
                        valid; false otherwise.
    
    @exception SQLException  If the result set is not open,
                             the result set is not scrollable,
                             the row number is 0,
                             or an error occurs.
    */
    public boolean absolute (int row) throws java.sql.SQLException
    {
       //if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "absolute " + row);
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();    
        wasNull_ = WAS_NULL_UNSET;
        boolean isValidCursorPosition;

        if (row >= 0)
            currentRowInRowset_ = row - 1;
        else
            currentRowInRowset_ = row + numberOfRows_;

        if (currentRowInRowset_ >= 0 && currentRowInRowset_ <= (numberOfRows_ - 1))
            isValidCursorPosition = true;
        else {
            isValidCursorPosition = false;
            if (currentRowInRowset_ < 0)
                currentRowInRowset_ = -1;
            else
                currentRowInRowset_ = numberOfRows_;
        }
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "absolute");
        return isValidCursorPosition;
    }

    /**
    Positions the cursor to a relative row number.
    
    <p>Attempting to move beyond the first row positions the
    cursor before the first row. Attempting to move beyond the last
    row positions the cursor after the last row.
     
    @param  rows         The relative row number.  If the relative row
                        number is positive, this positions the cursor
                        after the current position.  If the relative
                        row number is negative, this positions the
                        cursor before the current position.  If the
                        relative row number is 0, then the cursor
                        position does not change.
    @return             true if the requested cursor position is
                        valid, false otherwise.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not scrollable,
                                the cursor is not positioned on a valid row,
                                or an error occurs.
    */
    public boolean relative (int rows) throws java.sql.SQLException
    {
        //if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "relative", rows);
        checkForClosedResultSet ();
        checkThatResultSetTypeIsScrollable ();    
        wasNull_ = WAS_NULL_UNSET;
        boolean isValidCursorPosition;
        currentRowInRowset_ += rows;

        if (currentRowInRowset_ >= 0 && currentRowInRowset_ <= (numberOfRows_ - 1))
            isValidCursorPosition = true;
        else {
            isValidCursorPosition = false;
            if (currentRowInRowset_ < 0)
                currentRowInRowset_ = -1;
            else
                currentRowInRowset_ = numberOfRows_;
        }
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "relative");
        return isValidCursorPosition;
    }

    // ---------------------- state getter and setter methods ----------------------

    /**
    Returns the result set concurrency.
    
    @return The result set concurrency. Valid values are:
                                    <ul>
                                      <li>CONCUR_READ_ONLY
                                      <li>CONCUR_UPDATABLE
                                    </ul>
    
    
    @exception SQLException If the result set is not open.
    **/
    public int getConcurrency () throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getConcurrency");
        checkForClosedResultSet ();
        return concurrency_;
    }

    /**
    Returns the result set type.
    
    @return The result set type. Valid values are:
                                    <ul>
                                      <li>TYPE_FORWARD_ONLY
                                      <li>TYPE_SCROLL_INSENSITIVE
                                      <li>TYPE_SCROLL_SENSITIVE
                                    </ul>
    
    
    @exception SQLException If the result set is not open.
    **/
    public int getType () throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getType");
        checkForClosedResultSet ();
        return type_;
    }

    /**
    Returns the fetch direction.
    
    @return The fetch direction. 
            Valid values are:
                                <ul>
                                  <li>FETCH_FORWARD
                                  <li>FETCH_REVERSE
                                  <li>FETCH_UNKNOWN
                                </ul>
    
    @exception  SQLException    If the result is not open.
    **/
    public int getFetchDirection () throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getFetchDirection");
        checkForClosedResultSet ();
        return fetchDirection_;
    }

    /**
    Sets the direction in which the rows in a result set are
    processed.
    
    @param      direction  The fetch direction for processing rows.
                                Valid values are:
                                <ul>
                                  <li>FETCH_FORWARD
                                  <li>FETCH_REVERSE
                                  <li>FETCH_UNKNOWN
                                </ul>
                                The default is the statement's fetch
                                direction.
    
    @exception          SQLException    If the result set is not open,
                                        the result set is scrollable
                                        and the input value is not
                                        ResultSet.FETCH_FORWARD,
                                        or the input value is not valid.
    **/
    public void setFetchDirection (int direction) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "setFetchDirection");
        checkForClosedResultSet ();
        switch (direction) {
        case java.sql.ResultSet.FETCH_FORWARD:
        case java.sql.ResultSet.FETCH_REVERSE:
        case java.sql.ResultSet.FETCH_UNKNOWN:
            fetchDirection_ = direction;
            break;
        default:
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);
        }
    }

    /**
    Returns the fetch size.
    
    @return The fetch size.
    
    @exception  SQLException    If the result is not open.
    **/
    public int getFetchSize () throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getFetchSize");
        checkForClosedResultSet ();
        return fetchSize_;
    }

    /**
    Sets the number of rows to be fetched from the database when more
    rows are needed.  This may be changed at any time. If the value
    specified is zero, then the driver will choose an appropriate
    fetch size.
     
    
    @param rows         The number of rows.  This must be greater than
                        or equal to 0 and less than or equal to the
                        maximum rows limit.  The default is the
                        statement's fetch size.
    
    @exception          SQLException    If the result set is not open
                                        or the input value is not valid.
    **/
    public void setFetchSize (int rows) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "setFetchSize");
        checkForClosedResultSet ();
        if (rows < 0)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        fetchSize_ = rows;
    }

    /**
    Returns the name of the SQL cursor in use by the result set.
  
    @return     The cursor name.
    
    @exception  SQLException    If the result is not open.
    **/
    public String getCursorName () throws java.sql.SQLException
    {
        String cursorName = null;
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getCursorName");
        checkForClosedResultSet ();
        return cursorName;
    }

    /**
    Returns the ResultSetMetaData object that describes the
    result set's columns.  ResultSetMetadata on Array columns is not supported and the
    getMetaData method will return null.
    
    @return     The metadata object.
    
    @exception  SQLException    If an error occurs.
    **/
    public java.sql.ResultSetMetaData getMetaData () throws java.sql.SQLException
    {
        java.sql.ResultSetMetaData metaData = null;//new AS400JDBCResultSetMetaData ("", 2, "", new JDSimpleRow(new String[0], new SQLData[0], new int[0]), null, null, null); //@arrmd
             
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getMetaData");
        checkForClosedResultSet ();
        return metaData;
    }

    /**
    Returns the statement for this result set.
    
    @return The statement for this result set, or null if the
            result set was returned by a DatabaseMetaData
            catalog method.
    
    @exception SQLException If an error occurs.
    **/
    public java.sql.Statement getStatement () throws java.sql.SQLException
    {
        java.sql.Statement statement = null;
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getStatement");
        checkForClosedResultSet ();
        return statement;
    }

    /**
    Returns the first warning reported for the result set.
    Subsequent warnings may be chained to this warning.
    
    @return     The first warning or null if no warnings
                have been reported.
    
    @exception  SQLException    If an error occurs.
    **/
    public java.sql.SQLWarning getWarnings () throws java.sql.SQLException
    {
        java.sql.SQLWarning warnings = null;
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getWarnings");
        checkForClosedResultSet ();
        return warnings;
    }

    /**
    Clears all warnings that have been reported for the result set.
    After this call, getWarnings() returns null until a new warning
    is reported for the result set.
    
    @exception SQLException If an error occurs.
    **/
    public void clearWarnings () throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "clearWarnings");
        checkForClosedResultSet ();
    }

    /**
    Returns the column index for the specified column name.
    
    @param      columnName      The column name.
    @return                     The column index (1-based).
    
    @exception  SQLException    If the result set is not open
                                or the column name is not found.
    **/
    public int findColumn (String columnName) throws java.sql.SQLException
    {
        //if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "findColumn", columnName);
        checkForClosedResultSet ();
        int column = findColumnX (columnName);
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "findColumn");
        return column;
    }

    private final int findColumnX (String columnName) throws java.sql.SQLException
    {
        Integer index = (Integer) columnNameToIndexCache_.get (columnName.toUpperCase ());
        if (index != null)
            return index.intValue ();
        else
        {
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);
            return -1;//never happens
        }
        
    }

    /**
    Retrieves the current row number. The first row is number 1, the second number 2, and so on. 
     
    @return The current row number (1-based), or 0 if the current row
            is not valid.
    
    @exception SQLException If the result set is not open.
    **/
    public int getRow () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        int row;
        if (currentRowInRowset_ >= 0 && currentRowInRowset_ <= (numberOfRows_ - 1))
            row = currentRowInRowset_ + 1;
        else
            row = 0;
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getRow");
        return row;
    }

    /**
    Indicates if the last column read has the value of SQL NULL.
    
    @return     true if the value is SQL NULL;
                false otherwise.
    
    @exception  SQLException    If the result set is not open.
    **/
    public boolean wasNull () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        if (wasNull_ == WAS_NULL_UNSET)
            JDError.throwSQLException (JDError.EXC_CURSOR_POSITION_INVALID);
        boolean result = (wasNull_ == WAS_NULL);
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "wasNull");
        return result;
    }

    // ---------------------- get on column methods ----------------------
    /**
    Returns the value of a column as a Java boolean value.
    
    @param  column        The column name.
    @return               The column value or false if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public boolean getBoolean (int column) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
      
        Object[] columnData = data_[column - 1];
        boolean result = false; //@nulllocalarrelem
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getBoolean();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getBoolean();  
            }
        }
         
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBoolean");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java boolean value.
    
    @param   columnName  The column name.
    @return               The column value or false if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public boolean getBoolean (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBoolean");
        return getBoolean (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Java byte value.
   
    @param  column          The column name.
    @return                 The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public byte getByte (int column) throws java.sql.SQLException
    {
       
        checkGetterPreconditions (column);
        Object[] columnData = data_[column - 1];
        
        byte result = 0;
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getByte();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getByte();  
            }
        }
         
         
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getByte");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java byte value.
    
    @param   columnName  The column name.
    @return                 The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public byte getByte (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getByte");
        return getByte (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Java short value.
    
    @param  column   The column index (1-based).
    @return               The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public short getShort (int column) throws java.sql.SQLException
    {
         
        checkGetterPreconditions (column);
        short result = 0;
        Object[] columnData = data_[column - 1];
        
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getShort();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getShort();  
            }
        }
        
       
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getShort");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java short value.
    
    @param  columnName   The column name.
    @return               The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public short getShort (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getShort");
        return getShort (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Java int value.
 
    @param  column          The column name.
    @return                 The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column name is not found, or the
                                requested conversion is not valid.
    **/
    public int getInt (int column) throws java.sql.SQLException
    {
         
        checkGetterPreconditions (column);
        int result = 0;  //@nulllocalarrelem
        Object[] columnData = data_[column - 1];
        
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getInt();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getInt();  
            }
        }
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getInt");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java int value.
 
    @param  columnName  The column name.
    @return             The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column name is not found, or the
                                requested conversion is not valid.
    **/
    public int getInt (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getInt");
        return getInt (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Java long value.
    
    @param  column        The column index (1-based).
    @return               The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public long getLong (int column) throws java.sql.SQLException
    {
       
        checkGetterPreconditions (column);
        long result = 0;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getLong();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getLong();  
            }
        }
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getLong");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java long value.
    
    @param  columnName        The column name.
    @return                   The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public long getLong (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getLong");
        return getLong (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Java float value.
    
    @param  column        The column index (1-based).
    @return               The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public float getFloat (int column) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        float result = 0;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getFloat();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getFloat();  
            }
        }
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getFloat");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java float value.
    
    @param  columnName    The column name.
    @return               The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public float getFloat (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getFloat");
        return getFloat (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Java double value.
    
    @param  column      The column index (1-based).
    @return             The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column name is not found, or the
                                requested conversion is not valid.
    **/
    public double getDouble (int column) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        double result = 0;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getDouble();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getDouble();  
            }
        }
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getDouble");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java double value.
    
    @param  columnName  The column name.
    @return             The column value or 0 if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column name is not found, or the
                                requested conversion is not valid.
    **/
    public double getDouble (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getDouble");
        return getDouble (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a BigDecimal object.  
    
    @param  column          The column index (1-based).
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                or the requested conversion is not valid.
    **/
    public java.math.BigDecimal getBigDecimal (int column) throws java.sql.SQLException
    {
       
        checkGetterPreconditions (column);
        java.math.BigDecimal result = null;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getBigDecimal(-1);
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getBigDecimal(-1);
            }
        }
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBigDecimal");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a BigDecimal object.  
    
    @param  columnName          The column name.
    @return                     The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                or the requested conversion is not valid.
    **/
    public java.math.BigDecimal getBigDecimal (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBigDecimal");
        return getBigDecimal (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a BigDecimal object.  
    
    @param  column          The column index (1-based).
    @param  scale           The number of digits after the decimal.
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                the scale is not valid, or the
                                requested conversion is not valid.
    
    @deprecated Use getBigDecimal(int) instead.
    @see #getBigDecimal(int)
    **/
    public java.math.BigDecimal getBigDecimal (int column, int scale) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBigDecimal " + column + " " + scale);
        return getBigDecimal (column).setScale (scale);
    }

    /**
    Returns the value of a column as a BigDecimal object.  
    
    @param  columnName      The column name.
    @param  scale           The number of digits after the decimal.
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                the scale is not valid, or the
                                requested conversion is not valid.
    
    @deprecated Use getBigDecimal(int) instead.
    @see #getBigDecimal(int)
    **/
    public java.math.BigDecimal getBigDecimal (String columnName, int scale) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBigDecimal " + columnName + " " + scale);
        return getBigDecimal (findColumnX (columnName), scale);
    }

    /**
    Returns the value of a column as a java.sql.Date object using
    the default calendar.
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    synchronized public java.sql.Date getDate (int column) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getDate");
        return getDateX (column, java.util.TimeZone.getDefault ());
    }

    /**
    Returns the value of a column as a java.sql.Date object using
    the default calendar.
    
    @param  columnName        The column name.
    @return                   The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.sql.Date getDate (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getDate");
        return getDate (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a java.sql.Time object using the
    default calendar.  
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    synchronized public java.sql.Time getTime (int column) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getTime");
        return getTimeX (column, java.util.TimeZone.getDefault ());
    }

    /**
    Returns the value of a column as a java.sql.Time object using the
    default calendar.  
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.sql.Time getTime (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getTime");
        return getTime (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a java.sql.Timestamp object
    using the default calendar. 
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    synchronized public java.sql.Timestamp getTimestamp (int column) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getTimestamp");
        return getTimestampX (column, java.util.TimeZone.getDefault ());
    }

    /**
    Returns the value of a column as a java.sql.Timestamp object
    using the default calendar. 
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.sql.Timestamp getTimestamp (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getTimestamp");
        return getTimestamp (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a java.sql.Date object using
    a calendar other than the default.  
    
    @param  column        The column index (1-based).
    @param  calendar      The calendar.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                the calendar is null, or the
                                requested conversion is not valid.
    **/
    synchronized public java.sql.Date getDate (int column, java.util.Calendar calendar) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getDate " + column );
       
        java.sql.Date result = getDateX (column, calendar.getTimeZone ());
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getDate");
        return result;
    }

    /**
    Returns the value of a column as a java.sql.Date object using
    a calendar other than the default.  
    
    @param  columnName    The column name.
    @param  calendar      The calendar.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                the calendar is null, or the
                                requested conversion is not valid.
    **/
    public java.sql.Date getDate (String columnName, java.util.Calendar calendar) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getDate " + columnName );
        return getDate (findColumnX (columnName), calendar);
    }

    private java.sql.Date getDateX (int column, java.util.TimeZone timeZone) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        java.sql.Date date = null;
        Object[] columnData = data_[column - 1];
    
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                date = ((SQLData)columnData[currentRowInRowset_]).getDate(getCalendar(timeZone));
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], getCalendar(timeZone), -1); 
                date = contentTemplate_.getDate(getCalendar(timeZone));
            }
        } 
        
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return date;
    }

    /**
    Returns the value of a column as a java.sql.Time object using the
    default calendar. 
    
    @param  column        The column index (1-based).
    @param  calendar      The calendar.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    synchronized public java.sql.Time getTime (int column, java.util.Calendar calendar) throws java.sql.SQLException
    {
        java.sql.Time result = getTimeX (column, calendar.getTimeZone ());
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getTime");
        return result;
    }

    /**
    Returns the value of a column as a java.sql.Time object using the
    default calendar. 
    
    @param  columnName    The column name.
    @param  calendar      The calendar.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.sql.Time getTime (String columnName, java.util.Calendar calendar) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getTime");
        return getTime (findColumnX (columnName), calendar);
    }

    private java.sql.Time getTimeX (int column, java.util.TimeZone timeZone) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        java.sql.Time time = null;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                time = ((SQLData)columnData[currentRowInRowset_]).getTime(getCalendar(timeZone));
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], getCalendar(timeZone), -1); 
                time = contentTemplate_.getTime(getCalendar(timeZone));
            }
        } 
        
         
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return time;
    }

    /**
    Returns the value of a column as a java.sql.Timestamp object
    using a calendar other than the default. 
    
    @param  column        The column index (1-based).
    @param  calendar      The calendar.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                the calendar is null, or the
                                requested conversion is not valid.
    **/
    synchronized public java.sql.Timestamp getTimestamp (int column, java.util.Calendar calendar) throws java.sql.SQLException
    {
        java.sql.Timestamp result = getTimestampX (column, calendar.getTimeZone ());   
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getTimestamp");
        return result;
    }

    /**
    Returns the value of a column as a java.sql.Timestamp object
    using a calendar other than the default. 
    
    @param  columnName    The column name.
    @param  calendar      The calendar.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                the calendar is null, or the
                                requested conversion is not valid.
    **/
    public java.sql.Timestamp getTimestamp (String columnName, java.util.Calendar calendar) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getTimestamp");
        return getTimestamp (findColumnX (columnName), calendar);
    }

    private java.sql.Timestamp getTimestampX (int column, java.util.TimeZone timeZone) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        java.sql.Timestamp timestamp = null;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                timestamp = ((SQLData)columnData[currentRowInRowset_]).getTimestamp(getCalendar(timeZone));
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], getCalendar(timeZone), -1); 
                timestamp = contentTemplate_.getTimestamp(getCalendar(timeZone));
            }
        } 
        
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return timestamp;
    }
    
    /**
    Returns the value of a column as a Java byte array.
    
    <p>This can also be used to get values from columns 
    with other types.  The values are returned in their
    native IBM i format.  This is not supported for
    result sets returned by a DatabaseMetaData object.
    
    @param  column          The column index (1-based).
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public byte[] getBytes (int column) throws java.sql.SQLException
    {
       
        checkGetterPreconditions (column);
        byte[] result = null;
        Object[] columnData = data_[column - 1];
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getBytes();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getBytes();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBytes");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java byte array.
    
    <p>This can also be used to get values from columns 
    with other types.  The values are returned in their
    native IBM i format.  This is not supported for
    result sets returned by a DatabaseMetaData object.
    
    @param  columnName      The column name.
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public byte[] getBytes (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBytes");
        return getBytes (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a String object.
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    synchronized public String getString (int column) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        String result = null;
        Object[] columnData = data_[column - 1];
     
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getString();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getString();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getString");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a String object.
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public String getString (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getString");
        return getString (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a stream of uninterpreted
    bytes. 
    
    @param  column          The column index (1-based).
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.io.InputStream getBinaryStream (int column) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        java.io.InputStream result = null;
        Object[] columnData = data_[column - 1];
    
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getBinaryStream();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getBinaryStream();
            }
        } 
       
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBinaryStream");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a stream of uninterpreted
    bytes. 
    
    @param  columnName      The column name.
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.io.InputStream getBinaryStream (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBinaryStream");
        return getBinaryStream (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a stream of ASCII
    characters.  
    
    @param  column          The column index (1-based).
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                or the requested conversion is not valid.
    **/
    public java.io.InputStream getAsciiStream (int column) throws java.sql.SQLException
    {
     
        checkGetterPreconditions (column);
        java.io.InputStream result = null;
        Object[] columnData = data_[column - 1];
        
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getAsciiStream();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getAsciiStream();
            }
        } 
      
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getAsciiStream");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }


    /**
    Returns the value of a column as a stream of ASCII
    characters.  
    
    @param  columnName      The column name.
    @return                 The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                or the requested conversion is not valid.
    **/
    public java.io.InputStream getAsciiStream (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getAsciiStream");
        return getAsciiStream (findColumnX (columnName));
    }


    /**
    Returns the value of a column as a stream of Unicode
    characters.  
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    
    @deprecated Use getCharacterStream(int) instead.
    @see #getCharacterStream(int)
    **/
    public java.io.InputStream getUnicodeStream (int column) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        java.io.InputStream result = null;
        Object[] columnData = data_[column - 1];
        
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getUnicodeStream();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getUnicodeStream();
            }
        } 
      
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getUnicodeStream");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a stream of Unicode
    characters.  
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    
    @deprecated Use getCharacterStream(String) instead.
    @see #getCharacterStream(String)
    **/
    public java.io.InputStream getUnicodeStream (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getUnicodeStream");
        return getUnicodeStream (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a character stream.
     
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    */
    public java.io.Reader getCharacterStream (int column) throws java.sql.SQLException
    {
       
        checkGetterPreconditions (column);
        java.io.Reader result = null;
        Object[] columnData = data_[column - 1];
        
        if(isSQLData_)
        {
        	if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                result = ((SQLData)columnData[currentRowInRowset_]).getCharacterStream();
            }
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getCharacterStream();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getCharacterStream");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a character stream.
     
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    */
    public java.io.Reader getCharacterStream (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getCharacterStream");
        return getCharacterStream (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Blob object.
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.sql.Blob getBlob (int column) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        java.sql.Blob result = null;
        Object[] columnData = data_[column - 1];
        
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getBlob();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getBlob();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBlob");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Blob object.
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.sql.Blob getBlob (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getBlob");
        return getBlob (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Clob object.
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.sql.Clob getClob (int column) throws java.sql.SQLException
    {

        checkGetterPreconditions (column);
        java.sql.Clob result = null;
        Object[] columnData = data_[column - 1];
        
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getClob();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getClob();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getClob");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Clob object.
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public java.sql.Clob getClob (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getClob");
        return getClob (findColumnX (columnName));
    }

    /**
    Returns the value of a column as an Array object.
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    
    **/
    public java.sql.Array getArray (int column) throws java.sql.SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    /**
    Returns the value of a column as an Array object.
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException     
    **/
    public java.sql.Array getArray (String columnName) throws java.sql.SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    /**
    Returns the value of a column as a Ref object.
    DB2 for IBM i does not support structured types.
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    Always thrown because DB2 for IBM i does not support structured types.
    **/
    public java.sql.Ref getRef (int column) throws java.sql.SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    /**
    Returns the value of a column as a Ref object.
    DB2 for IBM i does not support structured types.
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    Always thrown because DB2 for IBM i does not support structured types.
    **/
    public java.sql.Ref getRef (String columnName) throws java.sql.SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }
 
    /**
    Returns the value of an SQL DATALINK output parameter as a
    java.net.URL object.
        
    @param  column          The column index (1-based).
    @return                 The parameter value or null if the value is SQL NULL.
        
    @exception  SQLException    If the statement is not open,
                                the index is not valid, the parameter name is
                                not registered as an output parameter,
                                the statement was not executed or
                                the requested conversion is not valid.
    **/
    public java.net.URL getURL (int column) throws java.sql.SQLException
    {

        checkGetterPreconditions (column);
        java.net.URL result = null;
        Object[] columnData = data_[column - 1];
        String stringResult = null;
        
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                stringResult = ((SQLData)columnData[currentRowInRowset_]).getString();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                stringResult = contentTemplate_.getString();
            }
        } 
        
        try
        {    
            if(stringResult == null)
                result = null;
            else
                result = new java.net.URL(stringResult);
        }
        catch(MalformedURLException e)
        {
            JDError.throwSQLException (JDError.EXC_PARAMETER_TYPE_INVALID, e);
            result = null;
        }
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getURL");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

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
    public java.net.URL getURL (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getURL");
        return getURL (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Java Object.
    This can be used to get values from columns with all
    SQL types.    
    
    @param  column        The column index (1-based).
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public Object getObject (int column) throws java.sql.SQLException
    {
        checkGetterPreconditions (column);
        Object[] columnData = data_[column - 1];
        Object result = null; 
        
        if(isSQLData_)
        {
            
            if(columnData[currentRowInRowset_] != null) //@nullelem
                result = ((SQLData)columnData[currentRowInRowset_]).getObject();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getObject();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getObject");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
    Returns the value of a column as a Java Object.
    This can be used to get values from columns with all
    SQL types.    
    
    @param  columnName    The column name.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public Object getObject (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getObject");
        return getObject (findColumnX (columnName));
    }

    /**
    Returns the value of a column as a Java Object.
    
    @param  column        The column index (1-based).
    @param  map           The type map.  This is not used.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                the type map is null, or the
                                requested conversion is not valid.
    **/
    public Object getObject (int column, java.util.Map map) throws java.sql.SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    /**
    Returns the value of a column as a Java Object.
    
    @param  columnName    The column name.
    @param  map           The type map.  This is not used.
    @return               The column value or null if the value is SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                the type map is null, or the
                                requested conversion is not valid.
    **/
    public Object getObject (String columnName, java.util.Map map) throws java.sql.SQLException
    {
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

    // ---------------------- update on column methods ----------------------

    /**
    Updates a column in the current row using SQL NULL.
    
    @param  column              The column index (1-based).
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateNull (int column) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNull");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using SQL NULL.
    
    @param  columnName          The column name.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateNull (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNull");
        updateNull (findColumnX (columnName));
    }

    /**
    Updates a column in the current row using a Java byte value.
    The driver converts this to an SQL SMALLINT value.
       
    @param  column        The column index (1-based).
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateByte (int column, byte x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateByte");
        checkUpdatePreconditions (column);
    }


    /**
    Updates a column in the current row using a Java byte value.
    The driver converts this to an SQL SMALLINT value.
       
    @param  columnName    The column index (1-based).
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateByte (String columnName, byte x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateByte");
        updateByte (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a Java boolean value.
     
    @param  column        The column index (1-based).
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBoolean (int column, boolean x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBoolean");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java boolean value.
     
    @param  columnName    The column name.
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBoolean (String columnName, boolean x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBoolean");
        updateBoolean (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a Java short value.
   
    @param  column        The column index (1-based).
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateShort (int column, short x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateShort");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java short value.
   
    @param  columnName    The column name.
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateShort (String columnName, short x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateShort");
        updateShort (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a Java int value.
    The driver converts this to an SQL INTEGER value.
    
    @param  column        The column index (1-based).
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateInt (int column, int x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateInt");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java int value.
    The driver converts this to an SQL INTEGER value.
    
    @param  columnName    The column name.
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateInt (String columnName, int x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateInt");
        updateInt (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a Java long value.
   
    @param  column        The column index (1-based).
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateLong (int column, long x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateLong");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java long value.
   
    @param  columnName    The column name.
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateLong (String columnName, long x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateLong");
        updateLong (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a Java float value.
    The driver converts this to an SQL REAL value.
    
    @param  column        The column index (1-based).
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateFloat (int column, float x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateFloat");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java float value.
    The driver converts this to an SQL REAL value.
    
    @param  columnName    The column name.
    @param  x             The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateFloat (String columnName, float x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateFloat");
        updateFloat (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a Java double value.
    The driver converts this to an SQL DOUBLE value.
    
    @param  column        The column index (1-based).
    @param  x   The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateDouble (int column, double x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateDouble");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java double value.
    The driver converts this to an SQL DOUBLE value.
    
    @param  columnName        The column name.
    @param  x                 The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateDouble (String columnName, double x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateDouble");
        updateDouble (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a BigDecimal value.  The
    driver converts this to an SQL NUMERIC value.
     
    @param  column        The column index (1-based).
    @param  x   The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBigDecimal (int column, java.math.BigDecimal x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBigDecimal");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a BigDecimal value.  The
    driver converts this to an SQL NUMERIC value.
     
    @param  columnName    The column name.
    @param  x   The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBigDecimal (String columnName, java.math.BigDecimal x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBigDecimal");
        updateBigDecimal (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a java.sql.Date value.
    The driver converts this to an SQL DATE value.
     
    @param  column        The column index (1-based).
    @param  x   The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateDate (int column, java.sql.Date x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateDate");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a java.sql.Date value.
    The driver converts this to an SQL DATE value.
     
    @param  columnName        The column name.
    @param  x   The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateDate (String columnName, java.sql.Date x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateDate");
        updateDate (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a java.sql.Time value.
    The driver converts this to an SQL TIME value.
        
    @param  column        The column index (1-based).
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateTime (int column, java.sql.Time x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateTime");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a java.sql.Time value.
    The driver converts this to an SQL TIME value.
        
    @param  columnName    The column name.
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateTime (String columnName, java.sql.Time x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateTime");
        updateTime (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a java.sql.Timestamp value.
    The driver converts this to an SQL TIMESTAMP value.
        
    @param  column        The column index (1-based).
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateTimestamp (int column, java.sql.Timestamp x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateTimestamp");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a java.sql.Timestamp value.
    The driver converts this to an SQL TIMESTAMP value.
        
    @param  columnName    The column name.
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateTimestamp (String columnName, java.sql.Timestamp x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateTimestamp");
        updateTimestamp (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a Java byte array value.
     
    @param  column        The column index (1-based).
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBytes (int column, byte x[]) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBytes");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java byte array value.
     
    @param  columnName    The column name.
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBytes (String columnName, byte x[]) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBytes");
        updateBytes (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a String value.
    The driver converts this to an SQL VARCHAR value.
    
    @param  column        The column index (1-based).
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                or the requested conversion is not valid.
    **/
    public void updateString (int column, String x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateString");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a String value.
    The driver converts this to an SQL VARCHAR value.
    
    @param  columnName    The column name.
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid,
                                or the requested conversion is not valid.
    **/
    public void updateString (String columnName, String x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateString");
        updateString (findColumnX (columnName), x);
    }

    /** 
     * Updates the designated column with a binary stream value, which will have
     * the specified number of bytes.
     * @param column column index
     * @param x the new column value     
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs,
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateBinaryStream (int column, java.io.InputStream x, int length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBinaryStream");
        checkUpdatePreconditions (column);
    }

    /** 
     * Updates the designated column with a binary stream value, which will have
     * the specified number of bytes.
     * @param columnName column index
     * @param x the new column value     
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs,
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateBinaryStream (String columnName, java.io.InputStream x, int length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBinaryStream");
        updateBinaryStream (findColumnX (columnName), x, length);
    }

    /** 
     * Updates the designated column with an ascii stream value, which will have
     * the specified number of bytes.
     * @param column 
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs,
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateAsciiStream (int column, java.io.InputStream x, int length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateAsciiStream");
        checkUpdatePreconditions (column);
    }


    /** 
     * Updates the designated column with an ascii stream value, which will have
     * the specified number of bytes.
     * @param columnName 
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs,
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateAsciiStream (String columnName, java.io.InputStream x, int length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateAsciiStream");
        updateAsciiStream (findColumnX (columnName), x, length);
    }

    /**
    Updates a column in the current row using a Reader value.
    The driver reads the data from the Reader as needed until no more
    characters are available.  The driver converts this to an SQL VARCHAR
    value.
    
    @param  column        The column index (1-based).
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    @param  length        The length.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid,
                                the length is not valid, or an error 
                                happens while reading the input stream.
    **/
    public void updateCharacterStream (int column, java.io.Reader x, int length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateCharacterStream");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Reader value.
    The driver reads the data from the Reader as needed until no more
    characters are available.  The driver converts this to an SQL VARCHAR
    value.
    
    @param  columnName    The column name.
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    @param  length        The length.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid,
                                the length is not valid, or an error 
                                happens while reading the input stream.
    **/
    public void updateCharacterStream (String columnName, java.io.Reader x, int length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateCharacterStream");
        updateCharacterStream (findColumnX (columnName), x, length);
    }

    /**
    Updates a column in the current row using a Java Blob value.
    The driver converts this to an SQL BLOB value.
    
    @param  column    The column index (1-based).
    @param  x         The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBlob (int column, java.sql.Blob x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBlob");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java Blob value.
    The driver converts this to an SQL BLOB value.
    
    @param  columnName    The column index (1-based).
    @param  x         The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateBlob (String columnName, java.sql.Blob x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBlob");
        updateBlob (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using a Java Clob value.
    The driver converts this to an SQL CLOB value.
     
    @param  column         The column index (1-based).
    @param  x              The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateClob (int column, java.sql.Clob x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateClob");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using a Java Clob value.
    The driver converts this to an SQL CLOB value.
     
    @param  columnName     The column name.
    @param  x              The column value.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, or the
                                requested conversion is not valid.
    **/
    public void updateClob (String columnName, java.sql.Clob x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateClob");
        updateClob (findColumnX (columnName), x);
    }

    /**
    Updates the value of a column as an Array object.
    
    @param  column     The column index (1-based).
    @param  x          The column value or null if the value is SQL NULL.
    
    @exception  SQLException  
    **/
    public void updateArray (int column, java.sql.Array x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateArray");
        checkUpdatePreconditions (column);
    }

    /**
    Updates the value of a column as an Array object.
    
    @param  columnName The column index (1-based).
    @param  x          The column value or null if the value is SQL NULL.
    
    @exception  SQLException  
    **/
    public void updateArray (String columnName, java.sql.Array x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateArray");
        updateArray (findColumnX (columnName), x);
    }

    /**
    Updates the value of an SQL REF output parameter as a Ref value.
        
    @param  column          The column index (1-based).
    @param  x               The column value or null to update
                                      the value to SQL NULL.
        
    @exception  SQLException    
    **/
    public void updateRef (int column, java.sql.Ref x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateRef");
        checkUpdatePreconditions (column);
    }

    /**
    Updates the value of an SQL REF output parameter as a Ref value.
        
    @param  columnName      The column index (1-based).
    @param  x               The column value or null to update
                                      the value to SQL NULL.
        
    @exception  SQLException    
    **/
    public void updateRef (String columnName, java.sql.Ref x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateRef");
        updateRef (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using an Object value.
    The driver converts this to a value of an SQL type, depending on
    the type of the specified value.  The JDBC specification defines
    a standard mapping from Java types to SQL types.  
    
    @param  column    The column index (1-based).
    @param  x         The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, 
                                or the requested conversion is not valid.
    **/
    public void updateObject (int column, Object x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateObject");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using an Object value.
    The driver converts this to a value of an SQL type, depending on
    the type of the specified value.  The JDBC specification defines
    a standard mapping from Java types to SQL types.  
    
    @param  columnName    The column name.
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, 
                                or the requested conversion is not valid.
    **/
    public void updateObject (String columnName, Object x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateObject");
        updateObject (findColumnX (columnName), x);
    }

    /**
    Updates a column in the current row using an Object value.
    The driver converts this to a value of an SQL type, depending on
    the type of the specified value.  The JDBC specification defines
    a standard mapping from Java types to SQL types.  
    
    @param  column        The column index.
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    @param  scale         The scale.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, 
                                or the requested conversion is not valid.
    **/
    public void updateObject (int column, Object x, int scale) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateObject");
        checkUpdatePreconditions (column);
    }

    /**
    Updates a column in the current row using an Object value.
    The driver converts this to a value of an SQL type, depending on
    the type of the specified value.  The JDBC specification defines
    a standard mapping from Java types to SQL types.  
    
    @param  columnName    The column name.
    @param  x             The column value or null to update
                                      the value to SQL NULL.
    @param  scale         The scale.
    
    @exception  SQLException    If the result set is not open,
                                the result set is not updatable,
                                the cursor is not positioned on a row,
                                the column index is not valid, 
                                or the requested conversion is not valid.
    **/
    public void updateObject (String columnName, Object x, int scale) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateObject");
        updateObject (findColumnX (columnName), x, scale);
    }

    /**
    Indicates if the current row has been updated.   This driver does
    not support this method.
    
    @return Always false.
    
    @exception SQLException If an error occurs.
    **/
    public boolean rowUpdated () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return false;
    }

    /**
    Indicates if the current row has been inserted.  This driver does
    not support this method.
    
    @return Always false.  
    
    @exception SQLException If an error occurs.
    **/
    public boolean rowInserted () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return false;
    }

    /**
    Indicates if the current row has been deleted. A result set
    of type TYPE_SCROLL_INSENSITIVE may contain rows that have
    been deleted.
    
    @return true if current row has been deleted; false otherwise.
    
    @exception SQLException If an error occurs.
    **/
    public boolean rowDeleted () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return false;
    }

    /**
    Inserts the contents of the insert row into the result set
    and the database.
    
    @exception SQLException If the result set is not open,
                            the result set is not updatable,
                            the cursor is not positioned on the insert row,
                            a column that is not nullable was not specified,
                            or an error occurs.
    **/
    public void insertRow () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
       
    }

    /**
    Cancels all pending updates that have been made since the last 
    call to updateRow(). 
    
    @exception  SQLException    If the result set is not open
                                or the result set is not updatable.
    **/   
    public void updateRow () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    }

    /**
    Deletes the current row from the result set and the database.
    After deleting a row, the cursor position is no longer valid,
    so it must be explicitly repositioned.
    
    @exception SQLException If the result set is not open,
                            the result set is not updatable,
                            the cursor is not positioned on a row,
                            the cursor is positioned on the insert row,
                            or an error occurs.
    **/
    public void deleteRow () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    }

    /**
    Refreshes the current row from the database.
    
    @exception SQLException If the result set is not open,
                            the result set is not scrollable,
                            the cursor is not positioned on a row,
                            the cursor is positioned on the
                            insert row or an error occurs.
    **/
    public void refreshRow () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    }

    /**
    Cancels all pending updates that have been made since the last 
    call to updateRow(). 
    
    @exception  SQLException    If the result set is not open
                                or the result set is not updatable.
    **/   
    public void cancelRowUpdates () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    }

    /**
    Positions the cursor to the insert row.
   
    @exception  SQLException    If the result set is not open,
                                the result set is not scrollable,
                                the result set is not updatable,
                                or an error occurs.
    **/
    public void moveToInsertRow () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    }

    /**
    Positions the cursor to the current row.   
    
    @exception  SQLException    If the result set is not open,
                                the result set is not scrollable,
                                or an error occurs.
    **/
    public void moveToCurrentRow () throws java.sql.SQLException
    {
        checkUpdatePreconditions ();
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
    }

    // ---------------------- condition checking helper methods ----------------------

    private final void checkForClosedResultSet () throws java.sql.SQLException
    {
        if (!openOnClient_)
            JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
    }

    private final void checkForValidColumnIndex (int column) throws java.sql.SQLException
    {
        if (column < 1 || column > numberOfColumns_)
            JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
    }

    private final void checkForValidPosition () throws java.sql.SQLException
    {
        if (currentRowInRowset_ < 0 || currentRowInRowset_ > (numberOfRows_ - 1))
            JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
    }

    private final void checkForConcurrency () throws java.sql.SQLException
    {
        if (concurrency_ != java.sql.ResultSet.CONCUR_UPDATABLE)
            JDError.throwSQLException (JDError.EXC_CURSOR_STATE_INVALID);
    }

    private final void checkGetterPreconditions (int column) throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkForValidColumnIndex (column);
        checkForValidPosition ();
    }

    private final void checkUpdatePreconditions (int column) throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkForValidColumnIndex (column);
        checkForConcurrency ();
    }

    private final void checkUpdatePreconditions () throws java.sql.SQLException
    {
        checkForClosedResultSet ();
        checkForConcurrency ();
    }
 
    // ---------- JDBC 4 methods ----------

    /**
    Indicates if the result set is closed.
    
    @return     true if this result set is closed;
                false otherwise.
    **/
    public boolean isClosed () throws java.sql.SQLException
    {
        return !openOnClient_;
    }

    /**
     * Retrieves the holdability.
     * @throws SQLException if a database error occurs
     */
    public int getHoldability () throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getHoldability");
        checkForClosedResultSet ();
        return holdability_;
    }

    /**
     * Retrieves the value of the designated column in the current row 
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object.
     * @return a <code>java.io.Reader</code> object that contains the column
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @param column
     * @exception SQLException if a database access error occurs
     */
    public java.io.Reader getNCharacterStream (int column) throws java.sql.SQLException
    {
       
        checkGetterPreconditions (column);
        java.io.Reader result = null;
        Object[] columnData = data_[column - 1];
       
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getNCharacterStream();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getNCharacterStream();
            }
        } 

        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getNCharacterStream");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
     * Retrieves the value of the designated column in the current row 
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object.
     * @return a <code>java.io.Reader</code> object that contains the column
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @param columnName
     * @exception SQLException if a database access error occurs
     */
    public java.io.Reader getNCharacterStream (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getNCharacterStream");
        return getNCharacterStream (findColumnX (columnName));
    }


 /**
  * Retrieves the value of the designated column in the current row
   * of this <code>ResultSet</code> object as a <code>NClob</code> object
   * in the Java programming language.
   *
   * @param column 
   * @return a <code>NClob</code> object representing the SQL 
   *         <code>NCLOB</code> value in the specified column
   * @exception SQLException if the driver does not support national
   *         character sets;  if the driver can detect that a data conversion
   *  error could occur; or if a database access error occurss
   */
/* ifdef JDBC40 */
    public java.sql.NClob getNClob (int column) throws java.sql.SQLException
    {
        
        checkGetterPreconditions (column);
        java.sql.NClob result = null;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getNClob();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getNClob();
            }
        } 

        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getNClob");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }
/* endif */ 
    
   /**
   * Retrieves the value of the designated column in the current row
   * of this <code>ResultSet</code> object as a <code>NClob</code> object
   * in the Java programming language.
   *
   * @param columnName 
   * @return a <code>NClob</code> object representing the SQL 
   *         <code>NCLOB</code> value in the specified column
   * @exception SQLException if the driver does not support national
   *         character sets;  if the driver can detect that a data conversion
   *  error could occur; or if a database access error occurss
   */
/* ifdef JDBC40 */
    public java.sql.NClob getNClob (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getNClob");
        return getNClob (findColumnX (columnName));
    }

/* endif */ 

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>String</code> in the Java programming language.
     * It is intended for use when
     * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> columns.
     *
     * @param column 
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs 
    */
    public String getNString (int column) throws java.sql.SQLException
    {
       
        checkGetterPreconditions (column);
        String result = null;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem  
                result = ((SQLData)columnData[currentRowInRowset_]).getNString();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getNString();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getNString");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>String</code> in the Java programming language.
     * It is intended for use when
     * accessing  <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> columns.
     *
     * @param columnName 
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs 
    */
    public String getNString (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getNString");
        return getNString (findColumnX (columnName));
    }

     /**
      * Retrieves the value of the designated column in the current row of this 
      * <code>ResultSet</code> object as a <code>java.sql.RowId</code> object in the Java
      * programming language.
      *
      * @param column    The column number
      * @return the column value ; if the value is a SQL <code>NULL</code> the
      *     value returned is <code>null</code>
      * @throws SQLException if a database access error occurs
      */
/* ifdef JDBC40 */
    public java.sql.RowId getRowId (int column) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getRowId");
        checkGetterPreconditions (column);
        java.sql.RowId result = null;
        Object[] columnData = data_[column - 1];

        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getRowId();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getRowId();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getRowId");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }
/* endif */ 

     /**
      * Retrieves the value of the designated column in the current row of this 
      * <code>ResultSet</code> object as a <code>java.sql.RowId</code> object in the Java
      * programming language.
      *
      * @param columnName  The column name
      * @return the column value ; if the value is a SQL <code>NULL</code> the
      *     value returned is <code>null</code>
      * @throws SQLException if a database access error occurs
      */
/* ifdef JDBC40 */
    public java.sql.RowId getRowId (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getRowId");
        return getRowId (findColumnX (columnName));
    }
/* endif */ 

     /**
      * Retrieves the value of the designated column in  the current row of
      *  this <code>ResultSet</code> as a
      * <code>java.sql.SQLXML</code> object in the Java programming language.
      * @param column
      * @return a <code>SQLXML</code> object that maps an <code>SQL XML</code> value
      * @throws SQLException if a database access error occurs
      */
/* ifdef JDBC40 */
    public java.sql.SQLXML getSQLXML (int column) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getSQLXML");
        checkGetterPreconditions (column);
        java.sql.SQLXML result = null;
        Object[] columnData = data_[column - 1];
       
        if(isSQLData_)
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
                result = ((SQLData)columnData[currentRowInRowset_]).getSQLXML();
        }
        else
        {
            if(columnData[currentRowInRowset_] != null) //@nulllocalarrelem
            {
                contentTemplate_.set(columnData[currentRowInRowset_], calendar_, -1); 
                result = contentTemplate_.getSQLXML();
            }
        } 
        
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getSQLXML");
        wasNull_ = (columnData[currentRowInRowset_] == null) ? WAS_NULL : WAS_NOT_NULL;
        return result;
    }
/* endif */ 

     /**
      * Retrieves the value of the designated column in  the current row of
      *  this <code>ResultSet</code> as a
      * <code>java.sql.SQLXML</code> object in the Java programming language.
      * @param columnName
      * @return a <code>SQLXML</code> object that maps an <code>SQL XML</code> value
      * @throws SQLException if a database access error occurs
      */
/* ifdef JDBC40 */
    public java.sql.SQLXML getSQLXML (String columnName) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "getSQLXML");
        return getSQLXML (findColumnX (columnName));
    }
/* endif */ 
    /** 
     * Updates the designated column with an ascii stream value, which will have
     * the specified number of bytes.
    
     * @param column 
     * @param x the new column value
     * @exception SQLException if a database access error occurs,
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateAsciiStream (int column, java.io.InputStream x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateAsciiStream");
        checkUpdatePreconditions (column);
    }

    /** 
     * Updates the designated column with an ascii stream value, which will have
     * the specified number of bytes.
     * @param columnName
     * @param x the new column value
     * @exception SQLException if a database access error occurs,
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateAsciiStream (String columnName, java.io.InputStream x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateAsciiStream");
        updateAsciiStream (findColumnX (columnName), x);
    }

    /** 
     * Updates the designated column with an ascii stream value, which will have
     * the specified number of bytes.
     * @param column
     * @param x the new column value
     * @param length
     * @exception SQLException if a database access error occurs,
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateAsciiStream (int column, java.io.InputStream x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateAsciiStream");
        checkUpdatePreconditions (column);
    }

    /** 
     * Updates the designated column with an ascii stream value, which will have
     * the specified number of bytes.
     * @param columnName
     * @param x the new column value
     * @param length
     * @exception SQLException if a database access error occurs,
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateAsciiStream (String columnName, java.io.InputStream x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateAsciiStream");
        updateAsciiStream (findColumnX (columnName), x, length);
    }

    /** 
     * Updates the designated column with a binary stream value.
     *
     * @param column
     * @param x the new column value     
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateBinaryStream (int column, java.io.InputStream x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBinaryStream");
        checkUpdatePreconditions (column);
    }

    /** 
     * Updates the designated column with a binary stream value.
     *
     * @param columnName
     * @param x the new column value     
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateBinaryStream (String columnName, java.io.InputStream x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBinaryStream");
        updateBinaryStream (findColumnX (columnName), x);
    }

    /** 
     * Updates the designated column with a binary stream value.
     *
     * @param column
     * @param x the new column value     
     * @param length
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateBinaryStream (int column, java.io.InputStream x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBinaryStream");
        checkUpdatePreconditions (column);
    }

    /** 
     * Updates the designated column with a binary stream value.
     *
     * @param columnName
     * @param x the new column value     
     * @param length
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateBinaryStream (String columnName, java.io.InputStream x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBinaryStream");
        updateBinaryStream (findColumnX (columnName), x, length);
    }

    /**
     * Updates the designated column using the given input stream. The data will be read from the stream
     * as needed until end-of-stream is reached.
     *
     * @param column
     * @param x     An object that contains the data to set the parameter value to.
     * @exception SQLException if the columnIndex is not valid; if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateBlob (int column, java.io.InputStream x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBlob");
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column using the given input stream. The data will be read from the stream
     * as needed until end-of-stream is reached.
     *
     * @param columnName
     * @param x     An object that contains the data to set the parameter value to.
     * @exception SQLException if the columnIndex is not valid; if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateBlob (String columnName, java.io.InputStream x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBlob" );
        updateBlob (findColumnX (columnName), x);
    }

    /**
     * Updates the designated column using the given input stream. The data will be read from the stream
     * as needed until end-of-stream is reached.
     *
     * @param column
     * @param x     An object that contains the data to set the parameter value to.
     * @param length
     * @exception SQLException if the columnIndex is not valid; if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateBlob (int column, java.io.InputStream x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBlob" );
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column using the given input stream. The data will be read from the stream
     * as needed until end-of-stream is reached.
     *
     * @param columnName
     * @param x     An object that contains the data to set the parameter value to.
     * @param length
     * @exception SQLException if the columnIndex is not valid; if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateBlob (String columnName, java.io.InputStream x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateBlob" );
        updateBlob (findColumnX (columnName), x, length);
    }

    /**
     * Updates the designated column with a character stream value.
     *
     * @param column
     * @param x the new column value
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateCharacterStream (int column, java.io.Reader x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateCharacterStream" );
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column with a character stream value.
     *
     * @param columnName
     * @param x the new column value
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateCharacterStream (String columnName, java.io.Reader x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateCharacterStream" );
        updateCharacterStream (findColumnX (columnName), x);
    }

    /**
     * Updates the designated column with a character stream value.
     *
     * @param column
     * @param x the new column value
     * @param length
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateCharacterStream (int column, java.io.Reader x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateCharacterStream");
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column with a character stream value.
     *
     * @param columnName
     * @param x the new column value
     * @param length
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateCharacterStream (String columnName, java.io.Reader x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateCharacterStream");
        updateCharacterStream (findColumnX (columnName), x, length);
    }

    /**
     * Updates the designated column using the given <code>Reader</code>
     * object.
     *
     * @param column
     * @param x An object that contains the data to set the parameter value to.
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateClob (int column, java.io.Reader x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateClob");
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column using the given <code>Reader</code>
     * object.
     *
     * @param columnName
     * @param x An object that contains the data to set the parameter value to.
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateClob (String columnName, java.io.Reader x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateClob");
        updateClob (findColumnX (columnName), x);
    }

    /**
     * Updates the designated column using the given <code>Reader</code>
     * object.
     *
     * @param column
     * @param x An object that contains the data to set the parameter value to.
     * @param length
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateClob (int column, java.io.Reader x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateClob");
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column using the given <code>Reader</code>
     * object.
     *
     * @param columnName
     * @param x An object that contains the data to set the parameter value to.
     * @param length
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs;
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * or this method is called on a closed result set
     */
    public void updateClob (String columnName, java.io.Reader x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateClob");
        updateClob (findColumnX (columnName), x, length);
    }

    /**
     * Updates the designated column with a character stream value.  
     *
     * @param column
     * @param x the new column value
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs; 
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateNCharacterStream (int column, java.io.Reader x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNCharacterStream");
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column with a character stream value.  
     *
     * @param columnName
     * @param x the new column value
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs; 
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateNCharacterStream (String columnName, java.io.Reader x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNCharacterStream");
        updateNCharacterStream (findColumnX (columnName), x);
    }

    /**
     * Updates the designated column with a character stream value.  
     *
     * @param column
     * @param x the new column value
     * @param length
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs; 
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateNCharacterStream (int column, java.io.Reader x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNCharacterStream");
        checkUpdatePreconditions (column);
    }


    /**
     * Updates the designated column with a character stream value.  
     *
     * @param columnName
     * @param x the new column value
     * @param length
     * @exception SQLException if the columnIndex is not valid; 
     * if a database access error occurs; 
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> or this method is called on a closed result set
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateNCharacterStream (String columnName, java.io.Reader x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNCharacterStream" );
        updateNCharacterStream (findColumnX (columnName), x, length);
    }

     /**
      * Updates the designated column using the given <code>Reader</code>
      *
      * @param column
      * @param x      An object that contains the data to set the parameter value to.
      * @throws SQLException if the columnIndex is not valid; 
      * if the driver does not support national
      *         character sets;  if the driver can detect that a data conversion
      *  error could occur; this method is called on a closed result set,  
      * if a database access error occurs or
      * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
      * this method
      */
/* ifdef JDBC40 */
    public void updateNClob (int column, java.sql.NClob x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNClob" );
        checkUpdatePreconditions (column);
    }
/* endif */ 

     /**
      * Updates the designated column using the given <code>Reader</code>
      *
      * @param columnName
      * @param x      An object that contains the data to set the parameter value to.
      * @throws SQLException if the columnIndex is not valid; 
      * if the driver does not support national
      *         character sets;  if the driver can detect that a data conversion
      *  error could occur; this method is called on a closed result set,  
      * if a database access error occurs or
      * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
      * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
      * this method
      */
/* ifdef JDBC40 */
    public void updateNClob (String columnName, java.sql.NClob x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNClob" );
        updateNClob (findColumnX (columnName), x);
    }
/* endif */ 
    /**
     * Updates the designated column using the given <code>Reader</code>
     *
     * @param column
     * @param x      An object that contains the data to set the parameter value to.
     * @throws SQLException if the columnIndex is not valid; 
     * if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; this method is called on a closed result set,  
     * if a database access error occurs or
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateNClob (int column, java.io.Reader x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNClob" );
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column using the given <code>Reader</code>
     *
     * @param columnName
     * @param x      An object that contains the data to set the parameter value to.
     * @throws SQLException if the columnIndex is not valid; 
     * if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; this method is called on a closed result set,  
     * if a database access error occurs or
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateNClob (String columnName, java.io.Reader x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNClob" );
        updateNClob (findColumnX (columnName), x);
    }

    /**
     * Updates the designated column using the given <code>Reader</code>
     *
     * @param column
     * @param x      An object that contains the data to set the parameter value to.
     * @param length
     * @throws SQLException if the columnIndex is not valid; 
     * if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; this method is called on a closed result set,  
     * if a database access error occurs or
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateNClob (int column, java.io.Reader x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNClob");
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column using the given <code>Reader</code>
     *
     * @param columnName
     * @param x      An object that contains the data to set the parameter value to.
     * @param length
     * @throws SQLException if the columnIndex is not valid; 
     * if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; this method is called on a closed result set,  
     * if a database access error occurs or
     * the result set concurrency is <code>CONCUR_READ_ONLY</code> 
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public void updateNClob (String columnName, java.io.Reader x, long length) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNClob");
        updateNClob (findColumnX (columnName), x, length);
    }

    /**
     * Updates the designated column with a <code>String</code> value.
     * It is intended for use when updating <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> columns.
     *
     * @param column 
     * @param x    The value for the column to be updated
     * @throws SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; or if a database access error occurs
     */
    public void updateNString (int column, String x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNString" );
        checkUpdatePreconditions (column);
    }

    /**
     * Updates the designated column with a <code>String</code> value.
     * It is intended for use when updating <code>NCHAR</code>,<code>NVARCHAR</code>
     * and <code>LONGNVARCHAR</code> columns.
     *
     * @param columnName 
     * @param x    The value for the column to be updated
     * @throws SQLException if the driver does not support national
     *         character sets;  if the driver can detect that a data conversion
     *  error could occur; or if a database access error occurs
     */
    public void updateNString (String columnName, String x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateNString" );
        updateNString (findColumnX (columnName), x);
    }

     /**
      * Updates the designated column with a <code>RowId</code> value. 
      * 
      * @param column
      * @param x the column value
      * @throws SQLException if a database access occurs 
      */
/* ifdef JDBC40 */

    public void updateRowId (int column, java.sql.RowId x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateRowId");
        checkUpdatePreconditions (column);
    }
/* endif */ 
     /**
      * Updates the designated column with a <code>RowId</code> value. 
      * 
      * @param columnName
      * @param x the column value
      * @throws SQLException if a database access occurs 
      */
/* ifdef JDBC40 */
    public void updateRowId (String columnName, java.sql.RowId x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateRowId");
        updateRowId (findColumnX (columnName), x);
    }
/* endif */ 
     /**
      * Updates the designated column with a <code>java.sql.SQLXML</code> value.
      *
      * @param column
      * @param x    The value for the column to be updated
      * @throws SQLException if a database access error occurs
      */
/* ifdef JDBC40 */

    public void updateSQLXML (int column, java.sql.SQLXML x) throws java.sql.SQLException
    {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateSQLXML");
        checkUpdatePreconditions (column);
    }
/* endif */ 
     /**
      * Updates the designated column with a <code>java.sql.SQLXML</code> value.
      *
      * @param columnName
      * @param x    The value for the column to be updated
      * @throws SQLException if a database access error occurs
      */
/* ifdef JDBC40 */

    public void updateSQLXML (String columnName, java.sql.SQLXML x) throws java.sql.SQLException
    {
        updateSQLXML (findColumnX (columnName), x);
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "updateSQLXML");
    }
/* endif */ 
    

        // jdbc4.1
/* ifdef JDBC40 */
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
      // TODO TODOJDBC41 Auto-generated method stub
      return null;
    }
/* endif */ 

    // jdbc4.1
/* ifdef JDBC40 */
    public <T> T getObject(String columnLabel, Class<T> type)
        throws SQLException {
      // TODO TODOJDBC41 Auto-generated method stub
      return null;
    } 
/* endif */ 

    
    protected String[] getValidWrappedList()
    {
        return new String[] {  "com.ibm.as400.access.AS400JDBCArrayResultSet",  "java.sql.ResultSet" };
    } 


    
    
}
