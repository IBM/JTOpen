package com.ibm.as400.access;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.beans.PropertyVetoException;

/**
 Represents a save file on a system.
**/

public class SaveFile
implements Serializable
{
  static final long serialVersionUID = 4L;

  private static final boolean DEBUG = false;

  /**
   Value for the "targetRelease" property, indicating "current release".  The object is to be restored to, and used on, the release of the operating system currently running on the system.  The object can also be restored to a system with any subsequent release of the operating system installed.
   **/
  public static final String CURRENT_RELEASE = "*CURRENT";

  /**
   Value for the "targetRelease" property, indicating "previous release".  The object is to be restored to the previous release with modification level 0 of the operating system.  The object can also be restored to a system with any subsequent release of the operating system installed.
   **/
  public static final String PREVIOUS_RELEASE = "*PRV";

  /**
   Value for the "maximumNumberOfRecords" property, indicating that the system maximum is used.
   **/
  public static final long NO_MAX = 0L;

  /**
   Value for the "waitTime" property, indicating "immediate".  The program does not wait; when the file is opened, an immediate allocation of the file resources is required.
   **/
  public static final int IMMED = -1;

  /**
   Special value for the "waitTime" property.  Indicates that the job default wait time is used as the wait time for the file resources being allocated.
   **/
  public static final int CLS = 0;

  /**
   Special value for the "maximumNumberOfRecords", "waitTime", and "asp" properties.  Indicates that the system default value is used.
   **/
  public static final int DEFAULT = -99;

  // Values for the existence_ variable:
  private static final int EXISTENCE_UNKNOWN = 0;  // save file existence is unknown
  private static final int EXISTENCE_YES     = 1;  // save file exists on system
  private static final int EXISTENCE_NO      = 2;  // save file doesn't exist

  private static final String USERSPACE_QUALIFIED_NAME = "JT4USRSPC QTEMP     ";
  private static final String USERSPACE_PATH = "/QSYS.LIB/QTEMP.LIB/JT4USRSPC.USRSPC";

  private final static ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);

  // Persistent attributes:
  private AS400  system_;   // the system where the save file is located
  private String path_;     // fully qualified IFS pathname of the save file
  private String library_;  // library where the save file is located
  private String name_;     // the name of the save file
  private String targetRelease_ = CURRENT_RELEASE; // target release for save file
  private String saveOutput_ = "*NONE"; // whether output from API's should be saved

  // The remaining fields are for internal housekeeping.

  private transient int               systemVRM_;
  private transient boolean           gotSystemVRM_;
  private transient ObjectDescription objectDescription_;
  private transient Record            fileAttributes_;  // format is described by class "SaveFileAttrFormat"
  private transient String            savefileNameAndLib_;

  private transient int existence_ = EXISTENCE_UNKNOWN;  // cached value from most recent existence check

  // Note: If it's ever required, we'll make this class into a Bean later.
  // Until then, we don't provide a default (zero-argument) constructor.


  /**
   Constructs a SaveFile object.
   <br>Note: This method does not create a save file on the system.  To create a save file, use {@link #create() create()} or {@link #create(long,int,int,boolean,String,String) create()}.
   @param system  The system where the save file is located.
   @param library  The library (on the system) where the save file is located.  Example: "MYLIB1".  Case is preserved.
   @param name  The name of the save file.  Example: "MYFILE1".  Case is preserved.
   **/
  public SaveFile(AS400 system, String library, String name)
  {
    if (system == null) { throw new NullPointerException("system"); }
    if (library == null) { throw new NullPointerException("library"); }
    if (name == null) { throw new NullPointerException("name"); }

    system_ = system;
    library_ = library;
    name_ = name;
    path_ = QSYSObjectPathName.toPath(library, name, "FILE");
  }


  /**
   Clears all existing records from the save file and reduces the amount of storage used by this file.
   <p>
   A save file must be cleared before it can be used again to receive data from a save command or to receive another save file.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void clear()
    throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    checkExistence();
    CommandCall cmd = new CommandCall(system_, "QSYS/CLRSAVF FILE("+library_+"/"+name_+")");
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
    fileAttributes_ = null;  // force a refresh
  }


  /**
   Copies the save file to another save file on the same system.
   <br>If the target save file doesn't exist on the system, it is created.
   <br>If the target save file already contains data, the data is replaced.
   <br>If the target exists but is not a save file, this method fails.
   <br>If the target has insufficient capacity, an exception is thrown.
   @param library  The library of the target save file.  Example: "MYLIB1".  Case is preserved.
   @param name  The name of the target save file.  Example: "MYFILE1".  Case is preserved.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void copyTo(String library, String name)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (library == null) { throw new NullPointerException("library"); }
    if (name == null) { throw new NullPointerException("name"); }

    checkExistence();
    String targetPath = QSYSObjectPathName.toPath(library, name, "FILE");
    CommandCall cmd = new CommandCall(system_, "QSYS/CPY OBJ('"+path_+"') TOOBJ('" + targetPath + "') REPLACE(*YES)");
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
  }

  /**
   Creates a save file on the system.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws ObjectAlreadyExistsException If the save file already exists on the system.
   @throws ObjectDoesNotExistException If the system API that queries save file description information is missing.
   **/
  public void create()
    throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException, ObjectAlreadyExistsException
  {
    create(DEFAULT, DEFAULT, DEFAULT, false, null, null);
  }


  /**
   Creates a save file on the system.
   @param maxRecords  The maximum number of records the save file can hold.  If {@link #NO_MAX NO_MAX} is specified, the system maximum is used.
   @param asp  The auxiliary storage pool (ASP) in which the system creates the save file.  If {@link #DEFAULT DEFAULT} is specified, the save file is created in the same ASP as the one containing the library holding the file.
   @param waitTime  The number of seconds that the program waits for the file resources and session resources to be allocated when the file is opened, or for the device or session resources to be allocated when an acquire operation is performed to the file.  Special values: {@link #CLS CLS}, {@link #IMMED IMMED}
   @param shared Specifies whether the open data path (ODP) for the save file is shared with other programs in the routing step.  When an ODP is shared, the programs accessing the file share facilities such as the file status and the buffer.  Default is false.
   @param authority  The authority given to users who do not have specific authority to the save file, who are not on an authorization list, and whose user group has no specific authority to the save file.  Defaults to *EXCLUDE if null.
   @param description  The text that briefly describes the save file.  Defaults to blank if <tt>null</tt>.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws ObjectAlreadyExistsException If the save file already exists on the system.
   @throws ObjectDoesNotExistException If the system API that queries save file description information is missing.
   **/
  public void create(long maxRecords, int asp, int waitTime, boolean shared, String authority, String description)
    throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException, ObjectAlreadyExistsException
  {
    if (exists()) {
      throw new ObjectAlreadyExistsException(path_, ObjectAlreadyExistsException.OBJECT_ALREADY_EXISTS);
    }

    // Note: We tolerate null args.  They are interpreted to indicate "Use the default value".

    StringBuffer cmdBuf = new StringBuffer("QSYS/CRTSAVF FILE("+library_+"/"+name_+") ");

    if (maxRecords == DEFAULT) {
      // Ignore; the user wants the default.
    }
    else if (maxRecords == NO_MAX) {
      cmdBuf.append("MAXRCDS(*NOMAX) ");
    }
    else {
      cmdBuf.append("MAXRCDS("+maxRecords+") ");
    }

    if (asp != DEFAULT) {  
      cmdBuf.append("ASP("+asp+") ");
    }

    switch (waitTime) {
      case DEFAULT:
        // Ignore; the user wants the default.
        break;
      case CLS:
        cmdBuf.append("WAITFILE(*CLS) ");
        break;
      case IMMED:  // 0
        cmdBuf.append("WAITFILE(*IMMED) ");
        break;
      default:
        cmdBuf.append("WAITFILE("+Integer.toString(waitTime) + ") ");
    }

    if (shared) { cmdBuf.append("SHARE(*YES) "); }
    // Note: Default is *NO.

    if (authority != null) {
      cmdBuf.append("AUT("+authority+") ");
    }

    if (description != null) {
      cmdBuf.append("TEXT('"+description+"') ");
    }

    if (DEBUG) System.out.println("Running command: " + cmdBuf.toString());
    CommandCall cmd = new CommandCall(system_, cmdBuf.toString());
    if (!cmd.run()) {
      existence_ = EXISTENCE_UNKNOWN;
      throw new AS400Exception(cmd.getMessageList());
    }
    existence_ = EXISTENCE_YES;
  }


  /**
   Deletes the save file.  If the save file does not exist, does nothing.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   **/
  public void delete()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
  {
    CommandCall cmd = new CommandCall(system_, "QSYS/DLTF FILE("+library_+"/"+name_+")");
    if (!cmd.run())
    {
      existence_ = EXISTENCE_UNKNOWN;
      AS400Message[] msgs = cmd.getMessageList();
      if (msgs.length > 0) {
        if (msgs[0].getID() != null &&
            !msgs[0].getID().equals("CPF2105") &&  // file not found
            !msgs[0].getID().equals("CPF2110") /*&&*/  // library not found
            /* !msgs[0].getID().equals("CPC2191") */ ) {
          throw new AS400Exception(msgs);
        }
      }
      else {
        throw new InternalErrorException("No messages returned from failed delete.",
                                         InternalErrorException.UNKNOWN);
      }
    }
    existence_ = EXISTENCE_NO;
  }


  /**
   Determines whether this SaveFile object is equal to another object.
   @return <tt>true</tt> if the two instances are equal
   **/
  public boolean equals(Object obj)
  {
    try
    {
      SaveFile other = (SaveFile)obj;

      // Note: For any given SaveFile object:  system_, library_, and name_ are all guaranteed to be non-null.
      if (!system_.equals(other.getSystem())) return false;
      if (!library_.equals(other.getLibrary())) return false;
      if (!name_.equals(other.getName())) return false;
      else return true;
    }
    catch (Throwable e) {
      return false;
    }
  }

  /**
   Returns a hash code value for the object.
   @return A hash code value for this object.
   **/
  public int hashCode()
  {
    // We must conform to the invariant that equal objects must have equal hashcodes.
    return (system_.hashCode() + library_.hashCode() + name_.hashCode());
  }


  /**
   Determines if the save file currently exists on the system.
   @return true if the save file exists; false if the save file does not exist.
   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws ObjectDoesNotExistException If the system API that queries save file description information is missing.
  **/
  public boolean exists()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (objectDescription_ == null) { objectDescription_ = getObjDesc(); }
    existence_ = (objectDescription_.exists() ? EXISTENCE_YES : EXISTENCE_NO);
    return (existence_==EXISTENCE_YES);
  }


  /**
   Returns the auxiliary storage pool ID for the save file.
   <br>If the save file doesn't exist on the system, an exception is thrown.
   @return The auxiliary storage pool ID.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public int getASP()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();
    if (fileAttributes_ == null) fileAttributes_ = getFileAttributes();

    BigDecimal decVal = (BigDecimal)fileAttributes_.getField("SAASP");
    int value = decVal.intValue();  // It's only a 3-digit field, so it'll fit.
    return value;
  }


  /**
   Returns the current number of records in the save file.
   <br>If the save file doesn't exist on the system, an exception is thrown.
   @return The current number of records in the save file.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public long getCurrentNumberOfRecords()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();
    if (fileAttributes_ == null) fileAttributes_ = getFileAttributes();

    BigDecimal decVal = (BigDecimal)fileAttributes_.getField("SACNRC");
    long value = decVal.longValue();
    if (value == 99999) {
      decVal = (BigDecimal)fileAttributes_.getField("SACNR2");
      value = decVal.longValue();
    }
    return value;
  }


  /**
   Returns the text description of the save file.
   @return  The text description.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public String getDescription()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    objectDescription_ = checkExistence();
    return (String)objectDescription_.getValue(ObjectDescription.TEXT_DESCRIPTION);
  }


  /**
   Returns the current total size (in bytes) of the save file.
   @return  The size of the save file.
   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public long getLength()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    // Note: From the spec for the CHGSAVF command:
    // The size of the save file is estimated in bytes at about 8192 + (512 x number of records in the save file).  There is room for approximately two thousand 512-byte records in 1 megabyte of space.  For example, to ensure that the save file does not exceed approximately 20 megabytes (20 x 2000), specify MAXRCDS (40000).  Note: The maximum amount of data that a save file can contain is approximately one terabyte (1,099,511,627,776 bytes).
    objectDescription_ = checkExistence();
    long value = ((Long)objectDescription_.getValue(ObjectDescription.OBJECT_SIZE)).longValue();
    return value;
  }

  /**
   Returns the name of the library where the save file is located on the system.
   @return The name of the library
   **/
  public String getLibrary()
  {
    return library_;
  }


  /**
   Returns the capacity (maximum number of records) of the save file.
   Returns {@link #NO_MAX NO_MAX} if there is no maximum.
   <br>If the save file doesn't exist on the system, an exception is thrown.
   @return The maximum number of records that the save file can hold.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public long getMaximumNumberOfRecords()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();
    if (fileAttributes_ == null) fileAttributes_ = getFileAttributes();

    BigDecimal decVal = (BigDecimal)fileAttributes_.getField("SASIZE");
    return decVal.longValue();
  }

  /**
   Returns the name of the save file on the system.
   @return The name of the save file
   **/
  public String getName()
  {
    return name_;
  }


  /**
   Returns an ObjectDescription instance representing the save file.  Various attributes of the save file can then be queried via the ObjectDescription.
   @return  An ObjectDescription representing the save file.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public ObjectDescription getObjectDescription()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    objectDescription_ = checkExistence();
    return objectDescription_;
  }


  /**
   Returns the fully-qualifed IFS pathname of the save file.
   Example: "/QSYS.LIB/MYLIB1.LIB/MYFILE1.SAVF"
   @return The path of the save file.
   **/
  public String getPath()
  {
    return path_;
  }


  /**
   Returns the system where the save file is located.
   @return The system.
   **/
  public AS400 getSystem()
  {
    return system_;
  }

  /**
   Returns the target release level of the operating system on which you intend to restore and use the object(s) saved in the save file by a subsequent invocation of one of the <tt>save()</tt> methods.
   The format VxRxMx is used to specify the target release, where Vx is the version, Rx is the release, and Mx is the modification level. For example, V5R2M0 is version 5, release 2, modification level 0.
   Special values include {@link #CURRENT_RELEASE CURRENT_RELEASE} and {@link #PREVIOUS_RELEASE PREVIOUS_RELEASE}.  The default is CURRENT_RELEASE.
   **/
  public String getTargetRelease()
  {
    return targetRelease_;
  }


  /**
   Returns the wait time for the save file.  This is the number of seconds to wait for the file resources and session resources to be allocated when the save file is opened, or for the device or session resources to be allocated when an acquire operation is performed to the save file.
   <br>Special values: {@link #IMMED IMMED}, {@link #CLS CLS}
   <br>If the save file doesn't exist on the system, an exception is thrown.
   @return The wait time.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public int getWaitTime()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();
    if (fileAttributes_ == null) fileAttributes_ = getFileAttributes();

    BigDecimal val = (BigDecimal)fileAttributes_.getField("SAWAIT");
    return val.intValue();
  }


  /**
   Indicates whether output from called API's will be saved in the job's spooled output.
   <br>By default, output is not saved.
   **/
  public boolean isSaveOutput()
  {
    return (saveOutput_.equals("*PRINT"));
  }

  /**
   Reports whether the open data path (ODP) for the save file is shared with other programs in the routing step.  When an ODP is shared, the programs accessing the file, share facilities such as the file status and the buffer.
   <br>The default is "not shared".
   <br>If the save file doesn't exist on the system, an exception is thrown.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public boolean isShared()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();
    if (fileAttributes_ == null) fileAttributes_ = getFileAttributes();

    String val = (String)fileAttributes_.getField("SASHAR");
    return (val.equals("Y") ? true : false);
  }



  /**
   Lists the contents of the save file.
   <br>Note: Due to a limitation in the underlying API (QSRLSAVF). files saved from the "root" file system ("/") cannot be listed.  Use the DSPSAVF to view the contents of such save files.
   @return The entries in the save file.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public SaveFileEntry[] listEntries()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, UnsupportedEncodingException
  {
    checkExistence();

// "List Save File" (QSRLSAVF) API:
//
//  Required Parameter Group:
//
// 1 	Qualified user space name 	Input 	Char(20)  <-  UserSpaceName / LibraryName
// 2 	Format name 	Input 	Char(8)                 <-  SAVF0200
// 3 	Qualified save file name 	Input 	Char(20)  <-  SaveFileName / LibraryName
// 4 	Object name filter 	Input 	Char(10)       <-  *ALL
// 5 	Object type filter 	Input 	Char(10)       <-  *ALL
// 6 	Continuation handle 	Input 	Char(36)       <-  <blanks>
// 7 	Error code 	I/O 	Char(*)
//
//  Default Public Authority: *USE
//
//  Threadsafe: No

    final int systemCCSID = system_.getCcsid();
    CharConverter conv = new CharConverter(systemCCSID);
    final AS400Text text36 = new AS400Text(36, systemCCSID);

    ProgramParameter[] parms = new ProgramParameter[7];

    parms[0] = new ProgramParameter(conv.stringToByteArray(USERSPACE_QUALIFIED_NAME));
    parms[1] = new ProgramParameter(conv.stringToByteArray("SAVF0200"));
    parms[2] = new ProgramParameter(conv.stringToByteArray(getNameAndLib()));
    parms[3] = new ProgramParameter(conv.stringToByteArray("*ALL      "));
    parms[4] = new ProgramParameter(conv.stringToByteArray("*ALL      "));
    parms[5] = new ProgramParameter(text36.toBytes(" "));
    parms[6] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QSRLSAVF.PGM", parms);
    byte[] buf = null;

    synchronized (USERSPACE_QUALIFIED_NAME)
    {
      // Create a user space in QTEMP to receive output.
      UserSpace space = new UserSpace(system_, USERSPACE_PATH);
      try
      {
        space.setMustUseProgramCall(true);
        space.setMustUseSockets(true);  // Must use sockets when running natively. We have to do it this way since UserSpace will otherwise make a native ProgramCall.
        space.create(256*1024, true, "", (byte)0, "User space for SaveFile", "*EXCLUDE");

        if (!pc.run()) {
          throw new AS400Exception(pc.getMessageList());
        }

        // Get the results from the user space.
        int size = space.getLength();
        buf = new byte[size];
        space.read(buf, 0);
      }

      finally {
        try { space.close(); }
        catch (Exception e) {
          Trace.log(Trace.ERROR, "Exception while closing temporary userspace", e);
        }
      }
    }

    // Parse the list data returned in the user space.

    int startingOffset = BinaryConverter.byteArrayToInt(buf, 124);      
    int numEntries = BinaryConverter.byteArrayToInt(buf, 132);
    int entrySize = BinaryConverter.byteArrayToInt(buf, 136);
    int entryCCSID = BinaryConverter.byteArrayToInt(buf, 140);

    if (entryCCSID == 0) entryCCSID = systemCCSID; // From the API spec: "The coded character set ID for data in the list entries.  If 0, then the data is not associated with a specific CCSID and should be treated as hexadecimal data."
    conv = new CharConverter(entryCCSID);

    String objName, libSaved, objType, extObjAttr;
    Date saveDateTime;
    int objSize, objSizeMult, asp;
    String dataSaved, objOwner, dloName, folder, desc, aspDevName;
    byte[] saveDateTimeBytes = new byte[8];

    SaveFileEntry[] entries = new SaveFileEntry[numEntries];
    int offset = 0;
    for (int i=0; i<numEntries; ++i)
    {
      offset = startingOffset + (i*entrySize); // offset to start of list entry

      objName = conv.byteArrayToString(buf, offset, 10).trim(); // Object name
      offset += 10;
      libSaved = conv.byteArrayToString(buf, offset, 10).trim(); // Library saved
      offset += 10;
      objType = conv.byteArrayToString(buf, offset, 10).trim(); // Object type
      offset += 10;
      extObjAttr = conv.byteArrayToString(buf, offset, 10).trim(); // Extended object attribute
      offset += 10;
      System.arraycopy(buf, offset, saveDateTimeBytes, 0, 8); // Save date and time (in system time-stamp format)
      saveDateTime = getAsSystemDate(saveDateTimeBytes);
      offset += 8;
      objSize = BinaryConverter.byteArrayToInt(buf, offset); // Object size
      offset += 4;
      objSizeMult = BinaryConverter.byteArrayToInt(buf, offset); // Object size multiplier
      offset += 4;
      asp = BinaryConverter.byteArrayToInt(buf, offset); // Auxiliary storage pool
      offset += 4;
      dataSaved = conv.byteArrayToString(buf, offset, 1); // Data saved
      offset += 1;
      objOwner = conv.byteArrayToString(buf, offset, 10).trim(); // Object owner
      offset += 10;
      dloName = conv.byteArrayToString(buf, offset, 20).trim(); // Document library object (DLO) name
      offset += 20;
      folder = conv.byteArrayToString(buf, offset, 63).trim(); // Folder
      offset += 63;
      desc = conv.byteArrayToString(buf, offset, 50).trim(); // Text description
      offset += 50;
      // Note: The "Auxiliary storage pool device name" field was added in V5R2.
      if (getSystemVRM() >= 0x00050200) {
        aspDevName = conv.byteArrayToString(buf, offset, 10).trim(); // 
      }
      else aspDevName = "";

      entries[i] = new SaveFileEntry(objName, libSaved, objType, extObjAttr, saveDateTime, objSize, objSizeMult, asp, dataSaved, objOwner, dloName, folder, desc, aspDevName);
    }

    return entries;
  }


  /**
   Lists the product loads in the save file.
   If the save file contains no product loads, returns an empty list.
   @return The product loads in the save file.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public Product[] listProducts()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();

// "List Product in a Save File" (QLPLPRDS) API:
//
// Required Parameter Group:
//
// 1 	Qualified user space name 	Input 	Char(20)
// 2 	Format name 	Input 	Char(8)
// 3 	Qualified save file name 	Input 	Char(20)
// 4 	Error code 	I/O 	Char(*)
//
// Default Public Authority: *USE
//
// Threadsafe: No

    final int systemCCSID = system_.getCcsid();
    CharConverter conv = new CharConverter(systemCCSID);

    ProgramParameter[] parms = new ProgramParameter[4];

    parms[0] = new ProgramParameter(conv.stringToByteArray(USERSPACE_QUALIFIED_NAME));
    parms[1] = new ProgramParameter(conv.stringToByteArray("PRDL0100"));
    parms[2] = new ProgramParameter(conv.stringToByteArray(getNameAndLib()));
    parms[3] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QLPLPRDS.PGM", parms);
    byte[] buf = null;

    synchronized (USERSPACE_QUALIFIED_NAME)
    {
      // Create a user space in QTEMP to receive output.
      UserSpace space = new UserSpace(system_, USERSPACE_PATH);
      try
      {
        space.setMustUseProgramCall(true);
        space.setMustUseSockets(true);  // Must use sockets when running natively. We have to do it this way since UserSpace will otherwise make a native ProgramCall.
        space.create(256*1024, true, "", (byte)0, "User space for SaveFile", "*EXCLUDE");

        if (!pc.run())
        {
          // Get the message id of the first message.
          AS400Message[] messageList = pc.getMessageList();
          String id = messageList[0].getID();

          // Handle a "No product found in save file" message.
          if (id.equalsIgnoreCase("CPF3D94"))
          {
            if (Trace.isTraceOn()) {
              Trace.log(Trace.INFORMATION, "No product found in save file");
            }
            return new Product[0];  // return an empty list
          }
          else
          {
            throw new AS400Exception(messageList);
          }
        }

        // Get the results from the user space.
        int size = space.getLength();
        buf = new byte[size];
        space.read(buf, 0);
      }

      finally {
        try { space.close(); }
        catch (Exception e) {
          Trace.log(Trace.ERROR, "Exception while closing temporary userspace", e);
        }
      }
    }

    // Parse the list data returned in the user space.

    int startingOffset = BinaryConverter.byteArrayToInt(buf, 124);      
    int numEntries = BinaryConverter.byteArrayToInt(buf, 132);
    int entrySize = BinaryConverter.byteArrayToInt(buf, 136);
    int entryCCSID = BinaryConverter.byteArrayToInt(buf, 140);

    if (entryCCSID == 0) entryCCSID = systemCCSID; // From the API spec: "The coded character set ID for data in the list entries. If 0, then the data is not associated with a specific CCSID and should be treated as hexadecimal data."
    conv = new CharConverter(entryCCSID);

    String prodID, releaseLevel, prodOption, loadType, langID;

    Product[] entries = new Product[numEntries];
    int offset = 0;
    for (int i=0; i<numEntries; ++i)
    {
      offset = startingOffset + (i*entrySize); // offset to start of list entry

      prodID = conv.byteArrayToString(buf, offset, 7).trim(); // Product ID
      offset += 7;
      releaseLevel = conv.byteArrayToString(buf, offset, 6).trim(); // Release level
      offset += 6;
      prodOption = conv.byteArrayToString(buf, offset, 4).trim(); // Product option
      offset += 4;
      loadType = conv.byteArrayToString(buf, offset, 10).trim(); // Load type
      offset += 10;
      langID = conv.byteArrayToString(buf, offset, 4).trim(); // Language ID

      entries[i] = new Product(system_, prodID, prodOption, releaseLevel, loadType, langID);
    }

    return entries;
  }


  /**
   Purges any cached attribute information about the save file.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void refresh()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    existence_ = EXISTENCE_UNKNOWN;  // force system lookup
    objectDescription_ = checkExistence();
    fileAttributes_ = getFileAttributes();
  }


  // Note: The RSTLIB rejects anything but a <library name> for the SAVLIB() argument, when we also specify the SAVF parameter.
  // "The SAVF parameter is not allowed when the value for either the LIB or SAVLIB parameter is specified as *ALLUSR, *IBM, or *NONSYS."
  // Note that RSTOBJ also requires a "saved library" parameter, if we specify OBJ(*ALL).


  /**
   Renames the save file.  The library is not changed.
   @param name The new name for the save file.  Example: "MYFILE1".

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void renameTo(String name)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (name == null) throw new NullPointerException("name");

    checkExistence();
    CommandCall cmd = new CommandCall(system_, "QSYS/RNM OBJ('"+path_+"') NEWOBJ("+name+".FILE)");
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
    name_ = name;
    path_ = QSYSObjectPathName.toPath(library_, name_, "FILE");
    fileAttributes_ = null;  // force a refresh
    objectDescription_ = null;
  }


  /**
   Restores a library that was saved into the save file.
   If the library does not exist, it is created.
   If the save file does not contain a library named <tt>libraryName</tt>, an exception is thrown.
   <br>Note: This method presents a subset of the functionality of the 'RSTLIB' CL command.  For full functionality, call the CL directly using the CommandCall class.
   @param libraryName  The library to be restored.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void restore(String libraryName)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (libraryName == null) throw new NullPointerException("libraryName");
    checkExistence();

    String cmdString = "QSYS/RSTLIB SAVLIB("+libraryName+") " +
      "DEV(*SAVF) SAVF("+library_+"/"+name_+") " +
      "OUTPUT("+saveOutput_+") ";
    if (DEBUG) System.out.println("Running command: " + cmdString);
    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   Restores objects that were saved into the save file.
   If the target library does not exist, it is created.
   If the save file does not contain the specified library or object, an exception is thrown.
   <br>Note: This method presents a subset of the functionality of the 'RSTOBJ' CL command.  For full functionality, call the CL directly using the CommandCall class.
   @param libraryName  The library from which the object was saved.
   @param objectList  The objects to be restored.  Objects are specified by simple object name.  For example: MYPROG1 or MYFILE2.
   @param toLibraryName  The library to which to restore the object.  If null, restores to the original library, that is, the library from which the object was saved.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void restore(String libraryName, String[] objectList, String toLibraryName)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (libraryName == null) throw new NullPointerException("libraryName");
    if (objectList == null) throw new NullPointerException("objectList");
    // Tolerate a null-valued toLibraryName.
    checkExistence();

    // Build the object list for the OBJ parameter.
    StringBuffer objects = new StringBuffer();
    for (int i=0; i<objectList.length; i++) {
      objects.append(objectList[i] + " ");
    }

    StringBuffer cmdBuf = new StringBuffer("QSYS/RSTOBJ OBJ("+objects.toString()+") " +
      "SAVLIB("+libraryName+") " +
      "DEV(*SAVF) SAVF("+library_+"/"+name_+") " +
      "OUTPUT("+saveOutput_+") ");
    if (toLibraryName != null) cmdBuf.append("RSTLIB("+toLibraryName+") ");
    if (DEBUG) System.out.println("Running command: " + cmdBuf.toString());
    CommandCall cmd = new CommandCall(system_);
    try
    {
      if (cmd.run(cmdBuf.toString())) {
        // The command succeeded, we're done.
        return;
      }

      // See if the failure was "Library xxx not found".  If so, create the library.
      AS400Message[] msgs = cmd.getMessageList();
      if (DEBUG) {
        for (int i=0; i<msgs.length; i++) {
          System.out.println(msgs[i].getID() + ": " + msgs[i].getText());
        }
      }
      if (msgs[0].getID().equals("CPF3781")) { // library not found
        // Attempt to create the library.
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Attempting to create library " + libraryName);
        if (!cmd.run("QSYS/CRTLIB LIB("+(toLibraryName == null ? libraryName : toLibraryName)+")"))
        {
          Trace.log(Trace.ERROR, new AS400Exception(cmd.getMessageList()));
        }
        // Retry the RSTOBJ.
        if (cmd.run(cmdBuf.toString())) {
          // The command succeeded, we're done.
          return;
        }
      }
      throw new AS400Exception(cmd.getMessageList());
    }
    catch (PropertyVetoException e) { Trace.log(Trace.ERROR, e); } // this will never happen
  }


  /**
   Restores a licenced program product that was saved into the save file.
   If the save file does not contain the specified product, an exception is thrown.
   <br>Note: This method presents a subset of the functionality of the 'RSTLICPGM' CL command.  For full functionality, call the CL directly using the CommandCall class.
   @param product  The product to be restored.

   @throws  AS400Exception  If the product is already installed, or if the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void restore(Product product)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (product == null) throw new NullPointerException("product");
    checkExistence();

    StringBuffer cmdBuf =
      new StringBuffer("QSYS/RSTLICPGM " +
                       "LICPGM("+product.getProductID()+") " +
                       "DEV(*SAVF) SAVF("+library_+"/"+name_+") " +
                       "OPTION("+getProductOption(product)+") " +
                       "RSTOBJ("+getObjectType(product)+") " +
                       "RLS("+product.getReleaseLevel()+") " +
                       "OUTPUT("+saveOutput_+") ");

    String lang = product.getPrimaryLanguageFeatureID();
    if (lang != null && lang.length() != 0) {
      cmdBuf.append("LNG("+lang+") ");
    }

    if (DEBUG) System.out.println("Running command: " + cmdBuf.toString());
    CommandCall cmd = new CommandCall(system_, cmdBuf.toString());
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  // Design note: If a SAVxxx command is called with CLEAR(*NONE) and the save file already contains data, then an enquiry message is sent:
  //  SAVE FILE [fileName] IN [libraryName] ALREADY CONTAINS DATA. (C G)
  // This will cause a hang, since we're not running interactive.
  // So the easiest solution is just to always specify CLEAR(*ALL)


  /**
   Saves a library into the save file.
   Any existing data in the save file is cleared.
   If libraryName does not specify an existing library, an exception is thrown.
   <br>Note: This method presents a subset of the functionality of the 'SAVLIB' CL command.  For full functionality, call the CL directly using the CommandCall class.
   @param libraryName  The library to be saved.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void save(String libraryName)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (libraryName == null) throw new NullPointerException("libraryName");
    checkExistence();

    String cmdString = "QSYS/SAVLIB LIB("+libraryName+") " +
      "DEV(*SAVF) SAVF("+library_+"/"+name_+") " +
      "TGTRLS("+targetRelease_+") " +
      "CLEAR(*ALL) " +
      "OUTPUT("+saveOutput_+") ";
    if (DEBUG) System.out.println("Running command: " + cmdString);
    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   Saves objects into the save file.
   Any existing data in the save file is cleared.
   If a specified library or object does not exist, an exception is thrown.
   <br>Note: This method presents a subset of the functionality of the 'SAVOBJ' CL command.  For full functionality, call the CL directly using the CommandCall class.
   @param libraryName  The library where the object resides.
   @param objectList  The objects to be saved.  Objects are specified by simple object name.  For example: MYPROG1 or MYFILE2.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void save(String libraryName, String[] objectList)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (libraryName == null) throw new NullPointerException("libraryName");
    if (objectList == null) throw new NullPointerException("objectList");
    checkExistence();

    // Build the object list for the OBJ parameter.
    StringBuffer objects = new StringBuffer();
    for (int i=0; i<objectList.length; i++) {
      objects.append(objectList[i] + " ");
    }

    // Build the command string.
    String cmdString = "QSYS/SAVOBJ OBJ("+objects.toString()+") " +
      "LIB("+libraryName+") " +
      "DEV(*SAVF) SAVF("+library_+"/"+name_+") " +
      "TGTRLS("+targetRelease_+") " +
      "CLEAR(*ALL) " +
      "OUTPUT("+saveOutput_+") ";
    if (DEBUG) System.out.println("Running command: " + cmdString);
    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   Saves files from the root file system into the save file.  The objects are specified by their fully qualified pathname in the Integrated File System.  Wildcard characters ("*") may be used.
   Any existing data in the save file is cleared.
   If a specified object does not exist, an exception is thrown.
   <br>Note: The objects must all reside in the root file system ("/").  This method will throw an exception if pathList specifies files under QSYS or QDLS.
   <br>Note: This method presents a subset of the functionality of the 'SAV' CL command.  For full functionality, call the CL directly using the CommandCall class.
   @param pathList  The objects to be saved; objects are specified by path.  For example: "/myDirectory/myFile" or "/myDirectory/*".

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void save(String[] pathList)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (pathList == null) throw new NullPointerException("pathList");
    checkExistence();

    // Build the path list for the OBJ parameter.
    StringBuffer paths = new StringBuffer();
    for (int i=0; i<pathList.length; i++) {
      paths.append("('" + pathList[i] + "') ");
    }

    // Build the command string.
    String cmdString = "QSYS/SAV DEV('"+path_+"') " +
      "OBJ("+paths.toString()+") " +
      "TGTRLS("+targetRelease_+") " +
      "CLEAR(*ALL) " +
      "OUTPUT("+saveOutput_+") ";

    if (DEBUG) System.out.println("Running command: " + cmdString);
    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   Saves a copy of all of the objects that make up a licenced program product, into the save file.
   Any existing data in the save file is cleared.
   If the product does not exist, an exception is thrown.
   <br>Note: Depending on the size of the product, this method may take a <em>very</em> long time to complete.
   <br>Note: This method presents a subset of the functionality of the 'SAVLICPGM' CL command.  For full functionality, call the CL directly using the CommandCall class.
   @param product  The product to be saved.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void save(Product product)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (product == null) throw new NullPointerException("product");
    checkExistence();

    StringBuffer cmdBuf =
      new StringBuffer("QSYS/SAVLICPGM " +
                       "LICPGM("+product.getProductID()+") " +
                       "DEV(*SAVF) SAVF("+library_+"/"+name_+") " +
                       "OPTION("+getProductOption(product)+") " +
                       "RLS("+product.getReleaseLevel()+") " +
                       "OBJTYPE("+getObjectType(product)+") " +
                       "TGTRLS("+targetRelease_+") " +
                       "CLEAR(*ALL) ");

    String lang = product.getPrimaryLanguageFeatureID();
    if (lang != null && lang.length() != 0) {
      cmdBuf.append("LNG("+lang+") ");
    }

    // Note: This API has no OUTPUT parameter.

    if (DEBUG) System.out.println("Running command: " + cmdBuf.toString());
    CommandCall cmd = new CommandCall(system_, cmdBuf.toString());
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   Sets the text description of the save file.
   If the save file doesn't exist on the system, an exception is thrown.
   @param description  The description.
   Maximum length is 50 characters.
 
   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
  **/
  public void setDescription(String description)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (description == null) throw new NullPointerException("description");
    objectDescription_ = checkExistence();

    String cmdString = "QSYS/CHGSAVF FILE("+library_+"/"+name_+") TEXT('"+description+"') ";
    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
    objectDescription_ = null;  // force a refresh on next getDescription()
  }

  /**
   Sets the capacity (maximum number of records) of the save file.
   <br>If the current number of records in the save file is greater than the new maximumNumberOfRecords value, an exception is thrown, and the save file is not changed.
   <br>Special value: {@link #NO_MAX NO_MAX}
   <br>If the save file doesn't exist on the system, an exception is thrown.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void setMaximumNumberOfRecords(long maximumNumberOfRecords)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();
    // Note: We let the system validate the argument.

    String value;
    if (maximumNumberOfRecords == NO_MAX) { value = "*NOMAX"; }
    else                 { value = Long.toString(maximumNumberOfRecords); }

    String cmdString = "QSYS/CHGSAVF FILE("+library_+"/"+name_+") MAXRCDS("+value+") ";
    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
    fileAttributes_ = null;  // force a refresh
  }


  /**
   Indicates whether output from called API's will be saved in the job's spooled output.  The job will be that of the Remote Command Host Server, job QUSRWRK/QZRCSRVS.  The saved information may be useful when analyzing errors.
   <br>By default, output is not saved.
   @param save Whether output should be saved.
   **/
  public void setSaveOutput(boolean save)
  {
    saveOutput_ = (save == true ? "*PRINT" : "*NONE");
  }

  /**
   Sets whether the open data path (ODP) for the save file is shared with other programs in the routing step.  When an ODP is shared, the programs accessing the file can share facilities such as the file status and the buffer.  For more details refer to the specification of the <tt>CHGSAVF</tt> CL command in the IBM i reference.
   If the save file doesn't exist on the system, an exception is thrown.
   <br>The default is "not shared".
   @param shared Whether ODP is shared.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void setShared(boolean shared)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();

    String value = (shared ? "*YES" : "*NO");

    String cmdString = "QSYS/CHGSAVF FILE("+library_+"/"+name_+") SHARE("+value+") ";
    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
    fileAttributes_ = null;  // force a refresh
  }


  /**
   Sets the target release level of the operating system on which you intend to restore and use the object(s) saved in the save file by a subsequent invocation of one of the <tt>save()</tt> methods.
   The format VxRxMx is used to specify the target release, where Vx is the version, Rx is the release, and Mx is the modification level. For example, V5R2M0 is version 5, release 2, modification level 0.
   <br>The object(s) in the save file can be restored to a system with the specified release or with any subsequent release of the operating system installed.  Valid values depend on the system's current version, release, and modification level.
   Special values include {@link #CURRENT_RELEASE CURRENT_RELEASE} and {@link #PREVIOUS_RELEASE PREVIOUS_RELEASE}.  The default is CURRENT_RELEASE.
   @param targetRelease The target release.
   **/
  public void setTargetRelease(String targetRelease)
  {
    if (targetRelease == null) throw new NullPointerException("targetRelease");
    targetRelease_ = targetRelease;
  }


  /**
   Sets the number of seconds to wait for the file resources and session resources to be allocated when the save file is opened, or for the device or session resources to be allocated when an acquire operation is performed to the save file.
   <br>The default is {@link #IMMED IMMED}.
   <br>If the save file doesn't exist on the system, an exception is thrown.
   @param seconds The wait time.

   @throws  AS400Exception  If the program call returns error messages.
   @throws  AS400SecurityException  If a security or authority error occurs.
   @throws  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @throws  InterruptedException  If this thread is interrupted.
   @throws  IOException  If an error occurs while communicating with the system.
   @throws  ObjectDoesNotExistException  If the object does not exist on the system.
   **/
  public void setWaitTime(int seconds)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    checkExistence();
    // Note: We let the system validate the argument.

    String value;
    switch (seconds) {
      case DEFAULT:
      case IMMED:
        value = "*IMMED";
        break;
      case CLS:
        value = "*CLS";
        break;
      default:
        value = Integer.toString(seconds);
    }
    String cmdString = "QSYS/CHGSAVF FILE("+library_+"/"+name_+") WAITFILE("+value+") ";
    CommandCall cmd = new CommandCall(system_, cmdString);
    if (!cmd.run()) {
      throw new AS400Exception(cmd.getMessageList());
    }
    fileAttributes_ = null;  // force a refresh
  }


  /**
   Returns the String representation of this object.
   @return  The String representation of this object.
   **/
  public String toString()
  {
    return "SaveFile (system: " + system_.getSystemName() + "; path: "+ path_ + "): " + super.toString();
  }




  // Helper methods.




  /**
   Returns the "qualified save file name" for use in API calls.
   **/
  private String getNameAndLib()
  {
    if (savefileNameAndLib_ == null)
    {
      StringBuffer buff = new StringBuffer("                    ");  // initialize to 20 blanks
      buff.replace(0,  name_.length(), name_);
      buff.replace(10, 10+library_.length(), library_);
      savefileNameAndLib_ = buff.toString();
    }
    return savefileNameAndLib_;
  }

  /**
   Converts an 8-byte system timestamp value into a Date object.
   **/
  private Date getAsSystemDate(byte[] data)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return new DateTimeConverter(system_).convert(data, "*DTS");
  }



  /**
   Generates a record containing the save file attributes.
   **/
  private Record getFileAttributes()
    throws AS400Exception, AS400SecurityException, InterruptedException, IOException
  {
    AS400FileRecordDescriptionImplRemote recDesc = new AS400FileRecordDescriptionImplRemote();
    recDesc.setSystem(system_.getImpl());
    recDesc.setPath(path_);
    return recDesc.getSavefileAttributes();
  }


  /**
   Returns the system VRM.
   **/
  private int getSystemVRM()
    throws AS400SecurityException, IOException
  {
    if (!gotSystemVRM_) { systemVRM_ = system_.getVRM(); gotSystemVRM_ = true; }
    return systemVRM_;
  }


  /**
   Gets an ObjectDescription object representing the save file.
   **/
  private ObjectDescription getObjDesc()
  {
    return new ObjectDescription(system_, library_, name_, "FILE");
  }


  /**
   Deserializes and initializes transient data.
   **/
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
  {
    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "De-serializing SaveFile object.");
    in.defaultReadObject();
  }


  /**
   Checks for existence of the save file on the system.
   **/
  private ObjectDescription checkExistence()
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    // If we've already checked, use cached state, assume it's still current.
    if (existence_ == EXISTENCE_YES && objectDescription_ != null) {
      return objectDescription_;
    }

    // Send query to the system.
    if (objectDescription_ == null) { objectDescription_ = getObjDesc(); }
    if (objectDescription_.exists()) return objectDescription_;
    else {
      throw new ObjectDoesNotExistException(path_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
    }
  }


  // Gets the "load type" from the product, and converts it to an "object type".
  private static String getObjectType(Product product)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    String type = product.getLoadType();
    if (type == null || type.length() == 0) { type = "*ALL"; }
    else if (type.equals(Product.LOAD_TYPE_CODE)) { type = "*PGM"; }
    else if (type.equals(Product.LOAD_TYPE_LANGUAGE)) { type = "*LNG"; }
    else {
      Trace.log(Trace.ERROR, "Unrecognized load type in Product: " + type);
      type = "*ALL";
    }
    return type;
  }


  // Gets the "product option" from the product, and strips leading zero's.
  private static String getProductOption(Product product)
    throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    String option = product.getProductOption();
    if (option == null) {
      Trace.log(Trace.ERROR, "Product.getProductOption() returned null.");
      return "";
    }
    // Strip any leading zero's.
    for (int i=0; i<option.length(); i++) {
      if (option.charAt(i) != '0') return option.substring(i);
    }
    return "0";  // this covers the case of "0000"
  }

}
