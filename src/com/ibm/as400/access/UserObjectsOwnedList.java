///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserObjectsOwnedList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2008 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

 
/**
Allows you to retrieve a list of i5/OS objects that a user is authorized to and/or list of i5/OS objects that the user owns.  
<br>Refer to the QSYLOBJA API for additional information.
<p>
The list of authorized objects only includes objects the user is specifically authorized to. 
The list does not include objects the user is solely authorized to because:
<ul> 
 <li>The user is part of a group that is authorized
 <li>The user can access the object using the public authority
 <li>The object is secured with an authorization list the user is authorized to
 <li>The user can access the object using adopted authority
</ul>
<p>Example code:
<ul> 
<pre>
      AS400 system = new AS400("sysname", "userid", "password");
      
      UserObjectsOwnedList list1 = new UserObjectsOwnedList(system, "USER1", UserObjectsOwnedList.SELECTION_FILE_SYSTEM_LIBRARY, UserObjectsOwnedList.SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED);
      UserObjectsOwnedListEntry[] entries1 = list1.getObjectList();
      System.out.println(list1);
      for (int i=0; i < entries1.length; ++i)
      {
        System.out.println("Entry["+i+"/"+entries1.length+"]= "+entries1[i]);
      }      
      list1.setSelectionObjectRelation(UserObjectsOwnedList.SELECTION_OBJECT_RELATION_OWNED);
      entries1 = list1.getObjectList();
      System.out.println(list1);
      for (int i=0; i < entries1.length; ++i)
      {
        System.out.println("Entry["+i+"/"+entries1.length+"]= "+entries1[i]);
      }      
</pre>
</ul> 

 @see  com.ibm.as400.access.User#getObjectsOwned
**/
public class UserObjectsOwnedList
{
  private static final String USERSPACE_NAME = "JT4SYLOBJAQTEMP     ";
  private static final String USERSPACE_PATH = "/QSYS.LIB/QTEMP.LIB/JT4SYLOBJA.USRSPC";
  private static final String CONTINUATION_HANDLE_BLANKS = "                    "; // 20 Blanks

  /**
  Selection value indicating to select i5/OS objects that reside in the QSYS library based file system.
   **/
  public  static final int    SELECTION_FILE_SYSTEM_LIBRARY      = 0;
  private static final String SELECTION_FILE_SYSTEM_LIBRARY_0300 = "OBJA0300";
  /**
  Selection value indicating to select i5/OS objects that reside in a directory (non-QSYS) based file system.
   **/
  public  static final int    SELECTION_FILE_SYSTEM_DIRECTORY      = 1;
  private static final String SELECTION_FILE_SYSTEM_DIRECTORY_0310 = "OBJA0310";
  /**
  Selection value indicating to select the list of objects the user is authorized to
   **/
  public  static final int    SELECTION_OBJECT_RELATION_AUTHORIZED      = 0;
  private static final String SELECTION_OBJECT_RELATION_AUTHORIZED_PRIV = "*OBJAUT   ";
  /**
  Selection value indicating to select the list of objects the user owns.
   **/
  public  static final int    SELECTION_OBJECT_RELATION_OWNED       = 1;
  private static final String SELECTION_OBJECT_RELATION_OWNED_PRIV  = "*OBJOWN   ";
  /**
  Selection value indicating to select the both list of objects the user is authorized to and the list of objects the user owns.
   **/
  public  static final int    SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED         = 2;
  private static final String SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED_PRIV    = "*BOTH     ";
  /**
  Selection value indicating the object type to request
   **/
  private static final String SELECTION_OBJECT_TYPE_ALL_PRIV = "*ALL      ";

  private final static ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);


  private AS400  system_;
  private String userName_;
  private String selectionFileSystem_;
  private String selectionObjectRelation_;

                                                         
  /**
  Constructs a UserObjectsOwnedList object. 
  <br>You must call {@link #getObjectList() getObjectList()} method to retrieve the object list.
  <br>Default values are set to select library based objects which the user owns.
  <br>The <i>selectionFileSystem</i> parameter defaults to {@link #SELECTION_FILE_SYSTEM_LIBRARY SELECTION_FILE_SYSTEM_LIBRARY}, 
  the <i>selectionObjectRelation</i> parameter defaults to {@link #SELECTION_OBJECT_RELATION_OWNED SELECTION_OBJECT_RELATION_OWNED}.
  @param system The system upon which the user resides.
  @param userName The user name which owns the objects to be returned.
 **/
 public UserObjectsOwnedList(AS400 system, String userName)
 {
   this(system, userName, SELECTION_FILE_SYSTEM_LIBRARY, SELECTION_OBJECT_RELATION_OWNED); 
 }

 /**
 Constructs a UserObjectsOwnedList object.  
 <br>You must call {@link #getObjectList() getObjectList()} method to retrieve the object list.
 @param system The system upon which the user resides.
 @param userName The user name which owns (or is authorized to) the objects to be returned.
 @param selectionFileSystem The format name.   Possible values are:
 <ul>
   <li>{@link #SELECTION_FILE_SYSTEM_LIBRARY SELECTION_FILE_SYSTEM_LIBRARY} - QSYS objects in the QSYS library based file system.
   <li>{@link #SELECTION_FILE_SYSTEM_DIRECTORY SELECTION_FILE_SYSTEM_DIRECTORY} - IFS objects that reside in a directory.
 </ul>
 @param selectionObjectRelation The objects to return.  Possible values are:
 <ul>
   <li>{@link #SELECTION_OBJECT_RELATION_AUTHORIZED SELECTION_OBJECT_RELATION_AUTHORIZED} - select the list of objects the user is authorized to
   <li>{@link #SELECTION_OBJECT_RELATION_OWNED SELECTION_OBJECT_RELATION_OWNED} - select the  list of objects the user owns
   <li>{@link #SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED} - select the list of objects the user is authorized to and the list of objects the user owns
 </ul>
**/
public UserObjectsOwnedList(AS400 system, String userName, int selectionFileSystem, int selectionObjectRelation)
{
  setSystem(system);
  setUserName(userName);
  setSelectionFileSystem(selectionFileSystem);
  setSelectionObjectRelation(selectionObjectRelation);
  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, this.toString());
}

  /**
   Returns a list of all UserObjectsOwnedListEntry i5/OS objects based on the current 
   selection criteria for the file system and object relation.
   <p>This method retrieves the list of objects from the system based on the selection critera set via the constructor or
   modified by any of the set methods.
   @return The array of objects retrieved from the system.
  **/
  public UserObjectsOwnedListEntry[] getObjectList()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    final int systemCCSID = system_.getCcsid();
    CharConverter conv = new CharConverter(systemCCSID);

    ProgramParameter[] parms = new ProgramParameter[7];

    parms[0] = new ProgramParameter(conv.stringToByteArray(USERSPACE_NAME)); //Qualified user space name
    parms[1] = new ProgramParameter(conv.stringToByteArray(selectionFileSystem_));     // Format Name "OBJA0300" or "OBJA0310"

    StringBuffer userNameBuff = new StringBuffer("          ");  // initialize to 10 blanks
    userNameBuff.replace(0,  userName_.length(), userName_);
    parms[2] = new ProgramParameter(conv.stringToByteArray(userNameBuff.toString()));   // User profile name
    parms[3] = new ProgramParameter(conv.stringToByteArray(SELECTION_OBJECT_TYPE_ALL_PRIV));   // Object type filter
    parms[4] = new ProgramParameter(conv.stringToByteArray(selectionObjectRelation_));   // Returned objects (Authorized to, Owns, or Both)
    parms[5] = new ProgramParameter(conv.stringToByteArray(CONTINUATION_HANDLE_BLANKS));   // Continuation Handle
    parms[6] = errorCode_;

    // QSYLOBJA is the API that is being used to get the object list into a user space. 
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QSYLOBJA.PGM", parms);

    // Determine the needed scope of synchronization.
    Object lockObject;
    boolean willRunProgramsOnThread = pc.isStayOnThread();
    if (willRunProgramsOnThread) {
      // The calls will run in the job of the JVM, so lock for entire JVM.
      lockObject = USERSPACE_NAME;
    }
    else {
      // The calls will run in the job of the Remote Command Host Server, so lock on the connection.
      lockObject = system_;
    }

    byte[] buf = null;

    synchronized (lockObject)
    {
      // Create a user space in QTEMP to receive output.
      UserSpace space = new UserSpace(system_, USERSPACE_PATH);
      try
      {
        space.setMustUseProgramCall(true);
        if (!willRunProgramsOnThread)
        {
          space.setMustUseSockets(true);
          // Force the use of sockets when running natively but not on-thread.
          // We have to do it this way since UserSpace will otherwise make a native ProgramCall, and will use a different QTEMP library than that used by the host server.
        }
        space.create(256*1024, true, "", (byte)0, "User space for UserObjectsOwnedList", "*EXCLUDE");
        // Note: User Spaces by default are auto-extendible (by QUSCRTUS API)
        //       So it will always have enough space available.
        //       Allocated 256K bytes as a reasonable initial size (1500+ entries)
        
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

    // --------------------------------------------------------------------------------------------
    // QSYLOBJA (List Objects User is Authorized to, Owns, or Is Primary Group of) is a "list" API.  
    // It puts the list of objects in a user space.  In addition, to the QSYLOBJA documentation,  
    // the developer must refer to additional documentation describing the data returned:
    //  - User spaces: List APIs return data to user spaces.  To provide a consistent design and
    //    use of user space objects, the list APIs use a general data structure.
    //  - General data structure & Common data structure format (General header format 0100)
    //    - This format in info center describes the general header format (referenced in the 
    //      code below).
    //  - This information is/was documented in info center under the following:
    //    - Programming -> API Concepts -> User spaces and receiver variables -> User spaces
    // --------------------------------------------------------------------------------------------
    // Parse the list data returned in the user space.
    int headerOffset   = BinaryConverter.byteArrayToInt(buf, 116);       // General header - Offset to header section      
    int startingOffset = BinaryConverter.byteArrayToInt(buf, 124);       // General header - Offset to list data section      
    int numEntries     = BinaryConverter.byteArrayToInt(buf, 132);       // General header - Number of list entries
    int entrySize      = BinaryConverter.byteArrayToInt(buf, 136);       // General header - Size of each entry  
    int entryCCSID     = BinaryConverter.byteArrayToInt(buf, 140);       // General header - CCSID of data in the list entries
    //String subsettedListIndicator = conv.byteArrayToString(buf, 149, 1); // General header - Subsetted list indicator  
    String informationStatus = conv.byteArrayToString(buf, 103, 1);      // General header - info status
    // informationStatus - 'C'=Complete, 'I'=Incomplete, 'P'=Partial
    //   - refer to QSYLOBJA documentation regarding the "Continuation Handle" for more info
    //     - see comment below regarding the continuationHandle      

    if (entryCCSID == 0) entryCCSID = systemCCSID; // From the API spec: "The coded character set ID for data in the list entries.  If 0, then the data is not associated with a specific CCSID and should be treated as hexadecimal data."
    conv = new CharConverter(entryCCSID);

    String objectName,libraryName,objectType,authorityHolder,ownership;
    String authorityValue,authorityListManagement,objectOperational,objectManagement;
    String objectExistence,dataRead,dataAdd,dataUpdate,dataDelete,attribute;
    String textDescription,dataExecute,objectAlter,objectReference,aspDeviceNameOfLibrary;
    String aspDeviceNameOfObject,pathName;

    // -----------------------------------------------------------------------------------------
    // Extract fields from the QSYLOBJA Specific Header
    // -----------------------------------------------------------------------------------------
    //String hdrUserProfile    = conv.byteArrayToString(buf, headerOffset+0,  10).trim(); // QSYLOBJA Specific Header - User profile name
    // There is currently no need to make use of the returned "continuationHandle".  Since    
    // the user space above is set to "auto-extend", there user space will grow as needed.  
    String continuationHandle= conv.byteArrayToString(buf, headerOffset+10, 20).trim(); // QSYLOBJA Specific Header - Continuation Handle
    // The reason code may have the following values:
    //       0000 = list returned in the user space contains all objects meeting the search criteria.
    //       0001 = Objects were found that meet the search criteria but could not be included in the returned list. 
    //              The requested format could not handle path names for directory objects
    //              (Normal behavior when requesting FMTOBJA0300 format but IFS objects could be returned)
    //       0002 = Objects were found that meet the search criteria but could not be included in the returned list. 
    //              The requested format could not handle objects found in library QSYS
    //              (Normal behavior when requesting FMTOBJA0310 format but QSYS objects could be returned)
    //       0003 = Directory objects were found but did not have links to them
    // There is no plan to make use of (nor make available to the caller)the reasonCode. 
    int reasonCode = BinaryConverter.byteArrayToInt(buf, headerOffset+30); // QSYLOBJA Specific Header - Reason code
    
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "UserObjectsOwnedList.getObjectList()  informationStatus='"+informationStatus+"' reasonCode='"+reasonCode+"' continuationHandle='"+continuationHandle+"' numEntries='"+numEntries+"'");
      
    UserObjectsOwnedListEntry[] entries = new UserObjectsOwnedListEntry[numEntries];
    
    // Extract the fields from each entry in the list
    int offset = startingOffset;
    for (int i=0; i<numEntries; ++i)
    {
      if (selectionFileSystem_.equals(SELECTION_FILE_SYSTEM_LIBRARY_0300)) // QSYS Object format
      {
        // Refer to the QSYLOBJA API documentation of the OBJA0300 format
        // Extract format OBJA0310 fields
        offset = startingOffset + (i*entrySize); // offset to start of list entry
        
        objectName = conv.byteArrayToString(buf, offset, 10).trim();     // Object name
        offset += 10;
        libraryName = conv.byteArrayToString(buf, offset, 10).trim();    // Library name
        offset += 10;
        objectType = conv.byteArrayToString(buf, offset, 10).trim();     // Object type
        offset += 10;
        authorityHolder = conv.byteArrayToString(buf, offset, 1).trim();  
        offset += 1;
        ownership = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        authorityValue = conv.byteArrayToString(buf, offset, 10).trim(); // 
        offset += 10;
        authorityListManagement = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectOperational = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectManagement = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectExistence = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        dataRead = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        dataAdd = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        dataUpdate = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        dataDelete = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        attribute = conv.byteArrayToString(buf, offset, 10).trim(); // 
        offset += 10;
        textDescription = conv.byteArrayToString(buf, offset, 50).trim(); // 
        offset += 50;
        dataExecute = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        offset += 10; // Skip over reserved(10 byte) field
        objectAlter = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectReference = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        aspDeviceNameOfLibrary = conv.byteArrayToString(buf, offset, 10).trim(); // 
        offset += 10;
        aspDeviceNameOfObject = conv.byteArrayToString(buf, offset, 10).trim(); // 
        offset += 10;
        
        // pathName = ""; // Not valid for format OBJA0300

        // Construct a UserObjectsOwnedListEntry() for a QSYS (non-directory) based object
        entries[i] = new UserObjectsOwnedListEntry(objectName,libraryName,objectType,authorityHolder,ownership,authorityValue,authorityListManagement,objectOperational,objectManagement,objectExistence,dataRead,dataAdd,dataUpdate,dataDelete,attribute,textDescription,dataExecute,objectAlter,objectReference,aspDeviceNameOfLibrary,aspDeviceNameOfObject);
      }
      else //Format FMTOBJA0310 - Directory object format
      {
        // Refer to the QSYLOBJA API documentation of the OBJA0310 format
        int offsetToPathName = BinaryConverter.byteArrayToInt(buf, offset);
        offset += 4;
        int lengthPathName = BinaryConverter.byteArrayToInt(buf, offset);
        offset += 4;
        
        //objectName  = ""; // Not valid for format OBJA0310
        //libraryName = ""; // Not valid for format OBJA0310

        objectType = conv.byteArrayToString(buf, offset, 10).trim();     // Object type
        offset += 10;
        authorityHolder = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        ownership = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        authorityValue = conv.byteArrayToString(buf, offset, 10).trim(); // 
        offset += 10;
        authorityListManagement = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectOperational = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectManagement = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectExistence = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectAlter = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        objectReference = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        offset += 10; // Skip over reserved(10 byte) field
        
        dataRead = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        dataAdd = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        dataUpdate = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        dataDelete = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        dataExecute = conv.byteArrayToString(buf, offset, 1).trim(); // 
        offset += 1;
        offset += 10; // Skip over reserved(10 byte) field

        attribute = conv.byteArrayToString(buf, offset, 10).trim(); // 
        offset += 10;
        textDescription = conv.byteArrayToString(buf, offset, 50).trim(); // 
        offset += 50;
        //aspDeviceNameOfLibrary = ""; // Not valid for format OBJA0310
        aspDeviceNameOfObject = conv.byteArrayToString(buf, offset, 10).trim(); // 
        offset += 10;
        
        // pathName may be in a different CCSID, so use that CCSID for character conversion.
        final int pathCCSID = BinaryConverter.byteArrayToInt(buf, offsetToPathName+0);
        CharConverter pathConv = new CharConverter(pathCCSID);
        
        pathName = pathConv.byteArrayToString(buf, offsetToPathName+32, lengthPathName-32).trim(); // 
        offset += 10;
        
        offset = offsetToPathName + lengthPathName; // offset to start of next list entry

        // Construct a UserObjectsOwnedListEntry() for an IFS directory (non-QSYS) based object
        entries[i] = new UserObjectsOwnedListEntry(objectType,authorityHolder,ownership,authorityValue,authorityListManagement,objectOperational,objectManagement,objectExistence,dataRead,dataAdd,dataUpdate,dataDelete,attribute,textDescription,dataExecute,objectAlter,objectReference,aspDeviceNameOfObject,pathName);
      }
      
    }

    return entries;
  }
  
  
  /**
   Sets the system from which to retrieve the list of objects that a user is authorized to and the list of objects the user owns.
   <p>This method changes the value set via the constructor, allowing subsequent calls to 
   {@link #getObjectList() getObjectList()} method to retrieve a different list of objects.
   @param system The system upon which the user resides.
  **/
  public void setSystem(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
  }

  /**
   Returns the system from which to retrieve the list of objects that a user is authorized to and the list of objects the user owns.
   @return The system upon which the user resides.
  **/
  public AS400 getSystem()
  {
      return system_;
  }
  /**
   Sets the user name for which to retrieve the list of objects that the user is authorized to and/or the list of objects the user owns.
   <p>This method changes the value set via the constructor, allowing subsequent calls to 
   {@link #getObjectList() getObjectList()} method to retrieve a different list of objects.
   @param userName The user name which owns (or is authorized to) the objects to be returned.
  **/
  public void setUserName(String userName)
  {
    if (userName == null) throw new NullPointerException("userName");
    userName_ = userName;
  }

  /**
   Returns the user name for which to retrieve the list of objects that a user is authorized to and list of objects the user owns.
   @return The user name which owns (or is authorized to) the objects to be returned.
  **/
  public String getUserName()
  {
      return userName_;
  }
  
  
  /**
   Sets the selection criteria for the file system from which to retrieve the list of objects that the user is authorized to or the list of objects the user owns.
   <p>This method changes the value set via the constructor, allowing subsequent calls to 
   {@link #getObjectList() getObjectList()} method to retrieve a different list of objects.
   @param selectionFileSystem The format name.  Valid values:
   <ul>
     <li>{@link #SELECTION_FILE_SYSTEM_LIBRARY SELECTION_FILE_SYSTEM_LIBRARY} - objects that reside in an i5/OS library
     <li>{@link #SELECTION_FILE_SYSTEM_DIRECTORY SELECTION_FILE_SYSTEM_DIRECTORY} - objects that reside in an i5/OS directory
   </ul>
  **/
  public void setSelectionFileSystem(int selectionFileSystem)
  {
    // The public const values are passed on the interface, but the private constant values
    // are stored in this object.
    switch (selectionFileSystem)
    {
      case SELECTION_FILE_SYSTEM_LIBRARY:
        selectionFileSystem_ = SELECTION_FILE_SYSTEM_LIBRARY_0300;
        break;
      case SELECTION_FILE_SYSTEM_DIRECTORY:
        selectionFileSystem_ = SELECTION_FILE_SYSTEM_DIRECTORY_0310;
        break;
      default:
      {
        Trace.log(Trace.ERROR, "UserObjectsOwnedList.setSelectionFileSystem() PARAMETER_VALUE_NOT_VALID");
        throw new ExtendedIllegalArgumentException("selectionFileSystem", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
    }
  }

  
  /**
  Returns the current selection criteria for the file system from which to retrieve the list of objects that a user is authorized to or list of objects the user owns.
  @return the selection file system.  Possible values:
   <ul>
     <li>{@link #SELECTION_FILE_SYSTEM_LIBRARY SELECTION_FILE_SYSTEM_LIBRARY} - objects that reside in an i5/OS library
     <li>{@link #SELECTION_FILE_SYSTEM_DIRECTORY SELECTION_FILE_SYSTEM_DIRECTORY} - objects that reside in an i5/OS directory
   </ul>
  **/
  public int getSelectionFileSystem()
  {
    // The public const values are passed on the interface, but the private constant values
    // are stored in this object.
    if (selectionFileSystem_.equals(SELECTION_FILE_SYSTEM_LIBRARY_0300))
      return (SELECTION_FILE_SYSTEM_LIBRARY);
    else // OBJECT_FORMAT_IFS_0310
      return (SELECTION_FILE_SYSTEM_DIRECTORY);
  }
  
  
  /**
   Sets which objects are to be selected (indicating objects authorized to, objects owned, or both).
   <p>This method changes the value set via the constructor, allowing subsequent calls to 
   {@link #getObjectList() getObjectList()} method to retrieve a different list of objects.
   @param selectionObjectRelation Which objects are to be selected . Valid values: 
   <ul>
     <li>{@link #SELECTION_OBJECT_RELATION_AUTHORIZED SELECTION_OBJECT_RELATION_AUTHORIZED} - select a list of objects the user is authorized to
     <li>{@link #SELECTION_OBJECT_RELATION_OWNED SELECTION_OBJECT_RELATION_OWNED} - select a list of objects the user owns
     <li>{@link #SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED} - select a list of objects the user is authorized to and a list of objects the user owns
   </ul>
  **/
  public void setSelectionObjectRelation(int selectionObjectRelation)
  {
    // The public const values are passed on the interface, but the private constant values
    // are stored in this object.
    switch (selectionObjectRelation)
    {
      case SELECTION_OBJECT_RELATION_AUTHORIZED:
        selectionObjectRelation_ = SELECTION_OBJECT_RELATION_AUTHORIZED_PRIV;
        break;
      case SELECTION_OBJECT_RELATION_OWNED:
        selectionObjectRelation_ = SELECTION_OBJECT_RELATION_OWNED_PRIV;
        break;
      case SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED:
        selectionObjectRelation_ = SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED_PRIV;
        break;
      default:
      {
        Trace.log(Trace.ERROR, "UserObjectsOwnedList.setSelectionObjectRelation() PARAMETER_VALUE_NOT_VALID");
        throw new ExtendedIllegalArgumentException("selectionObjectRelation", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
    }
  }

  
  /**
   Returns the returned objects setting indicating that objects to be returned are objects that the user is authorized to, objects owned, or both.
   @return the selection object relation.  Possible values:
   <ul>
     <li>{@link #SELECTION_OBJECT_RELATION_AUTHORIZED SELECTION_OBJECT_RELATION_AUTHORIZED} - select a list of objects the user is authorized to
     <li>{@link #SELECTION_OBJECT_RELATION_OWNED SELECTION_OBJECT_RELATION_OWNED} - select a list of objects the user owns
     <li>{@link #SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED} - select a list of objects the user is authorized to and a list of objects the user owns
   </ul>
  **/
  public int getSelectionObjectRelation()
  {
    // The public const values are passed on the interface, but the private constant values
    // are stored in this object.
    if (selectionObjectRelation_.equals(SELECTION_OBJECT_RELATION_AUTHORIZED_PRIV))
      return (SELECTION_OBJECT_RELATION_AUTHORIZED);
    else if (selectionObjectRelation_.equals(SELECTION_OBJECT_RELATION_OWNED_PRIV))
      return (SELECTION_OBJECT_RELATION_OWNED);
    else
      return (SELECTION_OBJECT_RELATION_OWNED_OR_AUTHORIZED);
  }

  
  /**
   Returns a string representation of this object.
   @return a string with the system, userName, selectionFileSystem, and selectionObjectRelation
   **/
  public String toString()
  {
    String returnString;
    returnString = "UserObjectsOwnedList (getSystem()="+getSystem()+" getUserName()="+getUserName() + " getSelectionFileSystem()="+getSelectionFileSystem()+" getSelectionObjectRelation()="+getSelectionObjectRelation()+")";
    return returnString;
  }
}
