///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLMetaDataTableModel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;



/**
The SQLMetaDataTableModel class represents the meta data
about the columns of a set of database files.  This model
is used to create a SQLMetaDataTablePane.
The rows of this table are the fields of the database files,
and the columns are the field attributes (field name, length, etc).
Rows can be removed from the table so the table only shows a subset
of the available database fields.
This class is used by SQLQueryBuilderPane and its coworkers.

<p>The data in the model is retrieved from the system only
when <i>load()</i> is called.

<p>SQLMetaDataTableModel objects generate the following events:
<ul>
  <li>ErrorEvent
  <li>TableModelEvent
  <li>WorkingEvent
</ul>
**/
class SQLMetaDataTableModel
extends AbstractTableModel
implements Cloneable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// Note that none of the JDBC resources are ever explicitly closed,
// but rather garbage collection is relied upon to close them.

// This class is not meant to be serialized, it should be transient.
// This class has items marked transient even though it is not
// serializable because otherwise errors were received when
// serializing objects that contained this class (even though they
// were transient instances.  readObject() was added to be safe.

// Number of columns in model.
static private int NUM_COLUMNS_ = 6;

// The columns of the table contains these meta data.
public static int FIELD_NAME_ = 0;
public static int FIELD_TYPE_ = 1;
public static int FIELD_LENGTH_ = 2;
public static int FIELD_DECIMALS_ = 3;
public static int FIELD_NULLS_ = 4;
public static int FIELD_DESC_ = 5;

// What this table represents
transient private Connection connection_ = null;
private String[] tables_ = null;  // The DB tables for which this model contains data.

// The table data
transient private String[][] data_ = new String[0][NUM_COLUMNS_]; // table data
transient private int[] types_ = new int[0];            // sql types
// Row information
transient private int numRows_ = 0;          // Number of rows in the table.

// Flag for if an error event was sent.
transient private boolean error_;

// Event support.
transient private ErrorEventSupport errorListeners_
    = new ErrorEventSupport (this);
transient private WorkingEventSupport workingListeners_
    = new WorkingEventSupport (this);



/**
Constructs a SQLMetaDataTableModel object.  The query is not done
until <i>load</i> is done.

@param       connection  The SQL connection.
@param       tables      The database files to retrieve info about.
                        Tables should be in the form of <library>.<file>.
**/
public SQLMetaDataTableModel (Connection connection,
                         String[] tables)
{
    super();
    connection_ = connection;
    tables_ = tables;
}


/**
Adds a listener to be notified when an error occurs.
The listener's errorOccurred method will be called.

@param  listener  The listener.
**/
public void addErrorListener (ErrorListener listener)
{
    errorListeners_.addErrorListener (listener);
}



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
public void addWorkingListener (WorkingListener listener)
{
    workingListeners_.addWorkingListener (listener);
}



/**
Makes a clone (copy) of this table.  No data (references) is shared
between this original and the new object.  Listeners are not copied
to the new object.
**/
synchronized public Object clone ()
{
    // create new object
    SQLMetaDataTableModel clone =
        new SQLMetaDataTableModel(connection_, tables_);
    // copy table data
    clone.data_ = new String[data_.length][NUM_COLUMNS_];
    System.arraycopy(data_, 0, clone.data_, 0 , data_.length);
    clone.numRows_ = numRows_;
    clone.types_ = new int[types_.length];
    System.arraycopy(types_, 0, clone.types_, 0 , types_.length);

    return clone;
}


/**
Returns the number of columns in the table.

@return The number of columns in the table.
**/
public int getColumnCount()
{
    return NUM_COLUMNS_;
}


/**
Returns the number of rows in the table.

@return The number of rows in the result set.
**/
synchronized public int getRowCount()
{
    return numRows_;
}


/**
Return the SQL type of the field.  Note this is not the same as
the data in FIELD_TYPE_, which is the database-dependent type name.
@see java.sql.Types

@param index The row for which to get the type.
@return The SQL type of the field.
**/
synchronized int getSQLType(int index)
{
    return types_[index];
}


/**
Returns the database file names this table contains data for.

@return The database file names this table contains data for.
**/
public String[] getTables ()
{
    return tables_;
}


/**
Returns the value at the specifed column and row.

@param  rowIndex            The row index.  Indices start at 0.
@param  columnIndex    The column index.  Indices start at 0.

@return The value at the specified column and row.
**/
synchronized public Object getValueAt (int rowIndex,
                          int columnIndex)
{
    error_ = false;

    // return the value
    return data_[rowIndex][columnIndex];
}




/**
Loads the table based on the state of the system.
**/
public void load ()
{
    synchronized (this)
    {
        // Set back fields in case there is an error.
        data_ = new String[0][0];
        types_ = new int[0];
        numRows_ = 0;
    }
    // notify listeners that we've changed number of rows.
    TableModelEvent event = new TableModelEvent(this, -1);
    fireTableChanged(event);
    Trace.log(Trace.DIAGNOSTIC, "Starting load, changed number of rows to:", numRows_);

    if (tables_ == null || tables_.length == 0 ||
        connection_ == null)
    {
        //If no tables, the panel will be disabled, don't throw
        //error, just leave table empty.
        return;
    }

    synchronized (this)
    {
        workingListeners_.fireStartWorking ();

        // Number of rows we create our table with and number of
        // rows we bump our capacity by each time we run out of room.
        int ROW_INCREMENT = 50;

        ResultSet resultSet = null;
        try
        {
            // Get database meta data
            DatabaseMetaData metaData = connection_.getMetaData();

            // Create new array to hold table values.
            data_ = new String[ROW_INCREMENT][NUM_COLUMNS_];
            types_ = new int[ROW_INCREMENT];

            // Loop through each database file.
            String library, table, tprefix;
            int sepIndex;
            int curRow;
            for (int i=0; i<tables_.length; ++i)
            {
                // Get meta data.
                sepIndex = tables_[i].indexOf(".");
                if (sepIndex == -1)
                {
                    // Incorrect table specification, send error
                    // and continue to next table.
                    // Create generic exception to hold error message
                    Exception e = new Exception(ResourceLoader.getText("EXC_TABLE_SPEC_NOT_VALID"));
                    errorListeners_.fireError(e);
                }
                else
                {
                    library = tables_[i].substring(0, sepIndex);
                    table = tables_[i].substring(sepIndex+1);
                    if (tables_.length > 1)
                        tprefix = table + "."; // need to qualify field names
                    else
                        tprefix = "";  // only 1 table, can just use field names
                    resultSet = metaData.getColumns(null, library, table, null);

                    // Loop through fields for this database file.
                    while (resultSet.next())
                    {
                        curRow = numRows_; // current row in table

                        // make sure we have room in table for this row.
                        if (curRow >= data_.length)                         // @D1C
                        {
                            String[][] newData =
                                new String[data_.length + ROW_INCREMENT][NUM_COLUMNS_];
                            System.arraycopy(data_, 0, newData, 0, data_.length);
                            data_ = newData;
                            int[] newTypes =
                                new int[types_.length + ROW_INCREMENT];
                            System.arraycopy(types_, 0, newTypes, 0, types_.length);
                            types_ = newTypes;
                        }

                        // Store SQL type for use by getSQLType,
                        // although this is not externalized in the table.
                        types_[curRow] = resultSet.getInt(5);

                        // Add field info to table
                        data_[curRow][FIELD_NAME_] = tprefix + resultSet.getString(4).trim();
                        data_[curRow][FIELD_TYPE_] = resultSet.getString(6);
                        // The following code should not be necessary when using
                        // most drivers, but makes the length values correct
                        // when using the i5/OS JDBC driver.
                        // These values came from the ODBC description of precision
                        // (in 2.0 ref, Appendix D page 624).
                        switch (types_[curRow])
                        {
                            case Types.SMALLINT:
                                data_[curRow][FIELD_LENGTH_] = "5";
                                break;
                            case Types.INTEGER:
                                data_[curRow][FIELD_LENGTH_] = "10";
                                break;
                            case Types.TIME:
                                data_[curRow][FIELD_LENGTH_] = "8";
                                break;
                            case Types.TIMESTAMP:
                                // We always give length = 23, even though
                                // we should give 19 if there is no decimals.
                                // In order to not mess up 'correct' values,
                                // only change it if we know the value is bad.
                                if (resultSet.getInt(7) == 10)
                                    data_[curRow][FIELD_LENGTH_] = "23";
                                break;
                            case Types.DATE:
                                data_[curRow][FIELD_LENGTH_] = "10";
                                break;
                            case Types.DOUBLE:
                                if (resultSet.getInt(7) == 4)
                                    // single precision (type REAL)
                                    data_[curRow][FIELD_LENGTH_] = "7";
                                else
                                    // double precison (type FLOAT)
                                    data_[curRow][FIELD_LENGTH_] = "15";
                                break;
                            default:
                                // Other types are correct.
                                data_[curRow][FIELD_LENGTH_] = resultSet.getString(7);
                        }
                        data_[curRow][FIELD_DECIMALS_] = resultSet.getString(9);
                        data_[curRow][FIELD_NULLS_] = resultSet.getString(18);
                        data_[curRow][FIELD_DESC_] = resultSet.getString(12);

                        numRows_++;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            // In case of error, set fields to init state
            data_ = new String[0][0];
            types_ = new int[0];
            numRows_ = 0;
            errorListeners_.fireError(e);
            error_ = true;
        }
        finally
        {
            if (resultSet != null)
            {
                try
                {
                    resultSet.close();
                }
                catch(SQLException e)
                {
                    errorListeners_.fireError(e);
                }
            }
        }
    }  // end of synchronized block

    // notify listeners that we've changed
    event = new TableModelEvent(this, -1);
    fireTableChanged(event);
    Trace.log(Trace.DIAGNOSTIC, "Did load, changed number of rows to:", numRows_);

    workingListeners_.fireStopWorking ();
}


/**
Restores the state of this object from an object input stream.
It is used when deserializing an object.
@param in The input stream of the object being deserialized.
@exception IOException
@exception ClassNotFoundException
**/
private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
{
    // Restore the non-static and non-transient fields.
    in.defaultReadObject();
    // Initialize the transient fields.
    connection_ = null;
    data_ = new String[0][NUM_COLUMNS_];
    types_ = new int[0];
    numRows_ = 0;
    errorListeners_ = new ErrorEventSupport (this);
    workingListeners_ = new WorkingEventSupport (this);
}


/**
Removes a row from the table.

@param  rowIndex  The row index.  Indices start at 0.
**/
public void removeRow (int rowIndex)
{
    synchronized (this)
    {
        Trace.log(Trace.DIAGNOSTIC, "Removing row ", rowIndex);

        // array to hold new data
        String[][] newData = new String[data_.length -1][NUM_COLUMNS_];
        int[] newTypes = new int[types_.length -1];

        // copy table data to new table less row being removed
        if (rowIndex == 0)
        {
            // remove first row
            System.arraycopy(data_, 1, newData, 0, newData.length);
            System.arraycopy(types_, 1, newTypes, 0, newTypes.length);
        }
        else if (rowIndex == data_.length - 1)
        {
            // remove last row
            System.arraycopy(data_, 0, newData, 0, newData.length);
            System.arraycopy(types_, 0, newTypes, 0, newTypes.length);
        }
        else
        {
            // remove row in middle
            System.arraycopy(data_, 0, newData, 0, rowIndex);
            System.arraycopy(data_, rowIndex+1, newData, rowIndex,
                                newData.length-rowIndex);
            System.arraycopy(types_, 0, newTypes, 0, rowIndex);
            System.arraycopy(types_, rowIndex+1, newTypes, rowIndex,
                                newTypes.length-rowIndex);
        }

        data_ = newData;
        numRows_--;
    }

    // notify listeners that we've changed
    fireTableRowsDeleted(rowIndex, rowIndex);
}


/**
Removes a listener from being notified when an error occurs.

@param  listener  The listener.
**/
public void removeErrorListener (ErrorListener listener)
{
    errorListeners_.removeErrorListener (listener);
}


/**
Removes a listener from being notified when work starts and stops.

@param  listener  The listener.
**/
public void removeWorkingListener (WorkingListener listener)
{
    workingListeners_.removeWorkingListener (listener);
}


/**
Sets the database files this table contains data for.
The new value will not be in effect until a <i>load()</i> is done.

@param tables The database files this table will contain data for.
                        Tables should be in the form of <library>.<file>.
**/
public void setTables (String[] tables)
{
    tables_ = tables;
}


/**
Sets the SQL connection this table contains data for.
The new value will not be in effect until a <i>load()</i> is done.

@param       connection  The SQL connection.
**/
public void setConnection (Connection connection)
{
    connection_ = connection;
}



}
