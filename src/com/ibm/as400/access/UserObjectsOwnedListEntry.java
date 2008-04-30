///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserObjectsOwnedListEntry.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2008 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 This entry represents a single i5/OS object that a user owns or is authorized to. 
 <p>Each entry corresponds to an entry from the QSYLOBJA API (format OBJA0300 or OBJA0310).<br>  
 Instances of this class are created by the {@link UserObjectsOwnedList#getObjectList() UserObjectsOwnedList.getObjectList()} method.
**/

public class UserObjectsOwnedListEntry
{
  
  /**
  Special authority value indicating that the user has all object (operational, management, existence, alter and reference) and 
     data (read, add, update, delete, and execute) authorities to the object.
   **/
  public static final String AUTHORITY_VALUE_ALL = "*ALL";
  
  /**
  Special authority value indicating that the user has object operational and all data authorities to the object.
   **/
  public  static final String AUTHORITY_VALUE_CHANGE   = "*CHANGE";
  
  /**
  Special authority value indicating that the user has object operational and data read and execute authorities to the object.
   **/
  public  static final String AUTHORITY_VALUE_USE      = "*USE";
  
  /**
  Special authority value indicating that the user has none of the object or data authorities to the object, or authorization list management authority.
   **/
  public  static final String AUTHORITY_VALUE_EXCLUDE  = "*EXCLUDE";

  /**
  Special authority value indicating that the user has some combination of object and data authorities that do not relate to a special value.
     The individual authorities for the user should be checked to determine what authority the user has to the object. 
     This value is returned if the user owns an object and all authority for the user to the object has been removed. 
     If this happens, all individual authority fields are set to false.
   **/
  public  static final String AUTHORITY_VALUE_USER_DEF = "USER DEF";

  
  private String  objectName_;
  private String  libraryName_;
  private String  objectType_;
  private String  authorityHolder_;
  private String  ownership_;
  private String  authorityValue_;
  private String  authorityListManagement_;
  private String  objectOperational_;
  private String  objectManagement_;
  private String  objectExistence_;
  private String  dataRead_;
  private String  dataAdd_;
  private String  dataUpdate_;
  private String  dataDelete_;
  private String  attribute_;
  private String  textDescription_;
  private String  dataExecute_;
  private String  objectAlter_;
  private String  objectReference_;
  private String  aspDeviceNameOfLibrary_;
  private String  aspDeviceNameOfObject_;
  private String  pathName_;
  private boolean qsysObjectEntry_;
  
 
  // Make constructors package scoped - only constructed by UserObjectsOwnedList.getObjectList()
  // No verification of data is performed in the constructors, since  
  // the data for these fields comes from the QSYLOBJA API.
  // This constructor is for QSYS library (non-directory) based objects (i.e. QSYLOBJA Format OBJA0300)
  UserObjectsOwnedListEntry(String objectName,String libraryName,String objectType,String authorityHolder,String ownership,String authorityValue,String authorityListManagement,String objectOperational,String objectManagement,
      String objectExistence,String dataRead,String dataAdd,String dataUpdate,String dataDelete,String attribute,String textDescription,
      String dataExecute,String objectAlter,String objectReference,String aspDeviceNameOfLibrary,String aspDeviceNameOfObject)
   {
     construct(objectType, authorityHolder, ownership, authorityValue, authorityListManagement, objectOperational, objectManagement,
          objectExistence, dataRead, dataAdd, dataUpdate, dataDelete, attribute, textDescription,
          dataExecute, objectAlter, objectReference, aspDeviceNameOfObject);
     // No verification of data is performed.  The data for these fields comes from the QSYLOBJA API.
     objectName_               = objectName;
     libraryName_              = libraryName;
     aspDeviceNameOfLibrary_   = aspDeviceNameOfLibrary;
     qsysObjectEntry_ = true;
   }
  
  
  // This constructor is for IFS directory (non-QSYS) based objects (i.e. QSYLOBJA Format OBJA0310)
  UserObjectsOwnedListEntry(String objectType,String authorityHolder,String ownership,String authorityValue,String authorityListManagement,String objectOperational,String objectManagement,
      String objectExistence,String dataRead,String dataAdd,String dataUpdate,String dataDelete,String attribute,String textDescription,
      String dataExecute,String objectAlter,String objectReference,String aspDeviceNameOfObject,String pathName)
   {
     construct(objectType, authorityHolder, ownership, authorityValue, authorityListManagement, objectOperational, objectManagement,
          objectExistence, dataRead, dataAdd, dataUpdate, dataDelete, attribute, textDescription,
          dataExecute, objectAlter, objectReference, aspDeviceNameOfObject);
     pathName_                 = pathName;
     qsysObjectEntry_ = false;
   }
  
  
  // Common code for all constructors
  private void construct(String objectType,String authorityHolder,String ownership,String authorityValue,String authorityListManagement,String objectOperational,String objectManagement,
      String objectExistence,String dataRead,String dataAdd,String dataUpdate,String dataDelete,String attribute,String textDescription,
      String dataExecute,String objectAlter,String objectReference,String aspDeviceNameOfObject)
  {
    // No verification of data is performed.  The data for these fields comes from the QSYLOBJA API.
    objectName_               = null; // Not passed - so default to NULL
    libraryName_              = null; // Not passed - so default to NULL
    objectType_               = objectType;
    authorityHolder_          = authorityHolder;
    ownership_                = ownership;
    authorityValue_           = authorityValue;
    authorityListManagement_  = authorityListManagement;
    objectOperational_        = objectOperational;
    objectManagement_         = objectManagement;
    objectExistence_          = objectExistence;
    dataRead_                 = dataRead;
    dataAdd_                  = dataAdd;
    dataUpdate_               = dataUpdate;
    dataDelete_               = dataDelete;
    attribute_                = attribute;
    textDescription_          = textDescription;
    dataExecute_              = dataExecute;
    objectAlter_              = objectAlter;
    objectReference_          = objectReference;
    aspDeviceNameOfLibrary_   = null; // Not passed - so default to NULL
    aspDeviceNameOfObject_    = aspDeviceNameOfObject;
    pathName_                 = null; // Not passed - so default to NULL
  }


  /**
   Returns the name of the object the user is authorized to, owns, or is the primary group for.
   @return The object name. Will return null when {@link #isQSYSObjectEntry() isQSYSObjectEntry()} is false.

   **/
  public String getObjectName()
  {
    return objectName_;
  }

  /**
   Returns the name of the library containing the object.
   @return The object library. Will return null when {@link #isQSYSObjectEntry() isQSYSObjectEntry()} is false.
   **/
  public String getLibraryName()
  {
    return libraryName_;
  }

  /**
   Returns the type of object the user is authorized to, owns, or is the primary group of.
   @return The object type.
   **/
  public String getObjectType()
  {
    return objectType_;
  }


  /**
  Returns whether the object is an authority holder. 
  @return true if the object is an authority holder;
  false otherwise
  **/
  public boolean getAuthorityHolder()
  {
    // If the object is an authority holder, this field is Y. If not, this field is N.
    return (authorityHolder_.equals("Y"));
  }

  /**
  Returns whether the user owns the object or is the primary group for the object. 
  If the user owns the object, this field is Y. If the user is the primary group 
  for the object, this field is G. Otherwise, this field is N.
  @return ownership
  **/
  public String getOwnership()
  {
    return ownership_;
  }

  /**
  Returns the special value indicating the user's authority to the object.
  @return authority value Possible values:
   <ul>
     <li>{@link #AUTHORITY_VALUE_ALL UserObjectsOwnedListEntry.AUTHORITY_VALUE_ALL} - The user has all object (operational, management, existence, alter and reference) and 
     data (read, add, update, delete, and execute) authorities to the object.
     <li>{@link #AUTHORITY_VALUE_CHANGE UserObjectsOwnedListEntry.AUTHORITY_VALUE_CHANGE} - The user has object operational and all data authorities to the object.
     <li>{@link #AUTHORITY_VALUE_USE UserObjectsOwnedListEntry.AUTHORITY_VALUE_USE} - The user has object operational and data read and execute authorities to the object.
     <li>{@link #AUTHORITY_VALUE_EXCLUDE UserObjectsOwnedListEntry.AUTHORITY_VALUE_EXCLUDE} - The user has none of the object or data authorities to the object, or authorization list management authority.
     <li>{@link #AUTHORITY_VALUE_USER_DEF UserObjectsOwnedListEntry.AUTHORITY_VALUE_USER_DEF} - The user has some combination of object and data authorities that do not relate to a special value. 
     The individual authorities for the user should be checked to determine what authority the user has to the object. 
     This value is returned if the user owns an object and all authority for the user to the object has been removed. 
     If this happens, all individual authority fields are set to false.
   </ul>
  **/
  public String getAuthorityValue()
  {
    return authorityValue_;
  }

  /**
  Returns whether the user has authorization list management authority to the 
  object.  This field is only valid if the object type is *AUTL.
  @return true if user has authorization list management authority to the object;
  false otherwise
  **/
  public boolean getAuthorityListManagement()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (authorityListManagement_.equals("Y"));
  }

  /**
  Returns whether the user has object operational authority to the object. 
  @return true if user has object operational authority to the object;
  false otherwise
  **/
  public boolean getObjectOperational()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (objectOperational_.equals("Y"));
  }

  /**
  Returns whether the user has object management authority to the object. 
  @return true if user has object management authority to the object;
  false otherwise
  **/
  public boolean getObjectManagement()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (objectManagement_.equals("Y"));
  }

  /**
  Returns whether the user has object existence authority to the object. 
  @return true if user has object existence authority to the object;
  false otherwise
  **/
  public boolean getObjectExistence()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (objectExistence_.equals("Y"));
  }

  /**
  Returns whether the user has data read authority to the object. 
  @return true if user has data read authority to the object;
  false otherwise
  **/
  public boolean getDataRead()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (dataRead_.equals("Y"));
  }

  /**
  Returns whether the user has data add authority to the object. 
  @return true if user has data add authority to the object;
  false otherwise
  **/
  public boolean getDataAdd()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (dataAdd_.equals("Y"));
  }

  /**
  Returns whether the user has data update authority to the object. 
  @return true if user has data update authority to the object;
  false otherwise
  **/
  public boolean getDataUpdate()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (dataUpdate_.equals("Y"));
  }

  /**
  Returns whether the user has data delete authority to the object. 
  @return true if user has data delete authority to the object;
  false otherwise
  **/
  public boolean getDataDelete()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (dataDelete_.equals("Y"));
  }

  /**
  Returns the object's attribute.
  @return attribute
  **/
  public String getAttribute()
  {
    return attribute_;
  }

  /**
  Returns the text description of the object.
  @return text description
  **/
  public String getTextDescription()
  {
    return textDescription_;
  }

  /**
  Returns whether the user has data execute authority to the object. 
  @return true if user has data execute authority to the object;
  false otherwise
  **/
  public boolean getDataExecute()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (dataExecute_.equals("Y"));
  }

  /**
  Returns whether the user has object alter authority to the object. 
  @return true if user has object alter authority to the object;
  false otherwise
  **/
  public boolean getObjectAlter()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (objectAlter_.equals("Y"));
  }

  /**
  Returns whether the user has object reference authority to the object. 
  @return true if user has object reference authority to the object;
  false otherwise
  **/
  public boolean getObjectReference()
  {
    // If the user has the authority, this field is Y. If not, this field is N.
    return (objectReference_.equals("Y"));
  }

  /**
  Returns the auxiliary storage pool (ASP) device name where the object's 
  library is stored. If the object's library is in the system ASP or one 
  of the basic user ASPs, this field contains *SYSBAS.
  @return asp device name of library. Will return null when {@link #isQSYSObjectEntry() isQSYSObjectEntry()} is false.
  **/
  public String getAspDeviceNameOfLibrary()
  {
    return aspDeviceNameOfLibrary_;
  }

  /**
  Returns the auxiliary storage pool (ASP) device name where the object 
  is stored. If the object is in the system ASP or one of the basic 
  user ASPs, this field contains *SYSBAS.
  @return asp device name of object 
  **/
  public String getAspDeviceNameOfObject()
  {
    return aspDeviceNameOfObject_;
  }

  /**
  Returns the path name of the object the user owns, is authorized to, or is the primary group for.
  @return path name of the object.  Will return null when {@link #isQSYSObjectEntry() isQSYSObjectEntry()} is true.
  **/
  public String getPathName()
  {
    return pathName_;
  }
  
  /**
  Indicates if this UserObjectsOwnedListEntry is for a QSYS library based object vs an IFS directory based object.
  A QSYS library based objects resides in a library in the QSYS file system.  Other i5/OS objects would
  reside in an IFS directory based file system.
  <p>Some fields are not valid for QSYS vs IFS directory based objects.  Refer to:
  <ul>
    <li>{@link #getObjectName() getObjectName()}
    <li>{@link #getLibraryName() getLibraryName()}
    <li>{@link #getAspDeviceNameOfLibrary()() getAspDeviceNameOfLibrary()()}
    <li>{@link #getPathName() getPathName()}
  </ul> 
  @return true if this entry is for a QSYS based object; false otherwise
  **/
  public boolean isQSYSObjectEntry()
  {
    return qsysObjectEntry_;
  }
  
  /**
   Returns a string representation of this object.
   @return a string representing the object by path name or by library name, object name, and object type
   **/
  public String toString()
  {
	  String returnString;
    if (isQSYSObjectEntry())
    {
      returnString = "UserObjectsOwnedListEntry (getLibraryName()="+getLibraryName()+" getObjectName()="+getObjectName() + " getObjectType()="+getObjectType() + ")";
    }
    else
    {
      returnString = "UserObjectsOwnedListEntry (getPathName()='" + getPathName()+"')";
    }
    return returnString;
  }


}
