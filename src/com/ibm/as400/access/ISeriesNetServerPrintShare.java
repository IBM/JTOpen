///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: ISeriesNetServerPrintShare.java
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
 The ISeriesNetServerPrintShare class represents a NetServer print share.
 **/
public class ISeriesNetServerPrintShare extends ISeriesNetServerShare
{
  static final long serialVersionUID = 1L;

  /**
   Value of the "spooled file type" attribute, indicating "Advanced Function Printing".
   **/
  public final static int AFP = 2;
  /**
   Value of the "spooled file type" attribute, indicating "Automatic type sensing".
   **/
  public final static int AUTO_DETECT = 4;
  /**
   Value of the "spooled file type" attribute, indicating "SNA character string".
   **/
  public final static int SCS = 3;
  /**
   Value of the "spooled file type" attribute, indicating "User ASCII".
   **/
  public final static int USER_ASCII = 1;

  // Note: For efficiency, these attributes are not private, so they are directly accessible by the ISeriesNetServer class when composing and refreshing share objects.

  String outputQueue_;  // qualified output queue name; always 20 chars long.
  String printDriver_;
  int spooledFileType_;

  // The following are optional parameters for the "Change Print Server Share" API.
  String printerFile_;  // qualified printer file name; always 20 chars long.
  boolean isPublished_;
  
  //@AE3A Start
  String encryptionRequired_; //"0", "1", 
  /**
  Value of the "Encryption Required" attribute, indicating "Encryption Required is not required".
  **/
 public final static int NOT_REQUIRED = 0;

 /**
  Value of the "Encryption Required" attribute, indicating "Encryption Required is required".
  **/
 public final static int REQUIRED = 1;
 String authorizationList_;
 //@AE3A End

  /**supports Authorization List on 7.5+
   * 
   * @param shareName
   * @param spooledFileType
   * @param outQueue
   * @param printDriver
   * @param description
   * @param printerFile
   * @param isPublished
   * @param encryptionRequired
   */
  ISeriesNetServerPrintShare(String shareName, int spooledFileType,
          String outQueue, String printDriver, String description,
          String printerFile, boolean isPublished, String encryptionRequired, String authorizationList)
  {
       setAttributeValues(shareName, spooledFileType, outQueue, printDriver, description,
          printerFile, isPublished, encryptionRequired, authorizationList);
  }

  // This method does no argument validity checking, nor does it update attributes on server.
  // For use by ISeriesNetServer class when composing lists of shares.
  //@AE3A Start
  void setAttributeValues(String shareName,
       int spooledFileType,
       String outQueue, String printDriver, String description,
       String printerFile, boolean isPublished, String encryptionRequired, String authorizationList)
  {
       super.setAttributeValues(shareName, description, false);
       spooledFileType_ = spooledFileType;
       outputQueue_ = outQueue;
       printDriver_ = printDriver;
       printerFile_ = printerFile;
       isPublished_ = isPublished;
       encryptionRequired_ = encryptionRequired;
       authorizationList_ = authorizationList;
   }
   //@AE3A End

  /**
   Returns the name of the output queue associated with the share.
   @return    The name of the output queue.
   **/
  public String getOutputQueueName()
  {
    // Extract queue name from first 10 chars of qualified queue name.
    return outputQueue_.substring(0,10).trim();
  }


  /**
   Returns the name of the library containing the output queue associated with the share.
   @return  The library containing the output queue.
   **/
  public String getOutputQueueLibrary()
  {
    // Extract queue library name from second 10 chars of qualified queue name.
    return outputQueue_.substring(10,20).trim();
  }

  /**
   Sets the name of the output queue associated with the share.
   @param name  The name of the output queue.
   **/
  public void setOutputQueueName(String name)
  {
    if (name == null) throw new NullPointerException("name");
    name = name.trim();
    if (name.length() > 10) {
      throw new ExtendedIllegalArgumentException(name, ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    // Set the queue name in the first 10 chars of qualified queue name.
    StringBuffer buff = new StringBuffer(outputQueue_);
    buff.replace(0,10,"          ");
    buff.replace(0,name.length(),name);

    outputQueue_ = buff.toString();
  }

  /**
   Sets the name of the library containing the output queue associated with the share.
   @param name  The library containing the output queue.
   **/
  public void setOutputQueueLibrary(String name)
  {
    if (name == null) throw new NullPointerException("name");
    name = name.trim();
    if (name.length() > 10) {
      throw new ExtendedIllegalArgumentException(name, ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    // Set the queue library in the second 10 chars of qualified queue name.
    StringBuffer buff = new StringBuffer(outputQueue_);
    buff.replace(10,20,"          ");
    buff.replace(10,10+name.length(),name);

    outputQueue_ = buff.toString();
  }


  /**
   Returns the text string that identifies the print driver appropriate for this share.
   When personal computers connect to this shared printer, this identifies the print driver they should use.
   @return  The print driver.
   **/
  public String getPrintDriver()
  {
    return printDriver_;
  }


  /**
   Sets the text string that identifies the print driver appropriate for this share.
   When personal computers connect to this shared printer, this identifies the print driver they should use.  This text should match the name of a print driver known to the personal computer operating system.
   @param printDriver  The print driver.
   **/
  public void setPrintDriver(String printDriver)
  {
    if (printDriver == null) throw new NullPointerException("printDriver");

    printDriver_ = printDriver;
  }


  /**
   Returns the name of the printer file associated with the share.
   @return    The name of the printer file.
   **/
  public String getPrinterFileName()
  {
    // Extract printer file name from first 10 chars of qualified printer file name.
    return printerFile_.substring(0,10).trim();
  }


  /**
   Returns the name of the library containing the printer file associated with the share.
   @return    The library containing the printer file.
   **/
  public String getPrinterFileLibrary()
  {
    // Extract printer file library from second 10 chars of qualified printer file name.
    return printerFile_.substring(10,20).trim();
  }

  /**
   Sets the name of the printer file associated with the share.
   @param name  The name of the printer file.
   **/
  public void setPrinterFileName(String name)
  {
    if (name == null) throw new NullPointerException("name");
    name = name.trim();
    if (name.length() > 10) {
      throw new ExtendedIllegalArgumentException(name, ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    // Set the printer file name in the first 10 chars of qualified printer file name.
    StringBuffer buff = new StringBuffer(printerFile_);
    buff.replace(0,10,"          ");
    buff.replace(0,name.length(),name);

    printerFile_ = buff.toString();
    // We'll need to use the 1st optional parm on the API.
    numOptionalParmsToSet_ = Math.max(numOptionalParmsToSet_, 1);
  }

  /**
   Sets the name of the library containing the printer file associated with the share.
   @param name  The library containing the printer file.
   **/
  public void setPrinterFileLibrary(String name)
  {
    if (name == null) throw new NullPointerException("name");
    name = name.trim();
    if (name.length() > 10) {
      throw new ExtendedIllegalArgumentException(name, ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    // Set the printer file library in the second 10 chars of qualified printer file name.
    StringBuffer buff = new StringBuffer(printerFile_);
    buff.replace(10,20,"          ");
    buff.replace(10,10+name.length(),name);

    printerFile_ = buff.toString();
    // We'll need to use the 1st optional parm on the API.
    numOptionalParmsToSet_ = Math.max(numOptionalParmsToSet_, 1);
  }


  /**
   Returns the value of the "publish print share" attribute.
   This attribute indicates whether the print share is to be published.
   <tt>true</tt> indicates that the print share is published.
   @return  The "publish print share" attribute.
   **/
  public boolean isPublished()
  {
    return isPublished_;
  }


  /**
   Sets the value of the "publish print share" attribute.
   This attribute indicates whether the print share is to be published.
   <tt>true</tt> indicates that the print share is published.
   @param isPublished  The "publish print share" attribute.
   **/
  public void setPublished(boolean isPublished)
  {
    isPublished_ = isPublished;
    // We'll need to use the 2nd optional parm on the API.
    numOptionalParmsToSet_ = Math.max(numOptionalParmsToSet_, 2);
  }


  /**
   Returns the spooled file type for the share.
   This specifies the type of spooled files that are created by using this share.
   Possible values are {@link #AFP AFP}, {@link #AUTO_DETECT AUTO_DETECT}, {@link #SCS SCS}, and {@link #USER_ASCII USER_ASCII}.

   @return    The spooled file type.
   **/
  public int getSpooledFileType()
  {
    return spooledFileType_;
  }

  /**
   Sets the spooled file type for the share.
   This specifies the type of spooled files that are created by using this share.
   Valid values are {@link #AFP AFP}, {@link #AUTO_DETECT AUTO_DETECT}, {@link #SCS SCS}, and {@link #USER_ASCII USER_ASCII}.

   @param spooledFileType  The spooled file type.
   **/
  public void setSpooledFileType(int spooledFileType)
  {
    if (spooledFileType < USER_ASCII || spooledFileType > AUTO_DETECT) {
      throw new ExtendedIllegalArgumentException(Integer.toString(spooledFileType), ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    spooledFileType_ = spooledFileType;
  }
  
  //@AE3A Start
  /**
  Gets the value of the "Encryption Required" attribute, only available on 7.4 and above
  Possible values are {@link #ENABLED ENABLED}, {@link #NOT_ENABLED NOT_ENABLED}.
  @return The Encryption Required.
  **/
 public int getEncryptionRequired()
 {
	 if (encryptionRequired_ == null ) throw new NullPointerException("Encryption required");
     switch (encryptionRequired_.charAt(0)) {
         case '0' : return NOT_REQUIRED;       // text conversion is not enabled
         case '1' : return REQUIRED;           // text conversion is enabled
         default  : return NOT_REQUIRED; // text conversion is enabled, and mixed data is allowed
     }
 }
 
 /**
 Sets the value of the "Encryption Required" attribute. only available on 7.4 and above
 Valid values are {@link #ENABLED ENABLED}, {@link #NOT_ENABLED NOT_ENABLED}.
 @param Encryption Required, required
 **/
public void setEncryptionRequired(int required)
{
  if (required < NOT_REQUIRED || required > REQUIRED) {
    throw new ExtendedIllegalArgumentException(Integer.toString(required), ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
  }
  char[] charArray = new char[1];
  switch (required) {
    case NOT_REQUIRED : charArray[0] = '0'; break;
    case REQUIRED : charArray[0] = '1'; break;
    default : charArray[0] = '0';
  }
  encryptionRequired_ = new String(charArray); 
  // We'll need to use the 2nd optional parm on the API.
  numOptionalParmsToSet_ = Math.max(numOptionalParmsToSet_, 3);
}

/**
Gets the value of the "Authorization List" attribute.
@return The Authorization List.
**/
public String getAuthorizationList()
{
	if (authorizationList_ == null ) throw new NullPointerException("Authorization list");
	 return authorizationList_;
}

/**
Sets the value of the "Authorization List" attribute.
@param Authorization List
**/
public void setAuthorizationList(String authorizationList)
{
	if (authorizationList.length() != 0) {
		authorizationList_ = authorizationList;
	}
	// We'll need to use the 2nd optional parm on the API.
	if (authorizationList_ != null) numOptionalParmsToSet_ = Math.max(numOptionalParmsToSet_, 4);
}
//@AE3A End

}


