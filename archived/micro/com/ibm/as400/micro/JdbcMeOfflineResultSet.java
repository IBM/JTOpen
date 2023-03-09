///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeOfflineResultSet.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.sql.*;
import java.util.Calendar;
import java.util.Map;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;

/**
 * JdbcMeOfflineResultSet implements the java.sql.ResultSet
 * interface as provided for a Java 2 Micro-Edition device
 * over the top of an existing MIDP Record Store or a Palm
 * OS database.
 *
 *  <p><b>Note:</b> Since Java 2 Micro-Edition does not include java.sql,
 *  JdbcMeOfflineResultSet implements the java.sql package that is also part 
 *  of this driver.
 **/
public class JdbcMeOfflineResultSet implements ResultSet
{
    static final int dbEyeCatcher_ = 0x4a444243; // JDBC
    private final static String hexDigits = "0123456789ABCDEF";

    private boolean imported_ = false;
    private int numColumns_ = 0;
    private int columnTypes_[] = null;

    /**
     * Row position is before the first row of the result set.
     */
    public final static int BEFORE_FIRST_ROW = -1;
    /**
     * Row position is after the last row of the result set.
     */
    public final static int AFTER_LAST_ROW   = -2;

    private int currentRSRow_ = BEFORE_FIRST_ROW;
    private int firstRowMIDPDBIndex_ = -1;
    private int lastRowMIDPDBIndex_ = -1;
    private JdbcMeOfflineData DB_;

    // Keep track of these
    private String dbName_ = null;
    private int dbCreator_ = 0;
    private int dbType_    = 0;


    /**
     *  Construct a result set based on the offline data indicated.
     *  <p>
     *  The offline data must have been created by JdbcMe (for
     *  example via JdbcMeStatement.executeToMIDPDB() or
     *  JdbcMeStatement.executeToPalmDB()).
     *  <p>
     *  No Connection or Statement is required when
     *  accessing a JdbcMeOfflineResultSet.
     *  <p>
     *  If the RecordStore was not created with
     *  JdbcMeStatement.executeToMIDPDB(), the
     *  constructor JdbcMeOfflineResultSet(String, int, int, int, int[])
     *  should be used to allow you to define meta-data
     *  about the imported database.
     *
     *  @param dbName  The name of the offline database.
     *  @param dbCreator The unique offline database creator identifier.
     *  @param dbType  The unique offline database type identifier.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public JdbcMeOfflineResultSet(String dbName, int dbCreator, int dbType) throws JdbcMeException 
    {
        completeInitialization(dbName, dbCreator, dbType);
    }

    /**
     *  Construct a result set based on the imported offline
     *  data records indicated.
     *  <p>
     *  No Connection or Statement is required when
     *  accesing a JdbcMeOfflineResultSet.
     *  <p>
     *  An imported offline data store (Palm DB or
     *  MIDP record store) is created by
     *  some other application. The caller indicates the number
     *  of columns and the format of the data using the
     *  numColumns and columnTypes parameters.
     *  <p>
     *  The offline data must be a proper result set (i.e. number
     *  of records in the offline data must evenly divisible by
     *  the number of columns).
     *  The length of the 'columnTypes' array must equal
     *  the 'numColumns' parameter.
     *
     *  @param dbName  The name of the offline database.
     *  @param dbCreator The unique offline database creator identifier.
     *  @param dbType  The unique offline database type identifier.
     *  @param numColumns The number of columns.
     *  @param columnTypes The column types.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public JdbcMeOfflineResultSet(String dbName, int dbCreator, int dbType, int numColumns, int columnTypes[]) throws JdbcMeException 
    {
        imported_ = true;
        numColumns_ = numColumns;
        columnTypes_ = new int[numColumns];
        System.arraycopy(columnTypes_, 0, columnTypes_, 0, numColumns_);
        completeInitialization(dbName, dbCreator, dbType);
    }

    /**
     *  Open the offline data, and do whatever processing is required
     *
     *  @param dbName  The name of the offline database.
     *  @param dbCreator The unique offline database creator identifier.
     *  @param dbType  The unique offline database type identifier.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    private void completeInitialization(String dbName, int dbCreator, int dbType) throws JdbcMeException 
    {
        dbName_ = dbName;
        dbCreator_ = dbCreator;
        dbType_ = dbType;

        DB_ = JdbcMeOfflineData.open(dbName, dbCreator, dbType, false);
        int numberOfRows = DB_.size();

        if (imported_)
        {
            if (numberOfRows % numColumns_ != 0)
                throw new JdbcMeException("Offline DB NumRecords % numColumns != 0", null);

            firstRowMIDPDBIndex_ = 0;
            lastRowMIDPDBIndex_ = numberOfRows-1;
            return;
        }

        byte data[] = null;
        int val;

        if (numberOfRows < 4)
            throw new JdbcMeException("Invalid Offline DB, number of rows=" + numberOfRows, null);

        data = DB_.getRecord(0);    // First record eye catcher
        val = 0;
        val |= (data[0] << 24) & 0xFF000000;
        val |= (data[1] << 16) & 0x00FF0000;
        val |= (data[2] <<  8) & 0x0000FF00;
        val |= (data[3] <<  0) & 0x000000FF;

        if (val != dbEyeCatcher_)
            throw new JdbcMeException("Offline DB eyecatcher invalid:" + val, null);

        data = DB_.getRecord(1);    // Second record is JdbcMe version
        val = 0;
        val |= (data[0] << 24) & 0xFF000000;
        val |= (data[1] << 16) & 0x00FF0000;
        val |= (data[2] <<  8) & 0x0000FF00;
        val |= (data[3] <<  0) & 0x000000FF;
        int version = val;         // Currently, only v1 supported.

        if (version != 1)
            throw new JdbcMeException("Offline DB version " + version + "not supported", null);

        data = DB_.getRecord(2);    // Third record is number of columns
        val = 0;
        val |= (data[0] << 24) & 0xFF000000;
        val |= (data[1] << 16) & 0x00FF0000;
        val |= (data[2] <<  8) & 0x0000FF00;
        val |= (data[3] <<  0) & 0x000000FF;
        numColumns_ = val;

        columnTypes_ = new int[numColumns_];
        data = DB_.getRecord(3);    // Fourth record is column types

        for (int i=0; i<numColumns_; ++i)
        {
            columnTypes_[i] = 0;
            columnTypes_[i] |= (data[i*4+0] << 24) & 0xFF000000;
            columnTypes_[i] |= (data[i*4+1] << 16) & 0x00FF0000;
            columnTypes_[i] |= (data[i*4+2] <<  8) & 0x0000FF00;
            columnTypes_[i] |= (data[i*4+3] <<  0) & 0x000000FF;
        }

        // Fourth record is the first data item (col1, record1)
        firstRowMIDPDBIndex_ = 4;
        lastRowMIDPDBIndex_  = numberOfRows-1;

        if (((numberOfRows-4) % numColumns_) != 0)
            throw new JdbcMeException("Offline DB DataRecords % numColumns != 0", null);
    }


    /**
     *  Close the ResultSet and the offline database.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public void close() throws JdbcMeException 
    {
        columnTypes_ = null;
        currentRSRow_ = BEFORE_FIRST_ROW;

        if (DB_ != null)
        {
            DB_.close();
            DB_ = null;
        }
    }

    /**
     *  Delete the current row.
     *
     *  @exception JdbcMeException If the result set is not open,
     *                   the result set is not updatable,
     *                   the cursor is not positioned on a row,
     *                   the cursor is positioned on the insert row,
     *                   or an error occurs.
     **/
    public void deleteRow() throws JdbcMeException 
    {
        if (currentRSRow_ < 0)
            throw new JdbcMeException("Not positioned on a row", null);

        // The result set row is made up of numColumn
        // records in the offline data DB.
        int firstTargetColumn = firstRowMIDPDBIndex_ + (numColumns_ * currentRSRow_);

        for (int i=0; i<numColumns_; ++i)
        {
            // Repeat deleting the same record numColumns
            // times, because subsequent record indexes will
            // be moved up by one after each delete.
            DB_.deleteRecord(firstTargetColumn);
        }
        return;
    }


    /**
     *  Returns the value of a column as a String object.
     *  This can be used to get values from columns with any SQL type.
     *
     *  @param  columnIndex   The column index (1-based).
     *  @return               The column value or null if the value is SQL NULL.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the cursor is not positioned on a row,
     *                       the column index is not valid, or the
     *                       requested conversion is not valid.
     **/
    public String getString(int columnIndex) throws JdbcMeException 
    {
        if (currentRSRow_ < 0)
            throw new JdbcMeException("Not positioned on a row", null);

        if (columnIndex < 1 || columnIndex > numColumns_)
            throw new JdbcMeException("RS bad column " + columnIndex, null);

        int   targetRow = firstRowMIDPDBIndex_ + (numColumns_ * currentRSRow_);

        byte  data[] = DB_.getRecord(targetRow+(columnIndex-1));
        String s = new String(data);
        return s;
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
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the cursor is not positioned on a row,
     *                       the column index is not valid, or the
     *                       requested conversion is not valid.
     **/
    public int getInt(int columnIndex) throws JdbcMeException 
    {
        // Since each column is actually stored as a string
        // anyway, we'll reuse this method even though
        // the number of objects created is a little more than
        // what is desired.
        String s = getString(columnIndex);
        // Attempt to convert existing string or object
        // to an integer.
        try
        {
            int  i = Integer.parseInt(s);
            return i;
        }
        catch (NumberFormatException e)
        {
            throw new JdbcMeException("Incorrect conversion to int: " + s, null);
        }
    }


    /**
     *  Inserts the contents of the insert row into the result set
     *  and the database.
     *  <p>
     *  <b>Note:</b> This method is currently not implemented and
     *  will throw a JdbcMeException.
     *  
     *  @exception JdbcMeException If the result set is not open,
     *                   the result set is not updatable,
     *                   the cursor is not positioned on the insert row,
     *                   a column that is not nullable was not specified,
     *                   or an error occurs.
     **/
    public void insertRow() throws JdbcMeException 
    {
        throw new JdbcMeException("unimplemented", null);
    }

    /**
     *  Positions the cursor to the insert row.
     *  If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *  <p>
     *  <b>Note:</b> This method is currently not implemented and
     *  will throw a JdbcMeException.
     *
     *  @exception  JdbcMeException    Always thrown.  This method is
     *               not implemented in an offline result set.
     **/
    public void moveToInsertRow() throws JdbcMeException 
    {
        throw new JdbcMeException("unimplemented", null);
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
     *  <p>
     *  <b>Note:</b> This method is currently not implemented and
     *  will throw a JdbcMeException.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    public void moveToCurrentRow() throws JdbcMeException 
    {
        throw new JdbcMeException("unimplemented", null);
    }


    /**
     *  Positions the cursor to the next row.
     *  If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @return     true if the requested cursor position is valid; false
     *       if there are no more rows.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       or an error occurs.
     **/
    public boolean next() throws JdbcMeException 
    {
        return relative(1);
    }

    /**
     *  Positions the cursor to the previous row.
     *  If an InputStream from the current row is open, it is implicitly
     *  closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @return    true if the requested cursor position is
     *                  valid; false otherwise.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    public boolean previous() throws JdbcMeException 
    {
        return relative(-1);
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
     *  @param  rows   The relative row number.  If the relative row
     *               number is positive, this positions the cursor
     *               after the current position.  If the relative
     *               row number is negative, this positions the
     *               cursor before the current position.  If the
     *               relative row number is 0, then the cursor
     *               position does not change.
     *
     *  @return             true if the requested cursor position is valid, false otherwise.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       the cursor is not positioned on a valid row,
     *                       or an error occurs.
     **/
    public boolean relative(int rows) throws JdbcMeException 
    {
        int newRow = 0;

        if (lastRowMIDPDBIndex_ < firstRowMIDPDBIndex_)
            return false; // No data

        // Special case if we are before the first row.
        if (currentRSRow_ == BEFORE_FIRST_ROW)
        {
            // Can't go any earlier
            if (rows <= 0)
                return false;

            // rows is posetive so set newRow = row-1, (if rows == 1,
            // we want to stay on the first row. (internally stored as 0
            // store the first row as 0);
            --rows;
            newRow = rows;
            // See if we went too many rows (past the last row).
            if ((firstRowMIDPDBIndex_ + (newRow * numColumns_)) > lastRowMIDPDBIndex_)
            {
                currentRSRow_ = AFTER_LAST_ROW;
                return false;
            }
            currentRSRow_ = newRow;
            return true;
        }
        if (currentRSRow_ == AFTER_LAST_ROW)
        {
            if (rows >= 0)
                return false;

            // rows is negative so we want to add rows+1 to the
            // last row (i.e. if rows == -1, then we want to stay
            // on the last row.
            ++rows;
            newRow = ((lastRowMIDPDBIndex_ - firstRowMIDPDBIndex_) / numColumns_)+rows;
            if (newRow < 0)
            {
                currentRSRow_ = BEFORE_FIRST_ROW;
                return false;
            }
            currentRSRow_ = newRow;
            return true;
        }

        newRow = currentRSRow_ + rows;
        if (newRow < 0)
        {
            currentRSRow_ = BEFORE_FIRST_ROW;
            return false;
        }
        if ((firstRowMIDPDBIndex_ + (newRow * numColumns_)) > lastRowMIDPDBIndex_)
        {
            currentRSRow_ = AFTER_LAST_ROW;
            return false;
        }
        currentRSRow_ = newRow;
        return true;
    }

    /**
     *  Positions the cursor to the first row.
     *  If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @return             true if the requested cursor position is
     *               valid; false otherwise.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    public boolean first() throws JdbcMeException 
    {
        return absolute(1);
    }

    /**
     *  Positions the cursor to the last row.
     *  If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @return             true if the requested cursor position is
     *               valid; false otherwise.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    public boolean last() throws JdbcMeException 
    {
        return absolute(-1);
    }

    /**
     *  Positions the cursor to an absolute row number.
     *
     *  <p>Attempting to move any number of positions before
     *  the first row positions the cursor to before the first row. 
     *  Attempting to move beyond the last
     *  row positions the cursor after the last row.
     *
     *  <p>If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @param  row   The absolute row number.  If the absolute row
     *               number is positive, this positions the cursor
     *               with respect to the beginning of the result set.
     *               If the absolute row number is negative, this
     *               positions the cursor with respect to the end
     *               of result set.
     *
     *  @return             true if the requested cursor position is
     *               valid; false otherwise.
     *
     *  @exception JdbcMeException  If the result set is not open,
     *                    the result set is not scrollable,
     *                    the row number is 0,
     *                    or an error occurs.
     **/
    public boolean absolute(int row) throws JdbcMeException 
    {
        int newRow = 0;
        if (row > 0)
        {
            // rows is posetive so set newRow = row
            // row of 1 takes us to first row. We store rows internally
            // starting at 0.
            --row;
            newRow = row;
            // See if we went too many rows (past the last row).
            if ((firstRowMIDPDBIndex_ + (newRow * numColumns_)) > lastRowMIDPDBIndex_)
            {
                currentRSRow_ = AFTER_LAST_ROW;
                return false;
            }
            currentRSRow_ = newRow;
            return true;
        }
        if (row < 0)
        {
            // rows is negative so we want to add rows+1 to the
            // last row (i.e. if rows == -1, then we want to stay
            // on the last row.
            ++row;
            newRow = ((lastRowMIDPDBIndex_ - firstRowMIDPDBIndex_) / numColumns_)+row;
            if (newRow < 0)
            {
                currentRSRow_ = BEFORE_FIRST_ROW;
                return false;
            }
            currentRSRow_ = newRow;
            return true;
        }
        throw new JdbcMeException("ResultSet position absolute 0", null);
    }

    /**
     *  Updates a column in the current row using a String value.
     *  <p>
     *  The updateString for the JdbcMeOfflineResultSet works
     *  rather differently than a standard result set,
     *  it updates the column specified by the 'columnIndex' parameter
     *  IMMEDIATELY, and IN PLACE. It does not require
     *  movement out of the current row, nor does it cause
     *  any cursor movement. It simply updates the value.
     *
     *  @param  columnIndex   The column index (1-based).
     *  @param  value   The column value or null to update
     *                             the value to SQL NULL.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not updatable,
     *                       the cursor is not positioned on a row,
     *                       the column index is not valid,
     *                       or the requested conversion is not valid.
     **/
    public void updateString(int columnIndex, String value) throws JdbcMeException 
    {
        if (currentRSRow_ < 0)
            throw new JdbcMeException("Not positioned on a row", null);

        if (columnIndex < 1 || columnIndex > numColumns_)
            throw new JdbcMeException("RS bad column " + columnIndex, null);

        int   targetRow = firstRowMIDPDBIndex_ + (numColumns_ * currentRSRow_);

        byte     data[] = value.getBytes();
        DB_.setRecord(targetRow+(columnIndex-1), data, 0, data.length);
        // This doesn't appear to work for Palm OS.
        // Some hints have suggested
        // that DB records don't always get flushed correctly
        // unless the database was closed and opened. We'll try it.
        DB_.close();
        DB_ = null;
        DB_ = JdbcMeOfflineData.open(dbName_, dbCreator_, dbType_, false);
        return;
    }

    /**
     *  Updates a column in the current row using a Java int value.
     *  <p>
     *  The updateInt for the JdbcMeOfflineResultSet works
     *  rather differently than a standard result set,
     *  it updates the column specified by the 'columnIndex' parameter
     *  IMMEDIATELY, and IN PLACE. It does not require
     *  movement out of the current row, nor does it cause
     *  any cursor movement. It simply updates the value.
     *
     *  @param  columnIndex   The column index (1-based).
     *  @param  value   The column value.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not updatable,
     *                       the cursor is not positioned on a row,
     *                       the column index is not valid, or the
     *                       requested conversion is not valid.
     **/
    public void updateInt(int columnIndex, int value) throws JdbcMeException 
    {
        // Since each column is actually stored as a string
        // anyway, we'll reuse this method.
        updateString(columnIndex, Integer.toString(value));
    }

    /**
     *  Updates the database with the new contents of the current row.
     *  <p>
     *  <b>Note:</b> This method is currently not implemented.
     *
     *  @exception JdbcMeException This exception is never thrown.
     **/
    public void updateRow() throws JdbcMeException 
    {
        return;
    }

    /**
     *  Returns the ResultSetMetaData object that describes the result set's columns.
     * 
     *  @return     The metadata object.
     *
     *  @exception  JdbcMeException    If an error occurs.
     **/
    public ResultSetMetaData getMetaData() throws JdbcMeException 
    {
        return new JdbcMeResultSetMetaData(numColumns_, columnTypes_);
    }


    /**
     *  Returns the statement for this result set.
     *  <p>
     *  <b>Note:</b> This method returns null, no JdbcMeOfflineResultSet
     *  is ever owned by a statement.
     *
     *  @return The statement for this result set, or null if the
     *          result set was returned by a DatabaseMetaData
     *          catalog method.
     *
     *  @exception JdbcMeException This exception is never thrown.
     **/
    public Statement getStatement() throws JdbcMeException
    {
        return null;
    }

    /**
     *  Dump out the current database (Using System.out.println).
     *  Optionally, convert the hex values to Strings so they
     *  are easier to view.
     *  
     *  @param dumpAsStrings true to convert the database hex values to strings; false otherwise.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public void dumpDB(boolean dumpAsStrings) throws JdbcMeException 
    {
        dumpDB(null, dumpAsStrings);
    }

    /**
     *  Dump out the specified database.
     *  Optionally, convert the values of each record
     *  to Strings so they are easier to view.
     *
     *  @param db The database.
     *  @param dumpAsStrings true to convert the database hex values to strings; false otherwise.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public void dumpDB(JdbcMeOfflineData db, boolean dumpAsStrings) throws JdbcMeException 
    {
        if (db == null)
            db = DB_;

        int numberOfRows_ = db.size();
        byte data[] = null;

        for (int i=0; i<numberOfRows_; ++i)
        {
            data = db.getRecord(i);
            dumpBytes(i, data);
            if (dumpAsStrings)
            {
                // Dump out the values of the strings if appropriate
                // for a JdbcMe created DB, the first 4 values
                // are not strings.
                // Any other DB, we'll simply do what the user
                // asked us to.
                if (db != DB_ || imported_ || i >= 4)
                {
                    // Attempt to dump as a string.
                    System.out.println("     " + new String(data));
                }
            }
        }
    }

    /**
     *  Return the number of ResultSet rows in the
     *  offline result set. This number is not the same as the number
     *  of records in the offline data DB.
     *
     *  @return The number of rows.
     **/
    public int numberOfRows()
    {
        return(lastRowMIDPDBIndex_ - firstRowMIDPDBIndex_) / numColumns_;
    }

    /**
     *  Return the number of the current row in the offline DB.
     *  The first row is 1, BEFORE_FIRST_ROW
     *  or AFTER_LAST_ROW may also be returned.
     *
     *  @return The current row index.
     **/
    public int getCurrentRow()
    {
        return currentRSRow_ + 1;
    }

    /**
     *  Dump out a byte array with a row label so that
     *  it may be used for debug purposes. This is most
     *  useful in the emulator.
     *
     *  @param row The row label.
     *  @param data The byte data.
     **/
    private void dumpBytes(int row, byte data[])
    {
        System.out.print("#" + row + " : 0x ");
        int      val;
        for (int i=0; i<data.length; ++i)
        {
            val = (((int)data[i]) & 0xF0) >> 4;
            System.out.print(hexDigits.charAt(val));
            val = (((int)data[i]) & 0x0F) >> 0;
            System.out.print(hexDigits.charAt(val));

            if ((i+1)%4==0)
                System.out.print(" ");
        }
        System.out.println();
    }

    /**
     *  Return the type of result set.
     *  <p>
     *  All offline database result sets are TYPE_SCROLL_SENSITIVE.
     *
     *  @return The result set type, which is always TYPE_SCROLL_SENSITIVE.
     *
     *  @exception JdbcMeException If the result set is not open.
     **/
    public int getType() throws JdbcMeException 
    {
        return TYPE_SCROLL_SENSITIVE;
    }

    /**
     *  Return the concurrency of the result set.
     *  <p>
     *  All offline database result sets are CONCUR_UPDATABLE.
     *
     *  @return The result set concurrency, which is always CONCUR_UPDATABLE.
     *
     *  @exception JdbcMeException If the result set is not open.
     **/
    public int getConcurrency() throws JdbcMeException 
    {
        return CONCUR_UPDATABLE;
    }

	public void afterLast() throws SQLException {
			throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void beforeFirst() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void cancelRowUpdates() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void clearWarnings() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public int findColumn(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
	}

	public Array getArray(int i) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Array getArray(String colName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public InputStream getAsciiStream(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public BigDecimal getBigDecimal(String columnName, int scale)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public InputStream getBinaryStream(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Blob getBlob(int i) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Blob getBlob(String colName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean getBoolean(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public byte getByte(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public byte getByte(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public byte[] getBytes(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Clob getClob(int i) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Clob getClob(String colName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public String getCursorName() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Date getDate(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Date getDate(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public double getDouble(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public double getDouble(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public int getFetchDirection() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public int getFetchSize() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public float getFloat(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public float getFloat(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public int getInt(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public long getLong(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public long getLong(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Object getObject(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Object getObject(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Object getObject(int i, Map map) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Object getObject(String colName, Map map) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Ref getRef(int i) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Ref getRef(String colName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public int getRow() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public short getShort(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public short getShort(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public String getString(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Time getTime(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Time getTime(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public Timestamp getTimestamp(String columnName, Calendar cal)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public URL getURL(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public URL getURL(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public InputStream getUnicodeStream(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public SQLWarning getWarnings() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean isAfterLast() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean isBeforeFirst() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean isFirst() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean isLast() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void refreshRow() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean rowDeleted() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean rowInserted() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean rowUpdated() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void setFetchDirection(int direction) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void setFetchSize(int rows) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateArray(String columnName, Array x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateAsciiStream(String columnName, InputStream x, int length)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBigDecimal(String columnName, BigDecimal x)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBinaryStream(String columnName, InputStream x, int length)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBlob(String columnName, Blob x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateByte(String columnName, byte x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateCharacterStream(String columnName, Reader reader,
			int length) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateClob(String columnName, Clob x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateDate(String columnName, Date x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateDouble(String columnName, double x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateFloat(String columnName, float x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateInt(String columnName, int x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateLong(String columnName, long x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateNull(int columnIndex) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateNull(String columnName) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateObject(String columnName, Object x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateObject(int columnIndex, Object x, int scale)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateObject(String columnName, Object x, int scale)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateRef(String columnName, Ref x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateShort(String columnName, short x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateString(String columnName, String x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateTime(String columnName, Time x) throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public void updateTimestamp(String columnName, Timestamp x)
			throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}

	public boolean wasNull() throws SQLException {
		throw new java.sql.SQLException("NOT AVAILABLE IN MICROEDITION"); 
		
	}
}
