///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FileImplBase.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.beans.PropertyVetoException;
import java.math.BigDecimal;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method; //@F0A
import java.text.Collator;

abstract class AS400FileImplBase implements AS400FileImpl, Cloneable //@B5C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // Is this class an ImplRemote or an ImplNative
  boolean isNative_ = false; //@E2A

  boolean discardReplys_ = false; //@D1A (moved out of AS400FileImplRemote)

  // Converter that converts to the AS400 job CCSID.
  ConverterImplRemote converter_; //@B5C

  // retrieve the requested record, do not consider deleted records as
  // part of the file
  static final byte DATA_DTA_DTARCD = 0;
  // position the file to the requested record, do not
  // retrieve the data, do not consider deleted records
  static final byte DATA_NODTA_DTARCD = 1;
  // GET
  static final byte OPER_GET = 1;
  // GETD
  static final byte OPER_GETD = 2;
  // GETK
  static final byte OPER_GETK = 3;
  // PUTDR
  static final byte OPER_PUTDR = 4;
  // PUT
  static final byte OPER_PUT = 5;
  // UPDATE
  static final byte OPER_UPDATE = 7;
  // DELETE
  static final byte OPER_DELETE = 8;
  // FEOD
  static final byte OPER_FEOD = 9;
  // retrieve for read only
  static final byte SHR_READ_NORM = 0;
  // retrieve for read only and give up lock on previously locked record
  static final byte SHR_READ_NORM_RLS = 2;
  // retrieve for update
  static final byte SHR_UPD_NORM = 3;
  // retrieve the record for read leaving the previous
  // file position as the current file position
  static final byte SHR_READ_NOPOS = 0x10;
  // retrieve for read only leaving the previous file position as the
  // current file position and give up lock on previously locked record
  static final byte SHR_READ_NOPOS_RLS = 0x12;
  // retrieve the record for update leaving the previous
  // file position as the current file position after the get request
  static final byte SHR_UPD_NOPOS = 0x13;
  // retrieve for update but do not wait on record locks
  // held by the same process
  static final byte SHR_UPD_NORM_NW = 0x23;
  // retrieve the record for update leaving the previous file position as
  // the current file position after the get request but do not wait on
  // record locks held by the same process
  static final byte SHR_UPD_NOPOS_NW = 0x33;
  // retrieve first record
  static final byte TYPE_GET_FIRST = 1;
  // retrieve last record
  static final byte TYPE_GET_LAST = 2;
  // retrieve next record
  static final byte TYPE_GET_NEXT = 3;
  // retrieve previous record
  static final byte TYPE_GET_PREV = 4;
  // retrieve current record
  static final byte TYPE_GET_SAME = 0x21;
  // Retrieve record at the specified position (relative to file start).
  static final byte TYPE_GETD_ABSRRN = 8;
  // WDMHLIB
  private static final int WDMHLIB = 72;
  // WDMHMBR
  private static final int WDMHMBR = 73;

  // Open feedback information from open().
  DDMS38OpenFeedback openFeedback_;

  // The following String constants are used to format the DDS source when
  // creating a file from a user supplied RecordFormat object
  static final String STR16 = "                ";
  static final String STR18 = "                  ";
  static final String STR44 = "                                            ";

  // Indicates which explicit locks, if any, have been obtained.  There are six
  // possible types of locks.  Initially each array element is false.
  Vector explicitLocksObtained_ = new Vector(6);

  // AS/400 systems currently under commitment control.
  static Vector commitmentControlSystems_ = new Vector();

  // Used for commitment control when we're running natively. See AS400FileImplBase.
  static boolean nativeCommitmentControlStarted_ = false; //@E2A

  //@B0A: These are duplicated from the public class since they are either
  //      beans or parts of beans.
  AS400ImplRemote system_ = null; //@B5C
  String name_ = null;
  RecordFormat recordFormat_ = null;
  String library_ = null;
  String file_ = null;
  String member_ = null;

  // Holds any cached records
  DDMRecordCache cache_ = new DDMRecordCache();
  // Indicates if records are to be cached; if blockingFactor_ > 1 and file
  // is not opened for READ_WRITE, records will be cached.
  boolean cacheRecords_ = false;

  // Caches the record formats retrieved from the AS/400 for the file
  RecordFormat[] rfCache_ = null; //@B2A

  // The lock level for commitment control for this file.  This value is
  // specified upon construction of the object if commitment control has been
  // started for the connection.
  int commitLockLevel_ = -1;

  // The number of records to retrieve during a read operation or the maximum
  // number of records to write at one time during a writeCollection()
  int blockingFactor_;

  // A boolean indicating if it's a 'read no update' in an open type of read/write.
  // If 'read no update' is true, it allows a file to be opened as read/write, yet
  // reading won't exclusively lock the records being read. The default is false.
  boolean readNoUpdate_ = false;

  // Manner in which file has been opened.  This value will be set upon open().
  // -1 indicates that the file is not open.
  int openType_ = -1;

  // These two variables are used to do the CTLL name conversion once after the
  // file is opened so that the converion result can be directly used in later
  // DDM request streams.
  byte[] recordFormatCTLLName_ = null;
  private String recordFormatName_ = "";

  //@B0A
  // This flag tells us if we are an impl for a KeyedFile or a SequentialFile.
  // Created primarily for use in refreshCache().
  private boolean isKeyed_ = false;


  //@B4A - constants for comparing keys
  private static final int EQUAL = 1; // keys are equal
  private static final int LESS_THAN = 2; // search key < current key
  private static final int GREATER_THAN = 3; // search key > current key
  private static final int UNKNOWN = 4; // Not equal, but keep searching.

  //@B4A - Use a Collator for comparing String keys
  private Collator collator_ = Collator.getInstance();

  public void doIt(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    try
    {
      invoke(this, methodName, classes, objects); //@F0C
    }
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
    catch(Exception e2)
    {
      if (Trace.isTraceErrorOn())
        Trace.log(Trace.ERROR, e2.toString(), e2);
      throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
    }
  }

  public void doItNoExceptions(String methodName, Class[] classes, Object[] objects)
  {
    try
    {
      invoke(this, methodName, classes, objects); //@F0C
    }
    catch(Exception e2)
    {
      if (Trace.isTraceErrorOn())
        Trace.log(Trace.ERROR, e2.toString(), e2);
      throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
    }
  }

  public Record doItRecord(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    try
    {
      return (Record)invoke(this, methodName, classes, objects); //@F0C
    }
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
    catch(Exception e2)
    {
      if (Trace.isTraceErrorOn())
        Trace.log(Trace.ERROR, e2.toString(), e2);
      throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
    }
  }

  public Record[] doItRecordArray(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    try
    {
      return (Record[])invoke(this, methodName, classes, objects); //@F0C
    }
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
    catch(Exception e2)
    {
      if (Trace.isTraceErrorOn())
        Trace.log(Trace.ERROR, e2.toString(), e2);
      throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
    }
  }

  //@B2A
  public RecordFormat doItRecordFormat(String methodName, Class[] classes, Object[] objects)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    try
    {
      return (RecordFormat)invoke(this, methodName, classes, objects); //@F0C
    }
    catch(InvocationTargetException e)
    {
      throw ProxyClientConnection.rethrow4a(e);
    }
    catch(Exception e2)
    {
      if (Trace.isTraceErrorOn())
        Trace.log(Trace.ERROR, e2.toString(), e2);
      throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
    }
  }

  public int doItInt(String methodName)
  {
    try
    {
      return ((Integer)invoke(this, methodName, new Class[0], new Object[0])).intValue(); //@F0C
    }
    catch(Exception e)
    {
      if (Trace.isTraceErrorOn())
        Trace.log(Trace.ERROR, e.toString(), e);
      throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
    }
  }

  public boolean doItBoolean(String methodName)
  {
    try
    {
      return ((Boolean)invoke(this, methodName, new Class[0], new Object[0])).booleanValue(); //@F0C
    }
    catch(Exception e)
    {
      if (Trace.isTraceErrorOn())
        Trace.log(Trace.ERROR, e.toString(), e);
      throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
    }
  }

  //@E2A
  public boolean doItBoolean(String methodName, Class[] classes, Object[] objects)
  {
    try
    {
      return ((Boolean)invoke(this, methodName, classes, objects)).booleanValue(); //@F0C
    }
    catch(Exception e)
    {
      if (Trace.isTraceErrorOn())
        Trace.log(Trace.ERROR, e.toString(), e);
      throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
    }
  }

  
  //@F0A - This method is copied from PxMethodReqSV. We don't
  // want to have dependencies on the proxy classes directly, so
  // that's why I included it here.
  static Object invoke(Object object, 
                       String methodName, 
                       Class[] argumentClasses,
                       Object[] arguments)
      throws ClassNotFoundException, 
             IllegalAccessException,
             InvocationTargetException, 
             NoSuchMethodException
  {      
      // Resolve the Method object.  First, try Class.getMethod() which 
      // only looks for public methods.  If that does not work, try 
      // Class.getDeclaredMethod(), which only looks for declared 
      // methods (not inherited methods).  Do this up the superclass tree.
      Method method = null;
      Class clazz = object.getClass();
      NoSuchMethodException e = null;
      while ((clazz != null) && (method == null)) {
          try {
              method = clazz.getMethod(methodName, argumentClasses);
          }
          catch (NoSuchMethodException e1) {
              try {
                  method = clazz.getDeclaredMethod(methodName, argumentClasses);
              }
              catch (NoSuchMethodException e2) {
                  e = e2;
                  clazz = clazz.getSuperclass();
              }
          }
      }
      if (method == null)
          throw e;

      // Call the method.
      return method.invoke (object, arguments);
  }


  public void setIsKeyed(boolean keyed)
  {
    isKeyed_ = keyed;
  }

  public boolean isReadNoUpdate()
  {
    return readNoUpdate_;
  }


  //@B0A
  /**
   *Adds a physical file member to the file represented by this object.
   *@param name The name of the member to create.  The <i>name</i> cannot
   *exceed 10 characters in length.  The <i>name</i> cannot be null.
   *@param textDescription The text description with which to create the file.
   *This value must be 50 characters or less.  If this value is null, the
   *text description will be blank.<br>
   *The name of the file and the AS400 system to which to connect must be set
   *prior to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped
   * unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the
   * AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public void addPhysicalFileMember(String name, String textDescription)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Create the ADDPFM command string
    StringBuffer cmd = new StringBuffer("QSYS/ADDPFM FILE(");
    cmd.append(library_);
    cmd.append("/");
    cmd.append(file_);
    cmd.append(") MBR(");
    cmd.append(name);
    cmd.append(")  TEXT(");
    // Determine correct text description and add it to the command string
    if (textDescription == null)
    { // No text description supplied.
      cmd.append(AS400File.BLANK); //@B3C
      cmd.append(")"); //@B3A
    }
    else if (textDescription.length() == 0 || textDescription.equalsIgnoreCase(AS400File.BLANK)) //@A3C
    { // Empty string passed for text or special value *BLANK specified
      cmd.append(AS400File.BLANK); //@B3C
      cmd.append(")"); //@B3A
    }
    else
    {
      // Enclose the text description in single quotes for the command
      cmd.append("'");
      cmd.append(textDescription);
      cmd.append("')");
    }

    // Add the member.
    AS400Message[] msgs = execute(cmd.toString()); //@B0C

    if (!(msgs.length > 0 && msgs[0].getID().equals("CPC7305")))
    {
      throw new AS400Exception(msgs);
    }
  }


  //@B4C
  /**
   *Determines if the keys are the same.
   *@param key key to compare
   *@param recKey key of record being compared to
   *@return The result of the key comparison (EQUAL, GREATER_THAN, LESS_THAN, or UNKNOWN).
  **/
  private int compareKeys(Object[] key, Object[] recKey)
  {
    if (key.length > recKey.length)
    { // Key is greater in length than recKey; not valid
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.WARNING, "compareKeys: Search key has too many fields.");
      }
      return UNKNOWN;
    }
    // Key has possibilities - may be a partial key, or may be a full key
    // so we only check matches up to the length of the "key" passed in.
    int match = EQUAL;
    for (int j = 0; j < key.length && match == EQUAL; ++j)
    {
      if (key[j] instanceof byte[] && recKey[j] instanceof byte[])
      {
        byte[] searchKey = (byte[])key[j];
        byte[] recordKey = (byte[])recKey[j];

        if (searchKey.length < recordKey.length)
          match = LESS_THAN;
        else if (searchKey.length > recordKey.length)
          match = GREATER_THAN;
        else // Check field byte by byte
        {
          for (int k = 0; k < searchKey.length && match == EQUAL; ++k)
          {
            if (searchKey[k] < recordKey[k]) match = LESS_THAN;
            else if (searchKey[k] > recordKey[k]) match = GREATER_THAN;
          }
        }
      }
      else if (recordFormat_.getKeyFieldDescription(j) instanceof VariableLengthFieldDescription)
      {
        //@B4C -- begin
        // Note: A String in a DDM field is always padded with blanks
        // to be the length of the field.
        // Therefore, we should ignore any trailing blanks in the keys,
        // but we must regard any leading ones.
        String searchKey = (String)key[j];
        String recordKey = (String)recKey[j];
        int searchKeyLength = searchKey.length();
        int recordKeyLength = recordKey.length();
        if (searchKeyLength < recordKeyLength)
        {
          // pad the search key with trailing blanks
          StringBuffer buf = new StringBuffer(searchKey);
          for (int i=searchKeyLength; i<recordKeyLength; ++i)
            buf.append(" ");
          searchKey = buf.toString();
        }
        else if (searchKeyLength > recordKeyLength)
        {
          // It is OK for the user to pass in a blank-padded search
          // key that is longer than the length of the field.
          // We just chop it to be the correct length.
          searchKey = searchKey.substring(0, recordKeyLength);
        }

        int res = collator_.compare(searchKey, recordKey);
        if (res > 0) match = GREATER_THAN;
        else if (res < 0) match = LESS_THAN;
        //@B4C -- end
      }
      else if ( key[j] instanceof BigDecimal && recKey[j] instanceof BigDecimal)
      { // decimal field
        BigDecimal searchKey = (BigDecimal)key[j];
        BigDecimal recordKey = (BigDecimal)recKey[j];
        int res = searchKey.compareTo(recordKey);
        if (res > 0) match = GREATER_THAN;
        else if (res < 0) match = LESS_THAN;
      }
      else if (!key[j].equals(recKey[j]))
      { // some other type
        // Since we don't know the type, we have to keep searching
        // the whole file.
        match = UNKNOWN;
      }
    }
    return match;
  }


  //@B4C
  // @A2A
  /**
   *Determines if the keys are the same.
   *@param key key in bytes to compare
   *@param recKey key of record in bytes being compared to
   *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>
   *being compared to <i>recKey</i>. The number of key fields must be greater than 0 and less
   *than the total number of key fields in the record format for this file.
   *@return The result of the key comparison (EQUAL, GREATER_THAN, LESS_THAN, or UNKNOWN).
  **/
  private int compareKeys(byte[] key, byte[] recKey, int numberOfKeyFields)
  {
    if (key.length > recKey.length)
    { // Key is greater in length than recKey; not valid
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.WARNING, "compareKeys: Search key is too long.");
      }
      return UNKNOWN;
    }
    if ((numberOfKeyFields < 1) || (numberOfKeyFields > recordFormat_.getNumberOfKeyFields()))
    {
      throw new ExtendedIllegalArgumentException("numberOfKeyFields",
         ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Check to see if the byte length of 'key' is valid on a per key field basis.
    int keyByteLength = 0;
    FieldDescription[] fd = recordFormat_.getKeyFieldDescriptions();
    for (int i=0; i<numberOfKeyFields; i++)
    {
      keyByteLength += fd[i].getDataType().getByteLength();
      if (fd[i] instanceof VariableLengthFieldDescription && ((VariableLengthFieldDescription)fd[i]).isVariableLength())
        keyByteLength += 2;
    }
    if (key.length != keyByteLength)
    {
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.WARNING, "compareKeys: Search key byte length is not valid.");
      }
      return UNKNOWN;
    }

    // Now do the byte by byte compare.
    for (int i = 0; i < key.length; ++i)
    {
      if (key[i] < recKey[i])
        return LESS_THAN;
      else if (key[i] > recKey[i])
        return GREATER_THAN;
    }
    return EQUAL;
  }


  public int getBlockingFactor()
  {
    return blockingFactor_;
  }


  /**
   *Returns any explicit locks that have been obtained for this file.
   *Any locks that have been obtained through the lock(int) method are returned.
   *@see AS400File#lock
   *@return The explicit file locks held for this file.
   *        Possible lock values are:
   *        <ul>
   *        <li>READ_EXCLUSIVE_LOCK
   *        <li>READ_ALLOW_SHARED_READ_LOCK
   *        <li>READ_ALLOW_SHARED_WRITE_LOCK
   *        <li>WRITE_EXCLUSIVE_LOCK
   *        <li>WRITE_ALLOW_SHARED_READ_LOCK
   *        <li>WRITE_ALLOW_SHARED_WRITE_LOCK
   *        </ul>
   *If no explicit locks have been obtained for the file, an array of size zero
   *is returned.
   **/
  public int[] getExplicitLocks()
  {
    // Create an array of the explicit locks that are currently set.
    int[] locks = new int[explicitLocksObtained_.size()];
    for (int i = 0; i < locks.length; i++)
    {
      locks[i] = ((Integer) explicitLocksObtained_.elementAt(i)).intValue();
    }
    return locks;
  }


  public int getOpenType()
  {
    return openType_;
  }


  /**
   *Closes the file on the AS400.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   **/
  public void close() //@B0C -- this method is overridden by the subclasses
    throws AS400Exception, AS400SecurityException, InterruptedException,  IOException
  {
      // Shut down any caching
      cacheRecords_ = false;
      cache_.setIsEmpty();

      // Reset the blocking factor
      blockingFactor_ = 0;

      // Reset the open type. -1 indicates that the file is not open
      openType_ = -1;

      // Reset the commit lock level.
      commitLockLevel_ = (isCommitmentControlStarted() ?
                          AS400File.COMMIT_LOCK_LEVEL_NONE : -1 );
  }


  /**
   * This is a convenience method for setting all 4 properties at once, so as to
   * avoid 4 separate proxy calls.
  **/
  public void setAll(AS400Impl system, String pathName, RecordFormat rf, boolean readNoUpdate, boolean isKeyed) //@B5C
    throws IOException //@B5A - 06/08/1999
  {
    setSystem(system);
    setPath(pathName);
    setRecordFormat(rf);
    setReadNoUpdate(readNoUpdate); //@B5A
    setIsKeyed(isKeyed);
    setConverter(); //@B5A - 06/08/1999
  }


  /**
   *Commits all transactions since the last commit boundary.  Invoking this
   *method will cause all transactions under commitment control for this
   *connection to be committed.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   **/
  public abstract void commit()
    throws AS400Exception, AS400SecurityException, InterruptedException,  IOException;


  //@E2A
  // This method should be static, but we can't make a static call to
  // a native method (the call to execute()) so we just set all of the
  // necessary state variables and act like we're a normal object.
  public void commit(AS400Impl system)
    throws AS400Exception, AS400SecurityException, InterruptedException,  IOException
  {
    if (isCommitmentControlStarted(system))
    {
      // Setup state variables
      setSystem(system);
      setConverter();

      commit();
    }
  }


  //@B0A
  public void setConverter() throws IOException
  {
    converter_ = ConverterImplRemote.getConverter(system_.getCcsid(), system_); //@B5C
//@E0D    if (recordFormat_ != null) recordFormat_.setConverter(converter_); //@D0A
  }


  public void setPath(String name)
  {
    // Construct a QSYSObjectPathName object and parse out the library,
    // file and member names
    QSYSObjectPathName ifs = new QSYSObjectPathName(name);
    if (!(ifs.getObjectType().equals("FILE") || ifs.getObjectType().equals("MBR")))
    { // Invalid object type
      throw new IllegalPathNameException(name, IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
    }
    // Set the instance data as appropriate
    library_ = ifs.getLibraryName();
    file_ = ifs.getObjectName();
    if (ifs.getObjectType().equals("FILE"))
    { // No member specified; default member to *FIRST
      member_ = "*FIRST";
    }
    else
    { // Member specified; if special value %FILE% was specified, member name
      // is the file name
      member_ = (ifs.getMemberName().equalsIgnoreCase("*FILE") ? file_ :
                 ifs.getMemberName());
    }
    name_ = name;
    rfCache_ = null; //@B2A
  }

  public void setReadNoUpdate(boolean readNoUpdate)
  {
    readNoUpdate_ = readNoUpdate;
  }

  //@B2A
  /**
   * Retrieves rfCache_[rf]. If the cache is empty, retrieves the record
   * formats from the AS/400.
  **/
  public RecordFormat setRecordFormat(int rf)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (rfCache_ == null)
    {
      AS400FileRecordDescriptionImplRemote desc = new AS400FileRecordDescriptionImplRemote(); //@B5C
      desc.setPath(name_);     //@B5A
      desc.setSystem(system_); //@B5A
      rfCache_ = desc.retrieveRecordFormat();
    }
    if (rfCache_.length <= rf) // the index is too big
    {
      throw new ExtendedIllegalArgumentException("recordFormat (" + String.valueOf(rf) + ") too large", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    return rfCache_[rf];
  }

  //@B2A
  /**
   * Retrieves "rf" from rfCache_. If the cache is empty, retrieves the record
   * formats from the AS/400.
  **/
  public RecordFormat setRecordFormat(String rf)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (rfCache_ == null)
    {
      AS400FileRecordDescriptionImplRemote desc = new AS400FileRecordDescriptionImplRemote(); //@B5C
      desc.setPath(name_);     //@B5A
      desc.setSystem(system_); //@B5A
      rfCache_ = desc.retrieveRecordFormat();
    }
    RecordFormat toSet = null;
    for (int i=0; i<rfCache_.length; ++i)
    {
      if (rfCache_[i].getName().equals(rf))
        toSet = rfCache_[i];
    }
    if (toSet == null)
    {
      throw new ExtendedIllegalArgumentException("recordFormat (" + rf + ") not found", ExtendedIllegalArgumentException.FIELD_NOT_FOUND);
    }
    return toSet;
  }

  public void setRecordFormat(RecordFormat rf)
    throws IOException //@E0A
  {
    if (rf != null) //@E0A
    {
      recordFormat_ = rf;

      //@E0: RecordFormat.setConverter() used to grep through all of the AS400Text
      // objects and set up all of their internal Converters. However, this would
      // use a Converter for the system ccsid, which would override the AS400Text's
      // ccsid if it had been set. So, we have to check to see if the AS400Text's
      // ccsid has been set before we blindly go and give it a new Converter.

//@E0D    if (converter_ != null) recordFormat_.setConverter(converter_); //@D0A
      if (converter_ != null) setConverter(); //@E0A

      //@E0A: This code block is from RecordFormat.setConverter().
      // initialize text objects
      for (int i=0; i<recordFormat_.getNumberOfFields(); ++i)
      {
        AS400DataType dt = ((FieldDescription)recordFormat_.getFieldDescription(i)).dataType_;
        if (dt instanceof AS400Text)
        {
          int textCcsid = ((AS400Text)dt).getCcsid();
          if (textCcsid != 65535) // ccsid already set, get a converter based on it
          {
            ((AS400Text)dt).setConverter(ConverterImplRemote.getConverter(textCcsid, system_));
          }
          else
          {
            ((AS400Text)dt).setConverter(converter_); // don't have a ccsid, just use the system's
          }
        }
      }
    }
  }

  public void setSystem(AS400Impl system) //@B5C
  {
    system_ = (AS400ImplRemote)system; //@B5C
    rfCache_ = null; //@B2A
  }

  /**
   *Creates the user file control block byte array.  XPF PLMIINC WWUFCB
   *contains the format for the ufcb.
   *@param openType Indicates how to open the file: READ_ONLY, READ_WRITE or
   *                WRITE_ONLY.
   *@param bf The blocking factor to specify.
   *@param access The type of access desired.  Valid values are "KEY" or "SEQ".
   *@param userBuffering if true use user buffering
   *@return The user file control block to be sent on an open() invocation.
  **/
  /*@D2D protected*/public byte[] createUFCB(int openType, int bf, String access,
                              boolean userBuffering)
    throws IOException
  {
    if (converter_ == null) setConverter(); //@B0A
    int openOptions;
    byte[] ufcb = null;
    boolean isCommitmentControlStarted =
      isCommitmentControlStarted();

    // The length of the byte array depends on which combination of
    // openType and access has been specified and whether commitment
    // control has been started.
    if (access.equalsIgnoreCase("SEQ") && openType == AS400File.WRITE_ONLY)
    { // Sequential, write_only
      ufcb = new byte[isCommitmentControlStarted ? 109 : 106]; //@A1C
    }
    else
    { // Sequential, read_write or read_only, commitment control has not been started.
      // Keyed, any type of open, commitment control has not been started
      ufcb = new byte[isCommitmentControlStarted ? 112 : 109]; //@A1C
    }

    // Set the open options byte.  This includes bits for turning off user buffering
    // and sharing
    int userBufferingAndODPSharingOff = (userBuffering ? 0x1003 : 0x1002);
    if (openType == AS400File.READ_ONLY)
    {
      openOptions = 0x20 | userBufferingAndODPSharingOff;
    }
    else if (openType == AS400File.READ_WRITE)
    {
      openOptions = 0x3C | userBufferingAndODPSharingOff;
    }
    else
    {
      openOptions = 0x10 | userBufferingAndODPSharingOff;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // The following section sets the fixed portion of the UFCB
    ////////////////////////////////////////////////////////////////////////////////////

    // Set the file portion of the UFCB (padded with EBCDIC blanks).
    converter_.stringToByteArray(file_, ufcb, 0);
    for (int i = file_.length(); i < 10; i++)
    {
      ufcb[i] = 0x40;
    }

    // Set the library portion of the UFCB (padded with EBCDIC blanks).
    BinaryConverter.unsignedShortToByteArray(WDMHLIB, ufcb, 10);
    converter_.stringToByteArray(library_, ufcb, 12);
    for (int i = library_.length(); i < 10; i++)
    {
      ufcb[i + 12] = 0x40;
    }

    // Set the member portion of the UFCB (padded with EBCDIC blanks).
    BinaryConverter.unsignedShortToByteArray(WDMHMBR, ufcb, 22);
    converter_.stringToByteArray(member_, ufcb, 24);
    for (int i = member_.length(); i < 10; i++)
    {
      ufcb[i + 24] = 0x40;
    }

    // Set the open options
    BinaryConverter.unsignedShortToByteArray(openOptions, ufcb, 46);

    // Set the release and version numbers
    ufcb[48] = (byte)0xF0;
    ufcb[49] = (byte)0xF1;
    ufcb[50] = (byte)0xF0;
    ufcb[51] = (byte)0xF0;

    // Set record blocking to yes
    ufcb[56] = 0x20;

    // Indicate that we can handle null capable fields
    ufcb[60] = 0x02;

    //////////////////////////////////////////////////////////////////////////////////
    // The following sections set the variable portion of the UFCB
    //////////////////////////////////////////////////////////////////////////////////
    int offset = 80;  // Keep track off the offset we are writing at
                      // This is necessary because in certain cases a parameter may not be
                      // specified, therefore the offset for the next parameter is not constant
    //////////////////////////////////////////////////////////////////////////////////
    // LVLCHK parameter
    //////////////////////////////////////////////////////////////////////////////////
    // Specify LVLCHK(NO);
    // Parameter id for LVLCHK is 6
    BinaryConverter.unsignedShortToByteArray(6, ufcb, offset);
    // All bits off indicates LVLCHK(NO)
    ufcb[offset + 2] = 0x00;
    offset += 3;

    //////////////////////////////////////////////////////////////////////////////////
    // ARRSEQ parameter
    //////////////////////////////////////////////////////////////////////////////////
    // Do not specify ARRSEQ parameter if access is SEQ and openType is WRITE_ONLY
    // Specify ARRSEQ(YES) if access is SEQ and openType is not WRITE_ONLY
    // Specify ARRSEQ(YES) if access is KEY and openType is WRITE_ONLY
    // Specify ARRSEQ(NO) if access is KEY and openType is not WRITE_ONLY
    if (!(access.equalsIgnoreCase("SEQ") && openType == AS400File.WRITE_ONLY))
    {
      // Parameter id for ARRSEQ is 60
      BinaryConverter.unsignedShortToByteArray(60, ufcb, offset);
      // Set the value; 0 = NO, 1 = YES
      if (access.equalsIgnoreCase("SEQ"))
      { // Use arrival sequence when accessing records
        ufcb[offset + 2] = (byte)0x80;
      }
      else if (openType == AS400File.WRITE_ONLY)
      { // Use arrival sequence
        ufcb[offset + 2] = (byte)0x80;
      }
      else
      { // Used keyed access path
        ufcb[offset + 2] = 0x00;
      }
      offset += 3;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // COMMIT parameter
    //////////////////////////////////////////////////////////////////////////////////
    if (isCommitmentControlStarted)
    {
      // Parameter id for COMMIT is 59
      BinaryConverter.unsignedShortToByteArray(59, ufcb, offset);
      switch(commitLockLevel_)
      {
      case AS400File.COMMIT_LOCK_LEVEL_NONE:
        // COMMIT(NO); No commitment control for this file
        ufcb[offset + 2] = (byte)0x00;
        break;
      case AS400File.COMMIT_LOCK_LEVEL_DEFAULT:
        // COMMIT(YES); use lock level specified by connection
        ufcb[offset + 2] = (byte)0x80;
        break;
      case AS400File.COMMIT_LOCK_LEVEL_CHANGE:
        // COMMIT(*CHG); use lock level of *CHG
        ufcb[offset + 2] = (byte)0x82;
        break;
      case AS400File.COMMIT_LOCK_LEVEL_CURSOR_STABILITY:
        // COMMIT(*CS); use lock level of *CS
        ufcb[offset + 2] = (byte)0x86;
        break;
      case AS400File.COMMIT_LOCK_LEVEL_ALL:
        // COMMIT(*ALL); use lock level of *ALL
        ufcb[offset + 2] = (byte)0x87;
        break;
      }
      offset += 3;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // SEQONLY parameter
    //////////////////////////////////////////////////////////////////////////////////
    // Parameter id for SEQONLY is 58
    BinaryConverter.unsignedShortToByteArray(58, ufcb, offset);
    if ((access.equalsIgnoreCase("SEQ") || access.equalsIgnoreCase("KEY")) &&
        openType != AS400File.READ_WRITE)
    {
      // SEQONLY(YES), number of records specified = YES
      ufcb[offset + 2] = (byte)0xC0;
      BinaryConverter.unsignedShortToByteArray(bf, ufcb, offset + 3);
    }
    else
    {
      // SEQONLY(NO), number of records specified = YES
      ufcb[offset + 2] = 0x40;
      BinaryConverter.unsignedShortToByteArray(bf, ufcb, offset + 3);
    }
    offset += 5;

    //@A1C: Added RECORD FORMAT GROUP section
    //////////////////////////////////////////////////////////////////////////////////
    // RECORD FORMAT GROUP parameter
    //////////////////////////////////////////////////////////////////////////////////
    BinaryConverter.unsignedShortToByteArray(9, ufcb, offset); // Parm id
    BinaryConverter.unsignedShortToByteArray(1, ufcb, offset + 2); // Max # of record formats
    BinaryConverter.unsignedShortToByteArray(1, ufcb, offset + 4); // Cur # of record formats
    String rfName = recordFormat_.getName(); // What if the record format name is too long?
    if (rfName.length() > 10) rfName = rfName.substring(0, 10);
    converter_.stringToByteArray(rfName, ufcb, offset + 6);
    for (int i = rfName.length(); i < 10; i++)
    {
      ufcb[i + offset + 6] = 0x40;
    }
    offset += 16;

    // Indicate the end of the variable portion of the UFCB; this is required
    BinaryConverter.unsignedShortToByteArray(32767, ufcb, offset);

    return ufcb;
  }

  /**
   *Deletes the record at the current cursor position.  The file must be open and
   *the cursor must be positioned on an active record.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public abstract void deleteCurrentRecord()
    throws AS400Exception, AS400SecurityException, InterruptedException,  IOException;

  /**
   *Deletes the member associated with this object from the file.  The object cannot
   *be open when invoking this method.<br>
   *The name of the file and the AS400 system to which to connect must be set prior
   *to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public void deleteMember()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    AS400Message[] msgs = execute("QSYS/RMVM FILE(" + library_ + "/" +  //@B0C
                                        file_ + ") MBR(" + member_ + ")");
    if (msgs.length > 0 && msgs[0].getID() != null)
    {
      if (!msgs[0].getID().equals("CPC7309"))
      {
        throw new AS400Exception(msgs);
      }
    }
    else
    {
      throw new InternalErrorException("No AS/400 messages",
                                       InternalErrorException.UNKNOWN);
    }
  }


  //@D1A
  /**
   * Turns on reply discarding for the data streams.
   * This is only used when our public object is being finalized.
  **/
  public void discardReplies()
  {
    discardReplys_ = true;
  }


  /**
   *Ends commitment control for this connection.
   *If commitment control has not been started for the connection, no action
   *is taken.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void endCommitmentControl()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    endCommitmentControl(system_); //@E2A
/*@E2D    if (isCommitmentControlStarted())
    {
      AS400Message[] msgs = execute("QSYS/ENDCMTCTL"); //@B0C
      if (msgs.length > 0 && msgs[0].getID() != null &&
          !msgs[0].getID().equals("CPI8351"))
      {
        throw new AS400Exception(msgs);
      }

      // Remove the current system from the list of commitment control
      // systems.
      commitmentControlSystems_.removeElement(system_);
//      server_.commitmentControlStarted_ = false; //@B0A
    }
*///@E2D
  }


  //@E2A
  // This method should be static, but we can't make a static call to
  // a native method (the call to execute()) so we just set all of the
  // necessary state variables and act like we're a normal object.
  public void endCommitmentControl(AS400Impl system)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    synchronized(commitmentControlSystems_)
    {
      if ((isNative_ && nativeCommitmentControlStarted_) || // native case
          commitmentControlSystems_.contains(system))       // remote case
      {
        // Setup state variables
        setSystem(system);
        setConverter();

        // End commitment control
        AS400Message[] msgs = execute("QSYS/ENDCMTCTL");
        if (msgs.length > 0 && msgs[0].getID() != null &&
            !msgs[0].getID().equals("CPI8351"))
        {
          throw new AS400Exception(msgs);
        }
        if (isNative_)
        {
          nativeCommitmentControlStarted_ = false;
        }
        else
        {
          commitmentControlSystems_.removeElement(system_);
        }
      }
    }
  }


  /**
   *Starts commitment control on this file (for this connection).  If commitment control
   *has already been started for the connection, an exception is thrown.
   *@param commitLockLevel The type of commitment control
   *                  to exercise.  Valid values are:
   *                  <ul>
   *                  <li>COMMIT_LOCK_LEVEL_ALL
   *                  <li>COMMIT_LOCK_LEVEL_CHANGE
   *                  <li>COMMIT_LOCK_LEVEL_CURSOR_STABILITY
   *                  </ul>
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public void startCommitmentControl(int commitLockLevel)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    startCommitmentControl(system_, commitLockLevel); //@E2A
/*@E2D
    // Build the command to start commitment control.
    StringBuffer cmd = new StringBuffer("QSYS/STRCMTCTL LCKLVL(*");
    switch(commitLockLevel)
    {
    case AS400File.COMMIT_LOCK_LEVEL_CHANGE:
      cmd.append("CHG)");
      break;
    case AS400File.COMMIT_LOCK_LEVEL_CURSOR_STABILITY:
      cmd.append("CS)");
      break;
    case AS400File.COMMIT_LOCK_LEVEL_ALL:
      cmd.append("ALL)");
      break;
    default:
      throw new ExtendedIllegalArgumentException("commitLockLevel", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Start commitment control.
    AS400Message[] msgs = execute(cmd.toString()); //@B0C
    if (msgs.length > 0 && msgs[0].getID() != null)
    {
      if (!msgs[0].getID().equals("CPI8351"))
      {
        throw new AS400Exception(msgs);
      }
    }

    // Indicate that commitment control is started for the current system.
//    server_.commitmentControlStarted_ = true;
    if (!commitmentControlSystems_.contains(system_))
    {
      commitmentControlSystems_.addElement(system_);
    }
*///@E2D
  }


  //@E2A
  // This method should be static, but we can't make a static call to
  // a native method (the call to execute()) so we just set all of the
  // necessary state variables and act like we're a normal object.
  public void startCommitmentControl(AS400Impl system, int commitLockLevel)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    synchronized(commitmentControlSystems_)
    {
      if ((isNative_ && nativeCommitmentControlStarted_) || // native case
          commitmentControlSystems_.contains(system))       // remote case
      {
        // AS400Impl that was passed in must be natively connected
        // or we would never have been instantiated as an AS400FileImplNative.
        // The AS400's loadImpl() method makes sure of that.
        // We just check a boolean flag in the ImplNative... no need to add
        // it to the Vector, since there should only ever be one "connection"
        // for an ImplNative.
        // If we're an ImplRemote, we just check the Vector like usual.
        throw new ExtendedIllegalStateException(ExtendedIllegalStateException.COMMITMENT_CONTROL_ALREADY_STARTED);
      }

      // Setup state variables
      setSystem(system);
      setConverter();

      // Build the command to start commitment control.
      StringBuffer cmd = new StringBuffer("QSYS/STRCMTCTL LCKLVL(*");
      switch(commitLockLevel)
      {
        case AS400File.COMMIT_LOCK_LEVEL_CHANGE:
          cmd.append("CHG)");
          break;
        case AS400File.COMMIT_LOCK_LEVEL_CURSOR_STABILITY:
          cmd.append("CS)");
          break;
        case AS400File.COMMIT_LOCK_LEVEL_ALL:
          cmd.append("ALL)");
          break;
        default:
          throw new ExtendedIllegalArgumentException("commitLockLevel", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }

      // Start commitment control.
      AS400Message[] msgs = execute(cmd.toString());
      if (msgs.length > 0 && msgs[0].getID() != null)
      {
        if (!msgs[0].getID().equals("CPI8351"))
        {
          throw new AS400Exception(msgs);
        }
      }

      // Indicate that commitment control is started for the current system.
      if (isNative_)
      {
        nativeCommitmentControlStarted_ = true;
      }
      else if (!commitmentControlSystems_.contains(system_))
      {
        commitmentControlSystems_.addElement(system_);
      }
    }
  }

  /**
   *Resets the state instance variables of this object to the appropriate
   *values for the file being closed.  This method is used to reset the
   *the state of the object when the connection has been ended abruptly.
   **/
  public synchronized void resetState()
  {
    // Shut down any caching
    cacheRecords_ = false;
    cache_.setIsEmpty();
    // Reset the blocking factor
    blockingFactor_ = 0;
    // Reset the open type. -1 indicates that the file is not open
    openType_ = -1;
    // Reset the commit lock level; as this method is only called if the
    // connection was ended abruptly, there is no server, so there is no
    // need to check if commitment control has been started.
    commitLockLevel_ = -1;
  }


  /**
   *Releases all locks acquired via the lock() method.  If no locks have been
   *explicitly obtained, no action is taken.
   *@see AS400File#lock
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public void releaseExplicitLocks()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (explicitLocksObtained_.size() > 0)
    {
      // Build the command to release the explicit locks.
      StringBuffer cmd = new StringBuffer("QSYS/DLCOBJ OBJ(");
      for (Enumeration e = explicitLocksObtained_.elements();
           e.hasMoreElements();)
      {
        cmd.append("(");
        cmd.append(library_);
        cmd.append("/");
        cmd.append(file_);
        cmd.append(" *FILE ");
        switch(((Integer) e.nextElement()).intValue())
        {
        case AS400File.READ_ALLOW_SHARED_READ_LOCK:
          cmd.append("*SHRNUP ");
          break;
        case AS400File.READ_ALLOW_SHARED_WRITE_LOCK:
          cmd.append("*SHRRD ");
          break;
        case AS400File.READ_EXCLUSIVE_LOCK:
        case AS400File.WRITE_EXCLUSIVE_LOCK:
          cmd.append("*EXCL ");
          break;
        case AS400File.WRITE_ALLOW_SHARED_READ_LOCK:
          cmd.append("*EXCLRD ");
          break;
        case AS400File.WRITE_ALLOW_SHARED_WRITE_LOCK:
          cmd.append("*SHRUPD ");
          break;
        default:
          throw new InternalErrorException(InternalErrorException.UNKNOWN);
        }
        cmd.append(member_);
        cmd.append(") ");
      }
      cmd.append(")");

      // Execute the command.
      AS400Message[] msgs = execute(cmd.toString()); //@B0C
      if (msgs.length > 0 && msgs[0].getID() != null)
      {
        throw new AS400Exception(msgs);
      }

      // Clear the list of explicit locks.
      explicitLocksObtained_.removeAllElements();
    }
  }


  /**
   *Executes a command on the AS/400.
   *@param cmd the command
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public abstract AS400Message[] execute(String cmd)
    throws AS400SecurityException, InterruptedException, IOException;


  /**
   *Returns the commit lock level for this file as specified on open.
   *@return The commit lock level for this file.
   *If commitment control has not been started for the connection or if
   *file has not been opened, -1 is returned.
   *Possible return values are:
   *                       <ul>
   *                       <li>COMMIT_LOCK_LEVEL_ALL
   *                       <li>COMMIT_LOCK_LEVEL_CHANGE
   *                       <li>COMMIT_LOCK_LEVEL_CURSOR_STABILITY
   *                       <li>COMMIT_LOCK_LEVEL_DEFAULT
   *                       <li>COMMIT_LOCK_LEVEL_NONE
   *                       <li>-1
   *                       </ul>
   **/
  public int getCommitLockLevel()
  {
    return (isCommitmentControlStarted() ? commitLockLevel_ : -1);
  }


  /**
   *Indicates if commitment control is started for
   *the connection.
   *@return true if commitment control has been started; false otherwise.
   **/
  public boolean isCommitmentControlStarted()
  {
    if (isNative_)                            //@E2A
      return nativeCommitmentControlStarted_; //@E2A
    else                                      //@E2A
      return commitmentControlSystems_.contains(system_);
//    return (server_ != null ? server_.commitmentControlStarted_ : false); //@B0A
  }



  //@E2A
  // This method should be static, but we can't make a static call to
  // a native method (the call to execute()) so we just set all of the
  // necessary state variables and act like we're a normal object.
  public boolean isCommitmentControlStarted(AS400Impl system)
  {
    if (isNative_)
      return nativeCommitmentControlStarted_;
    else
      return commitmentControlSystems_.contains(system);
  }


  /**
   *Creates a physical file with the specified record length and file type.
   *The record format for this object will be set by this method.  The
   *record format for the file is determined as follows:
   *<ul>
   *<li>If <i>fileType</i> is AS400File.TYPE_DATA,
   *<ul>
   *<li>The format name of the file is the name of the file as specified on the
   *constructor
   *<li>The record format contains one field whose name is the name of the file,
   *whose type is CHARACTER, and whose length is <i>recordLength</i>
   *</ul>
   *<li>If <i>fileType</i> is AS400File.TYPE_SOURCE,
   *<ul>
   *<li>The format name of the file is the name of the file as specified on the
   *constructor
   *<li>The record format contains three fields:
   *<ul>
   *<li>SRCSEQ whose type is ZONED(6, 2)
   *<li>SRCDAT whose type is ZONED(6, 0)
   *<li>SRCDTA whose type is CHARACTER and whose length is
   *<i>recordLength</i> - 12
   *</ul>
   *</ul>
   *</ul>
   *<b>Note:</b> The file is created using the default values for the AS/400
   * Create Physical File command (CRTPF).
   * Use the <a href="CommandCall.html">CommandCall</a> class to issue a CHGPF
   * command  to change the file after it
   *has been created.<br>
   *The name of the file and the AS400 system to which to connect must be set prior
   *to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@param recordLength The record length with which to create the file.  Valid values
   *                    are 1 through 32766 inclusive.
   *@param fileType The file type with which to create the file.  Valid values are
   *                AS400File.TYPE_DATA or AS400File.TYPE_SOURCE.  If AS400File.TYPE_DATA is specified, the record
   *                format for the file contains one field.  If AS400File.TYPE_SOURCE is
   *                specified, the record format for the file contains three
   *                fields: source sequence number, date, and source statement.
   *@param textDescription The text description with which to create the file.
   *This value must be between 1 and 50 characters inclusive.
   *If this value is null, the empty string, or AS400File.BLANK,
   *the text description is blank.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public void create(int recordLength, String fileType, String textDescription)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Create the CRTPF command string
    StringBuffer cmd = new StringBuffer("QSYS/CRTPF FILE(");
    cmd.append(library_);
    cmd.append("/");
    cmd.append(file_);
    cmd.append(") RCDLEN(");
    cmd.append(recordLength);
    cmd.append(") MBR(");
    cmd.append(member_);
    cmd.append(") TEXT(");
    // Determine correct text description
    if (textDescription == null)
    {
      // Specify *BLANK for text on the command
      cmd.append(AS400File.BLANK); //@B3C
    }
    else if (textDescription.length() == 0 || textDescription.equalsIgnoreCase(AS400File.BLANK)) //@B3C
    {
      // Specify *BLANK for text on the command
      cmd.append(AS400File.BLANK); //@B3C
    }
    else
    {
      // Enclose the text description in single quotes for the command
      cmd.append("'");
      cmd.append(textDescription);
      cmd.append("'");
    }
    cmd.append(") FILETYPE(");
    cmd.append(fileType);
    cmd.append(")");

    // Create the file.
    AS400Message[] msgs = execute(cmd.toString()); //@B0C
    if (!(msgs.length > 0 && msgs[0].getID().equals("CPC7301")))
    {
      throw new AS400Exception(msgs);
    }
  }

  /**
   *Creates a physical file using the specified DDS source file.
   *<b>Note:</b> The file is created using the default values for AS/400
   * Create Physical File (CRTPF) command.
   *Use the <a href="CommandCall.html">CommandCall</a> class to issue a CHGPF to change the file after it
   *has been created.<br>
   *The name of the file and the AS400 system to which to connect must be set prior
   *to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@param ddsSourceFile The integrated file system pathname of the file containing the
   *DDS source for the file being created.
   *@param textDescription The text description with which to create the file.
   *This value must be between 1 and 50 characters inclusive.
   *If this value is null, the empty string, or AS400File.BLANK,
   *the text description will be blank.
   *Specify AS400File.SOURCE_MEMBER_TEXT for the text description if the text
   *description from <i>ddsSourceFile</i> is to be used.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public void create(String ddsSourceFile,
              String textDescription)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Parse the ddsDourceFile into library, file and member
    QSYSObjectPathName ifs = new QSYSObjectPathName(ddsSourceFile);
    if (!(ifs.getObjectType().equals("FILE") || ifs.getObjectType().equals("MBR")))
    { // Invalid QSYSObjectPathName
      throw new IllegalPathNameException(ddsSourceFile,  IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
    }

    // Create the CRTPF command string
    StringBuffer cmd = new StringBuffer("QSYS/CRTPF FILE(");
    cmd.append(library_);
    cmd.append("/");
    cmd.append(file_);
    cmd.append(") SRCFILE(");
    cmd.append(ifs.getLibraryName());
    cmd.append("/");
    cmd.append(ifs.getObjectName());
    cmd.append(") SRCMBR(");
    cmd.append(ifs.getMemberName());
    cmd.append(") MBR(");
    cmd.append(member_);
    cmd.append(") TEXT(");
    // Determine correct text description
    if (textDescription == null)
    {
      // Specify *BLANK for text on the command
      cmd.append(AS400File.BLANK); //@B3C
      cmd.append(")"); //@B3A
    }
    else if (textDescription.length() == 0 || textDescription.equalsIgnoreCase(AS400File.BLANK)) //@A3C
    {
      // Specify *BLANK for text on the command
      cmd.append(AS400File.BLANK); //@B3C
      cmd.append(")"); //@B3A
    }
    else if (textDescription.equalsIgnoreCase(AS400File.SOURCE_MEMBER_TEXT)) //@B3C
    {
      cmd.append(textDescription);
      cmd.append(")");
    }
    else
    {
      // Enclose the text description in single quotes for the command
      cmd.append("'");
      cmd.append(textDescription);
      cmd.append("')");
    }
    // Create the file.
    AS400Message[] msgs = execute(cmd.toString()); //@B0C
    if (!(msgs.length > 0 && msgs[0].getID().equals("CPC7301")))
    {
      throw new AS400Exception(msgs);
    }
  }


  /**
   *Creates the DDS source file to be used to create a physical file based on a user
   *supplied RecordFormat.<br>
   *The name of the file and the AS400 system to which to connect must be set prior
   *to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@param recordFormat The record format to describe in the DDS source file.
   *@param altSeq The value to be specified for the file-level keyword ALTSEQ.  If no
   *value is to be specified, null may be specified.
   *@param ccsid The value to be specified for the file-level keyword CCSID.  If no
   *value is to be specified, null may be specified.
   *@param order The value to be specified to indicate in which order records are to be
   *retrieved from the file.  Valid values are one of the following file-level keywords:
   *<ul>
   *<li>FIFO - First in, first out
   *<li>LIFO - Last in, first out
   *<li>FCFO - First changed, first out
   *</ul>
   *If no ordering value is to be specified, null may be specified.
   *@param ref The value to be specified for the file-level keyword REF.  If no
   *value is to be specified, null may be specified.
   *@param unique Indicates if the file-level keyword UNIQUE is to be specified. True
   *indicates that the UNIQUE keyword should be specified; false indicates that it
   *should not be specified.
   *@param format The value to be specified for the record-level keyword FORMAT.  If no
   *value is to be specified, null may be specified.
   *@param text The value to be specified for the record-level keyword TEXT.  If no
   *value is to be specified, null may be specified.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public abstract void createDDSSourceFile(RecordFormat recordFormat, //@D0C 7/15/99 @E1C
                                                String altSeq,
                                                String ccsid,
                                                String order,
                                                String ref,
                                                boolean unique,
                                                String format,
                                                String text)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException;
  //@D0 7/15/99 - made abstract and moved code into ImplRemote


  /**
   *Deletes the file.  The object cannot be open when calling this method.  The file
   *and all its members will be deleted.
   *Use deleteMember() to delete only the member associated with this object.<br>
   *The name of the file and the AS400 system to which to connect must be set prior
   *to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@see AS400File#deleteMember
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public void delete()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    AS400Message[] msgs = execute("QSYS/DLTF FILE(" + library_ + "/" +  //@B0C
                                        file_ + ")");
    if (msgs.length > 0)
    {
      if (msgs[0].getID() != null && !msgs[0].getID().equals("CPC2191"))
      {
        throw new AS400Exception(msgs);
      }
    }
    else
    {
      throw new InternalErrorException("No AS/400 messages",
                                       InternalErrorException.UNKNOWN);
    }
  }



  /**
   *Obtains a lock on the file.
   *The name of the file and the AS400 system to which to connect must be set prior
   *to invoking this method.
   *@see AS400File#AS400File(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400File#setPath
   *@see AS400File#setSystem
   *@param lockToObtain The type of lock to acquire on the file.
   *                     Valid lock values are:
   *                     <ul>
   *                     <li>READ_EXCLUSIVE_LOCK
   *                     <li>READ_ALLOW_SHARED_READ_LOCK
   *                     <li>READ_ALLOW_SHARED_WRITE_LOCK
   *                     <li>WRITE_EXCLUSIVE_LOCK
   *                     <li>WRITE_ALLOW_SHARED_READ_LOCK
   *                     <li>WRITE_ALLOW_SHARED_WRITE_LOCK
   *                     </ul>
   *If <i>lockToObtain</i> has already been obtained, no action is taken.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public void lock(int lockToObtain)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    Integer l = new Integer(lockToObtain);
    if (!explicitLocksObtained_.contains(l))
    {
      // Lock the file using the ALCOBJ command.
      StringBuffer cmd = new StringBuffer("QSYS/ALCOBJ OBJ((");
      cmd.append(library_);
      cmd.append("/");
      cmd.append(file_);
      cmd.append(" *FILE ");
      switch(lockToObtain)
      {
      case AS400File.READ_ALLOW_SHARED_READ_LOCK:
        cmd.append("*SHRNUP ");
        break;
      case AS400File.READ_ALLOW_SHARED_WRITE_LOCK:
        cmd.append("*SHRRD ");
        break;
      case AS400File.READ_EXCLUSIVE_LOCK:
      case AS400File.WRITE_EXCLUSIVE_LOCK:
        cmd.append("*EXCL ");
        break;
      case AS400File.WRITE_ALLOW_SHARED_READ_LOCK:
        cmd.append("*EXCLRD ");
        break;
      case AS400File.WRITE_ALLOW_SHARED_WRITE_LOCK:
        cmd.append("*SHRUPD ");
        break;
      default:
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }
      cmd.append(member_);
      cmd.append("))");
      AS400Message[] msgs = execute(cmd.toString()); //@B0C
      if (msgs.length > 0 && msgs[0].getID() != null)
      {
        throw new AS400Exception(msgs);
      }

      // Indicate which lock has been obtained
      explicitLocksObtained_.addElement(l);
    }
  }


  /**
   *Opens the file.  Helper function to open file for keyed or
   *sequential files.
   *@param openType
   *@param bf blocking factor
   *@param access The type of file access for which to open the file.
   *@return the open feedback data
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public abstract DDMS38OpenFeedback openFile(int openType, int bf, String access)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException;


  //@B0A - need this for compatibility with the remote and native impls
  public String[] openFile2(int openType, int blockingFactor, int commitLockLevel, String access)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    return openFile2(openType, blockingFactor, commitLockLevel, access.equals("key"));
  }

  public String[] openFile2(int openType, int blockingFactor, int commitLockLevel, boolean access) //@B0C
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    commitLockLevel_ = (isCommitmentControlStarted() ? commitLockLevel : -1);

/*@D0M    // Set the blocking factor for the file
    if (openType != AS400File.READ_WRITE)
    {
      // Calculate the blocking factor if 0 was specified
      if (blockingFactor == 0)
      {
        blockingFactor_ = calculateBlockingFactor();
      }
      else
      {
        blockingFactor_ = blockingFactor;
      }

      // Estimate the record increment.
      int recordIncrement = recordFormat_.getNewRecord().getRecordLength() +
        recordFormat_.getNumberOfFields() +
        recordFormat_.getNumberOfKeyFields() + 16;

      // We can only retrieve 16Mb of record data per GET so limit the
      // blocking factor appropriately.
      blockingFactor_ = (blockingFactor_ * recordIncrement >= 16777216 ?
                         16777216 / recordIncrement : blockingFactor_);
    }
    else
    { // For open type of READ_WRITE or if the file is a KeyedFile, blocking
      // factor is set to 1 for data integrity
      // reasons (read_write implies we are reading and updating and therefore
      // want up-to-date data.
      blockingFactor_ = 1;
    }
*///@D0M
    blockingFactor_ = blockingFactor; //@D0A

    // Determine if we are to cache records.
    cacheRecords_ = (blockingFactor_ > 1);


    // @A5A
    // Set the record format CTLL name
    if (!recordFormatName_.equals(recordFormat_.getName())) {
        recordFormatName_ = recordFormat_.getName();
        StringBuffer recordName = new StringBuffer(recordFormat_.getName());
        while (recordName.length() < 10) recordName.append(' ');
        ConverterImplRemote c = ConverterImplRemote.getConverter(system_.getCcsid(), system_); //@B5C
        recordFormatCTLLName_ = c.stringToByteArray(recordName.toString());
    }


    // Open the file.
    DDMS38OpenFeedback openFeedback = openFile(openType, blockingFactor_, access ? "key" : "seq"); //@B0C - access is true for KeyedFiles

    // Set the open type
    openType_ = openType;

    String[] toReturn = new String[2]; //@B0A

    // If a special value was specified for library or member, set the actual name
    // now.  Note that the AS400 returns the names blank padded to ten characters
    // so we trim off any blanks.
    if (library_.charAt(0) == '*')
    {
      library_ = openFeedback.getLibraryName().trim();
      toReturn[0] = library_; //@B0A
    }
    if (member_.equalsIgnoreCase("*FIRST") || member_.equalsIgnoreCase("*LAST"))
    {
      member_ = openFeedback.getMemberName().trim();
      toReturn[1] = member_; //@B0A
    }
    return toReturn;
  }


  /**
   *Positions the file cursor to the first record whose record number
   *matches the specified record number.  The file must be open when invoking
   *this method.
   *@param recordNumber The record number of the record at which to position the
   *cursor.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursor(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // If caching, check if position is in the cache.  If it is not, refresh the
    // the cache and position appropriately.
    if (cacheRecords_)
    {
      if (!cache_.setPosition(recordNumber))
      {
        positionCursorToIndex(recordNumber);
      }
    }
    else
    { // Not caching
      positionCursorToIndex(recordNumber);
    }
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
    positionCursorToKey(key, searchType);
    // Invalidate the cache
    if (cacheRecords_)
    {
      cache_.setIsEmpty();
    }
  }


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
    positionCursorToKey(key, searchType, numberOfKeyFields);
    // Invalidate the cache
    if (cacheRecords_)
    {
      cache_.setIsEmpty();
    }
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
    // Position the cursor to the record matching key
    positionCursorToKey(key, KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ]);
    if (cacheRecords_)
    { // Invalidate the cache. This will cause it to be refreshed
      // if we are caching and allow for cache access for any
      // subsequent (to this method's invocation) sequential
      // positioning or reading.
      cache_.setIsEmpty();
    }
    positionCursorToNext();
  }


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
    // Position the cursor to the record matching key
    positionCursorToKey(key, KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ], numberOfKeyFields);
    if (cacheRecords_)
    { // Invalidate the cache. This will cause it to be refreshed
      // if we are caching and allow for cache access for any
      // subsequent (to this method's invocation) sequential
      // positioning or reading.
      cache_.setIsEmpty();
    }
    positionCursorToNext();
  }


   /**
    *Positions the file cursor to after the last record.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract void positionCursorAfterLast()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;


   /**
    *Positions the file cursor to the specified position (first, last, next,
    *previous).
    *@param type the type of position operation
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract Record[] positionCursorAt(int type)
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;


  /**
   *Positions the file cursor to the first record before the record specified
   *by the record number.  The file must be open when invoking
   *this method.
   *@param recordNumber The record number of the record before which to position
   *           the cursor.  The <i>recordNumber</i> must be greater than zero.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public void positionCursorBefore(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (cacheRecords_)
    {
      if (cache_.setPosition(recordNumber))
      {
        if (!cache_.isBeginningOfCache())
        {
          cache_.setPositionPrevious();
        }
        else
        {  // Record not in cache; position and refresh the cache
          positionCursorToIndex(recordNumber);
          positionCursorToPrevious();
        }
      }
      else
      { // Not in cache; position and refresh the cache
        positionCursorToIndex(recordNumber);
        positionCursorToPrevious();
      }
    }
    else
    { // Not caching
      positionCursorToIndex(recordNumber);
      positionCursorToPrevious();
    }
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
    // Position the cursor to the record matching key
    positionCursorToKey(key, KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ]);
    if (cacheRecords_)
    { // Invalidate the cache. This will cause it to be refreshed
      // if we are caching and allow for cache access for any
      // subsequent (to this method's invocation) sequential
      // positioning or reading.
      cache_.setIsEmpty();
    }
    // Call super's positionCursorToPrevious to get to the record
    // immediately following the matching record
    positionCursorToPrevious();
  }


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
    // Position the cursor to the record matching key
    positionCursorToKey(key, KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ], numberOfKeyFields);
    if (cacheRecords_)
    { // Invalidate the cache. This will cause it to be refreshed
      // if we are caching and allow for cache access for any
      // subsequent (to this method's invocation) sequential
      // positioning or reading.
      cache_.setIsEmpty();
    }
    // Call super's positionCursorToPrevious to get to the record
    // immediately following the matching record
    positionCursorToPrevious();
  }


   /**
    *Positions the file cursor to before the first record.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract void positionCursorBeforeFirst()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;


   /**
    *Positions the cursor to the first record.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public void positionCursorToFirst()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
   {
     // If we are caching records and the cache contains the first record,
     // position the cache.  Otherwise, position the file and refresh the
     // cache if we are caching records.
     if (cacheRecords_ && cache_.containsFirstRecord())
     {
       cache_.setPositionFirst();
     }
     else
     {
       if (cacheRecords_)
       {
         cache_.setIsEmpty();
       }
       positionCursorAt(TYPE_GET_FIRST);
     }
   }


   /**
    *Positions the cursor to the record at the specified file position.
    *@parm index the file position
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract Record positionCursorToIndex(int index)
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;

   /**
    *Positions the cursor to the first record in the file that matches the
    *specified key.
    *@param key the key
    *@param searchType the way to compare keys
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract Record positionCursorToKey(Object[] key,
                                       int searchType)
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;


   // @A2A
   /**
    *Positions the cursor to the first record in the file that matches the
    *specified key.
    *@param key the byte array that contains the byte values of the key
    *@param searchType the way to compare keys
    *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract Record positionCursorToKey(byte[] key,
                                       int searchType, int numberOfKeyFields)
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;



   /**
    *Positions the cursor to the last record in the file.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public void positionCursorToLast()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
   {
     // If we are caching records and the cache contains the first record,
     // position the cache.  Otherwise, position the file and refresh the
     // cache if we are caching records.
     if (cacheRecords_ && cache_.containsLastRecord())
     {
       cache_.setPositionLast();
     }
     else
     {
       if (cacheRecords_)
       {
         cache_.setIsEmpty();
       }
       positionCursorAt(TYPE_GET_LAST);
     }
   }

   /**
    *Positions the cursor to the next record in the file.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public void positionCursorToNext()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
   {
     // Check if we are caching.  If we are and we are not at the end of the cache,
     // just position ourselves within the cache, otherwise position ourselves in the file
     // and refresh the cache.
     if (cacheRecords_)
     {
       if (cache_.isEmpty() || (cache_.isEndOfCache() && cache_.currentDirection_ == DDMRecordCache.FORWARD))
       {
         cache_.refresh(positionCursorAt(TYPE_GET_NEXT), DDMRecordCache.FORWARD, false, false);
       }
       else if (cache_.isEndOfCache() && cache_.currentDirection_ != DDMRecordCache.FORWARD)
       {
         refreshCache(null, DDMRecordCache.FORWARD, false, false);
       }
       else
       {
         if (Trace.isTraceOn())
         {
           Trace.log(Trace.INFORMATION, "positionCursorToNext: positioning in cache.");
         }
         cache_.setPositionNext();
       }
     }
     else
     { // Not caching; just position ourselves in the file
       positionCursorAt(TYPE_GET_NEXT);
     }
   }

   /**
    *Positions the cursor to the previous record in the file.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public void positionCursorToPrevious()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
   {
     // Check if we are caching.  If we are and we are not at the end of the cache,
     // just position ourselves within the cache, otherwise position ourselves in the file
     // and refresh the cache.
     if (cacheRecords_)
     {
       if (cache_.isEmpty() || (cache_.isBeginningOfCache() && cache_.currentDirection_ == DDMRecordCache.BACKWARD))
       {
         cache_.refresh(positionCursorAt(TYPE_GET_PREV), DDMRecordCache.BACKWARD, false, false);
       }
       else if (cache_.isBeginningOfCache() && cache_.currentDirection_ != DDMRecordCache.BACKWARD)
       {
         refreshCache(null, DDMRecordCache.BACKWARD, false, false);
       }
       else
       {
         if (Trace.isTraceOn())
         {
           Trace.log(Trace.INFORMATION, "positionCursorToPrev: positioning in cache.");
         }
         cache_.setPositionPrevious();
       }
     }
     else
     { // Not caching; just position ourselves in the file
       positionCursorAt(TYPE_GET_PREV);
     }
   }

   /**
    Reads the record at the current file position.
    @return the record read.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public Record read()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
   {
     // If we are caching, get the current record at which we are pointing.  If we are
     // not positioned on record in the cache we are not positioned on a record in the
     // file either.  In order to get the correct error information thrown, we call
     // readRecord() to read from the file and throw the exception.
     Record r = null;
     if (cacheRecords_)
     {
       r = cache_.getCurrent();
     }
     if (r == null)
     {
       r = readRecord(TYPE_GET_SAME);
     }

     return r;
   }

   /**
    Reads the record at the specified file position.
    @param recordNumber the file position
    @return the record read.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
  public Record read(int recordNumber) //@B0C -- this is overridden in the subclasses
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
//@B0D    if (cacheRecords_)
//@B0D    {
      Record r = cache_.getRecord(recordNumber);
      if (r == null)
      { // Record is not in cache; read it from the file and refresh
        try
        {
          // Position to the record and refresh the cache
          r = positionCursorToIndex(recordNumber);
          refreshCache(null, DDMRecordCache.FORWARD, false, false);
          // Add the current record to the cache at the beginning
          cache_.add(r, false);
          cache_.setPositionFirst();
        }
        catch(AS400Exception e)
        { // If we get CPF5001 or CPF5006 (end of file or record not found)
          // from the position operation we want to return null in order
          // to mimic what we do when we are not caching.
          String id = e.getAS400Message().getID();
          if (id.equals("CPF5001") || id.equals("CPF5006"))
          {
            return null;
          }
          else
          {
            throw e;
          }
        }
      }
      return r;
//@B0D    }
//@B0D    else
//@B0D    {
//@B0D      return read(recordNumber);
//@B0D    }
  }

   /**
    *Reads the first record with the specified key based on the specified search type.
    *@param key The values that make up the key with which to find the record.
    *@param type The type of read.  This value is one of the TYPE_GETKEY_* constants.
    *@return The record read.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract Record read(Object[] key,
                        int searchType)
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;


   // @A2A
   /**
    *Reads the first record with the specified key based on the specified search type.
    *@param key The byte array that contains the byte values that make up the key with which to find the record.
    *@param type The type of read.  This value is one of the TYPE_GETKEY_* constants.
    *@param numberOfKeyFields The number of key fields contained in the byte array <i>key</i>.
    *@return The record read.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract Record read(byte[] key,
                        int searchType, int numberOfKeyFields)
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;


/*  Record read2(byte[] key,
                        int searchType, int numberOfKeyFields)
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
  {
    // Determine the type of get
    Record r = read(key, KeyedFile.TYPE_TABLE[searchType], numberOfKeyFields);
    if (cacheRecords_)
    {
      cache_.setIsEmpty();
    }
    return r;
  }
*/

  /**
   *Reads the first record after the record with the specified record number.
   *The file must be open when invoking this method.
   *@param recordNumber record number of the record prior to the record to be read.
   *The <i>recordNumber</i> must be greater than zero.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readAfter(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (cacheRecords_)
    {
      // Check for record in the cache
      if (cache_.setPosition(recordNumber))
      { // Record recordNumber is here; are we at the end of the cache?
        if (!cache_.isEndOfCache())
        { // Next record in cache is the one we want
          return cache_.getNext();
        }
        else
        { // We need to go to the file to get the record
          // Don't need to bother to invalidate the cache as we are at
          // the end of the cache and readNext will check that
          // and refresh.
          positionCursorToIndex(recordNumber);
          return readNext();
        }
      }
      else
      { // We need to go to the file to get the record
        // Invalidate the cache.
        cache_.setIsEmpty();
        positionCursorToIndex(recordNumber);
        return readNext();
      }
    }
    else
    { // Position the cursor and call super's readNext()
      positionCursorToIndex(recordNumber);
      return readNext();
    }
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
    positionCursorToKey(key, KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ]);
    if (cacheRecords_)
    {
      cache_.setIsEmpty();
    }
    return readNext();
  }


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
    positionCursorToKey(key, KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ], numberOfKeyFields);
    if (cacheRecords_)
    {
      cache_.setIsEmpty();
    }
    return readNext();
  }


   /**
    *Reads all the records in the file.
    *@param fileType The type of file.  Valid values are: key or seq
    *@return The records read.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract Record[] readAll(String fileType, int bf) //@D0C
     throws AS400Exception, AS400SecurityException, InterruptedException, IOException;

  /**
   *Reads the first record before the record with the specified record number.
   *The file must be open when invoking this method.
   *@param recordNumber The record number of the record after the record to be read.
   *The <i>recordNumber</i> must be greater than zero.
   *@return The record read.  If the record is not found, null is returned.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/
  public Record readBefore(int recordNumber)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (cacheRecords_)
    {
      // Check for record in the cache
      if (cache_.setPosition(recordNumber))
      { // Record recordNumber is here; are we at the beginning of the cache?
        if (!cache_.isBeginningOfCache())
        { // Next record in cache is the one we want
          return cache_.getPrevious();
        }
        else
        { // We need to go to the file to get the record
          // Don't need to bother to invalidate the cache as we are at
          // the beginning of the cache and readPrevious will check that
          // and refresh.
          positionCursorToIndex(recordNumber);
          return readPrevious();
        }
      }
      else
      { // We need to go to the file to get the record
        // Invalidate the cache.
        cache_.setIsEmpty();
        positionCursorToIndex(recordNumber);
        return readPrevious();
      }
    }
    else
    { // Position the cursor and call super's readPrevious()
      positionCursorToIndex(recordNumber);
      return readPrevious();
    }
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
    Record r = positionCursorToKey(key,  KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ]);
    if (cacheRecords_)
    {
      cache_.setIsEmpty();
    }
    return readPrevious();
  }


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
    Record r = positionCursorToKey(key,  KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ], numberOfKeyFields);
    if (cacheRecords_)
    {
      cache_.setIsEmpty();
    }
    return readPrevious();
  }


   /**
    *Reads the first record from the file.
    *@return the first record.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
    public Record readFirst()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
      // If we are caching, check the cache for the record.  IF not found refresh the
      // cache.
      if (cacheRecords_)
      {
        if (cache_.containsFirstRecord())
        {
          return cache_.getFirst();
        }
        else //@A4A: Invalidate the cache since we will be going to the system
        {
          // Invalidate the cache
          cache_.setIsEmpty();
        }
      }

      // Not caching, read from the file.
      return readRecord(TYPE_GET_FIRST);
    }

   /**
    *Reads the last record from the file.
    *@return the first record.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
    public Record readLast()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
      // If we are caching, check the cache for the record.  IF not found refresh the
      // cache.
      if (cacheRecords_)
      {
        if (cache_.containsLastRecord())
        {
          return cache_.getLast();
        }
        else //@A4A: Invalidate the cache since we will be going to the system
        {
          // Invalidate the cache
          cache_.setIsEmpty();
        }
      }

      // Not caching, read from the file.
      return readRecord(TYPE_GET_LAST);
    }

   /**
    *Reads the next record from the file.
    *@return the first record.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
    public Record readNext()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
      Record r = null;
      // If we are caching, check the cache for the record.  IF not found refresh the
      // cache.
      if (cacheRecords_)
      {
        r = cache_.getNext();
        if (r == null)
        {
          if (Trace.isTraceOn())
          {
            Trace.log(Trace.INFORMATION, "AS400FileImplBase.readNext(): cache_.getNext() returned null.");
          }
          refreshCache(null, DDMRecordCache.FORWARD, false, false);
          return cache_.getCurrent();
        }
        else
        {
          return r;
        }
      }

      // Not caching, read from the file.
      return readRecord(TYPE_GET_NEXT);
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
    Object[] key;
    if (cacheRecords_)
    {
      if (!cache_.isEmpty())
      {
        Record r = null;
        Record current = cache_.getCurrent();
        if (current != null)
        {
          key = current.getKeyFields();
          try
          {
            //@B4C - when reading, get out early by checking comparison
            r = readNext();
            int match = UNKNOWN;
            if (r != null) match = compareKeys(key, r.getKeyFields());
            while (r != null && (match == GREATER_THAN || match == UNKNOWN))
            {
              r = readNext();
              if (r != null) match = compareKeys(key, r.getKeyFields());
            }
            if (match != EQUAL) r = null;
            //@B4C - end change
          }
          catch(AS400Exception e)
          {
            if (e.getAS400Message().getID().equals("CPF5025"))
            {
              return null;
            }
            else
            {
              throw e;
            }
          }
          return r;
        }
      }
    }
    key = recordFormat_.getNewRecord().getKeyFields();
    return read(key, 0x0E);
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
    Record r = null;
    try
    {
      //@B4C - when reading, get out early by checking comparison
      r = readNext();
      int match = UNKNOWN;
      if (r != null) match = compareKeys(key, r.getKeyFields());
      while (r != null && (match == GREATER_THAN || match == UNKNOWN))
      {
        r = readNext();
        if (r != null) match = compareKeys(key, r.getKeyFields());
      }
      if (match != EQUAL) r = null;
      //@B4C - end change
    }
    catch(AS400Exception e)
    {
      if (e.getAS400Message().getID().equals("CPF5025"))
      {
        return null;
      }
      else
      {
        throw e;
      }
    }
    return r;
  }


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
    Record r = null;
    try
    {
            //@B4C - when reading, get out early by checking comparison
            r = readNext();
            int match = UNKNOWN;
            if (r != null) match = compareKeys(key, r.getKeyFieldsAsBytes(), numberOfKeyFields);
            while (r != null && (match == GREATER_THAN || match == UNKNOWN))
            {
              r = readNext();
              if (r != null) match = compareKeys(key, r.getKeyFieldsAsBytes(), numberOfKeyFields);
            }
            if (match != EQUAL) r = null;
            //@B4C - end change
    }
    catch(AS400Exception e)
    {
      if (e.getAS400Message().getID().equals("CPF5025"))
      {
        return null;
      }
      else
      {
        throw e;
      }
    }
    return r;
  }


   /**
    *Reads the previous record from the file.
    *@return the first record.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
    public Record readPrevious()
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException
    {
      // If we are caching, check the cache for the record.  IF not found refresh the
      // cache.
      Record r = null;
      if (cacheRecords_)
      {
        r = cache_.getPrevious();
        if (r == null)
        {
          if (Trace.isTraceOn())
          {
            Trace.log(Trace.INFORMATION, "AS400FileImplBase.readPrevious(): cache returned null.");
          }
          refreshCache(null, DDMRecordCache.BACKWARD, false, false);
          return cache_.getCurrent();
        }
        else
        {
          return r;
        }
      }

      // Not caching, read from the file.
      return readRecord(TYPE_GET_PREV);
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
    Object[] key;
    Record r;
    if (cacheRecords_)
    {
      if (!cache_.isEmpty())
      {
        Record current = cache_.getCurrent();
        if (current != null)
        {
          key = current.getKeyFields();
          try
          {
            //@B4C - when reading, get out early by checking comparison
            r = readPrevious();
            int match = UNKNOWN;
            if (r != null) match = compareKeys(key, r.getKeyFields());
            while (r != null && (match == LESS_THAN || match == UNKNOWN))
            {
              r = readPrevious();
              if (r != null) match = compareKeys(key, r.getKeyFields());
            }
            if (match != EQUAL) r = null;
            //@B4C - end change
          }
          catch(AS400Exception e)
          {
            if (e.getAS400Message().getID().equals("CPF5025"))
            {
              return null;
            }
            else
            {
              throw e;
            }
          }
          return r;
        }
      }
    }
    key = recordFormat_.getNewRecord().getKeyFields();
    return read(key, 0x0F);
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
    Record r = null;
    try
    {
            //@B4C - when reading, get out early by checking comparison
            r = readPrevious();
            int match = UNKNOWN;
            if (r != null) match = compareKeys(key, r.getKeyFields());
            while (r != null && (match == LESS_THAN || match == UNKNOWN))
            {
              r = readPrevious();
              if (r != null) match = compareKeys(key, r.getKeyFields());
            }
            if (match != EQUAL) r = null;
            //@B4C - end change
    }
    catch(AS400Exception e)
    {
      if (e.getAS400Message().getID().equals("CPF5025"))
      {
        return null;
      }
      else
      {
        throw e;
      }
    }
    return r;
  }


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
    Record r = null;
    try
    {
            //@B4C - when reading, get out early by checking comparison
            r = readPrevious();
            int match = UNKNOWN;
            if (r != null) match = compareKeys(key, r.getKeyFieldsAsBytes(), numberOfKeyFields);
            while (r != null && (match == LESS_THAN || match == UNKNOWN))
            {
              r = readPrevious();
              if (r != null) match = compareKeys(key, r.getKeyFieldsAsBytes(), numberOfKeyFields);
            }
            if (match != EQUAL) r = null;
            //@B4C - end change
    }
    catch(AS400Exception e)
    {
      if (e.getAS400Message().getID().equals("CPF5025"))
      {
        return null;
      }
      else
      {
        throw e;
      }
    }
    return r;
  }


    /**
     Reads the record at the current file position.
     *@param type type of read (first, last, next, previous)
     @return the record read.
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public abstract Record readRecord(int type)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;


    /**
     *Reads records from the file.  The next or previous 'blockingFactor_'
     *records are retrieved depending on the direction specified.
     *@param direction (DDMRecordCache.FORWARD or DDMRecordCache.BACKWARD)
     *@return the records read
     *@exception AS400Exception If the AS/400 system returns an error message.
     *@exception AS400SecurityException If a security or authority error occurs.
     *@exception InterruptedException If this thread is interrupted.
     *@exception IOException If an error occurs while communicating with the AS/400.
     **/
    public abstract Record[] readRecords(int direction)
      throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;


  /**
   *Refreshes the record cache for this file object.  Depending on the direction
   *specified, a readNext() (direction = FORWARD) or readPrevious()
   *(direction = BACKWARD) will be specified on the S38GETM (get multiple records).
   *@param records The records with which to refresh the cache.  If <i>records</i>
   *is null, the records will be retrieved via impl.readRecords().
   *@param direction The direction in which to search the cache for records.
   *@param containsFirstRecord Indicates if the first record will be contained in the
   *cache after the getm.
   *@param containsLastRecord Indicates if the last record will be contained in the
   *cache after the getm.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void refreshCache(Record[] records, int direction, boolean containsFirstRecord, boolean containsLastRecord)
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // Fill the cache.  If null was returned, the cache will set itself to empty.
    if (Trace.isTraceOn())
    {
      Trace.log(Trace.INFORMATION, "AS400FileImplBase.refreshCache: refreshing cache," + String.valueOf(direction));
    }
    Record r = null;
    if ((cache_.currentDirection_ == direction || cache_.isEmpty()))
    {
      // Invalidate the cache in case an exception occurs
      cache_.setIsEmpty();
      // We refresh if we are going in the same direction
      if (records == null)
      {
        // Read records.  We don't need to create the implementation because
        // this method is only called by code that ensures that this object is
        // already open.
        records = readRecords(direction);
      }
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.INFORMATION, "AS400FileImplBase.refreshCache(): cursors in synch.");
      }
      cache_.refresh(records, direction, containsFirstRecord,
                     containsLastRecord);
    }
    else
    {
      // The host cursor and cache cursor are out of synch - need to re-synch
      // If we are a SequentialFile, we will simply position by record number of
      // the record we are currently at in the cache to correctly position
      // ourselves.  If we are a KeyedFile, we need to position by key to
      // the record whose key matched the key of the record we are currently
      // at in the cache.  Then we compare the record number.  This is in case
      // the file has duplicate keys.  If the record number does not match,
      // we do a readNext() and compare, etc.
      int recordNumber = (cache_.getCurrent() == null)? cache_.getNext().getRecordNumber() : cache_.getCurrent().getRecordNumber();
      if (Trace.isTraceOn())
      {
        Trace.log(Trace.INFORMATION, "AS400FileImplBase.refreshCache(): cursors not in synch.");
      }
      if (!isKeyed_) //@B0C: Must be instance of SequentialFile
      {
        // Invalidate the cache in case an exception occurs
        cache_.setIsEmpty();
        positionCursor(recordNumber);
      }
      else
      {
        Object[] key = (cache_.getCurrent() == null)? cache_.getNext().getKeyFields() : cache_.getCurrent().getKeyFields();
        r = read(key, KeyedFile.TYPE_TABLE[KeyedFile.KEY_EQ]); //@B0C
        // Turn of caching so that we can position in the file
        cacheRecords_ = false;
        // Invalidte the cache in the event of an exception
        cache_.setIsEmpty();
        while (r != null && r.getRecordNumber() != recordNumber)
        {
          try
          {
            r = readNextEqual();
          }
          catch(AS400Exception e)
          {
            cacheRecords_ = true;
            throw e;
          }
          catch(AS400SecurityException e)
          {
            cacheRecords_ = true;
            throw e;
          }
          catch(IOException e)
          {
            cacheRecords_ = true;
            throw e;
          }
          catch(InterruptedException e)
          {
            cacheRecords_ = true;
            throw e;
          }
        }
        cacheRecords_ = true;
      }

      if (records == null)
      {
        // Read records.  We don't need to create the implementation because
        // this method is only called by code that ensures that this object is
        // already open.
        records = readRecords(direction);
      }
      // Now we can refresh the cache
      cache_.refresh(records, direction, containsFirstRecord,
                     containsLastRecord);
    }
  }


  /**
   *Refreshes the record cache for this file.  Invoking this method will cause the
   *retrieval of records from the AS/400.  The cursor position is set to the
   *first record of the file.  This method only needs to
   *be invoked if a blocking factor greater than 1 is being used, and the user
   *wants to refresh the records in the cache.  The file must be open when invoking
   *this method.  No action is taken if records are not being cached (for example, the
   *blocking factor is set to one).
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
  public void refreshRecordCache()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    // If we are caching, refresh the cache with records starting from the beginning
    // of the file.
    if (cacheRecords_)
    {
      positionCursorBeforeFirst();
      // Set cache direction to FORWARD
      cache_.currentDirection_ = DDMRecordCache.FORWARD;
      refreshCache(null, DDMRecordCache.FORWARD, true, false);
    }
  }


   /**
    *Rolls back any transactions since the last commit/rollback boundary.  Invoking this
    *method will cause all transactions under commitment control for this connection
    *to be rolled back.  This means that any AS400File object for which a commit
    *lock level was specified and that was opened under this connection will have
    *outstanding transactions rolled back.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract void rollback()
     throws AS400Exception,  AS400SecurityException, InterruptedException,   IOException;


  //@E2A
  // This method should be static, but we can't make a static call to
  // a native method (the call to execute()) so we just set all of the
  // necessary state variables and act like we're a normal object.
  public void rollback(AS400Impl system)
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    if (isCommitmentControlStarted(system))
    {
      // Setup state variables
      setSystem(system);
      setConverter();

      rollback();
    }
  }


   /**
    *Updates the record at the current cursor position. The cursor must be positioned to an active record.  The
    *last operation on the file must have been a cursor positioning operation or a
    *read operation.  If an attempt is made to update a record more than once without
    *reading the record or positioning the cursor to the record in between updates, an
    *AS400Exception is thrown.  The cursor position is not changed when this method
    *is invoked.
    *@param record The record with which to update.  The record must be a record whose
    *format matches the record format of this object.  To ensure that this
    *requirement is met, use the
    *<a href="RecordFormat.html">RecordFormat.getNewRecord()</a>
    *method to obtain a default record whose fields can be set appropriately by
    *the Java program and then written to the file.
    *@exception AS400Exception If the AS/400 system returns an error message.
    *@exception AS400SecurityException If a security or authority error occurs.
    *@exception InterruptedException If this thread is interrupted.
    *@exception IOException If an error occurs while communicating with the AS/400.
    **/
   public abstract void update(Record record)
     throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;

   /**
   *Writes an array of records to the end of the file.
   *The cursor is positioned to after the last record of the file as a
   *result of invoking this method.
   *@param records The records to write.  The records must have a format
   *which matches the record format of this object.  To ensure that this
   *requirement is met, use the
   *<a href="RecordFormat.html">RecordFormat.getNewRecord()</a>
   *method to obtain default records whose fields can be set appropriately
   *by the Java program and then written to the file.
   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception InterruptedException If this thread is interrupted.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   **/
  public abstract void write(Record[] records)
    throws AS400Exception, AS400SecurityException, InterruptedException,   IOException;

}




