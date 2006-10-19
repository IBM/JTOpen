///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: ISeriesNetServerFileShare.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2000 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 The ISeriesNetServerFileShare class represents a NetServer file share.
 **/
public class ISeriesNetServerFileShare extends ISeriesNetServerShare
{
  static final long serialVersionUID = 1L;

  /**
   Value of the "maximum number of users" attribute, indicating "no maximum".
   **/
  public final static int NO_MAX = -1;


  /**
   Value of the "current number of users" attribute, indicating that the system was unable to retrieve the actual value.
   **/
  public final static int UNKNOWN = -1;

  /**
   Value of the "permission" attribute, indicating read-only permission.
   **/
  public final static int READ_ONLY = 1;

  /**
   Value of the "permission" attribute, indicating read-write permission.
   **/
  public final static int READ_WRITE = 2;

  /**
   Value of the "text conversion enablement" attribute, indicating "text conversion is enabled".
   **/
  public final static int ENABLED = 0;

  /**
   Value of the "text conversion enablement" attribute, indicating "text conversion not enabled".
   **/
  public final static int NOT_ENABLED = 1;

  /**
   Value of the "text conversion enablement" attribute, indicating "text conversion is enabled, and mixed data is allowed".
   **/
  public final static int ENABLED_AND_MIXED = 2;

  // Note: For efficiency, these attributes are not private, so they are directly accessible by the ISeriesNetServer class when composing and refreshing share objects.
  String path_;
  int permissions_;
  int maxNumberOfUsers_;
  int curNumberOfUsers_;  // Note: This attribute has a getter but no setter.

  // The following attributes use optional parameters of the "Change File Server Share" API.
  int ccsidForTextConversion_;
  String textConversionEnablement_;  // "0", "1", or "2"
  String[] fileExtensions_;



  ISeriesNetServerFileShare(String shareName, int permissions, int maxUsers, int currentUsers, String description, String path, int ccsid, String enableConversion, String[] fileExtensions)
  {
    setAttributeValues(shareName, permissions, maxUsers, currentUsers, description, path, ccsid, enableConversion, fileExtensions);
  }


  // This method does no argument validity checking, nor does it update attributes on server.
  // For use by ISeriesNetServer class when composing lists of shares.
  void setAttributeValues(String shareName, int permissions, int maxUsers, int currentUsers, String description, String path, int ccsid, String enableConversion, String[] fileExtensions)
  {
    super.setAttributeValues(shareName, description, true);
    path_ = path;
    permissions_ = permissions;
    maxNumberOfUsers_ = maxUsers;
    curNumberOfUsers_ = currentUsers;
    ccsidForTextConversion_ = ccsid;
    textConversionEnablement_ = enableConversion;
    fileExtensions_ = fileExtensions;
  }


  /**
   Gets the path in the integrated file system to be shared with the network.
   @return The path.
   **/
  public String getPath()
  {
    return path_;
  }


  /**
   Sets the path in the integrated file system to be shared with the network.  A forward slash, '/', is required as the first character.
   @param path  The path.
   **/
  public void setPath(String path)
  {
    if (path == null) throw new NullPointerException("path");
    path_ = path.trim();
  }

  /**
   Gets the access available from the network for this share.
   Possible values are {@link #READ_ONLY READ_ONLY} and {@link #READ_WRITE READ_WRITE}.
   @return The permission for the share.
   **/
  public int getPermissions()
  {
    return permissions_;
  }

  /**
   Sets the access available from the network for this share.
   Valid values are {@link #READ_ONLY READ_ONLY} and {@link #READ_WRITE READ_WRITE}.
   **/
  public void setPermissions(int permissions)
  {
    if (permissions != READ_ONLY && permissions != READ_WRITE) {
      throw new ExtendedIllegalArgumentException(Integer.toString(permissions), ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    permissions_ = permissions;
  }

  /**
   Gets the maximum number of concurrent connections that the share can accommodate.
   A value of {@link #NO_MAX NO_MAX} indicates that there is no limit.
   A value of 0 indicates that the share is unavailable for use.
   @return The maximum number of users.
   **/
  public int getMaximumNumberOfUsers()
  {
    return maxNumberOfUsers_;
  }

  /**
   Sets the maximum number of concurrent connections that the share can accommodate.
   A value of {@link #NO_MAX NO_MAX} indicates that there is no limit.
   A value of 0 indicates that the share is unavailable for use.
   @param maximumUsers  The maximum number of users.
   **/
  public void setMaximumNumberOfUsers(int maximumUsers)
  {
    if (maximumUsers < NO_MAX) {
      throw new ExtendedIllegalArgumentException(Integer.toString(maximumUsers), ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    maxNumberOfUsers_ = maximumUsers;
  }


  /**
   Gets the number of connections that are currently made to the share.
   A value of {@link #UNKNOWN UNKNOWN} means that the system was unable to retrieve the value.
   @return The current number of users.
   **/
  public int getCurrentNumberOfUsers()
  {
    return curNumberOfUsers_;
  }
  // Note: There is no setter for the "current number of users" attribute.



  /**
   Gets the value of the "enable text conversion" attribute.
   Possible values are {@link #ENABLED ENABLED}, {@link #NOT_ENABLED NOT_ENABLED}, and {@link #ENABLED_AND_MIXED ENABLED_AND_MIXED}.
   @return The text conversion enablement.
   **/
  public int getTextConversionEnablement()
  {
    switch (textConversionEnablement_.charAt(0)) {
      case '0' : return NOT_ENABLED;       // text conversion is not enabled
      case '1' : return ENABLED;           // text conversion is enabled
      default  : return ENABLED_AND_MIXED; // text conversion is enabled, and mixed data is allowed
    }
  }


  /**
   Sets the value of the "enable text conversion" attribute.
   Valid values are {@link #ENABLED ENABLED}, {@link #NOT_ENABLED NOT_ENABLED}, and {@link #ENABLED_AND_MIXED ENABLED_AND_MIXED}.
   @param enablement The text conversion enablement.
   **/
  public void setTextConversionEnablement(int enablement)
  {
    if (enablement < ENABLED || enablement > ENABLED_AND_MIXED) {
      throw new ExtendedIllegalArgumentException(Integer.toString(enablement), ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    char[] charArray = new char[1];
    switch (enablement) {
      case ENABLED : charArray[0] = '0'; break;
      case NOT_ENABLED : charArray[0] = '1'; break;
      default : charArray[0] = '2';
    }
    textConversionEnablement_ = new String(charArray); 
    // We'll need to use the 2nd optional parm on the API.
    numOptionalParmsToSet_ = Math.max(numOptionalParmsToSet_, 2);
  }


  /**
   Gets the client CCSID that is used for text file data conversion. Text file data conversion is performed using this CCSID and the current CCSID of the system file.
   @return The CCSID that is used for text file data conversion.
   **/
  public int getCcsidForTextConversion()
  {
    return ccsidForTextConversion_;
  }


  /**
   Sets the client CCSID that is used for text file data conversion. Text file data conversion is performed using this CCSID and the current CCSID of the system file.
   A value of 0 indicates that the user would like to use the currently configured CCSID for the system.
   @param ccsid  The CCSID that is used for text file data conversion.
   **/
  public void setCcsidForTextConversion(int ccsid)
  {
    if (ccsid < 0) {
      throw new ExtendedIllegalArgumentException(Integer.toString(ccsid), ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    ccsidForTextConversion_ = ccsid;
    // We'll need to use the 1st optional parm on the API.
    numOptionalParmsToSet_ = Math.max(numOptionalParmsToSet_, 1);
  }


  /**
   Gets the list of file extensions for the share.  The file extensions list determines which files are converted by the system.
   @return The file extensions list.
   **/
  public String[] getFileExtensions()
  {
    String[] copyOfList = new String[fileExtensions_.length];
    System.arraycopy(fileExtensions_, 0, copyOfList, 0, fileExtensions_.length);
    return copyOfList;
  }


  /**
   Sets the list of file extensions for the share.  The file extensions list determines which files are converted by the system.
   @param extensions The file extensions list.
   **/
  public void setFileExtensions(String[] extensions)
  {
    if (extensions == null) throw new NullPointerException("extensions");

    fileExtensions_ = new String[extensions.length];
    for (int i=0; i<extensions.length; i++) {
      fileExtensions_[i] = extensions[i].trim();
    }

    // We'll need to use the 3rd and 4th optional parms on the API.
    numOptionalParmsToSet_ = Math.max(numOptionalParmsToSet_, 4);
  }

}


