///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ObjectList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.*;
import java.io.*;
import java.util.*;



/**
The ObjectList class represents a list of OS/400 objects in a
specific library, multiple libraries, or system-wide.
<p>
Implementation note:
This class internally uses the Open List APIs (e.g. QGYOLOBJ).

@see com.ibm.as400.access.ObjectDescription
**/
public class ObjectList implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  static final long serialVersionUID = 5L;

  /**
   * Selection value representing *ALL.
  **/
  public static final String ALL = "*ALL";

  /**
   * Selection value representing *ALLUSR.
  **/
  public static final String ALL_USER = "*ALLUSR";

  /**
   * Selection value representing an authority of *ALL.
  **/
  public static final String AUTH_ALL = "*ALL";
  
  /**
   * Selection value representing an authority of *ANY.
  **/
  public static final String AUTH_ANY = "*ANY";

  /**
   * Selection value representing an authority of *CHANGE.
  **/
  public static final String AUTH_CHANGE = "*CHANGE";
  
  /**
   * Selection value representing a data authority of *ADD.
  **/
  public static final String AUTH_DATA_ADD = "*ADD";
  
  /**
   * Selection value representing a data authority of *DLT.
  **/
  public static final String AUTH_DATA_DELETE = "*DLT";
  
  /**
   * Selection value representing a data authority of *EXECUTE.
  **/
  public static final String AUTH_DATA_EXECUTE = "*EXECUTE";
  
  /**
   * Selection value representing a data authority of *READ.
  **/
  public static final String AUTH_DATA_READ = "*READ";
  
  /**
   * Selection value representing a data authority of *UPD.
  **/
  public static final String AUTH_DATA_UPDATE = "*UPD";
  
  /**
   * Selection value representing an authority of *AUTLMGT.
  **/
  public static final String AUTH_LIST_MANAGEMENT = "*AUTLMGT";
  
  /**
   * Selection value representing an object authority of *OBJALTER.
  **/
  public static final String AUTH_OBJECT_ALTER = "*OBJALTER";
  
  /**
   * Selection value representing an object authority of *OBJEXIST.
  **/
  public static final String AUTH_OBJECT_EXISTENCE = "*OBJEXIST";
  
  /**
   * Selection value representing an object authority of *OBJMGT.
  **/
  public static final String AUTH_OBJECT_MANAGEMENT = "*OBJMGT";
  
  /**
   * Selection value representing an object authority of *OBJOPR.
  **/
  public static final String AUTH_OBJECT_OPERATIONAL = "*OBJOPR";
  
  /**
   * Selection value representing an object authority of *OBJREF.
  **/
  public static final String AUTH_OBJECT_REFERENCE = "*OBJREF";
  
  /**
   * Selection value representing an authority of *USE.
  **/
  public static final String AUTH_USE = "*USE";
  
  /**
   * Selection value representing *CURLIB.
  **/
  public static final String CURRENT_LIBRARY = "*CURLIB";

  /**
   * Selection value representing *IBM.
  **/
  public static final String IBM = "*IBM";

  /**
   * Selection value representing *LIBL.
  **/
  public static final String LIBRARY_LIST = "*LIBL";

  /**
   * Selection value representing any status.
   * @see #addObjectSelectionCriteria
  **/
  public static final byte STATUS_ANY = (byte)0x5C; // EBCDIC '*'

  /**
   * Selection value representing *USRLIBL.
  **/
  public static final String USER_LIBRARY_LIST = "*USRLIBL";




  private static final byte BINARY = (byte)0xC2; // EBCDIC 'B'
  private static final byte CHAR = (byte)0xC3; // EBCDIC 'C'
  private static final byte STRUCT = (byte)0xE2; // EBCDIC 'S'

  private AS400 system_;

  private int length_;
  private byte[] handle_; // handle that references the user space used by the open list APIs
  private byte[] handleToClose_; // used to close a previously opened list
  private boolean isConnected_;

  private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);

  // Library authority criteria
  private int currentLibAuthKey_ = 0;
  private String[] libAuthKeys_ = new String[10];

  // Library authority criteria
  private int currentObjectAuthKey_ = 0;
  private String[] objectAuthKeys_ = new String[11];

  // Information status selection criteria
  private int currentStatusKey_ = 0;
  private byte[] statusKeys_ = new byte[5];
  private boolean statusSelection_ = true;

  // Attributes to retrieve
  private int currentKey_ = 0;
  private int[] keys_ = new int[1];

  // Sort keys
  private int currentSortKey_ = 0;
  private int[] sortKeys_ = new int[1];
  private boolean[] sortOrders_ = new boolean[1];

  private String objectName_;
  private String objectLibrary_;
  private String objectType_;


  /**
   * Constructs an ObjectList object. The selection values default to:
   * <UL>
   * <LI>Object library - {@link #ALL ALL}
   * <LI>Object name - {@link #ALL ALL}
   * <LI>Object type - {@link #ALL ALL}
   * </UL>
   * @param system The system.
  **/
  public ObjectList(AS400 system)
  {
    this(system, ALL, ALL, ALL);
  }


  /** 
   * Constructs an ObjectList with the specified selection criteria.
   * @param system The system.
   * @param objectLibrary The library or set of libraries that are searched for objects.
   * Valid values are a specific name, a generic name, or one of the following
   * special values:
   * <UL>
   * <LI>{@link #ALL ALL} - All libraries are searched.
   * <LI>{@link #ALL_USER ALL_USER} - All user libraries are searched.
   * <LI>{@link #CURRENT_LIBRARY CURRENT_LIBRARY} - The current library is searched.
   * <LI>{@link #LIBRARY_LIST LIBRARY_LIST} - The library list is searched.
   * <LI>{@link #USER_LIBRARY_LIST USER_LIBRARY_LIST} - The user portion of the library list is searched.
   * </UL>
   * @param objectName The object name. Valid values are a specific name, a generic
   * name, or one of the following special values:
   * <UL>
   * <LI>{@link #ALL ALL} - All object names are searched.
   * <LI>{@link #ALL_USER ALL_USER} - All objects that are libraries in QSYS or the
   * library list are searched. The object library 
   * must either be {@link #LIBRARY_LIST LIBRARY_LIST} or QSYS. The object type
   * must be *LIB. A list of user libraries is returned.
   * <LI>{@link #IBM IBM} - All objects that are libraries in QSYS or the library
   * list are searched. The object library must either be {@link #LIBRARY_LIST LIBRARY_LIST}
   * or QSYS. The object type must be *LIB. A list of saved (SAVLIB) and restored (RSTLIB)
   * libraries is returned.
   * </UL>
   * @param objectType The type of objects that are searched. Valid values include
   * a specific object type or {@link #ALL ALL}.
  **/
  public ObjectList(AS400 system, String objectLibrary, String objectName, String objectType)
  {
    if (system == null) throw new NullPointerException("system");
    if (objectLibrary == null) throw new NullPointerException("objectLibrary");
    if (objectName == null) throw new NullPointerException("objectName");
    if (objectType == null) throw new NullPointerException("objectType");
    system_ = system;
    objectLibrary_ = objectLibrary;
    objectName_ = objectName;
    objectType_ = objectType;
  }


  /**
   * Adds a library authority as part of the selection criteria for generating
   * the list of objects. Libraries for which the user has the specified authorities
   * are searched. If no library authority criteria are added, the default is
   * {@link #AUTH_DATA_EXECUTE AUTH_DATA_EXECUTE}. A maximum of 10 authorities
   * can be added.
   * @param authority The authority to search. Valid values are:
   * <UL>
   * <LI>{@link #AUTH_ALL AUTH_ALL} - All authority. This consists of all 5 object
   * authorities and all 5 data authorities.
   * <LI>{@link #AUTH_CHANGE AUTH_CHANGE} - Change authority. This consists of all 5
   * data authorities and object operational authority.
   * <LI>{@link #AUTH_USE AUTH_USE} - Use authority. This consists of the read and
   * execute data authorities and object operational authority.
   * <LI>{@link #AUTH_OBJECT_OPERATIONAL AUTH_OBJECT_OPERATIONAL} - Object operational authority.
   * <LI>{@link #AUTH_OBJECT_MANAGEMENT AUTH_OBJECT_MANAGEMENT} - Object management authority.
   * <LI>{@link #AUTH_OBJECT_EXISTENCE AUTH_OBJECT_EXISTENCE} - Object existence authority.
   * <LI>{@link #AUTH_OBJECT_ALTER AUTH_OBJECT_ALTER} - Alter authority.
   * <LI>{@link #AUTH_OBJECT_REFERENCE AUTH_OBJECT_REFERENCE} - Reference authority.
   * <LI>{@link #AUTH_DATA_READ AUTH_DATA_READ} - Read authority.
   * <LI>{@link #AUTH_DATA_ADD AUTH_DATA_ADD} - Add authority.
   * <LI>{@link #AUTH_DATA_UPDATE AUTH_DATA_UPDATE} - Update authority.
   * <LI>{@link #AUTH_DATA_DELETE AUTH_DATA_DELETE} - Delete authority.
   * <LI>{@link #AUTH_DATA_EXECUTE AUTH_DATA_EXECUTE} - Execute authority.
   * </UL>
   * @see #clearLibraryAuthorityCriteria
  **/
  public void addLibraryAuthorityCriteria(String authority)
  {
    if (authority == null) throw new NullPointerException("authority");
    if (!authority.equals(AUTH_ALL) &&
        !authority.equals(AUTH_CHANGE) &&
        !authority.equals(AUTH_USE) &&
        !authority.equals(AUTH_OBJECT_OPERATIONAL) &&
        !authority.equals(AUTH_OBJECT_MANAGEMENT) &&
        !authority.equals(AUTH_OBJECT_EXISTENCE) &&
        !authority.equals(AUTH_OBJECT_ALTER) &&
        !authority.equals(AUTH_OBJECT_REFERENCE) &&
        !authority.equals(AUTH_DATA_READ) &&
        !authority.equals(AUTH_DATA_ADD) &&
        !authority.equals(AUTH_DATA_UPDATE) &&
        !authority.equals(AUTH_DATA_DELETE) &&
        !authority.equals(AUTH_DATA_EXECUTE))
    {
      throw new ExtendedIllegalArgumentException("authority", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (currentLibAuthKey_ >= 10)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Too many authorities added to ObjectList.");
      throw new ExtendedIllegalArgumentException("authority", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    libAuthKeys_[currentLibAuthKey_++] = authority;
    resetHandle();
  }


  /**
   * Adds an object attribute to retrieve when this list is built.
   * The attribute is cached as part of the ObjectDescription objects
   * that are returned by this list, so that another call to the server
   * is not necessary. Adding attributes to retrieve may increase list
   * build time on the server, as well as increasing the amount of
   * storage used to hold the list on the server.
   * <P>
   * The object NAME, LIBRARY, and TYPE are always retrieved. By
   * default, these are the only attributes that are retrieved.
   * If no other attributes are added, the statuses of the objects (returned
   * by {@link com.ibm.as400.access.ObjectDescription#getStatus ObjectDescription.getStatus()})
   * are unknown. Any attributes that are not retrieved via this interface
   * will require another call to the server to retrieve them when
   * {@link com.ibm.as400.access.ObjectDescription#getValue ObjectDescription.getValue()}
   * is called.
   * The exceptions to this are the various attributes that represent Date objects,
   * as they need to be converted from system timestamp format, which always requires
   * another call to the server.
   * @param attribute The attribute to retrieve. Valid values include
   * any of the attributes on the {@link com.ibm.as400.access.ObjectDescription ObjectDescription} class.
   * @see #clearObjectAttributesToRetrieve
  **/
  public void addObjectAttributeToRetrieve(int attribute)
  {
    if (attribute < 200)
    {
      throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (attribute == ObjectDescription.NAME ||
        attribute == ObjectDescription.LIBRARY ||
        attribute == ObjectDescription.TYPE)
    {
      return;
    }
    for (int i=0; i<currentKey_; ++i)
    {
      if (keys_[i] == attribute) return;
    }
    if (currentKey_ >= keys_.length)
    {
      int[] temp = keys_;
      keys_ = new int[temp.length*2];
      System.arraycopy(temp, 0, keys_, 0, temp.length);
    }
    keys_[currentKey_++] = attribute;
    resetHandle();
  }

  /**
   * Adds an object attribute used to sort the list. The attribute
   * is automatically added as an attribute to retrieve.
   * <P>
   * The list of object attributes to sort on is maintained internally even when this ObjectList is closed and re-used.
   * To start over with a new set of object attributes to sort on, call {@link #clearObjectAttributesToSortOn clearObjectAttributesToSortOn()}.
   * @param attribute The object attribute on which to sort.
   * Possible values are all object attributes contained in the {@link com.ibm.as400.access.ObjectDescription ObjectDescription} class,
   * excluding the following:
   * <UL>
   * <LI>{@link com.ibm.as400.access.ObjectDescription#LIBRARY ObjectDescription.LIBRARY}
   * <LI>{@link com.ibm.as400.access.ObjectDescription#NAME ObjectDescription.NAME}
   * <LI>{@link com.ibm.as400.access.ObjectDescription#TYPE ObjectDescription.TYPE}
   * </UL>
   * @param sortOrder true to sort ascending; false to sort descending.
   * @see #clearObjectAttributesToSortOn
   * @see #addObjectAttributeToRetrieve
   * @see com.ibm.as400.access.ObjectDescription
  **/
  public void addObjectAttributeToSortOn(int attribute, boolean sortOrder)
  {
    if (attribute < 200 || attribute > 9999)
    {
      throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    addObjectAttributeToRetrieve(attribute);
    if (currentSortKey_ >= sortKeys_.length)
    {
      int[] temp = sortKeys_;
      sortKeys_ = new int[temp.length*2];
      System.arraycopy(temp, 0, sortKeys_, 0, temp.length);
      boolean[] tempSort = sortOrders_;
      sortOrders_ = new boolean[tempSort.length*2];
      System.arraycopy(tempSort, 0, sortOrders_, 0, tempSort.length);
    }
    sortKeys_[currentSortKey_] = attribute;
    sortOrders_[currentSortKey_++] = sortOrder;
    resetHandle();
  }


  /**
   * Adds an object authority as part of the selection criteria for generating
   * the list of objects. Objects for which the user has the specified authorities
   * are searched. If no object authority criteria are added, the default is
   * {@link #AUTH_ANY AUTH_ANY}. A maximum of 11 authorities
   * can be added.
   * @param authority The authority to search. Valid values are:
   * <UL>
   * <LI>{@link #AUTH_ALL AUTH_ALL} - All authority. This consists of all 5 object
   * authorities and all 5 data authorities.
   * <LI>{@link #AUTH_CHANGE AUTH_CHANGE} - Change authority. This consists of all 5
   * data authorities and object operational authority.
   * <LI>{@link #AUTH_USE AUTH_USE} - Use authority. This consists of the read and
   * execute data authorities and object operational authority.
   * <LI>{@link #AUTH_LIST_MANAGEMENT AUTH_LIST_MANAGEMENT} - Authorization list management
   * authority. This value is valid only for objects whose type is *AUTL. It is
   * ignored for all other object types.
   * <LI>{@link #AUTH_OBJECT_OPERATIONAL AUTH_OBJECT_OPERATIONAL} - Object operational authority.
   * <LI>{@link #AUTH_OBJECT_MANAGEMENT AUTH_OBJECT_MANAGEMENT} - Object management authority.
   * <LI>{@link #AUTH_OBJECT_EXISTENCE AUTH_OBJECT_EXISTENCE} - Object existence authority.
   * <LI>{@link #AUTH_OBJECT_ALTER AUTH_OBJECT_ALTER} - Alter authority.
   * <LI>{@link #AUTH_OBJECT_REFERENCE AUTH_OBJECT_REFERENCE} - Reference authority.
   * <LI>{@link #AUTH_DATA_READ AUTH_DATA_READ} - Read authority.
   * <LI>{@link #AUTH_DATA_ADD AUTH_DATA_ADD} - Add authority.
   * <LI>{@link #AUTH_DATA_UPDATE AUTH_DATA_UPDATE} - Update authority.
   * <LI>{@link #AUTH_DATA_DELETE AUTH_DATA_DELETE} - Delete authority.
   * <LI>{@link #AUTH_DATA_EXECUTE AUTH_DATA_EXECUTE} - Execute authority.
   * <LI>{@link #AUTH_ANY AUTH_ANY} - Any authority other than *EXCLUDE. If this value
   * is specified, no other values can be specified.
   * </UL>
   * @see #clearObjectAuthorityCriteria
  **/
  public void addObjectAuthorityCriteria(String authority)
  {
    if (authority == null) throw new NullPointerException("authority");
    if (!authority.equals(AUTH_ALL) &&
        !authority.equals(AUTH_CHANGE) &&
        !authority.equals(AUTH_USE) &&
        !authority.equals(AUTH_LIST_MANAGEMENT) &&
        !authority.equals(AUTH_OBJECT_OPERATIONAL) &&
        !authority.equals(AUTH_OBJECT_MANAGEMENT) &&
        !authority.equals(AUTH_OBJECT_EXISTENCE) &&
        !authority.equals(AUTH_OBJECT_ALTER) &&
        !authority.equals(AUTH_OBJECT_REFERENCE) &&
        !authority.equals(AUTH_DATA_READ) &&
        !authority.equals(AUTH_DATA_ADD) &&
        !authority.equals(AUTH_DATA_UPDATE) &&
        !authority.equals(AUTH_DATA_DELETE) &&
        !authority.equals(AUTH_DATA_EXECUTE) &&
        !authority.equals(AUTH_ANY))
    {
      throw new ExtendedIllegalArgumentException("authority", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (currentObjectAuthKey_ >= 11)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Too many authorities added to ObjectList.");
      throw new ExtendedIllegalArgumentException("authority", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    objectAuthKeys_[currentObjectAuthKey_++] = authority;
    resetHandle();
  }


  /**
   * Adds an object attribute used to filter the list. If no statuses are
   * added as selection criteria, the default is to include objects with
   * {@link #STATUS_ANY STATUS_ANY}. A maximum of 5 statuses can be added.
   * @param status The object information status criteria. Possible values
   * include:
   * <UL>
   * <LI>{@link com.ibm.as400.access.ObjectDescription#STATUS_NO_AUTHORITY ObjectDescription.STATUS_NO_AUTHORITY} - Objects that
   * do not meet the authorities specified in the object authority criteria
   * for this user. See {@link #addObjectAuthorityCriteria addObjectAuthorityCriteria}.
   * <LI>{@link com.ibm.as400.access.ObjectDescription#STATUS_DAMAGED ObjectDescription.STATUS_DAMAGED} - Objects that are damaged.
   * <LI>{@link com.ibm.as400.access.ObjectDescription#STATUS_LOCKED ObjectDescription.STATUS_LOCKED} - Objects that are locked.
   * <LI>{@link com.ibm.as400.access.ObjectDescription#STATUS_PARTIALLY_DAMAGED ObjectDescription.STATUS_PARTIALLY_DAMAGED} - Objects
   * that are partially damaged.
   * <LI>{@link #STATUS_ANY ObjectList.STATUS_ANY} - Objects with any status.
   * </UL>
   * @see #clearObjectSelectionCriteria
   * @see #setObjectSelection
  **/
  public void addObjectSelectionCriteria(byte status)
  {
    if (status != ObjectDescription.STATUS_NO_AUTHORITY &&
        status != ObjectDescription.STATUS_DAMAGED &&
        status != ObjectDescription.STATUS_LOCKED &&
        status != ObjectDescription.STATUS_PARTIALLY_DAMAGED &&
        status != STATUS_ANY)
    {
      throw new ExtendedIllegalArgumentException("status", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (currentStatusKey_ >= 5)
    {
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Too many statuses specified for ObjectList.");
      throw new ExtendedIllegalArgumentException("status", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    statusKeys_[currentStatusKey_] = status;
    resetHandle();
  }


  /**
   * Clears the library authority criteria used to filter the list.
   * @see #addLibraryAuthorityCriteria
  **/
  public void clearLibraryAuthorityCriteria()
  {
    currentLibAuthKey_ = 0;
    libAuthKeys_ = new String[10];
    resetHandle();
  }


  /**
   * Clears the object attribtues to retrieve as part of this list.
   * This resets the attributes to retrieve back to the default
   * NAME, LIBRARY, and TYPE.
   * @see #addObjectAttributeToRetrieve
  **/
  public void clearObjectAttributesToRetrieve()
  {
    keys_ = new int[1];
    currentKey_ = 0;
    resetHandle();
  }

  /**
   * Clears the object attributes used to sort the list. This resets all of the 
   * object sort parameters to their default values.
   * @see #addObjectAttributeToSortOn
  **/
  public void clearObjectAttributesToSortOn()
  {
    currentSortKey_ = 0;
    sortKeys_ = new int[1];
    sortOrders_ = new boolean[1];
    resetHandle();
  }


  /**
   * Clears the object authority criteria used to filter the list.
   * @see #addObjectAuthorityCriteria
  **/
  public void clearObjectAuthorityCriteria()
  {
    currentObjectAuthKey_ = 0;
    objectAuthKeys_ = new String[11];
    resetHandle();
  }

  /**
   * Clears the object statuses used to filter the list and resets the
   * object selection to include objects in the list (true).
   * @see #addObjectSelectionCriteria
   * @see #setObjectSelection
  **/
  public void clearObjectSelectionCriteria()
  {
    currentStatusKey_ = 0;
    statusKeys_ = new byte[5];
    statusSelection_ = true;
    resetHandle();
  }


  /**
   * Closes the object list on the system.
   * This releases any system resources previously in use by this object list.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @see #load
  **/
  public synchronized void close() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!isConnected_)
    {
      return;
    }
    if (handleToClose_ != null && (handle_ == null || handle_ == handleToClose_))
    {
      handle_ = handleToClose_;
      handleToClose_ = null;
    }
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Closing object list with handle: ", handle_);
    }
    ProgramParameter[] parms = new ProgramParameter[]
    {
      new ProgramParameter(handle_),
      errorCode_
    };
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    isConnected_ = false;
    handle_ = null;
    if (handleToClose_ != null) // Just in case.
    {
      handle_ = handleToClose_;
      handleToClose_ = null;
      close();
    }
  }

  private void ensureSelectionKey(int key)
  {
    for (int i=0; i<currentKey_; ++i)
    {
      if (keys_[i] == key) return;
    }
    if (keys_.length  <= currentKey_)
    {
      int[] temp = keys_;
      keys_ = new int[temp.length+1];
      System.arraycopy(temp, 0, keys_, 0, temp.length);
    }
    keys_[currentKey_++] = key;
  }

  private void ensureSortKeyAfter(int indexKey, int key)
  {
    int found = -1;
    for (int i=0; i<currentSortKey_; ++i)
    {
      if (sortKeys_[i] == indexKey)
      {
        found = i;
        break;
      }
    }
    if (found == -1) return; // should never happen
    int offset = found+1;
    if (found < currentSortKey_-1 && sortKeys_[offset] == key) return;
    int[] temp = sortKeys_;
    boolean[] tempOrders = sortOrders_;
    sortKeys_ = new int[temp.length+1];
    sortOrders_ = new boolean[temp.length+1];
    System.arraycopy(temp, 0, sortKeys_, 0, offset);
    System.arraycopy(tempOrders, 0, sortOrders_, 0, offset);
    sortKeys_[offset] = key;
    sortOrders_[offset] = tempOrders[found];
    if (offset < temp.length)
    {
      System.arraycopy(temp, offset, sortKeys_, offset+1, temp.length-offset);
      System.arraycopy(tempOrders, offset, sortOrders_, offset+1, temp.length-offset);
    }
    ++currentSortKey_;
  }

   
  private void ensureSortKeyBefore(int indexKey, int key)
  {
    int found = -1;
    for (int i=0; i<currentSortKey_; ++i)
    {
      if (sortKeys_[i] == indexKey)
      {
        found = i;
        break;
      }
    }
    if (found == -1) return; // should never happen
    if (found > 0 && sortKeys_[found-1] == key) return;
    int[] temp = sortKeys_;
    boolean[] tempOrders = sortOrders_;
    sortKeys_ = new int[temp.length+1];
    sortOrders_ = new boolean[temp.length+1];
    System.arraycopy(temp, 0, sortKeys_, 0, found);
    System.arraycopy(tempOrders, 0, sortOrders_, 0, found);
    sortKeys_[found] = key;
    sortOrders_[found] = tempOrders[found];
    System.arraycopy(temp, found, sortKeys_, found+1, temp.length-found);
    System.arraycopy(tempOrders, found, sortOrders_, found+1, temp.length-found);
    ++currentSortKey_;
  }

   
  /**
   * Helper method used to determine which extra keys, if any, need to be added for
   * proper selection and sorting.
  **/
  private void fixUpKeys()
  {
    for (int i=0; i<currentKey_; ++i)
    {
      switch(keys_[i])
      {
        case ObjectDescription.SOURCE_FILE:
          ensureSelectionKey(ObjectDescription.SOURCE_FILE_LIBRARY);
          ensureSelectionKey(ObjectDescription.SOURCE_FILE_MEMBER);
          break;
        case ObjectDescription.SAVE_FILE:
          ensureSelectionKey(ObjectDescription.SAVE_FILE_LIBRARY);
          break;
        case ObjectDescription.OBJECT_SIZE:
          ensureSelectionKey(ObjectDescription.OBJECT_SIZE_MULTIPLIER);
          break;
        case ObjectDescription.JOURNAL:
          ensureSelectionKey(ObjectDescription.JOURNAL_LIBRARY);
          break;
        case ObjectDescription.SAVE_SIZE:
          ensureSelectionKey(ObjectDescription.SAVE_SIZE_MULTIPLIER);
          break;
      }
    }
    int i = 0;
    while (i < currentSortKey_)
    {
      int thisSortKey = currentSortKey_;
      for (i=0; i<currentSortKey_; ++i)
      {
        switch(sortKeys_[i])
        {
          case ObjectDescription.SOURCE_FILE:
            ensureSortKeyBefore(ObjectDescription.SOURCE_FILE, ObjectDescription.SOURCE_FILE_LIBRARY);
            ensureSortKeyAfter(ObjectDescription.SOURCE_FILE, ObjectDescription.SOURCE_FILE_MEMBER);
            break;
          case ObjectDescription.SAVE_FILE:
            ensureSortKeyBefore(ObjectDescription.SAVE_FILE, ObjectDescription.SAVE_FILE_LIBRARY);
            break;
          case ObjectDescription.OBJECT_SIZE:
            ensureSortKeyBefore(ObjectDescription.OBJECT_SIZE, ObjectDescription.OBJECT_SIZE_MULTIPLIER);
            break;
          case ObjectDescription.JOURNAL:
            ensureSortKeyBefore(ObjectDescription.JOURNAL, ObjectDescription.JOURNAL_LIBRARY);
            break;
          case ObjectDescription.SAVE_SIZE:
            ensureSortKeyBefore(ObjectDescription.SAVE_SIZE, ObjectDescription.SAVE_SIZE_MULTIPLIER);
            break;
        }
        if (thisSortKey != currentSortKey_)
        {
          // We made a change.
          i = 0;
          break;
        }
      }
    }
  }


  /**
   * Returns the number of objects in the object list. This method implicitly calls {@link #load load()}.
   * @return The number of objects, or 0 if no list was retrieved.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception ServerStartupException          If the server cannot be started.
   * @exception UnknownHostException            If the system cannot be located.
   * @see #load
  **/
  public synchronized int getLength() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    if (handle_ == null)
    {
      load();
    }
    return length_;
  }


  /**
   * Returns the library used to filter this list.
   * @return The library.
  **/
  public String getLibrary()
  {
    return objectLibrary_;
  }

  /**
   * Returns the object name used to filter this list.
   * @return The object name.
  **/
  public String getName()
  {
    return objectName_;
  }


  /**
   * Returns the list of objects in the object list.
   * @return An Enumeration of {@link com.ibm.as400.access.ObjectDescription ObjectDescription} objects.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception RequestNotSupportedException    If the requested function is not supported because the AS/400 system is not at the correct level.
   * @see #close
   * @see #load
  **/
  public synchronized Enumeration getObjects()
  throws AS400Exception,
  AS400SecurityException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  RequestNotSupportedException
  {
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (handle_ == null)
    {
      load(); // Need to get the length_
    }

    return new ObjectDescriptionEnumeration(this, length_);
  }



  /**
   * Returns a subset of the list of objects.
   * This method allows the user to retrieve the object list from the server
   * in pieces. If a call to {@link #load load()} is made (either implicitly or explicitly),
   * then the objects at a given offset will change, so a subsequent call to
   * getObjects() with the same <i>listOffset</i> and <i>number</i>
   * will most likely not return the same ObjectDescriptions as the previous call.
   * @param listOffset The offset into the list of objects. This value must be greater than 0 and
   * less than the list length, or specify -1 to retrieve all of the objects.
   * @param number The number of objects to retrieve out of the list, starting at the specified
   * <i>listOffset</i>. This value must be greater than or equal to 0 and less than or equal
   * to the list length. If the <i>listOffset</i> is -1, this parameter is ignored.
   * @return The array of retrieved {@link com.ibm.as400.access.ObjectDescription ObjectDescription} objects.
   * The length of this array may not necessarily be equal to <i>number</i>, depending upon the size
   * of the list on the server, and the specified <i>listOffset</i>.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @see com.ibm.as400.access.Job
   * @see #close
   * @see #load
  **/
  public synchronized ObjectDescription[] getObjects(int listOffset, int number) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (listOffset < -1)
    {
      throw new ExtendedIllegalArgumentException("listOffset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (number < 0 && listOffset != -1)
    {
      throw new ExtendedIllegalArgumentException("number", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (number == 0 && listOffset != -1)
    {
      return new ObjectDescription[0];
    }

    if (handle_ == null)
    {
      load();
    }

    if (listOffset == -1) number = length_;

    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);

    ProgramParameter[] parms2 = new ProgramParameter[7];
    int len = number*60; // best guess
    parms2[0] = new ProgramParameter(len); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_);
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(number)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(listOffset == -1 ? -1 : listOffset+1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }

    byte[] listInfo = parms2[3].getOutputData();
    if (listInfo.length == 0) return new ObjectDescription[0]; // Shouldn't have to do this, but this API doesn't like certain empty libraries for some reason.
    int totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
    int recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    int recordLength = BinaryConverter.byteArrayToInt(listInfo, 12);
    while (listOffset == -1 && totalRecords > recordsReturned)
    {
      len = len*(1+(totalRecords/(recordsReturned+1)));
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling ObjectList QGYGTLE again with an updated length of "+len+".");
      try
      {
        parms2[0].setOutputDataLength(len);
        parms2[1].setInputData(BinaryConverter.intToByteArray(len));
      }
      catch (PropertyVetoException pve)
      {
      }
      if (!pc2.run())
      {
        throw new AS400Exception(pc2.getMessageList());
      }
      listInfo = parms2[3].getOutputData();
      totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
      recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
      recordLength = BinaryConverter.byteArrayToInt(listInfo, 12);
    }

    ListUtilities.checkListStatus(listInfo[30]);  // check the list status indicator
    byte[] data = parms2[0].getOutputData();

    ObjectDescription[] objects = new ObjectDescription[recordsReturned];
    int offset = 0;
    for (int i=0; i<recordsReturned; ++i)
    {
      String objectName = conv.byteArrayToString(data, offset, 10).trim();
      String objectLibrary = conv.byteArrayToString(data, offset+10, 10).trim();
      String objectType = conv.byteArrayToString(data, offset+20, 10).trim().substring(1); // strip off leading *
      byte infoStatus = data[offset+30];
      int numFields = BinaryConverter.byteArrayToInt(data, offset+32);
      objects[i] = new ObjectDescription(system_, objectLibrary, objectName, objectType, infoStatus);
      if (infoStatus != ObjectDescription.STATUS_NO_AUTHORITY &&
          infoStatus != ObjectDescription.STATUS_LOCKED)
      {
        int fieldOffset = offset+36;
        for (int j=0; j<numFields && fieldOffset+16 < data.length; ++j) // Shouldn't need this check, but this API doesn't like certain objects for some reason.
        {
          int infoLength = BinaryConverter.byteArrayToInt(data, fieldOffset);
          int key = BinaryConverter.byteArrayToInt(data, fieldOffset+4);
          byte type = data[fieldOffset+8];
          int dataLength = BinaryConverter.byteArrayToInt(data, fieldOffset+12);
          byte[] keyData = new byte[dataLength];
          System.arraycopy(data, fieldOffset+16, keyData, 0, dataLength);
          Object value = null;
          if (type == BINARY)
          {
            value = new Integer(BinaryConverter.byteArrayToInt(keyData, 0));
            objects[i].set(key, value);
          }
          else if (type == CHAR)
          {
            // Check for system timestamps and whether we need to trim or not.
            switch(key)
            {
              case ObjectDescription.CREATION_DATE:
              case ObjectDescription.CHANGE_DATE:
              case ObjectDescription.SAVE_DATE:
              case ObjectDescription.RESTORE_DATE:
              case ObjectDescription.SAVE_ACTIVE_DATE:
              case ObjectDescription.JOURNAL_START_DATE:
              case ObjectDescription.LAST_USED_DATE:
              case ObjectDescription.RESET_DATE:
                value = keyData;
                break;
              case ObjectDescription.DOMAIN:
              case ObjectDescription.SOURCE_FILE_UPDATED_DATE:
              case ObjectDescription.SAVE_VOLUME_ID:
              case ObjectDescription.SYSTEM_LEVEL:
              case ObjectDescription.USER_CHANGED:
              case ObjectDescription.USAGE_INFO_UPDATED:
              case ObjectDescription.COMPRESSION:
              case ObjectDescription.ALLOW_CHANGE_BY_PROGRAM:
              case ObjectDescription.CHANGED_BY_PROGRAM:
              case ObjectDescription.OVERFLOWED_ASP:
              case ObjectDescription.JOURNAL_STATUS:
              case ObjectDescription.JOURNAL_IMAGES:
              case ObjectDescription.JOURNAL_OMITTED_ENTRIES:
              case ObjectDescription.DIGITALLY_SIGNED:
              case ObjectDescription.DIGITALLY_SIGNED_TRUSTED:
              case ObjectDescription.DIGITALLY_SIGNED_MULTIPLE:
                value = conv.byteArrayToString(keyData, 0, keyData.length);
                break;
              default:
                value = conv.byteArrayToString(keyData, 0, keyData.length).trim();
                break;
            }
            objects[i].set(key, value);
          }
/*          else if (type == STRUCT)
          {
            if (key >= COMBINATION_200)
            {
              objects[i].set(ObjectDescription.INFORMATION_STATUS, conv.byteArrayToString(keyData, 0, 1));
              objects[i].set(ObjectDescription.EXTENDED_ATTRIBUTE, conv.byteArrayToString(keyData, 1, 10).trim());
              objects[i].set(ObjectDescription.TEXT_DESCRIPTION, conv.byteArrayToString(keyData, 11, 10).trim());
              objects[i].set(ObjectDescription.USER_DEFINED_ATTRIBUTE, conv.byteArrayToString(keyData, 61, 10).trim());
              objects[i].set(ObjectDescription.ORDER_IN_LIBRARY_LIST, BinaryConverter.byteArrayToInt(keyData, 71));
            }
            if (key >= COMBINATION_300)
            {
              objects[i].set(ObjectDescription.OBJECT_ASP_NUMBER, BinaryConverter.byteArrayToInt(keyData, 80));
              objects[i].set(ObjectDescription.OWNER, conv.byteArrayToString(keyData, 84, 10).trim());
              objects[i].set(ObjectDescription.DOMAIN, conv.byteArrayToString(keyData, 94, 2));
              byte[] timestamp = new byte[8];
              System.arraycopy(keyData, 96, timestamp, 0, 8);
              objects[i].set(ObjectDescription.CREATION_DATE, timestamp);
              timestamp = new byte[8];
              System.arraycopy(keyData, 104, timestamp, 0, 8);
              objects[i].set(ObjectDescription.CHANGE_DATE, timestamp);
              objects[i].set(ObjectDescription.STORAGE_STATUS, conv.byteArrayToString(keyData, 112, 10).trim());
              objects[i].set(ObjectDescription.COMPRESSION, conv.byteArrayToString(keyData, 122, 1));
              objects[i].set(ObjectDescription.ALLOW_CHANGE_BY_PROGRAM, keyData[123] == (byte)0xF1);
              objects[i].set(ObjectDescription.CHANGED_BY_PROGRAM, keyData[124] == (byte)0xF1);
              objects[i].set(ObjectDescription.AUDITING, conv.byteArrayToString(keyData, 125, 10).trim());
              objects[i].set(ObjectDescription.DIGITALLY_SIGNED, keyData[135] == (byte)0xF1);
              objects[i].set(ObjectDescription.DIGITALLY_SIGNED_TRUSTED, keyData[136] == (byte)0xF1);
              objects[i].set(ObjectDescription.DIGITALLY_SIGNED_MULTIPLE, keyData[137] == (byte)0xF1);
              objects[i].set(ObjectDescription.LIBRARY_ASP_NUMBER, BinaryConverter.byteArrayToInt(keyData, 140));
            }
            if (key >= COMBINATION_400)
            {
            }
            System.out.println("Struct not supported: "+key);
          }
*/          
          else
          {
            throw new InternalErrorException("Unknown key type for key "+key+": "+type, InternalErrorException.SYNTAX_ERROR);
          }
          fieldOffset += infoLength;
        }
      }
      offset += recordLength;
    }

    return objects;
  }


  /**
   * Returns the system.
   * @return The system.
  **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
   * Returns the object type used to filter this list.
   * @return The object type.
  **/
  public String getType()
  {
    return objectType_;
  }


  /**
   * Loads the list of objects on the system. This method informs the
   * system to build a list of objects. This method blocks until the system returns
   * the total number of objects it has compiled. A subsequent call to
   * {@link #getObjects getObjects()} will retrieve the actual object information
   * and attributes for each object in the list from the system.
   * <p>This method updates the list length.
   *
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception ServerStartupException          If the server cannot be started.
   * @exception UnknownHostException            If the system cannot be located.
   * @see #getLength
   * @see #close
  **/
  public synchronized void load() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    // Close the previous list
    if (handle_ != null || handleToClose_ != null)
    {
      close();
    }

    // Generate text objects based on system CCSID
    final int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    AS400Text text10 = new AS400Text(10, ccsid, system_);

    // Setup program parameters
    ProgramParameter[] parms = new ProgramParameter[12];
    parms[0] = new ProgramParameter(1); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // length of receiver variable
    parms[2] = new ProgramParameter(80); // list information
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(-1)); // number of records to return
    
    fixUpKeys();

    byte[] sortInfo = null;
    int numSortKeys = currentSortKey_;
    if (numSortKeys > 0)
    {
      sortInfo = new byte[4+numSortKeys*12];
      BinaryConverter.intToByteArray(numSortKeys, sortInfo, 0);
      int offset = 4;
      for (int i=0; i<numSortKeys; ++i)
      {
        int key = sortKeys_[i];
        boolean order = sortOrders_[i];
        int fieldLength = ObjectDescription.keyLengths_.get(key);
        short dataType = (short)4; // Data type 4 = character data, NLS-sort supported, DBCS treated as single-byte.
        int fieldStartingPosition = 53; // It's dumb these are 1-based.
        // We'll use 0 (signed binary) for all of the int types:
        switch(key)
        {
          case ObjectDescription.ORDER_IN_LIBRARY_LIST:
          case ObjectDescription.OBJECT_ASP_NUMBER:
          case ObjectDescription.LIBRARY_ASP_NUMBER:
          case ObjectDescription.SAVE_SIZE:
          case ObjectDescription.SAVE_SIZE_MULTIPLIER:
          case ObjectDescription.SAVE_SEQUENCE_NUMBER:
          case ObjectDescription.DAYS_USED:
          case ObjectDescription.OBJECT_SIZE:
          case ObjectDescription.OBJECT_SIZE_MULTIPLIER:
          
          case ObjectDescription.CREATION_DATE:
          case ObjectDescription.CHANGE_DATE:
          case ObjectDescription.SAVE_DATE:
          case ObjectDescription.RESTORE_DATE:
          case ObjectDescription.SAVE_ACTIVE_DATE:
          case ObjectDescription.JOURNAL_START_DATE:
          case ObjectDescription.LAST_USED_DATE:
          case ObjectDescription.RESET_DATE:
            dataType = (short)0; // signed binary
            break;
        }
        for (int j=0; keys_[j] != key; ++j)
        {
          fieldStartingPosition += 16 + ObjectDescription.keyLengths_.get(keys_[j]); // see format of receiver variable
        }
        BinaryConverter.intToByteArray(fieldStartingPosition, sortInfo, offset);
        offset += 4;
        BinaryConverter.intToByteArray(fieldLength, sortInfo, offset);
        offset += 4;
        BinaryConverter.shortToByteArray(dataType, sortInfo, offset);
        offset += 2; 
        // '1' = ascending, '2' = descending (0xF1 = 1 and 0xF2 = 2)
        sortInfo[offset] = order ? (byte)0xF1 : (byte)0xF2;
        offset += 2;
      }
    }
    else
    {
      sortInfo = new byte[4]; // no sort info.
    }
    parms[4] = new ProgramParameter(sortInfo); // sort information
    byte[] objectNameAndLibrary = new byte[20];
    text10.toBytes(objectName_, objectNameAndLibrary, 0);
    text10.toBytes(objectLibrary_, objectNameAndLibrary, 10);
    parms[5] = new ProgramParameter(objectNameAndLibrary); // object and library name
    parms[6] = new ProgramParameter(text10.toBytes(objectType_)); // object type
    byte[] authInfo = new byte[28+((currentObjectAuthKey_+currentLibAuthKey_)*10)];
    BinaryConverter.intToByteArray(authInfo.length, authInfo, 0); // length of authority control format
    BinaryConverter.intToByteArray(1, authInfo, 4); // call level
    BinaryConverter.intToByteArray(currentObjectAuthKey_ == 0 ? 0 : 28, authInfo, 8); // displacement to object authorities
    BinaryConverter.intToByteArray(currentObjectAuthKey_, authInfo, 12); // number of object authorities
    BinaryConverter.intToByteArray(currentLibAuthKey_ == 0 ? 0 : 28+(currentObjectAuthKey_*10), authInfo, 16); // displacement to library authorities
    BinaryConverter.intToByteArray(currentLibAuthKey_, authInfo, 20); // number of library authorities
    BinaryConverter.intToByteArray(0, authInfo, 24); // reserved
    int offset = 28;
    for (int i=0; i<currentObjectAuthKey_; ++i)
    {
      text10.toBytes(objectAuthKeys_[i], authInfo, offset);
      offset += 10;
    }
    for (int i=0; i<currentLibAuthKey_; ++i)
    {
      text10.toBytes(libAuthKeys_[i], authInfo, offset);
      offset += 10;
    }
    parms[7] = new ProgramParameter(authInfo); // authority control

    int num = currentStatusKey_;
    byte[] statuses = statusKeys_;
    if (num == 0)
    {
      num = 1;
      statuses = new byte[] { STATUS_ANY};
    }
    byte[] selectionInfo = new byte[20+num];
    BinaryConverter.intToByteArray(selectionInfo.length, selectionInfo, 0); // length of selection control format
    BinaryConverter.intToByteArray(statusSelection_ ? 0 : 1, selectionInfo, 4); // select or omit status value
    BinaryConverter.intToByteArray(20, selectionInfo, 8); // displacement to statuses
    BinaryConverter.intToByteArray(num, selectionInfo, 12); // number of statuses
    BinaryConverter.intToByteArray(0, selectionInfo, 16); // reserved
    System.arraycopy(statuses, 0, selectionInfo, 20, num); // statuses
    parms[8] = new ProgramParameter(selectionInfo); // selection control

    parms[9] = new ProgramParameter(BinaryConverter.intToByteArray(currentKey_)); // number of fields to return

    byte[] keyInfo = new byte[currentKey_*4];
    offset = 0;
    for (int i=0; i<currentKey_; ++i)
    {
      BinaryConverter.intToByteArray(keys_[i], keyInfo, offset);
      offset += 4;
    }
    parms[10] = new ProgramParameter(keyInfo); // key fields to return;
    parms[11] = errorCode_;

    // Call the program
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLOBJ.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    isConnected_ = true;

    // List information returned
    byte[] listInformation = parms[2].getOutputData();
    ListUtilities.checkListStatus(listInformation[30]);  // check the list status indicator
    handle_ = new byte[4];
    System.arraycopy(listInformation, 8, handle_, 0, 4);

    // This second program call is to retrieve the number of messages in the list.
    // It will wait until the server has fully populated the list before it
    // returns.
    ProgramParameter[] parms2 = new ProgramParameter[7];
    parms2[0] = new ProgramParameter(1); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_); // request handle
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(-1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }
    byte[] listInfo2 = parms2[3].getOutputData();
    ListUtilities.checkListStatus(listInfo2[30]);  // check the list status indicator
    length_ = BinaryConverter.byteArrayToInt(listInfo2, 0);

    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Loaded object list with length = "+length_+" and handle: ", handle_);
    }
  }


  // Resets the handle to indicate we should close the list the next time
  // we do something, usually as a result of one of the selection criteria
  // being changed since that should build a new list on the server.
  private synchronized void resetHandle()
  {
    if (handleToClose_ == null) handleToClose_ = handle_; // Close the old list on the next load
    handle_ = null;
  }


  /**
   * Sets whether or not the object selection criteria are used to include
   * objects in the list or to omit them from the list. The default is
   * true, which is to include objects in the list with the specified criteria
   * as added by {@link #addObjectSelectionCriteria addObjectSelectionCriteria()}.
   * @param select true to include objects in the list that have the
   * specified statuses, false to omit objects from the list that have the
   * specified statuses.
   * @see #addObjectSelectionCriteria
   * @see #clearObjectSelectionCriteria
  **/
  public void setObjectSelection(boolean select)
  {
    statusSelection_ = select;
    resetHandle();
  }
}
