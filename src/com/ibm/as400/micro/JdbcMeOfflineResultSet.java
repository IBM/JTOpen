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
import java.io.*;

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
     *
     *  The offline data must have been created by JdbcMe (for
     *  example via JdbcMeStatement.executeToMIDPDB() or
     *  JdbcMeStatement.executeToPalmDB()).
     *
     *  No Connection or Statement is required when
     *  accesing a JdbcMeOfflineResultSet.
     *
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
     *
     *  No Connection or Statement is required when
     *  accesing a JdbcMeOfflineResultSet.
     *
     *  An imported offline data store (Palm DB or
     *  MIDP record store) is created by
     *  some other application. The caller indicates the number
     *  of columns and the format of the data using the
     *  numColumns and columnTypes parameters.
     *
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
     *  Close the ResultSet and the Offline database.
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
     *
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
     *
     *  <b>Note:</b> This method is currently not implemented and
     *  will throw a JdbcMeException.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       the result set is not updatable,
     *                       or an error occurs.
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
     *
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
     *  @param  rowNumber   The relative row number.  If the relative row
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
     *  @param  rowNumber   The absolute row number.  If the absolute row
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
     * 
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
     *
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
     *
     *  <b>Note:</b> This methos is currently not implemented.
     *
     *  @exception JdbcMeException If the result set is not open,
     *                   the result set is not updatable,
     *                   the cursor is not positioned on a row,
     *                   the cursor is positioned on the insert row,
     *                   or an error occurs.
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
     *
     *  <b>Note:</b> This method returns null, no JdbcMeOfflineResultSet
     *  is ever owned by a statement.
     *
     *  @return The statement for this result set, or null if the
     *          result set was returned by a DatabaseMetaData
     *          catalog method.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public Statement getStatement()
    {
        return null;
    }

    /**
     *  Dump out the current database (Using System.out.println).
     *  Optionally, convert the hex values to Strings so they
     *  are easier to view.
     *  This is most useful in the emulator.
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
     *  This is most useful in the emulator.
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
     *  The first row is 1, negative values BEFORE_FIRST_ROW
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
     *
     *  All offline database result sets are TYPE_SCROLL_SENSITIVE.
     *
     *  @return The result set type.
     *
     *  @exception JdbcMeException If the result set is not open.
     **/
    public int getType() throws JdbcMeException 
    {
        return TYPE_SCROLL_SENSITIVE;
    }

    /**
     *  Return the concurrency of the result set.
     *
     *  All offline database result sets are CONCUR_UPDATABLE.
     *
     *  @return The result set concurrency.
     *
     *  @exception JdbcMeException If the result set is not open.
     **/
    public int getConcurrency() throws JdbcMeException 
    {
        return CONCUR_UPDATABLE;
    }
}
