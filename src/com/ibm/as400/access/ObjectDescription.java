///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ObjectDescription.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.access;

import java.beans.*;
import java.io.*;
import java.util.*;

/**
 * Represents a server QSYS object and its attributes.
 * ObjectDescription objects can be constructed individually, or generated
 * from an {@link com.ibm.as400.access.ObjectList ObjectList}. An object's
 * attributes can be retrieved by calling {@link #getValue getValue()} and
 * passing one of the integer attribute constants defined in this class.
 * <P>
 * Implementation note:
 * This class internally uses the Retrieve Object Description (QUSROBJD) API.
 *
 * @see com.ibm.as400.access.ObjectList
**/
public class ObjectDescription
{  
  
  /**
   * Object attribute representing whether the object can be
   * changed by the Change Object Description (QLICOBJD) API.
   * <P>Type: {@link java.lang.Boolean Boolean}
  **/
  public static final int ALLOW_CHANGE_BY_PROGRAM = 308;
  
  /**
   * Object attribute representing the identifier of the
   * authorized program analysis report (APAR) that caused
   * this object to be replaced. This field is null if
   * the object did not change because of an APAR.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int APAR = 413;
  
  /**
   * Constant indicating the name of the auxiliary storage pool
   * device is not known.
   * @see #LIBRARY_ASP_DEVICE_NAME
   * @see #OBJECT_ASP_DEVICE_NAME
  **/
  public static final String ASP_NAME_UNKNOWN = "*N";
  
  /**
   * Object attribute representing the type of auditing for the object.
   * Valid values are:
   * <UL>
   * <LI>{@link #AUDITING_NONE AUDITING_NONE} - No auditing occurs for
   * the object when it is read or changed, regardless of the user
   * who is accessing the object.
   * <LI>{@link #AUDITING_USER_PROFILE AUDITING_USER_PROFILE} - Audit the
   * object only if the user accessing the object is being audited.
   * <LI>{@link #AUDITING_CHANGE AUDITING_CHANGE} - Audit all
   * change access to this object by all users on the system.
   * <LI>{@link #AUDITING_ALL AUDITING_ALL} - Audit all access to the object
   * by all users on the system. All access is defined as a read or
   * change operation.
   * </UL>
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int AUDITING = 310;
  
  /**
   * Constant representing the auditing value *ALL.
   * @see #AUDITING
  **/
  public static final String AUDITING_ALL = "*ALL";

  /**
   * Constant representing the auditing value *CHANGE.
   * @see #AUDITING
  **/
  public static final String AUDITING_CHANGE = "*CHANGE";
  
  /**
   * Constant representing the auditing value *NONE.
   * @see #AUDITING
  **/
  public static final String AUDITING_NONE = "*NONE";
  
  /**
   * Constant representing the auditing value *USRPRF.
   * @see #AUDITING
  **/
  public static final String AUDITING_USER_PROFILE = "*USRPRF";
  
  /**
   * Object attribute representing the time at which
   * the object was last changed.
   * <P>Type: {@link java.util.Date java.util.Date}
  **/
  public static final int CHANGE_DATE = 305;

  /**
   * Object attribute representing whether the object has been
   * changed by the Change Object Description (QLICOBJD) API.
   * <P>Type: {@link java.lang.Boolean Boolean}
  **/
  public static final int CHANGED_BY_PROGRAM = 309;

  // We dont' use these, but there's nothing to prevent the user
  // from manually entering an integer attribute equal to one of
  // these values.
  static final int COMBINATION_200 = 200;
  static final int COMBINATION_300 = 300;
  static final int COMBINATION_400 = 400;
  static final int COMBINATION_500 = 500;
  static final int COMBINATION_600 = 600;
  static final int COMBINATION_700 = 700;

  
  /**
   * Object attribute representing the licensed program
   * of the compiler used to generate this object.
   * The field will be null if the program was not compiled.
   * <P>Type: {@link com.ibm.as400.access.Product com.ibm.as400.access.Product}
  **/
  public static final int COMPILER = 408;
    
  /**
   * Object attribute representing the compression status of
   * the object. Valid values are:
   * <UL>
   * <LI>{@link #COMPRESSION_YES COMPRESSION_YES} - Compressed.
   * <LI>{@link #COMPRESSION_NO COMPRESSION_NO} - Permanently decompressed
   * and compressible.
   * <LI>{@link #COMPRESSION_INELIGIBLE COMPRESSION_INELIGIBLE} - Permanently
   * decompressed and not compressible.
   * <LI>{@link #COMPRESSION_TEMPORARY COMPRESSION_TEMPORARY} - Temporarily
   * decompressed.
   * <LI>{@link #COMPRESSION_STORAGE_FREED COMPRESSION_STORAGE_FREED} - Saved with
   * storage freed. The compression status cannot be determined.
   * </UL>
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int COMPRESSION = 307;
  
  /**
   * Constant representing the compression status "X".
   * @see #COMPRESSION
  **/
  public static final String COMPRESSION_INELIGIBLE = "X";
  
  /**
   * Constant representing the compression status "N".
   * @see #COMPRESSION
  **/
  public static final String COMPRESSION_NO = "N";
  
  /**
   * Constant representing the compression status "F".
   * @see #COMPRESSION
  **/
  public static final String COMPRESSION_STORAGE_FREED = "F";

  /**
   * Constant representing the compression status "T".
   * @see #COMPRESSION
  **/
  public static final String COMPRESSION_TEMPORARY = "T";
  
  /**
   * Constant representing the compression status "Y".
   * @see #COMPRESSION
  **/
  public static final String COMPRESSION_YES = "Y";
  
  /**
   * Object attribute representing the time at which the
   * object was created.
   * <P>Type: {@link java.util.Date java.util.Date}
  **/
  public static final int CREATION_DATE = 304;
  
  /**
   * Object attribute representing the name of the system on
   * which the object was created.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int CREATOR_SYSTEM = 406;
  
  /**
   * Object attribute representing the name of the user
   * that created the object.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int CREATOR_USER_PROFILE = 405;
  
  /**
   * Constant representing the value *CURLIB.
  **/
  public static final String CURRENT_LIBRARY = "*CURLIB";

  /**
   * Object attribute representing the number of days the
   * object was used, or 0 if the object does not have
   * a last-used date.
   * <P>Type: {@link java.lang.Integer Integer}
   * @see #LAST_USED_DATE
  **/
  public static final int DAYS_USED = 603;
  
  /**
   * Object attribute representing whether the object
   * has a digital signature.
   * <P>Type: {@link java.lang.Boolean Boolean}
   * @see #DIGITALLY_SIGNED_TRUSTED
   * @see #DIGITALLY_SIGNED_MULTIPLE
  **/
  public static final int DIGITALLY_SIGNED = 311;
  
  /**
   * Object attribute representing whether the object has
   * more than one digital signature.
   * <P>Type: {@link java.lang.Boolean Boolean}
   * @see #DIGITALLY_SIGNED
   * @see #DIGITALLY_SIGNED_TRUSTED
  **/
  public static final int DIGITALLY_SIGNED_MULTIPLE = 313;
  
  /**
   * Object attribute representing whether the object
   * is signed by a source that is trusted by the system.
   * <P>Type: {@link java.lang.Boolean Boolean}
   * @see #DIGITALLY_SIGNED
   * @see #DIGITALLY_SIGNED_MULTIPLE
  **/
  public static final int DIGITALLY_SIGNED_TRUSTED = 312;
  
  /**
   * Object attribute representing the domain that contains
   * the object. Valid values are:
   * <UL>
   * <LI>{@link #DOMAIN_USER DOMAIN_USER} - The object is in the user domain.
   * <LI>{@link #DOMAIN_SYSTEM DOMAIN_SYSTEM} - The object is in the system domain.
   * </UL>
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int DOMAIN = 303;
  
  /**
   * Constant representing the object domain value of "*S".
   * @see #DOMAIN
  **/
  public static final String DOMAIN_SYSTEM = "*S";

  /**
   * Constant representing the object domain value of "*U".
   * @see #DOMAIN
  **/
  public static final String DOMAIN_USER = "*U";
  
  /**
   * Object attribute representing the extended attribute
   * that further describes the object, such as a program or file type.
   * For example, an object type of *PGM may have a value
   * of RPG (RPG program) or CLP (CL program); an object type
   * of *FILE may have a value of PF (physical file), LF
   * (logical file), DSPF (display file), SAVF (save file), and
   * so on.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int EXTENDED_ATTRIBUTE = 202;
  
  
  // We don't use this, as it is only valid when retrieving attributes
  // via ObjectList.  Use getStatus() instead.
  static final int INFORMATION_STATUS = 201;
  
  
  /**
   * Object attribute representing whether the object is
   * currently being journaled or not.
   * <P>Type: {@link java.lang.Boolean Boolean}
  **/
  public static final int JOURNAL_STATUS = 513;

  /**
   * Object attribute representing the fully-qualified integrated
   * file system path name of the journal. This field is blank if the object
   * has never been journaled.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int JOURNAL = 514;
  static final int JOURNAL_LIBRARY = 515;

  /**
   * Object attribute representing the type of images that are
   * written to the journal receiver for updates to the object.
   * This field is true if both before and after images are
   * generated for changes to the object.
   * This field is false if only after images are generated
   * for changes to the object, or, if the object has never
   * been journaled.
   * <P>Type: {@link java.lang.Boolean Boolean}
  **/
  public static final int JOURNAL_IMAGES = 516;
  
  /**
   * Object attribute representing whether journal entries
   * to be omitted are journaled.
   * This field is true if no
   * entries are omitted; that is, all entries are journaled
   * including open and close entries that would normally be
   * omitted.
   * This field is false if
   * open and close operations do not generate open and close
   * journal entries, or, if this object has never been journaled.
   * <P>Type: {@link java.lang.Boolean Boolean}
  **/
  public static final int JOURNAL_OMITTED_ENTRIES = 517;
  
  /**
   * Object attribute representing the time at which journaling
   * for the object was last started. This field will contain
   * Date value of 0 ms if the object has never been journaled.
   * <P>Type: {@link java.util.Date java.util.Date}
  **/
  public static final int JOURNAL_START_DATE = 518;
  
  /**
   * Object attribute representing the time at which the
   * object was last used. This field will contain a Date
   * value of 0 ms if the object has no last-used date.
   * <P>Type: {@link java.util.Date java.util.Date}
  **/
  public static final int LAST_USED_DATE = 601;
  
  /**
   * Object attribute representing the library of the object.
   * <P>Type: {@link java.lang.String String}
   * @see #getLibrary
  **/
  public static final int LIBRARY = 10001;
  
  /**
   * Object attribute representing the name of the auxiliary
   * storage pool (ASP) device where storage is allocated
   * for the library containing the object. Special values
   * include:
   * <UL>
   * <LI>{@link #SYSTEM_OR_BASIC_ASP SYSTEM_OR_BASIC_ASP} - System
   * ASP (1) or defined basic user ASPs (2-32).
   * <LI>{@link #ASP_NAME_UNKNOWN ASP_NAME_UNKNOWN} - The name of
   * the ASP device cannot be determined.
   * </UL>
   * <P>Type: {@link java.lang.String String}
   * @see #LIBRARY_ASP_NUMBER
   * @see #OBJECT_ASP_NUMBER
   * @see #OBJECT_ASP_DEVICE_NAME
  **/
  public static final int LIBRARY_ASP_DEVICE_NAME = 606;
  
  /**
   * Object attribute representing the number of the auxiliary
   * storage pool (ASP) where storage is allocated for the library
   * containing the object. Valid values are:
   * <UL>
   * <LI>1: System ASP
   * <LI>2-32: Basic user ASP
   * <LI>33-255: Primary or secondary ASP
   * </UL>
   * <P>Type: {@link java.lang.Integer Integer}
   * @see #LIBRARY_ASP_DEVICE_NAME
   * @see #OBJECT_ASP_NUMBER
   * @see #OBJECT_ASP_DEVICE_NAME
  **/
  public static final int LIBRARY_ASP_NUMBER = 314;
  
  /**
   * Constant representing the value *LIBL.
  **/
  public static final String LIBRARY_LIST = "*LIBL";

  /**
   * Object attribute representing the licensed program of the object.
   * This field is null if the object does not belong to a licensed
   * program.
   * <P>Type: {@link com.ibm.as400.access.Product com.ibm.as400.access.Product}
  **/
  public static final int LICENSED_PROGRAM = 411;
  
  /**
   * Object attribute representing the name of the object.
   * <P>Type: {@link java.lang.String String}
   * @see #getName
  **/
  public static final int NAME = 10000;
  
  /**
   * Object attribute representing the name of the auxiliary
   * storage pool (ASP) device where storage is allocated
   * for the object. Special values include:
   * <UL>
   * <LI>{@link #SYSTEM_OR_BASIC_ASP SYSTEM_OR_BASIC_ASP} - System
   * ASP (1) or defined basic user ASPs (2-32).
   * <LI>{@link #ASP_NAME_UNKNOWN ASP_NAME_UNKNOWN} - The name of
   * the ASP device cannot be determined.
   * </UL>
   * <P>Type: {@link java.lang.String String}
   * @see #OBJECT_ASP_NUMBER
   * @see #LIBRARY_ASP_DEVICE_NAME
   * @see #LIBRARY_ASP_NUMBER
  **/
  public static final int OBJECT_ASP_DEVICE_NAME = 605;
  
  /**
   * Object attribute representing the number of the auxiliary
   * storage pool (ASP) where storage is allocated for the object.
   * Valid values are:
   * <UL>
   * <LI>1: System ASP
   * <LI>2-32: Basic user ASP
   * <LI>33-255: Primary or secondary ASP
   * </UL>
   * <P>Type: {@link java.lang.Integer Integer}
   * @see #OBJECT_ASP_DEVICE_NAME
   * @see #LIBRARY_ASP_DEVICE_NAME
   * @see #LIBRARY_ASP_NUMBER
  **/
  public static final int OBJECT_ASP_NUMBER = 301;
  
  /**
   * Object attribute representing the object control level
   * for the created object.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int OBJECT_LEVEL = 409;
  
  /**
   * Object attribute representing the size of the object
   * in bytes.
   * <P>Type: {@link java.lang.Long Long}
  **/
  public static final int OBJECT_SIZE = 701;
  static final int OBJECT_SIZE_MULTIPLIER = 702;

  /**
   * Object attribute representing the order in which the library
   * appears in the entire library list. If the library is in the
   * list more than once, the order of the first occurrence of
   * the library is returned. If the library is not in the library
   * list, 0 is returned.
   * <P>Note that this field can only be retrieved via the ObjectList
   * class. If {@link #getValue getValue()} is called and this field was not retrieved
   * via an ObjectList, -1 is returned.
   * <P>Type: {@link java.lang.Integer Integer}
  **/
  public static final int ORDER_IN_LIBRARY_LIST = 205; // ObjectList only
  
  /**
   * Object attribute representing whether the object overflowed
   * the auxiliary storage pool (ASP).
   * Note that it is not possible for objects that reside in the system ASP,
   * a primary ASP, or a secondary ASP to overflow the ASP.
   * <P>Type: {@link java.lang.Boolean Boolean}
  **/
  public static final int OVERFLOWED_ASP = 703;

  /**
   * Object attribute representing the name of the object
   * owner's user profile.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int OWNER = 302;

  /**
   * Object attribute representing the name of the primary group profile
   * for the object. Special values include:
   * <UL>
   * <LI>{@link #PRIMARY_GROUP_NONE PRIMARY_GROUP_NONE} - No primary group
   * exists for the object.
   * </UL>
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int PRIMARY_GROUP = 414;
  
  /**
   * Constant representing a primary group of *NONE.
   * @see #PRIMARY_GROUP
  **/
  public static final String PRIMARY_GROUP_NONE = "*NONE";
  
  /**
   * Object attribute representing the number of the program temporary
   * fix (PTF) number that caused this object to be replaced. This field
   * is blank if the object was not changed because of a PTF.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int PTF = 412;
  
  /**
   * Object attribute representing the time the days-used count
   * was last reset to 0. If the days-used count has never been
   * reset, this field contains a Date value of 0 ms.
   * <P>Type: {@link java.util.Date java.util.Date}
   * @see #DAYS_USED
  **/
  public static final int RESET_DATE = 602;
  
  /**
   * Object attribute representing the time at which the
   * object was restored. This field will contain a Date
   * value of 0 ms if the object has never been restored.
   * <P>Type: {@link java.util.Date java.util.Date}
  **/
  public static final int RESTORE_DATE = 502;

  /**
   * Object attribute representing the time the object was last
   * saved when the SAVACT(*LIB, *SYSDFN, or *YES) save
   * operation was specified. If the object has never been saved,
   * or if SAVACT(*NO) was specified on the last save operation for
   * the object, this field contains a Date value of 0 ms.
   * <P>Type: {@link java.util.Date java.util.Date}
  **/
  public static final int SAVE_ACTIVE_DATE = 512;
  
  /**
   * Object attribute representing the command used to save
   * the object. This field is blank if the object was not saved.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int SAVE_COMMAND = 506;
  
  /**
   * Object attribute representing the time at which the
   * object was saved. This field will contain a Date
   * value of 0 ms if the object has never been saved.
   * <P>Type: {@link java.util.Date java.util.Date}
  **/
  public static final int SAVE_DATE = 501;
  
  /**
   * Object attribute representing the type of device to which the
   * object was last saved. Possible values are:
   * <UL>
   * <LI>{@link #SAVE_DEVICE_SAVE_FILE SAVE_DEVICE_SAVE_FILE} - The last save
   * operation was to a save file.
   * <LI>{@link #SAVE_DEVICE_DISKETTE SAVE_DEVICE_DISKETTE} - The last save
   * operation was to diskette.
   * <LI>{@link #SAVE_DEVICE_TAPE SAVE_DEVICE_TAPE} - The last save
   * operation was to tape.
   * <LI>{@link #SAVE_DEVICE_OPTICAL SAVE_DEVICE_OPTICAL} - The last save
   * operation was to optical.
   * <LI>{@link #SAVE_DEVICE_NOT_SAVED SAVE_DEVICE_NOT_SAVED} - The object
   * was not saved.
   * </UL>
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int SAVE_DEVICE = 508;
  
  /**
   * Constant representing a save device of *DKT.
   * @see #SAVE_DEVICE
  **/
  public static final String SAVE_DEVICE_DISKETTE = "*DKT";
  
  /**
   * Constant representing no save device (blank).
   * @see #SAVE_DEVICE
  **/
  public static final String SAVE_DEVICE_NOT_SAVED = "";

  /**
   * Constant representing a save device of *OPT.
   * @see #SAVE_DEVICE
  **/
  public static final String SAVE_DEVICE_OPTICAL = "*OPT";
  
  /**
   * Constant representing a save device of *SAVF.
   * @see #SAVE_DEVICE
  **/
  public static final String SAVE_DEVICE_SAVE_FILE = "*SAVF";
  
  /**
   * Constant representing a save device of *TAP.
   * @see #SAVE_DEVICE
  **/
  public static final String SAVE_DEVICE_TAPE = "*TAP";
  
  /**
   * Object attribute representing the fully-qualified integrated file
   * system path name of the save file to which the object was saved.
   * This field is blank if the object was not saved to a save file.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int SAVE_FILE = 509;
  static final int SAVE_FILE_LIBRARY = 510;

  /**
   * Object attribute representing the file label used when the object
   * was saved. This field is blank if the object was not saved to tape,
   * diskette, or optical. This field corresponds to the LABEL parameter
   * on the command used to save the object.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int SAVE_LABEL = 511;
  
  /**
   * Object attribute representing the tape sequence number
   * assigned when the object was saved on tape, or 0
   * if the object was not saved.
   * <P>Type: {@link java.lang.Integer Integer}
  **/
  public static final int SAVE_SEQUENCE_NUMBER = 505;
  
  /**
   * Object attribute representing the size of the object in
   * bytes of storage at the time of the last save operation,
   * or 0 if the object was not saved.
   * <P>Type: {@link java.lang.Long Long}
  **/
  public static final int SAVE_SIZE = 503;
  static final int SAVE_SIZE_MULTIPLIER = 504;

  /**
   * Object attribute representing the tape, diskette, or optical
   * volumes that are used for saving the object. This field returns
   * a maximum of ten 6-character volumes. Each volume is separated
   * by a single character.
   * <P>
   * If the object was saved in parallel format, the separator character
   * contains a 2 before the first volume in the second media file, a 3
   * before the third media file, and so on, up to a 0 before the tenth
   * media file.  Otherwise, the separator characters are blank. If more
   * than ten volumes are used and the object was saved in serial format,
   * 1 is returned in the 71st character of this field.
   * <P>
   * The field is blank if the object was last saved to a save file or
   * if it was never saved.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int SAVE_VOLUME_ID = 507;

  /**
   * Object attribute representing the fully-qualified integrated
   * file system path name of the source file that was used to
   * create the object. This field is blank if no source file was
   * used to create the object.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int SOURCE_FILE = 401;
  static final int SOURCE_FILE_LIBRARY = 402;
  static final int SOURCE_FILE_MEMBER = 403;

  /**
   * Object attribute representing the time the member in the source
   * file was last updated. This field is null if no source file
   * created the object.
   * <P>Type: {@link java.util.Date java.util.Date}
   * @see #SOURCE_FILE
  **/
  public static final int SOURCE_FILE_UPDATED_DATE = 404;
  
  /** 
   * Constant representing a status of 'D' for the information in this
   * object when generated by an ObjectList.
   * @see #getStatus
  **/
  public static final byte STATUS_DAMAGED = (byte)0xC4; // EBCDIC 'D'
  
  /** 
   * Constant representing a status of 'L' for the information in this
   * object when generated by an ObjectList.
   * @see #getStatus
  **/
  public static final byte STATUS_LOCKED = (byte)0xD3; // EBCDIC 'L'
  
  /** 
   * Constant representing a status of 'A' for the information in this
   * object when generated by an ObjectList.
   * @see #getStatus
  **/
  public static final byte STATUS_NO_AUTHORITY = (byte)0xC1; // EBCDIC 'A'
  
  /** 
   * Constant representing a status of ' ' for the information in this
   * object when generated by an ObjectList.
   * @see #getStatus
  **/
  public static final byte STATUS_NO_ERRORS = (byte)0x40; // EBCDIC ' '
  
  /** 
   * Constant representing a status of 'P' for the information in this
   * object when generated by an ObjectList.
   * @see #getStatus
  **/
  public static final byte STATUS_PARTIALLY_DAMAGED = (byte)0xD7; // EBCDIC 'P'
  
  /** 
   * Constant representing an unknown status for the information in this
   * object.
   * @see #getStatus
  **/
  public static final byte STATUS_UNKNOWN = (byte)0x00; // We didn't retrieve any attributes.

  /**
   * Object attribute representing the storage status of the object.
   * Possible values are:
   * <UL>
   * <LI>{@link #STORAGE_STATUS_FREE STORAGE_STATUS_FREE} - Indicates
   * the object data is freed and the object is suspended.
   * <LI>{@link #STORAGE_STATUS_KEEP STORAGE_STATUS_KEEP} - Indicates
   * the object data is not freed and the object is not suspended.
   * </UL>
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int STORAGE_STATUS = 306;
  
  /**
   * Constant representing a storage status of *FREE.
   * @see #STORAGE_STATUS
  **/
  public static final String STORAGE_STATUS_FREE = "*FREE";
  
  /**
   * Constant representing a storage status of *FREE.
   * @see #STORAGE_STATUS
  **/
  public static final String STORAGE_STATUS_KEEP = "*KEEP";

  /**
   * Object attribute representing the level of the operating system
   * when the object was created. This field has the format <b>VvvRrrMmm</b>
   * where:
   * <UL>
   * <LI><b>Vvv</b> - The character 'V' followed by a 2-character version number.
   * <LI><b>Rrr</b> - The character 'R' followed by a 2-character release level.
   * <LI><b>Mmm</b> - The character 'M' followed by a 2-character modification level.
   * </UL>
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int SYSTEM_LEVEL = 407;
  
  /**
   * Constant indicating the auxiliary storage pool device
   * is the system ASP or a defined basic user ASP.
   * @see #LIBRARY_ASP_DEVICE_NAME
   * @see #OBJECT_ASP_DEVICE_NAME
  **/
  public static final String SYSTEM_OR_BASIC_ASP = "*SYSBAS";

  /**
   * Object attribute representing the text description of the object.
   * This field is blank if no text description is specified.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int TEXT_DESCRIPTION = 203;
  
  /**
   * Object attribute representing the type of the object.
   * <P>Type: {@link java.lang.String String}
   * @see #getType
  **/
  public static final int TYPE = 10002;

  /**
   * Object attribute representing whether the object usage
   * information is updated for this object type.
   * <P>Type: {@link java.lang.Boolean Boolean}
  **/
  public static final int USAGE_INFO_UPDATED = 604;

  /**
   * Object attribute representing whether the user program was changed.
   * This field is true if the user changed the object; false if the object
   * was not changed by the user.
   * <P>Type: {@link java.lang.Boolean Boolean}
  **/
  public static final int USER_CHANGED = 410;
  
  /**
   * Object attribute representing the characteristic of the object
   * type. This field is set by the user while using the Change Object
   * Description (QLICOBJD) API.
   * <P>Type: {@link java.lang.String String}
  **/
  public static final int USER_DEFINED_ATTRIBUTE = 204;



  
  // Holds the lengths for all of the keys.
  static final IntegerHashtable keyLengths_ = new IntegerHashtable();
  static
  {
    keyLengths_.put(COMBINATION_200, 80);
    keyLengths_.put(INFORMATION_STATUS, 1);
    keyLengths_.put(EXTENDED_ATTRIBUTE, 10);
    keyLengths_.put(TEXT_DESCRIPTION, 50);
    keyLengths_.put(USER_DEFINED_ATTRIBUTE, 10);
    keyLengths_.put(ORDER_IN_LIBRARY_LIST, 4);
    keyLengths_.put(COMBINATION_300, 144);
    keyLengths_.put(OBJECT_ASP_NUMBER, 4);
    keyLengths_.put(OWNER, 10);
    keyLengths_.put(DOMAIN, 2);
    keyLengths_.put(CREATION_DATE, 8);
    keyLengths_.put(CHANGE_DATE, 8);
    keyLengths_.put(STORAGE_STATUS, 10);
    keyLengths_.put(COMPRESSION, 1);
    keyLengths_.put(ALLOW_CHANGE_BY_PROGRAM, 1);
    keyLengths_.put(CHANGED_BY_PROGRAM, 1);
    keyLengths_.put(AUDITING, 10);
    keyLengths_.put(DIGITALLY_SIGNED, 1);
    keyLengths_.put(DIGITALLY_SIGNED_TRUSTED, 1);
    keyLengths_.put(DIGITALLY_SIGNED_MULTIPLE, 1);
    keyLengths_.put(LIBRARY_ASP_NUMBER, 4);
    keyLengths_.put(COMBINATION_400, 296);
    keyLengths_.put(SOURCE_FILE, 10);
    keyLengths_.put(SOURCE_FILE_LIBRARY, 10);
    keyLengths_.put(SOURCE_FILE_MEMBER, 10);
    keyLengths_.put(SOURCE_FILE_UPDATED_DATE, 13);
    keyLengths_.put(CREATOR_USER_PROFILE, 10);
    keyLengths_.put(CREATOR_SYSTEM, 8);
    keyLengths_.put(SYSTEM_LEVEL, 9);
    keyLengths_.put(COMPILER, 16);
    keyLengths_.put(OBJECT_LEVEL, 8);
    keyLengths_.put(USER_CHANGED, 1);
    keyLengths_.put(LICENSED_PROGRAM, 16);
    keyLengths_.put(PTF, 10);
    keyLengths_.put(APAR, 10);
    keyLengths_.put(PRIMARY_GROUP, 10);
    keyLengths_.put(COMBINATION_500, 504);
    keyLengths_.put(SAVE_DATE, 8);
    keyLengths_.put(RESTORE_DATE, 8);
    keyLengths_.put(SAVE_SIZE, 4);
    keyLengths_.put(SAVE_SIZE_MULTIPLIER, 4);
    keyLengths_.put(SAVE_SEQUENCE_NUMBER, 4);
    keyLengths_.put(SAVE_COMMAND, 10);
    keyLengths_.put(SAVE_VOLUME_ID, 71);
    keyLengths_.put(SAVE_DEVICE, 10);
    keyLengths_.put(SAVE_FILE, 10);
    keyLengths_.put(SAVE_FILE_LIBRARY, 10);
    keyLengths_.put(SAVE_LABEL, 17);
    keyLengths_.put(SAVE_ACTIVE_DATE, 8);
    keyLengths_.put(JOURNAL_STATUS, 1);
    keyLengths_.put(JOURNAL, 10);
    keyLengths_.put(JOURNAL_LIBRARY, 10);
    keyLengths_.put(JOURNAL_IMAGES, 1);
    keyLengths_.put(JOURNAL_OMITTED_ENTRIES, 1);
    keyLengths_.put(JOURNAL_START_DATE, 8);
    keyLengths_.put(COMBINATION_600, 548);
    keyLengths_.put(LAST_USED_DATE, 8);
    keyLengths_.put(RESET_DATE, 8);
    keyLengths_.put(DAYS_USED, 4);
    keyLengths_.put(USAGE_INFO_UPDATED, 1);
    keyLengths_.put(OBJECT_ASP_DEVICE_NAME, 10);
    keyLengths_.put(LIBRARY_ASP_DEVICE_NAME, 10);
    keyLengths_.put(COMBINATION_700, 560);
    keyLengths_.put(OBJECT_SIZE, 4);
    keyLengths_.put(OBJECT_SIZE_MULTIPLIER, 4);
    keyLengths_.put(OVERFLOWED_ASP, 1);
  }

  private AS400 system_;
  private String library_;
  private String name_;
  private String type_;
  private byte status_;

  private final JobHashtable values_ = new JobHashtable();


  /**
   * Constructs an ObjectDescription given the specified path to the object.
   * @param system The system.
   * @param path The fully-qualified integrated file system path to the object.
   * Special values for the library portion of the path include %CURLIB% and
   * %LIBL%. Only external object types are allowed for the object type.
   * Consider using {@link QSYSObjectPathName QSYSObjectPathName} to compose the fully-qualified path string.
  **/
  public ObjectDescription(AS400 system, String path)
  {
    if (system == null) throw new NullPointerException("system");
    if (path == null) throw new NullPointerException("path");
    system_ = system;
    QSYSObjectPathName parse = new QSYSObjectPathName(path);
    library_ = parse.getLibraryName();
    name_ = parse.getObjectName();
    type_ = parse.getObjectType();
  }

  /**
   * Constructs an ObjectDescription given the object's library, name, and type.
   * @param system The system.
   * @param objectLibrary The library. Special values include:
   * <UL>
   * <LI>{@link #CURRENT_LIBRARY CURRENT_LIBRARY} - The current library is searched for the object.
   * <LI>{@link #LIBRARY_LIST LIBRARY_LIST} - The library list is searched for the object.
   * </UL>
   * @param objectName The name of the object. Wildcards are not allowed.
   * @param objectType The type of the object, e.g. "FILE". Only external object types are allowed.
  **/
  public ObjectDescription(AS400 system, String objectLibrary, String objectName, String objectType)
  {
    if (system == null) throw new NullPointerException("system");
    if (objectLibrary == null) throw new NullPointerException("library");
    if (objectName == null) throw new NullPointerException("name");
    if (objectType == null) throw new NullPointerException("type");
    system_ = system;
    QSYSObjectPathName pn = new QSYSObjectPathName(objectLibrary, objectName, objectType); // Verify valid values.
    library_ = pn.getLibraryName(); // Use the QSYSObjectPathName in case the object names are quoted.
    name_ = pn.getObjectName();
    type_ = pn.getObjectType();
  }

  /**
   * Package scope constructor used by ObjectList.
  **/
  ObjectDescription(AS400 sys, String lib, String name, String type, byte status)
  {
    system_ = sys;
    QSYSObjectPathName pn = new QSYSObjectPathName(lib, name, type); // Verify valid values.
    library_ = pn.getLibraryName(); // Use the QSYSObjectPathName in case the object names are quoted.
    name_ = pn.getObjectName();
    type_ = pn.getObjectType();
    status_ = status;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   * @param obj The reference object with which to compare.
   * @return <tt>true</tt> if this object is the same as the obj argument; <tt>false</tt> otherwise.
   **/
  public boolean equals(Object obj)
  {
    try
    {
      ObjectDescription otherObj = (ObjectDescription)obj;
      if (!otherObj.getSystem().getSystemName().equals(system_.getSystemName())) return false;
      if (!otherObj.getLibrary().equals(library_)) return false;
      if (!otherObj.getName().equals(name_)) return false;
      if (!otherObj.getType().equals(type_)) return false;
      return true;
    }
    catch (Exception e) {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, e);
      return false;
    }
  }

  /**
   * Checks to see if this object currently exists on the system.
   * @return true if the object exists; false if the object or library do not exist.
  **/
  public boolean exists() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    try
    {
      refresh();
    }
    catch(AS400Exception e)
    {
      String id = e.getAS400Message().getID().trim();
      if (id.equalsIgnoreCase("CPF9801") ||  // Object &2 in library &3 not found.
          id.equalsIgnoreCase("CPF9810") ||  // Library &1 not found.
          id.equalsIgnoreCase("CPF9812"))    // File &1 in library &2 not found.
      {
        return false;
      }
      throw e;
    }
    return true;
  }

  /**
   * Helper method used to parse one of the attributes that are dates.
  **/
  private final Date getDate(Object o) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (o instanceof String)
    {
      String str = ((String)o).trim();
      switch (str.length())
      {
        case 7:
          Calendar c = Calendar.getInstance();
          c.clear();
          c.set(Integer.parseInt(str.substring(0,3)) + 1900,// year
                Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                Integer.parseInt(str.substring(5,7)));      // day
          return c.getTime();
        case 13:
          c = Calendar.getInstance();
          c.clear();
          c.set(Integer.parseInt(str.substring(0,3)) + 1900,// year
                Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                Integer.parseInt(str.substring(5,7)),       // day
                Integer.parseInt(str.substring(7,9)),       // hour
                Integer.parseInt(str.substring(9,11)),      // minute
                Integer.parseInt(str.substring(11,13)));    // second
          return c.getTime();
        default:
          return null;
      }
    }
    else if (o instanceof byte[])
    {
      byte[] b = (byte[])o; // system timestamp
      DateTimeConverter dtConv = new DateTimeConverter(system_);
      return dtConv.convert(b, "*DTS");
    }
    return null;
  }

  
  /**
   * Returns the library of this object.
   * @return The object library.
  **/
  public String getLibrary()
  {
    return library_;
  }


  /**
   * Returns the name of this object.
   * @return The object name.
  **/
  public String getName()
  {
    return name_;
  }


  /**
   * Returns the fully-qualified integrated file system path name of this object.
   * @return The object path name.
  **/
  public String getPath()
  {
    return QSYSObjectPathName.toPath(library_, name_, type_);
  }


  /**
   * Returns the status of the information returned in this object
   * if it was generated by an {@link com.ibm.as400.access.ObjectList ObjectList}.
   * @return The status.
   * Possible values are:
   * <UL>
   * <LI>{@link #STATUS_NO_ERRORS STATUS_NO_ERRORS} - The requested
   * attribute information was returned. No errors occurred.
   * <LI>{@link #STATUS_NO_AUTHORITY STATUS_NO_AUTHORITY} - No information
   * was returned because the job did not have the authority specified
   * in the object authorities field of the object.
   * <LI>{@link #STATUS_DAMAGED STATUS_DAMAGED} - The requested attribute
   * information was returned, but the object is damaged and should be
   * recreated as soon as possible.
   * <LI>{@link #STATUS_LOCKED STATUS_LOCKED} - No information was
   * returned because the object is locked.
   * <LI>{@link #STATUS_PARTIALLY_DAMAGED STATUS_PARTIALLY_DAMAGED} - The
   * requested information was returned, but the object is partially
   * damaged.
   * </UL>
   * If two or more conditions occur that include STATUS_NO_AUTHORITY,
   * the status is set to STATUS_NO_AUTHORITY. If the object is locked and either
   * damaged or partially damaged, the status is set to STATUS_LOCKED.
   * <P>If the status returned is either STATUS_NO_AUTHORITY or
   * STATUS_LOCKED, no attribute information will have been collected
   * by ObjectList, so any call to {@link #getValue getValue()} will result in another
   * call to the server, where an exception is likely to occur (because
   * of the lack of authority, for example). Only the object name, library,
   * and type are valid in this case.
  **/
  public byte getStatus()
  {
    return status_;
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
   * Returns the type of this object.
   * @return The object type.
  **/
  public String getType()
  {
    return type_;
  }


  /**
   * Returns the value of the given attribute of this ObjectDescription. If the value is not found,
   * it is retrieved from the system. The values are cached. Call
   * {@link #refresh refresh()} to retrieve all of the known attributes of this object from the system.
   * @param attribute One of the attribute constants.
   * @return The value for the attribute, or null if one was not found.
  **/
  public Object getValue(int attribute) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    switch (attribute)
    {
      case NAME:
        return getName();
      case LIBRARY:
        return getLibrary();
      case TYPE:
        return getType();
    }
    Object o = values_.get(attribute);
    if (o == null)
    {
      // Can only retrieve this via ObjectList
      if (attribute == ORDER_IN_LIBRARY_LIST)
      {
        return new Integer(-1);
      }
      
      retrieve(attribute);
      o = values_.get(attribute);
    }
    switch (attribute)
    {
      case CREATION_DATE:
      case CHANGE_DATE:
      case SOURCE_FILE_UPDATED_DATE:
      case SAVE_DATE:
      case RESTORE_DATE:
      case RESET_DATE:
      case SAVE_ACTIVE_DATE:
      case JOURNAL_START_DATE:
      case LAST_USED_DATE:
        return getDate(o);
      case JOURNAL:
        String name = (String)o;
        if (name.length() > 0)
        {
          String lib = (String)getValue(JOURNAL_LIBRARY);
          return QSYSObjectPathName.toPath(lib, name, "JRN");
        }
        return name;
      case SAVE_FILE:
        String name2 = (String)o;
        if (name2.length() > 0)
        {
          String lib2 = (String)getValue(SAVE_FILE_LIBRARY);
          return QSYSObjectPathName.toPath(lib2, name2, "SAVF");
        }
        return name2;
      case SOURCE_FILE:
        String name3 = (String)o;
        if (name3.length() > 0)
        {
          String lib3 = (String)getValue(SOURCE_FILE_LIBRARY);
          String member = (String)getValue(SOURCE_FILE_MEMBER);
          return QSYSObjectPathName.toPath(lib3, name3, member, "MBR");
        }
        return name3;
      case OBJECT_SIZE:
        long multi = ((Integer)getValue(OBJECT_SIZE_MULTIPLIER)).longValue();
        long size = ((Integer)o).longValue();
        return new Long(multi*size);
      case SAVE_SIZE:
        long multi2 = ((Integer)getValue(SAVE_SIZE_MULTIPLIER)).longValue();
        long size2 = ((Integer)o).longValue();
        return new Long(multi2*size2);
      case LICENSED_PROGRAM:
      case COMPILER:
        // pppppppVvvRrrMmm
        String lpp = (String)o;
        if (lpp.length() == 0) return null;
        else if (lpp.length() < 16)
        {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Disregarding COMPILER attribute value in nonstandard format: " + lpp);
          return null;
        }
        else
        {
          String prodID = lpp.substring(0,7);
          StringBuffer release = new StringBuffer(6);
          release.append('V');
          release.append(Integer.parseInt(lpp.substring(8,2)));
          release.append('R');
          release.append(Integer.parseInt(lpp.substring(11,2)));
          release.append('M');
          release.append(Integer.parseInt(lpp.substring(14,2)));
          return new Product(system_, prodID, Product.PRODUCT_OPTION_BASE, release.toString(), Product.PRODUCT_FEATURE_CODE);
        }
      case USER_CHANGED:
      case ALLOW_CHANGE_BY_PROGRAM:
      case CHANGED_BY_PROGRAM:
      case OVERFLOWED_ASP:
      case JOURNAL_STATUS:
      case JOURNAL_IMAGES:
      case DIGITALLY_SIGNED:
      case DIGITALLY_SIGNED_TRUSTED:
      case DIGITALLY_SIGNED_MULTIPLE:
        return new Boolean (((String)o).charAt(0) == '1');
      case USAGE_INFO_UPDATED:
        return new Boolean (((String)o).charAt(0) == 'Y');
      case JOURNAL_OMITTED_ENTRIES:
        return new Boolean(((String)o).charAt(0) == '0');

      default:
        return o;
    }
  }


  /**
   * Returns the value of the given attribute of this ObjectDescription, as a String. If the value is not found,
   * it is retrieved from the system. The values are cached. Call
   * {@link #refresh refresh()} to retrieve all of the known attributes of this object from the system.
   * @param attribute One of the attribute constants.
   * @return The value for the attribute, or null if one was not found.
  **/
  public String getValueAsString(int attribute) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (attribute == COMPILER)
    {
      // Either "", "pppppppVvvRrrMmm", or a 6- or 7-digit number in the format "wwmmdd" or "wwwmmdd", where ww = the week number (www = week# *10), mm = month and dd = day.
      Object o = values_.get(attribute);
      if (o == null)
      {
        // Can only retrieve this via ObjectList
        if (attribute == ORDER_IN_LIBRARY_LIST)
        {
          return "-1";
        }

        retrieve(attribute);
        o = values_.get(attribute);
      }
      String lpp = (String)o;
      if (lpp.length() == 0) return null;
      else return lpp;
    }
    else
    {
      Object value = getValue(attribute);
      if (value == null) return null;
      else if (value instanceof String) return (String)value;
      else return value.toString();
    }
  }


  private static final int lookupFormat(int attribute)
  {
    switch (attribute)
    {
      case NAME:
      case LIBRARY:
      case TYPE:
      case OBJECT_ASP_NUMBER:
      case OWNER:
      case DOMAIN:
      case CREATION_DATE:
      case CHANGE_DATE:

      case EXTENDED_ATTRIBUTE:
      case TEXT_DESCRIPTION:
      case SOURCE_FILE:
      case SOURCE_FILE_LIBRARY:
      case SOURCE_FILE_MEMBER:
        return 200;
      case SOURCE_FILE_UPDATED_DATE:
      case SAVE_DATE:
      case RESTORE_DATE:
      case CREATOR_USER_PROFILE:
      case CREATOR_SYSTEM:
      case RESET_DATE:
        //case SAVE_SIZE: // moved to 400 format
      case SAVE_SEQUENCE_NUMBER:
      case STORAGE_STATUS:
      case SAVE_COMMAND:
      case SAVE_VOLUME_ID:
      case SAVE_DEVICE:
      case SAVE_FILE:
      case SAVE_FILE_LIBRARY:
      case SAVE_LABEL:
      case SYSTEM_LEVEL:
      case COMPILER:
      case OBJECT_LEVEL:
      case USER_CHANGED:
      case LICENSED_PROGRAM:
      case PTF:
      case APAR:
        return 300;
      case SAVE_SIZE: // if the save size is > 2GB, we need the save size in units & multiplier.
      case LAST_USED_DATE:
      case USAGE_INFO_UPDATED:
      case DAYS_USED:
      case OBJECT_SIZE:
      case OBJECT_SIZE_MULTIPLIER:
      case COMPRESSION:
      case ALLOW_CHANGE_BY_PROGRAM:
      case CHANGED_BY_PROGRAM:
      case USER_DEFINED_ATTRIBUTE:
      case OVERFLOWED_ASP:
      case SAVE_ACTIVE_DATE:
      case AUDITING:
      case PRIMARY_GROUP:
      case JOURNAL_STATUS:
      case JOURNAL:
      case JOURNAL_LIBRARY:
      case JOURNAL_IMAGES:
      case JOURNAL_OMITTED_ENTRIES:
      case JOURNAL_START_DATE:
      case DIGITALLY_SIGNED:
        //case SAVE_SIZE_UNITS:
      case SAVE_SIZE_MULTIPLIER:
      case LIBRARY_ASP_NUMBER:
      case OBJECT_ASP_DEVICE_NAME:
      case LIBRARY_ASP_DEVICE_NAME:
      case DIGITALLY_SIGNED_TRUSTED:
      case DIGITALLY_SIGNED_MULTIPLE:
        return 400;
      default:
        throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }


  private static final int lookupSize(int format)
  {
    switch (format)
    {
      case 100:
        return 90;
      case 200:
        return 180;
      case 300:
        return 460;
      case 400:
        return 598;
      default:
        return 0;
    }
  }


  /**
   * Retrieves all possible attributes of this object from the system.
  **/
  public void refresh() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    retrieve(DIGITALLY_SIGNED_MULTIPLE);
  }
  
  
  /**
   * This method makes the actual program call to retrieve the attributes.
  **/
  private void retrieve(int attribute) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);

    int format = lookupFormat(attribute);
    int size = lookupSize(format);
    ProgramParameter[] parms = new ProgramParameter[5];
    parms[0] = new ProgramParameter(size); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(size)); // length of receiver variable
    parms[2] = new ProgramParameter(conv.stringToByteArray("OBJD0"+format));
    byte[] objectNameAndLibrary = new byte[20];
    AS400Text text10 = new AS400Text(10, ccsid, system_);
    text10.toBytes(name_, objectNameAndLibrary, 0);
    text10.toBytes(library_, objectNameAndLibrary, 10);
    parms[3] = new ProgramParameter(objectNameAndLibrary); // object and library name
    parms[4] = new ProgramParameter(text10.toBytes("*"+type_)); // object type

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QUSROBJD.PGM", parms); // retrieve object description
    // QUSROBJD is thread safe.
    if (!ProgramCall.isThreadSafetyPropertySet()) pc.setThreadSafe(true);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    byte[] data = parms[0].getOutputData();
    int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
    if (bytesReturned < bytesAvailable)
    {
      try
      {
        int newval = size+(bytesAvailable-bytesReturned);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling QUSROBJD again with new parm size: "+newval);
        parms[0].setOutputDataLength(newval);
        parms[1].setInputData(BinaryConverter.intToByteArray(newval));
      }
      catch (PropertyVetoException pve)
      {
      }
      if (!pc.run())
      {
        throw new AS400Exception(pc.getMessageList());
      }
      data = parms[0].getOutputData();
    }
    name_ = conv.byteArrayToString(data, 8, 10).trim();
    type_ = conv.byteArrayToString(data, 28, 10).trim().substring(1); // Strip off leading *
    library_ = conv.byteArrayToString(data, 38, 10).trim();
    set(OBJECT_ASP_NUMBER, BinaryConverter.byteArrayToInt(data, 48));
    set(OWNER, conv.byteArrayToString(data, 52, 10).trim());
    set(DOMAIN, conv.byteArrayToString(data, 62, 2));
    set(CREATION_DATE, conv.byteArrayToString(data, 64, 13)); // date
    set(CHANGE_DATE, conv.byteArrayToString(data, 77, 13)); // date
    if (format >= 200)
    {
      set(EXTENDED_ATTRIBUTE, conv.byteArrayToString(data, 90, 10).trim());
      set(TEXT_DESCRIPTION, conv.byteArrayToString(data, 100, 50).trim());
      set(SOURCE_FILE, conv.byteArrayToString(data, 150, 10).trim());
      set(SOURCE_FILE_LIBRARY, conv.byteArrayToString(data, 160, 10).trim());
      set(SOURCE_FILE_MEMBER, conv.byteArrayToString(data, 170, 10).trim());
    }
    if (format >= 300)
    {
      set(SOURCE_FILE_UPDATED_DATE, conv.byteArrayToString(data, 180, 13)); // date
      set(SAVE_DATE, conv.byteArrayToString(data, 193, 13)); // date
      set(RESTORE_DATE, conv.byteArrayToString(data, 206, 13)); // date
      set(CREATOR_USER_PROFILE, conv.byteArrayToString(data, 219, 10).trim());
      set(CREATOR_SYSTEM, conv.byteArrayToString(data, 229, 8).trim());
      set(RESET_DATE, conv.byteArrayToString(data, 237, 7)); // date-7
      set(SAVE_SIZE, (long)BinaryConverter.byteArrayToInt(data, 244)); // See 400 format section for more on save size.
      set(SAVE_SEQUENCE_NUMBER, BinaryConverter.byteArrayToInt(data, 248));
      set(STORAGE_STATUS, conv.byteArrayToString(data, 252, 10).trim());
      set(SAVE_COMMAND, conv.byteArrayToString(data, 262, 10).trim());
      set(SAVE_VOLUME_ID, conv.byteArrayToString(data, 272, 71));
      set(SAVE_DEVICE, conv.byteArrayToString(data, 343, 10).trim());
      set(SAVE_FILE, conv.byteArrayToString(data, 353, 10).trim());
      set(SAVE_FILE_LIBRARY, conv.byteArrayToString(data, 363, 10).trim());
      set(SAVE_LABEL, conv.byteArrayToString(data, 373, 17).trim());
      set(SYSTEM_LEVEL, conv.byteArrayToString(data, 390, 9));
      set(COMPILER, conv.byteArrayToString(data, 399, 16).trim());
      set(OBJECT_LEVEL, conv.byteArrayToString(data, 415, 8).trim());
      set(USER_CHANGED, conv.byteArrayToString(data, 423, 1)); // '1' if the user changed the object, '0' if not.
      set(LICENSED_PROGRAM, conv.byteArrayToString(data, 424, 16).trim());
      set(PTF, conv.byteArrayToString(data, 440, 10).trim());
      set(APAR, conv.byteArrayToString(data, 450, 10).trim());
    }
    if (format >= 400)
    {
      set(LAST_USED_DATE, conv.byteArrayToString(data, 460, 7).trim());
      set(USAGE_INFO_UPDATED, conv.byteArrayToString(data, 467, 1)); // 'Y' was changed, 'N' was not.
      set(DAYS_USED, BinaryConverter.byteArrayToInt(data, 468));
      set(OBJECT_SIZE, BinaryConverter.byteArrayToInt(data, 472));
      set(OBJECT_SIZE_MULTIPLIER, BinaryConverter.byteArrayToInt(data, 476));
      set(COMPRESSION, conv.byteArrayToString(data, 480, 1));
      set(ALLOW_CHANGE_BY_PROGRAM, conv.byteArrayToString(data, 481, 1)); // '1' means it can be changed by QLICOBJD, '0' means not.
      set(CHANGED_BY_PROGRAM, conv.byteArrayToString(data, 482, 1)); // '1' means it has been changed by QLICOBJD, '0' means not.
      set(USER_DEFINED_ATTRIBUTE, conv.byteArrayToString(data, 483, 10).trim());
      set(OVERFLOWED_ASP, conv.byteArrayToString(data, 493, 1)); // '1' means it overflowed the ASP it's in, '0' means not.
      set(SAVE_ACTIVE_DATE, conv.byteArrayToString(data, 494, 13)); // date
      set(AUDITING, conv.byteArrayToString(data, 507, 10).trim());
      set(PRIMARY_GROUP, conv.byteArrayToString(data, 517, 10).trim());
      set(JOURNAL_STATUS, conv.byteArrayToString(data, 527, 1)); // '1' means it is currently being journaled, '0' means not.
      set(JOURNAL, conv.byteArrayToString(data, 528, 10).trim());
      set(JOURNAL_LIBRARY, conv.byteArrayToString(data, 538, 10).trim());
      set(JOURNAL_IMAGES, conv.byteArrayToString(data, 548, 1)); // '1' means both before and after images are generated, '0' means just after images.
      set(JOURNAL_OMITTED_ENTRIES, conv.byteArrayToString(data, 549, 1)); // '0' means no entries are omitted (i.e. all entries are journaled), '1' means open/close ops don't generate entries, ' ' means object isn't journaled.
      set(JOURNAL_START_DATE, conv.byteArrayToString(data, 550, 13).trim()); // date; will be blank of not journaled
      set(DIGITALLY_SIGNED, conv.byteArrayToString(data, 563, 1)); // '1' means it has a digital signature, '0' means not.
      set(SAVE_SIZE, BinaryConverter.byteArrayToInt(data, 564));
      set(SAVE_SIZE_MULTIPLIER, BinaryConverter.byteArrayToInt(data, 568));
      set(LIBRARY_ASP_NUMBER, BinaryConverter.byteArrayToInt(data, 572));
      set(OBJECT_ASP_DEVICE_NAME, conv.byteArrayToString(data, 576, 10).trim());
      set(LIBRARY_ASP_DEVICE_NAME, conv.byteArrayToString(data, 586, 10).trim());
      set(DIGITALLY_SIGNED_TRUSTED, conv.byteArrayToString(data, 596, 1)); // '1' means at least one signature came from a trusted source, '0' means none did.
      set(DIGITALLY_SIGNED_MULTIPLE, conv.byteArrayToString(data, 597, 1)); // '1' means there is more than one signature, '0' means there is only one or no signatures.
    }
  }


  void set(int attribute, int value)
  {
    values_.put(attribute, new Integer(value));
  }

  void set(int attribute, long value)
  {
    values_.put(attribute, new Long(value));
  }

  void set(int attribute, boolean value)
  {
    values_.put(attribute, new Boolean(value));
  }

  void set(int attribute, Object value)
  {
    values_.put(attribute, value);
  }


  /** 
   * Returns a String representation of this ObjectDescription.
   * @return The object path name.
  **/
  public String toString()
  {
    return getPath();
  }
}
