///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: KeyedFile.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.math.BigDecimal;

/**
 *The KeyedFile class represents an AS/400 physical or logical file.
 *It allows the user to do the following:
 *<ul>
 *<li>Create an AS/400 physical file by:
 *<ul>
 *<li>Specifying a record length.
 *<li>Specifying an existing AS/400 DDS source file.
 *<li>Specifying a RecordFormat object that contains a description of the
 *    record format for the file.
 *</ul>
 *<li>Access the records in an AS/400 file sequentially or by key.
 *<li>Write records to an AS/400 file sequentially or by key.
 *<li>Update records in an AS/400 file sequentially or by key.
 *<li>Lock an AS/400 file for different types of access.
 *<li>Use commitment control when accessing an AS/400 file.  The user can:
 *<ul>
 *<li>Start commitment control for the connection.
 *<li>Specify different commitment control lock levels for the individual AS/400
 *    files being accessed.
 *<li>Commit and rollback transactions for the connection.
 *</ul>
 *<li>Delete an AS/400 physical or logical file or member.
 *</ul>
 *KeyedFile objects generate the following events:
 *<ul>
 *<li><a href="com.ibm.as400.access.FileEvent.html">FileEvent</a>
 *<br>The events fired are:
 *<ul>
 *<li>FILE_CLOSED
 *<li>FILE_CREATED
 *<li>FILE_DELETED
 *<li>FILE_MODIFIED
 *<li>FILE_OPENED
 *</ul>
 *<li><a href="java.beans.PropertyChangeEvent.html">PropertyChangeEvent</a>
 *<li><a href="java.beans.VetoableChangeEvent.html">VetoableChangeEvent</a>
 *</ul>
**/
public class KeyedFile extends AS400File implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  

  /**
   *Constant indicating search type of equal.
  **/
  static public final int KEY_EQ = 0;

  /**
   *Constant indicating search type of greater than.
  **/
  static public final int KEY_GT = 1;

  /**
   *Constant indicating search type of greater than or equal.
  **/
  static public final int KEY_GE = 2;

  /**
   *Constant indicating search type of less than.
  **/
  static public final int KEY_LT = 3;

  /**
   *Constant indicating search type of less than or equal.
  **/
  static public final int KEY_LE = 4;

  static protected final int[] TYPE_TABLE = {0x0B, 0x0D, 0x0C, 0x09, 0x0A};

  /**
   *Constructs a KeyedFile object.
  **/
  public KeyedFile()
  {
  }

  /**
   *Constructs a KeyedFile object. It uses the specified file.
   *If the <i>name</i> for the file does not include a member, the
   *first member of the file will be used.
   *@param system The AS/400 system to which to connect. The <i>system</i> cannot
   *be null.
   *@param name The integrated file system pathname of the file. The <i>name</i>
   *cannot be null.
  **/
  public KeyedFile(AS400 system, String name)
  {
    super(system, name);
  }

  /**
   *Deletes the record specified by key.  The file must be open when invoking
   *this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.  The number of elements in <i>key</i> cannot exceed the 
   *number of key fields in the record format for this file.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void deleteRecord(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    // Find the record to delete
    impl_.doIt("positionCursorToKey", new Class[] { Object[].class, Integer.TYPE }, new Object[] { key, new Integer(TYPE_TABLE[KEY_EQ]) });
    deleteCurrentRecord();
  }


  // @A2A
  /**
   *Deletes the record specified by key.  The file must be open when invoking
   *this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>. This number cannot exceed the 
   *total number of key fields in the record format for this file.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void deleteRecord(byte[] key, int numberOfKeyFields)
   throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    // Find the record to delete
    impl_.doIt("positionCursorToKey", new Class[] { byte[].class, Integer.TYPE, Integer.TYPE }, new Object[] { key, new Integer(TYPE_TABLE[KEY_EQ]), new Integer(numberOfKeyFields) });
    deleteCurrentRecord();
  }

  //@C0D - moved code to base class
  /**
   *Opens the file.  The file must not be open when invoking this method.
   *If commitment control is not started for the connection,
   *<i>commitLockLevel</i> is ignored.  The file cursor is positioned prior
   *to the first record.  If <i>blockingFactor</i> is greater than one (or
   *if zero is specified and a blocking factor greater than one is determined
   *by the object) and the file is opened for READ_ONLY, the record cache will
   *be filled with an initial set of records.<br>  
   *The record format for the file must be set prior to calling this method.<br>
   *The name of the file and the AS400 system to which to connect must be set prior
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
   *If the user either specifies a blocking factor greater than 1, or specifies 0
   *which will cause a blocking factor to be calculated, there is the risk of
   *obtaining stale data when doing multiple read operations.
   *Invoke the refreshRecordCache() method prior to reading a record to cause the object
   *to read from the AS/400 if this is a problem.<br>
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
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
/* @C0D  public void open(int openType, int blockingFactor, int commitLockLevel)
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
    // Open the file
    openFile(openType, blockingFactor, commitLockLevel, "key");
  }
*/ // @C0D

  /**
   *Positions the file cursor to the first record matching the specified
   *key.  The file must be open when invoking this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void positionCursor(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    positionCursor(key, KEY_EQ);
  }


  
  // @A2A
  /**
   *Positions the file cursor to the first record matching the specified
   *key.  The file must be open when invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void positionCursor(byte[] key, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    positionCursor(key, KEY_EQ, numberOfKeyFields);
  }




  /**
   *Positions the file cursor to the first record meeting the specified search criteria
   *based on <i>key</i>.  The <i>searchType</i> indicates that the cursor should be
   *positioned to the record whose key first meets the search criteria when compared
   *to <i>key</i>.  The file must be open when invoking this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param searchType Constant indicating the type of match required.  Valid values are:
   *<ul>
   *<li>KEY_EQ<br>
   *First record whose key is equal to <i>key</i>.
   *<li>KEY_LT<br>
   *First record whose key is less than <i>key</i>.
   *<li>KEY_LE<br>
   *First record whose key is less than or equal to <i>key</i>.
   *<li>KEY_GT<br>
   *First record whose key is greater than <i>key</i>.
   *<li>KEY_GE<br>
   *First record whose key is greater than or equal to <i>key</i>.
   *</ul>
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursor(Object[] key, int searchType)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    if (searchType < KEY_EQ || searchType > KEY_LE)
    {
      throw new ExtendedIllegalArgumentException("searchType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    impl_.doIt("positionCursor", new Class[] { Object[].class, Integer.TYPE }, new Object[] { key, new Integer(TYPE_TABLE[searchType]) });
  }
  
  // @A2A
  /**
   *Positions the file cursor to the first record meeting the specified search criteria
   *based on <i>key</i>.  The <i>searchType</i> indicates that the cursor should be
   *positioned to the record whose key first meets the search criteria when compared
   *to <i>key</i>.  The file must be open when invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param searchType Constant indicating the type of match required.  Valid values are:
   *<ul>
   *<li>KEY_EQ<br>
   *First record whose key is equal to <i>key</i>.
   *<li>KEY_LT<br>
   *First record whose key is less than <i>key</i>.
   *<li>KEY_LE<br>
   *First record whose key is less than or equal to <i>key</i>.
   *<li>KEY_GT<br>
   *First record whose key is greater than <i>key</i>.
   *<li>KEY_GE<br>
   *First record whose key is greater than or equal to <i>key</i>.
   *</ul>
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursor(byte[] key, int searchType, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    if (searchType < KEY_EQ || searchType > KEY_LE)
    {
      throw new ExtendedIllegalArgumentException("searchType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    impl_.doIt("positionCursor", new Class[] { byte[].class, Integer.TYPE, Integer.TYPE }, new Object[] { key, new Integer(TYPE_TABLE[searchType]), new Integer(numberOfKeyFields) });
  }


  /**
   *Positions the file cursor to the first record after the record specified
   *by key.  The file must be open when invoking this method.
   *@param key The values which make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursorAfter(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    impl_.doIt("positionCursorAfter", new Class[] { Object[].class }, new Object[] { key });
  }


  // @A2A
  /**
   *Positions the file cursor to the first record after the record specified
   *by key.  The file must be open when invoking this method.
   *@param key The byte array that contains the byte values which make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursorAfter(byte[] key, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    impl_.doIt("positionCursorAfter", new Class[] { byte[].class, Integer.TYPE }, new Object[] { key, new Integer(numberOfKeyFields) });
  }


  /**
   *Positions the file cursor to the first record before the record specified
   *by key.  The file must be open when invoking this method.
   *@param key The values which make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursorBefore(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    impl_.doIt("positionCursorBefore", new Class[] { Object[].class }, new Object[] { key });
  }


  // @A2A
  /**
   *Positions the file cursor to the first record before the record specified
   *by key.  The file must be open when invoking this method.
   *@param key The byte array that contains the byte values which make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursorBefore(byte[] key, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    impl_.doIt("positionCursorBefore", new Class[] { byte[].class, Integer.TYPE }, new Object[] { key, new Integer(numberOfKeyFields) });
  }


  /**
   *Reads the first record with the specified key.  The file must be open when
   *invoking this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record read(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    return read(key, KEY_EQ);
  }


  // @A2A
  /**
   *Reads the first record with the specified key.  The file must be open when
   *invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record read(byte[] key, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    return read(key, KEY_EQ, numberOfKeyFields);
  }



  /**
   *Reads the first record meeting the specified search criteria based on
   *<i>key</i>.  The <i>searchType</i> indicates that the record whose key first meets
   *the search criteria when compared to <i>key</i> should be returned.  The file must
   *be open when invoking this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param searchType Constant indicating the type of match required.  Valid values are:
   *<ul>
   *<li>KEY_EQ<br>
   *First record whose key is equal to <i>key</i>.
   *<li>KEY_LT<br>
   *First record whose key is less than <i>key</i>.
   *<li>KEY_LE<br>
   *First record whose key is less than or equal to <i>key</i>.
   *<li>KEY_GT<br>
   *First record whose key is greater than <i>key</i>.
   *<li>KEY_GE<br>
   *First record whose key is greater than or equal to <i>key</i>.
   *</ul>
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record read(Object[] key, int searchType)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    if (searchType < KEY_EQ || searchType > KEY_LE)
    {
      throw new ExtendedIllegalArgumentException("searchType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    return fillInRecord(impl_.doItRecord("read", new Class[] { Object[].class, Integer.TYPE }, new Object[] { key, new Integer(TYPE_TABLE[searchType]) })); //@D0C
  }



  // @A2A
  /**
   *Reads the first record meeting the specified search criteria based on
   *<i>key</i>.  The <i>searchType</i> indicates that the record whose key first meets
   *the search criteria when compared to <i>key</i> should be returned.  The file must
   *be open when invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param searchType Constant indicating the type of match required.  Valid values are:
   *<ul>
   *<li>KEY_EQ<br>
   *First record whose key is equal to <i>key</i>.
   *<li>KEY_LT<br>
   *First record whose key is less than <i>key</i>.
   *<li>KEY_LE<br>
   *First record whose key is less than or equal to <i>key</i>.
   *<li>KEY_GT<br>
   *First record whose key is greater than <i>key</i>.
   *<li>KEY_GE<br>
   *First record whose key is greater than or equal to <i>key</i>.
   *</ul>
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record read(byte[] key, int searchType, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    if (searchType < KEY_EQ || searchType > KEY_LE)
    {
      throw new ExtendedIllegalArgumentException("searchType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    return fillInRecord(impl_.doItRecord("read", new Class[] { byte[].class, Integer.TYPE, Integer.TYPE }, new Object[] { key, new Integer(TYPE_TABLE[searchType]), new Integer(numberOfKeyFields) })); //@D0C
  }


  /**
   *Reads the first record after the record with the specified key.  The file must
   *be open when invoking this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readAfter(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    return fillInRecord(impl_.doItRecord("readAfter", new Class[] { Object[].class }, new Object[] { key })); //@D0C
  }


  // @A2A
  /**
   *Reads the first record after the record with the specified key.  The file must
   *be open when invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readAfter(byte[] key, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    return fillInRecord(impl_.doItRecord("readAfter", new Class[] { byte[].class, Integer.TYPE }, new Object[] { key, new Integer(numberOfKeyFields) })); //@D0C
  }


  /**
   *Reads all the records in the file. The file must be closed when invoking this method.
   *The record format for the file must have been set prior to invoking this method.
   *@return The records read.  If no records are read, an array of size zero is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public Record[] readAll()
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           InterruptedException,
           IOException,
           ServerStartupException,
           UnknownHostException
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
    
    Record[] recs = impl_.doItRecordArray("readAll", new Class[] { String.class, Integer.TYPE }, new Object[] { "key", new Integer(bf) }); //@D0C
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
   *Reads the first record before the record with the specified key.  The file must
   *be open when invoking this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readBefore(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    return fillInRecord(impl_.doItRecord("readBefore", new Class[] { Object[].class }, new Object[] { key })); //@D0C
  }


  // @A2A
  /**
   *Reads the first record before the record with the specified key.  The file must
   *be open when invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readBefore(byte[] key, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    return fillInRecord(impl_.doItRecord("readBefore", new Class[] { byte[].class, Integer.TYPE }, new Object[] { key, new Integer(numberOfKeyFields) })); //@D0C
  }


  /**
   *Reads the next record whose key matches the full key of the current record.
   *The file must be open when invoking this method.  The file must be
   *positioned on an active record when invoking this method.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readNextEqual()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Verify object state
    checkOpen();
    return fillInRecord(impl_.doItRecord("readNextEqual", new Class[0], new Object[0])); //@D0C
  }


  /**
   *Reads the next record whose key matches the specified key.  The search does
   *not include the current record.  The <i>key</i> may be a partial key.
   *The file must be open when invoking this method.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readNextEqual(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    return fillInRecord(impl_.doItRecord("readNextEqual", new Class[] { Object[].class }, new Object[] { key })); //@D0C
  }


  // @A2A
  /**
   *Reads the next record whose key matches the specified key.  The search does
   *not include the current record.  The <i>key</i> may be a partial key.
   *The file must be open when invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.   
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readNextEqual(byte[] key, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    return fillInRecord(impl_.doItRecord("readNextEqual", new Class[] { byte[].class, Integer.TYPE }, new Object[] { key, new Integer(numberOfKeyFields) })); //@D0C
  }


  /**
   *Overrides the ObjectInputStream.readObject() method in order to return any
   *transient parts of the object to there properly initialized state.  We also
   *generate a declared file name for the object.  I.e we in effect
   *call the null constructor.  By calling ObjectInputStream.defaultReadObject()
   *we restore the state of any non-static and non-transient variables.  We
   *then continue on to restore the state (as necessary) of the remaining variables.
   *@param in The input stream from which to deserialize the object.

   *@exception ClassNotFoundException If the class being deserialized is not found.
   *@exception IOException If an error occurs during deserialization.
  **/
  private void readObject(java.io.ObjectInputStream in)
    throws ClassNotFoundException,
           IOException
  {
    in.defaultReadObject();
    initializeTransient(); //@C0C
  }


  /**
   *Reads the previous record whose key matches the key of the current record.
   * The file must be open when invoking this method.  The file must be
   *positioned on an active record when invoking this method.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readPreviousEqual()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Verify object state
    checkOpen();
    return fillInRecord(impl_.doItRecord("readPreviousEqual", new Class[0], new Object[0])); //@D0C
  }


  /**
   *Reads the previous record whose key matches the specified key.  The search does
   *not include the current record.  The <i>key</i> may be a partial key.
   *The file must be open when invoking this method.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readPreviousEqual(Object[] key)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameter(key); //@C0A
    return fillInRecord(impl_.doItRecord("readPreviousEqual", new Class[] { Object[].class }, new Object[] { key })); //@D0C
  }


  // @A2A
  /**
   *Reads the previous record whose key matches the specified key.  The search does
   *not include the current record.  The <i>key</i> may be a partial key.
   *The file must be open when invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.   
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readPreviousEqual(byte[] key, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    checkParameters(key, numberOfKeyFields); //@C0A
    return fillInRecord(impl_.doItRecord("readPreviousEqual", new Class[] { byte[].class, Integer.TYPE }, new Object[] { key, new Integer(numberOfKeyFields) })); //@D0C
  }


  /**
   *Updates the record specified by key.  The file must be open when invoking
   *this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param record The record with which to update the existing record.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void update(Object[] key, Record record)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Verify parameters
    if (record == null)
    {
      throw new NullPointerException("record");
    }
    positionCursor(key);
    update(record);
  }


  // @A2A
  /**
   *Updates the record specified by key.  The file must be open when invoking
   *this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param record The record with which to update the existing record.
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void update(byte[] key, Record record, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Verify parameters
    if (record == null)
    {
      throw new NullPointerException("record");
    }

    positionCursor(key, numberOfKeyFields);
    update(record);
  }



  /**
   *Updates the first record meeting the specified search criteria based on
   *<i>key</i>.  The <i>searchType</i> indicates that the record whose key first meets
   *the search criteria when compared to <i>key</i> should be returned.  The file
   *must be open when invoking this method.
   *@param key The values that make up the key with which to find the record.
   *The <i>key</i> must contain at least one element.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param record The record with which to update the existing record.
   *@param searchType Constant indicating the type of match required.  Valid values are:
   *<ul>
   *<li>KEY_EQ<br>
   *First record whose key is equal to <i>key</i>.
   *<li>KEY_LT<br>
   *First record whose key is less than <i>key</i>.
   *<li>KEY_LE<br>
   *First record whose key is less than or equal to <i>key</i>.
   *<li>KEY_GT<br>
   *First record whose key is greater than <i>key</i>.
   *<li>KEY_GE<br>
   *First record whose key is greater than or equal to <i>key</i>.
   *</ul>
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void update(Object[] key, Record record, int searchType)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Verify parameters
    if (record == null)
    {
      throw new NullPointerException("record");
    }
    positionCursor(key, searchType);
    update(record);
  }



  // @A2A
  /**
   *Updates the first record meeting the specified search criteria based on
   *<i>key</i>.  The <i>searchType</i> indicates that the record whose key first meets
   *the search criteria when compared to <i>key</i> should be returned.  The file
   *must be open when invoking this method.
   *@param key The byte array that contains the byte values that make up the key with which to find the record.
   *The byte array <i>key</i> must contain the byte values from at least one key field.  The types and order of
   *the elements that make up <i>key</i> must match the type and order of the
   *key fields in the record format for this object.  Null values for key fields
   *are not supported.
   *@param record The record with which to update the existing record.
   *@param searchType Constant indicating the type of match required.  Valid values are:
   *<ul>
   *<li>KEY_EQ<br>
   *First record whose key is equal to <i>key</i>.
   *<li>KEY_LT<br>
   *First record whose key is less than <i>key</i>.
   *<li>KEY_LE<br>
   *First record whose key is less than or equal to <i>key</i>.
   *<li>KEY_GT<br>
   *First record whose key is greater than <i>key</i>.
   *<li>KEY_GE<br>
   *First record whose key is greater than or equal to <i>key</i>.
   *</ul>
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void update(byte[] key, Record record, int searchType, int numberOfKeyFields)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Verify parameters
    if (record == null)
    {
      throw new NullPointerException("record");
    }
    positionCursor(key, searchType, numberOfKeyFields);
    update(record);
  }


  //@C0A
  /**
  * Make sure the file is open and the key is valid.
  **/
  private void checkParameter(Object[] key)
  {
    if (key == null)
    {
      throw new NullPointerException("key");
    }
    if (key.length == 0)
    {
      throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    if (key.length > recordFormat_.getNumberOfKeyFields())
    {
      throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    for (int i=0; i<key.length; ++i)
    {
      if (key[i] == null)
        throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Verify object state
    checkOpen();    
  }

  //@C0A
  /**
  * Make sure the file is open and the key is valid.
  **/
  private void checkParameters(byte[] key, int numberOfKeyFields)
  {
    if (key == null)
    {
      throw new NullPointerException("key");
    }
    if (key.length == 0)
    {
      throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    if ((numberOfKeyFields < 1) || (numberOfKeyFields > recordFormat_.getNumberOfKeyFields()))
    {
      throw new ExtendedIllegalArgumentException("numberOfKeyFields",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    checkOpen();
  }
}
