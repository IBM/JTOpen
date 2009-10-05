package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a database file member and its attributes.
 * MemberDescription objects can be constructed individually, or generated
 * by {@link MemberList#getMemberDescriptions MemberList.getMemberDescriptions()}.
 * A member's attributes can be retrieved by calling {@link #getValue getValue()}
 * and passing one of the attribute identifier constants defined in this class.
 * <p>
 * Note: String data is always stored and returned without trailing blanks.
 * <p>
 * Implementation note:
 * This class internally calls the "Retrieve Member Description" (QUSRMBRD) API.
 * Information from formats MBRD0100, MBRD0200, and MBRD0300 is retrieved.
 *
 * <p>
 * This class is mostly based on a prototype contributed by Mihael Schmidt.
 *
 * @see MemberList
**/
public class MemberDescription
{
  private static final SimpleDateFormat dateTimeFormat_ = new SimpleDateFormat("yyMMddHHmmss");
  private static final SimpleDateFormat dateFormat_ = new SimpleDateFormat("yyMMdd");

  private static final String QUSRMBRD_FORMAT_100 = "MBRD0100";
  private static final String QUSRMBRD_FORMAT_200 = "MBRD0200";
  private static final String QUSRMBRD_FORMAT_300 = "MBRD0300";


  /** The name of the database file whose member names are placed in the list. Attribute type: java.lang.String.*/
  public static final int FILE_NAME = 1;

  /** The name of the library where the database file is located. Attribute type: java.lang.String. */
  public static final int LIBRARY_NAME = 2;

  /** The name of a member found in the database file. Attribute type: java.lang.String. */
  public static final int MEMBER_NAME = 3;

  /** The type of file found. Attribute type: java.lang.String. */
  public static final int FILE_ATTRIBUTE = 4;

  /** The type of source member if this is a source file. Attribute type: java.lang.String. */
  public static final int SOURCE_TYPE = 5;

  /** The date and time the member was created. Attribute type: java.util.Date.*/
  public static final int CREATION_DATE_TIME = 6;

  /**
   * For source files, the date and time that this source member was last changed.
   * For SQL materialized query tables, the date and time that the last SQL Refresh
   * Table statement refreshed this member. Attribute type: java.util.Date (may be null).
   */
  public static final int LAST_SOURCE_CHANGE_DATE = 7;

  /** Description of the member found in the database file. Attribute type: java.lang.String. */
  public static final int MEMBER_TEXT_DESCRIPTION = 8;

  /** Whether the database file is a source file or a data file. Attribute type: java.lang.Boolean. */
  public static final int SOURCE_FILE = 9;

  /**
   * Whether the database file is a remote file (true = remote file, false = local file).
   * Attribute type: java.lang.Boolean.
   */
  public static final int REMOTE_FILE = 10;

  /**
   * Whether the database file is a logical or physical file (true = logical file, false =
   * physical file). Attribute type: java.lang.Boolean.
   */
  public static final int LOGICAL_FILE = 11;

  /**
   * Whether the open data path (ODP) allows sharing with other programs in the same job
   * (true = ODP sharing is allowed, false = ODP sharing is not allowed). Attribute type: java.lang.Boolean.
   */
  public static final int ODP_SHARING = 12;

  /**
   * The number of records that currently exist in this member.
   * A logical member returns the summarization of index entries.
   * If the requested physical file member is suspended, the value 0 is returned.
   * Attribute type: java.lang.Integer.
   */
  public static final int CURRENT_NUMBER_OF_RECORDS = 13;

  /**
   * The size of the space that contains the data of the file member, in bytes.
   * A logical file returns a 0. Attribute type: java.lang.Integer.
   */
  public static final int DATA_SPACE_SIZE = 14;

  /**
   * The access path size in bytes for this file member. If the file member is not keyed,
   * the value 0 is returned. DDM files, which are not from a System/38 or iSeries system,
   * return value 0. Attribute type: java.lang.Integer.
   */
  public static final int ACCESS_PATH_SIZE = 15;

  /**
   * The number of database file members for the logical file member. If the member is a physical
   * file member, the value is 0. Attribute type: java.lang.Integer.
   */
  public static final int NUMBER_OF_BASED_ON_PHYICAL_FILE_MEMBERS = 16;

  /**
   * The date and time this member was changed. The value contains null if the member was
   * never changed. Attribute type: java.util.Date.
   */
  public static final int CHANGE_DATE_AND_TIME = 17;

  /**
   * The date and time that this member was last saved. The value contains null if the member
   * was never saved. Attribute type: java.util.Date.
   */
  public static final int SAVE_DATE_AND_TIME = 18;

  /**
   * The date and time that the member was last restored. The value contains null if the member
   * was never restored. Attribute type: java.util.Date.
   */
  public static final int RESTORE_DATE_AND_TIME = 19;

  /** The date that this member expires. Attribute type: java.util.Date. */
  public static final int EXPIRATION_DATE = 20;

  /**
   * The number of days the member has been used. If the member does not have a last-used date,
   * the value 0 is returned. Attribute type: java.lang.Integer.
   */
  public static final int NUMBER_OF_DAYS_USED = 21;

  /**
   * The century and date this member was last used. The value contains null if the member was
   * never used. Attribute type: java.util.Date.
   */
  public static final int DATE_LAST_USED = 22;

  /**
   * The century and date when the days-used count was last set to 0. If the date is not available
   * the value contains null. Attribute type: java.util.Date.
   */
  public static final int USE_RESET_DATE = 23;

  /**
   * The value to multiply the data space size by to get its true size. Typically this is 1, but
   * for large files, the value may be greater than 1. If the data space size multiplier is
   * greater than 1, then the value in the data space size field is not the actual size of the file.
   * Attribute type: java.lang.Integer.
   */
  public static final int DATA_SPACE_SIZE_MULTIPLIER = 24;

  /** The value to multiply the access path size by to get its true size. Attribute type: java.lang.Integer. */
  public static final int ACCESS_PATH_SIZE_MULTIPLIER = 25;

  /** The CCSID for the member text description. Attribute type: java.lang.Integer. */
  public static final int MEMBER_TEXT_DESCRIPTION_CCSID = 26;

  /**
   * The number of deleted records returned in the file member. Keyed logical files return a 0.
   * DDM files that are not from a System/38 or iSeries system return a 0. If the requested
   * physical file member is suspended, the value 0 is returned. Attribute type: java.lang.Integer.
   */
  public static final int NUMBER_OF_DELETED_RECORDS = 27;

  /**
   * Whether the member's logical file member combines (in one record format) fields from two or
   * more physical file members (true = join member, false = not a join member).
   * Attribute type: java.lang.Boolean.
   */
  public static final int JOIN_MEMBER = 28;

  /**
   * Specifies, for files with key fields or join logical files, the type of access path
   * maintenance used for all members of the physical or logical file. Attribute type: java.lang.String.
   */
  public static final int ACCESS_PATH_MAINTENANCE = 29;

  /**
   * The kind of SQL file type the file is.  If the file isn't an SQL file, blank is returned.
   * Attribute type: java.lang.String.
   */
  public static final int SQL_FILE_TYPE = 30;

  /** Whether records in the physical file can be read. Values: Y/N. Attribute type: java.lang.Boolean. */
  public static final int ALLOW_READ_OPERATION = 31;

  /** Whether records can be written to the file. Values: Y/N. Attribute type: java.lang.Boolean. */
  public static final int ALLOW_WRITE_OPERATION = 32;

  /** Whether records in this file can be updated. Values: Y/N. Attribute type: java.lang.Boolean. */
  public static final int ALLOW_UPDATE_OPERATION = 33;

  /** Whether records in this file can be deleted. Values: Y/N. Attribute type: java.lang.Boolean. */
  public static final int ALLOW_DELETE_OPERATION = 34;

  /**
   * The number of inserted, updated, or deleted records that are processed before the records are
   * forced into auxiliary storage. A 0 indicates that records are not forced into auxiliary storage.
   * Attribute type: java.lang.Integer.
   */
  public static final int RECORDS_TO_FORCE_A_WRITE = 35;

  /**
   * The maximum allowed percentage of deleted records for each member in the physical file. The
   * percentage check is made when the member is closed. If the percentage of deleted records is
   * greater than the value shown, a message is sent to the history log. This field only applies
   * to physical files and is 0 when either no deleted records are allowed or the file is a
   * logical file. Attribute type: java.lang.Integer.
   */
  public static final int MAXIMUM_PERCENT_DELETED_RECORDS_ALLOWED = 36;

  /**
   * The number of records that can be written to each member of the file before the member size is
   * automatically extended. This field applies only to physical files and is 0 for logical files.
   * Attribute type: java.lang.Integer.
   */
  public static final int INITIAL_NUMBER_OF_RECORDS = 37;

  /**
   * The maximum number of records that are automatically added to the member when the number of
   * records in the member is greater than the initial member size. This field applies only to
   * physical files and is 0 for logical files. Attribute type: java.lang.Integer.
   */
  public static final int INCREMENT_NUMBER_OF_RECORDS = 38;

  /**
   * The maximum number of increments automatically added to the member size. This field only
   * applies to physical files and is 0 for a logical file. Attribute type: java.lang.Integer.
   */
  public static final int MAXIMUM_NUMBER_OF_INCREMENTS = 39;

  /**
   * The number of increments that have been added to the member size (data space size). This
   * field is 0 for logical files because the number of increments only applies to physical files.
   * Attribute type: java.lang.Integer.
   */
  public static final int CURRENT_NUMBER_OF_INCREMENTS = 40;

  /**
   * The actual number of records this member can contain. The value is calculated by multiplying
   * the increment number of records by the maximum number of increments, and adding the initial
   * number of records. This field only applies to a physical file and is 0 for a logical file.
   * Attribute type: java.lang.Integer.
   */
  public static final int RECORD_CAPACITY = 41;

  /**
   * The name of a record format selector program that is called when the logical file member
   * contains more than one logical record format. Attribute type: java.lang.String.
   */
  public static final int RECORD_FORMAT_SELECTOR_PROGRAM_NAME = 42;

  /**
   * The library in which the record format selector program resides. This field is blank for
   * physical files. Attribute type: java.lang.String.
   */
  public static final int RECORD_FORMAT_SELECTOR_LIBRARY_NAME = 43;

  /**
   * Specifies that all member information (all fields) should be retrieved.
   * @see #setAttribute(int, Object)
   */
  public static final int ALL_MEMBER_INFORMATION = Integer.MAX_VALUE;


  private AS400 system_;
  private QSYSObjectPathName path_;
  private final HashMap attributes_ = new HashMap();

  private final AS400Bin4 intConverter_ = new AS400Bin4();

  /**
   * Constructs a MemberDescription object.
   * @param system The system.
   * @param path The fully-qualified integrated file system path to the database file member.
   * Consider using {@link QSYSObjectPathName QSYSObjectPathName} to compose the fully-qualified path string.
   */
  public MemberDescription(AS400 system, QSYSObjectPathName path)
  {
    if (system == null) throw new NullPointerException("system");
    if (path == null) throw new NullPointerException("path");

    system_ = system;
    path_ = path;
  }

  /**
   * Constructs a MemberDescription object.
   *
   * @param system AS400 system object
   * @param libraryName Library where the database file is located.
   * @param objectName The name of the database file.
   * @param memberName The name of the member within the database file.
   */
  public MemberDescription(AS400 system, String libraryName, String objectName, String memberName)
  {
    this(system, new QSYSObjectPathName(libraryName, objectName, memberName, "MBR"));
  }

  /**
   * Constructs a MemberDescription given the specified path to the member with some preloaded
   * attributes.
   *
   * @param system The system.
   * @param path The fully-qualified integrated file system path to the member.
   * Consider using {@link QSYSObjectPathName QSYSObjectPathName} to compose the fully-qualified path string.
   * @param attributes Map with preloaded attributes
   *
   * @see MemberList
   */
  MemberDescription(AS400 system, QSYSObjectPathName path, Map attributes)
  {
    this(system, path);
    attributes_.putAll(attributes);
  }

  /**
   * Determine the format to use on the API call depending on the attribute.
   *
   * @param attributeKey Attribute key
   * @return Format name to be used for the API call.
   */
  private String lookupFormat(int attributeKey)
  {
    String format;

    switch (attributeKey)
    {
      case FILE_NAME:
      case LIBRARY_NAME:
      case MEMBER_NAME:
      case FILE_ATTRIBUTE:
      case SOURCE_TYPE:
      case CREATION_DATE_TIME:
      case LAST_SOURCE_CHANGE_DATE:
      case MEMBER_TEXT_DESCRIPTION:
      case SOURCE_FILE:
        format = QUSRMBRD_FORMAT_100;
        break;

      case REMOTE_FILE:
      case LOGICAL_FILE:
      case ODP_SHARING:
      case CURRENT_NUMBER_OF_RECORDS:
      case DATA_SPACE_SIZE:
      case ACCESS_PATH_SIZE:
      case NUMBER_OF_BASED_ON_PHYICAL_FILE_MEMBERS:
      case CHANGE_DATE_AND_TIME:
      case SAVE_DATE_AND_TIME:
      case RESTORE_DATE_AND_TIME:
      case EXPIRATION_DATE:
      case NUMBER_OF_DAYS_USED:
      case DATE_LAST_USED:
      case USE_RESET_DATE:
      case DATA_SPACE_SIZE_MULTIPLIER:
      case ACCESS_PATH_SIZE_MULTIPLIER:
      case MEMBER_TEXT_DESCRIPTION_CCSID:
      case NUMBER_OF_DELETED_RECORDS:
        format = QUSRMBRD_FORMAT_200;
        break;

      case JOIN_MEMBER:
      case ACCESS_PATH_MAINTENANCE:
      case SQL_FILE_TYPE:
      case ALLOW_READ_OPERATION:
      case ALLOW_WRITE_OPERATION:
      case ALLOW_UPDATE_OPERATION:
      case ALLOW_DELETE_OPERATION:
      case RECORDS_TO_FORCE_A_WRITE:
      case MAXIMUM_PERCENT_DELETED_RECORDS_ALLOWED:
      case INITIAL_NUMBER_OF_RECORDS:
      case INCREMENT_NUMBER_OF_RECORDS:
      case MAXIMUM_NUMBER_OF_INCREMENTS:
      case CURRENT_NUMBER_OF_INCREMENTS:
      case RECORD_CAPACITY:
      case RECORD_FORMAT_SELECTOR_PROGRAM_NAME:
      case RECORD_FORMAT_SELECTOR_LIBRARY_NAME:
      case ALL_MEMBER_INFORMATION:
        format = QUSRMBRD_FORMAT_300;
        break;

      default:
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unrecognized attribute key:", attributeKey);
        format = QUSRMBRD_FORMAT_100;
    }

    return format;
  }

  /**
   * Returns the requested member attribute information object. If the value is not in the cache
   * it will be retrieved from the system.
   *
   * @param attributeKey Attribute to be retrieved (either from cache or from system)
   *
   * @return Requested member attribute
   *
   * @throws ObjectDoesNotExistException If a system object necessary for the call does not exist on the system.
   * @throws InterruptedException If this thread is interrupted.
   * @throws IOException If an error occurs while communicating with the system.
   * @throws ErrorCompletingRequestException If an error occurs before the request is completed.
   * @throws AS400SecurityException If a security or authority error occurs.
   * @throws AS400Exception If the program on the server sends an escape message.
   */
  public Object getValue(int attributeKey)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException,
  IOException, InterruptedException, ObjectDoesNotExistException
  {
    final Integer key = new Integer(attributeKey);
    Object value = null;

    value = attributes_.get(key);

    // If the value has not yet been retrieved from the server, the value object is still null.
    // Retrieve it from the server and set the value object at last.
    if (value == null)
    {
      value = retrieve(attributeKey);
      // add value to the cache
      attributes_.put(key, value);
    }

    return value;
  }

  /**
   * Sets the attribute of the member. Any previous information of the same attribute will be
   * replaced.
   *
   * @param attributeKey Attribute key (Integer)
   * @param value member information object to be set
   */
  void setAttribute(int attributeKey, Object value)
  {
    attributes_.put(new Integer(attributeKey), value);
  }

  /**
   * This method makes the actual call to the server and gets the information about the member.
   * All other member information which belong to the same format are also retrieved.
   *
   * @param attributeKey Attribute to be retrieved and returned
   *
   * @return The value of the attribute
   *
   * @throws ObjectDoesNotExistException If a system object necessary for the call does not exist on the system.
   * @throws InterruptedException If this thread is interrupted.
   * @throws IOException If an error occurs while communicating with the system.
   * @throws ErrorCompletingRequestException If an error occurs before the request is completed.
   * @throws AS400SecurityException If a security or authority error occurs.
   * @throws AS400Exception If the program on the server sends an escape message.
   */
  private Object retrieve(int attributeKey)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException,
  IOException, InterruptedException, ObjectDoesNotExistException
  {
      final String format = lookupFormat(attributeKey);

      ProgramParameter[] parameters = buildProgramParameters(format);
      ProgramCall program = new ProgramCall(system_, "/QSYS.LIB/QUSRMBRD.PGM", parameters);  // this API is not threadsafe

      if (program.run())
      {
        readMemberInfo(parameters[0].getOutputData(), format);
        return attributes_.get(new Integer(attributeKey));
      }
      else
      {
        throw new AS400Exception(program.getMessageList());
      }
  }

  /**
   * Builds the program parameter list for a program call to QUSRMBRD (Retrieve Member Description).
   *
   * @return Program parameter list for QUSRMBRD
   */
  private ProgramParameter[] buildProgramParameters(String format)
    throws UnsupportedEncodingException
  {
    final CharConverter charConverter = new CharConverter(system_.getCcsid(), system_);

    ProgramParameter[] parameterList = new ProgramParameter[7];

    // Receiver variable:
    parameterList[0] = new ProgramParameter(332);
    // Length of receiver variable:
    parameterList[1] = new ProgramParameter(intConverter_.toBytes(332));
    // Format name:
    parameterList[2] = new ProgramParameter(charConverter.stringToByteArray(system_, format));
    // Qualified database file name:
    parameterList[3] = new ProgramParameter(charConverter.stringToByteArray(system_, path_.toQualifiedObjectName()));
    // Database member name:
    parameterList[4] = new ProgramParameter(new AS400Text(10).toBytes(path_.getMemberName()));
    // Override processing:  (1 == "overrides are processed")
    parameterList[5] = new ProgramParameter(charConverter.stringToByteArray(system_, "1"));
    // Error code:
    parameterList[6] = new ErrorCodeParameter();

    return parameterList;
  }


  // Returns the offset of the specified field, in the MBRD* structures.
  private static final int offsetOf(int attributeKey)
  {
    switch (attributeKey)
    {
        // Fields in format MBRD0100:
      case FILE_NAME:                               return   8;
      case LIBRARY_NAME:                            return  18;
      case MEMBER_NAME:                             return  28;
      case FILE_ATTRIBUTE:                          return  38;
      case SOURCE_TYPE:                             return  48;
      case CREATION_DATE_TIME:                      return  58;
      case LAST_SOURCE_CHANGE_DATE:                 return  71;
      case MEMBER_TEXT_DESCRIPTION:                 return  84;
      case SOURCE_FILE:                             return 134;

        // Additional fields in format MBRD0200:
      case REMOTE_FILE:                             return 135;
      case LOGICAL_FILE:                            return 136;
      case ODP_SHARING:                             return 137;
      case CURRENT_NUMBER_OF_RECORDS:               return 140;
        // offset 144: number of deleted records
      case DATA_SPACE_SIZE:                         return 148;
      case ACCESS_PATH_SIZE:                        return 152;
      case NUMBER_OF_BASED_ON_PHYICAL_FILE_MEMBERS: return 156;
      case CHANGE_DATE_AND_TIME:                    return 160;
      case SAVE_DATE_AND_TIME:                      return 173;
      case RESTORE_DATE_AND_TIME:                   return 186;
      case EXPIRATION_DATE:                         return 199;
        // offset 206: reserved
      case NUMBER_OF_DAYS_USED:                     return 212;
      case DATE_LAST_USED:                          return 216;
      case USE_RESET_DATE :                         return 223;
        // offset 230: reserved
      case DATA_SPACE_SIZE_MULTIPLIER:              return 232;
      case ACCESS_PATH_SIZE_MULTIPLIER:             return 236;
      case MEMBER_TEXT_DESCRIPTION_CCSID:           return 240;
      case NUMBER_OF_DELETED_RECORDS:               return 256;

        // Additional fields in format MBRD0300:
      case JOIN_MEMBER:                             return 266;
      case ACCESS_PATH_MAINTENANCE:                 return 267;
      case SQL_FILE_TYPE:                           return 268;
        // offset 278: reserved
      case ALLOW_READ_OPERATION:                    return 279;
      case ALLOW_WRITE_OPERATION:                   return 280;
      case ALLOW_UPDATE_OPERATION:                  return 281;
      case ALLOW_DELETE_OPERATION:                  return 282;
        // offset 283: reserved
      case RECORDS_TO_FORCE_A_WRITE:                return 284;
      case MAXIMUM_PERCENT_DELETED_RECORDS_ALLOWED: return 288;
      case INITIAL_NUMBER_OF_RECORDS:               return 292;
      case INCREMENT_NUMBER_OF_RECORDS:             return 296;
      case MAXIMUM_NUMBER_OF_INCREMENTS:            return 300;
      case CURRENT_NUMBER_OF_INCREMENTS:            return 304;
      case RECORD_CAPACITY:                         return 308;
      case RECORD_FORMAT_SELECTOR_PROGRAM_NAME:     return 312;
      case RECORD_FORMAT_SELECTOR_LIBRARY_NAME:     return 322;

      default:
        Trace.log(Trace.ERROR, "Unrecognized attribute key:", attributeKey);
        throw new InternalErrorException(InternalErrorException.UNKNOWN, attributeKey);
    }
  }


  /**
   * Reads all available information from the output (receiver) bytes regardless of the attribute to
   * be retrieved. The attribute to be retrieved determines in which content and format the data is sent.
   *
   * @param entryBytes Raw bytes of the entry
   * @param format Content and format name
   *
   * @throws UnsupportedEncodingException If the ccsid is not supported.
   */
  private void readMemberInfo(byte[] entryBytes, String format)
    throws UnsupportedEncodingException
  {
    final CharConverter charConverter = new CharConverter(system_.getCcsid(), system_);


    // Fields returned in all formats:

    setAttribute(FILE_NAME,
                 charConverter.byteArrayToString(entryBytes, offsetOf(FILE_NAME), 10).trim());
    setAttribute(LIBRARY_NAME,
                 charConverter.byteArrayToString(entryBytes, offsetOf(LIBRARY_NAME), 10).trim());
    setAttribute(FILE_ATTRIBUTE,
                 charConverter.byteArrayToString(entryBytes, offsetOf(FILE_ATTRIBUTE), 10).trim());
    setAttribute(MEMBER_NAME,
                 charConverter.byteArrayToString(entryBytes, offsetOf(MEMBER_NAME), 10).trim());
    setAttribute(SOURCE_TYPE,
                 charConverter.byteArrayToString(entryBytes, offsetOf(SOURCE_TYPE), 10).trim());
    setAttribute(MEMBER_TEXT_DESCRIPTION,
                 charConverter.byteArrayToString(entryBytes, offsetOf(MEMBER_TEXT_DESCRIPTION), 50).trim());
    setAttribute(LAST_SOURCE_CHANGE_DATE,  // Note: This field can contain hex zeros.
                 transformDate(charConverter.byteArrayToString(entryBytes, offsetOf(LAST_SOURCE_CHANGE_DATE), 13)));
    setAttribute(CREATION_DATE_TIME,
                 transformDate(charConverter.byteArrayToString(entryBytes, offsetOf(CREATION_DATE_TIME), 13)));
    setAttribute(SOURCE_FILE,
                 transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(SOURCE_FILE), 1)));


    // Fields returned in both formats 200 and 300:
    if (format != QUSRMBRD_FORMAT_100)
    {
      setAttribute(REMOTE_FILE,
                   transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(REMOTE_FILE), 1)));
      setAttribute(LOGICAL_FILE,
                   transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(LOGICAL_FILE), 1)));
      setAttribute(ODP_SHARING,
                   transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(ODP_SHARING), 1)));
      setAttribute(DATA_SPACE_SIZE,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(DATA_SPACE_SIZE))));
      setAttribute(ACCESS_PATH_SIZE,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(ACCESS_PATH_SIZE))));
      setAttribute(NUMBER_OF_BASED_ON_PHYICAL_FILE_MEMBERS,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(NUMBER_OF_BASED_ON_PHYICAL_FILE_MEMBERS))));
      setAttribute(CHANGE_DATE_AND_TIME,
                   transformDate(charConverter.byteArrayToString(entryBytes, offsetOf(CHANGE_DATE_AND_TIME), 13)));
      setAttribute(RESTORE_DATE_AND_TIME,
                   transformDate(charConverter.byteArrayToString(entryBytes, offsetOf(RESTORE_DATE_AND_TIME), 13)));
      setAttribute(SAVE_DATE_AND_TIME,
                   transformDate(charConverter.byteArrayToString(entryBytes, offsetOf(SAVE_DATE_AND_TIME), 13)));
      setAttribute(EXPIRATION_DATE,
                   transformDate(charConverter.byteArrayToString(entryBytes, offsetOf(EXPIRATION_DATE), 13)));
      setAttribute(NUMBER_OF_DAYS_USED,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(NUMBER_OF_DAYS_USED))));
      setAttribute(DATE_LAST_USED,
                   transformDate(charConverter.byteArrayToString(entryBytes, offsetOf(DATE_LAST_USED), 13)));
      setAttribute(USE_RESET_DATE,
                   transformDate(charConverter.byteArrayToString(entryBytes, offsetOf(USE_RESET_DATE), 13)));
      setAttribute(DATA_SPACE_SIZE_MULTIPLIER,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(DATA_SPACE_SIZE_MULTIPLIER))));
      setAttribute(ACCESS_PATH_SIZE_MULTIPLIER,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(ACCESS_PATH_SIZE_MULTIPLIER))));
      setAttribute(MEMBER_TEXT_DESCRIPTION_CCSID,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(MEMBER_TEXT_DESCRIPTION_CCSID))));
      setAttribute(NUMBER_OF_DELETED_RECORDS,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(NUMBER_OF_DELETED_RECORDS))));
    }

    // Fields returned only in format 300:
    if (format == QUSRMBRD_FORMAT_300)
    {
      setAttribute(JOIN_MEMBER,
                   transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(JOIN_MEMBER), 1)));
      setAttribute(ACCESS_PATH_MAINTENANCE,
                   charConverter.byteArrayToString(entryBytes, offsetOf(ACCESS_PATH_MAINTENANCE), 1));
      setAttribute(SQL_FILE_TYPE,
                   charConverter.byteArrayToString(entryBytes, offsetOf(SQL_FILE_TYPE), 10).trim());
      setAttribute(ALLOW_READ_OPERATION,
                   transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(ALLOW_READ_OPERATION), 1)));
      setAttribute(ALLOW_WRITE_OPERATION,
                   transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(ALLOW_WRITE_OPERATION), 1)));
      setAttribute(ALLOW_UPDATE_OPERATION,
                   transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(ALLOW_UPDATE_OPERATION), 1)));
      setAttribute(ALLOW_DELETE_OPERATION,
                   transformBoolean(charConverter.byteArrayToString(entryBytes, offsetOf(ALLOW_DELETE_OPERATION), 1)));
      setAttribute(RECORDS_TO_FORCE_A_WRITE,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(RECORDS_TO_FORCE_A_WRITE))));
      setAttribute(MAXIMUM_PERCENT_DELETED_RECORDS_ALLOWED,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(MAXIMUM_PERCENT_DELETED_RECORDS_ALLOWED))));
      setAttribute(INITIAL_NUMBER_OF_RECORDS,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(INITIAL_NUMBER_OF_RECORDS))));
      setAttribute(INCREMENT_NUMBER_OF_RECORDS,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(INCREMENT_NUMBER_OF_RECORDS))));
      setAttribute(MAXIMUM_NUMBER_OF_INCREMENTS,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(MAXIMUM_NUMBER_OF_INCREMENTS))));
      setAttribute(CURRENT_NUMBER_OF_INCREMENTS,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(CURRENT_NUMBER_OF_INCREMENTS))));
      setAttribute(RECORD_CAPACITY,
                   new Integer(intConverter_.toInt(entryBytes, offsetOf(RECORD_CAPACITY))));
      setAttribute(RECORD_FORMAT_SELECTOR_PROGRAM_NAME,
                   charConverter.byteArrayToString(entryBytes, RECORD_FORMAT_SELECTOR_PROGRAM_NAME, 10).trim());
      setAttribute(RECORD_FORMAT_SELECTOR_LIBRARY_NAME,
                   charConverter.byteArrayToString(entryBytes, RECORD_FORMAT_SELECTOR_LIBRARY_NAME, 10).trim());
    }
  }

  /**
   * Parses the date string which should be in the format CYYMMDDHHMMSS or CYYMMDD;
   *
   * @param dateString Date string
   *
   * @return Date object representing the passed date string or null if the
   *         date string is null, empty or not parseable.
   */
  private Date transformDate(String dateString)
  {
    Date retVal = null;

    if (dateString == null)
    {
      // nothing => returns null
    }
    else
    {
      dateString = dateString.trim();
      if (dateString.length() == 0)
      {
        // nothing => returns null
      }
      else
      {
        try
        {
          if (dateString.length() == 13)
          {
            synchronized(dateTimeFormat_)
            {
              retVal = dateTimeFormat_.parse(dateString.substring(1)); // skip the century digit
            }
          }
          else if (dateString.length() == 7)
          {
            synchronized(dateFormat_)
            {
              retVal = dateFormat_.parse(dateString.substring(1)); // skip the century digit
            }
          }
          else
          {
            if (Trace.traceOn_) Trace.log(Trace.WARNING, "Date string has unrecognized format:", dateString);
          }
        }
        catch(ParseException pe) { // return null
          if (Trace.traceOn_) Trace.log(Trace.ERROR, "Ignored error while parsing date string: " + dateString, pe);
        }
      }
    }

    return retVal;
  }

  /**
   * Transforms the value of 1 to true and everything else to false.
   *
   * @param value
   * @return Boolean object representing 1 as true and everything else as false
   */
  private Boolean transformBoolean(String value)
  {
    // Assume that the caller has verified that the argument is non-null.
    if (value.equals("1")) {
      return Boolean.TRUE;
    }
    else {
      return Boolean.FALSE;
    }
  }

  /**
   * Returns the IFS path of this member.
   *
   * @return IFS path to this member.
   */
  public String getPath()
  {
    return path_.getPath();
  }
}
