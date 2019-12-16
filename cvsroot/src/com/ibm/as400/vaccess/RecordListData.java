///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RecordListData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.DateFieldDescription;                           // @C1A
import com.ibm.as400.access.FieldDescription;
import com.ibm.as400.access.FloatFieldDescription;
import com.ibm.as400.access.FieldDescription;
import com.ibm.as400.access.HexFieldDescription;                            // @D1A
import com.ibm.as400.access.KeyedFile;
import com.ibm.as400.access.PackedDecimalFieldDescription;
import com.ibm.as400.access.SequentialFile;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.TimeFieldDescription;                           // @C1A
import com.ibm.as400.access.TimestampFieldDescription;                      // @C1A
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.ZonedDecimalFieldDescription;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;


/**
Represents the records in a server file that are
accessed either sequentially or by key by using the record-level
access classes in com.ibm.as400.access.
This class handles caching and storing the data fields so they can be
retrieved and displayed by different views.

<p>The data is retrieved from the system on the
first invocation of <i>readAllRecords()</i> or <i>readMoreRecords()</i>
or whenever <i>load()</i> is called.

<p>Data is retrieved from the access classes as needed.

<p>It is up to the user to call <i>close()</i> when the
record list data is no longer needed.

<p>Most errors are reported by firing ErrorEvents, rather
than throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>RecordListData objects generate the following events:
<ul>
  <li>ErrorEvent
  <li>WorkingEvent
</ul>
**/
// Note that this class throws events from within synchronized blocks,
// which could cause hangs if the handlers of these events do
// operations from a different thread an attempt to access another
// synchronized piece of code.
// At this time this seems to be an acceptable risk, since the
// events thrown are not likely to need enough processing to
// require another thread, and getting having the events thrown
// from outside a sychronized block would be nearly impossible.
// The other option is to have the firing of the events be done
// from another thread, but the overhead of creating another thread
// not only takes resources, but also delays the delivery of the event.
class RecordListData
implements Serializable
{

// Constants for search types
static final int KEY_EQ = KeyedFile.KEY_EQ;
static final int KEY_GT = KeyedFile.KEY_GT;
static final int KEY_GE = KeyedFile.KEY_GE;
static final int KEY_LT = KeyedFile.KEY_LT;
static final int KEY_LE = KeyedFile.KEY_LE;


// What the records in this object represent.
private AS400 system_ = null;
private String fileName_ = null;
private boolean newKeyed_ = false;    // access currently being used
private boolean keyed_ = false;       // value of access property
private int searchType_ = KEY_EQ;     // search type currently being used
private int newSearchType_ = KEY_EQ;  // value of search type property
private Object key_[] = null;         // key currently being used
private Object newKey_[] = null;      // value of key property

// The file objects.  One of these will be null.
transient private SequentialFile sequentialFile_ = null;
transient private KeyedFile keyedFile_ = null;
transient private boolean loadDone_ = false;   // has load() been done
transient private boolean resourceOpen_ = false; // file is open

// The record information
transient private Object[][] data_ = null;      // the data for the table
transient Record record_;                       // current record

// Column information
transient private int numColumns_ = 0;
transient FieldDescription[] fields_ = null;

// Row information
// Number of rows data_ is bumped by when more space needed. 100 is arbitrary.
private static int ROW_INCREMENT_ = 100;
// Number of rows in the table. Always 0 until allRecordsRead_ = true.
transient private int numRows_ = 0;
// The index of the last record read.  Up to this row is valid in data_.
transient private int lastRecordRead_ = -1;
// For keyed files using LE or LT, the last record.
transient private int lastLESSRecordNumber_;
// If all records in sequentialFile_ or keyedFile_ have been transferred to data_.
transient private boolean allRecordsRead_ = true;

// Flag for if an error event was sent.
transient private boolean error_;

// Event support.
transient private ErrorEventSupport errorListeners_
    = new ErrorEventSupport (this);
transient private WorkingEventSupport workingListeners_
    = new WorkingEventSupport (this);

private boolean fireWorkingEvents_ = true;

/**
Constructs a RecordListData object.
**/
public RecordListData ()
{
    super();
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
Closes the server file this record list represents.
**/
synchronized public void close ()
{
    if (resourceOpen_)
    {
        try
        {
            if (keyed_)
            {
                keyedFile_.close();
            }
            else
            {
                sequentialFile_.close();
            }
        }
        catch (Exception e)
        {
            errorListeners_.fireError(e);
        }
        resourceOpen_ = false;
    }
}


/**
Returns whether all records have been processed (stored in a local
cache).

@return true if all records have been processed; false otherwise.
**/
synchronized public boolean getAllRecordsProcessed ()
{
    return allRecordsRead_;
}



/**
Returns the display size for the column at the specified index.
Indices start at 0.

@param index Index of the column.
@return The display size for the column.
**/
synchronized public int getColumnDisplaySize(int index)
{
    if (!loadDone_)
    {
        throw new IndexOutOfBoundsException("index");
    }

    // For most types, we just consult with RLA.  However, for times, dates, and           @C1A
    // timestamps, RLA reports the display size for the server format.  And these          @C1A
    // GUIs actually internationalize the Strings before displaying them.  So for          @C1A
    // these types, we need to compute our own display sizes.                              @C1A

    FieldDescription field = fields_[index];
    if (field instanceof FloatFieldDescription ||
        field instanceof PackedDecimalFieldDescription ||
        field instanceof ZonedDecimalFieldDescription)
        return field.getLength() + 2;  // sign and decimal point
    else if (field instanceof TimeFieldDescription)                                         // @C1A
        return DBDateCellRenderer.getDisplaySize(DBDateCellRenderer.FORMAT_TIME);           // @C1A
    else if (field instanceof DateFieldDescription)                                         // @C1A
        return DBDateCellRenderer.getDisplaySize(DBDateCellRenderer.FORMAT_DATE);           // @C1A
    else if (field instanceof TimestampFieldDescription)                                    // @C1A
        return DBDateCellRenderer.getDisplaySize(DBDateCellRenderer.FORMAT_TIMESTAMP);      // @C1A
    else if (field instanceof HexFieldDescription)                                          // @D1A
        return field.getLength() * 2;                                                       // @D1A
    else
        return field.getLength();
}



/**
Returns the label for the column at the specified index.
Indices start at 0.

@param index Index of the column.
@return The label for the column.
**/
synchronized public String getColumnLabel(int index)
{
    if (!loadDone_)
    {
        throw new IndexOutOfBoundsException("index");
    }
    return fields_[index].getFieldName();
}



/**
Returns the name for the column at the specified index.
Indices start at 0.
If an error occurs, an empty string is returned.

@param index Index of the column.
@return The name for the column.
**/
synchronized public String getColumnName(int index)
{
    if (!loadDone_)
    {
        throw new IndexOutOfBoundsException("index");
    }
    return fields_[index].getFieldName();
}



/**
Returns the file name.
The name is formatted as a fully qualified path name in the library file system.

@return The file name.
**/
public String getFileName ()
{
    return fileName_;
}


/**
Returns the key.
The key is only used if the <i>keyed</i> property is true.

@return The key.
**/
public Object[] getKey ()
{
    return newKey_;
}


/**
Returns whether the file will be accessed in key or sequential order.

@return  true if the file will be accessed in key order; false
              if the file will be accessed in sequential order.
**/
public boolean getKeyed ()
{
    return newKeyed_;
}



/**
Returns the index of the last record which had been processed (stored
in the local cache). -1 indicates no records have been
processed.

@return The index of the last record processed.
**/
synchronized public int getLastRecordProcessed ()
{
    return lastRecordRead_;
}



/**
Returns the number of fields in this result set.
If an error occurs, 0 is returned.

@return The number of fields in this result set.
**/
synchronized public int getNumberOfColumns()
{
    return numColumns_;
}



/**
Returns the number of rows in the result set.
Note that this will result in all records being
processed.
If an error occurs, 0 is returned.

@return The number of rows in the record list.
**/
synchronized public int getNumberOfRows()
{
    if (!allRecordsRead_)
    {
        readAllRecords();
    }
    return numRows_;
}



/**
Returns the search type.
The search type is only used if the <i>keyed</i> property is true
and the <i>key</i> property is not null.

@return The search type.
**/
public int getSearchType ()
{
    return newSearchType_;
}


/**
Returns the system where the file is located.

@return The system where the file is located.
**/
public AS400 getSystem ()
{
    return system_;
}


/**
Returns the value at the specifed column and row.
Indices start at 0.
If an error occurs, an empty string is returned.

@param  rowIndex            The row index.
@param  columnIndex         The column index.
@return The value at the specified column and row.
**/
synchronized public Object getValueAt (int rowIndex,
                          int columnIndex)
{
    if (!loadDone_)
    {
        throw new IndexOutOfBoundsException("rowIndex");
    }

    // If we haven't yet transferred this row from the
    // result set to the table cache, do so.
    if (lastRecordRead_ < rowIndex)
    {
        readMoreRecords(rowIndex - lastRecordRead_);
    }
    if (lastRecordRead_ < rowIndex)
    {
        throw new IndexOutOfBoundsException("rowIndex");
    }

    // return the value
    return data_[rowIndex][columnIndex];
}



/**
Loads the table based on the state of the system.  This causes the
'query' to be run.
**/
synchronized public void load ()
{
    Trace.log(Trace.DIAGNOSTIC, "Doing data load.");

    if (system_ == null || fileName_ == null)
    {
        Trace.log(Trace.DIAGNOSTIC, "Data not set for load, exiting.");
        return;
    }

    if (fireWorkingEvents_)
        workingListeners_.fireStartWorking ();

    // cleanup old data
    if (resourceOpen_)
    {
        close();  // this sets resourceOpen_ to false
    }

    // Set back fields in case there is an error.
    fields_ = null;
    numColumns_ = 0;
    lastRecordRead_ = -1;
    allRecordsRead_ = true;
    loadDone_ = false;
    data_ = null;
    numRows_ = 0;
    key_ = newKey_;
    searchType_ = newSearchType_;
    keyed_ = newKeyed_;

    // Retrieve the record format for the file
    // Create an AS400FileRecordDescription object that represents the file
    AS400FileRecordDescription recordDesc;
    RecordFormat[] format;
    try
    {
        // next line is in try block to catch runtime exceptions
        // from parameter validation
        recordDesc = new AS400FileRecordDescription(system_, fileName_);
        format = recordDesc.retrieveRecordFormat();
    }
    catch (Exception e)
    {
        errorListeners_.fireError(e);
        return;
    }

    // get info about the columns returned
    fields_ = format[0].getFieldDescriptions();
    numColumns_ = fields_.length;

    // Open file and get first record.
    // Note we don't process any of the result set until the
    // values are asked for.
    if (!keyed_)
    {
        // sequential access
        keyedFile_ = null;
        try
        {
            // next line is in try block to catch runtime exceptions
            // from parameter validation
            sequentialFile_ = new SequentialFile(system_, fileName_);
        }
        catch(Exception e)
        {
            fields_ = null;
            numColumns_ = 0;
            errorListeners_.fireError(e);
            return;
        }
        // Set the record format
        try {sequentialFile_.setRecordFormat(format[0]);}
        catch (PropertyVetoException e) {} // should not occur
        try
        {
            // Open the file.
            sequentialFile_.open(AS400File.READ_ONLY, ROW_INCREMENT_,
                                 AS400File.COMMIT_LOCK_LEVEL_NONE);
            // Move cursor to first record.
            // Note that if we can't get to first record, we scrap
            // everything and fail.
            record_ = sequentialFile_.readNext();
            resourceOpen_ = true;
        }
        catch (Exception e)
        {
            try {sequentialFile_.close();} catch(Exception ex){}
            fields_ = null;
            numColumns_ = 0;
            errorListeners_.fireError(e);
            return;
        }
    }
    else
    {
        // keyed access
        sequentialFile_ = null;
        try
        {
            // next line is in try block to catch runtime exceptions
            // from parameter validation
            keyedFile_ = new KeyedFile(system_, fileName_);
        }
        catch(Exception e)
        {
            fields_ = null;
            numColumns_ = 0;
            errorListeners_.fireError(e);
            return;
        }
        // Set the record format
        try {keyedFile_.setRecordFormat(format[0]);}
        catch (PropertyVetoException e) {} // should not occur
        try
        {
            // Open the file.
            keyedFile_.open(AS400File.READ_ONLY, ROW_INCREMENT_,
                                 AS400File.COMMIT_LOCK_LEVEL_NONE);
            resourceOpen_ = true;
            if (key_ != null)
            {
                // Move cursor to first record.
                // Note that if we can't get to first record, we scrap
                // everything and fail.
                record_ = keyedFile_.read(key_, searchType_);
                if (searchType_ == KEY_LE)
                {
                    // If LE, we need to move to the last record
                    // that has a key equal.  This covers the case
                    // where there are duplicate keys, if we don't
                    // do this, we will miss all but one record
                    // with a key equal.
                    try
                    {
                        Record next = keyedFile_.readNextEqual(key_);
                        while (next != null)
                        {
                            record_ = next;
                            next = keyedFile_.readNextEqual(key_);
                        }
                    }
                    catch(Exception e)
                    {
                        // If we hit an error, report to users but
                        // otherwise ignore and continue.
                        errorListeners_.fireError(e);
                    }
                }
                if (searchType_ == KEY_LE || searchType_ == KEY_LT)
                {
                    // We want the records in the 'forward' order,
                    // so save the last record and then reposition
                    // at the first record.
                    lastLESSRecordNumber_ = record_.getRecordNumber();
                    record_ = keyedFile_.readFirst();
                }
            }
            else  // keyed access but no key value
            {
                // Move cursor to first record.
                // Note that if we can't get to first record, we scrap
                // everything and fail.
                record_ = keyedFile_.readNext();
            }
        }
        catch (Exception e)
        {
            try {keyedFile_.close();} catch(Exception ex){}
            fields_ = null;
            numColumns_ = 0;
            errorListeners_.fireError(e);
            return;
        }
    }

    if (record_ != null)
    {
        // there are records, but we don't know how many
        // Create new array to hold table values.
        data_ = new Object[ROW_INCREMENT_][numColumns_];
        allRecordsRead_ = false;
    }

    loadDone_ = true;
    if (fireWorkingEvents_)
        workingListeners_.fireStopWorking ();
}


/**
Processes records in the result set, moving the data into local cache
so they can be retrieved via <i>getValueAt</i>.
**/
synchronized public void readAllRecords()
{
    Trace.log(Trace.DIAGNOSTIC, "Reading all rows.");
    workingListeners_.fireStartWorking ();
    error_ = false;
    while (!allRecordsRead_ && !error_)
    {
        fireWorkingEvents_ = false;
        readMoreRecords(ROW_INCREMENT_);
        fireWorkingEvents_ = true;
    }
    workingListeners_.fireStopWorking ();
}


/**
Processes records in the result set, moving the data into local cache
so they can be retrieved via <i>getValueAt</i>.

@param numberToRead The number of records to process.
**/
synchronized public void readMoreRecords(int numberToRead)
{
    Trace.log(Trace.DIAGNOSTIC, "Reading more rows:", numberToRead);
    if (fireWorkingEvents_)
        workingListeners_.fireStartWorking ();

    error_ = false;
     // read only load done & more records to read
    if (!allRecordsRead_)
    {
        // Make sure we have room in data_ for records.
        if (lastRecordRead_ + numberToRead >= data_.length)
        {
            // increase by the greater of our increment or the number
            // of rows needed.
            int sizeNeeded = lastRecordRead_ + numberToRead + 1;
            int increment = ROW_INCREMENT_>sizeNeeded?ROW_INCREMENT_:sizeNeeded;
            Object[][] newData =
                new Object[data_.length + increment][numColumns_];
            System.arraycopy(data_, 0, newData, 0, data_.length);
            data_ = newData;
        }

        for (int i=0; i<numberToRead; ++i)
        {
            for (int j=0; j<numColumns_; ++j)
            {
                try
                {
                    // Note record_ is always valid since load()
                    // and this loop ensure the next record is valid
                    // but do not transfer the data into data_.
                    data_[lastRecordRead_+1][j] =  record_.getField(j);
                }
                catch (UnsupportedEncodingException e)
                {
                    // We don't set error_=true, since we are
                    // continuing to process, and are not aborting.
                    data_[lastRecordRead_+1][j] = null;  // set data to null
                    errorListeners_.fireError(e);
                }
            }
            ++lastRecordRead_;
            // Move cursor to next record.
            if (!keyed_)  // sequential access
            {
                try
                {
                    record_ = sequentialFile_.readNext();
                }
                catch (Exception e)
                {
                    errorListeners_.fireError(e);
                    error_ = true;
                    record_ = null;
                }
            }
            else  // keyed access
            {
                try
                {
                    if (key_ != null)
                    {
                        if (searchType_ == KEY_EQ)
                            record_ = keyedFile_.readNextEqual(key_);
                        else
                        {
                            if ((searchType_ == KEY_LT || searchType_ == KEY_LE) &&
                                 lastLESSRecordNumber_ == record_.getRecordNumber())
                                // The last valid record has been read, the
                                // next record does not match search criteria.
                                record_ = null;
                            else
                                record_ = keyedFile_.readNext();
                        }
                    }
                    else  // keyed access but no key
                    {
                        record_ = keyedFile_.readNext();
                    }
                }
                catch (Exception e)
                {
                    errorListeners_.fireError(e);
                    error_ = true;
                    record_ = null;
                }
            }

            if (record_ == null)
            {
                // No more records.
                allRecordsRead_ = true;
                close();
                numRows_ = lastRecordRead_+1;
                break;
            }
        }
    } // end if able to read records

    if (fireWorkingEvents_)
        workingListeners_.fireStopWorking ();
}


/**
Restore the state of this object from an object input stream.
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
    data_ = null;
    sequentialFile_ = null;
    keyedFile_ = null;
    loadDone_ = false;
    resourceOpen_ = false;
    numColumns_ = 0;
    fields_ = null;
    numRows_ = 0;
    lastRecordRead_ = -1;
    allRecordsRead_ = true;
    errorListeners_ = new ErrorEventSupport (this);
    workingListeners_ = new WorkingEventSupport (this);
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
Sets the file name.
Note that the data will not change until a <i>load()</i> is done.

@param       fileName        The file name.
 The name is specified as a fully qualified path name in the library file system.
**/
public void setFileName (String fileName)
{
    fileName_ = fileName;
}


/**
Sets the key.
The key is only used if the <i>keyed</i> property is true.
Note that the data will not change until a <i>load()</i> is done.

@param      key             The values which make up the key with which
                            to find the record.
                            This value may be null.
**/
public void setKey (Object[] key)
{
    newKey_ = key;
}


/**
Sets whether the file will be accessed in key or sequential order.
Note that the data will not change until a <i>load()</i> is done.
The default value is false.

@param keyed  true if the file will be accessed in key order; false
              if the file will be accessed in sequential order.
**/
public void setKeyed (boolean keyed)
{
    newKeyed_ = keyed;
}


/**
Sets the search type.
The search type is only used if the <i>keyed</i> property is true
and the <i>key</i> property is not null.
Note that the data will not change until a <i>load()</i> is done.

@param      searchType      Constant indicating the type of match required.
**/
public void setSearchType (int searchType)
{
    newSearchType_ = searchType;
}


/**
Sets the system where the file is located.
Note that the data will not change until a <i>load()</i> is done.

@param       system          The system where the file is located.
**/
public void setSystem (AS400 system)
{
    system_ = system;
}


}

