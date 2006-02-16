///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SequentialFile.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.io.Serializable;

/**
 *The SequentialFile class represents a physical or logical file on the server.
 *The SequentialFile class allows the user to do the following:
 *<ul>
 *<li>Create a physical file by:
 *<ul>
 *<li>Specifying a record length.
 *<li>Specifying an existing DDS source file.
 *<li>Specifying a RecordFormat object that contains a description of the
 *    record format for the file.
 *</ul>
 *<li>Access the records in a file sequentially or by record number.
 * Note: To read a keyed physical or logical file sequentially and have the records
 * returned in key order, use the <tt>read...()</tt> methods of {@link KeyedFile KeyedFile}.
 *<li>Write records to a file sequentially.
 *<li>Update records in a file sequentially or by record number.
 *<li>Lock a file for different types of access.
 *<li>Use commitment control when accessing a file.  The user can:
 *<ul>
 *<li>Start commitment control for the connection.
 *<li>Specify different commitment control lock levels for the individual
 *    files being accessed.
 *<li>Commit and rollback transactions for the connection.
 *</ul>
 *<li>Delete a physical or logical file or member.
 *</ul>
 *SequentialFile objects generate the following events:
 *<ul>
 *<li><a href="FileEvent.html">FileEvent</a>
 *<br>The events fired are:
 *<ul>
 *<li>FILE_CLOSED
 *<li>FILE_CREATED
 *<li>FILE_DELETED
 *<li>FILE_MODIFIED
 *<li>FILE_OPENED
 *</ul>
 *<li>PropertyChangeEvent
 *<li>VetoableChangeEvent
 *</ul>
**/
public class SequentialFile extends AS400File implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;



  /**
   *Constructs a SequentialFile object.
  **/
  public SequentialFile()
  {

  }

  /**
   *Constructs a SequentialFile object. It uses the
   * system and file name specified.
   *If the <i>name</i> for the file does not include a member, the
   *first member of the file will be used.
   *@param system The server to which to connect. The <i>system</i> cannot
   *be null.
   *@param name The integrated file system pathname of the file. The <i>name</i>
   *cannot be null.
  **/
  public SequentialFile(AS400 system, String name)
  {
    super(system, name);
  }

  /**
   *Deletes the record specified by record number.  The file must be open when
   *invoking this method.
   *@param recordNumber The record number of the record to be deleted.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
  **/
  public void deleteRecord(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Find the record
    positionCursor(recordNumber);
    // Call super to delete it
    deleteCurrentRecord();
  }

  //@C0D - moved open(...) code to base class
  /**
   *Opens the file.  The file must not be open when invoking this method.
   *If commitment control is not started for the connection,
   *<i>commitLockLevel</i> is ignored.  The file cursor is positioned prior
   *to the first record.  If <i>blockingFactor</i> is greater than one (or
   *if zero is specified and a blocking factor greater than one is determined
   *by the object) and the file is opened for READ_ONLY, the record cache will
   *be filled with an initial set of records.<br>
   *The record format for the file must be set prior to calling this method.<br>
   *The name of the file and the system to which to connect must be set prior
   *to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@see AS400File#refreshRecordCache
   *@see AS400File#setRecordFormat
   *@param openType The manner in which to open the file.  Valid values are:
   *                <ul>
   *                <li>READ_ONLY
   *                <li>READ_WRITE
   *                <li>WRITE_ONLY
   *                </ul>
   *@param blockingFactor The number of records to retrieve or to write during a
   *read or write operation.<br>
   *The AS400File object will attempt to anticipate the need for data by accessing
   *blocks of records if the <i>openType</i> is READ_ONLY.  If the <i>openType</i>
   *is WRITE_ONLY, <i>blockingFactor</i> number of records will be written at one
   *time when writing an array of records.
   *If the open type is READ_WRITE, <i>blockingFactor</i> is ignored and a
   *blocking factor of 1 will be used for data integrity reasons.
   *Specify an appropriate <i>blockingFactor</i> for your performance needs.<br>
   *If 0 is specified for <i>blockingFactor</i>, a default value will be calculated
   *by taking the integer result of dividing 2048 by the byte length of the record
   *plus 16.<br>
   *If the user either specifies a blocking factor greater than 1 or specifies 0,
   *which will cause a blocking factor to be calculated, there is the risk of
   *obtaining stale data when doing multiple read operations.
   *Invoke the refreshRecordCache() method prior to reading a record to cause the object
   *to read from the server if this is a problem.<br>
   *@param commitLockLevel Used to control record locking during a transaction if
   *commitment control has been started for the connection.
   *Valid values are:
   *<ul>
   *<li>COMMIT_LOCK_LEVEL_ALL
   *<li>COMMIT_LOCK_LEVEL_CHANGE
   *<li>COMMIT_LOCK_LEVEL_CURSOR_STABILITY
   *<li>COMMIT_LOCK_LEVEL_DEFAULT
   *<li>COMMIT_LOCK_LEVEL_NONE
   *</ul>
   *The <i>commitLockLevel</i> is ignored if commitment control is not started for
   *the connection.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
   *@exception ServerStartupException If the server cannot be started.
   *@exception UnknownHostException If the server cannot be located.
  **/
/*@C0D  public void open(int openType, int blockingFactor, int commitLockLevel)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Verify the object state
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
    // Verify the parameters
    if (openType != READ_ONLY &&
        openType != READ_WRITE &&
        openType != WRITE_ONLY)
    {
      throw new ExtendedIllegalArgumentException("openType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (blockingFactor < 0)
    {
      throw new ExtendedIllegalArgumentException("blockingFactor", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (commitLockLevel < 0 || commitLockLevel > 4)
    {
      throw new ExtendedIllegalArgumentException("commitLockLevel", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    openFile(openType, blockingFactor, commitLockLevel, "seq");
  }
*/ // @C0D

  /**
   *Positions the file cursor to the first record whose record number
   *matches the specified record number.  The file must be open when invoking
   *this method.
   *@param recordNumber The record number of the record at which to position the
   *cursor.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
  **/
  public void positionCursor(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    verifyState(recordNumber); //@C0A
    impl_.doIt("positionCursor", new Class[] { Integer.TYPE }, new Object[] { new Integer(recordNumber) });
  }


  /**
   *Positions the file cursor to the first record after the record specified
   *by the record number.  The file must be open when invoking
   *this method.
   *@param recordNumber The record number of the record after which to position the
   *           cursor.  The <i>recordNumber</i> must be greater than zero.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
  **/
  public void positionCursorAfter(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    positionCursor(recordNumber);  // This will cause the cache to be refreshed if we
                                   // are caching and the record is not in the cache
    positionCursorToNext();        // This will do the same, however at this point
                                   // the record will be in the cache if caching
  }

  /**
   *Positions the file cursor to the first record before the record specified
   *by the record number.  The file must be open when invoking
   *this method.
   *@param recordNumber The record number of the record before which to position
   *           the cursor.  The <i>recordNumber</i> must be greater than zero.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
  **/
  public void positionCursorBefore(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    verifyState(recordNumber); //@C0A
    impl_.doIt("positionCursorBefore", new Class[] { Integer.TYPE }, new Object[] { new Integer(recordNumber) });
  }


  /**
   *Reads the record with the specified record number.  The file must be open
   *when invoking this method.
   *@param recordNumber The record number of the record to be read.  The
   *<i>recordNumber</i> must be greater than zero.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
  **/
  public Record read(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    verifyState(recordNumber); //@C0A
    return fillInRecord(impl_.doItRecord("read", new Class[] { Integer.TYPE }, new Object[] { new Integer(recordNumber) })); //@D0C
  }


  /**
   *Reads the first record after the record with the specified record number.
   *The file must be open when invoking this method.
   *@param recordNumber record number of the record prior to the record to be read.
   *The <i>recordNumber</i> must be greater than zero.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
  **/
  public Record readAfter(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    verifyState(recordNumber); //@C0A
    return fillInRecord(impl_.doItRecord("readAfter", new Class[] { Integer.TYPE }, new Object[] { new Integer(recordNumber) })); //@D0C
  }


  /**
   *Reads all the records in the file. The file must be closed when invoking this method.
   *The record format for the file must have been set prior to invoking this method.
   *@return The records read.  If no records are read, an array of size zero is returned.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
   *@exception ServerStartupException If the server cannot be started.
   *@exception UnknownHostException If the server cannot be located.
  **/
  public Record[] readAll()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (isOpen_)
    {
      throw new ExtendedIllegalStateException(ExtendedIllegalStateException.OBJECT_CAN_NOT_BE_OPEN);
    }
    if (recordFormat_ == null)
    {
      throw new ExtendedIllegalStateException("recordFormat", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Read all records.
    // Since readAll() now behaves under-the-covers like readNext(),
    // need to open the file. Also synchronize on this, because
    // the file isn't supposed to be open and this might mess up
    // other references to this object, which expect it to be closed.
    // See AS400FileImplRemote.readAll() for information regarding
    // changes in the under-the-covers behavior.
    chooseImpl();

    // Before we calculate, make sure the record format has had
    // its text objects initialized.
    recordFormat_.initializeTextObjects(system_); //@D0A

    // Use a calculated blocking factor, else use a large blocking factor
    int bf = 2048/(recordFormat_.getNewRecord().getRecordLength() + 16); //@D0M
    if (bf <= 1) bf = 100; //@D0M

    Record[] recs = impl_.doItRecordArray("readAll", new Class[] { String.class, Integer.TYPE }, new Object[] { "seq", new Integer(bf) }); //@D0C
    //@D0A
    if (recs != null)
    {
      for (int i=0; i<recs.length; ++i)
      {
        recs[i] = fillInRecord(recs[i]);
      }
    }

    return recs; //@D0C
  }


  /**
   *Reads the first record before the record with the specified record number.
   *The file must be open when invoking this method.
   *@param recordNumber The record number of the record after the record to be read.
   *The <i>recordNumber</i> must be greater than zero.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
  **/
  public Record readBefore(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    verifyState(recordNumber); //@C0A
    return fillInRecord(impl_.doItRecord("readBefore", new Class[] { Integer.TYPE }, new Object[] { new Integer(recordNumber) })); //@D0C
  }


  /**
   *Overrides the ObjectInputStream.readObject() method in order to return any
   *transient parts of the object to there properly initialized state.  We also
   *generate a declared file name for the object.  I.e we in effect
   *call the null constructor.  By calling ObjectInputStream.defaultReadObject()
   *we restore the state of any non-static and non-transient variables.  We
   *then continue on to restore the state (as necessary) of the remaining varaibles.
   *@param in The input stream from which to deserialize the object.
   *@exception ClassNotFoundException If the class being deserialized is not found.
   *@exception IOException If an error occurs while communicating with the server.
  **/

  private void readObject(ObjectInputStream in)
    throws ClassNotFoundException,
           IOException
  {
    in.defaultReadObject();
    initializeTransient(); //@C0C
  }


  /**
   *Updates the record at the position specified by the record number.  The file
   *must be open when invoking this method.
   *@param recordNumber The record number of the record to update.
   *The <i>recordNumber</i> must be greater than zero.
   *@param record The record with which to update.
   *@exception AS400Exception If the server returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the server.
  **/
  public void update(int recordNumber, Record record)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (record == null)
    {
      throw new NullPointerException("record");
    }
    positionCursor(recordNumber);
    update(record);
  }


  //@C0A
  /**
  * Make sure the file is open and the recordNumber is valid.
  **/
  private void verifyState(int recordNumber)
  {
    // Verify object state
    if (!isOpen_)
    {
      throw new ExtendedIllegalStateException(ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
    }
    // Verify parameters
    if (recordNumber < 1)
    {
      throw new ExtendedIllegalArgumentException("recordNumber", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }
}
