///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMRecordCache.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

/**
 *Class providing caching services to the AS400File classes.
**/
class DDMRecordCache
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // Constant indicating direction of BACKWARD for the cache
  static final int BACKWARD = 1;
  // Constant indicating direction of FORWARD for the cache
  static final int FORWARD = 0;
  // Indicates current direction of the cache.  This is set whenever
  // the cache is refreshed.
  int currentDirection_ = FORWARD;
  // Current record position in the cache
  private int currentPosition_;
  // Indicates if the first record of the cache is the first record of the file
  private boolean containsFirstRecord_;
  // Indicates if the last record of the cache is the last record of the file
  private boolean containsLastRecord_;
  // Indicates if the cache contains any records
  private boolean isEmpty_;
  // The records contained in this cache
  private Record[] records_ = new Record[0];
  // The current size of the cache (number of records cached currently)
  private int size_;

  /**
   *Constructs a DDMRecordCache object.
  **/
  DDMRecordCache()
  {
    setIsEmpty();
  }

  /**
   *Constructs a DDMRecordCache object.
   *@param records the records with which to populate the cache.  This value may be
   *null.
   *@param direction the direction in which to populate the cache.  Valid values are
   *FORWARD and BACKWARD.  If <i>direction</i> is FORWARD, the records are stored
   *in the cache in the order in which they occur in <i>records</i>.  If
   *<i>direction</i> is BACKWARD, the records are stored in the reverse order in which
   *they occur in <i>records</i>.
   *@param firstRecord indicates if the first record of the file is in <i>records</i>.
   *@param lastRecord indicates if the last record of the file is in <i>records</i>.
  **/
  DDMRecordCache(Record[] records, int direction, boolean firstRecord, boolean lastRecord)
  {
    refresh(records, direction, firstRecord, lastRecord);
  }

  /**
   *Adds a record to the cache.
   *@param record the record to add.
   *@param end indicates if the record is to be added to the end of the cache.  If
   *true, the record is added to the end of the cache; otherwise the record is added
   *to the beginning of the cache.
  **/
  void add(Record record, boolean end)
  { // Create new record array to hold the old records plus the new record
    Record[] newRecords = new Record[records_.length + 1];
    if (end)
    { // Add the record to the end of the cache
      System.arraycopy(records_, 0, newRecords, 0, records_.length);
      newRecords[records_.length] = record;
    }
    else
    { // Add new record to the beginning of the cache
      System.arraycopy(records_, 0, newRecords, 1, records_.length);
      newRecords[0] = record;
      // Need to update the current position
     currentPosition_++;
    }
    // Update state variables
    records_ = newRecords;
    size_ = records_.length;
    isEmpty_ = false;
  }

  /**
   *Determines if the keys are the same.
   *@param key key to compare
   *@param recKey key of record being compared to
   *@return true if the keys match; false otherwise
  **/
  boolean compareKeys(Object[] key, Object[] recKey)
  {
    if (key.length > recKey.length)
    { // Key is greater in length than recKey; not valid
      return false;
    }
    // Key has possibilities - may be a partial key, or may be a full key
    // so we only check matches up to the length of the "key" passed in.
    boolean match = true;  // Assume we have a match until proven otherwise
    int keyIndex = 0;
    int recKeyIndex = 0;
    RecordFormat rf = records_[0].getRecordFormat();  // Record format for the records
                                                      // in the cache.
    for (int j = 0; j < key.length && match; ++j)
    {
      if (key[j] instanceof byte[] && recKey[j] instanceof byte[])
      {
        if (((byte[])key[j]).length != ((byte[])recKey[j]).length)
        { // Key field length mismatch; keys don't match
          match = false;
        }
        else
        { // Check field byte by byte
          for (int k = 0; k < ((byte[])key[j]).length; ++k)
          {
            if (((byte[])key[j])[k] != ((byte[])recKey[j])[k])
            { // Key field mismatch; keys don't match, get out of loop
              match = false;
              break;
            }
          }
        }
      }
      else if (rf.getKeyFieldDescription(j) instanceof VariableLengthFieldDescription)
      {
        if (((String)key[j]).equals("") && ((String)recKey[j]).equals(""))
        {
          // We have a match for this field
        }
        else
        {
          // @A1D
          // Deleted the code that checks for variable length field.
          // Strip the trailing blanks off the key and recKey for both
          // the variable length & fixed length fields.
//          if (((VariableLengthFieldDescription)rf.getKeyFieldDescription(j)).isVariableLength())    // @A1D
//          {                                                                                         // @A1D
            // For variable length character fields (i.e. not hex fields) we strip
            // the trailing blanks from the keys prior to checking for a match.
            // We do this because that is how DDM does it when retrieving from the file.
            keyIndex = ((String)key[j]).length() - 1;
            while (((String)key[j]).charAt(keyIndex) == ' ')
            {
              keyIndex--;
            }
            recKeyIndex = ((String)recKey[j]).length() - 1;
            while (((String)recKey[j]).charAt(recKeyIndex) == ' ')
            {
              recKeyIndex--;
            }
            // Check stripped values
            if (!((String)key[j]).substring(0, keyIndex + 1).equals(((String)recKey[j]).substring(0, recKeyIndex + 1)))
            {
              match = false;
            }
          // Start of @A1D
          /*
          }
          else
          {
            if (!key[j].equals(recKey[j]))
            { // Key field mismatch; keys don't match
              match = false;
            }
          }
          */
          // End of @A1D
        }
      }
      else
      { // Not variable length/not character field
        if (!key[j].equals(recKey[j]))
        { // Key field mismatch; keys don't match
          match = false;
        }
      }
    }
    return match;
  }

  /**
   *Indicates if the cache contains the first record of the file.
   *@return true if the cache contains the first record of the file; false otherwise.
  **/
  boolean containsFirstRecord()
  {
    return containsFirstRecord_;
  }

  /**
   *Indicates if the cache contains the last record of the file.
   *@return true if the cache contains the last record of the file; false otherwise.
  **/
  boolean containsLastRecord()
  {
    return containsLastRecord_;
  }

  /**
   *Dump the contents of the cache to standard out.
  **/
  void dump()
  {
    System.out.println("Dumping cache:");
    for (int i = 0; i < records_.length; ++i)
    {
      System.out.println(records_[i]);
    }
  }

  /**
   *Find the record specified by record number in the cache.
   *@param recordNumber the record number for which to search.
   *@return the index of the record in the cache or -1 if the record does not exist
   *in the cache.
  **/
  int findRecord(int recordNumber)
  {
    for (int i = 0; i < size_; ++i)
    { // Look for the record
      if (records_[i].getRecordNumber() == recordNumber)
      { // Match; return now
        return i;
      }
    }
    // No match
    return -1;
  }

  /**
   *Find the record specified by key from the current position in the cache.
   *@param key the key of the record for which to search.
   *@return the index of the record in the cache or -1 if the record does not exist
   *in the cache.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  int findRecord(Object[] key, boolean searchForward)
    throws UnsupportedEncodingException
  {
    boolean match;  // Indicates if we have a match

    // Search for a match.  If a match occurs we will return before this loop finishes
    if (key.length > records_[0].getKeyFields().length)
    { // Key passed in is invalid; return -1
      return -1;
    }
    if (searchForward)
    {
      for (int i = currentPosition_ + 1; i < size_; ++i)
      {
        match = compareKeys(key, records_[i].getKeyFields());
        if (match)
        {
          return i;
        }
      }
    }
    else
    {
      for (int i = currentPosition_ - 1; i > -1; --i)
      {
        match = compareKeys(key, records_[i].getKeyFields());
        if (match)
        {
          return i;
        }
      }
    }

    // No record found matching key
    return -1;
  }

  /**
   *Returns the copyright for the class.
   *@return the copyright for this class.
  **/
  private static String getCopyright()
  {
    return Copyright.copyright;
  }

  /**
   *Returns the record currently pointed to in the cache.
   *@return the record currently pointed to in the cache or null if not currently
   *positioned on a record.
  **/
  Record getCurrent()
  {
    return (currentPosition_ > -1 && currentPosition_ < size_)? records_[currentPosition_] : null;
  }

  /**
   *Returns the first record in the cache.
   *@return the first record in the cache or null if cache is empty.
  **/
  Record getFirst()
  {
    if (isEmpty_)
    {
      return null;
    }
    currentPosition_ = 0;
    return records_[currentPosition_];
  }

  /**
   *Returns the last record in the cache.
   *@return the last record in the cache or null if cache is empty.
  **/
  Record getLast()
  {
    if (isEmpty_)
    {
      return null;
    }
    currentPosition_ = size_ - 1;
    return records_[currentPosition_];
  }

  /**
   *Returns the next record in the cache.
   *@return the next record in the cache, or null if cache is empty or if
   *we are at the end of the cache.
  **/
  Record getNext()
  {
    if (isEmpty_)
    {
      return null;
    }
    return (currentPosition_ < size_ - 1)? records_[++currentPosition_] : null;
  }

  /**
   *Returns the previous record in the cache.
   *@return the previous record in the cache, or null if cache is empty or if
   *we are at the beginning of the cache.
  **/
  Record getPrevious()
  {
    if (isEmpty_)
    {
      return null;
    }
    return (currentPosition_ != 0)? records_[--currentPosition_] : null;
  }

  /**
   *Returns the first record in the cache that matches the specified record number.
   *@param recordNumber the record number of the record to return.
   *@return the first record in the cache that matches the specified record number,
   *or null if cache is empty or if the record is not found.
  **/
  Record getRecord(int recordNumber)
  {
    if (isEmpty_)
    {
      return null;
    }
    int index = findRecord(recordNumber);
    if (index != -1)
    {
      currentPosition_ = index;
      return records_[index];
    }
    else
    {
      return null;
    }
  }

  /**
   *Returns the next record in the cache that matches the specified key.
   *@param key the key of the record to return.
   *@return the next record in the cache that matches the specified key,
   *or null if cache is empty or if the record is not found.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  Record getNextEqualRecord(Object[] key)
    throws UnsupportedEncodingException
  {
    if (isEmpty_)
    {
      return null;
    }
    int index = findRecord(key, true);
    if (index != -1)
    {
      currentPosition_ = index;
      return records_[index];
    }
    else
    {
      return null;
    }
  }

  /**
   *Returns the previous record in the cache that matches the specified key.
   *@param key the key of the record to return.
   *@return the previous record in the cache that matches the specified key,
   *or null if cache is empty or if the record is not found.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  Record getPreviousEqualRecord(Object[] key)
    throws UnsupportedEncodingException
  {
    if (isEmpty_)
    {
      return null;
    }
    int index = findRecord(key, false);
    if (index != -1)
    {
      currentPosition_ = index;
      return records_[index];
    }
    else
    {
      return null;
    }
  }

  /**
   *Indicates if we are after the last record.
  **/
  boolean isAfterLast()
  {
    return (isEmpty_)? false : (currentPosition_ == size_);
  }

  /**
   *Indicates if we are before the first record.
  **/
  boolean isBeforeFirst()
  {
    return (isEmpty_)? false : (currentPosition_ == -1);
  }

  /**
   *Indicates if we are at the beginning of the cache.
   *@return true if we are at the beginning of the cache; false otherwise.
  **/
  boolean isBeginningOfCache()
  {
    return (currentPosition_ <= 0);
  }

  /**
   *Indicates if the cache is empty.
   *@return true if the cache is empty; false otherwise.
  **/
  boolean isEmpty()
  {
    return isEmpty_;
  }

  /**
   *Indicates if we are at the end of the cache.
   *@return true if we are at the end of the cache; false otherwise.
  **/
  boolean isEndOfCache()
  {
    return (currentPosition_ >= size_ - 1);
  }

  /**
   *Refreshes the cache with the supplied records.
   *@param records the records with which to populate the cache.  This value may be
   *null.
   *@param direction the direction in which to populate the cache.  Valid values are
   *FORWARD and BACKWARD.  If <i>direction</i> is FORWARD, the records are stored
   *in the cache in the order in which they occur in <i>records</i>.  If
   *<i>direction</i> is BACKWARD, the records are stored in the reverse order in which
   *they occur in <i>records</i>.
   *@param firstRecord indicates if the first record of the file is in <i>records</i>.
   *@param lastRecord indicates if the last record of the file is in <i>records</i>.
  **/
  void refresh(Record[] records, int direction, boolean firstRecord, boolean lastRecord)
  {
    // Set the current direction
    currentDirection_ = direction;
    if (records == null || records.length == 0)
    {
      // Set the state variables
      isEmpty_ = true;
      containsFirstRecord_ = false;
      containsLastRecord_ = false;
      currentPosition_ = -1;
      size_ = 0;
    }
    else
    {
      if (direction == FORWARD)
      { // Records are ordered correctly
        records_ = records;
      }
      else
      { // Need to reverse the order of the records to place them into the array correctly
        records_ = new Record[records.length];
        for (int i = records.length - 1, j = 0; i >= 0; --i, ++j)
        {
          records_[j] = records[i];
        }
      }
      containsFirstRecord_ = firstRecord;
      containsLastRecord_ = lastRecord;
      // The current position of the cache depends on the direction - if we are going forward it
      // is prior to the first record in the cache; if we are going backward it is after the last
      // record in the cache.  When direction is backward it is expected that the user will be
      // doing readPrevious()'s to search from the end of the cache (file).
      currentPosition_ = (direction == FORWARD)? 0 : records.length - 1;
      size_ = records_.length;
      isEmpty_ = false;
    }
    if (Trace.isTraceOn())
    {
      Trace.log(Trace.INFORMATION, "Record cache refreshed:");
      for (int i = 0; i < records_.length; ++i)
      {
        Trace.log(Trace.INFORMATION, records_[i].toString());
      }
    }
  }

  /**
   *Sets the state of the cache to empty.
  **/
  void setIsEmpty()
  {
    // Set the state variables
    isEmpty_ = true;
    containsFirstRecord_ = false;
    containsLastRecord_ = false;
    currentPosition_ = -1;
    size_ = 0;
  }

  /**
   *Sets the current cache position to the record matching the specified record number.
   *@param recordNumber the record number of the record to position to.
   *@return true if the record was found and positioned to; false otherwise.
  **/
  boolean setPosition(int recordNumber)
  {
    if (isEmpty_)
    {
      return false;
    }
    int i = findRecord(recordNumber);
    if (i > -1)
    {
      currentPosition_ = i;
    }
    return (i > -1);
  }

  /**
   *Sets the current cache position to the record matching the specified key.
   *@param key the key of the record to position to.
   *@return true if the record was found and positioned to; false otherwise.
   *@exception UnsupportedEncodingException If an error occurs when converting
   *the AS400 data to a Java Object.
  **/
  boolean setPosition(Object[] key)
    throws UnsupportedEncodingException
  {
    if (isEmpty_)
    {
      return false;
    }
    int i = findRecord(key, true);
    if (i > -1)
    {
      currentPosition_ = i;
    }
    return (i > -1);
  }

  /**
   *Sets the current cache position to after the last record in the cache
  **/
  void setPositionAfterLast()
  {
    if (!isEmpty_)
    {
      currentPosition_ = size_;
    }
  }

  /**
   *Sets the current cache position to before the first record in the cache
  **/
  void setPositionBeforeFirst()
  {
    if (!isEmpty_)
    {
      currentPosition_ = -1;
    }
  }

  /**
   *Sets the current cache position to the first record in the cache
  **/
  void setPositionFirst()
  {
    if (!isEmpty_)
    {
      currentPosition_ = 0;
    }
  }

  /**
   *Sets the current cache position to the last record in the cache
  **/
  void setPositionLast()
  {
    if (!isEmpty_)
    {
      currentPosition_ = size_ - 1;
    }
  }

  /**
   *Sets the current cache position to the next record in the cache
  **/
  void setPositionNext()
  {
    if (!isEmpty_ && currentPosition_ < size_)
    {
      currentPosition_++;
    }
  }

  /**
   *Sets the current cache position to the previous record in the cache
  **/
  void setPositionPrevious()
  {
    if (!isEmpty_ && currentPosition_ > -1)
    {
      currentPosition_--;
    }
  }
}
