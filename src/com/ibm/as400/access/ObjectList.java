///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename:  ObjectList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2007 International Business Machines Corporation and     
// others.  All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @A1 - 07/24/2007 - Changes to addObjectAuthorityCriteria() to enforce 
//                    documented interface restriction related to AUTH_ANY
// @A2 - 07/25/2007 - Changes to load() and getObjects() to obtain and use
//                    the correct recordLength for the records returned.
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.*;
import java.io.*;
import java.util.*;



/**
Represents a list of system objects in a
specific library, multiple libraries, or system-wide.
<p>
Implementation note:
This class internally uses the Open List APIs (e.g. QGYOLOBJ).

@see com.ibm.as400.access.ObjectDescription
**/
public class ObjectList implements Serializable
{
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
   * Constant indicating that the auxiliary storage pools that are currently
   * part of the the thread's library name space will be searched to locate
   * the library.  This includes the system ASP (ASP 1), all defined basic
   * user ASPs (ASPs 2-32), and, if the thread has an ASP group, the primary
   * and secondary ASPs in the thread's ASP group.
   */
  public static final String ASP_NAME_ALL = "*";		// @550A
  
  /**
   * Constant indicating that the system ASP (ASP 1) and all defined basic user ASPs (ASPs 2-32)
   * will be searched to locate the library.  No primary or secondary ASPs will be
   * searched, even if the thread has an ASP group. 
   */
  public static final String ASP_NAME_SYSBAS = "*SYSBAS";	// @550A
  
  /**
   * Constant indicating if the thread has an ASP group, the primary and secondary ASPs
   * in the ASP group will be searched to locate the library.  The system ASP (ASP 1) and
   * defined basic user ASPs (ASPs 2-32) will not be searched.
   */
  public static final String ASP_NAME_CURASPGRP = "*CURASPGRP";	// @550A
  
  /**
   * Constant indicating that all available ASPs will be searched.  This includes the system
   * ASP (ASP 1), all defined basic user ASPs (ASPs 2-32), and all available primary and
   * secondary ASPs (ASPs 33-255 with a status of 'Available').  The ASP groups are searched
   * in alphabetical order by the primary ASP.  The system ASP and all defined basic user
   * ASPs are searched after the ASP groups.  ASPs and libraries to which the user is not authorized
   * are bypassed and no authority error messages are sent.  The search ends when the first object
   * is found of the specified object name, library name, and object type.  If the user is
   * not authorized to the object, an authority error message is sent.
   */
  public static final String ASP_NAME_ALLAVL = "*ALLAVL";	// @550A
  
  /**
   * Constant indicating that only the single ASP named in the auxiliary storage
   * pool device name field will be searched.
   */
  public static final String ASP_SEARCH_TYPE_ASP = "*ASP";	// @550A
  
  /**
   * Constant indicating that all ASPs in the auxiliary storage pool group named
   * in the auxiliary storage pool device name field will be searched.  The device
   * name must be the name of the primary auxiliary storage pool in the group.
   */
  public static final String ASP_SEARCH_TYPE_ASPGRP = "*ASPGRP"; // @550A

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
  private int recLen_;	  // Length of a single record; should never be zero for this API @A2A
  private byte[] handle_; // handle that references the user space used by the open list APIs
  private byte[] handleToClose_; // used to close a previously opened list
  private boolean isConnected_;

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
  private String aspDeviceName_;		// @550A
  private String aspSearchType_ = ASP_SEARCH_TYPE_ASP;	// @550A


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
   * a specific object type (*LIB, *FILE, *OUTQ, etc) or {@link #ALL ALL}.
  **/
  public ObjectList(AS400 system, String objectLibrary, String objectName, String objectType)
  {
    if (system == null) throw new NullPointerException("system");
    if (objectLibrary == null) throw new NullPointerException("objectLibrary");
    if (objectName == null) throw new NullPointerException("objectName");
    if (objectType == null) throw new NullPointerException("objectType");
    system_ = system;
    objectLibrary_ = QSYSObjectPathName.toQSYSName(objectLibrary);//@O5C
    objectName_ = QSYSObjectPathName.toQSYSName(objectName);//@O5C
    objectType_ = objectType.toUpperCase();//@O5C
  }

  //@550A
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
   * a specific object type (*LIB, *FILE, *OUTQ, etc) or {@link #ALL ALL}.
   * @param aspDeviceName The name of an auxiliary storage pool (ASP) device in which storage is 
   * allocated for the library that contains the object or one of the following special values:
   * <ul>
   * <li>{@link #ASP_NAME_ALL ASP_NAME_ALL} - The ASPs in the thread's library name space.</li>
   * <li>{@link #ASP_NAME_ALLAVL ASP_NAME_ALLAVL} - All available ASPs.</li>
   * <li>{@link #ASP_NAME_CURASPGRP ASP_NAME_CURASPGRP} - The ASPs in the current thread's ASP group.</li>
   * <li>{@link #ASP_NAME_SYSBAS ASP_NAME_SYSBAS} - The system ASP (ASP 1) and defined basic user ASPs (ASPs 2-32).</li>
   * </ul>
  **/
  public ObjectList(AS400 system, String objectLibrary, String objectName, String objectType, String aspDeviceName)
  {
    if (system == null) throw new NullPointerException("system");
    if (objectLibrary == null) throw new NullPointerException("objectLibrary");
    if (objectName == null) throw new NullPointerException("objectName");
    if (objectType == null) throw new NullPointerException("objectType");
    system_ = system;
    objectLibrary_ = QSYSObjectPathName.toQSYSName(objectLibrary);//@O5C
    objectName_ = QSYSObjectPathName.toQSYSName(objectName);//@O5C
    objectType_ = objectType.toUpperCase();//@O5C
    aspDeviceName_ = aspDeviceName;
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
   * that are returned by this list, so that another call to the system
   * is not necessary. Adding attributes to retrieve may increase list
   * build time on the system, as well as increasing the amount of
   * storage used to hold the list on the system.
   * <P>
   * The object NAME, LIBRARY, and TYPE are always retrieved. By
   * default, these are the only attributes that are retrieved.
   * If no other attributes are added, the statuses of the objects (returned
   * by {@link com.ibm.as400.access.ObjectDescription#getStatus ObjectDescription.getStatus()})
   * are unknown. Any attributes that are not retrieved via this interface
   * will require another call to the system to retrieve them when
   * {@link com.ibm.as400.access.ObjectDescription#getValue ObjectDescription.getValue()}
   * is called.
   * The exceptions to this are the various attributes that represent Date objects,
   * as they need to be converted from system timestamp format, which always requires
   * another call to the system.
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

    // Start of changes for change flag --------------------------- @A1A
    // Following code is to enforce Javadoc which states:
    //   If this value [AUTH_ANY] is specified, no other values can be specified.
    if ((authority.equals(AUTH_ANY)) && (currentObjectAuthKey_ != 0))
    {
      // If AUTH_ANY is specified, then it must be the first/only entry
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Attempt to add AUTH_ANY auth after other auth was added.");
      throw new ExtendedIllegalArgumentException("authority", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    
    if ((currentObjectAuthKey_ > 0) &&         // Adding auth beyond 1st auth in list -AND-
	(objectAuthKeys_[0].equals(AUTH_ANY))) // first auth in list is AUTH_ANY
    {
      // If AUTH_ANY was already specified, cannot add another auth to the list.
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Attempt to add auth after AUTH_ANY auth was added.");
      throw new ExtendedIllegalArgumentException("authority", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    // End of changes for change flag ----------------------------- @A1A

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
   * This will not close the connection to the Host Server job held by the associated AS400 object.
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

    try {
      ListUtilities.closeList(system_, handle_);
    }
    finally {
      isConnected_ = false;
      handle_ = null;
    }


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

  //@550A
  /**
   * Returns the name of an auxiliary storage pool (ASP) device in which storage is 
   * allocated for the library that contains the object.
   * @return The auxiliary storage pool (ASP) device name or null if no ASP device name has been set.
   */
  public String getAspDeviceName()
  {
	  return aspDeviceName_;
  }

  //@550A
  /**
   * Returns the type of search to be used withn a specific auxiliary storage pool
   * device name is specified.
   * @return The search type.
   */
  public String getAspSearchType(){
	  return aspSearchType_;
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
   * @exception ServerStartupException          If the host server cannot be started.
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
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the system object does not exist.
   * @exception RequestNotSupportedException    If the requested function is not supported because the system is not at the correct level.
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
   * This method allows the user to retrieve the object list from the system
   * in pieces. If a call to {@link #load load()} is made (either implicitly or explicitly),
   * then the objects at a given list offset will change, so a subsequent call to
   * getObjects() with the same <i>listOffset</i> and <i>number</i>
   * will most likely not return the same ObjectDescriptions as the previous call.
   * @param listOffset The offset in the list of objects (0-based). This value must be greater than or equal to 0 and
   * less than the list length; or specify -1 to retrieve all of the objects.
   * @param number The number of objects to retrieve out of the list, starting at the specified
   * <i>listOffset</i>. This value must be greater than or equal to 0 and less than or equal
   * to the list length. If the <i>listOffset</i> is -1, this parameter is ignored.
   * @return The array of retrieved {@link com.ibm.as400.access.ObjectDescription ObjectDescription} objects.
   * The length of this array may not necessarily be equal to <i>number</i>, depending upon the size
   * of the list on the system, and the specified <i>listOffset</i>.
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

    if (handle_ == null) load();  // this sets the length_ variable

    if (length_ == 0 || (number == 0 && listOffset != -1)) {
      return new ObjectDescription[0];
    }

    if (listOffset == -1)
    {
      number = length_;  // request entire list
      listOffset = 0;    // ... starting at beginning of list
    }
    else if (listOffset >= length_)
    {
      if (Trace.traceOn_)
        Trace.log(Trace.WARNING, "Value of parameter 'listOffset' is beyond end of list:", listOffset + " (list length: " + length_ + ")");

      return new ObjectDescription[0];
    }
    else if (listOffset + number > length_)
    {
      number = length_ - listOffset;
    }

    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    
    // Use recLen_ from load()'s list information to calculate receiver length needed @A2A
    int len = number*recLen_; //@A2C

    // The 'List information' structure from call to QGYGTLE.
    // This value will be set by retrieveListEntries().
    Object[] listInfoContainer = new Object[1];  // initialized to null

    // Retrieve the entries in the list that was built by the most recent load().
    byte[] data = ListUtilities.retrieveListEntries(system_, handle_, len, number, listOffset, listInfoContainer);

    byte[] listInfo = (byte[])listInfoContainer[0];
    if (listInfo == null || listInfo.length == 0) {
      return new ObjectDescription[0];
      // Shouldn't have to do this, but this API doesn't like certain empty libraries for some reason.
    }
    //int totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
    int recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    int recordLength = BinaryConverter.byteArrayToInt(listInfo, 12);
    
    // Deleting following code, because there will not be a need to call a second  @A2A
    // time as we should always have enough room for the receiver now.             @A2A
    /*  Start of deleted code which calls QGYGTLE a second time -----------------  @A2D
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

    End of deleted code which calls QGYGTLE a second time -----------------  @A2D */

    ObjectDescription[] objects = new ObjectDescription[recordsReturned];
    int offset = 0;
    for (int i=0; i<recordsReturned; ++i)
    {
      String objectName = conv.byteArrayToString(data, offset, 10).trim();
      String objectLibrary = conv.byteArrayToString(data, offset+10, 10).trim();
      String objectType = conv.byteArrayToString(data, offset+20, 10).trim().substring(1); // strip off leading *
      byte infoStatus = data[offset+30];
      int numFields = BinaryConverter.byteArrayToInt(data, offset+32);
      if(aspDeviceName_ != null)	// @550A
    	  objects[i] = new ObjectDescription(system_, objectLibrary, objectName, objectType, infoStatus, aspDeviceName_, aspSearchType_);	// @550A
      else	// @550A do what we always have
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
            Trace.log(Trace.ERROR, "Unknown key type for key "+key+": "+type);
            throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR, key+": "+type,null);
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
   * Returns the object type used to filter this list.  (For example: *LIB, *FILE, *OUTQ, etc)
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
   * @exception ServerStartupException          If the host server cannot be started.
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
    ProgramParameter[] parms = new ProgramParameter[(aspDeviceName_ == null) ? 12 : 15];	// @550C changed to allow asp control
    parms[0] = new ProgramParameter(1); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // length of receiver variable
    parms[2] = new ProgramParameter(ListUtilities.LIST_INFO_LENGTH); // list information

    // Number of records to return.
    // Special value '-1' indicates that "all records are built synchronously in the list".
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(-1));
    
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
    parms[11] = new ErrorCodeParameter();
    
    if(parms.length == 15)	// @550A add job identification info, format of job identification info, and asp control
    {
    	parms[12] = new ProgramParameter(text10.toBytes("*"));	// @550A
    	parms[13] = new ProgramParameter(conv.stringToByteArray("JIDF0000"));	// @550A
    	//Construct the ASP Control Format
    	byte[] controlFormat = new byte[24];			// @550A
    	System.arraycopy(BinaryConverter.intToByteArray(24), 0, controlFormat, 0, 4);	// @550A
    	for(int i=4; i<controlFormat.length; i++) controlFormat[i] = 0x40;	// @550A blank pad characters
    	conv.stringToByteArray(aspDeviceName_, controlFormat, 4);			// @550A
    	if(!aspDeviceName_.equals(ASP_NAME_ALL) &&
    	   !aspDeviceName_.equals(ASP_NAME_SYSBAS) &&
    	   !aspDeviceName_.equals(ASP_NAME_CURASPGRP) &&
    	   !aspDeviceName_.equals(ASP_NAME_ALLAVL))	// @550A  if the device name is one of the special values, then blanks should be used for the search type
    	{
    		conv.stringToByteArray(aspSearchType_, controlFormat, 14);	// @550A specify the search type if device name is not a special value
    	}
    	parms[14] = new ProgramParameter(controlFormat);		// @550A
    }

    // Call the program
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLOBJ.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    isConnected_ = true;

    // List information returned
    byte[] listInformation = parms[2].getOutputData();
    handle_ = new byte[4];
    System.arraycopy(listInformation, 8, handle_, 0, 4);

    // Wait for the list-building to complete.
    listInformation = ListUtilities.waitForListToComplete(system_, handle_, listInformation);

    length_ = BinaryConverter.byteArrayToInt(listInformation, 0);

    // Obtain the recordLength from the QGYGTLE() listinfo output  @A2A
    recLen_ = BinaryConverter.byteArrayToInt(listInformation, 12); //@A2A
    if (recLen_ <= 0)                                            //@A2A
    {                                                            //@A2A
        Trace.log(Trace.ERROR, "invalid record length", recLen_);
        throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, 
                                         recLen_);
    }                                                            //@A2A

    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Loaded object list with length = "+length_+" and handle: ", handle_);
    }
  }


  // Resets the handle to indicate we should close the list the next time
  // we do something, usually as a result of one of the selection criteria
  // being changed since that should build a new list on the system.
  private synchronized void resetHandle()
  {
    if (handleToClose_ == null) handleToClose_ = handle_; // Close the old list on the next load
    handle_ = null;
  }

  //@550A
  /**
   * Specifies the type of the search when a specific auxiliary storage pool device name
   * is specified for the ASP device name.  
   * @param aspSearchType The type of search to be used.  One of the following values may be specified:
   * <ul>
   * <li>{@link #ASP_SEARCH_TYPE_ASP ASP_SEARCH_TYPE_ASP} - Only the single ASP named will be searched.</li>
   * <li>{@link #ASP_SEARCH_TYPE_ASPGRP ASP_SEARCH_TYPE_ASPGRP} - All ASPs in the auxiliary storage pool
   * group named will be searched.</li>
   * </ul>
   * The default value is {@link #ASP_SEARCH_TYPE_ASP ASP_SEARCH_TYPE_ASP}. 
   * @exception ExtendedIllegalArgumentException if an invalid search type is specified.  
   */
  public void setAspSearchType(String aspSearchType) throws ExtendedIllegalArgumentException{
	  if (aspSearchType == null) throw new NullPointerException("aspSearchType");
	  if (!aspSearchType.equals(ASP_SEARCH_TYPE_ASP) &&
	        !aspSearchType.equals(ASP_SEARCH_TYPE_ASPGRP))
	  {
		  throw new ExtendedIllegalArgumentException("aspSearchType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	  }
	  aspSearchType_ = aspSearchType;
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


  /**
   Closes the list on the system when this object is garbage collected.
   **/
  protected void finalize() throws Throwable
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Finalize method for object list invoked.");
    if (handle_ != null) try { close(); } catch (Throwable t) {}
    super.finalize();
  }

}
