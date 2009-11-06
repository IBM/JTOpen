package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a list of database file members.
 * <p>
 * Implementation note: This class internally calls the "List Database File Members" API (QUSLMBR).
 * Information from formats MBRL0100 and MBRL0200 is retrieved.
 *
 * <p>
 * This class is mostly based on a prototype contributed by Mihael Schmidt.
 *
 * @see MemberDescription
 * @see AS400File
 */
public class MemberList
{
  private static final SimpleDateFormat dateTimeFormat_ = new SimpleDateFormat("yyMMddHHmmss");

  private static final String QUSLMBR_FORMAT_100 = "MBRL0100";
  private static final String QUSLMBR_FORMAT_200 = "MBRL0200";

  private static final QSYSObjectPathName USERSPACE_PATH = new QSYSObjectPathName("QTEMP", "JT4QUSLMBR", "USRSPC"); // user space QTEMP/JT4QUSLMBR


  private AS400 system_;
  private QSYSObjectPathName path_;
  private String memberSelection_ = "*ALL";  // default: all members
  private final Map memberDescriptions_ = new HashMap();
  private final List attributes_ = new ArrayList();

  // Offsets for format MBRL0200, "List Data Section", of API QUSLMBR.
  private static final int OFFSET_MEMBER_NAME = 0;
  private static final int OFFSET_SOURCE_TYPE = 10;
  private static final int OFFSET_CREATION_DATE_TIME = 20;
  private static final int OFFSET_LAST_SOURCE_CHANGE_DATE = 33;
  private static final int OFFSET_MEMBER_TEXT_DESCRIPTION = 46;
  private static final int OFFSET_MEMBER_TEXT_DESCRIPTION_CCSID = 96;

  private final AS400Bin4 intConverter_ = new AS400Bin4();

  /**
   * Constructs a MemberList object.
   *
   * @param file A database file.
   */
  public MemberList(AS400File file)
  {
    if (file == null) throw new NullPointerException("file");

    system_ = file.getSystem();
    path_ = new QSYSObjectPathName(file.getLibraryName(), file.getFileName(), "FILE");

    String memberName = file.getMemberName();
    if (memberName == null || memberName.length() == 0)
    {
      // default to *ALL
    }
    else if (memberName.startsWith("*"))  // *FIRST, *LAST, etc.
    {
      // The "member name" parameter of the QUSLMBR API must be either:
      // - a specific member name;
      // - a generic member name (a wildcarded name pattern); or
      // - special value *ALL, indicating "all members".
      memberSelection_ = "*ALL";  // force it to *ALL
      if (Trace.traceOn_)
      {
        Trace.log(Trace.DIAGNOSTIC, "Setting member selection to *ALL.  Member name from AS400File object:", memberName);
      }
    }
    else
    {
      memberSelection_ = memberName;
    }
  }

  /**
   * Constructs a MemberList object.
   *
   * Note: Generic names are only supported for the 'member' part of the IFS path.
   * To retrieve all members, the IFS path should point to the database file.
   *
   * @param system AS400 system object.
   * @param path IFS path to the database file or member.
   */
  public MemberList(AS400 system, QSYSObjectPathName path)
  {
    if (system == null) throw new NullPointerException("system");
    if (path == null) throw new NullPointerException("path");

    system_ = system;
    path_ = path;

    String memberName = path_.getMemberName();
    if (memberName == null || memberName.length() == 0)
    {
      memberSelection_ = "*ALL";
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting member selection to *ALL.  Member name from QSYSObjectPathName object is: " + memberName);
    }
    else
    {
      memberSelection_ = memberName;
    }
  }

  /**
   * Constructs a MemberList object.
   *
   * @param system AS400 system object
   * @param libraryName Library where the physical file is located.
   * @param objectName The name of the physical file.
   */
  public MemberList(AS400 system, String libraryName, String objectName)
  {
    this(system, new QSYSObjectPathName(libraryName, objectName, "FILE"));
  }


  /**
   * Determine the format to use on the API call, depending on the attribute.
   *
   * @param attribute Attribute to be retrieved
   * @return format string
   */
  private String lookupFormat(int attribute)
  {
    String format;

    switch (attribute)
    {
      case MemberDescription.MEMBER_NAME:
        format = QUSLMBR_FORMAT_100;
        break;

      case MemberDescription.SOURCE_TYPE:
      case MemberDescription.CREATION_DATE_TIME:
      case MemberDescription.LAST_SOURCE_CHANGE_DATE:
      case MemberDescription.MEMBER_TEXT_DESCRIPTION:
      case MemberDescription.MEMBER_TEXT_DESCRIPTION_CCSID:
        format = QUSLMBR_FORMAT_200;
        break;

      default:
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unrecognized attribute key:", attribute);
        format = QUSLMBR_FORMAT_200;
    }

    return format;
  }

  /**
   * Removes all member descriptions from this object.
   */
  public void clear()
  {
    memberDescriptions_.clear();
  }

  /**
   * Clears the attribute list which specifies which attributes should be retrieved.
   */
  public void clearAttributeList()
  {
    attributes_.clear();
  }

  /**
   * Adds an attribute to the attribute list which specifies which attributes of the member
   * are to be retrieved.  Constants that specify attributes are available in class {@link MemberDescription MemberDescription}.
   *
   * @param attribute The attribute to be added.
   */
  public void addAttribute(int attribute)
  {
    attributes_.add(new Integer(attribute));
  }

  /**
   * Reloads all member descriptions that have been specified via {@link #addAttribute addAttribute()}.
   *
   * @throws ObjectDoesNotExistException If a system object necessary for the call does not exist on the system.
   * @throws InterruptedException If this thread is interrupted.
   * @throws IOException If an error occurs while communicating with the system.
   * @throws ErrorCompletingRequestException If an error occurs before the request is completed.
   * @throws AS400SecurityException If a security or authority error occurs.
   */
  public void refresh()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException,
  IOException, ObjectDoesNotExistException
  {
    clear();
    load();
  }

  /**
   * Loads all members from the specified file(s).
   *
   * @throws ObjectDoesNotExistException If an object necessary for the call does not exist on the system.
   * @throws InterruptedException If this thread is interrupted.
   * @throws IOException If an error occurs while communicating with the system.
   * @throws ErrorCompletingRequestException If an error occurs before the request is completed.
   * @throws AS400SecurityException If a security or authority error occurs.
   * @throws AS400Exception If the program on the server sends an escape message.
   */
  public void load()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException,
  IOException, InterruptedException, ObjectDoesNotExistException
  {
    ProgramCall program = new ProgramCall(system_, "/QSYS.LIB/QUSLMBR.PGM", buildProgramParameters(getFormat()));  // this API is not threadsafe

    // Determine the needed scope of synchronization.
    Object lockObject;
    boolean willRunProgramsOnThread = program.isStayOnThread();
    if (willRunProgramsOnThread) {
      // The calls will run in the job of the JVM, so lock for entire JVM.
      lockObject = USERSPACE_PATH;
    }
    else {
      // The calls will run in the job of the Remote Command Host Server, so lock on the connection.
      lockObject = system_;
    }

    synchronized (lockObject)
    {
      // Create a user space in QTEMP to receive output from program call.
      UserSpace us = new UserSpace(system_, USERSPACE_PATH.getPath());
      us.setMustUseProgramCall(true);
      if (!willRunProgramsOnThread)
      {
        us.setMustUseSockets(true);
        // Force the use of sockets when running natively but not on-thread.
        // We have to do it this way since UserSpace will otherwise make a native ProgramCall, and will use a different QTEMP library than that used by the host server.
      }

      try
      {
        us.create(65535, true, "JT400", (byte) 0x00, "Userspace for loading members","*ALL");  // set public authority to *ALL

        if (!program.run()) {
          throw new AS400Exception(program.getMessageList());
        }

        byte[] usBuf = new byte[65535];  // local buffer to hold bytes read from user space

        // Read the "generic header" from the user space, into the local buffer.
        // Note: For description of general layout of the "list API" headers, see:
        // http://publib.boulder.ibm.com/infocenter/iseries/v6r1m0/topic/apiref/listGeneral.htm
        int numBytesRead = us.read(usBuf, 0, 0, 0x90);  // just read the needed header fields
        if (numBytesRead < 0x90) // verify that we at least got a header, up through "CCSID" field
        {
          Trace.log(Trace.ERROR, "Failed to read the generic header.  Number of bytes read: " + numBytesRead);
          throw new InternalErrorException(InternalErrorException.UNKNOWN, numBytesRead);
        }

        // Parse the header, to get the offsets to the various sections.

        // (Generic header) Offset to header section:
        int offsetToHeaderSection = BinaryConverter.byteArrayToInt(usBuf, 0x74);

        // (Generic header) Header section size:
        int headerSectionSize = BinaryConverter.byteArrayToInt(usBuf, 0x78);

        // (Generic header) Offset to list data section:
        int offsetToListDataSection = BinaryConverter.byteArrayToInt(usBuf, 0x7C);

        // (Generic header) List data section size:
        int listDataSectionSize = BinaryConverter.byteArrayToInt(usBuf, 0x80);

        // (Generic header) Number of list entries:
        int numberOfListEntries = BinaryConverter.byteArrayToInt(usBuf, 0x84);

        // (Generic header) Size of each entry:
        int sizeOfEachEntry = BinaryConverter.byteArrayToInt(usBuf, 0x88);

        // (Generic header) CCSID of data in the user space
        int entryCCSID = BinaryConverter.byteArrayToInt(usBuf, 0x8C);

        // (Generic header) Subsetted list indicator:
        //String subsettedListIndicator = conv.byteArrayToString(usBuf, 0x95, 1);

        if (entryCCSID == 0) entryCCSID = system_.getCcsid();
        // From the API spec: "The coded character set ID for data in the list entries.  If 0, then the data is not associated with a specific CCSID and should be treated as hexadecimal data."

        final CharConverter conv = new CharConverter(entryCCSID);

        // Read the "header section" into the local buffer.
        numBytesRead = us.read(usBuf, offsetToHeaderSection, 0, headerSectionSize);
        if (numBytesRead < headerSectionSize)
        {
          Trace.log(Trace.ERROR, "Failed to read the header section.  Number of bytes read: " + numBytesRead);
          throw new InternalErrorException(InternalErrorException.UNKNOWN, numBytesRead);
        }

        // (Header section) File library name used:
        final String libraryNameUsed = conv.byteArrayToString(usBuf, 10, 10).trim();

        // Read the "list data section" into the local buffer.
        if (listDataSectionSize > usBuf.length) {
          usBuf = new byte[listDataSectionSize+1]; // allocate a larger buffer
        }
        numBytesRead = us.read(usBuf, offsetToListDataSection, 0, listDataSectionSize);
        if (numBytesRead < listDataSectionSize)
        {
          Trace.log(Trace.ERROR, "Failed to read the list data section.  Number of bytes read: " + numBytesRead);
          throw new InternalErrorException(InternalErrorException.UNKNOWN, numBytesRead);
        }

        // Parse the list data returned in the user space.
        String format = getFormat();
        for (int i = 0; i < numberOfListEntries; i++)
        {
          byte[] entryBuf = new byte[sizeOfEachEntry];
          System.arraycopy(usBuf, i * sizeOfEachEntry, entryBuf, 0, sizeOfEachEntry);
          readMemberInfoFromUserspaceEntry(entryBuf, format, conv, libraryNameUsed);
        }
      }

      finally {
        // Delete the temporary user space, to allow other threads to re-create and use it.
        try { us.delete(); }
        catch (Exception e) {
          Trace.log(Trace.ERROR, "Exception while deleting temporary user space", e);
        }
      }
    }

  }

  /**
   * Builds the program parameter list for a program call to QUSLMBR (List Database File Members).
   *
   * @return Program parameter list for QUSLMBR
   */
  private ProgramParameter[] buildProgramParameters(String format)
    throws UnsupportedEncodingException
  {
    final CharConverter conv = new CharConverter(system_.getCcsid(), system_);

    ProgramParameter[] parameterList = new ProgramParameter[6];

    // Qualified user space name:
    parameterList[0] = new ProgramParameter(conv.stringToByteArray(system_, USERSPACE_PATH.toQualifiedObjectName()));
    // Format name:
    parameterList[1] = new ProgramParameter(conv.stringToByteArray(system_, format));
    // Qualified database file name:
    parameterList[2] = new ProgramParameter(conv.stringToByteArray(system_, path_.toQualifiedObjectName()));
    // Member name:
    parameterList[3] = new ProgramParameter(new AS400Text(10).toBytes(memberSelection_));
    // Override processing:  (1 == "overrides are processed")
    parameterList[4] = new ProgramParameter(conv.stringToByteArray(system_, "1"));
    // Error code:
    parameterList[5] = new ErrorCodeParameter();

    return parameterList;
  }

  /**
   * Reads all available information from the userspace entry regardless of the attributes to
   * be retrieved. The attributes to be retrieved determines in which format the entry is in the
   * userspace.
   *
   * @param entryBuf Raw bytes of the entry
   * @param format Content and format name (f. e. MBRL0100, see {@link #getFormat() getFormat()}
   * @param charConverter CharConverter object used for translating bytes to String
   * @param usedLibraryName The resolved library name used for the API call.
   *
   */
  private void readMemberInfoFromUserspaceEntry(byte[] entryBuf, String format, CharConverter charConverter, String usedLibraryName)
  {
    String memberName = charConverter.byteArrayToString(entryBuf, OFFSET_MEMBER_NAME, 10).trim();

    // get existing member description or create a new one
    MemberDescription memberDescription = (MemberDescription) memberDescriptions_.get(memberName);
    if (memberDescription == null)
    {
      QSYSObjectPathName memberPath = new QSYSObjectPathName(path_.getPath());
      try {
        memberPath.setMemberName(memberName);
        memberPath.setLibraryName(usedLibraryName);
      }
      catch (java.beans.PropertyVetoException pve) { // will never happen
        Trace.log(Trace.ERROR, "Error ignored.", pve);
      }
      memberDescription = new MemberDescription(system_, memberPath);
      memberDescription.setAttribute(MemberDescription.MEMBER_NAME, memberName);
      memberDescriptions_.put(memberName, memberDescription);
    }

    if (format == QUSLMBR_FORMAT_200)
    {
      memberDescription.setAttribute(MemberDescription.SOURCE_TYPE,
                                     charConverter.byteArrayToString(entryBuf, OFFSET_SOURCE_TYPE, 10).trim());
      memberDescription.setAttribute(MemberDescription.MEMBER_TEXT_DESCRIPTION,
                                     charConverter.byteArrayToString(entryBuf, OFFSET_MEMBER_TEXT_DESCRIPTION, 50).trim());
      memberDescription.setAttribute(MemberDescription.MEMBER_TEXT_DESCRIPTION_CCSID,
                                     new Integer(intConverter_.toInt(entryBuf, OFFSET_MEMBER_TEXT_DESCRIPTION_CCSID)));
      memberDescription.setAttribute(MemberDescription.LAST_SOURCE_CHANGE_DATE,
                                     transformDate(charConverter.byteArrayToString(entryBuf, OFFSET_LAST_SOURCE_CHANGE_DATE, 13)));
      memberDescription.setAttribute(MemberDescription.CREATION_DATE_TIME,
                                     transformDate(charConverter.byteArrayToString(entryBuf, OFFSET_CREATION_DATE_TIME, 13)));
    }
  }

  /**
   * Returns an array of retrieved member descriptions. If no member descriptions could be
   * retrieved because there are no members or because of an error, an empty array is returned.
   * If no members has been retrieved yet due to no call to load(), then an empty array is
   * returned.
   *
   * @return Array of retrieved member descriptions
   */
  public MemberDescription[] getMemberDescriptions()
  {
    return (MemberDescription[]) memberDescriptions_.values().toArray(new MemberDescription[memberDescriptions_.size()]);
  }

  /**
   * Returns the format for the call to QUSLMBR depending on the attributes to be retrieved.
   *
   * @return format name (either MBRL0100 or MBRL0200 for now)
   */
  private String getFormat()
  {
    String format = QUSLMBR_FORMAT_100;

    for (Iterator iter = attributes_.iterator(); iter.hasNext(); )
    {
      String tmpFormat = lookupFormat(((Integer) iter.next()).intValue());
      if (format.compareTo(tmpFormat) < 0)
      {
        format = tmpFormat;
      }
    }

    return format;
  }

  /**
   * Parses the date string, which should be in the format CYYMMDDHHMMSS.
   *
   * @param dateString Date string
   *
   * @return Date object representing the passed date string, or null if the
   *         date string is null, empty or not parseable.
   */
  private Date transformDate(String dateString)
  {
    Date retVal = null;

    if (dateString == null || dateString.length() == 0)
    {
      // nothing => returns null
    }
    else
    {
      try {
        synchronized (dateTimeFormat_) // date formats are not synchronized
        {
          retVal = dateTimeFormat_.parse(dateString.substring(1));
        }
      }
      catch(ParseException pe) { // return null
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Ignored error while parsing date string: " + dateString, pe);
      }
    }

    return retVal;
  }
}
